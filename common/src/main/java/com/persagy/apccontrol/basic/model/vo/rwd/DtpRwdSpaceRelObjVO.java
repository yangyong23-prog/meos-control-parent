package com.persagy.apccontrol.basic.model.vo.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 物理世界空间对象带关联对象VO
 *
 * @author wxf
 * @since 7.250101.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界空间对象带关联对象VO")
public class DtpRwdSpaceRelObjVO extends DtpRwdSpaceVO {

    @Schema(description = "关联对象ID")
    private Set<String> relObjIds;

}
