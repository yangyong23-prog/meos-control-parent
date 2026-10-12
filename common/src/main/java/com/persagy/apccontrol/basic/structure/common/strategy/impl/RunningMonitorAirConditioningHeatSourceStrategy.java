package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
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
import java.util.Collections;
import java.util.List;

/**
 * 空调热源策略实现
 *
 * @author JiYinchuan
 * @since 7.251001.0
 */
@Validated
@Component(RunningMonitorAirConditioningHeatSourceStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorAirConditioningHeatSourceStrategy
        extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorAirConditioningHeatSourceStrategy";

    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.AIR_CONDITIONING_HEAT_SOURCE;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull @Valid RunningMonitorSpaceQueryParam param) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addWithColumnWithSimple(DtpRwdObjectDTO.Fields.id);
        List<DtpRwdSpaceDTO> spaces = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptyList();
        }
        return spaces;
    }

    @Override
    // @SuppressWarnings("unchecked")
    public List<DtpRwdEquipmentDTO> listEquipments(@NonNull @Valid RunningMonitorEquipmentQueryParam param) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        List<DtpRwdEquipmentDTO> equipments = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(equipments)) {
            return Collections.emptyList();
        }
        // // 移除没有手自动状态设定信息点规则的设备
        // Map<String, Set<String>> classCodeAndIds = DtpRwdEquipmentDTO.mapClassCodeAndIds(equipments);
        // Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = abstractIotDataCacheServer
        //         .mapObjectIdAndExistsRuleInfoCodes(classCodeAndIds,
        //                 Lists.newArrayList(InfoCodeEnum.MANUAL_AUTO_SET.getCode()));
        // equipments.removeIf(equipment ->
        //         !objectIdAndExistsRuleInfoCodes.containsKey(equipment.getId()));
        // if (CollectionUtils.isEmpty(equipments)) {
        //     return Collections.emptyList();
        // }
        // // 移除本地远程状态为本地的设备
        // DynamicInfoCodeFilter infoCodeFilter = new DynamicInfoCodeFilter(
        //         InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode(),
        //         DtpKnowledgeLocalRemoteStatusEnum.LOCAL.getValue());
        // List<String> equipmentIds = equipments.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList());
        // List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(equipmentIds,
        //         Lists.newArrayList(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode()));
        // Set<String> toBeRemovedEquipmentIds = new HashSet<>();
        // for (InfoCodeValueVO infoCodeValue : infoCodeValues) {
        //     if (infoCodeFilter.equalsDataValue(infoCodeValue)) {
        //         toBeRemovedEquipmentIds.add(infoCodeValue.getObjectId());
        //     }
        // }
        // if (CollectionUtils.isNotEmpty(toBeRemovedEquipmentIds)) {
        //     equipments.removeIf(equipment -> toBeRemovedEquipmentIds.contains(equipment.getId()));
        // }
        return equipments;
    }

    @Override
    public String logTag() {
        return "运行监测-空调热源";
    }
}
