package com.persagy.apccontrol.basic.model.param.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 照明-查询回路配置结果
 *
 */
@Data
@Schema(title = "照明-查询回路配置结果")
public class CircuitConfigResultsQueryParam {
    
    @Schema(description = "需求模版 ID")
    @NotNull(message = "{key_96be0b8e285a4e1697f3c8af52b152ad}")
    private Long demandTemplateId;
    
    @Schema(description = "场景 ID")
    @NotNull(message = "{key_84e23df06150448490c010470effcfac}")
    private Long sceneId;
    
    @Schema(description = "回路 ID 列表")
    private List<String> circuitIds;
    
    
}
