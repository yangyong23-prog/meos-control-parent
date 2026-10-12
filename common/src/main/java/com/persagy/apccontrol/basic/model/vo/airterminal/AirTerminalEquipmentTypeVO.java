package com.persagy.apccontrol.basic.model.vo.airterminal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

/**
 * 空调末端-设备类型VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-设备类型VO")
public class AirTerminalEquipmentTypeVO {

    @Schema(description = "设备类编码")
    private String classCode;

    @Schema(description = "设备类名称")
    private String classCodeName;

    @Schema(description = "关联空间ID")
    private Set<String> relSpaceIds;

}
