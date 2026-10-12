package com.persagy.apccontrol.basic.model.vo.systemgraph;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 交互配置
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/3/7 11:47
 * @since 6.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel("交互配置")
public class InteractiveConfigVo {

    /**
     * 点击事件
     */
    @ApiModelProperty("点击事件")
    private String clickEvent;
    /**
     * 交互对象类
     */
    @ApiModelProperty("交互对象类名称")
    private String interactiveObjectClass;
    /**
     * 交互对象编码
     */
    @ApiModelProperty("交互对象编码")
    private String interactiveObjectCode;
    /**
     * 跳转目标
     */
    @ApiModelProperty("跳转目标")
    private String jumpTarget;
    /**
     * 限制条件
     */
    @ApiModelProperty("限制条件")
    private String limitingCondition;
    /**
     * 图名
     */
    @ApiModelProperty("图名")
    private String mapTitle;
    /**
     * 跳转产品模块
     */
    @ApiModelProperty("跳转产品模块，根据对象类编码跳转")
    private String skipProductModule;

    @ApiModelProperty("系统对象id")
    private String systemObjectId;

    @ApiModelProperty("是否可跳转")
    private boolean canJump = true;

    @ApiModelProperty("关系图类型")
    private String graphTypeCode;

    @ApiModelProperty("关系")
    private String relation;
    @ApiModelProperty("关系图名称")
    private String graphName;

}
