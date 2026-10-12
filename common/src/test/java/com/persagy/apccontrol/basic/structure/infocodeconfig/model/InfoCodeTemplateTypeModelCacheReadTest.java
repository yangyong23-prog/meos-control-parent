package com.persagy.apccontrol.basic.structure.infocodeconfig.model;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheInfoCodeTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheInfoCodeTemplateRepository;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeFilterValueObject;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Collections;
import java.util.Optional;

/**
 * {@link InfoCodeTemplateTypeModel#parseFilterValueObject()} 缓存读取分支测试。
 *
 * <p>验证主查询参数缺失的缓存快照被视为未命中并回退实时解析，正常快照直接复用。</p>
 *
 * @author JiYinchuan
 * @since v7.260801.0
 */
public class InfoCodeTemplateTypeModelCacheReadTest {

    /**
     * 测试模板唯一 Key。
     *
     * @since v7.260801.0
     */
    private static final String UNIQUE_KEY = "TEST-CACHE-READ-001";

    /**
     * 模型类实际持有的缓存-信息点模版仓储 mock。
     *
     * @since v7.260801.0
     */
    private static CacheInfoCodeTemplateRepository cacheInfoCodeTemplateRepository;

    /**
     * 模型类实际持有的物理世界查询服务 mock。
     *
     * @since v7.260801.0
     */
    private static DtpRwdClientService dtpRwdClientService;

    /**
     * 原始 Spring 上下文，测试后恢复。
     *
     * @since v7.260801.0
     */
    private static ApplicationContext originalApplicationContext;

    /**
     * 注册模型类加载所需的全部 Bean，并读取模型类实际持有的静态依赖实例用于打桩。
     *
     * <p>模型类可能已被同 JVM 内其他测试加载，静态依赖指向其他上下文注册的 mock；
     * 通过反射读取实际实例（只读 final 静态字段），保证打桩目标与运行目标一致。</p>
     *
     * @since v7.260801.0
     */
    @BeforeAll
    public static void setUpSpringUtil() throws IllegalAccessException {
        originalApplicationContext = SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("objectMapper", new ObjectMapper());
        applicationContext.getBeanFactory().registerSingleton("dtpRwdConvert", Mockito.mock(DtpRwdConvert.class));
        applicationContext.getBeanFactory().registerSingleton("dtpRwdClientService",
                Mockito.mock(DtpRwdClientService.class));
        applicationContext.getBeanFactory().registerSingleton("abstractIotDataCacheServer",
                Mockito.mock(AbstractIotDataCacheServer.class));
        applicationContext.getBeanFactory().registerSingleton("infoCodeGateway", Mockito.mock(InfoCodeGateway.class));
        applicationContext.getBeanFactory().registerSingleton("redisCacheTemplate", Mockito.mock(RedisCacheTemplate.class));
        applicationContext.getBeanFactory().registerSingleton("cacheInfoCodeTemplateRepository",
                Mockito.mock(CacheInfoCodeTemplateRepository.class));
        applicationContext.getBeanFactory().registerSingleton("cacheObjectModelStatusService",
                Mockito.mock(CacheObjectModelStatusService.class));
        applicationContext.refresh();
        new SpringUtil().setApplicationContext(applicationContext);
        cacheInfoCodeTemplateRepository = (CacheInfoCodeTemplateRepository) FieldUtils.readStaticField(
                InfoCodeTemplateTypeModel.class, "cacheInfoCodeTemplateRepository", true);
        dtpRwdClientService = (DtpRwdClientService) FieldUtils.readStaticField(
                InfoCodeTemplateTypeModel.class, "dtpRwdClientService", true);
    }

    /**
     * 恢复原始 Spring 上下文。
     *
     * @since v7.260801.0
     */
    @AfterAll
    public static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(originalApplicationContext);
    }

    /**
     * 清理每个用例之间的 mock 状态。
     *
     * @since v7.260801.0
     */
    @BeforeEach
    public void setUp() {
        Mockito.reset(cacheInfoCodeTemplateRepository, dtpRwdClientService);
    }

    /**
     * 缓存快照主查询参数缺失时, 应回退实时解析并返回解析结果。
     *
     * @since v7.260801.0
     */
    @Test
    public void shouldResolveLiveWhenCachedQueryParamMissing() {
        InfoCodeTemplateTypeModel templateTypeModel = this.buildTemplateTypeModel();
        CacheInfoCodeTemplate cachedRow = new CacheInfoCodeTemplate();
        cachedRow.setCustomKey(UNIQUE_KEY);
        InfoCodeFilterValueObject brokenDataJson = new InfoCodeFilterValueObject();
        brokenDataJson.setUniqueKey(UNIQUE_KEY);
        // 主查询参数缺失的历史坏快照, 仅 source 与 uniqueKey 有值
        cachedRow.setDataJson(brokenDataJson);
        Mockito.when(cacheInfoCodeTemplateRepository.getByKeyOpt(UNIQUE_KEY))
                .thenReturn(Optional.of(cachedRow));
        DtpRwdObjectDTO sourceObject = new DtpRwdObjectDTO();
        sourceObject.setId("Eq001");
        Mockito.when(dtpRwdClientService.listObject(Mockito.any()))
                .thenReturn(Collections.singletonList(sourceObject));

        InfoCodeFilterValueObject result = templateTypeModel.parseFilterValueObject();

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getQueryParam());
        Assertions.assertTrue(result.getQueryParam().getId().contains("Eq001"));
        Mockito.verify(dtpRwdClientService, Mockito.atLeastOnce()).listObject(Mockito.any());
    }

    /**
     * 缓存快照主查询参数存在时, 应直接复用缓存且不触发 RWD 查询。
     *
     * @since v7.260801.0
     */
    @Test
    public void shouldReuseCacheWhenQueryParamPresent() {
        InfoCodeTemplateTypeModel templateTypeModel = this.buildTemplateTypeModel();
        CacheInfoCodeTemplate cachedRow = new CacheInfoCodeTemplate();
        cachedRow.setCustomKey(UNIQUE_KEY);
        InfoCodeFilterValueObject validDataJson = new InfoCodeFilterValueObject();
        validDataJson.setUniqueKey(UNIQUE_KEY);
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId("Eq002");
        validDataJson.setQueryParam(queryParam);
        cachedRow.setDataJson(validDataJson);
        Mockito.when(cacheInfoCodeTemplateRepository.getByKeyOpt(UNIQUE_KEY))
                .thenReturn(Optional.of(cachedRow));

        InfoCodeFilterValueObject result = templateTypeModel.parseFilterValueObject();

        Assertions.assertSame(validDataJson, result);
        Mockito.verify(dtpRwdClientService, Mockito.never()).listObject(Mockito.any());
    }

    /**
     * 强制刷新时, 即使缓存快照有效也应实时解析而非复用快照。
     *
     * @since v7.260801.0
     */
    @Test
    public void shouldResolveLiveWhenForceRefreshEvenCacheValid() {
        InfoCodeTemplateTypeModel templateTypeModel = this.buildTemplateTypeModel();
        CacheInfoCodeTemplate cachedRow = new CacheInfoCodeTemplate();
        cachedRow.setCustomKey(UNIQUE_KEY);
        InfoCodeFilterValueObject validDataJson = new InfoCodeFilterValueObject();
        validDataJson.setUniqueKey(UNIQUE_KEY);
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId("Eq002");
        validDataJson.setQueryParam(queryParam);
        cachedRow.setDataJson(validDataJson);
        Mockito.when(cacheInfoCodeTemplateRepository.getByKeyOpt(UNIQUE_KEY))
                .thenReturn(Optional.of(cachedRow));
        DtpRwdObjectDTO sourceObject = new DtpRwdObjectDTO();
        sourceObject.setId("Eq001");
        Mockito.when(dtpRwdClientService.listObject(Mockito.any()))
                .thenReturn(Collections.singletonList(sourceObject));

        InfoCodeFilterValueObject result = templateTypeModel.parseFilterValueObject(true);

        Assertions.assertNotNull(result);
        Assertions.assertNotSame(validDataJson, result);
        Assertions.assertTrue(result.getQueryParam().getId().contains("Eq001"));
        Mockito.verify(dtpRwdClientService, Mockito.atLeastOnce()).listObject(Mockito.any());
    }

    /**
     * 构建开启缓存的照明列表模板模型。
     *
     * @return 模板模型
     * @since v7.260801.0
     */
    private InfoCodeTemplateTypeModel buildTemplateTypeModel() {
        InfoCodeTemplateTypeModel result = new InfoCodeTemplateTypeModel();
        result.setTemplateTypeId(1L);
        result.setRunningMonitorType(RunningMonitorTypeEnum.LIGHTING);
        result.setTemplateType(InfoCodeTemplateTypeEnum.TABLE_LIST);
        result.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        result.setClassCode("LTLTCC");
        result.setInfoCodeFilter(
                "{\"uniqueKey\":\"" + UNIQUE_KEY + "\","
                        + "\"queryParam\":{\"id\":\"${circuitIds}\"},"
                        + "\"source\":{\"circuitIds\":{"
                        + "\"queryParam\":{\"classCode\":\"LTLTCC\",\"withColumns\":[\"id\"]},"
                        + "\"takeValueExpression\":\"id\"}}}");
        return result;
    }
}
