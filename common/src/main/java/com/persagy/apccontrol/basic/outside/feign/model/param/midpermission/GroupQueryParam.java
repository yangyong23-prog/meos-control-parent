package com.persagy.apccontrol.basic.outside.feign.model.param.midpermission;

import lombok.Data;

/**
 * 集团查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
public class GroupQueryParam {

    /**
     * 集团编码
     */
    private String code;

    /**
     * 集团查询关键字
     */
    private String keyword;

    /**
     * 分页查询页号
     */
    private Integer page;

    /**
     * 分页查询每页数量
     */
    private Integer pageSize;

    /**
     * 集团状态
     *
     * <ul>
     *     <li><b>1</b> - 启用</li>
     *     <li><b>0</b> - 已停用</li>
     * </ul>
     */
    private Integer status;

}
