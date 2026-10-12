package com.persagy.apccontrol.basic.model.vo.computeroom;


import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * 机房安全-设备VO
 *
 * @author JiYinchuan
 * @since 7.250902.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@Schema(title = "机房安全-设备VO")
public class ComputerRoomEquipmentVo extends DtpRwdEquipmentDTO {

    /**
     * 设备类名称
     */
    private String classCodeName;

    /**
     * 设备重要层级
     */
    private String equipCriticalLevel;

}
