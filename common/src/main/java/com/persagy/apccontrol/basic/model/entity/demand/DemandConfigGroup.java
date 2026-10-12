package com.persagy.apccontrol.basic.model.entity.demand;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 需求配置组表
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_config_group")
public class DemandConfigGroup extends BaseAssignIdDO implements Serializable {

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 分组名称
     */
    @TableField(value = "group_name")
    private String groupName;

    /**
     * 关联模版ID
     */
    @TableField(value = "rel_template_Id")
    private Long relTemplateId;

    /**
     * 关联场景ID
     */
    @TableField(value = "rel_scene_id")
    private Long relSceneId;

}