package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.watersupply.WaterSupplyModelViewQueryParam;
import com.persagy.apccontrol.basic.model.vo.instance.EquipmentVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectOrderParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生活给水控制器
 *
 * @author JiYinchuan
 * @since 7.251202.0
 */
@Validated
@Component(RunningMonitorWaterSupplyStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorWaterSupplyStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorWaterSupplyStrategy";

    /**
     * 模型图设备查询字段
     */
    private final String[] modelViewEquipmentWithColumns = new String[]{
            DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.classCode};

    private final DtpRwdConvert dtpRwdConvert;
    private final DtpRwdClientService dtpRwdClientService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.WATER_SUPPLY;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull @Valid RunningMonitorSpaceQueryParam param) {
        return Collections.emptyList();
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipments(@NonNull @Valid RunningMonitorEquipmentQueryParam param) {
        DtpRwdObjectOrderParam orderParam = new DtpRwdObjectOrderParam();
        orderParam.setColumn(DtpRwdObjectDTO.Fields.localName);
        orderParam.setAsc(true);
        orderParam.setChineseOrder(true);
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setOrderCustoms(Collections.singletonList(orderParam))
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        List<DtpRwdEquipmentDTO> equipments = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(equipments)) {
            return Collections.emptyList();
        }
        return equipments;
    }

    /**
     * 获取模型图设备
     *
     * @param param 查询参数
     * @param type  查询类型
     * @return 模型图设备
     * @since 7.251202.0
     */
    public List<EquipmentVO> listEquipmentsToModuleView(
            @NonNull WaterSupplyModelViewQueryParam param,
            @NonNull String type) {
        List<DtpRwdEquipmentDTO> equipments;
        switch (type) {
            case "SYSTEM_RELATED_EQUIPMENT":
                equipments = querySystemRelEquipments(param, true);
                break;
            case "EQUIPMENT_IN_SYSTEM":
                equipments = querySystemRelEquipments(param, false);
                break;
            case "EQUIPMENT_IN_SPACE":
                equipments = queryEquipmentInSpace(param);
                break;
            default:
                return Collections.emptyList();
        }
        List<EquipmentVO> equipmentVos = equipments.stream()
                .map(dtpRwdConvert::toEquipmentVo)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(equipmentVos)) {
            Set<String> classCodes = equipments.stream().map(DtpRwdEquipmentDTO::getClassCode).collect(Collectors.toSet());
            Map<String, String> classCodeAndName = this.dtpKnowledgeClientService.mapClassCodeAndName(true, false, classCodes);
            equipmentVos.forEach(equipment -> equipment.setClassCodeName(classCodeAndName.get(equipment.getClassCode())));
        }
        return equipmentVos;
    }

    /**
     * 查询机房内的全部设备
     *
     * @param param 查询参数
     * @return 机房内的全部设备
     * @since 7.251202.0
     */
    private List<DtpRwdEquipmentDTO> queryEquipmentInSpace(WaterSupplyModelViewQueryParam param) {
        String spaceId = param.getSpaceId();
        String[] withColumns = param.getWithColumns();
        return this.dtpRwdClientService.listEquipmentInBySpaceIds(Lists.newArrayList(spaceId),
                new DtpRwdObjectQueryParam().addWithColumn(modelViewEquipmentWithColumns).addWithColumn(withColumns));
    }

    /**
     * 查询系统关联的设备
     *
     * @param param     查询参数
     * @param queryDoor 是否查询安防设备
     * @return 系统关联的设备
     * @since 7.251202.0
     */
    private List<DtpRwdEquipmentDTO> querySystemRelEquipments(WaterSupplyModelViewQueryParam param, boolean queryDoor) {
        String systemId = param.getSystemId();
        String spaceId = param.getSpaceId();
        Set<String> equipmentIdsInGraph = param.getEquipmentIdsInGraph();
        String[] withColumns = param.getWithColumns();

        DtpRwdObjectDTO system = dtpRwdClientService.getObject(new DtpRwdObjectQueryParam()
                .setId(systemId).addWithColumn(DtpRwdObjectDTO.Fields.objType));
        if (system == null) {
            return Collections.emptyList();
        }
        DtpRwdObjectTypeEnum objectType = DtpRwdObjectTypeEnum.analyze(system.getObjType());
        if (objectType == DtpRwdObjectTypeEnum.EQUIPMENT && equipmentIdsInGraph == null) {
            throw new ValidationException("EquipmentIdsInGraph can not be null");
        }

        Set<String> specialEquipmentClassCodes = Sets.newHashSet(
                // 摄像头
                DtpRwdClassCodeEnum.SPVSCM.getValue(),
                // 传感器
                DtpRwdClassCodeEnum.WESSTP.getValue(), DtpRwdClassCodeEnum.WESSWV.getValue(),
                DtpRwdClassCodeEnum.WESSCT.getValue(), DtpRwdClassCodeEnum.WESSFL.getValue(),
                DtpRwdClassCodeEnum.WESSFD.getValue(), DtpRwdClassCodeEnum.WESSTH.getValue(),
                DtpRwdClassCodeEnum.WESSCP.getValue(), DtpRwdClassCodeEnum.WESSCO.getValue(),
                DtpRwdClassCodeEnum.WESSTO.getValue(), DtpRwdClassCodeEnum.WESSPE.getValue(),
                DtpRwdClassCodeEnum.WESSIL.getValue(), DtpRwdClassCodeEnum.WESSCB.getValue(),
                DtpRwdClassCodeEnum.WESSWL.getValue()
        );
        if (queryDoor) {
            specialEquipmentClassCodes.add(DtpRwdClassCodeEnum.CFCSDR.getValue());
            specialEquipmentClassCodes.add(DtpRwdClassCodeEnum.CFCSFR.getValue());
            specialEquipmentClassCodes.add(DtpRwdClassCodeEnum.CFCSED.getValue());
        }

        // 查询空间下的所有设备
        List<DtpRwdEquipmentDTO> equipmentsInSpace = this.dtpRwdClientService.listEquipmentInBySpaceIds(
                Lists.newArrayList(spaceId), new DtpRwdObjectQueryParam()
                        .addWithColumn(modelViewEquipmentWithColumns).addWithColumn(withColumns));
        if (CollectionUtils.isEmpty(equipmentsInSpace)) {
            return Collections.emptyList();
        }
        // 空间下所有设备ID
        Set<String> equipmentIdsInSpace = new HashSet<>();
        // 空间下摄像头/传感器/安防设备ID
        Set<String> specialEquipmentIds = new HashSet<>();
        Map<String, DtpRwdEquipmentDTO> equipmentId2Data = new HashMap<>();
        for (DtpRwdEquipmentDTO equipment : equipmentsInSpace) {
            String equipmentId = equipment.getId();
            String classCode = equipment.getClassCode();
            equipmentIdsInSpace.add(equipmentId);
            if (specialEquipmentClassCodes.contains(classCode)) {
                specialEquipmentIds.add(equipmentId);
            }
            equipmentId2Data.put(equipmentId, equipment);
        }
        Set<String> equipmentIdsInSystem;
        switch (objectType) {
            case SYSTEM:
                // 根据空间下的设备ID查询系统下的设备ID，从而得到空间下和系统下设备的交集
                equipmentIdsInSystem = this.dtpRwdClientService.listEquipmentInBySystemIds(
                                Lists.newArrayList(systemId), new DtpRwdObjectQueryParam()
                                        .setId(equipmentIdsInSpace)
                                        .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                break;
            case EQUIPMENT:
                // 为设备时取关系图中的设备
                equipmentIdsInSystem = equipmentIdsInGraph;
                break;
            default:
                equipmentIdsInSystem = Collections.emptySet();
        }

        // 得到最终所有设备的ID
        Collection<String> finalEquipmentIds = CollectionUtils.union(equipmentIdsInSystem, specialEquipmentIds);
        if (CollectionUtils.isEmpty(finalEquipmentIds)) {
            return Collections.emptyList();
        }

        List<DtpRwdEquipmentDTO> equipments = new ArrayList<>();
        for (String equipmentId : finalEquipmentIds) {
            DtpRwdEquipmentDTO equipment = equipmentId2Data.get(equipmentId);
            if (equipment != null) {
                equipments.add(equipment);
            }
        }
        equipments.sort(Comparator.comparing(DtpRwdEquipmentDTO::getLocalName,
                Comparator.nullsLast(Collator.getInstance(Locale.CHINA))));
        return equipments;
    }

    @Override
    public String logTag() {
        return "运行监测-生活给水";
    }
}
