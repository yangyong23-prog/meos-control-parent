package com.persagy.apccontrol.basic.model.vo.security;

import com.persagy.apccontrol.basic.model.enumtype.security.AccessDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.DeploymentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 安防-Dashboard偏离空间VO
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
@Schema(title = "安防-Dashboard偏离空间VO")
public class SecurityDashboardDeviatedSpaceVO {

    @Schema(description = "楼层ID")
    private String id;

    @Schema(description = "楼层名称")
    private String localName;

    @Schema(description = "撤布防需求状态")
    private DeploymentStatusEnum demandArmingStatus;

    @Schema(description = "撤布防实际状态")
    private DeploymentStatusEnum realArmingStatus;

    @Schema(description = "通行需求状态")
    private AccessDemandStatusEnum demandAccessStatus;

    @Schema(description = "通行实际状态")
    private AccessDemandStatusEnum realAccessStatus;

}
