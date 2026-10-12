package com.persagy.apccontrol.basic.model.vo.airterminal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * 空调末端-设备状态统计VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-设备状态统计VO")
public class AirTerminalEquipmentTypeStatisticsVO {

    @Schema(description = "设备类编码")
    private String classCode;

    @Schema(description = "设备类名称")
    private String classCodeName;

    @Schema(description = "总数量")
    private Integer totalCount;

    @Schema(description = "总开启数量")
    private Integer totalOpenCount;

    @Schema(description = "总关闭数量")
    private Integer totalCloseCount;

    @Schema(description = "总未知数量")
    private Integer totalUnknownCount;

    /**
     * 初始化数量
     *
     * @return 当前实例
     * @since 5.240502.0
     */
    public AirTerminalEquipmentTypeStatisticsVO initCount() {
        if (Objects.isNull(this.totalCount)) {
            this.totalCount = 0;
        }
        if (Objects.isNull(this.totalOpenCount)) {
            this.totalOpenCount = 0;
        }
        if (Objects.isNull(this.totalCloseCount)) {
            this.totalCloseCount = 0;
        }
        if (Objects.isNull(this.totalUnknownCount)) {
            this.totalUnknownCount = 0;
        }
        return this;
    }
}
