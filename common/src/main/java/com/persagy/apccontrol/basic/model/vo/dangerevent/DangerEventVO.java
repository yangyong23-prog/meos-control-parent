package com.persagy.apccontrol.basic.model.vo.dangerevent;

import com.persagy.apccontrol.basic.model.enumtype.dangerevent.StatusEnum;
import com.persagy.apccontrol.basic.outside.enums.MonitoringSituationsEnum;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.dto.EnumToObjectDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wxf
 * @date 18:14 2025/2/12
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "风险事件")
public class DangerEventVO {

    @Schema(description = "风险事件Id")
    private String dangerEventId;

    @Schema(description = "设备实例Id")
    private String objectId;

    @Schema(description = "风险事件状态")
    private StatusEnum status;

    @Schema(description = "是否在关注")
    private boolean followMark;

    @Schema(description = "风险事件名称")
    private String name;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 策略覆盖比例
     */
    private Double strategyCoverRatio;
    /**
     * 本地名称
     */
    private String localName;
    /**
     * 监控情况
     */
    private List<MonitoringSituationsEnum> monitoringSituations;
    /**
     * 超征兆问题等级
     */
    private EnumToObjectDTO symptomaticValueLevel;
    /**
     * 异常问题等级
     */
    private EnumToObjectDTO exceptionValueLevel;
    /**
     * 异常问题等级
     */
    private EnumToObjectDTO ordinary;

}
