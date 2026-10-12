package com.persagy.apccontrol.basic.structure.computeroom.converter;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.vo.computeroom.ComputerRoomDashboardSpaceVO;
import com.persagy.apccontrol.basic.model.vo.computeroom.ComputerRoomEquipmentVo;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 机房安全转换器
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ComputerRoomConverter extends BaseConvert {

    // ------------------------- To Vo -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 7.250201.0
     */
    @Mapping(target = "roomFuncTypeName", ignore = true)
    @Mapping(target = "equipmentsInSpace", ignore = true)
    ComputerRoomDashboardSpaceVO toDashboardSpaceVo(DtpRwdSpaceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 7.250902.0
     */
    @Mapping(target = "classCodeName", ignore = true)
    @Mapping(target = "equipCriticalLevel", ignore = true)
    ComputerRoomEquipmentVo toEquipmentVo(DtpRwdEquipmentDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 7.250902.0
     */
    @InheritConfiguration
    List<ComputerRoomEquipmentVo> toEquipmentVo(List<DtpRwdEquipmentDTO> source);

}
