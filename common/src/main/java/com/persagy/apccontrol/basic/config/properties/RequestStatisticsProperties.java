package com.persagy.apccontrol.basic.config.properties;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 请求统计配置文件
 *
 * @author JiYinchuan
 * @since 7.250403.0
 */
@Data
@ConfigurationProperties(prefix = RequestStatisticsProperties.KEY)
@RefreshScope
@FieldNameConstants
public class RequestStatisticsProperties {

    public static final String KEY = ProjectProperties.KEY + ".request-statistics";

    /**
     * 是否启动
     */
    private Boolean enabled;

    /**
     * 缓存Key前缀
     */
    private String redisKeyPrefix = "request-statistics";

}
