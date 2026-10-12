package com.persagy.apccontrol.basic.structure.common;

import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirTerminalStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

/**
 * 末端运行监测白名单空间的实时与新版缓存路由测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class RunningMonitorAirTerminalWhitelistRoutingTest {

    /**
     * 验证业务缓存开启且末端 READY 时只读取白名单新对象缓存键。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReadNewWhitelistObjectsWhenReady() {
        DtpRwdClientService dtpRwdClientService = Mockito.mock(DtpRwdClientService.class);
        RedisCacheTemplate redisCacheTemplate = Mockito.mock(RedisCacheTemplate.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository cacheObjectRepository = Mockito.mock(CacheObjectRepository.class);
        RunningMonitorAirTerminalStrategy strategy = new RunningMonitorAirTerminalStrategy(
                dtpRwdClientService,
                redisCacheTemplate,
                statusService,
                cacheObjectRepository);
        String whitelistKey = CacheObjectModelKeyConfig.AIR_TERMINAL_WHITELIST_SPACE;
        DtpRwdSpaceDTO expectedSpace = new DtpRwdSpaceDTO();
        expectedSpace.setId("space-1");
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.AIR_TERMINAL))
                .thenReturn(true);
        Mockito.when(cacheObjectRepository.listByCacheKey(
                        Mockito.eq(whitelistKey),
                        Mockito.eq(DtpRwdSpaceDTO.class),
                        Mockito.<String>anyCollection()))
                .thenReturn(Collections.singletonList(expectedSpace));
        RunningMonitorSpaceQueryParam param =
                Mockito.mock(RunningMonitorSpaceQueryParam.class);
        Mockito.when(param.isMeosCache()).thenReturn(true);
        Mockito.when(param.getWithColumns()).thenReturn(Collections.emptySet());

        List<DtpRwdSpaceDTO> result = strategy.listWhitelistSpaces(param);

        Assertions.assertEquals(Collections.singletonList(expectedSpace), result);
        Mockito.verifyNoInteractions(dtpRwdClientService);
    }

    /**
     * 验证关闭业务缓存后，仍按新版白名单 Key 的配置 ID 从 RWD 查询最新空间。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldQueryRwdWhenRebuildDisablesMeosCache() {
        DtpRwdClientService dtpRwdClientService = Mockito.mock(DtpRwdClientService.class);
        RedisCacheTemplate redisCacheTemplate = Mockito.mock(RedisCacheTemplate.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository cacheObjectRepository = Mockito.mock(CacheObjectRepository.class);
        RunningMonitorAirTerminalStrategy strategy = new RunningMonitorAirTerminalStrategy(
                dtpRwdClientService,
                redisCacheTemplate,
                statusService,
                cacheObjectRepository);
        String whitelistKey = CacheObjectModelKeyConfig.AIR_TERMINAL_WHITELIST_SPACE;
        DtpRwdSpaceDTO expectedSpace = new DtpRwdSpaceDTO();
        expectedSpace.setId("space-1");
        Mockito.when(cacheObjectRepository.listObjectIdsByCacheKey(whitelistKey))
                .thenReturn(Collections.singletonList("space-1"));
        RunningMonitorSpaceQueryParam param =
                Mockito.mock(RunningMonitorSpaceQueryParam.class);
        Mockito.when(param.queryObject(Mockito.<DtpRwdObjectQueryParam>any()))
                .thenReturn(Collections.singletonList(expectedSpace));

        List<DtpRwdSpaceDTO> result = strategy.listWhitelistSpaces(param);

        Assertions.assertEquals(Collections.singletonList(expectedSpace), result);
        Mockito.verify(cacheObjectRepository).listObjectIdsByCacheKey(whitelistKey);
        Mockito.verify(param).queryObject(Mockito.argThat(queryParam ->
                queryParam.getId().size() == 1
                        && queryParam.getId().contains("space-1")));
        Mockito.verifyNoInteractions(statusService);
        Mockito.verifyNoInteractions(dtpRwdClientService);
    }
}
