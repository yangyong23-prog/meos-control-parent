package com.persagy.apccontrol.basic.model.entity.demand.lighting;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingDataDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 需求配置-照明表
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_config_lighting", autoResultMap = true)
public class DemandConfigLighting extends BaseAssignIdDO implements Serializable {

    private static final long serialVersionUID = 7807084415904943337L;

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
    @TableField(value = "demand_template_Tag")
    private String demandTemplateTag;

    /**
     * 需求实例类型
     */
    @TableField(value = "demand_instance_type")
    private DemandInstanceTypeEnum demandInstanceType;

    /**
     * 需求实例ID
     */
    @TableField(value = "demand_instance_id")
    private String demandInstanceId;

    /**
     * 需求实例
     */
    @TableField(exist = false)
    private String demandInstanceName;

    /**
     * 是否自定义配置;当不是自定义配置时则查询引用配置
     */
    @TableField(value = "is_customized_setting")
    private Boolean isCustomizedSetting;

    /**
     * 配置数据
     */
    @TableField(value = "config_data", typeHandler = JacksonTypeHandler.class)
    private DemandConfigLightingDataDTO configData;

    /**
     * 关联需求模版ID
     */
    @TableField(value = "rel_demand_template_id")
    private Long relDemandTemplateId;

    /**
     * 关联照明场景ID
     */
    @TableField(value = "rel_demand_lighting_scene_id")
    private Long relDemandLightingSceneId;

    /**
     * 状态
     */
    @TableField(value = "st")
    private StateTypeEnum st;

}