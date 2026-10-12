package com.persagy.apccontrol.basic.structure.infocodeconfig.converter;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeConfigImportContext;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeGroupConfigImportContext;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeSheetImportContext;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeGroup;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.dashboard.DashboardBatchControlQueryParam;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigByObjectQueryParam;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigObjectByFiltersQueryParam;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigTemplateTypeQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigVO;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.BaseInfoCodeInitParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.*;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.mapstruct.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 信息点模版类型转换器
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR, imports = {
        CollectionUtils.class,
        IterableUtils.class,
        Objects.class,
        Collectors.class,
        LocalContextStorage.class,
        Sets.class
})
public interface InfoCodeConverter {

    // ------------------------- Copy -------------------------

    /**
     * 复制参数
     *
     * @param source 数据源
     * @return 副本
     * @since 7.260501.0
     */
    InfoCodeInitByObjectParam copy(InfoCodeInitByObjectParam source);

    // ------------------------- To VO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240602.0
     */
    @Mapping(target = "infoCode", source = "configModel.infoCode")
    @Mapping(target = "infoCodeName", source = "infoCodeAndDefineModel.name")
    @Mapping(target = "aliasName", source = "infoCodeAndDefineModel.aliasName")
    @Mapping(target = "infoCodeType", source = "configModel.infoCodeType")
    @Mapping(target = "existsIotRule", source = "configModel.existsIotRule")
    @Mapping(target = "showStyle", source = "configModel.showStyle")
    @Mapping(target = "dataValue", source = "infoCodeAndDataModel.dataValue")
    @Mapping(target = "dataTime", source = "infoCodeAndDataModel.dataTime")
    @Mapping(target = "scale", source = "infoCodeAndDefineModel.scale")
    @Mapping(target = "unit", source = "infoCodeAndDefineModel.unit")
    @Mapping(target = "sequence", source = "configModel.sequence")
    @Mapping(target = "dataSource", expression = "java(Objects.nonNull(source.getInfoCodeAndDefineModel()) && CollectionUtils.isNotEmpty(source.getInfoCodeAndDefineModel().getDataSources()) ? source.getInfoCodeAndDefineModel().getDataSources() : null)")
    @Mapping(target = "relTemplateTypeId", source = "templateTypeModel.templateTypeId")
    @Mapping(target = "additionalMapValue", source = "infoCodeAndDataModel.additionalMapValue")
    @Mapping(target = "originDataType", source = "infoCodeAndDefineModel.originDataType")
    @Mapping(target = "relInfoCodeConfig", ignore = true)
    @Mapping(target = "relInfoCodeConfigs", ignore = true)
    InfoCodeConfigVO toConfigVo(InfoCodeAggregate source);

    // ------------------------- To DTO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240801.0
     */
    @Mapping(target = "relInfoCodeGroupConfigs", ignore = true)
    @Mapping(target = "relObjects", ignore = true)
    @Mapping(target = "inSpaces", ignore = true)
    @Mapping(target = "inFloor", ignore = true)
    @Mapping(target = "inBuilding", ignore = true)
    InfoCodeRwdObjectDetailDTO toRwdObjectDto(DtpRwdObjectDTO source);

    // ------------------------- To Param -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.241001.0
     */
    @Mapping(target = "templateTypes", expression = "java(com.google.common.collect.Lists.newArrayList(source.getTemplateType()))")
    @Mapping(target = "objects", ignore = true)
    @Mapping(target = "page", ignore = true)
    @Mapping(target = "templateTypeIds", ignore = true)
    @Mapping(target = "templateTypeModels", ignore = true)
    @Mapping(target = "forceMathTemplate", ignore = true)
    @Mapping(target = "filterCustomGroupInfoCodeConfig", ignore = true)
    @Mapping(target = "infoCodeTypes", ignore = true)
    @Mapping(target = "fillInfoCodeDataModel", ignore = true)
    @Mapping(target = "fillInfoCodeDefineModel", ignore = true)
    InfoCodeInitByObjectParam toInitByObjectParam(InfoCodeConfigByObjectQueryParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 7.250901.0
     */
    @Mapping(target = "page", ignore = true)
    @Mapping(target = "templateTypeIds", ignore = true)
    @Mapping(target = "templateTypes", ignore = true)
    @Mapping(target = "templateTypeModels", expression = "java(com.google.common.collect.Lists.newArrayList(source))")
    @Mapping(target = "forceMathTemplate", ignore = true)
    @Mapping(target = "filterCustomGroupInfoCodeConfig", ignore = true)
    @Mapping(target = "groupCodes", ignore = true)
    @Mapping(target = "filterGroups", ignore = true)
    @Mapping(target = "excludeGroups", ignore = true)
    @Mapping(target = "infoCodeTypes", ignore = true)
    @Mapping(target = "queryIotInfoCodeRule", ignore = true)
    @Mapping(target = "notFilterIotInfoCodeRule", ignore = true)
    @Mapping(target = "filterBothNotExistIotInfoCodeRule", ignore = true)
    @Mapping(target = "filterSsePublishInfoCodeOld", ignore = true)
    @Mapping(target = "filterSsePublishInfoCode", ignore = true)
    @Mapping(target = "fillInfoCodeDefineModel", ignore = true)
    @Mapping(target = "fillInfoCodeDataModel", ignore = true)
    @Mapping(target = "placeholders", ignore = true)
    InfoCodeInitByObjectParam toInitByObjectParam(InfoCodeTemplateTypeModel source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.241001.0
     */
    @Mapping(target = "filterSsePublishInfoCodeOld", ignore = true)
    @Mapping(target = "filterSsePublishInfoCode", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "withColumns", ignore = true)
    @Mapping(target = "placeholders", ignore = true)
    InfoCodeConfigByObjectQueryParam toByObjectQueryParam(
            InfoCodeConfigObjectByFiltersQueryParam source,
            Set<String> objectIds);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 7.250701.0
     */
    @Mapping(target = "pageQuery", ignore = true)
    @Mapping(target = "templateTypeId", ignore = true)
    @Mapping(target = "templateTypeIds", source = "templateIds")
    @Mapping(target = "templateTypes", expression = "java(Objects.nonNull(source.getTemplateType()) ? Sets.newHashSet(source.getTemplateType()) : null)")
    @Mapping(target = "templateTypeModels", ignore = true)
    @Mapping(target = "classCode", ignore = true)
    @Mapping(target = "classCodes", ignore = true)
    @Mapping(target = "localName", ignore = true)
    @Mapping(target = "staticInfoCodeFilters", ignore = true)
    @Mapping(target = "signAndStaticInfoCodeFilters", ignore = true)
    @Mapping(target = "serviceSpaceIds", ignore = true)
    @Mapping(target = "inSpaceIds", ignore = true)
    @Mapping(target = "orders", ignore = true)
    InfoCodeObjectQueryParam toTemplateQueryParam(InfoCodeConfigTemplateTypeQueryParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 7.250901.0
     */
    @Mapping(target = "templateTypes", expression = "java(com.google.common.collect.Lists.newArrayList(source.getTemplateType()))")
    @Mapping(target = "pageQuery", ignore = true)
    @Mapping(target = "templateTypeId", ignore = true)
    @Mapping(target = "templateTypeIds", ignore = true)
    @Mapping(target = "templateTypeModels", ignore = true)
    @Mapping(target = "classCode", ignore = true)
    @Mapping(target = "classCodes", ignore = true)
    @Mapping(target = "excludeClassCodes", ignore = true)
    @Mapping(target = "filterControlObject", ignore = true)
    @Mapping(target = "filterObjectIds", ignore = true)
    @Mapping(target = "filterInSystemIds", ignore = true)
    @Mapping(target = "objectTypes", ignore = true)
    @Mapping(target = "localName", ignore = true)
    @Mapping(target = "staticInfoCodeFilters", ignore = true)
    @Mapping(target = "signAndStaticInfoCodeFilters", ignore = true)
    @Mapping(target = "serviceSpaceIds", ignore = true)
    @Mapping(target = "inSpaceIds", ignore = true)
    @Mapping(target = "orders", ignore = true)
    InfoCodeObjectQueryParam toTemplateQueryParam(DashboardBatchControlQueryParam source);

    /**
     * 转换模型
     *
     * @param source  数据源
     * @param objects 对象
     * @return 目标源
     * @since 7.250802.0
     */
    @Mapping(target = "filterSsePublishInfoCodeOld", ignore = true)
    @Mapping(target = "filterSsePublishInfoCode", ignore = true)
    InfoCodeInitByObjectParam toInitByObjectParam(BaseInfoCodeInitParam source, Collection<DtpRwdObjectDTO> objects);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 7.250701.0
     */
    @Mapping(target = "templateTypeId", ignore = true)
    @Mapping(target = "templateTypeIds", source = "filterTemplateTypeIds")
    @Mapping(target = "runningMonitorTypes", ignore = true)
    @Mapping(target = "runningMonitorSecondTypes", ignore = true)
    @Mapping(target = "templateTypes", ignore = true)
    @Mapping(target = "classCode", ignore = true)
    @Mapping(target = "classCodes", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "selectColumns", ignore = true)
    InfoCodeTemplateTypeQueryDaoParam toTemplateQueryDaoParam(InfoCodeConfigTemplateTypeQueryParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240602.0
     */
    @Mapping(target = "templateType", ignore = true)
    @Mapping(target = "runningMonitorTypes", ignore = true)
    @Mapping(target = "runningMonitorSecondTypes", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "selectColumns", ignore = true)
    InfoCodeTemplateTypeQueryDaoParam toTemplateQueryDaoParam(InfoCodeObjectQueryParam source);

    // ------------------------- To Model -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240602.0
     */
    @Mapping(target = "templateTypeId", source = "id")
    @Mapping(target = "filterValueObject", ignore = true)
    @Mapping(target = "classCodeName", ignore = true)
    @Mapping(target = "infoCodeFilterData", ignore = true)
    @Mapping(target = "infoCodeDefines", ignore = true)
    @Mapping(target = "templateTypeName", ignore = true)
    @Mapping(target = "cacheClassCodeObjectIds", ignore = true)
    @Mapping(target = "objects", ignore = true)
    InfoCodeTemplateTypeModel toTemplateTypeModel(InfoCodeTemplateType source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240602.0
     */
    @InheritConfiguration
    List<InfoCodeTemplateTypeModel> toTemplateTypeModel(Collection<InfoCodeTemplateType> source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240602.0
     */
    @Mapping(target = "configId", source = "id")
    @Mapping(target = "existsIotRule", ignore = true)
    InfoCodeConfigModel toConfigModel(InfoCodeConfig source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240602.0
     */
    @Mapping(target = "configModel", source = "source")
    InfoCodeAssembleModel toAssembleModel(InfoCodeConfig source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240602.0
     */
    @Mapping(target = "groupId", source = "id")
    @Mapping(target = "relParent", ignore = true)
    InfoCodeGroupModel toGroupModel(InfoCodeGroup source);

    // ------------------------- To Entity -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240602.0
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    InfoCodeGroup toEntity(InfoCodeGroupConfigImportContext source, Long relParentId);

    /**
     * 转换模型
     *
     * @param source             数据源
     * @param runningMonitorType 模版编码
     * @param templateType       模版编码
     * @return 目标源
     * @since 5.240602.0
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    InfoCodeTemplateType toEntity(
            InfoCodeSheetImportContext source,
            RunningMonitorTypeEnum runningMonitorType,
            RunningMonitorSecondTypeEnum runningMonitorSecondType,
            InfoCodeTemplateTypeEnum templateType);

    /**
     * 转换模型
     *
     * @param source            数据源
     * @param relTemplateTypeId 关联模版类型ID
     * @return 目标源
     * @since 5.240602.0
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "relInfoCodeConfigIds", ignore = true)
    InfoCodeConfig toEntity(InfoCodeConfigImportContext source, Long relTemplateTypeId);

    // ------------------------- Fill -------------------------

    /**
     * 转换模型
     *
     * @param target 目标源
     * @param source 数据源
     * @return 目标源
     * @since 7.250501.0
     */
    @Mapping(target = "infoCodeName", source = "name")
    @Mapping(target = "objectId", ignore = true)
    @Mapping(target = "infoCode", ignore = true)
    @Mapping(target = "dataValue", ignore = true)
    @Mapping(target = "dataTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "dataIndate", ignore = true)
    @Mapping(target = "dataIndicateTime", ignore = true)
    @Mapping(target = "infoCodeValue", ignore = true)
    @Mapping(target = "additionalMapValue", ignore = true)
    InfoCodeValueVO fill(@MappingTarget InfoCodeValueVO target, DtpKnowledgeInfoCodeDTO source);

    /**
     * 转换模型
     *
     * @param target 目标源
     * @param source 数据源
     * @return 目标源
     * @since 7.250501.0
     */
    InfoCodeValueVO fill(@MappingTarget InfoCodeValueVO target, InfoCodeValueVO source);

}
