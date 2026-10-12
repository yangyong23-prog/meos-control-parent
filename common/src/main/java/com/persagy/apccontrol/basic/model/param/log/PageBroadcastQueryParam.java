package com.persagy.apccontrol.basic.model.param.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(title = "页面播报查询参数")
public class PageBroadcastQueryParam {
    @Schema(description = "项目ID")
    private String projectId;
    
    @Schema(description = "系统ID")
    @NotBlank(message = "{key_6fc14063bc9143f19673d1d21b6c5e4d}")
    private String systemId;
    
    public PageBroadcastQueryParam(String projectId, String systemId) {
        this.projectId = projectId;
        this.systemId = systemId;
    }
}
