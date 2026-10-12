package com.persagy.apccontrol.basic.structure.demand.listener;

import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.message.dtx.DtxPublishMessageEvent;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceConfigChangeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateChangeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandInstanceConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandTemplateChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigResultRefreshRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigResultRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultRepositoryQueryParam;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 需求配置结果刷新事件监听器。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigResultRefreshEventListener {

    private final @NonNull DemandConfigResultRefreshRepository demandConfigResultRefreshRepository;
    private final @NonNull DemandConfigResultRepository demandConfigResultRepository;

    /**
     * DTX 发布后刷新所有模板类型结果。
     *
     * @param event DTX 发布事件
     * @since v7.260701.0
     */
    @Async
    @EventListener
    public void onDtxPublishEvent(@NonNull DtxPublishMessageEvent event) {
        CacheLockKey.Key key = CacheKey.Lock.DtxPublish.demandConfigResult(event.getProjectId());
        event.executeWithLock(() -> "需求配置结果-DTX发布刷新", key, () -> {
            // 支持范围由刷新仓储统一管理，新增接入类型时不需要再修改事件监听器。
            for (DemandTemplateTypeEnum templateType :
                    this.demandConfigResultRefreshRepository.listSupportedTemplateTypes()) {
                this.demandConfigResultRefreshRepository.refreshByTemplateType(templateType);
            }
        });
    }

    /**
     * 配置变化后刷新受影响实例。
     *
     * <p>监听器独立于刷新仓储实现类，通过 {@link DemandConfigResultRefreshRepository} 接口调用刷新方法，
     * 确保无事务发布事件的 fallback 场景也能走到 Spring 事务代理。</p>
     *
     * @param event 配置变化事件
     * @since v7.260701.0
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onDemandConfigChangeEvent(@NonNull DemandConfigChangeEvent event) {
        DemandConfigChangeEvent.Message message = (DemandConfigChangeEvent.Message) event.getSource();
        List<DemandConfigChangeEvent.Detail> details = message.getDetails();
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        if (!this.demandConfigResultRefreshRepository.supportsTemplate(message.getTemplateId())) {
            return;
        }
        Set<DemandInstanceValueObject> instances = details.stream()
                .flatMap(detail -> detail.getConfigs().stream())
                .map(config -> Objects.nonNull(config.getNewConfig()) ? config.getNewConfig() : config.getOldConfig())
                .filter(Objects::nonNull)
                .map(config -> new DemandInstanceValueObject(config.getDemandInstanceType(), config.getDemandInstanceId()))
                .collect(Collectors.toSet());
        // 普通配置删除不能直接删除结果：删除自身配置后可能露出上级继承值；
        // refreshInstances 会按实时结果决定 upsert 或删除失效预计算结果。这里先按实例类型合并，
        // 避免同一次配置变更为每个实例重复查询下级空间、实时计算和写结果表。
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds = instances.stream()
                .collect(Collectors.groupingBy(DemandInstanceValueObject::getInstanceType,
                        Collectors.mapping(DemandInstanceValueObject::getInstanceId, Collectors.toSet())));
        for (Map.Entry<DemandInstanceTypeEnum, Set<String>> entry : instanceTypeAndInstanceIds.entrySet()) {
            this.demandConfigResultRefreshRepository.refreshByInstanceAndChildren(
                    message.getTemplateId(), message.getSceneId(), entry.getKey(), entry.getValue());
        }
    }

    /**
     * 模板变化后维护模板维度预计算结果。
     *
     * @param event 模板变化事件
     * @since v7.260701.0
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onDemandTemplateChangeEvent(@NonNull DemandTemplateChangeEvent event) {
        DemandTemplateChangeEvent.Message message = (DemandTemplateChangeEvent.Message) event.getSource();
        DemandTemplate template = Objects.nonNull(message.getNewTemplate())
                ? message.getNewTemplate() : message.getOldTemplate();
        if (Objects.isNull(template)) {
            return;
        }
        if (!this.demandConfigResultRefreshRepository.supportsTemplateType(template.getDemandTemplateType())) {
            return;
        }
        DemandTemplateChangeTypeEnum dataType = message.getDataType();
        if (dataType == DemandTemplateChangeTypeEnum.REMOVE) {
            // 删除模板后实时链路已经查不到该模板，不能再走刷新；必须按旧模板ID清理预计算结果。
            this.deleteTemplateResults(template.getId());
            return;
        }
        if (dataType == DemandTemplateChangeTypeEnum.INSERT) {
            this.demandConfigResultRefreshRepository.refreshByTemplate(template.getId());
        }
        // UPDATE 只影响模板名称/启停等元数据，APPLIED 只影响日期到模板的应用关系；
        // 结果表按 relTemplateId 保存模板自身的最新最终配置，这两类变化不改变最终配置内容。
    }

    /**
     * 黑名单和屏蔽配置变化后刷新结果。
     *
     * @param event 实例配置变化事件
     * @since v7.260701.0
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onDemandInstanceConfigChangeEvent(@NonNull DemandInstanceConfigChangeEvent event) {
        DemandInstanceConfigChangeEvent.Param param = (DemandInstanceConfigChangeEvent.Param) event.getSource();
        if (!this.demandConfigResultRefreshRepository.supportsTemplateType(param.getTemplateType())) {
            return;
        }
        if (param.getChangeType() == DemandInstanceConfigChangeTypeEnum.SHIELDED) {
            // 屏蔽状态只影响结果是否可见，不需要重新计算最终配置JSON，直接更新结果表标记即可。
            this.demandConfigResultRefreshRepository.updateShielded(param.getTemplateType(), param.getInInstanceIds(), true);
            this.demandConfigResultRefreshRepository.updateShielded(param.getTemplateType(), param.getOutInstanceIds(), false);
            return;
        }
        if (param.getChangeType() == DemandInstanceConfigChangeTypeEnum.BLACKLIST) {
            Set<DemandInstanceValueObject> inInstances = new HashSet<>();
            this.addSpaceInstances(inInstances, param.getInInstanceIds());
            // 进入黑名单的空间不应该保留预计算结果，直接删除比走实时刷新后再判空更清晰，也少一次RWD链路计算。
            this.demandConfigResultRefreshRepository.removeByTemplateTypeAndInstances(param.getTemplateType(), inInstances);

            Set<DemandInstanceValueObject> outInstances = new HashSet<>();
            this.addSpaceInstances(outInstances, param.getOutInstanceIds());
            // 移出黑名单后可能重新继承到上级配置，需要走实时刷新把有效结果重新写回。
            this.demandConfigResultRefreshRepository.refreshByDtxChangedInstances(param.getTemplateType(), outInstances);
        }
    }

    /**
     * 向实例集合添加空间实例。
     *
     * @param result      实例集合
     * @param instanceIds 空间实例ID集合
     * @since v7.260701.0
     */
    private void addSpaceInstances(
            @NonNull Set<DemandInstanceValueObject> result,
            @Nullable Set<String> instanceIds) {
        if (CollectionUtils.isEmpty(instanceIds)) {
            return;
        }
        instanceIds.forEach(instanceId -> result.add(new DemandInstanceValueObject(DemandInstanceTypeEnum.SPACE, instanceId)));
    }

    /**
     * 按模板删除预计算结果。
     *
     * @param templateId 模板ID
     * @since v7.260701.0
     */
    private void deleteTemplateResults(@NonNull Long templateId) {
        DemandConfigResultRepositoryQueryParam queryParam = new DemandConfigResultRepositoryQueryParam();
        queryParam.setRelTemplateId(templateId);
        this.demandConfigResultRepository.removeResults(queryParam);
    }
}
