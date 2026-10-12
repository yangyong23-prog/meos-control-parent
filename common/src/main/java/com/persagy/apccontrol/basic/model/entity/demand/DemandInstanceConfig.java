package com.persagy.apccontrol.basic.model.entity.demand;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.io.Serializable;

/**
 * 需求实例配置表
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_instance_config")
public class DemandInstanceConfig extends BaseAssignIdDO implements Serializable {

    private static final long serialVersionUID = -6508339482820523773L;

    /**
     * 项目
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
     * 配置数据
     */
    @TableField(value = "config_data")
    private String configData;

    /**
     * 状态
     */
    @TableField(value = "st")
    private StateTypeEnum st;

    /**
     * 转换值对象
     *
     * @return 值对象
     * @since 5.240902.0
     */
    public DemandInstanceConfigValueObject toValueObject() {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        try {
            if (StringUtils.isBlank(configData)) {
                return DemandInstanceConfigValueObject.newInstance();
            }
            return objectMapper.readValue(configData, DemandInstanceConfigValueObject.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 填充配置
     *
     * @param config 配置
     * @return 当前实例
     * @since 5.240902.0
     */
    public DemandInstanceConfig fillConfig(@NonNull DemandInstanceConfigValueObject config) {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        try {
            this.configData = objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 创建实例配置
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @param instanceType 实例类型
     * @param instanceId   实例ID
     * @param valueObject  实例配置
     * @return 实例配置
     * @since v6.0.0
     */
    public static DemandInstanceConfig newInstance(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId,
            @NonNull DemandInstanceConfigValueObject valueObject) {
        DemandInstanceConfig result = new DemandInstanceConfig();
        result.setProjectId(projectId);
        result.setDemandType(templateType.toDemandTypeEnum());
        result.setDemandTemplateType(templateType);
        result.setDemandInstanceType(instanceType);
        result.setDemandInstanceId(instanceId);
        result.fillConfig(valueObject);
        result.setSt(StateTypeEnum.VALID);
        return result;
    }
}