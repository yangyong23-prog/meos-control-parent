package com.persagy.apccontrol.basic.model.dto.rwd;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.structure.common.parser.PropertyDescriptorParser;
import com.persagy.apccontrol.basic.utils.template.SpelUtils;
import com.persagy.framework.common.util.json.JsonUtils;
import com.persagy.validate.utility.executor.MapExecutor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 物理世界对象DTO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@Schema(title = "物理世界对象")
@FieldNameConstants
public class DtpRwdObjectDTO implements PropertyDescriptorParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DtpRwdObjectDTO.class);
    private static final String LOG_TAG = "物理世界对象DTO";

    /**
     * SPEL表达式解析器
     */
    public static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

    // ------------------------- 标准字段 -------------------------

    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "对象名称")
    private String name;

    @Schema(description = "对象本地ID")
    private String localId;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "对象类型")
    private String objType;

    @Schema(description = "类型编码")
    private String classCode;

    @Schema(description = "是否有效")
    private Boolean valid;

//    @Schema(description = "是否末级水泵组")
//    private String isTerminalPump;

    @Nullable
    @Schema(description = "关联对象ID")
    private Set<String> relObjIds;

    @Schema(description = "未定义属性列表")
    @JSONField(unwrapped = true)
    @FieldNameConstants.Exclude
    private Map<String, Object> undefinedProperties;

    // ------------------------- 拓展字段 -------------------------

    @Schema(description = "动态名称")
    @FieldNameConstants.Exclude
    private String dynamicName;

    /**
     * 获取动态名称
     *
     * @return 动态名称
     * @since v5.7.0
     */
    public String getDynamicName() {
        return StringUtils.defaultString(dynamicName, StringUtils.defaultString(localName, name));
    }

    /**
     * 生成关联对象ID和对象名称
     *
     * @param source 数据源
     * @param <T>    数据源元素类型
     * @return 对象ID -> 对象
     * @since 7.250302.0
     */
    public static <T extends DtpRwdObjectDTO> Map<String, T> mapIdAndData(@Nullable Collection<T> source) {
        if (CollectionUtils.isEmpty(source)) {
            return Maps.newLinkedHashMap();
        }
        return source.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId,
                Function.identity(), (o1, o2) -> o1, LinkedHashMap::new));
    }

    /**
     * 生成关联对象ID和对象名称
     *
     * @param source 数据源
     * @param <T>    数据源元素类型
     * @return 关联对象ID -> 对象名称
     * @since 7.250202.0
     */
    public static <T extends DtpRwdObjectDTO> Map<String, String> mapIdAndLocalName(@Nullable Collection<T> source) {
        Map<String, String> result = new HashMap<>();
        callback(source, DtpRwdObjectDTO::getLocalName, (sourceItem, localName) ->
                result.put(sourceItem.getId(), localName));
        return result;
    }

    /**
     * 生成关联对象ID和对象映射
     *
     * @param source 数据源
     * @param <T>    数据源元素类型
     * @return 关联对象ID -> 对象
     * @since 7.250201.0
     */
    public static <T extends DtpRwdObjectDTO> Map<String, List<T>> mapRelObjIdAndData(@Nullable Collection<T> source) {
        Map<String, List<T>> result = new HashMap<>();
        callbackCollection(source, DtpRwdObjectDTO::getRelObjIds, (sourceItem, relObjId) ->
                MapExecutor.dynamicPut(result, relObjId, sourceItem, ArrayList::new));
        return result;
    }

    /**
     * 生成关联对象ID和ID映射
     *
     * @param source 数据源
     * @param <T>    数据源元素类型
     * @return 关联对象ID -> ID
     * @since 5.240502.0
     */
    public static <T extends DtpRwdObjectDTO> Map<String, Set<String>> mapRelObjIdAndIds(@Nullable Collection<T> source) {
        Map<String, Set<String>> result = new HashMap<>();
        callbackCollection(source, DtpRwdObjectDTO::getRelObjIds, (sourceItem, relObjId) ->
                MapExecutor.dynamicPut(result, relObjId, sourceItem.getId(), LinkedHashSet::new));
        return result;
    }

    /**
     * 生成ID和关联对象ID映射
     *
     * @param source 数据源
     * @param <T>    数据源元素类型
     * @return ID -> 关联对象ID
     * @since 5.241202.0
     */
    public static <T extends DtpRwdObjectDTO> Map<String, Set<String>> mapIdAndRelObjIds(@Nullable Collection<T> source) {
        Map<String, Set<String>> result = new HashMap<>();
        callbackCollection(source, DtpRwdObjectDTO::getRelObjIds, (sourceItem, relObjId) ->
                MapExecutor.dynamicPut(result, sourceItem.getId(), relObjId, LinkedHashSet::new));
        return result;
    }

    /**
     * 生成设备类编码和关联对象ID映射
     *
     * @param source 数据源
     * @param <T>    数据源元素类型
     * @return 设备类编码 -> ID
     * @since 7.251002.0
     */
    public static <T extends DtpRwdObjectDTO> Map<String, Set<String>> mapClassCodeAndIds(@Nullable Collection<T> source) {
        Map<String, Set<String>> result = new HashMap<>();
        callback(source, DtpRwdObjectDTO::getClassCode, (sourceItem, classCode) ->
                MapExecutor.dynamicPut(result, classCode, sourceItem.getId(), LinkedHashSet::new));
        return result;
    }

    /**
     * 获取简单字段
     *
     * @return 简单字段
     * @since 7.250201.0
     */
    public static String[] listSimpleWithColumns() {
        return new String[]{
                Fields.id,
                Fields.localName,
                Fields.objType,
                Fields.classCode,
        };
    }

    /**
     * 清除无用的字段
     *
     * @param targets 目标源
     * @return 目标源
     * @since 7.250201.0
     */
    public static <T extends DtpRwdObjectDTO> List<T> clearUselessFieldValueByWithColumns(
            @NonNull List<T> targets, @Nullable Collection<String> withColumns) {
        if (CollectionUtils.isEmpty(withColumns)) {
            return targets;
        }
        for (T target : targets) {
            List<String> undefinedPropertiesFieldNames = new ArrayList<>(target.getUndefinedProperties().keySet());
            List<String> fieldNames = target.cacheFieldNames();

            for (String fieldName : undefinedPropertiesFieldNames) {
                if (!withColumns.contains(fieldName)) {
                    if (!undefinedPropertiesFieldNames.contains(fieldName)) {
                        target.setUndefinedProperties(fieldName, null);
                    }
                }
            }
            for (String fieldName : fieldNames) {
                if (!withColumns.contains(fieldName)) {
                    if (!fieldNames.contains(fieldName)) {
                        target.writePropertyValue(fieldName, null);
                    }
                }
            }
        }
        return targets;
    }

    /**
     * 根据RWD对象解析表达式
     *
     * @param expressionString 表达式字符串
     * @param clazz            结果类型
     * @param <T>              结果类型
     * @return 解析结果
     * @since 5.240602.0
     */
    @Nullable
    public <T> T analyzeExpressionByRwdObject(@NonNull String expressionString, @NonNull Class<T> clazz) {
        Expression expression = SpelUtils.getSpelExpressionParser().parseExpression(expressionString);
        StandardEvaluationContext context = SpelUtils.getDefaultStandardEvaluationContext(this);
        try {
            return expression.getValue(context, clazz);
        } catch (SpelEvaluationException e) {
            context.setRootObject(this.getUndefinedProperties());
            context.addPropertyAccessor(new MapAccessor());
            try {
                return expression.getValue(context, clazz);
            } catch (SpelEvaluationException e1) {
                LogExecutor.project().trace(LOGGER, LOG_TAG, "解析信息点失败", Arrays.asList(
                        Pair.of("expression", expressionString),
                        Pair.of("valueClass", clazz),
                        Pair.of("errorMessage", e1.getLocalizedMessage()),
                        Pair.of("object", this)
                ));
                return null;
            }
        }
    }

    /**
     * 动态解析属性
     *
     * @param name 属性名称
     * @return 属性值
     * @since 7.250902.0
     */
    public String dynamicAnalyzeProperties(@NonNull String name) {
        String property = this.analyzeProperties(name);
        if (StringUtils.isBlank(property)) {
            Object propertyValue = this.readPropertyValue(name);
            if (Objects.nonNull(propertyValue)) {
                property = propertyValue.toString();
            }
        }
        return property;
    }

    /**
     * 解析属性
     *
     * @param name 属性名称
     * @return 属性值
     * @since 7.240102.0
     */
    public String analyzeProperties(@NonNull String name) {
        Object properties = analyzeProperties(name, Object.class);
        return Objects.nonNull(properties) ? properties.toString() : null;
    }

    /**
     * 解析属性
     *
     * @param name 属性名称
     * @param cls  属性类型
     * @param <T>  属性类型
     * @return 属性值
     * @since 5.241101.0
     */
    public <T> T analyzeProperties(@NonNull String name, @NonNull Class<T> cls) {
        if (MapUtils.isEmpty(undefinedProperties)) {
            return null;
        }
        return cls.cast(undefinedProperties.get(name));
    }

    /**
     * 是否可控
     *
     * @return 是否可控
     * @since 5.241101.0
     */
    @JsonIgnore
    public boolean isControlled() {
        if (MapUtils.isEmpty(undefinedProperties)) {
            return false;
        }
        Object monitoringSurveillanceStatus = undefinedProperties.get(
                InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());
        if (Objects.isNull(monitoringSurveillanceStatus)) {
            return false;
        }
        return StringUtils.equalsAny(String.valueOf(monitoringSurveillanceStatus), "2", "3");
    }

    @JsonAnyGetter
    public Map<String, Object> getUndefinedProperties() {
        if (Objects.isNull(this.undefinedProperties)) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>(this.undefinedProperties);
        Arrays.stream(this.getClass().getDeclaredFields()).map(Field::getName).forEach(result::remove);
        return result;
    }

    @JsonAnySetter
    public void setUndefinedProperties(String key, Object value) {
        if (Objects.isNull(undefinedProperties)) {
            undefinedProperties = new HashMap<>(16);
        }
        undefinedProperties.put(key, value);
    }

    /**
     * 返回指定属性的字符串
     *
     * @param fieldName
     * @return
     * @deprecated {@link #analyzeProperties(String)} or {@link #analyzeProperties(String, Class)}
     */
    @Deprecated
    public String getStringUndefinedProperties(String fieldName) {
        Object fieldValue = this.getUndefinedProperties().get(fieldName);
        return fieldValue == null ? null : (String) fieldValue;
    }


    /**
     * 返回指定属性的集合形式
     *
     * @param fieldName
     * @return
     * @deprecated {@link #analyzeProperties(String)} or {@link #analyzeProperties(String, Class)}
     */
    @Deprecated
    public Set<String> getSetUndefinedProperties(String fieldName) {
        Set<String> fieldValues = new HashSet<>();
        try {
            Object fieldValue = this.getUndefinedProperties().get(fieldName);

            if (fieldValue != null && fieldValue instanceof String) {
                fieldValues.add((String) fieldValue);
            } else if (fieldValue != null && fieldValue instanceof List) {
                fieldValues = JsonUtils.fromJson(JsonUtils.toJson(fieldValue), new TypeReference<Set<String>>() {
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fieldValues.size() == 0 ? null : fieldValues;
    }

    /**
     * 回调
     *
     * @param source   数据源
     * @param parser   解析器
     * @param callback 回调
     * @param <T>      数据源元素类型
     * @param <R>      回调元素类型
     * @since 7.251002.0
     */
    private static <T extends DtpRwdObjectDTO, R> void callback(
            @Nullable Collection<T> source,
            @NonNull Function<T, R> parser,
            @NonNull BiConsumer<T, R> callback) {
        if (CollectionUtils.isEmpty(source)) {
            return;
        }
        for (T sourceItem : source) {
            R result = parser.apply(sourceItem);
            if (ObjectUtils.isNotEmpty(result)) {
                callback.accept(sourceItem, result);
            }
        }
    }

    /**
     * 集合回调
     *
     * @param source   数据源
     * @param parser   解析器
     * @param callback 回调
     * @param <T>      数据源元素类型
     * @param <R>      回调元素类型
     * @since 7.251002.0
     */
    private static <T extends DtpRwdObjectDTO, R> void callbackCollection(
            @Nullable Collection<T> source,
            @NonNull Function<T, Collection<R>> parser,
            @NonNull BiConsumer<T, R> callback) {
        callback(source, parser, (sourceItem, result) ->
                result.forEach(item -> callback.accept(sourceItem, item)));
    }
}
