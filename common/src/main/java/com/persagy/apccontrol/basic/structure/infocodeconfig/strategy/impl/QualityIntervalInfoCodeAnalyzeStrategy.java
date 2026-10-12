package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalCarbonDioxideIntervalEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalTemperatureIntervalEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalQualityIntervalQueryParam;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalQualityIntervalVO;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.*;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTackLogicDemandTargetModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 品质区间信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
@Component(QualityIntervalInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class QualityIntervalInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "equipmentQualityIntervalInfoCodeAnalyzeStrategy";

    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final MidCalendarClientService midCalendarClientService;
    private final AirTerminalService airTerminalService;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.QUALITY_INTERVAL;
    }

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        String projectId = ContextUtil.getProjectId();
        String dataType = String.class.getSimpleName();

        AirTerminalQualityIntervalVO qualityInterval = airTerminalService.getQualityInterval(
                new AirTerminalQualityIntervalQueryParam().setProjectId(projectId));
        for (InfoCodeAggregate aggregate : aggregates) {
            String infoCode = aggregate.getConfigModel().getInfoCode();
            String infoCodeAliasName = aggregate.getConfigModel().getInfoCodeAliasName();

            InfoCodeDefineModel defineModel = InfoCodeDefineModel.ofCustom(
                    infoCode, LocaleMessageUtil.getMessage("key_176126eae25b4e43843b1dacb95710b6"), DtpRwdClassCodeEnum.SPACE.value(), dataType);
            List<BaseEnumVO> intervals;
            switch (infoCode) {
                case DtpRwdSpaceDTO.Fields.tdb:
                    intervals = qualityInterval.getTemperatureIntervals();
                    break;
                case DtpRwdSpaceDTO.Fields.co2:
                    intervals = qualityInterval.getCarbonDioxideIntervals();
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply("QualityIntervalInfoCode", infoCode);
            }
            defineModel.setDataSources(intervals.stream()
                    .map(item -> new InfoCodeDataSourceDefineModel(item.getKey().toString(), item.getValue().toString()))
                    .collect(Collectors.toList()));
            aggregate.setInfoCodeAndDefineModel(defineModel.setAliasName(infoCodeAliasName));
        }
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        String projectId = ContextUtil.getProjectId();
        Set<String> allObjectIds = InfoCodeAggregate.listAllObjectIds(aggregates);

        Set<String> allIotInfoCodes = listAllIotInfoCodes(aggregates);

        // 查询IOT值
        Map<String, Map<String, InfoCodeValueVO>> objectIdInfoCodeAndValue =
                mapObjectIdInfoCodeAndValue(allObjectIds, allIotInfoCodes);
        // 查询空调季
        MidCalendarAirSeasonTypeEnum airSeasonType = midCalendarClientService
                .getAirSeasonType(projectId, DateUtils.now().toLocalDate());

        DemandConfigAirconditioningValueObject projectDemandConfig = airTerminalService.queryDemandProjectGlobalConfig();
        DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality = null;
        DemandConfigAirconditioningCO2QualityValueObject co2Quality = null;
        if (Objects.nonNull(projectDemandConfig)) {
            DemandConfigAirconditioningEnvironmentQualityObject environmentQuality =
                    projectDemandConfig.getEnvironmentQuality();
            if (Objects.nonNull(environmentQuality)) {
                DemandConfigAirconditioningTdbQualityValueObject tdbQuality = environmentQuality.getTdbQuality();
                if (Objects.nonNull(tdbQuality)) {
                    tdbSeasonQuality = tdbQuality.getCurrentSeasonQuality(airSeasonType);
                }
                co2Quality = environmentQuality.getCO2Quality();
            }
        }

        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();

            InfoCodeTackLogicDemandTargetModel tackValueLogicModel =
                    aggregate.getConfigModel().parseTackValueLogicModelByDemandType();
            InfoCodeTackLogicDemandTargetModel.TargetTypeEnum type = tackValueLogicModel.getCode();
            String infoCode = type.getValue();
            Map<String, InfoCodeValueVO> infoCodeAndValue = objectIdInfoCodeAndValue
                    .getOrDefault(objectId, Collections.emptyMap());

            String dataValue = null;
            LocalDateTime dataTime = null;

            InfoCodeValueVO infoCodeValue = infoCodeAndValue.get(infoCode);
            if (Objects.isNull(infoCodeValue)) {
                logProjectInstance(projectId).debug("对象无IOT值", Arrays.asList(
                        Pair.of("objectId", objectId),
                        Pair.of("infoCode", infoCode)
                ));
            } else {
                if (Objects.isNull(infoCodeValue.getInfoCodeValue())) {
                    logProjectInstance(projectId).debug("对象IOT值为空", Arrays.asList(
                            Pair.of("objectId", objectId),
                            Pair.of("infoCode", infoCode)
                    ));
                } else {
                    BigDecimal value = new BigDecimal(infoCodeValue.getInfoCodeValue().toString());
                    switch (infoCode) {
                        case DtpRwdSpaceDTO.Fields.tdb:
                            AirTerminalTemperatureIntervalEnum temperatureInterval = airTerminalService
                                    .calculateTemperatureInterval(value, tdbSeasonQuality);
                            if (Objects.nonNull(temperatureInterval)) {
                                dataValue = temperatureInterval.value();
                            }
                            break;
                        case DtpRwdSpaceDTO.Fields.co2:
                            AirTerminalCarbonDioxideIntervalEnum carbonDioxideInterval = airTerminalService
                                    .calculateCarbonDioxideInterval(value, co2Quality);
                            if (Objects.nonNull(carbonDioxideInterval)) {
                                dataValue = carbonDioxideInterval.value();
                            }
                            break;
                        default:
                            throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply("QualityIntervalInfoCode", infoCode);
                    }
                    dataTime = infoCodeValue.getUpdateTime();
                }
            }
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, dataValue, dataTime, null));
        }
    }

    /**
     * 获取所有IOT信息点(品质区间 特殊)
     *
     * @param aggregates 聚合根
     * @return 所有对象ID
     * @since 5.2420602.0
     */
    private Set<String> listAllIotInfoCodes(@NonNull Collection<InfoCodeAggregate> aggregates) {
        Set<String> allIotInfoCodes = new HashSet<>(16);
        for (InfoCodeAggregate aggregate : aggregates) {
            InfoCodeTackLogicDemandTargetModel tackValueLogicModel =
                    aggregate.getConfigModel().parseTackValueLogicModelByDemandType();
            InfoCodeTackLogicDemandTargetModel.TargetTypeEnum type = tackValueLogicModel.getCode();
            allIotInfoCodes.add(type.getValue());
        }
        return allIotInfoCodes;

    }

    /**
     * 查询空间ID和值映射
     *
     * @param spaceIds     空间ID
     * @param iotInfoCodes 信息点编码
     * @return 空间ID -> 信息点值
     * @since 5.240702.0
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, InfoCodeValueVO>> mapObjectIdInfoCodeAndValue(
            @NonNull Collection<String> spaceIds, @NonNull Collection<String> iotInfoCodes) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Maps.newHashMap();
        }
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValue(
                ContextUtil.getProjectId(), null, spaceIds, iotInfoCodes);
        Map<String, Map<String, InfoCodeValueVO>> result = new HashMap<>();
        for (InfoCodeValueVO infoCodeValue : infoCodeValues) {
            String objectId = infoCodeValue.getObjectId();
            String infoCode = infoCodeValue.getInfoCode();

            MapExecutor.dynamicPut(result, objectId, infoCode, infoCodeValue, LinkedHashMap::new);
        }
        return result;
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-品质区间";
    }
}
