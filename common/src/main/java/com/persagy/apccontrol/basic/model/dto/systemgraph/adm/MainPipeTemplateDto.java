package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.MainPipeType;
import lombok.Data;

import java.util.List;

/**
 * 模板 母线/辅助线/立管线列表
 *
 * @author houyuanzhi
 * @date 2023/9/21 15:35
 */
@Data
public class MainPipeTemplateDto {

    private String id;

    private String oriId;

    private String name;

    private String pipeType;

    private JSONObject dataFilters;

    private String lineType;

    /**
     * busbar母线；guideline辅助线；riser立管线
     */
    private MainPipeType mainPipeType;

    private String compType;

    private List<String> equipmentTypes;

    private boolean bindEquipment;
}
