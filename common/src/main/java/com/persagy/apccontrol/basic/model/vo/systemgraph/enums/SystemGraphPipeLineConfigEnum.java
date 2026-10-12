package com.persagy.apccontrol.basic.model.vo.systemgraph.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SystemGraphPipeLineConfigEnum implements BaseEnum<Integer> {
    PIPELINE_STYLE_CONFIG(0, "PIPELINE_STYLE_CONFIG", 2, PipelineStyleVo.class),
    PIPELINE_RELATION_COLOR_CONFIG(1, "PIPELINE_RELATION_COLOR_CONFIG", 1, PipelineRelationConfigVo.class),
    MULTIPLE_PIPE_TEXT(2, "MULTIPLE_PIPE_TEXT", 1, MultiplePipeTextVo.class),
    AREA_STYLE_CONFIG(3, "AREA_STYLE_CONFIG", 2, AreaStyleConfigVo.class);

    /**
     * sheet页数
     */
    Integer sheetNum;
    /**
     * 数据库tag
     */
    String tag;
    /**
     * 头部行数
     */
    Integer headerNum;
    /**
     * 配置类
     */
    Class<? extends PipelineStyleConfig> clazz;


    @Override
    public Integer value() {
        return sheetNum;
    }

    @Override
    public String getReasonPhrase() {
        return tag;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SystemGraphPipeLineConfigEnum analyze(Integer sheetNum) {
        return BaseEnum.analyze(sheetNum, SystemGraphPipeLineConfigEnum.class);
    }
}
