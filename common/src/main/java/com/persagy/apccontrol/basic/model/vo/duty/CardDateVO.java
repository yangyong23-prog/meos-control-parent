package com.persagy.apccontrol.basic.model.vo.duty;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 大卡片数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CardDateVO<T extends BasicsChildCardDataVO> extends BasicsCardVO {

    @Schema(description = "卡片标签数据")
    private CardTagVO cardTag;

    @Schema(description = "子卡片数据")
    private List<T> childCardList;

}
