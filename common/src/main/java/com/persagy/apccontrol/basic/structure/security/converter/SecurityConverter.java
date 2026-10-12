package com.persagy.apccontrol.basic.structure.security.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.vo.security.SecurityEquipmentStatusVO;
import com.persagy.apccontrol.basic.model.vo.security.SecuritySpaceStatusVO;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationEquipmentStatusVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 空间安防对象转换
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/4 20:00
 * @since v5.240801.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SecurityConverter {

    /**
     * 将空间对象DTO转换为VO，忽略空间通行，撤布防，异常状态的转换
     *
     * @param source 源
     * @return {@link SecuritySpaceStatusVO }
     */
    @Mapping(target = "deploymentStatus", ignore = true)
    @Mapping(target = "abnormal", ignore = true)
    @Mapping(target = "accessDemandStatus", ignore = true)
    SecuritySpaceStatusVO toSpaceStatusVo(DtpRwdSpaceDTO source);

    /**
     * 将设备对象DTO转换为VO
     *
     * @param source 源
     * @return {@link VentilationEquipmentStatusVO }
     */
    @Mapping(target = "abnormalTypes", ignore = true)
    @Mapping(target = "isControl", ignore = true)
    @Mapping(target = "cameraIsControl", ignore = true)
    @Mapping(target = "iconType", ignore = true)
    @Mapping(target = "runStatus", ignore = true)
    @Mapping(target = "accessDemandStatus", ignore = true)
    SecurityEquipmentStatusVO toEquipmentStatusVo(SecurityDtpRwdEquipmentDTO source);

}
