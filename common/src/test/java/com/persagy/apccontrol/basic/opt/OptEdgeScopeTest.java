package com.persagy.apccontrol.basic.opt;

import com.persagy.apccontrol.basic.outside.feign.client.DangerEventClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerEventQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerInstanceTreeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.impl.ExceptionEventClientServiceImpl;
import com.persagy.apccontrol.basic.outside.feign.wrapper.DangerEventClientPageWrapper;
import com.persagy.apccontrol.basic.outside.feign.wrapper.ExceptionEventClientPageWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.mock.env.MockEnvironment;

/**
 * opt：边缘盒子已移除服务的开关（docs/opt/edge-scope.md）
 */
public class OptEdgeScopeTest {

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
    }

    @Test
    void switchesDefaultOff() {
        OptSwitch.setEnvironmentForTest(new MockEnvironment());
        Assertions.assertFalse(OptEdgeScope.noYiheng());
        Assertions.assertFalse(OptEdgeScope.noEnergyDdp());
        Assertions.assertFalse(OptEdgeScope.noAiAu());
    }

    @Test
    void riskEventQueriesSkipYihengWhenOn() {
        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.edgeNoYiheng", "true"));
        DangerEventClientPageWrapper dangerWrapper = Mockito.mock(DangerEventClientPageWrapper.class);
        ExceptionEventClientPageWrapper exceptionWrapper = Mockito.mock(ExceptionEventClientPageWrapper.class);
        DangerEventClient dangerClient = Mockito.mock(DangerEventClient.class);
        ExceptionEventClientServiceImpl service =
                new ExceptionEventClientServiceImpl(dangerWrapper, exceptionWrapper, dangerClient);

        Assertions.assertTrue(service.listDangerEvent(new DangerEventQueryParam()).isEmpty());
        Assertions.assertTrue(service.listDangerEventTest(new DangerEventQueryParam()).isEmpty());
        Assertions.assertTrue(service.listDangerInstanceTree(Mockito.mock(DangerInstanceTreeQueryParam.class)).isEmpty());
        Mockito.verifyNoInteractions(dangerWrapper, exceptionWrapper, dangerClient);
    }

    @Test
    void kafkaAutoStartupExpression() {
        SpelExpressionParser parser = new SpelExpressionParser();
        // KafkaCommonConsumerListener.autoStartup 在占位符替换后的表达式
        Assertions.assertEquals(Boolean.FALSE, parser.parseExpression("'true'.trim() != 'true'").getValue());
        Assertions.assertEquals(Boolean.FALSE, parser.parseExpression("' true '.trim() != 'true'").getValue());
        Assertions.assertEquals(Boolean.TRUE, parser.parseExpression("'false'.trim() != 'true'").getValue());
    }
}
