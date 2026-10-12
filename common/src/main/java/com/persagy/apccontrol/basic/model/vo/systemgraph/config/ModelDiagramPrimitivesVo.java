package com.persagy.apccontrol.basic.model.vo.systemgraph.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 模型图图元
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
@ApiModel("模型图图元")
public class ModelDiagramPrimitivesVo {

    /**
     * 图名
     */
    @ApiModelProperty("图名")
    private String mapTitle;

    /**
     * 取值基础
     */
    @ApiModelProperty("取值基础")
    private long valueBasis;

    /**
     * 排除规则
     */
    @ApiModelProperty("排除规则")
    private String exclusiveRule;

    /**
     * 增加规则
     */
    @ApiModelProperty("增加规则")
    private String addRule;

}
