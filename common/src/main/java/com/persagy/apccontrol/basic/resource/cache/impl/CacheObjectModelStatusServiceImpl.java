package com.persagy.apccontrol.basic.resource.cache.impl;

import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.cache.CacheObjectModelStatusEnum;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存对象模型状态资源服务实现。
 *
 * <p>状态数据库主键由项目、固定内部配置类型和一级运行监测类型的规范化业务键计算，使用
 * 专用 UPSERT 避免并发完整构建在“先查再插”时产生重复记录。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
public class CacheObjectModelStatusServiceImpl implements CacheObjectModelStatusService, LogComponent {

    /**
     * 本版本唯一允许切换新缓存模型的一级运行监测类型。
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<RunningMonitorTypeEnum> SUPPORTED_RUNNING_MONITOR_TYPES =
            Collections.unmodifiableSet(EnumSet.of(
                    RunningMonitorTypeEnum.AIR_TERMINAL,
                    RunningMonitorTypeEnum.LIGHTING,
                    RunningMonitorTypeEnum.VENTILATION,
                    RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY,
                    RunningMonitorTypeEnum.SECURITY));

    /**
     * 状态配置主键业务键分隔符。
     *
     * @since v7.260701.0
     */
    private static final @NonNull String BUSINESS_KEY_SEPARATOR = ":";

    /**
     * 基础配置资源服务，统一承载状态记录的专用查询和固定主键 UPSERT 事务边界。
     *
     * @since v7.260701.0
     */
    private final @NonNull RcBasicsConfigService rcBasicsConfigService;

    /**
     * 当前部署环境的共享 Redis 操作模板。
     *
     * @since v7.260701.0
     */
    private final @NonNull RedisCacheTemplate redisCacheTemplate;

    /**
     * 构造状态服务。
     *
     * @param rcBasicsConfigService 基础配置资源服务
     * @param redisCacheTemplate    Redis 操作模板
     * @since v7.260701.0
     */
    public CacheObjectModelStatusServiceImpl(
            @NonNull RcBasicsConfigService rcBasicsConfigService,
            @NonNull RedisCacheTemplate redisCacheTemplate) {
        super();
        this.rcBasicsConfigService = rcBasicsConfigService;
        this.redisCacheTemplate = redisCacheTemplate;
    }

    @Override
    public @NonNull String logTag() {
        return "CacheObjectModelStatusService";
    }

    @Override
    public @NonNull Set<RunningMonitorTypeEnum> supportedRunningMonitorTypes() {
        return CacheObjectModelStatusServiceImpl.SUPPORTED_RUNNING_MONITOR_TYPES;
    }

    @Override
    public boolean isSupported(@Nullable RunningMonitorTypeEnum runningMonitorType) {
        return CacheObjectModelStatusServiceImpl.SUPPORTED_RUNNING_MONITOR_TYPES.contains(runningMonitorType);
    }

    @Override
    public boolean isReady(@Nullable RunningMonitorTypeEnum runningMonitorType) {
        return runningMonitorType != null && CacheObjectModelStatusEnum.READY.equals(
                this.getStatus(runningMonitorType));
    }

    @Override
    public @Nullable CacheObjectModelStatusEnum getStatus(
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        if (!this.isSupported(runningMonitorType)) {
            return null;
        }
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        if (StringUtils.isAnyBlank(groupCode, projectId)) {
            return null;
        }
        String key = RedisKey.getCacheObjectModelStatusKey(groupCode, projectId, runningMonitorType);
        try {
            Object value = this.redisCacheTemplate.object().get(key);
            if (value instanceof CacheObjectModelStatusEnum) {
                return (CacheObjectModelStatusEnum) value;
            }
            if (!(value instanceof String)) {
                if (value != null) {
                    this.logProjectInstance().warn("缓存模型状态值类型非法，按未就绪处理",
                            Pair.of("runningMonitorType", runningMonitorType.value()));
                }
                return null;
            }
            CacheObjectModelStatusEnum status = CacheObjectModelStatusEnum.parse((String) value);
            if (status == null) {
                this.logProjectInstance().warn("缓存模型状态值未知，按未就绪处理",
                        Pair.of("runningMonitorType", runningMonitorType.value()));
            }
            return status;
        } catch (RuntimeException exception) {
            this.logProjectInstance().warn("读取缓存模型状态失败，按未就绪处理", exception,
                    Pair.of("runningMonitorType", runningMonitorType.value()));
            return null;
        }
    }

    @Override
    public @Nullable LocalDateTime getPersistedReadyTime(
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        if (!this.isSupported(runningMonitorType)) {
            return null;
        }
        String projectId = ContextUtil.getProjectId();
        if (StringUtils.isBlank(projectId)) {
            return null;
        }
        RcBasicsConfig config = this.rcBasicsConfigService.getById(
                this.buildStatusId(projectId, runningMonitorType));
        if (config == null
                || !StringUtils.equals(runningMonitorType.value(), config.getChildType())
                || !CacheObjectModelStatusEnum.READY.equals(
                        this.parsePersistedStatus(config, projectId, runningMonitorType))) {
            return null;
        }
        return config.getUpdateTime();
    }

    @Override
    public @NonNull Map<RunningMonitorTypeEnum, CacheObjectModelStatusEnum> getPersistedStatuses(
            @NonNull Collection<RunningMonitorTypeEnum> runningMonitorTypes) {
        if (CollectionUtils.isEmpty(runningMonitorTypes)) {
            return Collections.emptyMap();
        }
        String projectId = ContextUtil.getProjectId();
        Set<RunningMonitorTypeEnum> supportedTypes = EnumSet.noneOf(RunningMonitorTypeEnum.class);
        for (RunningMonitorTypeEnum runningMonitorType : runningMonitorTypes) {
            if (this.isSupported(runningMonitorType)) {
                supportedTypes.add(runningMonitorType);
            }
        }
        if (CollectionUtils.isEmpty(supportedTypes)) {
            return Collections.emptyMap();
        }
        List<String> statusIds = new ArrayList<>(supportedTypes.size());
        for (RunningMonitorTypeEnum runningMonitorType : supportedTypes) {
            statusIds.add(this.buildStatusId(projectId, runningMonitorType));
        }
        // 状态使用确定性主键，直接批量命中主键索引，避免轮询期间扫描历史配置表。
        List<RcBasicsConfig> configs = this.rcBasicsConfigService.listByIds(statusIds);
        Map<RunningMonitorTypeEnum, CacheObjectModelStatusEnum> result =
                new EnumMap<>(RunningMonitorTypeEnum.class);
        Set<RunningMonitorTypeEnum> seenTypes = EnumSet.noneOf(RunningMonitorTypeEnum.class);
        Set<RunningMonitorTypeEnum> duplicateTypes = EnumSet.noneOf(RunningMonitorTypeEnum.class);
        for (RcBasicsConfig config : configs) {
            RunningMonitorTypeEnum runningMonitorType = this.parseSupportedType(config.getChildType());
            if (runningMonitorType == null) {
                continue;
            }
            if (!seenTypes.add(runningMonitorType)) {
                result.remove(runningMonitorType);
                duplicateTypes.add(runningMonitorType);
                this.logProjectInstance().warn("持久化缓存模型状态存在重复一级类型，按未就绪处理",
                        Pair.of("runningMonitorType", runningMonitorType.value()));
                continue;
            }
            CacheObjectModelStatusEnum status = this.parsePersistedStatus(
                    config, projectId, runningMonitorType);
            if (status != null && !duplicateTypes.contains(runningMonitorType)) {
                result.put(runningMonitorType, status);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @since v7.260701.0
     */
    @Override
    public void markReady(@NonNull RunningMonitorTypeEnum runningMonitorType) {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        // 数据库 READY 是持久化事实；Redis Key 所需上下文必须在写库前完整，避免只提交半个状态。
        if (StringUtils.isBlank(groupCode)) {
            throw CacheObjectModelExceptionFactory.requiredValue("groupCode");
        }
        if (StringUtils.isBlank(projectId)) {
            throw CacheObjectModelExceptionFactory.requiredValue("projectId");
        }
        LocalDateTime now = DateUtils.now();
        RcBasicsConfig config = new RcBasicsConfig();
        config.setId(this.buildStatusId(projectId, runningMonitorType));
        config.setProjectId(projectId);
        config.setType(BasicsConfigTypeEnum.CACHE_OBJECT_MODEL_STATUS);
        config.setChildType(runningMonitorType.value());
        config.setSt(StateTypeEnum.VALID);
        config.setResult(CacheObjectModelStatusEnum.READY.getCode());
        config.setMd5ByResult();
        config.setCreateUser(ContextUtil.getUserId());
        config.setCreateTime(now);
        config.setUpdateUser(ContextUtil.getUserId());
        config.setUpdateTime(now);
        this.rcBasicsConfigService.upsertCacheObjectModelStatus(config);
        String key = RedisKey.getCacheObjectModelStatusKey(groupCode, projectId, runningMonitorType);
        try {
            // READY 是持久化路由事实的运行时副本，不设置过期时间，避免自动回到旧链路。
            this.redisCacheTemplate.object().set(
                    key, CacheObjectModelStatusEnum.READY.getCode());
        } catch (RuntimeException exception) {
            // 原始异常作为 cause 继续上抛，完整堆栈由任务或事件边界统一打印一次。
            this.logProjectInstance().warn("刷新缓存模型 READY 状态失败", Arrays.asList(
                    Pair.of("runningMonitorType", runningMonitorType.value()),
                    Pair.of("failureType", exception.getClass().getSimpleName()),
                    Pair.of("failureReason", StringUtils.defaultIfBlank(
                            exception.getMessage(), exception.getClass().getSimpleName()))));
            throw CacheObjectModelExceptionFactory.operationFailed(
                    "cacheObjectModelReadyRedisRefresh", exception);
        }
    }

    /**
     * 创建状态记录的稳定主键。
     *
     * <p>固定内部类型也参与摘要，避免未来其他配置类型复用同一项目和 childType 时发生主键
     * 冲突。</p>
     *
     * @param projectId           项目 ID
     * @param runningMonitorType 一级运行监测类型
     * @return 32 位 MD5 主键
     * @since v7.260701.0
     */
    private @NonNull String buildStatusId(
            @NonNull String projectId,
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        String businessKey = projectId + CacheObjectModelStatusServiceImpl.BUSINESS_KEY_SEPARATOR
                + BasicsConfigTypeEnum.CACHE_OBJECT_MODEL_STATUS.getCode()
                + CacheObjectModelStatusServiceImpl.BUSINESS_KEY_SEPARATOR
                + runningMonitorType.value();
        return DigestUtils.md5Hex(businessKey);
    }

    /**
     * 将数据库记录解析为固定支持范围内的状态。
     *
     * @param config             数据库记录
     * @param projectId         期望的当前项目 ID
     * @param runningMonitorType 期望的一级运行监测类型
     * @return 有效状态，记录缺失或非法时返回 {@code null}
     * @since v7.260701.0
     */
    private @Nullable CacheObjectModelStatusEnum parsePersistedStatus(
            @NonNull RcBasicsConfig config,
            @NonNull String projectId,
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        if (!StringUtils.equals(projectId, config.getProjectId())
                || !BasicsConfigTypeEnum.CACHE_OBJECT_MODEL_STATUS.equals(config.getType())
                || !StateTypeEnum.VALID.equals(config.getSt())) {
            return null;
        }
        CacheObjectModelStatusEnum status = CacheObjectModelStatusEnum.parse(config.getResult());
        if (status == null) {
            this.logProjectInstance().warn("持久化缓存模型状态值非法，按未就绪处理",
                    Pair.of("runningMonitorType", runningMonitorType.value()));
        }
        return status;
    }

    /**
     * 解析并校验支持范围内的一级运行监测类型。
     *
     * @param childType 一级运行监测类型编码
     * @return 支持的类型，未知或未支持时返回 {@code null}
     * @since v7.260701.0
     */
    private @Nullable RunningMonitorTypeEnum parseSupportedType(@Nullable String childType) {
        if (childType == null) {
            return null;
        }
        for (RunningMonitorTypeEnum runningMonitorType : RunningMonitorTypeEnum.values()) {
            if (StringUtils.equals(runningMonitorType.value(), childType)
                    && this.isSupported(runningMonitorType)) {
                return runningMonitorType;
            }
        }
        return null;
    }

}
