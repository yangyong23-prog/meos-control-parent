package com.persagy.apccontrol.basic.model.dto.systemgraph;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.CoordinateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestSystemGraphDTO {

    /**
     * 基准对象id
     */
    @NotBlank(message = "{key_212e5860fe0e4185a6d6e3bd650fd868}")
    @Schema(description = "基准对象id")
    private String referenceObjectId;

    /**
     * 模板id
     */
//    @NotBlank(message = "模板id不可为空")
//    @Schema(description = "模板id")
//    private String templateOriId;

    private CoordinateDto size;

    /**
     * 放大倍数 240901 已弃用
     */
    @Deprecated
    @Schema(description = "放大倍数")
    private int legendScaleRatio = 1;

    /**
     * 是否禁用所有文本框
     */
    @Schema(description = "是否禁用所有文本框")
    private boolean textHidden = false;

    /**
     * 是否展示小附件
     */
    @Schema(description = "是否展示小附件")
    private boolean attachEnable = true;

    /**
     * 是否展示全部小附件
     */
    @Schema(description = "是否展示全部小附件")
    private boolean attachShowAll = false;

    @Schema(description = "是否开启缓存")
    private boolean enableCache = true;

    @Schema(description = "是否读取缓存，大刷为false，小刷为true")
    private boolean cacheRead = false;

    @Schema(description = "生成id")
    private String generateId;

    /**
     * 关系图类型
     */
    @NotBlank(message = "{key_81f3bb2d2b4a4d60b6027799c9007485}")
    @Schema(description = "关系图类型")
    private String graphTypeCode;

    @Schema(description = "关系图名称")
    private String graphName;

    /**-----v241101系统图图标从关系图引擎获取，不再需要以下4个参数------***/
    /**
     * 图例隐藏、锚点偏移量、文本隐藏
     */
//    @Schema(description = "图例隐藏、锚点偏移量、文本隐藏")
//    private JSONObject templateLegendDescMap;

    /**
     * 小附件隐藏、偏移量、文本隐藏
     */
//    @Schema(description = "小附件隐藏、偏移量、文本隐藏")
//    private JSONObject templateAttachDescMap;

    /**
     * 母线文本隐藏
     */
//    @Schema(description = "母线文本隐藏")
//    private JSONObject templateMainPipeDescMap;

    /**
     * 图例隐藏、锚点偏移量、文本隐藏
     */
//    @Schema(description = "图例隐藏、锚点偏移量、文本隐藏")
//    private JSONObject legendDescMap;

    @Schema(description = "父对象id对应子对象id排序值")
    private JSONObject parent2ObjectId2SortValue;

    @Schema(description = "字体大小，单位px")
    private Integer fontSize = 24;

    @Schema(description = "设备/设备组（机房）的文本框尺寸和留白")
    private JSONObject equipmentGroupDetails;

}
