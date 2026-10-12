package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.ExceptionRecordDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统能耗DTO
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
public class SystemEnergyDTO {

    /**
     * 系统对象
     */
    private DtpRwdObjectDTO system;

    /**
     * 系统日累计用电量
     */
    private BigDecimal dayAccElecConsum;

    /**
     * 系统累计用环比
     */
    private BigDecimal accElecConsumMom;

    /**
     * 系统累计用电量，时间-值
     */
    private Map<LocalDateTime, BigDecimal> dateTime2AccElecConsum;

    /**
     * 冷站能效映射，日期-能效指标-值
     */
    private Map<LocalDate, Map<String, BigDecimal>> date2Efficiency;

    /**
     * 信息点编码-时间-调节记录值
     */
    private Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> infoCode2DateTime2AdjustmentValue;

    /**
     * 信息点编码-实际运行值
     */
    private Map<String, List<SystemRealRunningDataDTO>> infoCode2RealRunningValue;

    /**
     * 异常记录
     */
    private List<ExceptionRecordDTO> exceptionRecords;

   /**
     * 空调控制分区
     */
    private List<AirConditioningControlZonesDto> airConditioningControlZones;

}
