package com.persagy.apccontrol.common.outside.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * paas平台demo用户信息表(paas_user)实体类
 *
 * @author luogy
 * @description 由 Mybatisplus Code Generator 创建
 * @since 2022-03-02 14:14:21
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
public class GroupDTO {

    /**
     * 集团id
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

    public GroupDTO(String code, String name, Integer status, String remark) {
        this.code = code;
        this.name = name;
        this.status = status;
        this.remark = remark;
    }
}