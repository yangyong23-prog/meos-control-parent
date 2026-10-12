package com.persagy.apccontrol.basic.model.vo.weakpower;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 弱电感知系统对象VO
 *
 * @author JiYinchuan
 * @since 7.250301.0
 */
@Data
@Schema(title = "弱电感知系统对象VO")
public class WeakPowerSystemVO {

    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "系统ID")
    private String systemId;

    @Schema(description = "系统名称")
    private String systemLocalName;

    @Schema(description = "类型编码")
    private String classCode;

}
