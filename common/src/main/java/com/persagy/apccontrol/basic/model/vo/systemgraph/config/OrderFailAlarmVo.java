package com.persagy.apccontrol.basic.model.vo.systemgraph.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 控反不一致配置
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/10 13:47
 * @since 240502.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel("控返不一致")
public class OrderFailAlarmVo {

    @ApiModelProperty("对象类code")
    private String classCode;

    @ApiModelProperty("对象类名称")
    private String className;

    @ApiModelProperty("信息点名称")
    private String infoName;

    @ApiModelProperty("信息点编码")
    private String infoCode;

    @ApiModelProperty("信息点类型")
    private String infoType;

    @ApiModelProperty("信息点值")
    private String infoKey;

}
