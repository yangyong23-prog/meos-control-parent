package com.persagy.apccontrol.basic.model.param.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 楼层查询参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@Schema(title = "楼层查询参数")
public class FloorQueryParam {

    @Schema(description = "建筑ID, 为空时查询所有")
    private List<String> buildingIds;

    @Schema(description = "楼层ID, 为空时查询所有")
    private List<String> floorIds;

    @Schema(description = "查询字段, 为空时查询所有")
    private String[] withColumns;

}
