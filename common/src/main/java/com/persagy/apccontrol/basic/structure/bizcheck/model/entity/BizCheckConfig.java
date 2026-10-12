package com.persagy.apccontrol.basic.structure.bizcheck.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 业务可用性检查配置表实体类
 * 对应表：biz_check_config
 *
 * @author wxf
 * @date 2026-02-26
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "biz_check_config")
public class BizCheckConfig extends BaseAssignIdDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 子业务编码
     * 示例：COOLING(供冷业务)、HEATING(供热业务)、AIR_CON(空调业务)
     */
    @TableField(value = "biz_code")
    private RunningMonitorTypeEnum bizCode;

    /**
     * 是否启用检查：1=启用，0=禁用
     * 建议搭配枚举使用，提升代码可读性
     */
    @TableField(value = "is_enable")
    private Boolean isEnable;

    /**
     * 是否启用自动检查：1=启用，0=禁用
     * 建议搭配枚举使用，提升代码可读性
     */
    @TableField(value = "is_auto_check")
    private Boolean isAutoCheck;

    /**
     * 自动检查的每日时间，如10:00
     */
    @TableField(value = "auto_check_time")
    private String autoCheckTime;

    /**
     * 日期段配置，JSON格式存储
     * 示例：[{"startDay":"01-26","endDay":"01-30"}]
     * 注：如需结构化处理，可定义对应的DTO类，结合TypeHandler实现JSON与Java对象的转换
     */
    @TableField(value = "date_range_config")
    private String dateRangeConfig;

    /**
     * 异常通知时段配置，JSON格式存储
     * 示例：{"systemInstanceId":"SYS001","notifyDays":[1,2,3,4,5,6]}
     */
    @TableField(value = "notify_period_config")
    private String notifyPeriodConfig;

}