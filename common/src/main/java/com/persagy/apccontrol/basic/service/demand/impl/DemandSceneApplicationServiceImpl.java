package com.persagy.apccontrol.basic.service.demand.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandConfigLightingComputeParam;
import com.persagy.apccontrol.basic.model.param.demand.scene.DemandSceneCopyParam;
import com.persagy.apccontrol.basic.model.param.demand.scene.DemandSceneParam;
import com.persagy.apccontrol.basic.model.param.demand.scene.DemandSceneSaveParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.scene.DemandAllSceneVO;
import com.persagy.apccontrol.basic.model.vo.demand.scene.DemandSceneVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingResultService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandSceneQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.DemandConfigLightingRemoveDaoParam;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.service.demand.DemandSceneApplicationService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandConfigLightingComputeNowService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandCalendarConverter;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandSceneConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandConfigClearTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigClearParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigCopyPairParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGateway;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.performance.StopWatch;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 需求场景查询服务
 *
 * @author wxf
 * @since v7.250801.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandSceneApplicationServiceImpl implements DemandSceneApplicationService, LogComponent {

    private final DemandSceneGateway demandSceneGateway;
    private final DemandConfigGateway demandConfigGateway;
    private final DemandSceneConverter demandSceneConverter;
    private final DemandCalendarQueryService demandCalendarQueryService;
    private final DemandTemplateService demandTemplateService;
    private final DemandCalendarConverter demandCalendarConverter;
    private final DemandConfigLightingResultService demandConfigLightingResultService;
    private final DemandConfigLightingComputeNowService demandConfigLightingComputeNowService;

    @Override
    public List<DemandSceneVO> listSceneConfig(@NonNull Long templateId) {
        DemandSceneQueryDaoParam queryParam = new DemandSceneQueryDaoParam();
        queryParam.setRelTemplateIds(Sets.newHashSet(templateId));
        List<DemandSceneEntity> scenes = demandSceneGateway.listScenes(queryParam);
        if (CollectionUtils.isEmpty(scenes)) {
            return Lists.newArrayList();
        }
        return demandSceneConverter.toSceneVo(scenes);
    }

    @Override
    public List<DemandAllSceneVO> listAllScene(@NonNull DemandTemplateBatchQueryParam param) {
        List<DemandAllSceneVO> result = Lists.newArrayList();
        //获取类型下所有模版
        List<DemandTemplateVO> demandTemplateVOS = demandCalendarQueryService.listTemplate(param);
        if (CollectionUtil.isEmpty(demandTemplateVOS)) {
            return result;
        }

        Map<Long, DemandTemplateVO> demandTemplateMap = demandTemplateVOS.stream().collect(Collectors.toMap(DemandTemplateVO::getTemplateId, Function.identity()));
        Set<Long> templateIds = demandTemplateVOS.stream().map(DemandTemplateVO::getTemplateId).collect(Collectors.toSet());
        DemandSceneQueryDaoParam queryParam = new DemandSceneQueryDaoParam();
        queryParam.setRelTemplateIds(templateIds);
        List<DemandSceneEntity> scenes = demandSceneGateway.listScenes(queryParam);
        if (CollectionUtil.isNotEmpty(param.getExcludeSceneIds())) {
            scenes = scenes.stream().filter(scene -> !param.getExcludeSceneIds().contains(scene.getId())).collect(Collectors.toList());
        }

        Map<Long, List<DemandSceneEntity>> sceneMap = scenes.stream().collect(Collectors.groupingBy(DemandSceneEntity::getRelTemplateId));
        for (Long templateId : demandTemplateMap.keySet()) {
            DemandTemplateVO template = demandTemplateMap.get(templateId);
            List<DemandSceneEntity> sceneList = sceneMap.get(templateId);
            if (template != null && CollectionUtil.isNotEmpty(sceneList)) {
                DemandAllSceneVO vo = new DemandAllSceneVO();
                vo.setTemplate(template);
                vo.setScenes(demandSceneConverter.toSceneVo(sceneList));
                result.add(vo);
            }
        }

        Comparator<DemandAllSceneVO> comparator = Comparator.comparing(
                item -> item.getTemplate().getTemplateId(),
                Comparator.nullsLast(Comparator.naturalOrder()));
        CollectionUtil.sort(result, comparator);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean editSceneConfigs(@NonNull DemandSceneSaveParam saveParams) {
        List<DemandSceneParam> configPrams = CollectionUtil.isEmpty(saveParams.getConfigParams()) ? Lists.newArrayList() : saveParams.getConfigParams();
        for (int i = 0; i < configPrams.size(); i++) {
            configPrams.get(i).setSequence(i);
        }
        Set<Long> currentSceneIds = CollectionUtil.isEmpty(configPrams) ? com.google.common.collect.Sets.newHashSet() : configPrams.stream()
                .map(DemandSceneParam::getSceneId).filter(Objects::nonNull).collect(Collectors.toSet());

        // 1.验证新场景的场景名称是否存在重复
        Set<String> tempSceneName = new HashSet<>();
        for (DemandSceneParam configParam : configPrams) {
            if (tempSceneName.contains(configParam.getSceneName())) {
                throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_d48abe672c2047febfba720e48b7b7a8") + configParam.getSceneName());
            }
            tempSceneName.add(configParam.getSceneName());
        }

        // 2.获取旧场景配置数据
        DemandSceneQueryDaoParam queryParam = new DemandSceneQueryDaoParam();
        queryParam.setRelTemplateIds(Sets.newHashSet(saveParams.getTemplateId()));
        List<DemandSceneEntity> oldScenes = demandSceneGateway.listScenes(queryParam);
        Set<Long> oldSceneIds = CollectionUtil.isEmpty(oldScenes) ? com.google.common.collect.Sets.newHashSet() : oldScenes.stream().map(DemandSceneEntity::getId).collect(Collectors.toSet());
        oldSceneIds.removeAll(currentSceneIds);

        // 4.场景数据分类存储
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(saveParams.getDemandTemplateType());
        templateParam.setTemplateId(saveParams.getTemplateId());
        Optional<DemandTemplate> templateOpt = demandConfigGateway.getByTemplate(templateParam);
        if (!templateOpt.isPresent()) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_51d6d966e2a047e38b789b622ca7f6b9") + saveParams.getTemplateId());
        }
        
        DemandTemplate template = templateOpt.get();
        Long templateId = saveParams.getTemplateId();
        String projectId = ContextUtil.getProjectId();

        // 5.删除历史旧场景数据
        for (Long removeSceneId : oldSceneIds) {
            demandSceneGateway.delScene(removeSceneId);

            // 同步移除配置表中，与此场景有关的所有配置
            DemandConfigLightingRemoveDaoParam removeDaoParam = new DemandConfigLightingRemoveDaoParam();
            removeDaoParam.setRelDemandTemplateId(templateId);
            removeDaoParam.setRelDemandLightingSceneId(removeSceneId);

            DemandConfigClearParam clearParam = new DemandConfigClearParam();
            clearParam.setTemplateId(template.getId());
            clearParam.setSceneId(removeSceneId);
            clearParam.setClearConfigType(DemandConfigClearTypeEnum.CURRENT_AND_CHILDREN);
            clearParam.setInstanceType(DemandInstanceTypeEnum.PROJECT);
            clearParam.setInstanceIds(Collections.singleton(projectId));
            demandConfigGateway.clearConfig(clearParam);
            // 同步移除回路表数据
            demandConfigLightingResultService.deleteByProjectIdAndTemplateIdAndSceneId(projectId, templateId, removeSceneId);
        }

        //保存场景数据
        for (DemandSceneParam param : configPrams) {
            Long sceneId = param.getSceneId();
            String sceneName = param.getSceneName();
            Integer sequence = param.getSequence();
            DemandTimeValueObject beginTime = param.getBeginTime();
            DemandTimeValueObject endTime = param.getEndTime();

            DemandSceneEntity demandScene = new DemandSceneEntity();
            demandScene.setId(sceneId);
            demandScene.setBeginTime(beginTime);
            demandScene.setEndTime(endTime);
            demandScene.setSceneName(sceneName);
            demandScene.setSequence(sequence);
            demandScene.setRelTemplateId(templateId);
            demandScene.setDemandTemplateType(template.getDemandTemplateType());
            demandScene.setDemandTemplateTag(template.getDemandTemplateTag());
            demandSceneGateway.saveOrUpdate(demandScene);

            if (sceneId == null) {
                this.resultComputeDemandLightingResult(template.getDemandType(), templateId, demandScene.getId());
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copySceneConfig(@NonNull DemandSceneCopyParam fromSceneParam) {
        StopWatch stopWatch = StopWatch.create("DemandLightingServiceImpl.copySceneConfig");
        stopWatch.start("基本数据查询");
        // 获取来源配置
        Long fromSceneId = fromSceneParam.getFromSceneId();
        Long sceneId = fromSceneParam.getSceneId();
        String projectId = ContextUtil.getProjectId();

        Optional<DemandSceneEntity> fromDemandScene = demandSceneGateway.getScene(fromSceneId);
        if (!fromDemandScene.isPresent()) {
            logProjectInstance().error("来源场景不存在，id:" + fromSceneId);
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_7af154a14211414789445618f9fb5ede") + fromSceneId);
        }

        Optional<DemandSceneEntity> demandScene = demandSceneGateway.getScene(sceneId);
        if (!demandScene.isPresent()) {
            logProjectInstance().error("目标场景不存在，id:" + sceneId);
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_7af154a14211414789445618f9fb5ede") + sceneId);
        }

        Long templateId = demandScene.get().getRelTemplateId();
        Long fromTemplateId = fromDemandScene.get().getRelTemplateId();
        stopWatch.stop();

        stopWatch.start("删除旧数据");
        DemandConfigClearParam clearParam = new DemandConfigClearParam();
        clearParam.setTemplateId(templateId);
        clearParam.setSceneId(sceneId);
        clearParam.setClearConfigType(DemandConfigClearTypeEnum.CURRENT_AND_CHILDREN);
        clearParam.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        clearParam.setInstanceIds(Collections.singleton(projectId));
        demandConfigGateway.clearConfig(clearParam);
        stopWatch.stop();

        stopWatch.start("保存数据");
        DemandConfigCopyPairParam fromParam = new DemandConfigCopyPairParam();
        fromParam.setTemplateId(fromTemplateId);
        fromParam.setSceneId(fromSceneId);
        DemandConfigCopyPairParam toParam = new DemandConfigCopyPairParam();
        toParam.setTemplateId(templateId);
        toParam.setSceneId(sceneId);
        demandConfigGateway.copyConfig(Pair.of(fromParam, toParam));
        stopWatch.stop();

        // 同步重计算回路表数据
        stopWatch.start("同步重计算回路表数据");
        this.resultComputeDemandLightingResult(demandScene.get().getDemandTemplateType().toDemandTypeEnum(), templateId, sceneId);
        logProjectInstance().info("场景复制，id:" + sceneId + " fromId:" + fromSceneId);
        stopWatch.stop();
        logProjectInstance().info(stopWatch.prettyPrint());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copySceneConfigByTemplate(Long templateId, Long fromTemplateId) {
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateId(templateId);
        Optional<DemandTemplate> currentTemplate = demandConfigGateway.getByTemplate(templateParam);

        BaseDemandTemplateParam fromTemplateParam = new BaseDemandTemplateParam();
        fromTemplateParam.setTemplateId(fromTemplateId);
        Optional<DemandTemplate> fromTemplate = demandConfigGateway.getByTemplate(fromTemplateParam);

        if (!currentTemplate.isPresent()) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_81c360a4093342b38343f046e485cbda") + templateId);
        }

        if (!fromTemplate.isPresent()) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_7b08194c78574b05b3e8a4a9ed5746e9") + fromTemplateId);
        }

        if (!currentTemplate.get().getDemandTemplateType().equals(fromTemplate.get().getDemandTemplateType()) || currentTemplate.get().getDemandTemplateTag().equals(fromTemplate.get().getDemandTemplateTag())) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_20438d66fc6a49b5a2bfd61bfaf29d2b"));
        }

        List<DemandSceneEntity> fromDemandConfigLightingScenes = demandSceneGateway.listScenesByTemplateId(Collections.singleton(fromTemplateId));
        if (CollectionUtil.isEmpty(fromDemandConfigLightingScenes)) {
            return true;
        }

        // 3.清除当前模板下的所有配置，操作以最后一次为准
        removeSceneAndConfig(templateId);

        // 4.将来源的模板配置修改为现有的模板，拷贝入库一份
        Map<Long, Long> oldSceneIdAndNewSceneId = new HashMap<>();
        for (DemandSceneEntity fromDemandConfigLightingScene : fromDemandConfigLightingScenes) {
            DemandSceneEntity demandScene = new DemandSceneEntity();
            demandScene.setBeginTime(fromDemandConfigLightingScene.getBeginTime());
            demandScene.setEndTime(fromDemandConfigLightingScene.getEndTime());
            demandScene.setSceneName(fromDemandConfigLightingScene.getSceneName());
            demandScene.setSequence(fromDemandConfigLightingScene.getSequence());
            demandScene.setRelTemplateId(templateId);
            demandScene.setDemandTemplateType(fromDemandConfigLightingScene.getDemandTemplateType());
            demandScene.setDemandTemplateTag(fromDemandConfigLightingScene.getDemandTemplateTag());
            demandSceneGateway.saveOrUpdate(demandScene);
            oldSceneIdAndNewSceneId.put(fromDemandConfigLightingScene.getId(), demandScene.getId());
        }

        oldSceneIdAndNewSceneId.forEach((fromSceneId, newSceneId) -> {
            DemandConfigCopyPairParam fromParam = new DemandConfigCopyPairParam();
            fromParam.setTemplateId(fromTemplateId);
            fromParam.setSceneId(fromSceneId);
            DemandConfigCopyPairParam toParam = new DemandConfigCopyPairParam();
            toParam.setTemplateId(templateId);
            toParam.setSceneId(newSceneId);
            demandConfigGateway.copyConfig(Pair.of(fromParam, toParam));
            // 同步重计算回路表数据
            this.resultComputeDemandLightingResult(currentTemplate.get().getDemandType(),templateId, newSceneId);
        });

        // 发送消息
        //publishMessage(projectId, templateId);
        return true;
    }

    @Override
    public List<DemandTemplateVO> listExistsSceneConfigTemplate(Long templateId) {
        // 1.校验模板是否存在
        BaseDemandTemplateParam fromTemplateParam = new BaseDemandTemplateParam();
        fromTemplateParam.setTemplateId(templateId);
        Optional<DemandTemplate> template = demandConfigGateway.getByTemplate(fromTemplateParam);

        if (!template.isPresent() || template.get().getIsEnabled() == false) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_565996aa89734b47ab0d7fabe1dd9ced") + templateId);
        }

        // 2.获取此需求类型下，所有的场景下实例配置数据
        DemandConfigByInstanceRepositoryBatchQueryParam batchQueryParam = new DemandConfigByInstanceRepositoryBatchQueryParam();
        batchQueryParam.setTemplateType(template.get().getDemandTemplateType());
        List<DemandConfigAggregate> configAggregateList = demandConfigGateway.listOriginConfigs(batchQueryParam);

        if (CollectionUtil.isEmpty(configAggregateList)) {
            return Lists.newArrayList();
        }

        // 3.获取存在配置数据的所有模板
        List<Long> templateIds = configAggregateList.stream()
                .filter(item -> !item.getIdentifier().getTemplateId().equals(templateId) && item.getConfig() != null)
                .map(item -> item.getIdentifier().getTemplateId()).distinct().collect(Collectors.toList());

        if (CollectionUtil.isEmpty(templateIds)) {
            return Lists.newArrayList();
        }

        DemandTemplateQueryDaoParam templateQueryDaoParam = new DemandTemplateQueryDaoParam();
        templateQueryDaoParam.setIds(templateIds);
        templateQueryDaoParam.setIsEnabled(true);
        List<DemandTemplate> demandTemplates = demandTemplateService.list(templateQueryDaoParam);
        return demandCalendarConverter.toTemplateVo(demandTemplates);
    }

    /**
     * 重新计算回路的配置
     *
     * @param demandType  需求类型
     * @param templateId 模版ID
     * @param sceneId    场景ID
     */
    public void resultComputeDemandLightingResult(DemandTypeEnum demandType, Long templateId, Long sceneId) {
        if(DemandTypeEnum.LIGHTING.equals(demandType)){
            DemandConfigLightingComputeParam demandConfigLightingComputeParam = new DemandConfigLightingComputeParam(
                    templateId, sceneId, null);
            demandConfigLightingComputeNowService.resultCompute(demandConfigLightingComputeParam);
        }
    }

    public void removeSceneAndConfig(Long templateId) {
        // 清除当前模板下的所有配置
        if (templateId == null) {
            return;
        }

        DemandConfigClearParam clearParam = new DemandConfigClearParam();
        clearParam.setTemplateId(templateId);
        clearParam.setClearConfigType(DemandConfigClearTypeEnum.CURRENT_AND_CHILDREN);
        clearParam.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        clearParam.setInstanceIds(Collections.singleton(ContextUtil.getProjectId()));
        demandConfigGateway.clearConfig(clearParam);

        demandSceneGateway.delSceneByTemplateId(Collections.singleton(templateId));
    }
    @Override
    public String logTag() {
        return "需求场景管理";
    }
}
