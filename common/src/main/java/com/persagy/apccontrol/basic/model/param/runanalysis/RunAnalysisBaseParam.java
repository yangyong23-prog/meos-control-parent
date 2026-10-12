package com.persagy.apccontrol.basic.model.param.runanalysis;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class RunAnalysisBaseParam {


    /**
     * 查询日期
     */
    @NotNull(message = "{key_c9e5b3bfdc0345828c98d7c6f0514bbd}")
    private LocalDate queryDate;
}
