package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 照明楼层带状态VO
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "照明楼层带状态VO")
public class LightingFloorWithStatusVO extends DtpRwdObjectVO {

    @Schema(description = "灯具功能类型带状态VO")
    private List<LightingLampFuncTypeWithStatusVO> lampFuncTypes;

}
