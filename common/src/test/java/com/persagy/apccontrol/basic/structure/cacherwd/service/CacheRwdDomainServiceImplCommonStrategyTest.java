package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRwdObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.CacheRwdStrategy;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存领域服务公共策略调度测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class CacheRwdDomainServiceImplCommonStrategyTest {

    @AfterEach
    public void tearDown() {
        ContextUtil.unloadContext();
    }

    @Test
    public void shouldBuildProjectStructureOnceBeforeSupportedTypeReady() {
        CacheRwdStrategy projectStructure = this.strategy(null, true, true);
        CacheRwdStrategy commonTemplate = this.strategy(null, false, true);
        CacheRwdStrategy security = this.strategy(
                RunningMonitorTypeEnum.SECURITY, false, true);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        Mockito.when(statusService.isSupported(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        CacheRwdDomainServiceImpl service = this.service(
                statusService, projectStructure, commonTemplate, security);

        Assertions.assertTrue(service.buildCache(RunningMonitorTypeEnum.SECURITY));

        Mockito.verify(projectStructure).buildCacheData(RunningMonitorTypeEnum.SECURITY);
        Mockito.verify(commonTemplate).buildCacheData(RunningMonitorTypeEnum.SECURITY);
        Mockito.verify(security).buildCacheData(RunningMonitorTypeEnum.SECURITY);
        Mockito.verify(statusService).markReady(RunningMonitorTypeEnum.SECURITY);
    }

    @Test
    public void shouldNotBuildTypeOrMarkReadyWhenProjectStructureFails() {
        CacheRwdStrategy projectStructure = this.strategy(null, true, false);
        CacheRwdStrategy commonTemplate = this.strategy(null, false, true);
        CacheRwdStrategy security = this.strategy(
                RunningMonitorTypeEnum.SECURITY, false, true);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        Mockito.when(statusService.isSupported(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        CacheRwdDomainServiceImpl service = this.service(
                statusService, projectStructure, commonTemplate, security);

        Assertions.assertFalse(service.buildCache(RunningMonitorTypeEnum.SECURITY));

        Mockito.verify(projectStructure).buildCacheData(RunningMonitorTypeEnum.SECURITY);
        Mockito.verify(commonTemplate, Mockito.never()).buildCacheData(Mockito.any());
        Mockito.verify(security, Mockito.never()).buildCacheData(Mockito.any());
        Mockito.verify(statusService, Mockito.never()).markReady(Mockito.any());
    }

    @Test
    public void shouldBuildProjectStructureOnlyOnceDuringFullBuild() {
        CacheRwdStrategy projectStructure = this.strategy(null, true, true);
        CacheRwdStrategy commonTemplate = this.strategy(null, false, true);
        CacheRwdStrategy security = this.strategy(
                RunningMonitorTypeEnum.SECURITY, false, true);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        Mockito.when(statusService.isSupported(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        CacheRwdDomainServiceImpl service = this.service(
                statusService, projectStructure, commonTemplate, security);

        Assertions.assertTrue(service.buildCache(null));

        Mockito.verify(projectStructure).buildCacheData(null);
        Mockito.verify(commonTemplate).buildCacheData(RunningMonitorTypeEnum.SECURITY);
        Mockito.verify(security).buildCacheData(RunningMonitorTypeEnum.SECURITY);
        Mockito.verify(statusService).markReady(RunningMonitorTypeEnum.SECURITY);
    }

    private CacheRwdDomainServiceImpl service(
            CacheObjectModelStatusService statusService,
            CacheRwdStrategy... strategies) {
        ContextUtil.setProjectId("project-1");
        Map<String, CacheRwdStrategy> strategyBeans = new LinkedHashMap<>();
        for (int index = 0; index < strategies.length; index++) {
            strategyBeans.put("strategy-" + index, strategies[index]);
        }
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        Mockito.when(applicationContext.getBeansOfType(CacheRwdStrategy.class))
                .thenReturn(strategyBeans);
        RedissonDistributedLockerService lockerService =
                Mockito.mock(RedissonDistributedLockerService.class);
        Mockito.when(lockerService.tryLock(
                        Mockito.anyString(), Mockito.eq(TimeUnit.SECONDS),
                        Mockito.eq(0), Mockito.eq(-1)))
                .thenReturn(true);
        CacheRwdDomainServiceImpl service = new CacheRwdDomainServiceImpl(
                Mockito.mock(CacheRwdObjectRepository.class),
                statusService,
                applicationContext);
        ReflectionTestUtils.setField(
                service, "redissonDistributedLockerService", lockerService);
        return service;
    }

    private CacheRwdStrategy strategy(
            RunningMonitorTypeEnum runningMonitorType,
            boolean onlyBuildInCommonPhase,
            boolean success) {
        CacheRwdStrategy strategy = Mockito.mock(CacheRwdStrategy.class);
        Mockito.when(strategy.runningMonitorType()).thenReturn(runningMonitorType);
        Mockito.when(strategy.onlyBuildInCommonPhase())
                .thenReturn(onlyBuildInCommonPhase);
        Mockito.when(strategy.buildCacheData(Mockito.any())).thenReturn(success);
        return strategy;
    }
}
