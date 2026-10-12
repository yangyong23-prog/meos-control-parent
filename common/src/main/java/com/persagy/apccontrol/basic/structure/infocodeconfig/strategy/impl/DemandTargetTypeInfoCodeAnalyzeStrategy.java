package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.calculate.CalculateResult;
import com.persagy.apccontrol.basic.model.calculate.CalculateResultDetail;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.RcCalculateResult;
import com.persagy.apccontrol.basic.model.enumtype.EnumCalculateValueType;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.resource.RcCalculateResultService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.DemandTargetResultDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTackLogicDemandTargetModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.TimeTypeEnum;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.DateTimeRangeExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求目标类型信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Component(DemandTargetTypeInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandTargetTypeInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "equipmentDemandTargetResultInfoCodeAnalyzeStrategy";

    private final RcCalculateResultService rcCalculateResultService;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.DEMAND_TARGET_TYPE;
    }

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        String dataType = String.class.getSimpleName();

        List<InfoCodeDataSourceDefineModel> dataSources = Arrays.stream(AirTerminalDemandStatusEnum.values())
                .map(item -> new InfoCodeDataSourceDefineModel(item.value(), item.getReasonPhrase()))
                .collect(Collectors.toList());
        for (InfoCodeAggregate aggregate : aggregates) {
            String infoCode = aggregate.getConfigModel().getInfoCode();
            String infoCodeAliasName = aggregate.getConfigModel().getInfoCodeAliasName();

            InfoCodeDefineModel defineModel = InfoCodeDefineModel.ofCustom(
                    infoCode, LocaleMessageUtil.getMessage("key_513c040ad7af4545a5a4deef4d183f6a"), DtpRwdClassCodeEnum.SPACE.value(), dataType);
            defineModel.setDataSources(dataSources);
            aggregate.setInfoCodeAndDefineModel(defineModel.setAliasName(infoCodeAliasName));
        }
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        Set<String> allObjectIds = InfoCodeAggregate.listAllObjectIds(aggregates);

        LocalDateTime now = DateUtils.now();
        LocalDate today = now.toLocalDate();
        LocalTime recent5MinuteEnd = DateUtils.getRecent5Minute(TimeTypeEnum.MINUTE.truncate(now.toLocalTime()));
        Map<String, Map<String, DemandTargetResultDTO>> spaceIdAndDemandStatus =
                mapSpaceIdAndDemandStatus(allObjectIds, today, recent5MinuteEnd);

        LocalDateTime dataTime = LocalDateTime.of(today, recent5MinuteEnd);
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            InfoCodeTackLogicDemandTargetModel tackValueLogicModel =
                    aggregate.getConfigModel().parseTackValueLogicModelByDemandType();

            DemandTargetResultDTO demandStatus = spaceIdAndDemandStatus.getOrDefault(
                    tackValueLogicModel.getCode().getValue(), Collections.emptyMap()).get(objectId);
            AirTerminalDemandStatusEnum demandStatusEnum = Objects.nonNull(demandStatus) ? demandStatus.getDemandStatus() : null;
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, demandStatusEnum, dataTime, null));
        }
    }

    /**
     * 查询空间ID和需求状态映射
     *
     * @param spaceIds 空间ID
     * @param date     日期
     * @param time     时间
     * @return 空间ID -> 需求结果
     * @since 5.240502.0
     */
    private Map<String, Map<String, DemandTargetResultDTO>> mapSpaceIdAndDemandStatus(
            @NonNull Set<String> spaceIds, @NonNull LocalDate date, @NonNull LocalTime time) {
        Map<String, Map<String, DemandTargetResultDTO>> result = new HashMap<>(16);
        String projectId = ContextUtil.getProjectId();

        List<RcCalculateResult> rcCalculateResults = listRcCalculateResult(spaceIds, date);
        if (CollectionUtils.isEmpty(rcCalculateResults)) {
            logProjectInstance(projectId).warn("未查询到价值计算结果", Arrays.asList(
                    Pair.of("spaceIds", spaceIds),
                    Pair.of("date", date)
            ));
            return result;
        }
        Map<String, DemandTargetResultDTO> spaceIdAndTemperatureDemandStatus =
                new HashMap<>(Math.max(spaceIds.size(), 16));
        Map<String, DemandTargetResultDTO> spaceIdAndCarbonDioxideDemandStatus =
                new HashMap<>(Math.max(spaceIds.size(), 16));
        for (RcCalculateResult rcCalculateResult : rcCalculateResults) {
            String spaceId = rcCalculateResult.getSpaceId();
            String workDate = rcCalculateResult.getWorkDate();
            CalculateResult calculateResult = rcCalculateResult.getCalculateResult();
            if (Objects.isNull(calculateResult)) {
                continue;
            }

            TreeMap<LocalTime, CalculateResultDetail> tdb = calculateResult.getTdb();
            TreeMap<LocalTime, CalculateResultDetail> co2 = calculateResult.getCO2();

            DemandTargetResultDTO temperatureDemandStatus = analyzeCalculateValueType(
                    projectId, spaceId, workDate, time, tdb);
            if (Objects.nonNull(temperatureDemandStatus)) {
                spaceIdAndTemperatureDemandStatus.put(spaceId, temperatureDemandStatus);
            }

            DemandTargetResultDTO carbonDioxideDemandStatus = analyzeCalculateValueType
                    (projectId, spaceId, workDate, time, co2);
            if (Objects.nonNull(carbonDioxideDemandStatus)) {
                spaceIdAndCarbonDioxideDemandStatus.put(spaceId, carbonDioxideDemandStatus);
            }
        }
        result.put(DtpRwdSpaceDTO.Fields.tdb, spaceIdAndTemperatureDemandStatus);
        result.put(DtpRwdSpaceDTO.Fields.co2, spaceIdAndCarbonDioxideDemandStatus);
        return result;
    }

    /**
     * 查询一天的价值计算结果
     *
     * @param spaceIds 空间ID
     * @param date     日期
     * @return {@code Map<tdb, Map<spaceId, Map<time, DemandTargetResultDTO>>>}
     */
    public Map<String, Map<String, Map<LocalTime, DemandTargetResultDTO>>> mapSpaceIdAndDemandStatusToDay(
            @NonNull Set<String> spaceIds, @NonNull LocalDate date) {
        Map<String, Map<String, Map<LocalTime, DemandTargetResultDTO>>> result = new HashMap<>(16);
        String projectId = ContextUtil.getProjectId();

        List<RcCalculateResult> rcCalculateResults = listRcCalculateResult(spaceIds, date);
        if (CollectionUtils.isEmpty(rcCalculateResults)) {
            logProjectInstance(projectId).warn("未查询到价值计算结果", Arrays.asList(
                    Pair.of("spaceIds", spaceIds),
                    Pair.of("date", date)
            ));
            return result;
        }
        Map<String, Map<LocalTime, DemandTargetResultDTO>> spaceIdAndTemperatureDemandStatus =
                new HashMap<>(Math.max(spaceIds.size(), 16));
        Map<String, Map<LocalTime, DemandTargetResultDTO>> spaceIdAndCarbonDioxideDemandStatus =
                new HashMap<>(Math.max(spaceIds.size(), 16));
        for (RcCalculateResult rcCalculateResult : rcCalculateResults) {
            String spaceId = rcCalculateResult.getSpaceId();
            String workDate = rcCalculateResult.getWorkDate();
            CalculateResult calculateResult = rcCalculateResult.getCalculateResult();
            if (Objects.isNull(calculateResult)) {
                continue;
            }


            TreeMap<LocalTime, CalculateResultDetail> tdb = calculateResult.getTdb();
            TreeMap<LocalTime, CalculateResultDetail> co2 = calculateResult.getCO2();
            Map<LocalTime, DemandTargetResultDTO> co2TimeMap = new LinkedHashMap<>();
            Map<LocalTime, DemandTargetResultDTO> tdbTimeMap = new LinkedHashMap<>();

            DateTimeRangeExecutor.generateTime(TimeTypeEnum.MINUTE, 5, LocalTime.MIN,
                    LocalTime.MAX, (time) -> {
                        DemandTargetResultDTO temperatureDemandStatus = analyzeCalculateValueType(
                                projectId, spaceId, workDate, time, tdb);
                        if (Objects.nonNull(temperatureDemandStatus)) {
                            tdbTimeMap.put(time, temperatureDemandStatus);
                        }

                        DemandTargetResultDTO carbonDioxideDemandStatus = analyzeCalculateValueType
                                (projectId, spaceId, workDate, time, co2);
                        if (Objects.nonNull(carbonDioxideDemandStatus)) {
                            co2TimeMap.put(time, carbonDioxideDemandStatus);
                        }
                    });
            spaceIdAndTemperatureDemandStatus.put(spaceId, tdbTimeMap);
            spaceIdAndCarbonDioxideDemandStatus.put(spaceId, co2TimeMap);
        }
        result.put(DtpRwdSpaceDTO.Fields.tdb, spaceIdAndTemperatureDemandStatus);
        result.put(DtpRwdSpaceDTO.Fields.co2, spaceIdAndCarbonDioxideDemandStatus);
        return result;
    }

    /**
     * 价值计算结果列表
     *
     * @param spaceIds 空间ID
     * @param date     日期
     * @return {@link List }<{@link RcCalculateResult }>
     */
    public List<RcCalculateResult> listRcCalculateResult(@NonNull Set<String> spaceIds, @NonNull LocalDate date) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Collections.emptyList();
        }

        String projectId = ContextUtil.getProjectId();
        String queryCalculateResultDateString = DateTimeFormatter.ofPattern("yyyyMMdd").format(date);

        return rcCalculateResultService.queryCalculateResultList(
                ContextUtil.getGroupCode(), projectId, spaceIds, queryCalculateResultDateString, null);
    }

    /**
     * 解析需求状态
     *
     * @param projectId 项目ID
     * @param spaceId   空间ID
     * @param workDate  计算日期
     * @param time      计算视角
     * @param source    价值计算结果详情
     * @return 需求状态
     * @since 5.240502.0
     */
    private DemandTargetResultDTO analyzeCalculateValueType(
            @NonNull String projectId, @NonNull String spaceId, @NonNull String workDate,
            @NonNull LocalTime time, @NonNull TreeMap<LocalTime, CalculateResultDetail> source) {
        if (MapUtils.isEmpty(source)) {
            return null;
        }
        CalculateResultDetail detail = source.get(time);
        if (Objects.isNull(detail)) {
            logProjectInstance(projectId).debug("当前时间无价值计算结果", Arrays.asList(
                    Pair.of("spaceId", spaceId),
                    Pair.of("workDate", workDate),
                    Pair.of("calculateTime", time)
            ));
            return null;
        }
        Boolean enable = detail.getEnable();
        if (BooleanUtils.isNotTrue(enable)) {
            logProjectInstance(projectId).debug("当前时间价值计算无需求", Arrays.asList(
                    Pair.of("spaceId", spaceId),
                    Pair.of("workDate", workDate),
                    Pair.of("calculateTime", time)
            ));
            return null;
        }
        String type = detail.getType();
        BigDecimal value = detail.getValue();
        if (StringUtils.isBlank(type)) {
            return null;
        }
        EnumCalculateValueType typeEnum = BaseEnum.analyze(type, EnumCalculateValueType.class, false);
        if (Objects.isNull(typeEnum)) {
            logProjectInstance(projectId).warn("价值计算类型解析失败", Arrays.asList(
                    Pair.of("spaceId", spaceId),
                    Pair.of("workDate", workDate),
                    Pair.of("calculateTime", time),
                    Pair.of("type", type)
            ));
            return null;
        }
        return new DemandTargetResultDTO(AirTerminalDemandStatusEnum.of(typeEnum), value);
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-需求目标结果";
    }
}
