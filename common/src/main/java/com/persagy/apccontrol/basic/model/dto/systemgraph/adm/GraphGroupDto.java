package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 设备容器
 *
 * @author houyuanzhi
 * @date 2023/6/1 11:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GraphGroupDto extends EquipmentNodeDto {

    /**
     * 对象类编码
     */
    private List<String> classCodes;

    private String groupCode;

    private String instanceId;

    private String typeCode;
//    private String templateId;

    private String templateOriId;

    private String templateName;

    private String styleCode;

    /**
     * 文本框信息
     */
    private JSONObject textMap;

    private JSONObject anchorLocations;

    /**
     * 锚点画布坐标
     */
    private JSONObject anchorAbsoluteLocations;

    /**
     * 实例基本信息
     */
    private JSONObject dataObject;


}
