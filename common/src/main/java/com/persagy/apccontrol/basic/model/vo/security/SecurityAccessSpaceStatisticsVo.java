package com.persagy.apccontrol.basic.model.vo.security;


import com.persagy.apccontrol.basic.model.enumtype.security.AccessDemandStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SecurityAccessSpaceStatisticsVo {


    @Schema(description = "总数")
    private int sum;

    @Schema(description = "不同状态的数据统计")
    private List<SecurityAccessSpaceStatusStatisticsVo> spaceList = new ArrayList<>();


    public SecurityAccessSpaceStatisticsVo initData() {
        for (AccessDemandStatusEnum statusEnum : AccessDemandStatusEnum.values()) {
            SecurityAccessSpaceStatusStatisticsVo vo = new SecurityAccessSpaceStatusStatisticsVo();
            vo.setSpaceStatus(statusEnum);
            spaceList.add(vo);
        }
        return this;

    }
}
