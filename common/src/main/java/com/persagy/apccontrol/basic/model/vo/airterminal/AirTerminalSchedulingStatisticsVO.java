package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalDemandStatusEnum;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 空调末端-调度统计VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-调度统计VO")
public class AirTerminalSchedulingStatisticsVO {

    @Schema(description = "品质类型")
    private List<BaseEnumVO> types;

    @Schema(description = "总数")
    private Integer totalCount;

    @Schema(description = "需求状态数量")
    private Map<AirTerminalDemandStatusEnum, Integer> demandStatusCount;


}
