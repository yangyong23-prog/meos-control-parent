package com.persagy.apccontrol.basic.structure.runanalysis.model.dto;

import lombok.Data;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-01-22 16:23
 **/
@Data
public class RunAnalysisReportChapterDto {
    /**
     * 章节id
     */
    long id;
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
     * objectId
     */
    String objectId;
    /**
     * 父章节code
     */
    String parentCode;

}