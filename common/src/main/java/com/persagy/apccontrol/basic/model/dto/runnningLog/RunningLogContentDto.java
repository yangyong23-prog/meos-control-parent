package com.persagy.apccontrol.basic.model.dto.runnningLog;

import lombok.Data;

import java.util.List;

@Data
public class RunningLogContentDto {

    /**
     * 模板
     */
    private String template;
    /**
     * 替换值
     */
    private List<String> vars;
    /**
     * 操作内容
     */
    private String contentDesc;

}
