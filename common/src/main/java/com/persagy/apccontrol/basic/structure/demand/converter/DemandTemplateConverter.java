package com.persagy.apccontrol.basic.structure.demand.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateUpdateDaoParam;
import org.mapstruct.*;

/**
 * 需求模版转换器
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DemandTemplateConverter {

    /**
     * 转换模型
     *
     * @param target    目标源
     * @param projectId 项目ID
     * @param source    数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mapping(target = "demandTemplateTag", source = "source.demandTemplateTag", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    DemandTemplate toDo(
            @MappingTarget DemandTemplate target,
            String projectId, DemandTemplateSaveDaoParam source);

    /**
     * 转换模型
     *
     * @param target    目标源
     * @param projectId 项目ID
     * @param source    数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    DemandTemplate toDo(
            @MappingTarget DemandTemplate target,
            String projectId, DemandTemplateUpdateDaoParam source);

}
