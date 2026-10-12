package com.persagy.apccontrol.basic.service.duty.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirDtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.duty.RunAnalysisCardDataVO;
import com.persagy.apccontrol.basic.model.vo.duty.RunAnalysisSpaceVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.dashboard.space.AirTerminalQuerySpace;
import com.persagy.apccontrol.basic.service.systemgraph.impl.RiskEventService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorRunAnalysisStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xiaofengwang
 */
@Service
@Slf4j
public class RunAnalysisHomePageCardDataHandler extends AbstractHomePageCardDataHandler<RunAnalysisCardDataVO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunAnalysisHomePageCardDataHandler.class);
    private static final String LOG_TAG = "运行值班首页-环境品质";

    private final DemandConfigGateway demandConfigGatewayImpl;
    private final RunningMonitorRunAnalysisStrategy runningMonitorRunAnalysisStrategy;
    private final BizDtpRwdService bizDtpRwdService;
    private final MidCalendarClientService midCalendarClientService;
    private final RiskEventService riskEventService;
    private final AbstractIotDataCacheServer iotDataCacheService;
    @Autowired
    private AirTerminalQuerySpace airTerminalQuerySpace;

    public RunAnalysisHomePageCardDataHandler(DemandConfigGateway demandConfigGatewayImpl, RunningMonitorRunAnalysisStrategy runningMonitorRunAnalysisStrategy, BizDtpRwdService bizDtpRwdService, MidCalendarClientService midCalendarClientService, RiskEventService riskEventService, AbstractIotDataCacheServer iotDataCacheService) {
        this.demandConfigGatewayImpl = demandConfigGatewayImpl;
        this.runningMonitorRunAnalysisStrategy = runningMonitorRunAnalysisStrategy;
        this.bizDtpRwdService = bizDtpRwdService;
        this.midCalendarClientService = midCalendarClientService;
        this.riskEventService = riskEventService;
        this.iotDataCacheService = iotDataCacheService;
    }

    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.RUN_ANALYSIS;
    }

    @Override
    public RunAnalysisCardDataVO execute(HomePageParam param) {
        RunAnalysisCardDataVO result = new RunAnalysisCardDataVO();
        result.setRunningMonitorType(getRunningMonitorType());
        AtomicInteger tooColdCount = new AtomicInteger();
        AtomicInteger coldCount = new AtomicInteger();
        AtomicInteger hotCount = new AtomicInteger();
        AtomicInteger tooHotCount = new AtomicInteger();
        AtomicInteger appropriateCount = new AtomicInteger();

        StopWatch stopWatch = StopWatch.create(LOG_TAG);

        stopWatch.start("查找有 tdb 实例化规则的空间");
        DemandInstanceValueObject instance = DemandInstanceValueObject.ofProject();
        List<? extends DtpRwdSpaceVO> dtpRwdSpaceVOS = airTerminalQuerySpace.querySpace(instance, true);
        if (CollectionUtil.isEmpty(dtpRwdSpaceVOS)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未获取到空间");
            return result;
        }
        dtpRwdSpaceVOS = dtpRwdSpaceVOS.stream().filter(space-> {
            if(space == null){
                return false;
            }
            if(space instanceof AirDtpRwdSpaceVO){
                try {
                return ((AirDtpRwdSpaceVO) space).getTdbRule() != null && ((AirDtpRwdSpaceVO) space).getTdbRule() == true;
                } catch (Exception e) {
                    LogExecutor.project().error(LOGGER, LOG_TAG, "获取空间信息异常", e);
                }
            }
            return false;
        }).collect(Collectors.toList());
        //查找有tdb实例化规则的空间
        //Set<String> spaceIds = runningMonitorRunAnalysisStrategy.listSpaceWithInfoCode(InfoCodeEnum.SPACE_TDB, ContextUtil.getProjectId());
        Set<String> spaceIds = dtpRwdSpaceVOS.stream().map(DtpRwdSpaceVO::getId).collect(Collectors.toSet());
        stopWatch.stop();
        if (CollectionUtil.isEmpty(spaceIds)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未获取到有tdb实例化规则的空间");
            return result;
        }

        //查找持久化数据，统计状态和达标率
        stopWatch.start("查找持久化数据");
        List<InfoCodeValueVO> infoCodeValues = iotDataCacheService.listInfoCodeValue(ContextUtil.getProjectId(), null, spaceIds.stream().collect(Collectors.toList()), InfoCodeEnum.SPACE_TDB.getCode());
        if (CollectionUtil.isEmpty(infoCodeValues)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未获取到持久化数据");
            return result;
        }
        stopWatch.stop();
        spaceIds = infoCodeValues.stream().map(InfoCodeValueVO::getObjectId).collect(Collectors.toSet());

        LogExecutor.project().info(LOGGER, LOG_TAG, "tdb实例化规则的空间数" + spaceIds.size());
        stopWatch.start("查找需求配置");
        //查找需求配置
        BaseDemandTemplateParam demandParam = new BaseDemandTemplateParam();
        demandParam.setDate(DateUtils.now().toLocalDate());
        demandParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        Optional<DemandTemplate> template = demandConfigGatewayImpl.getByTemplate(demandParam);
        if (!template.isPresent()) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未获取到需求模版");
            return result;
        }

        DemandConfigByInstanceBatchQueryParam batchQueryParam = new DemandConfigByInstanceBatchQueryParam();
        batchQueryParam.setTemplateId(template.get().getId());
        batchQueryParam.setDate(DateUtils.now().toLocalDate());
        batchQueryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(DemandInstanceTypeEnum.SPACE, spaceIds));
        batchQueryParam.setFilterShieldedSpaces(true);
        List<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> configs = demandConfigGatewayImpl.listConfigsByInstances(batchQueryParam);
        if (CollectionUtils.isEmpty(configs)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未获取到需求配置");
            return result;
        }
        stopWatch.stop();

        Map<String, InfoCodeValueVO> infoCodeValueMap = infoCodeValues.stream().collect(Collectors.toMap(InfoCodeValueVO::getObjectId, Function.identity(), (v1, v2) -> v2));
        LocalDate now = DateUtils.now().toLocalDate();
        stopWatch.start("查找空调季节");
        MidCalendarAirSeasonTypeEnum airSeasonType = midCalendarClientService.getAirSeasonType(ContextUtil.getProjectId(), now);
        MidCalendarAcSeasonTypeEnum acSeasonType = midCalendarClientService.getAcSeasonType(ContextUtil.getProjectId(), ContextUtil.getProjectId(), now);
        stopWatch.stop();

        stopWatch.start("数据分析");
        List<String> hasConfigSpaceIds = new ArrayList<>();
        List<RunAnalysisSpaceVO> specialSpaceRankList = new ArrayList<>();
        List<RunAnalysisSpaceVO> finalSpecialSpaceRankList = specialSpaceRankList;
        configs.forEach(config -> {
            try {
                String instanceId = config.getIdentifier().getInstance().getInstanceId();
                String localName = config.getIdentifier().getInstance().getInstanceName();
                DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality = new DemandConfigAirconditioningTdbSeasonValueObject();

                runningMonitorRunAnalysisStrategy.fillQuality(tdbSeasonQuality, null, config, airSeasonType);
                BigDecimal ipValue = new BigDecimal((String) Optional.ofNullable(infoCodeValueMap.get(instanceId)).map(InfoCodeValueVO::getDataValue).orElse(null));


                //舒适区下限值
                BigDecimal lowerComfortSeasonLimitValue = tdbSeasonQuality.getLowerComfortSeasonLimitValue();
                //过热门限
                BigDecimal overHeatingSeasonLimitValue = tdbSeasonQuality.getOverHeatingSeasonLimitValue();
                //过冷门限
                BigDecimal underCoolingSeasonLimitValue = tdbSeasonQuality.getUnderCoolingSeasonLimitValue();
                //舒适区上限
                BigDecimal upperComfortSeasonLimitValue = tdbSeasonQuality.getUpperComfortSeasonLimitValue();
                if (Objects.nonNull(lowerComfortSeasonLimitValue)
                        && Objects.nonNull(overHeatingSeasonLimitValue)
                        && Objects.nonNull(underCoolingSeasonLimitValue)
                        && Objects.nonNull(upperComfortSeasonLimitValue)) {

                    hasConfigSpaceIds.add(instanceId);
                    int compareOverHeating = ipValue.compareTo(overHeatingSeasonLimitValue);
                    int compareUnderCooling = ipValue.compareTo(underCoolingSeasonLimitValue);
                    int compareLowerComfort = ipValue.compareTo(lowerComfortSeasonLimitValue);
                    int compareUpperComfort = ipValue.compareTo(upperComfortSeasonLimitValue);

                    if (compareOverHeating > 0) {
                        tooHotCount.getAndIncrement();
                    } else if (compareUnderCooling < 0) {
                        tooColdCount.getAndIncrement();
                    } else if (compareUnderCooling >= 0 && compareLowerComfort < 0) {
                        coldCount.getAndIncrement();
                    } else if (compareUpperComfort > 0 && compareOverHeating <= 0) {
                        hotCount.getAndIncrement();
                    } else {
                        appropriateCount.getAndIncrement();
                    }

                    //空间列表
                    //if(CollectionUtil.isNotEmpty(finalSubEnergyDataMap) && finalSubEnergyDataMap.containsKey(instanceId)){
                    RunAnalysisSpaceVO runAnalysisSpaceVO = new RunAnalysisSpaceVO();
                    runAnalysisSpaceVO.setSpaceName(localName);
                    runAnalysisSpaceVO.setSpaceId(instanceId);
                    runAnalysisSpaceVO.setTdb(ipValue);
                    finalSpecialSpaceRankList.add(runAnalysisSpaceVO);
                    //}
                }
            } catch (Exception e) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "分析错误：" + e.getMessage());
                return;
            }
        });

        result.setTooHotCount(tooHotCount.get());
        result.setTooColdCount(tooColdCount.get());
        result.setHotCount(hotCount.get());
        result.setColdCount(coldCount.get());
        if (CollectionUtil.isNotEmpty(hasConfigSpaceIds)) {
            result.setTdbSatisfyPercentage(appropriateCount.get() * 100 / hasConfigSpaceIds.size());
        }

        if (MidCalendarAcSeasonTypeEnum.SEASON_COOLING.equals(acSeasonType)) {
            specialSpaceRankList = specialSpaceRankList.stream().sorted(Comparator.comparing(RunAnalysisSpaceVO::getTdb).reversed()).collect(Collectors.toList());
        } else if (MidCalendarAcSeasonTypeEnum.SEASON_HEATING.equals(acSeasonType)) {
            specialSpaceRankList = specialSpaceRankList.stream().sorted(Comparator.comparing(RunAnalysisSpaceVO::getTdb)).collect(Collectors.toList());
        }
        specialSpaceRankList = specialSpaceRankList.subList(0, specialSpaceRankList.size() > 5 ? 5 : specialSpaceRankList.size());
        result.setSpecialSpaceRankList(specialSpaceRankList);
        stopWatch.stop();

        try {
            stopWatch.start("异常事件");
            //异常事件
            List<String> abnormalSpaces = riskEventService.listAbnormalStateBySpaceIdsAppointTime(hasConfigSpaceIds, null, null, null, Arrays.asList(RiskMeasureAimLevel2.E34.getCode(), RiskMeasureAimLevel2.E35.getCode()));
            result.setErrorEventTotal(abnormalSpaces.size());
            stopWatch.stop();
        }catch (Exception e){
            LogExecutor.project().info(LOGGER, LOG_TAG, "异常事件错误："+e.getMessage());
            result.setErrorEventTotal(0);
        }

        LogExecutor.project().info(LOGGER, LOG_TAG, "响应时间记录", Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return result;
    }
}
