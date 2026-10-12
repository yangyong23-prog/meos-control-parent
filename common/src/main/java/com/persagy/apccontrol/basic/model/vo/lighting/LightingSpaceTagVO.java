package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 照明-空间标签VO
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Schema(title = "照明-空间标签VO")
public class LightingSpaceTagVO {

    @Schema(description = "开关状态")
    private BaseEnumVO openCloseStatus;

    @Schema(description = "异常状态")
    private List<BaseEnumVO> abnormalStatuses;

    @Schema(description = "运行偏差类型")
    private Set<BaseEnumVO> runningDeviationTypes;

    @Schema(description = "干预类型")
    private List<BaseEnumVO> interventionTypes;

}
