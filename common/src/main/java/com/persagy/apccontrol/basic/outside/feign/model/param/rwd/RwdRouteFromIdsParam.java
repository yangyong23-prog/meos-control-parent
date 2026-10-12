package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;


import lombok.Data;

import java.util.Collection;

/**
 *
 * @author houyuanzhi
 * @date 2024/3/13 18:04
 */
@Data
public class RwdRouteFromIdsParam {

    /**
     * 起点设备实例id
     */
    private Collection<String> objFroms;

    /**
     * 终点设备类型(非必填)
     */
    private String toClassCode;

    /**
     * 图类型编码
     */
    private String graphCode;

    /**
     * 边类型编码
     */
    private String relCode;

    /**
     * 输出仅包含设备id,0：输出所有信息点 1：仅输出设备id  默认值： 0
     */
    private Integer ifOnlyId;

    /**
     * 最大级数.若参数值为1，表示只找到与起点直接连接的终点，没有符合条件的，则不返回。此参数若不输入，则按原逻辑，查询到第一个符合条件的终点返回。
     */
    private Integer maxLevel;
}
