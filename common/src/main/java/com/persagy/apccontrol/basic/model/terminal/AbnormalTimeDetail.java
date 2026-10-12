package com.persagy.apccontrol.basic.model.terminal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/12/7 10:17
 */
@Data
public class AbnormalTimeDetail {

    @Schema(description = "超上限-时段")
    private List<LocalTime> upperLimitTime;

    @Schema(description = "超上限-时段")
    private List<LocalTime> lowerLimitTime;

    @Schema(description = "偏离目标-时段")
    private List<LocalTime> offsetTargetTime;

}
