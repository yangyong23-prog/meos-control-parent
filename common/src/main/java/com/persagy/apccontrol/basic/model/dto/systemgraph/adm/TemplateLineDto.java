package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

import java.util.List;

/**
 * 关系线列表
 *
 * @author houyuanzhi
 * @date 2023/9/21 15:57
 */
@Data
public class TemplateLineDto {
//    private String id;

    private String oriId;

    private String graphCodeVirtual;

    private String relCodeVirtual;

    private TemplateConnectorDto templateConnector1;

    private TemplateConnectorDto templateConnector2;

    private List<RelChainItemDto> relChainItemList;

    private boolean disable;

    private boolean sameRelType;


}
