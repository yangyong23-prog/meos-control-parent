package com.persagy.apccontrol.controller.vo;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.List;

/**
 * 末端设备vo
 *
 * @author MAF
 * @date 2023/12/13
 */
@Data
public class TerminalEquipmentVO {

    /**
     * 设备类编码
     */
    private String classCode;
    /**
     * 设备类名称
     */
    private String className;
    /**
     * 安装类型
     */
    private String installType;
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 总数量
     */
    private Integer totalNum;
    /**
     * 处于运行中得数量
     */
    private Integer runNum;
    /**
     * AI数量
     */
    private Integer aiNum;
    /**
     * 手动数量
     */
    private Integer manualNum;
    /**
     * 干预数量
     */
    private Integer interveneNum;
    /**
     * ai设备id列表
     */
    private List<String> aiIdList;
    /**
     * 手动设备id列表
     */
    private List<String> manualIdList;
    /**
     * 干预设备id列表
     */
    private List<String> interveneIdList;
    /**
     * 排序
     */
    private Integer ordered;

    private List<InfoCodes> infoCodes;

    /**
     * 信息点信息
     *
     * @author MAF
     * @date 2023/12/13
     */
    @Data
    public static class InfoCodes {
        /**
         * 信息码
         */
        private String infoCode;
        /**
         * 名称
         */
        private String name;
        /**
         * 排序
         */
        private int ordered;
        /**
         * 数据值
         */
        private String dataValue;
        /**
         * 数据值名称
         */
        private String dataValueName;
        /**
         * 单位
         */
        private String unit;
        /**
         * 数据源
         */
        private JSONArray dataSource;

        /** -1 或 null 手自动下均展示, 0-手动下展示，1-自动下展示 */
        private String force;
    }
}
