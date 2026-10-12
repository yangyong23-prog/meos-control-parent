package com.persagy.apccontrol.basic.outside.feign.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.properties.saas.SaasProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.DemandBasicProperties;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarDTO;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarGlobalRuleDTO;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarWorkTimeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.*;
import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.BusinessTimeRangeParam;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandInstantiationDateTagVO;
import com.persagy.apccontrol.basic.outside.feign.client.MidCalendarClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.*;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.feign.service.param.calendar.*;
import com.persagy.apccontrol.basic.structure.calendar.converter.MidCalendarConvert;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.utils.BeanUtil;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.DateTypeEnum;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.enumeration.error.third.MiddlewareServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.executor.DateTimeRangeExecutor;
import com.persagy.validate.utility.executor.MapExecutor;
import com.persagy.validate.utility.pattern.DatePattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleVO.localDateRange;

/**
 * 工作历客户端服务实现
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Service
@Slf4j
@Validated
public class MidCalendarClientServiceImpl implements MidCalendarClientService, LogComponent {

    private static final String LOG_TAG = "工作历服务";

    @Autowired
    private SaasProperties saasProperties;
    @Autowired
    private MidCalendarConvert midCalendarConvert;
    @Autowired
    private MidCalendarClient midCalendarClient;
    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Override
    @Nullable
    public MidCalendarDTO getCalendar(@NonNull String projectId, @NonNull MidCalendarQueryParam param) {
        return getOne(false, listCalendar(projectId, param));
    }

    @Override
    public MidCalendarDTO getOrInitObjectRealCalendar(
            @NonNull String projectId,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId) {
        MidCalendarDTO calendar = getCalendar(projectId,
                new MidCalendarQueryParam().setObjId(instanceId));
        if (Objects.isNull(calendar)) {
            MidCalendarDTO projectCalendar = getCalendar(projectId,
                    new MidCalendarQueryParam().setObjId(projectId));
            if (Objects.isNull(projectCalendar)) {
                String calendarId = resetProjectCalendar(projectId);
                projectCalendar = getCalendar(projectId, new MidCalendarQueryParam().setWorkId(calendarId));
            }
            if (instanceType == DemandInstanceTypeEnum.PROJECT) {
                return projectCalendar;
            }
            String calendarId = addCalendar(projectId, instanceId);
            calendar = getCalendar(projectId, new MidCalendarQueryParam().setWorkId(calendarId));
        } else {
            if (!StringUtils.equals(instanceId, calendar.getObjectId())) {
                String calendarId = addCalendar(projectId, instanceId);
                calendar = getCalendar(projectId, new MidCalendarQueryParam().setWorkId(calendarId));
            }
        }
        return calendar;
    }

    @Override
    @Nullable
    public MidCalendarDateTag getSeason(@NonNull String groupCode, @NonNull String projectId, @NonNull LocalDate localDate) {
        JSONObject requestParam = new JSONObject();
        requestParam.put("objId", projectId);
        requestParam.put("labelDateStart", localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        requestParam.put("labelDateEnd", localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        requestParam.put("dictType", MidCalendarDateTagTypeEnum.AC_SEASON.value());

        CustomerResponse<List<MidCalendarDateTag>> listCustomerResponse = midCalendarClient.dateTag(DtpRwdCommonParam.instance(groupCode, projectId), requestParam);
        return (listCustomerResponse == null || CollectionUtil.isEmpty(listCustomerResponse.getContent())) ? new MidCalendarDateTag()
                : listCustomerResponse.getContent().get(0);
    }

    @Nullable
    @Override
    public MidCalendarAcSeasonTypeEnum getAcSeasonType(
            @NonNull String projectId,
            @NonNull String objectId,
            @NonNull LocalDate date) {
        MidCalendarDayDateTagQueryParam tagQueryParam = new MidCalendarDayDateTagQueryParam();
        tagQueryParam.setObjId(objectId);
        tagQueryParam.setBeginDate(date);
        tagQueryParam.setEndDate(date);
        List<MidCalendarDayDateTagResponse> dateTags = listCalendarDayDateTag(projectId, tagQueryParam);
        if (CollectionUtils.isEmpty(dateTags)) {
            return null;
        }
        MidCalendarDayDateTagResponse dateTag = IterableUtils.first(dateTags);
        List<String> tagList = dateTag.getTagList();
        if (CollectionUtils.isEmpty(tagList)) {
            logProjectInstance(projectId).warn("日期标签列表为空", Arrays.asList(
                    Pair.of("objectId", objectId),
                    Pair.of("date", date),
                    Pair.of("dateTag", dateTag)));
            return null;
        }
        for (String tag : tagList) {
            MidCalendarAcSeasonTypeEnum acSeasonType = BaseEnum.analyze(tag,
                    MidCalendarAcSeasonTypeEnum.class, false);
            if (Objects.nonNull(acSeasonType)) {
                return acSeasonType;
            }
        }
        return null;
    }

    @Override
    public MidCalendarAirSeasonTypeEnum getAirSeasonType(@NonNull String objectId, @NonNull LocalDate date) {
        MidCalendarDayDateTagQueryParam tagQueryParam = new MidCalendarDayDateTagQueryParam();
        tagQueryParam.setObjId(objectId);
        tagQueryParam.setBeginDate(date);
        tagQueryParam.setEndDate(date);
        List<MidCalendarDayDateTagResponse> dateTags = listCalendarDayDateTag(ContextUtil.getProjectId(), tagQueryParam);
        if (CollectionUtils.isEmpty(dateTags)) {
            return null;
        }
        MidCalendarDayDateTagResponse dateTag = IterableUtils.first(dateTags);
        List<String> tagList = dateTag.getTagList();
        if (CollectionUtils.isEmpty(tagList)) {
            logProjectInstance().warn("日期标签列表为空", Arrays.asList(
                    Pair.of("objectId", objectId),
                    Pair.of("date", date),
                    Pair.of("dateTag", dateTag)));
            return null;
        }
        for (String tag : tagList) {
            MidCalendarAirSeasonTypeEnum acSeasonType = BaseEnum.analyze(tag,
                    MidCalendarAirSeasonTypeEnum.class, false);
            if (Objects.nonNull(acSeasonType)) {
                return acSeasonType;
            }
        }
        return null;
    }

    @Override
    public Map<LocalDate, MidCalendarAirSeasonTypeEnum> mapAirSeasonType(
            @NonNull String objectId,
            @NonNull LocalDate beginDate,
            @NonNull LocalDate endDate) {
        MidCalendarDayDateTagQueryParam tagQueryParam = new MidCalendarDayDateTagQueryParam();
        tagQueryParam.setObjId(objectId);
        tagQueryParam.setBeginDate(beginDate);
        tagQueryParam.setEndDate(endDate.minusDays(1));
        List<MidCalendarDayDateTagResponse> dateTags = listCalendarDayDateTag(ContextUtil.getProjectId(), tagQueryParam);
        if (CollectionUtils.isEmpty(dateTags)) {
            return null;
        }
        Map<LocalDate, MidCalendarDayDateTagResponse> date2Response = dateTags.stream().collect(Collectors.toMap(
                response -> response.getCurrentDay().toLocalDate(), Function.identity()));
        Map<LocalDate, MidCalendarAirSeasonTypeEnum> result = new HashMap<>();
        DateTimeRangeExecutor.generateDate(DateTypeEnum.DAY, beginDate, endDate, eachDate -> {
            MidCalendarDayDateTagResponse dateTag = date2Response.get(eachDate);
            List<String> tagList = dateTag.getTagList();
            if (CollectionUtils.isEmpty(tagList)) {
                logProjectInstance().warn("日期标签列表为空", Arrays.asList(
                        Pair.of("objectId", objectId),
                        Pair.of("beginDate", beginDate),
                        Pair.of("endDate", endDate),
                        Pair.of("dateTag", dateTag)));
                return;
            }
            for (String tag : tagList) {
                MidCalendarAirSeasonTypeEnum acSeasonType = BaseEnum.analyze(tag,
                        MidCalendarAirSeasonTypeEnum.class, false);
                if (Objects.nonNull(acSeasonType)) {
                    result.put(eachDate, acSeasonType);
                    break;
                }
            }
        });
        return result;
    }

    @Override
    public MidCalendarGlobalRuleDTO getGlobalCalendarRule(
            @NonNull String projectId, @NonNull String calendarId,
            @Nullable MidCalendarDateTagTypeEnum dateTagType,
            @Nullable MidCalendarDateTagValueEnum dateTagValue) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        MidCalendarRuleQueryParam queryParam = new MidCalendarRuleQueryParam();
        queryParam.setWorkId(calendarId);
        if (Objects.nonNull(dateTagType)) {
            queryParam.setTagType(dateTagType.value());
        }
        if (Objects.nonNull(dateTagValue)) {
            queryParam.setTagValue(dateTagValue.value());
        }
        MidCalendarGlobalRuleDTO result = new MidCalendarGlobalRuleDTO();
        result.setCalendarId(calendarId);
        List<MidCalendarRuleResponse> calendarRules = query(false, null, projectId, LocaleMessageUtil.getMessage("key_36e92ae31afc4bf0aa0be17df6f56583"), queryParam,
                () -> midCalendarClient.listCalendarRule(commonParam, queryParam), null);
        if (CollectionUtils.isEmpty(calendarRules)) {
            return result;
        }
        Map<MidCalendarDateTagValueEnum, String> typeAndRuleId = Optional.ofNullable(
                result.getGlobalTypeAndRuleId()).orElseGet(HashMap::new);

        for (MidCalendarRuleResponse calendarRule : calendarRules) {
            String ruleId = calendarRule.getId();
            Integer calLevel = calendarRule.getCalLevel();
            String tagType = calendarRule.getTagType();
            String tagValue = calendarRule.getTagValue();
            if (Objects.isNull(calLevel)) {
                LogExecutor.rpc(queryParam, calendarRule).warn(log, LOG_TAG, "未知的规则类型");
                continue;
            }
            MidCalendarDateTagTypeEnum tagTypeEnum = BaseEnum.analyze(tagType,
                    MidCalendarDateTagTypeEnum.class, false);
            if (Objects.isNull(tagTypeEnum)) {
                LogExecutor.rpc(queryParam, calendarRule).warn(log, LOG_TAG, "未知的日期标签类型");
                continue;
            }
            MidCalendarDateTagValueEnum tagValueEnum = BaseEnum.analyze(tagValue,
                    MidCalendarDateTagValueEnum.class, false);

            typeAndRuleId.put(tagValueEnum, ruleId);
            fillGlobalAcSeasonRule(result, queryParam, calendarRule, tagTypeEnum, tagValueEnum);
            fillGlobalWorkdayRule(result, queryParam, calendarRule, tagTypeEnum, tagValueEnum);
            fillGlobalAnniversaryRule(result, queryParam, calendarRule, tagTypeEnum, tagValueEnum);
            // 新增节假日
            fillGlobalGeneralRule(result, queryParam, calendarRule, tagTypeEnum, tagValueEnum);
            fillGlobalSpecialConfRule(result, queryParam, calendarRule, tagTypeEnum, tagValueEnum);
            // 星期号
            fillGlobalWeekConfRule(result, queryParam, calendarRule, tagTypeEnum, tagValueEnum);
        }
        result.setGlobalTypeAndRuleId(typeAndRuleId);
        return result;
    }

    @Override
    public MidCalendarGlobalRuleDTO getOrInitObjectGlobalCalendarRule(
            @NonNull String projectId,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId,
            @Nullable MidCalendarDateTagTypeEnum dateTagType,
            @Nullable MidCalendarDateTagValueEnum dateTagValue) {
        MidCalendarDTO projectCalendar = getOrInitObjectRealCalendar(projectId, instanceType, instanceId);
        String calendarId = projectCalendar.getCalendarId();
        return getGlobalCalendarRule(projectId, calendarId, dateTagType, dateTagValue);
    }

    @Override
    public List<MidCalendarDTO> listCalendar(@NonNull String projectId, @NonNull MidCalendarQueryParam param) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        List<MidCalendarResponse> calendars = query(false, null, projectId, LocaleMessageUtil.getMessage("key_c3552a84a3d447d29e9835efc1aa04a2"), param,
                () -> midCalendarClient.listCalendar(commonParam, param), null);
        if (CollectionUtils.isEmpty(calendars)) {
            return Lists.newArrayList();
        }
        List<MidCalendarDTO> result = new ArrayList<>();
        for (MidCalendarResponse calendar : calendars) {
            List<MidCalendarResponse.LinkObject> linkObjects = calendar.getLinkObjects();
            if (CollectionUtils.isEmpty(linkObjects)) {
                continue;
            }
            result.addAll(linkObjects.stream()
                    .map(item -> midCalendarConvert.toDto(calendar, item))
                    .collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public List<CalendarObjectNumResponse> calendarObjectNum(List<CalendarObjectNumQueryParam> param) {
        if (CollectionUtils.isEmpty(param)) {
            return Lists.newArrayList();
        }
        // 最多查询十个每次，所以分批次查询
        List<CalendarObjectNumResponse> calendarObjectNumAllList = new ArrayList<>();
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(ContextUtil.getProjectId());
        List<List<CalendarObjectNumQueryParam>> lists = BeanUtil.splitList(param, 100);
        for (List<CalendarObjectNumQueryParam> list : lists) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("objects", list);
            List<CalendarObjectNumResponse> calendars = query(false, null, ContextUtil.getProjectId(), LocaleMessageUtil.getMessage("key_c3552a84a3d447d29e9835efc1aa04a2"), null,
                    () -> midCalendarClient.calendarObjectNum(commonParam, jsonObject), null);
            if (CollectionUtils.isNotEmpty(calendars)) {
                calendarObjectNumAllList.addAll(calendars);
            }
        }
        return calendarObjectNumAllList;
    }

    @Override
    public List<MidCalendarDayDateTagResponse> listCalendarDayDateTag(
            @NonNull String projectId, @NonNull MidCalendarDayDateTagQueryParam param) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        return query(false, null, projectId, LocaleMessageUtil.getMessage("key_bad797794f2f42d9b7b363ca357fc814"), param,
                () -> midCalendarClient.listCalendarDayDateTag(commonParam, param), ArrayList::new);
    }

    @Override
    public List<MidCalendarDayPlanResponse> listCalendarDayPlan(
            @NonNull String projectId, @NonNull MidCalendarDayPlanQueryParam param) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        return query(false, null, projectId, LocaleMessageUtil.getMessage("key_d70b99c0c9c048bba0d4f4d3e028dac3"),
                param, () -> midCalendarClient.listCalendarDayPlan(commonParam, param), ArrayList::new);
    }

    @Override
    public List<MidCalendarWorkTimeDTO> listCalendarWorkTime(@NonNull MidCalendarWorkTimeQueryParam param) {
        param.checkAndSet();
        param.setWorkDateEnd(param.getWorkDateEnd().minusDays(1));
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(getProjectId());
        List<MidCalendarOpenTimeResponse> workTimeResponses = query(false, LocaleMessageUtil.getMessage("key_5971ec9e8d1344f194b5b4882fee09bd"),
                param, () -> midCalendarClient.listCalendarOpenTime(commonParam, param), ArrayList::new);
        if (CollectionUtils.isEmpty(workTimeResponses)) {
            return Lists.newArrayList();
        }
        List<MidCalendarWorkTimeDTO> result = new ArrayList<>();
        for (MidCalendarOpenTimeResponse workTimeResponse : workTimeResponses) {
            String workDate = workTimeResponse.getWorkDate();
            Boolean workday = workTimeResponse.getWorkday();
            Boolean anniversary = workTimeResponse.getAnniversary();
            List<MidCalendarOpenTimeResponse.TimeTable> timeTable = workTimeResponse.getTimeTable();
            if (CollectionUtils.isEmpty(timeTable)) {
                continue;
            }
            LocalDate date;
            try {
                date = LocalDate.parse(workDate, cn.hutool.core.date.DatePattern.PURE_DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                logProjectInstance().error("营业日期解析失败", e, Pair.of("workDate", workDate));
                throw new ApplicationException(MiddlewareServerErrorEnum.RPC_SERVER_ERROR);
            }
            for (MidCalendarOpenTimeResponse.TimeTable table : timeTable) {
                String workTime = table.getWorkTime();

                if (StringUtils.isBlank(workTime)) {
                    continue;
                }
                // 解析营业时间
                Range<LocalTime> timeRange = analyzeWorkTimeRange(workTime);

                MidCalendarWorkTimeDTO resultItem = new MidCalendarWorkTimeDTO();
                resultItem.setDate(date);
                resultItem.setWorkday(workday);
                resultItem.setAnniversary(anniversary);
                resultItem.setWorkId(table.getWorkId());
                resultItem.setBeginWorkTime(timeRange.getMinimum());
                resultItem.setEndWorkTime(timeRange.getMaximum());
                result.add(resultItem);
            }
        }
        return result;
    }

    @Override
    public List<MidCalendarOpenTimeProjectDTO> listOpenTimeProject(@NonNull LocalDate workDate) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(getProjectId());
        MidCalendarOpenTimeProjectQueryParam queryParam = new MidCalendarOpenTimeProjectQueryParam(workDate);
        queryParam.setWorkDate(workDate);
        List<MidCalendarOpenTimeProjectResponse> responses = query(false, LocaleMessageUtil.getMessage("key_debc6bb3df9f4b6c94f4072b607d50e7"),
                workDate, () -> midCalendarClient.listOpenTimeProject(commonParam, queryParam), ArrayList::new);
        if (CollectionUtils.isEmpty(responses)) {
            return Lists.newArrayList();
        }
        List<MidCalendarOpenTimeProjectDTO> result = new ArrayList<>();
        for (MidCalendarOpenTimeProjectResponse response : responses) {
            String workTime = response.getWorkTime();

            String[] splits = StringUtils.split(workTime, ",");
            if (Objects.isNull(splits)) {
                continue;
            }
            List<Range<LocalTime>> workTimeRanges = new ArrayList<>();
            for (String split : splits) {
                if (StringUtils.isBlank(split)) {
                    continue;
                }
                workTimeRanges.add(analyzeWorkTimeRange(split));
            }
            MidCalendarOpenTimeProjectDTO resultItem = midCalendarConvert.toOpenTimeProjectDto(response);
            resultItem.setWorkTimeRanges(workTimeRanges);
            result.add(resultItem);
        }
        return result;
    }

    @Override
    public String addCalendar(@NonNull String projectId, @NonNull String instanceId) {
        DtpRwdObjectDTO rwdObject = dtpRwdClientService.getObject(false, true,
                projectId, new DtpRwdObjectQueryParam().setId(instanceId));
        String calendarName = rwdObject.getDynamicName() + LocaleMessageUtil.getMessage("key_c3552a84a3d447d29e9835efc1aa04a2");
        return addCalendar(projectId,
                instanceId, calendarName, null);
    }

    @Override
    public String addCalendar(
            @NonNull String projectId, @NonNull String objectId,
            @NonNull String calendarName, @Nullable String replicaCalId) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        MidCalendarInsertParam insertParam = new MidCalendarInsertParam();
        insertParam.setObjId(objectId);
        insertParam.setName(calendarName);
        insertParam.setReplicaCalId(replicaCalId);
        MidCalendarInsertResponse response = insert(true, null, projectId,
                LocaleMessageUtil.getMessage("key_c3552a84a3d447d29e9835efc1aa04a2"), insertParam, () -> midCalendarClient.addCalendar(commonParam, insertParam));
        return Objects.nonNull(response) ? response.getWorkId() : null;
    }

    @Override
    public void editObjectPlanContent(@NonNull String projectId, @NonNull MidCalendarPlanDataInsertParam param) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        update(true, null, projectId, LocaleMessageUtil.getMessage("key_f98d866fa2e8406289b5a71752b74a27"),
                param, () -> midCalendarClient.editObjectPlanContent(commonParam, param));
    }

    @Override
    public void editGlobalDateWorkDayTagRule(
            @NonNull String projectId,
            @NonNull MidCalendarGlobalDateWorkDateTagRuleUpdateParam param) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        MidCalendarDateTagRuleUpdateParam.Content content = midCalendarConvert.toWorkDateTagRuleUpdateParamContent(param);
        MidCalendarDateTagRuleUpdateParam updateParam = new MidCalendarDateTagRuleUpdateParam()
                .setContents(Lists.newArrayList(content));
        update(true, null, projectId, LocaleMessageUtil.getMessage("key_b42382d4cf964dd687c2e434dcb9e7f8"), updateParam,
                () -> midCalendarClient.editDateTagRule(commonParam, updateParam));
    }

    @Override
    public void editGlobalDateAcSeasonTagRule(
            @NonNull String projectId,
            @NonNull Collection<MidCalendarGlobalDateAcSeasonTagRuleUpdateParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return;
        }
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        MidCalendarDateTagRuleUpdateParam updateParam = new MidCalendarDateTagRuleUpdateParam()
                .setContents(midCalendarConvert.toAcSeasonTagRuleUpdateParamContent(params));
        update(true, null, projectId, LocaleMessageUtil.getMessage("key_b7e105d0c8ba4f3fbc7e17599d632950"), updateParam,
                () -> midCalendarClient.editDateTagRule(commonParam, updateParam));
    }

    @Override
    public void editGlobalAnniversaryRule(
            @NonNull String projectId,
            @NonNull MidCalendarGlobalAnniversaryRuleUpdateParam param) {
        MidCalendarDateTagRuleUpdateParam.Content contents =
                midCalendarConvert.toAnniversaryRuleUpdateParamContent(param);
        if (Objects.nonNull(contents)) {
            DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
            MidCalendarDateTagRuleUpdateParam updateParam = new MidCalendarDateTagRuleUpdateParam()
                    .setContents(Lists.newArrayList(contents));
            update(true, null, projectId, LocaleMessageUtil.getMessage("key_044ed790ab44438ebe967462fb46c4d0"), updateParam,
                    () -> midCalendarClient.editDateTagRule(commonParam, updateParam));
        }
    }

    @Override
    public void editGlobalOpeningRule(
            @NonNull String projectId, @NonNull String calendarId,
            @Nullable Boolean isAsyncInit,
            @NonNull Collection<MidCalendarGlobalOpeningRuleUpdateParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return;
        }
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        MidCalendarSchedulePlanRuleUpdateParam updateParam = new MidCalendarSchedulePlanRuleUpdateParam();
        updateParam.setWorkId(calendarId);
        updateParam.setType(1);
        updateParam.setAsyncInit(isAsyncInit);
        updateParam.setContents(midCalendarConvert.toOpeningRuleUpdateParamContent(params));
        update(true, null, projectId, LocaleMessageUtil.getMessage("key_18ec74cb491b45628f8e6f6d2a835d37"), updateParam,
                () -> midCalendarClient.editSchedulePlan(commonParam, updateParam));
    }

    @Override
    public void batchEditDateTagGlobalRule(
            @NonNull String projectId,
            @NonNull MidCalendarDateTagGlobalRuleBatchUpdateParam param) {
        Boolean asyncInit = param.getAsyncInit();
        MidCalendarGlobalDateWorkDateTagRuleUpdateParam workDayTagRule = param.getWorkDayTagRule();
        Collection<MidCalendarGlobalDateAcSeasonTagRuleUpdateParam> acSeasonTagRules = param.getAcSeasonTagRules();
        Collection<MidCalendarGlobalDateAirSeasonTagRuleUpdateParam> airSeasonTagRules = param.getAirSeasonTagRules();
        MidCalendarGlobalAnniversaryRuleUpdateParam anniversaryRule = param.getAnniversaryRule();
        MidCalendarGlobalHolidayRuleUpdateParam holidayRule = param.getHolidayRule();

        List<MidCalendarDateTagRuleUpdateParam.Content> dateTagRuleUpdateParams = new ArrayList<>();

        if (Objects.nonNull(workDayTagRule)) {
            dateTagRuleUpdateParams.add(midCalendarConvert.toWorkDateTagRuleUpdateParamContent(workDayTagRule));
        }
        if (Objects.nonNull(acSeasonTagRules)) {
            dateTagRuleUpdateParams.addAll(midCalendarConvert.toAcSeasonTagRuleUpdateParamContent(acSeasonTagRules));
        }
        if (Objects.nonNull(airSeasonTagRules)) {
            dateTagRuleUpdateParams.addAll(midCalendarConvert.toAirSeasonTagRuleUpdateParamContent(airSeasonTagRules));
        }
        if (Objects.nonNull(anniversaryRule)) {
            dateTagRuleUpdateParams.add(midCalendarConvert.toAnniversaryRuleUpdateParamContent(anniversaryRule));
        }
        // 新增节假日
        if (Objects.nonNull(holidayRule)) {
            // 查询节假日规则数据,并删除节假日的规则(原因： yyyy-MM-dd 日期标签类型的需要先删除再保存(其实只需要删除特殊配置的，这里就不区分了一起删除也可以))
            DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
            MidCalendarRuleQueryParam queryParam = new MidCalendarRuleQueryParam();
            queryParam.setWorkId(holidayRule.getCalendarId());
            queryParam.setTagType(MidCalendarDateTagTypeEnum.HOLIDAY.value());
            queryParam.setTagValue(MidCalendarDateTagValueEnum.MINOR_VACATION.value());
            List<MidCalendarRuleResponse> calendarRules = query(false, null, projectId, LocaleMessageUtil.getMessage("key_36e92ae31afc4bf0aa0be17df6f56583"), queryParam,
                    () -> midCalendarClient.listCalendarRule(commonParam, queryParam), null);
            if (CollectionUtils.isNotEmpty(calendarRules)) {
                List<String> ruleIds = calendarRules.stream().map(MidCalendarRuleResponse::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                // 删除节假日规则
                if (CollectionUtils.isNotEmpty(ruleIds)) {
                    delRule(projectId, holidayRule.getCalendarId(), ruleIds);
                }
            }
            if (CollectionUtils.isNotEmpty(holidayRule.getGlobalDate())) {
                // 处理通用配置
                dateTagRuleUpdateParams.add(midCalendarConvert.toHolidayGeneralRuleUpdateParamContent(holidayRule));
            }
            if (CollectionUtils.isNotEmpty(holidayRule.getPeriodDate())) {
                // 处理特殊配置
                dateTagRuleUpdateParams.addAll(midCalendarConvert.toHolidaySpecialRuleUpdateParamContent(holidayRule));
            }
        }
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        MidCalendarDateTagRuleUpdateParam updateParam = new MidCalendarDateTagRuleUpdateParam();
        updateParam.setAsyncInit(asyncInit);
        if (CollectionUtils.isNotEmpty(dateTagRuleUpdateParams)) {
            updateParam.setContents(dateTagRuleUpdateParams);
        }
        update(true, null, projectId, LocaleMessageUtil.getMessage("key_bad797794f2f42d9b7b363ca357fc814"), updateParam,
                () -> midCalendarClient.editDateTagRule(commonParam, updateParam));
    }

    @Override
    public String resetProjectCalendar(@NonNull String projectId) {
        MidCalendarDTO projectCalendar = getCalendar(projectId, new MidCalendarQueryParam().setObjId(projectId));
        String calendarId = Objects.nonNull(projectCalendar)
                ? projectCalendar.getCalendarId()
                : addCalendar(projectId, projectId);
        MidCalendarGlobalRuleDTO calendarRule = new MidCalendarGlobalRuleDTO();
        calendarRule.setCalendarId(calendarId);

        DemandBasicProperties basicProperties = saasProperties.getDemand().getBasic();

        List<MidCalendarGlobalDateAcSeasonTagRuleUpdateParam> editAcSeasonParams = new ArrayList<>();
        // 编辑空调季-供冷季
        if (CollectionUtils.isEmpty(MapUtils.getObject(calendarRule.getGlobalSeasonDateRangeMap(),
                MidCalendarAcSeasonTypeEnum.SEASON_COOLING))) {
            editAcSeasonParams.add(new MidCalendarGlobalDateAcSeasonTagRuleUpdateParam(calendarId,
                    MidCalendarDateTagValueEnum.SEASON_COOLING, basicProperties.getCoolingSeasonDateRangeList()));
        }
        // 编辑空调季-供暖季
        if (CollectionUtils.isEmpty(MapUtils.getObject(calendarRule.getGlobalSeasonDateRangeMap(),
                MidCalendarAcSeasonTypeEnum.SEASON_HEATING))) {
            editAcSeasonParams.add(new MidCalendarGlobalDateAcSeasonTagRuleUpdateParam(calendarId,
                    MidCalendarDateTagValueEnum.SEASON_HEATING, basicProperties.getHeatingSeasonDateRangeList()));
        }
        if (CollectionUtils.isNotEmpty(editAcSeasonParams)) {
            editGlobalDateAcSeasonTagRule(projectId, editAcSeasonParams);
        }

        // 编辑工休日
        if (CollectionUtils.isEmpty(calendarRule.getGlobalWeekDays())) {
            editGlobalDateWorkDayTagRule(projectId,
                    new MidCalendarGlobalDateWorkDateTagRuleUpdateParam(calendarId,
                            MidCalendarWorkDayTypeEnum.WORKDAY, basicProperties.getWeekdays()));
        }

        // 编辑工休日营业时间, 默认不区分公休日
        if (CollectionUtils.isEmpty(calendarRule.getGlobalBusinessTimeRangeListOnWeekDays())) {
            editGlobalOpeningRule(projectId, calendarId, false, Lists.newArrayList(
                    new MidCalendarGlobalOpeningRuleUpdateParam(MidCalendarDateTagTypeEnum.DEFAULT,
                            null, basicProperties.getBusinessTimeRangeList(), null, null)));
        }
        return calendarId;
    }

    @Override
    public void delCalendar(@NonNull String projectId, @NonNull String calendarId) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        MidCalendarDeleteParam deleteParam = new MidCalendarDeleteParam();
        deleteParam.setWorkId(calendarId);
        delete(true, null, projectId, LocaleMessageUtil.getMessage("key_c3552a84a3d447d29e9835efc1aa04a2"), deleteParam,
                () -> midCalendarClient.delCalendar(commonParam, deleteParam));
    }

    @Override
    public void delBatchCalendar(@NonNull String projectId, @NonNull Collection<String> calendarIds) {
        if (CollectionUtils.isEmpty(calendarIds)) {
            return;
        }
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        MidCalendarDeleteBatchParam deleteBatchParam = new MidCalendarDeleteBatchParam();
        deleteBatchParam.setWorkIds(calendarIds);
        delete(true, null, projectId, LocaleMessageUtil.getMessage("key_c3552a84a3d447d29e9835efc1aa04a2"), deleteBatchParam,
                () -> midCalendarClient.delBatchCalendar(commonParam, deleteBatchParam));
    }

    @Override
    public void delRule(@NonNull String projectId, @NonNull String calendarId, @Nullable Collection<String> ruleIds) {
        if (CollectionUtils.isEmpty(ruleIds)) {
            return;
        }
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(projectId);
        MidCalendarRuleDeleteParam deleteParam = new MidCalendarRuleDeleteParam();
        deleteParam.setWorkId(calendarId);
        deleteParam.setRuleInfo(ruleIds.stream()
                .map(item -> new MidCalendarRuleDeleteParam.RuleInfo().setRuleId(item))
                .collect(Collectors.toSet()));
        delete(true, null, projectId, LocaleMessageUtil.getMessage("key_36e92ae31afc4bf0aa0be17df6f56583"), deleteParam,
                () -> midCalendarClient.delRule(commonParam, deleteParam));
    }

    /**
     * 解析营业时间
     *
     * @param workTime 营业时间
     * @return 解析后的营业时间
     * @since 7.250202.0
     */
    private Range<LocalTime> analyzeWorkTimeRange(@NonNull String workTime) {
        String[] split = StringUtils.split(workTime, MidCalendarWorkTimeDTO.DEFAULT_WORK_TIME_SPLIT);
        if (Objects.isNull(split)) {
            logProjectInstance().error("营业时间格式错误", Collections.emptyList());
            throw new ApplicationException(MiddlewareServerErrorEnum.RPC_SERVER_ERROR);
        }
        try {
            LocalTime beginTime = LocalTime.parse(split[0], MidCalendarClientService.OPENING_TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(split[1], MidCalendarClientService.OPENING_TIME_FORMATTER);
            return Range.between(beginTime, endTime);
        } catch (IndexOutOfBoundsException e) {
            logProjectInstance().error("营业时间不完整", Collections.emptyList());
            throw new ApplicationException(MiddlewareServerErrorEnum.RPC_SERVER_ERROR);
        }
    }

    /**
     * 填充全局店庆日规则
     *
     * @param target       目标源
     * @param queryParam   查询参数
     * @param calendarRule 工作历配置
     * @param tagTypeEnum  工作历标签类型
     * @param tagValueEnum 工作历标签值
     * @since v5.7.0
     */
    private void fillGlobalAnniversaryRule(
            @NonNull MidCalendarGlobalRuleDTO target,
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse calendarRule,
            @NonNull MidCalendarDateTagTypeEnum tagTypeEnum,
            MidCalendarDateTagValueEnum tagValueEnum) {
        if (calendarRule.getCalLevel() == 1 && tagValueEnum != null) {
            if (tagTypeEnum == MidCalendarDateTagTypeEnum.ANNIVERSARY
                    && tagValueEnum == MidCalendarDateTagValueEnum.ANNIVERSARY) {
                List<String> periodContents = calendarRule.getPeriodContents();
                if (CollectionUtils.isEmpty(periodContents)) {
                    LogExecutor.rpc(queryParam, calendarRule).warn(log, LOG_TAG, "无店庆日全局配置日期范围");
                    return;
                }
                LinkedHashSet<Range<MonthDay>> dateRange = analyzeMonthDayPeriodContents(periodContents,
                        queryParam, calendarRule, LocaleMessageUtil.getMessage("key_36ed918cc9234be1b256dd9139f53348"));
                if (CollectionUtils.isNotEmpty(dateRange)) {
                    target.setGlobalAnniversary(dateRange);
                }
            }
        }
    }

    /**
     * @author weiyizhong
     * @Company yushu.biz
     * 　　 * @date 2026/1/4 17:54
     * 　　 * @description: 填充节假日-通用配置规则
     */
    private void fillGlobalGeneralRule(
            @NonNull MidCalendarGlobalRuleDTO target,
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse calendarRule,
            @NonNull MidCalendarDateTagTypeEnum tagTypeEnum,
            MidCalendarDateTagValueEnum tagValueEnum) {
        if (calendarRule.getCalLevel() == 1 && tagValueEnum != null) {
            if (tagTypeEnum == MidCalendarDateTagTypeEnum.HOLIDAY
                    && tagValueEnum == MidCalendarDateTagValueEnum.MINOR_VACATION) {
                List<String> periodContents = calendarRule.getPeriodContents();
                if (CollectionUtils.isEmpty(periodContents)) {
                    LogExecutor.rpc(queryParam, calendarRule).warn(log, LOG_TAG, "节假日全局配置-通用配置日期范围");
                    return;
                }
                LinkedHashSet<Range<MonthDay>> dateRange = analyzeMonthDayPeriodContents(periodContents,
                        queryParam, calendarRule, LocaleMessageUtil.getMessage("key_holiday_general_config"));
                if (CollectionUtils.isNotEmpty(dateRange)) {
                    target.setHolidayGeneralConfiguration(dateRange);
                }
            }
        }
    }

    /**
     * @author weiyizhong
     * @Company yushu.biz
     * 　　 * @date 2026/1/4 17:54
     * 　　 * @description: 填充节假日-特殊配置规则
     */
    private void fillGlobalSpecialConfRule(
            @NonNull MidCalendarGlobalRuleDTO target,
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse calendarRule,
            @NonNull MidCalendarDateTagTypeEnum tagTypeEnum,
            MidCalendarDateTagValueEnum tagValueEnum) {
        if (calendarRule.getCalLevel() == 2 && tagValueEnum != null) {
            if (tagTypeEnum == MidCalendarDateTagTypeEnum.HOLIDAY
                    && tagValueEnum == MidCalendarDateTagValueEnum.MINOR_VACATION) {
                String beginDate = calendarRule.getBeginDate();
                String endDate = calendarRule.getEndDate();
                if (StringUtils.isEmpty(beginDate) || StringUtils.isEmpty(beginDate)) {
                    LogExecutor.rpc(queryParam, calendarRule).warn(log, LOG_TAG, "节假日全局配置-特殊配置日期范围");
                    return;
                }
                Range<LocalDate> keyHolidayGeneral = analyzeLocalDates(beginDate, endDate,
                        queryParam, calendarRule, LocaleMessageUtil.getMessage("key_holiday_special_config"));
                if (CollectionUtils.isNotEmpty(target.getHolidaySpecialConfiguration())) {
                    target.getHolidaySpecialConfiguration().add(keyHolidayGeneral);
                } else {
                    LinkedHashSet<Range<LocalDate>> holidaySpecialConfiguration = new LinkedHashSet<>();
                    holidaySpecialConfiguration.add(keyHolidayGeneral);
                    target.setHolidaySpecialConfiguration(holidaySpecialConfiguration);
                }
            }
        }
    }

    /**
     * @author weiyizhong
     * @Company yushu.biz
     * 　　 * @date 2026/1/6 15:11
     * 　　 * @description: 星期号的组装
     */
    private void fillGlobalWeekConfRule(
            @NonNull MidCalendarGlobalRuleDTO target,
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse calendarRule,
            @NonNull MidCalendarDateTagTypeEnum tagTypeEnum,
            MidCalendarDateTagValueEnum tagValueEnum) {
        if (calendarRule.getCalLevel() == 1 && tagValueEnum != null) {
            if (tagTypeEnum == MidCalendarDateTagTypeEnum.WEEK
                    && tagValueEnum == MidCalendarDateTagValueEnum.WEEK) {
                List<MidCalendarRuleResponse.ScheduleContent> scheduleContents = calendarRule.getScheduleContents();

                if (CollectionUtils.isEmpty(scheduleContents)) {
                    LogExecutor.rpc(queryParam, calendarRule).warn(log, LOG_TAG, "星期号全局配置日期范围");
                    return;
                }
                LinkedHashSet<BusinessTimeRangeParam.WeekNumberParam> weekNumberParamList = Sets.newLinkedHashSet();

                // 保存的时候工作里会将其合并实际取第一个元素即可
                MidCalendarRuleResponse.ScheduleContent scheduleContent = scheduleContents.get(0);
                List<MidCalendarRuleResponse.ScheduleContent.TimeContent> timeContents = scheduleContent.getTimeContents();
                if (CollectionUtils.isEmpty(timeContents)) {
                    return;
                }
                for (MidCalendarRuleResponse.ScheduleContent.TimeContent timeContent : timeContents) {
                    BusinessTimeRangeParam.WeekNumberParam weekNumberParam = new BusinessTimeRangeParam.WeekNumberParam();
                    List<String> timeList = timeContent.getTimeContent();
                    if (CollectionUtils.isNotEmpty(timeList)) {
                        for (String time : timeList) {
                            String[] split = time.split(",");
                            LocalTime beginTime = LocalTime.parse(split[0], OPENING_TIME_FORMATTER);
                            LocalTime endTime = LocalTime.parse(split[1], OPENING_TIME_FORMATTER);
                            if (CollectionUtils.isEmpty(weekNumberParam.getWeekNumberBusinessTimeRangeList())) {
                                LinkedHashSet<Range<LocalTime>> weekNumberBusinessTimeRangeList = Sets.newLinkedHashSet();
                                weekNumberBusinessTimeRangeList.add(Range.between(beginTime, endTime));
                                weekNumberParam.setWeekNumberBusinessTimeRangeList(weekNumberBusinessTimeRangeList);
                            } else {
                                weekNumberParam.getWeekNumberBusinessTimeRangeList().add(Range.between(beginTime, endTime));
                            }
                        }
                    }
                    // 星期
                    String periodDate = timeContent.getPeriodDate();
                    if (StringUtils.isNotBlank(periodDate)) {
                        String[] splits = periodDate.split(",");
                        for (String split : splits) {
                            if (CollectionUtils.isEmpty(weekNumberParam.getWeekdays())) {
                                LinkedHashSet<DayOfWeek> weekdays = Sets.newLinkedHashSet();
                                weekdays.add(midCalendarConvert.toCalendarDayOfWeek(split));
                                weekNumberParam.setWeekdays(weekdays);
                            } else {
                                weekNumberParam.getWeekdays().add(midCalendarConvert.toCalendarDayOfWeek(split));
                            }
                        }
                    }
                    weekNumberParamList.add(weekNumberParam);
                }
                target.setDateTagType(tagTypeEnum);
                if (CollectionUtils.isEmpty(target.getWeekNumberParamList())) {
                    target.setWeekNumberParamList(weekNumberParamList);
                } else {
                    target.getWeekNumberParamList().addAll(weekNumberParamList);
                }
            }
        }
    }

    /**
     * 填充全局工作历工休日|营业时间规则
     *
     * @param target       目标源
     * @param queryParam   查询参数
     * @param calendarRule 工作历配置
     * @param tagTypeEnum  工作历标签类型
     * @param tagValueEnum 工作历标签值
     * @since v5.7.0
     */
    private void fillGlobalWorkdayRule(
            @NonNull MidCalendarGlobalRuleDTO target,
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse calendarRule,
            @NonNull MidCalendarDateTagTypeEnum tagTypeEnum,
            MidCalendarDateTagValueEnum tagValueEnum) {
        if (calendarRule.getCalLevel() == 1) {
            if (tagTypeEnum == MidCalendarDateTagTypeEnum.WORKDAY && tagValueEnum != null) {
                MidCalendarWorkDayTypeEnum workDayTypeEnum = tagValueEnum.toWorkDayTypeEnum();
                switch (workDayTypeEnum) {
                    case WORKDAY:
                        target.setGlobalWeekDays(parseGlobalWorkDay(
                                target.getGlobalWeekDays(), queryParam, calendarRule));
                        target.setGlobalBusinessTimeRangeListOnWeekDays(parseGlobalOpenCloseTimeRangeList(
                                target.getGlobalBusinessTimeRangeListOnWeekDays(), queryParam, calendarRule));
                        if (CollectionUtils.isNotEmpty(target.getGlobalBusinessTimeRangeListOnWeekDays()))
                            target.setDateTagType(tagTypeEnum);
                        break;
                    case DAYOFF:
                        target.setGlobalNonWeekDays(parseGlobalWorkDay(
                                target.getGlobalNonWeekDays(), queryParam, calendarRule));
                        target.setGlobalBusinessTimeRangeListOnNonWeekDays(parseGlobalOpenCloseTimeRangeList(
                                target.getGlobalBusinessTimeRangeListOnNonWeekDays(), queryParam, calendarRule));
                        if (CollectionUtils.isNotEmpty(target.getGlobalBusinessTimeRangeListOnNonWeekDays()))
                            target.setDateTagType(tagTypeEnum);
                        break;
                    default:
                }
            } else if (tagTypeEnum == MidCalendarDateTagTypeEnum.DEFAULT) {
                target.setDefaultBusinessTimeRangeList(parseGlobalOpenCloseTimeRangeList(
                        target.getDefaultBusinessTimeRangeList(), queryParam, calendarRule));
                if (CollectionUtils.isNotEmpty(target.getDefaultBusinessTimeRangeList()))
                    target.setDateTagType(tagTypeEnum);
                // 新增解析特殊配置(只有特殊对象配置里面才有)
                LinkedHashSet<BusinessTimeRangeParam.SpecialConfigurationContentParam> specialConfigurationContentParamList = parseSpecialConfigurationList(queryParam, calendarRule);
                if (CollectionUtils.isNotEmpty(target.getSpecialConfigurationContentParamList())) {
                    target.getSpecialConfigurationContentParamList().addAll(specialConfigurationContentParamList);
                } else {
                    target.setSpecialConfigurationContentParamList(specialConfigurationContentParamList);
                }
            }
        }
    }

    /**
     * 填充全局空调工况规则
     *
     * @param target       目标源
     * @param queryParam   查询参数
     * @param calendarRule 工作历配置
     * @param tagTypeEnum  工作历标签类型
     * @param tagValueEnum 工作历标签值
     * @since v5.7.0
     */
    private void fillGlobalAcSeasonRule(
            @NonNull MidCalendarGlobalRuleDTO target,
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse calendarRule,
            @NonNull MidCalendarDateTagTypeEnum tagTypeEnum,
            MidCalendarDateTagValueEnum tagValueEnum) {
        if (calendarRule.getCalLevel() == 1 && tagValueEnum != null) {
            if (tagTypeEnum == MidCalendarDateTagTypeEnum.AC_SEASON ||
                    tagTypeEnum == MidCalendarDateTagTypeEnum.AIR_SEASON
            ) {
                List<String> periodContents = calendarRule.getPeriodContents();
                if (CollectionUtils.isEmpty(periodContents)) {
                    LogExecutor.rpc(queryParam, calendarRule)
                            .warn(log, LOG_TAG, "无空调季全局配置日期范围");
                    return;
                }
                LinkedHashSet<Range<MonthDay>> dateRange = analyzeMonthDayPeriodContents(periodContents,
                        queryParam, calendarRule, LocaleMessageUtil.getMessage("key_81f5bcab3d974d88b59a3080f2821a93"));
                if (CollectionUtils.isNotEmpty(dateRange)) {
                    if (tagTypeEnum == MidCalendarDateTagTypeEnum.AC_SEASON) {
                        if (tagValueEnum.toAcSeasonTypeEnum() == null) {
                            return;
                        }
                        Map<MidCalendarAcSeasonTypeEnum, LinkedHashSet<Range<MonthDay>>> globalSeasonDateRangeMap =
                                Optional.ofNullable(target.getGlobalSeasonDateRangeMap()).orElseGet(HashMap::new);
                        MapExecutor.dynamicPutAll(globalSeasonDateRangeMap,
                                tagValueEnum.toAcSeasonTypeEnum(), dateRange, LinkedHashSet::new);
                        target.setGlobalSeasonDateRangeMap(globalSeasonDateRangeMap);
                    } else {
                        Map<MidCalendarDateTagValueEnum, LinkedHashSet<Range<MonthDay>>> globalSeasonDateRangeMap =
                                Optional.ofNullable(target.getGlobalAirSeasonRangeDateRangeMap()).orElseGet(HashMap::new);
                        MapExecutor.dynamicPutAll(globalSeasonDateRangeMap,
                                tagValueEnum, dateRange, LinkedHashSet::new);
                        target.setGlobalAirSeasonRangeDateRangeMap(globalSeasonDateRangeMap);
                    }
                }
            }
        }
    }

    /**
     * 解析全局工休日
     *
     * @param queryParam 查询参数
     * @param response   响应
     * @return 全局工休日
     * @since v5.7.0
     */
    @Nullable
    private LinkedHashSet<DayOfWeek> parseGlobalWorkDay(
            @Nullable LinkedHashSet<DayOfWeek> source,
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse response) {
        List<String> periodContents = response.getPeriodContents();
        if (CollectionUtils.isEmpty(periodContents)) {
            LogExecutor.rpc(queryParam, response).warn(log, LOG_TAG, "无循环周期内容");
            return null;
        }
        if (Objects.isNull(source)) {
            source = new LinkedHashSet<>();
        }
        source.addAll(periodContents.stream().map(midCalendarConvert::toCalendarDayOfWeek).collect(Collectors.toSet()));
        return source;
    }

    /**
     * 解析全局营业时间范围
     *
     * @param source     数据源
     * @param queryParam 查询参数
     * @param response   响应
     * @return 全局营业时间范围
     * @since v5.7.0
     */
    @Nullable
    private LinkedHashSet<Range<LocalTime>> parseGlobalOpenCloseTimeRangeList(
            @Nullable LinkedHashSet<Range<LocalTime>> source,
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse response) {
        List<MidCalendarRuleResponse.ScheduleContent> scheduleContents = response.getScheduleContents();
        if (CollectionUtils.isEmpty(scheduleContents)) {
            LogExecutor.rpc(queryParam, response).info(log, LOG_TAG, "无计划内容");
            return null;
        }
        LinkedHashSet<Range<LocalTime>> timeRange = new LinkedHashSet<>();
        for (MidCalendarRuleResponse.ScheduleContent scheduleContent : scheduleContents) {
            String timeType = scheduleContent.getTimeType();
            List<MidCalendarRuleResponse.ScheduleContent.TimeContent> timeContents = scheduleContent.getTimeContents();
            if (!StringUtils.equals(timeType, MidCalendarRuleResponse.ScheduleContent.TIME_TYPE_OPENING)) {
                LogExecutor.rpc(queryParam, response).warn(log, LOG_TAG, "未知的时间类型");
                continue;
            }
            if (CollectionUtils.isEmpty(timeContents)) {
                LogExecutor.rpc(queryParam, response).warn(log, LOG_TAG, "无全局配置营业时间");
                continue;
            }
            List<String> timeList = new ArrayList<>();
            for (MidCalendarRuleResponse.ScheduleContent.TimeContent timeContent : timeContents) {
                if ("1".equals(timeContent.getType()) && CollectionUtils.isNotEmpty(timeContent.getTimeContent())) {
                    timeList.addAll(timeContent.getTimeContent());
                }
            }
            try {
                for (String time : timeList) {
                    String[] split = time.split(",");
                    LocalTime beginTime = LocalTime.parse(split[0], OPENING_TIME_FORMATTER);
                    LocalTime endTime = LocalTime.parse(split[1], OPENING_TIME_FORMATTER);
                    timeRange.add(Range.between(beginTime, endTime));
                }
            } catch (Exception e) {
                LogExecutor.rpc(queryParam, response).warn(log, LOG_TAG, "全局配置营业时间解析失败", e);
                throw e;
            }
            if (CollectionUtils.isNotEmpty(timeRange)) {
                if (Objects.isNull(source)) {
                    source = new LinkedHashSet<>();
                }
                source.addAll(timeRange);
            }
        }
        return source;
    }

    /**
     * @author weiyizhong
     * @Company yushu.biz
     * 　　 * @date 2026/1/7 14:28
     * 　　 * @description: 解析特殊配置
     */
    private LinkedHashSet<BusinessTimeRangeParam.SpecialConfigurationContentParam> parseSpecialConfigurationList(
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse response) {
        LinkedHashSet<BusinessTimeRangeParam.SpecialConfigurationContentParam> result = new LinkedHashSet<>();
        List<MidCalendarRuleResponse.ScheduleContent> scheduleContents = response.getScheduleContents();
        if (CollectionUtils.isEmpty(scheduleContents)) {
            LogExecutor.rpc(queryParam, response).info(log, LOG_TAG, "无计划内容");
            return result;
        }
        for (MidCalendarRuleResponse.ScheduleContent scheduleContent : scheduleContents) {
            String timeType = scheduleContent.getTimeType();
            List<MidCalendarRuleResponse.ScheduleContent.TimeContent> timeContents = scheduleContent.getTimeContents();
            if (!StringUtils.equals(timeType, MidCalendarRuleResponse.ScheduleContent.TIME_TYPE_OPENING)) {
                LogExecutor.rpc(queryParam, response).warn(log, LOG_TAG, "未知的时间类型");
                continue;
            }
            if (CollectionUtils.isEmpty(timeContents)) {
                LogExecutor.rpc(queryParam, response).warn(log, LOG_TAG, "无特殊配置营业时间");
                continue;
            }
            try {
                for (MidCalendarRuleResponse.ScheduleContent.TimeContent timeContent : timeContents) {
                    if ("0".equals(timeContent.getType())) {
                        BusinessTimeRangeParam.SpecialConfigurationContentParam specialConfigurationContentParam = new BusinessTimeRangeParam.SpecialConfigurationContentParam();
                        if (CollectionUtils.isNotEmpty(timeContent.getTimeContent())) {
                            // 处理营业时间
                            List<String> timeContentTime = timeContent.getTimeContent();
                            if (CollectionUtils.isNotEmpty(timeContentTime)) {
                                LinkedHashSet<Range<LocalTime>> specialConfigurationContentBusinessTimeRangeList = new LinkedHashSet<>();
                                for (String time : timeContentTime) {
                                    String[] split = time.split(",");
                                    LocalTime beginTime = LocalTime.parse(split[0], OPENING_TIME_FORMATTER);
                                    LocalTime endTime = LocalTime.parse(split[1], OPENING_TIME_FORMATTER);
                                    specialConfigurationContentBusinessTimeRangeList.add(Range.between(beginTime, endTime));
                                }
                                specialConfigurationContentParam.setSpecialConfigurationContentBusinessTimeRangeList(specialConfigurationContentBusinessTimeRangeList);
                            }
                        }
                        // 处理时间段
                        String periodDate = timeContent.getPeriodDate();
                        if (StringUtils.isNotBlank(periodDate)) {
                            String[] split = periodDate.split(",");
                            specialConfigurationContentParam.setTimePeriod(analyzeLocalDates(split[0], split[1],
                                    queryParam, response, LocaleMessageUtil.getMessage("key_special_config")));
                            // 有时间段不一定有营业时间，但是有营业时间一定有时间段
                            result.add(specialConfigurationContentParam);
                        }
                    }
                }
            } catch (Exception e) {
                LogExecutor.rpc(queryParam, response).warn(log, LOG_TAG, "全局配置-特殊配置解析失败", e);
                throw e;
            }
        }
        return result;
    }

    /**
     * 解析循环周期内容字符串
     *
     * @param periodContents 循环周期内容
     * @param queryParam     查询参数
     * @param response       响应
     * @param errorLogTag    异常日志标签
     * @return 循环周期内容
     * @since v5.7.0
     */
    private LinkedHashSet<Range<MonthDay>> analyzeMonthDayPeriodContents(
            @NonNull List<String> periodContents,
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse response,
            @NonNull String errorLogTag) {
        LinkedHashSet<Range<MonthDay>> dateRange = new LinkedHashSet<>();
        try {
            for (String periodContent : periodContents) {
                String[] split = periodContent.split(",");
                MonthDay beginMonthDay = MonthDay.parse(split[0].trim(), DatePattern.NORM_MONTHDAY_FORMATTER);
                MonthDay endMonthDay = MonthDay.parse(split[1].trim(), DatePattern.NORM_MONTHDAY_FORMATTER);
                dateRange.add(Range.between(beginMonthDay, endMonthDay));
            }
        } catch (Exception e) {
            LogExecutor.rpc(queryParam, response).warn(log, LOG_TAG, errorLogTag, e);
            throw e;
        }
        return dateRange;
    }

    private Range<LocalDate> analyzeLocalDates(
            @NonNull String beginDate,
            @NonNull String endDate,
            @NonNull MidCalendarRuleQueryParam queryParam,
            @NonNull MidCalendarRuleResponse response,
            @NonNull String errorLogTag) {
        try {
            LocalDate beginLocalDate = LocalDate.parse(beginDate.trim(), DatePattern.NORM_DATE_FORMATTER);
            LocalDate endLocalDate = LocalDate.parse(endDate.trim(), DatePattern.NORM_DATE_FORMATTER);
            return localDateRange(beginLocalDate, endLocalDate);
        } catch (Exception e) {
            LogExecutor.rpc(queryParam, response).warn(log, LOG_TAG, errorLogTag, e);
            throw e;
        }
    }

    @Override
    public Logger log() {
        return log;
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }

    @Override
    public MidCalendarOpenTimeResponse getMidCalendarOpenTime(String projectId, MidCalendarOpenTimeQueryParam param) {
        CustomerResponse<List<MidCalendarOpenTimeResponse>> response = midCalendarClient.getOpenTime(DtpRwdCommonParam.instance(ContextUtil.getGroupCode(), projectId), param);
        List<MidCalendarOpenTimeResponse> midCalendarOpenTimeResponses = CustomerResponse.handleResult(response);
        if (CollectionUtils.isNotEmpty(midCalendarOpenTimeResponses)) {
            return midCalendarOpenTimeResponses.get(0);
        }
        return null;
    }

    @Override
    public MidCalendarOpenTimeResponse getMidCalendarOpenTimeByDay(MidCalendarQueryParam param, LocalDate workDate) {
        MidCalendarOpenTimeQueryParam queryParam = new MidCalendarOpenTimeQueryParam();
        BeanUtils.copyProperties(param, queryParam);
        String time = workDate.format(DateTimeFormatter.ofPattern(cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN));
        queryParam.setWorkDateStart(time);
        queryParam.setWorkDateEnd(time);
        queryParam.setDictType(MidCalendarScheduleTagTypeEnum.OPENING);
        CustomerResponse<List<MidCalendarOpenTimeResponse>> response =
                midCalendarClient.getOpenTime(DtpRwdCommonParam.instance(ContextUtil.getGroupCode(), ContextUtil.getProjectId()), queryParam);
        List<MidCalendarOpenTimeResponse> list = CustomerResponse.handleResult(response);
        if (CollectionUtil.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Map<String, MidCalendarOpenTimeResponse> getMidCalendarOpenTimeMap(String projectId,
                                                                              Set<String> objects, MidCalendarOpenTimeQueryParam param) {
        StopWatch stopWatch = StopWatch.create("getMidCalendarOpenTimeMap");
        if (CollectionUtils.isEmpty(objects)) {
            return null;
        }
        stopWatch.start("查询营业时间，数量：" + objects.size());
        Map<String, MidCalendarOpenTimeResponse> midCalendarOpenTimeResponseMap = new HashMap<>();
        for (String object : objects) {
            param.setObjId(object);
            MidCalendarOpenTimeResponse midCalendarOpenTime = getMidCalendarOpenTime(projectId, param);
            midCalendarOpenTimeResponseMap.put(object, midCalendarOpenTime);
        }
        stopWatch.stop();
        logProjectInstance(ContextUtil.getProjectId()).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return midCalendarOpenTimeResponseMap;
    }

    @Override
    public Set<String> getObjectsFilter(List<String> objIds) {

        if (CollectionUtils.isEmpty(objIds)) {
            return new HashSet<>();
        }

        // 计算分组数量，每组最多100个元素
        int groupCount = (int) Math.ceil((double) objIds.size() / 100);
        List<List<String>> groups = new ArrayList<>();

        // 将objIds分割成多个组，每组最多100个元素
        for (int i = 0; i < groupCount; i++) {
            int startIndex = i * 100;
            int endIndex = Math.min((i + 1) * 100, objIds.size());
            groups.add(objIds.subList(startIndex, endIndex));
        }

        // 创建并发任务列表，每个任务对应一组objIds的查询
        List<CompletableFuture<CustomerResponse<List<String>>>> futures = groups.stream()
                .map(group -> CompletableFuture.supplyAsync(() ->
                        midCalendarClient.getObjectsFilter(DtpRwdCommonParam.instance(ContextUtil.getGroupCode(), ContextUtil.getProjectId()), group)))
                .collect(Collectors.toList());

        // 等待所有并发任务完成，并收集结果
        List<CustomerResponse<List<String>>> responses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // 从所有响应中提取内容（即查询到的String列表）并合并成一个总的列表返回
        return responses.stream()
                .filter(Objects::nonNull)
                .flatMap(response -> response.getContent().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public List<MidCalendarOpenTimeProjectResponse> getOpenTimeProject(LocalDate workDate) {
        if (workDate == null) {
            return Collections.emptyList();
        }
        return query(false, ContextUtil.getGroupCode(), ContextUtil.getProjectId(), LocaleMessageUtil.getMessage("key_801d9846702748a5af2cd9363609ff86"), workDate,
                () -> midCalendarClient.listOpenTimeProject(DtpRwdCommonParam.instance(ContextUtil.getGroupCode(), ContextUtil.getProjectId()),
                        new MidCalendarOpenTimeProjectQueryParam(workDate)), ArrayList::new);
    }

    @Override
    public List<DemandInstantiationDateTagVO> listInstantiationDateTag(
            @NonNull LocalDate beginDate, @NonNull LocalDate endDate) {
        String projectId = ContextUtil.getProjectId();

        MidCalendarDayDateTagQueryParam queryParam = new MidCalendarDayDateTagQueryParam();
        queryParam.setObjId(projectId);
        queryParam.setBeginDate(beginDate);
        queryParam.setEndDate(endDate);
        List<MidCalendarDayDateTagResponse> responses =
                listCalendarDayDateTag(projectId, queryParam);
        if (CollectionUtils.isEmpty(responses)) {
            LogExecutor.project(projectId).warn(log, LOG_TAG, "工作历无日期标签", Arrays.asList(
                    Pair.of("beginDate", beginDate),
                    Pair.of("endDate", endDate)
            ));
            return Lists.newArrayList();
        }

        List<DemandInstantiationDateTagVO> result = new ArrayList<>();
        for (MidCalendarDayDateTagResponse response : responses) {
            LocalDateTime currentDay = response.getCurrentDay();
            List<String> tagList = response.getTagList();

            List<MidCalendarDateTagValueEnum> calendarTags = new ArrayList<>();
            for (String tag : tagList) {
                MidCalendarDateTagValueEnum tagValue = BaseEnum.analyze(tag,
                        MidCalendarDateTagValueEnum.class, false);
                if (Objects.isNull(tagValue)) {
                    LogExecutor.project(projectId).warn(log, LOG_TAG, "无法识别的工作历日期标签值", Arrays.asList(
                            Pair.of("currentDay", calendarTags),
                            Pair.of("tagValue", tag))
                    );
                    continue;
                }
                calendarTags.add(tagValue);
            }

            DemandInstantiationDateTagVO resultItem = new DemandInstantiationDateTagVO();
            resultItem.setDate(currentDay.toLocalDate());
            resultItem.setCalendarTags(calendarTags);
            result.add(resultItem);
        }
        return result;
    }

    @Override
    public Set<MidCalendarDateTagValueEnum> listAirDateTagByRule() {
        String projectId = ContextUtil.getProjectId();
        MidCalendarGlobalRuleDTO globalCalendarRule = getOrInitObjectGlobalCalendarRule(projectId,
                DemandInstanceTypeEnum.PROJECT, projectId,
                MidCalendarDateTagTypeEnum.AIR_SEASON, null);
        if (Objects.isNull(globalCalendarRule)) {
            return Sets.newHashSet();
        }
        Map<MidCalendarDateTagValueEnum, LinkedHashSet<Range<MonthDay>>> globalAirSeasonRangeDateRangeMap =
                globalCalendarRule.getGlobalAirSeasonRangeDateRangeMap();
        if (MapUtils.isEmpty(globalAirSeasonRangeDateRangeMap)) {
            return Sets.newHashSet();
        }
        Set<MidCalendarDateTagValueEnum> result = new HashSet<>();
        globalAirSeasonRangeDateRangeMap.forEach((dateTag, ranges) -> {
            if (CollectionUtils.isNotEmpty(ranges)) {
                result.add(dateTag);
            }
        });
        return result;
    }
}
