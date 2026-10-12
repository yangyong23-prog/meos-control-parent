package com.persagy.apccontrol.basic.model.vo.systemgraph;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 模板树
 *
 * @author MAF
 * @date 2024/06/25
 */
@Data
public class TemplateTreeVO {

    /**
     * 编码
     */
    @ApiModelProperty("编码")
    private String code;
    /**
     * 名称
     */
    @ApiModelProperty("名称")
    private String name;
    /**
     * 父id
     */
    @ApiModelProperty("父id")
    private String parentId;
    /**
     * 模板id
     */
//    @ApiModelProperty("模板id")
//    private String templateId;
    /**
     * 系统图类型
     */
    @ApiModelProperty("系统图类型")
    private String systemGraphCode;
    /**
     * 更新状态
     */
    @ApiModelProperty("更新标识，0未更新，1更新")
    private Boolean updateFlag;

    @ApiModelProperty("图名")
    private String  graphName;

    /**
     * 小孩
     */
    private List<TemplateTreeVO> children;
}
