package com.persagy.apccontrol.basic.model.param.elevator;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/8/17 14:58
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ElevatorStatisticsParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_79ae698d8ca34f22a0784247c6543796}")
    private String projectId;

    @Schema(description = "实例ID")
    @NotBlank(message = "{key_0a46a15af9454051b2964100753715aa}")
    private String instanceId;

    @Schema(description = "实例类型")
    @NotNull(message = "{key_8f11a8c423744b25b3ce8cf1c2bd7b7d}")
    private DemandInstanceTypeEnum instanceType;

    /*@Schema(description = "设备类")
    @NotBlank(message = "设备类，不可为空")*/
    private String classCode;

}
