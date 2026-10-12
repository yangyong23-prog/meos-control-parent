package com.persagy.apccontrol.basic.config.properties.saas.children.demand;

import com.persagy.apccontrol.basic.config.properties.BaseProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.DemandProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 需求管理自启动任务配置文件
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = DemandTaskOnStartedProperties.KEY)
@RefreshScope
public class DemandTaskOnStartedProperties extends BaseProperties {

    public static final String KEY = DemandProperties.KEY + ".task-on-started";

    /**
     * 是否在启动时自动初始化模版
     */
    private boolean initTemplateOnStartUp = true;


}
