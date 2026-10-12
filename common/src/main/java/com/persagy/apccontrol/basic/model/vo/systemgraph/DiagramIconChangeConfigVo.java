package com.persagy.apccontrol.basic.model.vo.systemgraph;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.HashMap;

/**
 * 关系图图标更换配置
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
@ApiModel("关系图图标更换配置")
public class DiagramIconChangeConfigVo {
    /**
     * 图标编号
     */
    @ApiModelProperty("对象类编码")
    private String legendCode;
    /**
     * 图标名称
     */
    @ApiModelProperty("对象类编码")
    private String legendName;
    /**
     * 信息点
     */
    @ApiModelProperty("对象类编码")
    private HashMap<String, String> infos;
    /**
     * 对象类
     */
    @ApiModelProperty("对象类编码")
    private String classCode;
    /**
     * 对象类编码
     */
    @ApiModelProperty("对象类编码")
    private String className;
}
