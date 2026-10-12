package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.model.entity.cache.CacheInfoCodeTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.CacheInfoCodeTemplateService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheInfoCodeTemplateQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.support.CacheBatchExecutor;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheTemplateRebuildService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 缓存-信息点模版仓储实现
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheInfoCodeTemplateRepositoryImpl implements CacheInfoCodeTemplateRepository {

    private final InfoCodeGateway infoCodeGateway;
    private final CacheInfoCodeTemplateService cacheInfoCodeTemplateService;

    /**
     * 模板缓存同步重建服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheTemplateRebuildService templateRebuildService;

    /**
     * Redisson分布式锁服务, 用于项目级缓存重建锁。
     *
     * @since v7.260802.0
     */
    private final RedissonDistributedLockerService redissonDistributedLockerService;

    @Override
    public Optional<CacheInfoCodeTemplate> getByKeyOpt(@NonNull String key) {
        CacheInfoCodeTemplateQueryDaoParam queryDaoParam = new CacheInfoCodeTemplateQueryDaoParam();
        queryDaoParam.setKey(key);
        return this.list(queryDaoParam).stream().findFirst();
    }

    @Override
    public List<CacheInfoCodeTemplate> list(@NonNull CacheInfoCodeTemplateQueryDaoParam queryDaoParam) {
        return this.cacheInfoCodeTemplateService.lambdaQuery()
                .eq(CacheInfoCodeTemplate::getProjectId, ContextUtil.getProjectId())
                .eq(queryDaoParam.getId() != null,
                        CacheInfoCodeTemplate::getId, queryDaoParam.getId())
                .in(CollectionUtils.isNotEmpty(queryDaoParam.getIds()),
                        CacheInfoCodeTemplate::getId, queryDaoParam.getIds())
                .eq(queryDaoParam.getRunningMonitorType() != null,
                        CacheInfoCodeTemplate::getRunningMonitorType, queryDaoParam.getRunningMonitorType())
                .in(CollectionUtils.isNotEmpty(queryDaoParam.getRunningMonitorTypes()),
                        CacheInfoCodeTemplate::getRunningMonitorType, queryDaoParam.getRunningMonitorTypes())
                .eq(queryDaoParam.getRunningMonitorSecondType() != null,
                        CacheInfoCodeTemplate::getRunningMonitorSecondType, queryDaoParam.getRunningMonitorSecondType())
                .in(CollectionUtils.isNotEmpty(queryDaoParam.getRunningMonitorSecondTypes()),
                        CacheInfoCodeTemplate::getRunningMonitorSecondType, queryDaoParam.getRunningMonitorSecondTypes())
                .eq(queryDaoParam.getKey() != null,
                        CacheInfoCodeTemplate::getCustomKey, queryDaoParam.getKey())
                .in(CollectionUtils.isNotEmpty(queryDaoParam.getKeys()),
                        CacheInfoCodeTemplate::getCustomKey, queryDaoParam.getKeys())
                .eq(queryDaoParam.getRelTemplateId() != null,
                        CacheInfoCodeTemplate::getRelTemplateId, queryDaoParam.getRelTemplateId())
                .in(CollectionUtils.isNotEmpty(queryDaoParam.getRelTemplateIds()),
                        CacheInfoCodeTemplate::getRelTemplateId, queryDaoParam.getRelTemplateIds())
                .list();
    }

    @Override
    public @NonNull List<String> listKeysByRunningMonitorType(
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        CacheInfoCodeTemplateQueryDaoParam queryDaoParam =
                new CacheInfoCodeTemplateQueryDaoParam();
        queryDaoParam.setRunningMonitorType(runningMonitorType);
        queryDaoParam.addColumns(CacheInfoCodeTemplate::getCustomKey);
        return this.list(queryDaoParam).stream()
                .map(CacheInfoCodeTemplate::getCustomKey)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void add(@NonNull Long templateTypeId) {
        // 先查模板类型获取运行监测类型（锁外查询），再用按类型锁，避免同类型并发写入。
        RunningMonitorTypeEnum type = this.queryTemplateType(templateTypeId);
        if (type == null) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(),
                    "模板类型ID对应的运行监测类型不存在: " + templateTypeId);
        }
        this.executeWithProjectLock(CacheLockKey.CacheRebuild.byType(type),
                () -> this.addInternal(templateTypeId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(@NonNull Collection<Long> templateIds) {
        if (CollectionUtils.isEmpty(templateIds)) {
            return;
        }
        // 源模板删除后无法反查旧键，因此必须在删除模板元数据前捕获完整缓存键范围。
        Set<String> templateKeys = new HashSet<>();
        CacheBatchExecutor.execute(templateIds, partition -> {
            CacheInfoCodeTemplateQueryDaoParam queryDaoParam =
                    new CacheInfoCodeTemplateQueryDaoParam();
            queryDaoParam.setRelTemplateIds(partition);
            queryDaoParam.addColumns(CacheInfoCodeTemplate::getCustomKey);
            templateKeys.addAll(this.list(queryDaoParam).stream()
                    .map(CacheInfoCodeTemplate::getCustomKey)
                    .collect(Collectors.toSet()));
        });
        if (CollectionUtils.isEmpty(templateKeys)) {
            return;
        }
        // Key 是缓存范围的唯一标识；删除动作必须在元数据仍可用于内部路由时完成。
        this.templateRebuildService.removeTemplateKeys(templateKeys);
        CacheBatchExecutor.execute(templateKeys, partition ->
                this.cacheInfoCodeTemplateService.lambdaUpdate()
                        .eq(CacheInfoCodeTemplate::getProjectId, ContextUtil.getProjectId())
                        .in(CacheInfoCodeTemplate::getCustomKey, partition)
                        .remove());
    }

    @Override
    public void rebuildByParam(@NonNull InfoCodeTemplateTypeQueryDaoParam queryDaoParam) {
        // 重建链路"锁外准备 → 锁内删除/插入"必须按类型串行化写入，覆盖全部并发触发源。
        RunningMonitorTypeEnum type = queryDaoParam.getRunningMonitorType();
        if (type == null) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(),
                    "模板查询参数缺少运行监测类型");
        }
        boolean fullRunningMonitorScope = this.isFullRunningMonitorScope(queryDaoParam);
        // 锁外阶段一：快照元数据键集合，锁内重查不一致时说明并发模板增删发生，放弃本轮。
        // 全量范围快照可独立于源查询，必须提前到源查询之前，闭合"源查询定版 → 快照"之间
        // 的并发窗口，防止该窗口内提交的模板增删被复核漏检后按失效键误删。
        Set<String> fullScopeSnapshot = fullRunningMonitorScope
                ? this.listPreviousKeys(queryDaoParam, true, null)
                : null;
        // 锁外阶段二：源模板查询与允许缓存过滤（含三方依赖，可能耗时数小时，不能持有保存锁）。
        List<InfoCodeTemplateTypeModel> templateTypeModels =
                this.listAllowCacheTemplateTypes(queryDaoParam);
        if (CollectionUtils.isEmpty(templateTypeModels) && !fullRunningMonitorScope) {
            return;
        }
        // 锁外阶段三：窄范围旧键必须以源查询返回的目标模板 ID 定位，快照只能在源查询后；
        // 其 replacedKeys 限定目标模板，并发新增模板键不在范围内，无漏检误删危害。
        final Set<String> previousKeysAtPrepare = fullRunningMonitorScope
                ? fullScopeSnapshot
                : this.listPreviousKeys(queryDaoParam, false, templateTypeModels);
        // 锁外阶段四：模板对象与关系远程准备。
        List<CacheTemplateRebuildService.PreparedTemplateWrite> preparedWrites =
                this.templateRebuildService.prepareTemplates(templateTypeModels);
        // 锁内阶段：只保留本地 DB 查询与键替换（等锁时间由 CacheLockKey 统一承载）。
        this.executeWithProjectLock(CacheLockKey.CacheRebuild.byType(type),
                () -> this.rebuildByParamLocked(queryDaoParam, fullRunningMonitorScope,
                        templateTypeModels, previousKeysAtPrepare, preparedWrites));
    }

    /**
     * 在指定缓存锁内执行模板缓存写入。
     *
     * <p>按运行监测类型粒度串行化模板缓存写入。当前线程已持有该锁时（重建链路内再次进入）直接执行。</p>
     *
     * @param lockKey 缓存锁 Key
     * @param action  锁内写入动作
     * @since v7.260802.0
     */
    private void executeWithProjectLock(
            CacheLockKey.Key lockKey, @NonNull Runnable action) {
        // 等锁时间取 Key 上的统一等待值（CacheRebuild.byType 为 30 分钟），
        // 避免各调用入口各自定义造成时间散落。
        this.redissonDistributedLockerService.executeWithLock(
                lockKey.getKey(),
                lockKey.getExpire().getSeconds(),
                action);
    }

    /**
     * 在项目缓存锁内新增模板元数据。
     *
     * @param templateTypeId 模版类型ID
     * @since v7.260802.0
     */
    private void addInternal(@NonNull Long templateTypeId) {
        InfoCodeTemplateTypeQueryDaoParam queryDaoParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryDaoParam.setTemplateTypeId(templateTypeId);
        List<InfoCodeTemplateTypeModel> templateTypeModels = this.infoCodeGateway.listTemplateTypes(queryDaoParam);
        if (CollectionUtils.isEmpty(templateTypeModels)) {
            return;
        }
        List<CacheInfoCodeTemplate> cacheInfoCodeTemplates = templateTypeModels.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                InfoCodeTemplateTypeModel::getTemplateTypeKey,
                                CacheInfoCodeTemplate::from,
                                (first, ignored) -> first,
                                LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())));
        // 先用资源服务的短事务提交模板元数据；返回后再执行事务外 RWD 准备和单 Key 写入。
        this.cacheInfoCodeTemplateService.insertBatch(cacheInfoCodeTemplates);
        this.templateRebuildService.replaceTemplates(templateTypeModels);
    }

    /**
     * 在项目缓存锁内提交模板重建的本地写入。
     *
     * <p>锁内只包含本地 DB 查询与键替换，远程准备已由调用方在锁外完成。</p>
     *
     * @param queryDaoParam          模板查询参数
     * @param fullRunningMonitorScope 是否为单一级类型完整范围
     * @param templateTypeModels     锁外准备时的目标模板
     * @param previousKeysAtPrepare  锁外准备时刻的元数据键集合快照
     * @param preparedWrites         锁外准备的模板写入
     * @since v7.260901.0
     */
    private void rebuildByParamLocked(
            @NonNull InfoCodeTemplateTypeQueryDaoParam queryDaoParam,
            boolean fullRunningMonitorScope,
            @NonNull List<InfoCodeTemplateTypeModel> templateTypeModels,
            @NonNull Set<String> previousKeysAtPrepare,
            @NonNull List<CacheTemplateRebuildService.PreparedTemplateWrite> preparedWrites) {
        // 必须先捕获当前数据库中的旧键，再计算目标和失效键；源模板可能已被删除，
        // 不能依赖新模板 ID 反推上一版缓存范围。
        Set<String> previousKeys =
                this.listPreviousKeys(queryDaoParam, fullRunningMonitorScope, templateTypeModels);
        if (!previousKeys.equals(previousKeysAtPrepare)) {
            // 准备期间发生并发模板新增/删除（add/del 事件），快照已过期：放弃本次写入，
            // 避免误删新键或用旧快照覆盖新配置；并发入口自身已完成维护，下一轮重建自愈。
            this.logProjectInstance().warn("模板重建放弃本次写入，准备期间模板键集合发生变化",
                    Arrays.asList(
                            Pair.of("previousKeysAtPrepareSize", previousKeysAtPrepare.size()),
                            Pair.of("currentPreviousKeysSize", previousKeys.size())));
            return;
        }
        Set<String> targetKeys = templateTypeModels.stream()
                .map(InfoCodeTemplateTypeModel::getTemplateTypeKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> staleKeys = new LinkedHashSet<>(previousKeys);
        staleKeys.removeAll(targetKeys);

        // 全部远程准备成功后才逐键替换；失效键在所有目标键成功后清理。
        this.templateRebuildService.executePreparedTemplates(preparedWrites, staleKeys);

        Set<String> replacedKeys = new LinkedHashSet<>(previousKeys);
        replacedKeys.addAll(targetKeys);
        // Key 是项目内唯一范围标识；同一批次出现重复键时保留首个，防止单批插入重复记录。
        List<CacheInfoCodeTemplate> targetTemplates = templateTypeModels.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                InfoCodeTemplateTypeModel::getTemplateTypeKey,
                                CacheInfoCodeTemplate::from,
                                (first, ignored) -> first,
                                LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())));
        this.cacheInfoCodeTemplateService.replaceByCacheKeys(replacedKeys, targetTemplates);
    }

    /**
     * 按查询参数取得允许对象缓存的源模板。
     *
     * @param queryDaoParam 模板查询参数
     * @return 允许缓存的源模板列表
     * @since v7.260901.0
     */
    private @NonNull List<InfoCodeTemplateTypeModel> listAllowCacheTemplateTypes(
            @NonNull InfoCodeTemplateTypeQueryDaoParam queryDaoParam) {
        return this.infoCodeGateway.listTemplateTypes(queryDaoParam).stream()
                .filter(InfoCodeTemplateTypeModel::allowCache)
                .collect(Collectors.toList());
    }

    /**
     * 查询当前模板元数据中的缓存键范围。
     *
     * @param queryDaoParam          模板查询参数
     * @param fullRunningMonitorScope 是否为单一级类型完整范围
     * @param templateTypeModels     目标模板（仅窄范围时用于定位模板 ID，全量范围可传 null）
     * @return 当前元数据缓存键集合
     * @since v7.260901.0
     */
    private @NonNull Set<String> listPreviousKeys(
            @NonNull InfoCodeTemplateTypeQueryDaoParam queryDaoParam,
            boolean fullRunningMonitorScope,
            @Nullable List<InfoCodeTemplateTypeModel> templateTypeModels) {
        CacheInfoCodeTemplateQueryDaoParam existingParam =
                new CacheInfoCodeTemplateQueryDaoParam();
        if (fullRunningMonitorScope) {
            existingParam.setRunningMonitorType(queryDaoParam.getRunningMonitorType());
            existingParam.setRunningMonitorSecondType(queryDaoParam.getRunningMonitorSecondType());
        } else {
            Set<Long> templateTypeIds = templateTypeModels.stream()
                    .map(InfoCodeTemplateTypeModel::getTemplateTypeId)
                    .collect(Collectors.toSet());
            existingParam.setRelTemplateIds(templateTypeIds);
        }
        existingParam.addColumns(CacheInfoCodeTemplate::getCustomKey);
        return this.list(existingParam).stream()
                .map(CacheInfoCodeTemplate::getCustomKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 判断重建参数是否覆盖单个一级类型、可选单个二级类型的完整模板范围。
     *
     * <p>只有该范围能够在目标模板为空时安全地清理全部旧键；导入场景还带有模板类型或
     * 设备类筛选，旧模板可能已从源表删除，无法仅凭当前返回模型推导其缓存键，必须保留
     * 窄范围的旧数据并由导入删除流程按模板 ID 精确清理。</p>
     *
     * @param queryDaoParam 模板查询参数
     * @return 是否为单一级类型下的完整模板范围
     * @since v7.260701.0
     */
    private boolean isFullRunningMonitorScope(
            @NonNull InfoCodeTemplateTypeQueryDaoParam queryDaoParam) {
        return queryDaoParam.getRunningMonitorType() != null
                && CollectionUtils.isEmpty(queryDaoParam.getRunningMonitorTypes())
                && CollectionUtils.isEmpty(queryDaoParam.getRunningMonitorSecondTypes())
                && queryDaoParam.getTemplateTypeId() == null
                && CollectionUtils.isEmpty(queryDaoParam.getTemplateTypeIds())
                && queryDaoParam.getTemplateType() == null
                && CollectionUtils.isEmpty(queryDaoParam.getTemplateTypes())
                && queryDaoParam.getObjectType() == null
                && CollectionUtils.isEmpty(queryDaoParam.getObjectTypes())
                && StringUtils.isBlank(queryDaoParam.getClassCode())
                && CollectionUtils.isEmpty(queryDaoParam.getClassCodes())
                && CollectionUtils.isEmpty(queryDaoParam.getExcludeClassCodes());
    }

    /**
     * 查询模板类型对应的运行监测类型。
     *
     * @param templateTypeId 模板类型ID
     * @return 运行监测类型，查不到时返回 null
     * @since v7.260802.0
     */
    private @Nullable RunningMonitorTypeEnum queryTemplateType(@NonNull Long templateTypeId) {
        InfoCodeTemplateTypeQueryDaoParam queryDaoParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryDaoParam.setTemplateTypeId(templateTypeId);
        List<InfoCodeTemplateTypeModel> types = this.infoCodeGateway.listTemplateTypes(queryDaoParam);
        return CollectionUtils.isEmpty(types) ? null : types.get(0).getRunningMonitorType();
    }

    @Override
    public String logTag() {
        return "缓存-信息点模版仓储实现";
    }
}
