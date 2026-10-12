package com.persagy.apccontrol.basic.model.vo.duty;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 冷热源大卡片数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HotColdCardDataVO extends BasicsCardVO {

    @Schema(description = "子卡片数据")
    private List<HotColdChildCardDataVO> hotColdchildCardVOList;

}
