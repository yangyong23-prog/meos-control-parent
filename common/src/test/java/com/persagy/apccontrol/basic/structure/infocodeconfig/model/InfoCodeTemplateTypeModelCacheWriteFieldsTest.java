package com.persagy.apccontrol.basic.structure.infocodeconfig.model;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheInfoCodeTemplateRepository;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 模板缓存写入查询字段边界测试。
 *
 * <p>验证 {@link InfoCodeTemplateTypeModel#queryTemplateObjectsForCacheWrite()} 按模板
 * classCode 组装完整白名单字段；并验证跨类返回对象识别逻辑，防字段子集写入共享对象行。</p>
 *
 * @author JiYinchuan
 * @since v7.260802.3
 */
public class InfoCodeTemplateTypeModelCacheWriteFieldsTest {

    /**
     * 模型类实际持有的物理世界查询服务 mock。
     *
     * @since v7.260802.3
     */
    private static DtpRwdClientService dtpRwdClientService;

    /**
     * 模型类实际持有的新缓存对象模型支持范围服务 mock。
     *
     * @since v7.260802.3
     */
    private static CacheObjectModelStatusService cacheObjectModelStatusService;

    /**
     * 原始 Spring 上下文，测试后恢复。
     *
     * @since v7.260802.3
     */
    private static ApplicationContext originalApplicationContext;

    /**
     * 注册模型类加载所需的全部 Bean，并读取模型类实际持有的静态依赖实例用于打桩。
     *
     * @since v7.260802.3
     */
    @BeforeAll
    public static void setUpSpringUtil() throws IllegalAccessException {
        originalApplicationContext = SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("objectMapper", new ObjectMapper());
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
                Mockito.mock(CacheInfoCodeTemplateRepository.class));
        applicationContext.getBeanFactory().registerSingleton("cacheObjectModelStatusService",
                Mockito.mock(CacheObjectModelStatusService.class));
        applicationContext.refresh();
        new SpringUtil().setApplicationContext(applicationContext);
        dtpRwdClientService = (DtpRwdClientService) FieldUtils.readStaticField(
                InfoCodeTemplateTypeModel.class, "dtpRwdClientService", true);
        cacheObjectModelStatusService = (CacheObjectModelStatusService) FieldUtils.readStaticField(
                InfoCodeTemplateTypeModel.class, "CACHE_OBJECT_MODEL_STATUS_SERVICE", true);
    }

    /**
     * 恢复原始 Spring 上下文。
     *
     * @since v7.260802.3
     */
    @AfterAll
    public static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(originalApplicationContext);
    }

    /**
     * 清理每个用例之间的 mock 状态。
     *
     * @since v7.260802.3
     */
    @BeforeEach
    public void setUp() {
        Mockito.reset(dtpRwdClientService, cacheObjectModelStatusService);
    }

    /**
     * 清理项目上下文。
     *
     * @since v7.260802.3
     */
    @AfterEach
    public void tearDown() {
        ContextUtil.unloadContext();
    }

    /**
     * 验证缓存写入查询按模板 classCode 组装完整白名单字段。
     *
     * @since v7.260802.3
     */
    @Test
    public void shouldQueryFullFieldsForCacheWriteWithTemplateClassCode() {
        ContextUtil.setProjectId("project-1");
        Mockito.when(cacheObjectModelStatusService.isSupported(RunningMonitorTypeEnum.LIGHTING))
                .thenReturn(true);
        DtpRwdObjectDTO circuit = new DtpRwdObjectDTO();
        circuit.setId("circuit-1");
        circuit.setClassCode(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value());
        Mockito.when(dtpRwdClientService.listObject(Mockito.any()))
                .thenReturn(Collections.singletonList(circuit));
        InfoCodeTemplateTypeModel templateTypeModel = this.buildTemplateTypeModel();

        List<DtpRwdObjectDTO> objects = templateTypeModel.queryTemplateObjectsForCacheWrite();

        Assertions.assertEquals(1, objects.size());
        ArgumentCaptor<DtpRwdObjectQueryParam> queryCaptor =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(dtpRwdClientService).listObject(queryCaptor.capture());
        Set<String> withColumns = new LinkedHashSet<>(queryCaptor.getValue().getWithColumns());
        Assertions.assertTrue(withColumns.containsAll(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(
                        DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value())));
    }

    /**
     * 验证跨类返回对象识别：仅返回非模板 classCode 的对象，空入参返回空列表。
     *
     * @since v7.260802.3
     */
    @Test
    public void shouldDetectCrossClassObjects() {
        DtpRwdObjectDTO circuit = new DtpRwdObjectDTO();
        circuit.setId("circuit-1");
        circuit.setClassCode(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value());
        DtpRwdObjectDTO camera = new DtpRwdObjectDTO();
        camera.setId("camera-1");
        camera.setClassCode(DtpRwdClassCodeEnum.SPVSCM.value());

        List<DtpRwdObjectDTO> crossClassObjects = InfoCodeTemplateTypeModel.listCrossClassObjects(
                DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value(), Arrays.asList(circuit, camera));

        Assertions.assertEquals(1, crossClassObjects.size());
        Assertions.assertEquals("camera-1", crossClassObjects.get(0).getId());
        Assertions.assertTrue(InfoCodeTemplateTypeModel.listCrossClassObjects(
                DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value(),
                Collections.singletonList(circuit)).isEmpty());
        Assertions.assertTrue(InfoCodeTemplateTypeModel.listCrossClassObjects(
                DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value(),
                Collections.emptyList()).isEmpty());
    }

    /**
     * 构建开启缓存的照明列表模板模型。
     *
     * @return 模板模型
     * @since v7.260802.3
     */
    private InfoCodeTemplateTypeModel buildTemplateTypeModel() {
        InfoCodeTemplateTypeModel result = new InfoCodeTemplateTypeModel();
        result.setTemplateTypeId(1L);
        result.setRunningMonitorType(RunningMonitorTypeEnum.LIGHTING);
        result.setTemplateType(InfoCodeTemplateTypeEnum.TABLE_LIST);
        result.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        result.setClassCode(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value());
        return result;
    }
}
