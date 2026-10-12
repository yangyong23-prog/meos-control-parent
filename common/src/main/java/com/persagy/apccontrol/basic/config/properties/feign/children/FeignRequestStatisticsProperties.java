package com.persagy.apccontrol.basic.config.properties.feign.children;

import com.persagy.apccontrol.basic.config.properties.feign.FeignProperties;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.cache.CacheKeyPrefix;

import java.util.ArrayList;
import java.util.List;

/**
 * Feign请求统计配置文件
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Data
@ConfigurationProperties(prefix = FeignRequestStatisticsProperties.KEY)
@RefreshScope
@FieldNameConstants
public class FeignRequestStatisticsProperties {

    public static final String KEY = FeignProperties.KEY + ".request-statistics";

    /**
     * 是否启动
     */
    private Boolean enabled;

    /**
     * 服务名称
     */
    private List<String> serviceNames = new ArrayList<>();

    /**
     * 缓存Key
     */
    private String redisKey = "feign" + CacheKeyPrefix.SEPARATOR + "request-statistics";

}
