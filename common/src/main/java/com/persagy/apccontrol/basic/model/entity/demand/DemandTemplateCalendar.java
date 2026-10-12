package com.persagy.apccontrol.basic.model.entity.demand;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 需求模版日历表
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_template_calendar")
public class DemandTemplateCalendar extends BaseAssignIdDO implements Serializable {

    private static final long serialVersionUID = 7706926024668982021L;

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 需求类型
     */
    @TableField(value = "demand_type")
    private DemandTypeEnum demandType;

    /**
     * 需求模版类型
     */
    @TableField(value = "demand_template_type")
    private DemandTemplateTypeEnum demandTemplateType;

    /**
     * 日期
     */
    @TableField(value = "work_date")
    private LocalDate workDate;

    /**
     * 是否自定义配置
     */
    @TableField(value = "is_customized_setting")
    private Boolean isCustomizedSetting;

    /**
     * 关联需求模版ID
     */
    @TableField(value = "rel_demand_template_id")
    private Long relDemandTemplateId;

    /**
     * 状态
     */
    @TableField(value = "st")
    private StateTypeEnum st;

}