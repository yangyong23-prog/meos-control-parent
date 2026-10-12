package com.persagy.apccontrol.basic.structure.iot;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.dto.DtpPointInfo;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Map;

/**
 * IOT转换器
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IotConverter {

    /**
     * 转换模型
     *
     * @param source             数据源
     * @param additionalMapValue 拓展值
     * @return 目标源
     * @since 5.240502.0
     */
    @Mapping(target = "infoCode", source = "source.infoCode")
    @Mapping(target = "dataValue", expression = "java(source.getInfoCodeValue() != null ? source.getInfoCodeValue().toString() : null)")
    @Mapping(target = "dataTime", source = "source.updateTime")
    @Mapping(target = "additionalMapValue", source = "additionalMapValue")
    InfoCodeDataModel toInfoCodeDataModel(InfoCodeValueVO source, Map<String, Object> additionalMapValue);

    // ------------------------- To VO -------------------------

    /**
     * 转换模型
     *
     * @param source         数据源
     * @param infoCodeDefine 信息点定义
     * @return 目标源
     * @since 5.240502.0
     */
    @Mapping(target = "infoCodeName", source = "infoCodeDefine.name")
    @Mapping(target = "unit", source = "infoCodeDefine.unit")
    @Mapping(target = "infoCodeValue", source = "source.dataValue")
    @Mapping(target = "dataTime", expression = "java(source.toDataTime())")
    @Mapping(target = "updateTime", expression = "java(source.toDataTime())")
    @Mapping(target = "dataIndicateTime", expression = "java(source.toDataIndicateTime())")
    @Mapping(target = "additionalMapValue", ignore = true)
    InfoCodeValueVO toInfoCodeValueVo(
            FeedbackInfoMsgMqttVO source,
            DtpKnowledgeInfoCodeDTO infoCodeDefine);

    /**
     * 转换模型
     *
     * @param source         数据源
     * @param infoCodeDefine 信息点定义
     * @return 目标源
     * @since 5.240502.0
     */
    @Mapping(target = "infoCodeName", source = "infoCodeDefine.name")
    @Mapping(target = "unit", source = "infoCodeDefine.unit")
    @Mapping(target = "infoCodeValue", source = "source.ipValue")
    @Mapping(target = "dataTime", expression = "java(source.toDataTime())")
    @Mapping(target = "updateTime", expression = "java(source.toDataTime())")
    @Mapping(target = "additionalMapValue", ignore = true)
    @Mapping(target = "dataIndicateTime", ignore = true)
    InfoCodeValueVO toInfoCodeValueVo(
            DtpPointInfo source,
            DtpKnowledgeInfoCodeDTO infoCodeDefine);

    /**
     * 转换模型
     *
     * @param source         数据源
     * @param infoCodeDefine 信息点定义
     * @return 目标源
     * @since 5.240502.0
     */
    @Mapping(target = "infoCodeName", source = "infoCodeDefine.name")
    @Mapping(target = "unit", source = "infoCodeDefine.unit")
    @Mapping(target = "scale", source = "infoCodeDefine.scale")
    @Mapping(target = "originDataType", source = "infoCodeDefine.originDataType")
    @Mapping(target = "dataSources", source = "infoCodeDefine.dataSources")
    @Mapping(target = "additionalMapValue", ignore = true)
    InfoCodeValueVO toInfoCodeValueVo(
            InfoCodeValueVO source,
            DtpKnowledgeInfoCodeDTO infoCodeDefine);

    /**
     * 转换模型
     *
     * @param source         数据源
     * @param infoCodeDefine 信息点定义
     * @since 5.240502.0
     */
    @Mapping(target = "infoCodeName", source = "infoCodeDefine.name")
    @Mapping(target = "unit", source = "infoCodeDefine.unit")
    @Mapping(target = "scale", source = "infoCodeDefine.scale")
    @Mapping(target = "originDataType", source = "infoCodeDefine.originDataType")
    @Mapping(target = "dataSources", source = "infoCodeDefine.dataSources")
    @Mapping(target = "additionalMapValue", ignore = true)
    @Mapping(target = "objectId", ignore = true)
    @Mapping(target = "infoCode", ignore = true)
    @Mapping(target = "infoCodeValue", ignore = true)
    @Mapping(target = "dataValue", ignore = true)
    @Mapping(target = "dataTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "dataIndate", ignore = true)
    @Mapping(target = "dataIndicateTime", ignore = true)
    void fill(@MappingTarget InfoCodeValueVO source, DtpKnowledgeInfoCodeDTO infoCodeDefine);


    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240502.0
     */
    @Mapping(target = "infoCodeValue", source = "source.ipValue")
    @Mapping(target = "dataTime", expression = "java(source.toDataTime())")
    @Mapping(target = "updateTime", expression = "java(source.toDataTime())")
    @Mapping(target = "infoCodeName", ignore = true)
    @Mapping(target = "dataValue", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "scale", ignore = true)
    @Mapping(target = "dataSources", ignore = true)
    @Mapping(target = "additionalMapValue", ignore = true)
    @Mapping(target = "originDataType", ignore = true)
    @Mapping(target = "dataIndate", ignore = true)
    @Mapping(target = "dataIndicateTime", ignore = true)
    InfoCodeValueVO toInfoCodeValueVo(EnergyDataResponse source);

}
