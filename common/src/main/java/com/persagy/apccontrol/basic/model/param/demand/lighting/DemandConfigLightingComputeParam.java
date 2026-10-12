package com.persagy.apccontrol.basic.model.param.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 照明回路结果计算参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandConfigLightingComputeParam {
    @Schema(description = "模版 iD,触发计算必传")
    private Long templateId;
    
    @Schema(description = "场景 ID,触发计算必传")
    private Long sceneId;
    
    @Schema(description = "空间 ID集合")
    private Set<String> spaceIds;
    
}
