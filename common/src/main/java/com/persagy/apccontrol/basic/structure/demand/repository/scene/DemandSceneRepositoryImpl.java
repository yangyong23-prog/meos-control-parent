package com.persagy.apccontrol.basic.structure.demand.repository.scene;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.entity.demand.DemandScene;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.resource.demand.DemandSceneService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandSceneQueryDaoParam;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandSceneConverter;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandSceneChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求场景仓储
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandSceneRepositoryImpl implements DemandSceneRepository {

    private final DemandSceneService demandSceneService;
    private final DemandSceneConverter demandSceneConverter;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public DemandSceneEntity getByIdAndDate(boolean isThrows, @NonNull Long sceneId, @Nullable LocalDate date) {
        Optional<DemandSceneEntity> sceneOpt = listByIdsAndDate(Sets.newHashSet(sceneId), date).stream().findFirst();
        return isThrows ? sceneOpt.orElseThrow(() ->
                new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_01cd45169a314fd0946b99eb9275ee13")))
                : sceneOpt.orElse(null);
    }

    @Override
    public List<DemandSceneEntity> listByIdsAndDate(
            @NonNull Set<Long> sceneIds,
            @Nullable LocalDate date) {
        DemandSceneQueryDaoParam queryDaoParam = new DemandSceneQueryDaoParam();
        queryDaoParam.setIds(sceneIds);
        queryDaoParam.setDate(date);
        return this.list(queryDaoParam);
    }

    @Override
    public List<DemandSceneEntity> list(@NonNull DemandSceneQueryDaoParam param) {
        if (CollectionUtils.isNotEmpty(param.getSelectColumns())) {
            param.addColumns(DemandScene::getSequence);
        }
        List<DemandScene> scenes = demandSceneService.list(param);
        if (CollectionUtils.isNotEmpty(scenes)) {
            return scenes.stream()
                    .map(DemandSceneEntity::of)
                    .sorted(Comparator.comparingInt(DemandSceneEntity::getSequence))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(DemandSceneEntity scene) {
        DataTypeEnum type = DataTypeEnum.ADD;
        DemandSceneEntity oldScene = null;
        if (Objects.nonNull(scene.getId())) {
            type = DataTypeEnum.EDIT;
            oldScene = getByIdAndDate(true, scene.getId(), null);
        }
        DemandScene demandScene = demandSceneConverter.toScene(scene);
        demandScene.setProjectId(ContextUtil.getProjectId());
        demandSceneService.saveOrUpdate(demandScene);
        scene.setId(demandScene.getId());
        this.publishChangeEvent(scene.getRelTemplateId(), Lists.newArrayList(
                new DemandSceneChangeEvent.Detail(type, Lists.newArrayList(
                        new DemandSceneChangeEvent.Config(oldScene, scene)))));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delSceneByTemplateId(@NonNull Set<Long> templateIds) {
        if (CollectionUtils.isEmpty(templateIds)) {
            return;
        }
        DemandSceneQueryDaoParam queryDaoParam = new DemandSceneQueryDaoParam();
        queryDaoParam.setRelTemplateIds(templateIds);
        List<DemandSceneEntity> scenes = this.list(queryDaoParam);
        if (CollectionUtils.isEmpty(scenes)) {
            return;
        }
        this.del(scenes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delSceneById(@NonNull Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        DemandSceneQueryDaoParam queryDaoParam = new DemandSceneQueryDaoParam();
        queryDaoParam.setIds(ids);
        List<DemandSceneEntity> scenes = this.list(queryDaoParam);
        if (CollectionUtils.isEmpty(scenes)) {
            return;
        }
        this.del(scenes);
    }

    /**
     * 删除场景
     *
     * @param scenes 场景
     * @since 7.250801.0
     */
    private void del(@NonNull List<DemandSceneEntity> scenes) {
        Map<Long, List<DemandSceneChangeEvent.Config>> templateIdAndEventConfigs = new LinkedHashMap<>();
        Set<Long> toBeDelIds = new HashSet<>();
        for (DemandSceneEntity scene : scenes) {
            toBeDelIds.add(scene.getId());
            MapExecutor.dynamicPut(templateIdAndEventConfigs, scene.getRelTemplateId(),
                    new DemandSceneChangeEvent.Config(scene, null), ArrayList::new);
        }
        demandSceneService.removeByIdIgnore(toBeDelIds);
        if (MapUtils.isNotEmpty(templateIdAndEventConfigs)) {
            templateIdAndEventConfigs.forEach((templateId, eventConfigs) ->
                    this.publishChangeEvent(templateId, Lists.newArrayList(
                            new DemandSceneChangeEvent.Detail(DataTypeEnum.DELETE, eventConfigs))));
        }
    }

    /**
     * 发布变更事件
     *
     * @param templateId 模版ID
     * @param details    详情
     * @since 7.250801.0
     */
    private void publishChangeEvent(@NonNull Long templateId, @NonNull List<DemandSceneChangeEvent.Detail> details) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        DemandSceneChangeEvent.Message message = new DemandSceneChangeEvent.Message();
        message.setTemplateId(templateId);
        message.setDetails(details);
        applicationEventPublisher.publishEvent(new DemandSceneChangeEvent(message));
    }

    @Override
    public String logTag() {
        return "需求管理-需求场景仓储";
    }
}
