package com.persagy.apccontrol.basic.model.dto.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 自控参数下发
 *
 * @author yuyao
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpTwinAutoBatchSendDTO extends DtpTwinAutoSendDTO {

    /**
     * 自控指令会在手动下 是否生效 默认生效
     */
    @Schema(description = "自控指令会在手动下 是否生效 默认生效")
    private Boolean ignoreLock = true;

}

