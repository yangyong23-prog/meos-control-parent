package com.persagy.apccontrol.basic.model.param.strategy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 冷机数量查询参数
 *
 * @author LiuYang
 * @since 7.250201.0
 */
@Data
@Schema(title = "冷机数量查询参数")
public class ChillersNumberQueryParam {

    @Schema(description = "中央供冷系统ID")
    @NotBlank(message = "{key_f424e5924de240339438b71ec6a25e84}")
    private String systemId;

}
