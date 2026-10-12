package com.persagy.apccontrol.basic.controller.shellconfig.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/8 6:02 PM
 */
@Data
public class QueryShellConfigParam {

    @NotNull(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @NotNull(message = "{key_f50f6fb295a841508be6af5ad04431b6}")
    private String scene;

    @Schema(description = "分组编码")
    private Set<String> groups;

}
