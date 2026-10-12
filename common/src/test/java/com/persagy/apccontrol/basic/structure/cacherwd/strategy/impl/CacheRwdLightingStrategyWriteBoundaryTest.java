package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingCircuitDetailDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheWriteAction;
import com.persagy.apccontrol.basic.structure.cacherwd.service.LightingCacheObjectRelationService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorLightingStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Collections;
import java.util.List;

/**
 * 照明完整构建的新对象模型写入边界测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheRwdLightingStrategyWriteBoundaryTest {

    /**
     * 原 Hutool Spring 上下文。
     *
     * @since v7.260701.0
     */
    private static @Nullable ApplicationContext originalApplicationContext;

    /**
     * 查询参数构造所需的隔离轻量上下文。
     *
     * @since v7.260701.0
     */
    private static @Nullable StaticApplicationContext testApplicationContext;

    /**
     * 注册运行监测查询参数构造函数读取的三个纯 Mock 依赖。
     *
     * @since v7.260701.0
     */
    @BeforeAll
    public static void setUpSpringUtil() {
        originalApplicationContext = SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton(
                "dtpRwdClientService", Mockito.mock(DtpRwdClientService.class));
        applicationContext.getBeanFactory().registerSingleton(
                "abstractIotDataCacheServer", Mockito.mock(AbstractIotDataCacheServer.class));
        applicationContext.getBeanFactory().registerSingleton(
                "dtpRwdConvert", Mockito.mock(DtpRwdConvert.class));
        applicationContext.refresh();
        testApplicationContext = applicationContext;
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * 恢复 Hutool Spring 上下文并关闭隔离上下文。
     *
     * @since v7.260701.0
     */
    @AfterAll
    public static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(originalApplicationContext);
        if (testApplicationContext != null) {
            testApplicationContext.close();
        }
        testApplicationContext = null;
        originalApplicationContext = null;
    }

    /**
     * 验证五个照明 Key 均提交完整源快照，并保留调用方准备的关系动作。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldCommitSourceSnapshotAndTypedRelationActionForEveryKey() {
        RunningMonitorLightingStrategy sourceStrategy =
                Mockito.mock(RunningMonitorLightingStrategy.class);
        CacheObjectModelWriteService writeService =
                Mockito.mock(CacheObjectModelWriteService.class);
        LightingCacheObjectRelationService relationService =
                Mockito.mock(LightingCacheObjectRelationService.class);
        CacheRwdLightingStrategy strategy = new CacheRwdLightingStrategy(
                sourceStrategy, writeService, relationService);
        LightingCircuitDetailDTO circuit = new LightingCircuitDetailDTO();
        circuit.setId("circuit-source");
        List<LightingCircuitDetailDTO> circuits = Collections.singletonList(circuit);
        Mockito.when(sourceStrategy.listCacheModelSourceCircuits(Mockito.any()))
                .thenReturn(circuits);
        CacheWriteAction relationAction = Mockito.mock(CacheWriteAction.class);
        Mockito.when(relationService.prepareSaveActionFromCircuitRelations(
                        Mockito.anyString(), Mockito.anyCollection()))
                .thenReturn(relationAction);
        Mockito.when(writeService.replaceModelCacheKey(
                        Mockito.anyString(), Mockito.anyCollection(),
                        Mockito.anyCollection()))
                .thenReturn(true);

        boolean result = strategy.buildCacheData(RunningMonitorTypeEnum.LIGHTING);

        Assertions.assertTrue(result);
        Mockito.verify(relationService, Mockito.times(5))
                .prepareSaveActionFromCircuitRelations(
                        Mockito.anyString(), Mockito.eq(circuits));
        Mockito.verify(writeService, Mockito.times(5)).replaceModelCacheKey(
                Mockito.anyString(),
                Mockito.eq(circuits),
                Mockito.eq(Collections.singletonList(relationAction)));
    }
}
