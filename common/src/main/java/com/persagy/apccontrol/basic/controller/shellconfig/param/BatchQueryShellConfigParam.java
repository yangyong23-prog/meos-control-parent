package com.persagy.apccontrol.basic.controller.shellconfig.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/8 6:02 PM
 */
@Data
public class BatchQueryShellConfigParam {

    @NotNull(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    @Schema(name = "项目id")
    private String projectId;

    @NotNull(message = "{key_084485a601e5437688eeacf92ba08c45}")
    @Schema(name = "对象id")
    private String objectId;

    @NotNull(message = "{key_a5cd9536031f4f4e8c090154f86ef607}")
    @Schema(name = "配置所属分组")
    private String group;
}
