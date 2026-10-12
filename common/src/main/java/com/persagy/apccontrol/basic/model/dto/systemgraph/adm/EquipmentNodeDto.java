package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseUnwrapperResponse;
import lombok.Data;

/**
 * 设备容器
 *
 * @author houyuanzhi
 * @date 2023/6/1 11:54
 */
@Data
public class EquipmentNodeDto extends BaseUnwrapperResponse {
    /**
     * 设备容器id
     */
    private String id;

    /**
     * 设备名称
     */
    private String name;

    /**
     * "packNode" 设备集合
     * "equipmentNode" 设备节点
     */
    private String compType;

    /**
     * 设备总体坐标，不需要绘制
     */
    private CoordinateDto absoluteLocation;

    /**
     *
     */
    private CoordinateDto location;
    /**
     * 基准点
     */
    private CoordinateDto centerLocation;

    /**
     * 设备总体大小（包含文本框等），不需要绘制
     */
    private CoordinateDto size;

    /**
     * 实例基本信息
     */
    private JSONObject dataObject;

    private String statCode;

    private String dataObjectId;

    private String objClassCode;
    /**
     * 上级容器id
     */
    private String parentContainerId;
    /**
     * 顺序码：设备对象是上级容器的第几个下级
     */
    private String sortValue;

    /**
     * 模板图例id
     */
//    private String legendId;

    private String legendOriId;

    private String legendSize;

    private LabelDto label;

    private JSONObject anchorLocations;

    /**
     * 锚点画布坐标
     */
    private JSONObject anchorAbsoluteLocations;

    private String multiAnchorScope;

    /**
     * 设备图例信息
     */
    private LegendNodeDto legendNode;

    /**
     * 文本框信息
     */
    private JSONObject textMap;
    /**
     * 锚点下的设备列表
     */
    private JSONObject anchor2EquipmentNodeList;

    /**
     * 面板节点
     */
    private PanelNodeDTO panelNode;

}
