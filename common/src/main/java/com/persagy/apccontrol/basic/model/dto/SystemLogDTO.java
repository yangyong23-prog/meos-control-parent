package com.persagy.apccontrol.basic.model.dto;

import com.persagy.apccontrol.basic.model.enumtype.system.LogSourceEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.LogTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * 运行日志查询
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/20 18:20
 */
@Getter
@Setter
@ToString
public class SystemLogDTO {

    @Schema(description = "集团编码")
    private String groupCode;

    @Schema(description = "项目编码")
    @NotBlank(message = "{key_79ae698d8ca34f22a0784247c6543796}")
    private String projectId;

    @Schema(description = "系统ID")
    private String systemId;

    @Schema(description = "设备类编码")
    private String classCode;

    @Schema(description = "对象ID")
    private String objectId;

    @Schema(description = "日志来源")
    private LogSourceEnum logSource;

    @Schema(description = "日志类型")
    private LogTypeEnum logType;

    @NotBlank(message = "{key_d9f74d555a264d8da969f5a23c785e72}")
    @Schema(description = "日志查询的开始时间")
    private String startTime;

    @NotBlank(message = "{key_34e4b4584ec4464c8f8accecbb63d1fa}")
    @Schema(description = "日志查询的结束时间")
    private String endTime;

    @Schema(description = "是否排除页面播报")
    private Boolean excludePageBroadcast;

}
