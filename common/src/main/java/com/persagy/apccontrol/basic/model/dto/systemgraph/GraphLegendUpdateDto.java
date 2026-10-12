package com.persagy.apccontrol.basic.model.dto.systemgraph;

import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphIconVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.LegendAnchorVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/11 16:08
 */
@Data
public class GraphLegendUpdateDto {

    /**
     * 文件url 初始化时是否需要默认值
     */
    @ApiModelProperty("文件url,")
    private String fileUrl;
    /**
     * 锚点
     */
    @ApiModelProperty("锚点")
    private List<LegendAnchorVo> anchors;

    @ApiModelProperty("图标列表")
    private List<GraphIconVo> iconVoList;



}
