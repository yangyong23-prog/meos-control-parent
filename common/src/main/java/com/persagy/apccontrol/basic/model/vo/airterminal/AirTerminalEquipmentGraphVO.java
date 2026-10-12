package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalEquipmentTagDTO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdEquipmentVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 空调末端-设备图VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "空调末端-设备图VO")
public class AirTerminalEquipmentGraphVO extends DtpRwdEquipmentVO {

    @Schema(description = "是否为受控制")
    private Boolean controlled;

    @Schema(description = "设备标签")
    private AirTerminalEquipmentTagDTO tag;

    @Schema(description = "可监可控")
    private String monitoringSurveillanceStatus;

    @Schema(description = "空调机组控制组关联的空调机组ID")
    private Set<String> controlledAirHandlingUnitIds;

}
