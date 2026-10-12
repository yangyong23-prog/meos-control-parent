package com.persagy.apccontrol.basic.structure.demand.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplateCalendar;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarUpdateDaoParam;
import org.mapstruct.*;

/**
 * 需求模版日历转换器
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DemandTemplateCalendarConverter {

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
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUser", ignore = true),
    })
    DemandTemplateCalendar toDo(
            @MappingTarget DemandTemplateCalendar target,
            String projectId, DemandTemplateCalendarSaveDaoParam source);

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
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUser", ignore = true),
    })
    DemandTemplateCalendar toDo(
            @MappingTarget DemandTemplateCalendar target,
            String projectId, DemandTemplateCalendarUpdateDaoParam source);

}
