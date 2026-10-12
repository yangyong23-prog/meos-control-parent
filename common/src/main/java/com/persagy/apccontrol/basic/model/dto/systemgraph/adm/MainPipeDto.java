package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseUnwrapperResponse;
import lombok.Data;

import java.util.List;

/**
 * 主管道
 *
 * @author houyuanzhi
 * @date 2023/6/20 15:58
 */
@Data
public class MainPipeDto extends BaseUnwrapperResponse {


    private String id;
    private String name;
    private String compType;
    private String styleId;
    private String label;
    /**
     * 母线数据
     */
    private JSONObject dataObject;
    /**
     * 环管\立管的所有数据
     */
    private JSONArray dataObjects;

    private String dataObjectId;
    private String flag;
    private String remark;
    private String pipeType;
    private String passbyRels;
    private String lineType;
    private String connectEquips;
    private List<String> equipmentTypes;
    private String equipGroupLinkRels;
    private String bindEquipment;
    private String dataFilter;
    private String inSystem;
    private String diagramMode;
    private String trim;
    private List<List<CoordinateDto>> path;
    private String relatedContainers;
    private List<List<CoordinateDto>> locationPath;
    private String rawLocationPath;
    private String dynTarget;
    private String riserData;
    private String auto;
    private JSONObject text;


}
