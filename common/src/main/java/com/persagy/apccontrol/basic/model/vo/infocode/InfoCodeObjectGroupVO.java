package com.persagy.apccontrol.basic.model.vo.infocode;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigTemplateTypeQueryParam;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeGroupModel;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/6/28 18:29
 * @Version 1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InfoCodeObjectGroupVO extends DtpRwdObjectDTO {

    /**
     * 对象ID
     */
    @Deprecated
    private String objectId;

    /**
     * 对象名称
     */
    @Deprecated
    private String objectName;

    /**
     * 模版类型ID
     */
    private Long templateTypeId;
    /**
     * 模版类型名称
     */
    private String templateTypeName;

    /**
     * 关联信息点分组配置
     */
    private List<InfoCodeGroupVO> relInfoCodeGroupConfigs;

    /**
     * 根据聚合根列表生成对象信息点分组VO
     *
     * @param aggregates 聚合根列表
     * @return 对象信息点分组VO列表
     * @since 7.250802.0
     */
    public static List<InfoCodeObjectGroupVO> of(@Nullable List<InfoCodeAggregate> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return Lists.newArrayList();
        }
        InfoCodeConfigService infoCodeConfigService = SpringUtil.getBean(InfoCodeConfigService.class);
        Set<Long> allTemplateTypeIds = new HashSet<>();
        Map<DtpRwdObjectDTO, List<InfoCodeAggregate>> objectIdAndAggregates = new LinkedHashMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            Long templateTypeId = aggregate.getTemplateTypeModel().getTemplateTypeId();
            DtpRwdObjectDTO object = aggregate.getIdentifier().getObject();
            allTemplateTypeIds.add(templateTypeId);
            MapExecutor.dynamicPut(objectIdAndAggregates, object, aggregate, ArrayList::new);
        }
        // FIXME 不支持多个模版查询, 可能影响调试工具, 云端IOT推送直接调的网关层所以不影响
        InfoCodeAggregate next = aggregates.iterator().next();
        InfoCodeConfigTemplateTypeQueryParam param = new InfoCodeConfigTemplateTypeQueryParam();
        param.setRunningMonitorType(next.getIdentifier().getRunningMonitorType());
        param.setRunningMonitorSecondType(next.getIdentifier().getRunningMonitorSecondType());
        param.setTemplateType(next.getIdentifier().getTemplateType());
        param.setFilterTemplateTypeIds(allTemplateTypeIds);
        param.setNotQueryObjectCount(true);
        List<InfoCodeTemplateVO> infoCodeTemplateVOS = infoCodeConfigService.listTemplateTypes(param);
        Map<Long, InfoCodeTemplateVO> equipmentInfoCodeTemplateVOMap = new HashMap<>();
        for (InfoCodeTemplateVO infoCodeTemplateVO : infoCodeTemplateVOS) {
            equipmentInfoCodeTemplateVOMap.put(infoCodeTemplateVO.getTemplateTypeId(), infoCodeTemplateVO);
        }
        List<InfoCodeObjectGroupVO> result = new ArrayList<>();

        for (Map.Entry<DtpRwdObjectDTO, List<InfoCodeAggregate>> entry : objectIdAndAggregates.entrySet()) {
            DtpRwdObjectDTO object = entry.getKey();
            List<InfoCodeAggregate> aggregatesOfObject = entry.getValue();

            InfoCodeAggregate infoCodeAggregate = aggregatesOfObject.get(0);
            Long templateTypeId = infoCodeAggregate.getTemplateTypeModel().getTemplateTypeId();

            // 分组编码 -> [信息点编码 -> 聚合根]
            Map<String, List<InfoCodeAggregate>> groupCodeInfoCodeAndAggregates =
                    InfoCodeAggregate.mapGroupCodeAndAggregates(aggregatesOfObject);

            // 按 groupCode 去重，收集所有分组模型的规范实例
            // 为什么需要这一步？
            // 每个聚合根(aggregate)的 groupModel 是独立的实例，虽然它们代表同一个逻辑分组
            // (如16个信息点都属于runningParameter)，但它们的equals/hashCode包含relParent引用链
            // 会导致在HashSet/HashMap中被当作不同的对象，从而造成分组重复。
            // 我们通过 groupCode(String) 作为唯一标识去重，确保同一个分组只保留一个规范实例
            Map<String, InfoCodeGroupModel> allGroupCodeModels = new LinkedHashMap<>();
            for (InfoCodeAggregate aggregate : aggregatesOfObject) {
                // 当前聚合根所属的分组模型(如runningParameter)
                InfoCodeGroupModel groupModel = aggregate.getGroupModel();
                // putIfAbsent: 如果该groupCode已经处理过(第一个遇到的实例)，后续重复的不再覆盖
                allGroupCodeModels.putIfAbsent(groupModel.getGroupCode(), groupModel);
                // 同时向上追溯父链，把每一层父分组也纳入集合
                // 比如 runningParameter → machineParameter → topGroup，三个分组都会收集进来
                InfoCodeGroupModel parent = groupModel.getRelParent();
                while (parent != null) {
                    allGroupCodeModels.putIfAbsent(parent.getGroupCode(), parent);
                    parent = parent.getRelParent();
                }
            }
            // 从规范实例集合中，构建两级数据结构：
            // 1. firstLevelGroupModels: 所有没有父级的分组（树的最顶层节点）
            // 2. parentGroupModelAndGroupModel: Map<父分组, 该父分组的子分组集合>（树的分支关系）
            // 这里统一使用 allGroupCodeModels 中收集的规范实例作为key/value，
            // 保证递归查找(analyzeGroupVo)时，key与value引用的是同一个对象，避免key匹配失败
            Set<InfoCodeGroupModel> firstLevelGroupModels = new HashSet<>();
            Map<InfoCodeGroupModel, Set<InfoCodeGroupModel>> parentGroupModelAndGroupModel = new HashMap<>();
            for (InfoCodeGroupModel model : allGroupCodeModels.values()) {
                // 当前分组的父级(从规范实例中取得)
                InfoCodeGroupModel relParent = model.getRelParent();
                if (Objects.isNull(relParent)) {
                    // 没有父级 → 顶级分组 → 放入一级分组列表
                    firstLevelGroupModels.add(model);
                } else {
                    // 从规范实例映射中查找父分组的规范实例
                    // (避免使用原始聚合根中可能携带的不同引用实例)
                    InfoCodeGroupModel canonicalParent = allGroupCodeModels.getOrDefault(
                            relParent.getGroupCode(), relParent);
                    // 防御：deepFillRelParentModel 对顶级分组(relParentId=null)的实现
                    // 会返回该分组自身的一个副本(而不是返回null)
                    // 这会导致一个分组成为它自己的子分组(自引用)，造成无限递归/重复显示
                    // 当发现父级groupCode与自己相同时，说明是自引用，将其视为顶级分组
                    if (!Objects.equals(canonicalParent.getGroupCode(), model.getGroupCode())) {
                        MapExecutor.dynamicPut(parentGroupModelAndGroupModel, canonicalParent, model, HashSet::new);
                    } else {
                        firstLevelGroupModels.add(model);
                    }
                }
            }
            // 递归设置分组和分组下的信息点
            List<InfoCodeGroupVO> groupVos = new ArrayList<>();
            for (InfoCodeGroupModel firstLevelGroupModel : firstLevelGroupModels) {
                InfoCodeGroupVO groupVo = analyzeGroupVo(firstLevelGroupModel, aggregatesOfObject,
                        groupCodeInfoCodeAndAggregates, parentGroupModelAndGroupModel);
                if (Objects.nonNull(groupVo)) {
                    groupVos.add(groupVo);
                }
            }
            groupVos.sort(Comparator.comparing(InfoCodeGroupVO::getGroupOrder));
            InfoCodeObjectGroupVO objectVo = new InfoCodeObjectGroupVO();
            BeanUtil.copyProperties(object, objectVo);
            if (LocalContextStorage.isDeprecatedVersion()) {
                objectVo.setObjectId(object.getId());
                objectVo.setObjectName(object.getLocalName());
            }
            objectVo.setRelInfoCodeGroupConfigs(groupVos);

            InfoCodeTemplateVO infoCodeTemplateVO = equipmentInfoCodeTemplateVOMap.get(templateTypeId);
            if (Objects.nonNull(infoCodeTemplateVO)) {
                objectVo.setTemplateTypeId(infoCodeTemplateVO.getTemplateTypeId());
                objectVo.setTemplateTypeName(infoCodeTemplateVO.getTemplateTypeName());
            }
            result.add(objectVo);
        }
        return result;
    }

    /**
     * 根据聚合根列表生成分组配置列表（原始数据，不包含对象属性）
     *
     * @param aggregates 聚合根列表
     * @return 分组VO列表
     * @since 7.260501.0
     */
    public static List<InfoCodeGroupVO> ofOrigin(@Nullable List<InfoCodeAggregate> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return Lists.newArrayList();
        }
        return toGroupVos(aggregates);
    }

    /**
     * 将聚合根列表转换为分组VO列表
     *
     * @param aggregates 聚合根列表
     * @return 分组VO列表
     * @since 7.260501.0
     */
    private static List<InfoCodeGroupVO> toGroupVos(@NonNull List<InfoCodeAggregate> aggregates) {
        // 分组编码 -> [信息点编码 -> 聚合根]
        Map<String, List<InfoCodeAggregate>> groupCodeInfoCodeAndAggregates =
                InfoCodeAggregate.mapGroupCodeAndAggregates(aggregates);

        // 按 groupCode 去重，收集所有分组模型的规范实例
        Map<String, InfoCodeGroupModel> allGroupCodeModels = new LinkedHashMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            InfoCodeGroupModel groupModel = aggregate.getGroupModel();
            allGroupCodeModels.putIfAbsent(groupModel.getGroupCode(), groupModel);
            InfoCodeGroupModel parent = groupModel.getRelParent();
            while (parent != null) {
                allGroupCodeModels.putIfAbsent(parent.getGroupCode(), parent);
                parent = parent.getRelParent();
            }
        }
        Set<InfoCodeGroupModel> firstLevelGroupModels = new HashSet<>();
        Map<InfoCodeGroupModel, Set<InfoCodeGroupModel>> parentGroupModelAndGroupModel = new HashMap<>();
        for (InfoCodeGroupModel model : allGroupCodeModels.values()) {
            InfoCodeGroupModel relParent = model.getRelParent();
            if (Objects.isNull(relParent)) {
                firstLevelGroupModels.add(model);
            } else {
                InfoCodeGroupModel canonicalParent = allGroupCodeModels.getOrDefault(
                        relParent.getGroupCode(), relParent);
                if (!Objects.equals(canonicalParent.getGroupCode(), model.getGroupCode())) {
                    MapExecutor.dynamicPut(parentGroupModelAndGroupModel, canonicalParent, model, HashSet::new);
                } else {
                    firstLevelGroupModels.add(model);
                }
            }
        }
        // 递归设置分组和分组下的信息点
        List<InfoCodeGroupVO> groupVos = new ArrayList<>();
        for (InfoCodeGroupModel firstLevelGroupModel : firstLevelGroupModels) {
            InfoCodeGroupVO groupVo = analyzeGroupVo(firstLevelGroupModel, aggregates,
                    groupCodeInfoCodeAndAggregates, parentGroupModelAndGroupModel);
            if (Objects.nonNull(groupVo)) {
                groupVos.add(groupVo);
            }
        }
        groupVos.sort(Comparator.comparing(InfoCodeGroupVO::getGroupOrder));
        return groupVos;
    }

    /**
     * 解析分组VO
     *
     * @param groupModel                     分组模型
     * @param allAggregates                  对象所有聚合根
     * @param groupCodeInfoCodeAndAggregates 分组编码 -> [信息点编码 -> 聚合根]
     * @param parentGroupModelAndGroupModel  上级分组模型 -> 关联分组模型
     * @return 分组VO
     * @since 5.240602.0
     */
    private static @Nullable InfoCodeGroupVO analyzeGroupVo(
            InfoCodeGroupModel groupModel,
            List<InfoCodeAggregate> allAggregates,
            Map<String, List<InfoCodeAggregate>> groupCodeInfoCodeAndAggregates,
            Map<InfoCodeGroupModel, Set<InfoCodeGroupModel>> parentGroupModelAndGroupModel) {
        String groupCode = groupModel.getGroupCode();
        String groupName = groupModel.getGroupName();
        Integer sequence = groupModel.getSequence();
        // 下级聚合根
        Set<InfoCodeGroupModel> groupModelChildren = parentGroupModelAndGroupModel.get(groupModel);

        // 分组下所有聚合根
        List<InfoCodeAggregate> infoCodeAggregates = groupCodeInfoCodeAndAggregates.get(groupCode);
        List<InfoCodeConfigVO> infoCodeConfigs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(infoCodeAggregates)) {
            List<MultiKey<String>> relInfoCodeKeys = new ArrayList<>();
            for (InfoCodeAggregate infoCodeAggregate : infoCodeAggregates) {
                InfoCodeConfigVO infoCodeConfig = InfoCodeConfigVO.of(infoCodeAggregate, allAggregates);
                relInfoCodeKeys.addAll(infoCodeConfig.toRelInfoCodeKeys());
                infoCodeConfigs.add(infoCodeConfig);
            }
            // 当存在关联信息点时, 过滤外层重复信息点
            if (CollectionUtils.isNotEmpty(relInfoCodeKeys)) {
                infoCodeConfigs.removeIf(item -> relInfoCodeKeys.contains(new MultiKey<>(
                        item.getInfoCodeType().value(), item.getInfoCode())));
            }
            infoCodeConfigs.sort(Comparator.comparing(InfoCodeConfigVO::getSequence));
        }
        List<InfoCodeGroupVO> groupChildren = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(groupModelChildren)) {
            for (InfoCodeGroupModel groupModelChild : groupModelChildren) {
                InfoCodeGroupVO groupChildrenVo = analyzeGroupVo(groupModelChild, allAggregates,
                        groupCodeInfoCodeAndAggregates, parentGroupModelAndGroupModel);
                if (Objects.nonNull(groupChildrenVo)) {
                    groupChildren.add(groupChildrenVo);
                }
            }
            groupChildren.sort(Comparator.comparing(InfoCodeGroupVO::getGroupOrder));
        }
        InfoCodeGroupVO groupVo = new InfoCodeGroupVO();
        groupVo.setGroupCode(groupCode);
        groupVo.setGroupName(groupName);
        groupVo.setGroupOrder(sequence);
        if (LocalContextStorage.isDeprecatedVersion() || CollectionUtils.isNotEmpty(infoCodeConfigs)) {
            groupVo.setRelInfoCodeConfigs(infoCodeConfigs);
        }
        if (LocalContextStorage.isDeprecatedVersion() || CollectionUtils.isNotEmpty(groupChildren)) {
            groupVo.setGroupChildren(groupChildren);
        }
        if (emptyGroup(groupVo)) {
            return null;
        }
        return groupVo;
    }

    /**
     * 是否为空分组
     *
     * @param groupVo 分组VO
     * @return 是否为空分组
     * @since 7.250402.0
     */
    private static boolean emptyGroup(@NonNull InfoCodeGroupVO groupVo) {
        List<InfoCodeConfigVO> relInfoCodeConfigs = groupVo.getRelInfoCodeConfigs();
        List<InfoCodeGroupVO> groupChildren = groupVo.getGroupChildren();
        return CollectionUtils.isEmpty(relInfoCodeConfigs)
                && CollectionUtils.isNotEmpty(groupChildren)
                && groupChildren.stream().allMatch(item ->
                InfoCodeGroupModel.IOT_SETTING_CONDITION_GROUP_NAMES.contains(item.getGroupCode()));
    }
}
