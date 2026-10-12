package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/22 18:02
 */
@Data
public class GraphTemplateFilterDto {

    /**
     * 专业
     */
    private String majorCode;

    /**
     * 关系图类(中文)
     */
    private String graphTypeCode;

    /**
     * 基准对象id
     */
    private String referenceObjectId;

}
