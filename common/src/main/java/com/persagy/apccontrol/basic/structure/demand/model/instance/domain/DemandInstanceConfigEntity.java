package com.persagy.apccontrol.basic.structure.demand.model.instance.domain;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.base.domain.Entity;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.identifier.DemandInstanceConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;
import org.springframework.lang.NonNull;

/**
 * 需求实例配置实体
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandInstanceConfigEntity implements Entity<DemandInstanceConfigIdentifier> {

    /**
     * 标识符
     */
    private DemandInstanceConfigIdentifier identifier;

    /**
     * 实例配置值对象
     */
    private DemandInstanceConfigValueObject config;

    /**
     * 转换新增配置
     *
     * @return 新增配置
     * @since 5.240902.0
     */
    public DemandInstanceConfig toConfigAddParam() {
        DemandInstanceConfig result = new DemandInstanceConfig();
        result.setProjectId(ContextUtil.getProjectId());
        result.setDemandType(identifier.getTemplateType().toDemandTypeEnum());
        result.setDemandTemplateType(identifier.getTemplateType());
        result.setDemandInstanceType(identifier.getInstance().getInstanceType());
        result.setDemandInstanceId(identifier.getInstance().getInstanceId());
        result.fillConfig(config);
        result.setSt(StateTypeEnum.VALID);
        return result;
    }

    /**
     * 转换新增配置
     *
     * @return 新增配置
     * @since 5.240902.0
     */
    public DemandInstanceConfig toConfigEditParam() {
        DemandInstanceConfig result = new DemandInstanceConfig();
        result.setId(identifier.getConfigId());
        result.fillConfig(config);
        return result;
    }

    /**
     * 创建实例配置实体
     *
     * @param config 配置
     * @return 实例配置实体
     * @since 5.240902.0
     */
    public static DemandInstanceConfigEntity of(@NonNull DemandInstanceConfig config) {
        Long configId = config.getId();
        DemandTemplateTypeEnum templateType = config.getDemandTemplateType();
        DemandInstanceTypeEnum instanceType = config.getDemandInstanceType();
        String instanceId = config.getDemandInstanceId();
        DemandInstanceConfigIdentifier identifier = new DemandInstanceConfigIdentifier();
        identifier.setConfigId(configId);
        identifier.setTemplateType(templateType);
        identifier.setInstance(new DemandInstanceValueObject(instanceType, instanceId));
        DemandInstanceConfigEntity entity = new DemandInstanceConfigEntity();
        entity.setIdentifier(identifier);
        entity.setConfig(config.toValueObject());
        return entity;
    }
}
