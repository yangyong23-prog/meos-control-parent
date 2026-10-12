package com.persagy.apccontrol.service.impl;


import cn.hutool.core.builder.GenericBuilder;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.ElectricityPriceQueryParam;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarScheduleTagTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.runningstandardboard.RunningStandardBoardQueryObjectParam;
import com.persagy.apccontrol.basic.model.param.runningstandardboard.RunningStandardBoardQueryParam;
import com.persagy.apccontrol.basic.model.param.runningstandardboard.RwdObjectQueryParam;
import com.persagy.apccontrol.basic.model.param.runningstandardboard.SpecialBusinessTimeQuoteCountQueryParam;
import com.persagy.apccontrol.basic.model.param.rwd.BuildingQueryParam;
import com.persagy.apccontrol.basic.model.param.rwd.FloorQueryParam;
import com.persagy.apccontrol.basic.model.param.rwd.SpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigVO;
import com.persagy.apccontrol.basic.model.vo.runningstandardboard.*;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.param.ElectricityData.CarbonCategoryDataParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.CalendarObjectNumQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarDayDateTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarOpenTimeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.ElectricityData.CarbonCategoryDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.CalendarObjectNumResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarDayDateTagResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarOpenTimeProjectResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarOpenTimeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.ElectricityDataClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.service.demand.DemandQueryService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInheritModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.ParticipateInSchedulingEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.*;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.service.RunningStandardService;
import com.persagy.framework.common.context.ContextUtil;
import groovy.util.logging.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RunningStandardServiceImpl<T extends BaseDemandConfig> implements RunningStandardService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RunningStandardServiceImpl.class);

    @Autowired
    private MidCalendarClientService midCalendarClientService;

    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Autowired
    private MidPermissionClientService midPermissionClientService;

    @Autowired
    private ElectricityDataClientService electricityDataClientService;

    @Autowired
    private DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;

    @Autowired
    private DemandQueryService demandQueryService;

    @Autowired
    private DemandConfigGateway<T> demandConfigGateway;

    @Autowired
    private InstanceService instanceService;


    /**
     * 查询-某日营业时间的配置情况
     *
     * @param param
     * @return
     */
    @Override
    public BusinessTimeObjectVo previewProjectBusinessTime(RunningStandardBoardQueryParam param) {

        LocalDate localDate = DateUtils.str2LocalDate(param.getWorkDate(), DateUtils.SDF_DAY);
        List<MidCalendarOpenTimeProjectResponse> openTimeProject = midCalendarClientService.getOpenTimeProject(localDate);
        if (CollectionUtils.isEmpty(openTimeProject)) {
            openTimeProject = new ArrayList<>();
        }
        BusinessTimeObjectVo businessTimeObjectVo = new BusinessTimeObjectVo();
        businessTimeObjectVo.setCustomConfigCount(openTimeProject.size());
        if (param.getPage() != null && param.getPageSize() != null) {
            //分页
            openTimeProject = openTimeProject.stream().skip((param.getPage() - 1) * param.getPageSize()).limit(param.getPageSize()).collect(Collectors.toList());
        }
        //查询营业时间
        MidCalendarOpenTimeQueryParam midCalendarOpenTimeQueryParam = new MidCalendarOpenTimeQueryParam();
        String transferDateFormat = DateUtils.transferDateFormat(param.getWorkDate(), DateUtils.SDF_DAY, DateUtils.SDFDAY);
        midCalendarOpenTimeQueryParam.setWorkDateStart(transferDateFormat);
        midCalendarOpenTimeQueryParam.setWorkDateEnd(transferDateFormat);

        midCalendarOpenTimeQueryParam.setDictType(MidCalendarScheduleTagTypeEnum.OPENING);
        midCalendarOpenTimeQueryParam.setObjId(param.getProjectId());
        //项目营业时间
        MidCalendarOpenTimeResponse midCalendarOpenTime = midCalendarClientService.getMidCalendarOpenTime(param.getProjectId(), midCalendarOpenTimeQueryParam);
        if (midCalendarOpenTime != null && CollectionUtils.isNotEmpty(midCalendarOpenTime.getTimeTable())) {
            List<MidCalendarOpenTimeResponse.TimeTable> timeTable = midCalendarOpenTime.getTimeTable();
            List<String> businessTimeRange = new ArrayList<>();
            for (MidCalendarOpenTimeResponse.TimeTable table : timeTable) {
                businessTimeRange.add(table.getWorkTime());
            }
            businessTimeObjectVo.setBusinessTimeRange(businessTimeRange);
        }
        //工作历日期标签
        MidCalendarDayDateTagQueryParam midCalendarDayDateTagQueryParam = new MidCalendarDayDateTagQueryParam();
        midCalendarDayDateTagQueryParam.setObjId(param.getProjectId());
        midCalendarDayDateTagQueryParam.setBeginDate(localDate);
        midCalendarDayDateTagQueryParam.setEndDate(localDate);
        List<MidCalendarDayDateTagResponse> midCalendarDayDateTagResponses = midCalendarClientService.listCalendarDayDateTag(param.getProjectId(), midCalendarDayDateTagQueryParam);
        if (CollectionUtils.isNotEmpty(midCalendarDayDateTagResponses)) {
            List<String> calendarTags = new ArrayList<>();
            for (MidCalendarDayDateTagResponse midCalendarDayDateTagRespons : midCalendarDayDateTagResponses) {
                calendarTags.addAll(midCalendarDayDateTagRespons.getTagList());
            }
            businessTimeObjectVo.setCalendarTags(calendarTags);
        }
        List<String> objectIds = new ArrayList<>();
        // objectId==>MidCalendarOpenTimeProject
        Map<String, MidCalendarOpenTimeProjectResponse> midCalendarOpenTimeProjectMap = new HashMap<>();
        for (MidCalendarOpenTimeProjectResponse midCalendarOpenTimeProject : openTimeProject) {
            objectIds.add(midCalendarOpenTimeProject.getObjectId());
            midCalendarOpenTimeProjectMap.put(midCalendarOpenTimeProject.getObjectId(), midCalendarOpenTimeProject);
        }
        if (CollectionUtils.isNotEmpty(objectIds)) {
            //建筑  楼层  空间
            List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setId(objectIds));

            Set<String> floorIdAndBuildingId = new HashSet<>();
            List<BusinessTimeCustomConfigVo> customConfigList = new ArrayList<>();
            for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
                BusinessTimeCustomConfigVo businessTimeCustomConfigVo = new BusinessTimeCustomConfigVo();
                businessTimeCustomConfigVo.setId(dtpRwdObjectDTO.getId());
                businessTimeCustomConfigVo.setLocalId(dtpRwdObjectDTO.getLocalId());
                businessTimeCustomConfigVo.setLocalName(dtpRwdObjectDTO.getLocalName());
                businessTimeCustomConfigVo.setClassCode(dtpRwdObjectDTO.getClassCode());
                String floorId = (String) dtpRwdObjectDTO.getUndefinedProperties().get("floorId");
                String buildingId = (String) dtpRwdObjectDTO.getUndefinedProperties().get("buildingId");
                floorIdAndBuildingId.add(floorId);
                floorIdAndBuildingId.add(buildingId);
                businessTimeCustomConfigVo.setFloorId(floorId);
                businessTimeCustomConfigVo.setBuildingId(buildingId);
                businessTimeCustomConfigVo.setObjectType(dtpRwdObjectDTO.getObjType());
                MidCalendarOpenTimeProjectResponse midCalendarOpenTimeProject = midCalendarOpenTimeProjectMap.get(dtpRwdObjectDTO.getId());
                businessTimeCustomConfigVo.setBusinessTimeRange(Lists.newArrayList(Arrays.stream(midCalendarOpenTimeProject.getWorkTime().split(",")).iterator()));
                customConfigList.add(businessTimeCustomConfigVo);
            }
            //建筑  楼层
            Map<String, String> floorIdAndBuildingIdMap = dtpRwdClientService.mapObjectIdAndLocalName(false, floorIdAndBuildingId);
            for (BusinessTimeCustomConfigVo businessTimeCustomConfigVo : customConfigList) {
                if (businessTimeCustomConfigVo.getFloorId() != null) {
                    businessTimeCustomConfigVo.setFloorLocalName(floorIdAndBuildingIdMap.get(businessTimeCustomConfigVo.getFloorId()));
                }
                if (businessTimeCustomConfigVo.getBuildingId() != null) {
                    businessTimeCustomConfigVo.setBuildingLocalName(floorIdAndBuildingIdMap.get(businessTimeCustomConfigVo.getBuildingId()));
                }
            }

            businessTimeObjectVo.setCustomConfig(customConfigList);
        }

        return businessTimeObjectVo;
    }

    @Override
    public List<SpecialBusinessTimeQuoteCountVo> specialBusinessTimeQuoteCount(SpecialBusinessTimeQuoteCountQueryParam param) {

        List<CalendarObjectNumQueryParam> calendarParam = new ArrayList<>();
        for (RwdObjectQueryParam object : param.getObjects()) {
            String objectType = object.getObjectType();
            String objectId = object.getObjectId();
            CalendarObjectNumQueryParam calendarObjectNumQueryParam = new CalendarObjectNumQueryParam();
            calendarObjectNumQueryParam.setObjType(objectType);
            calendarObjectNumQueryParam.setObjectId(objectId);
            calendarParam.add(calendarObjectNumQueryParam);
        }
        List<CalendarObjectNumResponse> calendarObjectNumResponses = midCalendarClientService.calendarObjectNum(calendarParam);
        if (CollectionUtils.isEmpty(calendarObjectNumResponses)) {
            return new ArrayList<>();
        }
        List<SpecialBusinessTimeQuoteCountVo> specialBusinessTimeQuoteCountVos = new ArrayList<>();
        for (CalendarObjectNumResponse calendarObjectNumResponse : calendarObjectNumResponses) {
            String objectId = calendarObjectNumResponse.getObjectId();
            List<String> objects = calendarObjectNumResponse.getObjects();
            SpecialBusinessTimeQuoteCountVo specialBusinessTimeQuoteCountVo = new SpecialBusinessTimeQuoteCountVo();
            specialBusinessTimeQuoteCountVo.setWorkId(calendarObjectNumResponse.getWorkId());
            specialBusinessTimeQuoteCountVo.setObjectId(objectId);
            specialBusinessTimeQuoteCountVo.setName(calendarObjectNumResponse.getName());
            specialBusinessTimeQuoteCountVo.setNum(objects.size());
            specialBusinessTimeQuoteCountVo.setObjects(objects);
            specialBusinessTimeQuoteCountVos.add(specialBusinessTimeQuoteCountVo);
        }
        return specialBusinessTimeQuoteCountVos;
    }

    @Override
    public List<AirconditioningRunningStandardObjectVo> previewProjectGlobalConfig(RunningStandardBoardQueryParam param) {
        DemandConfigQueryParam demandConfigQueryParam = new DemandConfigQueryParam();
        demandConfigQueryParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        demandConfigQueryParam.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        demandConfigQueryParam.setInstanceIds(Sets.newHashSet(param.getProjectId()));
        LocalDate localDate = DateUtils.str2LocalDate(param.getWorkDate(), DateUtils.SDF_DAY);
        demandConfigQueryParam.setDate(localDate);
        List<DemandConfigVO<BaseDemandConfig>> demandConfigVOS = demandQueryService.queryDemandConfig(demandConfigQueryParam);
        if (CollectionUtils.isEmpty(demandConfigVOS)) {
            return new ArrayList<>();
        }
        //查询工作历日期标签
        MidCalendarDayDateTagQueryParam queryParam = new MidCalendarDayDateTagQueryParam();
        queryParam.setObjId(param.getProjectId());
        queryParam.setBeginDate(localDate);
        queryParam.setEndDate(localDate);
        List<MidCalendarDayDateTagResponse> midCalendarDayDateTagResponses = midCalendarClientService.listCalendarDayDateTag(param.getProjectId(), queryParam);
        //空调季
        String seasonType = "";
        //空调工况
        String seasonCondition = "";
        if (CollectionUtils.isNotEmpty(midCalendarDayDateTagResponses)) {
            for (String tag : midCalendarDayDateTagResponses.get(0).getTagList()) {
                if (StringUtils.isEmpty(seasonCondition)) {
                    MidCalendarAcSeasonTypeEnum enumByCode = MidCalendarAcSeasonTypeEnum.getEnumByCode(tag);
                    if (enumByCode != null) {
                        seasonCondition = enumByCode.getValue();
                    }
                }
                if (StringUtils.isEmpty(seasonType)) {
                    MidCalendarAirSeasonTypeEnum analyze = MidCalendarAirSeasonTypeEnum.getEnumByCode(tag);
                    if (analyze != null) {
                        seasonType = analyze.getValue();
                    }
                }
            }
        }
        List<AirconditioningRunningStandardObjectVo> airconditioningRunningStandardObjectVoList = new ArrayList<>();
        for (DemandConfigVO<BaseDemandConfig> baseDemandConfigDemandConfigVO : demandConfigVOS) {
            AirconditioningRunningStandardObjectVo airconditioningRunningStandardObjectVo = new AirconditioningRunningStandardObjectVo();
            airconditioningRunningStandardObjectVo.setSeasonType(seasonType);
            airconditioningRunningStandardObjectVo.setSeasonCondition(seasonCondition);
            DemandConfigValueObject<BaseDemandConfig> config = baseDemandConfigDemandConfigVO.getConfig();
            if (config != null) {
                DemandConfigAirconditioningValueObject globalConfig = (DemandConfigAirconditioningValueObject) config.getGlobalConfig();
                if (globalConfig != null) {
                    if (globalConfig.getValueSelect() != null) {
                        airconditioningRunningStandardObjectVo.setValueSelectType(globalConfig.getValueSelect().getValueSelectType());
                    }
                    DemandConfigAirconditioningBaseConfigValueObject baseConfig = globalConfig.getBaseConfig();
                    if (baseConfig != null) {
                        airconditioningRunningStandardObjectVo.setIsAllowEarlyShutdown(baseConfig.getIsAllowEarlyShutdown());
                        airconditioningRunningStandardObjectVo.setIsAllowResponseRegulation(baseConfig.getIsAllowResponseRegulation());
                        airconditioningRunningStandardObjectVo.setResponseRegulationTimeIntervals(baseConfig.getResponseRegulationTimeIntervals());
                        airconditioningRunningStandardObjectVo.setNoDemand(baseConfig.getNoDemand());
                        airconditioningRunningStandardObjectVo.setIsAlgorithmCompute(baseConfig.getIsAlgorithmCompute());
                        airconditioningRunningStandardObjectVo.setAirConditioningDemandTimeIntervals(baseConfig.getAirConditioningDemandTimeIntervals());
                    }
                    DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = globalConfig.getEnvironmentQuality();
                    if (environmentQuality != null) {
                        airconditioningRunningStandardObjectVo.setTdbQuality(environmentQuality.getTdbQuality());
                        airconditioningRunningStandardObjectVo.setCO2Quality(environmentQuality.getCO2Quality());
                        airconditioningRunningStandardObjectVo.setRhQuality(environmentQuality.getRhQuality());
                        airconditioningRunningStandardObjectVo.setPressureDiffQuality(environmentQuality.getPressureDiffQuality());
                        if(Objects.nonNull(airconditioningRunningStandardObjectVo.getPressureDiffQuality())){
                            airconditioningRunningStandardObjectVo.getPressureDiffQuality().setIsPressureDiffQualityEnabled(environmentQuality.getIsPressureDiffQualityEnabled());
                        }
                    }
                    airconditioningRunningStandardObjectVo.setWorkCondition(globalConfig.getWorkCondition());
                }
            }
            // airconditioningRunningStandardObjectVo.setParticipateInScheduling(isParticipateInScheduling(localDate));
            airconditioningRunningStandardObjectVoList.add(airconditioningRunningStandardObjectVo);
        }
        return airconditioningRunningStandardObjectVoList;
    }

    /**
     * 是否参与调度
     *
     * @return
     */
    @Override
    public ParticipateInSchedulingEnum isParticipateInScheduling(LocalDate localDate) {
        BaseDemandTemplateParam baseDemandTemplateParam = new BaseDemandTemplateParam();
        baseDemandTemplateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        baseDemandTemplateParam.setDate(localDate);

        Optional<DemandTemplate> byTemplate = demandConfigGateway.getByTemplate(baseDemandTemplateParam);
        if (!byTemplate.isPresent()) {
            return ParticipateInSchedulingEnum.NON_CONFIG;
        }
        Long templateId = byTemplate.get().getId();

        DemandConfigByInstanceBatchQueryParam demandConfigByInstanceBatchQueryParam = new DemandConfigByInstanceBatchQueryParam();
        demandConfigByInstanceBatchQueryParam.setDate(localDate);
        demandConfigByInstanceBatchQueryParam.setTemplateId(templateId);
        demandConfigByInstanceBatchQueryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(DemandInstanceTypeEnum.SPACE, null));
        demandConfigByInstanceBatchQueryParam.setAutoFillEmptyInstanceIds(true);
        List<DemandConfigAggregate<T>> demandConfigAggregates = demandConfigGateway.listConfigsByInstances(demandConfigByInstanceBatchQueryParam);

        if (CollectionUtils.isEmpty(demandConfigAggregates)) {
            return ParticipateInSchedulingEnum.NON_CONFIG;
        }
        //总数
        Integer allCount = demandConfigAggregates.size();
        //参与调度数量
        Integer participateInSchedulingCount = 0;
        //不参与调度得数量
        Integer noParticipateInSchedulingCount = 0;

        for (DemandConfigAggregate<T> demandConfigAggregate : demandConfigAggregates) {
            DemandConfigValueObject<T> finalConfig = demandConfigAggregate.getFinalConfig();
            if (finalConfig == null) {
                noParticipateInSchedulingCount++;
                continue;
            }
            DemandConfigAirconditioningValueObject globalConfig = (DemandConfigAirconditioningValueObject) finalConfig.getGlobalConfig();
            if (globalConfig == null) {
                noParticipateInSchedulingCount++;
                continue;
            }
            DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = globalConfig.getEnvironmentQuality();
            if (environmentQuality == null) {
                noParticipateInSchedulingCount++;
                continue;
            }
            Boolean isParticipateInScheduling = environmentQuality.getIsParticipateInScheduling();
            if (isParticipateInScheduling) {
                participateInSchedulingCount++;
            } else {
                noParticipateInSchedulingCount++;
            }
            if (noParticipateInSchedulingCount > 0 && participateInSchedulingCount > 0) {
                return ParticipateInSchedulingEnum.PARTIAL_SCHEDULE;
            }
            if (noParticipateInSchedulingCount == 0 && participateInSchedulingCount.equals(allCount)) {
                return ParticipateInSchedulingEnum.ALL_SCHEDULE;
            }
        }
        return ParticipateInSchedulingEnum.NON_SCHEDULE;
    }


    @Override
    public ValueSelectObjectVo previewProjectValueSelect(RunningStandardBoardQueryParam runningStandardBoardQueryParam) {
        try {
            // 查询日期
            LocalDate queryDate = LocalDate.parse(runningStandardBoardQueryParam.getWorkDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
            templateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
            templateParam.setDate(queryDate);
            DemandConfigAirconditioningCustomDTO demandConfigAirconditioningCustomDTO = demandConfigAirconditioningCustomService.getCustomConfig(templateParam);
            if (null == demandConfigAirconditioningCustomDTO) {
                logger.info("项目:{}日期:{}未进行价值选择的配置", runningStandardBoardQueryParam.getProjectId(), runningStandardBoardQueryParam.getWorkDate());
                return new ValueSelectObjectVo();
            }
            ValueSelectObjectVo valueSelectObjectVo = new ValueSelectObjectVo();
            // 价值选择类型
            BasicsConfigTypeEnum basicsConfigTypeEnum = demandConfigAirconditioningCustomDTO.getValueSelectType();
            valueSelectObjectVo.setValueSelectType(basicsConfigTypeEnum.getCode());
            if (BasicsConfigTypeEnum.VALUE_SELECT_QUALITY_FIRST.getCode().equals(basicsConfigTypeEnum.getCode())) {
                // 品质最优时满足率默认为百分百
                List<ValueSelectConfigDto> valueSelectConfigDtos = new ArrayList<>();
                ValueSelectConfigDto valueSelectConfigDto = new ValueSelectConfigDto();
                valueSelectConfigDto.setTempFillRate(BigDecimal.valueOf(100));
                valueSelectConfigDto.setCo2FillRate(BigDecimal.valueOf(100));
                valueSelectConfigDtos.add(valueSelectConfigDto);
                valueSelectObjectVo.setValueSelectRange(valueSelectConfigDtos);
            } else {
                valueSelectObjectVo.setValueSelectRange(demandConfigAirconditioningCustomDTO.parseValueSelectRange());
            }
            // 查询日期开始时间
            LocalDateTime beginDateTime = queryDate.atStartOfDay();
            // 查询日期结束时间
            LocalDateTime endDateTime = beginDateTime.plusDays(1);
            // 电价或碳因子数据列表
            List<ValueSelectionTrendTableDto.ElecData> iotDataList = new ArrayList<>();
            // 获取项目的城市编码
            ProjectDTO projectDTO = midPermissionClientService.getProject(false, true, runningStandardBoardQueryParam.getProjectId());
            if (BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CARBON_EMISSIONS.getCode().equals(basicsConfigTypeEnum.getCode())) {
                // 查询碳因子数据
                CarbonCategoryDataParam queryParam = GenericBuilder.of(CarbonCategoryDataParam::new)
                        .with(CarbonCategoryDataParam::setProjectId, runningStandardBoardQueryParam.getProjectId())
                        .with(CarbonCategoryDataParam::setCityCode, projectDTO.getCityCode())
                        .with(CarbonCategoryDataParam::setGroupCode, ContextUtil.getGroupCode())
                        .with(CarbonCategoryDataParam::setCategoryCode, "CREL")
                        .with(CarbonCategoryDataParam::setBeginTime, beginDateTime)
                        .with(CarbonCategoryDataParam::setEndTime, endDateTime).build();
                List<CarbonCategoryDataResponse.CarBonData> carbonDataList = electricityDataClientService.listCarbonCategoryData(queryParam);
                if (CollectionUtils.isNotEmpty(carbonDataList)) {
                    for (CarbonCategoryDataResponse.CarBonData carBonData : carbonDataList) {
                        BigDecimal dataValue = CollectionUtils.isEmpty(carBonData.getDataList()) ? null : carBonData.getDataList().get(0).getValue();
                        iotDataList.add(GenericBuilder.of(ValueSelectionTrendTableDto.ElecData::new)
                                .with(ValueSelectionTrendTableDto.ElecData::setDataTime, carBonData.getDate())
                                .with(ValueSelectionTrendTableDto.ElecData::setDataValue, dataValue)
                                .build());
                    }
                }
            } else if (BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CAST.getCode().equals(basicsConfigTypeEnum.getCode())) {
                // 获取电价
                ElectricityPriceQueryParam queryParam = new ElectricityPriceQueryParam();
                queryParam.setElectricityType("1");
                queryParam.setStartTime(beginDateTime);
                queryParam.setEndTime(endDateTime);
                List<EnergyDataResponse> elecDataList = electricityDataClientService.listElectrovalenceData(queryParam);
                if (CollectionUtils.isNotEmpty(elecDataList)) {
                    for (EnergyDataResponse data : elecDataList) {
                        ValueSelectionTrendTableDto.ElecData elecData = new ValueSelectionTrendTableDto.ElecData();
                        elecData.setDataTime(LocalDateTime.parse(data.getIpTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                        elecData.setDataValue(data.getIpValue());
                        iotDataList.add(elecData);
                    }
                }
            }
            valueSelectObjectVo.setElecDataList(iotDataList);
            return valueSelectObjectVo;
        } catch (Exception e) {
            logger.error("项目:{}日期:{}价值选择配置报错:", runningStandardBoardQueryParam.getProjectId(), runningStandardBoardQueryParam.getWorkDate(), e);
        }
        return null;
    }

    @Override
    public SpecialDemandConfigObjectVo previewSpecialDemandConfig(RunningStandardBoardQueryParam runningStandardBoardQueryParam) {
        try {
            // 查询日期
            LocalDate queryDate = LocalDate.parse(runningStandardBoardQueryParam.getWorkDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BaseDemandTemplateParam baseDemandTemplateParam = new BaseDemandTemplateParam();
            baseDemandTemplateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
            baseDemandTemplateParam.setDate(queryDate);
            //当前项目当前日期下空调运行标准配置模版信息
            DemandTemplate demandTemplate = baseDemandTemplateParam.queryAndGetTemplate();
            if (Objects.isNull(demandTemplate)) {
                logger.info("项目:{}日期:{}未进行空调运行标准的配置", runningStandardBoardQueryParam.getProjectId(), runningStandardBoardQueryParam.getWorkDate());
                return new SpecialDemandConfigObjectVo();
            }
            // 模版id
            Long templateId = demandTemplate.getId();
            Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds = new HashMap<>();
            instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.PROJECT, null);
            instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.BUILDING, null);
            instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.FLOOR, null);
            instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.SPACE, null);
            DemandConfigByInstanceBatchQueryParam queryParam = new DemandConfigByInstanceBatchQueryParam();
            queryParam.setTemplateId(templateId);
            queryParam.setDate(queryDate);
            queryParam.setInstanceTypeAndInstanceIds(instanceTypeAndInstanceIds);
            queryParam.setAutoFillEmptyInstanceIds(true);
            // 获取当前模版下建筑、楼层、空间配置信息
            List<DemandConfigAggregate<T>> allConfigs = demandConfigGateway.listConfigsByInstances(queryParam);
            if (CollectionUtils.isEmpty(allConfigs)) {
                logger.info("项目:{}日期:{}未有配置信息", runningStandardBoardQueryParam.getProjectId(), runningStandardBoardQueryParam.getWorkDate());
                return new SpecialDemandConfigObjectVo();
            }
            SpaceQueryParam spaceQueryParam = new SpaceQueryParam();
            List<DtpRwdSpaceDTO> spaceDTOS = instanceService.listSpaces(spaceQueryParam);
            if (CollectionUtil.isEmpty(spaceDTOS)) {
                logger.info("项目:{}下未有空间", runningStandardBoardQueryParam.getProjectId());
                return new SpecialDemandConfigObjectVo();
            }
            //{空间id:空间对象}
            Map<String, DtpRwdSpaceDTO> objId_spaceDto = spaceDTOS.stream().collect(Collectors.toMap(DtpRwdSpaceDTO::getId, Function.identity(), (v1, v2) -> v1));
            //特殊空间需求总数(引用建筑楼层空间类型的空间数和空间自定义过配置数之和)
            Integer specialTotal = 0;
            // 继承的全局配置:{引用类型:{对象引用实例id:对象id列表}}
            Map<String, Map<String, List<String>>> type_instanceId_objectInstanceIdList = new HashMap<>();
            // 继承的空间功能类型配置:{对象引用实例id:{空间功能类型编码:对象id列表}}
            Map<String, Map<String, List<String>>> instanceId_roomFuncType_objectInstanceIdList = new HashMap<>();
            // 空间模块
            List<SpecialDemandConfigObjectDetailVo> spaceModule = new ArrayList<>();
            // 建筑配置列表
            List<DemandConfigAggregate<T>> configsOfBuilding = new ArrayList<>();
            // 楼层配置列表
            List<DemandConfigAggregate<T>> configsOfFloor = new ArrayList<>();
            // 楼层或建筑的空间功能配置{实例id:配置信息}
            Map<String, DemandConfigAggregate<T>> instanceId_configMap = new HashMap<>();
            for (DemandConfigAggregate<T> demandConfig : allConfigs) {
                if (Objects.isNull(demandConfig.getFinalConfig())) {
                    continue;
                }
                DemandInstanceValueObject demandInstanceValue = demandConfig.getIdentifier().getInstance();
                if (null == demandInstanceValue) {
                    continue;
                }
                // 项目时要做空间类型配置的展示
                if (demandInstanceValue.getInstanceType().equals(DemandInstanceTypeEnum.PROJECT)) {
                    if ((demandConfig.getConfig() != null && CollectionUtils.isNotEmpty(demandConfig.getConfig().getSpaceFuncTypeConfigs()))) {
                        // 楼层自身配置中有空间类型配置才算有
                        instanceId_configMap.put(demandInstanceValue.getInstanceId(), demandConfig);
                    }
                    continue;
                }
                // 处理楼层或建筑配置
                if (demandInstanceValue.getInstanceType().equals(DemandInstanceTypeEnum.BUILDING) || demandInstanceValue.getInstanceType().equals(DemandInstanceTypeEnum.FLOOR)) {
                    // 楼层或建筑需要过滤不是自定义的配置
                    if (!demandConfig.isCustomizedConfig()) {
                        continue;
                    }
                    // 楼层或建筑模块只统计自定义的全局配置
                    if (!Objects.isNull(demandConfig.getConfig().getGlobalConfig()) && demandConfig.getConfig().getGlobalConfig() instanceof DemandConfigAirconditioningValueObject) {
                        if (demandInstanceValue.getInstanceType().equals(DemandInstanceTypeEnum.BUILDING)) {
                            configsOfBuilding.add(demandConfig);
                        }
                        if (demandInstanceValue.getInstanceType().equals(DemandInstanceTypeEnum.FLOOR)) {
                            configsOfFloor.add(demandConfig);
                        }
                    }
                    if ((demandConfig.getConfig() != null && CollectionUtils.isNotEmpty(demandConfig.getConfig().getSpaceFuncTypeConfigs()))) {
                        // 建筑或楼层的自身配置中有空间类型配置才算有
                        instanceId_configMap.put(demandInstanceValue.getInstanceId(), demandConfig);
                    }
                    continue;
                }
                // 处理空间配置
                DemandConfigValueObject<T> configValueObject = demandConfig.getFinalConfig();
                // 自定义过的要在页面空间模块下做展示,引用的要在建筑楼层空间类型下做统计展示
                if (demandConfig.isCustomizedConfig()) {
                    DemandConfigAirconditioningValueObject globalConfig = (DemandConfigAirconditioningValueObject) configValueObject.getGlobalConfig();
                    SpecialDemandConfigObjectDetailVo specialDemandConfigObjectDetailVo = generateCommonFieldVo(demandConfig.getIdentifier().getInstance(), globalConfig);
                    spaceModule.add(specialDemandConfigObjectDetailVo);
                    specialTotal++;
                    continue;
                }
                LinkedHashMap<DemandInstanceValueObject, Set<DemandInheritModeEnum>> inheritInstanceAndModes = demandConfig.getInheritInstanceAndModes();
                if (null == inheritInstanceAndModes || inheritInstanceAndModes.size() == 0) {
                    continue;
                }
                // 空间继承的实例对象
                DemandInstanceValueObject demandInstanceValueObject = inheritInstanceAndModes.entrySet().iterator().next().getKey();
                if (Objects.isNull(demandInstanceValueObject)) {
                    continue;
                }
                //空间继承实例对象类型
                Set<DemandInheritModeEnum> demandInheritModeEnums = inheritInstanceAndModes.entrySet().iterator().next().getValue();
                if (CollectionUtils.isEmpty(demandInheritModeEnums)) {
                    continue;
                }
                // 继承项目全局配置时不做统计
                if (demandInstanceValueObject.getInstanceType().equals(DemandInstanceTypeEnum.PROJECT) && demandInheritModeEnums.contains(DemandInheritModeEnum.GLOBAL)) {
                    continue;
                }
                if (demandInheritModeEnums.contains(DemandInheritModeEnum.GLOBAL)) {
                    //继承全局配置
                    Map<String, List<String>> instanceId_objectInstanceIdList = type_instanceId_objectInstanceIdList.get(demandInstanceValueObject.getInstanceType().getValue());
                    if (null == instanceId_objectInstanceIdList) {
                        instanceId_objectInstanceIdList = new HashMap<>();
                    }
                    List<String> objectInstanceIds = instanceId_objectInstanceIdList.get(demandInstanceValueObject.getInstanceId());
                    if (CollectionUtils.isEmpty(objectInstanceIds)) {
                        objectInstanceIds = new ArrayList<>();
                    }
                    objectInstanceIds.add(demandConfig.getIdentifier().getInstance().getInstanceId());
                    instanceId_objectInstanceIdList.put(demandInstanceValueObject.getInstanceId(), objectInstanceIds);
                    type_instanceId_objectInstanceIdList.put(demandInstanceValueObject.getInstanceType().getValue(), instanceId_objectInstanceIdList);
                } else if (demandInheritModeEnums.contains(DemandInheritModeEnum.ROOM_FUN_TYPE)) {
                    // 继承空间类型配置
                    DtpRwdSpaceDTO spaceDTO = objId_spaceDto.get(demandConfig.getIdentifier().getInstance().getInstanceId());
                    if (null == spaceDTO) {
                        continue;
                    }
                    Map<String, List<String>> roomFuncType_objectInstanceIdList = instanceId_roomFuncType_objectInstanceIdList.get(demandInstanceValueObject.getInstanceId());
                    if (null == roomFuncType_objectInstanceIdList) {
                        roomFuncType_objectInstanceIdList = new HashMap<>();
                    }
                    List<String> objectInstanceIds = roomFuncType_objectInstanceIdList.get(spaceDTO.getRoomFuncType());
                    if (CollectionUtils.isEmpty(objectInstanceIds)) {
                        objectInstanceIds = new ArrayList<>();
                    }
                    objectInstanceIds.add(demandConfig.getIdentifier().getInstance().getInstanceId());
                    roomFuncType_objectInstanceIdList.put(spaceDTO.getRoomFuncType(), objectInstanceIds);
                    instanceId_roomFuncType_objectInstanceIdList.put(demandInstanceValueObject.getInstanceId(), roomFuncType_objectInstanceIdList);
                }
                // 特殊空间总数
                specialTotal++;
            }
            // 建筑模块
            List<SpecialDemandConfigObjectDetailVo> buildingModule = new ArrayList<>();
            Map<String, List<String>> building_instanceId_objectInstanceIds = type_instanceId_objectInstanceIdList.get(DemandInstanceTypeEnum.BUILDING.getValue());
            generateBuildingOrFloorModule(configsOfBuilding, building_instanceId_objectInstanceIds, buildingModule, null);
            // 楼层模块
            List<SpecialDemandConfigObjectDetailVo> floorModule = new ArrayList<>();
            Map<String, List<String>> floor_instanceId_objectInstanceIds = type_instanceId_objectInstanceIdList.get(DemandInstanceTypeEnum.FLOOR.getValue());
            generateBuildingOrFloorModule(configsOfFloor, floor_instanceId_objectInstanceIds, null, floorModule);
            // 空间类型模块
            List<SpecialDemandConfigObjectDetailVo> functionModule = new ArrayList<>();
            Set<String> functionFloorInstanceIds = new HashSet<>();
            generateFunctionModule(instanceId_configMap, instanceId_roomFuncType_objectInstanceIdList, functionModule, functionFloorInstanceIds);
            Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds = new HashMap<>();
            Set<String> floorInstanceIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(floorModule)) {
                floorInstanceIds.addAll(floorModule.stream().map(SpecialDemandConfigObjectDetailVo::getInstanceId).collect(Collectors.toSet()));
            }
            floorInstanceIds.addAll(functionFloorInstanceIds);
            if (CollectionUtils.isNotEmpty(floorInstanceIds)) {
                instanceTypeAndIds.put(DemandInstanceTypeEnum.FLOOR, floorInstanceIds);
            }
            if (CollectionUtils.isNotEmpty(spaceModule)) {
                Set<String> spaceInstanceIds = spaceModule.stream().map(SpecialDemandConfigObjectDetailVo::getInstanceId).collect(Collectors.toSet());
                instanceTypeAndIds.put(DemandInstanceTypeEnum.SPACE, spaceInstanceIds);
            }
            Map<String, List<DemandInstanceValueObject>> instanceIdAndParentInfoMap = instanceService.mapInstanceIdAndAllParents(instanceTypeAndIds);
            SpecialDemandConfigObjectVo specialDemandConfigObjectVo = new SpecialDemandConfigObjectVo();
            specialDemandConfigObjectVo.setBuildModule(buildingModule);
            specialDemandConfigObjectVo.setFloorModule(getFinalSpecialDemandConfigs(floorModule, instanceIdAndParentInfoMap));
            specialDemandConfigObjectVo.setFunctionModule(getFinalSpecialDemandConfigs(functionModule, instanceIdAndParentInfoMap));
            specialDemandConfigObjectVo.setSpaceModule(getFinalSpecialDemandConfigs(spaceModule, instanceIdAndParentInfoMap));
            specialDemandConfigObjectVo.setSpecialTotal(specialTotal);
            return specialDemandConfigObjectVo;
        } catch (Exception e) {
            logger.info("项目:{}日期:{}查询特殊空间配置报错:{}", runningStandardBoardQueryParam.getProjectId(), runningStandardBoardQueryParam.getWorkDate(), e);
        }
        return null;
    }

    private List<SpecialDemandConfigObjectDetailVo> getFinalSpecialDemandConfigs(List<SpecialDemandConfigObjectDetailVo> modules, Map<String, List<DemandInstanceValueObject>> instanceIdAndParentInfoMap) {
        if (CollectionUtils.isEmpty(modules)) {
            return new ArrayList<>();
        }
        if (null == instanceIdAndParentInfoMap || instanceIdAndParentInfoMap.size() == 0) {
            return modules;
        }
        List<SpecialDemandConfigObjectDetailVo> resultList = new ArrayList<>();
        for (SpecialDemandConfigObjectDetailVo vo : modules) {
            List<DemandInstanceValueObject> parentInfos = instanceIdAndParentInfoMap.get(vo.getInstanceId());
            if (CollectionUtils.isEmpty(parentInfos)) {
                resultList.add(vo);
                continue;
            }
            for (DemandInstanceValueObject valueObject : parentInfos) {
                if (DemandInstanceTypeEnum.PROJECT.equals(valueObject.getInstanceType())) {
                    continue;
                }
                if (DemandInstanceTypeEnum.BUILDING.equals(valueObject.getInstanceType())) {
                    vo.setBuildingId(valueObject.getInstanceId());
                    vo.setBuildingName(valueObject.getInstanceName());
                }
                if (DemandInstanceTypeEnum.FLOOR.equals(valueObject.getInstanceType())) {
                    vo.setFloorId(valueObject.getInstanceId());
                    vo.setFloorName(valueObject.getInstanceName());
                }
            }
            resultList.add(vo);
        }
        return resultList;
    }

    @Override
    public List<ReferenceSpaceDetailVo> previewReferenceSpaceDetail(RunningStandardBoardQueryObjectParam param) {
        if (CollectionUtils.isEmpty(param.getObjIds())) {
            return null;
        }
        BuildingQueryParam buildingQueryParam = new BuildingQueryParam();
        //当前项目下建筑信息
        List<DtpRwdBuildingDTO> buildings = instanceService.listBuildings(buildingQueryParam);
        //{建筑id:建筑名称}
        Map<String, String> buildingIdBuildingNameMap = CollectionUtils.isEmpty(buildings)
                ? new HashMap<>()
                : buildings.stream()
                .collect(HashMap::new,
                        (map, item) -> map.put(item.getId(), item.getLocalName()),
                        HashMap::putAll
                );
        FloorQueryParam floorQueryParam = new FloorQueryParam();
        //当前项目下楼层信息
        List<DtpRwdFloorDTO> floors = instanceService.listFloors(floorQueryParam);
        //{楼层id:楼层名称}
        Map<String, String> floorIdFloorNameMap = CollectionUtils.isEmpty(floors)
                ? new HashMap<>()
                : floors.stream()
                .collect(HashMap::new,
                        (map, item) -> map.put(item.getId(), item.getLocalName()),
                        HashMap::putAll
                );
        SpaceQueryParam spaceQueryParam = new SpaceQueryParam();
        List<DtpRwdSpaceDTO> spaceDTOS = instanceService.listSpaces(spaceQueryParam);
        if (CollectionUtil.isEmpty(spaceDTOS)) {
            return null;
        }
        //{空间id:空间对象}
        Map<String, DtpRwdSpaceDTO> objId_spaceDto = spaceDTOS.stream().collect(Collectors.toMap(DtpRwdSpaceDTO::getId, Function.identity(), (v1, v2) -> v1));
        return generateReferenceSpaceList(param.getObjIds(), buildingIdBuildingNameMap, floorIdFloorNameMap, objId_spaceDto);
    }

    /**
     * 组装页面建筑或楼层模块信息
     *
     * @param configs                      建筑或楼层运行标准配置列表
     * @param instanceId_objectInstanceIds {对象引用实例id:对象实例id列表}
     * @param buildingModule               建筑模块
     * @param floorModule                  楼层模块
     */
    private void generateBuildingOrFloorModule(List<DemandConfigAggregate<T>> configs, Map<String, List<String>> instanceId_objectInstanceIds, List<SpecialDemandConfigObjectDetailVo> buildingModule,
                                               List<SpecialDemandConfigObjectDetailVo> floorModule) {
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        for (DemandConfigAggregate<T> demandConfig : configs) {
            DemandConfigValueObject<T> configValueObject = demandConfig.getFinalConfig();
            DemandConfigAirconditioningValueObject globalConfig = (DemandConfigAirconditioningValueObject) configValueObject.getGlobalConfig();
            SpecialDemandConfigObjectDetailVo specialDemandConfigObjectDetailVo = generateCommonFieldVo(demandConfig.getIdentifier().getInstance(), globalConfig);
            if (null != instanceId_objectInstanceIds && instanceId_objectInstanceIds.size() != 0 && instanceId_objectInstanceIds.keySet().contains(demandConfig.getIdentifier().getInstance().getInstanceId())) {
                ReferenceSpaceVo referenceSpace = new ReferenceSpaceVo();
                referenceSpace.setSpaceTotal(instanceId_objectInstanceIds.get(demandConfig.getIdentifier().getInstance().getInstanceId()).size());
                referenceSpace.setObjIds(instanceId_objectInstanceIds.get(demandConfig.getIdentifier().getInstance().getInstanceId()));
                specialDemandConfigObjectDetailVo.setReferenceSpace(referenceSpace);
            }
            if (null != buildingModule) {
                buildingModule.add(specialDemandConfigObjectDetailVo);
            }
            if (null != floorModule) {
                floorModule.add(specialDemandConfigObjectDetailVo);
            }
        }
    }

    /**
     * 组装页面空间模块信息
     *
     * @param instanceId_config                         建筑和楼层运行标准配置列表
     * @param instanceId_roomFuncType_objectInstanceIds {对象引用实例id:{空间功能类型编码:对象实例id列表}}
     * @param functionModule                            空间模块
     */
    private void generateFunctionModule(Map<String, DemandConfigAggregate<T>> instanceId_config, Map<String, Map<String, List<String>>> instanceId_roomFuncType_objectInstanceIds,
                                        List<SpecialDemandConfigObjectDetailVo> functionModule, Set<String> functionFloorInstanceIds) {
        if (null == instanceId_config || instanceId_config.size() == 0) {
            return;
        }
        for (Map.Entry<String, DemandConfigAggregate<T>> entry : instanceId_config.entrySet()) {
            DemandConfigValueObject<T> configValueObject = entry.getValue().getConfig();
            Map<String, List<String>> roomFuncType_objectInstanceIds = instanceId_roomFuncType_objectInstanceIds.get(entry.getKey());
            for (DemandSpaceFuncTypeConfig<T> config : configValueObject.getSpaceFuncTypeConfigs()) {
                List<String> codeList = config.getFuncTypes().stream().map(DtpKnowledgeInfoCodeDataSourceDTO::getCode).collect(Collectors.toList());
                List<String> nameList = config.getFuncTypes().stream().map(DtpKnowledgeInfoCodeDataSourceDTO::getName).collect(Collectors.toList());
                DemandConfigAirconditioningValueObject funcTypeConfig = (DemandConfigAirconditioningValueObject) config.getConfig();
                DemandInstanceValueObject demandInstanceValueObject = entry.getValue().getIdentifier().getInstance();
                SpecialDemandConfigObjectDetailVo specialDemandConfigObjectDetailVo = generateCommonFieldVo(demandInstanceValueObject, funcTypeConfig);
                specialDemandConfigObjectDetailVo.setInstanceName(nameList.stream().filter(name -> name != null).collect(Collectors.joining(" ")));
                if (null != demandInstanceValueObject) {
                    if (DemandInstanceTypeEnum.BUILDING.equals(demandInstanceValueObject.getInstanceType())) {
                        specialDemandConfigObjectDetailVo.setBuildingId(demandInstanceValueObject.getInstanceId());
                        specialDemandConfigObjectDetailVo.setBuildingName(demandInstanceValueObject.getInstanceName());
                    }
                    if (DemandInstanceTypeEnum.FLOOR.equals(demandInstanceValueObject.getInstanceType())) {
                        specialDemandConfigObjectDetailVo.setFloorId(demandInstanceValueObject.getInstanceId());
                        specialDemandConfigObjectDetailVo.setFloorName(demandInstanceValueObject.getInstanceName());
                        functionFloorInstanceIds.add(demandInstanceValueObject.getInstanceId());
                    }
                }
                if (null != roomFuncType_objectInstanceIds && !roomFuncType_objectInstanceIds.isEmpty()) {
                    List<String> roomFuncTypes = new ArrayList<>(CollectionUtils.intersection(codeList, roomFuncType_objectInstanceIds.keySet()));
                    if (!CollectionUtils.isEmpty(roomFuncTypes)) {
                        int spaceTotal = 0;
                        List<String> objIds = new ArrayList<>();
                        for (String code : codeList) {
                            List<String> instanceIds = roomFuncType_objectInstanceIds.get(code);
                            if (CollectionUtils.isNotEmpty(instanceIds)) {
                                spaceTotal = spaceTotal + instanceIds.size();
                                objIds.addAll(instanceIds);
                            }
                        }
                        ReferenceSpaceVo referenceSpace = new ReferenceSpaceVo();
                        referenceSpace.setSpaceTotal(spaceTotal);
                        referenceSpace.setObjIds(objIds);
                        specialDemandConfigObjectDetailVo.setReferenceSpace(referenceSpace);
                    }
                }
                functionModule.add(specialDemandConfigObjectDetailVo);
            }
        }
    }

    /**
     * 组装引用对象信息列表
     *
     * @param ids                     对象id列表
     * @param buildingId_buildingName {建筑id:建筑本地名称}
     * @param floorId_floorName       {楼层id:楼层本地名称}
     * @param objId_spaceDto          {空间id:空间对象信息}
     * @return
     */
    private List<ReferenceSpaceDetailVo> generateReferenceSpaceList(List<String> ids, Map<String, String> buildingId_buildingName,
                                                                    Map<String, String> floorId_floorName,
                                                                    Map<String, DtpRwdSpaceDTO> objId_spaceDto) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<ReferenceSpaceDetailVo> referenceSpaceDetailVos = new ArrayList<>();
        for (String id : ids) {
            if (Objects.isNull(objId_spaceDto.get(id))) {
                continue;
            }
            ReferenceSpaceDetailVo referenceSpaceDetailVo = new ReferenceSpaceDetailVo();
            referenceSpaceDetailVo.setBuildingId(objId_spaceDto.get(id).getBuildingId());
            referenceSpaceDetailVo.setBuildingLocalName(buildingId_buildingName.get(objId_spaceDto.get(id).getBuildingId()));
            referenceSpaceDetailVo.setFloorId(objId_spaceDto.get(id).getFloorId());
            referenceSpaceDetailVo.setFloorLocalName(floorId_floorName.get(objId_spaceDto.get(id).getFloorId()));
            referenceSpaceDetailVo.setSpaceId(id);
            referenceSpaceDetailVo.setSpaceLocalName(objId_spaceDto.get(id).getLocalName());
            referenceSpaceDetailVos.add(referenceSpaceDetailVo);
        }
        return referenceSpaceDetailVos;
    }

    /**
     * 组装特殊需求配置对象信息
     *
     * @param instanceValueObject 实例对象信息
     * @param demandConfig        需求配置信息
     * @return
     */
    private SpecialDemandConfigObjectDetailVo generateCommonFieldVo(DemandInstanceValueObject instanceValueObject, DemandConfigAirconditioningValueObject demandConfig) {
        if (null == instanceValueObject || null == demandConfig) {
            return new SpecialDemandConfigObjectDetailVo();
        }
        SpecialDemandConfigObjectDetailVo specialDemandConfigObjectDetailVo = new SpecialDemandConfigObjectDetailVo();
        specialDemandConfigObjectDetailVo.setInstanceId(instanceValueObject.getInstanceId());
        specialDemandConfigObjectDetailVo.setInstanceType(instanceValueObject.getInstanceType().getValue());
        specialDemandConfigObjectDetailVo.setInstanceName(instanceValueObject.getInstanceName());
        specialDemandConfigObjectDetailVo.setNoDemand(null == demandConfig.getBaseConfig() ? null : demandConfig.getBaseConfig().getNoDemand());
        specialDemandConfigObjectDetailVo.setIsAlgorithmCompute(null == demandConfig.getBaseConfig() ? null : demandConfig.getBaseConfig().getIsAlgorithmCompute());
        specialDemandConfigObjectDetailVo.setAirConditioningDemandTimeIntervals(null == demandConfig.getBaseConfig() ? new ArrayList<>() : demandConfig.getBaseConfig().getAirConditioningDemandTimeIntervals());
        specialDemandConfigObjectDetailVo.setResponseRegulationTimeIntervals(null == demandConfig.getBaseConfig() ? new ArrayList<>() : demandConfig.getBaseConfig().getResponseRegulationTimeIntervals());
        specialDemandConfigObjectDetailVo.setTdbQuality(null == demandConfig.getEnvironmentQuality() ? new DemandConfigAirconditioningTdbQualityValueObject() : demandConfig.getEnvironmentQuality().getTdbQuality());
        specialDemandConfigObjectDetailVo.setCO2Quality(null == demandConfig.getEnvironmentQuality() ? new DemandConfigAirconditioningCO2QualityValueObject() : demandConfig.getEnvironmentQuality().getCO2Quality());
        specialDemandConfigObjectDetailVo.setIsRhQualityEnabled(null == demandConfig.getEnvironmentQuality() ? null : demandConfig.getEnvironmentQuality().getIsRhQualityEnabled());
        specialDemandConfigObjectDetailVo.setRhQuality(null == demandConfig.getEnvironmentQuality() ? new DemandConfigAirconditioningRhQualityValueObject() : demandConfig.getEnvironmentQuality().getRhQuality());
        specialDemandConfigObjectDetailVo.setIsPressureDiffQualityEnabled(null == demandConfig.getEnvironmentQuality() ? null : demandConfig.getEnvironmentQuality().getIsPressureDiffQualityEnabled());
        specialDemandConfigObjectDetailVo.setPressureDiffQuality(null == demandConfig.getEnvironmentQuality() ? new DemandConfigAirconditioningPressureDiffQualityValueObject() : demandConfig.getEnvironmentQuality().getPressureDiffQuality());
        specialDemandConfigObjectDetailVo.setIsAllowResponseRegulation(null == demandConfig.getBaseConfig() ? null : demandConfig.getBaseConfig().getIsAllowResponseRegulation());
        return specialDemandConfigObjectDetailVo;
    }

}
