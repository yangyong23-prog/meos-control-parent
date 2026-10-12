package com.persagy.apccontrol.basic.structure.lighitng.converter;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingLampFuncTypeTagDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSpaceTagDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingInterventionTypeEnum;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.model.vo.lighting.*;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdCircuitVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Objects;

/**
 * 需求管理-需求日历模版转换器
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LightingConverter extends BaseConvert {

    // ------------------------- To Vo -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    LightingSpaceTagVO toSpaceTagVo(LightingSpaceTagDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    @Mapping(target = "lampFuncType", ignore = true)
    LightingHoverTagVO toHoverTagVo(LightingSpaceTagDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    @Mapping(target = "spaceId", ignore = true)
    LightingHoverTagVO toHoverTagVo(LightingLampFuncTypeTagDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    @Mapping(target = "openCloseStatus", ignore = true)
    @Mapping(target = "abnormalStatuses", ignore = true)
    @Mapping(target = "interventionTypes", ignore = true)
    @Mapping(target = "runningDeviationTypes", ignore = true)
    @Mapping(target = "demandFeedbackAdjustment", ignore = true)
    @Mapping(target = "funcTypes", ignore = true)
    @Mapping(target = "lampFuncTypes", ignore = true)
    @Mapping(target = "relControllers", ignore = true)
    @Mapping(target = "workPlaneLux", ignore = true)
    LightingSpaceWithStatusVO toSpaceWithStatusVo(DtpRwdSpaceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240501.0
     */
    @Mapping(target = "lampFuncTypes", ignore = true)
    LightingFloorWithStatusVO toFloorWithStatusVo(DtpRwdObjectDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "relControllers", ignore = true)
    @Mapping(target = "relCircuits", ignore = true)
    @Mapping(target = "relFloor", ignore = true)
    LightingSpaceDetailVO toSpaceDetailVo(DtpRwdObjectDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "runningParameters", ignore = true)
    @Mapping(target = "settingParameters", ignore = true)
    @Mapping(target = "abnormalEvents", ignore = true)
    @Mapping(target = "relController", ignore = true)
    @Mapping(target = "relSpace", ignore = true)
    @Mapping(target = "relDistributionCabinet", ignore = true)
    @Mapping(target = "restrictInfos", ignore = true)
    LightingCircuitDetailVO toCircuitDetailVo(DtpRwdCircuitVO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "abnormalEvents", ignore = true)
    @Mapping(target = "relCircuits", ignore = true)
    @Mapping(target = "relSpace", ignore = true)
    @Mapping(target = "relDistributionCabinet", ignore = true)
    @Mapping(target = "relFloor", ignore = true)
    LightingControllerDetailVO toControllerDetailVo(DtpRwdObjectDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    default BaseEnumVO toBaseEnumVo(LightingInterventionTypeEnum source) {
        if (Objects.isNull(source)) {
            return null;
        }
        return new BaseEnumVO(source.value(), source.getViewValue());
    }
}
