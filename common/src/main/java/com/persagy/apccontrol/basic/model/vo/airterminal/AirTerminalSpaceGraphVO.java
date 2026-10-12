package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalSpaceTagDTO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 空调末端-空间图VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "空调末端-空间图VO")
public class AirTerminalSpaceGraphVO extends DtpRwdSpaceVO {

    @Schema(description = "是否无法监测")
    private Boolean unableMonitor;

    @Schema(description = "是否为受控制")
    private Boolean controlled;

    @Schema(description = "受控制关联设备ID")
    private Set<String> controlledRelEquipmentIds;

    @Schema(description = "空间标签")
    private AirTerminalSpaceTagDTO tag;

    @Schema(description = "是否存在温度规则")
    private Boolean tdbRule;

    @Schema(description = "是否存在 co2 规则")
    private Boolean co2Rule;

}
