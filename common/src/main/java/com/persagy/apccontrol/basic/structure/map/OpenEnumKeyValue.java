package com.persagy.apccontrol.basic.structure.map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 枚举键值对Map
 *
 * @author JiYinchuan
 * @see OpenEnum
 * @since v5.8.0
 */
@Component
public class OpenEnumKeyValue implements EnvironmentAware, ResourceLoaderAware, InitializingBean {

    private static final String ENUM_PACKAGE = "com.persagy.apccontrol.basic.model.enumtype";
    private static final ClassPathScanningCandidateComponentProvider SCANNER =
            new ClassPathScanningCandidateComponentProvider(false);
    private static final ResourcePatternResolver RESOURCE_RESOLVER = new PathMatchingResourcePatternResolver();
    private static final Map<String, List<BaseEnumVO>> OPEN_ENUMS = new HashMap<>(16);

    /**
     * 获取枚举常量
     *
     * @param key 枚举Key
     * @return 枚举常量
     * @since v5.8.0
     */
    public List<BaseEnumVO> getEnumConstants(@NonNull String key) {
        if (StringUtils.equals(key, DtpRwdSpaceDTO.Fields.roomFuncType)) {
            return analyzeRoomFuncType();
        }
        return Optional.ofNullable(OPEN_ENUMS.get(key)).orElseGet(ArrayList::new);
    }

    /**
     * 解析空间功能类型
     *
     * @return 空间功能类型
     * @since v5.8.0
     */
    @SneakyThrows
    private List<BaseEnumVO> analyzeRoomFuncType() {
        URL jsonUrl = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + "DtpRwdSpaceFuncTypeEnum.json");
        String fileString = IOUtils.toString(jsonUrl.openStream(), StandardCharsets.UTF_8);
        List<BaseEnumVO> result = new ArrayList<>();
        JSONArray defineDataSourceList = JSONObject.parseObject(fileString).getJSONArray("defineDataSourceList");
        for (int i = 0; i < defineDataSourceList.size(); i++) {
            deepAnalyzeRoomFuncType(result, defineDataSourceList.getJSONObject(i));
        }
        return result;
    }

    /**
     * 递归解析空间功能类型
     *
     * @param target     目标源
     * @param jsonObject JSON对象
     * @since v5.8.0
     */
    private void deepAnalyzeRoomFuncType(@NonNull List<BaseEnumVO> target, @NonNull JSONObject jsonObject) {
        String enumKey = jsonObject.getString("enumKey");
        String enumValue = jsonObject.getString("enumValue");
        JSONArray children = jsonObject.getJSONArray("children");
        target.add(new BaseEnumVO(enumKey, enumValue));
        if (CollectionUtils.isNotEmpty(children)) {
            for (int i = 0; i < children.size(); i++) {
                deepAnalyzeRoomFuncType(target, children.getJSONObject(i));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        SCANNER.addIncludeFilter(new AndTypeFilter(Lists.newArrayList(
                new AssignableTypeFilter(Enum.class),
                new AssignableTypeFilter(BaseEnum.class),
                new AnnotationTypeFilter(OpenEnum.class)
        )));
        Set<BeanDefinition> definitions = SCANNER.findCandidateComponents(ENUM_PACKAGE);
        if (CollectionUtils.isEmpty(definitions)) {
            return;
        }
        for (BeanDefinition definition : definitions) {
            Class<?> clazz = Class.forName(definition.getBeanClassName());
            OpenEnum openEnum = clazz.getAnnotation(OpenEnum.class);
            String openEnumName = openEnum.value();
            boolean serializeAdditionalValue = openEnum.serializeAdditionalValue();
            if (StringUtils.isBlank(openEnumName)) {
                openEnumName = clazz.getSimpleName();
            }
            if (OPEN_ENUMS.containsKey(openEnumName)) {
                throw new IllegalArgumentException("@OpenEnum name must be unique value");
            }
            List<BaseEnumVO> values = new ArrayList<>();
            for (Object enumConstant : clazz.getEnumConstants()) {
                BaseEnum<Object> baseEnum = (BaseEnum<Object>) enumConstant;
                values.add(new BaseEnumVO(
                        baseEnum.value(),
                        baseEnum.getReasonPhrase(),
                        serializeAdditionalValue ? baseEnum.additionalMapValue() : null)
                );
            }
            OPEN_ENUMS.put(openEnumName, values);
        }
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        SCANNER.setResourceLoader(resourceLoader);
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        SCANNER.setEnvironment(environment);
    }

    /**
     * 逻辑与过滤器
     *
     * @author JiYinchuan
     * @since v5.8.0
     */
    static class AndTypeFilter implements TypeFilter {

        private final List<TypeFilter> filters;

        public AndTypeFilter(List<TypeFilter> filters) {
            this.filters = filters;
        }

        @Override
        public boolean match(
                @NonNull MetadataReader metadataReader,
                @NonNull MetadataReaderFactory metadataReaderFactory)
                throws IOException {
            for (TypeFilter filter : filters) {
                if (!filter.match(metadataReader, metadataReaderFactory)) {
                    return false;
                }
            }
            return true;
        }
    }
}
