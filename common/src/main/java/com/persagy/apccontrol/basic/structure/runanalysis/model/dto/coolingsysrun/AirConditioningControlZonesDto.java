package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun;

import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.ExceptionRecordDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Author weiyizhong
 * @Date 2026/2/7 11:12
 * @Company yushu.biz
 * @Description: 空调控制分区
 */
@Data
public class AirConditioningControlZonesDto {

    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "对象名称")
    private String name;

    @Schema(description = "对象类型")
    private String objType;

    @Schema(description = "类型编码")
    private String classCode;

    @Schema(description = "调节记录")
    private AdjustmentRecords adjustmentRecords;

    @Schema(description = "实际运行")
    private List<IotFieldRecord> actualRun;

    @Schema(description = "设备异常记录")
    private List<ExceptionRecordDTO> exceptionRecord;




}
