package com.persagy.apccontrol.basic.model.dto.systemgraph;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

/**
 * 管道连接件
 *
 * @author chenlong
 * @date 2022/5/23 3:13 下午
 */
@Data
public class PipeConnector {
    private String id;
    private String classCode;
    private String localName;
    private String bimLocation;
    private JSONArray outline;
    private JSONArray skeletonLine;

}
