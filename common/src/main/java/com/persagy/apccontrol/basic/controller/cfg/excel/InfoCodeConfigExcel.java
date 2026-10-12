package com.persagy.apccontrol.basic.controller.cfg.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 信息点配置
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/28 14:18
 */
@Data
public class InfoCodeConfigExcel {

    /**
     * 一级标签
     */
    @ExcelProperty("Primary Tag")
    private String firstTag;

    /**
     * 二级标签
     */
    @ExcelProperty("Secondary Tag")
    private String secondTag;

    /**
     * 信息点编码
     */
    @ExcelProperty("Information Code")
    private String infoCode;

    /**
     * 信息点名称
     */
    @ExcelProperty("Information Name")
    private String name;

    /**
     * 信息点别名
     */
    @ExcelProperty("Custom Name")
    private String aliasName;

    /**
     * 缩略名称，后端不处理，前端自行处理
     */
    @ExcelProperty("Abbreviation")
    private String abbreviationName;

    /**
     * 是否在前端显示,N-不显示
     */
    @ExcelProperty("List Display")
    private String display;

    /**
     * 信息点合并编码
     */
    @ExcelProperty("Aggregation Code")
    private String mergeCode;

    /**
     * 信息点合并名称
     */
    @ExcelProperty("Aggregation Name")
    private String mergeName;

    @ExcelProperty("Primary Grouping")
    private String firstGroup;

    @ExcelProperty("Primary Grouping Ordering")
    private String firstOrder;

    @ExcelProperty("Secondary Grouping")
    private String secondGroup;

    @ExcelProperty("Secondary Grouping Ordering")
    private String secondOrder;

    @ExcelProperty("Information Ordering")
    private String infoOrder;

    @ExcelProperty("Custom Unit")
    private String unit;

    @ExcelProperty("Conversion Factor")
    private String scale;

    @ExcelProperty("importance")
    private String importanceLevel;

    @ExcelIgnore
    private String classCode;

    @ExcelIgnore
    private String className;

    public String getFirstTag() {
        return firstTag == null ? null : firstTag.replace(" ", " ").trim();
    }

    public String getSecondTag() {
        return secondTag == null ? null : "--".equals(secondTag) ? null : secondTag.replace(" ", " ").trim();
    }

    public String getInfoCode() {
        return infoCode == null ? null : infoCode.trim();
    }

    public String getName() {
        return name == null ? null : name.trim();
    }

    public String getAliasName() {
        return aliasName == null ? null : aliasName.trim();
    }

    public String getMergeCode() {
        return mergeCode == null ? null : mergeCode.trim();
    }

    public String getMergeName() {
        return mergeName == null ? null : mergeName.trim();
    }

    public String getFirstGroup() {
        return firstGroup == null ? null : firstGroup.replace(" ", " ").trim();
    }

    public String getSecondGroup() {
        return secondGroup == null ? null : "--".equals(secondGroup) ? null : secondGroup.replace(" ", " ").trim();
    }

    public String getUnit() {
        return unit == null ? null : unit.trim();
    }

    public String getScale() {
        return scale == null ? null : scale.trim();
    }

    public String getFirstOrder() {
        return firstOrder == null ? null : firstOrder.trim();
    }

    public String getSecondOrder() {
        return secondOrder == null ? null : secondOrder.trim();
    }

    public String getInfoOrder() {
        return infoOrder == null ? null : infoOrder.trim();
    }

    public String getAbbreviationName() {
        return abbreviationName == null ? null : abbreviationName.trim();
    }

    public String getDisplay() {
        return display == null ? null : display.trim();
    }

}
