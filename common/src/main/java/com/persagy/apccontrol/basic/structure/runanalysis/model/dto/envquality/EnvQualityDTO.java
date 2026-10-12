package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.envquality;

import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 环境品质 DTO
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EnvQualityDTO extends RunAnalysisReportDataDto {

    /**
     * 空间数据
     */
    private EnvQualitySpaceDTO space;

}
