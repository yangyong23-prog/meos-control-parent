package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseUnwrapperResponse;
import lombok.Data;

import java.util.List;

/**
 * LineDto
 *
 * @author houyuanzhi
 * @date 2023/6/20 16:07
 */
@Data
public class LineDto extends BaseUnwrapperResponse {

    private String id;
    private String name;
    private String compType;
    private String styleId;
    private String label;
    /**
     * 线对应的关系数据
     */
    private LineDataObjectDto dataObject;
    //    private JSONObject dataObjectAll;
    private String dataObjectId;
    private String flag;
    private String direction;
    /**
     * 起点
     */
    private LinePointDto from;
    /**
     * 终点
     */
    private LinePointDto to;

    private String relType;

    private Boolean onMainPipe;
    /**
     * 点的坐标列表
     */
    private List<CoordinateDto> locationPath;

    private Integer flowDirection;

    private List<String> passByList;

    private String pipelineStyleCode = "line00";

    private String lineName;

}
