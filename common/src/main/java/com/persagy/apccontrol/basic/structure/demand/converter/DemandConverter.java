package com.persagy.apccontrol.basic.structure.demand.converter;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.param.demand.DemandTreeInstanceQueryParam;
import com.persagy.apccontrol.basic.model.param.external.demand.DemandTreeInstanceExternalQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.*;
import com.persagy.apccontrol.basic.model.vo.external.demand.*;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 需求管理转换器
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Mapper(
        componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = DemandInstanceTypeEnum.class
)
public interface DemandConverter extends BaseConvert {

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "instanceType", expression = "java(DemandInstanceTypeEnum.SPACE)")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "floorSequenceId", ignore = true)
    @Mapping(target = "height", ignore = true)
    @Mapping(target = "floorElevation", ignore = true)
    @Mapping(target = "buildingSequenceId", ignore = true)
    DemandTreeBuildingFloorSpaceVO toTreeBuildingFloorSpaceVo(DtpRwdSpaceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "instanceType", expression = "java(DemandInstanceTypeEnum.FLOOR)")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "buildingSequenceId", ignore = true)
    DemandTreeBuildingFloorSpaceVO toTreeBuildingFloorSpaceVo(DtpRwdFloorDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "instanceType", expression = "java(DemandInstanceTypeEnum.BUILDING)")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "buildingId", ignore = true)
    @Mapping(target = "floorSequenceId", ignore = true)
    @Mapping(target = "height", ignore = true)
    @Mapping(target = "floorElevation", ignore = true)
    DemandTreeBuildingFloorSpaceVO toTreeBuildingFloorSpaceVo(DtpRwdBuildingDTO source);

    // ------------------------- QueryParam To QueryParam -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241202.0
     */
    @Mapping(target = "querySpace", constant = "true")
    @Mapping(target = "queryCustomizedTag", constant = "false")
    @Mapping(target = "filterInShieldedSpace", constant = "true")
    @Mapping(target = "filterInBlacklistedSpace", constant = "true")
    DemandTreeInstanceQueryParam toTreeInstanceQueryParam(DemandTreeInstanceExternalQueryParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241202.0
     */
    DemandTreeInstanceProjectExternalVO toTreeInstanceProjectExternalVo(DemandTreeInstanceProjectVO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241202.0
     */
    DemandTreeInstanceBuildingExternalVO toTreeInstanceBuildingExternalVo(DemandTreeInstanceBuildingVO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241202.0
     */
    DemandTreeInstanceFloorExternalVO toTreeInstanceFloorExternalVo(DemandTreeInstanceFloorVO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241202.0
     */
    DemandTreeInstanceSpaceExternalVO toTreeInstanceSpaceExternalVo(DemandTreeInstanceSpaceVO source);

    // ------------------------- Dto To Vo -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241102.0
     */
    @Mapping(target = "instanceType", ignore = true)
    @Mapping(target = "children", ignore = true)
    DemandTreeInstanceBuildingVO toTreeInstanceBuildingVo(DtpRwdBuildingDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241102.0
     */
    @Mapping(target = "instanceType", ignore = true)
    @Mapping(target = "children", ignore = true)
    DemandTreeInstanceFloorVO toTreeInstanceFloorVo(DtpRwdFloorDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241102.0
     */
    @Mapping(target = "instanceType", ignore = true)
    DemandTreeInstanceSpaceVO toTreeInstanceSpaceVo(DtpRwdSpaceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @param config 实例配置
     * @return 模型
     * @since 5.241102.0
     */
    @Mapping(target = "instanceType", ignore = true)
    DemandInstanceConfigProjectVO toTreeInstanceConfigVo(
            DtpRwdObjectDTO source,
            DemandInstanceConfigValueObject config);

    /**
     * 转换模型
     *
     * @param source       数据源
     * @param instanceType 实例类型
     * @return 模型
     * @since 5.241102.0
     */
    DemandCustomizedConfigInstanceVO toCustomizedConfigInstanceVo(
            DtpRwdObjectDTO source, DemandInstanceTypeEnum instanceType);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241102.0
     */
    DemandSpaceInFloorVO toSpaceInFloorVo(DtpRwdSpaceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241102.0
     */
    DemandReferenceParentSpaceInFloorVO toReferenceParentSpaceInFloorVo(DtpRwdSpaceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241102.0
     */
    DemandSpaceExternalVO toSpaceExternalVo(DtpRwdSpaceDTO source);

    // ------------------------- Do To Vo -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240902.0
     */
    @Mapping(target = "spaceCount", ignore = true)
    @Mapping(target = "children", ignore = true)
    DemandTreeShieldedObjectVO toTreeShieldedObjectVo(DtpRwdObjectDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240902.0
     */
    DemandConfigGroupVO.DemandInstanceValueObjectVO instanceValueToVo(DemandInstanceValueObject source, String buildingName);

}
