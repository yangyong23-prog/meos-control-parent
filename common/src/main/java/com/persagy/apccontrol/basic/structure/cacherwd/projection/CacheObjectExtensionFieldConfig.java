package com.persagy.apccontrol.basic.structure.cacherwd.projection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 * 缓存对象与模型视图字段静态配置。
 *
 * <p>对象 JSON 和模型视图 JSON 使用两份完全独立的 classCode 字段 Map。新增设备类或字段时
 * 必须直接在对应 Map 中显式登记，避免配置来源分散或运行时变化。模型图字段只允许登记在
 * 模型视图字段 Map：设备类使用 bimId、bimLocation，outline 仅用于 SPACE 和 AC_SPACE；
 * 对象扩展 JSON 不存储也不返回这些字段。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public final class CacheObjectExtensionFieldConfig {

    /**
     * 禁止写入扩展 JSON 的公共字段。
     *
     * <p>这些字段已有独立列、专用结构，或明确不属于本缓存的扩展字段契约；配置为设备类
     * 扩展字段会造成数据源冲突或扩大对象 JSON。监测监控状态使用独立字符串列作为唯一
     * 存储来源，因此也必须在此禁止。</p>
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> FORBIDDEN_EXTENSION_FIELDS = ImmutableSet.of(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.name,
            DtpRwdObjectDTO.Fields.localId,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdObjectDTO.Fields.projectId,
            DtpRwdObjectDTO.Fields.objType,
            "objectType",
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.valid,
            DtpRwdObjectDTO.Fields.relObjIds,
            DtpRwdEquipmentDTO.Fields.buildingId,
            DtpRwdEquipmentDTO.Fields.floorId,
            InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(),
            "undefinedProperties",
            "dynamicName"
    );

    /**
     * 每次查询 RWD 都必须携带的基础字段。
     *
     * <p>基础字段用于识别对象所属设备类并填充缓存独立列，不随设备类扩展字段配置变化。
     * 监测监控状态虽然禁止进入扩展 JSON，仍必须查询以写入独立单值字符串列。</p>
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> RWD_QUERY_BASE_FIELDS = ImmutableSet.of(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.localId,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.objType,
            DtpRwdEquipmentDTO.Fields.buildingId,
            DtpRwdEquipmentDTO.Fields.floorId,
            InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value()
    );

    /**
     * 无条件写入对象扩展 JSON 的通用字段，不按 classCode 区分。
     *
     * <p>这些字段是所有对象共有的业务属性（如专业 major），不依赖特定设备类白名单。
     * 投影和重建时自动与 classCode 特定字段合并，确保任何 classCode 都能存取这些字段。</p>
     *
     * @since v7.260802.0
     */
    private static final @NonNull Set<String> UNIVERSAL_OBJECT_FIELDS = ImmutableSet.of(
            InfoCodeEnum.MAJOR.getCode()
    );

    /**
     * 无条件写入模型视图 JSON 的通用字段，不按 classCode 区分。
     *
     * <p>模型图字段（bimId、bimLocation）是所有对象共有的空间定位属性，不依赖特定设备类
     * 白名单。投影和重建时自动与 classCode 特定字段合并，确保任何 classCode 的 view_data
     * 都能存取这些字段。</p>
     *
     * @since v7.260802.0
     */
    private static final @NonNull Set<String> UNIVERSAL_MODEL_VIEW_FIELDS = ImmutableSet.of(
            DtpRwdEquipmentDTO.Fields.bimId,
            DtpRwdEquipmentDTO.Fields.bimLocation
    );

    /**
     * 按设备类编码维护的对象扩展字段不可变配置。
     *
     * <p>每个设备类都在 Map 中直接声明自己的字段集合，不复用可变配置，便于审查和后续独立演进。
     * 只登记 classCode 特有的业务字段；模型图字段（bimId、bimLocation、outline）由模型视图字段
     * Map 独立声明，不得登记到此 Map。通用字段（如 major）由 {@link #UNIVERSAL_OBJECT_FIELDS}
     * 统一管理，不在此 Map 中重复登记。</p>
     *
     * @since v7.260701.0
     */
    private static final @NonNull Map<String, Set<String>> OBJECT_EXTENSION_FIELDS =
            ImmutableMap.<String, Set<String>>builder()
            .put(DtpRwdClassCodeEnum.SPACE.value(), ImmutableSet.of(
                    DtpRwdSpaceDTO.Fields.roomFuncType,
                    InfoCodeEnum.REPLACED_BY_NAT_LIGHTING.getCode()))
            .put(DtpRwdClassCodeEnum.AC_SPACE.value(), ImmutableSet.of(
                    DtpRwdSpaceDTO.Fields.roomFuncType))
            .put(DtpRwdClassCodeEnum.FLOOR.value(), ImmutableSet.of(
                    DtpRwdFloorDTO.Fields.floorSequenceId))
            .put(DtpRwdClassCodeEnum.BUILDING.value(), ImmutableSet.of(
                    DtpRwdBuildingDTO.Fields.buildingSequenceId))
            .put(DtpRwdClassCodeEnum.ACVTSF.value(), ImmutableSet.of(
                    InfoCodeEnum.FUNC_TYPE.value()))
            .put(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value(), ImmutableSet.of(
                    DtpRwdCircuitDTO.Fields.serviceAreaType,
                    DtpRwdCircuitDTO.Fields.funcType,
                    DtpRwdCircuitDTO.Fields.lampFuncType))
            .put(DtpRwdClassCodeEnum.SPVSCM.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.hasArmingFunc,
                    SecurityDtpRwdEquipmentDTO.Fields.cameraType,
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer,
                    InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode()))
            .put(DtpRwdClassCodeEnum.SPIACU.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer))
            .put(DtpRwdClassCodeEnum.WEGMCM.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.hasArmingFunc,
                    SecurityDtpRwdEquipmentDTO.Fields.cameraType,
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer,
                    InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode()))
            .put(DtpRwdClassCodeEnum.SPVSVE.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer))
            .put(DtpRwdClassCodeEnum.SPVSDR.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer))
            .put(DtpRwdClassCodeEnum.SPIAGD.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer))
            .put(DtpRwdClassCodeEnum.SPIAID.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer))
            .put(DtpRwdClassCodeEnum.SPIAMA.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer))
            .put(DtpRwdClassCodeEnum.SPEGCU.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer))
            .put(DtpRwdClassCodeEnum.CFCSDR.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer))
            .put(DtpRwdClassCodeEnum.CFCSFR.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer))
            .put(DtpRwdClassCodeEnum.SPEGFG.value(), ImmutableSet.of(
                    SecurityDtpRwdEquipmentDTO.Fields.isControl,
                    SecurityDtpRwdEquipmentDTO.Fields.manufacturer))
            .build();

    /**
     * 按设备类编码维护的模型视图字段不可变配置。
     *
     * <p>每个 classCode 必须显式登记模型视图 JSON 允许写入的完整字段集合，不再根据 objType
     * 隐式补充字段。该 Map 与对象字段 Map 保持独立，防止两种 JSON 相互串字段。未显式登记
     * 的 classCode 仍由 {@link #UNIVERSAL_MODEL_VIEW_FIELDS} 兜底提供基础 BIM 字段。</p>
     *
     * @since v7.260701.0
     */
    private static final @NonNull Map<String, Set<String>> MODEL_VIEW_FIELDS =
            ImmutableMap.<String, Set<String>>builder()
            .put(DtpRwdClassCodeEnum.SPACE.value(), ImmutableSet.of(
                    DtpRwdSpaceDTO.Fields.roomFuncType,
                    DtpRwdSpaceDTO.Fields.bimId,
                    DtpRwdSpaceDTO.Fields.bimLocation,
                    DtpRwdSpaceDTO.Fields.outline))
            .put(DtpRwdClassCodeEnum.AC_SPACE.value(), ImmutableSet.of(
                    DtpRwdSpaceDTO.Fields.roomFuncType,
                    DtpRwdSpaceDTO.Fields.bimId,
                    DtpRwdSpaceDTO.Fields.bimLocation,
                    DtpRwdSpaceDTO.Fields.outline))
            .put(DtpRwdClassCodeEnum.ACATDH.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACATAH.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACATFC.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACVRVI.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACCCWP.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACATFA.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACATVA.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACATFU.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACATRA.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACVTAC.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACAHG_GROUP.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACATHT.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACVTSF.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACVVER.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACVVEA.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.FFSCAF.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.CFCSEW.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.ACVVEJ.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.LIGHTING_CONTROLLER.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.WEFCDD.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.SPIACU.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.SPVSCM.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.WEGMCM.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.SPVSVE.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.SPVSDR.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.SPIAGD.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.SPIAID.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.SPIAMA.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.SPEGCU.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.CFCSDR.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.CFCSFR.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))
            .put(DtpRwdClassCodeEnum.SPEGFG.value(), ImmutableSet.of(
                    DtpRwdEquipmentDTO.Fields.bimId, DtpRwdEquipmentDTO.Fields.bimLocation))

            .build();

    /**
     * 防止实例化静态配置工具类。
     *
     * @since v7.260701.0
     */
    private CacheObjectExtensionFieldConfig() {
        super();
    }

    /**
     * 获取对象扩展字段静态配置。
     *
     * @return 深度不可变的对象扩展字段 Map
     * @since v7.260701.0
     */
    public static @NonNull Map<String, Set<String>> getObjectExtensionFields() {
        return CacheObjectExtensionFieldConfig.OBJECT_EXTENSION_FIELDS;
    }

    /**
     * 获取模型视图 classCode 字段静态配置。
     *
     * @return 深度不可变的模型视图 classCode 完整字段 Map
     * @since v7.260701.0
     */
    public static @NonNull Map<String, Set<String>> getModelViewFields() {
        return CacheObjectExtensionFieldConfig.MODEL_VIEW_FIELDS;
    }

    /**
     * 获取无条件投影的通用对象扩展字段。
     *
     * @return 深度不可变的通用字段集合
     * @since v7.260802.0
     */
    public static @NonNull Set<String> getUniversalObjectFields() {
        return CacheObjectExtensionFieldConfig.UNIVERSAL_OBJECT_FIELDS;
    }

    /**
     * 获取无条件投影的通用模型视图字段。
     *
     * @return 深度不可变的通用模型图字段集合
     * @since v7.260802.0
     */
    public static @NonNull Set<String> getUniversalModelViewFields() {
        return CacheObjectExtensionFieldConfig.UNIVERSAL_MODEL_VIEW_FIELDS;
    }

    /**
     * 获取单个设备类的对象扩展字段，未配置是合法状态。
     *
     * <p>返回 classCode 特定字段与通用字段的并集。</p>
     *
     * @param classCode 设备类编码
     * @return 对象扩展字段（含通用字段）；未配置时仅返回通用字段
     * @since v7.260701.0
     */
    public static @NonNull Set<String> getObjectFields(@Nullable String classCode) {
        Set<String> classCodeFields = CacheObjectExtensionFieldConfig.OBJECT_EXTENSION_FIELDS.getOrDefault(
                classCode, ImmutableSet.of());
        if (classCodeFields.isEmpty()) {
            return CacheObjectExtensionFieldConfig.UNIVERSAL_OBJECT_FIELDS;
        }
        Set<String> merged = new LinkedHashSet<>(classCodeFields);
        merged.addAll(CacheObjectExtensionFieldConfig.UNIVERSAL_OBJECT_FIELDS);
        return Collections.unmodifiableSet(merged);
    }

    /**
     * 获取单个设备类的模型视图字段，未配置是合法状态。
     *
     * <p>返回 classCode 特定字段与通用模型图字段的并集。</p>
     *
     * @param classCode 设备类编码
     * @return 模型视图允许写入的完整字段（含通用字段）；未配置时仅返回通用字段
     * @since v7.260701.0
     */
    public static @NonNull Set<String> getModelViewFields(@Nullable String classCode) {
        Set<String> classCodeFields = CacheObjectExtensionFieldConfig.MODEL_VIEW_FIELDS.getOrDefault(
                classCode, ImmutableSet.of());
        if (classCodeFields.isEmpty()) {
            return CacheObjectExtensionFieldConfig.UNIVERSAL_MODEL_VIEW_FIELDS;
        }
        Set<String> merged = new LinkedHashSet<>(classCodeFields);
        merged.addAll(CacheObjectExtensionFieldConfig.UNIVERSAL_MODEL_VIEW_FIELDS);
        return Collections.unmodifiableSet(merged);
    }

    /**
     * 获取单个对象读取缓存对象主表所需的字段。
     *
     * <p>只包含公共基础字段和对象扩展字段，不含模型视图字段；模型图字段由模型视图
     * 字段接口独立提供，缓存对象主表读取不得请求。</p>
     *
     * @param classCode 设备类编码
     * @return 公共查询字段和对象字段的并集，深度不可变
     * @since v7.260801.0
     */
    public static @NonNull Set<String> listObjectQueryFields(@Nullable String classCode) {
        ImmutableSet.Builder<String> result = ImmutableSet.builder();
        result.addAll(CacheObjectExtensionFieldConfig.RWD_QUERY_BASE_FIELDS);
        result.addAll(CacheObjectExtensionFieldConfig.getObjectFields(classCode));
        return result.build();
    }

    /**
     * 获取混合 classCode 范围读取缓存对象主表所需的字段。
     *
     * <p>混合范围仍以每个 classCode 的显式对象字段配置为准，只合并对象主表字段，
     * 不把模型视图字段带入读取请求。</p>
     *
     * @param classCodes classCode 集合
     * @return 公共查询字段和各对象字段的并集，深度不可变
     * @since v7.260802.0
     */
    public static @NonNull Set<String> listObjectQueryFields(
            @NonNull Collection<String> classCodes) {
        ImmutableSet.Builder<String> result = ImmutableSet.builder();
        result.addAll(CacheObjectExtensionFieldConfig.RWD_QUERY_BASE_FIELDS);
        for (String classCode : classCodes) {
            result.addAll(CacheObjectExtensionFieldConfig.getObjectFields(classCode));
        }
        return result.build();
    }

    /**
     * 获取单个对象写入两种缓存所需的 RWD 查询字段。
     *
     * @param classCode 设备类编码
     * @return 公共查询字段、对象字段和模型视图字段的并集，深度不可变
     * @since v7.260701.0
     */
    public static @NonNull Set<String> listRwdQueryFields(@Nullable String classCode) {
        ImmutableSet.Builder<String> result = ImmutableSet.builder();
        result.addAll(CacheObjectExtensionFieldConfig.RWD_QUERY_BASE_FIELDS);
        // 查询阶段取两种用途的字段并集以减少 RWD 调用，落盘时仍必须分别投影，避免 JSON 串字段。
        result.addAll(CacheObjectExtensionFieldConfig.getObjectFields(classCode));
        result.addAll(CacheObjectExtensionFieldConfig.getModelViewFields(classCode));
        return result.build();
    }

    /**
     * 获取无法枚举 classCode 的混合设备类写入所需的 RWD 查询字段全量并集。
     *
     * <p>用于按空间/楼层等范围查询"全量设备"且无法限定 classCode 的缓存写入入口。
     * 同一 cache_object 行会被多个缓存 Key 共享，字段子集写入会在 UPSERT 整列覆盖时
     * 丢失其他策略已写入的字段，因此此类入口必须请求全部已登记字段并集，保证覆盖幂等。</p>
     *
     * @return 基础字段、全部对象扩展字段、全部模型视图字段及通用字段的并集，深度不可变
     * @since v7.260802.3
     */
    public static @NonNull Set<String> listAllRwdQueryFields() {
        Set<String> merged = new LinkedHashSet<>(CacheObjectExtensionFieldConfig.RWD_QUERY_BASE_FIELDS);
        CacheObjectExtensionFieldConfig.OBJECT_EXTENSION_FIELDS.values().forEach(merged::addAll);
        CacheObjectExtensionFieldConfig.MODEL_VIEW_FIELDS.values().forEach(merged::addAll);
        merged.addAll(CacheObjectExtensionFieldConfig.UNIVERSAL_OBJECT_FIELDS);
        merged.addAll(CacheObjectExtensionFieldConfig.UNIVERSAL_MODEL_VIEW_FIELDS);
        return Collections.unmodifiableSet(merged);
    }

    /**
     * 获取禁止写入扩展 JSON 的公共字段，供静态配置契约测试扫描。
     *
     * @return 深度不可变的公共字段集合
     * @since v7.260701.0
     */
    static @NonNull Set<String> getForbiddenExtensionFields() {
        return CacheObjectExtensionFieldConfig.FORBIDDEN_EXTENSION_FIELDS;
    }
}
