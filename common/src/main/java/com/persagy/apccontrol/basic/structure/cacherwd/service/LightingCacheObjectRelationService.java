package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingCircuitDetailDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 照明回路关系快照与本地写入服务。
 *
 * <p>照明关系既包含回路作为主体的服务空间/服务楼层，也包含控制模块、DDC 作为主体的反向
 * 控制关系。该服务按当前 Key 批量取得完整关系结果，再将目标对象、模型视图和关系删除重建
 * 动作交给对象模型写事务执行，避免在 READY 读取时依赖 RWD。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LightingCacheObjectRelationService {

    /**
     * RWD 对象客户端。
     *
     * @since v7.260701.0
     */
    private final @NonNull DtpRwdClientService dtpRwdClientService;

    /**
     * 新对象主表仓储。
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
     * 对象独立列与扩展字段转换器。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRecordConverter cacheObjectRecordConverter;

    /**
     * 模型视图独立列与扩展字段转换器。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelViewRecordConverter cacheObjectModelViewRecordConverter;

    /**
     * 准备一个照明缓存键的关系写入动作。
     *
     * <p>每个 Key 只删除自身替换前后回路涉及的旧关系，并写入本次 RWD 查询结果；
     * 不枚举其他照明 Key，也不重建其他 Key 的关系范围。该单键入口供模板增量调用，按
     * 二级类型从 RWD 查询服务空间或服务楼层，并继续查询控制模块和 DDC。</p>
     *
     * @param cacheKey 当前被替换的缓存键
     * @param circuits 当前缓存键的完整回路快照
     * @param secondType 当前照明二级类型，可为空
     * @return 已完成远程计算的本地写入动作
     * @since v7.260701.0
     */
    public @NonNull CacheWriteAction prepareSaveAction(
            @NonNull String cacheKey,
            @NonNull Collection<? extends DtpRwdObjectDTO> circuits,
            @Nullable RunningMonitorSecondTypeEnum secondType) {
        Set<String> currentCircuitIds = new LinkedHashSet<>(this.objectIds(circuits));
        Set<String> replacedCircuitIds = new LinkedHashSet<>(
                this.cacheObjectRepository.listObjectIdsByCacheKey(cacheKey));
        replacedCircuitIds.addAll(currentCircuitIds);
        RelationSnapshot snapshot = this.prepareSnapshot(
                currentCircuitIds, replacedCircuitIds, secondType);
        return () -> this.writeSnapshot(snapshot);
    }

    /**
     * 使用完整初始化已附着的空间和楼层准备单个照明 Key 的关系写入动作。
     *
     * <p>该入口只读取当前 Key 的上一版回路，并只查询当前回路对应的控制模块和 DDC；
     * 不枚举或合并其他照明 Key。空间和楼层直接复用源查询结果，避免对同一关系重复请求 RWD。</p>
     *
     * @param cacheKey 当前被替换的缓存键
     * @param circuits 当前缓存键包含空间或楼层关系的完整回路快照
     * @return 已完成远程计算的本地写入动作
     * @since v7.260701.0
     */
    public @NonNull CacheWriteAction prepareSaveActionFromCircuitRelations(
            @NonNull String cacheKey,
            @NonNull Collection<LightingCircuitDetailDTO> circuits) {
        Set<String> currentCircuitIds = new LinkedHashSet<>(this.objectIds(circuits));
        Set<String> replacedCircuitIds = new LinkedHashSet<>(
                this.cacheObjectRepository.listObjectIdsByCacheKey(cacheKey));
        replacedCircuitIds.addAll(currentCircuitIds);
        RelationSnapshot snapshot = this.prepareSnapshotFromCircuitRelations(
                circuits, currentCircuitIds, replacedCircuitIds);
        return () -> this.writeSnapshot(snapshot);
    }

    /**
     * 准备删除缓存键时的关系清理动作。
     *
     * <p>关系表不保存缓存 Key，删除时按该 Key 的回路 ID 分别清理正向主体关系和反向目标
     * 关系；其他 Key 若包含相同关系，可在自身重建时重复写入。</p>
     *
     * @param cacheKeys 待删除缓存键
     * @return 已完成本地关系范围计算的清理动作
     * @since v7.260701.0
     */
    public @NonNull CacheWriteAction prepareRemoveAction(
            @NonNull Collection<String> cacheKeys) {
        Map<String, List<String>> objectIdsByKey =
                this.cacheObjectRepository.mapObjectIdsByCacheKeys(cacheKeys);
        Set<String> removedCircuitIds = new LinkedHashSet<>();
        for (List<String> objectIds : objectIdsByKey.values()) {
            removedCircuitIds.addAll(objectIds);
        }
        if (CollectionUtils.isEmpty(removedCircuitIds)) {
            return () -> {
            };
        }
        return () -> this.replaceRelations(
                removedCircuitIds, Collections.emptyList());
    }

    /**
     * 关系快照中的对象和关系记录。
     *
     * @since v7.260701.0
     */
    private static final class RelationSnapshot {

        /**
         * 关系目标对象主表记录。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<CacheObject> objects;

        /**
         * 关系目标对象模型视图记录。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<CacheObjectModelView> modelViews;

        /**
         * 当前 Key 替换前后涉及的回路 ID。
         *
         * @since v7.260701.0
         */
        private final @NonNull Set<String> circuitIds;

        /**
         * 当前 Key 的完整关系记录。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<CacheObjectRel> relations;

        /**
         * 创建一次照明关系重建所需的完整快照。
         *
         * @param objects 关系目标对象主表记录
         * @param modelViews 关系目标对象模型视图记录
         * @param circuitIds 当前 Key 替换前后涉及的回路 ID
         * @param relations 当前 Key 的完整关系记录
         * @since v7.260701.0
         */
        private RelationSnapshot(
                @NonNull List<CacheObject> objects,
                @NonNull List<CacheObjectModelView> modelViews,
                @NonNull Collection<String> circuitIds,
                @NonNull List<CacheObjectRel> relations) {
            super();
            this.objects = objects;
            this.modelViews = modelViews;
            this.circuitIds = new LinkedHashSet<>(circuitIds);
            this.relations = relations;
        }
    }

    /**
     * 事务内写入关系目标对象、视图和当前 Key 关系。
     *
     * @param snapshot 已完成的关系快照
     * @since v7.260701.0
     */
    private void writeSnapshot(@NonNull RelationSnapshot snapshot) {
        this.cacheObjectRepository.upsertObjects(snapshot.objects);
        this.cacheObjectModelViewRepository.upsertModelViews(snapshot.modelViews);
        this.replaceRelations(snapshot.circuitIds, snapshot.relations);
    }

    /**
     * 按回路 ID 删除照明关系并写入当前 Key 的完整结果。
     *
     * <p>服务空间、楼层关系以回路为主体，控制模块、DDC 关系以回路为目标，因此必须分别
     * 使用正向和反向删除条件。所有调用都由对象模型复合写事务包裹。</p>
     *
     * @param circuitIds 当前 Key 替换前后涉及的回路 ID
     * @param relations 当前 Key 的完整关系记录
     * @since v7.260701.0
     */
    private void replaceRelations(
            @NonNull Collection<String> circuitIds,
            @NonNull Collection<CacheObjectRel> relations) {
        this.removeForwardRelations(
                circuitIds, DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE);
        this.removeForwardRelations(
                circuitIds, DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_FLOOR);
        this.removeReverseRelations(
                circuitIds, DtpRwdRelationTypeEnum.EQUIPMENT_CONTROLLER);
        this.removeReverseRelations(
                circuitIds, DtpRwdRelationTypeEnum.WE_NETWORK);
        this.cacheObjectRelRepository.addRelations(relations);
    }

    /**
     * 按回路主体删除指定类型的正向关系。
     *
     * @param circuitIds 回路 ID
     * @param relation 关系类型
     * @since v7.260701.0
     */
    private void removeForwardRelations(
            @NonNull Collection<String> circuitIds,
            @NonNull DtpRwdRelationTypeEnum relation) {
        this.cacheObjectRelRepository.removeForwardRelations(
                circuitIds, relation.value().getLeft(), relation.value().getRight());
    }

    /**
     * 按回路目标删除指定类型的反向命中关系。
     *
     * @param circuitIds 回路 ID
     * @param relation 关系类型
     * @since v7.260701.0
     */
    private void removeReverseRelations(
            @NonNull Collection<String> circuitIds,
            @NonNull DtpRwdRelationTypeEnum relation) {
        this.cacheObjectRelRepository.removeReverseRelations(
                circuitIds, relation.value().getLeft(), relation.value().getRight());
    }

    /**
     * 生成保存用关系快照。
     *
     * @param currentCircuitIds 当前缓存键回路 ID
     * @param replacedCircuitIds 当前键替换前后回路并集
     * @param secondType 二级类型
     * @return 关系快照
     * @since v7.260701.0
     */
    private @NonNull RelationSnapshot prepareSnapshot(
            @NonNull Collection<String> currentCircuitIds,
            @NonNull Collection<String> replacedCircuitIds,
            @Nullable RunningMonitorSecondTypeEnum secondType) {
        Set<String> currentIds = new LinkedHashSet<>(currentCircuitIds);
        Set<String> replacedIds = new LinkedHashSet<>(replacedCircuitIds);
        List<CacheObjectRel> relations = new ArrayList<>();
        Set<DtpRwdObjectDTO> targetObjects = new LinkedHashSet<>();

        DtpRwdRelationTypeEnum serviceSpace = DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE;
        DtpRwdRelationTypeEnum serviceFloor = DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_FLOOR;

        List<DtpRwdObjectDTO> spaces = this.queryForwardTargets(
                DtpRwdClassCodeEnum.SPACE, serviceSpace, currentIds,
                !this.isNightLighting(secondType));
        List<DtpRwdObjectDTO> floors = this.queryForwardTargets(
                DtpRwdClassCodeEnum.FLOOR, serviceFloor, currentIds,
                secondType == null || this.isNightLighting(secondType));
        this.addForwardRelations(relations, serviceSpace, currentIds, spaces, targetObjects);
        this.addForwardRelations(relations, serviceFloor, currentIds, floors, targetObjects);

        return this.completeSnapshot(
                currentIds, replacedIds, relations, targetObjects);
    }

    /**
     * 从回路拥有的嵌套空间和楼层生成初始化关系快照。
     *
     * <p>需求照明查询在把关系目标附着到回路后会清空目标的 {@code relObjIds}，所以正向关系
     * 的主体必须取拥有该嵌套目标的回路 ID，不能再从目标对象反推。</p>
     *
     * @param circuits 带服务空间或服务楼层的初始化回路
     * @param currentCircuitIds 当前缓存键回路 ID
     * @param replacedCircuitIds 当前键替换前后回路并集
     * @return 关系快照
     * @since v7.260701.0
     */
    private @NonNull RelationSnapshot prepareSnapshotFromCircuitRelations(
            @NonNull Collection<LightingCircuitDetailDTO> circuits,
            @NonNull Collection<String> currentCircuitIds,
            @NonNull Collection<String> replacedCircuitIds) {
        Set<String> currentIds = new LinkedHashSet<>(currentCircuitIds);
        Set<String> replacedIds = new LinkedHashSet<>(replacedCircuitIds);
        List<CacheObjectRel> relations = new ArrayList<>();
        Set<DtpRwdObjectDTO> targetObjects = new LinkedHashSet<>();
        DtpRwdRelationTypeEnum serviceSpace = DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE;
        DtpRwdRelationTypeEnum serviceFloor = DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_FLOOR;

        for (LightingCircuitDetailDTO circuit : circuits) {
            String circuitId = circuit.getId();
            this.addOwnedForwardRelations(
                    relations, serviceSpace, circuitId, circuit.getRelSpace(), targetObjects);
            this.addOwnedForwardRelations(
                    relations, serviceFloor, circuitId, circuit.getRelFloor(), targetObjects);
        }
        return this.completeSnapshot(
                currentIds, replacedIds, relations, targetObjects);
    }

    /**
     * 查询控制模块和 DDC 后完成关系快照。
     *
     * <p>调用方先准备正向空间和楼层关系：完整初始化复用回路嵌套目标，模板增量使用 RWD
     * 查询结果。两个反向控制关系仍按既有顺序和当前 Key 的完整回路范围分别查询 RWD。</p>
     *
     * @param currentCircuitIds 当前缓存键回路 ID
     * @param replacedCircuitIds 当前键替换前后回路并集
     * @param relations 已准备的正向关系
     * @param targetObjects 已准备的正向关系目标
     * @return 包含四类关系的完整快照
     * @since v7.260701.0
     */
    private @NonNull RelationSnapshot completeSnapshot(
            @NonNull Set<String> currentCircuitIds,
            @NonNull Set<String> replacedCircuitIds,
            @NonNull List<CacheObjectRel> relations,
            @NonNull Set<DtpRwdObjectDTO> targetObjects) {
        DtpRwdRelationTypeEnum controller = DtpRwdRelationTypeEnum.EQUIPMENT_CONTROLLER;
        DtpRwdRelationTypeEnum ddc = DtpRwdRelationTypeEnum.WE_NETWORK;
        List<DtpRwdObjectDTO> controllers = this.queryReverseTargets(
                DtpRwdClassCodeEnum.LIGHTING_CONTROLLER, controller, currentCircuitIds);
        List<DtpRwdObjectDTO> ddcs = this.queryReverseTargets(
                DtpRwdClassCodeEnum.WEFCDD, ddc, currentCircuitIds);
        this.addReverseRelations(
                relations, controller, currentCircuitIds, controllers, targetObjects);
        this.addReverseRelations(
                relations, ddc, currentCircuitIds, ddcs, targetObjects);

        return this.toRelationSnapshot(
                targetObjects, replacedCircuitIds, relations);
    }

    /**
     * 批量查询“主体为回路”的关系目标。
     *
     * <p>完整回路范围一次交给 RWD 客户端，分页和并发由客户端包装层统一处理。</p>
     *
     * @param classCode 目标对象类编码
     * @param relation 关系类型
     * @param circuitIds 回路主体 ID
     * @param enabled 是否执行该关系查询
     * @return RWD 目标对象
     * @since v7.260701.0
     */
    private @NonNull List<DtpRwdObjectDTO> queryForwardTargets(
            @NonNull DtpRwdClassCodeEnum classCode,
            @NonNull DtpRwdRelationTypeEnum relation,
            @NonNull Collection<String> circuitIds,
            boolean enabled) {
        if (!enabled || CollectionUtils.isEmpty(circuitIds)) {
            return Collections.emptyList();
        }
        DtpRwdObjectQueryParam query = this.baseRelationQuery(classCode);
        query.setRelationTo(new DtpRwdObjectRelationQueryParam(relation)
                .setObjFrom(circuitIds));
        return this.dtpRwdClientService.listObject(query);
    }

    /**
     * 批量查询“主体为控制模块或 DDC”的反向关系。
     *
     * <p>完整回路范围一次交给 RWD 客户端，分页和并发由客户端包装层统一处理。</p>
     *
     * @param classCode 主体对象类编码
     * @param relation 关系类型
     * @param circuitIds 回路目标 ID
     * @return RWD 主体对象
     * @since v7.260701.0
     */
    private @NonNull List<DtpRwdObjectDTO> queryReverseTargets(
            @NonNull DtpRwdClassCodeEnum classCode,
            @NonNull DtpRwdRelationTypeEnum relation,
            @NonNull Collection<String> circuitIds) {
        if (CollectionUtils.isEmpty(circuitIds)) {
            return Collections.emptyList();
        }
        DtpRwdObjectQueryParam query = this.baseRelationQuery(classCode);
        query.setRelationFrom(new DtpRwdObjectRelationQueryParam(relation)
                .setObjTo(circuitIds));
        return this.dtpRwdClientService.listObject(query);
    }

    /**
     * 构造关系查询的公共字段和项目范围。
     *
     * @param classCode 对象类编码
     * @return RWD 查询参数
     * @since v7.260701.0
     */
    private @NonNull DtpRwdObjectQueryParam baseRelationQuery(
            @NonNull DtpRwdClassCodeEnum classCode) {
        DtpRwdObjectQueryParam query = new DtpRwdObjectQueryParam();
        query.setGroupCode(ContextUtil.getGroupCode());
        query.setProjectId(ContextUtil.getProjectId());
        query.setClassCodeEnum(classCode);
        Set<String> queryFields = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(classCode.value()));
        queryFields.add(DtpRwdObjectDTO.Fields.id);
        queryFields.add(DtpRwdObjectDTO.Fields.relObjIds);
        query.addWithColumn(queryFields.toArray(new String[0]));
        return query;
    }

    /**
     * 将正向关系响应展开为回路主体关系。
     *
     * @param relations 关系收集器
     * @param relation 关系类型
     * @param circuitIds 当前回路 ID
     * @param targets RWD 目标对象
     * @param targetObjects 目标对象收集器
     * @since v7.260701.0
     */
    private void addForwardRelations(
            @NonNull List<CacheObjectRel> relations,
            @NonNull DtpRwdRelationTypeEnum relation,
            @NonNull Set<String> circuitIds,
            @NonNull Collection<DtpRwdObjectDTO> targets,
            @NonNull Set<DtpRwdObjectDTO> targetObjects) {
        for (DtpRwdObjectDTO target : targets) {
            String targetId = target.getId();
            targetObjects.add(target);
            for (String circuitId : this.readRelationIds(target)) {
                if (circuitIds.contains(circuitId)) {
                    CacheObjectRel relationRow = new CacheObjectRel();
                    relationRow.setObjectId(circuitId);
                    relationRow.setGraphCode(relation.value().getLeft());
                    relationRow.setRelCode(relation.value().getRight());
                    relationRow.setRelObjectId(targetId);
                    relations.add(relationRow);
                }
            }
        }
    }

    /**
     * 将回路嵌套持有的目标展开为正向关系。
     *
     * <p>目标的 {@code relObjIds} 已在上游清空；{@code circuitId} 是该嵌套关系的唯一主体
     * 归属。空目标表示该回路在当前关系方向没有命中，是有效结果。</p>
     *
     * @param relations 关系收集器
     * @param relation 关系类型
     * @param circuitId 拥有嵌套目标的回路 ID
     * @param targets 回路已附着的关系目标，可为空
     * @param targetObjects 目标对象收集器
     * @since v7.260701.0
     */
    private void addOwnedForwardRelations(
            @NonNull List<CacheObjectRel> relations,
            @NonNull DtpRwdRelationTypeEnum relation,
            @NonNull String circuitId,
            @Nullable Collection<? extends DtpRwdObjectDTO> targets,
            @NonNull Set<DtpRwdObjectDTO> targetObjects) {
        if (CollectionUtils.isEmpty(targets)) {
            return;
        }
        for (DtpRwdObjectDTO target : targets) {
            targetObjects.add(target);
            CacheObjectRel relationRow = new CacheObjectRel();
            relationRow.setObjectId(circuitId);
            relationRow.setGraphCode(relation.value().getLeft());
            relationRow.setRelCode(relation.value().getRight());
            relationRow.setRelObjectId(target.getId());
            relations.add(relationRow);
        }
    }

    /**
     * 将反向关系响应展开为控制主体关系。
     *
     * @param relations 关系收集器
     * @param relation 关系类型
     * @param circuitIds 当前 Key 回路 ID
     * @param subjects RWD 控制主体
     * @param targetObjects 目标对象收集器
     * @since v7.260701.0
     */
    private void addReverseRelations(
            @NonNull List<CacheObjectRel> relations,
            @NonNull DtpRwdRelationTypeEnum relation,
            @NonNull Set<String> circuitIds,
            @NonNull Collection<DtpRwdObjectDTO> subjects,
            @NonNull Set<DtpRwdObjectDTO> targetObjects) {
        for (DtpRwdObjectDTO subject : subjects) {
            String subjectId = subject.getId();
            for (String circuitId : this.readRelationIds(subject)) {
                if (circuitIds.contains(circuitId)) {
                    // RWD 可能返回请求范围外的关系 ID，只有命中当前 Key 才能写入该快照。
                    targetObjects.add(subject);
                    CacheObjectRel relationRow = new CacheObjectRel();
                    relationRow.setObjectId(subjectId);
                    relationRow.setGraphCode(relation.value().getLeft());
                    relationRow.setRelCode(relation.value().getRight());
                    relationRow.setRelObjectId(circuitId);
                    relations.add(relationRow);
                }
            }
        }
    }

    /**
     * 将 RWD 关系字段转换为去重后的对象 ID。
     *
     * @param object 关系响应对象
     * @return 关系对象 ID
     * @since v7.260701.0
     */
    private @NonNull Set<String> readRelationIds(@NonNull DtpRwdObjectDTO object) {
        return CollectionUtils.isEmpty(object.getRelObjIds())
                ? Collections.emptySet() : object.getRelObjIds();
    }

    /**
     * 提取回路 ID。
     *
     * @param circuits 回路对象
     * @return 回路 ID
     * @since v7.260701.0
     */
    private @NonNull List<String> objectIds(
            @NonNull Collection<? extends DtpRwdObjectDTO> circuits) {
        return circuits.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toList());
    }

    /**
     * 将 RWD 关系目标投影为完整关系快照。
     *
     * <p>每个目标只转换一次字段 Map，同一 Map 先生成对象记录，再生成模型视图记录。
     * 两类记录分别按 ID 去重，并保留目标首次出现的稳定顺序。</p>
     *
     * @param objects RWD 关系目标
     * @param circuitIds 当前 Key 替换前后涉及的回路 ID
     * @param relations 当前 Key 的完整关系记录
     * @return 包含目标对象、模型视图和关系的完整快照
     * @since v7.260701.0
     */
    private @NonNull RelationSnapshot toRelationSnapshot(
            @NonNull Collection<DtpRwdObjectDTO> objects,
            @NonNull Set<String> circuitIds,
            @NonNull List<CacheObjectRel> relations) {
        Map<String, CacheObject> cacheObjectsById = new LinkedHashMap<>();
        Map<String, CacheObjectModelView> modelViewsById = new LinkedHashMap<>();
        for (DtpRwdObjectDTO object : objects) {
            Map<String, Object> fields = this.toFields(object);
            CacheObject cacheObject = this.cacheObjectRecordConverter.toCacheObject(fields);
            cacheObjectsById.put(cacheObject.getId(), cacheObject);
            CacheObjectModelView modelView =
                    this.cacheObjectModelViewRecordConverter.toModelView(fields);
            modelViewsById.put(modelView.getId(), modelView);
        }
        return new RelationSnapshot(
                new ArrayList<>(cacheObjectsById.values()),
                new ArrayList<>(modelViewsById.values()),
                circuitIds,
                relations);
    }

    /**
     * 序列化 RWD DTO 后恢复为字段 Map，保持与对象主缓存写入完全一致。
     *
     * @param object RWD 对象
     * @return 字段 Map
     * @since v7.260701.0
     */
    private @NonNull Map<String, Object> toFields(@NonNull DtpRwdObjectDTO object) {
        String json = ObjectMapperUtils.writeValueAsStringFailSafe(object);
        if (StringUtils.isBlank(json)) {
            throw CacheObjectModelExceptionFactory.invalidStructure("lighting.relationObject");
        }
        Map<String, Object> fields = ObjectMapperUtils.readMapFailSafe(json);
        if (fields == null) {
            throw CacheObjectModelExceptionFactory.invalidStructure("lighting.relationObject");
        }
        return fields;
    }

    /**
     * 判断照明二级类型是否为夜景照明。
     *
     * @param secondType 二级类型
     * @return 是否为夜景照明
     * @since v7.260701.0
     */
    private boolean isNightLighting(@Nullable RunningMonitorSecondTypeEnum secondType) {
        return secondType != null
                && secondType.toDemandTemplateTypeEnum().isNightLighting();
    }

}
