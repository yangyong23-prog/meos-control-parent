package com.persagy.apccontrol.basic.model.vo.security;

import com.persagy.apccontrol.basic.model.enumtype.security.DeploymentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 空间安防统计
 */
@Data
public class SecuritySpaceStatisticsVo {

    @Schema(description = "总数")
    private int sum ;

    @Schema(description = "不同状态的数据统计")
    private List<SecuritySpaceStatusStatisticsVo> spaceList = new ArrayList<>();



    public SecuritySpaceStatisticsVo initData(){
       for(DeploymentStatusEnum spaceStatus: DeploymentStatusEnum.values()){
           SecuritySpaceStatusStatisticsVo vo = new SecuritySpaceStatusStatisticsVo();
           vo.setSpaceStatus(spaceStatus);
           spaceList.add(vo);
       }
       return this;

    }

}
