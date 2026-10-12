package com.persagy.apccontrol.basic.controller.shellconfig.param;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @description: 时序大模型策略开关设置
 * @author: wxf
 * @create: 2025-05-07 16:09
 **/
@Data
@Schema(title = "时序大模型策略开关参数")
@NoArgsConstructor
@AllArgsConstructor
public class LtsmConfigParam {

    @Schema(description = "策略类型")
    @NotNull(message = "{key_d54e1825d4fd48f6b23f00bd635bf0b8}")
    BasicsConfigTypeEnum type;

    @Schema(description = "开关状态")
    @NotNull(message = "{key_a07ebb9d1afe4510ad00e2a86fb98856}")
    Boolean status;
}