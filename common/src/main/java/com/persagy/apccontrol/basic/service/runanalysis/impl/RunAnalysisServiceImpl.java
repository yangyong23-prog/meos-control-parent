package com.persagy.apccontrol.basic.service.runanalysis.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.builder.GenericBuilder;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractDtpPersistServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.*;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.ElectricityPriceQueryParam;
import com.persagy.apccontrol.basic.model.dto.EnergyDataInfoQueryParam;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherHourHistoryDataDTO;
import com.persagy.apccontrol.basic.model.entity.AnalysisAirConditionerSpace;
import com.persagy.apccontrol.basic.model.entity.HistoryStorageResult;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.EnumTimeType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalTemperatureIntervalEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runanalysis.AnalysisTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runanalysis.GroupTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.*;
import com.persagy.apccontrol.basic.model.po.common.TimeTagPO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.*;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.RunAnalysisConfigVO;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.enums.EnergyModelTagEnum;
import com.persagy.apccontrol.basic.outside.enums.EnergyModelTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.client.EnergyResearchServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.ElectricityData.CarbonCategoryDataParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.SyncHistoryStorageParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarDayDateTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.energyresearch.EnergyModelItemParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectsOfExistRuleQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.ElectricityData.CarbonCategoryDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarDayDateTagResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.energyresearch.EnergyItemResponse;
import com.persagy.apccontrol.basic.outside.feign.service.*;
import com.persagy.apccontrol.basic.resource.HistoryStorageResultService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.service.airterminal.AnalysisAirConditionerSpaceService;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.runanalysis.RunAnalysisService;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorRunAnalysisStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.*;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeGroupModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.DemandTargetResultDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl.DemandTargetTypeInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.number.BigDecimalUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisServiceImpl implements RunAnalysisService {

    private final CommonConfigService commonConfigService;
    private final DtpRwdClientService dtpRwdClientService;
    private final EnergyResearchServerClient energyResearchServerClient;
    private final AbstractDtpPersistServer abstractDtpPersistServer;
    private final MidCalendarClientService midCalendarClientService;
    private final DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;
    private final ElectricityDataClientService electricityDataClientService;
    private final MidPermissionClientService midPermissionClientService;
    private final WeatherClientService weatherClientService;
    private final AnalysisAirConditionerSpaceService analysisAirConditionerSpacesService;
    private final InfoCodeGateway infoCodeGateway;
    private final DemandConfigGateway<DemandConfigAirconditioningValueObject> demandConfigGateway;
    private final DemandTargetTypeInfoCodeAnalyzeStrategy demandTargetTypeInfoCodeAnalyzeStrategy;
    private final HistoryStorageResultService historyStorageResultService;
    private final RunningMonitorRunAnalysisStrategy runningMonitorRunAnalysisStrategy;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final ShellConfigService shellConfigService;


    @Override
    public RunAnalysisStatisticsVO statistics(RunAnalysisBaseParam param) {
        LocalDate queryDate = param.getQueryDate();
        RunAnalysisStatisticsVO vo = new RunAnalysisStatisticsVO();
        List<SystemEfficiencyVO> efficiencyDataList = new ArrayList<>();
        //冷热源和空调冷源不判断空调工况
        SystemEfficiencyVO coldHotEfficiency = buildEfficiency(param, DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.getValue(), COLD_HEAT_INFO_CODES, 3);
        if (Objects.nonNull(coldHotEfficiency)) {
            efficiencyDataList.add(coldHotEfficiency);
        }
        SystemEfficiencyVO airColdEfficiency = buildEfficiency(param, DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue(), COLD_INFO_CODES, 1);
        if (Objects.nonNull(airColdEfficiency)) {
            efficiencyDataList.add(airColdEfficiency);
        }
        //获取当日空调工况
        MidCalendarDateTagValueEnum currentDayDateTag = getCurrentDayDateTag(queryDate);
        if (currentDayDateTag != null) {
            buildValueSelection(param, vo);
            buildEnergyStatisticsList(param, vo, currentDayDateTag);
            if (checkSeasonToCooling(currentDayDateTag)) {
                SystemEfficiencyVO coldEfficiency = buildEfficiency(param, DtpRwdClassCodeEnum.COLD_SYSTEM.getValue(), COLD_INFO_CODES, 2);
                if (Objects.nonNull(coldEfficiency)) {
                    efficiencyDataList.add(coldEfficiency);
                }
            }
        }
        efficiencyDataList.sort(Comparator.comparingInt(SystemEfficiencyVO::getOrder));
        vo.setSystemEfficiencyList(efficiencyDataList);
        return vo;
    }

    /**
     * 检查季节是否是供冷
     *
     * @param currentDayDateTag 当前日期标签
     * @return boolean
     */
    private boolean checkSeasonToCooling(MidCalendarDateTagValueEnum currentDayDateTag) {
        if (currentDayDateTag == null) {
            return false;
        }
        return currentDayDateTag == MidCalendarDateTagValueEnum.SEASON_COOLING;
    }

    /**
     * 获取当日的空调工况
     *
     * @param queryDate 查询日期
     * @return {@link MidCalendarDateTagValueEnum }
     */
    private MidCalendarDateTagValueEnum getCurrentDayDateTag(LocalDate queryDate) {
        MidCalendarDayDateTagQueryParam calendarDayDateTagQueryParam = new MidCalendarDayDateTagQueryParam();
        calendarDayDateTagQueryParam.setObjId(ContextUtil.getProjectId());
        calendarDayDateTagQueryParam.setBeginDate(queryDate);
        calendarDayDateTagQueryParam.setEndDate(queryDate);
        //查询空调工况
        List<MidCalendarDayDateTagResponse> midCalendarDayDateTagResponses = midCalendarClientService.listCalendarDayDateTag(ContextUtil.getProjectId(), calendarDayDateTagQueryParam);
        if (CollectionUtils.isEmpty(midCalendarDayDateTagResponses)) {
            logProjectInstance().error("未查询到空调工况信息，");
            return null;
        }
        MidCalendarDayDateTagResponse midCalendarDayDateTag = midCalendarDayDateTagResponses.get(0);
        List<String> tagList = midCalendarDayDateTag.getTagList();
        if (CollectionUtils.isEmpty(tagList)) {
            logProjectInstance().error("未查询到空调工况信息");
            return null;
        }
        MidCalendarDateTagValueEnum currentDayDateTag = resolveMidCalendarDateTagValueEnum(tagList);
        if (currentDayDateTag == null) {
            logProjectInstance().error("没有符合的空调工况信息", Pair.of("tagList", tagList));
            return null;
        }
        return currentDayDateTag;
    }

    /**
     * 根据tagList解析空调工况
     *
     * @param tagList tag列表
     * @return {@link MidCalendarDateTagValueEnum }
     */
    @Nullable
    private MidCalendarDateTagValueEnum resolveMidCalendarDateTagValueEnum(List<String> tagList) {
        MidCalendarDateTagValueEnum currentDayDateTag = null;
        if (tagList.contains(MidCalendarDateTagValueEnum.SEASON_COOLING.value())) {
            currentDayDateTag = MidCalendarDateTagValueEnum.SEASON_COOLING;
        } else if (tagList.contains(MidCalendarDateTagValueEnum.SEASON_HEATING.value())) {
            currentDayDateTag = MidCalendarDateTagValueEnum.SEASON_HEATING;
        } else if (tagList.contains(MidCalendarDateTagValueEnum.SEASON_TRANSITION.value())) {
            currentDayDateTag = MidCalendarDateTagValueEnum.SEASON_TRANSITION;
        }
        return currentDayDateTag;
    }

    /**
     * 构建效率
     *
     * @param param 参数
     * @param classCode 设备类
     * @param infoCodes 信息点列表
     * @param order 排序
     * @return {@code SystemEfficiencyVO }
     * @since 7.250801.0
     */
    private SystemEfficiencyVO buildEfficiency(RunAnalysisBaseParam param, String classCode, List<String> infoCodes, Integer order) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCode(classCode);
        queryParam.filterMonitoringSurveillanceStatus();
        queryParam.setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.classCode, InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value()));
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(queryParam);
        if (CollectionUtils.isEmpty(dtpRwdObjectDTOS)) {
            return null;
        }
        LocalDate queryDate = param.getQueryDate();
        List<ColdEfficiencyVO> coldEfficiency = new ArrayList<>();
        Set<String> systemIds = dtpRwdObjectDTOS.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
        classCodeAndObjectIds.put(DtpRwdClassCodeEnum.COLD_SYSTEM.value(), systemIds);
        Map<String, Set<String>> ruleSetMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndObjectIds, COLD_INFO_CODES);
        //如果规则为空，直接返回
        if (MapUtils.isEmpty(ruleSetMap)) {
            return null;
        }
        Map<String, DtpKnowledgeInfoCodeDTO> infoCodeKnowledgeInfoCodeMap = dtpKnowledgeClientService.mapInfoCodeAndData(true, false, classCode);
        Map<String, Map<String, List<EnergyDataResponse>>> valueMap = queryRwdObjectValueMap(systemIds, queryDate, EnumTimeType.DAY, infoCodes);
        for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
            String id = dtpRwdObjectDTO.getId();
            Set<String> infoRules = ruleSetMap.get(id);
            if (CollectionUtils.isEmpty(infoRules)) {
                continue;
            }
            ColdEfficiencyVO coldEfficiencyVO = new ColdEfficiencyVO();
            coldEfficiencyVO.setSystemId(id);
            coldEfficiencyVO.setSystemName(dtpRwdObjectDTO.getLocalName());
            coldEfficiencyVO.setClassCode(dtpRwdObjectDTO.getClassCode());
            coldEfficiencyVO.setMonitoringSurveillanceStatus(String.valueOf(dtpRwdObjectDTO
                    .analyzeProperties(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value())));
            Map<String, List<EnergyDataResponse>> valueListMap = valueMap.get(id);
            List<InfoCodeValueVO> dataList = new ArrayList<>();
            for (String infoRule : infoRules) {
                InfoCodeValueVO infoValue = new InfoCodeValueVO();
                infoValue.setInfoCode(infoRule);
                DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO = infoCodeKnowledgeInfoCodeMap.getOrDefault(infoRule, new DtpKnowledgeInfoCodeDTO());
                infoValue.setDataValue(getValue(infoRule, valueListMap));
                infoValue.setInfoCodeName(DashBoardInfoCodeEnum.getName(infoRule));
                infoValue.setUnit(dtpKnowledgeInfoCodeDTO.getUnit());
                dataList.add(infoValue);
            }
            if (CollectionUtils.isNotEmpty(dataList)) {
                //排序
                dataList.sort(Comparator.comparing(e -> {
                    String infoCode = e.getInfoCode();
                    return DashBoardInfoCodeEnum.getOrder(infoCode);
                }));
            }
            coldEfficiencyVO.setDataList(dataList);
            coldEfficiency.add(coldEfficiencyVO);
        }
        if (CollectionUtils.isNotEmpty(coldEfficiency)) {
            //排序
            coldEfficiency = coldEfficiency.stream().filter(x -> StringUtils.isNotBlank(x.getMonitoringSurveillanceStatus()))
                    .sorted(Comparator.comparing(ColdEfficiencyVO::getMonitoringSurveillanceStatus).reversed().thenComparing(ColdEfficiencyVO::getSystemName)).collect(Collectors.toList());
        }
        SystemEfficiencyVO systemEfficiencyVO = new SystemEfficiencyVO();
        systemEfficiencyVO.setClassCode(classCode);
        systemEfficiencyVO.setEfficiencyDataList(coldEfficiency);
        systemEfficiencyVO.setOrder(order);
        return systemEfficiencyVO;
    }


    private BigDecimal getValue(String infoCode, Map<String, List<EnergyDataResponse>> valueListMap) {
        if (MapUtils.isEmpty(valueListMap)) {
            return null;
        }
        List<EnergyDataResponse> energyDataResponses = valueListMap.get(infoCode);
        if (CollectionUtils.isEmpty(energyDataResponses)) {
            return null;
        }
        return energyDataResponses.stream().map(EnergyDataResponse::getIpValue).reduce(BigDecimal::add).orElse(null);
    }

    /**
     * 查询rwd对象值映射
     *
     * @param objectIds 对象列表
     * @param queryDate 查询日期
     * @param ipType    分精度类型
     * @return objectId->infoCode->List<EnergyDataResponse>
     */
    private Map<String, Map<String, List<EnergyDataResponse>>> queryRwdObjectValueMap(Set<String> objectIds, LocalDate queryDate, EnumTimeType ipType, List<String> infoCode) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Maps.newHashMap();
        }
        EnergyDataQueryParam queryParam = new EnergyDataQueryParam();
        queryParam.setIpType(ipType.getName());
        queryParam.setStartTime(queryDate.atStartOfDay().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
        queryParam.setEndTime(queryDate.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
        List<EnergyDataInfoQueryParam> objectInfos = new ArrayList<>();
        for (String objectId : objectIds) {
            EnergyDataInfoQueryParam infoQueryParam = new EnergyDataInfoQueryParam();
            infoQueryParam.setObjectId(objectId);
            infoQueryParam.setInfoCodes(infoCode);
            objectInfos.add(infoQueryParam);
        }
        queryParam.setObjects(objectInfos);
        CustomerResponse<List<EnergyDataResponse>> response = abstractDtpPersistServer.listIpDataInPeriod(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), queryParam);
        List<EnergyDataResponse> energyDataResponses = CustomerResponse.handleResult(response);
        if (CollectionUtils.isEmpty(energyDataResponses)) {
            return new HashMap<>();
        }
        return energyDataResponses.stream().collect(Collectors.groupingBy(EnergyDataResponse::getObjectId, Collectors.groupingBy(EnergyDataResponse::getInfoCode)));
    }

    /**
     * 构建能源统计列表
     *
     * @param param param
     * @param vo    vo
     */
    private void buildEnergyStatisticsList(RunAnalysisBaseParam param, RunAnalysisStatisticsVO vo, MidCalendarDateTagValueEnum tag) {
        Map<EnergyModelTagEnum, Set<String>> energyItemRwdIdMap = new HashMap<>();
        //为了支持跳转，每个能源类型需要保存一个
        Map<EnergyModelTagEnum, EnergyItemResponse> energyTagItemMap = new HashMap<>();
        for (EnergyModelTagEnum modelTagEnum : ENERGY_ITEM_TAG_LIST) {
            EnergyItemResponse energyItem = queryEnergyItemRwdIdList(modelTagEnum, energyItemRwdIdMap);
            if (Objects.nonNull(energyItem)) {
                energyTagItemMap.put(modelTagEnum, energyItem);
            }
        }
        Map<String, Map<String, List<EnergyDataResponse>>> valueMap = queryEnergyValue(param.getQueryDate(), EnumTimeType.DAY, energyItemRwdIdMap);
        EnergyStatisticsVO energyStatistics = new EnergyStatisticsVO();
        for (EnergyModelTagEnum modelTagEnum : ENERGY_ITEM_TAG_LIST) {
            Set<String> rwdIdSet = energyItemRwdIdMap.get(modelTagEnum);
            if (CollectionUtils.isEmpty(rwdIdSet)) {
                continue;
            }
            List<EnergyDataResponse> all = new ArrayList<>();
            for (String rwdId : rwdIdSet) {
                Map<String, List<EnergyDataResponse>> infoCodeListMap = valueMap.get(rwdId);
                if (MapUtils.isEmpty(infoCodeListMap)) {
                    continue;
                }
                all.addAll(infoCodeListMap.get(DashBoardInfoCodeEnum.SUBENTRY_ACC_CONSUM.getCode()));
            }
            switch (modelTagEnum) {
                case COOL:
                    energyStatistics.setCoolEnergy(handleEnergyValue(all, EnergyModelTagEnum.COOL, energyTagItemMap));
                    break;
                case HEAT:
                    energyStatistics.setHeatEnergy(handleEnergyValue(all, EnergyModelTagEnum.HEAT, energyTagItemMap));
                    break;
                case COOL_HEAT:
                    energyStatistics.setCoolAndHeatEnergy(handleEnergyValue(all, EnergyModelTagEnum.COOL_HEAT, energyTagItemMap));
                    break;
                case AIR_TERMINAL:
                    energyStatistics.setAirTerminalEnergy(handleEnergyValue(all, EnergyModelTagEnum.AIR_TERMINAL, energyTagItemMap));
                    break;
                default:
            }
        }
        //处理总能耗
        BigDecimal heatValue = energyStatistics.getHeatEnergy() != null ? energyStatistics.getHeatEnergy().getDataValue() : null;
        BigDecimal coolValue = energyStatistics.getCoolEnergy() != null ? energyStatistics.getCoolEnergy().getDataValue() : null;
        BigDecimal coolAndHeatValue = energyStatistics.getCoolAndHeatEnergy() != null ? energyStatistics.getCoolAndHeatEnergy().getDataValue() : null;
        BigDecimal airTerminalValue = energyStatistics.getAirTerminalEnergy() != null ? energyStatistics.getAirTerminalEnergy().getDataValue() : null;
        EnergyStatisticsVO.EnergyValue totalEnergy = new EnergyStatisticsVO.EnergyValue();
        totalEnergy.setName(EnergyModelTagEnum.TOTAL.getEnergyName());
        switch (tag) {
            case SEASON_COOLING:
                totalEnergy.setDataValue(BigDecimalUtils.add(coolValue, coolAndHeatValue, airTerminalValue));
                break;
            case SEASON_HEATING:
                totalEnergy.setDataValue(BigDecimalUtils.add(heatValue, coolAndHeatValue, airTerminalValue));
                break;
            case SEASON_TRANSITION:
                totalEnergy.setDataValue(BigDecimalUtils.add(heatValue, coolValue, coolAndHeatValue, airTerminalValue));
                break;
            default:
        }
        if (totalEnergy.getDataValue() != null) {
            energyStatistics.setTotalEnergy(totalEnergy);
        }
        vo.setEnergyStatistics(energyStatistics);
    }


    /**
     * 处理能源值
     *
     * @param energyValueList 能源值列表
     * @param modelTagEnum    能源类型
     * @return {@link EnergyStatisticsVO.EnergyValue }
     */
    private EnergyStatisticsVO.EnergyValue handleEnergyValue(List<EnergyDataResponse> energyValueList,
                                                             EnergyModelTagEnum modelTagEnum,
                                                             Map<EnergyModelTagEnum, EnergyItemResponse> energyTagItemMap) {
        EnergyStatisticsVO.EnergyValue energyValue = new EnergyStatisticsVO.EnergyValue();
        energyValue.setName(modelTagEnum.getEnergyName());
        EnergyItemResponse energyItemResponse = energyTagItemMap.get(modelTagEnum);
        if (Objects.nonNull(energyItemResponse)) {
            energyValue.setEnergyModelId(energyItemResponse.getEnergyModelId());
            energyValue.setItemId(energyItemResponse.getId());
        }
        BigDecimal value = energyValueList.stream().map(EnergyDataResponse::getIpValue).reduce(BigDecimal::add).orElse(null);
        energyValue.setDataValue(value);
        return energyValue;
    }

    /**
     * 构建价值选择
     *
     * @param param param
     * @param vo    vo
     */
    private void buildValueSelection(RunAnalysisBaseParam param, RunAnalysisStatisticsVO vo) {
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        templateParam.setDate(param.getQueryDate());
        DemandConfigAirconditioningCustomDTO valueSelectConfig = demandConfigAirconditioningCustomService.getCustomConfig(templateParam);
        if (null == valueSelectConfig) {
            return;
        }
        ValueSelectionVO valueSelection = new ValueSelectionVO();
        valueSelection.setName(valueSelectConfig.getValueSelectType().getExplain());
        valueSelection.setValueSelectionType(valueSelectConfig.getValueSelectType());
        valueSelection.setTempFillRateConfig(valueSelectConfig.parseValueSelectRange());
        vo.setValueSelection(valueSelection);
    }

    private Map<String, Map<String, List<EnergyDataResponse>>> queryEnergyValue(LocalDate queryDate, EnumTimeType timeType, Map<EnergyModelTagEnum, Set<String>> energyItemRwdIdMap) {
        Set<String> allRwdIdSet = energyItemRwdIdMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Map<String, Map<String, List<EnergyDataResponse>>> valueMap = queryRwdObjectValueMap(allRwdIdSet, queryDate,
                timeType, Collections.singletonList(DashBoardInfoCodeEnum.SUBENTRY_ACC_CONSUM.getCode()));
        if (MapUtils.isEmpty(valueMap)) {
            return new HashMap<>();
        }
        return valueMap;
    }

    /**
     * 查询rwdId列表
     *
     * @param tagName 标签名称
     */
    @Nullable
    private EnergyItemResponse queryEnergyItemRwdIdList(EnergyModelTagEnum tagName, Map<EnergyModelTagEnum, Set<String>> energyItemRwdIdMap) {
        EnergyModelItemParam energyModelItemParam = new EnergyModelItemParam();
        energyModelItemParam.setUserId(StringUtils.isNotBlank(ContextUtil.getUserId()) ? ContextUtil.getUserId() : CommonConst.USER_ID);
        energyModelItemParam.setProjectId(ContextUtil.getProjectId());
        energyModelItemParam.setEnergyModelType(EnergyModelTypeEnum.GROUP);
        energyModelItemParam.setEnergyTypeId("1");
        energyModelItemParam.setTagNames(Collections.singletonList(tagName.getName()));
        CustomerResponse<List<EnergyItemResponse>> response = energyResearchServerClient.queryEnergyModelItem(energyModelItemParam);
        List<EnergyItemResponse> energyItemResponses = CustomerResponse.handleResult(response);
        if (CollectionUtils.isNotEmpty(energyItemResponses)) {
            Set<String> rwdIdList = energyItemResponses.stream().map(EnergyItemResponse::getRwdId).collect(Collectors.toSet());
            energyItemRwdIdMap.put(tagName, rwdIdList);
            return energyItemResponses.get(0);
        }
        return null;
    }

    @Override
    public EnergyCurveDataVO listEnergyCurveData(EnergyCoverDataParam param) {
        BasicsConfigTypeEnum valueSelectionType = param.getValueSelectionType();
        LocalDate queryDate = param.getQueryDate();
        EnergyCurveDataVO vo = new EnergyCurveDataVO();
        StopWatch stopWatch = new StopWatch("曲线查询");
        stopWatch.start("查询能耗数据");
        Map<EnergyModelTagEnum, Set<String>> energyItemRwdIdMap = new HashMap<>();
        for (EnergyModelTagEnum modelTagEnum : ENERGY_ITEM_TAG_LIST) {
            queryEnergyItemRwdIdList(modelTagEnum, energyItemRwdIdMap);
        }
        Map<String, Map<String, List<EnergyDataResponse>>> valueMap = queryEnergyValue(queryDate, EnumTimeType.HOUR, energyItemRwdIdMap);
        //记录总列表，用来计算总能耗
        List<EnergyDataResponse> allEnergyValueList = new ArrayList<>();
        for (EnergyModelTagEnum modelTagEnum : ENERGY_ITEM_TAG_LIST) {
            Set<String> objectIds = energyItemRwdIdMap.get(modelTagEnum);
            if (CollectionUtils.isEmpty(objectIds)) {
                continue;
            }
            List<EnergyDataResponse> allEnergyValueByModelTag = new ArrayList<>();
            //先通过能耗模型，合并能耗记录
            for (String objectId : objectIds) {
                Map<String, List<EnergyDataResponse>> infoCodeValueMap = valueMap.get(objectId);
                if (MapUtils.isEmpty(infoCodeValueMap)) {
                    continue;
                }
                //先按能耗模型合并
                List<EnergyDataResponse> valueList = infoCodeValueMap.get(DashBoardInfoCodeEnum.SUBENTRY_ACC_CONSUM.getCode());
                if (CollectionUtils.isNotEmpty(valueList)) {
                    allEnergyValueByModelTag.addAll(valueList);
                }
            }
            allEnergyValueList.addAll(allEnergyValueByModelTag);
            //再按时间分组
            switch (modelTagEnum) {
                case COOL:
                    vo.setCoolResourceData(valueGroupByTime(allEnergyValueByModelTag));
                    break;
                case HEAT:
                    vo.setHotResourceData(valueGroupByTime(allEnergyValueByModelTag));
                    break;
                case COOL_HEAT:
                    vo.setCoolAndHotResourceData(valueGroupByTime(allEnergyValueByModelTag));
                    break;
                case AIR_TERMINAL:
                    vo.setTerminalData(valueGroupByTime(allEnergyValueByModelTag));
                    break;
                default:
            }
        }
        //总能耗
        vo.setRealEnergyData(valueGroupByTime(allEnergyValueList));
        stopWatch.stop();
        stopWatch.start("查询碳排放或者电价数据");
        // 获取项目的城市编码
        ProjectDTO projectDTO = midPermissionClientService.getProject(false, true, ContextUtil.getProjectId());
        switch (valueSelectionType) {
            case VALUE_SELECT_LOWEST_CAST:
                vo.setElectricityPriceData(queryElectricityPrice(queryDate, projectDTO));
                break;
            case VALUE_SELECT_LOWEST_CARBON_EMISSIONS:
                vo.setCarbonFactorData(queryCarbonFactor(queryDate, projectDTO));
                break;
            default:
        }
        stopWatch.stop();
        stopWatch.start("查询室外温度曲线");
        //查询室外温度曲线
        vo.setOutdoorTempData(queryOutdoorTemp(queryDate));
        stopWatch.stop();
        stopWatch.start("查询蓄热蓄冷标签");
        //查询蓄热蓄冷标签
        vo.setTagData(queryCoolHeatTag(queryDate, valueSelectionType));
        stopWatch.stop();
        stopWatch.start("查询室内温度曲线");
        //查询室内温度曲线
        vo.setInDoorTempData(queryIndoorTemp(queryDate));
        stopWatch.stop();
        logProjectInstance().debug("曲线查询", Pair.of("cost", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return vo;
    }

    /**
     * 查询室内温度
     *
     * @param queryDate 查询日期
     * @return {@link List }<{@link DataValueVO }>
     */
    private List<DataValueVO> queryIndoorTemp(LocalDate queryDate) {
        List<DataValueVO> resultList = new ArrayList<>();
        // 先查询六类空调系统实例（旧版 ACCC/ACCI/ACCH + 新版 ACAC/ACAH/ACAI）
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setClassCode(DtpRwdClassCodeEnum.listAirConditionSystemClassCodes());
        param.addWithColumn(DtpRwdObjectDTO.Fields.id);
        List<DtpRwdObjectDTO> systems = dtpRwdClientService.listObject(param);
        if (CollectionUtils.isEmpty(systems)) {
            return resultList;
        }
        StopWatch stopWatch = new StopWatch("查询室内温度曲线");
        Set<String> systemIdSet = systems.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Set<String> spaceIds = runningMonitorRunAnalysisStrategy.listExistsRuleSpaceIdBySystemIds(ContextUtil.getProjectId(), systemIdSet, InfoCodeEnum.SPACE_TDB);
        if (CollectionUtils.isEmpty(spaceIds)) {
            return resultList;
        }
        stopWatch.start("查询需求空间");
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        templateParam.setDate(queryDate);
        Set<String> demandSpaceIds = demandConfigGateway.listHasConfigSpaceIds(templateParam, spaceIds);
        stopWatch.stop();
        if (CollectionUtils.isEmpty(demandSpaceIds)) {
            logProjectInstance().info("未查询到有空调需求的空间id");
            return resultList;
        }
        stopWatch.start("查询室外温度值信息");
        Map<String, Map<String, List<EnergyDataResponse>>> valueMap = queryRwdObjectValueMap(demandSpaceIds, queryDate, EnumTimeType.MINUTE5, Collections.singletonList(InfoCodeEnum.SPACE_TDB.getCode()));
        stopWatch.stop();
        if (MapUtils.isEmpty(valueMap)) {
            return resultList;
        }
        stopWatch.start("处理结果");
        //合并成一个列表
        List<EnergyDataResponse> values = valueMap.values().stream().flatMap(e -> e.get(InfoCodeEnum.SPACE_TDB.getCode()).stream()).collect(Collectors.toList());
        Map<String, List<EnergyDataResponse>> timeValueMap = values.parallelStream().collect(Collectors.groupingBy(EnergyDataResponse::getIpTime));
        for (String time : timeValueMap.keySet()) {
            List<EnergyDataResponse> energyDataResponses = timeValueMap.get(time);
            if (CollectionUtils.isEmpty(energyDataResponses)) {
                continue;
            }
            DataValueVO energyDataVO = new DataValueVO();
            energyDataVO.setDataTime(LocalDateTime.parse(time, DatePattern.PURE_DATETIME_FORMATTER));
            energyDataVO.setUnit(com.persagy.apccontrol.basic.model.enumtype.systemgraph.InfoCodeEnum.SPACE_TDB.getUnit());
            //获取空间数量
            Set<String> timeSpaceIds = energyDataResponses.stream().map(EnergyDataResponse::getObjectId).collect(Collectors.toSet());
            //获取温度和
            BigDecimal sum = energyDataResponses.stream().map(EnergyDataResponse::getIpValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            energyDataVO.setDataValue(sum.divide(new BigDecimal(timeSpaceIds.size()), 1, RoundingMode.HALF_UP));
            resultList.add(energyDataVO);
        }
        stopWatch.stop();
        logProjectInstance().debug("室内温度查询", Pair.of("cost", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return resultList.stream().sorted(Comparator.comparing(DataValueVO::getDataTime)).collect(Collectors.toList());
    }

    /**
     * 查询室外温度
     *
     * @param queryDate 查询日期
     * @return {@link List }<{@link DataValueVO }>
     */
    private List<DataValueVO> queryOutdoorTemp(LocalDate queryDate) {
        List<WeatherHourHistoryDataDTO> weatherHourHistoryDataDTOS = weatherClientService.listHourHistoryData(ContextUtil.getProjectId(), queryDate.atStartOfDay(), queryDate.plusDays(1).atStartOfDay());
        if (CollectionUtils.isEmpty(weatherHourHistoryDataDTOS)) {
            return Collections.emptyList();
        }
        List<DataValueVO> resultList = new ArrayList<>();
        for (WeatherHourHistoryDataDTO historyDataDTO : weatherHourHistoryDataDTOS) {
            DataValueVO vo = new DataValueVO();
            vo.setUnit(com.persagy.apccontrol.basic.model.enumtype.systemgraph.InfoCodeEnum.SPACE_TDB.getUnit());
            vo.setDataTime(historyDataDTO.getHourTime());
            vo.setDataValue(historyDataDTO.getTemperature());
            resultList.add(vo);
        }
        return resultList;
    }

    private List<DataValueVO> queryCoolHeatTag(LocalDate queryDate, BasicsConfigTypeEnum valueSelectionType) {
        LambdaQueryWrapper<HistoryStorageResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HistoryStorageResult::getRecordDate, queryDate);
        List<HistoryStorageResult> list = historyStorageResultService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        HistoryStorageResult historyStorageResult = list.get(0);
        List<SyncHistoryStorageParam.RecordContent> content = historyStorageResult.getContent();
        List<DataValueVO> resultList = new ArrayList<>();
        for (SyncHistoryStorageParam.RecordContent recordContent : content) {
            resultList.add(new DataValueVO(new BigDecimal(recordContent.getStorageTag().getTag()), null, recordContent.getTime().atDate(queryDate)));
        }
        return resultList;
    }

    /**
     * 根据日期查询电价
     *
     * @param queryDate  查询日期
     * @param projectDTO 项目dto
     * @return {@link List }<{@link DataValueVO }>
     */
    private List<DataValueVO> queryElectricityPrice(LocalDate queryDate, ProjectDTO projectDTO) {
        // 获取电价
        ElectricityPriceQueryParam queryParam = new ElectricityPriceQueryParam();
        queryParam.setElectricityType("1");
        queryParam.setStartTime(queryDate.atStartOfDay());
        queryParam.setEndTime(queryDate.plusDays(1).atStartOfDay());
        List<EnergyDataResponse> elecDataList = electricityDataClientService.listElectrovalenceData(queryParam);
        List<DataValueVO> resultList = new ArrayList<>();
        DtpKnowledgeInfoCodeDTO infoCode = dtpKnowledgeClientService.getInfoCode(false, DtpRwdClassCodeEnum.PROJECT.value(), InfoCodeEnum.ELEC_PRICE.getCode());
        if (CollectionUtils.isNotEmpty(elecDataList)) {
            for (EnergyDataResponse data : elecDataList) {
                DataValueVO vo = new DataValueVO();
                vo.setUnit(Objects.nonNull(infoCode) ? infoCode.getUnit() : null);
                vo.setDataTime(LocalDateTime.parse(data.getIpTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                vo.setDataValue(data.getIpValue());
                resultList.add(vo);
            }
        }
        return resultList;
    }

    /**
     * 查询碳因子数据
     *
     * @param queryDate  查询日期
     * @param projectDTO 项目dto
     * @return {@link List }<{@link DataValueVO }>
     */
    private List<DataValueVO> queryCarbonFactor(LocalDate queryDate, ProjectDTO projectDTO) {
        // 查询碳因子数据
        CarbonCategoryDataParam queryParam = GenericBuilder.of(CarbonCategoryDataParam::new)
                .with(CarbonCategoryDataParam::setProjectId, ContextUtil.getProjectId())
                .with(CarbonCategoryDataParam::setCityCode, projectDTO.getCityCode())
                .with(CarbonCategoryDataParam::setGroupCode, ContextUtil.getGroupCode())
                .with(CarbonCategoryDataParam::setCategoryCode, "CREL")
                .with(CarbonCategoryDataParam::setBeginTime, queryDate.atStartOfDay())
                .with(CarbonCategoryDataParam::setEndTime, queryDate.plusDays(1).atStartOfDay()).build();
        List<CarbonCategoryDataResponse.CarBonData> carbonDataList = electricityDataClientService.listCarbonCategoryData(queryParam);
        List<DataValueVO> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(carbonDataList)) {
            for (CarbonCategoryDataResponse.CarBonData carBonData : carbonDataList) {
                DataValueVO vo = new DataValueVO();
                BigDecimal dataValue = CollectionUtils.isEmpty(carBonData.getDataList()) ? null : carBonData.getDataList().get(0).getValue();
                vo.setDataValue(dataValue);
                vo.setDataTime(carBonData.getDate());
                vo.setUnit("kgCO₂/kWh");
                resultList.add(vo);
            }
        }
        return resultList;
    }

    /**
     * 按时间划分组
     *
     * @param allEnergyValueByModelTag 时间价值图
     * @return {@link List }<{@link DataValueVO }>
     */
    private List<DataValueVO> valueGroupByTime(List<EnergyDataResponse> allEnergyValueByModelTag) {
        Map<String, List<EnergyDataResponse>> timeValueMap = allEnergyValueByModelTag.stream().collect(Collectors.groupingBy(EnergyDataResponse::getIpTime));
        if (MapUtils.isEmpty(timeValueMap)) {
            return Collections.emptyList();
        }
        List<DataValueVO> energyDataList = new ArrayList<>();
        for (String time : timeValueMap.keySet()) {
            DataValueVO vo = new DataValueVO();
            vo.setDataTime(LocalDateTime.parse(time, DatePattern.PURE_DATETIME_FORMATTER));
            vo.setDataValue(timeValueMap.get(time).stream().map(EnergyDataResponse::getIpValue).reduce(BigDecimal::add).orElse(null));
            vo.setUnit("kWh");
            energyDataList.add(vo);
        }
        //按时间排序
        return energyDataList.stream().sorted(Comparator.comparing(DataValueVO::getDataTime)).collect(Collectors.toList());
    }

    @Override
    public List<AnalysisDataTreeVO> getAnalysisDataTree(AnalysisDataTreeParam param) {
        Map<String, List<RunAnalysisConfigVO>> configMap = commonConfigService.getRunAnalysisConfig();
        if (MapUtils.isEmpty(configMap)) {
            logProjectInstance().error("未获取到运行分析配置");
            return Collections.emptyList();
        }
        DtpRwdObjectDTO systemObject = dtpRwdClientService.getObject(new DtpRwdObjectQueryParam().setId(param.getSystemId()).addWithColumn(DtpRwdObjectDTO.listSimpleWithColumns()));
        if (Objects.isNull(systemObject)) {
            logProjectInstance().error("未获取到系统对象", Pair.of("systemId", param.getSystemId()));
            return Collections.emptyList();
        }
        List<RunAnalysisConfigVO> configList = configMap.get(systemObject.getClassCode());
        if (CollectionUtils.isEmpty(configList)) {
            logProjectInstance().error("未获取到运行分析配置", Pair.of("classCode", systemObject.getClassCode()));
            return Collections.emptyList();
        }
        Map<AnalysisTypeEnum, List<RunAnalysisConfigVO>> analysisTypeListMap = configList.stream().collect(Collectors.groupingBy(RunAnalysisConfigVO::getAnalysisType,
                LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
        List<RunAnalysisConfigVO> runAnalysisConfigList = analysisTypeListMap.get(param.getAnalysisType());
        if (CollectionUtils.isEmpty(runAnalysisConfigList)) {
            logProjectInstance().error("未获取到运行分析配置", Pair.of("analysisTypeName", param.getAnalysisType().name()));
            return Collections.emptyList();
        }
        List<RunAnalysisConfigVO> equipConfigList = runAnalysisConfigList.stream().filter(e -> Objects.isNull(e.getGroupType())).collect(Collectors.toCollection(LinkedList::new));
        List<RunAnalysisConfigVO> equipGroupConfigList = runAnalysisConfigList.stream().filter(e -> Objects.nonNull(e.getGroupType())).collect(Collectors.toList());
        Map<String, List<RunAnalysisConfigVO>> firstGroupMap = runAnalysisConfigList.stream().collect(Collectors.groupingBy(RunAnalysisConfigVO::getFirstGroup, LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
        List<AnalysisDataTreeVO> resultList = new LinkedList<>();
        Set<String> eqClassCode = new HashSet<>();
        Set<String> eqGroupClassCode = new HashSet<>();
        Set<String> equipWithColumn = new HashSet<>();
        Set<String> groupWithColumn = new HashSet<>();
        equipConfigList.forEach(e -> {
            if (CollectionUtils.isNotEmpty(e.getByClassCode())) {
                eqClassCode.addAll(e.getByClassCode());
            }
            String additionalInfoCode = e.getAdditionalInfoCode();
            if (StringUtils.isNotBlank(additionalInfoCode)) {
                equipWithColumn.addAll(Sets.newHashSet(StringUtils.split(additionalInfoCode, ",")));
            }
        });
        equipGroupConfigList.forEach(e -> {
            if (CollectionUtils.isNotEmpty(e.getByClassCode())) {
                eqGroupClassCode.addAll(e.getByClassCode());
            }
            String additionalInfoCode = e.getAdditionalInfoCode();
            if (StringUtils.isNotBlank(additionalInfoCode)) {
                groupWithColumn.addAll(Sets.newHashSet(StringUtils.split(additionalInfoCode, ",")));
            }
        });
        Map<String, List<DtpRwdObjectDTO>> allRwdObjectMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(eqClassCode)) {
            allRwdObjectMap.putAll(getRwdObjectMap(param, eqClassCode, DtpRwdRelationTypeEnum.EQ_IN_SYSTEM, equipWithColumn.toArray(new String[0])));
        }
        if (CollectionUtils.isNotEmpty(eqGroupClassCode)) {
            allRwdObjectMap.putAll(getRwdObjectMap(param, eqGroupClassCode, DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM, groupWithColumn.toArray(new String[0])));
        }
        //所有classCode列表，包含系统，组， 设备，组下设备
        Set<String> equipInGroupClassCodeSet = runAnalysisConfigList.stream().filter(e -> Objects.nonNull(e.getEquipInGroup())).flatMap(e -> e.getEquipInGroup().stream()).collect(Collectors.toSet());
        Set<String> allClassCode = Sets.newHashSet(systemObject.getClassCode());
        allClassCode.addAll(eqClassCode);
        allClassCode.addAll(eqGroupClassCode);
        allClassCode.addAll(equipInGroupClassCodeSet);
        //查询信息点数据, classCode->(infoCode->infoDefine)
        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> mapClassCodeInfoCodeAndDefine = dtpKnowledgeClientService.mapClassCodeInfoCodeAndDefine(true, false, allClassCode);
        Map<String, DtpKnowledgeInfoCodeDTO> infoCodeDefineMap = mapClassCodeInfoCodeAndDefine.values().stream().flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));
        //查询规则 classCode->(objectId->infoCodes)
        Map<String, Map<String, Set<String>>> classCodeObjectIdInfoCodesMap = dtpRwdClientService.mapClassCodeObjectIdInfoCodesByClassCode(DtpRwdObjectsOfExistRuleQueryParam.of(allClassCode));
        for (String key : firstGroupMap.keySet()) {
            AnalysisDataTreeVO vo = new AnalysisDataTreeVO();
            vo.setName(key);
            List<RunAnalysisConfigVO> configVOS = firstGroupMap.get(key);
            if (CollectionUtils.isEmpty(configVOS)) {
                continue;
            }
            List<String> newClassifyInfoCode = new LinkedList<>();
            Map<String, LinkedList<RunAnalysisConfigVO>> secondMap = configVOS.stream().collect(Collectors.groupingBy(RunAnalysisConfigVO::getSecondGroup, LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
            List<AnalysisDataTreeVO> secondChildren = new LinkedList<>();
            for (String secondKey : secondMap.keySet()) {
                AnalysisDataTreeVO secondChild = new AnalysisDataTreeVO();
                LinkedList<RunAnalysisConfigVO> runAnalysisConfigVOS = secondMap.get(secondKey);
                secondChild.setName(secondKey);
                boolean flag = buildTree(secondChild, runAnalysisConfigVOS, systemObject, allRwdObjectMap, newClassifyInfoCode, infoCodeDefineMap, classCodeObjectIdInfoCodesMap);
                if (flag) {
                    secondChildren.add(secondChild);
                }
            }
            if (CollectionUtils.isNotEmpty(secondChildren)) {
                vo.setNewClassifyInfoCodes(newClassifyInfoCode);
            }
            vo.setChildren(secondChildren);
            if (CollectionUtils.isNotEmpty(secondChildren)) {
                resultList.add(vo);
            }

        }

        return resultList;
    }

    @Override
    public List<AnalysisTypeVO> listAnalysisType(LocalDate date) {
        MidCalendarDateTagValueEnum currentDayDateTag = getCurrentDayDateTag(date);
        //查询是否有ACCC的实例化对象
        int coldSysNum = dtpRwdClientService.countObject(new DtpRwdObjectQueryParam()
                .setClassCode(Lists.newArrayList(DtpRwdClassCodeEnum.COLD_SYSTEM.value(), DtpRwdClassCodeEnum.ACAC_SYSTEM.value())));
        List<AnalysisTypeVO> result = Lists.newArrayList();
        if (coldSysNum > 0) {
            //查询是否有可监可控的系统
            int monitorColdSysNum = dtpRwdClientService.countObject(new DtpRwdObjectQueryParam()
                    .filterMonitoringSurveillanceStatus()
                    .setClassCode(Lists.newArrayList(DtpRwdClassCodeEnum.COLD_SYSTEM.value(), DtpRwdClassCodeEnum.ACAC_SYSTEM.value())));
            //查询有无蓄冷设备
            DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
            param.addWithColumn(DtpRwdObjectDTO.listSimpleWithColumns());
            param.filterMonitoringSurveillanceStatus();
            param.setClassCode(DtpRwdClassCodeEnum.ACAC_SYSTEM.value());
            param.addInfoCode(InfoCodeEnum.IS_COOL_STORE.getCode(), "1");
            int isCoolStoreNum = dtpRwdClientService.countObject(param);
            //如果有供冷工况且系统是可监可控，则添加冷冻侧，冷却侧
            if (checkSeasonToCooling(currentDayDateTag)) {
                result.addAll(AnalysisTypeVO.toList(monitorColdSysNum > 0));
            }
            //如果可监可控的系统，则添加泵组
            if (monitorColdSysNum > 0) {
                result.add(AnalysisTypeVO.of(AnalysisTypeEnum.END_PUMP_GROUP));
            }
            //如果有蓄冷设备，则添加蓄冷侧
            if (isCoolStoreNum > 0) {
                result.add(AnalysisTypeVO.of(AnalysisTypeEnum.COLD_STORE_SIDE));
            }
        }
        //排序
        if (CollectionUtils.isNotEmpty(result)) {
            result.sort(Comparator.comparingInt(AnalysisTypeVO::getOrder));
        }
        return result;
    }

    @Override
    public Boolean storageTagSync(SyncHistoryStorageParam param) {
        HistoryStorageResult result = new HistoryStorageResult();
        result.setRecordDate(param.getDate());
        result.setProjectId(ContextUtil.getProjectId());
        result.setContent(param.getRecordContent());
        result.clearUpdateParameter();
        LambdaQueryWrapper<HistoryStorageResult> uw = new LambdaQueryWrapper<>();
        uw.eq(HistoryStorageResult::getRecordDate, param.getDate());
        //默认保存180天数据
        LocalDate date = DateUtils.now().toLocalDate().plusDays(-180);
        LambdaQueryWrapper<HistoryStorageResult> delQw = new LambdaQueryWrapper<>();
        //删除180天前的数据
        delQw.lt(HistoryStorageResult::getRecordDate, date);
        historyStorageResultService.remove(delQw);
        return historyStorageResultService.saveOrUpdate(result, uw);
    }

    /**
     * rwd对象map
     *
     * @param param            param
     * @param classCodeSet     设备类列表
     * @param relationTypeEnum 关系类型
     * @param withColumns 查询列
     * @return {@link Map }<{@link String }, {@link List }<{@link DtpRwdObjectDTO }>>
     */
    private Map<String, List<DtpRwdObjectDTO>> getRwdObjectMap(AnalysisDataTreeParam param, Set<String> classCodeSet, DtpRwdRelationTypeEnum relationTypeEnum, String[] withColumns) {
        Map<String, List<DtpRwdObjectDTO>> resultMap = new HashMap<>();
        if (CollectionUtils.isEmpty(classCodeSet)) {
            return resultMap;
        }
        DtpRwdObjectQueryParam rwdObjectQueryParam = new DtpRwdObjectQueryParam();
        rwdObjectQueryParam.setClassCode(classCodeSet);
        DtpRwdObjectRelationQueryParam rwdObjectRelationQueryParam = new DtpRwdObjectRelationQueryParam(relationTypeEnum);
        rwdObjectRelationQueryParam.setObjFrom(param.getSystemId());
        rwdObjectQueryParam.setRelationTo(rwdObjectRelationQueryParam);
        rwdObjectQueryParam.addWithColumn(DtpRwdObjectDTO.listSimpleWithColumns());
        if (ArrayUtils.isNotEmpty(withColumns)) {
            rwdObjectQueryParam.addWithColumn(withColumns);
        }
        List<DtpRwdObjectDTO> dtpRwdObjectList = dtpRwdClientService.listObject(rwdObjectQueryParam);
        resultMap = dtpRwdObjectList.stream().collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode));
        return resultMap;
    }

    private Boolean buildTree(AnalysisDataTreeVO vo, List<RunAnalysisConfigVO> configVOS, DtpRwdObjectDTO systemObject, Map<String, List<DtpRwdObjectDTO>> rwdObjectClassCodeMap, List<String> newClassifyInfoCode,
                              Map<String, DtpKnowledgeInfoCodeDTO> infoCodeDefineMap, Map<String, Map<String, Set<String>>> classCodeObjectIdInfoCodesMap) {
        if (CollectionUtils.isEmpty(configVOS)) {
            return false;
        }
        Map<String, List<DtpRwdObjectDTO>> equipInGroupClassCodeMap = getEquipInGroupClassCodeMap(configVOS, rwdObjectClassCodeMap);
        Set<String> classCodeSet = configVOS.stream().filter(e -> CollectionUtils.isNotEmpty(e.getByClassCode())).flatMap(e -> e.getByClassCode().stream()).collect(Collectors.toSet());
        JSONObject classCodeConditionMap = buildClassCodeConditionMap(configVOS, classCodeSet);
        List<AnalysisDataTreeVO> children = new LinkedList<>();
        for (RunAnalysisConfigVO configVO : configVOS) {
            AnalysisDataTreeVO child = new AnalysisDataTreeVO();
            String infoCode = configVO.getInfoCode();
            DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO = infoCodeDefineMap.get(infoCode);
            if (Objects.isNull(dtpKnowledgeInfoCodeDTO)) {
                //如果字典没有信息点，则跳过
                continue;
            }
            child.setName(dtpKnowledgeInfoCodeDTO.getName());
            child.setUnit(dtpKnowledgeInfoCodeDTO.getUnit());
            child.setInfoCode(infoCode);
            child.setGroupName(configVO.getSecondGroup());
            child.setStatisticalValue(configVO.getStatisticalValue());
            if (configVO.isNewClassify()) {
                newClassifyInfoCode.add(infoCode);
            }
            List<String> classCodeList = configVO.getByClassCode();
            if (CollectionUtils.isEmpty(classCodeList)) {
                Map<String, Set<String>> objectIdInfoCodes = classCodeObjectIdInfoCodesMap.get(systemObject.getClassCode());
                if (MapUtils.isEmpty(objectIdInfoCodes)) {
                    continue;
                }
                Set<String> infoCodeRules = objectIdInfoCodes.get(systemObject.getId());
                if (CollectionUtils.isEmpty(infoCodeRules)) {
                    continue;
                }
                //如何没有规则，跳过
                if (!infoCodeRules.contains(infoCode)) {
                    continue;
                }
                AnalysisDataTreeVO.ObjectInfo objectInfo = new AnalysisDataTreeVO.ObjectInfo();
                objectInfo.setObjectId(systemObject.getId());
                objectInfo.setClassCode(systemObject.getClassCode());
                objectInfo.setLocalName(systemObject.getLocalName());
                child.setObjects(Collections.singletonList(objectInfo));
            } else {
                List<AnalysisDataTreeVO.ObjectInfo> objectInfos;
                if (CollectionUtils.isNotEmpty(configVO.getEquipInGroup())) {
                    objectInfos = buildObjectInfoList(configVO.getEquipInGroup(), equipInGroupClassCodeMap, null, classCodeObjectIdInfoCodesMap, infoCode);
                } else {
                    objectInfos = buildObjectInfoList(classCodeList, rwdObjectClassCodeMap, classCodeConditionMap, classCodeObjectIdInfoCodesMap, infoCode);
                }
                child.setObjects(objectInfos);
            }
            child.setSelect(configVO.isSelect());
            if (CollectionUtils.isNotEmpty(child.getObjects())) {
                children.add(child);
            }
        }
        vo.setChildren(children);
        return CollectionUtils.isNotEmpty(children);
    }

    /**
     * 得到配备在……里面组班代码map
     *
     * @param configVOS 配置vos
     * @param rwdObjectClassCodeMap rwd对象类map
     * @return {@code Map<String, List<DtpRwdObjectDTO>> }
     * @since 7.250701.0
     */
    @NotNull
    private Map<String, List<DtpRwdObjectDTO>> getEquipInGroupClassCodeMap(List<RunAnalysisConfigVO> configVOS, Map<String, List<DtpRwdObjectDTO>> rwdObjectClassCodeMap) {
        Set<String> groupClassCodeSet = configVOS.stream().filter(e -> e.getGroupType() == GroupTypeEnum.EQUIP_IN_GROUP).flatMap(e -> e.getByClassCode().stream()).collect(Collectors.toSet());
        Set<String> equipInGroupClassCodeSet = configVOS.stream().filter(e -> e.getGroupType() == GroupTypeEnum.EQUIP_IN_GROUP).flatMap(e -> e.getEquipInGroup().stream()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(groupClassCodeSet) || CollectionUtils.isEmpty(equipInGroupClassCodeSet)) {
            return Collections.emptyMap();
        }
        JSONObject groupClassCodeConditionMap = buildClassCodeConditionMap(configVOS, groupClassCodeSet);
        //拿到所有的设备组id
        Set<String> groupIdSet = Sets.newHashSet();
        groupClassCodeSet.forEach(classCode -> {
            List<DtpRwdObjectDTO> dtpRwdObjectDTOS = rwdObjectClassCodeMap.get(classCode);
            if (CollectionUtils.isNotEmpty(dtpRwdObjectDTOS)) {
                JSONObject conditionMap = groupClassCodeConditionMap.getJSONObject(classCode);
                groupIdSet.addAll(dtpRwdObjectDTOS.stream().filter(e -> applyCondition(e, conditionMap)).map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
            }
        });
        if (CollectionUtils.isEmpty(groupIdSet)) {
            return Collections.emptyMap();
        }
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setClassCode(equipInGroupClassCodeSet);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIP_IN_GROUP).setObjFrom(groupIdSet));
        param.addWithColumn(DtpRwdObjectDTO.listSimpleWithColumns());
        return dtpRwdClientService.listObject(param).stream().collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode));
    }

    /**
     * 构建设备类条件map
     *
     * @param configVOS         配置vos
     * @param groupClassCodeSet 组类代码集
     * @return {@code Map<String, Map<String, Object>> }
     * @since 7.250801.0
     */
    @NotNull
    private JSONObject buildClassCodeConditionMap(List<RunAnalysisConfigVO> configVOS, Set<String> groupClassCodeSet) {
        JSONObject condition = new JSONObject();
        groupClassCodeSet.forEach(classCode -> {
            configVOS.stream().filter(e -> Objects.nonNull(e.getCondition()))
                    .forEach(e -> {
                        if (MapUtils.isNotEmpty(e.getCondition().getJSONObject(classCode)))
                            condition.putAll(e.getCondition());
                    });
        });
        return condition;
    }

    /**
     * 应用条件
     *
     * @param rwdObjectDTO 需要执行条件的rwd对象
     * @param conditionMap 条件map
     * @return boolean true满足条件,false不满足条件
     * @since 7.250801.0
     */
    private boolean applyCondition(DtpRwdObjectDTO rwdObjectDTO, JSONObject conditionMap) {
        if (MapUtils.isEmpty(conditionMap)) {
            return true;
        }
        String type = conditionMap.getString("type");
        boolean isOrCondition = StringUtils.isNotBlank(type) && StringUtils.equals(type, "or");

        if (isOrCondition) {
            for (Map.Entry<String, Object> entry : conditionMap.entrySet()) {
                //type不参与比较
                if (StringUtils.equals(entry.getKey(), "type")) {
                    continue;
                }
                Object value = rwdObjectDTO.getUndefinedProperties().get(entry.getKey());
                //如果是or，有一个满足就返回true
                if (StringUtils.equals(value.toString(), entry.getValue().toString())) {
                    return true;
                }
            }
            return false;
        } else {
            for (Map.Entry<String, Object> entry : conditionMap.entrySet()) {
                //type不参与比较
                if (StringUtils.equals(entry.getKey(), "type")) {
                    continue;
                }
                Object value = rwdObjectDTO.getUndefinedProperties().get(entry.getKey());
                //如果不是or，有一个不满足就返回false
                if (!StringUtils.equals(value.toString(), entry.getValue().toString())) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 构建对象信息List
     *
     * @param classCodeList                 类代码列表
     * @param rwdObjectClassCodeMap         rwd对象类代码Map
     * @param classCodeObjectIdInfoCodesMap
     * @return {@link List }<{@link AnalysisDataTreeVO.ObjectInfo }>
     */
    private List<AnalysisDataTreeVO.ObjectInfo> buildObjectInfoList(@NotNull List<String> classCodeList, @NotNull Map<String, List<DtpRwdObjectDTO>> rwdObjectClassCodeMap,
                                                                    @org.jetbrains.annotations.Nullable JSONObject classCodeConditionMap, Map<String, Map<String, Set<String>>> classCodeObjectIdInfoCodesMap,
                                                                    String infoCode) {
        List<AnalysisDataTreeVO.ObjectInfo> resultList = new ArrayList<>();
        classCodeList.forEach(classCode -> {
            List<DtpRwdObjectDTO> dtpRwdObjectList = rwdObjectClassCodeMap.get(classCode);
            Map<String, Set<String>> objectIdInfoCodesMap = classCodeObjectIdInfoCodesMap.get(classCode);
            if (CollectionUtils.isNotEmpty(dtpRwdObjectList) && MapUtils.isNotEmpty(objectIdInfoCodesMap)) {
                dtpRwdObjectList.forEach(dtpRwdObject -> {
                    if (!MapUtils.isEmpty(classCodeConditionMap) && !applyCondition(dtpRwdObject, classCodeConditionMap.getJSONObject(classCode))) {
                        return;
                    }
                    Set<String> rules = objectIdInfoCodesMap.get(dtpRwdObject.getId());
                    if (CollectionUtils.isEmpty(rules) || !rules.contains(infoCode)) {
                        return;
                    }
                    AnalysisDataTreeVO.ObjectInfo objectInfo = new AnalysisDataTreeVO.ObjectInfo();
                    objectInfo.setObjectId(dtpRwdObject.getId());
                    objectInfo.setClassCode(dtpRwdObject.getClassCode());
                    objectInfo.setLocalName(dtpRwdObject.getLocalName());
                    resultList.add(objectInfo);
                });
            }
        });
        return resultList;
    }


    @Override
    public AirConditionerSpaceAnalysisVO querySpaceAnalysisList(BasicsAnalysisParam param) {
        List<AnalysisAirConditionerSpace> spacesAnalysis = new ArrayList<>();
        AirConditionerSpaceAnalysisVO vo = new AirConditionerSpaceAnalysisVO();
        Double sp = analysisAirConditionerSpacesService.projectSatisfyPercentage(param.getDate(), param.getType());
        if (sp != null) {
            vo.setSatisfyPercentage(String.format("%.2f", sp * 100));
        }

        //项目查询
        if (InstanceTypeEnum.PROJECT.equals(param.getInstanceType())) {
            spacesAnalysis = analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(null, null, null,param.getDate(), true, param.getType());
        }
        //楼层查询
        if (InstanceTypeEnum.FLOOR.equals(param.getInstanceType())) {
            spacesAnalysis = analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(null, param.getInstanceIds(), null,param.getDate(), null, param.getType());
        }
        //空间查询
        if (InstanceTypeEnum.SPACE.equals(param.getInstanceType())) {
            spacesAnalysis = analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(param.getInstanceIds(), null, null,param.getDate(), null, param.getType());
        }
        if (CollectionUtils.isEmpty(spacesAnalysis)) {
            return vo;
        }
        setSpacesAnalysis(spacesAnalysis, vo);
        return vo;
    }

    @Override
    public AirConditionerSpaceAnalysisVO getSpaceAnalysisList(BasicsAnalysisParam param) {
        List<AnalysisAirConditionerSpace> spacesAnalysis = new ArrayList<>();
        AirConditionerSpaceAnalysisVO vo = new AirConditionerSpaceAnalysisVO();

        //项目查询
        if (InstanceTypeEnum.PROJECT.equals(param.getInstanceType())) {
            spacesAnalysis = analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(null, null, null,param.getDate(), null, param.getType());
        }
        //建筑查询
        if (InstanceTypeEnum.BUILDING.equals(param.getInstanceType())) {
            spacesAnalysis = analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(null, null, param.getInstanceIds(), param.getDate(), null, param.getType());
        }
        //楼层查询
        if (InstanceTypeEnum.FLOOR.equals(param.getInstanceType())) {
            spacesAnalysis = analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(null, param.getInstanceIds(), null, param.getDate(), null, param.getType());
        }
        //空间查询
        if (InstanceTypeEnum.SPACE.equals(param.getInstanceType())) {
            spacesAnalysis = analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(param.getInstanceIds(), null,null, param.getDate(), null, param.getType());
        }
        if (CollectionUtils.isEmpty(spacesAnalysis)) {
            return vo;
        }
        int totalTimeNumber = spacesAnalysis.stream()
                .mapToInt(item -> item.getTimeNumber() != null ? item.getTimeNumber() : 0)
                .sum();
        int totalSatisfyTimeNumber = spacesAnalysis.stream()
                .mapToInt(item -> item.getSatisfyTimeNumber() != null ? item.getSatisfyTimeNumber() : 0)
                .sum();

        Double sp = totalTimeNumber == 0 ? 0 : totalSatisfyTimeNumber / (double) totalTimeNumber;
        if (sp != null) {
            vo.setSatisfyPercentage(String.format("%.2f", sp * 100));
        }
        setSpacesAnalysis(spacesAnalysis, vo);
        return vo;
    }

    @Override
    public Collection<AnalysisInfoCodeVO> listInfoCode(AnalysisInfoCodeParam infoCodeParam) {
        List<DtpRwdObjectDTO> objects = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setId(infoCodeParam.getObjectIds()));
        InfoCodeInitByObjectParam param = new InfoCodeInitByObjectParam();
        param.setObjects(objects);
        param.setRunningMonitorType(RunningMonitorTypeEnum.RUN_ANALYSIS);
        param.setRunningMonitorSecondType(infoCodeParam.getSecondType());
        param.setTemplateTypes(Lists.newArrayList(InfoCodeTemplateTypeEnum.CURVE));
        List<InfoCodeAggregate> infoCodeAggregates = infoCodeGateway.of(param);
        if (CollectionUtils.isEmpty(infoCodeAggregates)) {
            return Collections.emptySet();
        }
        infoCodeGateway.fillInfoCodeDefineModel(infoCodeAggregates);
        Map<String, AnalysisInfoCodeVO> analysisInfoCodeMap = getAnalysisInfoCodeVOMap(infoCodeAggregates);
        return analysisInfoCodeMap.values();
    }

    /**
     * 获取分析信息代码vomap
     *
     * @param infoCodeAggregates 信息代码聚合
     * @return {@link Map }<{@link String }, {@link AnalysisInfoCodeVO }>
     */
    private Map<String, AnalysisInfoCodeVO> getAnalysisInfoCodeVOMap(List<InfoCodeAggregate> infoCodeAggregates) {
        Map<String, AnalysisInfoCodeVO> analysisInfoCodeMap = new HashMap<>();
        for (InfoCodeAggregate infoCodeAggregate : infoCodeAggregates) {
            AnalysisInfoCodeVO vo = new AnalysisInfoCodeVO();
            InfoCodeDefineModel infoCodeAndDefineModel = infoCodeAggregate.getInfoCodeAndDefineModel();
            InfoCodeGroupModel groupModel = infoCodeAggregate.getGroupModel();
            String code = infoCodeAndDefineModel.getCode();
            if (!analysisInfoCodeMap.containsKey(code)) {
                vo.fillByInfoCodeAndDefineModel(infoCodeAndDefineModel);
                vo.fillByGroupModel(groupModel);
                analysisInfoCodeMap.put(code, vo);
            }
        }
        return analysisInfoCodeMap;
    }

    @Override
    public List<AnalysisEquipmentVO> listEquip(AnalysisListEquipmentParam equipmentParam) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setClassCode(equipmentParam.getClassCode());
        param.filterMonitoringSurveillanceStatus();
        //如果是新风分析且是空调机组的时候，还需要加上有无新风段信息点判断
        if (equipmentParam.getSecondType() == RunningMonitorSecondTypeEnum.RUN_ANALYSIS_FRESH_AIR
                && equipmentParam.getClassCode().equals(DtpRwdClassCodeEnum.ACATAH.getValue())) {
            param.addInfoCode(InfoCodeEnum.IS_FRESH_AIR_SECTION.getCode(), "1");
        }
        param.addWithColumn(DtpRwdEquipmentDTO.Fields.class);
        List<DtpRwdEquipmentDTO> dtpRwdObjectDTOS = dtpRwdClientService.listEquipment(param);
        if (CollectionUtils.isEmpty(dtpRwdObjectDTOS)) {
            return Collections.emptyList();
        }
        Set<String> floorIdAndBuildingIds = Sets.newHashSet();
        dtpRwdObjectDTOS.forEach(e -> {
            String floorId = e.getFloorId();
            if (floorId != null) {
                floorIdAndBuildingIds.add(floorId);
            }
            String buildingId = e.getBuildingId();
            if (buildingId != null) {
                floorIdAndBuildingIds.add(buildingId);
            }
        });
        List<AnalysisEquipmentVO> analysisEquipmentVOS = BeanUtil.copyToList(dtpRwdObjectDTOS, AnalysisEquipmentVO.class);
        Map<String, String> mapObjectIdAndLocalName = dtpRwdClientService.mapObjectIdAndLocalName(false, floorIdAndBuildingIds);
        if (MapUtils.isNotEmpty(mapObjectIdAndLocalName)) {
            analysisEquipmentVOS.forEach(e -> {
                e.setFloorName(mapObjectIdAndLocalName.get(e.getFloorId()));
                e.setBuildingName(mapObjectIdAndLocalName.get(e.getBuildingId()));
            });
        }
        //查询设备服务的空间
        Set<String> equioSet = analysisEquipmentVOS.stream().map(AnalysisEquipmentVO::getId).collect(Collectors.toSet());
        DtpRwdObjectQueryParam relParam = new DtpRwdObjectQueryParam();
        relParam.setClassCode(DtpRwdClassCodeEnum.SPACE.value());
        DtpRwdObjectRelationQueryParam relationTo = new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE);
        relationTo.setObjFrom(equioSet);
        relParam.setRelationTo(relationTo);
        relParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.relObjIds);
        List<DtpRwdObjectDTO> relSpaces = dtpRwdClientService.listObject(relParam);
        Map<String, Set<String>> mapRelObjIdAndIds = DtpRwdObjectDTO.mapRelObjIdAndIds(relSpaces);
        if (MapUtils.isNotEmpty(mapRelObjIdAndIds)) {
            analysisEquipmentVOS.forEach(e -> e.setRelObjIds(mapRelObjIdAndIds.get(e.getId())));
        }
        return analysisEquipmentVOS;
    }

    @Override
    public SpaceDetailVO spaceDetail(AnalysisSpaceDetailParam param) {
        String spaceId = param.getSpaceId();
        LocalDate queryDate = param.getQueryDate();
        DtpRwdSpaceDTO space = dtpRwdClientService.getSpace(new DtpRwdObjectQueryParam().setId(spaceId));
        if (space == null) {
            return null;
        }
        SpaceDetailVO vo = new SpaceDetailVO();
        vo.fillBySpaceDTO(space);
        List<String> floorIdAndBuildingId = Arrays.asList(space.getFloorId(), space.getBuildingId());
        Map<String, String> mapObjectIdAndLocalName = dtpRwdClientService.mapObjectIdAndLocalName(false, floorIdAndBuildingId);
        if (MapUtils.isNotEmpty(mapObjectIdAndLocalName)) {
            vo.setFloorName(mapObjectIdAndLocalName.get(space.getFloorId()));
            vo.setBuildingName(mapObjectIdAndLocalName.get(space.getBuildingId()));
        }

        List<AnalysisAirConditionerSpace> analysisAirConditionerSpaces = analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(
                Collections.singletonList(spaceId), null, null, queryDate, null, null);
        SpaceDetailVO.BasicInfoVO basicInfo = vo.getBasicInfo() == null ? new SpaceDetailVO.BasicInfoVO() : vo.getBasicInfo();
        SpaceDetailVO.DataAnalysisVO dataAnalysis = vo.getDataAnalysis() == null ? new SpaceDetailVO.DataAnalysisVO() : vo.getDataAnalysis();
        if (CollectionUtils.isNotEmpty(analysisAirConditionerSpaces)) {
            //分析数据，一天一条
            for (AnalysisAirConditionerSpace analysisAirConditionerSpace : analysisAirConditionerSpaces) {
                basicInfo.setBusinessTimeRangeList(analysisAirConditionerSpace.serializeBusinessTime());
                if (analysisAirConditionerSpace.getTagType() == RunningMonitorSecondTypeEnum.RUN_QUALIFY_CO2) {
                    dataAnalysis.setCo2ComplianceRate(NumberUtil.div(analysisAirConditionerSpace.getSatisfyTimeNumber(), analysisAirConditionerSpace.getTimeNumber(), 3));
                } else if (analysisAirConditionerSpace.getTagType() == RunningMonitorSecondTypeEnum.RUN_QUALIFY_TDB){
                    dataAnalysis.setTdbComplianceRate(NumberUtil.div(analysisAirConditionerSpace.getSatisfyTimeNumber(), analysisAirConditionerSpace.getTimeNumber(), 3));
                }
            }
            vo.setDataAnalysis(dataAnalysis);
            vo.setBasicInfo(basicInfo);
        }
        buildTdbAndCO2RealValue(spaceId, queryDate, vo);
        buildTdbAndCO2TargetValue(spaceId, queryDate, vo);
        buildTdbQualityAndCo2Quality(queryDate, vo, spaceId);

        return vo;
    }

    @Override
    public List<ClassCodeVO> listTerminalClassCode(RunningMonitorSecondTypeEnum secondType) {
        List<String> classCode;
        if (secondType == RunningMonitorSecondTypeEnum.RUN_ANALYSIS_AIR_TERMINAL) {
            classCode = TERMINAL_CLASS_CODES;
        } else if (secondType == RunningMonitorSecondTypeEnum.RUN_ANALYSIS_FRESH_AIR) {
            classCode = FRESH_AIR_CLASS_CODES;
        } else {
            return Collections.emptyList();
        }
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam().setClassCode(classCode);
        param.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(param);
        if (CollectionUtils.isEmpty(dtpRwdObjectDTOS)) {
            return Collections.emptyList();
        }

        Set<String> classCodes = dtpRwdObjectDTOS.stream().map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toSet());
        List<ClassCodeVO> result = new ArrayList<>();
        classCodes.forEach(e ->

        {
            DtpRwdClassCodeEnum analyze = DtpRwdClassCodeEnum.analyze(e);
            if (analyze == null) {
                return;
            }
            String classCodeName = dtpKnowledgeClientService.getClassCodeName(false, false, e);
            classCodeName = StringUtils.isEmpty(classCodeName)? analyze.getReasonPhrase() : classCodeName;
            result.add(new ClassCodeVO(analyze.value(), classCodeName));
        });
        return result;
    }

    @Override
    public Map<String, Integer> getSpaceTagCount(LocalDate day, RunningMonitorSecondTypeEnum tagType, List<String> spaceIds) {
        analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(spaceIds, null, null, day, null, tagType);
        return null;
    }

    @Override
    public SpaceSummaryVo getSpaceSummary(BasicsAnalysisParam param) {
        SpaceSummaryVo spaceSummaryVo = new SpaceSummaryVo();
        Double sp = analysisAirConditionerSpacesService.spaceSatisfyPercentage(param.getDate(), param.getType(), Sets.newHashSet(param.getInstanceIds()));
        if(sp != null) {
            spaceSummaryVo.setSatisfyPercentage(String.format("%.2f", sp * 100));
        }

        Map<String,Set<String>> tagSpaces = new HashMap<>();
        Map<String,Map<String,BigDecimal>> spaceAnalysis = new HashMap<>();
        Map<String,SpaceDetailVO> spaceDetailVOMap = new HashMap<>();

        //查明细
        List<AnalysisAirConditionerSpace> analysisAirConditionerSpaces = analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(param.getInstanceIds(), null, null, param.getDate(), null, param.getType());
        if (CollectionUtils.isNotEmpty(analysisAirConditionerSpaces)) {
            for(AnalysisAirConditionerSpace analysisAirConditionerSpace : analysisAirConditionerSpaces){
                List<TimeTagPO> timeTags = analysisAirConditionerSpace.getTimeTag();
                SpaceDetailVO spaceDetailVO = new SpaceDetailVO();
                String spaceId = analysisAirConditionerSpace.getSpaceId();
                spaceDetailVO.setSpaceId(analysisAirConditionerSpace.getSpaceId());
                spaceDetailVO.setSpaceName(analysisAirConditionerSpace.getSpaceName());
                spaceDetailVOMap.put(spaceId, spaceDetailVO);
                for(TimeTagPO timeTag : timeTags){
                    String tag = timeTag.getTagKey();
                    if(!AirTerminalTemperatureIntervalEnum.APPROPRIATE.value().equals(tag)){
                        BigDecimal value = new BigDecimal(Optional.ofNullable(timeTag.getTagValue()).orElse("0"));
                        tagSpaces.putIfAbsent(tag, new HashSet<>());
                        tagSpaces.get(tag).add(analysisAirConditionerSpace.getSpaceId());

                        //生成空间统计
                        String tagCount = tag + "_Count";
                        String tagValue = tag + "_Value";

                        Map<String, BigDecimal> tagCountMap = spaceAnalysis.get(tagCount)==null?new HashMap<>():spaceAnalysis.get(tagCount);
                        tagCountMap.putIfAbsent(spaceId,BigDecimal.ZERO);
                        tagCountMap.put(spaceId,tagCountMap.get(spaceId).add(BigDecimal.ONE));
                        spaceAnalysis.put(tagCount,tagCountMap);

                        Map<String, BigDecimal> tagValueMap = spaceAnalysis.get(tagValue)==null?new HashMap<>():spaceAnalysis.get(tagValue);
                        tagValueMap.putIfAbsent(spaceId,value);
                        if(value.compareTo(tagValueMap.get(spaceId))>0){
                            tagValueMap.put(spaceId,value);
                        }
                        spaceAnalysis.put(tagValue,tagValueMap);
                    }
                }
            }
            spaceSummaryVo.setTagCount(tagSpaces.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size())));
            spaceSummaryVo.setSpaceAnalysis(new HashMap<>());
            for(String tag:spaceAnalysis.keySet()){
                String maxSpaceId = "";
                BigDecimal maxValue = BigDecimal.ZERO;
                Map<String, BigDecimal> tagBigDecimalMap = spaceAnalysis.get(tag);
                for (String spaceId:tagBigDecimalMap.keySet()){
                    BigDecimal value = tagBigDecimalMap.get(spaceId);
                    if(value.compareTo(maxValue)>0){
                        maxSpaceId = spaceId;
                        maxValue = value;
                    }
                }
                spaceSummaryVo.getSpaceAnalysis().put(tag,spaceDetailVOMap.get(maxSpaceId));
            }
        }
        return spaceSummaryVo;
    }

    private void buildTdbAndCO2TargetValue(String spaceId, LocalDate queryDate, SpaceDetailVO vo) {
        Map<String, Map<String, Map<LocalTime, DemandTargetResultDTO>>> mapSpaceIdAndDemandStatusToDay = demandTargetTypeInfoCodeAnalyzeStrategy.mapSpaceIdAndDemandStatusToDay(Sets.newHashSet(spaceId), queryDate);
        if (MapUtils.isEmpty(mapSpaceIdAndDemandStatusToDay)) {
            return;
        }
        Map<LocalTime, DemandTargetResultDTO> tdbTimeMap = MapExecutor.dynamicGet(mapSpaceIdAndDemandStatusToDay, DtpRwdSpaceDTO.Fields.tdb, spaceId);
        Map<LocalTime, DemandTargetResultDTO> co2TimeMap = MapExecutor.dynamicGet(mapSpaceIdAndDemandStatusToDay, DtpRwdSpaceDTO.Fields.co2, spaceId);
        List<DataValueVO> tdbTargetValue = new LinkedList<>();
        List<DataValueVO> co2TargetValue = new LinkedList<>();
        if (MapUtils.isNotEmpty(tdbTimeMap)) {
            tdbTimeMap.forEach((time, targetResult) -> {
                AirTerminalDemandStatusEnum demandStatus = targetResult.getDemandStatus();
                if (demandStatus == AirTerminalDemandStatusEnum.TARGET_VALUE) {
                    tdbTargetValue.add(new DataValueVO(targetResult.getDemandStatusValue(),
                            com.persagy.apccontrol.basic.model.enumtype.systemgraph.InfoCodeEnum.SPACE_TDB.getUnit(),
                            LocalDateTime.of(queryDate, time)));
                }
            });
        }
        if (MapUtils.isNotEmpty(co2TimeMap)) {
            co2TimeMap.forEach((time, targetResult) -> {
                AirTerminalDemandStatusEnum demandStatus = targetResult.getDemandStatus();
                if (demandStatus == AirTerminalDemandStatusEnum.TARGET_VALUE) {
                    co2TargetValue.add(new DataValueVO(targetResult.getDemandStatusValue(),
                            com.persagy.apccontrol.basic.model.enumtype.systemgraph.InfoCodeEnum.SPACE_CO2.getUnit(),
                            LocalDateTime.of(queryDate, time)));
                }
            });
        }
        vo.getDataAnalysis().setTargetTemperature(tdbTargetValue);
        vo.getDataAnalysis().setTargetCo2(co2TargetValue);
    }

    /**
     * 建立tdb和co2实际值
     *
     * @param spaceId   空间id
     * @param queryDate 查询日期
     * @param vo        vo
     */
    private void buildTdbAndCO2RealValue(String spaceId, LocalDate queryDate, SpaceDetailVO vo) {
        Map<String, Map<String, List<EnergyDataResponse>>> valueMap = queryRwdObjectValueMap(Sets.newHashSet(spaceId), queryDate, EnumTimeType.MINUTE5,
                Lists.newArrayList(InfoCodeEnum.SPACE_TDB.getCode(), InfoCodeEnum.SPACE_CO2.getCode()));
        if (MapUtils.isEmpty(valueMap)) {
            return;
        }
        Map<String, List<EnergyDataResponse>> infoCodeMap = valueMap.get(spaceId);
        List<EnergyDataResponse> tdbValues = infoCodeMap.get(InfoCodeEnum.SPACE_TDB.getCode());
        List<EnergyDataResponse> co2Values = infoCodeMap.get(InfoCodeEnum.SPACE_CO2.getCode());
        SpaceDetailVO.DataAnalysisVO dataAnalysis = vo.getDataAnalysis() == null ? new SpaceDetailVO.DataAnalysisVO() : vo.getDataAnalysis();
        if (CollectionUtils.isNotEmpty(tdbValues)) {
            List<DataValueVO> tdbRealValue = tdbValues.stream().map(e -> {
                DataValueVO dataValueVO = new DataValueVO();
                dataValueVO.setDataValue(e.getIpValue());
                dataValueVO.setDataTime(LocalDateTime.parse(e.getIpTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                dataValueVO.setUnit(com.persagy.apccontrol.basic.model.enumtype.systemgraph.InfoCodeEnum.SPACE_TDB.getUnit());
                return dataValueVO;
            }).collect(Collectors.toList());
            dataAnalysis.setTemperatureRealValue(tdbRealValue);
        }
        if (CollectionUtils.isNotEmpty(co2Values)) {
            List<DataValueVO> co2RealValue = co2Values.stream().map(e -> {
                DataValueVO dataValueVO = new DataValueVO();
                dataValueVO.setDataValue(e.getIpValue());
                dataValueVO.setDataTime(LocalDateTime.parse(e.getIpTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                dataValueVO.setUnit(com.persagy.apccontrol.basic.model.enumtype.systemgraph.InfoCodeEnum.SPACE_CO2.getUnit());
                return dataValueVO;
            }).collect(Collectors.toList());
            dataAnalysis.setCo2RealValue(co2RealValue);
        }
        vo.setDataAnalysis(dataAnalysis);
    }


    /**
     * 构建温度质量和co2质量
     *
     * @param queryDate 查询日期
     * @param vo        vo
     * @param spaceId   空间id
     */
    private void buildTdbQualityAndCo2Quality(LocalDate queryDate, SpaceDetailVO vo, String spaceId) {
        MidCalendarAirSeasonTypeEnum airSeasonType = midCalendarClientService
                .getAirSeasonType(ContextUtil.getProjectId(), queryDate);
        if (airSeasonType == null) {
            return;
        }
        BaseDemandTemplateParam baseDemandTemplateParam = new BaseDemandTemplateParam();
        baseDemandTemplateParam.setDate(queryDate);
        baseDemandTemplateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        Optional<DemandTemplate> template = demandConfigGateway.getByTemplate(baseDemandTemplateParam);
        if (!template.isPresent()) {
            return;
        }
        DemandConfigByInstanceQueryParam param = new DemandConfigByInstanceQueryParam();
        param.setDate(queryDate);
        param.setInstance(new DemandInstanceValueObject(DemandInstanceTypeEnum.SPACE, spaceId));
        param.setTemplateId(template.get().getId());
        Optional<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> optional = demandConfigGateway.getConfigByInstance(param);
        if (!optional.isPresent()) {
            return;
        }
        DemandConfigAggregate<DemandConfigAirconditioningValueObject> config = optional.get();
        DemandConfigValueObject<DemandConfigAirconditioningValueObject> finalConfig = config.getFinalConfig();
        DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality = null;
        DemandConfigAirconditioningCO2QualityValueObject co2Quality = null;
        if (Objects.nonNull(finalConfig)) {
            DemandConfigAirconditioningEnvironmentQualityObject environmentQuality =
                    finalConfig.getGlobalConfig().getEnvironmentQuality();
            if (Objects.nonNull(environmentQuality)) {
                DemandConfigAirconditioningTdbQualityValueObject tdbQuality = environmentQuality.getTdbQuality();
                if (Objects.nonNull(tdbQuality)) {
                    tdbSeasonQuality = tdbQuality.getCurrentSeasonQuality(airSeasonType);
                }
                co2Quality = environmentQuality.getCO2Quality();
            }
            DemandConfigAirconditioningBaseConfigValueObject baseConfig = finalConfig.getGlobalConfig().getBaseConfig();
            if (Objects.nonNull(baseConfig)) {
                SpaceDetailVO.BasicInfoVO basicInfo = vo.getBasicInfo();
                if (Objects.isNull(basicInfo)) {
                    basicInfo = new SpaceDetailVO.BasicInfoVO();
                }
                List<DemandTimeIntervalValueObject> airConditioningDemandTimeIntervals = baseConfig.getAirConditioningDemandTimeIntervals();
                LinkedHashSet<String> timeList = new LinkedHashSet<>();
                if (CollectionUtils.isNotEmpty(airConditioningDemandTimeIntervals)) {
                    timeList = airConditioningDemandTimeIntervals.stream().map(e -> e.getBeginTime().getAbsoluteTime() + "," + e.getEndTime().getAbsoluteTime())
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                }
                Boolean isAlgorithmCompute = baseConfig.getIsAlgorithmCompute();
                if (BooleanUtils.isTrue(isAlgorithmCompute)) {
                    basicInfo.setIsAlgorithmCompute(isAlgorithmCompute);
                } else {
                    basicInfo.setIsAlgorithmCompute(false);
                    basicInfo.setAirConditioningDemandTimeRangeList(timeList);
                }
                vo.setBasicInfo(basicInfo);
            }
        }
        vo.setCo2Standard(co2Quality);
        vo.setTemperatureStandard(tdbSeasonQuality);
        SpaceDetailVO.DataAnalysisVO dataAnalysis = vo.getDataAnalysis();
        if (Objects.isNull(dataAnalysis)) {
            dataAnalysis = new SpaceDetailVO.DataAnalysisVO();
        }
        dataAnalysis.setTemperatureStandard(tdbSeasonQuality);
        dataAnalysis.setCo2Standard(co2Quality);
        vo.setDataAnalysis(dataAnalysis);
    }

    private void setSpacesAnalysis(List<AnalysisAirConditionerSpace> spacesAnalysis, AirConditionerSpaceAnalysisVO vo) {
        List<SpacesAnalysis> spacesAnalysisList = new ArrayList<>();

        //把 AnalysisAirConditionerSpaces 转换为 SpacesAnalysis
        for (AnalysisAirConditionerSpace analysis : spacesAnalysis) {
            SpacesAnalysis spaces = new SpacesAnalysis();
            spaces.setSpaceId(analysis.getSpaceId());
            spaces.setSpaceName(analysis.getSpaceName());
            spaces.setFloorId(analysis.getFloorId());
            spaces.setTimeTag(analysis.getTimeTag());
            spaces.setBusinessTime(analysis.getBusinessTime());
            spaces.setInfoCodeRule(analysis.getInfoCodeRule());
            spacesAnalysisList.add(spaces);
        }
        vo.setSpacesAnalysisList(spacesAnalysisList);
    }

    @Override
    public String logTag() {
        return "运行分析";
    }
}
