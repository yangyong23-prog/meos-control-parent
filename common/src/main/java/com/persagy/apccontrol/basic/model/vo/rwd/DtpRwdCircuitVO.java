package com.persagy.apccontrol.basic.model.vo.rwd;

import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 物理世界照明回路对象VO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S2-5.18.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界照明回路对象VO")
public class DtpRwdCircuitVO extends DtpRwdObjectVO {

    @Schema(description = "服务区域类型")
    private BaseEnumVO serviceAreaType;

    @Schema(description = "功能类型")
    private BaseEnumVO funcType;

    @Schema(description = "灯具类型")
    private BaseEnumVO lampFuncType;

}
