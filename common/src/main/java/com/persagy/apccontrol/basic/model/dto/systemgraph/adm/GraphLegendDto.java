package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 * 图例
 *
 * @author houyuanzhi
 * @date 2023/6/21 11:40
 */
@Data
public class GraphLegendDto {

//    private String id;

    private String oriId;

    private String classCode;

    private String objType;

    private String legendCode;

    private String legendName;

    private Long creationTime;

    private Long modifiedTime;

    private boolean valid;

    private Integer legendVersion;

    private String remark;

    private GraphLegendContentDto legendContent;
}
