package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 末端-物理世界空间对象VO
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "末端-物理世界空间对象VO")
public class AirDtpRwdSpaceVO extends DtpRwdSpaceVO {

    @Schema(description = "是否存在温度规则")
    private Boolean tdbRule;

    @Schema(description = "是否存在 co2 规则")
    private Boolean co2Rule;
}
