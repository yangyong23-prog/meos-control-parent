package com.persagy.apccontrol.basic.model.entity.demand;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingScene;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;

/**
 * 需求场景表
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_scene", autoResultMap = true)
public class DemandScene extends BaseAssignIdDO implements Serializable {

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

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
     * 场景名称
     */
    @TableField(value = "scene_name")
    private String sceneName;

    /**
     * 开始时间
     */
    @TableField(value = "begin_time", typeHandler = JacksonTypeHandler.class)
    private DemandTimeValueObject beginTime;

    /**
     * 结束时间
     */
    @TableField(value = "end_time", typeHandler = JacksonTypeHandler.class)
    private DemandTimeValueObject endTime;

    /**
     * 排序
     */
    @TableField(value = "sequence")
    private Integer sequence;

    /**
     * 关联模版ID
     */
    @TableField(value = "rel_template_id")
    private Long relTemplateId;

    /**
     * 创建实例
     *
     * @param scene 照明场景
     * @return 当前实例
     * @since 7.250801.0
     */
    public static DemandScene of(@NonNull DemandConfigLightingScene scene) {
        DemandScene result = new DemandScene();
        result.setProjectId(scene.getProjectId());
        result.setDemandTemplateType(scene.getDemandTemplateType());
        result.setDemandTemplateTag(scene.getDemandTemplateTag());
        result.setSceneName(scene.getSceneName());
        result.setBeginTime(DemandTimeValueObject.of(scene.getBeginTime()));
        result.setEndTime(DemandTimeValueObject.of(scene.getEndTime()));
        result.setSequence(scene.getSequence());
        result.setRelTemplateId(scene.getRelDemandTemplateId());
        result.setId(scene.getId());
        result.setCreateTime(scene.getCreateTime());
        result.setCreateUser(scene.getCreateUser());
        result.setUpdateTime(scene.getUpdateTime());
        result.setUpdateUser(scene.getUpdateUser());
        return result;
    }
}