package com.persagy.apccontrol.basic.model.entity.demand;

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
import java.time.LocalDateTime;

/**
 * 需求配置预计算结果快照表
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_config_result_snapshot", autoResultMap = true)
public class DemandConfigResultSnapshot extends BaseAssignIdDO implements Serializable {

    /**
     * 当前有效快照结束时间
     */
    public static final LocalDateTime OPEN_END_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

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
    @TableField(value = "final_config_json", typeHandler = JacksonTypeHandler.class)
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
    @TableField(value = "inherit_instances_json", typeHandler = JacksonTypeHandler.class)
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

    /**
     * 生效截止时间
     */
    @TableField(value = "valid_end_time")
    private LocalDateTime validEndTime;

    /**
     * 根据当前结果创建开放快照
     *
     * @param result 结果
     * @return 结果快照
     * @since v7.260701.0
     */
    public static DemandConfigResultSnapshot of(DemandConfigResult result) {
        DemandConfigResultSnapshot snapshot = new DemandConfigResultSnapshot();
        snapshot.setId(result.getId());
        snapshot.setProjectId(result.getProjectId());
        snapshot.setDemandTemplateType(result.getDemandTemplateType());
        snapshot.setDemandInstanceType(result.getDemandInstanceType());
        snapshot.setDemandInstanceId(result.getDemandInstanceId());
        snapshot.setFinalConfigJson(result.getFinalConfigJson());
        snapshot.setIsCustomizedConfig(result.getIsCustomizedConfig());
        snapshot.setIsShielded(result.getIsShielded());
        snapshot.setInheritInstancesJson(result.getInheritInstancesJson());
        snapshot.setRelTemplateId(result.getRelTemplateId());
        snapshot.setRelSceneId(result.getRelSceneId());
        snapshot.setCreateTime(result.getCreateTime());
        snapshot.setCreateUser(result.getCreateUser());
        snapshot.setUpdateTime(result.getUpdateTime());
        snapshot.setUpdateUser(result.getUpdateUser());
        snapshot.setValidEndTime(DemandConfigResultSnapshot.OPEN_END_TIME);
        return snapshot;
    }
}
