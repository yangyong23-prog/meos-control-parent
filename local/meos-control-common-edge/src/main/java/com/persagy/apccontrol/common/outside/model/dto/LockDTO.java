package com.persagy.apccontrol.common.outside.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author tangzy
 * @date 2023年03月17日 15:51
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Schema(title = "切换设备锁定状态入参")
@ToString(callSuper = true)
public class LockDTO extends com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.TwcInstructionSend {



    @Schema(description = "信息点")
    private String infoCode;

    @Schema(description = "操作内容")
    private String content;

    @Schema(description = "修改前内容")
    private String beforeModify;

    @Schema(description = "锁状态")
    private Integer lock;

    @Schema(description = "操作类型")
    private String operateType;
}
