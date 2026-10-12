package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @author houyuanzhi
 * @date 2023/9/21 15:24
 */
@Data
public class TemplateContentDto {

    /**
     * 容器（包含设备图例、机房；有图例id的就是设备图例、其他的是机房）列表
     */
    private List<TemplateLegendDto> templateLegendList;

    /**
     * 母线/辅助线/立管线列表
     */
    private List<MainPipeTemplateDto> mainPipes;

    /**
     * 关系线列表
     */
    private List<TemplateLineDto> templateLineList;

    /**
     * 小附件列表
     */
    private List<AttachTemplateDto> attachs;

    private JSONObject attach2LineList;

    private JSONObject line2AttachList;
}
