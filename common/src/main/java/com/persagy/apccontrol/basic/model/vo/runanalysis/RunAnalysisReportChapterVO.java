package com.persagy.apccontrol.basic.model.vo.runanalysis;

import lombok.Data;

import java.util.List;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-01-19 17:37
 **/
@Data
public class RunAnalysisReportChapterVO {

    /**
     * 章节id
     */
    private String id;

    /**
     * 章节名称
     */
    String name;
    /**
     * 章节顺序
     */
    int sort;
    /**
     * 章节code
     */
    String code;

    /**
     * 对象id
     */
    String objectId;

    /**
     * 父章节code
     */
    String parentCode;

    /**
     * 章节子项
     */
    List<RunAnalysisReportChapterVO> children;
}