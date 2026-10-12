package com.persagy.apccontrol.basic.structure.demand.repository;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigResultService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigResultSnapshotService;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultBusinessKey;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultRepositoryQueryParam;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 需求配置预计算结果仓储实现
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigResultRepositoryImpl implements DemandConfigResultRepository {

    /**
     * 按业务键查询旧结果的批次大小。
     *
     * <p>全量初始化时单模板、单场景可能覆盖约一万个对象。业务键查询在 SQL 中会展开为 OR 条件，
     * 每个业务键又包含模板、场景、实例类型和实例ID等多个条件，单次500个业务键已经是较大的谓词集合；
     * 继续放大会明显增加 SQL 解析和优化成本，所以这里保持较小批次。</p>
     */
    private static final int BUSINESS_KEY_QUERY_BATCH_SIZE = 500;

    /**
     * 结果ID删除/更新批次大小。
     *
     * <p>该批次只用于已经查出的结果ID，SQL 形态是主键 IN 条件。相比业务键 OR 查询可以适当放大，
     * 以减少全量初始化或模板类型清理、已有结果更新时的循环次数，同时避免单次关闭快照、删除或更新主表持锁过久。</p>
     */
    private static final int RESULT_DELETE_BATCH_SIZE = 1000;

    /**
     * 屏蔽状态更新批次大小。
     *
     * <p>屏蔽更新先分页查询结果ID，再按ID关闭快照、更新主表并写入新快照；该路径同样使用主键 IN，
     * 批次设置为1000可以减少屏蔽空间批量变化时的数据库往返次数。</p>
     */
    private static final int SHIELDED_UPDATE_BATCH_SIZE = 1000;

    private static final String UPSERT_LOCK_BUSINESS = "demandConfigResultUpsert";

    private static final String NO_SCENE_LOCK_PART = "NO_SCENE";

    private static final String ALL_SCENE_LOCK_PART = "ALL_SCENE";

    /**
     * 一次结果写入获取全部范围锁的最长等待时间。
     *
     * <p>同一次写入可能同时获取模板级锁和场景级锁，多个锁共用该等待预算，避免按锁累计等待。
     * 锁租期仍使用 Redisson watchdog 自动续期，确保大批量结果和快照写入不会因固定租期到期而并发穿透。</p>
     */
    private static final int WRITE_LOCK_WAIT_MILLIS = 120_000;

    private final @NonNull DemandConfigResultService demandConfigResultService;
    private final @NonNull DemandConfigResultSnapshotService demandConfigResultSnapshotService;
    private final @NonNull RedissonDistributedLockerService redissonDistributedLockerService;

    @Override
    public @NonNull Optional<DemandConfigResult> getOptByBusinessKey(@NonNull DemandConfigResultBusinessKey businessKey) {
        DemandConfigResultRepositoryQueryParam queryParam = new DemandConfigResultRepositoryQueryParam();
        queryParam.setBusinessKeys(Collections.singleton(businessKey));
        return this.list(queryParam).stream().findFirst();
    }

    @Override
    public @NonNull List<DemandConfigResult> list(@NonNull DemandConfigResultRepositoryQueryParam param) {
        return this.demandConfigResultService.list(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public @NonNull DemandConfigResult upsert(@NonNull DemandConfigResult result) {
        return this.upsert(Collections.singleton(result)).stream().findFirst().orElse(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public @NonNull List<DemandConfigResult> upsert(@NonNull Collection<DemandConfigResult> results) {
        return this.syncSameProject(Collections.emptyList(), new ArrayList<>(results));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public @NonNull List<DemandConfigResult> syncResults(
            @NonNull Collection<DemandConfigResult> deleteParams,
            @NonNull Collection<DemandConfigResult> upsertParams) {
        return this.syncSameProject(new ArrayList<>(deleteParams), new ArrayList<>(upsertParams));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public @NonNull List<DemandConfigResult> syncRefreshScope(
            @NonNull DemandConfigResultRepositoryQueryParam scopeParam,
            @NonNull Collection<DemandConfigResult> upsertParams) {
        List<DemandConfigResult> toBeSavedResults = upsertParams.stream()
                .peek(this::fillDefaultValue)
                .collect(Collectors.toList());
        List<DemandConfigResult> deduplicatedResults = this.deduplicateByBusinessKey(toBeSavedResults);
        this.validateRefreshScope(scopeParam, deduplicatedResults);
        List<String> acquiredLockKeys = new ArrayList<>();
        boolean releaseInFinally = true;
        try {
            this.lockConcreteQueryWriteScopes(scopeParam, acquiredLockKeys);
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                this.unlockAfterTransactionCompletion(acquiredLockKeys);
                releaseInFinally = false;
            }
            List<DemandConfigResult> existsResults = this.listRefreshScopeResults(scopeParam);
            Map<DemandConfigResultBusinessKey, DemandConfigResult> existsResultMap =
                    this.mapResultsByBusinessKey(existsResults);
            Set<DemandConfigResultBusinessKey> upsertBusinessKeys = deduplicatedResults.stream()
                    .map(DemandConfigResultBusinessKey::of)
                    .collect(Collectors.toSet());
            List<Long> deleteResultIds = existsResults.stream()
                    .filter(result -> !upsertBusinessKeys.contains(DemandConfigResultBusinessKey.of(result)))
                    .map(BaseAssignIdDO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            this.removeResultsByIds(deleteResultIds);
            if (CollectionUtils.isEmpty(deduplicatedResults)) {
                return Collections.emptyList();
            }
            return this.upsertSameProject(deduplicatedResults, existsResultMap);
        } finally {
            if (releaseInFinally) {
                this.unlockReverse(acquiredLockKeys);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertSnapshot(@NonNull DemandConfigResult result) {
        this.demandConfigResultSnapshotService.insertSnapshot(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertSnapshots(@NonNull Collection<DemandConfigResult> results) {
        this.demandConfigResultSnapshotService.insertSnapshots(results);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSnapshot(@NonNull Collection<Long> resultIds) {
        this.demandConfigResultSnapshotService.closeOpenSnapshots(resultIds, DateUtils.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShielded(@NonNull DemandConfigResultBusinessKey businessKey, boolean shielded) {
        this.updateShielded(Collections.singleton(businessKey), shielded);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShielded(
            @NonNull Collection<DemandConfigResultBusinessKey> businessKeys,
            boolean shielded) {
        if (CollectionUtils.isEmpty(businessKeys)) {
            return;
        }
        List<String> acquiredLockKeys = new ArrayList<>();
        boolean releaseInFinally = true;
        try {
            this.lockWriteScopes(businessKeys, acquiredLockKeys);
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                this.unlockAfterTransactionCompletion(acquiredLockKeys);
                releaseInFinally = false;
            }
            LocalDateTime validEndTime = DateUtils.now();
            for (List<DemandConfigResultBusinessKey> businessKeyPartition : Lists.partition(
                    new ArrayList<>(businessKeys), DemandConfigResultRepositoryImpl.BUSINESS_KEY_QUERY_BATCH_SIZE)) {
                this.updateShieldedByBusinessKeys(businessKeyPartition, shielded, validEndTime);
            }
        } finally {
            if (releaseInFinally) {
                this.unlockReverse(acquiredLockKeys);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeResults(@NonNull DemandConfigResultRepositoryQueryParam param) {
        List<String> acquiredLockKeys = new ArrayList<>();
        boolean releaseInFinally = true;
        try {
            this.lockConcreteQueryWriteScopes(param, acquiredLockKeys);
            if (TransactionSynchronizationManager.isActualTransactionActive()
                    && CollectionUtils.isNotEmpty(acquiredLockKeys)) {
                this.unlockAfterTransactionCompletion(acquiredLockKeys);
                releaseInFinally = false;
            }
            this.removeResultsWithoutLock(param);
        } finally {
            if (releaseInFinally) {
                this.unlockReverse(acquiredLockKeys);
            }
        }
    }

    /**
     * 按查询参数分页删除结果，不额外获取写锁。
     *
     * <p>调用方如果已经通过业务键或模板/场景边界持有写锁，可以走这个内部方法，避免重复加锁。
     * 删除仍会按ID分页关闭快照和删除主表，防止模板类型级清理一次加载全部结果ID。</p>
     *
     * @param param 查询参数
     * @since v7.260701.0
     */
    private void removeResultsWithoutLock(@NonNull DemandConfigResultRepositoryQueryParam param) {
        List<Long> resultIds = this.demandConfigResultService.listIds(
                param, DemandConfigResultRepositoryImpl.RESULT_DELETE_BATCH_SIZE);
        while (CollectionUtils.isNotEmpty(resultIds)) {
            this.removeResultsByIds(resultIds);
            resultIds = this.demandConfigResultService.listIds(
                    param, DemandConfigResultRepositoryImpl.RESULT_DELETE_BATCH_SIZE);
        }
    }

    /**
     * 同步删除失效结果并新增或更新有效结果。
     *
     * @param deleteParams 删除定位参数集合
     * @param upsertParams 新增或更新结果集合
     * @return 新增或更新后的结果集合
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> syncSameProject(
            @NonNull Collection<DemandConfigResult> deleteParams,
            @NonNull Collection<DemandConfigResult> upsertParams) {
        if (CollectionUtils.isEmpty(deleteParams) && CollectionUtils.isEmpty(upsertParams)) {
            return Collections.emptyList();
        }
        List<DemandConfigResult> toBeDeletedResults = deleteParams.stream()
                .peek(this::fillDefaultValue)
                .collect(Collectors.toList());
        List<DemandConfigResult> toBeSavedResults = upsertParams.stream()
                .peek(this::fillDefaultValue)
                .collect(Collectors.toList());
        List<DemandConfigResult> deduplicatedResults = this.deduplicateByBusinessKey(toBeSavedResults);
        List<DemandConfigResult> deduplicatedDeleteParams =
                this.deduplicateDeleteParams(toBeDeletedResults, deduplicatedResults);
        if (CollectionUtils.isEmpty(deduplicatedDeleteParams) && CollectionUtils.isEmpty(deduplicatedResults)) {
            return Collections.emptyList();
        }
        List<String> acquiredLockKeys = new ArrayList<>();
        boolean releaseInFinally = true;
        try {
            this.lockWriteScopes(deduplicatedDeleteParams, deduplicatedResults, acquiredLockKeys);
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                this.unlockAfterTransactionCompletion(acquiredLockKeys);
                releaseInFinally = false;
            }
            if (CollectionUtils.isNotEmpty(deduplicatedDeleteParams)) {
                this.removeResultsByBusinessKeys(deduplicatedDeleteParams);
            }
            if (CollectionUtils.isEmpty(deduplicatedResults)) {
                return Collections.emptyList();
            }
            return this.upsertSameProject(deduplicatedResults);
        } finally {
            if (releaseInFinally) {
                this.unlockReverse(acquiredLockKeys);
            }
        }
    }

    /**
     * 校验模板和场景刷新范围。
     *
     * <p>范围同步只允许处理一个明确的模板/场景边界。传入结果如果超出该边界，已有结果查询和写锁
     * 将无法覆盖实际写入范围，因此必须在执行数据库操作前拒绝。</p>
     *
     * @param scopeParam  刷新范围查询参数
     * @param upsertParams 新增或更新结果集合
     * @since v7.260701.0
     */
    private void validateRefreshScope(
            @NonNull DemandConfigResultRepositoryQueryParam scopeParam,
            @NonNull Collection<DemandConfigResult> upsertParams) {
        Long relTemplateId = scopeParam.getRelTemplateId();
        Long relSceneId = scopeParam.getRelSceneId();
        boolean noScene = Boolean.TRUE.equals(scopeParam.getRelSceneIdIsNull());
        if (Objects.isNull(relTemplateId)
                || Objects.nonNull(relSceneId) == noScene
                || CollectionUtils.isNotEmpty(scopeParam.getRelTemplateIds())
                || CollectionUtils.isNotEmpty(scopeParam.getRelSceneIds())) {
            throw new IllegalArgumentException("刷新需求配置结果必须指定唯一模板和明确场景范围");
        }
        for (DemandConfigResult upsertParam : upsertParams) {
            if (!Objects.equals(relTemplateId, upsertParam.getRelTemplateId())
                    || !Objects.equals(relSceneId, upsertParam.getRelSceneId())) {
                throw new IllegalArgumentException("刷新需求配置结果超出模板和场景范围");
            }
        }
    }

    /**
     * 删除与 upsert 无重叠的定位参数。
     *
     * @param deleteParams 待删除定位参数集合
     * @param upsertParams 待新增或更新结果集合
     * @return 去重后的删除定位参数集合
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> deduplicateDeleteParams(
            @NonNull List<DemandConfigResult> deleteParams,
            @NonNull List<DemandConfigResult> upsertParams) {
        if (CollectionUtils.isEmpty(deleteParams)) {
            return Collections.emptyList();
        }
        List<DemandConfigResult> deduplicatedDeleteParams = this.deduplicateByBusinessKey(deleteParams);
        if (CollectionUtils.isEmpty(upsertParams)) {
            return deduplicatedDeleteParams;
        }
        Set<DemandConfigResultBusinessKey> upsertBusinessKeys = upsertParams.stream()
                .map(DemandConfigResultBusinessKey::of)
                .collect(Collectors.toSet());
        return deduplicatedDeleteParams.stream()
                .filter(result -> !upsertBusinessKeys.contains(DemandConfigResultBusinessKey.of(result)))
                .collect(Collectors.toList());
    }

    /**
     * 按结果ID删除当前结果并关闭开放快照。
     *
     * <p>该方法只允许在已持有模板/场景写锁的内部删除流程中调用。对外删除入口必须传查询参数，
     * 由 {@link #removeResults(DemandConfigResultRepositoryQueryParam)} 先获取写锁，再分页调用这里。</p>
     *
     * @param resultIds 结果ID集合
     * @since v7.260701.0
     */
    private void removeResultsByIds(@NonNull Collection<Long> resultIds) {
        if (CollectionUtils.isEmpty(resultIds)) {
            return;
        }
        LocalDateTime validEndTime = DateUtils.now();
        List<Long> resultIdList = new ArrayList<>(resultIds);
        for (List<Long> resultIdPartition : Lists.partition(
                resultIdList, DemandConfigResultRepositoryImpl.RESULT_DELETE_BATCH_SIZE)) {
            this.demandConfigResultSnapshotService.closeOpenSnapshots(resultIdPartition, validEndTime);
            this.demandConfigResultService.removeResultByIds(resultIdPartition);
        }
    }

    /**
     * 根据业务键删除结果并关闭开放快照。
     *
     * @param deleteParams 删除定位参数集合
     * @since v7.260701.0
     */
    private void removeResultsByBusinessKeys(@NonNull Collection<DemandConfigResult> deleteParams) {
        if (CollectionUtils.isEmpty(deleteParams)) {
            return;
        }
        List<DemandConfigResultBusinessKey> businessKeys = deleteParams.stream()
                .map(DemandConfigResultBusinessKey::of)
                .collect(Collectors.toList());
        for (List<DemandConfigResultBusinessKey> businessKeyPartition : Lists.partition(
                businessKeys, DemandConfigResultRepositoryImpl.BUSINESS_KEY_QUERY_BATCH_SIZE)) {
            DemandConfigResultRepositoryQueryParam queryParam = new DemandConfigResultRepositoryQueryParam();
            queryParam.setBusinessKeys(businessKeyPartition);
            this.removeResultsWithoutLock(queryParam);
        }
    }

    /**
     * 按业务键分片更新屏蔽状态。
     *
     * @param businessKeys 业务键分片
     * @param shielded     是否屏蔽
     * @param validEndTime 快照截止时间
     * @since v7.260701.0
     */
    private void updateShieldedByBusinessKeys(
            @NonNull List<DemandConfigResultBusinessKey> businessKeys,
            boolean shielded,
            @NonNull LocalDateTime validEndTime) {
        DemandConfigResultRepositoryQueryParam queryParam = new DemandConfigResultRepositoryQueryParam();
        queryParam.setBusinessKeys(businessKeys);
        queryParam.setIsShielded(!shielded);
        List<Long> resultIds = this.demandConfigResultService.listIds(
                queryParam, DemandConfigResultRepositoryImpl.SHIELDED_UPDATE_BATCH_SIZE);
        while (CollectionUtils.isNotEmpty(resultIds)) {
            this.updateShieldedByResultIds(resultIds, shielded, validEndTime);
            resultIds = this.demandConfigResultService.listIds(
                    queryParam, DemandConfigResultRepositoryImpl.SHIELDED_UPDATE_BATCH_SIZE);
        }
    }

    /**
     * 按结果 ID 分片更新屏蔽状态并维护快照。
     *
     * @param resultIds    结果 ID 分片
     * @param shielded     是否屏蔽
     * @param validEndTime 快照截止时间
     * @since v7.260701.0
     */
    private void updateShieldedByResultIds(
            @NonNull Collection<Long> resultIds,
            boolean shielded,
            @NonNull LocalDateTime validEndTime) {
        for (List<Long> resultIdPartition : Lists.partition(
                new ArrayList<>(resultIds), DemandConfigResultRepositoryImpl.SHIELDED_UPDATE_BATCH_SIZE)) {
            DemandConfigResultRepositoryQueryParam queryParam = new DemandConfigResultRepositoryQueryParam();
            queryParam.setIds(resultIdPartition);
            queryParam.setIsShielded(!shielded);
            List<DemandConfigResult> toBeUpdatedResults = this.list(queryParam).stream()
                    .peek(item -> item.setIsShielded(shielded))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(toBeUpdatedResults)) {
                continue;
            }
            List<Long> toBeUpdatedResultIds = toBeUpdatedResults.stream()
                    .map(BaseAssignIdDO::getId)
                    .collect(Collectors.toList());
            // 屏蔽变化不改最终配置JSON，但必须形成新版本：关闭旧快照、更新主表、再插入新快照。
            this.demandConfigResultSnapshotService.closeOpenSnapshots(toBeUpdatedResultIds, validEndTime);
            this.demandConfigResultService.updateByIdValidate(
                    toBeUpdatedResults, DemandConfigResultRepositoryImpl.SHIELDED_UPDATE_BATCH_SIZE);
            this.demandConfigResultSnapshotService.insertSnapshots(toBeUpdatedResults);
        }
    }

    /**
     * 同项目内批量新增或更新结果
     *
     * @param results 结果集合
     * @return 新增或更新后的结果集合
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> upsertSameProject(@NonNull List<DemandConfigResult> results) {
        List<DemandConfigResultBusinessKey> businessKeys = results.stream()
                .map(DemandConfigResultBusinessKey::of)
                .collect(Collectors.toList());
        Map<DemandConfigResultBusinessKey, DemandConfigResult> existsResultMap =
                this.mapExistsResultsByBusinessKeys(businessKeys);
        return this.upsertSameProject(results, existsResultMap);
    }

    /**
     * 使用已查询的旧结果批量新增或更新同项目结果。
     *
     * <p>刷新范围同步已经在写锁内查询过当前结果，直接复用该映射可以避免再次按业务键查询，
     * 同时保证删除差异计算和新增或更新判断基于同一个锁内数据快照。</p>
     *
     * @param results         结果集合
     * @param existsResultMap 业务键到已存在结果的映射
     * @return 新增或更新后的结果集合
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> upsertSameProject(
            @NonNull List<DemandConfigResult> results,
            @NonNull Map<DemandConfigResultBusinessKey, DemandConfigResult> existsResultMap) {

        // 这里不用数据库原生upsert：需要保留已有记录的创建人/创建时间，并在更新前关闭旧快照。
        // 所以先按业务键查旧记录，再拆分新增和更新两条路径处理。
        List<DemandConfigResult> toBeAddedResults = new ArrayList<>();
        List<DemandConfigResult> toBeUpdatedResults = new ArrayList<>();
        for (DemandConfigResult result : results) {
            DemandConfigResult existsResult = existsResultMap.get(DemandConfigResultBusinessKey.of(result));
            if (Objects.isNull(existsResult)) {
                toBeAddedResults.add(result);
                continue;
            }
            result.setId(existsResult.getId());
            result.setCreateTime(existsResult.getCreateTime());
            result.setCreateUser(existsResult.getCreateUser());
            toBeUpdatedResults.add(result);
        }

        if (CollectionUtils.isNotEmpty(toBeAddedResults)) {
            this.demandConfigResultService.insertResults(toBeAddedResults);
            this.demandConfigResultSnapshotService.insertSnapshots(toBeAddedResults);
        }
        if (CollectionUtils.isNotEmpty(toBeUpdatedResults)) {
            this.updateExistingResults(toBeUpdatedResults);
        }
        List<DemandConfigResult> savedResults = new ArrayList<>(toBeAddedResults.size() + toBeUpdatedResults.size());
        savedResults.addAll(toBeAddedResults);
        savedResults.addAll(toBeUpdatedResults);
        return savedResults;
    }

    /**
     * 查询刷新范围内的当前结果。
     *
     * <p>调用方必须已经持有对应模板/场景写锁。查询只返回差异计算和新增或更新判断需要的字段，
     * 避免批量刷新时把最终配置和继承链JSON大字段加载到内存。</p>
     *
     * @param scopeParam 刷新范围查询参数
     * @return 刷新范围内的当前结果
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> listRefreshScopeResults(
            @NonNull DemandConfigResultRepositoryQueryParam scopeParam) {
        this.addExistingResultColumns(scopeParam);
        return this.demandConfigResultService.list(scopeParam);
    }

    /**
     * 分片更新已有结果并维护快照。
     *
     * @param results 需要更新的结果集合
     * @since v7.260701.0
     */
    private void updateExistingResults(@NonNull Collection<DemandConfigResult> results) {
        LocalDateTime validEndTime = DateUtils.now();
        for (List<DemandConfigResult> resultPartition : Lists.partition(
                new ArrayList<>(results), DemandConfigResultRepositoryImpl.RESULT_DELETE_BATCH_SIZE)) {
            List<Long> resultIds = resultPartition.stream()
                    .map(BaseAssignIdDO::getId)
                    .collect(Collectors.toList());
            // 更新结果时快照必须和主表保持同一片事务顺序：先关闭旧开放快照，再更新主表，最后写入新快照。
            this.demandConfigResultSnapshotService.closeOpenSnapshots(resultIds, validEndTime);
            this.demandConfigResultService.updateByIdValidate(
                    resultPartition, DemandConfigResultRepositoryImpl.RESULT_DELETE_BATCH_SIZE);
            this.demandConfigResultSnapshotService.insertSnapshots(resultPartition);
        }
    }

    /**
     * 按业务键去重。
     *
     * <p>同一次刷新内同业务键可能出现多条聚合结果。这里按集合迭代顺序采用最后写入覆盖前值，
     * 保证查询旧记录和拆分新增/更新前，每个业务键只保留一个确定结果。</p>
     *
     * @param results 结果集合
     * @return 去重后的结果集合
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> deduplicateByBusinessKey(@NonNull List<DemandConfigResult> results) {
        Map<DemandConfigResultBusinessKey, DemandConfigResult> resultMap = new LinkedHashMap<>();
        for (DemandConfigResult result : results) {
            resultMap.put(DemandConfigResultBusinessKey.of(result), result);
        }
        return new ArrayList<>(resultMap.values());
    }

    /**
     * 锁定结果写入范围。
     *
     * <p>每个写入范围同时获取模板级锁和场景级锁。模板级锁用于挡住模板删除这类不带具体场景的清理，
     * 场景级锁用于保留普通刷新边界，避免同模板/场景并发刷新时出现删除和 upsert 交错。</p>
     *
     * @param deleteParams     删除定位参数集合
     * @param upsertParams     新增或更新结果集合
     * @param acquiredLockKeys 已获取的锁
     * @since v7.260701.0
     */
    private void lockWriteScopes(
            @NonNull List<DemandConfigResult> deleteParams,
            @NonNull List<DemandConfigResult> upsertParams,
            @NonNull List<String> acquiredLockKeys) {
        List<String> lockKeys = new ArrayList<>(deleteParams.size() + upsertParams.size());
        for (DemandConfigResult deleteParam : deleteParams) {
            lockKeys.addAll(this.buildWriteScopeLockKeys(deleteParam));
        }
        for (DemandConfigResult upsertParam : upsertParams) {
            lockKeys.addAll(this.buildWriteScopeLockKeys(upsertParam));
        }
        this.lockUnacquiredKeysInOrder(lockKeys, acquiredLockKeys);
    }

    /**
     * 锁定业务键对应的结果写入范围。
     *
     * @param businessKeys      业务键集合
     * @param acquiredLockKeys 已获取的锁
     * @since v7.260701.0
     */
    private void lockWriteScopes(
            @NonNull Collection<DemandConfigResultBusinessKey> businessKeys,
            @NonNull List<String> acquiredLockKeys) {
        List<String> lockKeys = businessKeys.stream()
                .flatMap(businessKey -> this.buildWriteScopeLockKeys(businessKey).stream())
                .collect(Collectors.toList());
        this.lockUnacquiredKeysInOrder(lockKeys, acquiredLockKeys);
    }

    /**
     * 按固定顺序锁定尚未获取的写范围锁。
     *
     * <p>不同入口可能一次锁多个模板/场景写入范围。这里统一去重、排序和跳过已持有锁的逻辑，
     * 保证加锁顺序稳定，避免并发刷新时因为锁顺序不一致产生死锁风险。</p>
     *
     * @param lockKeys         待获取的锁Key集合
     * @param acquiredLockKeys 已获取的锁
     * @since v7.260701.0
     */
    private void lockUnacquiredKeysInOrder(
            @NonNull Collection<String> lockKeys,
            @NonNull List<String> acquiredLockKeys) {
        List<String> sortedLockKeys = lockKeys.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        Set<String> acquiredLockKeySet = new HashSet<>(acquiredLockKeys);
        long lockDeadlineNanos = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(DemandConfigResultRepositoryImpl.WRITE_LOCK_WAIT_MILLIS);
        for (String lockKey : sortedLockKeys) {
            if (acquiredLockKeySet.contains(lockKey)) {
                continue;
            }
            long remainingNanos = lockDeadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                throw this.buildWriteLockTimeoutException(lockKey);
            }
            long remainingMillis = Math.max(1L, TimeUnit.NANOSECONDS.toMillis(remainingNanos));
            boolean locked = this.redissonDistributedLockerService.tryLock(
                    lockKey,
                    TimeUnit.MILLISECONDS,
                    (int) Math.min(remainingMillis, Integer.MAX_VALUE),
                    -1);
            if (!locked) {
                throw this.buildWriteLockTimeoutException(lockKey);
            }
            acquiredLockKeys.add(lockKey);
            acquiredLockKeySet.add(lockKey);
        }
    }

    /**
     * 构造结果写锁获取超时异常。
     *
     * <p>锁Key已包含项目、模板和场景范围，异常向上传递后由刷新入口统一记录，避免仓储层和入口层重复打印日志。</p>
     *
     * @param lockKey 获取失败的锁Key
     * @return 写锁获取超时异常
     * @since v7.260701.0
     */
    private @NonNull BusinessException buildWriteLockTimeoutException(@NonNull String lockKey) {
        return new BusinessException(
                ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
                "需求配置结果写锁获取超时，lockKey=" + lockKey);
    }

    /**
     * 对查询参数中明确的模板/场景写入边界加锁。
     *
     * <p>模板删除、场景删除、无需求空间清理这类入口通常不是按业务键删除，而是按模板/场景范围删除。
     * 这些删除也会关闭快照并修改当前表，必须和 upsert 使用同一粒度的写锁，避免并发刷新时读到
     * “还没提交的删除”或插入重复当前结果。只按模板删除时没有场景条件，会退化为模板级锁。</p>
     *
     * @param param            查询参数
     * @param acquiredLockKeys 已获取的锁
     * @since v7.260701.0
     */
    private void lockConcreteQueryWriteScopes(
            @NonNull DemandConfigResultRepositoryQueryParam param,
            @NonNull List<String> acquiredLockKeys) {
        Long relTemplateId = param.getRelTemplateId();
        if (Objects.isNull(relTemplateId)) {
            return;
        }
        List<DemandConfigResult> writeScopes = new ArrayList<>();
        if (Objects.nonNull(param.getRelSceneId())) {
            writeScopes.add(this.toWriteScope(relTemplateId, param.getRelSceneId()));
        }
        if (CollectionUtils.isNotEmpty(param.getRelSceneIds())) {
            param.getRelSceneIds().stream()
                    .filter(Objects::nonNull)
                    .map(sceneId -> this.toWriteScope(relTemplateId, sceneId))
                    .forEach(writeScopes::add);
        }
        if (Boolean.TRUE.equals(param.getRelSceneIdIsNull())) {
            writeScopes.add(this.toWriteScope(relTemplateId, null));
        }
        if (CollectionUtils.isEmpty(writeScopes)) {
            // 只按模板删除时没有具体场景条件，使用模板级锁挡住同模板所有场景的并发 upsert。
            writeScopes.add(this.toWriteScope(relTemplateId, null));
        }
        if (CollectionUtils.isNotEmpty(writeScopes)) {
            this.lockWriteScopes(writeScopes, Collections.emptyList(), acquiredLockKeys);
        }
    }

    /**
     * 构造用于写锁定位的结果范围对象。
     *
     * @param relTemplateId 关联模板ID
     * @param relSceneId    关联场景ID
     * @return 写锁范围对象
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResult toWriteScope(@NonNull Long relTemplateId, @Nullable Long relSceneId) {
        DemandConfigResult result = new DemandConfigResult();
        result.setProjectId(ContextUtil.getProjectId());
        result.setRelTemplateId(relTemplateId);
        result.setRelSceneId(relSceneId);
        return result;
    }

    /**
     * 构建结果写入范围锁Key。
     *
     * @param result 结果
     * @return 锁Key
     * @since v7.260701.0
     */
    private @NonNull List<String> buildWriteScopeLockKeys(@NonNull DemandConfigResult result) {
        List<String> lockKeys = new ArrayList<>();
        lockKeys.add(this.buildTemplateWriteScopeLockKey(result.getProjectId(), result.getRelTemplateId()));
        lockKeys.add(this.buildSceneWriteScopeLockKey(result.getProjectId(), result.getRelTemplateId(), result.getRelSceneId()));
        return lockKeys;
    }

    /**
     * 构建结果写入模板范围锁Key。
     *
     * @param projectId     项目ID
     * @param relTemplateId 关联模板ID
     * @return 锁Key
     * @since v7.260701.0
     */
    private @NonNull String buildTemplateWriteScopeLockKey(
            @NonNull String projectId,
            @NonNull Long relTemplateId) {
        return this.buildWriteScopeLockKey(projectId, relTemplateId,
                DemandConfigResultRepositoryImpl.ALL_SCENE_LOCK_PART);
    }

    /**
     * 构建结果写入场景范围锁Key。
     *
     * @param projectId     项目ID
     * @param relTemplateId 关联模板ID
     * @param relSceneId    关联场景ID
     * @return 锁Key
     * @since v7.260701.0
     */
    private @NonNull String buildSceneWriteScopeLockKey(
            @NonNull String projectId,
            @NonNull Long relTemplateId,
            @Nullable Long relSceneId) {
        return this.buildWriteScopeLockKey(projectId, relTemplateId,
                Objects.toString(relSceneId, DemandConfigResultRepositoryImpl.NO_SCENE_LOCK_PART));
    }

    /**
     * 构建结果写入范围锁Key。
     *
     * @param projectId     项目ID
     * @param relTemplateId 关联模板ID
     * @param scenePart     场景锁片段
     * @return 锁Key
     * @since v7.260701.0
     */
    private @NonNull String buildWriteScopeLockKey(
            @NonNull String projectId,
            @NonNull Long relTemplateId,
            @NonNull String scenePart) {
        StringJoiner keyJoiner = new StringJoiner(CacheKeyPrefix.SEPARATOR, CacheLockKey.LOCK_KEY_PREFIX, "");
        keyJoiner.add(DemandConfigResultRepositoryImpl.UPSERT_LOCK_BUSINESS);
        keyJoiner.add(projectId);
        keyJoiner.add(String.valueOf(relTemplateId));
        keyJoiner.add(scenePart);
        return keyJoiner.toString();
    }

    /**
     * 构建业务键对应的结果写入范围锁Key。
     *
     * @param businessKey 业务键
     * @return 锁Key集合
     * @since v7.260701.0
     */
    private @NonNull List<String> buildWriteScopeLockKeys(@NonNull DemandConfigResultBusinessKey businessKey) {
        List<String> lockKeys = new ArrayList<>();
        String projectId = ContextUtil.getProjectId();
        lockKeys.add(this.buildTemplateWriteScopeLockKey(projectId, businessKey.getRelTemplateId()));
        lockKeys.add(this.buildSceneWriteScopeLockKey(
                projectId, businessKey.getRelTemplateId(), businessKey.getRelSceneId()));
        return lockKeys;
    }

    /**
     * 事务完成后释放锁。
     *
     * @param acquiredLockKeys 已获取的锁
     * @since v7.260701.0
     */
    private void unlockAfterTransactionCompletion(@NonNull List<String> acquiredLockKeys) {
        List<String> lockKeysToRelease = new ArrayList<>(acquiredLockKeys);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCompletion(int status) {
                // 锁必须覆盖提交/回滚窗口，否则后续事务可能在本事务提交前查不到旧行并插入重复当前结果。
                DemandConfigResultRepositoryImpl.this.unlockReverse(lockKeysToRelease);
            }
        });
    }

    /**
     * 按获取相反顺序释放锁。
     *
     * @param lockKeys 锁Key
     * @since v7.260701.0
     */
    private void unlockReverse(@NonNull List<String> lockKeys) {
        for (int index = lockKeys.size() - 1; index >= 0; index--) {
            this.redissonDistributedLockerService.unlock(lockKeys.get(index));
        }
    }

    /**
     * 按业务键查询已存在的结果。
     *
     * <p>业务键在 mapper 中会被展开为多组 OR 条件。初始化量级达到一万对象时，必须分片查询，
     * 避免单条 SQL 过大导致数据库解析慢或网络包过大。</p>
     *
     * @param businessKeys 业务键集合
     * @return 业务键 -> 已存在结果
     * @since v7.260701.0
     */
    private @NonNull Map<DemandConfigResultBusinessKey, DemandConfigResult> mapExistsResultsByBusinessKeys(
            @NonNull List<DemandConfigResultBusinessKey> businessKeys) {
        Map<DemandConfigResultBusinessKey, DemandConfigResult> result = new LinkedHashMap<>();
        for (List<DemandConfigResultBusinessKey> businessKeyPartition : Lists.partition(
                businessKeys, DemandConfigResultRepositoryImpl.BUSINESS_KEY_QUERY_BATCH_SIZE)) {
            result.putAll(this.mapExistsResultsByBusinessKeyPartition(businessKeyPartition));
        }
        return result;
    }

    /**
     * 查询单个业务键分片对应的旧结果。
     *
     * @param businessKeys 业务键分片
     * @return 业务键 -> 已存在结果
     * @since v7.260701.0
     */
    private @NonNull Map<DemandConfigResultBusinessKey, DemandConfigResult> mapExistsResultsByBusinessKeyPartition(
            @NonNull List<DemandConfigResultBusinessKey> businessKeys) {
        DemandConfigResultRepositoryQueryParam queryParam = new DemandConfigResultRepositoryQueryParam();
        queryParam.setBusinessKeys(businessKeys);
        this.addExistingResultColumns(queryParam);
        return this.mapResultsByBusinessKey(this.demandConfigResultService.list(queryParam));
    }

    /**
     * 添加旧结果查询需要的字段。
     *
     * @param queryParam 查询参数
     * @since v7.260701.0
     */
    private void addExistingResultColumns(@NonNull DemandConfigResultRepositoryQueryParam queryParam) {
        // 判断新增/更新只需要旧记录ID、创建审计字段和业务键，不能默认select *把JSON大字段带出来。
        queryParam.addColumns(BaseAssignIdDO::getId,
                BaseAssignIdDO::getCreateTime,
                BaseAssignIdDO::getCreateUser,
                DemandConfigResult::getDemandTemplateType,
                DemandConfigResult::getDemandInstanceType,
                DemandConfigResult::getDemandInstanceId,
                DemandConfigResult::getRelTemplateId,
                DemandConfigResult::getRelSceneId);
    }

    /**
     * 按业务键映射结果。
     *
     * @param results 结果集合
     * @return 业务键到结果的映射
     * @since v7.260701.0
     */
    private @NonNull Map<DemandConfigResultBusinessKey, DemandConfigResult> mapResultsByBusinessKey(
            @NonNull Collection<DemandConfigResult> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        DemandConfigResultBusinessKey::of,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    /**
     * 填充默认值
     *
     * @param result 预计算结果
     * @since v7.260701.0
     */
    private void fillDefaultValue(@NonNull DemandConfigResult result) {
        if (StringUtils.isBlank(result.getProjectId())) {
            result.setProjectId(ContextUtil.getProjectId());
        }
    }

    @Override
    public @NonNull String logTag() {
        return "需求管理-需求配置预计算结果仓储";
    }
}
