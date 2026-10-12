package com.persagy.apccontrol.common.structure.demand.migrate;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandScene;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingScene;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingSceneService;
import com.persagy.apccontrol.basic.resource.demand.DemandSceneService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.repository.scene.DemandSceneRepository;
import com.persagy.apccontrol.basic.task.BaseTask;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 需求迁移照明场景任务
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandMigrateLightingSceneTask extends BaseTask {

    private final DemandSceneService demandSceneService;
    private final DemandSceneRepository demandSceneRepository;
    private final DemandConfigLightingSceneService demandConfigLightingSceneService;

    /**
     * 迁移
     *
     * @param templateType 模版类型, 为空时迁移所有模版
     * @since 7.250801.0
     */
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public void migrate(@Nullable DemandTemplateTypeEnum templateType) {
        handlerProjectByContext(project -> {
            String projectId = ContextUtil.getProjectId();
            // 删除旧数据
            Set<Long> sceneIds = demandSceneService.lambdaQuery()
                    .select(BaseAssignIdDO::getId)
                    .eq(DemandScene::getProjectId, projectId)
                    .eq(Objects.nonNull(templateType), DemandScene::getDemandTemplateType, templateType)
                    .list().stream()
                    .map(BaseAssignIdDO::getId)
                    .collect(Collectors.toSet());
            demandSceneRepository.delSceneById(sceneIds);
            int pageNo = 1;
            while (true) {
                List<DemandConfigLightingScene> records = demandConfigLightingSceneService.lambdaQuery()
                        .eq(DemandConfigLightingScene::getProjectId, ContextUtil.getProjectId())
                        .eq(Objects.nonNull(templateType), DemandConfigLightingScene::getDemandTemplateType, templateType)
                        .page(new Page<>(pageNo++, 1000, false)).getRecords();
                if (CollectionUtils.isEmpty(records)) {
                    break;
                }
                List<DemandScene> scenes = records.stream()
                        .map(DemandScene::of)
                        .collect(Collectors.toList());
                demandSceneService.saveBatch(scenes);
            }
        });
    }

    @Override
    public String logTag() {
        return "需求管理-照明场景迁移";
    }
}