package com.persagy.apccontrol.common.structure.cacherwd.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheIotRule;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRule;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheIotRuleRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRuleRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheTemplateWriteExtension;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheWriteAction;
import com.persagy.apccontrol.basic.structure.infocodeconfig.context.InfoCodeAggregateInitContext;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.lifecycle.InfoCodeAggregateLifeAware;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 模板规则缓存写入扩展。
 *
 * <p>新版对象模型和现有 IOT 规则缓存分别由独立准备方法生成动作，模板重建服务每次只会调用
 * 其中一条路径，因此不会在同一模板替换中操作两套规则表。</p>
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Slf4j
@Component
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheIotRuleEventListener implements CacheTemplateWriteExtension {

    private final CacheIotRuleRepository cacheIotRuleRepository;

    /**
     * 去重规则主表与缓存键关联仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheRuleRepository cacheRuleRepository;

    private final List<InfoCodeAggregateLifeAware> awares;

    @Override
    public @NonNull CacheWriteAction prepareModelSave(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull List<DtpRwdObjectDTO> objects) {
        String templateKey = templateTypeModel.getTemplateTypeKey();
        RuleCodeSnapshot snapshot = this.prepareRuleCodeSnapshot(templateTypeModel, objects);
        List<CacheRule> rules = new ArrayList<>();
        rules.addAll(this.toCacheRules(
                objects, snapshot.cloudRuleCodes, TerminalEnum.CLOUD));
        rules.addAll(this.toCacheRules(
                objects, snapshot.edgeRuleCodes, TerminalEnum.EDGE));
        return () -> this.cacheRuleRepository.replaceRules(templateKey, rules);
    }

    @Override
    public @NonNull CacheWriteAction prepareRwdSave(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull List<DtpRwdObjectDTO> objects) {
        String templateKey = templateTypeModel.getTemplateTypeKey();
        RuleCodeSnapshot snapshot = this.prepareRuleCodeSnapshot(templateTypeModel, objects);
        List<CacheIotRule> cloudRules = this.toAddCacheIotRule(
                templateTypeModel, templateKey, objects,
                snapshot.cloudRuleCodes, TerminalEnum.CLOUD);
        List<CacheIotRule> edgeRules = this.toAddCacheIotRule(
                templateTypeModel, templateKey, objects,
                snapshot.edgeRuleCodes, TerminalEnum.EDGE);
        return () -> {
            this.cacheIotRuleRepository.removeByKeys(Collections.singletonList(templateKey));
            this.cacheIotRuleRepository.add(CollectionUtils.union(cloudRules, edgeRules));
        };
    }

    @Override
    public @NonNull CacheWriteAction prepareModelRemove(
            @NonNull Collection<String> templateKeys,
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        List<String> keys = new ArrayList<>(templateKeys);
        return () -> this.cacheRuleRepository.removeByKeys(keys);
    }

    @Override
    public @NonNull CacheWriteAction prepareRwdRemove(
            @NonNull Collection<String> templateKeys,
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        List<String> keys = new ArrayList<>(templateKeys);
        return () -> this.cacheIotRuleRepository.removeByKeys(keys);
    }

    /**
     * 在删除任何缓存前查询当前模板的云端、边缘端规则信息点快照。
     *
     * @param templateTypeModel 模板类型模型
     * @param objects 当前模板键的完整对象范围
     * @return 云端和边缘端规则信息点快照
     * @since v7.260701.0
     */
    private @NonNull RuleCodeSnapshot prepareRuleCodeSnapshot(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull List<DtpRwdObjectDTO> objects) {
        RunningMonitorTypeEnum runningMonitorType = templateTypeModel.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType =
                templateTypeModel.getRunningMonitorSecondType();
        Long templateTypeId = templateTypeModel.getTemplateTypeId();
        InfoCodeTemplateTypeEnum templateType = templateTypeModel.getTemplateType();

        InfoCodeInitByObjectParam objectParam = new InfoCodeInitByObjectParam();
        objectParam.setObjects(objects);
        objectParam.setRunningMonitorType(runningMonitorType);
        objectParam.setRunningMonitorSecondType(runningMonitorSecondType);
        objectParam.setTemplateTypeIds(Sets.newHashSet(templateTypeId));
        objectParam.setTemplateTypes(Lists.newArrayList(templateType));
        objectParam.setTemplateTypeModels(Lists.newArrayList(templateTypeModel));
        objectParam.setForceMathTemplate(true);
        objectParam.setQueryIotInfoCodeRule(false);
        InfoCodeAggregateInitContext context =
                new InfoCodeAggregateInitContext(objectParam, this.awares);
        context.fillTemplateTypes();
        context.fillInfoCodeConfigs();
        Set<String> infoCodes = Optional.ofNullable(
                        context.getTemplateTypeIdAndInfoCodeConfigs())
                .map(map -> map.get(templateTypeId))
                .orElse(Collections.emptyList()).stream()
                .map(InfoCodeConfig::getInfoCode)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(infoCodes)) {
            return new RuleCodeSnapshot(new HashMap<>(), new HashMap<>());
        }
        Map<String, Set<String>> cloudRuleCodes = context.queryObjectIdAndExistsRuleInfoCodes(
                templateTypeId, objects, TerminalEnum.CLOUD);
        Map<String, Set<String>> edgeRuleCodes = context.queryObjectIdAndExistsRuleInfoCodes(
                templateTypeId, objects, TerminalEnum.EDGE);
        return new RuleCodeSnapshot(cloudRuleCodes, edgeRuleCodes);
    }

    /**
     * 根据 RWD 对象和已实例化规则信息码构造新版规则实体。
     *
     * @param objects                        RWD 对象
     * @param objectIdAndExistsRuleInfoCodes 对象 ID 到规则信息点编码
     * @param terminalType                   云端或边缘端
     * @return 新版规则实体
     * @since v7.260701.0
     */
    private @NonNull List<CacheRule> toCacheRules(
            @NonNull List<DtpRwdObjectDTO> objects,
            @NonNull Map<String, Set<String>> objectIdAndExistsRuleInfoCodes,
            @NonNull TerminalEnum terminalType) {
        List<CacheRule> result = new ArrayList<>();
        for (DtpRwdObjectDTO object : objects) {
            String objectId = object.getId();
            for (String infoCode : objectIdAndExistsRuleInfoCodes.getOrDefault(
                    objectId, Collections.emptySet())) {
                CacheRule rule = new CacheRule();
                rule.setObjectId(objectId);
                rule.setInfoCode(infoCode);
                rule.setType(CacheIotRuleTypeEnum.from(terminalType));
                result.add(rule);
            }
        }
        return result;
    }

    /**
     * 根据模板类型模型、对象列表、对象ID和已存在的规则信息码、终端类型，新增的缓存IOT规则列表
     *
     * @param templateTypeModel              模板类型模型
     * @param templateKey                    模板键
     * @param objects                        对象列表
     * @param objectIdAndExistsRuleInfoCodes 对象ID和已存在的规则信息码
     * @param terminalType                   终端类型
     * @return 新增的缓存IOT规则列表
     * @since 7.260302.0
     */
    private List<CacheIotRule> toAddCacheIotRule(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull String templateKey,
            @NonNull List<DtpRwdObjectDTO> objects,
            @NonNull Map<String, Set<String>> objectIdAndExistsRuleInfoCodes,
            @NonNull TerminalEnum terminalType) {
        RunningMonitorTypeEnum runningMonitorType = templateTypeModel.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = templateTypeModel.getRunningMonitorSecondType();

        List<CacheIotRule> cacheIotRules = new ArrayList<>();
        for (DtpRwdObjectDTO object : objects) {
            String objectId = object.getId();
            Set<String> existsRuleInfoCodes = objectIdAndExistsRuleInfoCodes.getOrDefault(objectId, Collections.emptySet());
            for (String infoCode : existsRuleInfoCodes) {
                CacheIotRule addIotRule = new CacheIotRule();
                addIotRule.setProjectId(ContextUtil.getProjectId());
                addIotRule.setRunningMonitorType(runningMonitorType);
                addIotRule.setRunningMonitorSecondType(runningMonitorSecondType);
                addIotRule.setCustomKey(templateKey);
                addIotRule.setObjectId(objectId);
                addIotRule.setInfoCode(infoCode);
                addIotRule.setType(CacheIotRuleTypeEnum.from(terminalType));
                cacheIotRules.add(addIotRule);
            }
        }
        return cacheIotRules;
    }

    /**
     * 单个模板的云端、边缘端规则信息点快照。
     *
     * @since v7.260701.0
     */
    private static final class RuleCodeSnapshot {

        /**
         * 云端对象规则信息点。
         *
         * @since v7.260701.0
         */
        private final @NonNull Map<String, Set<String>> cloudRuleCodes;

        /**
         * 边缘端对象规则信息点。
         *
         * @since v7.260701.0
         */
        private final @NonNull Map<String, Set<String>> edgeRuleCodes;

        /**
         * 创建规则信息点快照。
         *
         * @param cloudRuleCodes 云端对象规则信息点
         * @param edgeRuleCodes 边缘端对象规则信息点
         * @since v7.260701.0
         */
        private RuleCodeSnapshot(
                @NonNull Map<String, Set<String>> cloudRuleCodes,
                @NonNull Map<String, Set<String>> edgeRuleCodes) {
            super();
            this.cloudRuleCodes = cloudRuleCodes;
            this.edgeRuleCodes = edgeRuleCodes;
        }
    }
}
