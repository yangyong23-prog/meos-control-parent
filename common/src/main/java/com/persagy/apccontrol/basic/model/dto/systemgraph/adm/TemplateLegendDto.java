package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * 容器（包含设备图例、机房；有图例id的就是设备图例、其他的是机房）列表
 *
 * @author houyuanzhi
 * @date 2023/9/21 15:25
 */
@Data
public class TemplateLegendDto {

    /**
     * 容器id
     */
    private String containerId;
    /**
     * 容器名
     */
    private String containerName;
    /**
     * 图例id
     */
//    private String legendId;

    private String legendOriId;
    /**
     * 设备类型编码
     */
    private String classCode;
    /**
     * 有效文本框编码
     */
    private List<String> textList;
    /**
     * 锚点详情
     */
    private JSONObject anchors;
    /**
     * 有效锚点编码
     */
    private List<String> validAnchors;
    /**
     * 设备图例是否允许隐藏
     */
    private String hiddenEnable;
    /**
     * 设备信息节点
     */
    private EquipmentNodeDto equipmentNode;
}
