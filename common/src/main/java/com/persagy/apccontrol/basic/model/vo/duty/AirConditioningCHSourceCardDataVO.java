package com.persagy.apccontrol.basic.model.vo.duty;

import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 空调冷热源卡片VO
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AirConditioningCHSourceCardDataVO extends BasicsCardVO {

    @Schema(description = "需求模版")
    private DemandTemplateVO demandTemplate;

    @Schema(description = "子卡片数据")
    private List<AirConditioningColdSourceChildCardDataVO> childCards;

    /**
     * 创建空卡片数据
     *
     * @return 空卡片数据
     * @since 7.260302.0
     */
    public static AirConditioningCHSourceCardDataVO empty() {
        AirConditioningCHSourceCardDataVO result = new AirConditioningCHSourceCardDataVO();
        result.setChildCards(Collections.emptyList());
        return result;
    }
}
