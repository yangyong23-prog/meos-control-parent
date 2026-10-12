package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigResultBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigResultVO;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.*;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

/**
 * Demand-config info-code analyze strategy.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component(DemandConfigInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "demandConfigInfoCodeAnalyzeStrategy";

    private final @NonNull MidCalendarClientService midCalendarClientService;
    private final @NonNull DemandConfigGateway<DemandConfigAirconditioningValueObject> demandConfigGateway;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.DEMAND_CONFIG;
    }

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        String dataType = String.class.getSimpleName();

        for (InfoCodeAggregate aggregate : aggregates) {
            String infoCode = aggregate.getConfigModel().getInfoCode();
            String infoCodeAliasName = aggregate.getConfigModel().getInfoCodeAliasName();

            InfoCodeDefineModel defineModel = InfoCodeDefineModel.ofCustom(
                    infoCode,
                    LocaleMessageUtil.getMessage("key_ce4a33050fe14ea6af865b5e81efe28c"),
                    DtpRwdClassCodeEnum.SPACE.value(),
                    dataType);
            aggregate.setInfoCodeAndDefineModel(defineModel.setAliasName(infoCodeAliasName));
        }
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        Set<String> allObjectIds = InfoCodeAggregate.listAllObjectIds(aggregates);
        Map<String, Map<String, ValueObject>> configMap = this.mapSpaceIdAndDemandConfig(allObjectIds);

        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            Map<String, ValueObject> configByInfoCode = configMap.get(infoCode);
            if (configByInfoCode != null) {
                ValueObject valueObject = configByInfoCode.get(objectId);
                if (valueObject != null) {
                    aggregate.setInfoCodeAndDataModel(
                            new InfoCodeDataModel(infoCode, JSONObject.toJSON(valueObject), DateUtils.now(), null));
                }
            } else if (InfoCodeEnum.RELATIVE_PRESSURE_DIFF.getCode().equals(infoCode)) {
                aggregate.setInvalid(true);
            }
        }
    }

    private @NonNull Map<String, Map<String, ValueObject>> mapSpaceIdAndDemandConfig(
            @NonNull Set<String> spaceIds) {
        if (spaceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, ValueObject>> result = new HashMap<>(16);
        Map<String, ValueObject> spaceIdAndTemperatureDemandConfigs =
                new HashMap<>(Math.max(spaceIds.size(), 16));
        Map<String, ValueObject> spaceIdAndCarbonDioxideDemandConfigs =
                new HashMap<>(Math.max(spaceIds.size(), 16));
        Map<String, ValueObject> spaceIdAndRhDemandConfigs =
                new HashMap<>(Math.max(spaceIds.size(), 16));
        Map<String, ValueObject> spaceIdAndPressureDiffDemandConfigs =
                new HashMap<>(Math.max(spaceIds.size(), 16));

        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("QueryDemandConfig");
        try (PerformanceTracker.SafeNode ignored = tracker.track("mapSpaceIdAndDemandConfig")) {
            LocalDate now = DateUtils.now().toLocalDate();
            MidCalendarAirSeasonTypeEnum airSeasonType =
                    this.midCalendarClientService.getAirSeasonType(ContextUtil.getProjectId(), now);
            tracker.recordStep("load-air-season");

            BaseDemandTemplateParam baseDemandTemplateParam = new BaseDemandTemplateParam();
            baseDemandTemplateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
            baseDemandTemplateParam.setDate(now);
            DemandTemplate demandTemplate = baseDemandTemplateParam.queryAndGetTemplate();
            tracker.recordStep("query-demand-template");
            if (demandTemplate == null) {
                super.logProjectInstance().warn("No demand template found");
                return result;
            }

            DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
            param.setTemplateId(demandTemplate.getId());
            param.setDate(now);
            param.setFillInstanceName(false);
            param.setInstanceType(DemandInstanceTypeEnum.SPACE);
            param.setInstanceIds(spaceIds);
            List<DemandConfigResultVO<DemandConfigAirconditioningValueObject>> configs =
                    this.demandConfigGateway.listConfigResultsByInstances(param);
            tracker.recordStep("query-demand-config");
            if (configs.isEmpty()) {
                return result;
            }
            configs.forEach(configResult -> {
                String instanceId = configResult.getInstanceId();
                try {
                    DemandConfigAirconditioningValueObject globalConfig =
                            this.getAirconditioningGlobalConfig(configResult.getFinalConfig());
                    if (ObjectUtil.isNull(globalConfig)) {
                        return;
                    }

                    DemandConfigAirconditioningEnvironmentQualityObject environmentQuality =
                            globalConfig.getEnvironmentQuality();
                    if (ObjectUtil.isNull(environmentQuality)) {
                        return;
                    }

                    DemandConfigAirconditioningTdbQualityValueObject tdbQuality = environmentQuality.getTdbQuality();
                    if (Objects.nonNull(airSeasonType) && Objects.nonNull(tdbQuality)) {
                        DemandConfigAirconditioningTdbSeasonValueObject currentSeasonQuality =
                                tdbQuality.getCurrentSeasonQuality(airSeasonType);
                        if (ObjectUtil.isNotNull(currentSeasonQuality)) {
                            spaceIdAndTemperatureDemandConfigs.put(instanceId, currentSeasonQuality);
                        }
                    }
                    DemandConfigAirconditioningCO2QualityValueObject co2Quality = environmentQuality.getCO2Quality();
                    spaceIdAndCarbonDioxideDemandConfigs.put(instanceId, co2Quality);
                    DemandConfigAirconditioningRhQualityValueObject rhQuality = environmentQuality.getRhQuality();
                    if (Objects.nonNull(airSeasonType) && Objects.nonNull(rhQuality)) {
                        DemandConfigAirconditioningRhSeasonValueObject currentSeasonQuality =
                                rhQuality.getCurrentSeasonQuality(airSeasonType);
                        if (ObjectUtil.isNotNull(currentSeasonQuality)) {
                            spaceIdAndRhDemandConfigs.put(instanceId, currentSeasonQuality);
                        }
                    }
                    if (Boolean.TRUE.equals(environmentQuality.getIsPressureDiffQualityEnabled())
                            && Objects.nonNull(environmentQuality.getPressureDiffQuality())) {
                        spaceIdAndPressureDiffDemandConfigs.put(instanceId, environmentQuality.getPressureDiffQuality());
                    }
                } catch (Exception exception) {
                    super.logProjectInstance()
                            .warn("Failed to map demand config, instanceId: " + instanceId, exception);
                }
            });

            result.put(DtpRwdSpaceDTO.Fields.tdb, spaceIdAndTemperatureDemandConfigs);
            result.put(DtpRwdSpaceDTO.Fields.co2, spaceIdAndCarbonDioxideDemandConfigs);
            result.put(DtpRwdSpaceDTO.Fields.rh, spaceIdAndRhDemandConfigs);
            if (!spaceIdAndPressureDiffDemandConfigs.isEmpty()) {
                result.put(InfoCodeEnum.RELATIVE_PRESSURE_DIFF.getCode(), spaceIdAndPressureDiffDemandConfigs);
            }
        }
        return result;
    }

    /**
     * 获取空调全局配置.
     *
     * @param finalConfig 最终需求配置
     * @return 空调全局配置，最终配置为空时返回 {@literal null}
     * @since v7.260701.0
     */
    private @Nullable DemandConfigAirconditioningValueObject getAirconditioningGlobalConfig(
            @Nullable DemandConfigValueObject<DemandConfigAirconditioningValueObject> finalConfig) {
        return Objects.isNull(finalConfig) ? null : finalConfig.getGlobalConfig();
    }

    @Override
    public String logTag() {
        return "InfoCodeConfig DemandConfigAnalyzeStrategy";
    }
}
