package com.persagy.apccontrol.basic.model.entity.demand.lighting;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingResultDataDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInheritModeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 需求管理-照明结果表
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_config_lighting_result", autoResultMap = true)
public class DemandConfigLightingResult extends BaseAssignIdDO implements Serializable {
    
    private static final long serialVersionUID = -690863038297158436L;

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 照明回路ID
     */
    @TableField(value = "circuit_id")
    private String circuitId;

    /**
     * 配置数据
     */
    @TableField(value = "config_data", typeHandler = JacksonTypeHandler.class)
    private DemandConfigLightingResultDataDTO configData;
    
    /**
     * 配置来源对象 ID
     */
    @TableField(value = "config_source_obj_id")
    private String configSourceObjId;
    /**
     * 配置来源类型
     */
    @TableField(value = "config_source_type")
    private DemandInheritModeEnum configSourceType;

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
    
    
}