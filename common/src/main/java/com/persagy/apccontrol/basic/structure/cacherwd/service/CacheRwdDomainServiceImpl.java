package com.persagy.apccontrol.basic.structure.cacherwd.service;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.structure.cacherwd.model.entity.CacheRwdObject;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdBuildStatusVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdStatisticsVO;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRwdObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.CacheRwdStrategy;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @description: 物理世界对象缓存领域服务实现
 * @author: wxf
 * @create: 2026-02-26 14:26
 **/
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdDomainServiceImpl implements CacheRwdDomainService {

    /**
     * 公共项目结构构建专用的锁 Key 标识。
     */
    private static final String COMMON_STRUCTURE_LOCK_KEY = "COMMON_STRUCTURE";

    private final CacheRwdObjectRepository cacheRwdObjectRepository;

    /**
     * 缓存对象模型状态服务，用于限定异常传播范围并在完整构建成功后提交 READY。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    private final ApplicationContext applicationContext;

    @Resource
    private RedissonDistributedLockerService redissonDistributedLockerService;

    /**
     * 白名单 Key 前缀（全局配置，与 instance 参数无关）
     */
    private static final String WHITELIST_KEY_PREFIX = "WHITELIST_";

    @Override
    public String logTag() {
        return "CacheRwdDomainServiceImpl";
    }

    @Override
    public boolean saveBatch(List<CacheRwdObject> cacheRwdObjects) {
        if (cacheRwdObjects == null || cacheRwdObjects.isEmpty()) {
            return false;
        }
        return cacheRwdObjectRepository.saveBatch(cacheRwdObjects);
    }

    @Override
    public boolean saveByCustomIndexKey(String customIndexKey, List<CacheRwdObject> cacheRwdObjects) {
        if (cacheRwdObjects == null || cacheRwdObjects.isEmpty()) {
            return false;
        }
        // 先删除旧数据
        cacheRwdObjectRepository.removeByCustomIndexKey(customIndexKey);
        // 再保存新数据
        cacheRwdObjects.forEach(BaseAssignIdDO::clearSaveParameter);
        return cacheRwdObjectRepository.saveBatch(cacheRwdObjects);
    }

    @Override
    public List<CacheRwdObject> getByCustomIndexKey(String customIndexKey) {
        logProjectInstance().debug("获取缓存",
                Pair.of("customIndexKey", customIndexKey));
        return cacheRwdObjectRepository.getByCustomIndexKey(customIndexKey);
    }

    @Override
    public List<DtpRwdObjectDTO> listObjects(@NonNull String customIndexKey) {
        return this.getByCustomIndexKey(customIndexKey).stream()
                .filter(obj -> StringUtils.isNotBlank(obj.getObjectData()))
                .map(CacheRwdObject::toRwdObject)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteByCustomIndexKey(String customIndexKey) {
        int deletedRows = cacheRwdObjectRepository.removeByCustomIndexKey(customIndexKey);
        return deletedRows > 0;
    }

    @Override
    public Boolean buildCache(RunningMonitorTypeEnum runningMonitorType) {
        // 如果指定了类型，只构建该类型
        if (runningMonitorType != null) {
            // 项目结构由公共策略负责，但显式类型构建仍必须先确保公共结构快照完成。
            if (!this.buildCommonOnlyStrategies(runningMonitorType)) {
                return false;
            }
            return this.buildSingleType(runningMonitorType);
        }

        // 如果未指定类型，遍历构建所有类型（每个类型独立加锁）
        logProjectInstance().info("物理世界缓存-开始构建所有类型");
        Collection<CacheRwdStrategy> strategies = loadStrategies();
        if (CollectionUtil.isEmpty(strategies)) {
            this.logProjectInstance().error("物理世界缓存-构建失败，没有找到策略实现");
            return false;
        }

        // 同一一级类型可能包含多个策略。先按首次出现顺序去重，避免重复执行整套类型构建。
        Set<RunningMonitorTypeEnum> runningMonitorTypes = strategies.stream()
                .map(CacheRwdStrategy::runningMonitorType)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 先执行公共项目结构阶段，失败时阻止新版对象模型类型继续构建。
        boolean commonStructureSuccess = this.buildCommonStrategies();
        boolean allSuccess = commonStructureSuccess;
        for (RunningMonitorTypeEnum type : runningMonitorTypes) {
            if (!commonStructureSuccess) {
                this.logProjectInstance().error(
                        "物理世界缓存-类型构建跳过，公共项目结构未完成",
                        Pair.of("runningMonitorType", type.value()));
                continue;
            }
            try {
                boolean success = this.buildSingleType(type);
                if (!success) {
                    allSuccess = false;
                }
            } catch (RuntimeException exception) {
                allSuccess = false;
                // 单类型方法负责显式构建的异常结果；全量入口在这里补充一次异常堆栈。
                this.logProjectInstance().error("物理世界缓存-单类型构建异常", exception,
                        Pair.of("runningMonitorType", type));
            }
        }
        List<Pair<String, Object>> resultDetails = Arrays.asList(
                Pair.of("success", allSuccess),
                Pair.of("runningMonitorTypeCount", runningMonitorTypes.size()));
        if (allSuccess) {
            this.logProjectInstance().info("物理世界缓存-所有类型构建成功", resultDetails);
        } else {
            this.logProjectInstance().error("物理世界缓存-所有类型构建失败", resultDetails);
        }
        return allSuccess;
    }

    /**
     * 在独立全量锁内仅执行公共项目结构策略。
     *
     * <p>构建项目结构（建筑/楼层等公共对象）。公共项目结构失败时，
     * 依赖新版对象模型的一级类型不能继续构建或写 READY。</p>
     *
     * @return 公共项目结构是否构建成功
     * @since v7.260701.0
     */
    private boolean buildCommonStrategies() {
        String redisLockKey = RedisKey.getCacheRwdBuildKey(
                COMMON_STRUCTURE_LOCK_KEY, ContextUtil.getProjectId());
        if (!this.redissonDistributedLockerService.tryLock(
                redisLockKey, TimeUnit.SECONDS, 0, -1)) {
            this.logProjectInstance().warn("物理世界缓存-通用策略构建跳过，已有构建任务");
            return false;
        }

        try {
            return this.executeStrategiesBuild(null, true, true);
        } catch (RuntimeException exception) {
            this.logProjectInstance().error("物理世界缓存-通用策略构建失败", exception);
            return false;
        } finally {
            this.redissonDistributedLockerService.unlock(redisLockKey);
        }
    }

    /**
     * 在显式一级类型构建前，仅执行不会绑定业务类型的公共策略。
     *
     * @return 公共策略是否全部构建成功
     * @since v7.260802.0
     */
    private boolean buildCommonOnlyStrategies(
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        String redisLockKey = RedisKey.getCacheRwdBuildKey(
                        COMMON_STRUCTURE_LOCK_KEY, ContextUtil.getProjectId());
        if (!this.redissonDistributedLockerService.tryLock(
                redisLockKey, TimeUnit.SECONDS, 0, -1)) {
            this.logProjectInstance().warn("物理世界缓存-公共结构构建跳过，已有构建任务");
            return false;
        }
        try {
            return this.executeStrategiesBuild(runningMonitorType, true, true);
        } finally {
            this.redissonDistributedLockerService.unlock(redisLockKey);
        }
    }

    /**
     * 构建单个类型的缓存
     *
     * <p>各运行监测类型使用相同的策略执行流程；每个缓存键仍由写服务独立完成短事务替换。
     * 本版支持的一级类型只有在全部策略成功后，才会在当前构建锁释放前提交 READY。</p>
     *
     * @param runningMonitorType 运行监测类型
     * @return 是否构建成功
     * @throws RuntimeException 显式构建本版支持的新对象模型类型失败时抛出
     * @since v7.260701.0
     */
    private Boolean buildSingleType(RunningMonitorTypeEnum runningMonitorType) {
        String redisLockKey = CacheRwdDomainServiceImpl.getRedisLockKey(runningMonitorType);
        if (!this.redissonDistributedLockerService.tryLock(
                redisLockKey, TimeUnit.SECONDS, 0, -1)) {
            this.logProjectInstance().warn("物理世界缓存-类型构建跳过，已有构建任务",
                    Pair.of("runningMonitorType", runningMonitorType.value()));
            return false;
        }

        PerformanceTracker tracker = PerformanceTracker.currentOrDetached(
                "物理世界缓存-类型构建-" + runningMonitorType.value());
        this.logProjectInstance().info("物理世界缓存-类型构建开始",
                Pair.of("runningMonitorType", runningMonitorType.value()));
        try {
            boolean success;
            try (PerformanceTracker.SafeNode safeNode = tracker.track("类型构建-" + runningMonitorType.value())) {
                success = this.executeStrategiesBuild(runningMonitorType, false, false);
                if (success) {
                    // READY 必须在完整构建成功后、构建锁释放前提交，避免并发重建插入
                    // “缓存已完成但状态尚未切换”的窗口；失败或 false 结果不会进入该分支。
                    this.cacheObjectModelStatusService.markReady(runningMonitorType);
                }
            }
            if (success) {
                this.logProjectInstance().info("物理世界缓存-类型构建成功",
                        Pair.of("runningMonitorType", runningMonitorType.value()));
            } else {
                this.logProjectInstance().error("物理世界缓存-类型构建失败，策略未完成",
                        Pair.of("runningMonitorType", runningMonitorType.value()));
            }
            return success;
        } catch (RuntimeException exception) {
            // 任务、事件或接口边界负责打印一次堆栈；这里仅记录类型最终结果和可检索原因。
            this.logProjectInstance().error("物理世界缓存-类型构建异常", Arrays.asList(
                    Pair.of("runningMonitorType", runningMonitorType.value()),
                    Pair.of("failureType", exception.getClass().getSimpleName()),
                    Pair.of("failureReason", exception.getMessage())));
            throw exception;
        } finally {
            if (this.logProjectInstance().isDebugEnabled()) {
                this.logProjectInstance().debug("物理世界缓存-类型构建耗时",
                        Pair.of("data", tracker.prettyPrint()));
            }
            this.redissonDistributedLockerService.unlock(redisLockKey);
        }
    }

    /**
     * 执行策略构建，并按阶段筛选公共策略。
     *
     * @param runningMonitorType 一级运行监测类型
     * @param onlyCommonStrategies 是否只执行未声明一级类型的策略
     * @param commonOnlyStage 是否只执行标记为公共阶段专属的策略
     * @return 是否全部成功
     */
    private Boolean executeStrategiesBuild(
            @Nullable RunningMonitorTypeEnum runningMonitorType,
            boolean onlyCommonStrategies,
            boolean commonOnlyStage) {

        // 获取策略集合
        Collection<CacheRwdStrategy> strategies = loadStrategies();
        if (CollectionUtil.isEmpty(strategies)) {
            this.logProjectInstance().warn("物理世界缓存-策略集合为空",
                    Pair.of("runningMonitorType", runningMonitorType));
            return false;
        }

        boolean allSuccess = true;
        boolean strategyExecuted = false;
        for (CacheRwdStrategy strategy : strategies) {
            if (strategy == null) {
                continue;
            }
            RunningMonitorTypeEnum strategyType = strategy.runningMonitorType();
            if (commonOnlyStage && !strategy.onlyBuildInCommonPhase()) {
                continue;
            }
            if (!commonOnlyStage && strategy.onlyBuildInCommonPhase()) {
                continue;
            }
            if (onlyCommonStrategies && strategyType != null) {
                continue;
            }
            // 单类型构建同时执行匹配策略和通用策略，保证 READY 覆盖该类型的完整缓存范围。
            if (!onlyCommonStrategies && runningMonitorType != null && strategyType != null
                    && strategyType != runningMonitorType) {
                continue;
            }
            strategyExecuted = true;
            long startNanos = System.nanoTime();
            boolean strategySuccess = strategy.buildCacheData(runningMonitorType);
            long costTimeMs = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - startNanos);
            if (!strategySuccess) {
                // 策略主动返回失败表示本次写入没有提交；必须保留失败结果，避免随后误写 READY。
                allSuccess = false;
                this.logProjectInstance().warn("物理世界缓存-策略构建未完成",
                        Arrays.asList(
                                Pair.of("strategy", strategy.getClass().getSimpleName()),
                                Pair.of("runningMonitorType", runningMonitorType),
                                Pair.of("costTimeMs", costTimeMs)));
                continue;
            }
            this.logProjectInstance().info("物理世界缓存-策略构建成功",
                    Arrays.asList(
                            Pair.of("strategy", strategy.getClass().getSimpleName()),
                            Pair.of("runningMonitorType", runningMonitorType),
                            Pair.of("costTimeMs", costTimeMs)));
        }

        return !commonOnlyStage || strategyExecuted && allSuccess;
    }

    /**
     * 加载所有策略实现
     *
     * @return 策略集合
     */
    private Collection<CacheRwdStrategy> loadStrategies() {
        try {
            Map<String, CacheRwdStrategy> strategyBeans =
                    applicationContext.getBeansOfType(CacheRwdStrategy.class);
            return strategyBeans != null ? strategyBeans.values() : Collections.emptyList();
        } catch (Exception e) {
            logProjectInstance().error("物理世界缓存-加载策略失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取 Redis 锁 key
     *
     * @param runningMonitorType 运行监测类型
     * @return Redis 锁 key
     */
    private static String getRedisLockKey(@NonNull RunningMonitorTypeEnum runningMonitorType) {
        String typeKey = runningMonitorType.name();
        return RedisKey.getCacheRwdBuildKey(typeKey, ContextUtil.getProjectId());
    }

    @Override
    public CacheRwdBuildStatusVO getBuildStatus(@Nullable RunningMonitorTypeEnum runningMonitorType) {
        // 检查是否正在构建（通过尝试获取锁来判断）
        boolean isBuilding = runningMonitorType != null
                ? this.redissonDistributedLockerService.isLocked(
                        CacheRwdDomainServiceImpl.getRedisLockKey(runningMonitorType))
                : this.isAnyTypeBuilding();

        // 新模型以 READY 状态时间为准；历史类型仍使用旧缓存数据时间。
        LocalDateTime lastUpdateTime = this.getLastUpdateTimeByMonitorType(runningMonitorType);

        return CacheRwdBuildStatusVO.builder()
                .runningMonitorType(runningMonitorType)
                .isBuilding(isBuilding)
                .lastUpdateTime(lastUpdateTime)
                .build();
    }

    /**
     * 判断当前项目是否存在任意类型的构建任务。
     *
     * <p>全量构建由公共结构锁和逐类型计算锁组合保护；空类型查询状态时任一锁被持有
     * 即视为构建中。</p>
     *
     * @return 是否存在构建任务
     * @since v7.260802.0
     */
    private boolean isAnyTypeBuilding() {
        if (this.redissonDistributedLockerService.isLocked(RedisKey.getCacheRwdBuildKey(
                COMMON_STRUCTURE_LOCK_KEY, ContextUtil.getProjectId()))) {
            return true;
        }
        for (RunningMonitorTypeEnum type : RunningMonitorTypeEnum.values()) {
            if (this.redissonDistributedLockerService.isLocked(
                    CacheRwdDomainServiceImpl.getRedisLockKey(type))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据运行监测类型从 MySQL 中查找缓存最后成功更新时间。
     *
     * <p>本版支持的新缓存对象模型以持久化 READY 状态的更新时间为准；其他类型继续读取
     * 旧缓存对象表，避免改变历史构建状态语义。</p>
     *
     * @param runningMonitorType 运行监测类型
     * @return 最后更新时间，如果没有数据则返回 null
     */
    private @Nullable LocalDateTime getLastUpdateTimeByMonitorType(
            @Nullable RunningMonitorTypeEnum runningMonitorType) {
        if (this.cacheObjectModelStatusService.isSupported(runningMonitorType)) {
            return this.cacheObjectModelStatusService.getPersistedReadyTime(runningMonitorType);
        }
        // 查询指定 monitorType 的数据的最新更新时间
        CacheRwdObject latestRecord = this.cacheRwdObjectRepository
                .getOneByRunningMonitorType(runningMonitorType);
        return latestRecord != null ? latestRecord.getUpdateTime() : null;
    }

    @Override
    public CacheRwdStatisticsVO getStatistics() {
        // 按监测类型分组统计数量
        Map<RunningMonitorTypeEnum, Long> countByMonitorType = cacheRwdObjectRepository.countGroupByMonitorType();

        // 计算总数量
        long totalCount = countByMonitorType.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return CacheRwdStatisticsVO.builder()
                .countByMonitorType(countByMonitorType)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public String buildWhitelistKey(RunningMonitorTypeEnum runningMonitorType, DtpRwdObjectTypeEnum objectType) {
        return WHITELIST_KEY_PREFIX + runningMonitorType.name()
                + "_" + objectType.value();
    }
}
