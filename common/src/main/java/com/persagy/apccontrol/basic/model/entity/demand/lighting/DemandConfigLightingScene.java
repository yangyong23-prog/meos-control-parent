package com.persagy.apccontrol.basic.model.entity.demand.lighting;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingSceneTimeIntervalDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 需求配置-照明场景表
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_config_lighting_scene", autoResultMap = true)
public class DemandConfigLightingScene extends BaseAssignIdDO implements Serializable {

    private static final long serialVersionUID = 2291508432143794911L;

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
     * 开始时间
     */
    @TableField(value = "begin_time", typeHandler = JacksonTypeHandler.class)
    private DemandLightingSceneTimeIntervalDTO beginTime;

    /**
     * 结束时间
     */
    @TableField(value = "end_time", typeHandler = JacksonTypeHandler.class)
    private DemandLightingSceneTimeIntervalDTO endTime;

    /**
     * 场景名称
     */
    @TableField(value = "scene_name")
    private String sceneName;

    /**
     * 排序
     */
    @TableField(value = "sequence")
    private Integer sequence;

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