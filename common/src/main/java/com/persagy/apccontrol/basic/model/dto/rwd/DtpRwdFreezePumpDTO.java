package com.persagy.apccontrol.basic.model.dto.rwd;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.freezepumpgroup.DtpRwdFreezePumpGroupInfoCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 物理世界冷冻泵DTO
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界冷冻泵DTO")
public class DtpRwdFreezePumpDTO extends DtpRwdObjectDTO {

    /**
     * 信息点-是否末级水泵组
     */
    public static final String INFO_CODE_IS_TERMINAL_PUMP = "isTerminalPump";

    // ------------------------- 标准字段 -------------------------

    @Schema(description = "是否末级水泵组")
    private Boolean isTerminalPump;

    // ------------------------- 拓展字段 -------------------------

    @Schema(description = "关联中央冷冻系统ID")
    private String relationAcccId;

    @Schema(description = "关联空调末端系统ID")
    private String relationAcatId;

    @Schema(description = "策略干预末级泵组信息点设置类型")
    private DtpRwdFreezePumpGroupInfoCodeEnum infoCodeSetType;

    @Schema(description = "策略干预末级泵组信息点设置单位")
    private String infoCodeUnit;

}
