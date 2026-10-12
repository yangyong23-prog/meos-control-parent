package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphTypeTreeDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/14 18:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GraphTypeTreeVo extends GraphTypeTreeDto {

    @ApiModelProperty("更新标识")
    private boolean updateMark = false;


    private List<GraphTypeTreeVo> childrenVo = new LinkedList<>();
}
