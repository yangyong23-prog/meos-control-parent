package com.persagy.apccontrol.basic.model.vo.systemgraph.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 系统运行参数
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/10 13:47
 * @since 24Q2T1-SP1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel("系统运行参数")
public class SystemRunParamVo {

    /**
     * 图名
     */
    @ApiModelProperty("图名")
    private String mapTitle;

    /**
     * 系统运行参数分组
     */
    @ApiModelProperty("系统运行参数分组")
    private String group;

    /**
     * 系统运行参数名称
     */
    @ApiModelProperty("系统运行参数名称")
    private String paramName;

    /**
     * 系统运行参数编码
     */
    @ApiModelProperty("系统运行参数编码")
    private String paramCode;

    /**
     * 信息点类型
     */
    @ApiModelProperty("信息点类型")
    private String infoType;

    /**
     * 是否定制
     */
    @ApiModelProperty("是否定制")
    private String isCustomize;

}
