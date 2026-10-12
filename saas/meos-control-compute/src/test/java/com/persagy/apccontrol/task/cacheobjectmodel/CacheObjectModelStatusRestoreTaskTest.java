package com.persagy.apccontrol.task.cacheobjectmodel;

import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.cache.CacheObjectModelStatusEnum;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.common.outside.MidPermissionService;
import com.persagy.apccontrol.common.outside.dto.GroupDTO;
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
 * 云端缓存对象模型 READY 状态恢复任务测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheObjectModelStatusRestoreTaskTest {

    /**
     * 验证云端启动恢复任务的条件注册属性。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldDeclareExpectedConditionalRegistration() {
        ConditionalOnProperty condition = CacheObjectModelStatusRestoreTask.class
                .getAnnotation(ConditionalOnProperty.class);

        Assertions.assertNotNull(condition);
        Assertions.assertEquals("project.cache.rwd", condition.prefix());
        Assertions.assertArrayEquals(new String[]{"enabled"}, condition.name());
        Assertions.assertEquals("true", condition.havingValue());
        Assertions.assertTrue(condition.matchIfMissing());
    }

    /**
     * 验证开关关闭时云端启动恢复任务 Bean 不注册。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldNotRegisterRestoreTaskWhenDisabled() {
        new ApplicationContextRunner()
                .withPropertyValues("project.cache.rwd.enabled=false")
                .withUserConfiguration(CacheObjectModelStatusRestoreTask.class)
                .run(context -> Assertions.assertTrue(
                        context.getBeansOfType(CacheObjectModelStatusRestoreTask.class).isEmpty()));
    }

    /**
     * 验证只对 Redis 缺失的一级类型批量查库并以 SETNX 写入 READY。
     *
     * @since v7.260701.0
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldQueryOnlyMissingTypesAndWriteReadyIfAbsent() {
        MidPermissionService permissionService = Mockito.mock(MidPermissionService.class);
        CacheObjectModelStatusService statusService = Mockito.mock(CacheObjectModelStatusService.class);
        RedisCacheTemplate redisCacheTemplate = Mockito.mock(RedisCacheTemplate.class);
        RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        GroupDTO group = new GroupDTO();
        group.setCode("group-1");
        ProjectDTO project = new ProjectDTO();
        project.setProjectId("project-1");
        Mockito.when(permissionService.queryValidGroupList()).thenReturn(Collections.singletonList(group));
        Mockito.when(permissionService.queryIsInUseProjectDTOList("group-1"))
                .thenReturn(Collections.singletonList(project));
        Mockito.when(statusService.supportedRunningMonitorTypes()).thenReturn(
                new LinkedHashSet<>(Arrays.asList(
                        RunningMonitorTypeEnum.AIR_TERMINAL,
                        RunningMonitorTypeEnum.LIGHTING)));
        Mockito.when(redisCacheTemplate.hasKey(Mockito.argThat(
                key -> key != null && key.contains(RunningMonitorTypeEnum.AIR_TERMINAL.value()))))
                .thenReturn(true);
        Mockito.when(redisCacheTemplate.hasKey(Mockito.argThat(
                key -> key != null && key.contains(RunningMonitorTypeEnum.LIGHTING.value()))))
                .thenReturn(false);
        Mockito.when(statusService.getPersistedStatuses(Mockito.anyCollection())).thenReturn(
                Collections.singletonMap(
                        RunningMonitorTypeEnum.LIGHTING, CacheObjectModelStatusEnum.READY));
        Mockito.when(redisCacheTemplate.delegate()).thenReturn(redisTemplate);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.eq("READY")))
                .thenReturn(true);
        CacheObjectModelStatusRestoreTask task = new CacheObjectModelStatusRestoreTask(
                permissionService, statusService, redisCacheTemplate);

        task.run();

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

    /**
     * 验证两个 Redis Key 均存在时不再读取数据库或写入状态。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldSkipDatabaseWhenAllRedisKeysExist() {
        MidPermissionService permissionService = Mockito.mock(MidPermissionService.class);
        CacheObjectModelStatusService statusService = Mockito.mock(CacheObjectModelStatusService.class);
        RedisCacheTemplate redisCacheTemplate = Mockito.mock(RedisCacheTemplate.class);
        GroupDTO group = new GroupDTO();
        group.setCode("group-1");
        ProjectDTO project = new ProjectDTO();
        project.setProjectId("project-1");
        Mockito.when(permissionService.queryValidGroupList()).thenReturn(Collections.singletonList(group));
        Mockito.when(permissionService.queryIsInUseProjectDTOList("group-1"))
                .thenReturn(Collections.singletonList(project));
        Mockito.when(statusService.supportedRunningMonitorTypes()).thenReturn(
                new LinkedHashSet<>(Arrays.asList(
                        RunningMonitorTypeEnum.AIR_TERMINAL,
                        RunningMonitorTypeEnum.LIGHTING)));
        Mockito.when(redisCacheTemplate.hasKey(Mockito.anyString())).thenReturn(true);
        CacheObjectModelStatusRestoreTask task = new CacheObjectModelStatusRestoreTask(
                permissionService, statusService, redisCacheTemplate);

        task.run();

        Mockito.verify(statusService, Mockito.never()).getPersistedStatuses(Mockito.anyCollection());
        Mockito.verify(redisCacheTemplate, Mockito.never()).delegate();
    }
}
