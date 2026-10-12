package com.persagy.apccontrol.basic.structure.demand.lifecycle.config;

import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarDTO;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarGlobalRuleDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigResultVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarDayDateTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarDayDateTagResponse;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueSelectValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.airconditioning.DemandConfigAirConditioningAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 空调需求配置查询期补全器。
 *
 * <p>继承后的稳定配置可以进入预计算结果表；营业日历空调季、项目空调工况、DTX绑点、RWD对象和
 * 本地化名称会随查询日期或当前运行态变化，只能在结果读取时补充。原实时查询继续由
 * {@link DemandConfigAirconditoningAware} 执行既有生命周期逻辑，本类不得接入原聚合查询链路。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigAirconditioningQueryEnricher {

    /**
     * 营业日历客户端。
     *
     * @since v7.260701.0
     */
    private final @NonNull MidCalendarClientService midCalendarClientService;

    /**
     * 空调需求项目级自定义配置服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;

    /**
     * 空调需求配置解析策略，用于获取与实时链路一致的项目默认配置。
     *
     * @since v7.260701.0
     */
    private final @NonNull DemandConfigAirConditioningAnalyzeStrategy demandConfigAnalyzeStrategy;

    /**
     * 补充预计算结果查询返回的空调需求配置。
     *
     * <p>结果表只保存稳定核心配置。本方法在JSON反序列化后按请求日期补充动态内容，
     * 不修改结果表或快照表。</p>
     *
     * @param <T> 需求配置类型
     * @param results 需求配置结果VO
     * @param templateType 模板类型
     * @param templateId 已解析的模板ID
     * @param instanceType 请求的实例类型
     * @param configDate 请求日期，未传时按当天处理
     * @since v7.260701.0
     */
    public <T extends BaseDemandConfig> void enrichResults(
            @NonNull List<DemandConfigResultVO<T>> results,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Long templateId,
            @NonNull DemandInstanceTypeEnum instanceType,
            @Nullable LocalDate configDate) {
        if (templateType != DemandTemplateTypeEnum.AIRCONDITIONING
                || CollectionUtils.isEmpty(results)) {
            return;
        }
        LocalDate effectiveConfigDate = this.resolveConfigDate(configDate);
        if (instanceType == DemandInstanceTypeEnum.PROJECT) {
            this.fillProjectResultCustomConfig(results, templateId, effectiveConfigDate);
        }
        List<DemandConfigValueObject<DemandConfigAirconditioningValueObject>> finalConfigs = results.stream()
                .map(DemandConfigResultVO::getFinalConfig)
                .map(this::toAirconditioningConfig)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.overrideAirConditioningTdbQualityConfigData(finalConfigs, effectiveConfigDate);
    }

    /**
     * 为结果表中的项目记录补充项目级动态配置。
     *
     * @param <T> 需求配置类型
     * @param results 需求配置结果VO
     * @param templateId 模板ID
     * @param configDate 有效查询日期
     * @since v7.260701.0
     */
    private <T extends BaseDemandConfig> void fillProjectResultCustomConfig(
            @NonNull List<DemandConfigResultVO<T>> results,
            @NonNull Long templateId,
            @NonNull LocalDate configDate) {
        for (DemandConfigResultVO<T> result : results) {
            DemandConfigValueObject<DemandConfigAirconditioningValueObject> configValueObject =
                    this.toAirconditioningConfig(result.getFinalConfig());
            if (Objects.isNull(configValueObject)) {
                continue;
            }
            this.fillProjectCustomConfig(
                    configValueObject, templateId, configDate);
            return;
        }
    }

    /**
     * 补充项目级价值选择和空调工况。
     *
     * <p>{@link DemandConfigAirconditioningCustomService#getConfigData(String, String, LocalDate)}
     * 会结合当前DTX绑点、RWD系统和泵组生成查询结果，因此此步骤不能在预计算刷新时执行。</p>
     *
     * @param configValueObject 项目最终配置
     * @param templateId 模板ID
     * @param configDate 查询日期，实时聚合路径未传时保持为空
     * @since v7.260701.0
     */
    private void fillProjectCustomConfig(
            @NonNull DemandConfigValueObject<DemandConfigAirconditioningValueObject> configValueObject,
            @NonNull Long templateId,
            @Nullable LocalDate configDate) {
        DemandConfigAirconditioningValueObject globalConfig = configValueObject.getGlobalConfig();
        if (Objects.isNull(globalConfig)) {
            globalConfig = this.demandConfigAnalyzeStrategy
                    .defaultConfig(DemandInstanceTypeEnum.PROJECT).get();
        }
        if (Objects.isNull(globalConfig)) {
            return;
        }
        DemandConfigAirconditioningCustomDTO configData =
                this.demandConfigAirconditioningCustomService.getConfigData(
                        ContextUtil.getProjectId(), templateId.toString(), configDate);
        if (Objects.nonNull(configData)) {
            globalConfig.setValueSelect(
                    new DemandConfigAirconditioningValueSelectValueObject(configData.getValueSelectType()));
            globalConfig.setWorkCondition(configData.getWorkCondition());
        }
        configValueObject.setGlobalConfig(globalConfig);
    }

    /**
     * 按营业日历中存在的空调季重写温度品质季节配置。
     *
     * @param finalConfigs 继承后的空调最终配置
     * @param configDate 有效查询日期
     * @since v7.260701.0
     */
    private void overrideAirConditioningTdbQualityConfigData(
            @NonNull Collection<DemandConfigValueObject<DemandConfigAirconditioningValueObject>> finalConfigs,
            @NonNull LocalDate configDate) {
        if (CollectionUtils.isEmpty(finalConfigs)) {
            return;
        }
        Set<MidCalendarAirSeasonTypeEnum> existingAirSeasons = this.listExistingAirSeasons(configDate);
        @Nullable List<DemandConfigAirconditioningTdbSeasonValueObject> defaultSeasonConfigs = null;
        for (DemandConfigValueObject<DemandConfigAirconditioningValueObject> configValueObject : finalConfigs) {
            DemandConfigAirconditioningValueObject currentConfig = configValueObject.getGlobalConfig();
            if (Objects.isNull(currentConfig)
                    || Objects.isNull(currentConfig.getEnvironmentQuality())
                    || Objects.isNull(currentConfig.getEnvironmentQuality().getTdbQuality())) {
                continue;
            }
            if (Objects.isNull(defaultSeasonConfigs)) {
                defaultSeasonConfigs = this.listDefaultSeasonConfigs(existingAirSeasons);
            }
            List<DemandConfigAirconditioningTdbSeasonValueObject> currentSeasonConfigs =
                    currentConfig.getEnvironmentQuality().getTdbQuality().getSeasonQuality();
            Map<MidCalendarAirSeasonTypeEnum, DemandConfigAirconditioningTdbSeasonValueObject>
                    currentSeasonConfigMap = CollectionUtils.isEmpty(currentSeasonConfigs)
                    ? Collections.emptyMap()
                    : currentSeasonConfigs.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> Objects.nonNull(item.getAirSeason()))
                    .collect(Collectors.toMap(
                            DemandConfigAirconditioningTdbSeasonValueObject::getAirSeason,
                            Function.identity(),
                            (first, second) -> second));
            List<DemandConfigAirconditioningTdbSeasonValueObject> effectiveSeasonConfigs = new ArrayList<>();
            for (DemandConfigAirconditioningTdbSeasonValueObject defaultSeasonConfig : defaultSeasonConfigs) {
                DemandConfigAirconditioningTdbSeasonValueObject currentSeasonConfig =
                        currentSeasonConfigMap.get(defaultSeasonConfig.getAirSeason());
                effectiveSeasonConfigs.add(Objects.nonNull(currentSeasonConfig)
                        ? currentSeasonConfig : defaultSeasonConfig);
            }
            currentConfig.getEnvironmentQuality().getTdbQuality()
                    .setSeasonQuality(effectiveSeasonConfigs);
        }
    }

    /**
     * 查询目标日期对应的项目空调季集合。
     *
     * <p>历史日期沿用实时链路的日标签实例数据；当天和未来日期沿用全局规则中已配置的季节类型。</p>
     *
     * @param configDate 有效查询日期
     * @return 项目中存在的空调季集合
     * @since v7.260701.0
     */
    private @NonNull Set<MidCalendarAirSeasonTypeEnum> listExistingAirSeasons(
            @NonNull LocalDate configDate) {
        String projectId = ContextUtil.getProjectId();
        if (configDate.isBefore(DateUtils.now().toLocalDate())) {
            MidCalendarDayDateTagQueryParam queryParam = new MidCalendarDayDateTagQueryParam();
            queryParam.setObjId(projectId);
            queryParam.setBeginDate(configDate);
            queryParam.setEndDate(configDate);
            List<MidCalendarDayDateTagResponse> responses =
                    this.midCalendarClientService.listCalendarDayDateTag(projectId, queryParam);
            if (CollectionUtils.isEmpty(responses)
                    || Objects.isNull(responses.get(0))
                    || CollectionUtils.isEmpty(responses.get(0).getTagList())) {
                return Collections.emptySet();
            }
            return responses.get(0).getTagList().stream()
                    .map(MidCalendarAirSeasonTypeEnum::analyze)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        MidCalendarDTO projectCalendar = this.midCalendarClientService.getCalendar(
                projectId, new MidCalendarQueryParam().setObjId(projectId));
        if (Objects.isNull(projectCalendar) || StringUtils.isBlank(projectCalendar.getCalendarId())) {
            return Collections.emptySet();
        }
        MidCalendarGlobalRuleDTO calendarRule = this.midCalendarClientService.getGlobalCalendarRule(
                projectId, projectCalendar.getCalendarId(), null, null);
        Map<MidCalendarDateTagValueEnum, LinkedHashSet<Range<MonthDay>>> airSeasonRanges =
                Objects.isNull(calendarRule)
                        ? Collections.emptyMap()
                        : calendarRule.getGlobalAirSeasonRangeDateRangeMap();
        if (MapUtils.isEmpty(airSeasonRanges)) {
            return Collections.emptySet();
        }
        Set<MidCalendarAirSeasonTypeEnum> result = new LinkedHashSet<>();
        airSeasonRanges.forEach((dateTag, ranges) -> {
            MidCalendarAirSeasonTypeEnum airSeason =
                    MidCalendarDateTagValueEnum.toACSeasonValueNewEnum(dateTag);
            if (Objects.nonNull(airSeason) && CollectionUtils.isNotEmpty(ranges)) {
                result.add(airSeason);
            }
        });
        return result;
    }

    /**
     * 获取并过滤项目默认空调季配置。
     *
     * @param existingAirSeasons 项目中存在的空调季
     * @return 保持默认顺序的空调季配置
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigAirconditioningTdbSeasonValueObject> listDefaultSeasonConfigs(
            @NonNull Set<MidCalendarAirSeasonTypeEnum> existingAirSeasons) {
        if (CollectionUtils.isEmpty(existingAirSeasons)) {
            return Collections.emptyList();
        }
        DemandConfigAirconditioningValueObject defaultConfig =
                this.demandConfigAnalyzeStrategy.defaultConfig(DemandInstanceTypeEnum.PROJECT).get();
        if (Objects.isNull(defaultConfig)
                || Objects.isNull(defaultConfig.getEnvironmentQuality())
                || Objects.isNull(defaultConfig.getEnvironmentQuality().getTdbQuality())
                || CollectionUtils.isEmpty(
                defaultConfig.getEnvironmentQuality().getTdbQuality().getSeasonQuality())) {
            return Collections.emptyList();
        }
        return defaultConfig.getEnvironmentQuality().getTdbQuality().getSeasonQuality().stream()
                .filter(item -> existingAirSeasons.contains(item.getAirSeason()))
                .collect(Collectors.toList());
    }

    /**
     * 将通用配置值对象转换为空调配置值对象。
     *
     * <p>模板类型已经在入口处校验；这里仍检查全局配置的运行时类型，防止错误JSON或错误策略
     * 被当作空调配置继续处理。</p>
     *
     * @param configValueObject 通用需求配置值对象
     * @return 空调配置值对象，类型不匹配时返回空
     * @since v7.260701.0
     */
    @SuppressWarnings("unchecked")
    private @Nullable DemandConfigValueObject<DemandConfigAirconditioningValueObject> toAirconditioningConfig(
            @Nullable DemandConfigValueObject<? extends BaseDemandConfig> configValueObject) {
        if (Objects.isNull(configValueObject)) {
            return null;
        }
        BaseDemandConfig globalConfig = configValueObject.getGlobalConfig();
        if (Objects.nonNull(globalConfig)
                && !(globalConfig instanceof DemandConfigAirconditioningValueObject)) {
            return null;
        }
        return (DemandConfigValueObject<DemandConfigAirconditioningValueObject>)
                (DemandConfigValueObject<?>) configValueObject;
    }

    /**
     * 解析实际配置日期。
     *
     * @param configDate 请求日期
     * @return 请求日期，未传时返回当天
     * @since v7.260701.0
     */
    private @NonNull LocalDate resolveConfigDate(@Nullable LocalDate configDate) {
        return Objects.nonNull(configDate) ? configDate : DateUtils.now().toLocalDate();
    }
}
