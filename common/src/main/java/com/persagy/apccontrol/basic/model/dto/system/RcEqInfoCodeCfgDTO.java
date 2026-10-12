package com.persagy.apccontrol.basic.model.dto.system;

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
@Schema(title = "查询设备信息关系表入参")
@ToString(callSuper = true)
public class RcEqInfoCodeCfgDTO {

    @Schema(description = "设备id")
    private String equipmentId;
    /**
     * 参数类型枚举字段说明: ParameterTypeEnum
     */
    @Schema(description = "参数类型")
    private String parameterType;

    @Schema(description = "项目id")
    private String projectId;

    @Schema(description = "信息点")
    private String infoCode;

    @Schema(description = "设备类")
    private String classCode;

    @Schema(description = "锁定状态")
    private String lock;

    @Schema(description = "系统id")
    private String systemId;

}
