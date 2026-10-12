package com.persagy.apccontrol.task.cacheobjectmodel.param;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleContext;
import com.persagy.framework.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 缓存对象模型初始化任务参数测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheObjectModelTaskParamTest {

    /**
     * 当前版本固定支持类型。
     *
     * @since v7.260701.0
     */
    private static final Set<RunningMonitorTypeEnum> SUPPORTED_TYPES =
            new LinkedHashSet<>(Arrays.asList(
                    RunningMonitorTypeEnum.AIR_TERMINAL,
                    RunningMonitorTypeEnum.LIGHTING));

    /**
     * 用于替换项目级 ObjectMapper 的反射字段。
     *
     * @since v7.260701.0
     */
    private Field objectMapperField;

    /**
     * 测试前保存的项目级 ObjectMapper 实例。
     *
     * @since v7.260701.0
     */
    private Object originalObjectMapper;

    /**
     * 为纯单元测试安装独立 ObjectMapper。
     *
     * @throws Exception 反射访问项目级 ObjectMapper 字段失败时抛出
     * @since v7.260701.0
     */
    @BeforeEach
    public void setUp() throws Exception {
        LocaleContext.setLocaleContext(Locale.CHINESE);
        this.objectMapperField = ObjectMapperUtils.class.getDeclaredField("objectMapper");
        this.objectMapperField.setAccessible(true);
        this.originalObjectMapper = this.objectMapperField.get(null);
        this.objectMapperField.set(null, new ObjectMapper());
    }

    /**
     * 恢复测试前的项目级 ObjectMapper。
     *
     * @throws Exception 反射恢复项目级 ObjectMapper 字段失败时抛出
     * @since v7.260701.0
     */
    @AfterEach
    public void tearDown() throws Exception {
        this.objectMapperField.set(null, this.originalObjectMapper);
        LocaleContext.remove();
    }

    /**
     * 验证空任务参数默认遍历全部项目并处理末端和照明。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldUseSupportedTypesForBlankJobParam() {
        CacheObjectModelTaskParam result = CacheObjectModelTaskParam.parse(
                null, CacheObjectModelTaskParamTest.SUPPORTED_TYPES);

        Assertions.assertNull(result.getGroupCodes());
        Assertions.assertNull(result.getProjectIds());
        Assertions.assertEquals(CacheObjectModelTaskParamTest.SUPPORTED_TYPES,
                result.getRunningMonitorTypes());
    }

    /**
     * 验证指定集团、项目和单个一级类型时保留输入顺序并规范化文本。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldNormalizeExplicitScope() {
        String json = "{\"groupCodes\":[\" group-1 \"],"
                + "\"projectIds\":[\" project-1 \"],"
                + "\"runningMonitorTypes\":[\"LIGHTING\"]}";

        CacheObjectModelTaskParam result = CacheObjectModelTaskParam.parse(
                json, CacheObjectModelTaskParamTest.SUPPORTED_TYPES);

        Assertions.assertEquals(Collections.singleton("group-1"), result.getGroupCodes());
        Assertions.assertEquals(Collections.singleton("project-1"), result.getProjectIds());
        Assertions.assertEquals(Collections.singleton(RunningMonitorTypeEnum.LIGHTING),
                result.getRunningMonitorTypes());
    }

    /**
     * 验证不支持的运行监测类型和损坏 JSON 均在遍历项目前抛出业务异常。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldRejectUnsupportedTypeAndMalformedJson() {
        Assertions.assertThrows(BusinessException.class,
                () -> CacheObjectModelTaskParam.parse(
                        "{\"runningMonitorTypes\":[\"VENTILATION\"]}",
                        CacheObjectModelTaskParamTest.SUPPORTED_TYPES));
        Assertions.assertThrows(BusinessException.class,
                () -> CacheObjectModelTaskParam.parse(
                        "{broken", CacheObjectModelTaskParamTest.SUPPORTED_TYPES));
    }
}
