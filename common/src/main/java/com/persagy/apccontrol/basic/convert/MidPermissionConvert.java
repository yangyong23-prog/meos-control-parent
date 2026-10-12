package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.dto.GroupDTO;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.outside.feign.model.response.midpermission.GroupResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.midpermission.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

/**
 * 运维平台转换器
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MidPermissionConvert {

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mappings({
            @Mapping(target = "groupId", source = "id")
    })
    GroupDTO toDto(GroupResponse source);

    /**
     * 转换模型
     *
     * @param source    数据源
     * @param groupCode 数据源
     * @return 模型
     * @since v5.7.0
     */
    @Mappings({
            @Mapping(target = "groupId", ignore = true),
            @Mapping(target = "groupName", ignore = true),
    })
    ProjectDTO toDto(ProjectResponse source, String groupCode);

}
