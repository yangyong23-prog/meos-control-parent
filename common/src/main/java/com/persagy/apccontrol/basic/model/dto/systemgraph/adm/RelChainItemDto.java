package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author houyuanzhi
 * @date 2023/9/21 16:27
 */
@Data
public class RelChainItemDto {

    private Integer type;

    private String relObjType;

    private String relObjId;

    private String classCode;

    private List<String> relTypes;

    private boolean isDirect;
}
