package com.persagy.apccontrol.basic.config;

import com.persagy.apccontrol.basic.config.aspect.RequestStatisticsAspect;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.config.interceprot.DebugApiAuthenticationInterceptor;
import com.persagy.apccontrol.basic.config.properties.ProjectProperties;
import com.persagy.apccontrol.basic.config.properties.RequestStatisticsProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WebConfigurer implements WebMvcConfigurer {

    private final ProjectProperties projectProperties;

    @Bean
    public CustomHeaderThreadLocalInterceptor customHeaderThreadLocalInterceptor() {
        return new CustomHeaderThreadLocalInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customHeaderThreadLocalInterceptor());
        if (BooleanUtils.isTrue(projectProperties.getDebugInterceptor().isEnabled())) {
            registry.addInterceptor(new DebugApiAuthenticationInterceptor(projectProperties))
                    .addPathPatterns("/debug/beanDebug");
        }
    }

    @Bean
    @ConditionalOnProperty(
            value = RequestStatisticsProperties.Fields.enabled,
            prefix = RequestStatisticsProperties.KEY,
            havingValue = "true")
    public RequestStatisticsAspect requestStatisticsAspect(
            RequestStatisticsProperties properties,
            RedisCacheTemplate redisCacheTemplate) {
        return new RequestStatisticsAspect(properties, redisCacheTemplate);
    }
}
