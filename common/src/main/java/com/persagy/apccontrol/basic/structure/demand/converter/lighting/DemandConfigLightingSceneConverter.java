package com.persagy.apccontrol.basic.structure.demand.converter.lighting;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingScene;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneUpdateDaoParam;
import org.mapstruct.*;

/**
 * 需求配置-照明需求-场景配置转换器
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DemandConfigLightingSceneConverter {

    // ------------------------- To Do -------------------------

    /**
     * 转换模型
     *
     * @param target    目标源
     * @param projectId 项目ID
     * @param template  模版
     * @param source    数据源
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
    })
    DemandConfigLightingScene toDo(
            @MappingTarget DemandConfigLightingScene target,
            String projectId, DemandTemplate template, DemandConfigLightingSceneSaveDaoParam source);

    /**
     * 转换模型
     *
     * @param target    目标源
     * @param projectId 项目ID
     * @param source    数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "demandType", ignore = true),
            @Mapping(target = "demandTemplateType", ignore = true),
            @Mapping(target = "demandTemplateTag", ignore = true),
            @Mapping(target = "relDemandTemplateId", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUser", ignore = true)
    })
    DemandConfigLightingScene toDo(
            @MappingTarget DemandConfigLightingScene target,
            String projectId, DemandConfigLightingSceneUpdateDaoParam source);

}
