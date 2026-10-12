package com.persagy.apccontrol.basic.model.vo.computeroom;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 机房安全Dashboard空间VO
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "机房安全Dashboard空间VO")
public class ComputerRoomDashboardSpaceVO extends DtpRwdSpaceVO {

    @Schema(description = "空间功能类型名称")
    private String roomFuncTypeName;

    @Schema(description = "空间下的设备")
    private List<ComputerRoomEquipmentVo> equipmentsInSpace;

}
