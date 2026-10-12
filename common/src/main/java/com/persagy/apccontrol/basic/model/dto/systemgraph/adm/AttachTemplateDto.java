package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

import java.util.List;

/**
 * @author houyuanzhi
 * @date 2023/9/21 16:45
 */
@Data
public class AttachTemplateDto {
//    private String id;

    private String oriId;

    private String name;

    private List<String> classCodes;

    /**
     * 小附件图例（第一个横版，第二个竖版）
     */
    private List<AttachNodeTemplateDto> attachNodes;


}
