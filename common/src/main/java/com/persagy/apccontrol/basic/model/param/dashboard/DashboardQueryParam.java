package com.persagy.apccontrol.basic.model.param.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/9 7:54 PM
 */
@Data
public class DashboardQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "系统ID")
    @NotBlank(message = "{key_6fc14063bc9143f19673d1d21b6c5e4d}")
    private String systemId;

    @Schema(description = "系统编码")
    @NotBlank(message = "{key_e11a2f1663394c5888457493450b1e24}")
    private String systemCode;
}
