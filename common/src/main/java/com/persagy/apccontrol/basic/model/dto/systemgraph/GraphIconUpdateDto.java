package com.persagy.apccontrol.basic.model.dto.systemgraph;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/11 16:08
 */
@Data
public class GraphIconUpdateDto {

    @ApiModelProperty("主键id")
    private String id;

    @ApiModelProperty("文件url")
    private String fileUrl;

    @ApiModelProperty("文件url")
    private String picUrl;

    @ApiModelProperty("设备类编码")
    private String classCode;

    @ApiModelProperty("图标名称")
    private String legendName;

    @ApiModelProperty("图例id")
    private String legendId;

}
