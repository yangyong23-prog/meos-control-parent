package com.persagy.apccontrol.basic.structure.cacherwd.projection;

import com.google.common.collect.ImmutableSet;
import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.framework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * RWD 实际字段与缓存对象模型视图记录转换器。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheObjectModelViewRecordConverter {

    /**
     * 模型视图恢复时默认返回的公共业务字段。
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> DEFAULT_COMMON_FIELDS = ImmutableSet.of(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.objType,
            DtpRwdEquipmentDTO.Fields.buildingId,
            DtpRwdEquipmentDTO.Fields.floorId);

    /**
     * 模型视图字段投影器。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectExtensionProjector extensionProjector;

    /**
     * 将 RWD 实际返回字段转换为缓存对象模型视图记录。
     *
     * <p>楼层与建筑独立列只承载单一归属语义，RWD 返回逗号分隔字符串或 JSON 数组时该列只写入第一个值。</p>
     *
     * @param actualFields RWD 实际返回字段
     * @return 缓存对象模型视图记录
     * @throws BusinessException 对象 ID、设备类或对象类型缺失时抛出
     * @since v7.260701.0
     */
    public @NonNull CacheObjectModelView toModelView(@NonNull Map<String, ?> actualFields) {
        String id = this.requireString(actualFields, DtpRwdObjectDTO.Fields.id);
        String classCode = this.requireString(actualFields, DtpRwdObjectDTO.Fields.classCode);
        String objectType = this.requireString(actualFields, DtpRwdObjectDTO.Fields.objType);
        CacheObjectModelView result = new CacheObjectModelView();
        result.setId(id);
        result.setProjectId(this.readString(actualFields.get(DtpRwdObjectDTO.Fields.projectId)));
        result.setLocalName(this.readString(actualFields.get(DtpRwdObjectDTO.Fields.localName)));
        result.setClassCode(classCode);
        result.setObjectType(objectType);
        result.setBuildingId(this.readSingleValue(actualFields.get(DtpRwdEquipmentDTO.Fields.buildingId)));
        result.setFloorId(this.readSingleValue(actualFields.get(DtpRwdEquipmentDTO.Fields.floorId)));
        String viewData = this.extensionProjector.projectModelView(classCode, actualFields);
        if (StringUtils.isBlank(viewData)) {
            throw CacheObjectModelExceptionFactory.invalidStructure(
                    "cacheObjectModelView.viewData");
        }
        result.setViewData(viewData);
        return result;
    }

    /**
     * 使用默认字段重建模型视图对象。
     *
     * <p>默认返回公共业务字段和当前 classCode 配置的全部模型视图 JSON 字段，
     * 不默认返回 {@code projectId}。</p>
     *
     * @param modelView  模型视图记录
     * @param targetClass 目标 RWD DTO 类型
     * @param <T>         RWD DTO 类型
     * @return 重建后的 RWD 对象；目标类型无法转换时返回 {@code null}
     * @since v7.260701.0
     */
    public <T extends DtpRwdObjectDTO> @Nullable T toRwdObject(
            @NonNull CacheObjectModelView modelView,
            @NonNull Class<T> targetClass) {
        return this.toRwdObject(modelView, targetClass, Collections.emptySet());
    }

    /**
     * 使用默认字段和调用方额外字段重建模型视图对象。
     *
     * <p>最终允许字段为默认公共业务字段、当前 classCode 配置的模型视图字段和额外字段的并集。
     * 额外字段不能绕过 classCode 配置读取 JSON；独立列仍作为可信来源最后覆盖 JSON。</p>
     *
     * @param modelView       模型视图记录
     * @param targetClass     目标 RWD DTO 类型
     * @param additionalFields 当前业务场景需要额外返回的 RWD 字段
     * @param <T>             RWD DTO 类型
     * @return 重建后的 RWD 对象；目标类型无法转换时返回 {@code null}
     * @since v7.260701.0
     */
    public <T extends DtpRwdObjectDTO> @Nullable T toRwdObject(
            @NonNull CacheObjectModelView modelView,
            @NonNull Class<T> targetClass,
            @NonNull Collection<String> additionalFields) {
        Set<String> allowedFields = new LinkedHashSet<>(
                CacheObjectModelViewRecordConverter.DEFAULT_COMMON_FIELDS);
        allowedFields.addAll(CacheObjectExtensionFieldConfig.getModelViewFields(
                modelView.getClassCode()));
        allowedFields.addAll(additionalFields);
        Map<String, Object> commonFields = new LinkedHashMap<>();
        commonFields.put(DtpRwdObjectDTO.Fields.id, modelView.getId());
        commonFields.put(DtpRwdObjectDTO.Fields.projectId, modelView.getProjectId());
        commonFields.put(DtpRwdObjectDTO.Fields.localName, modelView.getLocalName());
        commonFields.put(DtpRwdObjectDTO.Fields.classCode, modelView.getClassCode());
        commonFields.put(DtpRwdObjectDTO.Fields.objType, modelView.getObjectType());
        if (modelView.getBuildingId() != null) {
            commonFields.put(DtpRwdEquipmentDTO.Fields.buildingId, modelView.getBuildingId());
        }
        if (modelView.getFloorId() != null) {
            commonFields.put(DtpRwdEquipmentDTO.Fields.floorId, modelView.getFloorId());
        }
        commonFields.keySet().retainAll(allowedFields);
        return this.extensionProjector.rebuildModelView(
                modelView.getClassCode(), commonFields, modelView.getViewData(), allowedFields,
                targetClass);
    }

    /**
     * 读取必填字符串字段。
     *
     * @param fields    RWD 实际字段
     * @param fieldName 字段名
     * @return 非空字段值
     * @throws BusinessException 字段不存在或为空时抛出
     * @since v7.260701.0
     */
    private @NonNull String requireString(
            @NonNull Map<String, ?> fields,
            @NonNull String fieldName) {
        return this.requireText(this.readString(fields.get(fieldName)), fieldName);
    }

    /**
     * 校验必填字符串字段。
     *
     * @param value     字段值
     * @param fieldName 稳定字段名
     * @return 原字段值
     * @throws BusinessException 字段为空时抛出
     * @since v7.260701.0
     */
    private @NonNull String requireText(
            @Nullable String value,
            @NonNull String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw CacheObjectModelExceptionFactory.requiredValue(fieldName);
        }
        return value;
    }

    /**
     * 将动态字段值转换为字符串。
     *
     * @param value 字段值
     * @return 字符串值，原值为空时返回 {@code null}
     * @since v7.260701.0
     */
    private @Nullable String readString(@Nullable Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 读取单一归属语义的独立列字段。
     *
     * <p>RWD 对个别设备类可能返回逗号分隔字符串或 JSON 数组两种多值结构；独立列只承载
     * 单一归属语义，多值只取第一个元素写入：先按首个英文逗号截断取第一段，再剥离该段
     * 首尾的数组括号、元素引号与空白装饰。单值与空串等非多值结构原样保留。</p>
     *
     * @param value 字段值
     * @return 多值结构的第一个值；其余场景原样返回字段值
     * @since v7.260802.3
     */
    private @Nullable String readSingleValue(@Nullable Object value) {
        String text = this.readString(value);
        if (text == null) {
            return null;
        }
        String trimmed = StringUtils.trim(text);
        if (StringUtils.contains(trimmed, ',') || StringUtils.startsWith(trimmed, "[")) {
            return StringUtils.strip(StringUtils.substringBefore(trimmed, ","), "[]\" ");
        }
        return text;
    }
}
