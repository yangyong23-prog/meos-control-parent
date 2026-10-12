package com.persagy.apccontrol.common.service.lighting.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.PointInfoDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingLampFuncTypeTagDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSpaceTagDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogObjectCategpryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTemplateEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.param.lighting.LightingControllerWithCircuitQueryParam;
import com.persagy.apccontrol.basic.model.param.lighting.LightingInstanceQueryParam;
import com.persagy.apccontrol.basic.model.param.lighting.LightingOperatingParametersSetParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.rules.SetOperatingParametersVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdTwinControlObjectVO;
import com.persagy.apccontrol.basic.outside.dto.lighting.BasicInfos;
import com.persagy.apccontrol.basic.outside.dto.lighting.InfoCodeParameters;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingDataDto;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingRecords;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogInsertParam;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.service.lighting.AbstractLightingService;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.common.model.context.LightingCircuitPageListQueryContext;
import com.persagy.apccontrol.common.model.dto.ExecSend;
import com.persagy.apccontrol.common.model.dto.RunningModeDTO;
import com.persagy.apccontrol.common.model.dto.RunningModeLightingDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.TwcInstructionSend;
import com.persagy.apccontrol.common.service.RunningLogEdgeService;
import com.persagy.apccontrol.common.service.RunningMonitorService;
import com.persagy.apccontrol.common.service.lighting.LightingService;
import com.persagy.apccontrol.common.util.DataUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 本地端-照明监测
 *
 * @author LiuYang
 * @since 5.240402.0
 */
@Slf4j
@Service("localLightingServiceImpl")
@Validated
@SuppressWarnings("DuplicatedCode")
public class LightingServiceImpl extends AbstractLightingService implements LightingService {
    @Resource
    Executor executor;
    @Resource
    DtpRwdClientService dtpRwdClientService;
    @Resource
    AbstractIotDataCacheServer<FeedbackInfoMsgMqttVO> abstractIotDataCacheServer;
    @Resource
    DemandLightingStrategy demandLightingStrategy;
    @Resource
    LocalRedisCacheHandler localRedisCacheHandler;
    @Resource
    InstanceService instanceService;
    @Resource
    BizDtpRwdService bizDtpRwdService;
    @Resource
    RunningMonitorService runningMonitorService;
    @Resource
    RunningLogEdgeService runningLogEdgeService;
    
    /**
     * 按实例查询照明回路
     *
     * @param lightingInstanceQueryParam 照明实例查询参数
     * @return {@link List }<{@link DtpRwdCircuitDTO }>
     */
    @Override
    public List<DtpRwdCircuitDTO> listLightLoopByInstance(LightingInstanceQueryParam lightingInstanceQueryParam) {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = lightingInstanceQueryParam.getProjectId();
        
        // 照明回路列表
        DemandTemplateTypeEnum templateType = lightingInstanceQueryParam.getDemandTemplateType();
        List<DtpRwdCircuitDTO> dtpRwdCircuitDtoList = demandLightingStrategy.listCircuitsByInstance(
                projectId,
                templateType,
                lightingInstanceQueryParam.getInstanceType(),
                lightingInstanceQueryParam.getInstanceId()
        );
        if (CollectionUtils.isEmpty(dtpRwdCircuitDtoList)) {
            return new ArrayList<>();
        }
        return dtpRwdCircuitDtoList;
    }
    
    
    @Override
    public List<LightingSpaceTagDTO> listSpaceTags(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Lists.newArrayList();
        }
        return new ArrayList<>(mapSpaceIdAndTag(projectId, templateType, spaceIds).values());
    }
    
    @Override
    public List<LightingLampFuncTypeTagDTO> listLampFuncTypeTags(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<String> lampFuncTypes) {
        if (CollectionUtils.isEmpty(lampFuncTypes)) {
            return Lists.newArrayList();
        }
        return new ArrayList<>(mapLampFuncTypeAndTag(projectId, templateType, lampFuncTypes).values());
    }
    
    @Override
    public R<Boolean> setOperatingParameters(@NonNull LightingOperatingParametersSetParam param) {
        param.checkAndSet();
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        String spaceId = param.getSpaceId();
        String controllerId = param.getControllerId();
        Set<String> circuitIds = param.getCircuitIds();
        List<PointInfoDTO> pointInfos = param.getPointInfos();
        
        if (CollectionUtils.isNotEmpty(circuitIds)) {
            SetOperatingParametersVO setOperatingParametersVO = setOperatingParameters(projectId, circuitIds, pointInfos, false);
            if (setOperatingParametersVO.getIsAllSuccess()) {
                return R.success(true).setCause(setOperatingParametersVO.getObjectIdAndErrorMessages().toString());
            } else {
                String errorMessage = (setOperatingParametersVO.getAllCount().equals(setOperatingParametersVO.getSuccessCount())
                        ? LocaleMessageUtil.getMessage("key_437957876bc14ea286f4ae197682d9af") : LocaleMessageUtil.getMessage("key_76b8f456b065481985cc7046ebfc51a8")) + LocaleMessageUtil.getMessage("key_2cd02a3c338c4927b899102da8297eee");
                return R.fail(ErrorCodeConstants.SERVICE_ERROR_C0110.getCode(),
                        errorMessage, setOperatingParametersVO.getObjectIdAndErrorMessages().toString());
            }
        }
        if (StringUtils.isNotBlank(controllerId)) {
            circuitIds = demandLightingStrategy.mapCircuitIdAndController(projectId,
                    templateType, null, Lists.newArrayList(controllerId)).keySet();
            SetOperatingParametersVO setOperatingParametersVO = setOperatingParameters(projectId, circuitIds, pointInfos, false);
            
            if (setOperatingParametersVO.getIsAllSuccess()) {
                return R.success(true).setCause(setOperatingParametersVO.getObjectIdAndErrorMessages().toString());
            } else {
                String errorMessage = (setOperatingParametersVO.getAllCount().equals(setOperatingParametersVO.getSuccessCount())
                        ? LocaleMessageUtil.getMessage("key_437957876bc14ea286f4ae197682d9af") : LocaleMessageUtil.getMessage("key_76b8f456b065481985cc7046ebfc51a8")) + LocaleMessageUtil.getMessage("key_2cd02a3c338c4927b899102da8297eee");
                return R.fail(ErrorCodeConstants.SERVICE_ERROR_C0110.getCode(),
                        errorMessage, setOperatingParametersVO.getObjectIdAndErrorMessages().toString());
            }
        }
        if (StringUtils.isNotBlank(spaceId)) {
            List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuitsBySpaceIds(
                    projectId, templateType, Lists.newArrayList(spaceId), null, null, null);
            circuitIds = circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            SetOperatingParametersVO setOperatingParametersVO = setOperatingParameters(projectId, circuitIds, pointInfos, false);
            if (setOperatingParametersVO.getIsAllSuccess()) {
                return R.success(true).setCause(setOperatingParametersVO.getObjectIdAndErrorMessages().toString());
            } else {
                String errorMessage = (setOperatingParametersVO.getAllCount().equals(setOperatingParametersVO.getSuccessCount())
                        ? LocaleMessageUtil.getMessage("key_437957876bc14ea286f4ae197682d9af") : LocaleMessageUtil.getMessage("key_76b8f456b065481985cc7046ebfc51a8")) + LocaleMessageUtil.getMessage("key_2cd02a3c338c4927b899102da8297eee");
                return R.fail(ErrorCodeConstants.SERVICE_ERROR_C0110.getCode(),
                        errorMessage, setOperatingParametersVO.getObjectIdAndErrorMessages().toString());
            }
        }
        return R.success(false);
    }
    
    @Override
    public SetOperatingParametersVO setOperatingParametersReturnInfo(@NonNull LightingOperatingParametersSetParam param) {
        param.checkAndSet();
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        String spaceId = param.getSpaceId();
        String controllerId = param.getControllerId();
        Set<String> circuitIds = param.getCircuitIds();
        List<PointInfoDTO> pointInfos = param.getPointInfos();
        if (CollectionUtils.isEmpty(circuitIds)) {
            if (StringUtils.isNotBlank(controllerId)) {
                circuitIds = demandLightingStrategy.mapCircuitIdAndController(projectId,
                        templateType, null, Lists.newArrayList(controllerId)).keySet();
            } else if (StringUtils.isNotBlank(spaceId)) {
                List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuitsBySpaceIds(
                        projectId, templateType, Lists.newArrayList(spaceId), null, null, null);
                circuitIds = circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            }
        }
        
        List<FeedbackInfoMsgMqttVO> feedbackInfoMsgMqttList = abstractIotDataCacheServer.queryIotRealDataListByList(
                ContextUtil.getGroupCode(), projectId, circuitIds, Lists.newArrayList(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode()));
        if (CollectionUtils.isNotEmpty(feedbackInfoMsgMqttList)) {
            circuitIds = feedbackInfoMsgMqttList.stream().filter(x -> "0".equals(x.getDataValue()))
                                                .map(FeedbackInfoMsgMqttVO::getObjectId).collect(Collectors.toSet());
        } else {
            circuitIds = new HashSet<>();
        }
        
        return setOperatingParameters(projectId, circuitIds, pointInfos, true);
        
    }
    
    
    private SetOperatingParametersVO setOperatingParameters(
            @NonNull String projectId,
            @NonNull Collection<String> objectIds,
            @NonNull List<PointInfoDTO> pointInfos,
            Boolean isControl) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(pointInfos)) {
            return new SetOperatingParametersVO();
        }
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setId(objectIds));
        Map<String, DtpRwdObjectDTO> dtpRwdObjectDTOSMap = new HashMap<>();
        Set<String> allClassCodes = new HashSet<>();
        for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
            dtpRwdObjectDTOSMap.put(dtpRwdObjectDTO.getId(), dtpRwdObjectDTO);
            allClassCodes.add(dtpRwdObjectDTO.getClassCode());
        }
        // 可控数据
        List<TwcInstructionSend> twcInstructionSends = new ArrayList<>();
        // 不可控得数据
        Set<DtpRwdObjectDTO> dtpRwdObjectDTOSNoControl = new HashSet<>();
        // 照明查询是否管控
        if (isControl) {
            Map<String, DtpRwdTwinControlObjectVO> dtpRwdTwinControlObjectVOMap = bizDtpRwdService.queryControlObjects(allClassCodes);
            for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
                List<PointInfoDTO> pointInfosNew = new ArrayList<>();
                for (PointInfoDTO pointInfo : pointInfos) {
                    Boolean aBoolean = bizDtpRwdService.booleanControlObject(dtpRwdObjectDTO.getId(), pointInfo.getInfoCode(), dtpRwdTwinControlObjectVOMap);
                    if (aBoolean) {
                        pointInfosNew.add(pointInfo);
                    } else {
                        dtpRwdObjectDTOSNoControl.add(dtpRwdObjectDTO);
                    }
                }
                if (CollectionUtils.isNotEmpty(pointInfosNew)) {
                    TwcInstructionSend twcInstructionSend = new TwcInstructionSend();
                    twcInstructionSend.setProjectId(projectId);
                    twcInstructionSend.setObjectId(dtpRwdObjectDTO.getId());
                    twcInstructionSend.setPointInfos(pointInfosNew);
                    twcInstructionSends.add(twcInstructionSend);
                }
            }
        } else {
            for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
                TwcInstructionSend twcInstructionSend = new TwcInstructionSend();
                twcInstructionSend.setProjectId(projectId);
                twcInstructionSend.setObjectId(dtpRwdObjectDTO.getId());
                twcInstructionSend.setPointInfos(pointInfos);
                twcInstructionSends.add(twcInstructionSend);
            }
        }
        Map<String, String> objectIdAndErrorMessages = new HashMap<>(16);
        List<DtpRwdObjectDTO> errorCountObject = new ArrayList<>();
        for (TwcInstructionSend twcInstructionSend : twcInstructionSends) {
            try {
                // 手动进行指令下发（不记录日志，使用saveEquipSwitchSetRunningLog单独记录）
                runningMonitorService.setOperatingParameters(twcInstructionSend, false);
            } catch (Exception e) {
                logProjectInstance(projectId).warn("手动指令下发失败", e);
                objectIdAndErrorMessages.put(twcInstructionSend.getObjectId(), e.getLocalizedMessage());
                errorCountObject.add(dtpRwdObjectDTOSMap.get(twcInstructionSend.getObjectId()));
            }
        }
        
        // 保存回路开关设定操作日志
        if (pointInfos.get(0).getInfoCode().equals(InfoCodeEnum.EQUIP_SWITCH_SET.getCode())) {
            saveEquipSwitchSetRunningLog("0".equals(pointInfos.get(0).getValue()), dtpRwdObjectDTOS);
        }
        
        SetOperatingParametersVO setOperatingParametersVO = new SetOperatingParametersVO();
        Integer successCount = twcInstructionSends.size() - objectIdAndErrorMessages.size();
        setOperatingParametersVO.setSuccessCount(successCount);
        setOperatingParametersVO.setErrorCount(objectIdAndErrorMessages.size());
        setOperatingParametersVO.setNoControlCount(dtpRwdObjectDTOSNoControl.size());
        setOperatingParametersVO.setErrorCountObject(errorCountObject);
        setOperatingParametersVO.setNoControlObject(dtpRwdObjectDTOSNoControl);
        setOperatingParametersVO.setAllCount(twcInstructionSends.size());
        setOperatingParametersVO.setObjectIdAndErrorMessages(objectIdAndErrorMessages);
        setOperatingParametersVO.setIsAllSuccess(setOperatingParametersVO.getErrorCount().equals(0) && setOperatingParametersVO.getNoControlCount().equals(0));
        return setOperatingParametersVO;
        
    }
    
    /**
     * 保存回路开关设置操作日志
     *
     * @param offFlag          关闭标志
     * @param dtpRwdObjectDTOS 回路列表
     * @author yuecaipu
     * @since v5.241101.0
     */
    private void saveEquipSwitchSetRunningLog(boolean offFlag, List<DtpRwdObjectDTO> dtpRwdObjectDTOS) {
        boolean singleFlag = dtpRwdObjectDTOS.size() == 1;
        RunningLogInsertParam insertParam = new RunningLogInsertParam(ContextUtil.getProjectId(),
                SubscribeTypeEnum.LIGHT_MONITORING.getValue(),
                singleFlag ? dtpRwdObjectDTOS.get(0).getId() : SubscribeTypeEnum.LIGHT_MONITORING.getValue(),
                singleFlag ? dtpRwdObjectDTOS.get(0).getLocalName() : SubscribeTypeEnum.LIGHT_MONITORING.getReasonPhrase(),
                DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.getValue(), RunningLogObjectCategpryEnum.EQUIPMENT,
                RunningLogCategoryEnum.LIGHT_MONITORING_MANUAL_CONTROL_ARTIFICIAL, RunningLogTypeEnum.MANUAL, ContextUtil.getUserName());
        //  0关闭  1开启
        if (!offFlag) {
            if (singleFlag) {
                insertParam.buildRunningLogTemplate(RunningLogTemplateEnum.LIGHTING_SINGLE_ON, dtpRwdObjectDTOS.get(0).getLocalName());
            } else {
                insertParam.buildRunningLogTemplate(RunningLogTemplateEnum.LIGHTING_BATCH_ON, dtpRwdObjectDTOS.size() + "");
            }
        } else {
            if (singleFlag) {
                insertParam.buildRunningLogTemplate(RunningLogTemplateEnum.LIGHTING_SINGLE_OFF, dtpRwdObjectDTOS.get(0).getLocalName());
            } else {
                insertParam.buildRunningLogTemplate(RunningLogTemplateEnum.LIGHTING_BATCH_OFF, dtpRwdObjectDTOS.size() + "");
            }
        }
        runningLogEdgeService.saveLog(ContextUtil.getGroupCode(), insertParam);
    }
    
    @Override
    public SetOperatingParametersVO changeManualAutoStatusAndEquipSwitchSetByObjectIds(RunningModeLightingDTO dto) {
        /*
         先下发设备切换手自动的指令，手自动切换的指令成功后才可下发设备开关设定的指令；
         如果设备的手自动指令下发失败，设定开关指令不下发状态默认为失败
         （单个设备维度，单个设备失败不影响其他设备）
         */
        com.persagy.apccontrol.common.model.dto.RunningModeDTO runningModeDTO = new com.persagy.apccontrol.common.model.dto.RunningModeDTO();
        runningModeDTO.setModel(dto.getModel());
        runningModeDTO.setOperateType(dto.getOperateType());
        runningModeDTO.setOperateReason(dto.getOperateReason());
        runningModeDTO.setContentInfo(dto.getContentInfo());
        runningModeDTO.setExpireTime(dto.getExpireTime());
        runningModeDTO.setTemplateType(dto.getTemplateType());
        runningModeDTO.setObjectType(dto.getObjectType());
        runningModeDTO.setProjectId(dto.getProjectId());
        runningModeDTO.setUserId(ContextUtil.getUserId());
        if (dto.getIsController() != null && dto.getIsController() && dto.getObjectType().equals("equipment")) {
            // 控制模块关联得回路
            List<DtpRwdCircuitDTO> dtpRwdCircuitDTOS = demandLightingStrategy.listCircuitsByController(
                    dto.getProjectId(), dto.getTemplateType(), Lists.newArrayList(dto.getObjectIds()), Lists.newArrayList());
            if (CollectionUtils.isEmpty(dtpRwdCircuitDTOS)) {
                return new SetOperatingParametersVO();
            }
            Set<String> idSet = dtpRwdCircuitDTOS.stream().map(DtpRwdObjectDTO::getId)
                                                 .collect(Collectors.toSet());
            runningModeDTO.setObjectIds(idSet);
        } else {
            runningModeDTO.setObjectIds(dto.getObjectIds());
        }
        //是否是开关设定
        if (StringUtils.isNotBlank(dto.getEquipSwitchSet())) {
            return switchSet(dto, runningModeDTO);
        }
        //不是开关设定 直接下发手自动
        ExecSend execSend = runningMonitorService.changeManualAutoStatusByObjectIds(ContextUtil.getGroupCode(),
                runningModeDTO, false);
        return getSetOperatingParametersVO(execSend);
    }
    
    /**
     * 开关指令下发
     *
     * @param dto
     * @param runningModeDTO
     * @return
     */
    private SetOperatingParametersVO switchSet(RunningModeLightingDTO dto, RunningModeDTO runningModeDTO) {
        PointInfoDTO pointInfoDTO = new PointInfoDTO();
        pointInfoDTO.setInfoCode(InfoCodeEnum.EQUIP_SWITCH_SET.getCode());
        pointInfoDTO.setValue(dto.getEquipSwitchSet());
        //判断是否传递了截止日期 如果没有传递截止日期,说明全是手动场景下发控制指令 不再进行手动切换
        if (StringUtils.isNotEmpty(dto.getExpireTime())) {
            // 先下发成手动
            ExecSend execSend = runningMonitorService.changeManualAutoStatusByObjectIds(ContextUtil.getGroupCode(),
                    runningModeDTO, false);
            if (StringUtils.isBlank(execSend.getStringBuilder())) {
                // 再开启还是关闭
                return setOperatingParameters(dto.getProjectId(), dto.getObjectIds(),
                        Lists.newArrayList(pointInfoDTO), true);
            } else {
                //原逻辑 存在失败指令 不下发开关
                return getSetOperatingParametersVO(execSend);
            }
        }
        //直接下发开关控制指令
        return setOperatingParameters(dto.getProjectId(), dto.getObjectIds(), Lists.newArrayList(pointInfoDTO), true);
    }
    
    private SetOperatingParametersVO getSetOperatingParametersVO(ExecSend execSend) {
        SetOperatingParametersVO setOperatingParametersVO = new SetOperatingParametersVO();
        setOperatingParametersVO.setSuccessCount(execSend.getSuccessCount());
        if (CollectionUtils.isNotEmpty(execSend.getErrorCountObject())) {
            setOperatingParametersVO.setErrorCount(execSend.getErrorCountObject().size());
        }
        if (CollectionUtils.isNotEmpty(execSend.getNoControlObject())) {
            setOperatingParametersVO.setNoControlCount(execSend.getNoControlObject().size());
        }
        setOperatingParametersVO.setAllCount(execSend.getAllCount());
        setOperatingParametersVO.setErrorCountObject(execSend.getErrorCountObject());
        setOperatingParametersVO.setNoControlObject(execSend.getNoControlObject());
        setOperatingParametersVO.setIsAllSuccess(setOperatingParametersVO.getErrorCount().equals(0) && setOperatingParametersVO.getNoControlCount().equals(0));
        return setOperatingParametersVO;
    }
    
    @Override
    public LightingDataDto pageCircuitDetails(LightingControllerWithCircuitQueryParam param) {
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        
        LightingCircuitPageListQueryContext context = new LightingCircuitPageListQueryContext(param);
        // 初始化照明回路
        context.initCircuits();
        if (context.isFailed()) {
            context.printExecuteTime();
            return LightingDataDto.empty();
        }
        // 处理IOT信息点
        context.handleIotInfoCode();
        if (context.isFailed()) {
            context.printExecuteTime();
            return LightingDataDto.empty();
        }
        // 处理照明回路关联配置结果
        context.handleCircuitIdAndConfigResult(circuitIds ->
                mapCircuitIdAndConfigResult(projectId, templateType, circuitIds));
        if (context.isFailed()) {
            context.printExecuteTime();
            return LightingDataDto.empty();
        }
        // 处理照明回路关联标签
        context.handleCircuitIdAndTag(circuitIds -> mapCircuitIdAndTag(circuitIds,
                context.getCircuitIdAndConfigResult(),
                context.toCircuitIdAndInfoCodeValue()));
        if (context.isFailed()) {
            context.printExecuteTime();
            return LightingDataDto.empty();
        }
        // 处理照明回路服务空间
        context.handleCircuitServiceSpaces();
        if (context.isFailed()) {
            context.printExecuteTime();
            return LightingDataDto.empty();
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // 填充关联建筑
        futures.add(CompletableFuture.runAsync(context::fillRelBuilding, executor));
        // 填充关联楼层
        futures.add(CompletableFuture.runAsync(context::fillRelFloor, executor));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // 处理照明回路分页
        context.handleCircuitPage();
        if (context.isFailed()) {
            context.printExecuteTime();
            return LightingDataDto.empty();
        }
        futures.clear();
        // 填充照明回路IOT信息点规则
        futures.add(CompletableFuture.runAsync(context::fillCircuitIotInfoCodeExistsRule, executor));
        // 填充管控照明回路
        futures.add(CompletableFuture.runAsync(context::fillControlCircuits, executor));
        // 填充照明回路关联控制模块
        futures.add(CompletableFuture.runAsync(context::fillCircuitRelController, executor));
        // 填充照明回路关联低压末端配电柜
        futures.add(CompletableFuture.runAsync(context::fillCircuitIdAndDistributionCabinet, executor));
        // 填充照明回路关联异常事件
        futures.add(CompletableFuture.runAsync(context::fillCircuitIdAndExceptionEvents, executor));
        // 填充当前场景
        futures.add(CompletableFuture.runAsync(context::fillScene, executor));
        context.getStopWatch().start("异步任务");
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        context.getStopWatch().stop();
        // 转换结果
        LightingDataDto result = context.toLightingDataDto();
        context.printExecuteTime();
        return result;
    }
    
    /**
     * 获取照明回路的现在信息点
     * 在线状态 本地远程状态
     * key信息点,val 信息点值 未交付 返回 NULL, 无数据 返回-1
     *
     * @return val限制信息点
     */
    private Map<String, Object> getRestrictInfos(String id, Map<String, InfoCodeParameters> infoCodeParametersMap, Set<String> objectsExistRulesSet) {
        
        Map<String, Object> map = new HashMap<>();
        InfoCodeParameters localRemoteStatus = infoCodeParametersMap.get(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode());
        InfoCodeParameters onlineStatus = infoCodeParametersMap.get(InfoCodeEnum.ONLINE_STATUS.getCode());
        
        // 先判断是否有绑点信息
        if (!objectsExistRulesSet.contains(id + InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode())) {
            map.put(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode(), null);
        } else {
            map.put(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode(), localRemoteStatus == null || localRemoteStatus.getInfoCodeValue() == null
                    ? "-1" : localRemoteStatus.getInfoCodeValue());
        }
        if (!objectsExistRulesSet.contains(id + InfoCodeEnum.ONLINE_STATUS.getCode())) {
            map.put(InfoCodeEnum.ONLINE_STATUS.getCode(), null);
        } else {
            map.put(InfoCodeEnum.ONLINE_STATUS.getCode(), onlineStatus == null || onlineStatus.getInfoCodeValue() == null
                    ? "-1" : onlineStatus.getInfoCodeValue());
        }
        return map;
    }
    
    private DtpKnowledgeInfoCodeDataSourceDTO translateDataValeAndName(RcInfoCodeCfg dtpKnowledgeInfoCodeDTO
            , String dataValue) {
        
        List<DtpKnowledgeInfoCodeDataSourceDTO> dataSource = dtpKnowledgeInfoCodeDTO.getDataSource();
        if (CollectionUtils.isNotEmpty(dataSource)) {
            for (DtpKnowledgeInfoCodeDataSourceDTO dtpKnowledgeInfoCodeDataSourceDTO : dataSource) {
                String code = dtpKnowledgeInfoCodeDataSourceDTO.getCode();
                if (code.equals(dataValue)) {
                    return dtpKnowledgeInfoCodeDataSourceDTO;
                }
            }
        } else {
            DtpKnowledgeInfoCodeDataSourceDTO dtpKnowledgeInfoCodeDataSourceDTO = new DtpKnowledgeInfoCodeDataSourceDTO();
            dtpKnowledgeInfoCodeDataSourceDTO.setCode(dtpKnowledgeInfoCodeDTO.getName());
            dtpKnowledgeInfoCodeDataSourceDTO.setName(dataValue);
            return dtpKnowledgeInfoCodeDataSourceDTO;
        }
        
        return null;
    }
    
    @Override
    @SuppressWarnings({"AlibabaLowerCamelCaseVariableNaming", "AlibabaMethodTooLong"})
    public LightingDataDto pageControllerDetails(LightingControllerWithCircuitQueryParam lightingParam) {
        DemandTemplateTypeEnum templateType = lightingParam.getTemplateType();
        Set<String> inRoomFuncTypes = lightingParam.getInRoomFuncTypes();
        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuitsByInstance(lightingParam.getProjectId(),
                templateType, DemandInstanceTypeEnum.PROJECT, lightingParam.getProjectId());
        if (CollectionUtils.isEmpty(circuits)) {
            return new LightingDataDto();
        }
        List<String> circuitIds = circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        // 控制模块
        List<DtpRwdObjectDTO> controllers = demandLightingStrategy.listControllersByCircuit(
                lightingParam.getProjectId(), templateType, circuitIds, Lists.newArrayList());
        if (CollectionUtils.isEmpty(controllers)) {
            return new LightingDataDto();
        }
        List<String> controllerIds = controllers.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        
        List<LightingRecords> records = new ArrayList<>();
        // 信息点  对象key  信息点key  value=InfoCodeParameters
        Map<String, Map<String, InfoCodeParameters>> infoCodeParametersEqMap = new HashMap<>(16);
        Map<String, DtpRwdObjectDTO> circuitIdAndDistributionCabinet = new HashMap<>(16);
        Map<String, List<ExceptionEventBaseInfoVO>> objectIdAndAbnormalEvents = new HashMap<>(16);
        Map<String, List<DtpRwdSpaceDTO>> controllerIdAndInSpaces = new HashMap<>(16);
        
        List<DtpRwdSpaceDTO> spaces = demandLightingStrategy.listSpacesByController(
                lightingParam.getProjectId(), templateType, controllerIds, new ArrayList<>());
        Iterator<DtpRwdSpaceDTO> spaceIterator = spaces.iterator();
        while (spaceIterator.hasNext()) {
            DtpRwdSpaceDTO space = spaceIterator.next();
            String roomFuncType = space.getRoomFuncType();
            Set<String> relControllerIds = space.getRelObjIds();
            
            if (CollectionUtils.isNotEmpty(relControllerIds)) {
                // 存在空间功能类型过滤时进行过滤
                if (CollectionUtils.isNotEmpty(inRoomFuncTypes)) {
                    if (inRoomFuncTypes.contains(roomFuncType)) {
                        relControllerIds.forEach(item -> MapExecutor.dynamicPut(
                                controllerIdAndInSpaces, item, space, ArrayList::new));
                    } else {
                        // 如果不在筛选的空间功能类型中, 则移除数据
                        controllers.removeIf(item -> relControllerIds.contains(item.getId()));
                        controllerIds.removeIf(relControllerIds::contains);
                        spaceIterator.remove();
                    }
                } else {
                    relControllerIds.forEach(item -> MapExecutor.dynamicPut(
                            controllerIdAndInSpaces, item, space, ArrayList::new));
                }
            }
        }
        // 2.获取信息点配置
        Map<String, RcInfoCodeCfg> dtpKnowledgeInfoCodeDTOMap = this.localRedisCacheHandler.getInfoCodeConfigMap(
                DtpRwdClassCodeEnum.LIGHTING_CONTROLLER.value());
        List<String> infoCodeList = Lists.newArrayList(
                InfoCodeEnum.FAULT_STATUS.getCode(),
                InfoCodeEnum.ONLINE_STATUS.getCode()
        );
        // 数据集转对象id
        List<FeedbackInfoMsgMqttVO> objectDataList = this.abstractIotDataCacheServer.queryIotRealDataListByList(
                ContextUtil.getGroupCode(), lightingParam.getProjectId(), controllerIds, infoCodeList);
        if (objectDataList != null) {
            for (FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO : objectDataList) {
                InfoCodeParameters infoCodeParameters = new InfoCodeParameters();
                infoCodeParameters.setInfoCode(feedbackInfoMsgMqttVO.getInfoCode());
                RcInfoCodeCfg dtpKnowledgeInfoCodeDTO = dtpKnowledgeInfoCodeDTOMap.get(feedbackInfoMsgMqttVO.getInfoCode());
                infoCodeParameters.setInfoCodeName(dtpKnowledgeInfoCodeDTO.getName());
                DtpKnowledgeInfoCodeDataSourceDTO dtpKnowledgeInfoCodeDataSourceDTO = translateDataValeAndName(dtpKnowledgeInfoCodeDTO, feedbackInfoMsgMqttVO.getDataValue());
                infoCodeParameters.setDataSource(Lists.newArrayList(dtpKnowledgeInfoCodeDataSourceDTO));
                infoCodeParameters.setInfoCodeValue(feedbackInfoMsgMqttVO.getDataValue());
                Map<String, InfoCodeParameters> infoCodeParametersMap = infoCodeParametersEqMap.get(feedbackInfoMsgMqttVO.getObjectId());
                if (infoCodeParametersMap == null) {
                    infoCodeParametersMap = new HashMap<>(16);
                }
                infoCodeParametersMap.put(infoCodeParameters.getInfoCode(), infoCodeParameters);
                infoCodeParametersEqMap.put(feedbackInfoMsgMqttVO.getObjectId(), infoCodeParametersMap);
            }
        }
        if (CollectionUtils.isNotEmpty(controllerIds)) {
            // 基本信息
            // 查询照明回路和低压末端配电柜之间的关系
            circuitIdAndDistributionCabinet = demandLightingStrategy
                    .mapCircuitIdAndDistributionCabinet(lightingParam.getProjectId(), controllerIds);
            objectIdAndAbnormalEvents = mapObjectIdAndAbnormalEvents(controllerIds);
        }
        
        Map<String, DtpRwdBuildingDTO> buildingIdAndData = new HashMap<>(16);
        Map<String, DtpRwdFloorDTO> floorIdAndData = new HashMap<>(16);
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(null);
        if (CollectionUtils.isNotEmpty(buildings)) {
            for (DtpRwdBuildingDTO building : buildings) {
                buildingIdAndData.put(building.getId(), building);
            }
        }
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(null);
        if (CollectionUtils.isNotEmpty(floors)) {
            for (DtpRwdFloorDTO floor : floors) {
                floorIdAndData.put(floor.getId(), floor);
            }
        }
        Set<String> spaceIds = new HashSet<>();
        if (StringUtils.isNotBlank(lightingParam.getInstanceId())) {
            spaceIds = instanceService.listSpaceIdsByInstance(
                    lightingParam.getInstanceType(), lightingParam.getInstanceId());
        }
        for (DtpRwdObjectDTO controller : controllers) {
            String localId = controller.getLocalId();
            String localName = controller.getLocalName();
            String keyword = lightingParam.getKeyword();
            if (StringUtils.isNotBlank(keyword)) {
                if (StringUtils.isBlank(localId)) {
                    localId = "";
                }
                if (StringUtils.isBlank(localName)) {
                    localName = "";
                }
                // 筛选keywords
                if (!localId.contains(keyword) && !localName.contains(keyword)) {
                    continue;
                }
            }
            LightingRecords lightingRecords = new LightingRecords();
            lightingRecords.setId(controller.getId());
            lightingRecords.setName(controller.getName());
            lightingRecords.setLocalId(localId);
            lightingRecords.setLocalName(localName);
            lightingRecords.setObjType(controller.getObjType());
            lightingRecords.setClassCode(controller.getClassCode());
            lightingRecords.setValid(controller.getValid());
            Map<String, InfoCodeParameters> infoCodeParametersMap = infoCodeParametersEqMap.get(controller.getId());
            if (infoCodeParametersMap == null) {
                infoCodeParametersMap = new HashMap<>(16);
            }
            
            // 筛选在线状态
            String onlineStatus = lightingParam.getOnlineStatus();
            if (StringUtils.isNotBlank(onlineStatus)) {
                InfoCodeParameters infoCodeParameters = infoCodeParametersMap.get(InfoCodeEnum.ONLINE_STATUS.getCode());
                if (infoCodeParameters == null) {
                    continue;
                }
                if (!infoCodeParameters.getInfoCodeValue().equals(onlineStatus)) {
                    continue;
                }
            }
            // 筛选故障状态
            String abnormalStatus = lightingParam.getAbnormalStatus();
            if (StringUtils.isNotBlank(abnormalStatus)) {
                InfoCodeParameters infoCodeParameters = infoCodeParametersMap.get(InfoCodeEnum.FAULT_STATUS.getCode());
                if (infoCodeParameters == null) {
                    continue;
                }
                if (!infoCodeParameters.getInfoCodeValue().equals(abnormalStatus)) {
                    continue;
                }
            }
            ArrayList<InfoCodeParameters> infoCodeParameters = Lists.newArrayList(infoCodeParametersMap.values());
            // 前端要求如果信息点如果为空或者没有需要补全空的数据防止列表错乱
            if (CollectionUtils.isEmpty(infoCodeParameters) || infoCodeParameters.size() != infoCodeList.size()) {
                for (String infoCode : infoCodeList) {
                    InfoCodeParameters infoCodeParameter = infoCodeParametersMap.get(infoCode);
                    if (infoCodeParameter != null) {
                        continue;
                    }
                    infoCodeParameter = new InfoCodeParameters();
                    RcInfoCodeCfg dtpKnowledgeInfoCodeDTO = dtpKnowledgeInfoCodeDTOMap.get(infoCode);
                    infoCodeParameter.setInfoCode(infoCode);
                    infoCodeParameter.setInfoCodeName(dtpKnowledgeInfoCodeDTO.getName());
                    infoCodeParameters.add(infoCodeParameter);
                }
            }
            lightingRecords.setRunningParameters(infoCodeParameters);
            
            BasicInfos basicInfos = new BasicInfos();
            // 配电箱
            basicInfos.setRelDistributionCabinet(circuitIdAndDistributionCabinet.get(controller.getId()));
            
            // 所属空间
            List<DtpRwdSpaceDTO> inSpace = controllerIdAndInSpaces.get(controller.getId());
            if (CollectionUtils.isNotEmpty(inSpace)) {
                String dynamicName = "";
                for (DtpRwdSpaceDTO dtpRwdSpaceDTO : inSpace) {
                    String floorId = dtpRwdSpaceDTO.getFloorId();
                    String buildingId = dtpRwdSpaceDTO.getBuildingId();
                    DtpRwdFloorDTO dtpRwdFloorDTO = floorIdAndData.get(floorId);
                    DtpRwdBuildingDTO dtpRwdBuildingDTO = buildingIdAndData.get(buildingId);
                    String buildingName = "";
                    String floorName = "";
                    if (Objects.nonNull(dtpRwdFloorDTO)) {
                        floorName = dtpRwdFloorDTO.getLocalName();
                    }
                    if (Objects.nonNull(dtpRwdBuildingDTO)) {
                        buildingName = dtpRwdBuildingDTO.getLocalName();
                    }
                    dynamicName = buildingName + "/" + floorName + "/" + dtpRwdSpaceDTO.getLocalName();
                    dtpRwdSpaceDTO.setDynamicName(dynamicName);
                }
                basicInfos.setRelSpace(inSpace);
            }
            // 筛选所属空间
            if (StringUtils.isNotBlank(lightingParam.getInstanceId())) {
                List<DtpRwdSpaceDTO> relSpace = basicInfos.getRelSpace();
                if (CollectionUtils.isEmpty(relSpace)) {
                    continue;
                }
                if (CollectionUtils.isEmpty(spaceIds)) {
                    return new LightingDataDto();
                }
                boolean isSpace = false;
                for (DtpRwdSpaceDTO dtpRwdSpaceDTO : relSpace) {
                    String id = dtpRwdSpaceDTO.getId();
                    if (spaceIds.contains(id)) {
                        isSpace = true;
                        break;
                    }
                }
                if (!isSpace) {
                    continue;
                }
            }
            lightingRecords.setBasicInfos(basicInfos);
            List<ExceptionEventBaseInfoVO> exceptionEventBaseInfoVOS = objectIdAndAbnormalEvents.get(controller.getId());
            if (CollectionUtils.isNotEmpty(exceptionEventBaseInfoVOS)) {
                lightingRecords.setAbnormalEventCount(exceptionEventBaseInfoVOS.size());
                lightingRecords.setAbnormalEvents(exceptionEventBaseInfoVOS);
            } else {
                lightingRecords.setAbnormalEventCount(0);
            }
            records.add(lightingRecords);
        }
        LightingDataDto lightingDataDto = new LightingDataDto();
        lightingDataDto.setTotalCount(records.size());
        // 分页
        if (lightingParam.getPage() != null && lightingParam.getPageSize() != null) {
            records = DataUtil.getPage(records, lightingParam.getPage(), lightingParam.getPageSize());
        }
        lightingDataDto.setRecords(records);
        return lightingDataDto;
    }
    
    @Override
    public Map<String, Set<String>> mapClaasCode2objectIds(String projectId, DemandTemplateTypeEnum demandTemplateType,
                                                           String instanceId, DemandInstanceTypeEnum instanceType) {
        // 1、回路查询
        List<DtpRwdCircuitDTO> dtpRwdCircuitList = demandLightingStrategy.listCircuitsByInstance(projectId, demandTemplateType, instanceType, instanceId);
        if (CollectionUtils.isEmpty(dtpRwdCircuitList)) {
            return Collections.emptyMap();
        }
        Map<String, Set<String>> resultMap = new HashMap<>();
        // 2、回路id集合
        Set<String> circuitIds = dtpRwdCircuitList.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toSet());
        resultMap.put(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.getValue(), circuitIds);
        
        // 3、查询回路服务空间
        if (!demandTemplateType.isNightLighting()) {
            List<DtpRwdSpaceDTO> dtpRwdSpaceList = demandLightingStrategy.querySpaceByCircuit(projectId, circuitIds,
                    null, null);
            if (CollectionUtils.isNotEmpty(dtpRwdSpaceList)) {
                Set<String> spaceIds = dtpRwdSpaceList.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
                resultMap.put(DtpRwdClassCodeEnum.SPACE.getValue(), spaceIds);
            }
        }
        return resultMap;
    }
    
    public Map<String, Map<String, Map<String, String>>> groupMapInfo(String groupCode, String projectId, List<String> objectIdList) {
        List<String> infoCodeList = Lists.newArrayList(
                InfoCodeEnum.ONLINE_STATUS.getCode(),
                InfoCodeEnum.ORDER_FAIL_ALARM.getCode());
        List<FeedbackInfoMsgMqttVO> feedbackInfoMsgMqttList = abstractIotDataCacheServer.queryIotRealDataListByList(
                groupCode, projectId, objectIdList, infoCodeList);
        Map<String, Map<String, Map<String, String>>> groupMapInfo = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(feedbackInfoMsgMqttList)) {
            // 先按id分组，再按信息点分组，再按数值分组，名称为value
            groupMapInfo = feedbackInfoMsgMqttList.stream()
                                                  .collect(Collectors.groupingBy(FeedbackInfoMsgMqttVO::getObjectId,
                                                          Collectors.groupingBy(FeedbackInfoMsgMqttVO::getInfoCode,
                                                                  Collectors.toMap(
                                                                          // 使用数组作为key
                                                                          FeedbackInfoMsgMqttVO::getDataValue,
                                                                          x -> StringUtils.isNotBlank(x.getLocalName()) ? x.getLocalName() : ""
                                                                  )
                                                          )
                                                  ));
        }
        return groupMapInfo;
    }
}
