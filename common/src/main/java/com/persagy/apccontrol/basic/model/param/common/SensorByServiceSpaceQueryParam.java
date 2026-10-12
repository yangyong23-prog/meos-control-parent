package com.persagy.apccontrol.basic.model.param.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 查询服务空间的传感器信息（设备服务空间关系）
 *
 * @author 薛涛
 * @since 5.240602.0
 */
@Data
public class SensorByServiceSpaceQueryParam {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "空间ID")
    @NotEmpty(message = "{key_c78c2ee89cbd491ea193306bff87f524}")
    private String spaceId;



}
