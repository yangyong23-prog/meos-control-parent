package com.persagy.apccontrol.basic.structure.weakpower.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.weakpower.RunningMonitorWeakPowerEquipmentDTO;
import com.persagy.apccontrol.basic.model.vo.weakpower.WeakPowerSystemVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import static com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorWeakPowerStrategy.RUNNING_MONITOR_SECOND_TYPE_AND_SYSTEM_CLASS_CODE;


/**
 * 弱电转换器
 *
 * @author JiYinchuan
 * @since 7.250301.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WeakPowerConverter {

    // ------------------------- To DTO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 7.250301.0
     */
    @Mapping(target = "children", ignore = true)
    RunningMonitorWeakPowerEquipmentDTO toWeakPowerEquipmentDto(DtpRwdEquipmentDTO source);

    List<RunningMonitorWeakPowerEquipmentDTO> toWeakPowerEquipmentDto(List<DtpRwdEquipmentDTO> source);

    // ------------------------- To VO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 7.250301.0
     */
    @Mapping(target = "systemId", source = "id")
    @Mapping(target = "systemLocalName", source = "localName")
    @Mapping(target = "runningMonitorSecondType", expression = "java(classCodeToRunningMonitorSecondTypeEnum(source.getClassCode()))")
    WeakPowerSystemVO toWeakPowerSystem(DtpRwdObjectDTO source);

    /**
     * rwd对象类转换为运行监控二级类型
     *
     * @param classCode 对象类
     * @return 运行监控二级类型
     */
    default RunningMonitorSecondTypeEnum classCodeToRunningMonitorSecondTypeEnum(String classCode) {
        return RUNNING_MONITOR_SECOND_TYPE_AND_SYSTEM_CLASS_CODE.getKey(DtpRwdClassCodeEnum.analyze(classCode));
    }

}
