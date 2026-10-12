package com.persagy.apccontrol.basic.structure.lighitng.listener;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.model.message.dtx.DtxPublishMessageEvent;
import com.persagy.apccontrol.basic.structure.demand.context.lighting.DemandLightingDtxPublishChangeContext;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 照明DTX发布变更监听器
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LightingDtxPublishChangedListener implements LogComponent, ApplicationListener<DtxPublishMessageEvent> {

    private final TransactionTemplate transactionTemplate;

    @Async
    @Override
    public void onApplicationEvent(@NonNull DtxPublishMessageEvent event) {
        execute(event, true, true);
    }

    /**
     * 执行DTX发布变更
     *
     * @param event          事件
     * @param updateDatabase 是否更新数据库
     * @param computeResult  是否计算配置结果
     * @since 7.250602.0
     */
    public List<DemandLightingDtxPublishChangeContext> execute(
            @NonNull DtxPublishMessageEvent event,
            boolean updateDatabase,
            boolean computeResult) {
        CacheLockKey.Key key = CacheKey.Lock.DtxPublish.lighting();
        List<DemandLightingDtxPublishChangeContext> result = event.executeWithLock(this, key, () ->
                this.doExecute((DtxPublishMessageEvent.Message) event.getSource(), updateDatabase, computeResult));
        return result != null ? result : Collections.emptyList();
    }

    /**
     * 执行DTX发布变更
     *
     * @param eventMessage   事件消息
     * @param updateDatabase 是否更新数据库
     * @param computeResult  是否计算配置结果
     * @return 执行结果
     * @throws BackOffInterruptedException 重试线程中断异常
     * @since 7.250602.0
     */
    private List<DemandLightingDtxPublishChangeContext> doExecute(
            DtxPublishMessageEvent.@NonNull Message eventMessage,
            boolean updateDatabase,
            boolean computeResult) throws BackOffInterruptedException {
        RetryTemplateBuilder retryTemplateBuilder = new RetryTemplateBuilder();
        // 重试间隔5秒, 每次递增2倍, 最大不超过60秒
        retryTemplateBuilder.exponentialBackoff(5000, 2, 60000);
        // 重试3次
        retryTemplateBuilder.maxAttempts(3);
        // 存在InterruptedException异常时不进行重试
        retryTemplateBuilder.notRetryOn(InterruptedException.class);
        // 遍历堆栈异常
        retryTemplateBuilder.traversingCauses();
        RetryTemplate retryTemplate = retryTemplateBuilder.build();
        List<DemandLightingDtxPublishChangeContext> contexts = new ArrayList<>();
        for (DemandTemplateTypeEnum templateType : DemandTypeEnum.LIGHTING.toDemandTemplateTypes()) {
            AtomicInteger executeCount = new AtomicInteger(1);
            retryTemplate.execute(retryContext -> transactionTemplate.execute(status -> {
                DemandLightingDtxPublishChangeContext context = new DemandLightingDtxPublishChangeContext(eventMessage, templateType);
                try {
                    // 填充照明回路相关数据
                    context.fillAboutCircuits();
                    // 填充照明回路关联楼层
                    context.fillCircuitRelFloor();
                    // 填充照明回路关联建筑
                    context.fillCircuitRelBuilding();
                    // 填充需求配置
                    context.fillDemandConfig();
                    // 填充需求编组
                    context.fillDemandInstanceGroup();
                    // 处理需求编组
                    context.handleDemandInstanceGroup();
                    // 处理需求配置
                    context.handleDemandConfig();
                    // 更新数据库
                    if (updateDatabase) {
                        context.updateDatabase();
                    }
                    // 重算配置结果
                    if (computeResult) {
                        context.computeResult();
                    }
                    context.checkInterrupt();
                } catch (InterruptedException e) {
                    logProjectInstance().warn("线程中断成功", Arrays.asList(
                            Pair.of("eventMessage", eventMessage),
                            Pair.of("templateType", templateType)
                    ));
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    logProjectInstance().error("处理失败", e, Arrays.asList(
                            Pair.of("eventMessage", eventMessage),
                            Pair.of("templateType", templateType)
                    ));
                    throw e;
                }
                contexts.add(context);
                globalStopWatch().addTaskGroup(templateType.getValue() + executeCount.getAndIncrement(), context.getStopWatch());
                return context;
            }));
        }
        return contexts;
    }

    @Override
    public String logTag() {
        return "照明DTX发布变更监听器";
    }
}
