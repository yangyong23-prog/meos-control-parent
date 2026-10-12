package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

import java.util.List;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/10 17:41
 */
@Data
public class GraphTypeTreeDto {

    private String classCode;

    private String name;

    private String objType;

    private List<GraphTypeTreeDto> children;
}
