package com.persagy.apccontrol.basic.structure.knowledge.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.base.domain.model.ClassCodeDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.knowledge.DtpKnowledgeInfoCdeDataSourceVO;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeObjectClassCodeResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeObjectInfoCodeResponse;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 数据字典转换器
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DtpKnowledgeConverter {

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.241101.0
     */
    @Mapping(target = "infoCodeDefines", ignore = true)
    ClassCodeDefineModel toClassCodeDefineModel(DtpKnowledgeObjectClassCodeResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240602.0
     */
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "aliasName", ignore = true)
    InfoCodeDefineModel toInfoCodeDefineModel(DtpKnowledgeInfoCodeDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240602.0
     */
    @Mapping(target = "aliasName", ignore = true)
    @Mapping(target = "spaces", ignore = true)
    InfoCodeDataSourceDefineModel toInfoCodeDataSourceDefineModel(DtpKnowledgeInfoCodeDataSourceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240602.0
     */
    @InheritConfiguration
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<InfoCodeDataSourceDefineModel> toInfoCodeDataSourceDefineModel(Collection<DtpKnowledgeInfoCodeDataSourceDTO> source);

    // ------------------------- To VO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    @Mapping(target = "infoCode", source = "source.code")
    @Mapping(target = "infoCodeName", source = "source.name")
    @Mapping(target = "objectId", ignore = true)
    @Mapping(target = "infoCodeValue", ignore = true)
    @Mapping(target = "dataTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "additionalMapValue", ignore = true)
    @Mapping(target = "dataValue", ignore = true)
    @Mapping(target = "dataIndate", ignore = true)
    @Mapping(target = "dataIndicateTime", ignore = true)
    InfoCodeValueVO toRunningParameterVo(DtpKnowledgeInfoCodeDTO source);

    /**
     * 转换模型
     *
     * @param source        数据源
     * @param infoCodeValue 信息点值
     * @param updateTime    更新时间
     * @return 模型
     * @since 5.240402.0
     */
    @Mapping(target = "infoCode", source = "source.code")
    @Mapping(target = "infoCodeName", source = "source.name")
    @Mapping(target = "dataTime", source = "updateTime")
    @Mapping(target = "updateTime", source = "updateTime")
    @Mapping(target = "objectId", ignore = true)
    @Mapping(target = "additionalMapValue", ignore = true)
    @Mapping(target = "dataValue", ignore = true)
    @Mapping(target = "dataIndate", ignore = true)
    @Mapping(target = "dataIndicateTime", ignore = true)
    InfoCodeValueVO toRunningParameterVo(
            DtpKnowledgeInfoCodeDTO source,
            Object infoCodeValue,
            LocalDateTime updateTime);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 24Q1 T3S1-5.17.0
     */
    DtpKnowledgeInfoCdeDataSourceVO toDataSourceVo(DtpKnowledgeInfoCodeDataSourceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    @InheritConfiguration
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<DtpKnowledgeInfoCdeDataSourceVO> toDataSourceVo(Collection<DtpKnowledgeInfoCodeDataSourceDTO> source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 24Q1 T3S2-5.18.0
     */
    @Mapping(target = "key", source = "code")
    @Mapping(target = "value", source = "name")
    @Mapping(target = "userTips", ignore = true)
    @Mapping(target = "additionalValue", ignore = true)
    @Mapping(target = "additionalMapValue", ignore = true)
    BaseEnumVO toBaseEnumVo(DtpKnowledgeInfoCodeDataSourceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 24Q1 T3S2-5.18.0
     */
    @InheritConfiguration
    List<BaseEnumVO> toBaseEnumVo(List<DtpKnowledgeInfoCodeDataSourceDTO> source);

    // ------------------------- To Dto -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 24Q1 T3S1-5.17.0
     */
    @Mapping(target = "originDataType", source = "origDataType")
    @Mapping(target = "dataSources", source = "dataSource")
    @Mapping(target = "category", source = "category")
    DtpKnowledgeInfoCodeDTO toInfoCodeDto(DtpKnowledgeObjectInfoCodeResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 24Q1 T3S1-5.17.0
     */
    @InheritConfiguration
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<DtpKnowledgeInfoCodeDTO> toInfoCodeDto(Collection<DtpKnowledgeObjectInfoCodeResponse> source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 24Q1 T3S1-5.17.0
     */
    DtpKnowledgeInfoCodeDataSourceDTO toDataSourceDto(DtpKnowledgeObjectInfoCodeResponse.DataSource source);

    // ------------------------- Copy -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 7.250901.0
     */
    InfoCodeDefineModel copy(InfoCodeDefineModel source);

}
