package com.persagy.apccontrol.basic.service.demand.airconditioning.impl;

import cn.hutool.core.builder.GenericBuilder;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.ElectricityPriceQueryParam;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarGlobalRuleDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigLogDto;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogContentDto;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfigLog;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTemplateEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.DemandValueSelectTypeQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.ValueSelectQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.valueselect.DemandValueSelectTypeConfigQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.airconditioning.DemandValueSelectTypeVO;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.param.ElectricityData.CarbonCategoryDataParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.ElectricityData.CarbonCategoryDataResponse;
import com.persagy.apccontrol.basic.outside.feign.service.ElectricityDataClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigLogService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogInsertParam;
import com.persagy.apccontrol.basic.service.BizComputeService;
import com.persagy.apccontrol.basic.service.demand.airconditioning.ValueSelectionService;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectElecQueryParam;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectInsertParam;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectQueryParamByPage;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ValueSelectionServiceImpl implements ValueSelectionService, LogComponent {

    private static final String LOG_TAG = "价值选择";

    @Autowired
    private RcBasicsConfigLogService rcBasicsConfigLogService;
    @Autowired
    private MidPermissionClientService midPermissionClientService;
    @Autowired
    private MidCalendarClientService midCalendarClientService;
    @Autowired
    private ElectricityDataClientService electricityDataClientService;

    @Autowired
    private BizComputeService bizComputeService;

    @Lazy
    @Autowired
    private DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;

    @Override
    public List<DemandValueSelectTypeVO> listValueSelectType(@NonNull DemandValueSelectTypeQueryParam param) {
        // 获取当前项目配置的价值选择类型
        BasicsConfigTypeEnum valueSelectType = this.demandConfigAirconditioningCustomService.getValueSelectType(param.getProjectId(), DateUtils.now().toLocalDate());
        return TYPE_LIST.stream().map(item -> new DemandValueSelectTypeVO(item.getExplain(), item.equals(valueSelectType))).collect(Collectors.toList());
    }

    @Override
    public List<ValueSelectConfigDto> listConfigsByType(@NonNull DemandValueSelectTypeConfigQueryParam param) {
        param.checkAndSet();
        BasicsConfigTypeEnum type = param.getType();
        DemandTemplate template = param.queryAndGetTemplate();
        if (Objects.isNull(template)) {
            logProjectInstance().warn("模版不存在", Pair.of("param", param));
            return Lists.newArrayList();
        }
        return demandConfigAirconditioningCustomService.getValueSelectRangeConfig(template.getId(), type);
    }
    
    
    @Override
    public JSONObject currentTempAndCarbonRate(ValueSelectQueryParam param) {
        JSONObject response = new JSONObject();
        RcBasicsConfig rcBasicsConfig = this.currentApplyConfig(param);
        if (Objects.isNull(rcBasicsConfig)) {
            return response;
        }
        response.put("type", rcBasicsConfig.getType());
        response.put("config", StrUtil.isBlank(rcBasicsConfig.getResult()) ? new JSONArray() : JSONArray.parseArray(rcBasicsConfig.getResult()));

        if (BasicsConfigTypeEnum.VALUE_SELECT_QUALITY_FIRST.equals(rcBasicsConfig.getType()) || BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_ENERGY.equals(rcBasicsConfig.getType())
                || StrUtil.isBlank(rcBasicsConfig.getResult()) || StrUtil.isBlank(param.getIpTime())) {
            return response;
        }
        List<ValueSelectionTrendTableDto.ElecData> iotDataList = new ArrayList<>();
        try {
            LocalDateTime startTime = LocalDateTime.parse(param.getIpTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            LocalDateTime endTime = StringUtils.isEmpty(param.getEndTime()) ? startTime.plusMinutes(15) : LocalDateTime.parse(param.getEndTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            // 获取项目的城市编码
            ProjectDTO projectDTO = midPermissionClientService.getProject(false, true, param.getProjectId());
            if (BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CARBON_EMISSIONS.equals(rcBasicsConfig.getType())) {
                // 查询碳因子数据
                CarbonCategoryDataParam queryParam = GenericBuilder.of(CarbonCategoryDataParam::new)
                        .with(CarbonCategoryDataParam::setProjectId, param.getProjectId())
                        .with(CarbonCategoryDataParam::setCityCode, projectDTO.getCityCode())
                        .with(CarbonCategoryDataParam::setGroupCode, ContextUtil.getGroupCode())
                        .with(CarbonCategoryDataParam::setCategoryCode, "CREL")
                        .with(CarbonCategoryDataParam::setBeginTime, startTime)
                        .with(CarbonCategoryDataParam::setEndTime, endTime).build();
                List<CarbonCategoryDataResponse.CarBonData> carbonDataList = electricityDataClientService.listCarbonCategoryData(queryParam);
                if (CollectionUtils.isNotEmpty(carbonDataList)) {
                    BigDecimal value = CollectionUtils.isEmpty(carbonDataList.get(0).getDataList()) ? null : carbonDataList.get(0).getDataList().get(0).getValue();
                    response.put("iot", value);
                    for (CarbonCategoryDataResponse.CarBonData carBonData : carbonDataList) {
                        BigDecimal dataValue = CollectionUtils.isEmpty(carBonData.getDataList()) ? null : carBonData.getDataList().get(0).getValue();
                        iotDataList.add(GenericBuilder.of(ValueSelectionTrendTableDto.ElecData::new)
                                .with(ValueSelectionTrendTableDto.ElecData::setDataTime, carBonData.getDate())
                                .with(ValueSelectionTrendTableDto.ElecData::setDataValue, dataValue)
                                .build());
                    }
                }
            } else {
                // 获取电价
                ElectricityPriceQueryParam queryParam = new ElectricityPriceQueryParam();
                queryParam.setElectricityType("1");
                queryParam.setStartTime(startTime);
                queryParam.setEndTime(endTime);
                List<EnergyDataResponse> elecDataList = electricityDataClientService.listElectrovalenceData(queryParam);
                if (CollectionUtils.isNotEmpty(elecDataList)) {
                    response.put("iot", elecDataList.get(0).getIpValue());
                    for (EnergyDataResponse data : elecDataList) {
                        ValueSelectionTrendTableDto.ElecData elecData = new ValueSelectionTrendTableDto.ElecData();
                        elecData.setDataTime(LocalDateTime.parse(data.getIpTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                        elecData.setDataValue(data.getIpValue());
                        iotDataList.add(elecData);
                    }
                }
            }
            response.put("iotDataList", iotDataList);
        } catch (Exception e) {
            LogExecutor.project(param.getProjectId()).error(log, LOG_TAG, "查询温度二氧化碳满足率失败", e,
                    Collections.singletonList(Pair.of("param", param)));
        }
        return response;
    }

    @Override
    public RcBasicsConfig currentApplyConfig(@NonNull ValueSelectQueryParam param) {
        // 获取当前项目配置的价值选择类型
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        templateParam.setDate(param.getDate());
        DemandConfigAirconditioningCustomDTO valueSelectConfig = this.demandConfigAirconditioningCustomService.getCustomConfig(templateParam);
        RcBasicsConfig rcBasicsConfig = new RcBasicsConfig();
        if (Objects.nonNull(valueSelectConfig)) {
            rcBasicsConfig.setType(valueSelectConfig.getValueSelectType());
            rcBasicsConfig.setResult(CollectionUtils.isEmpty(valueSelectConfig.parseValueSelectRange()) ? null : JsonUtils.toJson(valueSelectConfig.parseValueSelectRange()));
        }
        return rcBasicsConfig;
    }

    @Override
    public boolean saveConfig(ValueSelectInsertParam param) {
        return this.demandConfigAirconditioningCustomService.saveOrUpdateValueSelectRange(param.getTemplateId(), param.getType(), param.getDetails());
    }

    /**
     * 根据价值选择类型获取对应日志分类
     *
     * @param type 价值选择类型
     * @return 日志分类
     */
    RunningLogCategoryEnum getLogCategoryByType(BasicsConfigTypeEnum type) {
        if (type.equals(BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_ENERGY)) {
            return RunningLogCategoryEnum.ENERGY_LOWEST_ADJUST;
        } else if (type.equals(BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CAST)) {
            return RunningLogCategoryEnum.COST_LOWEST_ADJUST;
        } else if (type.equals(BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CARBON_EMISSIONS)) {
            return RunningLogCategoryEnum.CARBON_EMISSIONS_LOWEST_ADJUST;
        }
        return null;
    }

    /**
     * 判定配置是否一致
     *
     * @param beforeConfigList 之前配置
     * @param configList       当前配置
     * @return 配置是否一致
     */
    boolean judgeConsistence(List<ValueSelectConfigDto> beforeConfigList, List<ValueSelectConfigDto> configList) {
        for (int i = 0; i < beforeConfigList.size(); i++) {
            ValueSelectConfigDto dto = beforeConfigList.get(i);
            ValueSelectConfigDto dto1 = configList.get(i);
            if (dto.getIntervalMin().doubleValue() != dto1.getIntervalMin().doubleValue() || dto.getIntervalMax().doubleValue() != dto1.getIntervalMax().doubleValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取日志内容详情
     *
     * @param config   配置
     * @param before   之前配置
     * @param category 日志分类
     * @param insert   日志新增参数
     */
    void setDetail(RcBasicsConfig config, RcBasicsConfig before, RunningLogCategoryEnum category, RunningLogInsertParam insert) {
        List<ValueSelectConfigDto> beforeConfigList = JSONArray.parseArray(before.getResult(), ValueSelectConfigDto.class);
        List<ValueSelectConfigDto> configList = JSONArray.parseArray(config.getResult(), ValueSelectConfigDto.class);
        List<RunningLogContentDto> details = new ArrayList<>();
        insert.setDetail(details);
        if (category.equals(RunningLogCategoryEnum.ENERGY_LOWEST_ADJUST)) {
            if (!Objects.equals(beforeConfigList.get(0).getTempFillRate(), configList.get(0).getTempFillRate())) {
                details.add(insert.buildDto(RunningLogTemplateEnum.PARAM_ADJUST_DETAIL.getTemplate(), LocaleMessageUtil.getMessage("key_c7f94669e0c94d1497a505e8c288a703"), beforeConfigList.get(0).getTempFillRate().toString(), configList.get(0).getTempFillRate().toString()));
            }
            if (!Objects.equals(beforeConfigList.get(0).getCo2FillRate(), configList.get(0).getCo2FillRate())) {
                details.add(insert.buildDto(RunningLogTemplateEnum.PARAM_ADJUST_DETAIL.getTemplate(), LocaleMessageUtil.getMessage("key_3f6c2765c6504148a93a642c1d247484"), beforeConfigList.get(0).getCo2FillRate().toString(), configList.get(0).getCo2FillRate().toString()));
            }
        } else {
            String param = "";
            String unit = "";
            if (category.equals(RunningLogCategoryEnum.COST_LOWEST_ADJUST)) {
                param = LocaleMessageUtil.getMessage("key_585357157efb4d12aa94dbd2126061aa");
                unit = LocaleMessageUtil.getMessage("key_1b9799c7f08d477f8f8c1492daf227d8");
            } else if (category.equals(RunningLogCategoryEnum.CARBON_EMISSIONS_LOWEST_ADJUST)) {
                param = LocaleMessageUtil.getMessage("key_8aff2df472084977acf45633ae254330");
                unit = "kgCO₂/kWh";
            }
            if (beforeConfigList.size() != configList.size()) {
                details.add(insert.buildDto(RunningLogTemplateEnum.VALUE_SELECTION_CONFIG_ADJUST_DETAIL.getTemplate(),
                        param, beforeConfigList.size() + LocaleMessageUtil.getMessage("key_b6fed673a764491983ab130292f2f15d"), configList.size() + LocaleMessageUtil.getMessage("key_b6fed673a764491983ab130292f2f15d"), LocaleMessageUtil.getMessage("key_b7c4e5360711407182064d30bccc1cbe")));
            } else {
                if (judgeConsistence(beforeConfigList, configList)) {
                    for (int i = 0; i < beforeConfigList.size(); i++) {
                        ValueSelectConfigDto dto = beforeConfigList.get(i);
                        ValueSelectConfigDto dto1 = configList.get(i);
                        if (dto.getTempFillRate().doubleValue() != dto1.getTempFillRate().doubleValue()) {
                            details.add(insert.buildDto(RunningLogTemplateEnum.VALUE_SELECTION_PARAM_ADJUST_DETAIL.getTemplate(),
                                    LocaleMessageUtil.getMessage("key_519c7010443240a9a14c732a38258f38") + dto.getIntervalMin() + "≤" + param + "≤" + dto.getIntervalMax() + unit + LocaleMessageUtil.getMessage("key_2d64b826882e4b4984ad124883ece393"), LocaleMessageUtil.getMessage("key_c7f94669e0c94d1497a505e8c288a703"), dto.getTempFillRate() + "%", dto1.getTempFillRate() + "%"));
                        }
                        if (dto.getCo2FillRate().doubleValue() != dto1.getCo2FillRate().doubleValue()) {
                            details.add(insert.buildDto(RunningLogTemplateEnum.VALUE_SELECTION_PARAM_ADJUST_DETAIL.getTemplate(),
                                    LocaleMessageUtil.getMessage("key_519c7010443240a9a14c732a38258f38") + dto.getIntervalMin() + "≤" + param + "≤" + dto.getIntervalMax() + unit + LocaleMessageUtil.getMessage("key_2d64b826882e4b4984ad124883ece393"), LocaleMessageUtil.getMessage("key_3f6c2765c6504148a93a642c1d247484"), dto.getCo2FillRate() + "%", dto1.getCo2FillRate() + "%"));
                        }
                    }
                } else {
                    details.add(insert.buildDto(RunningLogTemplateEnum.VALUE_SELECTION_PARAM_CONFIG_ADJUST_DETAIL.getTemplate(),
                            param + LocaleMessageUtil.getMessage("key_b3e844a41c1f4e148bd399a14b90a514"), LocaleMessageUtil.getMessage("key_b7c4e5360711407182064d30bccc1cbe")));
                }
            }
        }
    }

    @Override
    public List<ValueSelectConfigLogDto> listLog(ValueSelectQueryParamByPage param) {
        List<ValueSelectConfigLogDto> resultList = new ArrayList<>();
        List<RcBasicsConfigLog> configLog = rcBasicsConfigLogService.lambdaQuery()
                .eq(RcBasicsConfigLog::getProjectId, param.getProjectId())
                .ge(RcBasicsConfigLog::getCreateTime, param.getStartTime())
                .lt(RcBasicsConfigLog::getCreateTime, param.getEndTime())
                .in(RcBasicsConfigLog::getType, TYPE_LIST)
                .orderByDesc(RcBasicsConfigLog::getCreateTime)
                .last("limit " + (param.getPageNumber() - 1) * param.getPageSize() + "," + param.getPageSize())
                .list();

        if (CollectionUtils.isEmpty(configLog)) {
            return new ArrayList<>();
        }
        Map<String, RcBasicsConfigLog> beforeValueMap = configLog.stream().collect(Collectors.toMap(RcBasicsConfigLog::getId, basicsConfigLog -> basicsConfigLog));
        for (RcBasicsConfigLog log : configLog) {
            List<ValueSelectConfigDto> list = JSONObject.parseArray(log.getResult(), ValueSelectConfigDto.class);
            BasicsConfigTypeEnum beforeType = beforeValueMap.get(log.getParentId()) == null ? null : beforeValueMap.get(log.getParentId()).getType();
            Date beforeDateTime = beforeValueMap.get(log.getParentId()) == null ? null : beforeValueMap.get(log.getParentId()).getCreateTime();
            if (beforeType == null) {
                RcBasicsConfigLog config = rcBasicsConfigLogService.getById(log.getParentId());
                beforeType = config == null ? null : config.getType();
                beforeDateTime = config == null ? null : config.getCreateTime();
            }
            ValueSelectConfigLogDto dto = GenericBuilder.of(ValueSelectConfigLogDto::new).with(ValueSelectConfigLogDto::setProjectId, log.getProjectId())
                    .with(ValueSelectConfigLogDto::setDateTime, LocalDateTime.ofInstant(log.getCreateTime().toInstant(), DateUtils.getZoneId()))
                    .with(ValueSelectConfigLogDto::setBeforeDateTime, beforeDateTime != null ? LocalDateTime.ofInstant(beforeDateTime.toInstant(), DateUtils.getZoneId()) : null)
                    .with(ValueSelectConfigLogDto::setType, log.getType())
                    .with(ValueSelectConfigLogDto::setBeforeType, beforeType)
                    .with(ValueSelectConfigLogDto::setCreatUser, log.getCreateUser())
                    .with(ValueSelectConfigLogDto::setDetails, list).build();
            resultList.add(dto);
        }
        return resultList;
    }

    @Override
    public ValueSelectionTrendTableDto listElectricityData(ValueSelectElecQueryParam param) {
        ValueSelectionTrendTableDto resDto = new ValueSelectionTrendTableDto();
        // 获取电价数据
        ElectricityPriceQueryParam queryParam = new ElectricityPriceQueryParam();
        queryParam.setElectricityType("1");
        queryParam.setStartTime(param.getStartTime());
        queryParam.setEndTime(param.getEndTime());
        List<EnergyDataResponse> elecDataList = electricityDataClientService.listElectrovalenceData(queryParam);
        if (CollectionUtils.isEmpty(elecDataList)) {
            return resDto;
        }
        // 排序，按照时间正序排列
        elecDataList = elecDataList.stream().sorted(Comparator.comparing(EnergyDataResponse::getIpTime)).collect(Collectors.toList());
        for (EnergyDataResponse data : elecDataList) {
            ValueSelectionTrendTableDto.ElecData elecData = new ValueSelectionTrendTableDto.ElecData();
            elecData.setDataTime(LocalDateTime.parse(data.getIpTime(), DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
            elecData.setDataValue(data.getIpValue());
            resDto.getElecDataList().add(elecData);
        }

        if (!BooleanUtil.isFalse(param.getEnableSample())) {
            // 获取营业时间和样本数据列表
            List<ValueSelectionTrendTableDto.ElecData> sampleDataList = getSampleDataList(param, resDto, resDto.getElecDataList());
            // 计算样本数据
            if (CollectionUtils.isNotEmpty(sampleDataList)) {
                List<BigDecimal> valueList = sampleDataList.stream().filter(x -> x != null && x.getDataValue() != null)
                        .map(ValueSelectionTrendTableDto.ElecData::getDataValue).collect(Collectors.toList());
                getSampleCalculateResult(valueList, resDto);
            }
        }
        return resDto;
    }

    @Override
    public ValueSelectionTrendTableDto listCarbonData(ValueSelectElecQueryParam param) {
        ValueSelectionTrendTableDto resDto = new ValueSelectionTrendTableDto();
        // 获取项目的城市编码
        ProjectDTO projectDTO = midPermissionClientService.getProject(false, true, param.getProjectId());
        // 查询碳因子数据
        CarbonCategoryDataParam queryParam = GenericBuilder.of(CarbonCategoryDataParam::new)
                .with(CarbonCategoryDataParam::setProjectId, param.getProjectId())
                .with(CarbonCategoryDataParam::setCityCode, projectDTO.getCityCode())
                .with(CarbonCategoryDataParam::setGroupCode, ContextUtil.getGroupCode())
                .with(CarbonCategoryDataParam::setCategoryCode, "CREL")
                .with(CarbonCategoryDataParam::setBeginTime, param.getStartTime())
                .with(CarbonCategoryDataParam::setEndTime, param.getEndTime()).build();
        List<CarbonCategoryDataResponse.CarBonData> carbonDataList = electricityDataClientService.listCarbonCategoryData(queryParam);
        if (CollectionUtils.isEmpty(carbonDataList)) {
            return resDto;
        }
        List<ValueSelectionTrendTableDto.ElecData> elecDataList = new ArrayList<>();
        resDto.setElecDataList(elecDataList);
        for (CarbonCategoryDataResponse.CarBonData carBonData : carbonDataList) {
            BigDecimal value = CollectionUtils.isEmpty(carBonData.getDataList()) ? null : carBonData.getDataList().get(0).getValue();
            elecDataList.add(GenericBuilder.of(ValueSelectionTrendTableDto.ElecData::new)
                    .with(ValueSelectionTrendTableDto.ElecData::setDataTime, carBonData.getDate())
                    .with(ValueSelectionTrendTableDto.ElecData::setDataValue, value)
                    .build());
        }
        if (!BooleanUtil.isFalse(param.getEnableSample())) {
            // 获取营业时间和样本数据列表
            List<ValueSelectionTrendTableDto.ElecData> sampleDataList = getSampleDataList(param, resDto, elecDataList);
            // 计算样本数据
            if (CollectionUtils.isNotEmpty(sampleDataList)) {
                List<BigDecimal> valueList = sampleDataList.stream().filter(x -> x != null && x.getDataValue() != null)
                        .map(ValueSelectionTrendTableDto.ElecData::getDataValue).collect(Collectors.toList());
                getSampleCalculateResult(valueList, resDto);
            }
        }
        return resDto;
    }

    /**
     * 筛选样本数据
     *
     * @param param
     * @param resDto
     * @param elecDataList
     */
    private List<ValueSelectionTrendTableDto.ElecData> getSampleDataList(ValueSelectElecQueryParam param, ValueSelectionTrendTableDto resDto, List<ValueSelectionTrendTableDto.ElecData> elecDataList) {
        if (param.getSampleStartTime() == null || param.getSampleEndTime() == null) {
            // 查询营业时间，项目工作历
            MidCalendarGlobalRuleDTO midCalendarGlobalRuleDTO = midCalendarClientService.getOrInitObjectGlobalCalendarRule(
                    param.getProjectId(), DemandInstanceTypeEnum.PROJECT, param.getProjectId(), null, null);
            if (midCalendarGlobalRuleDTO != null && CollectionUtils.isNotEmpty(midCalendarGlobalRuleDTO.getGlobalBusinessTimeRangeListOnWeekDays())) {
                LinkedHashSet<Range<LocalTime>> businessTime = midCalendarGlobalRuleDTO.getGlobalBusinessTimeRangeListOnWeekDays();
                for (Range<LocalTime> timeRange : businessTime) {
                    if (resDto.getOpenTime() == null || resDto.getOpenTime().isAfter(timeRange.getMinimum())) {
                        resDto.setOpenTime(timeRange.getMinimum());
                    }
                    if (resDto.getCloseTime() == null || resDto.getCloseTime().isBefore(timeRange.getMaximum())) {
                        resDto.setCloseTime(timeRange.getMaximum());
                    }
                }
            }
        }
        // 筛选样本数据
        List<ValueSelectionTrendTableDto.ElecData> sampleDataList = elecDataList;
        if (param.getSampleStartTime() != null && param.getSampleEndTime() != null) {
            sampleDataList = elecDataList.stream().filter(data ->
                            !(data.getDataTime().toLocalTime().isBefore(param.getSampleStartTime()) || data.getDataTime().toLocalTime().isAfter(param.getSampleEndTime())))
                    .collect(Collectors.toList());
        }
        return sampleDataList;
    }

    /**
     * 计算数据范围
     *
     * @param valueList
     * @param resDto
     */
    private void getSampleCalculateResult(List<BigDecimal> valueList, ValueSelectionTrendTableDto resDto) {
        if (CollectionUtils.isEmpty(valueList) || resDto == null) {
            return;
        }
        resDto.setLowerQuartile(bizComputeService.calculateLowerQuartile(valueList));
        resDto.setMedian(bizComputeService.calculateMedian(valueList));
        resDto.setUpperQuartile(bizComputeService.calculateUpperQuartile(valueList));
        resDto.setAverageValue(bizComputeService.calculateAverage(valueList, 2));
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
