package com.persagy.apccontrol.basic.structure.infocodeconfig.model;

import com.persagy.apccontrol.basic.model.base.domain.AggregateRoot;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 信息点聚合根
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeAggregate implements AggregateRoot<InfoCodeIdentifier> {

    // ------------------------- 标识符 -------------------------

    /**
     * 标识符
     */

    private final @NonNull InfoCodeIdentifier identifier;

    // ------------------------- 核心字段 -------------------------

    /**
     * 模版类型模型
     */
    private final @NonNull InfoCodeTemplateTypeModel templateTypeModel;

    /**
     * 信息点配置模型
     */
    private final @NonNull InfoCodeConfigModel configModel;

    /**
     * 组合配置模型
     */
    private final @Nullable List<@NonNull InfoCodeAssembleModel> assembleModels;

    /**
     * 分组配置模型
     */
    private final @NonNull InfoCodeGroupModel groupModel;

    // ------------------------- 聚合模型 -------------------------

    /**
     * 信息点定义模型
     */
    private InfoCodeDefineModel infoCodeAndDefineModel;

    /**
     * 信息点值模型
     */
    private InfoCodeDataModel infoCodeAndDataModel;

    /**
     * 是否无效
     */
    private boolean invalid = false;

    // ------------------------- 静态行为 -------------------------

    /**
     * 获取所有对象ID
     *
     * @param aggregates 聚合根
     * @return 所有对象ID
     * @since 5.2420602.0
     */
    public static Set<String> listAllObjectIds(@NonNull List<InfoCodeAggregate> aggregates) {
        return aggregates.stream()
                .map(item -> item.getIdentifier().getObject().getId())
                .collect(Collectors.toSet());
    }

    /**
     * 获取对象ID和聚合根映射
     *
     * @param aggregates 聚合根
     * @return 对象ID -> 聚合根
     * @since 7.250601.0
     */
    public static Map<String, List<InfoCodeAggregate>> mapObjectIdAndAggregates(
            @NonNull List<InfoCodeAggregate> aggregates) {
        Map<String, List<InfoCodeAggregate>> result = new LinkedHashMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.identifier.getObject().getId();
            MapExecutor.dynamicPut(result, objectId, aggregate, ArrayList::new);
        }
        return result;
    }

    /**
     * 获取对象ID和信息点聚合根映射
     *
     * @param aggregates 聚合根
     * @return 对象ID -> [信息点编码 -> 聚合根]
     * @since 5.240602.0
     */
    public static Map<String, Map<String, InfoCodeAggregate>> mapObjectIdInfoCodeAndAggregates(
            @NonNull List<InfoCodeAggregate> aggregates) {
        Map<String, Map<String, InfoCodeAggregate>> result = new LinkedHashMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.identifier.getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            MapExecutor.dynamicPut(result, objectId, infoCode, aggregate, LinkedHashMap::new);
        }
        return result;
    }

    /**
     * 获取分组编码和信息点聚合根映射
     *
     * @param aggregates 聚合根
     * @return 分组编码 -> [信息点编码 -> 聚合根]
     * @since 5.240602.0
     */
    public static Map<String, List<InfoCodeAggregate>> mapGroupCodeAndAggregates(
            @NonNull List<InfoCodeAggregate> aggregates) {
        return aggregates.stream().collect(Collectors.groupingBy(item -> item.getGroupModel().getGroupCode()));
    }

    // ------------------------- 普通行为 -------------------------

    /**
     * 解析关联聚合根
     *
     * @param aggregates 聚合根
     * @return 关联聚合根
     * @since 5.240602.0
     */
    public @Nullable List<@NonNull InfoCodeAggregate> analyzeAssembleAggregate(
            @NonNull Collection<@NonNull InfoCodeAggregate> aggregates) {
        if (CollectionUtils.isEmpty(assembleModels)) {
            return null;
        }
        Set<Long> configIds = assembleModels.stream()
                .map(item -> item.getConfigModel().getConfigId())
                .collect(Collectors.toSet());
        return aggregates.stream()
                .filter(item -> configIds.contains(item.getConfigModel().getConfigId()))
                .sorted(Comparator.comparing(item -> item.getConfigModel().getSequence()))
                .collect(Collectors.toList());
    }
}
