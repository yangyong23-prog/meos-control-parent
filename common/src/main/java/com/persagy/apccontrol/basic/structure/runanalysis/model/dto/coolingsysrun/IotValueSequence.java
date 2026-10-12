package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author weiyizhong
 * @Date 2026/2/7 14:11
 * @Company yushu.biz
 * @Description: iot 值序列
 */
@Data
public class IotValueSequence {

    @Schema(description = "时刻")
    private LocalDateTime timestamp;

    @Schema(description = "reg-整点值/rcov-切变+整点值,可以为空")
    private Object value;

}
