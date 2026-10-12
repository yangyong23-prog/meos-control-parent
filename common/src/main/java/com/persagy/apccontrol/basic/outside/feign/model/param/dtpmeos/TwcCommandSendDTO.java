package com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos;

import com.persagy.apccontrol.basic.model.dto.ExtendSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * 单点控制参数
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Schema(title = "手动设定控制指令")
@ToString(callSuper = true)
public class TwcCommandSendDTO {
    
    @Schema(description = "项目ID")
    @NotBlank(message = "{key_61bdc1ef7fa249738155e48dcb89f5c8}")
    private String projectId;
    
    @Schema(description = "系统ID")
    private String systemId;
    
    @Schema(description = "对象ID")
    private String objectId;
    
    @Schema(description = "设定信息点编码")
    private String infoCode;
    
    @Schema(description = "设定值")
    private String infoValue;
    
    @Schema(description = "上一次的设定值")
    private String beforeValue;
    
    @Schema(description = "操作原因")
    private String content;
    
    @Schema(description = "扩展信息")
    private ExtendSetting extendSetting;
    
}

