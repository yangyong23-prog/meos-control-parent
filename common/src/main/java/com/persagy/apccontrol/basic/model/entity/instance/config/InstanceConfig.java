package com.persagy.apccontrol.basic.model.entity.instance.config;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.instance.InstanceConfigTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 实例配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "instance_config", autoResultMap = true)
public class InstanceConfig extends BaseAssignIdDO {
    /**
     * 运行监测枚举
     */
    @TableField(value = "running_monitor_type")
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型枚举
     */
    @TableField(value = "running_monitor_second_type")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 实例类型
     */
    @TableField(value = "object_type")
    private DtpRwdObjectTypeEnum objectType;

    /**
     * 配置类型: WHITE_LIST白名单, BLACK_LIST黑名单
     */
    @TableField(value = "config_type")
    private InstanceConfigTypeEnum configType;

    /**
     * 实例id列表
     */
    @TableField(value = "config_data", typeHandler = JacksonTypeHandler.class)
    private JsonNode configData;
    
    /**
     * 关联信息
     */
    @TableField(value = "rel_msg")
    private String relMsg;

    /**
     * 项目id
     */
    @TableField(value = "project_id")
    private String projectId;
}