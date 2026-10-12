package com.persagy.apccontrol.basic.structure.demand.converter;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.resource.demand.param.DemandInstanceConfigSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandInstanceConfigUpdateDaoParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.param.DemandInstanceConfigQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandInstanceConfigRepositoryQueryParam;
import org.mapstruct.*;

/**
 * 需求实例配置
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DemandInstanceConfigConverter extends BaseConvert {

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 7.250301.0
     */
    @Mapping(target = "selectColumns", ignore = true)
    DemandInstanceConfigRepositoryQueryParam toRepositoryQueryParam(DemandInstanceConfigQueryParam source);

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
            @Mapping(target = "configData", expression = "java(JSON_WRITE_VALUE_AS_STRING.apply(source.getConfigData()))"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUser", ignore = true),
            @Mapping(target = "fillConfig", ignore = true)
    })
    DemandInstanceConfig toDo(
            @MappingTarget DemandInstanceConfig target,
            String projectId, DemandInstanceConfigSaveDaoParam source);

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
            @Mapping(target = "configData", expression = "java(JSON_WRITE_VALUE_AS_STRING.apply(source.getConfigData()))"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUser", ignore = true),
            @Mapping(target = "fillConfig", ignore = true)
    })
    DemandInstanceConfig toDo(
            @MappingTarget DemandInstanceConfig target,
            String projectId, DemandInstanceConfigUpdateDaoParam source);

}
