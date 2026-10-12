package com.persagy.apccontrol.basic.outside.feign.model.response.midpermission;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 集团数据响应
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GroupResponse extends BaseResponse {

    /**
     * 集团ID
     */
    private String id;

    /**
     * 集团编码
     */
    @JsonAlias("code")
    private String groupCode;

    /**
     * 集团名称
     */
    @JsonAlias("name")
    private String groupName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 集团状态
     *
     * <ul>
     *     <li><b>1</b> - 启用</li>
     *     <li><b>0</b> - 已停用</li>
     * </ul>
     */
    private Integer status;

    /**
     * 方案ID
     */
    private String schemaId;

    /**
     * 方案名称
     */
    private String schemaName;

    /**
     * 方案备注
     */
    private String schemaRemark;

    /**
     * 方案状态
     */
    private Integer schemaStatus;

}
