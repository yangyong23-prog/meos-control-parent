package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 空调末端-设备状态统计VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "空调末端-设备状态统计VO")
public class AirTerminalFloorEquipmentTypeStatisticsVO extends DtpRwdObjectVO {

    @Schema(description = "关联楼层")
    private List<AirTerminalFloorEquipmentTypeStatisticsVO> relFloors;

    @Schema(description = "设备统计")
    private AirTerminalEquipmentTypeStatisticsVO equipmentStatistics;

    @Schema(description = "设备类分组统计")
    private List<AirTerminalEquipmentTypeStatisticsVO> equipmentGroupStatistics;

}
