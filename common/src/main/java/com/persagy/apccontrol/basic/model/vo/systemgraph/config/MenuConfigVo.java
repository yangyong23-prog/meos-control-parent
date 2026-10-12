package com.persagy.apccontrol.basic.model.vo.systemgraph.config;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 菜单配置
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/3/7 11:27
 * @since 6.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel("菜单配置")
public class MenuConfigVo {

    @ApiModelProperty("一级菜单")
    private String firstlevelMenu;

    @ApiModelProperty("二级菜单")
    private String secondaryMenu;

    @ApiModelProperty("三级菜单")
    private String threeLevelMenu;

    @ApiModelProperty("动态菜单名称")
    private String dynamicName;

    @ApiModelProperty("图名/产品名")
    private String imageOrProductName;

    @ApiModelProperty("产品模块")
    private String productModule;

    @ApiModelProperty("基准对象")
    private String diagramBaseObject;

    @ApiModelProperty("关系图图类型")
    private String graphTypeCode;

    @ApiModelProperty("菜单编码")
    private String menuCode;

    @ApiModelProperty("对象类型")
    private String objectType;

    @ApiModelProperty("是否有控制")
    private int isControl;

    @ApiModelProperty("菜单层级")
    private int hierarchy;

    @ApiModelProperty("系统判断条件")
    private JSONObject systemJudgmentCondition;

}
