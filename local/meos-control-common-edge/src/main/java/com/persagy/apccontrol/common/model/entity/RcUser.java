package com.persagy.apccontrol.common.model.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 本地用户信息表
 *
 * @TableName rc_user
 */
@TableName(value = "rc_user", autoResultMap = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RcUser implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId
    private String userId;
    /**
     * 项目编号
     */
    private String projectId;

    /**
     * 用户名称
     */
    private String userName;
    /**
     * 状态
     */
    private String state;
    /**
     * 密码（MD5密码）
     */
    private String password;
    /**
     * 账号是否锁定，0-未锁定，1-锁定
     */
    private String lockState;

    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject functions;

    /**
     * 生效时间
     */
    private Date validTime;
    /**
     * 同步时间
     */
    private Date sysTime;

}