package com.persagy.apccontrol.basic.model.vo.systemgraph.config;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 图配置
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/3/7 11:46
 * @since 6.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel("图配置")
public class GraphConfigVo {

    /**
     * 图名
     */
    @ApiModelProperty("图名")
    private String mapTitle;

    /**
     * 类型
     */
    @ApiModelProperty("类型")
    private String type;

    /**
     * 关系图基准对象名称
     */
    @ApiModelProperty("关系图基准对象名称")
    private String diagramBaseObjectName;

    /**
     * 关系图基准对象编码
     */
    @ApiModelProperty("关系图基准对象编码")
    private String diagramBaseObjectCode;

    /**
     * 图对象类型
     */
    @ApiModelProperty("图对象类型")
    private String objectType;

    /**
     * 关系图图类型
     */
    @ApiModelProperty("关系图图类型")
    private String diagramTypeOfTheDiagram;

    /**
     * 楼层地图
     */
    @ApiModelProperty("楼层地图")
    private long floorMap;

    /**
     * 是否有控制
     */
    @ApiModelProperty("是否有控制")
    private int isControl;

    /**
     * 不可编辑
     */
    @ApiModelProperty("不可编辑")
    private int notEdit;

    /**
     * 是否有控反不一致
     */
    @ApiModelProperty("是否有控反不一致")
    private int isOrderFailAlarm;

    /**
     * 1. 完全展示整个图纸
     * 2. 100%缩放比例展示图纸
     * 3. 以图纸的宽度适配展示图纸
     * 4. 以图纸的高度适配展示图纸
     * 不填默认就是1
     */
    @ApiModelProperty("展示适配方式")
    private Integer displayMode;

    @ApiModelProperty("系统判断条件")
    private JSONObject systemJudgmentCondition;

}
