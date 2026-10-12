package com.persagy.apccontrol.task.lighting;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.config.properties.edge.children.task.child.LightingFeedbackAdjustTaskProperties;
import com.persagy.apccontrol.basic.model.base.CachePersistenceKey;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoBatchSendDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoSendDTO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingFeedbackAdjustTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitFeedbackAdjustSettingDTO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingCircuitValueObject;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingFuncTypeValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.task.BaseEdgeTask;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 照明反馈调节下发任务
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ConditionalOnProperty(prefix = LightingFeedbackAdjustTaskProperties.KEY,
        name = "enabled", havingValue = "true", matchIfMissing = true)
public class LightingFeedbackAdjustTask extends BaseEdgeTask {

    private final DemandLightingStrategy demandLightingStrategy;
    private final DtpTwinClientService dtpTwinClientService;
    private final DemandConfigGateway<DemandConfigLightingValueObject> demandConfigGateway;
    private final RedisCacheTemplate redisCacheTemplate;

    private static final List<DemandTemplateTypeEnum> ALLOWED_TEMPLATE_TYPES = Lists.newArrayList(
            DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING, DemandTemplateTypeEnum.PARKING_LOT_LIGHTING
    );

    @Scheduled(cron = "0 0/5 * * * ?")
    public void execute() {
        initGroupCodeAndProjectId();
        LocalDateTime now = LocalDateTime.now();
        // 上一次下发过指令的空间ID
        Set<String> preSpaceIdsByOrder = Collections.emptySet();
        CachePersistenceKey.Key preOrderKey = CachePersistenceKey.Demand.lightingFeedbackAdjustRecord(getProjectId());
        List<DtpTwinAutoBatchSendDTO> preOrders = redisCacheTemplate.object().get(preOrderKey.getKey());
        if (CollectionUtils.isNotEmpty(preOrders)) {
            preSpaceIdsByOrder = preOrders.stream().map(DtpTwinAutoSendDTO::getObjectId).collect(Collectors.toSet());
        }
        for (DemandTemplateTypeEnum templateType : ALLOWED_TEMPLATE_TYPES) {
            Set<String> spaceIds = demandLightingStrategy.listCircuits(Lists.newArrayList(templateType),
                            null, null).stream()
                    .map(DtpRwdCircuitDTO::getServiceSpaces)
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .map(DtpRwdObjectDTO::getId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(spaceIds)) {
                continue;
            }
            logProjectInstance().info("开始执行空间反馈调节配置下发", Pair.of("templateType", templateType));
            // 当前场景空间ID和配置
            Map<String, DemandConfigAggregate<DemandConfigLightingValueObject>> nowSceneSpaceIdAndConfig =
                    queryNowSceneSpaceIdAndConfig(templateType, now);
            // 处理当前需求类型下每个空间
            List<DtpTwinAutoBatchSendDTO> orders = new ArrayList<>();
            for (String spaceId : spaceIds) {
                DemandConfigAggregate<DemandConfigLightingValueObject> nowSceneConfig =
                        nowSceneSpaceIdAndConfig.get(spaceId);
                // 当前场景空间反馈调节类型
                DemandLightingFeedbackAdjustTypeEnum nowSceneFeedbackAdjustType =
                        parseFeedbackAdjustSettingFromConfig(nowSceneConfig);
                // 解析自动指令
                DtpTwinAutoBatchSendDTO order = parseAutoOrder(spaceId,
                        preSpaceIdsByOrder, nowSceneFeedbackAdjustType);
                logProjectInstance().debug("当前空间反馈调节指令", Arrays.asList(
                        Pair.of("templateType", templateType),
                        Pair.of("spaceId", spaceId),
                        Pair.of("nowSceneConfig", nowSceneFeedbackAdjustType),
                        Pair.of("order", order)
                ));
                if (Objects.isNull(order)) {
                    logProjectInstance().warn("当前空间未解析出反馈调节指令", Arrays.asList(
                            Pair.of("templateType", templateType),
                            Pair.of("spaceId", spaceId),
                            Pair.of("nowSceneConfig", nowSceneFeedbackAdjustType)
                    ));
                    continue;
                }
                orders.add(order);
            }
            if (CollectionUtils.isEmpty(orders)) {
                logProjectInstance().info("不存在需要下发的空间反馈调节指令",
                        Pair.of("templateType", templateType));
            } else {
                logProjectInstance().info("开始下发空间反馈调节指令", Arrays.asList(
                        Pair.of("templateType", templateType),
                        Pair.of("orders", orders)
                ));
                this.dtpTwinClientService.autoCommandBatchSend(orders);
                List<DtpTwinAutoBatchSendDTO> validOrders = orders.stream()
                        .filter(item -> !item.allClear())
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(validOrders)) {
                    this.redisCacheTemplate.object().set(preOrderKey.getKey(), validOrders, preOrderKey.getExpire());
                } else {
                    this.redisCacheTemplate.del(preOrderKey.getKey());
                }
            }

            // 回路级反馈调节条件下发
            this.executeCircuitFeedbackAdjust(templateType, nowSceneSpaceIdAndConfig);
        }
    }

    /**
     * 解析自动指令
     *
     * @param objectId                      对象ID
     * @param preSpaceIdsByOrder            上次下发的空间ID
     * @param nowSceneFeedbackAdjustType 当前场景空间反馈调节类型
     * @return 自动指令
     * @since 7.250602.0
     */
    private @Nullable DtpTwinAutoBatchSendDTO parseAutoOrder(
            @NonNull String objectId,
            @Nullable Set<String> preSpaceIdsByOrder,
            @Nullable DemandLightingFeedbackAdjustTypeEnum nowSceneFeedbackAdjustType) {
        // 上一个场景和当前场景均无配置时不执行任何操作
        if ((CollectionUtils.isEmpty(preSpaceIdsByOrder) || !preSpaceIdsByOrder.contains(objectId))
                && Objects.isNull(nowSceneFeedbackAdjustType)) {
            return null;
        }
        // 当前场景只要有配置, 就按当前场景配置下发
        // 当前场景无配置, 上一个场景有配置时, 进行清空已经下发的指令
        Map<String, Object> orders = parseOrders(nowSceneFeedbackAdjustType);
        if (MapUtils.isEmpty(orders)) {
            return null;
        }
        DtpTwinAutoBatchSendDTO order = new DtpTwinAutoBatchSendDTO();
        order.setObjectId(objectId);
        order.setData(orders);
        return order;
    }

    /**
     * 解析指令集
     *
     * @param feedbackAdjustType 反馈调节类型, 为空时则清除配置
     * @return 指令集
     * @since 7.250602.0
     */
    private @NonNull Map<String, Object> parseOrders(
            @Nullable DemandLightingFeedbackAdjustTypeEnum feedbackAdjustType) {
        BigDecimal illumFbCtrType = BigDecimal.valueOf(3);
        if (Objects.nonNull(feedbackAdjustType)) {
            illumFbCtrType = feedbackAdjustType.parseIllumFbCtrTypeOrder();
        }
        Map<String, Object> orders = new LinkedHashMap<>();
        orders.put(InfoCodeEnum.ILLUM_FB_CTR_TYPE.getValue(), illumFbCtrType);
        return orders;
    }

    /**
     * 执行回路级反馈调节条件下发
     *
     * @param templateType               模版类型
     * @param nowSceneSpaceIdAndConfig  当前场景空间ID和配置
     * @since 7.260801.0
     */
    private void executeCircuitFeedbackAdjust(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Map<String, DemandConfigAggregate<DemandConfigLightingValueObject>> nowSceneSpaceIdAndConfig) {
        // 1. 提取所有空间的回路级反馈调节条件（按回路ID去重）
        Map<String, DemandLightingCircuitFeedbackAdjustSettingDTO> circuitFeedbackSettings = new HashMap<>();
        for (DemandConfigAggregate<DemandConfigLightingValueObject> config : nowSceneSpaceIdAndConfig.values()) {
            Map<String, DemandLightingCircuitFeedbackAdjustSettingDTO> circuits =
                    this.parseCircuitFeedbackAdjustSettings(config);
            circuitFeedbackSettings.putAll(circuits);
        }
        if (circuitFeedbackSettings.isEmpty()) {
            logProjectInstance().info("不存在需要下发的回路反馈调节指令",
                    Pair.of("templateType", templateType));
            return;
        }

        // 2. 查询上次下发的回路指令
        Set<String> preCircuitIdsByOrder = Collections.emptySet();
        CachePersistenceKey.Key preCircuitOrderKey =
                CachePersistenceKey.Demand.lightingCircuitFeedbackAdjustRecord(this.getProjectId());
        List<DtpTwinAutoBatchSendDTO> preCircuitOrders =
                this.redisCacheTemplate.object().get(preCircuitOrderKey.getKey());
        if (CollectionUtils.isNotEmpty(preCircuitOrders)) {
            preCircuitIdsByOrder = preCircuitOrders.stream()
                    .map(DtpTwinAutoSendDTO::getObjectId).collect(Collectors.toSet());
        }

        // 3. 生成回路指令
        List<DtpTwinAutoBatchSendDTO> circuitOrders = new ArrayList<>();
        for (Map.Entry<String, DemandLightingCircuitFeedbackAdjustSettingDTO> entry :
                circuitFeedbackSettings.entrySet()) {
            String circuitId = entry.getKey();
            DemandLightingCircuitFeedbackAdjustSettingDTO setting = entry.getValue();
            DtpTwinAutoBatchSendDTO order = this.parseCircuitAutoOrder(
                    circuitId, preCircuitIdsByOrder, setting);
            if (Objects.nonNull(order)) {
                circuitOrders.add(order);
            }
        }
        // 4. 处理上次有但本次无的回路（清空条件）
        for (String preCircuitId : preCircuitIdsByOrder) {
            if (!circuitFeedbackSettings.containsKey(preCircuitId)) {
                DtpTwinAutoBatchSendDTO order = this.parseCircuitAutoOrder(
                        preCircuitId, preCircuitIdsByOrder, null);
                if (Objects.nonNull(order)) {
                    circuitOrders.add(order);
                }
            }
        }

        if (CollectionUtils.isEmpty(circuitOrders)) {
            logProjectInstance().info("不存在需要下发的回路反馈调节指令",
                    Pair.of("templateType", templateType));
            return;
        }
        logProjectInstance().info("开始下发回路反馈调节指令", Arrays.asList(
                Pair.of("templateType", templateType),
                Pair.of("orders", circuitOrders)
        ));
        this.dtpTwinClientService.autoCommandBatchSend(circuitOrders);
        List<DtpTwinAutoBatchSendDTO> validCircuitOrders = circuitOrders.stream()
                .filter(item -> !item.allClear())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(validCircuitOrders)) {
            this.redisCacheTemplate.object().set(preCircuitOrderKey.getKey(),
                    validCircuitOrders, preCircuitOrderKey.getExpire());
        } else {
            this.redisCacheTemplate.del(preCircuitOrderKey.getKey());
        }
    }

    /**
     * 从空间配置中提取回路级反馈调节条件
     *
     * @param config 配置
     * @return circuitId -> feedbackAdjustSetting 映射
     * @since 7.260801.0
     */
    private @NonNull Map<String, DemandLightingCircuitFeedbackAdjustSettingDTO> parseCircuitFeedbackAdjustSettings(
            @Nullable DemandConfigAggregate<DemandConfigLightingValueObject> config) {
        Map<String, DemandLightingCircuitFeedbackAdjustSettingDTO> result = new HashMap<>();
        if (Objects.isNull(config)) {
            return result;
        }
        DemandConfigValueObject<DemandConfigLightingValueObject> finalConfig = config.getFinalConfig();
        if (Objects.isNull(finalConfig)) {
            return result;
        }
        DemandConfigLightingValueObject globalConfig = finalConfig.getGlobalConfig();
        if (Objects.isNull(globalConfig)) {
            return result;
        }
        List<DemandConfigLightingFuncTypeValueObject> globalSettings = globalConfig.getGlobalSettings();
        if (CollectionUtils.isEmpty(globalSettings)) {
            return result;
        }
        for (DemandConfigLightingFuncTypeValueObject funcTypeSetting : globalSettings) {
            List<DemandConfigLightingCircuitValueObject> circuitSettings =
                    funcTypeSetting.getCircuitSettings();
            if (CollectionUtils.isEmpty(circuitSettings)) {
                continue;
            }
            for (DemandConfigLightingCircuitValueObject circuitSetting : circuitSettings) {
                if (circuitSetting.getSettingMode() == DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT
                        && Objects.nonNull(circuitSetting.getFeedbackAdjustSetting())) {
                    result.put(circuitSetting.getCircuitId(),
                            circuitSetting.getFeedbackAdjustSetting());
                }
            }
        }
        return result;
    }

    /**
     * 解析回路级自动指令
     *
     * @param circuitId                 回路ID
     * @param preCircuitIdsByOrder      上次下发的回路ID
     * @param feedbackAdjustSetting     当前回路反馈调节条件，为空时清空
     * @return 自动指令
     * @since 7.260801.0
     */
    private @Nullable DtpTwinAutoBatchSendDTO parseCircuitAutoOrder(
            @NonNull String circuitId,
            @Nullable Set<String> preCircuitIdsByOrder,
            @Nullable DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting) {
        if ((CollectionUtils.isEmpty(preCircuitIdsByOrder) || !preCircuitIdsByOrder.contains(circuitId))
                && Objects.isNull(feedbackAdjustSetting)) {
            return null;
        }
        Map<String, Object> orders = this.parseCircuitOrders(feedbackAdjustSetting);
        DtpTwinAutoBatchSendDTO order = new DtpTwinAutoBatchSendDTO();
        order.setObjectId(circuitId);
        order.setData(orders);
        return order;
    }

    /**
     * 解析回路级指令集（4个条件信息点）
     *
     * @param feedbackAdjustSetting 回馈调节条件，为空时清空全部
     * @return 指令集
     * @since 7.260801.0
     */
    private @NonNull Map<String, Object> parseCircuitOrders(
            @Nullable DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting) {
        Map<String, Object> orders = new LinkedHashMap<>();
        if (Objects.nonNull(feedbackAdjustSetting)) {
            orders.put(InfoCodeEnum.OUTDOOR_ILLUMINATION_VALUE_TO_TURN_ON_GENERAL_LIGHTING.getValue(),
                    feedbackAdjustSetting.getUnderOutdoorIlluminance());
            orders.put(InfoCodeEnum.FEEDBACKOF_LIGHTING_TURNON_LAST_TIME.getValue(),
                    feedbackAdjustSetting.getUnderDuration());
            orders.put(InfoCodeEnum.OUTDOOR_ILLUMINATION_VALUE_TO_TURN_OFF_GENERAL_LIGHTING.getValue(),
                    feedbackAdjustSetting.getOvertopOutdoorIlluminance());
            orders.put(InfoCodeEnum.FEEDBACKOF_LIGHTING_TURNOFF_LAST_TIME.getValue(),
                    feedbackAdjustSetting.getOvertopDuration());
        } else {
            orders.put(InfoCodeEnum.OUTDOOR_ILLUMINATION_VALUE_TO_TURN_ON_GENERAL_LIGHTING.getValue(), null);
            orders.put(InfoCodeEnum.FEEDBACKOF_LIGHTING_TURNON_LAST_TIME.getValue(), null);
            orders.put(InfoCodeEnum.OUTDOOR_ILLUMINATION_VALUE_TO_TURN_OFF_GENERAL_LIGHTING.getValue(), null);
            orders.put(InfoCodeEnum.FEEDBACKOF_LIGHTING_TURNOFF_LAST_TIME.getValue(), null);
        }
        return orders;
    }

    /**
     * 从配置中解析反馈调节配置
     *
     * @param config 配置
     * @return 反馈调节配置
     * @since 7.250602.0
     */
    private @Nullable DemandLightingFeedbackAdjustTypeEnum parseFeedbackAdjustSettingFromConfig(
            @Nullable DemandConfigAggregate<DemandConfigLightingValueObject> config) {
        if (Objects.isNull(config)) {
            return null;
        }
        DemandConfigValueObject<DemandConfigLightingValueObject> finalConfig = config.getFinalConfig();
        if (Objects.isNull(finalConfig)) {
            return null;
        }
        DemandConfigLightingValueObject globalConfig = finalConfig.getGlobalConfig();
        if (Objects.isNull(globalConfig)) {
            return null;
        }
        List<DemandConfigLightingFuncTypeValueObject> globalSettings = globalConfig.getGlobalSettings();
        if (CollectionUtils.isEmpty(globalSettings)) {
            return null;
        }
        for (DemandConfigLightingFuncTypeValueObject globalSetting : globalSettings) {
            DemandLightingFeedbackAdjustTypeEnum spaceFeedbackAdjustType = globalSetting.getSpaceFeedbackAdjustType();
            if (Objects.nonNull(spaceFeedbackAdjustType)) {
                return spaceFeedbackAdjustType;
            }
        }
        return null;
    }

    /**
     * 查询当前场景空间ID和配置
     *
     * @param templateType 模版类型
     * @param dateTime     当前时间
     * @return 当前场景的空间ID-配置
     * @since 7.250602.0
     */
    private Map<String, DemandConfigAggregate<DemandConfigLightingValueObject>> queryNowSceneSpaceIdAndConfig(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDateTime dateTime) {
        List<LightingSceneDTO> scenes = demandLightingStrategy.listScenesByTime(
                getProjectId(), templateType, dateTime.toLocalDate());
        Optional<LightingSceneDTO> sceneOpt = scenes.stream()
                .filter(item -> BooleanUtils.isTrue(item.getIsCurrentScene()))
                .findFirst();
        if (!sceneOpt.isPresent()) {
            logProjectInstance().warn("当前时段无场景配置", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("dateTime", dateTime)
            ));
            return Collections.emptyMap();
        }
        LightingSceneDTO scene = sceneOpt.get();
        Long templateId = scene.getRelDemandTemplateId();
        Long sceneId = scene.getSceneId();
        return queryExistsFeedbackAdjustSpaceIdAndConfig(templateId, sceneId);
    }

    /**
     * 查询存在反馈调节的空间ID和配置
     *
     * @param templateId 模板ID
     * @param sceneId    场景ID
     * @return 空间ID-配置
     * @since 7.250602.0
     */
    private Map<String, DemandConfigAggregate<DemandConfigLightingValueObject>>
    queryExistsFeedbackAdjustSpaceIdAndConfig(
            @NonNull Long templateId,
            @NonNull Long sceneId) {
        DemandConfigByInstanceBatchQueryParam queryParam = new DemandConfigByInstanceBatchQueryParam();
        queryParam.setTemplateId(templateId);
        queryParam.setSceneId(sceneId);
        queryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(DemandInstanceTypeEnum.SPACE, null));
        queryParam.setAutoFillEmptyInstanceIds(true);
        queryParam.setFilterShieldedSpaces(true);
        List<DemandConfigAggregate<DemandConfigLightingValueObject>> aggregates =
                demandConfigGateway.listConfigsByInstances(queryParam);
        if (CollectionUtils.isEmpty(aggregates)) {
            logProjectInstance().warn("当前场景下无空间配置", Arrays.asList(
                    Pair.of("templateId", templateId),
                    Pair.of("sceneId", sceneId)
            ));
            return Collections.emptyMap();
        }
        return aggregates.stream().collect(Collectors.toMap(item ->
                        item.getIdentifier().getInstance().getInstanceId(), Function.identity(),
                (o1, o2) -> o1));
    }

    @Override
    public String logTag() {
        return "照明需求-空间反馈调节配置下发";
    }
}
