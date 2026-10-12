package com.persagy.apccontrol.basic.resource.cache.impl;

import com.persagy.apccontrol.basic.config.cache.provider.CacheObjectOperation;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.cache.CacheObjectModelStatusEnum;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 缓存对象模型固定支持范围和共享 READY 状态测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheObjectModelStatusServiceImplTest {

    /**
     * 清理测试设置的集团和项目上下文，避免污染其他用例。
     *
     * @since v7.260701.0
     */
    @AfterEach
    public void tearDown() {
        ContextUtil.unloadContext();
    }

    /**
     * 验证固定支持范围包含末端、照明、通风和安防且集合不可修改。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldExposeOnlyFixedSupportedRunningMonitorTypes() {
        CacheObjectModelStatusServiceImpl statusService =
                new CacheObjectModelStatusServiceImpl(
                        Mockito.mock(RcBasicsConfigService.class),
                        Mockito.mock(RedisCacheTemplate.class));

        Set<RunningMonitorTypeEnum> supportedTypes =
                statusService.supportedRunningMonitorTypes();

        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList(
                RunningMonitorTypeEnum.AIR_TERMINAL,
                RunningMonitorTypeEnum.LIGHTING,
                RunningMonitorTypeEnum.VENTILATION,
                RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY,
                RunningMonitorTypeEnum.SECURITY)), supportedTypes);
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> supportedTypes.add(RunningMonitorTypeEnum.SECURITY));
    }

    /**
     * 验证只有固定支持类型的共享状态为 READY 时才允许读取新缓存模型。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReturnReadyOnlyForSupportedReadyStatus() {
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setTenant("group-1");
        ContextUtil.setProjectId("project-1");
        RedisCacheTemplate redisCacheTemplate = Mockito.mock(RedisCacheTemplate.class);
        CacheObjectOperation objectOperation = Mockito.mock(CacheObjectOperation.class);
        Mockito.when(redisCacheTemplate.object()).thenReturn(objectOperation);
        CacheObjectModelStatusServiceImpl statusService =
                new CacheObjectModelStatusServiceImpl(
                        Mockito.mock(RcBasicsConfigService.class), redisCacheTemplate);

        Mockito.when(objectOperation.get(Mockito.anyString()))
                .thenReturn(CacheObjectModelStatusEnum.READY);
        Assertions.assertTrue(statusService.isReady(RunningMonitorTypeEnum.AIR_TERMINAL));
        Assertions.assertTrue(statusService.isReady(RunningMonitorTypeEnum.SECURITY));

        Mockito.when(objectOperation.get(Mockito.anyString()))
                .thenReturn(CacheObjectModelStatusEnum.FAILED);
        Assertions.assertFalse(statusService.isReady(RunningMonitorTypeEnum.AIR_TERMINAL));
        Assertions.assertFalse(statusService.isReady(RunningMonitorTypeEnum.SECURITY));
        Assertions.assertFalse(statusService.isReady(null));
    }

    /**
     * 验证 Redis 缺失、非法值和访问异常均按未就绪处理，不向业务读取入口抛出异常。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldTreatMissingInvalidAndFailedRedisReadsAsNotReady() {
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setTenant("group-1");
        ContextUtil.setProjectId("project-1");
        RedisCacheTemplate redisCacheTemplate = Mockito.mock(RedisCacheTemplate.class);
        CacheObjectOperation objectOperation = Mockito.mock(CacheObjectOperation.class);
        Mockito.when(redisCacheTemplate.object()).thenReturn(objectOperation);
        CacheObjectModelStatusServiceImpl statusService =
                new CacheObjectModelStatusServiceImpl(
                        Mockito.mock(RcBasicsConfigService.class), redisCacheTemplate);

        Mockito.when(objectOperation.get(Mockito.anyString())).thenReturn(null);
        Assertions.assertFalse(statusService.isReady(RunningMonitorTypeEnum.AIR_TERMINAL));

        Mockito.when(objectOperation.get(Mockito.anyString())).thenReturn("UNKNOWN");
        Assertions.assertFalse(statusService.isReady(RunningMonitorTypeEnum.AIR_TERMINAL));

        Mockito.when(objectOperation.get(Mockito.anyString()))
                .thenThrow(new IllegalStateException("redis unavailable"));
        Assertions.assertFalse(statusService.isReady(RunningMonitorTypeEnum.AIR_TERMINAL));
    }
}
