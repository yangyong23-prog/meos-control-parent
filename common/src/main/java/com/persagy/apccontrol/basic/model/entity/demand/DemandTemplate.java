package com.persagy.apccontrol.basic.model.entity.demand;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTagEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

/**
 * 需求模版表
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_template")
public class DemandTemplate extends BaseAssignIdDO implements Serializable {

    private static final long serialVersionUID = 3264755958539550155L;

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
     * 需求模版标签
     */
    @TableField(value = "demand_template_tag")
    private String demandTemplateTag;

    /**
     * 模版名称
     */
    @TableField(value = "demand_template_name")
    private String demandTemplateName;

    /**
     * 是否为预置模版
     */
    @TableField(value = "is_initialized")
    private Boolean isInitialized;

    /**
     * 是否启用
     */
    @TableField(value = "is_enabled")
    private Boolean isEnabled;

    /**
     * 状态, 暂未使用, 可以删除
     */
    @TableField(value = "st")
    private StateTypeEnum st;

    /**
     * 转换需求模版标签枚举
     *
     * @return 需求模版标签枚举
     * @since v6.0.0
     */
    public DemandTemplateTagEnum toDemandTemplateTagEnum() {
        DemandTemplateTagEnum result = BaseEnum.analyze(demandTemplateTag, DemandTemplateTagEnum.class, false);
        return Objects.nonNull(result) ? result : DemandTemplateTagEnum.CUSTOM;
    }
}