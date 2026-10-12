package com.persagy.apccontrol.basic.model.param.bizcheck;

import com.persagy.apccontrol.basic.model.dto.StartEndDayDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 *
 * @author wxf
 * @date 2026-02-26
 */
@Data
public class BizCheckConfigParam {
    @Schema(description = "配置id")
    private Long id;
    /**
     * 子业务编码
     */
    @Schema(description = "子业务编码")
    @NotNull(message = "子业务编码不能为空")
    private RunningMonitorTypeEnum bizCode;

    /**
     * 是否启用检查：1=启用，0=禁用
     */
    @Schema(description = "是否启用检查：1=启用，0=禁用")
    private Boolean isEnable;

    /**
     * 是否启用自动检查：1=启用，0=禁用
     */
    @Schema(description = "是否启用自动检查：1=启用，0=禁用")
    private Boolean isAutoCheck;

    /**
     * 自动检查的每日时间，如10:00
     */
    @Schema(description = "自动检查的每日时间，如10:00")
    private String autoCheckTime;

    /**
     * 日期段配置，JSON格式存储
     * 示例：[{"startDay":"01-26","endDay":"01-30"}]
     * 注：如需结构化处理，可定义对应的DTO类，结合TypeHandler实现JSON与Java对象的转换
     */
    @Schema(description = "日期段配置，JSON格式存储")
    private List<StartEndDayDTO> dateRangeConfig;

    /**
     * 异常通知时段配置，JSON格式存储
     */
    @Schema(description = "异常通知时段配置，JSON格式存储")
    private List<NotifyPeriodConfigParam> notifyPeriodConfig;

}