package com.persagy.apccontrol.basic.model.entity.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheInfoCodeTemplateRepository;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeFilterValueObject;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

/**
 * {@link CacheInfoCodeTemplate#from(InfoCodeTemplateTypeModel)} 转换测试。
 *
 * <p>验证主查询参数缺失的解析结果不固化进缓存快照，正常解析结果原样保留。</p>
 *
 * @author JiYinchuan
 * @since v7.260801.0
 */
public class CacheInfoCodeTemplateFromTest {

    /**
     * 测试模板唯一 Key。
     *
     * @since v7.260801.0
     */
    private static final String UNIQUE_KEY = "TEST-FROM-001";

    /**
     * 原始 Spring 上下文，测试后恢复。
     *
     * @since v7.260801.0
     */
    private static ApplicationContext originalApplicationContext;

    /**
     * 注册模型类加载所需的全部 Bean，避免 mock 模型类时静态初始化失败。
     *
     * @since v7.260801.0
     */
    @BeforeAll
    public static void setUpSpringUtil() {
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
     * 初始化当前项目上下文。
     *
     * @since v7.260801.0
     */
    @BeforeEach
    public void setUp() {
        ContextUtil.setProjectId("project-1");
    }

    /**
     * 清理当前项目上下文。
     *
     * @since v7.260801.0
     */
    @AfterEach
    public void tearDown() {
        ContextUtil.remove();
    }

    /**
     * 解析结果主查询参数缺失时, 应写入空 dataJson 避免坏快照固化。
     *
     * @since v7.260801.0
     */
    @Test
    public void shouldWriteNullDataJsonWhenQueryParamMissing() {
        InfoCodeTemplateTypeModel templateTypeModel = Mockito.mock(InfoCodeTemplateTypeModel.class);
        Mockito.when(templateTypeModel.getTemplateTypeKey()).thenReturn(UNIQUE_KEY);
        Mockito.when(templateTypeModel.getRunningMonitorType()).thenReturn(RunningMonitorTypeEnum.LIGHTING);
        Mockito.when(templateTypeModel.getTemplateTypeId()).thenReturn(101L);
        InfoCodeFilterValueObject dataJson = new InfoCodeFilterValueObject();
        dataJson.setUniqueKey(UNIQUE_KEY);
        // 主查询参数缺失的解析结果
        Mockito.when(templateTypeModel.parseFilterValueObject(true)).thenReturn(dataJson);

        CacheInfoCodeTemplate result = CacheInfoCodeTemplate.from(templateTypeModel);

        Assertions.assertNull(result.getDataJson());
    }

    /**
     * 解析结果整体为空时, 应写入空 dataJson。
     *
     * @since v7.260801.0
     */
    @Test
    public void shouldWriteNullDataJsonWhenParseResultMissing() {
        InfoCodeTemplateTypeModel templateTypeModel = Mockito.mock(InfoCodeTemplateTypeModel.class);
        Mockito.when(templateTypeModel.getTemplateTypeKey()).thenReturn(UNIQUE_KEY);
        Mockito.when(templateTypeModel.getRunningMonitorType()).thenReturn(RunningMonitorTypeEnum.LIGHTING);
        Mockito.when(templateTypeModel.getTemplateTypeId()).thenReturn(101L);
        Mockito.when(templateTypeModel.parseFilterValueObject(true)).thenReturn(null);

        CacheInfoCodeTemplate result = CacheInfoCodeTemplate.from(templateTypeModel);

        Assertions.assertNull(result.getDataJson());
    }

    /**
     * 解析结果主查询参数存在时, 应原样保留 dataJson。
     *
     * @since v7.260801.0
     */
    @Test
    public void shouldKeepDataJsonWhenQueryParamPresent() {
        InfoCodeTemplateTypeModel templateTypeModel = Mockito.mock(InfoCodeTemplateTypeModel.class);
        Mockito.when(templateTypeModel.getTemplateTypeKey()).thenReturn(UNIQUE_KEY);
        Mockito.when(templateTypeModel.getRunningMonitorType()).thenReturn(RunningMonitorTypeEnum.LIGHTING);
        Mockito.when(templateTypeModel.getTemplateTypeId()).thenReturn(101L);
        InfoCodeFilterValueObject dataJson = new InfoCodeFilterValueObject();
        dataJson.setUniqueKey(UNIQUE_KEY);
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId("Eq001");
        dataJson.setQueryParam(queryParam);
        Mockito.when(templateTypeModel.parseFilterValueObject(true)).thenReturn(dataJson);

        CacheInfoCodeTemplate result = CacheInfoCodeTemplate.from(templateTypeModel);

        Assertions.assertNotNull(result.getDataJson());
        Assertions.assertSame(dataJson, result.getDataJson());
    }
}
