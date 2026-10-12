package com.persagy.apccontrol.basic.model.vo.duty;


import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大卡片
 */
@Data
public class BasicsCardVO {

    @Schema(description = "系统")
    RunningMonitorTypeEnum RunningMonitorType;

}
