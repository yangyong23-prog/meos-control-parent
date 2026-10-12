package com.persagy.apccontrol.basic.model.param.dangerevent;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wxf
 * @description
 * @date 14:03 2025/2/14
 **/
@Data
public class QueryByObjectParam {

    private List<String> objectIds;

    /**
     * 是否仅返回主动管理的风险事件实例，默认值：false
     */
    private Boolean followMark;

    @Schema(description = "运行监测一级类型")
    private RunningMonitorTypeEnum runningMonitorType;

}
