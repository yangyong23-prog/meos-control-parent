package com.persagy.apccontrol.basic.structure.demand.converter.lighting;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingDataDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.DemandConfigLightingClearSettingDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.DemandConfigLightingQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.DemandConfigLightingRemoveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.DemandConfigLightingSaveDaoParam;
import org.mapstruct.*;

/**
 * 需求配置-照明需求-配置转换器
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DemandConfigLightingConverter {

    // ------------------------- To Dao Param -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 24Q1 T3S1-5.17.0
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "ids", ignore = true),
            @Mapping(target = "projectId", ignore = true),
            @Mapping(target = "demandType", ignore = true),
            @Mapping(target = "demandTemplateType", ignore = true),
            @Mapping(target = "demandTemplateTypes", ignore = true),
            @Mapping(target = "demandTemplateTag", ignore = true),
            @Mapping(target = "isCustomizedSetting", ignore = true),
            @Mapping(target = "likeConfigData", ignore = true),
            @Mapping(target = "relDemandLightingSceneIds", ignore = true),
            @Mapping(target = "relDemandTemplateIds", ignore = true),
            @Mapping(target = "demandInstanceIds", ignore = true),
            @Mapping(target = "demandInstanceType", ignore = true),
            @Mapping(target = "demandInstanceTypes", ignore = true),
            @Mapping(target = "st", ignore = true)
    })
    DemandConfigLightingQueryDaoParam toQueryDaoParam(DemandConfigLightingClearSettingDaoParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 24Q1 T3S1-5.17.0
     */
    @Mappings({
            @Mapping(target = "demandType", ignore = true),
            @Mapping(target = "demandTemplateType", ignore = true),
            @Mapping(target = "demandTemplateTypes", ignore = true),
            @Mapping(target = "demandTemplateTag", ignore = true),
            @Mapping(target = "isCustomizedSetting", ignore = true),
            @Mapping(target = "likeConfigData", ignore = true),
            @Mapping(target = "relDemandLightingSceneIds", ignore = true),
            @Mapping(target = "relDemandTemplateIds", ignore = true)
    })
    DemandConfigLightingQueryDaoParam toQueryDaoParam(DemandConfigLightingRemoveDaoParam source);

    // ------------------------- To Do -------------------------

    /**
     * 转换模型
     *
     * @param target              目标源
     * @param projectId           项目ID
     * @param isCustomizedSetting 是否自定义配置
     * @param template            模版
     * @param source              数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mappings({
            @Mapping(target = "st", source = "source.st"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUser", ignore = true),
            @Mapping(target = "demandInstanceName", ignore = true)
    })
    DemandConfigLighting toDo(
            @MappingTarget DemandConfigLighting target,
            String projectId, Boolean isCustomizedSetting,
            DemandTemplate template, DemandConfigLightingSaveDaoParam source);

    /**
     * 转换模型
     *
     * @param target              目标源
     * @param projectId           项目ID
     * @param isCustomizedSetting 是否自定义配置
     * @param configData          配置数据
     * @return 模型
     * @since v6.0.0
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "demandType", ignore = true),
            @Mapping(target = "demandTemplateType", ignore = true),
            @Mapping(target = "demandTemplateTag", ignore = true),
            @Mapping(target = "demandInstanceId", ignore = true),
            @Mapping(target = "demandInstanceName", ignore = true),
            @Mapping(target = "demandInstanceType", ignore = true),
            @Mapping(target = "relDemandTemplateId", ignore = true),
            @Mapping(target = "relDemandLightingSceneId", ignore = true),
            @Mapping(target = "st", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUser", ignore = true),
    })
    DemandConfigLighting toDo(
            @MappingTarget DemandConfigLighting target,
            String projectId, Boolean isCustomizedSetting,
            DemandConfigLightingDataDTO configData);

}
