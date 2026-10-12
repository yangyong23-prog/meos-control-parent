package com.persagy.apccontrol.basic.model.entity.intervention;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.InterventionTaskConfigDataTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.InterventionTaskConfigSourceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.intervention.InterventionInstanceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 干预任务配置表
 * @TableName intervention_task_config
 */
@TableName(value ="intervention_task_config", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class InterventionTaskConfig  extends BaseAssignIdDO implements Serializable {
    
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 项目 ID
     */
    private String projectId;

    /**
     * 任务ID(雪花 id)
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 一级监测类型
     */
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型
     */
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 数据类型(0主配置/1关联配置)
     */
    private InterventionTaskConfigDataTypeEnum dataType;
    /**
     * 数据来源类型
     */
    private InterventionTaskConfigSourceTypeEnum sourceType;

    /**
     * 实例类型
     */
    private InterventionInstanceTypeEnum instanceType;

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 开始生效时间
     */
    private LocalDateTime beginTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime endTime;

    /**
     * 配置信息(回路列表的开关状态)
     */
    @TableField(value = "config_data", typeHandler = JacksonTypeHandler.class)
    private JsonNode configData;

    /**
     * 原因
     */
    private String reason;
    
    
}