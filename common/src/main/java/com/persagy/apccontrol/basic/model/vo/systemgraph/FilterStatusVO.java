package com.persagy.apccontrol.basic.model.vo.systemgraph;

import lombok.Data;

@Data
public class FilterStatusVO {

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 存在iot规则
     */
    private Boolean existsIotRule;
}
