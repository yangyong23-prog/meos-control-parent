package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 空调末端-品质统计VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-品质统计VO")
public class AirTerminalQualityStatisticsVO {

    @Schema(description = "品质类型")
    private List<BaseEnumVO> types;

    @Schema(description = "总空间数")
    private Integer totalSpaceCount;

    @Schema(description = "总有值数")
    private Integer totalCount;

    @Schema(description = "最小值")
    private BigDecimal minimum;

    @Schema(description = "最大值")
    private BigDecimal maximum;

    @Schema(description = "平均值")
    private BigDecimal average;

    @Schema(description = "中位数")
    private BigDecimal median;

    @Schema(description = "上四分位数")
    private BigDecimal upperQuartile;

    @Schema(description = "下四分位数")
    private BigDecimal lowerQuartile;

}
