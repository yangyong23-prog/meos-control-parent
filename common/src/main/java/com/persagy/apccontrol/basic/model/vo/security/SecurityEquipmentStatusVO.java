package com.persagy.apccontrol.basic.model.vo.security;

import com.persagy.apccontrol.basic.model.enumtype.AbnormalEquipmentEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.AccessDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.IconTypeEnum;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdEquipmentVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/**
 * 安防设备状态
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/4 19:37
 * @since v5.240801.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SecurityEquipmentStatusVO extends DtpRwdEquipmentVO {

    @Schema(description = "异常类型，为空时为正常")
    private List<AbnormalEquipmentEnum> abnormalTypes;

    @Schema(description = "关联空间ID")
    private Set<String> relObjIds;

    @Schema(description = "是否可控")
    private Boolean isControl;

    @Schema(description = "是否有布撤防功能")
    private String hasArmingFunc;

    @Schema(description = "摄像头类型")
    private String cameraType;

    @Schema(description = "摄像头是否可控")
    private String cameraIsControl;

    @Schema(description = "图标类型")
    private IconTypeEnum iconType;

    @Schema(description = "门的通行状态")
    private AccessDemandStatusEnum accessDemandStatus;

    @Schema(description = "门的通行状态")
    private RunStatusEnum runStatus;

    @Schema(description = "所属厂家")
    private String manufacturer;

    @Schema(description = "空间功能类型")
    private List<String> roomFuncTypes;

}
