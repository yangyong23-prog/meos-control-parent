package com.persagy.apccontrol.basic.model.param.debugtool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * 根据对象id和模板id集合查询对象信息
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/1/22 11:03
 * @since v7.250102.0
 */
@Data
@Schema(title = "调试工具-根据对象id和模板id集合查询对象信息")
public class DebugToolObjectInfoCodesQueryParam {

    @Schema(description = "对象id")
    @NotBlank(message = "{key_084485a601e5437688eeacf92ba08c45}")
    private String objectId;

    @Schema(description = "模板id集合")
    @NotEmpty(message = "{key_bb1540d8209a4754b4fbbe3c52ecad1e}")
    private Set<Long> templateTypeIds;

}
