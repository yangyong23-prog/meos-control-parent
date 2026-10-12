package com.persagy.apccontrol.basic.model.param.duty;


import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


/**
 * 值班首页查询接口参数
 */
@Data
@Schema(title = "值班首页查询接口参数")
public class HomePageParam {

    @Schema(description = "筛选系统列表")
    private List<RunningMonitorTypeEnum> systemList;

}
