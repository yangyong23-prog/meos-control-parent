package com.persagy.apccontrol.basic.model.entity.demand;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 需求配置表
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_config", autoResultMap = true)
public class DemandConfig extends BaseAssignIdDO implements Serializable {

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
     * 配置数据
     */
    @TableField(value = "config_data", typeHandler = JacksonTypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private JsonNode configData;

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

    /**
     * 关联分组ID
     */
    @TableField(value = "rel_group_id")
    private Long relGroupId;

    /**
     * 填充配置数据
     *
     * @param configData 配置数据
     * @param <T>        配置类型
     * @since 5.240902.0
     */
    public <T extends BaseDemandConfig> void fillConfigData(@NonNull DemandConfigValueObject<T> configData) {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        this.configData = objectMapper.convertValue(configData, JsonNode.class);
    }

    /**
     * 创建配置实例
     *
     * @param templateId 模版ID
     * @param sceneId    场景ID
     * @param instance   实例
     * @param configData 配置数据
     * @return 配置实例
     * @since 5.240902.0
     */
    public static DemandConfig of(
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull DemandInstanceValueObject instance,
            @Nullable Object configData) {
        DemandConfig config = new DemandConfig();
        config.setProjectId(ContextUtil.getProjectId());
        config.setDemandInstanceType(instance.getInstanceType());
        config.setDemandInstanceId(instance.getInstanceId());
        if (Objects.nonNull(configData)) {
            ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
            config.setConfigData(objectMapper.convertValue(configData, JsonNode.class));
        }
        config.setRelTemplateId(templateId);
        config.setRelSceneId(sceneId);
        return config;
    }

    /**
     * 创建配置实例
     *
     * @param lightingConfig 照明配置
     * @param configData     配置数据
     * @param relGroupId     关联分组ID
     * @return 配置实例
     * @since 7.250801.0
     */
    public static @NonNull DemandConfig of(
            @NonNull DemandConfigLighting lightingConfig,
            @NonNull DemandConfigValueObject<DemandConfigLightingValueObject> configData,
            @NonNull Long relGroupId) {
        DemandConfig result = new DemandConfig();
        result.setProjectId(lightingConfig.getProjectId());
        result.setDemandInstanceType(lightingConfig.getDemandInstanceType());
        result.setDemandInstanceId(lightingConfig.getDemandInstanceId());
        result.fillConfigData(configData);
        result.setRelTemplateId(lightingConfig.getRelDemandTemplateId());
        result.setRelSceneId(lightingConfig.getRelDemandLightingSceneId());
        result.setRelGroupId(relGroupId);
        result.setCreateTime(lightingConfig.getCreateTime());
        result.setCreateUser(lightingConfig.getCreateUser());
        result.setUpdateTime(lightingConfig.getUpdateTime());
        result.setUpdateUser(lightingConfig.getUpdateUser());
        return result;
    }

    /**
     * 转换模版场景ID和配置映射
     *
     * @param configs 配置
     * @return 模版ID-场景ID -> 配置
     * @since 7.250801.0
     */
    public static MultiKeyMap<Long, List<DemandConfig>> toTemplateSceneIdAndConfigs(
            @NonNull List<DemandConfig> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return new MultiKeyMap<>();
        }
        MultiKeyMap<Long, List<DemandConfig>> templateSceneIdAndConfig = new MultiKeyMap<>();
        for (DemandConfig config : configs) {
            Long relTemplateId = config.getRelTemplateId();
            Long relSceneId = config.getRelSceneId();
            MultiKey<Long> multiKey = new MultiKey<>(relTemplateId, relSceneId);
            MapExecutor.dynamicPut(templateSceneIdAndConfig, multiKey, config, ArrayList::new);
        }
        return templateSceneIdAndConfig;
    }
}