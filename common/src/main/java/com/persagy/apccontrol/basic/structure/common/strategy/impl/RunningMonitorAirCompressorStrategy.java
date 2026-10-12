package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 空压机策略实现
 *
 * @author MuAnFu
 * @date 2025-06-11
 */
@Validated
@Component(RunningMonitorAirCompressorStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorAirCompressorStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorAirCompressorStrategy";
    public final DtpRwdClientService dtpRwdClientService;

    //系统下的设备classCode
    private static final List<DtpRwdClassCodeEnum> AIR_COMPRESSOR_CLASS_CODES = Arrays.asList(DtpRwdClassCodeEnum.IMIMIM,
            DtpRwdClassCodeEnum.FFSRAC, DtpRwdClassCodeEnum.TPASHR, DtpRwdClassCodeEnum.TPASCT, DtpRwdClassCodeEnum.TPASCF,
            DtpRwdClassCodeEnum.TPASRD, DtpRwdClassCodeEnum.TPASDD, DtpRwdClassCodeEnum.ACCCOP, DtpRwdClassCodeEnum.TPASPP,
            DtpRwdClassCodeEnum.ACCCOF, DtpRwdClassCodeEnum.ACCCOT, DtpRwdClassCodeEnum.ACOTU_GROUP,
            DtpRwdClassCodeEnum.ACVVEO, DtpRwdClassCodeEnum.ACVVEJ, DtpRwdClassCodeEnum.TPPPAS, DtpRwdClassCodeEnum.WESSTP,
            DtpRwdClassCodeEnum.WESSPE, DtpRwdClassCodeEnum.WESSFL, DtpRwdClassCodeEnum.ACPPCS, DtpRwdClassCodeEnum.ACPPCR,
            DtpRwdClassCodeEnum.ACPPCB, DtpRwdClassCodeEnum.ACPPAS, DtpRwdClassCodeEnum.ACPPAR, DtpRwdClassCodeEnum.ACPPAB,
            DtpRwdClassCodeEnum.ACPPRE, DtpRwdClassCodeEnum.ACPPRA, DtpRwdClassCodeEnum.ACPPEA, DtpRwdClassCodeEnum.ACPPSA,
            DtpRwdClassCodeEnum.ACPPFS, DtpRwdClassCodeEnum.ACPPWC
    );
    //空间下的的设备classCode
    private static final Set<DtpRwdClassCodeEnum> SPACE_IN_EQUIPMENT_CLASS_CODES = Sets.newHashSet(
            DtpRwdClassCodeEnum.SPVSCM, DtpRwdClassCodeEnum.WESSTP,
            DtpRwdClassCodeEnum.WESSWV, DtpRwdClassCodeEnum.WESSCT, DtpRwdClassCodeEnum.WESSFL, DtpRwdClassCodeEnum.WESSFD,
            DtpRwdClassCodeEnum.WESSTH, DtpRwdClassCodeEnum.WESSCP, DtpRwdClassCodeEnum.WESSCO, DtpRwdClassCodeEnum.WESSTO,
            DtpRwdClassCodeEnum.WESSPE, DtpRwdClassCodeEnum.WESSIL, DtpRwdClassCodeEnum.WESSCB, DtpRwdClassCodeEnum.WESSWL,
            DtpRwdClassCodeEnum.CFCSDR, DtpRwdClassCodeEnum.CFCSFR, DtpRwdClassCodeEnum.CFCSED
    );

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull @Valid RunningMonitorSpaceQueryParam param) {
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        DtpRwdObjectQueryParam eqQueryParam = new DtpRwdObjectQueryParam();
        eqQueryParam.setClassCode(DtpRwdClassCodeEnum.FFSRAC);
        eqQueryParam.addWithColumn(DtpRwdEquipmentDTO.listSimpleWithColumns());
        if (CollectionUtils.isNotEmpty(param.getFilterSpaceServiceSystemIds())) {
            equipmentQueryParam.setFilterEquipmentInSystemIds(param.getFilterSpaceServiceSystemIds());
            //因为查询空间是通过设备查询，所有需要清空查询空间的系统id
            param.setFilterSpaceServiceSystemIds(null);
        }
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = equipmentQueryParam.queryObject(eqQueryParam);
        if (CollectionUtils.isEmpty(dtpRwdEquipmentDTOS)) {
            return Collections.emptyList();
        }
        param.setFilterSpaceInEquipmentIds(dtpRwdEquipmentDTOS.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet()));
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addWithColumnWithSimple(DtpRwdSpaceDTO.listSimpleWithColumns());
        List<DtpRwdSpaceDTO> spaces = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptyList();
        }
        //按照名称排序
        spaces.sort(Comparator.comparing(DtpRwdSpaceDTO::getLocalName));
        return spaces;
    }

    @Override
    public List<? extends DtpRwdEquipmentDTO> listEquipments(RunningMonitorEquipmentQueryParam param) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(AIR_COMPRESSOR_CLASS_CODES);
        queryParam.addWithColumnWithSimple(DtpRwdEquipmentDTO.listSimpleWithColumns());
        //查询系统下设备
        List<DtpRwdEquipmentDTO> equipmentInSystem = param.queryObject(queryParam);
        Set<String> filterEquipmentInSpaceIds = param.getFilterEquipmentInSpaceIds();
        //如何空间不为空，则附加空间下的设备
        if (CollectionUtils.isNotEmpty(filterEquipmentInSpaceIds)) {
            DtpRwdObjectQueryParam equipmentQueryParam = new DtpRwdObjectQueryParam();
            equipmentQueryParam.setClassCodeEnum(SPACE_IN_EQUIPMENT_CLASS_CODES);
            equipmentQueryParam.addWithColumn(DtpRwdEquipmentDTO.listSimpleWithColumns());
            List<DtpRwdEquipmentDTO> equipmentInSpace = dtpRwdClientService.listEquipmentInBySpaceIds(filterEquipmentInSpaceIds, equipmentQueryParam);
            if (CollectionUtils.isNotEmpty(equipmentInSpace)) {
                equipmentInSystem.addAll(equipmentInSpace);
            }
        }
        return equipmentInSystem;
    }


    /**
     * 运行监测类型
     *
     * @return 信息点类型
     * @since 5.240702.0
     */
    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.AIR_COMPRESSOR;
    }

    /**
     * 需求继承类型
     *
     * @return 需求继承类型
     * @since 5.240702.0
     */
    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "空压机策略实现";
    }
}
