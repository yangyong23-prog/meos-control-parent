package com.persagy.apccontrol.basic.service.demand.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.mapper.demand.DemandConfigAirconditioningCustomMapper;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.airconditioning.DemandConfigAirconditioningCustomTimeGroup;
import com.persagy.apccontrol.basic.model.enumtype.EnumSpaceBusinessModel;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarScheduleTagTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandTreeInstanceQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.valueselect.DemandValueSelectConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.external.demand.DemandSpaceExternalQueryParam;
import com.persagy.apccontrol.basic.model.param.external.demand.DemandSpaceSequenceExternalQueryParam;
import com.persagy.apccontrol.basic.model.param.external.demand.DemandTreeInstanceExternalQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandTreeInstanceProjectVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandValueSelectConfigVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.DemandSpaceExternalVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.DemandSpaceSequenceExternalVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.DemandTreeInstanceProjectExternalVO;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarOpenTimeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarOpenTimeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.demand.DemandExternalService;
import com.persagy.apccontrol.basic.service.demand.DemandQueryService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.*;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.CalculateComputeUtil;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 需求管理对外服务实现
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class DemandExternalServiceImpl implements DemandExternalService {

    private final DemandConverter demandConverter;
    private final BizDtpRwdService bizDtpRwdService;
    private final DemandQueryService demandQueryService;
    private final MidCalendarClientService midCalendarClientService;
    private final DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;
    
    private final DemandConfigAirconditioningCustomMapper demandConfigAirconditioningCustomMapper;
    @Lazy
    @Autowired
    private AbstractCommonServer abstractCommonServer;

    @Override
    public R<DemandTreeInstanceProjectExternalVO> treeDemandInstance(
            @NonNull DemandTreeInstanceExternalQueryParam param) {
        param.checkAndSet();
        DemandTreeInstanceQueryParam queryParam = demandConverter.toTreeInstanceQueryParam(param);
        DemandTreeInstanceProjectVO project = demandQueryService.treeDemandInstance(queryParam).getData();
        if (Objects.isNull(project)) {
            return R.success(null);
        }
        return R.success(DemandTreeInstanceProjectExternalVO.of(project));
    }

    @Override
    public R<Collection<DemandSpaceExternalVO>> listDemandInstances(@NonNull DemandSpaceExternalQueryParam param) {
        param.checkAndSet();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        String[] withColumns = ArrayUtils.isEmpty(param.getWithColumns())
                ? new String[]{DtpRwdObjectDTO.Fields.id}
                : param.getWithColumns();

        DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(templateType);
        queryParam.queryOnlyControlled();
        queryParam.addWithColumn(withColumns);
        List<DtpRwdSpaceDTO> spaces = demandStrategy.listDemandSpace(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return R.successMulti(Collections.emptyList());
        }
        return R.successMulti(spaces.stream().map(demandConverter::toSpaceExternalVo)
                .collect(Collectors.toCollection(ArrayList::new)), (long) spaces.size());
    }

    @Override
    public TreeMap<LocalDate, List<DemandSpaceSequenceExternalVO>> listSpaceDemandConfigSequenceData(DemandSpaceSequenceExternalQueryParam param) throws Exception {
        param.checkQueryDate();
        TreeMap<LocalDate, List<DemandSpaceSequenceExternalVO>> spaceSequenceExternalTreeMap = new TreeMap<>();

        LocalDate currentDate = DateUtils.now().toLocalDate();
        LocalDate startDate = param.getStartDate();
        LocalDate futureDay = currentDate.plusDays(7);
        futureDay = futureDay.isAfter(param.getEndDate()) ? param.getEndDate().plusDays(1) : futureDay;

        // 返回算法的数据，因目前不根据计算结果来，所以这里的数据，实时解析配置数据进行返回，未来最多允许7天（包含当日），后续计算不包含 futureDay 此日
        List<LocalTime> timeList = CalculateComputeUtil.spiltLocalTimeByMinute(LocalTime.MIN, LocalTime.MAX, 5);
        Map<LocalDate, CompletableFuture<List<DemandSpaceSequenceExternalVO>>> spaceSequenceExternalFutures = new HashMap<>();
        do {
            final LocalDate tempDate = startDate;
            CompletableFuture<List<DemandSpaceSequenceExternalVO>> spaceFuture = CompletableFuture.supplyAsync(() ->
                    this.handlerFutureSpaceDemandConfigSequenceData(tempDate, timeList), BusinessThreadPoolExecutor.ttlExecutor());
            spaceSequenceExternalFutures.put(startDate, spaceFuture);

            startDate = startDate.plusDays(1);
        } while (startDate.isBefore(futureDay));

        for (Map.Entry<LocalDate, CompletableFuture<List<DemandSpaceSequenceExternalVO>>> completableFutureEntry : spaceSequenceExternalFutures.entrySet()) {
            LocalDate localDate = completableFutureEntry.getKey();
            CompletableFuture<List<DemandSpaceSequenceExternalVO>> futureEntryValue = completableFutureEntry.getValue();
            List<DemandSpaceSequenceExternalVO> demandSpaceSequenceExternalVOMap = futureEntryValue == null ? null : futureEntryValue.get();
            if (CollectionUtil.isEmpty(demandSpaceSequenceExternalVOMap)) {
                continue;
            }
            spaceSequenceExternalTreeMap.put(localDate, demandSpaceSequenceExternalVOMap);
        }
        return spaceSequenceExternalTreeMap;
    }

    /**
     * 处理单日的配置数据，返回给算法使用
     *
     * @param localDate
     * @param timeList
     * @return
     */
    private List<DemandSpaceSequenceExternalVO> handlerFutureSpaceDemandConfigSequenceData(LocalDate localDate, List<LocalTime> timeList) {
        List<DemandSpaceSequenceExternalVO> resultList = new ArrayList<>();

        // 1.获取此日的需求配置数据
        List<DemandConfigAirconditioningValueObjectSpaceDTO> objectSpaceDTOs = this.getDemandConfigAirconditioningValueObjectSpaceDTOs(localDate, null, null, null, true, true, false, true);
        if (CollectionUtil.isEmpty(objectSpaceDTOs)) {
            return resultList;
        }

        // 2.解析当前的空间配置数据，返回算法所需要的时序数据
        for (DemandConfigAirconditioningValueObjectSpaceDTO objectSpaceDTO : objectSpaceDTOs) {
            TreeMap<LocalTime, DemandSpaceSequenceExternalVO.SpaceSequenceData> spaceSequenceExternalVOTreeMap = new TreeMap<>();

            // 解析营业模式
            List<LocalTime> businessTimes = new ArrayList<>();
            EnumSpaceBusinessModel spaceBusinessModel = CalculateComputeUtil.parseCurrentSpaceConfig(businessTimes, objectSpaceDTO);

            DemandConfigAirconditioningTdbSeasonValueObject tdbQuality = objectSpaceDTO.getGlobalConfig().getEnvironmentQuality().getTdbQuality().getCurrentSeasonQuality(objectSpaceDTO.getAirSeason());
            DemandConfigAirconditioningCO2QualityValueObject co2Quality = objectSpaceDTO.getGlobalConfig().getEnvironmentQuality().getCO2Quality();
            BigDecimal tempSeasonTargetValue = tdbQuality == null ? null : tdbQuality.getOldTempSeasonTargetValue();
            BigDecimal tempSeasonUpperValue = tdbQuality == null ? null : tdbQuality.getOldTempSeasonUpperValue();
            BigDecimal tempSeasonLowerValue = tdbQuality == null ? null : tdbQuality.getOldTempSeasonLowerValue();
            BigDecimal carbonDioxideTargetValue = co2Quality == null ? null : co2Quality.getOldCarbonDioxideTargetValue();
            BigDecimal carbonDioxideLimitValue = co2Quality == null ? null : co2Quality.getOldCarbonDioxideLimitValue();

            boolean shielded = BooleanUtil.isTrue(objectSpaceDTO.getShielded());
            for (LocalTime localTime : timeList) {
                // 验证是否属于当前营业时间范围内
                boolean enable = shielded ? false : CalculateComputeUtil.checkCurrentTimeBelongBusinessTime(localTime, businessTimes, EnumSpaceBusinessModel.ALL_THE_BUSINESS_MODEL.equals(spaceBusinessModel));
                DemandSpaceSequenceExternalVO.SpaceSequenceData externalVO = new DemandSpaceSequenceExternalVO.SpaceSequenceData(enable, tempSeasonTargetValue, tempSeasonUpperValue, tempSeasonLowerValue, carbonDioxideTargetValue, carbonDioxideLimitValue);
                spaceSequenceExternalVOTreeMap.put(localTime, externalVO);
            }

            DemandSpaceSequenceExternalVO spaceSequenceExternalVO = new DemandSpaceSequenceExternalVO(localDate, objectSpaceDTO.getInstanceId(), spaceSequenceExternalVOTreeMap);
            resultList.add(spaceSequenceExternalVO);
        }
        return resultList;
    }


    @Override
    public List<DemandConfigAirconditioningValueObjectSpaceDTO> getDemandConfigAirconditioningValueObjectSpaceDTOs(LocalDate currentDate, MidCalendarAirSeasonTypeEnum airSeason, MidCalendarAcSeasonTypeEnum season,
                                                                                                                   List<DemandConfigAirconditioningValueObjectSpaceDTO> objectSpaceDTOS, boolean filterSeason, boolean filterSystemServiceSpaceRelation, boolean checkInfoCodeRule, boolean queryBusinessTime) {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        currentDate = currentDate == null ? DateUtils.now().toLocalDate() : currentDate;

        // 获取空调季
        if (airSeason == null) {
            airSeason = this.abstractCommonServer.getProjectAirSeason(groupCode, projectId, currentDate);
        }
        // 获取空调工况
        if (season == null) {
            season = this.abstractCommonServer.getProjectSeason(groupCode, projectId, currentDate);
        }
        if (airSeason == null || season == null || (filterSeason && MidCalendarAcSeasonTypeEnum.SEASON_TRANSITION.equals(season))) {
            ControlLogUtil.warn("空调季为空，本次不进行数据处理，项目ID：【{}】，日期：【{}】，空调季：【{}-{}】", projectId, currentDate, airSeason, season);
            return null;
        }

        // 获取空间需求配置
        if (CollectionUtil.isEmpty(objectSpaceDTOS)) {
            DemandConfigQueryParam param = new DemandConfigQueryParam();
            param.setDate(currentDate);
            param.setInstanceType(DemandInstanceTypeEnum.SPACE);
            param.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
            List<DemandConfigVO<BaseDemandConfig>> demandConfigVOS = this.demandQueryService.queryDemandConfig(param);
            if (CollectionUtil.isEmpty(demandConfigVOS)) {
                ControlLogUtil.warn("当前日期【{}】下，不存在空间需求配置信息，不进行处理", currentDate);
                return null;
            }
            objectSpaceDTOS = new ArrayList<>();
            for (DemandConfigVO<BaseDemandConfig> demandConfigVO : demandConfigVOS) {
                DemandConfigValueObject<BaseDemandConfig> finalConfig = demandConfigVO.getFinalConfig();
                BaseDemandConfig globalConfig = finalConfig == null ? null : finalConfig.getGlobalConfig();
                if (globalConfig == null || !(globalConfig instanceof DemandConfigAirconditioningValueObject)) {
                    continue;
                }

                // 校验必须字段
                DemandConfigAirconditioningValueObjectSpaceDTO objectSpaceDTO = new DemandConfigAirconditioningValueObjectSpaceDTO();
                DemandConfigAirconditioningValueObject airconditioningValueObject = (DemandConfigAirconditioningValueObject) globalConfig;
                if (airconditioningValueObject.getBaseConfig() == null || airconditioningValueObject.getEnvironmentQuality() == null
                        || airconditioningValueObject.getEnvironmentQuality().getTdbQuality() == null || airconditioningValueObject.getEnvironmentQuality().getCO2Quality() == null) {
                    continue;
                }

                objectSpaceDTO.setSeason(season);
                objectSpaceDTO.setAirSeason(airSeason);
                objectSpaceDTO.setInstanceId(demandConfigVO.getInstanceId());
                objectSpaceDTO.setInstanceType(demandConfigVO.getInstanceType());
                objectSpaceDTO.setIsParticipateInScheduling(airconditioningValueObject.getEnvironmentQuality() == null ? null : airconditioningValueObject.getEnvironmentQuality().getIsParticipateInScheduling());
                objectSpaceDTO.setShielded(demandConfigVO.getInstanceTag() == null ? false : BooleanUtil.isTrue(demandConfigVO.getInstanceTag().getShielded()));
                objectSpaceDTO.setCustomConfig(demandConfigVO.getInstanceTag() == null ? false : BooleanUtil.isTrue(demandConfigVO.getInstanceTag().getCustomConfig()));
                objectSpaceDTO.setGlobalConfig(airconditioningValueObject);
                objectSpaceDTOS.add(objectSpaceDTO);
            }
        } else {
            for (DemandConfigAirconditioningValueObjectSpaceDTO objectSpaceDTO : objectSpaceDTOS) {
                objectSpaceDTO.setSeason(season);
                objectSpaceDTO.setAirSeason(airSeason);
            }
        }

        // 空间数据过滤返回
        return this.filterDemandConfigAirconditioningValueObjectSpace(projectId, objectSpaceDTOS, filterSystemServiceSpaceRelation, checkInfoCodeRule, queryBusinessTime);
    }
    
    // 生成日期范围列表
    public List<LocalDate> generateDateRange(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        while (!start.isAfter(end)) {
            dates.add(start);
            start = start.plusDays(1);
        }
        return dates;
    }
    
    /**
     * 每日取最大一条更新日期的数据作为当日的配置
     *
     * @param targetDateTime
     * @param acList
     * @return
     */
    private List<DemandValueSelectConfigVO> processDailyMax(
            LocalDateTime targetDateTime,
            List<DemandConfigAirconditioningCustomTimeGroup> acList) {
        
        // 标准化为当日零点（按天分组）
        LocalDate targetDate = targetDateTime.toLocalDate();
        LocalDateTime dayStart = targetDate.atStartOfDay();
        LocalDateTime nextDayStart = dayStart.plusDays(1);
        
        return acList.stream()
                     // 过滤当日有效记录
                     .filter(ac -> ac.getMaxValidEndTime().isAfter(dayStart.minusNanos(1)) &&
                             ac.getUpdateTime().isBefore(nextDayStart))
                     // 分组
                     .collect(Collectors.groupingBy(
                             ac -> targetDate))
                     .values().stream()
                     // 取组内UpdateTime最大的记录
                     .map(group -> group.stream()
                                        .max(Comparator.comparing(DemandConfigAirconditioningCustomTimeGroup::getUpdateTime))
                                        .orElse(null))
                     .filter(Objects::nonNull)
                     // 对象转换
                     .map(ac -> new DemandValueSelectConfigVO(targetDate, ac.getConfigData().getValueSelectType()))
                     .collect(Collectors.toList());
    }
    
    @Override
    public List<DemandValueSelectConfigVO> queryEverydayFixDataList(@NonNull DemandValueSelectConfigQueryParam param) {
        
        // 1. 生成日期序列
        List<LocalDate> dateRange = generateDateRange(param.getStartDate(), param.getEndDate());
        
        // 2. 从数据库获取基础数据
        List<DemandConfigAirconditioningCustomTimeGroup> acList =
                demandConfigAirconditioningCustomService.queryEverydayFixDataList(param.getStartDate(),
                        param.getEndDate());
        
        // 3. 处理逻辑
        return dateRange.parallelStream()
                        .flatMap(targetDate -> processDailyMax(targetDate.atStartOfDay(), acList).stream())
                        .sorted(Comparator.comparing(DemandValueSelectConfigVO::getDate))
                        .collect(Collectors.toList());
        
    }
    
    /**
     * 过滤当前空间需求配置的数据
     *
     * @param projectId
     * @param objectSpaceDTOS
     * @param filterSystemServiceSpaceRelation 是否校验空间与冷站、冷热站、热站的服务关系，true-校验，进行数据过滤
     * @param checkInfoCodeRule                是否检查空间存在孪生温度点位规则数据，仅代表查询，不进行数据上的过滤
     * @param queryBusinessTime                是否查询出该空间的营业时间配置，true-查询
     */
    private List<DemandConfigAirconditioningValueObjectSpaceDTO> filterDemandConfigAirconditioningValueObjectSpace(String projectId, List<DemandConfigAirconditioningValueObjectSpaceDTO> objectSpaceDTOS,
                                                                                                                   boolean filterSystemServiceSpaceRelation, boolean checkInfoCodeRule, boolean queryBusinessTime) {
        List<DemandConfigAirconditioningValueObjectSpaceDTO> resultObjectSpaceDTOs = new ArrayList<>();
        if (CollectionUtil.isEmpty(objectSpaceDTOS)) {
            return resultObjectSpaceDTOs;
        }

        // 校验温度孪生点位规则
        List<ObjectsExistRule> objectsExistRules = !checkInfoCodeRule ? null : this.abstractCommonServer.queryObjectsExistRule(ContextUtil.getGroupCode(), projectId, Lists.newArrayList(new ClassCode(DtpRwdClassCodeEnum.SPACE.getValue(), InfoCodeEnum.SPACE_TDB.getCode())));
        Set<String> existsTdbSpaceIds = CollectionUtil.isEmpty(objectsExistRules) ? new HashSet<>() : objectsExistRules.stream().filter(item -> InfoCodeEnum.SPACE_TDB.getCode().equals(item.getInfoCode())).map(ObjectsExistRule::getObjectId).collect(Collectors.toSet());

        // 校验当前空间与系统的服务关系（冷站、冷热站、热站）
        Set<String> spaceIds = new HashSet<>();
        Map<String, String> spaceSystemMap = new HashMap<>();
        if (filterSystemServiceSpaceRelation) {
            List<JSONObject> systemObjects = this.bizDtpRwdService.queryObjectList(ContextUtil.getGroupCode(), projectId, Lists.newArrayList("ACCC", "ACCI", "ACCH"), null, null, null, null);
            List<String> systemIds = CollectionUtil.isEmpty(systemObjects) ? null : systemObjects.stream().map(item -> item.getString("id")).distinct().collect(Collectors.toList());
            List<JSONObject> spaceObjects = CollectionUtil.isEmpty(systemIds) ? null : this.bizDtpRwdService.queryRelation(ContextUtil.getGroupCode(), projectId, "MechForArch", "Sy2Sp", DtpRwdClassCodeEnum.SPACE.getValue(), systemIds, null);
            if (CollectionUtil.isEmpty(spaceObjects)) {
                return resultObjectSpaceDTOs;
            }
            spaceSystemMap = spaceObjects.stream().collect(Collectors.toMap(key -> key.getString("objTo"), value -> value.getString("objFrom"), (v1, v2) -> v2));
            spaceIds.addAll(spaceSystemMap.keySet());
        } else {
            spaceIds = objectSpaceDTOS.stream().map(DemandConfigAirconditioningValueObjectSpaceDTO::getInstanceId).collect(Collectors.toSet());
        }

        // 获取所有空间的营业时间
        Map<String, List<Range<LocalTime>>> spaceBusinessTimeRangeMap = null;
        if (queryBusinessTime) {
            try {
                spaceBusinessTimeRangeMap = this.getAllSpaceBusinessTimeRangeList(projectId, DateUtils.now().toLocalDate(), spaceIds);
            } catch (Exception e) {
                ControlLogUtil.warn("价值计算-空间营业时间获取异常，日期【{}】，空间【{}】", LocalDate.now(), spaceIds);
            }
        }

        // 数据过滤后返回
        for (DemandConfigAirconditioningValueObjectSpaceDTO objectSpaceDTO : objectSpaceDTOS) {
            if (CollectionUtil.isEmpty(objectSpaceDTO.getBusinessTimeRangeList())) {
                objectSpaceDTO.setBusinessTimeRangeList(spaceBusinessTimeRangeMap == null ? null : spaceBusinessTimeRangeMap.get(objectSpaceDTO.getInstanceId()));
            }
            if (filterSystemServiceSpaceRelation) {
                if (!spaceSystemMap.containsKey(objectSpaceDTO.getInstanceId())) {
                    continue;
                }
                objectSpaceDTO.setSystemId(spaceSystemMap.get(objectSpaceDTO.getInstanceId()));
            }
            if (checkInfoCodeRule) {
                objectSpaceDTO.setTdb(existsTdbSpaceIds.contains(objectSpaceDTO.getInstanceId()));
            }
            DemandConfigAirconditioningBaseConfigValueObject baseConfig = objectSpaceDTO.getGlobalConfig().getBaseConfig();
            if (BooleanUtil.isTrue(baseConfig.getIsAllowResponseRegulation()) && CollectionUtil.isNotEmpty(baseConfig.getResponseRegulationTimeIntervals())) {
                objectSpaceDTO.setResponseRegulationTimeList(CalculateComputeUtil.convertTimeIntervals(baseConfig.getResponseRegulationTimeIntervals()));
            }
            resultObjectSpaceDTOs.add(objectSpaceDTO);
        }

        return resultObjectSpaceDTOs;
    }

    /**
     * 获取空间的营业时间
     *
     * @param localDate
     * @param spaceIds
     * @return
     */
    private Map<String, List<Range<LocalTime>>> getAllSpaceBusinessTimeRangeList(String projectId, LocalDate localDate, Set<String> spaceIds) throws Exception {
        Map<String, List<Range<LocalTime>>> spaceBusinessTimeRangeMap = new HashMap<>();
        if (CollectionUtil.isEmpty(spaceIds)) {
            return spaceBusinessTimeRangeMap;
        }
        // 1.查询每一个空间的营业时间
        String currentDate = localDate.format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATE_PATTERN));
        Map<String, CompletableFuture<MidCalendarOpenTimeResponse>> spaceBusinessTimeFutures = new HashMap<>();
        for (String spaceId : spaceIds) {
            CompletableFuture<MidCalendarOpenTimeResponse> spaceFuture = CompletableFuture.supplyAsync(() -> {
                MidCalendarOpenTimeQueryParam midCalendarOpenTimeQueryParam = new MidCalendarOpenTimeQueryParam();
                midCalendarOpenTimeQueryParam.setWorkDateStart(currentDate);
                midCalendarOpenTimeQueryParam.setWorkDateEnd(currentDate);
                midCalendarOpenTimeQueryParam.setDictType(MidCalendarScheduleTagTypeEnum.OPENING);
                midCalendarOpenTimeQueryParam.setObjId(spaceId);
                return midCalendarClientService.getMidCalendarOpenTime(projectId, midCalendarOpenTimeQueryParam);
            }, BusinessThreadPoolExecutor.ttlExecutor());
            spaceBusinessTimeFutures.put(spaceId, spaceFuture);
        }

        // 2.转换为本方所能识别的应用时间
        for (Map.Entry<String, CompletableFuture<MidCalendarOpenTimeResponse>> completableFutureEntry : spaceBusinessTimeFutures.entrySet()) {
            String spaceId = completableFutureEntry.getKey();
            CompletableFuture<MidCalendarOpenTimeResponse> futureEntryValue = completableFutureEntry.getValue();
            MidCalendarOpenTimeResponse midCalendarOpenTimeResponse = futureEntryValue == null ? null : futureEntryValue.get();
            if (midCalendarOpenTimeResponse == null || CollectionUtil.isEmpty(midCalendarOpenTimeResponse.getTimeTable())) {
                continue;
            }

            List<Range<LocalTime>> spaceBusinessTimeRange = new ArrayList<>();
            for (MidCalendarOpenTimeResponse.TimeTable timeTable : midCalendarOpenTimeResponse.getTimeTable()) {
                String workTime = timeTable.getWorkTime();
                if (StrUtil.isNotBlank(workTime)) {
                    String[] split = workTime.split("-");
                    if (split.length > 1) {
                        LocalTime startTime = LocalTime.parse(split[0], DateTimeFormatter.ofPattern("HH:mm"));
                        LocalTime endTime = LocalTime.parse(split[1], DateTimeFormatter.ofPattern("HH:mm"));
                        spaceBusinessTimeRange.add(Range.between(startTime, endTime));
                    }
                }
            }
            if (spaceBusinessTimeRange.size() > 0) {
                spaceBusinessTimeRangeMap.put(spaceId, spaceBusinessTimeRange);
            }
        }
        return spaceBusinessTimeRangeMap;
    }

    @Override
    public String logTag() {
        return "对外服务-需求管理";
    }
}
