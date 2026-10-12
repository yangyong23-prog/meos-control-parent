package com.persagy.apccontrol.basic.model.vo.systemgraph.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * 关系图信息
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
@ApiModel("关系图信息")
public class GraphInformationVo {

    /**
     * 对象类编码
     */
    @ApiModelProperty("对象类编码")
    private String classCode;
    /**
     * 对象类
     */
    @ApiModelProperty("对象类名称")
    private String className;
    /**
     * 信息点
     */
    @ApiModelProperty("信息点名称")
    private String infoName;
    /**
     * 信息点编码
     */
    @ApiModelProperty("信息点编码")
    private String infoCode;
    /**
     * 信息点类型
     */
    @ApiModelProperty("信息点类型")
    private String infoType;
    /**
     * 展示方式
     */
    @ApiModelProperty("展示方式：1.直接展示；2.图标替换；3.在信息板中展示的信息点（强电图适用）")
    private long displayMode;
    /**
     * 应用图纸
     */
    @ApiModelProperty("应用图纸列表")
    private List<String> applicationDrawing;

    /**
     * hover是否展示
     */
    @ApiModelProperty("hover是否展示")
    private String hover;

}
