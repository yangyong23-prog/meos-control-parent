package com.persagy.apccontrol.basic.model.param.runanalysis;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AnalysisListEquipmentParam {

    @NotNull(message = "{key_734676aba0524a718d4831a8e284dd0b}")
    private String classCode;

    /**
     * 运行监测二级
     * RUN_ANALYSIS_AIR_TERMINAL 末端设备分析
     * RUN_ANALYSIS_FRESH_AIR 新风分析
     */
    @NotNull(message = "{key_3196cbb51d704ac2ad1f32b83e77e6bf}")
    private RunningMonitorSecondTypeEnum secondType;

}
