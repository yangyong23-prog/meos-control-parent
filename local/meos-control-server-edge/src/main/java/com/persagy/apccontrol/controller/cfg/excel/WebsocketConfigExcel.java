package com.persagy.apccontrol.controller.cfg.excel;

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
public class WebsocketConfigExcel {

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
    private String infoName;

    @ExcelIgnore
    private String classCode;

    @ExcelIgnore
    private String className;

    public String getFirstTag() {
        return firstTag == null ? null : firstTag.trim();
    }

    public String getSecondTag() {
        return secondTag == null ? null : "--".equals(secondTag) ? null : secondTag.trim();
    }

    public String getInfoCode() {
        return infoCode == null ? null : infoCode.trim();
    }

    public String getInfoName() {
        return infoName == null ? null : infoName.trim();
    }

}
