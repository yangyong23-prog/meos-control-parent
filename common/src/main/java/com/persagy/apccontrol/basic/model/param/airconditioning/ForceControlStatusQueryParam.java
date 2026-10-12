package com.persagy.apccontrol.basic.model.param.airconditioning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 强制控制状态编辑参数
 *
 * @author JiYinchuan
 * @since 7.250403.0
 */
@Data
@Schema(title = "强制控制状态查询参数")
public class ForceControlStatusQueryParam {

    @Schema(description = "对象ID")
    @NotBlank(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private String objectId;

}
