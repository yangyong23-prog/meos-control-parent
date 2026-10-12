package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.info.InfoTypeEnum;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 物理世界转换器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Mapper(imports = {InfoTypeEnum.class},
        componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface RcInfoCodeCfgConvert extends BaseConvert {

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     */
    @Mapping(target = "infoCode", source = "source.code")
    @Mapping(target = "scale", source = "source.scale")
    @Mapping(target = "iotDecimal", source = "source.scale")
    @Mapping(target = "dataSource", source = "source.dataSources")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groupCode", ignore = true)
    @Mapping(target = "aliasName", ignore = true)
    @Mapping(target = "abbreviationName", ignore = true)
    @Mapping(target = "mergeCode", ignore = true)
    @Mapping(target = "mergeName", ignore = true)
    @Mapping(target = "groupInfo", ignore = true)
    @Mapping(target = "objType", ignore = true)
    @Mapping(target = "ordered", ignore = true)
    @Mapping(target = "extendInfo", ignore = true)
    @Mapping(target = "extendSetting", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "topGroupId", ignore = true)
    @Mapping(target = "objectId", ignore = true)
    @Mapping(target = "view", ignore = true)
    @Mapping(target = "force", ignore = true)
    @Mapping(target = "parameterType", ignore = true)
    @Mapping(target = "dataValue", ignore = true)
    @Mapping(target = "dataValueName", ignore = true)
    @Mapping(target = "dataTime", ignore = true)
    @Mapping(target = "forceModel", ignore = true)
    @Mapping(target = "forceInfo", ignore = true)
    @Mapping(target = "expireTime", ignore = true)
    @Mapping(target = "changeContent", ignore = true)
    @Mapping(target = "linkCodes", ignore = true)
    @Mapping(target = "systemId", ignore = true)
    @Mapping(target = "systemType", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "dataSourceByMap", ignore = true)
    @Mapping(target = "dataType", ignore = true)
    @Mapping(target = "importanceLevel", ignore = true)
    @Mapping(target = "displayMode", ignore = true)
    @Mapping(target = "dataIndate", ignore = true)
    RcInfoCodeCfg to(DtpKnowledgeInfoCodeDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     */
    @InheritConfiguration
    List<RcInfoCodeCfg> toList(List<DtpKnowledgeInfoCodeDTO> source);
}
