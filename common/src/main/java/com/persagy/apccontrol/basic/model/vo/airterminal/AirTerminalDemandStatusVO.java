package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 空调末端-需求状态列表VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-需求状态VO")
public class AirTerminalDemandStatusVO {

    @Schema(description = "温度需求状态")
    private List<BaseEnumVO> temperatureDemandStatuses;

    @Schema(description = "CO₂需求状态")
    private List<BaseEnumVO> carbonDioxideDemandStatuses;

}
