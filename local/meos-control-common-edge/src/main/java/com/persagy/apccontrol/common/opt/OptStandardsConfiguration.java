package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.common.mapper.OptStandardsModeMapper;
import com.persagy.apccontrol.common.outside.feign.OptDtpMeosStandardClient;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/**
 * 运行标准过渡 B1–B4 的 Bean。全部由 {@code switch.opt.*} 运行时开关控制，默认关闭时不改变任何行为。
 * 拦截器在 meos-control-server-edge 的 InterceptorConfig 中注册。
 */
@Configuration
public class OptStandardsConfiguration {

    @Bean
    public OptStandardsMode optStandardsMode(OptStandardsModeMapper mapper) {
        return new OptStandardsMode(projectId -> mapper.countStandardConfig(projectId) > 0, System::currentTimeMillis);
    }

    @Bean
    public OptStandardsReadonlyInterceptor optStandardsReadonlyInterceptor(OptStandardsMode mode,
                                                                           ObjectProvider<ObjectMapper> objectMapper) {
        return new OptStandardsReadonlyInterceptor(mode, objectMapper.getIfAvailable(ObjectMapper::new));
    }

    @Bean
    public OptDtpMeosCallCounter optDtpMeosCallCounter(RedisCacheUtil redisCacheUtil) {
        return new OptDtpMeosCallCounter(new OptRedisCallCountStore(redisCacheUtil), System::currentTimeMillis, ZoneId.systemDefault());
    }

    @Bean
    public OptStandardsDisplay optStandardsDisplay(OptDtpMeosStandardClient client) {
        return new OptStandardsDisplay(
                (projectId, body) -> client.listStandardComputeResult(projectId, ContextUtil.getGroupCode(), body),
                (projectId, body) -> client.listStandardComputeResultDetail(projectId, ContextUtil.getGroupCode(), body));
    }
}
