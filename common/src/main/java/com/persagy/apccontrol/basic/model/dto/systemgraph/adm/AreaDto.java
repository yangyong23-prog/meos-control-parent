package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author houyuanzhi
 * @date 2024/1/8 14:46
 */
@Data
public class AreaDto {

    private String type;

    private JSONObject dataObject;

    private CoordinateDto location;

    private CoordinateDto absoluteLocation;

    private CoordinateDto size;

    private JSONObject text;

    private JSONObject infos;
}
