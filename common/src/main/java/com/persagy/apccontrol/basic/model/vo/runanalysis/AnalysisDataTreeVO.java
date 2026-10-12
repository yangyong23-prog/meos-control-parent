package com.persagy.apccontrol.basic.model.vo.runanalysis;

import lombok.Data;

import java.util.List;

@Data
public class AnalysisDataTreeVO {

    /**
     * 名称
     */
    private String name;

    /**
     * 信息点
     */
    private String infoCode;
    /**
     * 分组名称
     */
    private String groupName;
    /**
     * 单位
     */
    private String unit;
    /**
     * 如果不为空，则需要统计，值为需要统计的信息点值
     */
    private String statisticalValue;

    /**
     * 对象列表
     */
    private List<ObjectInfo> objects;

    /** 新分类信息代码 */
    private List<String> newClassifyInfoCodes;

    /**
     * 是否选择
     */
    private boolean isSelect;

    private List<AnalysisDataTreeVO> children;

    @Data
    public static class ObjectInfo {

        private String objectId;

        private String localName;

        private String classCode;
    }
}
