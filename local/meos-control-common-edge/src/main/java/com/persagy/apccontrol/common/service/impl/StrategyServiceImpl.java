package com.persagy.apccontrol.common.service.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.*;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.ChangeContent;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.CoolerRuleTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.centralcooling.DtpRwdCentralCoolingInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.freezepumpgroup.DtpRwdFreezePumpGroupInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyBusinessTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyInfoCodeEnum;
import com.persagy.apccontrol.basic.model.param.dashboard.StrategyAupportAiQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.QueryFreezePumpGroupParam;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFreezeGroupObjectQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.ChillerRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.lastfreezepumpgroup.LastFreezePumpGroupRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.ColdSiteSystemContrastVO;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningTimeVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningValueVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.ChillerUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.LastFreezePumpGroupRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.RunningCoolerVO;
import com.persagy.apccontrol.basic.model.vo.system.QuerySystemParameterVO;
import com.persagy.apccontrol.basic.outside.dto.AcccccGroupDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdTwinObjectRuleQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdTwinObjectRuleResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.BizStrategyQueryService;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.basic.service.param.equiprecord.EquipRecordQueryDaoParam;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.apccontrol.common.model.dto.CommandExecuteDTO;
import com.persagy.apccontrol.common.model.param.strategy.StrategyQueryParam;
import com.persagy.apccontrol.common.model.param.strategy.StrategyTypeChangeInsertParam;
import com.persagy.apccontrol.common.outside.feign.AlgorithmChillerPlantFeignClient;
import com.persagy.apccontrol.common.service.DataBoardService;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import com.persagy.apccontrol.common.service.ScheduleInstructService;
import com.persagy.apccontrol.common.service.StrategyService;
import com.persagy.apccontrol.common.service.command.ManualAutoMaticSetService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import com.persagy.validate.utility.executor.ObjectExecutor;
import com.persagy.validate.utility.validate.range.DataRangeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 策略干预服务实现
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StrategyServiceImpl implements StrategyService {

    private static final String LOG_TAG = "策略服务";

    private final ProjectCfg projectCfg;
    private final Executor executor;
    private final DtpRwdClientService dtpRwdClientService;
    private final BizDtpRwdService bizDtpRwdService;
    private final BizStrategyQueryService bizStrategyQueryService;
    private final IotDataCacheService iotDataCacheService;
    private final RcBasicsConfigService rcBasicsConfigService;
    private final AlgorithmChillerPlantFeignClient algorithmChillerPlantFeignClient;
    private final EquipRecordService equipRecordService;
    private final ManualAutoMaticSetService manualAutoMaticSetService;
    private final ScheduleInstructService scheduleInstructService;
    private final AbstractCommonServer abstractCommonServer;
    @Lazy
    @Autowired
    DataBoardService dataBoardService;


    @Override
    public List<DtpRwdFreezePumpDTO> listLastFreezePumpGroups(@NonNull StrategyFreezeGroupObjectQueryParam param) {
        return bizStrategyQueryService.listLastFreezePumpGroups(param);
    }


    @Override
    public RunningStrategyVO<ChillerUnitRunningPlanVO> listChillerRunningStrategy(
            @NonNull ChillerRunningStrategyQueryParam param) {
        String projectId = param.getProjectId();
        LocalDateTime beginDateTime = param.getBeginDateTime();
        LocalDateTime endDateTime = param.getEndDateTime();
        DataRangeValidator.date().allowFutureTime().validate(beginDateTime, endDateTime);
        String acccSystemId = param.getAcccSystemId();

        StopWatch stopWatch = StopWatch.create("查询冷机运行策略");
        stopWatch.start("查询中央供冷系统");
        DtpRwdObjectDTO acccSystem = dtpRwdClientService.getObject(true, true,
                projectId, new DtpRwdObjectQueryParam().setId(acccSystemId));
        stopWatch.stop();

        // 查询冷站最近一次运行状态
        stopWatch.start("查询冷站最近一次运行状态");
        EquipRecordQueryDaoParam equipRecordQueryDaoParam = new EquipRecordQueryDaoParam()
                .setProjectId(projectCfg.getProjectId())
                .setGroupCode(projectCfg.getGroupCode())
                .setSystemCode(DtpRwdClassCodeEnum.COLD_SYSTEM.value()).setSystemId(acccSystemId)
                .setObjId(acccSystemId).setChangeContent(new ChangeContent().setInfoCode(
                        AutoMaticInfoCodeEnum.RUN_STATUS.getPracticalCode())).setExecStatus(1);
        Map<String, RunStatusEnum> objectIdAndRunStatus = equipRecordService.mapRecentRunningStatus(equipRecordQueryDaoParam);
        stopWatch.stop();
        // 查询冷站运行记录
        equipRecordQueryDaoParam.setBeginExecuteTime(beginDateTime).setEndExecuteTime(endDateTime);
        equipRecordQueryDaoParam.setExecStatus(null);
        // 只显示已经执行成功和未执行的指令
        List<Integer> st = new ArrayList<>();
        st.add(-1);
        st.add(1);
        equipRecordQueryDaoParam.setExecStatuses(st);
        stopWatch.start("查询冷站运行记录");
        List<EquipRecord> equipRecords = equipRecordService.list(equipRecordQueryDaoParam);
        stopWatch.stop();
        // 查询冷站温差/压差设定值类型
        stopWatch.start("查询冷站温差/压差设定值类型");
        DtpRwdCentralCoolingInfoCodeEnum infoCodeSetType = queryCentralCollingTempPressSetType(projectId, acccSystemId);
        stopWatch.stop();

        RunningStrategyVO<ChillerUnitRunningPlanVO> result = new RunningStrategyVO<>();
        // 填充自动/人工状态
        stopWatch.start("填充自动/人工状态");
        fillManualAutoMatic(result, infoCodeSetType, acccSystemId);
        stopWatch.stop();
        // 填充运行时间
        stopWatch.start("填充运行时间");
        fillRunningTime(result, equipRecords);
        stopWatch.stop();
        // 查询并填充干预结束时间
        stopWatch.start("查询并填充干预结束时间");
        fillManualIntervention(result, infoCodeSetType, acccSystemId);
        stopWatch.stop();

        ChillerUnitRunningPlanVO runningPlan = new ChillerUnitRunningPlanVO();
        runningPlan.setChillWaterDetaSetType(infoCodeSetType);
        // 查询并填充实际值和设定值
        stopWatch.start("查询并填充实际值和设定值");
        QuerySystemParameterVO systemParameter = dataBoardService.querySystemParameter(acccSystemId);
        fillRunningValue(runningPlan, infoCodeSetType, systemParameter);
        stopWatch.stop();

        // 获取大小机的自动设定反馈值
        stopWatch.start("获取大小机的自动设定反馈值");
        setChillerNum(projectId, acccSystemId, runningPlan, systemParameter);
        stopWatch.stop();

        result.setObjectId(acccSystem.getId());
        result.setObjectName(acccSystem.getName());
        result.setObjectLocalName(acccSystem.getLocalName());
        result.setRunStatus(objectIdAndRunStatus.get(acccSystemId));
        result.setRunningPlan(runningPlan);
        if (log.isDebugEnabled()) {
            LogExecutor.project(projectId).debug(log, LOG_TAG, "查询冷机运行策略结束",
                    Collections.singletonList(Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS))));
        }
        return result;
    }

    /**
     * 获取大小机的自动设定反馈值
     *
     * @param projectId    项目 ID
     * @param acccSystemId 系统 ID
     * @param vo           返回视图
     */
    private void setChillerNum(String projectId, String acccSystemId, ChillerUnitRunningPlanVO vo, QuerySystemParameterVO systemParamete) {

        // 查询系统下的冷机数量
        AcccccGroupDTO acccacGroup = bizDtpRwdService.queryAcccccGroupBySystemId(ContextUtil.getGroupCode(), projectId, acccSystemId);

        if (acccacGroup == null) {
            return;
        }

        Map<String, Integer> autoSetChillerNum = new HashMap<>();
        Map<String, Integer> feedbackChillerNum = new HashMap<>();

        // 本地大小机数量
        int lLockRun = 0;
        int sLockRun = 0;
        // 手动运行数量
        int lRun = 0;
        int sRun = 0;
        // 自动运行数量
        int lAiRun = 0;
        int sAiRun = 0;

        if (CollectionUtils.isNotEmpty(acccacGroup.getLarge())) {
            for (String item : acccacGroup.getLarge()) {
                lLockRun = lLockRun + chillerLockRun(item);
                lRun = lRun + chillerRun(item);
                lAiRun = lAiRun + feedbackChillerNum(item);
            }
        }
        if (CollectionUtils.isNotEmpty(acccacGroup.getSmall())) {
            for (String item : acccacGroup.getSmall()) {
                sLockRun = sLockRun + chillerLockRun(item);
                sRun = sRun + chillerRun(item);
                sAiRun = sAiRun + feedbackChillerNum(item);
            }
        }
        // 设定值：当前用户设置的冷机台数，控制台设置的结果
        Object lSet = systemParamete.getChillerLNum().getValue();
        int lSetInt = lSet != null ? Integer.parseInt((String) lSet) : 0;
        Object sSet = systemParamete.getChillerSNum().getValue();
        int sSetInt = sSet != null ? Integer.parseInt((String) sSet) : 0;

        // 自动设定值=设定值-当前手动数量
        // i. 当前手动数量：设备手动数量=本地运行的数量+手动运行的数量

        autoSetChillerNum.put(CoolerRuleTypeEnum.LARGE_COOLER.getCode(), negativeNumberProcessing(lSetInt - (lLockRun + lRun)));
        autoSetChillerNum.put(CoolerRuleTypeEnum.SMALL_COOLER.getCode(), negativeNumberProcessing(sSetInt - (sLockRun + sRun)));
        // 自动反馈值
        feedbackChillerNum.put(CoolerRuleTypeEnum.LARGE_COOLER.getCode(), negativeNumberProcessing(lAiRun));
        feedbackChillerNum.put(CoolerRuleTypeEnum.SMALL_COOLER.getCode(), negativeNumberProcessing(sAiRun));

        vo.setAutoSetChillerNum(autoSetChillerNum);
        vo.setFeedbackChillerNum(feedbackChillerNum);
    }

    /**
     * 负数处理 如果是负数 返回 0
     *
     * @return
     */
    private int negativeNumberProcessing(int i) {
        return Math.max(i, 0);
    }


    /**
     * 本地运行的数量：根据“本地远程状态(localRemoteStatus)=本地”且“运行状态(runStatus)=运行”进行数量统计
     *
     * @param item 设备 ID
     * @return 运行数量
     */
    private int chillerLockRun(String item) {
        FeedbackInfoMsgMqttVO localRemoteStatus = iotDataCacheService.queryIotRealDataVO(item, "localRemoteStatus", null);
        FeedbackInfoMsgMqttVO runStatus = iotDataCacheService.queryIotRealDataVO(item, "runStatus", null);
        if (localRemoteStatus != null && runStatus != null) {
            return StringUtils.equals(localRemoteStatus.getDataValue(), "0") && StringUtils.equals(runStatus.getDataValue(), "1") ? 1 : 0;
        }
        return 0;
    }

    /**
     * 手动运行的数量：根据“本地远程状态(localRemoteStatus)=远程”且“设备手自动状态(manualAutoStatus)=手动”且“启用命令(enableCtrl)=启用”。（备注说明：该种查询逻辑包含了设备已开启和处于开启中两种情况）
     *
     * @param item 设备 ID
     * @return 运行数量
     */
    private int chillerRun(String item) {
        FeedbackInfoMsgMqttVO localRemoteStatus = iotDataCacheService.queryIotRealDataVO(item, "localRemoteStatus", null);
        FeedbackInfoMsgMqttVO manualAutoStatus = iotDataCacheService.queryIotRealDataVO(item, "manualAutoStatus", null);
        FeedbackInfoMsgMqttVO enableCtrl = iotDataCacheService.queryIotRealDataVO(item, "enableCtrl", null);
        if (localRemoteStatus != null && manualAutoStatus != null && enableCtrl != null) {
            return StringUtils.equals(localRemoteStatus.getDataValue(), "1") &&
                    StringUtils.equals(manualAutoStatus.getDataValue(), "0") &&
                    StringUtils.equals(enableCtrl.getDataValue(), "1")
                    ? 1 : 0;
        }
        return 0;
    }

    /**
     * 自动反馈值：根据“本地远程状态(localRemoteStatus)=远程”且“设备手自动状态(manualAutoStatus)=自动”且“运行状态(runStatus)=运行”进行数量统计
     *
     * @param item 设备 ID
     * @return 运行数量
     */
    private int feedbackChillerNum(String item) {
        FeedbackInfoMsgMqttVO localRemoteStatus = iotDataCacheService.queryIotRealDataVO(item, "localRemoteStatus", null);
        FeedbackInfoMsgMqttVO manualAutoStatus = iotDataCacheService.queryIotRealDataVO(item, "manualAutoStatus", null);
        FeedbackInfoMsgMqttVO enableCtrl = iotDataCacheService.queryIotRealDataVO(item, "runStatus", null);
        if (localRemoteStatus != null && manualAutoStatus != null && enableCtrl != null) {
            return StringUtils.equals(localRemoteStatus.getDataValue(), "1") &&
                    StringUtils.equals(manualAutoStatus.getDataValue(), "1") &&
                    StringUtils.equals(enableCtrl.getDataValue(), "1")
                    ? 1 : 0;
        }
        return 0;
    }

    @Override
    public List<RunningStrategyVO<LastFreezePumpGroupRunningPlanVO>> listLastFreezePumpGroupRunningStrategy(
            @NonNull LastFreezePumpGroupRunningStrategyQueryParam param) {
        String projectId = param.getProjectId();
        LocalDateTime beginDateTime = param.getBeginDateTime();
        LocalDateTime endDateTime = param.getEndDateTime();
        DataRangeValidator.date().allowFutureTime().validate(beginDateTime, endDateTime);
        String acccSystemId = param.getAcccSystemId();


        StopWatch stopWatch = StopWatch.create("查询末级泵组运行策略");
        stopWatch.start("查询末级冷冻泵组");
        List<DtpRwdFreezePumpDTO> lastFreezePumpGroups = bizStrategyQueryService.listLastFreezePumpGroups(
                new StrategyFreezeGroupObjectQueryParam().setProjectId(projectId).setSystemId(acccSystemId));
        stopWatch.stop();

        stopWatch.start("查询末级泵组最近一次运行状态");
        if (CollectionUtils.isEmpty(lastFreezePumpGroups)) {
            return Lists.newArrayList();
        }
        Set<String> lastFreezePumpGroupIds = lastFreezePumpGroups.stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        // 查询末级泵组最近一次运行状态
        EquipRecordQueryDaoParam equipRecordQueryDaoParam = new EquipRecordQueryDaoParam()
                .setProjectId(projectCfg.getProjectId())
                .setGroupCode(projectCfg.getGroupCode())
                .setSystemCode(null).setSystemId(null)
                .setObjIds(lastFreezePumpGroupIds).setChangeContent(new ChangeContent().setInfoCode(
                        DashBoardInfoCodeEnum.ENABLE_CTRL.getCode())).setExecStatus(1);
        Map<String, RunStatusEnum> objectIdAndRunStatus =
                equipRecordService.mapRecentRunningStatus(equipRecordQueryDaoParam);
        stopWatch.stop();

        stopWatch.start("查询末级泵组运行记录");
        List<Integer> execStatuses = Lists.newArrayList(-1, 1);
        equipRecordQueryDaoParam.setBeginExecuteTime(beginDateTime).setEndExecuteTime(endDateTime);
        equipRecordQueryDaoParam.setExecStatus(null);
        equipRecordQueryDaoParam.setExecStatuses(execStatuses);
        List<EquipRecord> equipRecords = equipRecordService.list(equipRecordQueryDaoParam);
        Map<String, List<EquipRecord>> objectIdAndEquipRecords = CollectionUtils.isEmpty(equipRecords) ? new HashMap<>()
                : equipRecords.stream().collect(Collectors.groupingBy(EquipRecord::getObjId));
        stopWatch.stop();

        stopWatch.start("查询末级泵组供回水温差、压差策略干预结束时间");

        // 查询末级泵组供回水温差、压差策略干预结束时间
        List<ColdSiteSystemContrastVO> coldSiteSystemContrasts = dataBoardService.queryManualLastpumbParameter(acccSystemId);
        Map<String, List<ColdSiteSystemContrastVO>> objectIdAndContrasts = coldSiteSystemContrasts.stream()
                .collect(Collectors.groupingBy(ColdSiteSystemContrastVO::getObjectId));
        stopWatch.stop();

        stopWatch.start("查询结果整合");
        List<RunningStrategyVO<LastFreezePumpGroupRunningPlanVO>> result = new ArrayList<>();
        for (DtpRwdFreezePumpDTO lastFreezePumpGroup : lastFreezePumpGroups) {
            String lastFreezePumpGroupId = lastFreezePumpGroup.getId();
            String lastFreezePumpGroupName = lastFreezePumpGroup.getName();
            String lastFreezePumpGroupLocalName = lastFreezePumpGroup.getLocalName();
            DtpRwdFreezePumpGroupInfoCodeEnum infoCodeSetType = lastFreezePumpGroup.getInfoCodeSetType();
            String infoCodeUnit = lastFreezePumpGroup.getInfoCodeUnit();

            RunningStrategyVO<LastFreezePumpGroupRunningPlanVO> resultItem = new RunningStrategyVO<>();
            // 填充运行时间
            fillRunningTime(resultItem, objectIdAndEquipRecords.get(lastFreezePumpGroupId));
            // 填充自动/人工状态
            fillManualAutoMatic(resultItem, infoCodeSetType, lastFreezePumpGroupId);
            // 填充人工干预结束时间
            fillManualIntervention(resultItem, infoCodeSetType, lastFreezePumpGroupId);

            LastFreezePumpGroupRunningPlanVO runningPlan = new LastFreezePumpGroupRunningPlanVO();
            runningPlan.setBranchDetaSetType(infoCodeSetType);
            runningPlan.setBranchDetaSetUnit(infoCodeUnit);
            // 填充运行对比值
            fillRunningValue(runningPlan, infoCodeSetType, objectIdAndContrasts.get(lastFreezePumpGroupId));

            // 查询泵组下的设备
            runningPlan.setEquipmentList(dataBoardService.queryObjByFreezePumpGroupId(new QueryFreezePumpGroupParam(projectId, lastFreezePumpGroup.getId())));

            resultItem.setObjectId(lastFreezePumpGroupId);
            resultItem.setObjectName(lastFreezePumpGroupName);
            resultItem.setObjectLocalName(lastFreezePumpGroupLocalName);
            resultItem.setRunStatus(objectIdAndRunStatus.get(lastFreezePumpGroupId));
            resultItem.setRunningPlan(runningPlan);
            result.add(resultItem);
        }

        // 通过设备名称进行排序
        result.sort((o1, o2) -> ObjectUtils.compare(o1.getObjectName(), o2.getObjectName()));

        stopWatch.stop();

        if (log.isDebugEnabled()) {
            LogExecutor.project(projectId).info(log, LOG_TAG, "查询冷机运行策略结束",
                    Collections.singletonList(Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS))));
        }

        return result;
    }


    @Override
    public Boolean fetchSupportAi(StrategyQueryParam param) {
        try {
            CustomerResponse<Boolean> res = algorithmChillerPlantFeignClient.modelEvalResult(JSONObject.parseObject(JSON.toJSONString(param)));
            if (res != null && res.getContent() != null) {
                return res.getContent();
            }
        } catch (Exception e) {
            log.error("modelEvalResult error:{}", e.getMessage());
        }
        return false;
    }

    @Override
    public List<StrategyTypeChangeInsertParam.AiStrategyStatus> querySupportAi(StrategyAupportAiQueryParam param) {
        List<StrategyTypeChangeInsertParam.AiStrategyStatus> resList = new ArrayList<>();
        boolean algorithmConfig = abstractCommonServer.getAlgorithmConfig(ContextUtil.getGroupCode(), param.getProjectId());
        if (param.getStrategyType() == StrategyBusinessTypeEnum.LAST_FREEZE_PUMP_GROUP) {
            // 末级泵组需要查询泵组列表
            StrategyFreezeGroupObjectQueryParam queryParam = new StrategyFreezeGroupObjectQueryParam();
            queryParam.setProjectId(param.getProjectId());
            queryParam.setSystemIds(Lists.newArrayList(param.getSystemId()));
            List<DtpRwdFreezePumpDTO> objectVos = listLastFreezePumpGroups(queryParam);
            // 默认固定时间点策略
            for (DtpRwdFreezePumpDTO object : objectVos) {
                StrategyTypeChangeInsertParam.AiStrategyStatus aiStrategyStatus =
                        new StrategyTypeChangeInsertParam.AiStrategyStatus();
                aiStrategyStatus.setObjectId(object.getId());
                aiStrategyStatus.setLocalName(object.getLocalName());
                aiStrategyStatus.setIsAiStrategy(algorithmConfig);
                resList.add(aiStrategyStatus);
            }
        } else if (param.getStrategyType() == StrategyBusinessTypeEnum.CHILLER_UNIT ||
                param.getStrategyType() == StrategyBusinessTypeEnum.HOT_COLD_STATION
        ) {
            // 冷站策略
            StrategyTypeChangeInsertParam.AiStrategyStatus aiStrategyStatus =
                    new StrategyTypeChangeInsertParam.AiStrategyStatus();
            aiStrategyStatus.setObjectId(param.getSystemId());
            aiStrategyStatus.setIsAiStrategy(algorithmConfig);
            resList.add(aiStrategyStatus);
        }
        return resList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveSupportAi(StrategyTypeChangeInsertParam param) {
        RcBasicsConfig before = rcBasicsConfigService.lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, param.getProjectId())
                .eq(RcBasicsConfig::getSystemId, param.getSystemId())
                .eq(RcBasicsConfig::getType, BasicsConfigTypeEnum.STRATEGY_TYPE)
                .eq(RcBasicsConfig::getChildType, param.getStrategyType())
                .eq(RcBasicsConfig::getSt, StateTypeEnum.VALID)
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
        RcBasicsConfig basicsConfig = new RcBasicsConfig();
        if (before == null) {
            basicsConfig.setProjectId(param.getProjectId());
            basicsConfig.setSystemId(param.getSystemId());
            basicsConfig.setType(BasicsConfigTypeEnum.STRATEGY_TYPE);
            basicsConfig.setChildType(param.getStrategyType().getValue());
        } else {
            BeanUtils.copyProperties(before, basicsConfig);
        }
        basicsConfig.setResult(JSON.toJSONString(param.getStatusList()));
        rcBasicsConfigService.saveOrUpdateBasicsConfig(basicsConfig);

        if (param.getStrategyType().equals(StrategyBusinessTypeEnum.CHILLER_UNIT)) {
            // 冷站策略切换记录运行日志
//            saveRunningLog(before, basicsConfig);
            executor.execute(() -> {
                ContextUtil.setGroupCode(projectCfg.getGroupCode());
                ContextUtil.setTenant(projectCfg.getGroupCode());
                scheduleInstructService.updateScheduleInstruct(projectCfg.getGroupCode(),
                        param.getProjectId(), param.getSystemId(),
                        DtpRwdClassCodeEnum.COLD_SYSTEM.value(), param.getSystemId());
            });
        }
        return true;
    }

    /**
     * 查询中央供冷系统温差/压差设定值类型
     *
     * @param projectId    项目ID
     * @param acccSystemId 中央供冷系统ID
     * @return 中央供冷系统温差/压差设定值类型
     * @since v5.9.0
     */
    @Nullable
    private DtpRwdCentralCoolingInfoCodeEnum queryCentralCollingTempPressSetType(
            @NonNull String projectId, @NonNull String acccSystemId) {
        List<DtpRwdTwinObjectRuleResponse> twinObjectRules = dtpRwdClientService.listTwinObjectRule(
                Lists.newArrayList(new DtpRwdTwinObjectRuleQueryParam(DtpRwdClassCodeEnum.COLD_SYSTEM)));
        if (CollectionUtils.isEmpty(twinObjectRules)) {
            return null;
        }
        Map<String, List<String>> objectIdAndInfoCodes = new HashMap<>(twinObjectRules.size());
        twinObjectRules.forEach(item -> MapExecutor.dynamicPut(objectIdAndInfoCodes,
                item.getObjectId(), item.getInfoCode(), ArrayList::new));
        List<String> infoCodes = objectIdAndInfoCodes.get(acccSystemId);
        if (CollectionUtils.isEmpty(infoCodes)) {
            LogExecutor.instance().warn(log, LOG_TAG, "中央供冷系统无温差/压差实例化规则", Arrays.asList(
                    Pair.of("projectId", projectId),
                    Pair.of("acccSystemId", acccSystemId)
            ));
            return null;
        }
        DtpRwdCentralCoolingInfoCodeEnum result = null;
        if (infoCodes.contains(DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET.value())) {
            result = DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET;
        }
        if (infoCodes.contains(DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.value())) {
            result = DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET;
        }
        if (infoCodes.contains(DtpRwdCentralCoolingInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.value())) {
            result = DtpRwdCentralCoolingInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET;
        }
        return result;
    }

    /**
     * 填充人工干预结束时间
     *
     * @param target          目标源
     * @param infoCodeSetType 信息点类型
     * @param acccSystemId    数据对比
     * @since v5.9.0
     */
    private void fillManualIntervention(
            @NonNull RunningStrategyVO<ChillerUnitRunningPlanVO> target,
            @Nullable DtpRwdCentralCoolingInfoCodeEnum infoCodeSetType,
            @NonNull String acccSystemId) {
        // 查询并填充干预结束时间 大小机是一样的取其中一个作为时间
        queryManualInterventionAndFillData(target, acccSystemId,
                StrategyInfoCodeEnum.CHILLERNUM.getInfoCode(),
                StrategyInfoCodeEnum.CHILLERNUM.getInfoCode());
        queryManualInterventionAndFillData(target, acccSystemId,
                DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_OUT_TEMP_SET.value(),
                ChillerUnitRunningPlanVO.Fields.chillWaterOutTempSet);
        if (Objects.nonNull(infoCodeSetType)) {
            switch (infoCodeSetType) {
                case WORST_TERMINAL_DETA_PRESS_SET:
                    queryManualInterventionAndFillData(target, acccSystemId,
                            DtpRwdCentralCoolingInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.value(),
                            ChillerUnitRunningPlanVO.Fields.chillWaterDetaSet);
                    break;
                case CHILL_WATER_DETA_TEMP_SET:
                    queryManualInterventionAndFillData(target, acccSystemId,
                            DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.value(),
                            ChillerUnitRunningPlanVO.Fields.chillWaterDetaSet);
                    break;
                case CHILL_WATER_DETA_PRESS_SET:
                    queryManualInterventionAndFillData(target, acccSystemId,
                            DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET.value(),
                            ChillerUnitRunningPlanVO.Fields.chillWaterDetaSet);
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                            infoCodeSetType.getClass().getSimpleName(), infoCodeSetType);
            }
        }
    }

    /**
     * 填充人工干预结束时间
     *
     * @param target                目标源
     * @param infoCodeSetType       信息点类型
     * @param lastFreezePumpGroupId 数据对比
     * @since v5.9.0
     */
    private void fillManualIntervention(
            @NonNull RunningStrategyVO<LastFreezePumpGroupRunningPlanVO> target,
            @NonNull DtpRwdFreezePumpGroupInfoCodeEnum infoCodeSetType,
            @NonNull String lastFreezePumpGroupId) {
        // 查询并填充干预结束时间
        switch (infoCodeSetType) {
            case WORST_TERMINAL_DETA_PRESS_SET:
                queryManualInterventionAndFillData(target, lastFreezePumpGroupId,
                        DtpRwdFreezePumpGroupInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.value(),
                        LastFreezePumpGroupRunningPlanVO.Fields.branchDetaSet);
                break;
            case BRANCH_DETA_TEMP_SET:
                queryManualInterventionAndFillData(target, lastFreezePumpGroupId,
                        DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_TEMP_SET.value(),
                        LastFreezePumpGroupRunningPlanVO.Fields.branchDetaSet);
                break;
            case BRANCH_DETA_PRESS_SET:
                queryManualInterventionAndFillData(target, lastFreezePumpGroupId,
                        DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_PRESS_SET.value(),
                        LastFreezePumpGroupRunningPlanVO.Fields.branchDetaSet);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                        infoCodeSetType.getClass().getSimpleName(), infoCodeSetType);
        }
    }

    /**
     * 填充手自动状态
     *
     * @param target          目标源
     * @param infoCodeSetType 信息点类型
     * @param acccSystemId    数据对比
     * @since v5.9.0
     */
    private void fillManualAutoMatic(
            @NonNull RunningStrategyVO<ChillerUnitRunningPlanVO> target,
            @Nullable DtpRwdCentralCoolingInfoCodeEnum infoCodeSetType,
            @NonNull String acccSystemId) {
        // 查询并填充自动/人工状态
        queryManualAutoMaticAndFillData(target, acccSystemId,
                AutoMaticInfoCodeEnum.RUN_STATUS.getPracticalCode(),
                RunningStrategyVO.RUNNING_TYPE_OPEN_CLOSE);
        queryManualAutoMaticAndFillData(target, acccSystemId,
                StrategyInfoCodeEnum.CHILLERNUM.getInfoCode(),
                StrategyInfoCodeEnum.CHILLERNUM.getInfoCode());
        queryManualAutoMaticAndFillData(target, acccSystemId,
                DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_OUT_TEMP_SET.value(),
                ChillerUnitRunningPlanVO.Fields.chillWaterOutTempSet);
        if (Objects.nonNull(infoCodeSetType)) {
            switch (infoCodeSetType) {
                case WORST_TERMINAL_DETA_PRESS_SET:
                    queryManualAutoMaticAndFillData(target, acccSystemId,
                            DtpRwdCentralCoolingInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.value(),
                            ChillerUnitRunningPlanVO.Fields.chillWaterDetaSet);
                    break;
                case CHILL_WATER_DETA_TEMP_SET:
                    queryManualAutoMaticAndFillData(target, acccSystemId,
                            DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.value(),
                            ChillerUnitRunningPlanVO.Fields.chillWaterDetaSet);
                    break;
                case CHILL_WATER_DETA_PRESS_SET:
                    queryManualAutoMaticAndFillData(target, acccSystemId,
                            DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET.value(),
                            ChillerUnitRunningPlanVO.Fields.chillWaterDetaSet);
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                            infoCodeSetType.getClass().getSimpleName(), infoCodeSetType);
            }
        }
    }

    /**
     * 填充手自动状态
     *
     * @param target                目标源
     * @param infoCodeSetType       信息点类型
     * @param lastFreezePumpGroupId 数据对比
     * @since v5.9.0
     */
    private void fillManualAutoMatic(
            @NonNull RunningStrategyVO<LastFreezePumpGroupRunningPlanVO> target,
            @NonNull DtpRwdFreezePumpGroupInfoCodeEnum infoCodeSetType,
            @NonNull String lastFreezePumpGroupId) {
        // 查询并填充自动/人工状态
        queryManualAutoMaticAndFillData(target, lastFreezePumpGroupId,
                AutoMaticInfoCodeEnum.ENABLE_CTRL.getPracticalCode(),
                RunningStrategyVO.RUNNING_TYPE_OPEN_CLOSE);
        switch (infoCodeSetType) {
            case WORST_TERMINAL_DETA_PRESS_SET:
                queryManualAutoMaticAndFillData(target, lastFreezePumpGroupId,
                        DtpRwdFreezePumpGroupInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.value(),
                        LastFreezePumpGroupRunningPlanVO.Fields.branchDetaSet);
                break;
            case BRANCH_DETA_TEMP_SET:
                queryManualAutoMaticAndFillData(target, lastFreezePumpGroupId,
                        DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_TEMP_SET.value(),
                        LastFreezePumpGroupRunningPlanVO.Fields.branchDetaSet);
                break;
            case BRANCH_DETA_PRESS_SET:
                queryManualAutoMaticAndFillData(target, lastFreezePumpGroupId,
                        DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_PRESS_SET.value(),
                        LastFreezePumpGroupRunningPlanVO.Fields.branchDetaSet);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                        infoCodeSetType.getClass().getSimpleName(), infoCodeSetType);
        }
    }

    /**
     * 填充运行对比值
     *
     * @param target          目标源
     * @param infoCodeSetType 信息点类型
     * @param systemParameter 系统参数
     * @since v5.9.0
     */
    private void fillRunningValue(
            @NonNull ChillerUnitRunningPlanVO target,
            @Nullable DtpRwdCentralCoolingInfoCodeEnum infoCodeSetType,
            @Nullable QuerySystemParameterVO systemParameter) {
        if (Objects.isNull(systemParameter)) {
            return;
        }
        // 查询并填充实际值和设定值
        analyzeRunningValue(systemParameter.getChillerLNum(), Integer.class, target::setChillerLNum);
        analyzeRunningValue(systemParameter.getChillerSNum(), Integer.class, target::setChillerSNum);
        analyzeRunningValue(systemParameter.getPrimaryChillWaterMainSupplyTemp(),
                BigDecimal.class, target::setChillWaterOutTempSet);
        if (Objects.nonNull(infoCodeSetType)) {
            switch (infoCodeSetType) {
                case WORST_TERMINAL_DETA_PRESS_SET:
                    analyzeRunningValue(systemParameter.getWorstTerminalDetaPress(),
                            BigDecimal.class, target::setChillWaterDetaSet);
                    target.setChillWaterDetaSetUnit("kPa");
                    break;
                case CHILL_WATER_DETA_TEMP_SET:
                    analyzeRunningValue(systemParameter.getChillWaterDetaTemp(),
                            BigDecimal.class, target::setChillWaterDetaSet);
                    break;
                case CHILL_WATER_DETA_PRESS_SET:
                    analyzeRunningValue(systemParameter.getChillWaterDetaPress(),
                            BigDecimal.class, target::setChillWaterDetaSet);
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                            infoCodeSetType.getClass().getSimpleName(), infoCodeSetType);
            }
        }
    }

    /**
     * 填充运行对比值
     *
     * @param target          目标源
     * @param infoCodeSetType 信息点类型
     * @param contrasts       数据对比
     * @since v5.9.0
     */
    private void fillRunningValue(
            @NonNull LastFreezePumpGroupRunningPlanVO target,
            @NonNull DtpRwdFreezePumpGroupInfoCodeEnum infoCodeSetType,
            @Nullable List<ColdSiteSystemContrastVO> contrasts) {
        if (CollectionUtils.isEmpty(contrasts)) {
            return;
        }
        // 查询并填充实际值和设定值
        Map<String, ColdSiteSystemContrastVO> infoCodeAndContrast = contrasts.stream().collect(Collectors.toMap(
                ColdSiteSystemContrastVO::getInfoCode, Function.identity(), (o1, o2) -> o1));
        switch (infoCodeSetType) {
            case WORST_TERMINAL_DETA_PRESS_SET:
                analyzeRunningValue(infoCodeAndContrast.get(DtpRwdFreezePumpGroupInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.value()),
                        BigDecimal.class,
                        target::setBranchDetaSet);
                target.setBranchDetaSetUnit("kPa");
                break;
            case BRANCH_DETA_TEMP_SET:
                analyzeRunningValue(infoCodeAndContrast.get(DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_TEMP_SET.value()),
                        BigDecimal.class,
                        target::setBranchDetaSet);
                break;
            case BRANCH_DETA_PRESS_SET:
                analyzeRunningValue(infoCodeAndContrast.get(DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_PRESS_SET.value()),
                        BigDecimal.class,
                        target::setBranchDetaSet);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                        infoCodeSetType.getClass().getSimpleName(), infoCodeSetType);
        }
    }

    /**
     * 查询并填充字段名称和策略干预时间
     *
     * @param target    目标源
     * @param objectId  对象ID
     * @param infoCode  信息点编码
     * @param fieldName 字段名称, 为 {@code null} 时则使用信息点编码值
     * @since v5.9.0
     */
    @Override
    public void queryManualInterventionAndFillData(
            @NonNull RunningStrategyVO<?> target, @NonNull String objectId,
            @NonNull String infoCode, @Nullable String fieldName) {
        CommandExecuteDTO queryManualInterventionParam = new CommandExecuteDTO();
        queryManualInterventionParam.setType(SourceFromEnum.ARTIFICIAL);
        queryManualInterventionParam.setObjectId(objectId);
        queryManualInterventionParam.setInfoCode(infoCode);
        LocalDateTime timeDuration = manualAutoMaticSetService.queryManualIntervention(queryManualInterventionParam.getObjectId(), queryManualInterventionParam.getInfoCode());
        if (Objects.isNull(timeDuration)) {
            return;
        }

        Map<String, LocalDateTime> fieldNameAndInterveneEndTime = Optional.ofNullable(
                target.getFieldNameAndInterveneEndTime()).orElseGet(HashMap::new);
        fieldName = StringUtils.defaultString(fieldName, infoCode);
        fieldNameAndInterveneEndTime.put(fieldName, timeDuration);
        target.setFieldNameAndInterveneEndTime(fieldNameAndInterveneEndTime);
    }

    /**
     * 查询自动人工标签并填充数据
     *
     * @param target    目标源
     * @param objectId  对象ID
     * @param infoCode  信息点编码
     * @param fieldName 字段名称, 为 {@code null} 时则使用信息点编码值
     * @since v5.9.0
     */
    @Override
    public void queryManualAutoMaticAndFillData(
            @NonNull RunningStrategyVO<?> target, @NonNull String objectId,
            @NonNull String infoCode, @Nullable String fieldName) {
        boolean manualAutoMatic = manualAutoMaticSetService.getManualAutoMatic(objectId, infoCode);
        RunningLogTypeEnum runningType = toRunningType(manualAutoMatic);

        Map<String, RunningLogTypeEnum> fieldNameAndRunningType = Optional.ofNullable(
                target.getFieldNameAndRunningType()).orElseGet(HashMap::new);
        fieldName = StringUtils.defaultString(fieldName, infoCode);
        fieldNameAndRunningType.put(fieldName, runningType);
        target.setFieldNameAndRunningType(fieldNameAndRunningType);
    }

    /**
     * 填充运行值
     *
     * @param source            数据源
     * @param runningValueClass 运行值类型
     * @param callback          解析成功回调函数
     * @param <T>               运行值类型
     * @since v5.9.0
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void analyzeRunningValue(
            @Nullable ColdSiteSystemContrastVO source,
            @NonNull Class<T> runningValueClass,
            @NonNull Consumer<RunningValueVO<T>> callback) {
        RunningValueVO<T> runningValueVO = new RunningValueVO<>();
        if (Objects.isNull(source)) {
            callback.accept(runningValueVO);
            return;
        }
        Object actualValue = source.getActualValue();
        Object value = source.getValue();
        if (ClassUtils.isAssignable(runningValueClass, String.class)) {
            ObjectExecutor.notNullExecute(actualValue, () ->
                    runningValueVO.setActualValue((T) actualValue.toString()));
            ObjectExecutor.notNullExecute(value, () ->
                    runningValueVO.setSettingValue((T) value.toString()));
        } else if (ClassUtils.isAssignable(runningValueClass, Integer.class)) {
            ObjectExecutor.notNullExecute(actualValue, () ->
                    runningValueVO.setActualValue((T) Integer.valueOf(actualValue.toString())));
            ObjectExecutor.notNullExecute(value, () ->
                    runningValueVO.setSettingValue((T) Integer.valueOf(value.toString())));
        } else if (ClassUtils.isAssignable(runningValueClass, BigDecimal.class)) {
            ObjectExecutor.notNullExecute(actualValue, () ->
                    runningValueVO.setActualValue((T) new BigDecimal(actualValue.toString())));
            ObjectExecutor.notNullExecute(value, () ->
                    runningValueVO.setSettingValue((T) new BigDecimal(value.toString())));
        } else {
            runningValueVO.setActualValue((T) actualValue);
            runningValueVO.setSettingValue((T) value);
        }
        callback.accept(runningValueVO);
    }

    /**
     * 填充运行时间
     *
     * @param target       目标源
     * @param equipRecords 设备执行记录
     * @since v5.8.0
     */
    @Override
    public <T> void fillRunningTime(@NonNull RunningStrategyVO<T> target, @NonNull List<EquipRecord> equipRecords) {
        if (CollectionUtils.isEmpty(equipRecords)) {
            return;
        }
        DateTimeFormatter dateTimeFormatter = DatePattern.PURE_DATETIME_FORMATTER;
        equipRecords = equipRecords.stream()
                .filter(item -> Objects.nonNull(item.getChangeContent()))
                .sorted(Comparator.comparing(equipRecord -> LocalDateTime.parse(
                        equipRecord.getChangeContent().getExecTime(), dateTimeFormatter)))
                .collect(Collectors.toList());

        List<RunningTimeVO> openCloseTimes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        LocalDate today = LocalDate.now();
        TreeSet<LocalDateTime> openDateTimes = new TreeSet<>();
        TreeSet<LocalDateTime> closeDateTimes = new TreeSet<>();
        for (EquipRecord equipRecord : equipRecords) {
            ChangeContent changeContent = equipRecord.getChangeContent();
            String execTime = changeContent.getExecTime();
            String infoValue = changeContent.getInfoValue();
            boolean isAi = BooleanUtils.isTrue(changeContent.getAi());
            boolean manualAutoMatic = manualAutoMaticSetService.getManualAutoMatic(equipRecord.getObjId(), equipRecord.getInfoCode());
            // 如果是 人工模式 未来的ai指令不显示
            LocalDateTime localDateTime = LocalDateTime.parse(execTime, DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));
            if (!manualAutoMatic && isAi && localDateTime.isAfter(now)) {
                continue;
            }

            LocalDateTime executeTime = LocalDateTime.parse(execTime, dateTimeFormatter);
            LocalDate executeDate = executeTime.toLocalDate();
            boolean isOpen = StringUtils.equals(infoValue, "1");
            if (today.isEqual(executeDate)) {
                boolean r = isOpen ? openDateTimes.add(executeTime) : closeDateTimes.add(executeTime);
            }
            openCloseTimes.add(new RunningTimeVO().setDateTime(executeTime)
                    .setRunStatus(isOpen ? RunStatusEnum.OPEN : RunStatusEnum.CLOSE)
                    .setRunType(toRunningType(changeContent.getAi())));
        }

        // 当关闭时间在开启时间之前, 关闭时间设置为空
        LocalDateTime openDateTime = openDateTimes.size() == 0 ? null : openDateTimes.first();
        LocalDateTime closeDateTime = closeDateTimes.size() == 0 ? null : closeDateTimes.last();
        if (Objects.nonNull(openDateTime) && Objects.nonNull(closeDateTime) && !closeDateTime.isAfter(openDateTime)) {
            closeDateTime = null;
        }
        target.setOpenDateTime(openDateTime);
        target.setCloseDateTime(closeDateTime);
        target.setOpenCloseTimes(openCloseTimes);
    }

    /**
     * 转换运行类型
     *
     * @param isAi 是否AI
     * @return 运行类型
     * @since v5.8.0
     */
    private RunningLogTypeEnum toRunningType(@NonNull Boolean isAi) {
        return BooleanUtils.isTrue(isAi) ? RunningLogTypeEnum.AUTO : RunningLogTypeEnum.MANUAL;
    }

    @Override
    public RunningCoolerVO getRunningChillerData(String projectId, String systemId) {
        ParameterCheck.isNull(ContextUtil.getGroupCode(), LocaleMessageUtil.getMessage("key_c0659325aa57425499811b4a531f4315"));
        RunningCoolerVO runningCoolerVO = new RunningCoolerVO();

        // 判断当前系统类型
        List<JSONObject> systemInfos = this.bizDtpRwdService.queryObjectList(ContextUtil.getGroupCode(), projectId, Lists.newArrayList(systemId));
        String systemClassCode = CollectionUtils.isEmpty(systemInfos) ? null : systemInfos.get(0).getString("classCode");
        if (StringUtils.isBlank(systemClassCode)) {
            return runningCoolerVO;
        }

        // 冷站
        if (DtpRwdSystemClassCodeEnum.ACCC.value().equals(systemClassCode)) {
            // 查询额定制冷量
            List<Integer> ratedCoolList = getRate(ContextUtil.getGroupCode(), projectId, systemId);
            if (CollectionUtils.isEmpty(ratedCoolList)) {
                return runningCoolerVO;
            }
            ratedCoolList = ratedCoolList.stream().sorted(Comparator.comparing(Integer::intValue)).collect(Collectors.toList());
            runningCoolerVO.setRatedCoolMax(new BigDecimal(ratedCoolList.get(ratedCoolList.size() - 1)));
            runningCoolerVO.setRatedCoolMin(new BigDecimal(ratedCoolList.get(0)));
            // 查询当前运行的冷机台数
            AcccccGroupDTO acccacGroup = bizDtpRwdService.queryAcccccGroupBySystemId(ContextUtil.getGroupCode(), projectId, systemId);
            if (CollectionUtils.isNotEmpty(acccacGroup.getSmall())) {
                for (String s : acccacGroup.getSmall()) {
                    Boolean isRunning = queryObjectIsRunning(s);
                    if (isRunning) {
                        runningCoolerVO.setRunningChillerSNum(runningCoolerVO.getRunningChillerSNum() + 1);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(acccacGroup.getLarge())) {
                for (String l : acccacGroup.getLarge()) {
                    Boolean isRunning = queryObjectIsRunning(l);
                    if (isRunning) {
                        runningCoolerVO.setRunningChillerLNum(runningCoolerVO.getRunningChillerLNum() + 1);
                    }
                }
            }
        } else if (DtpRwdSystemClassCodeEnum.ACCI.value().equals(systemClassCode)) {
            // 获取冷热站下的空气源热泵控制组
            DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
            param.setClassCodeEnum(DtpRwdClassCodeEnum.ACAHC_GROUP);
            param.setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(systemId));
            // 查询系统下的空气源热泵控制组
            List<DtpRwdObjectDTO> lstAcahc = dtpRwdClientService.listObject(true, false, projectId, param);
            if (CollectionUtils.isEmpty(lstAcahc)) {
                return runningCoolerVO;
            }
            int currentChillerLNum = 0;
            for (DtpRwdObjectDTO dtpRwdObjectDTO : lstAcahc) {
                Boolean running = this.queryObjectIsRunning(dtpRwdObjectDTO.getId());
                if (BooleanUtil.isTrue(running)) {
                    currentChillerLNum++;
                }
            }
            runningCoolerVO.setRunningChillerLNum(currentChillerLNum);
            runningCoolerVO.setRunningChillerSNum(0);
        } else if (DtpRwdSystemClassCodeEnum.ACCH.value().equals(systemClassCode)) {
            // 获取当前热站对应的空调换热器
            DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
            param.setClassCodeEnum(DtpRwdClassCodeEnum.ACGEHE);
            param.setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(systemId));
            List<DtpRwdObjectDTO> lstAcgehe = dtpRwdClientService.listObject(true, false, projectId, param);
            if (CollectionUtils.isEmpty(lstAcgehe)) {
                return runningCoolerVO;
            }
            int currentChillerLNum = 0;
            for (DtpRwdObjectDTO dtpRwdObjectDTO : lstAcgehe) {
                Boolean running = this.queryObjectIsRunning(dtpRwdObjectDTO.getId());
                if (BooleanUtil.isTrue(running)) {
                    currentChillerLNum++;
                }
            }
            runningCoolerVO.setRunningChillerLNum(currentChillerLNum);
            runningCoolerVO.setRunningChillerSNum(0);
        }
        return runningCoolerVO;
    }

    /**
     * 判断设备当前状态是否是运行
     *
     * @param objectId 对象ID
     * @return 设备当前是否运行
     */
    @Override
    public Boolean queryObjectIsRunning(String objectId) {
        String runStatus = iotDataCacheService.queryIotRealDataForFiveMin(objectId, "runStatus", null);
        String onlineStatus = iotDataCacheService.queryIotRealDataForFiveMin(objectId, "onlineStatus", null);
        return "1".equals(runStatus) && "1".equals(onlineStatus);
    }

    /**
     * 查询设备额定功率
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param systemId  系统ID
     * @return 设备额定功率
     * @since v5.6.0
     */
    @Override
    @Nullable
    @Cacheable(cacheNames = "getRate", key = "#groupCode+#projectId+#systemId+'-30m'", cacheManager = CacheKey.CAFFEINE_CACHE_MANAGER, sync = true)
    public List<Integer> getRate(String groupCode, String projectId, String systemId) {
        List<JSONObject> acccacList = bizDtpRwdService.queryObjectList(groupCode, projectId,
                Lists.newArrayList(DtpRwdClassCodeEnum.ACCCCC.getValue(), DtpRwdClassCodeEnum.ACCCSC.getValue()),
                DtpRwdRelationTypeEnum.EQ_IN_SYSTEM.value().getLeft(), DtpRwdRelationTypeEnum.EQ_IN_SYSTEM.value().getRight(),
                Lists.newArrayList(systemId), null);
        return CollectionUtils.isEmpty(acccacList) ? null : acccacList.stream().filter(x -> x.getInteger("ratedCool") != null).map(x -> x.getInteger("ratedCool")).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
