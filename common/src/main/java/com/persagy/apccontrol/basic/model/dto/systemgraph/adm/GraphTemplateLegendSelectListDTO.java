package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

import java.util.List;

/**
 * @author houyuanzhi
 * @date 2023/8/15 16:27
 */
@Data
public class GraphTemplateLegendSelectListDTO {

    private List<GraphTemplateLegendSelectDto> items;
}
