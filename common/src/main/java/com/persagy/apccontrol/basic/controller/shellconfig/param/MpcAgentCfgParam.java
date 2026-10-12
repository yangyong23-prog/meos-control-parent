package com.persagy.apccontrol.basic.controller.shellconfig.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "壳配置mpc 智能体配置")
@NoArgsConstructor
@AllArgsConstructor
public class MpcAgentCfgParam {
    
    @Schema(description = "空间")
    Boolean space;
    @Schema(description = "冷战")
    Boolean cold;
    @Schema(description = "末端泵")
    Boolean terminalPump;
    @Schema(name = "系统 ID")
    @NotNull(message = "系统 ID不能为空")
    private String systemId;
}