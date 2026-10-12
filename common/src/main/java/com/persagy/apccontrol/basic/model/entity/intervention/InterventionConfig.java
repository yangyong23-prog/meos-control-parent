package com.persagy.apccontrol.basic.model.entity.intervention;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.common.model.valueobject.DateTimeRangeValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.validate.utility.enumeration.DateTimeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 人工干预配置表
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "intervention_config", autoResultMap = true)
public class InterventionConfig extends BaseAssignIdDO implements Serializable {

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 运行监测二级类型
     */
    @TableField(value = "running_monitor_second_type")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 实例类型
     */
    @TableField(value = "instance_type")
    private DemandInstanceTypeEnum instanceType;

    /**
     * 实例ID
     */
    @TableField(value = "instance_id")
    private String instanceId;

    /**
     * 开始时间
     */
    @TableField(value = "begin_time")
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 干预数据
     */
    @TableField(value = "config_data", typeHandler = JacksonTypeHandler.class)
    private JsonNode configData;

    /**
     * 干预原因
     */
    @TableField(value = "reason")
    private String reason;

    /**
     * 继承列表
     */
    @TableField(exist = false)
    private List<InterventionConfig> inherit;

    /**
     * 转换时间范围
     *
     * @return 时间范围
     * @since 5.240702.0
     */
    public DateTimeRangeValueObject toDateTimeRange() {
        return new DateTimeRangeValueObject(beginTime, endTime);
    }

    /**
     * 转换干预状态枚举
     *
     * @return 干预状态枚举
     * @since 5.240801.0
     */
    public InterventionStatusEnum toInterventionStatusEnum() {
        LocalDateTime nowByMinute = DateTimeTypeEnum.MINUTE.truncate(DateUtils.now());
        if (endTime.isBefore(nowByMinute)) {
            return InterventionStatusEnum.COMPLETED;
        } else {
            return InterventionStatusEnum.EFFECTING;
        }
    }
}