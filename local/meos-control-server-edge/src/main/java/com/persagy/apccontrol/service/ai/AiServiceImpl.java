package com.persagy.apccontrol.service.ai;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseEventStrategy;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandInstanceRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandInstanceConfigRepositoryQueryParam;
import com.persagy.apccontrol.basic.structure.performance.StopWatch;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.common.model.param.ai.AiDemandConfigQueryParam;
import com.persagy.apccontrol.common.model.vo.ai.AiDemandConfigDetailVO;
import com.persagy.apccontrol.common.model.vo.ai.AiDemandConfigVO;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.DateTypeEnum;
import com.persagy.validate.utility.executor.DateTimeRangeExecutor;
import com.persagy.validate.utility.executor.MapExecutor;
import com.persagy.validate.utility.pattern.DatePattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 运行智能体服务实现
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AiServiceImpl implements AiService {

    private final DemandConfigGateway<DemandConfigAirconditioningValueObject> demandConfigGateway;
    private final DemandInstanceRepository demandInstanceRepository;
    private final DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;
    private final DtpRwdClientService dtpRwdClientService;
    private final MidCalendarClientService midCalendarClientService;
    private final SseHandlerContext sseHandlerContext;

    @Override
    public AiDemandConfigVO getDemandConfig(@NonNull AiDemandConfigQueryParam param) {
        LocalDateTime next15minute = DateUtils.getNextNearest15Minute(param.getComputeTime());
        LocalDate beginDate = next15minute.toLocalDate();
        LocalDate endDate = LocalTime.MIN.equals(next15minute.toLocalTime())
                ? beginDate : beginDate.plusDays(1);
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        Set<String> spaceIds = param.getSpaceIds();
        Integer querySpaceCount = param.getQuerySpaceCount();
        if (templateType != DemandTemplateTypeEnum.AIRCONDITIONING) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_563fab2c7ff44b8ba0a4b64aa8063f2c"));
        }
        AtomicReference<List<DtpRwdSpaceDTO>> spaces = new AtomicReference<>(Collections.emptyList());
        Map<LocalDate, Map<String, DemandConfigAirconditioningEnvironmentQualityObject>>
                dateAndConfigs = new ConcurrentHashMap<>();
        Map<LocalDate, DemandConfigAirconditioningCustomDTO> dateAndValueSelect = new ConcurrentHashMap<>();
        Map<LocalDate, List<MidCalendarDateTagValueEnum>> dateAndSeasonTypes = new HashMap<>();
        StopWatch stopWatch = StopWatch.create();
        if (CollectionUtils.isNotEmpty(spaceIds)) {
            // 黑名单中的空间
            stopWatch.start("查询黑名单空间");
            Set<String> inBlacklistSpaceIds = queryInBlacklistSpaceIds(spaceIds);
            stopWatch.stop();
            if (CollectionUtils.isNotEmpty(inBlacklistSpaceIds)) {
                for (String inBlacklistSpaceId : inBlacklistSpaceIds) {
                    spaceIds.remove(inBlacklistSpaceId);
                }
            }
            // 存在查询数量先进行截取
            spaceIds = Objects.nonNull(querySpaceCount)
                    ? spaceIds.stream().limit(querySpaceCount).collect(Collectors.toSet())
                    : spaceIds;
        }
        Set<String> finalSpaceIds = spaceIds;
        List<StopWatch.TaskGroup> taskGroups = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(finalSpaceIds)) {
            taskGroups.add(new StopWatch.TaskGroup("查询空间", () -> spaces.set(dtpRwdClientService.listSpace(
                    new DtpRwdObjectQueryParam().setId(finalSpaceIds).addWithColumn(
                            DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName)))));
        }
        taskGroups.add(new StopWatch.TaskGroup("查询工作历日期标签", () -> midCalendarClientService
                .listInstantiationDateTag(beginDate, endDate).forEach(item ->
                        dateAndSeasonTypes.put(item.getDate(), item.getCalendarTags()))));
        DateTimeRangeExecutor.generateDate(DateTypeEnum.DAY, beginDate, endDate, true, date -> {
            BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
            templateParam.setTemplateType(templateType);
            templateParam.setDate(date);
            Optional<DemandTemplate> templateOpt = demandConfigGateway.getByTemplate(templateParam);
            if (!templateOpt.isPresent()) {
                return;
            }
            DemandTemplate template = templateOpt.get();
            Long templateId = template.getId();
            templateParam.setTemplateId(templateId);
            String dateString = date.format(DatePattern.NORM_DATE_FORMATTER);
            if (CollectionUtils.isNotEmpty(finalSpaceIds)) {
                taskGroups.add(new StopWatch.TaskGroup("查询需求配置" + dateString, () -> {
                    DemandConfigByInstanceBatchQueryParam batchQueryParam = new DemandConfigByInstanceBatchQueryParam();
                    batchQueryParam.setDate(date);
                    batchQueryParam.setTemplateId(templateId);
                    batchQueryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(
                            DemandInstanceTypeEnum.SPACE, finalSpaceIds));
                    List<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> configs =
                            demandConfigGateway.listConfigsByInstances(batchQueryParam);
                    if (CollectionUtils.isEmpty(configs)) {
                        return;
                    }
                    for (DemandConfigAggregate<DemandConfigAirconditioningValueObject> config : configs) {
                        DemandConfigIdentifier identifier = config.getIdentifier();
                        String instanceId = identifier.getInstance().getInstanceId();
                        DemandConfigValueObject<DemandConfigAirconditioningValueObject> finalConfig = config.getFinalConfig();
                        if (Objects.isNull(finalConfig)) {
                            continue;
                        }
                        DemandConfigAirconditioningValueObject globalConfig = finalConfig.getGlobalConfig();
                        if (Objects.isNull(globalConfig)) {
                            continue;
                        }
                        DemandConfigAirconditioningEnvironmentQualityObject environmentQuality =
                                globalConfig.getEnvironmentQuality();
                        if (Objects.isNull(environmentQuality)) {
                            continue;
                        }
                        MapExecutor.dynamicPut(dateAndConfigs, date, instanceId,
                                environmentQuality, ConcurrentHashMap::new);
                    }
                }));
            }
            taskGroups.add(new StopWatch.TaskGroup("查询价值选择" + dateString, () -> {
                DemandConfigAirconditioningCustomDTO configData =
                        demandConfigAirconditioningCustomService.getCustomConfig(templateParam);
                if (Objects.isNull(configData)) {
                    return;
                }
                dateAndValueSelect.put(date, configData);
            }));
        });
        stopWatch.addAsyncTaskGroup(LocaleMessageUtil.getMessage("key_278ee8c4ee13457d92a409c656924615"), taskGroups);
        Map<LocalDate, AiDemandConfigDetailVO> details = new LinkedHashMap<>();
        DateTimeRangeExecutor.generateDate(DateTypeEnum.DAY, beginDate, endDate, true, date -> {
            List<MidCalendarDateTagValueEnum> dateTags = dateAndSeasonTypes.get(date);
            Map<String, DemandConfigAirconditioningEnvironmentQualityObject> spaceIdAndConfig = dateAndConfigs.get(date);
            DemandConfigAirconditioningCustomDTO valueSelect = dateAndValueSelect.get(date);
            DemandConfigAirconditioningCustomDTO finalValueSelect = null;
            if (Objects.nonNull(valueSelect)) {
                finalValueSelect = new DemandConfigAirconditioningCustomDTO();
                finalValueSelect.setValueSelectType(valueSelect.getValueSelectType());
                finalValueSelect.setValueSelectConfig(valueSelect.getValueSelectConfig());
            }
            AiDemandConfigDetailVO detail = new AiDemandConfigDetailVO();
            detail.setValueSelect(finalValueSelect);
            if (MapUtils.isNotEmpty(spaceIdAndConfig)) {
                detail.setSpaceIdAndConfig(spaceIdAndConfig);
            }
            if (CollectionUtils.isNotEmpty(dateTags)) {
                detail.setDateTags(dateAndSeasonTypes.get(date));
            }
            details.put(date, detail);
        });
        AiDemandConfigVO result = new AiDemandConfigVO();
        result.setNext15minute(next15minute);
        result.setDetails(details);
        result.fillTexts(spaces.get());
        result.fillValueSelectText();
        globalStopWatch().addTaskGroup(stopWatch);
        return result;
    }

    @Override
    public boolean notifyCalcStatus(@NonNull JsonNode param) {
        SseEventStrategy strategy = sseHandlerContext.getStrategy(SseEmitterEventEnum.AI_NOTIFY_CALC_STATUS);
        strategy.doOperation(ContextUtil.getProjectId(), SubscribeTypeEnum.AI, param);
        return true;
    }

    /**
     * 查询黑名单空间
     *
     * @param spaceIds 空间ID
     * @return 黑名单空间ID
     * @since 7.250702.0
     */
    private Set<String> queryInBlacklistSpaceIds(@NonNull Set<String> spaceIds) {
        DemandInstanceConfigRepositoryQueryParam instanceConfigQueryParam = new DemandInstanceConfigRepositoryQueryParam();
        instanceConfigQueryParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        instanceConfigQueryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
        instanceConfigQueryParam.setInstanceIds(spaceIds);
        instanceConfigQueryParam.setFilterConfig(new DemandInstanceConfigValueObject().setInBlacklisted(true));
        instanceConfigQueryParam.addColumns(DemandInstanceConfig::getDemandInstanceId);
        return demandInstanceRepository.listConfig(instanceConfigQueryParam).stream()
                .map(DemandInstanceConfig::getDemandInstanceId)
                .collect(Collectors.toSet());
    }

    @Override
    public String logTag() {
        return "运行智能体";
    }
}
