package com.persagy.apccontrol.basic.config.properties.saas.children.demand.ventilation;

import com.persagy.apccontrol.basic.config.properties.BaseProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.DemandProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 需求管理-通风需求配置文件
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = DemandVentilationProperties.KEY)
@RefreshScope
public class DemandVentilationProperties extends BaseProperties {

    public static final String KEY = DemandProperties.KEY + ".ventilation";

    /**
     * 默认配置
     */
    @NestedConfigurationProperty
    private DemandVentilationDefaultSettingProperties defaultSetting = new DemandVentilationDefaultSettingProperties();

}
