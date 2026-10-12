package com.persagy.apccontrol.basic.convert;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.instance.EquipmentVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdCircuitVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdTreeObjectVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdSpaceByRoomFuncTypeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCriteriaQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdObjectResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdRelationResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdTwinControlObjectResponse;
import org.apache.commons.collections4.IterableUtils;
import org.mapstruct.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 物理世界转换器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Mapper(imports = {IterableUtils.class, DtpRwdClassCodeEnum.class},
        componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface DtpRwdConvert extends BaseConvert {

    // -------------------- To VO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.6.0
     */
    DtpRwdObjectVO toObjectVo(DtpRwdObjectDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @InheritConfiguration
    List<DtpRwdObjectVO> toObjectVo(List<DtpRwdObjectDTO> source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241102.0
     */
    DtpRwdSpaceVO toSpaceVo(DtpRwdSpaceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.1.0
     */
    @Mappings({
            @Mapping(target = "id", source = "source.toId"),
            @Mapping(target = "name", source = "source.toName"),
            @Mapping(target = "localId", source = "source.toLocalId"),
            @Mapping(target = "localName", source = "source.toLocalName"),
            @Mapping(target = "objType", source = "source.toObjType"),
            @Mapping(target = "classCode", source = "source.toClassCode"),
            @Mapping(target = "valid", source = "source.toValid")
    })
    DtpRwdObjectVO toObjectVo(DtpRwdRelationResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.1.0
     */
    @InheritConfiguration
    List<DtpRwdObjectVO> toObjectVo(Collection<DtpRwdRelationResponse> source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241101.0
     */
    @Mapping(target = "inStrategy", ignore = true)
    @Mapping(target = "operableRunning", ignore = true)
    @Mapping(target = "classCodeName", ignore = true)
    @Mapping(target = "majorName", ignore = true)
    EquipmentVO toEquipmentVo(DtpRwdEquipmentDTO source);

    /**
     * 转换模型
     *
     * @param source   数据源
     * @param children 子节点
     * @return 模型
     * @since v5.8.0
     */
    DtpRwdTreeObjectVO toTreeObjectVo(DtpRwdObjectDTO source, Collection<DtpRwdTreeObjectVO> children);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    @Mapping(target = "serviceAreaType", expression = "java(toEnumVo(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT, DtpRwdConvert.INFO_CODE_SERVICE_AREA_TYPE, source.getServiceAreaType()))")
    @Mapping(target = "funcType", expression = "java(toEnumVo(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT, DtpRwdConvert.INFO_CODE_FUNC_TYPE, source.getFuncType()))")
    @Mapping(target = "lampFuncType", expression = "java(toEnumVo(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT, DtpRwdConvert.INFO_CODE_LAMP_FUNC_TYPE, source.getLampFuncType()))")
    DtpRwdCircuitVO toCircuitVo(DtpRwdCircuitDTO source);

    @Mapping(target = "serviceAreaType", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_SERVICE_AREA_TYPE))")
    @Mapping(target = "funcType", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_FUNC_TYPE))")
    @Mapping(target = "autoCtrlType", expression = "java(ANALYZE_LIST_STRING.apply(source.getUndefinedProperties(), INFO_CODE_AUTO_CTRL_TYPE))")
    @Mapping(target = "lampFuncType", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_LAMP_FUNC_TYPE))")
    @Mapping(target = "floorSequenceId", ignore = true)
    @Mapping(target = "buildingSequenceId", ignore = true)
    @Mapping(target = "serviceSpaces", ignore = true)
    @Mapping(target = "serviceFloors", ignore = true)
    DtpRwdCircuitDTO toCircuitDto(DtpRwdEquipmentDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    @InheritConfiguration
    List<DtpRwdCircuitVO> toCircuitVo(Collection<DtpRwdCircuitDTO> source);

    // -------------------- To DTO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.1.0
     */
    DtpRwdObjectDTO toDto(DtpRwdObjectResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.1.0
     */
    @Mappings({
            @Mapping(target = "buildingSequenceId", expression = "java(ANALYZE_INTEGER.apply(source.getUndefinedProperties(), INFO_CODE_BUILDING_SEQUENCE_ID))"),
            @Mapping(target = "floors", ignore = true)
    })
    DtpRwdBuildingDTO toBuildingDto(DtpRwdObjectResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.1.0
     */
    @Mapping(target = "buildingId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BUILDING_ID))")
    @Mapping(target = "floorSequenceId", expression = "java(ANALYZE_INTEGER.apply(source.getUndefinedProperties(), INFO_CODE_FLOOR_SEQUENCE_ID))")
    @Mapping(target = "height", expression = "java(ANALYZE_BIG_DECIMAL.apply(source.getUndefinedProperties(), INFO_CODE_HEIGHT))")
    @Mapping(target = "floorElevation", expression = "java(ANALYZE_BIG_DECIMAL.apply(source.getUndefinedProperties(), INFO_CODE_FLOOR_ELEVATION))")
    @Mapping(target = "spaces", ignore = true)
    @Mapping(target = "avgData", ignore = true)
    @Mapping(target = "controlSpaceList", ignore = true)
    @Mapping(target = "roomFuncTypes", ignore = true)
    DtpRwdFloorDTO toFloorDto(DtpRwdObjectResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.1.0
     */
    @Mapping(target = "buildingId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BUILDING_ID))")
    @Mapping(target = "floorId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_FLOOR_ID))")
    @Mapping(target = "tdb", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_TDB))")
    @Mapping(target = "co2", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_CO2))")
    @Mapping(target = "rh", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_RH))")
    @Mapping(target = "roomFuncType", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_ROOM_FUNC_TYPE))")
    @Mapping(target = "bimId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BIM_ID))")
    @Mapping(target = "bimLocation", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BIM_LOCATION))")
    @Mapping(target = "outline", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_OUTLINE))")
    @Mapping(target = "roomFuncTypeVO", ignore = true)
    @Mapping(target = "systemIds", ignore = true)
    DtpRwdSpaceDTO toSpaceDto(DtpRwdObjectResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240701.0
     */
    @Mapping(target = "floorId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_FLOOR_ID))")
    @Mapping(target = "buildingId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BUILDING_ID))")
    @Mapping(target = "bimId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BIM_ID))")
    @Mapping(target = "bimLocation", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BIM_LOCATION))")
    DtpRwdEquipmentDTO toEquipmentDto(DtpRwdObjectResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240701.0
     */
    @Mapping(target = "floorId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_FLOOR_ID))")
    @Mapping(target = "buildingId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BUILDING_ID))")
    @Mapping(target = "bimId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BIM_ID))")
    @Mapping(target = "bimLocation", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BIM_LOCATION))")
    @Mapping(target = "hasArmingFunc", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_HAS_ARMING_FUNC))")
    @Mapping(target = "cameraType", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_CAMERA_TYPE))")
    @Mapping(target = "isControl", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_IS_CONTROL))")
    @Mapping(target = "manufacturer", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_MANUFACTURER))")
    @Mapping(target = "roomFuncTypes", ignore = true)
    SecurityDtpRwdEquipmentDTO toSecurityEquipmentDto(DtpRwdObjectResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.1.0
     */
    @Mapping(target = "id", source = "source.toId")
    @Mapping(target = "name", source = "source.toName")
    @Mapping(target = "localId", source = "source.toLocalId")
    @Mapping(target = "localName", source = "source.toLocalName")
    @Mapping(target = "projectId", source = "source.toProjectId")
    @Mapping(target = "objType", source = "source.toObjType")
    @Mapping(target = "classCode", source = "source.toClassCode")
    @Mapping(target = "valid", source = "source.toValid")
    @Mapping(target = "buildingId", expression = "java(analyzeInfoField(INFO_CODE_BUILDING_ID, source.getToInfos()))")
    @Mapping(target = "floorId", expression = "java(analyzeInfoField(INFO_CODE_FLOOR_ID, source.getToInfos()))")
    @Mapping(target = "tdb", expression = "java(analyzeInfoField(INFO_CODE_TDB, source.getToInfos()))")
    @Mapping(target = "co2", expression = "java(analyzeInfoField(INFO_CODE_CO2, source.getToInfos()))")
    @Mapping(target = "rh", expression = "java(analyzeInfoField(INFO_CODE_RH, source.getToInfos()))")
    @Mapping(target = "roomFuncType", expression = "java(ANALYZE_STRING.apply(source.getToInfos(), INFO_CODE_ROOM_FUNC_TYPE))")
    @Mapping(target = "bimId", expression = "java(analyzeInfoField(INFO_CODE_BIM_ID, source.getToInfos()))")
    @Mapping(target = "bimLocation", expression = "java(analyzeInfoField(INFO_CODE_BIM_LOCATION, source.getToInfos()))")
    @Mapping(target = "outline", expression = "java(analyzeInfoField(INFO_CODE_OUTLINE, source.getToInfos()))")
    @Mapping(target = "relObjIds", ignore = true)
    @Mapping(target = "roomFuncTypeVO", ignore = true)
    @Mapping(target = "systemIds", ignore = true)
    @Mapping(target = "dynamicName", ignore = true)
    DtpRwdSpaceDTO toSpaceDto(DtpRwdRelationResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.1.0
     */
    @InheritConfiguration
    List<DtpRwdSpaceDTO> toSpaceDto(List<DtpRwdRelationResponse> source);

    /**
     * 转换模型
     *
     * @param source         数据源
     * @param relationAcccId 关联中央供冷系统ID
     * @return 模型
     * @since v5.8.0
     */
    @Mappings({
            @Mapping(target = "isTerminalPump", expression = "java(ANALYZE_BOOLEAN.apply(source.getUndefinedProperties(), DtpRwdFreezePumpDTO.INFO_CODE_IS_TERMINAL_PUMP))"),
            @Mapping(target = "relationAcatId", expression = "java(IterableUtils.first(source.getRelObjIds()))"),
            @Mapping(target = "infoCodeSetType", ignore = true),
            @Mapping(target = "infoCodeUnit", ignore = true)
    })
    DtpRwdFreezePumpDTO toFreezePumpDto(DtpRwdObjectResponse source, String relationAcccId);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.8.0
     */
    @Mapping(target = "serviceAreaType", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_SERVICE_AREA_TYPE))")
    @Mapping(target = "funcType", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_FUNC_TYPE))")
    @Mapping(target = "autoCtrlType", expression = "java(ANALYZE_LIST_STRING.apply(source.getUndefinedProperties(), INFO_CODE_AUTO_CTRL_TYPE))")
    @Mapping(target = "lampFuncType", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_LAMP_FUNC_TYPE))")
    @Mapping(target = "floorId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_FLOOR_ID))")
    @Mapping(target = "buildingId", expression = "java(ANALYZE_STRING.apply(source.getUndefinedProperties(), INFO_CODE_BUILDING_ID))")
    @Mapping(target = "floorSequenceId", ignore = true)
    @Mapping(target = "buildingSequenceId", ignore = true)
    @Mapping(target = "serviceSpaces", ignore = true)
    @Mapping(target = "serviceFloors", ignore = true)
    DtpRwdCircuitDTO toCircuitDto(DtpRwdObjectResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240601.0
     */
    @Mapping(target = "infoCodes", source = "points", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    DtpRwdTwinControlObjectDTO toTwinControlObjectDto(DtpRwdTwinControlObjectResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240601.0
     */
    @Mapping(target = "infoCodeType", source = "pointType")
    DtpRwdTwinControlInfoCodeDTO toTwinControlInfoCodeDto(DtpRwdTwinControlObjectResponse.Point source);

    // -------------------- COPY -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240602.0
     */
    @Mapping(target = "classCodeEnum", ignore = true)
    @Mapping(target = "replacePlaceholder", ignore = true)
    DtpRwdObjectQueryParam copy(DtpRwdObjectQueryParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240702.0
     */
    DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> copy(DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240702.0
     */
    DtpRwdSpaceByRoomFuncTypeQueryParam copy(DtpRwdSpaceByRoomFuncTypeQueryParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240702.0
     */
    DtpRwdRelationQueryParam copy(DtpRwdRelationQueryParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240702.0
     */
    BaseQueryParamDTO copy(BaseQueryParamDTO source);

    /**
     * 解析字段值
     *
     * @param fieldName 字段
     * @param infos     数据源
     * @return 字段值
     * @since v5.1.0
     */
    default String analyzeInfoField(@NonNull String fieldName, @Nullable JSONObject infos) {
        if (Objects.isNull(infos)) {
            return null;
        }
        return infos.getString(fieldName);
    }
}
