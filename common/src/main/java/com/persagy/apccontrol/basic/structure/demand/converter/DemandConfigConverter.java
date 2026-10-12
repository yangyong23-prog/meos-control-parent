package com.persagy.apccontrol.basic.structure.demand.converter;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigGroupNameCheckParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigSaveOrUpdateParam;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandConfigGroupEntity;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.identifier.DemandConfigGroupIdentifier;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigAddOrEditParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryQueryParam;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.Objects;

/**
 * 需求配置转换器
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Mapper(componentModel = Constant.SPRING,
        imports = {
                BaseAssignIdDO.class,
                DemandConfig.class,
                Objects.class,
                Collections.class,
                CollectionUtils.class,
                Sets.class
        },
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DemandConfigConverter extends BaseConvert {

    // ------------------------- Entity -------------------------

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "groupId", source = "id")
    DemandConfigGroupIdentifier toConfigGroupIdentifier(DemandConfigGroup source);

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "identifier", expression = "java(this.toConfigGroupIdentifier(source))")
    DemandConfigGroupEntity toConfigGroupEntity(DemandConfigGroup source);

    // ------------------------- Gateway -------------------------

    // ------------------------- Repository -------------------------

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "configIds", ignore = true)
    @Mapping(target = "templateType", ignore = true)
    @Mapping(target = "templateIds", ignore = true)
    @Mapping(target = "sceneIds", ignore = true)
    @Mapping(target = "instanceTypes", ignore = true)
    @Mapping(target = "instanceIds", ignore = true)
    @Mapping(target = "likeConfigData", ignore = true)
    @Mapping(target = "selectColumns", ignore = true)
    DemandConfigByInstanceRepositoryBatchQueryParam toBathQueryParam(DemandConfigByInstanceBatchQueryParam source);

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "instanceTypeAndInstanceIds", expression = "java(Objects.nonNull(source.getInstanceType()) && CollectionUtils.isNotEmpty(source.getInstanceIds()) ? Collections.singletonMap(source.getInstanceType(), source.getInstanceIds()) : null)")
    @Mapping(target = "configGroupIds", expression = "java(Objects.nonNull(source.getConfigGroupId()) ? Sets.newHashSet(source.getConfigGroupId()) : null)")
    @Mapping(target = "configIds", ignore = true)
    @Mapping(target = "templateType", ignore = true)
    @Mapping(target = "templateIds", ignore = true)
    @Mapping(target = "sceneIds", ignore = true)
    @Mapping(target = "instanceTypes", ignore = true)
    @Mapping(target = "instanceIds", ignore = true)
    @Mapping(target = "likeConfigData", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "selectColumns", ignore = true)
    DemandConfigByInstanceRepositoryBatchQueryParam toBathQueryParam(DemandConfigSaveOrUpdateParam<?> source);

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "instanceTypeAndInstanceIds", expression = "java(Objects.nonNull(source.getInstanceType()) && CollectionUtils.isNotEmpty(source.getInstanceIds()) ? Collections.singletonMap(source.getInstanceType(), source.getInstanceIds()) : null)")
    @Mapping(target = "configGroupIds", expression = "java(Objects.nonNull(source.getConfigGroupId()) ? Sets.newHashSet(source.getConfigGroupId()) : null)")
    @Mapping(target = "configIds", ignore = true)
    @Mapping(target = "templateType", ignore = true)
    @Mapping(target = "templateIds", ignore = true)
    @Mapping(target = "sceneIds", ignore = true)
    @Mapping(target = "instanceTypes", ignore = true)
    @Mapping(target = "instanceIds", ignore = true)
    @Mapping(target = "likeConfigData", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "selectColumns", ignore = true)
    DemandConfigByInstanceRepositoryBatchQueryParam toBathQueryParam(DemandConfigAddOrEditParam source);

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "configGroupIds", expression = "java(com.google.common.collect.Sets.newHashSet(source.getConfigGroupId()))")
    @Mapping(target = "excludeConfigGroupId", ignore = true)
    DemandConfigGroupRepositoryBatchQueryParam toGroupBatchQueryParam(DemandConfigGroupRepositoryQueryParam source);

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "configGroupIds", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "selectColumns", ignore = true)
    DemandConfigGroupRepositoryBatchQueryParam toGroupBatchQueryParam(DemandConfigGroupNameCheckParam source);

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "publishEvent", ignore = true)
    DemandConfigAddOrEditParam toAddOrEditParam(DemandConfigSaveOrUpdateParam<?> source);

    // ------------------------- ORM -------------------------

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "relTemplateId", source = "source.templateId")
    @Mapping(target = "relSceneId", source = "source.sceneId")
    @Mapping(target = "relGroupId", source = "source.configGroupId")
    @Mapping(target = "projectId", expression = "java(com.persagy.framework.common.context.ContextUtil.getProjectId())")
    @Mapping(target = "demandInstanceType", source = "source.instanceType")
    @Mapping(target = "demandInstanceId", source = "instanceId")
    @Mapping(target = "configData", expression = "java(JSON_WRITE_VALUE_CONVERT_VALUE.apply(source.getConfigData()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    DemandConfig toAddConfig(DemandConfigAddOrEditParam source, String instanceId);

    /**
     * 转换模型
     *
     * @param source    目标源
     * @param groupName 分组名称
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "relTemplateId", source = "source.templateId")
    @Mapping(target = "relSceneId", source = "source.sceneId")
    @Mapping(target = "projectId", expression = "java(com.persagy.framework.common.context.ContextUtil.getProjectId())")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    DemandConfigGroup toAddConfigGroup(DemandConfigSaveOrUpdateParam<?> source, String groupName);

    // ------------------------- Copy -------------------------

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250202.0
     */
    DemandConfigByInstanceRepositoryBatchQueryParam copy(DemandConfigByInstanceRepositoryBatchQueryParam source);

    /**
     * 转换模型
     *
     * @param source 目标源
     * @return 模型
     * @since 7.250801.0
     */
    DemandConfig copy(DemandConfig source);

}
