package com.persagy.apccontrol.basic.resource.cache;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.cache.CacheObjectModelStatusEnum;
import com.persagy.framework.common.exception.BusinessException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 缓存对象模型状态资源服务。
 *
 * <p>数据库是状态事实源，Redis 只承担当前部署实例间的共享运行时传播。完整缓存构建成功后，
 * 由领域服务在构建锁释放前调用本服务统一提交数据库 READY 并刷新 Redis。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheObjectModelStatusService {

    /**
     * 获取本版本支持的一级运行监测类型。
     *
     * @return 不可修改的支持类型集合
     * @since v7.260701.0
     */
    @NonNull Set<RunningMonitorTypeEnum> supportedRunningMonitorTypes();

    /**
     * 判断一级运行监测类型是否属于本版本支持范围。
     *
     * @param runningMonitorType 一级运行监测类型
     * @return 是否支持
     * @since v7.260701.0
     */
    boolean isSupported(@Nullable RunningMonitorTypeEnum runningMonitorType);

    /**
     * 判断当前项目的一级运行监测类型是否已经可以读取新缓存模型。
     *
     * <p>类型不在固定支持范围、项目上下文缺失、Redis Key 缺失、状态非法或 Redis
     * 异常时均返回 {@code false}。本版本支持的末端、照明读取入口据此使用实时链路，其他未
     * 支持的监测系统继续保持原有缓存链路。</p>
     *
     * @param runningMonitorType 一级运行监测类型；缺失时按未就绪处理
     * @return 共享 Redis 状态是否为 {@link CacheObjectModelStatusEnum#READY}
     * @since v7.260701.0
     */
    boolean isReady(@Nullable RunningMonitorTypeEnum runningMonitorType);

    /**
     * 从共享 Redis 读取当前项目状态。
     *
     * <p>Key 缺失、状态未知或 Redis 异常均返回 {@code null}，调用方应按未就绪处理。</p>
     *
     * @param runningMonitorType 一级运行监测类型
     * @return Redis 状态，未就绪或不可判定时返回 {@code null}
     * @since v7.260701.0
     */
    @Nullable CacheObjectModelStatusEnum getStatus(@NonNull RunningMonitorTypeEnum runningMonitorType);

    /**
     * 获取当前项目指定一级运行监测类型最近一次成功构建时间。
     *
     * <p>仅当持久化状态记录有效且状态为 {@link CacheObjectModelStatusEnum#READY} 时返回其
     * 更新时间；记录缺失、非法或历史 {@code FAILED} 状态均返回 {@code null}。</p>
     *
     * @param runningMonitorType 一级运行监测类型
     * @return 最近一次成功构建时间，不存在时返回 {@code null}
     * @since v7.260701.0
     */
    @Nullable LocalDateTime getPersistedReadyTime(
            @NonNull RunningMonitorTypeEnum runningMonitorType);

    /**
     * 批量读取当前项目数据库状态。
     *
     * @param runningMonitorTypes 一级运行监测类型集合
     * @return 已解析的状态映射，不包含缺失、未支持或非法状态
     * @since v7.260701.0
     */
    @NonNull Map<RunningMonitorTypeEnum, CacheObjectModelStatusEnum> getPersistedStatuses(
            @NonNull Collection<RunningMonitorTypeEnum> runningMonitorTypes);

    /**
     * 将当前项目指定一级运行监测类型标记为就绪。
     *
     * <p>该方法先通过独立数据库事务幂等提交 READY，再刷新共享 Redis。调用方必须只在该类型
     * 的完整缓存构建成功后调用；Redis 刷新失败会抛出业务异常，数据库 READY 留待启动恢复或
     * 后续成功构建重新传播。</p>
     *
     * @param runningMonitorType 一级运行监测类型
     * @throws BusinessException 集团或项目上下文缺失，或 Redis 刷新失败时抛出
     * @since v7.260701.0
     */
    void markReady(@NonNull RunningMonitorTypeEnum runningMonitorType);
}
