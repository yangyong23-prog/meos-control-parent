package com.persagy.apccontrol.basic.structure.demand.lifecycle.config;

import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarDTO;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarGlobalRuleDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarDayDateTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarDayDateTagResponse;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueSelectValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.DemandConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 空调需求配置生命周期
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigAirconditoningAware implements DemandConfigLifeAware<DemandConfigAirconditioningValueObject> {

    private final MidCalendarClientService midCalendarClientService;
    private final DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;

    @Override
    public void afterAggregateInit(@NonNull List<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> aggregates) {
        // 取出空调需求中的配置聚合根
        aggregates = aggregates.stream()
                .filter(item -> item.getIdentifier().getTemplateType() == DemandTemplateTypeEnum.AIRCONDITIONING)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }

        // 优先处理一次项目级别的配置，填空空调需求--项目级别，自定义的配置数据--价值选择方案、空调工况
        this.fillAirConditioningCustomConfigData(aggregates);

        // 不论是项目级别还是下层实例级别，均需要根据当前的营业日历，重写环境品质温度的配置项
        this.overrideAirConditioningTdbQualityConfigData(aggregates);
    }


    /**
     * 填空空调需求--项目级别，自定义的配置数据--价值选择方案、空调工况
     *
     * @param projectAggregates 当前所有实例的配置数据集合
     */
    private void fillAirConditioningCustomConfigData(List<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> projectAggregates) {
        for (DemandConfigAggregate<DemandConfigAirconditioningValueObject> projectAggregate : projectAggregates) {
            DemandConfigIdentifier identifier = projectAggregate.getIdentifier();
            if (DemandInstanceTypeEnum.PROJECT.equals(identifier.getInstance().getInstanceType()) && identifier.getTemplateId() != null) {
                DemandConfigAnalyzeStrategy<DemandConfigAirconditioningValueObject> configAnalyzeStrategy = identifier.getTemplateType().demandConfigAnalyzeStrategy();
                Supplier<DemandConfigAirconditioningValueObject> defaultConfigAirconditioningValueObjectSupplier = configAnalyzeStrategy.defaultConfig(DemandInstanceTypeEnum.PROJECT);
                // 解析出项目配置最终对象, 当配置对象不存在时新建
                DemandConfigValueObject<DemandConfigAirconditioningValueObject> configValueObject = Optional.ofNullable(projectAggregate.getFinalConfig()).orElseGet(DemandConfigValueObject::new);
                DemandConfigAirconditioningValueObject globalConfig = Optional.ofNullable(configValueObject.getGlobalConfig()).orElseGet(defaultConfigAirconditioningValueObjectSupplier);

                // 填写空调工况以及价值选择方案数据
                DemandConfigAirconditioningCustomDTO configData = this.demandConfigAirconditioningCustomService.getConfigData(identifier.getInstance().getInstanceId(), identifier.getTemplateId().toString(), identifier.getConfigDate());
                if (configData != null) {
                    globalConfig.setValueSelect(new DemandConfigAirconditioningValueSelectValueObject(configData.getValueSelectType()));
                    globalConfig.setWorkCondition(configData.getWorkCondition());
                }

                configValueObject.setGlobalConfig(globalConfig);
                projectAggregate.setConfig(configValueObject);
                break;
            }
        }
    }

    /**
     * 根据当前项目所配置的空调季数据，重写环境品质的配置项
     *
     * @param aggregates 当前所有实例的配置数据集合
     */
    private void overrideAirConditioningTdbQualityConfigData(List<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> aggregates) {
        String projectId = ContextUtil.getProjectId();

        // 1.优先判断是否是历史日期，历史走当日实例化的数据，不根据规则数据进行过滤
        Set<MidCalendarAirSeasonTypeEnum> existsGlobalRuleAirSeasons = new HashSet<>();
        LocalDate configDate = aggregates.get(0).getIdentifier().getConfigDate();
        if (configDate != null && configDate.isBefore(DateUtils.now().toLocalDate())) {
            // 查询项目此日的规则标签数据
            MidCalendarDayDateTagQueryParam queryParam = new MidCalendarDayDateTagQueryParam();
            queryParam.setObjId(projectId);
            queryParam.setBeginDate(configDate);
            queryParam.setEndDate(configDate);
            List<MidCalendarDayDateTagResponse> responses = this.midCalendarClientService.listCalendarDayDateTag(projectId, queryParam);
            List<String> tagList = CollectionUtils.isEmpty(responses) ? null : responses.get(0).getTagList();
            if (CollectionUtils.isNotEmpty(tagList)) {
                for (String tag : tagList) {
                    MidCalendarAirSeasonTypeEnum airSeasonTypeEnum = MidCalendarAirSeasonTypeEnum.analyze(tag);
                    if (airSeasonTypeEnum != null) {
                        existsGlobalRuleAirSeasons.add(airSeasonTypeEnum);
                    }
                }
            }
        } else {
            // 查询项目的空调季规则，重写环境品质数据
            MidCalendarDTO objCalendar = this.midCalendarClientService.getCalendar(projectId, new MidCalendarQueryParam().setObjId(projectId));
            if (objCalendar != null && StringUtils.isNotBlank(objCalendar.getCalendarId())) {
                MidCalendarGlobalRuleDTO calendarRule = this.midCalendarClientService.getGlobalCalendarRule(projectId, objCalendar.getCalendarId(), null, null);
                Map<MidCalendarDateTagValueEnum, LinkedHashSet<Range<MonthDay>>> dateTagValueEnumMap = calendarRule == null ? null : calendarRule.getGlobalAirSeasonRangeDateRangeMap();
                if (dateTagValueEnumMap != null) {
                    for (Map.Entry<MidCalendarDateTagValueEnum, LinkedHashSet<Range<MonthDay>>> enumLinkedHashSetEntry : dateTagValueEnumMap.entrySet()) {
                        LinkedHashSet<Range<MonthDay>> monthDay = enumLinkedHashSetEntry.getValue();
                        MidCalendarAirSeasonTypeEnum airSeasonTypeEnum = MidCalendarDateTagValueEnum.toACSeasonValueNewEnum(enumLinkedHashSetEntry.getKey());
                        if (CollectionUtils.isNotEmpty(monthDay) && airSeasonTypeEnum != null) {
                            existsGlobalRuleAirSeasons.add(airSeasonTypeEnum);
                        }
                    }
                }
            }
        }

        // 2.重写环境品质--温度的空调季，这里以默认的配置为基准，重赋值自定义配置，这里不区分实例，均需要处理
        List<DemandConfigAirconditioningTdbSeasonValueObject> defaultSeasonValueObjects = null;
        for (DemandConfigAggregate<DemandConfigAirconditioningValueObject> aggregate : aggregates) {
            // 解析出项目配置最终对象
            DemandConfigIdentifier identifier = aggregate.getIdentifier();
            DemandConfigValueObject<DemandConfigAirconditioningValueObject> configValueObject = Optional.ofNullable(aggregate.getFinalConfig()).orElseGet(DemandConfigValueObject::new);
            DemandConfigAirconditioningValueObject currentInstanceConfig = configValueObject.getGlobalConfig();

            // 只有当前实例配置不为空的的时候才处理（防止继承关系变动导致出问题）
            if (currentInstanceConfig == null || currentInstanceConfig.getEnvironmentQuality() == null || currentInstanceConfig.getEnvironmentQuality().getTdbQuality() == null) {
                return;
            }

            // 当不存在空调季规则时，直接置为空即可
            List<DemandConfigAirconditioningTdbSeasonValueObject> newSeasonQuality = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(existsGlobalRuleAirSeasons)) {
                // 存在空调季规则，获取一次默认配置
                if (defaultSeasonValueObjects == null) {
                    DemandConfigAnalyzeStrategy<DemandConfigAirconditioningValueObject> configAnalyzeStrategy = identifier.getTemplateType().demandConfigAnalyzeStrategy();
                    DemandConfigAirconditioningValueObject defaultConfigAirconditioningValueObject = configAnalyzeStrategy.defaultConfig(DemandInstanceTypeEnum.PROJECT).get();
                    defaultSeasonValueObjects = defaultConfigAirconditioningValueObject.getEnvironmentQuality().getTdbQuality().getSeasonQuality();
                }

                // 根据默认配置，以及当前实例自定义配置，返回新的规则温度标准数据
                if (CollectionUtils.isNotEmpty(defaultSeasonValueObjects)) {
                    List<DemandConfigAirconditioningTdbSeasonValueObject> seasonQuality = currentInstanceConfig.getEnvironmentQuality().getTdbQuality().getSeasonQuality();
                    Map<MidCalendarAirSeasonTypeEnum, DemandConfigAirconditioningTdbSeasonValueObject> currentConfigMap = CollectionUtils.isEmpty(seasonQuality) ? new HashMap<>()
                            : seasonQuality.stream().collect(Collectors.toMap(DemandConfigAirconditioningTdbSeasonValueObject::getAirSeason, Function.identity(), (v1, v2) -> v2));
                    // 数据过滤及赋值处理
                    for (DemandConfigAirconditioningTdbSeasonValueObject defaultSeasonValueObject : defaultSeasonValueObjects) {
                        if (!existsGlobalRuleAirSeasons.contains(defaultSeasonValueObject.getAirSeason())) {
                            continue;
                        }
                        boolean r = currentConfigMap.containsKey(defaultSeasonValueObject.getAirSeason()) ? newSeasonQuality.add(currentConfigMap.get(defaultSeasonValueObject.getAirSeason()))
                                : newSeasonQuality.add(defaultSeasonValueObject);
                    }
                }
            }
            currentInstanceConfig.getEnvironmentQuality().getTdbQuality().setSeasonQuality(newSeasonQuality);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
