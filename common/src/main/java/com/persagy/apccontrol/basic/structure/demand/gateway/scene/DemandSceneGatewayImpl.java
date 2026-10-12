package com.persagy.apccontrol.basic.structure.demand.gateway.scene;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherSunRiseAndSetDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherClientService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandSceneQueryDaoParam;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateChangeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandTemplateChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.param.DemandSceneSaveParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.param.DemandSceneUpdateParam;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.repository.scene.DemandSceneRepository;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.*;

/**
 * 需求场景实现
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Component
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandSceneGatewayImpl implements DemandSceneGateway {

    private final DemandSceneRepository demandSceneRepository;
    private final DemandCalendarQueryService demandCalendarQueryService;
    private final WeatherClientService weatherClientService;

    @Override
    public Optional<DemandSceneEntity> getScene(@NonNull Long sceneId) {
        Optional<DemandSceneEntity> sceneOpt = Optional.ofNullable(
                demandSceneRepository.getByIdAndDate(false, sceneId, null));
        if (sceneOpt.isPresent()) {
            List<DemandSceneEntity> scenes = this.fillSceneRealTime(DateUtils.now().toLocalDate(), Lists.newArrayList(sceneOpt.get()));
            if (CollectionUtils.isNotEmpty(scenes)) {
                return Optional.of(scenes.get(0));
            }
        }
        return sceneOpt;
    }

    @Override
    public Optional<DemandSceneEntity> getValidSceneByNow(@NonNull DemandTemplateTypeEnum templateType) {
        return getValidSceneByTime(templateType, DateUtils.now());
    }

    @Override
    public Optional<DemandSceneEntity> getValidSceneByTime(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDateTime dateTime) {
        List<DemandSceneEntity> lightingScenes = this.listValidScenesByDate(templateType, dateTime.toLocalDate());
        Optional<DemandSceneEntity> sceneOpt = lightingScenes.stream()
                .filter(item -> BooleanUtils.isTrue(item.isCurrentScene(dateTime)))
                .findFirst();
        if (!sceneOpt.isPresent()) {
            logProjectInstance().warn("当前时间无场景", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("dateTime", dateTime)
            ));
            return Optional.empty();
        }
        return sceneOpt;
    }

    @Override
    public List<DemandSceneEntity> listScenes(@NonNull DemandSceneQueryDaoParam param) {
        return this.fillSceneRealTime(DateUtils.now().toLocalDate(), demandSceneRepository.list(param));
    }

    @Override
    public List<DemandSceneEntity> listScenesByTemplateId(@NonNull Set<Long> templateIds) {
        DemandSceneQueryDaoParam queryParam = new DemandSceneQueryDaoParam();
        queryParam.setRelTemplateIds(templateIds);
        return this.fillSceneRealTime(DateUtils.now().toLocalDate(), demandSceneRepository.list(queryParam));
    }

    @Override
    public List<DemandSceneEntity> listValidScenesByDate(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date) {
        Pair<DemandTemplateDateSettingVO, List<DemandSceneEntity>> dateSettingAndLightingScenes =
                this.queryDateSettingAndValidScenes(templateType, date);
        if (Objects.isNull(dateSettingAndLightingScenes)) {
            return Lists.newArrayList();
        }
        return dateSettingAndLightingScenes.getRight();
    }

    @Override
    public List<DemandSceneEntity> listValidScenesByAllDate(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date) {
        Pair<DemandTemplateDateSettingVO, List<DemandSceneEntity>> dateSettingAndLightingScenes =
                this.queryDateSettingAndValidScenes(templateType, date);
        LocalDateTime eachBeginDateTime = date.atStartOfDay();
        LocalDateTime eachEndDateTime = eachBeginDateTime.plusDays(1);
        DemandTemplateVO relTemplate = null;
        List<DemandSceneEntity> scenes = null;
        if (Objects.nonNull(dateSettingAndLightingScenes)) {
            relTemplate = dateSettingAndLightingScenes.getLeft().getRelTemplate();
            scenes = dateSettingAndLightingScenes.getRight();
        }
        if (Objects.isNull(relTemplate) || CollectionUtils.isEmpty(scenes)) {
            // 当天模版或场景时, 添加一个覆盖全天的临时场景
            return Lists.newArrayList(DemandSceneEntity.withoutScene(
                    relTemplate, eachBeginDateTime, eachEndDateTime));
        }
        TemporalUnit sceneTimeTemporalUnit = LightingSceneDTO.SCENE_TIME_TYPE.convertTemporalUnit();
        int sceneTimeAmount = LightingSceneDTO.SCENE_TIME_AMOUNT;
        LinkedList<DemandSceneEntity> result = new LinkedList<>();
        LocalDateTime tempBeginDateTime = null;
        do {
            if (Objects.isNull(tempBeginDateTime)) {
                tempBeginDateTime = eachBeginDateTime;
            }
            Iterator<DemandSceneEntity> sceneIterator = scenes.iterator();
            while (sceneIterator.hasNext()) {
                DemandSceneEntity scene = sceneIterator.next();
                LocalDateTime sceneBeginDateTime = scene.getRealBeginTime();
                LocalDateTime sceneEndDateTime = scene.getRealEndTime();
                Range<ChronoLocalDateTime<?>> timeRange = Range.between(sceneBeginDateTime,
                        sceneEndDateTime.minus(sceneTimeAmount, sceneTimeTemporalUnit));
                if (timeRange.contains(eachBeginDateTime)) {
                    // 当结果集中无场景或最后一个场景结束时间和当前场景开始时间不相等时, 将空场景添加至结果集中
                    if (CollectionUtils.isNotEmpty(result)) {
                        LocalDateTime lastEndDateTime = result.getLast().getRealEndTime();
                        if (!lastEndDateTime.isEqual(sceneBeginDateTime)) {
                            result.add(DemandSceneEntity.withoutScene(relTemplate,
                                    tempBeginDateTime, sceneBeginDateTime));
                        }
                    }
                    // 当场景时段覆盖遍历时间时, 将当前场景添加至结果集中
                    result.add(scene);
                    // 因已经将当前场景添加至结果集中, 所以将遍历时间设置为当前场景的结束时间, 即跳过了所有当前场景覆盖的时间
                    eachBeginDateTime = sceneEndDateTime;
                    tempBeginDateTime = eachBeginDateTime;
                    // 避免后续重复添加, 将当前场景从列表中移除
                    sceneIterator.remove();
                    break;
                }
            }
            eachBeginDateTime = eachBeginDateTime.plus(sceneTimeAmount, sceneTimeTemporalUnit);
        } while (eachBeginDateTime.isBefore(eachEndDateTime));
        if (CollectionUtils.isEmpty(result)) {
            // 当结果集中不存在场景时, 添加覆盖全天的临时场景
            result.add(DemandSceneEntity.withoutScene(relTemplate,
                    date.atStartOfDay(), eachEndDateTime));
        } else {
            DemandSceneEntity firstScene = result.getFirst();
            DemandSceneEntity lastScene = result.getLast();
            LocalDateTime dayBeginTime = date.atStartOfDay();
            if (!firstScene.getRealBeginTime().isEqual(dayBeginTime)) {
                // 当第一个场景的开始时间不是当日开始时间时, 补充临时场景至第一个时段
                result.addFirst(DemandSceneEntity.withoutScene(relTemplate,
                        dayBeginTime, firstScene.getRealBeginTime()));
            }
            if (!lastScene.getRealEndTime().isEqual(eachEndDateTime)) {
                // 当最后一个场景的结束时间不是当日结束时间时, 补充临时场景至最后一个时段
                result.addLast(DemandSceneEntity.withoutScene(relTemplate,
                        lastScene.getRealEndTime(), eachEndDateTime));
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(DemandSceneEntity scene) {
        demandSceneRepository.saveOrUpdate(scene);
    }

    @Override
    public void addScene(@NonNull DemandSceneSaveParam param) {
        // TODO 实现新增场景逻辑
    }

    @Override
    public void editScene(@NonNull DemandSceneUpdateParam param) {
        // TODO 实现编辑场景逻辑
    }

    @Override
    public void delScene(@NonNull Long sceneId) {
        demandSceneRepository.delSceneById(Collections.singleton(sceneId));
    }

    @Override
    public void delSceneByTemplateId(@NonNull Set<Long> templateIds) {
        if (CollectionUtils.isEmpty(templateIds)) {
            return;
        }
        demandSceneRepository.delSceneByTemplateId(templateIds);
    }

    @EventListener(DemandTemplateChangeEvent.class)
    public void onDemandTemplateChangeEvent(DemandTemplateChangeEvent event) {
        DemandTemplateChangeEvent.Message message = (DemandTemplateChangeEvent.Message) event.getSource();
        DemandTemplateChangeTypeEnum changeType = message.getDataType();
        DemandTemplate oldTemplate = message.getOldTemplate();

        Long oldTemplateId;
        switch (changeType) {
            case REMOVE:
                oldTemplateId = oldTemplate.getId();
                this.delSceneByTemplateId(Sets.newHashSet(oldTemplateId));
                break;
            case INSERT:
            case APPLIED:
            case UPDATE:
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(changeType.getClass().getSimpleName(), changeType);
        }
    }

    /**
     * 填充场景真实时间
     *
     * @param date   日期
     * @param scenes 场景
     * @return 填充后的场景
     * @since 7.250801.0
     */
    private List<DemandSceneEntity> fillSceneRealTime(
            @NonNull LocalDate date,
            @Nullable List<DemandSceneEntity> scenes) {
        if (CollectionUtils.isEmpty(scenes)) {
            return scenes;
        }
        WeatherSunRiseAndSetDTO sunRiseAndSet = weatherClientService.getSunRiseAndSetCache(ContextUtil.getProjectId(), date);
        if (scenes.stream().anyMatch(item ->
                item.getBeginTime().isSunRiseAndSetTimeType()
                        || item.getEndTime().isSunRiseAndSetTimeType())) {
            sunRiseAndSet = weatherClientService.getSunRiseAndSetCache(ContextUtil.getProjectId(), date);
            if (Objects.isNull(sunRiseAndSet)) {
                logProjectInstance().warn("当前日期无日出日落时间", Pair.of("date", date));
                return Collections.emptyList();
            }
        }
        LocalDateTime dateBeginTime = date.atStartOfDay();
        for (DemandSceneEntity scene : scenes) {
            LocalDateTime beginSceneTime = LocalDateTime.of(date, scene.getBeginTime().analyzeAbsoluteTime(sunRiseAndSet));
            LocalDateTime endSceneTime = LocalDateTime.of(date, scene.getEndTime().analyzeAbsoluteTime(sunRiseAndSet));
            // 当结束时间是当天起始时间时, 认为结束时间是到当天结束时间
            if (endSceneTime.isEqual(dateBeginTime)) {
                endSceneTime = dateBeginTime.plusDays(1);
            }
            // 结束时间小于或等于开始时间时视为无效场景
            if (!endSceneTime.isAfter(beginSceneTime)) {
                logProjectInstance().warn("无效的场景时间", Arrays.asList(
                        Pair.of("sunRiseAndSet", sunRiseAndSet),
                        Pair.of("lightingScene", scene)
                ));
                continue;
            }
            scene.setRealBeginTime(beginSceneTime);
            scene.setRealEndTime(endSceneTime);
        }
        return scenes;
    }

    /**
     * 查询日期标签和有效场景
     *
     * @param templateType 模版类型
     * @param date         日期
     * @return 日期标签和有效场景
     * @since 7.250801.0
     */
    private @Nullable Pair<DemandTemplateDateSettingVO, List<DemandSceneEntity>> queryDateSettingAndValidScenes(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date) {
        DemandTemplateDateSettingVO dateSetting = demandCalendarQueryService.getDateSetting(templateType, date);
        if (Objects.isNull(dateSetting)) {
            logProjectInstance().warn("当前日期未配置模版", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("date", date))
            );
            return null;
        }
        Long templateId = dateSetting.getRelTemplate().getTemplateId();
        DemandSceneQueryDaoParam queryParam = new DemandSceneQueryDaoParam();
        queryParam.setRelTemplateId(templateId);
        queryParam.setDate(date);
        queryParam.addColumns();
        List<DemandSceneEntity> scenes = this.fillSceneRealTime(date, demandSceneRepository.list(queryParam));
        // 移除无效场景
        scenes.removeIf(DemandSceneEntity::isInvalid);
        if (CollectionUtils.isEmpty(scenes)) {
            logProjectInstance().warn("当前日期模版类型下无场景配置", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("date", date))
            );
            return Pair.of(dateSetting, Lists.newArrayList());
        }
        return Pair.of(dateSetting, this.toLightingScenes(scenes));
    }

    /**
     * 转换照明场景DTO
     *
     * @param scenes 照明场景配置
     * @return 照明场景DTO
     * @since 7.250801.0
     */
    private List<DemandSceneEntity> toLightingScenes(
            @NonNull List<DemandSceneEntity> scenes) {
        if (CollectionUtils.isEmpty(scenes)) {
            return Lists.newArrayList();
        }
        if (CollectionUtils.isEmpty(scenes)) {
            return Lists.newArrayList();
        }
        scenes.sort(Comparator.comparing(DemandSceneEntity::getRealBeginTime));
        LinkedList<DemandSceneEntity> result = new LinkedList<>();
        result.add(scenes.get(0));
        for (int i = 1; i < scenes.size(); i++) {
            this.handleSceneIntersectionTime(result, scenes.get(i));
        }
        return result;
    }

    /**
     * 处理场景重叠时间
     *
     * @param source 数据源
     * @param target 目标源
     * @since 7.250801.0
     */
    private void handleSceneIntersectionTime(
            @NonNull LinkedList<DemandSceneEntity> source,
            @NonNull DemandSceneEntity target) {
        if (CollectionUtils.isEmpty(source)) {
            source.add(target);
            return;
        }
        DemandSceneEntity previous = source.getLast();
        Range<ChronoLocalDateTime<?>> previousTimeRange = Range.between(
                previous.getRealBeginTime(), previous.getRealEndTime().minusMinutes(1));
        Range<ChronoLocalDateTime<?>> currentTimeRange = Range.between(
                target.getRealBeginTime(), target.getRealEndTime().minusMinutes(1));
        // 上一个时间段包含当前时间段时, 直接使用上一个时间段
        if (previousTimeRange.containsRange(currentTimeRange)) {
            return;
        }
        // 当前时间段包含上一个时间段, 直接使用当前时间段
        if (currentTimeRange.containsRange(previousTimeRange)) {
            source.removeLast();
            source.add(target);
            return;
        }
        // 时间范围交叉时
        if (currentTimeRange.isOverlappedBy(previousTimeRange)) {
            // 当前时间段开始时间为上一个时段的结束时间
            LocalDateTime newBeginDateTime = previous.getRealEndTime();
            // 结束时间小于或等于开始时间时视为无效场景
            if (!target.getRealEndTime().isAfter(newBeginDateTime)) {
                logProjectInstance().warn("无效的场景开始时间", Arrays.asList(
                        Pair.of("current", target),
                        Pair.of("previous", previous)
                ));
                return;
            }
            // 将当前时间段开始时间为上一个时段的结束时间
            target.setRealBeginTime(newBeginDateTime);
            // 保留上一个时段并添加当前时间段
            source.add(target);
            return;
        }
        source.add(target);
    }

    @Override
    public String logTag() {
        return "需求管理-需求场景网关";
    }
}
