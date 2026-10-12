package com.persagy.apccontrol.basic.structure.cacherwd.projection;

import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 缓存对象与模型视图字段投影器。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component
public class CacheObjectExtensionProjector {

    /**
     * 没有可投影字段时使用的标准空 JSON 对象。
     *
     * @since v7.260701.0
     */
    private static final @NonNull String EMPTY_JSON_OBJECT = "{}";

    /**
     * 投影对象扩展字段。
     *
     * @param classCode   设备类编码
     * @param actualFields RWD 实际返回字段
     * @return 对象扩展 JSON；序列化失败时返回 {@code null}
     * @since v7.260701.0
     */
    public @Nullable String projectObject(
            @Nullable String classCode,
            @NonNull Map<String, ?> actualFields) {
        return this.project(CacheObjectExtensionFieldConfig.getObjectFields(classCode), actualFields);
    }

    /**
     * 投影模型视图字段。
     *
     * <p>模型视图字段完全由 classCode 配置决定；投影器只保留 RWD 实际返回的字段，避免为缺失数据
     * 补写默认值。</p>
     *
     * @param classCode    设备类编码
     * @param actualFields RWD 实际返回字段
     * @return 模型视图 JSON；序列化失败时返回 {@code null}
     * @since v7.260701.0
     */
    public @Nullable String projectModelView(
            @Nullable String classCode,
            @NonNull Map<String, ?> actualFields) {
        return this.project(
                CacheObjectExtensionFieldConfig.getModelViewFields(classCode),
                actualFields);
    }

    /**
     * 重建对象 DTO。
     *
     * @param classCode     设备类编码
     * @param commonFields  独立列公共属性
     * @param extensionJson 对象扩展 JSON，SQL NULL 时传入 null
     * @param targetClass   目标类型
     * @param <T>           目标类型
     * @return 重建对象；目标类型无法转换时返回 {@code null}
     * @since v7.260701.0
     */
    public <T> @Nullable T rebuildObject(
            @Nullable String classCode,
            @NonNull Map<String, ?> commonFields,
            @Nullable String extensionJson,
            @NonNull Class<T> targetClass) {
        return this.rebuild(
                CacheObjectExtensionFieldConfig.getObjectFields(classCode),
                commonFields,
                extensionJson,
                targetClass);
    }

    /**
     * 使用独立列、对象 JSON 和调用方字段重建目标对象。
     *
     * <p>对象 JSON 可以为了减少 SQL 复杂度整列读取，但这里只恢复“classCode 存储白名单”和
     * “调用方请求字段”的交集，防止同一缓存记录向不同调用点透传无关字段。</p>
     *
     * @param classCode      设备类编码
     * @param commonFields   已按请求字段筛选的独立列公共属性
     * @param extensionJson  对象扩展 JSON，SQL NULL 时传入 null
     * @param requestedFields 调用方请求的 RWD 字段
     * @param targetClass    目标类型
     * @param <T>            目标类型
     * @return 重建对象；目标类型无法转换时返回 {@code null}
     * @since v7.260701.0
     */
    public <T> @Nullable T rebuildObject(
            @Nullable String classCode,
            @NonNull Map<String, ?> commonFields,
            @Nullable String extensionJson,
            @NonNull Collection<String> requestedFields,
            @NonNull Class<T> targetClass) {
        Set<String> projectedFields = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.getObjectFields(classCode));
        projectedFields.retainAll(requestedFields);
        return this.rebuild(projectedFields, commonFields, extensionJson, targetClass);
    }

    /**
     * 重建模型视图 DTO。
     *
     * @param classCode     设备类编码
     * @param commonFields  独立列公共属性
     * @param extensionJson 模型视图 JSON，SQL NULL 时传入 null
     * @param targetClass   目标类型
     * @param <T>           目标类型
     * @return 重建后的模型视图 DTO；目标类型无法转换时返回 {@code null}
     * @since v7.260701.0
     */
    public <T> @Nullable T rebuildModelView(
            @Nullable String classCode,
            @NonNull Map<String, ?> commonFields,
            @Nullable String extensionJson,
            @NonNull Class<T> targetClass) {
        return this.rebuild(
                CacheObjectExtensionFieldConfig.getModelViewFields(classCode),
                commonFields,
                extensionJson,
                targetClass);
    }

    /**
     * 使用调用方字段重建模型视图对象。
     *
     * @param classCode      设备类编码
     * @param commonFields   已按请求字段筛选的独立列公共属性
     * @param extensionJson  模型视图扩展 JSON，SQL NULL 时传入 null
     * @param requestedFields 调用方请求的 RWD 字段
     * @param targetClass    目标类型
     * @param <T>            目标类型
     * @return 重建对象；目标类型无法转换时返回 {@code null}
     * @since v7.260701.0
     */
    public <T> @Nullable T rebuildModelView(
            @Nullable String classCode,
            @NonNull Map<String, ?> commonFields,
            @Nullable String extensionJson,
            @NonNull Collection<String> requestedFields,
            @NonNull Class<T> targetClass) {
        Set<String> projectedFields = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.getModelViewFields(classCode));
        projectedFields.retainAll(requestedFields);
        return this.rebuild(projectedFields, commonFields, extensionJson, targetClass);
    }

    /**
     * 按允许字段和实际响应字段的交集生成投影 JSON。
     *
     * @param allowedFields 当前投影入口允许写入 JSON 的字段
     * @param actualFields RWD 实际返回字段
     * @return 投影 JSON；无可投影数据时固定返回空对象，序列化失败时返回 {@code null}
     * @since v7.260701.0
     */
    private @Nullable String project(
            @NonNull Set<String> allowedFields,
            @NonNull Map<String, ?> actualFields) {
        if (CollectionUtils.isEmpty(allowedFields) || MapUtils.isEmpty(actualFields)) {
            return CacheObjectExtensionProjector.EMPTY_JSON_OBJECT;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : allowedFields) {
            // containsKey 区分“RWD 未返回”与“RWD 显式返回 null”，不能改成非空值判断。
            if (actualFields.containsKey(field)) {
                result.put(field, actualFields.get(field));
            }
        }
        return ObjectMapperUtils.writeValueAsStringFailSafe(result);
    }

    /**
     * 使用独立列公共属性和字段 JSON 重建目标对象。
     *
     * @param allowedFields 当前重建入口允许从 JSON 恢复的字段
     * @param commonFields 独立列公共属性
     * @param extensionJson 扩展 JSON
     * @param targetClass 目标类型
     * @param <T> 目标类型
     * @return 重建对象；目标类型转换失败时返回 {@code null}
     * @since v7.260701.0
     */
    private <T> @Nullable T rebuild(
            @NonNull Set<String> allowedFields,
            @NonNull Map<String, ?> commonFields,
            @Nullable String extensionJson,
            @NonNull Class<T> targetClass) {
        Map<String, Object> mergedFields = new LinkedHashMap<>();
        Map<String, Object> extensionFields = this.readExtensionFields(extensionJson);
        for (String field : allowedFields) {
            if (extensionFields.containsKey(field)) {
                mergedFields.put(field, extensionFields.get(field));
            }
        }
        // 独立列是公共属性的唯一可信来源，必须最后写入并覆盖历史脏 JSON。
        mergedFields.putAll(commonFields);
        String mergedJson = ObjectMapperUtils.writeValueAsStringFailSafe(mergedFields);
        if (StringUtils.isBlank(mergedJson)) {
            return null;
        }
        return ObjectMapperUtils.readValueFailSafe(mergedJson, targetClass);
    }

    /**
     * 读取字段 JSON。
     *
     * <p>正常无扩展字段持久化为 {@code {}}。SQL NULL、空字符串或无法解析的历史值均按
     * 无扩展字段处理，独立列仍可正常恢复。</p>
     *
     * @param extensionJson 扩展 JSON
     * @return JSON 中的字段；没有可读取字段时返回空 Map
     * @since v7.260701.0
     */
    private @NonNull Map<String, Object> readExtensionFields(@Nullable String extensionJson) {
        if (StringUtils.isBlank(extensionJson)) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = ObjectMapperUtils.readMapFailSafe(extensionJson);
        return result == null ? Collections.emptyMap() : result;
    }
}
