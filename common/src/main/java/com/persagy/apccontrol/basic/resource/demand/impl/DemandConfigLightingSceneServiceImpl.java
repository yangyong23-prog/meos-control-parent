package com.persagy.apccontrol.basic.resource.demand.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.demand.DemandConfigLightingSceneMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingScene;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingSceneService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateService;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneRemoveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneUpdateDaoParam;
import com.persagy.apccontrol.basic.structure.demand.converter.lighting.DemandConfigLightingSceneConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Comparator;
import java.util.List;

/**
 * 需求管理-照明需求-配置场景服务
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Service
@Validated
public class DemandConfigLightingSceneServiceImpl
        extends BaseServiceImpl<DemandConfigLightingSceneMapper, DemandConfigLightingScene>
        implements DemandConfigLightingSceneService {

    @Autowired
    private DemandConfigLightingSceneConverter demandConfigLightingSceneConverter;
    @Autowired
    private DemandTemplateService demandTemplateService;

    @Override
    public DemandConfigLightingScene get(
            @NonNull String projectId,
            @NonNull DemandConfigLightingSceneQueryDaoParam param) {
        param.setProjectId(projectId);
        return getBaseMapper().get(param);
    }

    @Override
    public List<DemandConfigLightingScene> list(
            @NonNull String projectId,
            @NonNull DemandConfigLightingSceneQueryDaoParam param) {
        param.setProjectId(projectId);
        List<DemandConfigLightingScene> result = getBaseMapper().list(param);
        if (CollectionUtils.isNotEmpty(result)) {
            result.sort(Comparator.comparing(DemandConfigLightingScene::getSequence));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandConfigLightingScene add(@NonNull String projectId, @NonNull DemandConfigLightingSceneSaveDaoParam param) {
        param.checkAndSet();
        DemandTemplate dbTemplate = demandTemplateService.getByIdValidate(param.getRelDemandTemplateId());
        DemandConfigLightingScene addEntity = demandConfigLightingSceneConverter.toDo(
                new DemandConfigLightingScene(), projectId, dbTemplate, param);
        saveValidate(addEntity);
        return addEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(@NonNull String projectId, @NonNull DemandConfigLightingSceneUpdateDaoParam param) {
        param.checkAndSet();
        DemandConfigLightingScene originConfig = getByIdValidate(param.getId());
        return updateByIdValidate(demandConfigLightingSceneConverter.toDo(originConfig, projectId, param));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(@NonNull String projectId, @NonNull DemandConfigLightingSceneRemoveDaoParam param) {
        return removeByIdIgnore(param.getId());
    }
}
