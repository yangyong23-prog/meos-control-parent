package com.persagy.apccontrol.basic.model.vo.security;


import com.persagy.apccontrol.basic.model.enumtype.security.DeploymentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SecuritySpaceStatusStatisticsVo {

    @Schema(description = "空间状态")
    private DeploymentStatusEnum spaceStatus;

    @Schema(description = "对应设备状态的数量统计")
    private int count;
}
