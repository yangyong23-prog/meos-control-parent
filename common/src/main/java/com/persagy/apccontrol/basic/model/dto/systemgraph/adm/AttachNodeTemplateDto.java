package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import cn.hutool.json.JSONObject;
import lombok.Data;

/**
 * 小附件图例（第一个横版，第二个竖版）
 *
 * @author houyuanzhi
 * @date 2023/9/21 16:53
 */
@Data
public class AttachNodeTemplateDto {
    private String id;

    private String compType;

    private CoordinateDto size;

    private String objClassCode;
//    private String legendId;

    private String legendOriId;

    private LegendNodeDto legendNode;

    private JSONObject textMap;


}
