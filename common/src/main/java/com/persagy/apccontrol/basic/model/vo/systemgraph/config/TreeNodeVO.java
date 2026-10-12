package com.persagy.apccontrol.basic.model.vo.systemgraph.config;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel("菜单配置")
public class TreeNodeVO {
    private String name;
    private String id;

    @ApiModelProperty("产品模块")
    private String productModule;

    @ApiModelProperty("图名")
    private String imageOrProductName;

    @ApiModelProperty("动态菜单名称")
    private String dynamicName;

    @ApiModelProperty("基准对象")
    private String diagramBaseObject;

    @ApiModelProperty("关系图图类型")
    private String graphTypeCode;

    @ApiModelProperty("本地名称")
    private String localName;

    @ApiModelProperty("本地id")
    private String localId;

    @ApiModelProperty("系统id")
    private String systemId;

    @ApiModelProperty("对象类")
    private String classCode;

    @ApiModelProperty("对象类型")
    private String objType;

    @ApiModelProperty("菜单编码")
    private String menuCode;

    @ApiModelProperty("子集")
    private List<TreeNodeVO> children;

    @ApiModelProperty("菜单层级")
    private Integer hierarchy;

    @ApiModelProperty("是否定制")
    private Integer isControl;

    @ApiModelProperty("工况展示：显示当前系统所处的工况")
    private String currentOperatingMode;

    @ApiModelProperty("可监可控状态")
    private String monitoringSurveillanceStatus;

    @ApiModelProperty("运行模式，仅空调冷热源存在")
    private String runModeSet;

    @ApiModelProperty("是否隐藏供冷控制, 隐藏为true,不隐藏为false")
    private Boolean unHideCoolingControl;

    @ApiModelProperty("是否隐藏蓄冷控制, 隐藏为true,不隐藏为false")
    private Boolean isShowCoolStore;

    @ApiModelProperty("是否隐藏空调分区控制, 隐藏为true,不隐藏为false")
    private Boolean unHideAirConditioningZoneControl;

    /**
     * 有无蓄冷设备,1表示有
     */
    private Object isCoolStore;

    /**
     * 冷冻泵形式 2表示二次泵系统
     */
    private Object chillPumpType;

    @JsonIgnore
    @ApiModelProperty("系统判断条件")
    private JSONObject systemJudgmentCondition;

    public TreeNodeVO(String name, String id) {
        this.name = name;
        this.localName = name;
        this.id = id;
        this.children = new ArrayList<>();
    }

}
