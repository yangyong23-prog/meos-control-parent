package com.persagy.apccontrol.common.logtrace.entity;

import lombok.Data;

import java.util.List;

@Data
public class LogQueryTemplateParamEntity {

    private String nodecode;
    private String key ;
    private String name;
    private int type;
    private String defaultValue;
    //返回值对应的key列表
    private List<String> returnValue;


}
