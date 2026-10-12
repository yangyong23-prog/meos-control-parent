package com.persagy.apccontrol.common.model.param;

import com.persagy.apccontrol.basic.model.dto.ExtendSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author shaohongbo
 * @date 2023年03月29日 20:04
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Schema(title = "冷却塔出水温度配置")
@ToString(callSuper = true)
public class CoolWaterReturnTempCfgParam {


    @Schema(description = "系统ID")
    @NotBlank(message = "{key_a583017cbc084d8a8bad66873f3a4699}")
    private String systemId;

    @Schema(description = "信息点")
    @NotBlank(message = "{key_461eddeb31ae47fdac633efa4cccd6e7}")
    private String infoCode;

    @Schema(description = "操作原因")
    @NotBlank(message = "{key_461eddeb31ae47fdac633efa4cccd6e7}")
    private String content;
    
    @Schema(description = "修改前内容")
    private String beforeValue;

    @Schema(description = "扩展信息")
    private ExtendSetting extendSetting;


}

