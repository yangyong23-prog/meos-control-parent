package com.persagy.apccontrol.basic.config.properties.saas.children.demand.airconditioning;

import com.persagy.apccontrol.basic.config.properties.BaseProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.DemandProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 需求管理-空调需求配置文件
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = DemandAirconditioningProperties.KEY)
@RefreshScope
public class DemandAirconditioningProperties extends BaseProperties {

    public static final String KEY = DemandProperties.KEY + ".airconditioning";

    /**
     * 默认配置
     */
    @NestedConfigurationProperty
    private DemandAirconditioningDefaultSettingProperties defaultSetting =
            new DemandAirconditioningDefaultSettingProperties();

}
