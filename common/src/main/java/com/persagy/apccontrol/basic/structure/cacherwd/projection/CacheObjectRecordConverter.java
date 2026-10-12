package com.persagy.apccontrol.basic.structure.cacherwd.projection;

import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.framework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RWD 实际字段与缓存对象记录转换器。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheObjectRecordConverter {

    /**
     * 对象扩展字段投影器。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectExtensionProjector extensionProjector;

    /**
     * 将 RWD 实际返回字段转换为缓存对象记录。
     *
     * <p>监测监控状态按 RWD 返回的单个 code 原样转为字符串；未返回时独立列保持 SQL NULL。
     * 楼层与建筑独立列只承载单一归属语义，RWD 返回逗号分隔字符串或 JSON 数组时该列只写入第一个值。</p>
     *
     * @param actualFields RWD 实际返回字段
     * @return 缓存对象记录
     * @throws BusinessException 对象 ID、设备类或对象类型缺失时抛出
     * @since v7.260701.0
     */
    public @NonNull CacheObject toCacheObject(@NonNull Map<String, ?> actualFields) {
        String id = this.requireString(actualFields, DtpRwdObjectDTO.Fields.id);
        String classCode = this.requireString(actualFields, DtpRwdObjectDTO.Fields.classCode);
        String objectType = this.requireString(actualFields, DtpRwdObjectDTO.Fields.objType);
        CacheObject result = new CacheObject();
        result.setId(id);
        result.setProjectId(this.readString(actualFields.get(DtpRwdObjectDTO.Fields.projectId)));
        result.setLocalId(this.readString(actualFields.get(DtpRwdObjectDTO.Fields.localId)));
        result.setLocalName(this.readString(actualFields.get(DtpRwdObjectDTO.Fields.localName)));
        result.setClassCode(classCode);
        result.setObjectType(objectType);
        result.setBuildingId(this.readSingleValue(actualFields.get(DtpRwdEquipmentDTO.Fields.buildingId)));
        result.setFloorId(this.readSingleValue(actualFields.get(DtpRwdEquipmentDTO.Fields.floorId)));
        result.setMonitoringSurveillanceStatus(this.readString(actualFields.get(
                InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value())));
        String objectData = this.extensionProjector.projectObject(classCode, actualFields);
        if (StringUtils.isBlank(objectData)) {
            throw CacheObjectModelExceptionFactory.invalidStructure("cacheObject.objectData");
        }
        result.setObjectData(objectData);
        return result;
    }

    /**
     * 使用独立列和扩展 JSON 重建 RWD 对象。
     *
     * @param cacheObject 缓存对象记录
     * @param targetClass 目标 RWD DTO 类型
     * @param <T>         RWD DTO 类型
     * @return 重建后的 RWD 对象
     * @since v7.260701.0
     */
    public <T extends DtpRwdObjectDTO> @Nullable T toRwdObject(
            @NonNull CacheObject cacheObject,
            @NonNull Class<T> targetClass) {
        return this.toRwdObject(cacheObject, targetClass, null);
    }

    /**
     * 使用调用方字段重建 RWD 对象。
     *
     * <p>{@code requestedFields == null} 表示兼容旧调用方的全字段恢复；非空引用（包括空集合）
     * 由调用方明确控制最终 DTO 字段，不会因为对象主表 JSON 整列被读取而扩大返回内容。</p>
     *
     * @param cacheObject 缓存对象记录
     * @param targetClass 目标 RWD DTO 类型
     * @param requestedFields 调用方请求的 RWD 字段；为 {@code null} 时兼容旧行为
     * @param <T> RWD DTO 类型
     * @return 重建后的 RWD 对象
     * @since v7.260701.0
     */
    public <T extends DtpRwdObjectDTO> @Nullable T toRwdObject(
            @NonNull CacheObject cacheObject,
            @NonNull Class<T> targetClass,
            @Nullable Collection<String> requestedFields) {
        Map<String, Object> commonFields = new LinkedHashMap<>();
        commonFields.put(DtpRwdObjectDTO.Fields.id, cacheObject.getId());
        commonFields.put(DtpRwdObjectDTO.Fields.projectId, cacheObject.getProjectId());
        commonFields.put(DtpRwdObjectDTO.Fields.localId, cacheObject.getLocalId());
        commonFields.put(DtpRwdObjectDTO.Fields.localName, cacheObject.getLocalName());
        commonFields.put(DtpRwdObjectDTO.Fields.classCode, cacheObject.getClassCode());
        commonFields.put(DtpRwdObjectDTO.Fields.objType, cacheObject.getObjectType());
        if (cacheObject.getBuildingId() != null) {
            commonFields.put(DtpRwdEquipmentDTO.Fields.buildingId, cacheObject.getBuildingId());
        }
        if (cacheObject.getFloorId() != null) {
            commonFields.put(DtpRwdEquipmentDTO.Fields.floorId, cacheObject.getFloorId());
        }
        if (cacheObject.getMonitoringSurveillanceStatus() != null) {
            commonFields.put(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(),
                    cacheObject.getMonitoringSurveillanceStatus());
        }
        if (requestedFields == null) {
            return this.extensionProjector.rebuildObject(cacheObject.getClassCode(), commonFields,
                    cacheObject.getObjectData(), targetClass);
        }
        // 独立列和扩展 JSON 使用同一份调用方字段边界，内部查询字段不能透传到最终 DTO。
        commonFields.keySet().retainAll(requestedFields);
        return this.extensionProjector.rebuildObject(cacheObject.getClassCode(), commonFields,
                cacheObject.getObjectData(), requestedFields, targetClass);
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
