package com.persagy.apccontrol.task.cacheobjectmodel;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.cache.CacheObjectModelStatusEnum;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.common.config.SystemInitService;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 边缘端缓存对象模型 READY 状态收敛任务。
 *
 * <p>本任务只传播状态，不检查七张缓存表、不访问 RWD，也不改变业务读取链路。每轮先
 * 检查边缘 Redis 中的显式状态；只有缺失的一级类型才批量查询本地
 * {@code rc_basics_config}，并且只把数据库中的 {@code READY} 状态写入 Redis。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component
@ConditionalOnProperty(prefix = "project.cache.rwd", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class CacheObjectModelStatusConvergenceTask implements SystemInitService, LogComponent {

    /**
     * 状态收敛的固定间隔，Redis 已有显式值时后续轮次不会访问数据库。
     *
     * @since v7.260701.0
     */
    private static final long STATUS_REFRESH_INTERVAL_MILLIS = 30_000L;

    /**
     * 本地项目配置，提供边缘 Redis Key 所需的集团和项目维度。
     *
     * @since v7.260701.0
     */
    private final @NonNull ProjectCfg projectCfg;

    /**
     * 缓存对象模型状态服务，负责读取已同步到边缘数据库的状态。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService statusService;

    /**
     * 边缘部署环境的共享 Redis 操作模板。
     *
     * @since v7.260701.0
     */
    private final @NonNull RedisCacheTemplate redisCacheTemplate;

    /**
     * 创建边缘状态收敛任务。
     *
     * @param projectCfg         本地项目配置
     * @param statusService      缓存对象模型状态服务
     * @param redisCacheTemplate 边缘 Redis 操作模板
     * @since v7.260701.0
     */
    public CacheObjectModelStatusConvergenceTask(
            @NonNull ProjectCfg projectCfg,
            @NonNull CacheObjectModelStatusService statusService,
            @NonNull RedisCacheTemplate redisCacheTemplate) {
        super();
        this.projectCfg = projectCfg;
        this.statusService = statusService;
        this.redisCacheTemplate = redisCacheTemplate;
    }

    /**
     * 服务启动阶段执行一次状态收敛。
     *
     * @since v7.260701.0
     */
    @Override
    public void init() {
        this.converge();
    }

    /**
     * 在启动收敛后每 30 秒重试缺失状态。
     *
     * <p>初始延迟与固定间隔相同，避免启动初始化和调度线程同时查询；任务执行期间
     * 即使 Redis 中出现人工状态，也由写入阶段的原子 SETNX 保证不覆盖。</p>
     *
     * @since v7.260701.0
     */
    @Scheduled(
            fixedDelay = CacheObjectModelStatusConvergenceTask.STATUS_REFRESH_INTERVAL_MILLIS,
            initialDelay = CacheObjectModelStatusConvergenceTask.STATUS_REFRESH_INTERVAL_MILLIS)
    public void scheduledConverge() {
        this.converge();
    }

    /**
     * 返回启动初始化服务名称。
     *
     * @return 初始化服务名称
     * @since v7.260701.0
     */
    @Override
    public @NonNull String getName() {
        return "缓存对象模型状态收敛";
    }

    /**
     * 返回项目日志标签。
     *
     * @return 日志标签
     * @since v7.260701.0
     */
    @Override
    public @NonNull String logTag() {
        return "CacheObjectModelStatusConvergenceTask";
    }

    /**
     * 执行一轮状态收敛。
     *
     * <p>Redis Key 扫描失败时不能可靠地区分“缺失”和“已有人工值”，因此整轮直接结束，
     * 防止 Redis 故障期间查询数据库并覆盖运维开关。数据库查询或写入失败也只记录日志，
     * 留待下一轮重试；任务本身不向业务请求传播异常。</p>
     *
     * @since v7.260701.0
     */
    private void converge() {
        String groupCode = StringUtils.trimToNull(this.projectCfg.getGroupCode());
        String projectId = StringUtils.trimToNull(this.projectCfg.getProjectId());
        if (StringUtils.isAnyBlank(groupCode, projectId)) {
            this.logProjectInstance().warn("缓存对象模型状态收敛跳过，边缘项目上下文未配置");
            return;
        }

        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        ContextUtil.setProjectId(projectId);
        try {
            List<RunningMonitorTypeEnum> missingTypes = this.findMissingTypes(groupCode, projectId);
            if (CollectionUtils.isEmpty(missingTypes)) {
                return;
            }

            final Map<RunningMonitorTypeEnum, CacheObjectModelStatusEnum> persistedStatuses;
            try {
                // 只有缺失 Key 的类型进入一次批量查询，已有 READY/FAILED/非法值均不查库。
                persistedStatuses = this.statusService.getPersistedStatuses(missingTypes);
            } catch (RuntimeException exception) {
                this.logProjectInstance().warn("缓存对象模型状态收敛读取边缘数据库失败，等待下一轮重试",
                        exception, Pair.of("missingTypes", missingTypes));
                return;
            }

            for (RunningMonitorTypeEnum runningMonitorType : missingTypes) {
                if (CacheObjectModelStatusEnum.READY.equals(persistedStatuses.get(runningMonitorType))) {
                    this.writeReadyIfAbsent(groupCode, projectId, runningMonitorType);
                }
            }
        } finally {
            // 收敛任务运行在线程池中，必须清理项目上下文，避免污染后续边缘任务。
            ContextUtil.unloadContext();
        }
    }

    /**
     * 扫描本版本两个状态 Key 并收集缺失类型。
     *
     * <p>这里只调用 Redis 的存在性接口，不读取或解析值；这样测试手工写入的 FAILED、
     * 非法字符串或其他显式值都能被视为“已占位”，不会被任务覆盖。</p>
     *
     * @param groupCode 集团编码
     * @param projectId 项目 ID
     * @return 缺失类型；Redis 不可用时返回 {@code null} 并由调用方跳过本轮
     * @since v7.260701.0
     */
    private @Nullable List<RunningMonitorTypeEnum> findMissingTypes(
            @NonNull String groupCode, @NonNull String projectId) {
        List<RunningMonitorTypeEnum> missingTypes = new ArrayList<>();
        for (RunningMonitorTypeEnum runningMonitorType : this.statusService.supportedRunningMonitorTypes()) {
            String key = RedisKey.getCacheObjectModelStatusKey(groupCode, projectId, runningMonitorType);
            try {
                if (!this.redisCacheTemplate.hasKey(key)) {
                    missingTypes.add(runningMonitorType);
                }
            } catch (RuntimeException exception) {
                this.logProjectInstance().warn("缓存对象模型状态收敛检查 Redis Key 失败，跳过本轮",
                        exception, Pair.of("runningMonitorType", runningMonitorType.value()));
                return null;
            }
        }
        return missingTypes;
    }

    /**
     * 以 SETNX 方式写入 READY。
     *
     * <p>第一次扫描和写入之间可能有测试或运维线程写入状态，因此不能使用无条件 SET；
     * 原子写入保证任何已经存在的显式值都保持原样，删除 Key 后下一轮才会再次尝试。</p>
     *
     * @param groupCode           集团编码
     * @param projectId           项目 ID
     * @param runningMonitorType 一级运行监测类型
     * @since v7.260701.0
     */
    private void writeReadyIfAbsent(
            @NonNull String groupCode,
            @NonNull String projectId,
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        String key = RedisKey.getCacheObjectModelStatusKey(groupCode, projectId, runningMonitorType);
        try {
            Boolean written = this.redisCacheTemplate.delegate().opsForValue().setIfAbsent(
                    key, CacheObjectModelStatusEnum.READY.getCode());
            if (Boolean.TRUE.equals(written)) {
                this.logProjectInstance().info("缓存对象模型 READY 状态已收敛到边缘 Redis",
                        Pair.of("runningMonitorType", runningMonitorType.value()));
            }
        } catch (RuntimeException exception) {
            this.logProjectInstance().warn("缓存对象模型 READY 状态写入边缘 Redis 失败，等待下一轮重试",
                    exception, Pair.of("runningMonitorType", runningMonitorType.value()));
        }
    }
}
