package com.persagy.apccontrol.basic.structure.cacherwd.projection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.exception.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * 缓存模板对象明细记录转换器。
 *
 * <p>负责在有序 RWD 明细列表与持久化 JSON 字符串之间转换，并在写入前从所有
 * 关联对象层级移除仅在请求期间有效的信息点分组配置。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component
public class CacheTemplateObjectDetailRecordConverter {

    /**
     * 模板对象明细列表反序列化类型描述。
     *
     * @since v7.260701.0
     */
    private static final @NonNull TypeReference<List<InfoCodeRwdObjectDetailDTO>>
            RESULT_TYPE_REFERENCE =
            new TypeReference<List<InfoCodeRwdObjectDetailDTO>>() {
            };

    /**
     * 关联对象 JSON 字段名。
     *
     * @since v7.260701.0
     */
    private static final @NonNull String REL_OBJECTS_FIELD = "relObjects";

    /**
     * 请求态关联信息点分组配置 JSON 字段名。
     *
     * @since v7.260701.0
     */
    private static final @NonNull String REL_INFO_CODE_GROUP_CONFIGS_FIELD =
            "relInfoCodeGroupConfigs";

    /**
     * RWD 对象 ID 的 JSON 字段名。
     *
     * @since v7.260701.0
     */
    private static final @NonNull String OBJECT_ID_FIELD = "id";

    /**
     * 将有序模板对象明细结果转换为待持久化记录。
     *
     * @param templateKey 模板键
     * @param objectId    当前信息点聚合的根对象 ID
     * @param infoCode    信息点编码
     * @param source      有序模板对象明细结果，空集合将保存为 {@code []}
     * @return 待持久化模板对象明细记录
     * @throws BusinessException 结果无法转换、JSON 结构非法或序列化失败时抛出
     * @since v7.260701.0
     */
    public @NonNull CacheTemplateObjectDetail toDetail(
            @NonNull String templateKey,
            @NonNull String objectId,
            @NonNull String infoCode,
            @NonNull List<InfoCodeRwdObjectDetailDTO> source) {
        String sourceJson = ObjectMapperUtils.writeValueAsStringFailSafe(source);
        ArrayNode resultTree = StringUtils.isBlank(sourceJson)
                ? null
                : ObjectMapperUtils.readValueFailSafe(sourceJson, ArrayNode.class);
        if (resultTree == null) {
            throw CacheObjectModelExceptionFactory.invalidStructure(
                    "cacheTemplateObjectDetail.resultJson");
        }
        // fail-safe 序列化与反序列化生成独立树，字段删除不会污染调用方仍在使用的 DTO。
        this.removeRequestScopedConfigs(resultTree);
        String resultJson = ObjectMapperUtils.writeValueAsStringFailSafe(resultTree);
        if (StringUtils.isBlank(resultJson)) {
            throw CacheObjectModelExceptionFactory.invalidStructure(
                    "cacheTemplateObjectDetail.resultJson");
        }
        CacheTemplateObjectDetail detail = new CacheTemplateObjectDetail();
        detail.setTemplateKey(templateKey);
        detail.setObjectId(objectId);
        detail.setInfoCode(infoCode);
        detail.setResultJson(resultJson);
        return detail;
    }

    /**
     * 将持久化记录恢复为有序模板对象明细结果。
     *
     * @param detail 模板对象明细记录
     * @return 有序模板对象明细结果；持久化值为 {@code []} 时返回空列表，JSON 无法转换时返回
     *         {@code null}
     * @since v7.260701.0
     */
    public @Nullable List<InfoCodeRwdObjectDetailDTO> toResult(
            @NonNull CacheTemplateObjectDetail detail) {
        return ObjectMapperUtils.readValueFailSafe(
                detail.getResultJson(),
                CacheTemplateObjectDetailRecordConverter.RESULT_TYPE_REFERENCE);
    }

    /**
     * 迭代移除根对象及任意深度关联对象中的请求态信息点配置。
     *
     * <p>显式队列避免深关系链造成递归栈溢出；遍历入口只允许 {@code relObjects}，
     * 从而保留空间、楼层、建筑及其他嵌套业务字段的原始结构。</p>
     *
     * @param rootNodes 根对象 JSON 数组
     * @throws BusinessException 根元素或关联对象不是对象，或关联对象字段不是数组时抛出
     * @since v7.260701.0
     */
    private void removeRequestScopedConfigs(@NonNull ArrayNode rootNodes) {
        Deque<ObjectNode> pendingNodes = new ArrayDeque<>();
        for (JsonNode rootNode : rootNodes) {
            if (!rootNode.isObject()) {
                throw CacheObjectModelExceptionFactory.invalidStructure(
                        "cacheTemplateObjectDetail.resultJson[]");
            }
            pendingNodes.addLast((ObjectNode) rootNode);
        }
        while (CollectionUtils.isNotEmpty(pendingNodes)) {
            ObjectNode currentNode = pendingNodes.removeFirst();
            JsonNode objectIdNode = currentNode.get(
                    CacheTemplateObjectDetailRecordConverter.OBJECT_ID_FIELD);
            if (objectIdNode == null
                    || !objectIdNode.isTextual()
                    || StringUtils.isBlank(objectIdNode.textValue())) {
                throw CacheObjectModelExceptionFactory.requiredValue(
                        "cacheTemplateObjectDetail.resultJson.id");
            }
            currentNode.remove(
                    CacheTemplateObjectDetailRecordConverter.REL_INFO_CODE_GROUP_CONFIGS_FIELD);
            // 只有 relObjects 表达递归关联对象，其他嵌套对象必须作为业务结果原样保留。
            JsonNode relObjectsNode = currentNode.get(
                    CacheTemplateObjectDetailRecordConverter.REL_OBJECTS_FIELD);
            if (relObjectsNode == null || relObjectsNode.isNull()) {
                continue;
            }
            if (!relObjectsNode.isArray()) {
                throw CacheObjectModelExceptionFactory.invalidStructure(
                        "cacheTemplateObjectDetail.resultJson.relObjects");
            }
            for (JsonNode relObjectNode : relObjectsNode) {
                if (!relObjectNode.isObject()) {
                    throw CacheObjectModelExceptionFactory.invalidStructure(
                            "cacheTemplateObjectDetail.resultJson.relObjects[]");
                }
                pendingNodes.addLast((ObjectNode) relObjectNode);
            }
        }
    }

}
