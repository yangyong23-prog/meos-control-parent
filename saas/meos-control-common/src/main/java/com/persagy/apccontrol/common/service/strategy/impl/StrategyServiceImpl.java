package com.persagy.apccontrol.common.service.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.centralcooling.DtpRwdCentralCoolingInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.freezepumpgroup.DtpRwdFreezePumpGroupInfoCodeEnum;
import com.persagy.apccontrol.basic.model.param.strategy.QueryFreezePumpGroupParam;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFreezeGroupObjectQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.ChillerRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.lastfreezepumpgroup.LastFreezePumpGroupRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.ColdSiteSystemContrastVO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningValueVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.ChillerUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.LastFreezePumpGroupRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.system.QuerySystemParameterVO;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizStrategyQueryService;
import com.persagy.apccontrol.basic.structure.iot.IotConverter;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.common.service.strategy.DataBoardService;
import com.persagy.apccontrol.common.service.strategy.StrategyService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import com.persagy.validate.utility.executor.ObjectExecutor;
import com.persagy.validate.utility.validate.range.DataRangeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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

    private final DtpRwdClientService dtpRwdClientService;
    private final BizStrategyQueryService bizStrategyQueryService;
    private final AbstractCommonServer abstractCommonServer;
    private final DataBoardService dataBoardService;
    private final IotConverter iotConverter;

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
        RunStatusEnum runStatusEnum = queryColdStationRunningStatus(acccSystemId);
        stopWatch.stop();


        // 查询冷站温差/压差设定值类型
        stopWatch.start("查询冷站温差/压差设定值类型");
        DtpRwdCentralCoolingInfoCodeEnum infoCodeSetType = queryCentralCollingTempPressSetType(projectId, acccSystemId);
        stopWatch.stop();

        RunningStrategyVO<ChillerUnitRunningPlanVO> result = new RunningStrategyVO<>();

        ChillerUnitRunningPlanVO runningPlan = new ChillerUnitRunningPlanVO();
        runningPlan.setChillWaterDetaSetType(infoCodeSetType);
        // 查询并填充实际值和设定值
        stopWatch.start("查询并填充实际值和设定值");

        QuerySystemParameterVO systemParameter = dataBoardService.querySystemParameter(acccSystemId);
        fillRunningValue(runningPlan, infoCodeSetType, systemParameter);
        stopWatch.stop();

        result.setObjectId(acccSystem.getId());
        result.setObjectName(acccSystem.getName());
        result.setObjectLocalName(acccSystem.getLocalName());
        result.setRunStatus(runStatusEnum);
        result.setRunningPlan(runningPlan);
        if (log.isDebugEnabled()) {
            LogExecutor.project(projectId).debug(log, LOG_TAG, "查询冷机运行策略结束",
                    Collections.singletonList(Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS))));
        }
        return result;
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

        // 数据时间回退值（面板更新时间展示；latest 语义下优先取实际数据时间）
        LocalDateTime theLast30Minutes = DateUtils.getTheLast15Minus15Minutes();
        // 查询末级泵组最近一次运行状态（latest 双条件门控，未命中回退 15min 查询）
        List<InfoCodeValueVO> infoCodeValues = this.queryObjectIdLatestPersistentDataList(
                lastFreezePumpGroupIds,
                Collections.singletonList(InfoCodeEnum.RUN_STATUS.value()));

        Map<String, RunStatusEnum> objectIdAndRunStatus = new HashMap<>();
        if (CollectionUtils.isNotEmpty(infoCodeValues)) {
            // 每个泵组取自身最新一条运行状态
            infoCodeValues.forEach(x -> {
                RunStatusEnum runStatusEnum = null;
                Object infoCodeValue = x.getInfoCodeValue();
                if (Objects.nonNull(infoCodeValue)) {
                    runStatusEnum = StringUtils.equals(String.valueOf(infoCodeValue), "1")
                            ? RunStatusEnum.OPEN : RunStatusEnum.CLOSE;
                }
                objectIdAndRunStatus.put(x.getObjectId(), runStatusEnum);
            });

        }
        stopWatch.stop();


        stopWatch.start("查询末级泵组供回水温差、压差策略干预结束时间");

        // 查询末级泵组供回水温差、压差策略干预结束时间
        List<ColdSiteSystemContrastVO> coldSiteSystemContrasts =
                dataBoardService.queryManualLastpumbParameter(acccSystemId);
        Map<String, List<ColdSiteSystemContrastVO>> objectIdAndContrasts = coldSiteSystemContrasts.stream()
                .collect(Collectors.groupingBy(ColdSiteSystemContrastVO::getObjectId));
        stopWatch.stop();

        stopWatch.start("查询结果整合");
        // 面板更新时间取本次实际数据时间，无数据时回退最近窗口刻度
        LocalDateTime latestDataTime = infoCodeValues.stream()
                .map(InfoCodeValueVO::getDataTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(theLast30Minutes);
        List<RunningStrategyVO<LastFreezePumpGroupRunningPlanVO>> result = new ArrayList<>();
        for (DtpRwdFreezePumpDTO lastFreezePumpGroup : lastFreezePumpGroups) {
            String lastFreezePumpGroupId = lastFreezePumpGroup.getId();
            String lastFreezePumpGroupName = lastFreezePumpGroup.getName();
            String lastFreezePumpGroupLocalName = lastFreezePumpGroup.getLocalName();
            DtpRwdFreezePumpGroupInfoCodeEnum infoCodeSetType = lastFreezePumpGroup.getInfoCodeSetType();
            String infoCodeUnit = lastFreezePumpGroup.getInfoCodeUnit();

            RunningStrategyVO<LastFreezePumpGroupRunningPlanVO> resultItem = new RunningStrategyVO<>();

            LastFreezePumpGroupRunningPlanVO runningPlan = new LastFreezePumpGroupRunningPlanVO();
            runningPlan.setBranchDetaSetType(infoCodeSetType);
            runningPlan.setBranchDetaSetUnit(infoCodeUnit);
            // 填充运行对比值
            fillRunningValue(runningPlan, infoCodeSetType, objectIdAndContrasts.get(lastFreezePumpGroupId));

            // 查询泵组下的设备
            runningPlan.setEquipmentList(dataBoardService.queryObjByFreezePumpGroupId(
                    new QueryFreezePumpGroupParam(projectId, lastFreezePumpGroup.getId())));

            resultItem.setObjectId(lastFreezePumpGroupId);
            resultItem.setObjectName(lastFreezePumpGroupName);
            resultItem.setObjectLocalName(lastFreezePumpGroupLocalName);
            resultItem.setRunStatus(objectIdAndRunStatus.get(lastFreezePumpGroupId));
            resultItem.setRunningPlan(runningPlan);
            resultItem.setUpdateTime(latestDataTime);
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
    public RunStatusEnum queryColdStationRunningStatus(String acccSystemId) {
        // latest 双条件门控下查询最新一条运行状态，未命中回退 15min 查询
        List<InfoCodeValueVO> infoCodeValues = this.queryObjectIdLatestPersistentDataList(
                Collections.singletonList(acccSystemId),
                Collections.singletonList(AutoMaticInfoCodeEnum.RUN_STATUS.getPracticalCode()));

        RunStatusEnum runStatusEnum = null;
        if (CollectionUtils.isNotEmpty(infoCodeValues)) {
            InfoCodeValueVO infoCodeValueVO = infoCodeValues.get(0);
            Object infoCodeValue = infoCodeValueVO.getInfoCodeValue();
            if (Objects.nonNull(infoCodeValue)) {
                runStatusEnum = StringUtils.equals(String.valueOf(infoCodeValue), "1")
                        ? RunStatusEnum.OPEN : RunStatusEnum.CLOSE;
            }

        }
        return runStatusEnum;
    }

    /**
     * 查询对象信息点的最新持久化数据（分精度改造 latest 双条件门控）
     * <p>双条件（请求头 removeDefaultIpType=true 且壳配置持久化数据查询逻辑=latest）命中时查询"距离当前最新"的数据，
     * 否则由底层回退为 15min + 前端替换参数查询，与改造前行为一致；
     * moment 查询按 对象+信息点 返回最新一条，此处直接转换消费</p>
     *
     * @param objectIds 对象ID集合
     * @param infoCodes 信息点编码集合
     * @return 最新持久化数据集合
     * @since v7.260901.0
     */
    private List<InfoCodeValueVO> queryObjectIdLatestPersistentDataList(
            @NonNull Collection<String> objectIds, @NonNull Collection<String> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Lists.newArrayList();
        }
        List<EnergyDataResponse> responses = this.dtpRwdClientService.queryIpDataOnMoment(
                ContextUtil.getGroupCode(), ContextUtil.getProjectId(),
                new ArrayList<>(objectIds), new ArrayList<>(infoCodes));
        if (CollectionUtils.isEmpty(responses)) {
            return Lists.newArrayList();
        }
        return responses.stream()
                .map(this.iotConverter::toInfoCodeValueVo)
                .collect(Collectors.toList());
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
    @SuppressWarnings({"DuplicatedCode"})
    private DtpRwdCentralCoolingInfoCodeEnum queryCentralCollingTempPressSetType(
            @NonNull String projectId, @NonNull String acccSystemId) {
        // 获取绑点规则
        List<ObjectsExistRule> objectsExistRules = this.abstractCommonServer.queryObjectsExistRule(
                ContextUtil.getGroupCode(), projectId, Lists.newArrayList(new ClassCode(DtpRwdClassCodeEnum.COLD_SYSTEM.value())));
        if (CollectionUtil.isEmpty(objectsExistRules)) {
            return null;
        }
        Map<String, List<String>> objectIdAndInfoCodes = new HashMap<>(objectsExistRules.size());
        objectsExistRules.forEach(item -> MapExecutor.dynamicPut(objectIdAndInfoCodes,
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
                analyzeRunningValue(infoCodeAndContrast.get(DtpRwdFreezePumpGroupInfoCodeEnum.
                        WORST_TERMINAL_DETA_PRESS_SET.value()), BigDecimal.class, target::setBranchDetaSet);
                target.setBranchDetaSetUnit("kPa");
                break;
            case BRANCH_DETA_TEMP_SET:
                analyzeRunningValue(infoCodeAndContrast.get(DtpRwdFreezePumpGroupInfoCodeEnum.
                        BRANCH_DETA_TEMP_SET.value()), BigDecimal.class, target::setBranchDetaSet);
                break;
            case BRANCH_DETA_PRESS_SET:
                analyzeRunningValue(infoCodeAndContrast.get(DtpRwdFreezePumpGroupInfoCodeEnum.
                        BRANCH_DETA_PRESS_SET.value()), BigDecimal.class, target::setBranchDetaSet);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                        infoCodeSetType.getClass().getSimpleName(), infoCodeSetType);
        }
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
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    private <T> void analyzeRunningValue(
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

}
