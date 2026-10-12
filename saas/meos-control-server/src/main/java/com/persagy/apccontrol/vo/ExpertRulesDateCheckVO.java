package com.persagy.apccontrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @Description: 策略时间校验参数
 * @author: yancheng
 * @date: 2023/3/31
 * @version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpertRulesDateCheckVO {

    @Schema(description = "项目id")
    @NotEmpty(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @Schema(description = "系统编号")
    @NotEmpty(message = "{key_19d7dc7042934ab9a93f77d2ade9956a}")
    private String systemId;

    @Schema(description = "开始日期")
    @NotNull(message = "{key_6dc6cc33082c41d189980dd415b06083}")
    private Date startTime;

    @Schema(description = "结束日期")
    @NotNull(message = "{key_fe10d89294244a90a5a3d488db40d202}")
    private Date endTime;

    @Schema(description = "规则策略Id，修改时传入")
    private String configId;
}
