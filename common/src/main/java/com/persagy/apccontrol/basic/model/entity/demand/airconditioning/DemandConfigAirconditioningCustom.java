package com.persagy.apccontrol.basic.model.entity.demand.airconditioning;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;

/**
 * 需求配置-空调需求配置-定制功能配置表
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_config_airconditioning_custom", autoResultMap = true)
public class DemandConfigAirconditioningCustom extends BaseAssignIdDO implements Serializable {

    private static final long serialVersionUID = 7807084415904943337L;

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

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
     * 配置数据
     */
    @TableField(value = "config_data", typeHandler = JacksonTypeHandler.class)
    private DemandConfigAirconditioningCustomDTO configData;

    /**
     * 关联需求模版ID
     */
    @TableField(value = "rel_template_id")
    private String relTemplateId;

    /**
     * 关联需求场景ID
     */
    @TableField(value = "rel_scene_id")
    private String relSceneId;

    /**
     * 根据项目创建默认实例
     *
     * @return 项目默认实例
     * @since 7.250702.0
     */
    public static DemandConfigAirconditioningCustom defaultInstanceByProject(@NonNull Long relTemplateId) {
        String projectId = ContextUtil.getProjectId();
        DemandConfigAirconditioningCustom result = new DemandConfigAirconditioningCustom();
        result.setProjectId(projectId);
        result.setDemandInstanceType(DemandInstanceTypeEnum.PROJECT);
        result.setDemandInstanceId(projectId);
        result.setConfigData(DemandConfigAirconditioningCustomDTO.defaultInstance());
        result.setRelTemplateId(relTemplateId.toString());
        result.setId(IdUtil.getSnowflakeNextId());
        return result;
    }
}