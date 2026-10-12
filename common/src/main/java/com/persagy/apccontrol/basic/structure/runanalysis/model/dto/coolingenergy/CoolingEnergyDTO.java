package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingenergy;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 冷量及能耗DTO
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CoolingEnergyDTO extends RunAnalysisReportDataDto {

    /**
     * 关联系统
     */
    private List<DtpRwdObjectDTO> relSystems;

    /**
     * 当日累计供冷量
     */
    private BigDecimal dayAccCool;

    /**
     * 当日累计耗电量
     */
    private BigDecimal dayAccElecConsum;

    /**
     * 累计差分供冷量
     */
    private Map<LocalDateTime, BigDecimal> dateTime2AccCool;

    /**
     * 累计差分耗电量
     */
    private Map<LocalDateTime, BigDecimal> dateTime2AccElecConsum;

}
