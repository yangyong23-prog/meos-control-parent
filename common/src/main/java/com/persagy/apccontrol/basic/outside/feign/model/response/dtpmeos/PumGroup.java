package com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos;

import lombok.Data;

import java.util.List;

/**
 * @Author weiyizhong
 * @Date 2026/3/13 10:25
 * @Company yushu.biz
 * @Description: 泵组
 */
@Data
public class PumGroup {

    /**
     * 空调分区id
     */
    private String airTerminalZoneId;

    /**
     * 空调分区名称
     */
    private String airTerminalZoneName;

    /**
     * 分类
     */
    private String classCode;

    /**
     * 泵组名称
     */
    private String pumpGroupName;

    /**
     * 泵组id
     */
    private String pumpGroupId;

    /**
     * 泵组信息点
     */
    private List<InfoCode> pumpGroupInfoCodes;


    @Data
    public static class InfoCode{

        /**
         * 信息点点(水泵开启台数时用比较特殊)
         */
        private String infoCode;

        /**
         * 设定值的信息点
         */
        private String setInfoCode;

        /**
         * 信息点名称
         */
        private String infoCodeName;

        /**
         * 单位
         */
        private String unit;


    }
}
