package com.persagy.apccontrol.basic.model.vo.systemgraph;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 图中设备已绑定的信息点全集
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/6/27 14:27
 * @since v5.240502.0
 */
@Data
public class ListInfoCodeVo {

    @ApiModelProperty("对象ID")
    private String id;

    @ApiModelProperty("对象名称")
    private String localName;

    @ApiModelProperty("信息点数据")
    private List<SystemGraphEditorInfoCodeVO> infoCodeList;

}
