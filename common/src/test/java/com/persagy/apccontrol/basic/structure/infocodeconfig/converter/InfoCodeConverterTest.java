package com.persagy.apccontrol.basic.structure.infocodeconfig.converter;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigTemplateTypeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.lang.reflect.Proxy;
import java.util.*;

public class InfoCodeConverterTest {

    private final InfoCodeConverter infoCodeConverter = new InfoCodeConverterImpl();

    @BeforeAll
    public static void setUpSpringUtil() {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("dtpRwdClientService",
                noopClient(DtpRwdClientService.class));
        applicationContext.getBeanFactory().registerSingleton("dtpKnowledgeClientService",
                noopClient(DtpKnowledgeClientService.class));
        applicationContext.refresh();
        new SpringUtil().setApplicationContext(applicationContext);
    }

    @Test
    public void shouldMapExcludeClassCodesToTemplateQueryDaoParam() {
        Set<String> excludeClassCodes = new LinkedHashSet<>(Arrays.asList("classA", "classB"));
        InfoCodeConfigTemplateTypeQueryParam param = new InfoCodeConfigTemplateTypeQueryParam();
        param.setExcludeClassCodes(excludeClassCodes);

        InfoCodeTemplateTypeQueryDaoParam queryDaoParam = infoCodeConverter.toTemplateQueryDaoParam(param);

        Assertions.assertEquals(excludeClassCodes, new LinkedHashSet<>(queryDaoParam.getExcludeClassCodes()));
    }

    @Test
    public void shouldMapExcludeClassCodesThroughObjectQueryParam() {
        Set<String> excludeClassCodes = new LinkedHashSet<>(Arrays.asList("classA", "classB"));
        InfoCodeConfigTemplateTypeQueryParam param = new InfoCodeConfigTemplateTypeQueryParam();
        param.setExcludeClassCodes(excludeClassCodes);

        InfoCodeObjectQueryParam objectQueryParam = infoCodeConverter.toTemplateQueryParam(param);
        InfoCodeTemplateTypeQueryDaoParam queryDaoParam = infoCodeConverter.toTemplateQueryDaoParam(objectQueryParam);

        Assertions.assertEquals(excludeClassCodes, new LinkedHashSet<>(objectQueryParam.getExcludeClassCodes()));
        Assertions.assertEquals(excludeClassCodes, new LinkedHashSet<>(queryDaoParam.getExcludeClassCodes()));
    }

    @SuppressWarnings("unchecked")
    private static <T> T noopClient(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            Class<?> returnType = method.getReturnType();
            if (Boolean.TYPE.equals(returnType)) {
                return false;
            }
            if (Integer.TYPE.equals(returnType) || Long.TYPE.equals(returnType)) {
                return 0;
            }
            if (Map.class.isAssignableFrom(returnType)) {
                return Collections.emptyMap();
            }
            if (Iterable.class.isAssignableFrom(returnType)) {
                return Collections.emptyList();
            }
            return null;
        });
    }
}
