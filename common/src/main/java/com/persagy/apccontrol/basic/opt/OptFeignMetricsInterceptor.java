package com.persagy.apccontrol.basic.opt;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * B0：按下游服务、按 Feign 方法计数（{@code feign.<服务名>.<方法>}），只在 {@code switch.opt.metrics=true} 时累加。
 *
 * <p>与已有的 FeignRequestStatisticsInterceptor（写 Redis、按调用栈统计）互不影响，
 * 本类只写进程内计数，不访问 Redis。
 */
public class OptFeignMetricsInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        if (!OptSwitch.on(OptSwitch.METRICS)) {
            return;
        }
        OptMetrics.inc(metricName(template));
    }

    static String metricName(RequestTemplate template) {
        String target = template.feignTarget() == null ? null : template.feignTarget().name();
        String configKey = template.methodMetadata() == null ? null : template.methodMetadata().configKey();
        return metricName(target, configKey, template.path());
    }

    /**
     * @param target    Feign 客户端名（下游服务）
     * @param configKey 形如 {@code DtpTwinClient#queryTwinDataOnMoment(String,String)}，为空时退回请求路径
     * @param path      请求路径
     */
    static String metricName(String target, String configKey, String path) {
        String method = configKey == null ? path : configKey;
        int paren = method == null ? -1 : method.indexOf('(');
        if (paren > 0) {
            method = method.substring(0, paren);
        }
        return "feign." + (target == null ? "unknown" : target) + "." + method;
    }
}
