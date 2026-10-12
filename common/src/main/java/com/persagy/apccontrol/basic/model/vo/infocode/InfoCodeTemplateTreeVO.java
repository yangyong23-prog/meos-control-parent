package com.persagy.apccontrol.basic.model.vo.infocode;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description: 模版树节点
 * @Author: wxf
 * @Date: 2025/9/2
 */
@Data
public class InfoCodeTemplateTreeVO {

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点id
     */
    private String nodeId;

    /**
     * 模版类型ID
     */
    private Long templateTypeId;

    /**
     * 模版类型名称
     */
    private String templateTypeName;

    /**
     * 总对象数量
     */
    private Integer totalObjectCount = 0;

    /**
     * 序列
     */
    private Integer sequence;

    /**
     * 子节点
     */
    List<InfoCodeTemplateTreeVO> children;

    /**
     * 空间ID列表
     */
    Map<String,Set<String>> spaceIds;

    /**
     * 设备ID列表
     */
    Set<String> equipmentIds;
}
