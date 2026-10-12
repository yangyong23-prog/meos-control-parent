package com.persagy.apccontrol.basic.model.param.rwd;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * 建筑楼层空间树查询参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@Schema(title = "建筑楼层空间树查询参数")
public class TreeBuildingFloorSpaceQueryParam {

    @Schema(description = "中央供冷系统ID")
    private String systemId;

    @Schema(description = "是否查询空间 默认true")
    private Boolean isQuerySpace;

    @Schema(description = "实例类型 -> 查询字段, 为空时查询所有")
    private Map<InstanceTypeEnum, Set<String>> instanceTypeAndWithColumns;

}
