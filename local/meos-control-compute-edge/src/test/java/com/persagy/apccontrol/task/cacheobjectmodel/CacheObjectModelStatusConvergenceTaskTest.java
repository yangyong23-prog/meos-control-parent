package com.persagy.apccontrol.task.cacheobjectmodel;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.cache.CacheObjectModelStatusEnum;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * 边缘缓存对象模型 READY 状态收敛任务测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheObjectModelStatusConvergenceTaskTest {

    /**
     * 验证边缘状态收敛任务的条件注册属性。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldDeclareExpectedConditionalRegistration() {
        ConditionalOnProperty condition = CacheObjectModelStatusConvergenceTask.class
                .getAnnotation(ConditionalOnProperty.class);

        Assertions.assertNotNull(condition);
        Assertions.assertEquals("project.cache.rwd", condition.prefix());
        Assertions.assertArrayEquals(new String[]{"enabled"}, condition.name());
        Assertions.assertEquals("true", condition.havingValue());
        Assertions.assertTrue(condition.matchIfMissing());
    }

    /**
     * 验证开关关闭时边缘启动和调度收敛任务 Bean 不注册。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldNotRegisterConvergenceTaskWhenDisabled() {
        new ApplicationContextRunner()
                .withPropertyValues("project.cache.rwd.enabled=false")
                .withUserConfiguration(CacheObjectModelStatusConvergenceTask.class)
                .run(context -> Assertions.assertTrue(
                        context.getBeansOfType(CacheObjectModelStatusConvergenceTask.class).isEmpty()));
    }

    /**
     * 验证边缘端只批量查询 Redis 缺失类型，以 SETNX 写入 READY 后下一轮不再查数据库。
     *
     * @since v7.260701.0
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldConvergeOnlyMissingReadyTypes() {
        ProjectCfg projectCfg = Mockito.mock(ProjectCfg.class);
        CacheObjectModelStatusService statusService = Mockito.mock(CacheObjectModelStatusService.class);
        RedisCacheTemplate redisCacheTemplate = Mockito.mock(RedisCacheTemplate.class);
        RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        Mockito.when(projectCfg.getGroupCode()).thenReturn("group-1");
        Mockito.when(projectCfg.getProjectId()).thenReturn("project-1");
        Mockito.when(statusService.supportedRunningMonitorTypes()).thenReturn(
                new LinkedHashSet<>(Arrays.asList(
                        RunningMonitorTypeEnum.AIR_TERMINAL,
                        RunningMonitorTypeEnum.LIGHTING)));
        Mockito.when(redisCacheTemplate.hasKey(Mockito.argThat(
                key -> key != null && key.contains(RunningMonitorTypeEnum.AIR_TERMINAL.value()))))
                .thenReturn(true);
        Mockito.when(redisCacheTemplate.hasKey(Mockito.argThat(
                key -> key != null && key.contains(RunningMonitorTypeEnum.LIGHTING.value()))))
                .thenReturn(false, true);
        Mockito.when(statusService.getPersistedStatuses(Mockito.anyCollection())).thenReturn(
                Collections.singletonMap(
                        RunningMonitorTypeEnum.LIGHTING, CacheObjectModelStatusEnum.READY));
        Mockito.when(redisCacheTemplate.delegate()).thenReturn(redisTemplate);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.eq("READY")))
                .thenReturn(true);
        CacheObjectModelStatusConvergenceTask task = new CacheObjectModelStatusConvergenceTask(
                projectCfg, statusService, redisCacheTemplate);

        task.init();
        task.scheduledConverge();

        ArgumentCaptor<Collection<RunningMonitorTypeEnum>> typeCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(statusService).getPersistedStatuses(typeCaptor.capture());
        Assertions.assertEquals(Collections.singleton(RunningMonitorTypeEnum.LIGHTING),
                new LinkedHashSet<>(typeCaptor.getValue()));
        Mockito.verify(valueOperations).setIfAbsent(
                Mockito.argThat(key -> key.contains(RunningMonitorTypeEnum.LIGHTING.value())),
                Mockito.eq("READY"));
        Mockito.verify(valueOperations, Mockito.never()).setIfAbsent(
                Mockito.argThat(key -> key.contains(RunningMonitorTypeEnum.AIR_TERMINAL.value())),
                Mockito.any());
    }
}
