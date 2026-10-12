package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 空调末端-品质类型列表VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-品质类型VO")
public class AirTerminalQualityIntervalVO {

    @Schema(description = "温度区间")
    private List<BaseEnumVO> temperatureIntervals;

    @Schema(description = "CO₂区间")
    private List<BaseEnumVO> carbonDioxideIntervals;

}
