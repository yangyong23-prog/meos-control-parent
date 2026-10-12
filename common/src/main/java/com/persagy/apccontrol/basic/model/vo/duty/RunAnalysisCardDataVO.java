package com.persagy.apccontrol.basic.model.vo.duty;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 空间分析卡片数据
 * @author xiaofengwang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RunAnalysisCardDataVO extends BasicsCardVO {

    @Schema(description = "异常事件总数")
    private Integer errorEventTotal;

    @Schema(description = "温度达标率")
    private Integer tdbSatisfyPercentage;

    @Schema(description = "过冷空间数")
    private Integer tooColdCount;

    @Schema(description = "偏冷空间数")
    private Integer coldCount;

    @Schema(description = "偏热空间数")
    private Integer hotCount;

    @Schema(description = "过热空间数")
    private Integer tooHotCount;

    @Schema(description = "特殊空间排行榜")
    private List<RunAnalysisSpaceVO> specialSpaceRankList;
}
