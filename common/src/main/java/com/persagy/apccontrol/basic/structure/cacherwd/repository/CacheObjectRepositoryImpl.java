package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectKeyRel;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectKeyRelService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectRecordConverter;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 新缓存对象与缓存键关联仓储实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheObjectRepositoryImpl implements CacheObjectRepository {

    /**
     * 缓存对象资源服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectService cacheObjectService;

    /**
     * 缓存键与对象关联资源服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectKeyRelService cacheObjectKeyRelService;

    /**
     * 对象公共列与扩展 JSON 的重建转换器。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRecordConverter cacheObjectRecordConverter;

    @Override
    public @NonNull List<DtpRwdObjectDTO> listByCacheKey(
            @NonNull String cacheKey,
            @Nullable Collection<String> withColumns) {
        return this.listByCacheKey(cacheKey, DtpRwdObjectDTO.class, withColumns);
    }

    @Override
    public <T extends DtpRwdObjectDTO> @NonNull List<T> listByCacheKey(
            @NonNull String cacheKey,
            @NonNull Class<T> targetClass,
            @Nullable Collection<String> withColumns) {
        return this.listByObjectIds(
                this.listObjectIdsByCacheKey(cacheKey),
                targetClass, withColumns);
    }

    @Override
    public <T extends DtpRwdObjectDTO> @NonNull List<T> listByClassCode(
            @NonNull String classCode,
            @NonNull Class<T> targetClass,
            @Nullable Collection<String> withColumns) {
        CacheObjectQueryDaoParam queryParam = new CacheObjectQueryDaoParam();
        queryParam.setClassCodes(Collections.singletonList(classCode));
        boolean queryAllFields = CollectionUtils.isEmpty(withColumns);
        queryParam.setIds(null);
        Map<String, CacheObject> records = queryAllFields
                ? this.mapRecordsByClassCodes(Collections.singletonList(classCode))
                : this.mapRecordsByClassCodes(
                        Collections.singletonList(classCode), withColumns);
        return this.rebuildObjects(
                records.keySet(), records, targetClass,
                queryAllFields ? null : withColumns);
    }

    @Override
    public @NonNull List<DtpRwdObjectDTO> listByObjectIds(
            @NonNull Collection<String> objectIds,
            @Nullable Collection<String> withColumns) {
        return this.listByObjectIds(objectIds, DtpRwdObjectDTO.class, withColumns);
    }

    @Override
    public <T extends DtpRwdObjectDTO> @NonNull List<T> listByObjectIds(
            @NonNull Collection<String> objectIds,
            @NonNull Class<T> targetClass,
            @Nullable Collection<String> withColumns) {
        boolean queryAllFields = CollectionUtils.isEmpty(withColumns);
        return this.rebuildObjects(
                objectIds,
                queryAllFields
                        ? this.mapRecordsById(objectIds)
                        : this.mapRecordsById(objectIds, withColumns),
                targetClass, queryAllFields ? null : withColumns);
    }

    @Override
    public @NonNull List<String> listObjectIdsByCacheKey(@NonNull String cacheKey) {
        List<CacheObjectKeyRel> relations = this.cacheObjectKeyRelService.listByCacheKeys(
                Collections.singletonList(cacheKey));
        List<String> objectIds = new ArrayList<>(relations.size());
        for (CacheObjectKeyRel relation : relations) {
            objectIds.add(relation.getObjectId());
        }
        return objectIds;
    }

    @Override
    public @NonNull List<CacheObject> listObjects(@NonNull CacheObjectQueryDaoParam param) {
        return this.cacheObjectService.list(param);
    }

    @Override
    public @NonNull Map<String, List<DtpRwdObjectDTO>> mapByCacheKeys(
            @NonNull Collection<String> cacheKeys,
            @Nullable Collection<String> withColumns) {
        return this.mapByCacheKeys(cacheKeys, DtpRwdObjectDTO.class, withColumns);
    }

    @Override
    public <T extends DtpRwdObjectDTO> @NonNull Map<String, List<T>> mapByCacheKeys(
            @NonNull Collection<String> cacheKeys,
            @NonNull Class<T> targetClass,
            @Nullable Collection<String> withColumns) {
        if (CollectionUtils.isEmpty(cacheKeys)) {
            return Collections.emptyMap();
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("批量查询新缓存对象");
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("CacheObjectRepositoryImpl#mapByCacheKeys")) {
            Map<String, List<String>> objectIdsByCacheKey =
                    this.mapObjectIdsByCacheKeys(cacheKeys);
            safeNode.recordStep("查询缓存键对象关系");
            Set<String> objectIds = this.collectObjectIds(objectIdsByCacheKey);
            boolean queryAllFields = CollectionUtils.isEmpty(withColumns);
            Map<String, CacheObject> recordById = queryAllFields
                    ? this.mapRecordsById(objectIds)
                    : this.mapRecordsById(objectIds, withColumns);
            if (CollectionUtils.isNotEmpty(objectIds)) {
                safeNode.recordStep("查询缓存对象主表");
            }
            Map<String, List<T>> result = new LinkedHashMap<>();
            for (String cacheKey : cacheKeys) {
                result.put(cacheKey, this.rebuildObjects(
                        objectIdsByCacheKey.getOrDefault(cacheKey, Collections.emptyList()),
                        recordById, targetClass, queryAllFields ? null : withColumns));
            }
            safeNode.recordStep("重建缓存对象模型");
            return result;
        }
    }

    @Override
    public @NonNull Map<String, List<String>> mapObjectIdsByCacheKeys(
            @NonNull Collection<String> cacheKeys) {
        Map<String, Set<String>> groupedObjectIds = new LinkedHashMap<>();
        for (String cacheKey : cacheKeys) {
            groupedObjectIds.computeIfAbsent(cacheKey, key -> new LinkedHashSet<>());
        }
        List<CacheObjectKeyRel> relations = this.cacheObjectKeyRelService.listByCacheKeys(
                cacheKeys);
        for (CacheObjectKeyRel relation : relations) {
            groupedObjectIds.get(relation.getCacheKey()).add(relation.getObjectId());
        }
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : groupedObjectIds.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return result;
    }

    @Override
    public void upsertObjects(@NonNull Collection<CacheObject> objects) {
        this.cacheObjectService.upsertObjects(objects);
    }

    @Override
    public void removeObjectsByClassCodes(@NonNull Collection<String> classCodes) {
        this.cacheObjectService.deleteByClassCodes(classCodes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceObjectIds(@NonNull String cacheKey, @NonNull Collection<String> objectIds) {
        // 领域仓储持有完整替换事务；插入失败时必须回滚删除，保留替换前的完整关联集合。
        this.cacheObjectKeyRelService.removeByCacheKey(cacheKey);
        this.cacheObjectKeyRelService.insertObjectIds(cacheKey, objectIds);
    }

    @Override
    public void removeObjectKeyRelations(@NonNull Collection<String> cacheKeys) {
        this.cacheObjectKeyRelService.removeByCacheKeys(cacheKeys);
    }

    /**
     * 批量查询对象主记录并按 ID 建立映射（全字段）。
     *
     * @param objectIds 对象 ID
     * @return 对象 ID 到主记录的映射
     * @since v7.260701.0
     */
    private @NonNull Map<String, CacheObject> mapRecordsById(
            @NonNull Collection<String> objectIds) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Collections.emptyMap();
        }
        CacheObjectQueryDaoParam queryParam = new CacheObjectQueryDaoParam();
        queryParam.setIds(objectIds);
        queryParam.addColumns(
                CacheObject::getId,
                CacheObject::getProjectId,
                CacheObject::getLocalId,
                CacheObject::getLocalName,
                CacheObject::getClassCode,
                CacheObject::getObjectType,
                CacheObject::getFloorId,
                CacheObject::getBuildingId,
                CacheObject::getMonitoringSurveillanceStatus,
                CacheObject::getObjectData);
        Map<String, CacheObject> result = new LinkedHashMap<>();
        for (CacheObject record : this.listObjects(queryParam)) {
            result.put(record.getId(), record);
        }
        return result;
    }

    /**
     * 批量查询指定对象类编码的完整主表快照。
     *
     * @param classCodes 对象类编码集合
     * @return 对象 ID 到主记录的映射
     * @since v7.260802.0
     */
    private @NonNull Map<String, CacheObject> mapRecordsByClassCodes(
            @NonNull Collection<String> classCodes) {
        CacheObjectQueryDaoParam queryParam = new CacheObjectQueryDaoParam();
        queryParam.setClassCodes(classCodes);
        return this.mapRecords(queryParam);
    }

    /**
     * 批量查询指定对象类编码和字段的主表快照。
     *
     * @param classCodes 对象类编码集合
     * @param withColumns RWD 字段
     * @return 对象 ID 到主记录的映射
     * @since v7.260802.0
     */
    private @NonNull Map<String, CacheObject> mapRecordsByClassCodes(
            @NonNull Collection<String> classCodes,
            @NonNull Collection<String> withColumns) {
        CacheObjectQueryDaoParam queryParam = new CacheObjectQueryDaoParam();
        queryParam.setClassCodes(classCodes);
        this.addRequestedColumns(queryParam, withColumns);
        return this.mapRecords(queryParam);
    }

    /**
     * 按查询参数组装主表映射。
     *
     * @param queryParam 查询参数
     * @return 对象 ID 到主记录的映射
     * @since v7.260802.0
     */
    private @NonNull Map<String, CacheObject> mapRecords(
            @NonNull CacheObjectQueryDaoParam queryParam) {
        Map<String, CacheObject> result = new LinkedHashMap<>();
        for (CacheObject record : this.listObjects(queryParam)) {
            result.put(record.getId(), record);
        }
        return result;
    }

    /**
     * 使用同一字段投影批量查询对象主记录。
     *
     * @param objectIds 对象 ID
     * @param withColumns 调用方请求字段
     * @return 对象 ID 到主记录的映射
     * @since v7.260701.0
     */
    private @NonNull Map<String, CacheObject> mapRecordsById(
            @NonNull Collection<String> objectIds,
            @NonNull Collection<String> withColumns) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Collections.emptyMap();
        }
        CacheObjectQueryDaoParam queryParam = new CacheObjectQueryDaoParam();
        queryParam.setIds(objectIds);
        this.addRequestedColumns(queryParam, withColumns);
        Map<String, CacheObject> result = new LinkedHashMap<>();
        for (CacheObject record : this.listObjects(queryParam)) {
            result.put(record.getId(), record);
        }
        return result;
    }

    /**
     * 将业务字段转换为缓存对象表的受控查询列。
     *
     * @param queryParam 查询参数
     * @param withColumns RWD 字段
     * @since v7.260701.0
     */
    private void addRequestedColumns(
            @NonNull CacheObjectQueryDaoParam queryParam,
            @NonNull Collection<String> withColumns) {
        queryParam.addColumns(CacheObject::getId, CacheObject::getClassCode);
        if (withColumns.contains(DtpRwdObjectDTO.Fields.projectId)) {
            queryParam.addColumns(CacheObject::getProjectId);
        }
        if (withColumns.contains(DtpRwdObjectDTO.Fields.localId)) {
            queryParam.addColumns(CacheObject::getLocalId);
        }
        if (withColumns.contains(DtpRwdObjectDTO.Fields.localName)) {
            queryParam.addColumns(CacheObject::getLocalName);
        }
        if (withColumns.contains(DtpRwdObjectDTO.Fields.objType)) {
            queryParam.addColumns(CacheObject::getObjectType);
        }
        if (withColumns.contains(DtpRwdEquipmentDTO.Fields.floorId)) {
            queryParam.addColumns(CacheObject::getFloorId);
        }
        if (withColumns.contains(DtpRwdEquipmentDTO.Fields.buildingId)) {
            queryParam.addColumns(CacheObject::getBuildingId);
        }
        if (withColumns.contains(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value())) {
            queryParam.addColumns(CacheObject::getMonitoringSurveillanceStatus);
        }
        if (this.containsObjectExtensionField(withColumns)) {
            queryParam.addColumns(CacheObject::getObjectData);
        }
    }

    /**
     * 判断调用方字段是否命中对象 JSON 存储白名单。
     *
     * @param fields 请求字段
     * @return 命中时返回 {@code true}
     * @since v7.260701.0
     */
    private boolean containsObjectExtensionField(@NonNull Collection<String> fields) {
        for (Set<String> configuredFields
                : CacheObjectExtensionFieldConfig.getObjectExtensionFields().values()) {
            if (!Collections.disjoint(configuredFields, fields)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按指定 ID 顺序使用同一批主记录独立重建 DTO。
     *
     * @param objectIds  当前结果需要的对象 ID
     * @param recordById 已查询到的对象主记录
     * @param targetClass 目标 DTO 类型
     * @param withColumns 调用方请求字段；为 {@code null} 时兼容全字段恢复
     * @param <T>         RWD DTO 类型
     * @return 按 ID 顺序重建的 DTO；未查到的对象自动忽略
     * @since v7.260701.0
     */
    private <T extends DtpRwdObjectDTO> @NonNull List<T> rebuildObjects(
            @NonNull Collection<String> objectIds,
            @NonNull Map<String, CacheObject> recordById,
            @NonNull Class<T> targetClass,
            @Nullable Collection<String> withColumns) {
        return objectIds.stream()
                .map(recordById::get)
                .filter(Objects::nonNull)
                .map(record -> withColumns == null
                        ? this.cacheObjectRecordConverter.toRwdObject(record, targetClass)
                        : this.cacheObjectRecordConverter.toRwdObject(
                                record, targetClass, withColumns))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 汇总多个缓存键关联的对象 ID。
     *
     * @param objectIdsByCacheKey 缓存键到对象 ID
     * @return 去重后的对象 ID
     * @since v7.260701.0
     */
    private @NonNull Set<String> collectObjectIds(
            @NonNull Map<String, List<String>> objectIdsByCacheKey) {
        Set<String> result = new LinkedHashSet<>();
        for (List<String> objectIds : objectIdsByCacheKey.values()) {
            result.addAll(objectIds);
        }
        return result;
    }

    @Override
    public @NonNull String logTag() {
        return "CacheObjectRepositoryImpl";
    }
}
