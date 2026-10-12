package com.persagy.apccontrol.basic.model.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 设备类业务定义
 *
 * @TableName class_define_config
 */
@Data
@Accessors(chain = true)
@TableName(value = "class_define_config", autoResultMap = true)
public class ClassDefineConfig {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 本方自定义的classCode，无需自定义时，与中台保持一致
     */
    @TableId
    private String classCode;

    private String groupCode;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 对应中台的classCode
     */
    private String refClassCode;

    /**
     * 设备类名称
     */
    private String className;

    /**
     * 显示顺序
     */
    private Integer ordered;

    /**
     * 扩展信息
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject extendInfo;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}