package com.persagy.apccontrol.basic.service.dashboard.space;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.cacherwd.WhitelistTypeEnum;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirDtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheIotRuleRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRuleRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirTerminalStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 末端空间实时查询与新版缓存消费者结果对账测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class AirTerminalQuerySpaceCacheRoutingTest {

    /**
     * 初始化生产代码日志和工具类依赖的最小 Spring 静态上下文。
     *
     * @since v7.260701.0
     */
    @BeforeAll
    public static void setUpSpringUtil() {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton(
                "dtpRwdClientService", Mockito.mock(DtpRwdClientService.class));
        applicationContext.getBeanFactory().registerSingleton(
                "abstractIotDataCacheServer", Mockito.mock(AbstractIotDataCacheServer.class));
        applicationContext.getBeanFactory().registerSingleton(
                "dtpRwdConvert", Mockito.mock(DtpRwdConvert.class));
        applicationContext.getBeanFactory().registerSingleton(
                "objectMapper", new ObjectMapper());
        applicationContext.refresh();
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * 清理集团和项目上下文。
     *
     * @since v7.260701.0
     */
    @AfterEach
    public void tearDown() {
        ContextUtil.unloadContext();
    }

    /**
     * 验证同一空间和规则数据经实时 RWD 与新模型视图路径恢复出一致的业务结果。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReturnSameSpaceAndRuleFlagsFromRealtimeAndReadyPaths() {
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setTenant("group-1");
        ContextUtil.setProjectId("project-1");
        RedisCacheTemplate redisCacheTemplate = Mockito.mock(RedisCacheTemplate.class);
        AbstractIotDataCacheServer<?> iotDataCacheServer =
                Mockito.mock(AbstractIotDataCacheServer.class);
        ShellConfigService shellConfigService = Mockito.mock(ShellConfigService.class);
        RunningMonitorAirTerminalStrategy runningMonitorStrategy =
                Mockito.mock(RunningMonitorAirTerminalStrategy.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        CacheRuleRepository ruleRepository = Mockito.mock(CacheRuleRepository.class);
        CacheIotRuleRepository iotRuleRepository = Mockito.mock(CacheIotRuleRepository.class);
        AirTerminalQuerySpace querySpace = new AirTerminalQuerySpace(
                redisCacheTemplate,
                iotDataCacheServer,
                shellConfigService,
                runningMonitorStrategy,
                statusService,
                objectRepository,
                modelViewRepository,
                modelViewConverter,
                ruleRepository,
                iotRuleRepository);
        DtpRwdSpaceDTO sourceSpace = new DtpRwdSpaceDTO();
        sourceSpace.setId("space-1");
        sourceSpace.setClassCode("Space");
        Map<String, Set<String>> ruleInfoCodes = Collections.singletonMap(
                "space-1", Collections.singleton(DtpRwdSpaceDTO.Fields.tdb));
        Mockito.when(runningMonitorStrategy.listSpaces(Mockito.any()))
                .thenReturn(Collections.singletonList(sourceSpace));
        Mockito.when(iotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                Mockito.eq("project-1"), Mockito.anyMap(), Mockito.anyList()))
                .thenReturn(ruleInfoCodes);

        DemandInstanceValueObject instance = new DemandInstanceValueObject(
                DemandInstanceTypeEnum.PROJECT, "project-1");
        List<AirDtpRwdSpaceVO> realtimeResult = querySpace.querySpace(instance, false);

        Mockito.when(statusService.isReady(
                RunningMonitorTypeEnum.AIR_TERMINAL)).thenReturn(true);
        Mockito.when(objectRepository.listObjectIdsByCacheKey(Mockito.anyString()))
                .thenAnswer(invocation -> CacheObjectModelKeyConfig
                        .AIR_TERMINAL_WHITELIST_SPACE.equals(
                        invocation.getArgument(0))
                        ? Collections.emptyList() : Collections.singletonList("space-1"));
        CacheObjectModelView modelView = new CacheObjectModelView();
        modelView.setId("space-1");
        Mockito.when(modelViewRepository.listModelViews(Mockito.any()))
                .thenReturn(Collections.singletonList(modelView));
        Mockito.when(modelViewConverter.toRwdObject(
                Mockito.same(modelView), Mockito.eq(DtpRwdSpaceDTO.class)))
                .thenReturn(sourceSpace);
        Mockito.when(iotRuleRepository.cacheIotRuleType())
                .thenReturn(CacheIotRuleTypeEnum.PERSISTENCE);
        Mockito.when(ruleRepository.mapInfoCodesByObjectIds(
                Mockito.anyCollection(), Mockito.anyCollection(),
                Mockito.eq(CacheIotRuleTypeEnum.PERSISTENCE)))
                .thenReturn(ruleInfoCodes);

        List<AirDtpRwdSpaceVO> readyResult = querySpace.querySpace(instance, true);

        Assertions.assertEquals(1, realtimeResult.size());
        Assertions.assertEquals(1, readyResult.size());
        Assertions.assertEquals(realtimeResult.get(0).getId(), readyResult.get(0).getId());
        Assertions.assertEquals(realtimeResult.get(0).getTdbRule(), readyResult.get(0).getTdbRule());
        Assertions.assertEquals(realtimeResult.get(0).getCo2Rule(), readyResult.get(0).getCo2Rule());
        Mockito.verify(runningMonitorStrategy, Mockito.times(1)).listSpaces(Mockito.any());
        Mockito.verify(iotDataCacheServer, Mockito.times(1))
                .mapObjectIdAndExistsRuleInfoCodes(
                        Mockito.eq("project-1"), Mockito.anyMap(), Mockito.anyList());
    }

    /**
     * 验证项目配置白名单且末端 READY 时，空间从白名单新模型键读取。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReadWhitelistModelViewWhenAirTerminalIsReady() {
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setTenant("group-1");
        ContextUtil.setProjectId("project-1");
        RedisCacheTemplate redisCacheTemplate = Mockito.mock(RedisCacheTemplate.class);
        AbstractIotDataCacheServer<?> iotDataCacheServer =
                Mockito.mock(AbstractIotDataCacheServer.class);
        ShellConfigService shellConfigService = Mockito.mock(ShellConfigService.class);
        RunningMonitorAirTerminalStrategy runningMonitorStrategy =
                Mockito.mock(RunningMonitorAirTerminalStrategy.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        CacheRuleRepository ruleRepository = Mockito.mock(CacheRuleRepository.class);
        CacheIotRuleRepository iotRuleRepository = Mockito.mock(CacheIotRuleRepository.class);
        AirTerminalQuerySpace querySpace = new AirTerminalQuerySpace(
                redisCacheTemplate,
                iotDataCacheServer,
                shellConfigService,
                runningMonitorStrategy,
                statusService,
                objectRepository,
                modelViewRepository,
                modelViewConverter,
                ruleRepository,
                iotRuleRepository);
        String whitelistKey = CacheObjectModelKeyConfig.AIR_TERMINAL_WHITELIST_SPACE;
        ShellConfigItem configuredItem = new ShellConfigItem();
        configuredItem.setValue(WhitelistTypeEnum.CUSTOM.getShellConfigValue());
        Mockito.when(shellConfigService.queryByInfoCode(
                        Mockito.anyString(), Mockito.any()))
                .thenReturn(configuredItem);
        Mockito.when(statusService.isReady(
                RunningMonitorTypeEnum.AIR_TERMINAL)).thenReturn(true);
        Mockito.when(objectRepository.listObjectIdsByCacheKey(whitelistKey))
                .thenReturn(Collections.singletonList("space-1"));
        CacheObjectModelView modelView = new CacheObjectModelView();
        modelView.setId("space-1");
        Mockito.when(modelViewRepository.listModelViews(Mockito.any()))
                .thenReturn(Collections.singletonList(modelView));
        DtpRwdSpaceDTO sourceSpace = new DtpRwdSpaceDTO();
        sourceSpace.setId("space-1");
        sourceSpace.setClassCode("Space");
        Mockito.when(modelViewConverter.toRwdObject(
                        Mockito.same(modelView), Mockito.eq(DtpRwdSpaceDTO.class)))
                .thenReturn(sourceSpace);
        Mockito.when(iotRuleRepository.cacheIotRuleType())
                .thenReturn(CacheIotRuleTypeEnum.PERSISTENCE);
        Mockito.when(ruleRepository.mapInfoCodesByObjectIds(
                        Mockito.anyCollection(), Mockito.anyCollection(),
                        Mockito.eq(CacheIotRuleTypeEnum.PERSISTENCE)))
                .thenReturn(Collections.emptyMap());

        List<AirDtpRwdSpaceVO> result = querySpace.querySpace(
                new DemandInstanceValueObject(
                        DemandInstanceTypeEnum.PROJECT, "project-1"),
                true);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("space-1", result.get(0).getId());
        Mockito.verify(objectRepository).listObjectIdsByCacheKey(whitelistKey);
        Mockito.verifyNoInteractions(runningMonitorStrategy);
    }
}
