package com.persagy.apccontrol.basic.model.param.lighting;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Set;

/**
 * 照明空间的回路功能类型和关联的控制模块-实例查询条件
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LightingSpaceFuncTypeCircuitParam {
    
    @Schema(description = "空间 ID 集合")
    @NotEmpty(message = "{key_7e03303094f04b7dadd6c2c40acad8a7}")
    private Set<String> spaceIds;
    

    @Schema(description = "运行监测二级类型")
    @NotNull(message = "{key_04f7774960b7463cb4c3a33200ae4e11}")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;
    
    @Schema(description = "需要过滤的照明回路 ID列表")
    private Collection<String> filterCircuitIds;


}
