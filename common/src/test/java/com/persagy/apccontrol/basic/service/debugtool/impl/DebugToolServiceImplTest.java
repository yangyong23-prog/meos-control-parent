package com.persagy.apccontrol.basic.service.debugtool.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.vo.debugtool.DebugToolTemplateTypeAndClassCodeVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheInfoCodeTemplateRepository;
import com.persagy.apccontrol.basic.structure.debugTool.converter.DebugToolConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.framework.common.i18n.I18nProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.support.StaticApplicationContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DebugToolServiceImplTest {

    private final DebugToolServiceImpl service = new DebugToolServiceImpl(null, null, null, null, null);

    @BeforeAll
    public static void setUpSpringUtil() {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("objectMapper", new ObjectMapper());
        applicationContext.getBeanFactory().registerSingleton("i18nProperties", DebugToolServiceImplTest.i18nProperties());
        applicationContext.getBeanFactory().registerSingleton("dtpRwdConvert", Mockito.mock(DtpRwdConvert.class));
        applicationContext.getBeanFactory().registerSingleton("dtpRwdClientService", Mockito.mock(DtpRwdClientService.class));
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

    @Test
    public void shouldMergeObjectShowStyleAndPreferDebugToolFields() throws Exception {
        String originalShowStyle = "{\"color\":\"blue\",\"font\":{\"size\":12,\"weight\":\"normal\"}}";
        String debugToolShowStyle = "{\"font\":{\"weight\":\"bold\"},\"icon\":\"snow\"}";

        String result = this.mergeShowStyle(originalShowStyle, debugToolShowStyle);
        JsonNode resultNode = new ObjectMapper().readTree(result);

        Assertions.assertEquals("blue", resultNode.get("color").asText());
        Assertions.assertEquals(12, resultNode.get("font").get("size").asInt());
        Assertions.assertEquals("bold", resultNode.get("font").get("weight").asText());
        Assertions.assertEquals("snow", resultNode.get("icon").asText());
    }

    @Test
    public void shouldMergeArrayShowStyleAndPreferDebugToolDuplicateItem() throws Exception {
        String originalShowStyle = "[{\"infoCode\":\"temp\",\"name\":\"温度\",\"color\":\"blue\"},{\"infoCode\":\"press\",\"name\":\"压力\"}]";
        String debugToolShowStyle = "[{\"infoCode\":\"temp\",\"name\":\"温度调试\",\"color\":\"red\"},{\"infoCode\":\"flow\",\"name\":\"流量\"}]";

        String result = this.mergeShowStyle(originalShowStyle, debugToolShowStyle);
        JsonNode resultNode = new ObjectMapper().readTree(result);

        Assertions.assertEquals(3, resultNode.size());
        Assertions.assertEquals("temp", resultNode.get(0).get("infoCode").asText());
        Assertions.assertEquals("red", resultNode.get(0).get("color").asText());
        Assertions.assertEquals("press", resultNode.get(1).get("infoCode").asText());
        Assertions.assertEquals("flow", resultNode.get(2).get("infoCode").asText());
    }

    @Test
    public void shouldPreferDebugToolShowStyleWhenJsonCannotBeMerged() throws Exception {
        String result = this.mergeShowStyle("origin-style", "debug-style");

        Assertions.assertEquals("debug-style", result);
    }

    @Test
    public void shouldHideAssociatedDebugToolRunningMonitorTypeWhenListingTemplateTypes() {
        DtpKnowledgeClientService dtpKnowledgeClientService = Mockito.mock(DtpKnowledgeClientService.class);
        InfoCodeGateway infoCodeGateway = Mockito.mock(InfoCodeGateway.class);
        DebugToolServiceImpl service = new DebugToolServiceImpl(
                dtpKnowledgeClientService, null, infoCodeGateway, null, Mockito.mock(DebugToolConverter.class));
        InfoCodeTemplateTypeModel coldSourceTemplateType = this.buildTemplateTypeModel(
                1L, RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE, "ACCS");
        InfoCodeTemplateTypeModel lightingTemplateType = this.buildTemplateTypeModel(
                2L, RunningMonitorTypeEnum.LIGHTING, "LIGHT");
        InfoCodeTemplateTypeModel coldSourceDebugToolTemplateType = this.buildTemplateTypeModel(
                3L, RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL, "ACCS");
        Mockito.when(infoCodeGateway.listTemplateTypes(Mockito.any(InfoCodeTemplateTypeQueryDaoParam.class)))
                .thenReturn(Arrays.asList(
                        coldSourceTemplateType, lightingTemplateType, coldSourceDebugToolTemplateType));
        Mockito.when(dtpKnowledgeClientService.mapClassCodeAndName(
                        Mockito.eq(false), Mockito.eq(false), Mockito.anySet()))
                .thenReturn(Collections.emptyMap());

        Collection<DebugToolTemplateTypeAndClassCodeVO> result = service.listTemplateTypeAndClassCodes();

        Map<RunningMonitorTypeEnum, DebugToolTemplateTypeAndClassCodeVO> runningMonitorTypeAndResult =
                result.stream().collect(Collectors.toMap(
                        DebugToolTemplateTypeAndClassCodeVO::getRunningMonitorType,
                        Function.identity()));
        Assertions.assertTrue(runningMonitorTypeAndResult.containsKey(
                RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE));
        Assertions.assertTrue(runningMonitorTypeAndResult.containsKey(RunningMonitorTypeEnum.LIGHTING));
        Assertions.assertFalse(runningMonitorTypeAndResult.containsKey(
                RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL));
    }

    @Test
    public void shouldQueryDefaultTemplateTypeWhenMergingAssociatedDebugToolTemplateType() throws Exception {
        InfoCodeGateway infoCodeGateway = Mockito.mock(InfoCodeGateway.class);
        DebugToolServiceImpl service = new DebugToolServiceImpl(null, null, infoCodeGateway, null, null);
        InfoCodeTemplateTypeModel coldSourceTemplateType = this.buildTemplateTypeModel(
                1L, RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE, "ACCS", InfoCodeTemplateTypeEnum.GRID);
        InfoCodeTemplateTypeModel coldSourceDebugToolTemplateType = this.buildTemplateTypeModel(
                3L, RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL, "ACCS");
        Mockito.when(infoCodeGateway.listTemplateTypes(Mockito.any(InfoCodeTemplateTypeQueryDaoParam.class)))
                .thenAnswer(invocation -> {
                    InfoCodeTemplateTypeQueryDaoParam queryDaoParam = invocation.getArgument(0);
                    Assertions.assertEquals(RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL,
                            queryDaoParam.getRunningMonitorType());
                    Assertions.assertEquals(InfoCodeTemplateTypeEnum.DEFAULT, queryDaoParam.getTemplateType());
                    Assertions.assertNull(queryDaoParam.getTemplateTypes());
                    Assertions.assertEquals(Collections.singleton(DtpRwdObjectTypeEnum.EQUIPMENT),
                            queryDaoParam.getObjectTypes());
                    Assertions.assertEquals("ACCS", queryDaoParam.getClassCode());
                    return Collections.singletonList(coldSourceDebugToolTemplateType);
                });

        Set<Long> result = this.listMergedTemplateTypeIds(service,
                new LinkedHashSet<>(Collections.singletonList(1L)),
                Collections.singletonList(coldSourceTemplateType),
                RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL,
                "ACCS");

        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList(1L, 3L)), result);
    }

    private String mergeShowStyle(String originalShowStyle, String debugToolShowStyle) throws Exception {
        Method method = DebugToolServiceImpl.class.getDeclaredMethod(
                "mergeShowStyle", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(this.service, originalShowStyle, debugToolShowStyle);
    }

    @SuppressWarnings("unchecked")
    private Set<Long> listMergedTemplateTypeIds(DebugToolServiceImpl service,
                                                Set<Long> templateTypeIds,
                                                Collection<InfoCodeTemplateTypeModel> templateTypeModels,
                                                RunningMonitorTypeEnum debugToolRunningMonitorType,
                                                String classCode) throws Exception {
        Method method = DebugToolServiceImpl.class.getDeclaredMethod(
                "listMergedTemplateTypeIds", Set.class, Collection.class, RunningMonitorTypeEnum.class, String.class);
        method.setAccessible(true);
        return (Set<Long>) method.invoke(service, templateTypeIds, templateTypeModels, debugToolRunningMonitorType,
                classCode);
    }

    private InfoCodeTemplateTypeModel buildTemplateTypeModel(Long templateTypeId,
                                                             RunningMonitorTypeEnum runningMonitorType,
                                                             String classCode) {
        return this.buildTemplateTypeModel(templateTypeId, runningMonitorType, classCode,
                InfoCodeTemplateTypeEnum.DEFAULT);
    }

    private InfoCodeTemplateTypeModel buildTemplateTypeModel(Long templateTypeId,
                                                             RunningMonitorTypeEnum runningMonitorType,
                                                             String classCode,
                                                             InfoCodeTemplateTypeEnum templateType) {
        InfoCodeTemplateTypeModel result = new InfoCodeTemplateTypeModel();
        result.setTemplateTypeId(templateTypeId);
        result.setRunningMonitorType(runningMonitorType);
        result.setTemplateType(templateType);
        result.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        result.setClassCode(classCode);
        return result;
    }

    private static I18nProperties i18nProperties() {
        I18nProperties result = Mockito.mock(I18nProperties.class);
        Mockito.when(result.getI18n()).thenReturn("zh-CN");
        return result;
    }
}
