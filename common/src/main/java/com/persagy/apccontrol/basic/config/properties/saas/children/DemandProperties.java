package com.persagy.apccontrol.basic.config.properties.saas.children;


import com.persagy.apccontrol.basic.config.properties.saas.SaasProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.DemandBasicProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.DemandTaskOnStartedProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.airconditioning.DemandAirconditioningProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.lighting.DemandLightingProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.ventilation.DemandVentilationProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 需求管理配置文件
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@ConfigurationProperties(prefix = DemandProperties.KEY)
@RefreshScope
public class DemandProperties {

    public static final String KEY = SaasProperties.KEY + ".demand";

    /**
     * 需求管理启动任务
     */
    @NestedConfigurationProperty
    private DemandTaskOnStartedProperties taskOnStarted = new DemandTaskOnStartedProperties();

    /**
     * 基础配置
     */
    @NestedConfigurationProperty
    private DemandBasicProperties basic = new DemandBasicProperties();

    /**
     * 空调需求配置
     */
    @NestedConfigurationProperty
    private DemandAirconditioningProperties airconditioning = new DemandAirconditioningProperties();

    /**
     * 照明需求配置
     */
    @NestedConfigurationProperty
    private DemandLightingProperties lighting = new DemandLightingProperties();

    /**
     * 通风需求配置
     */
    @NestedConfigurationProperty
    private DemandVentilationProperties ventilation = new DemandVentilationProperties();

}
