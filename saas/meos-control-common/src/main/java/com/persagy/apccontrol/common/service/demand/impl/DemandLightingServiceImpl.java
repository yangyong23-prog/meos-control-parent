package com.persagy.apccontrol.common.service.demand.impl;

import com.persagy.apccontrol.basic.model.vo.demand.lighting.FeedbackConsistencyConflictVO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandAddParam;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandConfigLightingComputeParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.DemandConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.demand.validator.DemandLightingFeedbackConsistencyValidator;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandConfigLightingComputeNowService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingService;
import com.persagy.apccontrol.common.structure.demand.migrate.DemandMigrateLightingConfigTask;
import com.persagy.apccontrol.common.structure.demand.migrate.DemandMigrateLightingSceneTask;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

/**
 * 照明需求服务实现
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandLightingServiceImpl implements DemandLightingService, LogComponent {

    private final DemandConfigLightingComputeNowService demandConfigLightingComputeNowService;
    private final DemandConfigGateway<DemandConfigLightingValueObject> demandConfigGateway;
    private final DemandMigrateLightingSceneTask demandMigrateLightingSceneTask;
    private final DemandMigrateLightingConfigTask demandMigrateLightingConfigTask;
    private final DemandTemplateRepository demandTemplateRepository;
    private final DemandLightingFeedbackConsistencyValidator feedbackConsistencyValidator;
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public boolean migrateSceneData(@Nullable DemandTemplateTypeEnum templateType) {
        demandMigrateLightingSceneTask.migrate(templateType);
        return true;
    }

    @Override
    public boolean migrateConfigData(
            @Nullable DemandTemplateTypeEnum templateType,
            @Nullable Boolean forceMigrate) {
        demandMigrateLightingConfigTask.migrate(templateType, Objects.isNull(forceMigrate) || forceMigrate);
        return true;
    }

    @Override
    public List<FeedbackConsistencyConflictVO> feedbackConsistencyCheck(
            @NonNull DemandAddParam param) {
        param.checkAndSet();
        DemandTemplate template = this.demandTemplateRepository.getByTemplateId(true, param.getTemplateId());
        DemandConfigAnalyzeStrategy<?> strategy = template.getDemandTemplateType().demandConfigAnalyzeStrategy();
        DemandConfigValueObject<?> config = strategy.analyzeConfig(param.getConfigData());
        DemandConfigLightingValueObject globalConfig = (DemandConfigLightingValueObject) config.getGlobalConfig();
        List<FeedbackConsistencyConflictVO> conflicts = this.feedbackConsistencyValidator.feedbackConsistencyCheck(
                param.getTemplateId(), param.getSceneId(), globalConfig);
        if (conflicts.isEmpty()) {
            return conflicts;
        }
        // 回填回路名称和空间名称
        Set<String> allObjectIds = new HashSet<>();
        for (FeedbackConsistencyConflictVO conflict : conflicts) {
            allObjectIds.add(conflict.getCircuitId());
            for (FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO spaceConfig : conflict.getSpaceConfigs()) {
                allObjectIds.add(spaceConfig.getInstanceId());
            }
        }
        Map<String, String> idNameMap = this.dtpRwdClientService.mapObjectIdAndLocalName(allObjectIds);
        for (FeedbackConsistencyConflictVO conflict : conflicts) {
            conflict.setCircuitName(idNameMap.get(conflict.getCircuitId()));
            for (FeedbackConsistencyConflictVO.SpaceFeedbackConfigVO spaceConfig : conflict.getSpaceConfigs()) {
                spaceConfig.setInstanceName(idNameMap.get(spaceConfig.getInstanceId()));
            }
        }
        return conflicts;
    }

    @Override
    public String logTag() {
        return "需求管理-照明服务";
    }

    /**
     * 监听配置改变事件 需求变更通知照明计算
     *
     * @param event 配置变更事件
     */
    @EventListener(DemandConfigChangeEvent.class)
    public void onDemandConfigChangeEvent(DemandConfigChangeEvent event) {
        DemandConfigChangeEvent.Message message = (DemandConfigChangeEvent.Message) event.getSource();
        Long templateId = message.getTemplateId();
        Long sceneId = message.getSceneId();
        List<DemandConfigChangeEvent.Detail> details = message.getDetails();

        BaseDemandTemplateParam param = new BaseDemandTemplateParam();
        param.setTemplateId(templateId);
        DemandTemplate template = param.queryAndGetTemplate();
        if (Objects.isNull(template) || template.getDemandType() != DemandTypeEnum.LIGHTING) {
            return;
        }
        Set<String> changeSpaceIds = new HashSet<>();
        Set<String> changeNotSpaceIds = new HashSet<>();
        for (DemandConfigChangeEvent.Detail detail : details) {
            DataTypeEnum dataType = detail.getDataType();
            List<DemandConfigChangeEvent.Config> configs = detail.getConfigs();
            for (DemandConfigChangeEvent.Config config : configs) {
                DemandConfig oldConfig = config.getOldConfig();
                DemandConfig newConfig = config.getNewConfig();
                DemandConfig finalConfig = null;
                switch (dataType) {
                    case ADD:
                    case EDIT:
                        finalConfig = newConfig;
                        break;
                    case DELETE:
                        finalConfig = oldConfig;
                        break;
                }
                if (Objects.nonNull(finalConfig)) {
                    if (finalConfig.getDemandInstanceType() == DemandInstanceTypeEnum.SPACE) {
                        changeSpaceIds.add(finalConfig.getDemandInstanceId());
                    } else {
                        changeNotSpaceIds.add(finalConfig.getDemandInstanceId());
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(changeSpaceIds) && CollectionUtils.isEmpty(changeNotSpaceIds)) {
            return;
        }
        DemandConfigLightingComputeParam computeParam = new DemandConfigLightingComputeParam();
        computeParam.setTemplateId(templateId);
        computeParam.setSceneId(sceneId);
        if (CollectionUtils.isNotEmpty(changeSpaceIds) && CollectionUtils.isEmpty(changeNotSpaceIds)) {
            computeParam.setSpaceIds(changeSpaceIds);
        }
        demandConfigLightingComputeNowService.resultCompute(computeParam);
    }

    /**
     * 通过运行监控类型计算照明配置结果
     *
     * @param type    运行监控二级类型
     * @param groupId 编组ID
     */
    public void resultComputeDemandLightingResult(RunningMonitorSecondTypeEnum type, Long groupId) {
        DemandConfigByInstanceRepositoryBatchQueryParam batchQueryParam = new DemandConfigByInstanceRepositoryBatchQueryParam();
        batchQueryParam.setTemplateType(type.toDemandTemplateTypeEnum());
        batchQueryParam.setLikeConfigData(groupId);
        List<DemandConfigAggregate<DemandConfigLightingValueObject>> configAggregateList =
                demandConfigGateway.listOriginConfigs(batchQueryParam);

        Map<Long, Set<Long>> ids = new HashMap<>();
        configAggregateList.forEach(configAggregate -> {
            if (configAggregate.getIdentifier().getScene() == null) {
                return;
            }
            DemandSceneEntity cj = configAggregate.getIdentifier().getScene();
            ids.computeIfAbsent(cj.getRelTemplateId(), k -> new HashSet<>()).add(cj.getId());
        });

        if (ids.isEmpty()) {
            return;
        }
        ids.forEach((templateId, sceneIds) -> sceneIds.forEach(sceneId -> {
            DemandConfigLightingComputeParam demandConfigLightingComputeParam =
                    new DemandConfigLightingComputeParam();
            demandConfigLightingComputeParam.setTemplateId(templateId);
            demandConfigLightingComputeParam.setSceneId(sceneId);
            demandConfigLightingComputeNowService.resultCompute(demandConfigLightingComputeParam);
        }));
    }
}