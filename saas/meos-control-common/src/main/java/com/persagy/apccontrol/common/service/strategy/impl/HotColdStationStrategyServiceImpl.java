package com.persagy.apccontrol.common.service.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.centralcooling.DtpRwdCentralCoolingInfoCodeEnum;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.HotColdStationRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.ColdSiteSystemContrastVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningValueVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.HotColdStationUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.QueryHotColdStationSystemParameterVO;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.common.service.strategy.DataBoardService;
import com.persagy.apccontrol.common.service.strategy.HotColdStationStrategyService;
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
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

/**
 * 冷热站时间表
 *
 * @author yuyao
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class HotColdStationStrategyServiceImpl implements HotColdStationStrategyService {

    private static final String LOG_TAG = "策略服务";

    private final DtpRwdClientService dtpRwdClientService;
    private final DataBoardService dataBoardService;
    private final StrategyService strategyService;
    private final AbstractCommonServer abstractCommonServer;

    @Override
    public RunningStrategyVO<HotColdStationUnitRunningPlanVO> listChillerRunningStrategy(HotColdStationRunningStrategyQueryParam param) {
        String projectId = param.getProjectId();
        LocalDateTime beginDateTime = param.getBeginDateTime();
        LocalDateTime endDateTime = param.getEndDateTime();
        DataRangeValidator.date().allowFutureTime().validate(beginDateTime, endDateTime);
        String acccSystemId = param.getSystemId();

        DtpRwdObjectDTO acccSystem = dtpRwdClientService.getObject(true, true,
                projectId, new DtpRwdObjectQueryParam().setId(acccSystemId));

        // 查询最近一次运行状态
        RunStatusEnum acccSystemRunStatusEnum = strategyService.queryColdStationRunningStatus(acccSystemId);

        // 查询温差/压差设定值类型
        DtpRwdCentralCoolingInfoCodeEnum infoCodeSetType = queryCentralCollingTempPressSetType(projectId, acccSystemId);

        RunningStrategyVO<HotColdStationUnitRunningPlanVO> result = new RunningStrategyVO<>();

        HotColdStationUnitRunningPlanVO runningPlan = new HotColdStationUnitRunningPlanVO();
        runningPlan.setChillWaterDetaSetType(infoCodeSetType);
        // 查询并填充实际值和设定值
        QueryHotColdStationSystemParameterVO systemParameter = dataBoardService.queryHotColdStationSystemParameter(
                acccSystemId, DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.value());
        fillRunningValue(runningPlan, infoCodeSetType, systemParameter);

        //查询壳配置默认值
        runningPlan.setPolicySiteColdHotColdTempSetCfg(getShellConfigByInfoCode(ShellConfigItemEnum.POLICY_SITE_COLDHOT_COLD_TEMP_SET, acccSystemId));
        runningPlan.setPolicySiteColdHotHeatTempSetCfg(getShellConfigByInfoCode(ShellConfigItemEnum.POLICY_SITE_COLDHOT_HEAT_TEMP_SET, acccSystemId));

        result.setObjectId(acccSystem.getId());
        result.setObjectName(acccSystem.getName());
        result.setObjectLocalName(acccSystem.getLocalName());
        result.setRunStatus(acccSystemRunStatusEnum);
        result.setRunningPlan(runningPlan);
        return result;
    }

    /**
     * @description: 通过信息点编码获取可配置信息
     * @param: shellConfigItemEnum 壳配值枚举
     * @return: 对应的壳配置的值
     * @author moshan
     * @date: 2023/12/29 15:46
     */
    public String getShellConfigByInfoCode(ShellConfigItemEnum shellConfigItemEnum, String systemId) {
        return this.abstractCommonServer.getShellConfig(ContextUtil.getGroupCode(), ContextUtil.getProjectId(),
                systemId, shellConfigItemEnum.getCode(), null);
    }

    /**
     * 查询系统温差/压差设定值类型
     *
     * @param projectId    项目ID
     * @param acccSystemId 系统ID
     * @return 系统温差/压差设定值类型
     * @since v5.9.0
     */
    @Nullable
    @SuppressWarnings({"DuplicatedCode"})
    private DtpRwdCentralCoolingInfoCodeEnum queryCentralCollingTempPressSetType(
            @NonNull String projectId, @NonNull String acccSystemId) {
        List<ObjectsExistRule> objectsExistRules = this.abstractCommonServer.queryObjectsExistRule(
                ContextUtil.getGroupCode(), projectId,
                Lists.newArrayList(new ClassCode(DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.value())));
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
            @NonNull HotColdStationUnitRunningPlanVO target,
            @Nullable DtpRwdCentralCoolingInfoCodeEnum infoCodeSetType,
            @Nullable QueryHotColdStationSystemParameterVO systemParameter) {
        if (Objects.isNull(systemParameter)) {
            return;
        }
        // 查询并填充实际值和设定值
        analyzeRunningValue(systemParameter.getChillerLNum(), Integer.class, target::setChillerLNum);
        analyzeRunningValue(systemParameter.getChillWaterOutTemp(), BigDecimal.class, target::setChillWaterOutTempSet);
        analyzeRunningValue(systemParameter.getHeatWaterSupplyTemp(), BigDecimal.class, target::setHeatWaterSupplyTempSet);

        if (Objects.nonNull(infoCodeSetType)) {
            switch (infoCodeSetType) {
                case CHILL_WATER_DETA_TEMP_SET:
                    analyzeRunningValue(systemParameter.getChillWaterDetaTemp(),
                            BigDecimal.class, target::setChillWaterDetaTempSet);
                    break;
                case CHILL_WATER_DETA_PRESS_SET:
                    analyzeRunningValue(systemParameter.getChillWaterDetaPress(),
                            BigDecimal.class, target::setChillWaterDetaPressSet);
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                            infoCodeSetType.getClass().getSimpleName(), infoCodeSetType);
            }
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
