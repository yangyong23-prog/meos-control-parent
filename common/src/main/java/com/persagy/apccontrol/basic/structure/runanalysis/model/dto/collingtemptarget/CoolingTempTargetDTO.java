package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.collingtemptarget;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 空间温度目标DTO
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CoolingTempTargetDTO extends RunAnalysisReportDataDto {

    /**
     * 温度孪生控制目标
     */
    private Map<LocalDateTime, BigDecimal> dateTime2TempTwinControlAim;

    /**
     * 空气（干球）温度
     */
    private Map<LocalDateTime, BigDecimal> dateTimeTdb;

    /**
     * 价值选择配置
     */
    private JSONObject currentTempAndCarbonRate;

}
