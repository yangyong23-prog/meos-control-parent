package com.persagy.apccontrol.basic.model.param.runanalysis;

import com.persagy.apccontrol.basic.model.enumtype.runanalysis.AnalysisTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AnalysisDataTreeParam {

    @NotNull(message = "{key_50ce0de2fc194044af8bca037ee0d74c}")
    private AnalysisTypeEnum analysisType;

    @NotNull(message = "{key_6fc14063bc9143f19673d1d21b6c5e4d}")
    private String systemId;
}
