package com.persagy.apccontrol.basic.structure.bizcheck.model.dto;

import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统关系图检查项结果 DTO
 * 包含系统关系图的各项基本信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemRelationshipDiagramCheckItemDTO {

    private List<SystemRelationshipDiagramCheckPublishDTO> publishList;
    private BizCheckStatusEnum status;
}
