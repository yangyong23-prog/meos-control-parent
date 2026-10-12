package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheWriteAction;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.BaseCacheRwdStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 安防静态对象、空间和设备空间关系缓存构建策略。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
@Component(CacheRwdSecurityStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdSecurityStrategy extends BaseCacheRwdStrategy {

    /**
     * Spring Bean 名称。
     *
     * @since v7.260802.0
     */
    public static final @NonNull String BEAN_NAME = "cacheRwdSecurityStrategy";

    /**
     * 安防设备固定缓存范围。
     *
     * @since v7.260802.0
     */
    private static final @NonNull List<DtpRwdClassCodeEnum> SECURITY_CLASS_CODES =
            Collections.unmodifiableList(Arrays.asList(
                    DtpRwdClassCodeEnum.CFCSDR,
                    DtpRwdClassCodeEnum.CFCSFR,
                    DtpRwdClassCodeEnum.SPEGFG,
                    DtpRwdClassCodeEnum.SPVSCM,
                    DtpRwdClassCodeEnum.WEGMCM,
                    DtpRwdClassCodeEnum.SPIACU,
                    DtpRwdClassCodeEnum.SPIAGD,
                    DtpRwdClassCodeEnum.SPIAID,
                    DtpRwdClassCodeEnum.SPIAMA,
                    DtpRwdClassCodeEnum.SPEGCU,
                    DtpRwdClassCodeEnum.SPVSVE,
                    DtpRwdClassCodeEnum.SPVSDR));

    /**
     * 需要维护设备所在空间关系的安防设备类。
     *
     * <p>三个门类用于通行空间，SPVSCM 用于布撤防空间；SPVSCM 和 WEGMCM 还用于设备
     * 响应的空间功能类型，因此两类摄像头均保留完整所在空间快照。</p>
     *
     * @since v7.260802.0
     */
    private static final @NonNull Set<String> IN_SPACE_CLASS_CODES =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    DtpRwdClassCodeEnum.CFCSDR.value(),
                    DtpRwdClassCodeEnum.CFCSFR.value(),
                    DtpRwdClassCodeEnum.SPEGFG.value(),
                    DtpRwdClassCodeEnum.SPVSCM.value(),
                    DtpRwdClassCodeEnum.WEGMCM.value())));

    /**
     * RWD 客户端，用于事务外准备对象与完整关系快照。
     *
     * @since v7.260802.0
     */
    private final @NonNull DtpRwdClientService dtpRwdClientService;

    /**
     * 新对象模型写入服务。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheObjectModelWriteService cacheObjectModelWriteService;

    /**
     * 新对象仓储，用于在完整重建前取得上一版安防设备主体范围。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    @Override
    public @NonNull RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.SECURITY;
    }

    @Override
    public boolean buildCacheData(RunningMonitorTypeEnum runningMonitorType) {
        List<DtpRwdSpaceDTO> spaces = this.listSpaces();
        List<SecurityDtpRwdEquipmentDTO> equipments = this.listEquipments();
        List<SecurityDtpRwdEquipmentDTO> previousEquipments =
                this.cacheObjectRepository.listByCacheKey(
                        CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT,
                        SecurityDtpRwdEquipmentDTO.class,
                        Arrays.asList(
                                DtpRwdObjectDTO.Fields.id,
                                DtpRwdObjectDTO.Fields.classCode));
        Set<String> spaceIds = spaces.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> inSpaceEquipmentIds = equipments.stream()
                .filter(item -> CacheRwdSecurityStrategy.IN_SPACE_CLASS_CODES.contains(
                        item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> serviceSpaceEquipmentIds = equipments.stream()
                .filter(item -> DtpRwdClassCodeEnum.SPIACU.value().equals(item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> inSpaceRelationSubjectIds = new LinkedHashSet<>(
                inSpaceEquipmentIds);
        previousEquipments.stream()
                .filter(item -> CacheRwdSecurityStrategy.IN_SPACE_CLASS_CODES.contains(
                        item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .forEach(inSpaceRelationSubjectIds::add);
        Set<String> serviceSpaceRelationSubjectIds = new LinkedHashSet<>(
                serviceSpaceEquipmentIds);
        previousEquipments.stream()
                .filter(item -> DtpRwdClassCodeEnum.SPIACU.value().equals(
                        item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .forEach(serviceSpaceRelationSubjectIds::add);

        CompletableFuture<List<DtpRwdObjectDTO>> serviceSpaceFuture =
                CompletableFuture.supplyAsync(
                        () -> this.listRelationFromObjects(
                                serviceSpaceEquipmentIds,
                                serviceSpaceRelationSubjectIds,
                                spaceIds,
                                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE),
                        BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture<List<DtpRwdObjectDTO>> inSpaceFuture =
                CompletableFuture.supplyAsync(
                        () -> this.listRelationFromObjects(
                                inSpaceEquipmentIds,
                                inSpaceRelationSubjectIds,
                                spaceIds,
                                DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE),
                        BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture.allOf(serviceSpaceFuture, inSpaceFuture).join();

        Map<String, Collection<? extends DtpRwdObjectDTO>> sourcesByKey =
                new LinkedHashMap<>();
        sourcesByKey.put(CacheObjectModelKeyConfig.SECURITY_LIST_SPACE, spaces);
        sourcesByKey.put(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT, equipments);
        List<CacheWriteAction> relationActions = Arrays.asList(
                this.cacheObjectModelWriteService.prepareObjectRelationsReplaceAction(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE,
                        serviceSpaceFuture.join()),
                this.cacheObjectModelWriteService.prepareObjectRelationsReplaceAction(
                        DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE,
                        inSpaceFuture.join()));
        return this.cacheObjectModelWriteService.replaceModelCacheKeys(
                sourcesByKey, relationActions);
    }

    /**
     * 查询项目全量空间静态快照。
     *
     * @return 可直接写入新对象模型的空间列表
     * @since v7.260802.0
     */
    private @NonNull List<DtpRwdSpaceDTO> listSpaces() {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(DtpRwdClassCodeEnum.SPACE);
        queryParam.addWithColumn(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                DtpRwdClassCodeEnum.SPACE.value()).toArray(new String[0]));
        return this.dtpRwdClientService.listSpace(queryParam);
    }

    /**
     * 一次查询安防固定十二类设备及其对象和模型视图字段。
     *
     * @return 安防设备完整静态快照
     * @since v7.260802.0
     */
    private @NonNull List<SecurityDtpRwdEquipmentDTO> listEquipments() {
        Set<String> fields = new LinkedHashSet<>();
        for (DtpRwdClassCodeEnum classCode : CacheRwdSecurityStrategy.SECURITY_CLASS_CODES) {
            fields.addAll(CacheObjectExtensionFieldConfig.listRwdQueryFields(classCode.value()));
        }
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(CacheRwdSecurityStrategy.SECURITY_CLASS_CODES);
        queryParam.addWithColumn(fields.toArray(new String[0]));
        return this.dtpRwdClientService.listSecurityEquipment(
                false, ContextUtil.getProjectId(), queryParam);
    }

    /**
     * 查询并规范化一类设备正向关系。
     *
     * <p>每个请求主体始终保留一条空目标快照，确保关系删除或迁移后可以清理旧值；RWD
     * 返回的主体和目标均限制在本次完整缓存范围内，并通过集合去重。</p>
     *
     * @param currentSubjectIds 本次仍属于该关系范围、需要实时查询的主体设备 ID
     * @param replacementSubjectIds 新旧范围并集，旧主体以空目标快照清理残留关系
     * @param targetIds 允许的空间 ID
     * @param relationType 关系类型
     * @return 可提交给关系替换入口的主体完整快照
     * @since v7.260802.0
     */
    private @NonNull List<DtpRwdObjectDTO> listRelationFromObjects(
            @NonNull Collection<String> currentSubjectIds,
            @NonNull Collection<String> replacementSubjectIds,
            @NonNull Collection<String> targetIds,
            @NonNull DtpRwdRelationTypeEnum relationType) {
        Map<String, DtpRwdObjectDTO> normalized = new LinkedHashMap<>();
        for (String subjectId : replacementSubjectIds) {
            DtpRwdObjectDTO relationObject = new DtpRwdObjectDTO();
            relationObject.setId(subjectId);
            relationObject.setRelObjIds(new LinkedHashSet<>());
            normalized.put(subjectId, relationObject);
        }
        if (currentSubjectIds.isEmpty() || targetIds.isEmpty()) {
            return new ArrayList<>(normalized.values());
        }

        Set<String> expectedTargetIds = new LinkedHashSet<>(targetIds);
        Set<String> expectedCurrentSubjectIds = new LinkedHashSet<>(currentSubjectIds);
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(new ArrayList<>(expectedCurrentSubjectIds));
        queryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(relationType)
                .setObjTo(new ArrayList<>(expectedTargetIds)));
        queryParam.addWithColumn(
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
        for (DtpRwdObjectDTO result : this.dtpRwdClientService.listObject(queryParam)) {
            if (!expectedCurrentSubjectIds.contains(result.getId())) {
                continue;
            }
            DtpRwdObjectDTO relationObject = normalized.get(result.getId());
            if (relationObject == null || result.getRelObjIds() == null) {
                continue;
            }
            result.getRelObjIds().stream()
                    .filter(expectedTargetIds::contains)
                    .forEach(relationObject.getRelObjIds()::add);
        }
        return new ArrayList<>(normalized.values());
    }

    @Override
    public @NonNull String logTag() {
        return "物理世界缓存-安防策略";
    }
}
