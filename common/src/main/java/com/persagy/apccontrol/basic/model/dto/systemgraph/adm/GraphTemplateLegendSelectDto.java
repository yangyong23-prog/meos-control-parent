package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/15 16:27
 */
@Data
public class GraphTemplateLegendSelectDto {

//    private String templateId;

    private String templateOriId;

    private String graphTypeCode;

    private String referenceObjectTypeCode;

    private String referenceObjectId;

    private Integer sequenceNumberCurrent;
}
