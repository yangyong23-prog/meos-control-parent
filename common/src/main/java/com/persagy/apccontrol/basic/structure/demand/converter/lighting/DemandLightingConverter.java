package com.persagy.apccontrol.basic.structure.demand.converter.lighting;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandLightingCircuitVO;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandLightingFloorWithCircuitVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 需求配置-照明需求转换器
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Mapper(
        componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = {DtpRwdConvert.class, DtpRwdClassCodeEnum.class}
)
public interface DemandLightingConverter extends BaseConvert {

    /**
     * 转换模型
     *
     * @param source      数据源
     * @param relCircuits 关联照明回路
     * @return 模型
     * @since 24Q1 T3S2-5.18.0
     */
    DemandLightingFloorWithCircuitVO toFloorWithCircuitVo(
            DtpRwdFloorDTO source,
            Collection<DemandLightingCircuitVO> relCircuits);

    /**
     * 转换模型
     *
     * @param source      数据源
     * @param settingMode 配置模式
     * @return 模型
     * @since 24Q1 T3S2-5.18.0
     */
    @Mapping(target = "serviceAreaType", expression = "java(toEnumVo(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT, DtpRwdConvert.INFO_CODE_SERVICE_AREA_TYPE, source.getServiceAreaType()))")
    @Mapping(target = "funcType", expression = "java(toEnumVo(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT, DtpRwdConvert.INFO_CODE_FUNC_TYPE, source.getFuncType()))")
    @Mapping(target = "lampFuncType", expression = "java(toEnumVo(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT, DtpRwdConvert.INFO_CODE_LAMP_FUNC_TYPE, source.getLampFuncType()))")
    DemandLightingCircuitVO toCircuitVo(
            DtpRwdCircuitDTO source,
            DemandLightingCircuitSettingModeEnum settingMode);

    // ------------------------- To Dto -------------------------

    /**
     * 转换模型
     *
     * @param source        数据源
     * @param beginDateTime 场景开始时间
     * @param endDateTime   场景结束时间
     * @return 模型
     * @since 5.240402.0
     */
    @Mapping(target = "sceneId", source = "source.id")
    @Mapping(target = "isCurrentScene", ignore = true)
    @Mapping(target = "relDemandTemplateId", source = "source.relTemplateId")
    LightingSceneDTO toLightingSceneDto(
            DemandSceneEntity source,
            LocalDateTime beginDateTime,
            LocalDateTime endDateTime);

}
