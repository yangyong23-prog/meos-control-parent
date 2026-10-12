package com.persagy.apccontrol.basic.model.vo.security;

import com.persagy.apccontrol.basic.model.enumtype.security.AccessDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.DeploymentStatusEnum;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 安防-空间信息
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/4 19:37
 * @since v5.240801.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SecuritySpaceStatusVO extends DtpRwdSpaceVO {

    @Schema(description = "撤布防状态")
    private DeploymentStatusEnum deploymentStatus;

    @Schema(description = "异常类型，为空时为正常")
    private Boolean abnormal;

    @Schema(description = "通行需求状态")
    private AccessDemandStatusEnum accessDemandStatus;

}
