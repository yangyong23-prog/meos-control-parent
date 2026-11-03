package com.persagy.apccontrol.basic.opt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * B0：注册与开关配合的 Bean。Bean 始终存在，是否生效由 switch.opt.* 决定。
 */
@Configuration
public class OptFoundationConfiguration {

    @Bean
    public OptFeignMetricsInterceptor optFeignMetricsInterceptor() {
        return new OptFeignMetricsInterceptor();
    }
}
