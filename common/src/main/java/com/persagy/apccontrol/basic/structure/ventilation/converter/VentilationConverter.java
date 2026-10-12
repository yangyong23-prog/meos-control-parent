package com.persagy.apccontrol.basic.structure.ventilation.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationDashboardVO;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationDtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationEquipmentStatusVO;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationSpaceStatusVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 通风转换器
 *
 * @author MAF
 * @date 2024/07/26
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VentilationConverter {

    /**
     * 转换模型
     *
     * @param source 来源
     * @return {@link VentilationSpaceStatusVO }
     */
    @Mapping(target = "spaceStatus", ignore = true)
    @Mapping(target = "isAbnormal", ignore = true)
    @Mapping(target = "isControl", ignore = true)
    @Mapping(target = "infoCodes", ignore = true)
    @Mapping(target = "equipTypes", ignore = true)
    VentilationSpaceStatusVO toSpaceStatusVo(DtpRwdSpaceDTO source);

    /**
     * 转换模型
     *
     * @param source 来源
     * @return {@link VentilationEquipmentStatusVO }
     */
    @Mapping(target = "abnormalTypes", ignore = true)
    @Mapping(target = "runStatus", ignore = true)
    @Mapping(target = "isControl", ignore = true)
    @Mapping(target = "equipType", ignore = true)
    VentilationEquipmentStatusVO toEquipmentStatusVo(DtpRwdEquipmentDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241201.0
     */
    @Mapping(target = "existsIotRuleByO2", ignore = true)
    @Mapping(target = "existsIotRuleByTdb", ignore = true)
    @Mapping(target = "existsIotRuleByCo", ignore = true)
    @Mapping(target = "existsIotRuleByNH3", ignore = true)
    @Mapping(target = "existsIotRuleByH2S", ignore = true)
    VentilationDashboardVO toDashboardVO(DtpRwdSpaceDTO source);
    
    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241201.0
     */
    @Mapping(target = "tdbQuality", ignore = true)
    @Mapping(target = "co2Quality", ignore = true)
    @Mapping(target = "o2Target", ignore = true)
    @Mapping(target = "coTarget", ignore = true)
    @Mapping(target = "nh3Target", ignore = true)
    @Mapping(target = "h2sTarget", ignore = true)
    @Mapping(target = "relObjects", ignore = true)
    VentilationDtpRwdSpaceVO toVentilationDtpRwdSpaceVO(DtpRwdSpaceDTO source);

}
