package com.persagy.apccontrol.basic.model.vo.duty;

import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 空调冷源卡片VO
 *
 * @author JiYinchuan
 * @since 7.250601.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AirConditioningColdSourceCardDataVO extends BasicsCardVO {

    @Schema(description = "需求模版")
    private DemandTemplateVO demandTemplate;

    @Schema(description = "子卡片数据")
    private List<AirConditioningColdSourceChildCardDataVO> childCards;

    /**
     * 创建空卡片数据
     *
     * @return 空卡片数据
     * @since 7.250601.0
     */
    public static AirConditioningColdSourceCardDataVO empty() {
        AirConditioningColdSourceCardDataVO result = new AirConditioningColdSourceCardDataVO();
        result.setChildCards(Collections.emptyList());
        return result;
    }
}
