package com.persagy.apccontrol.basic.model.param.runanalysis;

import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class RunAnalysisReportChapterParam {
    @Schema(description = "章节code")
    @NotNull(message = "chapterCode can not be null")
    private String chapterCode;

    @Schema(description = "对象id")
    private String objectId;
}
