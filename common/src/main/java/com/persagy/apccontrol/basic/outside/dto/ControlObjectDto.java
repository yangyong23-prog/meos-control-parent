package com.persagy.apccontrol.basic.outside.dto;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

@Data
public class ControlObjectDto {

    private String objectId;

    private String classCode;

    private String systemId;

    private JSONArray points;
}
