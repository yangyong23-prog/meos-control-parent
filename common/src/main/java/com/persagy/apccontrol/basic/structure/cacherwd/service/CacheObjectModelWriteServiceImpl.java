package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelReplaceScope;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

/**
 * 云端缓存对象模型远程无关的短事务提交实现。
 *
 * <p>该服务不访问 RWD，也不推断关系范围。对象入口在事务前一次投影完整快照；
 * 关系入口只提交调用方已经准备好的完整替换范围。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheObjectModelWriteServiceImpl implements CacheObjectModelWriteService, LogComponent {

    /**
     * 新对象主表与 Key 关联仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    /**
     * 新模型视图仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelViewRepository cacheObjectModelViewRepository;

    /**
     * 新对象关系仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRelRepository cacheObjectRelRepository;

    /**
     * 对象记录转换器。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRecordConverter cacheObjectRecordConverter;

    /**
     * 模型视图记录转换器。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelViewRecordConverter
            cacheObjectModelViewRecordConverter;

    /**
     * 当前事务管理器。
     *
     * @since v7.260701.0
     */
    private final @NonNull PlatformTransactionManager transactionManager;

    /**
     * 对象模型项目级保存锁服务。
     *
     * @since v7.260802.0
     */
    private final @NonNull RedissonDistributedLockerService redissonDistributedLockerService;

    @Override
    public @NonNull String logTag() {
        return "CacheObjectModelWriteServiceImpl";
    }

    @Override
    public boolean replaceModelCacheKey(
            @NonNull String modelCacheKey,
            @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects) {
        return this.replaceModelCacheKey(
                modelCacheKey, sourceObjects, Collections.emptyList());
    }

    @Override
    public boolean replaceModelCacheKey(
            @NonNull String modelCacheKey,
            @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects,
            @NonNull Collection<CacheWriteAction> writeActions) {
        return this.replaceModelCacheKey(
                modelCacheKey, sourceObjects, writeActions, () -> true);
    }

    @Override
    public boolean replaceModelCacheKey(
            @NonNull String modelCacheKey,
            @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects,
            @NonNull Collection<CacheWriteAction> writeActions,
            @NonNull BooleanSupplier afterReplaceAction) {
        return this.replaceModelCacheKeyInternal(
                modelCacheKey, sourceObjects, writeActions, afterReplaceAction);
    }

    /**
     * 替换单个模型缓存键的完整对象快照。
     *
     * @param modelCacheKey     模型缓存键
     * @param sourceObjects     当前缓存键的完整对象快照
     * @param writeActions      已准备的本地写入动作
     * @param afterReplaceAction 替换完成后的追加动作
     * @return 是否完成替换并提交
     * @since v7.260802.0
     */
    private boolean replaceModelCacheKeyInternal(
            @NonNull String modelCacheKey,
            @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects,
            @NonNull Collection<CacheWriteAction> writeActions,
            @NonNull BooleanSupplier afterReplaceAction) {
        PreparedObjectRows preparedRows = this.prepareObjectRows(sourceObjects);
        boolean replaced = this.executeWithObjectModelWriteLock(() -> {
            this.cacheObjectRepository.upsertObjects(preparedRows.objects);
            this.cacheObjectModelViewRepository.upsertModelViews(preparedRows.modelViews);
            this.cacheObjectRepository.replaceObjectIds(
                    modelCacheKey, preparedRows.objectIds);
            this.executeWriteActions(writeActions);
            return afterReplaceAction.getAsBoolean();
        });
        if (!replaced) {
            this.logProjectInstance().error("缓存对象模型 Key 替换未提交",
                    Pair.of("modelCacheKey", modelCacheKey));
        }
        return replaced;
    }

    @Override
    public boolean replaceModelCacheKeys(
            @NonNull Map<String, ? extends Collection<? extends DtpRwdObjectDTO>>
                    sourceObjectsByKey,
            @NonNull Collection<CacheWriteAction> writeActions) {
        Map<String, PreparedObjectRows> preparedRowsByKey =
                this.prepareAllObjectRows(sourceObjectsByKey);
        boolean replaced = this.executeWithObjectModelWriteLock(() -> {
            this.upsertAllKeys(preparedRowsByKey);
            this.executeWriteActions(writeActions);
            return true;
        });
        if (!replaced) {
            this.logProjectInstance().error("缓存对象模型多 Key 替换未提交",
                    Pair.of("modelCacheKeys", preparedRowsByKey.keySet()));
        }
        return replaced;
    }

    @Override
    public boolean replaceModelCacheKeysAndRelations(
            @NonNull Map<String, ? extends Collection<? extends DtpRwdObjectDTO>> sourceObjectsByKey,
            @NonNull Collection<CacheWriteAction> writeActions,
            @NonNull Map<DtpRwdRelationTypeEnum, ? extends Collection<? extends DtpRwdObjectDTO>> relationsByType,
            @NonNull BooleanSupplier afterReplaceAction) {
        Map<String, PreparedObjectRows> preparedRowsByKey =
                this.prepareAllObjectRows(sourceObjectsByKey);
        Map<DtpRwdRelationTypeEnum, List<CacheObjectRelReplaceScope>> relationScopesByType =
                this.prepareAllRelationScopes(relationsByType);

        boolean replaced = this.executeWithObjectModelWriteLock(() -> {
            this.upsertAllKeys(preparedRowsByKey);
            this.replaceAllRelations(relationScopesByType);
            this.executeWriteActions(writeActions);
            return afterReplaceAction.getAsBoolean();
        });
        if (!replaced) {
            this.logProjectInstance().error("缓存对象模型多 Key 含关系替换未提交",
                    Pair.of("modelCacheKeys", preparedRowsByKey.keySet()));
        }
        return replaced;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 批量准备多个缓存 Key 的对象投影。
     *
     * @param sourceObjectsByKey 缓存 Key 到完整对象快照的映射
     * @return 缓存 Key 到预处理对象行的映射
     * @since v7.260802.0
     */
    private @NonNull Map<String, PreparedObjectRows> prepareAllObjectRows(
            @NonNull Map<String, ? extends Collection<? extends DtpRwdObjectDTO>> sourceObjectsByKey) {
        Map<String, PreparedObjectRows> preparedRowsByKey = new LinkedHashMap<>();
        for (Map.Entry<String, ? extends Collection<? extends DtpRwdObjectDTO>> entry
                : sourceObjectsByKey.entrySet()) {
            if (StringUtils.isBlank(entry.getKey())) {
                throw CacheObjectModelExceptionFactory.requiredValue("modelCacheKey");
            }
            preparedRowsByKey.put(entry.getKey(), this.prepareObjectRows(entry.getValue()));
        }
        return preparedRowsByKey;
    }

    /**
     * 批量准备多个关系类型的关系替换范围。
     *
     * @param relationsByType 关系类型到完整关系对象快照的映射
     * @return 关系类型到关系替换范围的映射
     * @since v7.260802.0
     */
    private @NonNull Map<DtpRwdRelationTypeEnum, List<CacheObjectRelReplaceScope>> prepareAllRelationScopes(
            @NonNull Map<DtpRwdRelationTypeEnum, ? extends Collection<? extends DtpRwdObjectDTO>> relationsByType) {
        Map<DtpRwdRelationTypeEnum, List<CacheObjectRelReplaceScope>> relationScopesByType =
                new LinkedHashMap<>();
        for (Map.Entry<DtpRwdRelationTypeEnum, ? extends Collection<? extends DtpRwdObjectDTO>> entry
                : relationsByType.entrySet()) {
            relationScopesByType.put(entry.getKey(),
                    this.toRelationScopes(entry.getKey(), entry.getValue()));
        }
        return relationScopesByType;
    }

    /**
     * 在事务内逐 Key UPSERT 对象、模型视图和键关联。
     *
     * @param preparedRowsByKey 缓存 Key 到预处理对象行的映射
     * @since v7.260802.0
     */
    private void upsertAllKeys(
            @NonNull Map<String, PreparedObjectRows> preparedRowsByKey) {
        for (Map.Entry<String, PreparedObjectRows> entry : preparedRowsByKey.entrySet()) {
            PreparedObjectRows preparedRows = entry.getValue();
            this.cacheObjectRepository.upsertObjects(preparedRows.objects);
            this.cacheObjectModelViewRepository.upsertModelViews(preparedRows.modelViews);
            this.cacheObjectRepository.replaceObjectIds(
                    entry.getKey(), preparedRows.objectIds);
        }
    }

    /**
     * 在事务内替换所有关系类型的完整关系快照。
     *
     * @param relationScopesByType 关系类型到关系替换范围的映射
     * @since v7.260802.0
     */
    private void replaceAllRelations(
            @NonNull Map<DtpRwdRelationTypeEnum, List<CacheObjectRelReplaceScope>> relationScopesByType) {
        for (Map.Entry<DtpRwdRelationTypeEnum, List<CacheObjectRelReplaceScope>> entry
                : relationScopesByType.entrySet()) {
            List<CacheObjectRelReplaceScope> scopes = entry.getValue();
            if (!scopes.isEmpty()) {
                this.cacheObjectRelRepository.replaceRelations(scopes);
            }
        }
    }

    @Override
    public void replaceObjectRelations(
            @NonNull DtpRwdRelationTypeEnum relationType,
            @NonNull Collection<? extends DtpRwdObjectDTO> relationObjects) {
        // 关系写入已由外层调用方提供锁保护（如 CacheRwdBuild 或 CacheRebuild.byType），
        // 此处不再重复加锁。
        this.replaceObjectRelationsInternal(relationType, relationObjects);
    }

    /**
     * 替换指定关系类型的完整关系快照。
     *
     * @param relationType    关系类型
     * @param relationObjects 当前关系类型的完整对象快照
     * @since v7.260802.0
     */
    private void replaceObjectRelationsInternal(
            @NonNull DtpRwdRelationTypeEnum relationType,
            @NonNull Collection<? extends DtpRwdObjectDTO> relationObjects) {
        List<CacheObjectRelReplaceScope> relationScopes =
                this.toRelationScopes(relationType, relationObjects);
        if (relationScopes.isEmpty()) {
            return;
        }
        this.executeTransaction(() -> {
            this.cacheObjectRelRepository.replaceRelations(relationScopes);
            return true;
        });
    }

    @Override
    public @NonNull CacheWriteAction prepareObjectRelationsReplaceAction(
            @NonNull DtpRwdRelationTypeEnum relationType,
            @NonNull Collection<? extends DtpRwdObjectDTO> relationObjects) {
        List<CacheObjectRelReplaceScope> relationScopes =
                this.toRelationScopes(relationType, relationObjects);
        if (relationScopes.isEmpty()) {
            return () -> {
            };
        }
        // 空目标范围必须原样提交，资源层会把它解释为清理该关系范围。
        return () -> this.cacheObjectRelRepository.replaceRelations(relationScopes);
    }

    @Override
    public boolean replaceObjectClassCodes(
            @NonNull Collection<String> classCodes,
            @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects) {
        if (CollectionUtils.isEmpty(classCodes)) {
            throw CacheObjectModelExceptionFactory.requiredValue("classCodes");
        }
        Set<String> normalizedClassCodes = new LinkedHashSet<>(classCodes);
        if (normalizedClassCodes.stream().anyMatch(StringUtils::isBlank)) {
            throw CacheObjectModelExceptionFactory.requiredValue("classCode");
        }
        PreparedObjectRows preparedRows = this.prepareObjectRows(sourceObjects);
        if (preparedRows.objects.stream()
                .anyMatch(object -> !normalizedClassCodes.contains(object.getClassCode()))) {
            throw CacheObjectModelExceptionFactory.invalidStructure("classCode");
        }
        return this.executeWithObjectModelWriteLock(() -> {
            this.cacheObjectRepository.removeObjectsByClassCodes(normalizedClassCodes);
            this.cacheObjectModelViewRepository.removeModelViewsByClassCodes(normalizedClassCodes);
            this.cacheObjectRepository.upsertObjects(preparedRows.objects);
            this.cacheObjectModelViewRepository.upsertModelViews(preparedRows.modelViews);
            return true;
        });
    }

    /**
     * 在事务开始前把完整 RWD 快照投影为对象和模型视图记录。
     *
     * <p>每个源对象只序列化一次，同一字段 Map 同时交给两个投影转换器。</p>
     *
     * @param sourceObjects 当前缓存 Key 的完整对象快照
     * @return 已完成校验和投影的本地记录
     * @throws BusinessException RWD 对象无法转换时抛出
     * @since v7.260701.0
     */
    private @NonNull PreparedObjectRows prepareObjectRows(
            @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects) {
        List<CacheObject> objects = new ArrayList<>(sourceObjects.size());
        List<CacheObjectModelView> modelViews = new ArrayList<>(sourceObjects.size());
        for (DtpRwdObjectDTO sourceObject : sourceObjects) {
            String objectJson = ObjectMapperUtils.writeValueAsStringFailSafe(sourceObject);
            if (StringUtils.isBlank(objectJson)) {
                throw CacheObjectModelExceptionFactory.invalidStructure("rwdObject");
            }
            Map<String, Object> actualFields = ObjectMapperUtils.readMapFailSafe(objectJson);
            if (actualFields == null) {
                throw CacheObjectModelExceptionFactory.invalidStructure("rwdObject");
            }
            objects.add(this.cacheObjectRecordConverter.toCacheObject(actualFields));
            modelViews.add(this.cacheObjectModelViewRecordConverter.toModelView(actualFields));
        }
        List<String> objectIds = objects.stream()
                .map(CacheObject::getId)
                .collect(Collectors.toList());
        return new PreparedObjectRows(objectIds, objects, modelViews);
    }

    /**
     * 在事务开始前校验关系 DTO，并转换为资源层替换范围。
     *
     * <p>同一关系类型下每个主体只能出现一次，因为每个 DTO 都声明该主体的完整目标集合；
     * 重复主体无法判定哪一份是权威快照，必须在删除旧关系前拒绝。</p>
     *
     * @param relationType 关系类型
     * @param relationObjects 调用方准备的完整关系对象快照
     * @return 已校验的资源层替换范围
     * @throws BusinessException 主体、目标或完整范围冲突时抛出
     * @since v7.260701.0
     */
    private @NonNull List<CacheObjectRelReplaceScope> toRelationScopes(
            @NonNull DtpRwdRelationTypeEnum relationType,
            @NonNull Collection<? extends DtpRwdObjectDTO> relationObjects) {
        if (CollectionUtils.isEmpty(relationObjects)) {
            return Collections.emptyList();
        }
        List<CacheObjectRelReplaceScope> result = new ArrayList<>(relationObjects.size());
        Set<String> subjectIds = new LinkedHashSet<>();
        for (DtpRwdObjectDTO relationObject : relationObjects) {
            if (relationObject == null || StringUtils.isBlank(relationObject.getId())) {
                throw CacheObjectModelExceptionFactory.requiredValue("relationObject.id");
            }
            if (!subjectIds.add(relationObject.getId())) {
                throw CacheObjectModelExceptionFactory.invalidStructure(
                        "relationObject.scope");
            }
            List<String> targetIds = relationObject.getRelObjIds() == null
                    ? Collections.emptyList()
                    : new ArrayList<>(relationObject.getRelObjIds());
            if (targetIds.stream().anyMatch(StringUtils::isBlank)) {
                throw CacheObjectModelExceptionFactory.requiredValue(
                        "relationObject.relObjId");
            }
            result.add(new CacheObjectRelReplaceScope(
                    relationObject.getId(),
                    relationType.value().getLeft(),
                    relationType.value().getRight(),
                    targetIds));
        }
        return result;
    }

    @Override
    public void removeCacheKeys(@NonNull Collection<String> cacheKeys) {
        this.removeCacheKeys(cacheKeys, Collections.emptyList());
    }

    @Override
    public void removeCacheKeys(
            @NonNull Collection<String> cacheKeys,
            @NonNull Collection<CacheWriteAction> writeActions) {
        if (CollectionUtils.isEmpty(cacheKeys)) {
            return;
        }
        // 新版 Key 没有主表所有权，删除配置时只移除关联和调用方明确拥有的扩展范围。
        this.executeTransaction(() -> {
            this.cacheObjectRepository.removeObjectKeyRelations(cacheKeys);
            this.executeWriteActions(writeActions);
            return true;
        });
    }

    @Override
    public boolean removeCacheKeys(
            @NonNull Collection<String> cacheKeys,
            @NonNull BooleanSupplier beforeRemoveAction) {
        if (CollectionUtils.isEmpty(cacheKeys)) {
            return true;
        }
        return this.executeTransaction(() -> {
            if (!beforeRemoveAction.getAsBoolean()) {
                return false;
            }
            this.cacheObjectRepository.removeObjectKeyRelations(cacheKeys);
            return true;
        });
    }

    /**
     * 按调用方给定顺序执行已准备的本地扩展动作。
     *
     * @param writeActions 已准备的有序本地写入动作
     * @since v7.260701.0
     */
    private void executeWriteActions(
            @NonNull Collection<CacheWriteAction> writeActions) {
        for (CacheWriteAction writeAction : writeActions) {
            writeAction.execute();
        }
    }

    /**
     * 执行短事务。SQL、连接和事务错误继续上抛，返回 {@code false} 时显式回滚。
     *
     * @param callback 本地事务操作
     * @return 是否完成事务操作并提交
     * @since v7.260701.0
     */
    private boolean executeTransaction(@NonNull BooleanSupplier callback) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
        Boolean completed = transactionTemplate.execute(status -> {
            boolean callbackCompleted = callback.getAsBoolean();
            if (!callbackCompleted) {
                status.setRollbackOnly();
            }
            return callbackCompleted;
        });
        return Boolean.TRUE.equals(completed);
    }

    /**
     * 在项目级保存锁内完成一次数据库事务，锁等待超时最多重试两次。
     *
     * @param callback 本地事务动作
     * @return 是否提交成功
     * @since v7.260802.0
     */
    private boolean executeWithObjectModelWriteLock(@NonNull BooleanSupplier callback) {
        CacheLockKey.Key key = CacheLockKey.CacheObjectModelWrite.cache();
        return this.redissonDistributedLockerService.executeWithLock(
                key.getKey(), key.getExpire().getSeconds(),
                () -> this.executeTransaction(callback), 5000L, 10000L);
    }

    /**
     * 事务开始前已经准备好的对象写入记录。
     *
     * @since v7.260701.0
     */
    private static final class PreparedObjectRows {

        /**
         * 当前 Key 的完整对象 ID。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<String> objectIds;

        /**
         * 已完成投影的对象主表记录。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<CacheObject> objects;

        /**
         * 已完成投影的模型视图记录。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<CacheObjectModelView> modelViews;

        /**
         * 创建一个已完成事务外投影的对象写入范围。
         *
         * @param objectIds 当前 Key 的完整对象 ID
         * @param objects 对象主表记录
         * @param modelViews 模型视图记录
         * @since v7.260701.0
         */
        private PreparedObjectRows(
                @NonNull List<String> objectIds,
                @NonNull List<CacheObject> objects,
                @NonNull List<CacheObjectModelView> modelViews) {
            super();
            this.objectIds = objectIds;
            this.objects = objects;
            this.modelViews = modelViews;
        }
    }
}
