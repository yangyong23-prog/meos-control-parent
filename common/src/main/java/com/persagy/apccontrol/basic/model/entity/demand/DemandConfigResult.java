package com.persagy.apccontrol.basic.model.entity.demand;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 需求配置预计算结果表
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_config_result", autoResultMap = true)
public class DemandConfigResult extends BaseAssignIdDO implements Serializable {

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 需求模板类型
     */
    @TableField(value = "demand_template_type")
    private DemandTemplateTypeEnum demandTemplateType;

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
     * 最终合并后的配置JSON
     */
    @TableField(value = "final_config_json", typeHandler = JacksonTypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private JsonNode finalConfigJson;

    /**
     * 是否自身存在自定义配置
     */
    @TableField(value = "is_customized_config")
    private Boolean isCustomizedConfig;

    /**
     * 是否屏蔽空间
     */
    @TableField(value = "is_shielded")
    private Boolean isShielded;

    /**
     * 从自身到项目的继承链路JSON
     */
    @TableField(value = "inherit_instances_json", typeHandler = JacksonTypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private JsonNode inheritInstancesJson;

    /**
     * 关联需求模板ID
     */
    @TableField(value = "rel_template_id")
    private Long relTemplateId;

    /**
     * 关联需求场景ID
     */
    @TableField(value = "rel_scene_id")
    private Long relSceneId;

}
