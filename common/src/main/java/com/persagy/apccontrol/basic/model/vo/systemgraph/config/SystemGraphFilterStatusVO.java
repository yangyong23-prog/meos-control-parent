package com.persagy.apccontrol.basic.model.vo.systemgraph.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统图滤镜状态")
public class SystemGraphFilterStatusVO {

    @Schema(description = "产品分组")
    private String group;
    @Schema(description = "设备类名称")
    private String className;
    @Schema(description = "设备类编码")
    private String classCode;
    @Schema(description = "滤镜状态1：默认图标，2：运行状态，3：开闭状态, 4:设备组“运行”状态")
    private String filterStatus;
}
