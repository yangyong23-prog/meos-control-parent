package com.persagy.apccontrol.basic.sse.strategy;

import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSseCoalescer;
import com.persagy.apccontrol.basic.opt.OptSsePushDedupe;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.api.cmdb.pojo.EntityEventType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.sse.*;
import com.persagy.apccontrol.basic.utils.DailyStringCounter;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * sse推送策略模板
 *
 * @author yuecaipu
 * @date 2024/5/4 15:35
 * @since v5.240402.00
 */
@Slf4j
@Component
public abstract class AbstractSseEventStrategyTemplate implements SseEventStrategy {

    @Resource
    ObjectMapper objectMapper;

    @Resource
    SseEmitterServer sseEmitterServer;

    @Resource
    RedissonDistributedLockerService redissonDistributedLockerService;

    @Autowired
    @Qualifier("asyncSSEExecutor")
    private ThreadPoolTaskExecutor asyncSSEExecutor;

    @Lazy
    @Resource
    private OptSseCoalescer optSseCoalescer;

    @Lazy
    @Resource
    private OptSsePushDedupe optSsePushDedupe;

    /**
     * 锁等待和自动释放时间定义
     */
    private static final int LOCK_WAIT_TIME = 0;
    private static final int LOCK_LEASE_TIME = 5;
    private static final int LOCK_LEASE_TIME_LASTING = 60 * 60;

    @Override
    public void doOperation(String eventKey, SseSubscribeParam param, List<SseEmitter> sseEmitters) {
        if (CollUtil.isEmpty(sseEmitters)) {
            return;
        }
        if (isSubscribedEventType(param)) {
            return;
        }
        asyncSSEExecutor.execute(() -> publishEvent(eventKey, param, true));
    }


    /**
     * 是否订阅该事件类型
     * 未订阅，则不发布事件
     *
     * @return {@link boolean}
     * @author yuecaipu
     * @date 2024/7/1 16:45
     * @since 24Q1 T3S2-5.18.0
     */
    protected boolean isSubscribedEventType(SseSubscribeParam param) {
        return !CollUtil.isEmpty(param.getEventType()) && !param.getEventType().contains(getEventType());
    }

    /**
     * 获取异常事件类型
     *
     * @return {@link EntityEventType}
     * @author yuecaipu
     * @date 2024/7/1 16:49
     * @since 24Q1 T3S2-5.18.0
     */
    protected abstract SseEmitterEventEnum getEventType();


    @Override
    public void doOperation(String projectId, SubscribeTypeEnum subscribeType) {
        // 输入参数校验
        if (projectId == null || subscribeType == null) {
            throw new IllegalArgumentException("projectId and subscribeType must not be null");
        }

        // opt B9（switch.opt.sseCoalesce）：照明等全量重算事件按窗口合并
        String eventType = getEventType() == null ? null : getEventType().name();
        if (optSseCoalescer.applies(eventType)
                && optSseCoalescer.offer(eventType + "|" + projectId + "|" + subscribeType,
                () -> doOperationNow(projectId, subscribeType))) {
            return;
        }
        doOperationNow(projectId, subscribeType);
    }

    private void doOperationNow(String projectId, SubscribeTypeEnum subscribeType) {
        // 根据项目ID获取所有订阅者
        Map<String, SseSubscribeParam> eventKeyMap = sseEmitterServer.getEventKeys(projectId, subscribeType);
        if (CollUtil.isEmpty(eventKeyMap)) {
            return;
        }

        // 异步执行发布事件任务
        eventKeyMap.forEach((eventKey, sseSubscribeParam) -> {
            if (isSubscribedEventType(sseSubscribeParam)) {
                return;
            }
            asyncSSEExecutor.execute(() -> publishEvent(eventKey, sseSubscribeParam, false));
        });
    }

    @Override
    public void doOperation(String projectId, SubscribeTypeEnum subscribeType, Object condition) {
        // 输入参数校验
        if (projectId == null || subscribeType == null || condition == null) {
            return;
        }

        // 根据项目ID获取所有订阅者
        Map<String, SseSubscribeParam> eventKeyMap = sseEmitterServer.getEventKeys(projectId, subscribeType);
        if (CollUtil.isEmpty(eventKeyMap)) {
            return;
        }

        // 异步执行发布事件任务
        eventKeyMap.forEach((eventKey, sseSubscribeParam) -> {
            if (isSubscribedEventType(sseSubscribeParam) || preFiltered(sseSubscribeParam, condition)) {
                return;
            }
            asyncSSEExecutor.execute(() -> publishEvent(eventKey, sseSubscribeParam, condition));
        });
    }

    /**
     * 解析事件参数
     *
     * @param param         事件订阅参数
     * @param typeReference 事件参数类型
     * @param <T>           事件参数类型
     * @return 事件参数
     * @since 7.250403.0
     */
    protected <T> @Nullable T analyzeEventParam(
            @NonNull SseSubscribeParam param,
            @NonNull TypeReference<@NonNull T> typeReference) {
        Map<SseEmitterEventEnum, Object> eventParamMap = Optional.ofNullable(
                param.getEventParamMap()).orElseGet(Collections::emptyMap);
        Object eventParam = eventParamMap.get(getEventType());
        if (Objects.isNull(eventParam)) {
            return null;
        }
        return objectMapper.convertValue(eventParam, typeReference);
    }

    /**
     * publishEvent
     *
     * @param eventKey        事件key
     * @param subscriberParam 事件订阅参数
     * @param lastingFlag     使用持久过期时间
     * @author yuecaipu
     * @date 2024/5/30 23:57
     * @since 24Q1 T3S2-5.18.0
     */
    private void publishEvent(String eventKey, SseSubscribeParam subscriberParam, boolean lastingFlag) {
        String lockKey = getRedisLockKey(eventKey);
        // 尝试获取锁，并处理未获取到锁的情况
        if (!tryAcquireLock(lockKey, lastingFlag)) {
            log.info("有任务在执行，直接返回。Event Key: {}, param:{}", lockKey, subscriberParam);
            return;
        }

        try {
            // 事件耗时记录
            if (!SseEmitterEventEnum.IOT_DATA_CHANGE.equals(getEventType())) {
                DailyStringCounter.sseExecuteElapsedTimeBegin(eventKey + getEventType());
            }

            ContextUtil.setGroupCode(subscriberParam.getGroupCode());
            ContextUtil.setTenant(subscriberParam.getGroupCode());
            ContextUtil.setProjectId(subscriberParam.getProjectId());
            // 设置上下文，并执行事件发布逻辑
            SseEmitterObject object = getSseEmitterObject(subscriberParam);
            // opt B9（switch.opt.ssePushDedupe）：内容未变化且未到兜底时间不推
            if (object != null && optSsePushDedupe.shouldSkip(eventKey, String.valueOf(object.getEventType()), object.getData())) {
                return;
            }
            sseEmitterServer.publishEvent(eventKey, object);
        } catch (Exception e) {
            log.error("发布事件失败。Event Key: {}, param:{}", eventKey, subscriberParam, e);
        } finally {
            // 确保释放锁和清理上下文
            // ContextUtil.remove();
            unlock(lockKey);
        }
    }


    /**
     * 根据条件，推送对应的事件，实际限制条件，有具体的实现类判断，这里不加锁
     *
     * @param eventKey
     * @param subscriberParam
     * @param condition
     */
    private void publishEvent(String eventKey, SseSubscribeParam subscriberParam, Object condition) {
        try {
            // 设置上下文，并执行事件发布逻辑
            sseEmitterServer.publishEvent(eventKey, getSseEmitterObject(subscriberParam, condition));
        } catch (Exception e) {
            log.error("发布事件失败。Event Key: {}, param:{}, condition:{}", eventKey, subscriberParam, condition, e);
        }
    }


    /**
     * 获取分布式锁key
     * 默认不加锁，若需要加锁，请重写该方法
     *
     * @param eventKey 事件id
     * @return {@link String}
     * @author yuecaipu
     * @date 2024/6/18 16:08
     * @since 24Q1 T3S2-5.18.0
     */
    protected String getRedisLockKey(String eventKey) {
        return null;
    }

    protected String getRedisLockKey(String eventKey, Object condition) {
        return null;
    }

    /**
     * 获取锁
     * 当参数为空时,不加锁
     * 当获取锁失败，则返回false
     *
     * @param lockKey     key
     * @param lastingFlag 使用持久过期时间
     * @return {@link boolean}
     * @author yuecaipu
     * @date 2024/6/18 15:51
     * @since 24Q1 T3S2-5.18.0
     */
    protected boolean tryAcquireLock(String lockKey, boolean lastingFlag) {
        // 默认不加锁
        if (null == lockKey) {
            return true;
        }
        return redissonDistributedLockerService.tryLock(lockKey, TimeUnit.SECONDS, LOCK_WAIT_TIME, lastingFlag ? LOCK_LEASE_TIME_LASTING : LOCK_LEASE_TIME);
    }

    /**
     * 解除锁
     *
     * @param lockKey key
     * @author yuecaipu
     * @date 2024/6/28 11:25
     * @since 24Q1 T3S2-5.18.0
     */
    protected void unlock(String lockKey) {
        if (null != lockKey) {
            redissonDistributedLockerService.unlock(lockKey);
        }
    }


    /**
     * iot 触发推送
     *
     * @param projectId 项目id
     * @param condition iot源数据
     * @author yuecaipu
     * @date 2024/6/24 13:34
     * @since v5.240602
     */
    @Override
    public void doOperation(String projectId, Object condition) {
        if (condition == null) {
            return;
        }
        // 根据项目ID获取所有订阅者
        Map<String, SseSubscribeParam> eventKeyMap = sseEmitterServer.getEventKeysByProjectId(projectId);
        if (CollUtil.isEmpty(eventKeyMap)) {
            return;
        }

        // 异步执行发布事件任务
        eventKeyMap.forEach((eventKey, sseSubscribeParam) -> {
            if (isSubscribedEventType(sseSubscribeParam) || preFiltered(sseSubscribeParam, condition)) {
                return;
            }
            asyncSSEExecutor.execute(() -> publishEvent(eventKey, sseSubscribeParam, condition));
        });

    }


    /**
     * 获取推送数据
     * 返回数据为null时 不进行推
     *
     * @param param 订阅参数
     * @return {@link SseEmitterObject}
     * @author yuecaipu
     * @date 2024/4/30 20:20
     * @since 24Q1 T3S2-5.18.0
     */
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        log.error("实现类未重写，不支持改方法调用");
        return null;
    }

    /**
     * 根据条件，输送对应的事件，实际限制条件，有具体的实现类判断
     * 返回数据为null时 不进行推送
     *
     * @param param
     * @param condition
     * @return
     */
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param, Object condition) {
        log.error("实现类未重写，不支持改方法调用");
        return null;
    }

    /**
     * opt B9（switch.opt.ssePreFilter）：提交线程池前判断该订阅者是否一定收不到这条消息。
     * 子类只在结论确定时返回 true（与 getSseEmitterObject 会返回 null 的条件一致），默认不过滤。
     *
     * @param param     订阅参数
     * @param condition 触发条件
     * @return true 表示跳过，不提交计算任务
     */
    protected boolean skipBeforeCompute(SseSubscribeParam param, Object condition) {
        return false;
    }

    private boolean preFiltered(SseSubscribeParam param, Object condition) {
        if (!OptSwitch.on(OptSwitch.SSE_PRE_FILTER)) {
            return false;
        }
        try {
            boolean skip = skipBeforeCompute(param, condition);
            if (skip) {
                OptMetrics.inc("sse.preFiltered");
            }
            return skip;
        } catch (Exception e) {
            // 判断出错时按原逻辑提交，由计算阶段处理
            return false;
        }
    }

    /**
     * 校验类编码和objectId是否在订阅参数中
     *
     * @param param     订阅参数
     * @param classCode 类编码
     * @param objId     对象 ID
     * @return 是否匹配
     */
    protected boolean checkClaasCode2objectIds(SseSubscribeParam param, String classCode, String objId) {

        // 不过滤
        if (param.getClaasCode2objectIds() == null || param.getClaasCode2objectIds().isEmpty()) {
            return false;
        }

        // 根据订阅参数过滤iot数据
        if (!param.getClaasCode2objectIds().containsKey(classCode)) {
            return true;
        }
        return !param.getClaasCode2objectIds().get(classCode).contains(objId);
    }
}
