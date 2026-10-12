package com.persagy.apccontrol.basic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.config.properties.feign.children.FeignRequestStatisticsProperties;
import com.persagy.apccontrol.basic.outside.feign.decoder.FeignResponseDecoder;
import com.persagy.apccontrol.basic.outside.feign.interceptor.RequestCustomHeaderInterceptor;
import com.persagy.apccontrol.basic.outside.feign.interceptor.FeignRequestStatisticsInterceptor;
import com.persagy.apccontrol.basic.outside.feign.interceptor.RequestTokenInterceptor;
import com.persagy.validate.utility.framework.converter.*;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.optionals.OptionalDecoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Feign配置
 *
 * @author JiYinchuan
 * @see org.springframework.cloud.openfeign.FeignClientsConfiguration
 * @since v5.1.0
 */
@SpringBootConfiguration
public class FeignConfiguration {

    public static final String HEADER_APP_ID = "appId";
    public static final String HEADER_REQUEST_TIMESTAMP = "requestTimestamp";

    @Bean
    public FeignFormatterRegistrar dateFormatRegister() {
        return registry -> {
            registry.addConverter(new DateConverter());
            registry.addConverter(new LocalDateTimeConverter());
            registry.addConverter(new LocalDateConverter());
            registry.addConverter(new LocalTimeConverter());
            registry.addConverter(new YearConverter());
            registry.addConverter(new YearMonthConverter());
            registry.addConverter(new MonthDayConverter());
            registry.addConverter(new ZonedDateTimeConverter());
            registry.addConverter(new OffsetDateTimeConverter());
            registry.addConverter(new OffsetTimeConverter());
        };
    }

    @Bean
    public Decoder decoder(
            ObjectProvider<HttpMessageConverters> messageConverters,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new OptionalDecoder(new ResponseEntityDecoder(new FeignResponseDecoder(
                new SpringDecoder(messageConverters), objectMapperProvider)));
    }

    @Bean
    @Profile({"local", "dev"})
    public feign.RequestInterceptor requestTokenInterceptor(@Value("${token:}") String token) {
        return new RequestTokenInterceptor(token);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestCustomHeaderInterceptor();
    }

    @Bean
    @ConditionalOnProperty(
            value = FeignRequestStatisticsProperties.Fields.enabled,
            prefix = FeignRequestStatisticsProperties.KEY,
            havingValue = "true")
    public RequestInterceptor feignRequestStatisticsInterceptor(
            FeignRequestStatisticsProperties properties,
            RedisCacheTemplate redisCacheTemplate) {
        return new FeignRequestStatisticsInterceptor(properties, redisCacheTemplate);
    }
}
