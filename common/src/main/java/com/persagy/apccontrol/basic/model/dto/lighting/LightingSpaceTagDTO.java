package com.persagy.apccontrol.basic.model.dto.lighting;

import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingRunningDeviationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.Nullable;

import java.util.Set;

/**
 * 照明-空间标签DTO
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "照明-空间标签DTO")
public class LightingSpaceTagDTO extends LightingBaseTagDTO {

    @Schema(description = "空间ID")
    private String spaceId;

    @Schema(description = "运行偏差类型")
    private Set<LightingRunningDeviationTypeEnum> runningDeviationTypes;

    @Schema(description = "参考面照度")
    @Nullable
    private String workPlaneLux;

}
