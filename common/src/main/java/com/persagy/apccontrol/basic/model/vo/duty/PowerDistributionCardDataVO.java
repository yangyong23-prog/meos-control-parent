package com.persagy.apccontrol.basic.model.vo.duty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 给排水卡片数据
 * @author xiaofengwang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PowerDistributionCardDataVO extends BasicsChildCardDataVO {
    @Schema(description = "对象ID")
    private String objectId;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "负载率")
    private String lULoadRate;
}
