package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

import java.util.List;

/**
 * 小附件类型数节点
 *
 * @author houyuanzhi
 * @date 2023/8/23 16:40
 */
@Data
public class AttachTypeNodeDto {

    private String classCode;

    private String name;

    private List<AttachTypeNodeDto> children;
}
