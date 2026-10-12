package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.entity.cache.CacheInfoCodeTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheInfoCodeTemplateService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheInfoCodeTemplateQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheTemplateRebuildService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.i18n.I18nProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * 信息点模板缓存全量与窄范围重建边界测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheInfoCodeTemplateRepositoryImplTest {

    /**
     * 原始 Spring 上下文, 测试后恢复。
     *
     * @since v7.260802.0
     */
    private static ApplicationContext originalApplicationContext;

    /**
     * 注册模板模型与国际化工具类加载所需的全部 Bean。
     *
     * @since v7.260802.0
     */
    @BeforeAll
    public static void setUpSpringUtil() {
        originalApplicationContext = SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("objectMapper", new ObjectMapper());
        applicationContext.getBeanFactory().registerSingleton("i18nProperties",
                CacheInfoCodeTemplateRepositoryImplTest.i18nProperties());
        applicationContext.getBeanFactory().registerSingleton("dtpRwdConvert",
                Mockito.mock(DtpRwdConvert.class));
        applicationContext.getBeanFactory().registerSingleton("dtpRwdClientService",
                Mockito.mock(DtpRwdClientService.class));
        applicationContext.getBeanFactory().registerSingleton("abstractIotDataCacheServer",
                Mockito.mock(AbstractIotDataCacheServer.class));
        applicationContext.getBeanFactory().registerSingleton("infoCodeGateway",
                Mockito.mock(InfoCodeGateway.class));
        applicationContext.getBeanFactory().registerSingleton("redisCacheTemplate",
                Mockito.mock(RedisCacheTemplate.class));
        applicationContext.getBeanFactory().registerSingleton("cacheInfoCodeTemplateRepository",
                Mockito.mock(com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheInfoCodeTemplateRepository.class));
        applicationContext.getBeanFactory().registerSingleton("cacheObjectModelStatusService",
                Mockito.mock(CacheObjectModelStatusService.class));
        applicationContext.refresh();
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * 恢复原始 Spring 上下文。
     *
     * @since v7.260802.0
     */
    @AfterAll
    public static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(originalApplicationContext);
    }

    /**
     * 构建国际化配置。
     *
     * @return 国际化配置
     * @since v7.260802.0
     */
    private static I18nProperties i18nProperties() {
        I18nProperties result = Mockito.mock(I18nProperties.class);
        Mockito.when(result.getI18n()).thenReturn("zh-CN");
        return result;
    }

    /**
     * 构建已注入项目缓存锁服务的仓储。
     *
     * @param infoCodeGateway 信息点网关
     * @param templateService 模板元数据服务
     * @param rebuildService  模板重建服务
     * @param locker          分布式锁服务
     * @return 仓储
     * @since v7.260802.0
     */
    private CacheInfoCodeTemplateRepositoryImpl buildRepository(
            InfoCodeGateway infoCodeGateway,
            CacheInfoCodeTemplateService templateService,
            CacheTemplateRebuildService rebuildService,
            RedissonDistributedLockerService locker) {
        return Mockito.spy(new CacheInfoCodeTemplateRepositoryImpl(
                infoCodeGateway, templateService, rebuildService, locker));
    }

    /**
     * 构建可成功获取锁并直接执行动作的分布式锁服务。
     *
     * @return 分布式锁服务
     * @since v7.260802.0
     */
    private RedissonDistributedLockerService mockLockerAvailable() {
        RedissonDistributedLockerService locker =
                Mockito.mock(RedissonDistributedLockerService.class);
        Mockito.doAnswer(invocation -> {
            invocation.getArgument(2, Runnable.class).run();
            return null;
        }).when(locker).executeWithLock(
                Mockito.anyString(), Mockito.anyLong(), Mockito.any(Runnable.class));
        return locker;
    }

    /**
     * 让重建服务在锁外准备阶段直接返回空列表, 避免锁内透传 null。
     *
     * @param rebuildService 模板重建服务
     * @since v7.260901.0
     */
    private void stubPrepareTemplatesEmpty(CacheTemplateRebuildService rebuildService) {
        Mockito.when(rebuildService.prepareTemplates(Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());
    }

    /**
     * 单个一级运行监测类型全量重建为空时应删除该类型上一版全部缓存键。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldRemovePreviousKeysWhenFullRunningMonitorScopeIsEmpty() throws InterruptedException {
        InfoCodeGateway infoCodeGateway = Mockito.mock(InfoCodeGateway.class);
        CacheInfoCodeTemplateService templateService =
                Mockito.mock(CacheInfoCodeTemplateService.class);
        CacheTemplateRebuildService rebuildService =
                Mockito.mock(CacheTemplateRebuildService.class);
        this.stubPrepareTemplatesEmpty(rebuildService);
        CacheInfoCodeTemplateRepositoryImpl repository = this.buildRepository(
                infoCodeGateway, templateService, rebuildService, this.mockLockerAvailable());
        CacheInfoCodeTemplate previousTemplate = new CacheInfoCodeTemplate();
        previousTemplate.setCustomKey("previous-template-key");
        Mockito.when(infoCodeGateway.listTemplateTypes(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.doReturn(Collections.singletonList(previousTemplate))
                .when(repository).list(Mockito.any(CacheInfoCodeTemplateQueryDaoParam.class));
        InfoCodeTemplateTypeQueryDaoParam queryParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryParam.setRunningMonitorType(RunningMonitorTypeEnum.AIR_TERMINAL);

        repository.rebuildByParam(queryParam);

        // 新两段式实现：锁外快照与锁内复核各查询一次元数据键集合。
        ArgumentCaptor<CacheInfoCodeTemplateQueryDaoParam> existingParamCaptor =
                ArgumentCaptor.forClass(CacheInfoCodeTemplateQueryDaoParam.class);
        Mockito.verify(repository, Mockito.times(2)).list(existingParamCaptor.capture());
        for (CacheInfoCodeTemplateQueryDaoParam existingParam : existingParamCaptor.getAllValues()) {
            Assertions.assertEquals(RunningMonitorTypeEnum.AIR_TERMINAL,
                    existingParam.getRunningMonitorType());
            Assertions.assertNull(existingParam.getRelTemplateIds());
        }
        Mockito.verify(rebuildService).prepareTemplates(Collections.emptyList());
        Mockito.verify(rebuildService).executePreparedTemplates(
                Collections.emptyList(), Collections.singleton("previous-template-key"));
        Mockito.verify(templateService).replaceByCacheKeys(
                Collections.singleton("previous-template-key"), Collections.emptyList());
    }

    /**
     * 带设备类筛选的重建为空时不能按一级类型清理其他模板缓存。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldKeepPreviousKeysWhenNarrowScopeIsEmpty() throws InterruptedException {
        InfoCodeGateway infoCodeGateway = Mockito.mock(InfoCodeGateway.class);
        CacheInfoCodeTemplateService templateService =
                Mockito.mock(CacheInfoCodeTemplateService.class);
        CacheTemplateRebuildService rebuildService =
                Mockito.mock(CacheTemplateRebuildService.class);
        CacheInfoCodeTemplateRepositoryImpl repository = this.buildRepository(
                infoCodeGateway, templateService, rebuildService, this.mockLockerAvailable());
        Mockito.when(infoCodeGateway.listTemplateTypes(Mockito.any()))
                .thenReturn(Collections.emptyList());
        InfoCodeTemplateTypeQueryDaoParam queryParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryParam.setRunningMonitorType(RunningMonitorTypeEnum.AIR_TERMINAL);
        queryParam.setClassCodes(Collections.singleton("ACATFC"));

        repository.rebuildByParam(queryParam);

        Mockito.verify(repository, Mockito.never())
                .list(Mockito.any(CacheInfoCodeTemplateQueryDaoParam.class));
        Mockito.verifyNoInteractions(templateService, rebuildService);
    }

    /**
     * 项目缓存锁获取超时时应明确失败, 不执行任何重建写入。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldThrowWhenProjectLockWaitTimeout() {
        InfoCodeGateway infoCodeGateway = Mockito.mock(InfoCodeGateway.class);
        CacheInfoCodeTemplateService templateService =
                Mockito.mock(CacheInfoCodeTemplateService.class);
        CacheTemplateRebuildService rebuildService =
                Mockito.mock(CacheTemplateRebuildService.class);
        this.stubPrepareTemplatesEmpty(rebuildService);
        RedissonDistributedLockerService locker =
                Mockito.mock(RedissonDistributedLockerService.class);
        Mockito.doThrow(new BusinessException(
                        com.persagy.framework.common.exception.enums.ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
                        "获取分布式锁超时"))
                .when(locker).executeWithLock(
                        Mockito.anyString(), Mockito.anyLong(), Mockito.any(Runnable.class));
        CacheInfoCodeTemplateRepositoryImpl repository = this.buildRepository(
                infoCodeGateway, templateService, rebuildService, locker);
        Mockito.when(infoCodeGateway.listTemplateTypes(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.doReturn(Collections.emptyList())
                .when(repository).list(Mockito.any(CacheInfoCodeTemplateQueryDaoParam.class));
        InfoCodeTemplateTypeQueryDaoParam queryParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryParam.setRunningMonitorType(RunningMonitorTypeEnum.AIR_TERMINAL);

        // 锁外准备正常完成, 失败发生在锁内提交阶段（等锁超时）:
        // 仅锁外快照查询一次元数据键集合, 锁内复核与写入均不执行。
        Assertions.assertThrows(BusinessException.class,
                () -> repository.rebuildByParam(queryParam));

        Mockito.verify(repository, Mockito.times(1))
                .list(Mockito.any(CacheInfoCodeTemplateQueryDaoParam.class));
        Mockito.verify(rebuildService).prepareTemplates(Collections.emptyList());
        Mockito.verify(rebuildService, Mockito.never()).executePreparedTemplates(
                Mockito.anyList(), Mockito.anyCollection());
        Mockito.verifyNoInteractions(templateService);
    }

    /**
     * 锁外准备期间模板键集合发生变化时应放弃本次写入, 防止误删并发新增的模板键。
     *
     * @since v7.260901.0
     */
    @Test
    public void shouldSkipWriteWhenPrepareSnapshotChanged() {
        InfoCodeGateway infoCodeGateway = Mockito.mock(InfoCodeGateway.class);
        CacheInfoCodeTemplateService templateService =
                Mockito.mock(CacheInfoCodeTemplateService.class);
        CacheTemplateRebuildService rebuildService =
                Mockito.mock(CacheTemplateRebuildService.class);
        this.stubPrepareTemplatesEmpty(rebuildService);
        CacheInfoCodeTemplateRepositoryImpl repository = this.buildRepository(
                infoCodeGateway, templateService, rebuildService, this.mockLockerAvailable());
        Mockito.when(infoCodeGateway.listTemplateTypes(Mockito.any()))
                .thenReturn(Collections.singletonList(this.buildTableListModel(1L)));
        // 锁外快照为空, 锁内复核发现并发新增模板键（add 事件在准备期间提交）。
        Mockito.doReturn(Collections.emptyList(),
                Collections.singletonList(this.buildCachedTemplate("LIGHTING--TABLE_LIST-LTLTCC-")))
                .when(repository).list(Mockito.any(CacheInfoCodeTemplateQueryDaoParam.class));
        InfoCodeTemplateTypeQueryDaoParam queryParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryParam.setRunningMonitorType(RunningMonitorTypeEnum.LIGHTING);

        repository.rebuildByParam(queryParam);

        Mockito.verify(rebuildService).prepareTemplates(
                Mockito.anyCollection());
        Mockito.verify(rebuildService, Mockito.never()).executePreparedTemplates(
                Mockito.anyList(), Mockito.anyCollection());
        Mockito.verifyNoInteractions(templateService);
    }

    /**
     * 构建指定缓存键的模板元数据。
     *
     * @param customKey 缓存键
     * @return 模板元数据
     * @since v7.260901.0
     */
    private CacheInfoCodeTemplate buildCachedTemplate(String customKey) {
        CacheInfoCodeTemplate template = new CacheInfoCodeTemplate();
        template.setCustomKey(customKey);
        return template;
    }

    /**
     * 同一批次出现相同模板 Key 时插入集合应去重, 防止单批插入重复记录。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldDeduplicateTargetTemplatesBySameKey() throws InterruptedException {
        InfoCodeGateway infoCodeGateway = Mockito.mock(InfoCodeGateway.class);
        CacheInfoCodeTemplateService templateService =
                Mockito.mock(CacheInfoCodeTemplateService.class);
        CacheTemplateRebuildService rebuildService =
                Mockito.mock(CacheTemplateRebuildService.class);
        CacheInfoCodeTemplateRepositoryImpl repository = this.buildRepository(
                infoCodeGateway, templateService, rebuildService, this.mockLockerAvailable());
        InfoCodeTemplateTypeModel firstModel = this.buildTableListModel(1L);
        InfoCodeTemplateTypeModel duplicateModel = this.buildTableListModel(2L);
        Mockito.when(infoCodeGateway.listTemplateTypes(Mockito.any()))
                .thenReturn(Arrays.asList(firstModel, duplicateModel));
        Mockito.doReturn(Collections.emptyList())
                .when(repository).list(Mockito.any(CacheInfoCodeTemplateQueryDaoParam.class));
        InfoCodeTemplateTypeQueryDaoParam queryParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryParam.setRunningMonitorType(RunningMonitorTypeEnum.LIGHTING);

        repository.rebuildByParam(queryParam);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<CacheInfoCodeTemplate>> templatesCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(templateService).replaceByCacheKeys(
                Mockito.anySet(), templatesCaptor.capture());
        Assertions.assertEquals(1, templatesCaptor.getValue().size());
        Assertions.assertEquals("LIGHTING--TABLE_LIST-LTLTCC-",
                templatesCaptor.getValue().iterator().next().getCustomKey());
    }

    /**
     * 构建允许缓存的列表模板模型, 两个实例仅模板 ID 不同, 模板 Key 相同。
     *
     * @param templateTypeId 模板类型ID
     * @return 模板模型
     * @since v7.260802.0
     */
    private InfoCodeTemplateTypeModel buildTableListModel(long templateTypeId) {
        InfoCodeTemplateTypeModel model = new InfoCodeTemplateTypeModel();
        model.setTemplateTypeId(templateTypeId);
        model.setRunningMonitorType(RunningMonitorTypeEnum.LIGHTING);
        model.setTemplateType(InfoCodeTemplateTypeEnum.TABLE_LIST);
        model.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        model.setClassCode("LTLTCC");
        return model;
    }
}
