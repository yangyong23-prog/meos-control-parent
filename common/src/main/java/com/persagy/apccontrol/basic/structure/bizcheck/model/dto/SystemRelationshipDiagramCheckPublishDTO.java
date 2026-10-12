package com.persagy.apccontrol.basic.structure.bizcheck.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统关系图检查项结果 DTO
 * 包含系统关系图的各项基本信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemRelationshipDiagramCheckPublishDTO {

    /**
     * 系统图 ID
     */
    private String id;
    
    /**
     * 系统图本地名称
     */
    private String localName;
    
    /**
     * 系统图类编码
     */
    private String classCode;
    
    /**
     * 图发布状态
     */
    private boolean published;
}
