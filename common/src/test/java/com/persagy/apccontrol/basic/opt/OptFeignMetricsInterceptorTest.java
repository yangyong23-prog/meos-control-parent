package com.persagy.apccontrol.basic.opt;

import feign.RequestTemplate;
import feign.Target;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

public class OptFeignMetricsInterceptorTest {

    interface DemoClient {
        String hello(String a);
    }

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
        OptMetrics.drain();
    }

    @Test
    void metricNameUsesTargetAndMethodWithoutArgs() {
        Assertions.assertEquals("feign.dtp-twin.DtpTwinClient#queryTwinDataOnMoment",
                OptFeignMetricsInterceptor.metricName("dtp-twin", "DtpTwinClient#queryTwinDataOnMoment(String,String)", "/x"));
        Assertions.assertEquals("feign.unknown./twin/query",
                OptFeignMetricsInterceptor.metricName(null, null, "/twin/query"));
    }

    @Test
    void countsOnlyWhenSwitchOn() {
        RequestTemplate template = new RequestTemplate();
        template.feignTarget(new Target.HardCodedTarget<>(DemoClient.class, "dtp-twin", "http://x"));
        template.uri("/hello");

        OptSwitch.setEnvironmentForTest(new MockEnvironment());
        OptMetrics.drain();
        new OptFeignMetricsInterceptor().apply(template);
        Assertions.assertTrue(OptMetrics.drain().isEmpty());

        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.metrics", "true"));
        new OptFeignMetricsInterceptor().apply(template);
        new OptFeignMetricsInterceptor().apply(template);
        Assertions.assertEquals(2L, OptMetrics.drain().get("feign.dtp-twin./hello"));
    }
}
