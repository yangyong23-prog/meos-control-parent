package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 照明-照明控制模块详情VO
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "照明-照明控制模块详情VO")
public class LightingControllerDetailVO extends DtpRwdObjectVO {

    @Schema(description = "照明控制模块标签")
    private LightingControllerTagVO tag;

    @Schema(description = "异常事件")
    private List<ExceptionEventBaseInfoVO> abnormalEvents;

    @Schema(description = "关联照明回路")
    private List<LightingCircuitDetailVO> relCircuits;

    @Schema(description = "关联低压末端配电柜")
    private DtpRwdObjectVO relDistributionCabinet;

    @Schema(description = "关联空间")
    private DtpRwdObjectVO relSpace;

    @Schema(description = "关联楼层")
    private DtpRwdObjectVO relFloor;

}
