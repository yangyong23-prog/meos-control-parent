package com.persagy.apccontrol.basic.model.param.runanalysis;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AnalysisInfoCodeParam {

    /**
     * 对象id列表
     */
    @NotNull(message = "{key_9f4f3dd621fb415e88ff0e83734a6710}")
    private List<String> objectIds;

    /**
     * 运行监测二级
     * RUN_ANALYSIS_AIR_TERMINAL 末端设备分析
     * RUN_ANALYSIS_FRESH_AIR 新风分析
     */
    @NotNull(message = "{key_3196cbb51d704ac2ad1f32b83e77e6bf}")
    private RunningMonitorSecondTypeEnum secondType;
}
