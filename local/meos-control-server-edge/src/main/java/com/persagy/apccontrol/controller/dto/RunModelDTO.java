package com.persagy.apccontrol.controller.dto;

import com.persagy.apccontrol.basic.model.dto.SystemDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author zhangyanchang
 * @version 1.0
 * @title: RunModelDTO
 * @title: RunModelDTO
 * @description: 运行模式实体
 * @date: 2023/12/1 16:51
 */
@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "修改运行模式")
public class RunModelDTO extends SystemDTO {

    @Schema(description = "运行模式")
    private MidCalendarAcSeasonTypeEnum runMode;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "原因")
    private String reason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "用户ID")
    private String userId;

    public RunModelDTO(MidCalendarAcSeasonTypeEnum runMode, String endTime) {
        this.runMode = runMode;
        this.endTime = endTime;
    }
}
