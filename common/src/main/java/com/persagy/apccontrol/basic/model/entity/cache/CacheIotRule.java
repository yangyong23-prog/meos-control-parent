package com.persagy.apccontrol.basic.model.entity.cache;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 缓存-IOT规则表
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "cache_iot_rule")
public class CacheIotRule extends BaseAssignIdDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 运行监测类型
     */
    @TableField(value = "running_monitor_type")
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型
     */
    @TableField(value = "running_monitor_second_type")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 自定义 Key
     */
    @TableField(value = "custom_key")
    private String customKey;

    /**
     * 对象ID
     */
    @TableField(value = "object_id")
    private String objectId;

    /**
     * 信息点编码
     */
    @TableField(value = "info_code")
    private String infoCode;

    /**
     * 类型
     */
    @TableField(value = "type")
    private CacheIotRuleTypeEnum type;


}
