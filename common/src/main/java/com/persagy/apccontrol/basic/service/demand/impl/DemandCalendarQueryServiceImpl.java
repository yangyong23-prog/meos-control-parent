package com.persagy.apccontrol.basic.service.demand.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplateCalendar;
import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.DemandInstantiationDateTagQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateDateSettingQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateTypeQueryParam;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandInstantiationDateTagVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateTypeVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateCalendarService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateQueryDaoParam;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandCalendarConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTagEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 需求管理-需求日历模版服务实现
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandCalendarQueryServiceImpl implements DemandCalendarQueryService {

    private final MidCalendarClientService midCalendarClientService;
    private final DemandCalendarConverter demandCalendarConverter;
    private final DemandTemplateService demandTemplateService;
    private final DemandTemplateCalendarService demandTemplateCalendarService;

    @Override
    public boolean isApplyTemplateByDate(@NonNull Long templateId, @NonNull LocalDate date) {
        Optional<DemandTemplate> templateOpt = demandTemplateService.getByIdIgnoreOpt(templateId);
        if (!templateOpt.isPresent()) {
            return false;
        }
        DemandTemplate template = templateOpt.get();
        DemandTemplateTypeEnum demandTemplateType = template.getDemandTemplateType();
        DemandTemplateDateSettingVO dateSetting = getDateSetting(demandTemplateType, date);
        if (Objects.isNull(dateSetting)) {
            return false;
        }
        DemandTemplateVO relTemplate = dateSetting.getRelTemplate();
        return Objects.equals(templateId, relTemplate.getTemplateId());
    }

    @Override
    public DemandTemplateVO getTemplate(@NonNull DemandTemplateQueryParam param) {
        DemandTemplateBatchQueryParam templateQueryParam = new DemandTemplateBatchQueryParam();
        templateQueryParam.setTemplateId(param.getTemplateId());
        templateQueryParam.setTemplateType(param.getTemplateType());
        templateQueryParam.setTemplateTag(param.getTemplateTag());
        templateQueryParam.setEnabled(param.getEnabled());
        List<DemandTemplateVO> templates = listTemplate(templateQueryParam);
        if (CollectionUtils.isEmpty(templates)) {
            return null;
        }
        return IterableUtils.first(templates);
    }

    @Override
    @Nullable
    public DemandTemplateDateSettingVO getDateSetting(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date) {
        DemandTemplateDateSettingQueryParam templateDateSettingParam = new DemandTemplateDateSettingQueryParam();
        templateDateSettingParam.setProjectId(ContextUtil.getProjectId());
        templateDateSettingParam.setTemplateType(templateType);
        templateDateSettingParam.setBeginDate(date);
        templateDateSettingParam.setEndDate(date.plusDays(1));

        List<DemandTemplateDateSettingVO> result = listDateSetting(templateDateSettingParam);
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return IterableUtils.first(result);
    }

    @Override
    public List<DemandTemplateTypeVO> listTemplateType(@NonNull DemandTemplateTypeQueryParam param) {
        // 临时手动添加项目ID, 避免之前前端没传项目ID的情况, 确认前端传了可以去掉
        ContextUtil.setProjectId(param.getProjectId());
        DemandTypeEnum[] demandTypes = Objects.nonNull(param.getDemandType())
                ? new DemandTypeEnum[]{param.getDemandType()}
                : DemandTypeEnum.values();
        List<DemandTemplateTypeVO> result = new ArrayList<>();
        for (DemandTypeEnum demandType : demandTypes) {
            DemandStrategy strategy = demandType.demandStrategy();
            List<DemandTemplateTypeEnum> templateTypes = strategy.listTemplateTypes();
            if (CollectionUtils.isNotEmpty(templateTypes)) {
                result.add(new DemandTemplateTypeVO().setDemandType(demandType)
                        .setProductCode(demandType.getProductCode())
                        .setTemplateTypes(templateTypes.stream()
                                .map(item -> new BaseEnumVO(item.value(), item.getReasonPhrase()))
                                .collect(Collectors.toList())));
            }
        }
        return result;
    }

    @Override
    public List<DemandTemplateVO> listTemplate(@NonNull DemandTemplateBatchQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        String templateTag = param.getTemplateTag();
        Long templateId = param.getTemplateId();
        Boolean enabled = param.getEnabled();
        List<DemandTemplate> dbTemplates = demandTemplateService.list(
                new DemandTemplateQueryDaoParam().setId(templateId)
                        .setDemandTemplateType(templateType)
                        .setDemandTemplateTag(templateTag)
                        .setIsEnabled(enabled));
        return demandCalendarConverter.toTemplateVo(dbTemplates);
    }

    @Override
    public List<DemandInstantiationDateTagVO> listInstantiationDateTag(
            @NonNull DemandInstantiationDateTagQueryParam param) {
        param.checkAndSet();
        LocalDate beginDate = param.getBeginDate();
        LocalDate endDate = param.getEndDate();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        boolean filterByDemandTemplate = param.isFilterByDemandTemplate();

        List<DemandInstantiationDateTagVO> instantiationDateTags =
                midCalendarClientService.listInstantiationDateTag(beginDate, endDate);
        if (CollectionUtils.isEmpty(instantiationDateTags)) {
            return Lists.newArrayList();
        }
        if (filterByDemandTemplate) {
            DemandTemplateBatchQueryParam queryParam = new DemandTemplateBatchQueryParam();
            queryParam.setTemplateType(templateType);
            queryParam.setEnabled(true);
            List<DemandTemplateVO> templates = listTemplate(queryParam);
            List<DemandTemplateTagEnum> allTemplateTags = CollectionUtils.isNotEmpty(templates)
                    ? templates.stream()
                    .map(item -> BaseEnum.analyze(item.getTemplateTag(), DemandTemplateTagEnum.class, false))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            // 移除在需求模版范围内没有的日期标签
            for (DemandInstantiationDateTagVO instantiationDateTag : instantiationDateTags) {
                instantiationDateTag.getCalendarTags().removeIf(dateTagValue -> {
                    DemandTemplateTagEnum templateTag = DemandTemplateTagEnum.create(dateTagValue);
                    // 当工作历标签在需求模版标签范围内, 且不在已启用模版范围中时, 进行移除
                    return Objects.nonNull(templateTag) && !allTemplateTags.contains(templateTag);
                });
            }
        }
        return instantiationDateTags;
    }

    @Override
    public List<DemandTemplateDateSettingVO> listDateSetting(@NonNull DemandTemplateDateSettingQueryParam param) {
        param.checkAndSet();
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        LocalDate beginDate = param.getBeginDate();
        LocalDate endDate = param.getEndDate();

        DemandTemplateCalendarQueryDaoParam queryDaoParam = new DemandTemplateCalendarQueryDaoParam();
        queryDaoParam.setProjectId(projectId);
        queryDaoParam.setDemandType(templateType.toDemandTypeEnum());
        queryDaoParam.setDemandTemplateType(templateType);
        queryDaoParam.setWorkDateRange(Pair.of(beginDate, endDate));
        List<DemandTemplateCalendar> dbTemplateCalendars = demandTemplateCalendarService.list(projectId, queryDaoParam);
        if (CollectionUtils.isEmpty(dbTemplateCalendars)) {
            return Lists.newArrayList();
        }

        List<DemandTemplate> dbTemplates = demandTemplateService.list(
                new DemandTemplateQueryDaoParam().setDemandTemplateType(templateType));
        Map<Long, DemandTemplate> templateIdAndData = dbTemplates.stream().collect(Collectors.toMap(
                BaseAssignIdDO::getId, Function.identity(), (o1, o2) -> o1));

        List<DemandTemplateDateSettingVO> result = new ArrayList<>();
        for (DemandTemplateCalendar dbTemplateCalendar : dbTemplateCalendars) {
            DemandTemplate template = templateIdAndData.get(dbTemplateCalendar.getRelDemandTemplateId());
            if (Objects.nonNull(template)) {
                DemandTemplateDateSettingVO resultItem = new DemandTemplateDateSettingVO();
                resultItem.setDate(dbTemplateCalendar.getWorkDate());
                resultItem.setIsCustomizedSetting(dbTemplateCalendar.getIsCustomizedSetting());
                resultItem.setRelTemplate(demandCalendarConverter.toTemplateVo(template));
                result.add(resultItem);
            }
        }
        return result;
    }

    @Override
    public String logTag() {
        return "需求管理-需求日历查询";
    }
}
