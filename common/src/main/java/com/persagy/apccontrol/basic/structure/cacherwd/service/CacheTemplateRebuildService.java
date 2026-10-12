package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheInfoCodeTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheInfoCodeTemplateService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.model.entity.CacheRwdObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模板对象缓存同步重建编排服务。
 *
 * <p>服务先完成全部模板的 RWD 查询以及明细、规则等扩展动作准备，再按一级类型选择新版
 * 对象模型或现有 RWD 缓存入口。两条路径互斥，任一准备失败时均不会先删除当前缓存范围。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheTemplateRebuildService {

    /**
     * 新版缓存对象模型写入服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelWriteService cacheObjectModelWriteService;

    /**
     * RWD 对象服务，用于在写事务外取得模板关系完整快照。
     *
     * @since v7.260701.0
     */
    private final @NonNull DtpRwdClientService dtpRwdClientService;

    /**
     * 尚未接入新对象模型的系统所使用的现有 RWD 缓存服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheRwdDomainService cacheRwdDomainService;

    /**
     * 新对象模型固定支持范围服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    /**
     * 模板缓存元数据服务，用于按唯一 Key 获取内部存储路由信息。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheInfoCodeTemplateService cacheInfoCodeTemplateService;

    /**
     * 模板明细、规则和照明关系等写入扩展。
     *
     * @since v7.260701.0
     */
    private final @NonNull List<CacheTemplateWriteExtension> writeExtensions;

    /**
     * 同步替换模板对象缓存。
     *
     * @param templateTypeModels 当前目标模板
     * @throws BusinessException 模板远程计算或写入失败时抛出
     * @since v7.260701.0
     */
    public void replaceTemplates(
            @NonNull Collection<InfoCodeTemplateTypeModel> templateTypeModels) {
        this.rebuildTemplates(templateTypeModels, Collections.emptyList());
    }

    /**
     * 同步删除失效模板键。
     *
     * @param templateKeys 失效模板键
     * @throws BusinessException 删除扩展准备或写入失败时抛出
     * @since v7.260701.0
     */
    public void removeTemplateKeys(@NonNull Collection<String> templateKeys) {
        if (CollectionUtils.isEmpty(templateKeys)) {
            return;
        }
        Map<RunningMonitorTypeEnum, List<String>> templateKeysByType =
                this.mapTemplateKeysByType(templateKeys);
        Map<RunningMonitorTypeEnum, List<CacheWriteAction>> removeActionsByType =
                this.prepareRemoveActions(templateKeysByType);
        this.removePreparedKeys(templateKeysByType, removeActionsByType);
    }

    /**
     * 同步重建目标模板，并在全部准备成功后清理失效键。
     *
     * @param templateTypeModels 当前目标模板
     * @param staleTemplateKeys  已不属于当前目标的旧模板键
     * @throws BusinessException 模板远程计算或写入失败时抛出
     * @since v7.260701.0
     */
    public void rebuildTemplates(
            @NonNull Collection<InfoCodeTemplateTypeModel> templateTypeModels,
            @NonNull Collection<String> staleTemplateKeys) {
        this.executePreparedTemplates(this.prepareTemplates(templateTypeModels), staleTemplateKeys);
    }

    /**
     * 在锁外准备目标模板的完整写入数据，不执行任何缓存替换。
     *
     * <p>准备阶段包含 RWD 远程查询与模板配置实时解析，受三方依赖影响可能耗时数小时，
     * 必须由调用方放在保存锁之外执行；返回结果通过 {@link #executePreparedTemplates}
     * 在保存锁内提交，避免远程准备长时间占用写入锁。</p>
     *
     * @param templateTypeModels 当前目标模板
     * @return 已完成远程计算的模板写入列表
     * @throws BusinessException 模板数据或远程对象结果不合法时抛出
     * @since v7.260901.0
     */
    public @NonNull List<PreparedTemplateWrite> prepareTemplates(
            @NonNull Collection<InfoCodeTemplateTypeModel> templateTypeModels) {
        List<PreparedTemplateWrite> preparedWrites =
                new ArrayList<>(templateTypeModels.size());
        for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
            // 重建必须基于最新配置解析, 跳过数据库旧快照复用; 后续对象查询与 from() 直接命中内存值
            templateTypeModel.parseFilterValueObject(true);
            preparedWrites.add(this.prepareTemplate(templateTypeModel));
        }
        return preparedWrites;
    }

    /**
     * 在保存锁内提交已经完成准备的模板写入，并清理失效键。
     *
     * <p>只包含本地数据库替换动作（对象键替换、扩展动作与失效键清理），不允许包含远程
     * 准备；失效键的扩展范围也必须在任何目标键写入前完成准备，防止准备失败后只更新部分模板。</p>
     *
     * @param preparedWrites    已完成远程计算的模板写入列表
     * @param staleTemplateKeys 已不属于当前目标的旧模板键
     * @throws BusinessException 本地写入失败时抛出
     * @since v7.260901.0
     */
    public void executePreparedTemplates(
            @NonNull List<PreparedTemplateWrite> preparedWrites,
            @NonNull Collection<String> staleTemplateKeys) {
        Map<RunningMonitorTypeEnum, List<String>> staleKeysByType =
                this.mapTemplateKeysByType(staleTemplateKeys);
        // 失效键的扩展范围也必须在任何目标键写入前完成准备，防止准备失败后只更新部分模板。
        Map<RunningMonitorTypeEnum, List<CacheWriteAction>> removeActionsByType =
                this.prepareRemoveActions(staleKeysByType);

        for (PreparedTemplateWrite prepared : preparedWrites) {
            this.executePreparedWrite(prepared);
        }
        this.removePreparedKeys(staleKeysByType, removeActionsByType);
    }

    /**
     * 按项目内唯一模板 Key 查询其所属一级运行监测类型。
     *
     * <p>类型只用于内部选择新版或原缓存存储路径，不参与 Key 的业务唯一性。</p>
     *
     * @param templateKeys 模板唯一 Key
     * @return 一级运行监测类型到模板 Key 的分组
     * @since v7.260701.0
     */
    private @NonNull Map<RunningMonitorTypeEnum, List<String>> mapTemplateKeysByType(
            @NonNull Collection<String> templateKeys) {
        if (CollectionUtils.isEmpty(templateKeys)) {
            return Collections.emptyMap();
        }
        List<CacheInfoCodeTemplate> templates =
                this.cacheInfoCodeTemplateService.lambdaQuery()
                        .select(CacheInfoCodeTemplate::getCustomKey,
                                CacheInfoCodeTemplate::getRunningMonitorType)
                        .eq(CacheInfoCodeTemplate::getProjectId, ContextUtil.getProjectId())
                        .in(CacheInfoCodeTemplate::getCustomKey, templateKeys)
                        .list();
        Map<RunningMonitorTypeEnum, List<String>> templateKeysByType = new LinkedHashMap<>();
        for (CacheInfoCodeTemplate template : templates) {
            templateKeysByType.computeIfAbsent(
                            template.getRunningMonitorType(), ignored -> new ArrayList<>())
                    .add(template.getCustomKey());
        }
        return templateKeysByType;
    }

    /**
     * 在任何删除发生前，按内部存储路径准备全部模板 Key 的扩展动作。
     *
     * @param templateKeysByType 一级运行监测类型到模板 Key 的分组
     * @return 一级运行监测类型到已准备删除动作的分组
     * @since v7.260701.0
     */
    private @NonNull Map<RunningMonitorTypeEnum, List<CacheWriteAction>> prepareRemoveActions(
            @NonNull Map<RunningMonitorTypeEnum, List<String>> templateKeysByType) {
        Map<RunningMonitorTypeEnum, List<CacheWriteAction>> removeActionsByType =
                new LinkedHashMap<>();
        templateKeysByType.forEach((runningMonitorType, templateKeys) -> {
            boolean useObjectModel =
                    this.cacheObjectModelStatusService.isSupported(runningMonitorType);
            removeActionsByType.put(
                    runningMonitorType,
                    this.prepareRemoveActions(
                            templateKeys, runningMonitorType, useObjectModel));
        });
        return removeActionsByType;
    }

    /**
     * 执行已经按一级运行监测类型准备完成的模板 Key 删除。
     *
     * @param templateKeysByType 一级运行监测类型到模板 Key 的分组
     * @param removeActionsByType 一级运行监测类型到删除动作的分组
     * @since v7.260701.0
     */
    private void removePreparedKeys(
            @NonNull Map<RunningMonitorTypeEnum, List<String>> templateKeysByType,
            @NonNull Map<RunningMonitorTypeEnum, List<CacheWriteAction>> removeActionsByType) {
        templateKeysByType.forEach((runningMonitorType, templateKeys) ->
                this.removePreparedKeys(
                        templateKeys,
                        this.cacheObjectModelStatusService.isSupported(runningMonitorType),
                        removeActionsByType.get(runningMonitorType)));
    }

    /**
     * 在删除前准备失效模板键的扩展动作。
     *
     * @param templateKeys       待删除的模板键
     * @param runningMonitorType 模板所属一级类型
     * @param useObjectModel 是否删除新版对象模型范围
     * @return 已完成远程或本地范围计算的删除动作
     * @since v7.260701.0
     */
    private @NonNull List<CacheWriteAction> prepareRemoveActions(
            @NonNull Collection<String> templateKeys,
            @NonNull RunningMonitorTypeEnum runningMonitorType,
            boolean useObjectModel) {
        if (useObjectModel) {
            return this.writeExtensions.stream()
                    .map(extension -> extension.prepareModelRemove(
                            templateKeys, runningMonitorType))
                    .collect(Collectors.toList());
        }
        return this.writeExtensions.stream()
                .map(extension -> extension.prepareRwdRemove(
                        templateKeys, runningMonitorType))
                .collect(Collectors.toList());
    }

    /**
     * 按支持范围执行互斥的新版删除或现有 RWD 缓存删除。
     *
     * @param templateKeys       待删除的模板键
     * @param useObjectModel 是否删除新版对象模型范围
     * @param removeActions      已准备的扩展删除动作
     * @since v7.260701.0
     */
    private void removePreparedKeys(
            @NonNull Collection<String> templateKeys,
            boolean useObjectModel,
            @NonNull Collection<CacheWriteAction> removeActions) {
        if (useObjectModel) {
            this.cacheObjectModelWriteService.removeCacheKeys(
                    templateKeys, removeActions);
            return;
        }
        // 尚未接入新模型的系统保持现有顺序：先删除对象缓存，再执行其扩展表清理。
        for (String templateKey : templateKeys) {
            this.cacheRwdDomainService.deleteByCustomIndexKey(templateKey);
        }
        this.executeWriteActions(removeActions);
    }

    /**
     * 在任何删除发生前准备单个模板的完整写入数据。
     *
     * @param templateTypeModel 模板模型
     * @return 已完成远程计算的模板写入
     * @throws BusinessException 模板数据或远程对象结果不合法时抛出
     * @since v7.260701.0
     */
    private @NonNull PreparedTemplateWrite prepareTemplate(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel) {
        String cacheKey = templateTypeModel.getTemplateTypeKey();
        RunningMonitorTypeEnum runningMonitorType = templateTypeModel.getRunningMonitorType();
        boolean useObjectModel = this.cacheObjectModelStatusService.isSupported(runningMonitorType);
        boolean allowCache = templateTypeModel.allowCache();
        // 支持类型中未开放缓存的模板不会执行任何新版写入，无需先发起无效的 RWD 远程查询。
        List<DtpRwdObjectDTO> objects = useObjectModel && !allowCache
                ? Collections.emptyList()
                : templateTypeModel.queryTemplateObjectsForCacheWrite();
        DtpRwdObjectQueryParam relationQueryParam = useObjectModel && allowCache
                ? templateTypeModel.toDtpRwdObjectQueryParam() : null;
        List<CacheWriteAction> actions;
        if (useObjectModel) {
            actions = allowCache
                    ? this.writeExtensions.stream()
                            .map(extension -> extension.prepareModelSave(
                                    templateTypeModel, objects))
                            .collect(Collectors.toList())
                    : Collections.emptyList();
        } else {
            actions = this.writeExtensions.stream()
                    .map(extension -> extension.prepareRwdSave(
                            templateTypeModel, objects))
                    .collect(Collectors.toList());
        }
        List<PreparedObjectRelation> objectRelations = useObjectModel && allowCache
                ? this.prepareObjectRelations(objects, relationQueryParam)
                : Collections.emptyList();
        return new PreparedTemplateWrite(
                cacheKey, templateTypeModel,
                useObjectModel, allowCache,
                objects, objectRelations, actions);
    }

    /**
     * 在任何本地写入前准备模板查询携带的通用对象关系。
     *
     * @param objects 模板对象完整快照
     * @param relationQueryParam 模板关系查询参数
     * @return 按查询方向规范化的关系对象快照
     * @since v7.260701.0
     */
    private @NonNull List<PreparedObjectRelation> prepareObjectRelations(
            @NonNull Collection<? extends DtpRwdObjectDTO> objects,
            @Nullable DtpRwdObjectQueryParam relationQueryParam) {
        if (relationQueryParam == null) {
            return Collections.emptyList();
        }
        Set<String> objectIds = objects.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (relationQueryParam.getId() != null) {
            objectIds.retainAll(new LinkedHashSet<>(relationQueryParam.getId()));
        }

        Map<DtpRwdRelationTypeEnum, Map<String, DtpRwdObjectDTO>> relationsByType =
                new LinkedHashMap<>();
        if (relationQueryParam.getRelationFrom() != null) {
            DtpRwdObjectRelationQueryParam relation =
                    relationQueryParam.getRelationFrom();
            DtpRwdRelationTypeEnum relationType = this.mapRelationType(relation);
            this.mergeRelationObjects(
                    relationsByType,
                    relationType,
                    this.listRelationFromObjects(
                            objectIds, relation.getObjTo(), relationType));
        }
        if (relationQueryParam.getRelationTo() != null) {
            DtpRwdObjectRelationQueryParam relation = relationQueryParam.getRelationTo();
            DtpRwdRelationTypeEnum relationType = this.mapRelationType(relation);
            this.mergeRelationObjects(
                    relationsByType,
                    relationType,
                    this.listInvertedRelationToObjects(
                            objectIds, relation.getObjFrom(), relationType));
        }
        List<PreparedObjectRelation> result = new ArrayList<>(relationsByType.size());
        relationsByType.forEach((relationType, relationObjects) ->
                result.add(new PreparedObjectRelation(
                        relationType, new ArrayList<>(relationObjects.values()))));
        return result;
    }

    /**
     * 按关系类型和主体合并不同查询方向取得的完整关系结果。
     *
     * <p>模板可以同时配置同一关系类型的正向和反向条件。关系写入口按主体执行完整替换，
     * 因此两个方向必须先合并目标集合，再对该主体提交一次，避免后一次覆盖前一次。</p>
     *
     * @param relationsByType 已按关系类型分组的主体快照
     * @param relationType 当前关系类型
     * @param relationObjects 当前查询方向归一化后的主体快照
     * @since v7.260701.0
     */
    private void mergeRelationObjects(
            @NonNull Map<DtpRwdRelationTypeEnum, Map<String, DtpRwdObjectDTO>> relationsByType,
            @NonNull DtpRwdRelationTypeEnum relationType,
            @NonNull Collection<DtpRwdObjectDTO> relationObjects) {
        Map<String, DtpRwdObjectDTO> merged = relationsByType.computeIfAbsent(
                relationType, ignored -> new LinkedHashMap<>());
        for (DtpRwdObjectDTO relationObject : relationObjects) {
            DtpRwdObjectDTO existing = merged.get(relationObject.getId());
            if (existing == null) {
                merged.put(relationObject.getId(), relationObject);
                continue;
            }
            if (relationObject.getRelObjIds() != null) {
                existing.getRelObjIds().addAll(relationObject.getRelObjIds());
            }
        }
    }

    /**
     * 查询并补齐正向关系主体，未命中的主体保留空目标集合用于清理旧关系。
     *
     * @param subjectIds 关系主体 ID
     * @param targetIds 允许的关系目标 ID
     * @param relationType 关系类型
     * @return 关系主体 DTO
     * @since v7.260701.0
     */
    private @NonNull List<DtpRwdObjectDTO> listRelationFromObjects(
            @NonNull Collection<String> subjectIds,
            @Nullable Collection<String> targetIds,
            @NonNull DtpRwdRelationTypeEnum relationType) {
        Map<String, DtpRwdObjectDTO> normalized =
                CacheTemplateRebuildService.emptyRelationObjects(subjectIds);
        if (normalized.isEmpty() || CollectionUtils.isEmpty(targetIds)) {
            return new ArrayList<>(normalized.values());
        }

        Set<String> expectedTargetIds = new LinkedHashSet<>(targetIds);
        DtpRwdObjectQueryParam query = new DtpRwdObjectQueryParam();
        query.setId(new ArrayList<>(normalized.keySet()));
        query.setRelationFrom(new DtpRwdObjectRelationQueryParam(relationType)
                .setObjTo(new ArrayList<>(expectedTargetIds)));
        query.addWithColumn(
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
        for (DtpRwdObjectDTO relationResult : this.dtpRwdClientService.listObject(query)) {
            DtpRwdObjectDTO normalizedObject = normalized.get(relationResult.getId());
            if (normalizedObject == null || relationResult.getRelObjIds() == null) {
                continue;
            }
            relationResult.getRelObjIds().stream()
                    .filter(expectedTargetIds::contains)
                    .forEach(normalizedObject.getRelObjIds()::add);
        }
        return new ArrayList<>(normalized.values());
    }

    /**
     * 查询反向筛选结果并转换成原始关系方向的主体 DTO。
     *
     * <p>RWD 的 {@code relationTo} 返回目标对象，且其 {@code relObjIds} 是来源主体。
     * 写入前按来源主体反转为 {@code id = sourceId, relObjIds = targetIds}。</p>
     *
     * @param targetIds 查询返回侧对象 ID
     * @param sourceIds 原始关系主体 ID
     * @param relationType 关系类型
     * @return 原始关系方向的主体 DTO
     * @since v7.260701.0
     */
    private @NonNull List<DtpRwdObjectDTO> listInvertedRelationToObjects(
            @NonNull Collection<String> targetIds,
            @Nullable Collection<String> sourceIds,
            @NonNull DtpRwdRelationTypeEnum relationType) {
        Collection<String> actualSourceIds = sourceIds == null
                ? Collections.emptyList() : sourceIds;
        Map<String, DtpRwdObjectDTO> normalized =
                CacheTemplateRebuildService.emptyRelationObjects(actualSourceIds);
        if (normalized.isEmpty() || targetIds.isEmpty()) {
            return new ArrayList<>(normalized.values());
        }

        Set<String> expectedTargetIds = new LinkedHashSet<>(targetIds);
        DtpRwdObjectQueryParam query = new DtpRwdObjectQueryParam();
        query.setId(new ArrayList<>(expectedTargetIds));
        query.setRelationTo(new DtpRwdObjectRelationQueryParam(relationType)
                .setObjFrom(new ArrayList<>(normalized.keySet())));
        query.addWithColumn(
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
        for (DtpRwdObjectDTO relationResult : this.dtpRwdClientService.listObject(query)) {
            if (!expectedTargetIds.contains(relationResult.getId())
                    || relationResult.getRelObjIds() == null) {
                continue;
            }
            for (String sourceId : relationResult.getRelObjIds()) {
                DtpRwdObjectDTO normalizedObject = normalized.get(sourceId);
                if (normalizedObject != null) {
                    normalizedObject.getRelObjIds().add(relationResult.getId());
                }
            }
        }
        return new ArrayList<>(normalized.values());
    }

    /**
     * 为每个关系主体创建空目标 DTO，并保持调用方顺序和去重语义。
     *
     * @param subjectIds 关系主体 ID
     * @return 主体 ID 到空关系 DTO 的映射
     * @since v7.260701.0
     */
    private static @NonNull Map<String, DtpRwdObjectDTO> emptyRelationObjects(
            @NonNull Collection<String> subjectIds) {
        Map<String, DtpRwdObjectDTO> result = new LinkedHashMap<>();
        for (String subjectId : subjectIds) {
            DtpRwdObjectDTO relationObject = new DtpRwdObjectDTO();
            relationObject.setId(subjectId);
            relationObject.setRelObjIds(new LinkedHashSet<>());
            result.put(subjectId, relationObject);
        }
        return result;
    }

    /**
     * 把模板 JSON 中的图谱、关系编码映射到稳定关系枚举。
     *
     * @param relation 模板关系参数
     * @return 对应关系枚举
     * @throws BusinessException 图谱和关系编码不受支持时抛出
     * @since v7.260701.0
     */
    private @NonNull DtpRwdRelationTypeEnum mapRelationType(
            @NonNull DtpRwdObjectRelationQueryParam relation) {
        for (DtpRwdRelationTypeEnum relationType : DtpRwdRelationTypeEnum.values()) {
            if (Objects.equals(relation.getGraphCode(), relationType.value().getLeft())
                    && Objects.equals(
                            relation.getRelCode(), relationType.value().getRight())) {
                return relationType;
            }
        }
        throw CacheObjectModelExceptionFactory.invalidStructure("relationType");
    }

    /**
     * 执行已经完成远程准备的单个模板写入。
     *
     * @param prepared 已准备写入
     * @since v7.260701.0
     */
    private void executePreparedWrite(@NonNull PreparedTemplateWrite prepared) {
        if (prepared.useObjectModel) {
            if (!prepared.allowCache) {
                return;
            }
            // 末端、照明模板只替换新版七表；其他系统仍在下方使用现有 RWD 缓存写入。
            boolean replaced = this.cacheObjectModelWriteService.replaceModelCacheKey(
                    prepared.cacheKey,
                    prepared.sourceObjects,
                    prepared.writeActions);
            if (!replaced) {
                throw CacheObjectModelExceptionFactory.operationFailed(
                        "replaceModelCacheKey");
            }
            for (PreparedObjectRelation objectRelation : prepared.objectRelations) {
                this.cacheObjectModelWriteService.replaceObjectRelations(
                        objectRelation.relationType, objectRelation.relationObjects);
            }
            return;
        }
        this.replaceRwdTemplate(prepared);
    }

    /**
     * 使用现有服务替换尚未接入新模型系统的 RWD 对象和扩展缓存。
     *
     * <p>该路径保持现有顺序执行边界，不进入新版对象模型事务，也不会删除任何新版表。</p>
     *
     * @param prepared 已完成远程准备的现有 RWD 模板写入
     * @throws BusinessException RWD 对象保存失败时抛出
     * @since v7.260701.0
     */
    private void replaceRwdTemplate(@NonNull PreparedTemplateWrite prepared) {
        List<CacheRwdObject> cacheRwdObjects = new ArrayList<>(prepared.sourceObjects.size());
        for (DtpRwdObjectDTO object : prepared.sourceObjects) {
            cacheRwdObjects.add(CacheRwdObject.toAddObject(prepared.templateTypeModel, object));
        }
        if (CollectionUtils.isEmpty(cacheRwdObjects)) {
            this.cacheRwdDomainService.deleteByCustomIndexKey(prepared.cacheKey);
        } else if (!this.cacheRwdDomainService.saveByCustomIndexKey(
                prepared.cacheKey, cacheRwdObjects)) {
            throw CacheObjectModelExceptionFactory.invalidStructure("cacheRwdObject");
        }
        this.executeWriteActions(prepared.writeActions);
    }

    /**
     * 按准备顺序执行当前存储路径的本地扩展动作。
     *
     * @param writeActions 已完成远程准备的写入动作
     * @since v7.260701.0
     */
    private void executeWriteActions(@NonNull Collection<CacheWriteAction> writeActions) {
        for (CacheWriteAction writeAction : writeActions) {
            writeAction.execute();
        }
    }

    /**
     * 已完成远程计算的模板写入数据。
     *
     * <p>作为“锁外准备、锁内提交”两段式执行的不透明句柄：调用方只负责在
     * {@link #prepareTemplates} 与 {@link #executePreparedTemplates} 之间传递，
     * 不得读取或修改内部字段。</p>
     *
     * @since v7.260701.0
     */
    public static final class PreparedTemplateWrite {

        /**
         * 缓存键。
         *
         * @since v7.260701.0
         */
        private final @NonNull String cacheKey;

        /**
         * 是否使用新版对象模型路径。
         *
         * @since v7.260701.0
         */
        private final boolean useObjectModel;

        /**
         * 是否允许模板对象缓存。
         *
         * @since v7.260701.0
         */
        private final boolean allowCache;

        /**
         * 当前 RWD 对象快照。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<DtpRwdObjectDTO> sourceObjects;

        /**
         * 调用方已经准备好的通用关系完整快照。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<PreparedObjectRelation> objectRelations;

        /**
         * 已准备的扩展写入动作。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<CacheWriteAction> writeActions;

        /**
         * 原始模板模型，仅供现有 RWD 对象转换使用。
         *
         * @since v7.260701.0
         */
        private final @NonNull InfoCodeTemplateTypeModel templateTypeModel;

        /**
         * 创建已准备模板写入。
         *
         * @param cacheKey 缓存键
         * @param templateTypeModel 原始模板模型
         * @param useObjectModel 是否使用新版对象模型路径
         * @param allowCache 是否允许模板对象缓存
         * @param sourceObjects RWD 快照
         * @param objectRelations 通用关系完整快照
         * @param writeActions 扩展动作
         * @since v7.260701.0
         */
        private PreparedTemplateWrite(
                @NonNull String cacheKey,
                @NonNull InfoCodeTemplateTypeModel templateTypeModel,
                boolean useObjectModel,
                boolean allowCache,
                @NonNull List<DtpRwdObjectDTO> sourceObjects,
                @NonNull List<PreparedObjectRelation> objectRelations,
                @NonNull List<CacheWriteAction> writeActions) {
            super();
            this.cacheKey = cacheKey;
            this.useObjectModel = useObjectModel;
            this.allowCache = allowCache;
            this.sourceObjects = sourceObjects;
            this.objectRelations = objectRelations;
            this.writeActions = writeActions;
            this.templateTypeModel = templateTypeModel;
        }
    }

    /**
     * 已完成远程查询和方向规范化的单类通用关系写入。
     *
     * @since v7.260701.0
     */
    private static final class PreparedObjectRelation {

        /**
         * 当前完整关系快照的关系类型。
         *
         * @since v7.260701.0
         */
        private final @NonNull DtpRwdRelationTypeEnum relationType;

        /**
         * 以主体 ID 和完整目标集合表示的关系对象。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<DtpRwdObjectDTO> relationObjects;

        /**
         * 创建一个已完成查询方向归一化的关系写入。
         *
         * @param relationType 关系类型
         * @param relationObjects 关系主体完整快照
         * @since v7.260701.0
         */
        private PreparedObjectRelation(
                @NonNull DtpRwdRelationTypeEnum relationType,
                @NonNull List<DtpRwdObjectDTO> relationObjects) {
            super();
            this.relationType = relationType;
            this.relationObjects = relationObjects;
        }
    }
}
