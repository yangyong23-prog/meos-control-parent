package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 定制功能
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/17 11:38
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CustomFunctionDTO {

    @Schema(description = "功能ID")
    private String funcId;

    @Schema(description = "功能编码")
    private String funcCode;

    @Schema(description = "功能名称")
    private String funcName;

    @Schema(description = "功能类型")
    private String funcType;

}
