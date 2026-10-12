package com.persagy.apccontrol.basic.model.vo.security;

import com.persagy.apccontrol.basic.model.enumtype.security.AccessDemandStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SecurityAccessSpaceStatusStatisticsVo {


    @Schema(description = "空间状态")
    private AccessDemandStatusEnum spaceStatus;

    @Schema(description = "对应设备状态的数量统计")
    private int count;
}
