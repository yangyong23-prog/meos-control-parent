package com.persagy.apccontrol.basic.model.vo.duty;


import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeFigureVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 客流卡片数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PassengerFlowCardDataVO extends BasicsCardVO {

    @Schema(description = "客流今日进入人次趋势图")
    private List<InfoCodeFigureVO> passengerFlowFigure;

    @Schema(description = "今日累计进入人次")
    private Long todayTotalNumberOfPeople;

    @Schema(description = "卡片标签数据")
    private CardTagVO cardTag;

}
