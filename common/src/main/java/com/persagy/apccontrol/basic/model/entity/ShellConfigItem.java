package com.persagy.apccontrol.basic.model.entity;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/7 5:29 PM
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShellConfigItem extends ShellConfigBasic {

    private String scene;
    private String group;
    private String tab;
    private String name;
    private String unit;
    private String defaultValues;
    private Integer step;
    private Integer min;
    private Integer max;
    private String hint;
    private String desc;
    private boolean editAuth = true;
    private String valueType;
    private String valSource;
    private List<JSONObject> mulInstance;
    //分组对象名
    private String tabObjectName;

    /**
     * 获取壳配置值
     *
     * @return
     */
    public String getShellConfigValue() {
        String value = getValue();
        return StringUtils.isBlank(value) ? defaultValues : value;
    }

}
