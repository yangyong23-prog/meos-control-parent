package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 * 模板详情
 *
 * @author houyuanzhi
 * @date 2023/9/21 15:05
 */
@Data
public class GraphTemplateDto {
//    private String id;

    private String oriId;

    private String majorCode;

    private String referenceObjectTypeCode;
    private String graphTypeCode;
    private String graphSubTypeCode;
    private String version;
    private String sequenceNumberCurrent;
    private String sequenceNumberTotal;
    private String templateCode;
    private String templateName;
    private String templateStatus;
    private TemplateContentDto templateContent;

}
