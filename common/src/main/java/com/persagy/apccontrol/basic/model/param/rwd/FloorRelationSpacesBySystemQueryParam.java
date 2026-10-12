package com.persagy.apccontrol.basic.model.param.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 楼层关联空间查询参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@Schema(title = "楼层关联空间查询参数")
public class FloorRelationSpacesBySystemQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "中央供冷系统ID")
    @NotBlank(message = "{key_f424e5924de240339438b71ec6a25e84}")
    private String systemId;

    @Schema(description = "楼层ID")
    @NotBlank(message = "{key_4a7e14edef4d45f5a3c128903799b601}")
    private String floorId;

}
