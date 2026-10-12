package com.persagy.apccontrol.basic.service.demand.validator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitFeedbackAdjustSettingDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.FeedbackConsistencyConflictVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingCircuitValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingFuncTypeValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求管理-照明需求-反馈调节条件多空间一致性校验器
 *
 * <p>回路与空间的关联关系以配置中的 instanceIdAndFeedbackAdjustSettings 为准：
 * 某空间条目中配置了该回路即视为该回路关联该空间（同配置组中未被该回路配置的空间不参与校验）。
 * 校验时排除入参中回路关联的空间（instanceIdAndFeedbackAdjustSettings 的 key，DB 中为旧值，
 * 不排除则永远无法编辑），同一回路在其他空间的反馈调节条件不一致时返回冲突。</p>
 *
 * @author JiYinchuan
 * @since 7.260801.0
 */
@Component
@RequiredArgsConstructor
public class DemandLightingFeedbackConsistencyValidator {

    private final DemandConfigRepository demandConfigRepository;
    private final ObjectMapper objectMapper;

    private static final TypeReference<DemandConfigValueObject<DemandConfigLightingValueObject>> LIGHTING_CONFIG_TYPE =
            new TypeReference<DemandConfigValueObject<DemandConfigLightingValueObject>>() {};

    /**
     * 反馈调节条件多空间一致性校验
     *
     * <p>校验逻辑：
     * 1. 从请求配置的 instanceIdAndFeedbackAdjustSettings 中提取各空间配置的反馈调节回路及条件，
     *    其 key 集合即本次编辑的空间（比对时排除）
     * 2. 查询 DB 同模板同场景下其他空间的 instanceIdAndFeedbackAdjustSettings，
     *    按各自空间条目归属提取，排除本次编辑空间的条目
     * 3. 同一回路在多个空间中的条件不一致时返回冲突</p>
     *
     * @param templateId   模版ID
     * @param sceneId      场景ID
     * @param globalConfig 请求中的配置值对象
     * @return 冲突列表，空列表表示无冲突
     * @since 7.260801.0
     */
    public List<FeedbackConsistencyConflictVO> feedbackConsistencyCheck(
            @NonNull Long templateId, @NonNull Long sceneId,
            @NonNull DemandConfigLightingValueObject globalConfig) {
        List<FeedbackConsistencyConflictVO> conflicts = new ArrayList<>();

        // 1. 提取请求中各空间配置的反馈调节回路：circuitId -> (spaceId -> spaceConfig)
        Map<String, Map<String, FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO>> requestCircuitSpaceConfigs =
                new HashMap<>();
        Set<String> requestSpaceIds = new HashSet<>();
        Map<String, List<DemandConfigLightingFuncTypeValueObject>> requestInstanceSettings =
                globalConfig.getInstanceIdAndFeedbackAdjustSettings();
        if (MapUtils.isNotEmpty(requestInstanceSettings)) {
            for (Map.Entry<String, List<DemandConfigLightingFuncTypeValueObject>> entry : requestInstanceSettings.entrySet()) {
                String spaceId = entry.getKey();
                requestSpaceIds.add(spaceId);
                Map<String, DemandLightingCircuitFeedbackAdjustSettingDTO> spaceCircuits = new HashMap<>();
                this.extractFeedbackCircuits(entry.getValue(), spaceCircuits);
                for (Map.Entry<String, DemandLightingCircuitFeedbackAdjustSettingDTO> circuitEntry : spaceCircuits.entrySet()) {
                    requestCircuitSpaceConfigs.computeIfAbsent(circuitEntry.getKey(), k -> new HashMap<>())
                            .put(spaceId, new FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO(
                                    spaceId, null, null, circuitEntry.getValue()));
                }
            }
        }
        if (requestCircuitSpaceConfigs.isEmpty()) {
            return conflicts;
        }

        // 2. 查询DB中同模板同场景其他空间的配置（排除本次编辑空间），按各自空间条目归属提取
        Map<String, Map<String, FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO>> dbCircuitSpaceConfigs =
                this.collectDbCircuitSpaceConfigs(templateId, sceneId, requestSpaceIds);

        // 3. 逐个回路比对：任一空间条件不一致 → 冲突
        for (Map.Entry<String, Map<String, FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO>> entry : requestCircuitSpaceConfigs.entrySet()) {
            String circuitId = entry.getKey();
            List<FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO> requestSpaceConfigs =
                    new ArrayList<>(entry.getValue().values());
            Set<DemandLightingCircuitFeedbackAdjustSettingDTO> requestSettings = requestSpaceConfigs.stream()
                    .map(FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO::getFeedbackAdjustSetting)
                    .collect(Collectors.toSet());
            // 3.1 请求内部不一致：同一回路在请求多个空间中的条件不同
            if (requestSettings.size() > 1) {
                conflicts.add(this.buildConflict(circuitId, requestSpaceConfigs));
                continue;
            }
            // 3.2 与DB其他空间比对
            Map<String, FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO> dbSpaceConfigs =
                    dbCircuitSpaceConfigs.get(circuitId);
            if (MapUtils.isEmpty(dbSpaceConfigs)) {
                continue;
            }
            List<FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO> conflictSpaceConfigs = new ArrayList<>();
            for (FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO dbSpaceConfig : dbSpaceConfigs.values()) {
                if (!requestSettings.contains(dbSpaceConfig.getFeedbackAdjustSetting())) {
                    conflictSpaceConfigs.add(dbSpaceConfig);
                }
            }
            if (!conflictSpaceConfigs.isEmpty()) {
                List<FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO> spaceConfigs =
                        new ArrayList<>(requestSpaceConfigs);
                spaceConfigs.addAll(conflictSpaceConfigs);
                conflicts.add(this.buildConflict(circuitId, spaceConfigs));
            }
        }
        return conflicts;
    }

    /**
     * 查询DB中同模板同场景的配置，按 instanceIdAndFeedbackAdjustSettings 各自空间条目归属提取，
     * 排除本次编辑空间的条目
     *
     * @param templateId       模版ID
     * @param sceneId          场景ID
     * @param excludeSpaceIds  排除的空间ID（本次编辑空间）
     * @return circuitId -> (spaceId -> spaceConfig)
     */
    @SneakyThrows
    private Map<String, Map<String, FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO>> collectDbCircuitSpaceConfigs(
            @NonNull Long templateId, @NonNull Long sceneId,
            @NonNull Set<String> excludeSpaceIds) {
        Map<String, Map<String, FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO>> result = new HashMap<>();
        DemandConfigByInstanceRepositoryBatchQueryParam queryParam = new DemandConfigByInstanceRepositoryBatchQueryParam();
        queryParam.setTemplateId(templateId);
        queryParam.setSceneId(sceneId);
        List<DemandConfig> dbConfigs = this.demandConfigRepository.listByInstances(queryParam);
        if (CollectionUtils.isEmpty(dbConfigs)) {
            return result;
        }
        for (DemandConfig dbConfig : dbConfigs) {
            JsonNode configData = dbConfig.getConfigData();
            if (configData == null) {
                continue;
            }
            DemandConfigValueObject<DemandConfigLightingValueObject> configValue =
                    this.objectMapper.readValue(configData.toString(), LIGHTING_CONFIG_TYPE);
            DemandConfigLightingValueObject lightingConfig = configValue.getGlobalConfig();
            if (lightingConfig == null) {
                continue;
            }
            Map<String, List<DemandConfigLightingFuncTypeValueObject>> dbInstanceSettings =
                    lightingConfig.getInstanceIdAndFeedbackAdjustSettings();
            if (MapUtils.isEmpty(dbInstanceSettings)) {
                continue;
            }
            for (Map.Entry<String, List<DemandConfigLightingFuncTypeValueObject>> entry : dbInstanceSettings.entrySet()) {
                String spaceId = entry.getKey();
                // 排除本次编辑空间（DB 中为旧值，不排除则永远无法编辑）
                if (excludeSpaceIds.contains(spaceId)) {
                    continue;
                }
                Map<String, DemandLightingCircuitFeedbackAdjustSettingDTO> spaceCircuits = new HashMap<>();
                this.extractFeedbackCircuits(entry.getValue(), spaceCircuits);
                for (Map.Entry<String, DemandLightingCircuitFeedbackAdjustSettingDTO> circuitEntry : spaceCircuits.entrySet()) {
                    result.computeIfAbsent(circuitEntry.getKey(), k -> new HashMap<>())
                            .put(spaceId, new FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO(
                                    spaceId, DemandInstanceTypeEnum.SPACE, null, circuitEntry.getValue()));
                }
            }
        }
        return result;
    }

    /**
     * 构建冲突VO
     *
     * @param circuitId    回路ID
     * @param spaceConfigs 各空间配置详情
     * @return 冲突VO
     */
    private FeedbackConsistencyConflictVO buildConflict(
            @NonNull String circuitId,
            @NonNull List<FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO> spaceConfigs) {
        FeedbackConsistencyConflictVO conflict = new FeedbackConsistencyConflictVO();
        conflict.setCircuitId(circuitId);
        conflict.setSpaceConfigs(spaceConfigs);
        return conflict;
    }

    /**
     * 从功能类型值对象集合中提取 FEEDBACK_ADJUSTMENT 回路
     *
     * @param funcTypeSettings 功能类型值对象集合
     * @param result           结果映射：circuitId -> 条件
     */
    private void extractFeedbackCircuits(
            @Nullable List<DemandConfigLightingFuncTypeValueObject> funcTypeSettings,
            @NonNull Map<String, DemandLightingCircuitFeedbackAdjustSettingDTO> result) {
        if (CollectionUtils.isEmpty(funcTypeSettings)) {
            return;
        }
        for (DemandConfigLightingFuncTypeValueObject funcTypeSetting : funcTypeSettings) {
            if (CollectionUtils.isEmpty(funcTypeSetting.getCircuitSettings())) {
                continue;
            }
            for (DemandConfigLightingCircuitValueObject circuitSetting : funcTypeSetting.getCircuitSettings()) {
                if (circuitSetting.getSettingMode() == DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT
                        && circuitSetting.getFeedbackAdjustSetting() != null) {
                    result.put(circuitSetting.getCircuitId(), circuitSetting.getFeedbackAdjustSetting());
                }
            }
        }
    }
}