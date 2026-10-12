package com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportStatusEnum;
import lombok.Data;

@Data
public class RunAnalysisReportACConfig extends RunAnalysisReportConfig {
   protected String des;
}