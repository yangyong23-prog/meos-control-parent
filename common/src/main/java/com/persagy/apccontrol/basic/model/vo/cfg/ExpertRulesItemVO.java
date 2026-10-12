package com.persagy.apccontrol.basic.model.vo.cfg;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @Description: 专家规则  配置扩展明细VO
 * @author: yancheng
 * @date: 2023/3/27
 * @version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExpertRulesItemVO  extends BasicsConfigVO{

    @Schema(description = "最大值(开关机时间-最早时间)")
    @NotNull(message = "{key_5539695fdd1745cf86c31634d3e0a5a1}")
    private BigDecimal max;

    @Schema(description = "最小值(开关机时间-最晚时间)")
    @NotNull(message = "{key_cc91ae3a05a84617802fa4d3513731c8}")
    private BigDecimal min;

    @Schema(description = "默认最大值")
    private BigDecimal defaultMax;
}
