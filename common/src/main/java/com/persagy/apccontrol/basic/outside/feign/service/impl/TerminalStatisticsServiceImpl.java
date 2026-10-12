package com.persagy.apccontrol.basic.outside.feign.service.impl;

import cn.hutool.core.date.StopWatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.outside.energy.StatisticsTypeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalAbnormalQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.outside.energy.StatisticsVO;
import com.persagy.apccontrol.basic.outside.feign.service.TerminalStatisticsService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirTerminalStrategy;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
public class TerminalStatisticsServiceImpl implements TerminalStatisticsService {

    private final AirTerminalService airTerminalService;
    private final RunningMonitorAirTerminalStrategy runningMonitorAirTerminalStrategy;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final Executor executor;

    @Override
    public StatisticsVO getTerminalStatistics(StatisticsTypeEnum type) {
        StatisticsVO result = new StatisticsVO();
        switch (type) {
            case TERMINAL_EQUIP_GROUP_STATISTICS:
                getTerminalEquipGroupStatistics(result);
                break;
            case TERMINAL_INFO_CODE_STATISTICS:
                getTerminalInfoCodeStatistics(result);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(type.getClass().getSimpleName(), type);
        }
        return result;
    }

    /**
     * 获取末端设备组统计信息
     *
     * @param result 结果
     */
    @SuppressWarnings("unchecked")
    private void getTerminalEquipGroupStatistics(StatisticsVO result) {
        List<StatisticsVO.TerminalEquipGroupStatistics> terminalEquipGroupStatisticsList = new ArrayList<>();
        String projectId = ContextUtil.getProjectId();
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        equipmentQueryParam.setFilterExcludeClassCodes(Sets.newHashSet(DtpRwdClassCodeEnum.ACVVEJ.value()));
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam);
        Set<String> ids = dtpRwdEquipmentDTOS.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        List<InfoCodeValueVO> valueList = abstractIotDataCacheServer.listInfoCodeValue(projectId, null, ids, InfoCodeEnum.RUN_STATUS.getCode());
        Map<String, Object> idValueMap = valueList.stream().filter(v -> Objects.nonNull(v.getInfoCodeValue()) && "1".equals(v.getInfoCodeValue().toString()))
                .collect(Collectors.toMap(InfoCodeValueVO::getObjectId, InfoCodeValueVO::getInfoCodeValue, (v1, v2) -> v2));
        Map<String, List<DtpRwdEquipmentDTO>> classMap = dtpRwdEquipmentDTOS.stream().filter(e -> StringUtils.isNotEmpty(e.getClassCode())).collect(Collectors.groupingBy(DtpRwdEquipmentDTO::getClassCode));
        classMap.forEach((k, v) -> {
            List<String> classIds = v.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList());
            StatisticsVO.TerminalEquipGroupStatistics terminalEquipGroupStatistics = new StatisticsVO.TerminalEquipGroupStatistics();
            terminalEquipGroupStatistics.setClassCode(k);
            DtpRwdClassCodeEnum analyze = BaseEnum.analyze(k, DtpRwdClassCodeEnum.class);
            terminalEquipGroupStatistics.setClassName(analyze.getReasonPhrase());
            terminalEquipGroupStatistics.setTotal(v.size());
            long runTotal = 0;
            for (String id : classIds) {
                Object value = idValueMap.get(id);
                if (Objects.nonNull(value)) {
                    runTotal++;
                }
            }
            terminalEquipGroupStatistics.setRunTotal(runTotal);
            terminalEquipGroupStatisticsList.add(terminalEquipGroupStatistics);
        });
        result.setTerminalEquipGroupStatistics(terminalEquipGroupStatisticsList);
    }

    /**
     * 获取末端信息代码统计
     *
     * @param result 结果
     */
    private void getTerminalInfoCodeStatistics(StatisticsVO result) {
        StatisticsVO.TerminalInfoCodeStatistics terminalInfoCodeStatistics = new StatisticsVO.TerminalInfoCodeStatistics();
        String projectId = ContextUtil.getProjectId();
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        equipmentQueryParam.setFilterExcludeClassCodes(Sets.newHashSet(DtpRwdClassCodeEnum.ACVVEJ.value()));
        List<String> equipIds = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam)
                .stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList());
        StopWatch stopWatch = StopWatch.create("获取末端信息代码统计");
        stopWatch.start("获取终端信息代码统计");
        List<CompletableFuture<Map<String, Integer>>> futureList = new ArrayList<>();
        futureList.add(CompletableFuture.supplyAsync(() -> getAbnormalEventNum(projectId), executor));
        futureList.add(CompletableFuture.supplyAsync(() -> getInfoCodeValueMap(equipIds), executor));
        Map<String, Integer> map = new HashMap<>();
        for (CompletableFuture<Map<String, Integer>> future : futureList) {
            map.putAll(future.join());
        }
        stopWatch.stop();
        terminalInfoCodeStatistics.setOppositeNum(map.get("oppositeNum"));
        terminalInfoCodeStatistics.setOffEqNum(map.get("offEqNum"));
        terminalInfoCodeStatistics.setAbnormalEventNum(map.get("abnormalEventNum"));
        logProjectInstance(projectId).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        result.setTerminalInfoCodeStatistics(terminalInfoCodeStatistics);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> getInfoCodeValueMap(List<String> equipIds) {
        //查询信息点
        List<String> infoCodes = Lists.newArrayList(InfoCodeEnum.ORDER_FAIL_ALARM.getCode(), InfoCodeEnum.ONLINE_STATUS.getCode());
        StopWatch stopWatch = StopWatch.create("查询逻辑");
        stopWatch.start("查询信息点-listInfoCodeValue");
        List<InfoCodeValueVO> infoCodeValueList = abstractIotDataCacheServer.listInfoCodeValue(ContextUtil.getProjectId(), null, equipIds, infoCodes);
        Map<String, List<InfoCodeValueVO>> infoCodeValueMap = infoCodeValueList.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId));
        stopWatch.stop();
        Integer offEqNum = 0;
        Integer oppositeNum = 0;
        for (String objId : equipIds) {
            List<InfoCodeValueVO> infoCodeValueVOS = infoCodeValueMap.get(objId);
            if (CollectionUtils.isNotEmpty(infoCodeValueVOS)) {
                Map<String, Object> infoValueMap = infoCodeValueVOS.stream().filter(e -> Objects.nonNull(e.getInfoCodeValue()))
                        .collect(Collectors.toMap(InfoCodeValueVO::getInfoCode, InfoCodeValueVO::getInfoCodeValue, (v1, v2) -> v1));
                Object orderFailAlarm = infoValueMap.get(InfoCodeEnum.ORDER_FAIL_ALARM.getCode());
                Object onlineStatus = infoValueMap.get(InfoCodeEnum.ONLINE_STATUS.getCode());
                //控反不一致
                if (Objects.nonNull(orderFailAlarm)) {
                    String orderFailAlarmStr = orderFailAlarm.toString();
                    if ("1".equals(orderFailAlarmStr)) {
                        oppositeNum++;
                    }
                }
                //离线
                if (Objects.nonNull(onlineStatus)) {
                    String onlineStatusStr = onlineStatus.toString();
                    if ("0".equals(onlineStatusStr)) {
                        offEqNum++;
                    }
                }
            }
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("offEqNum", offEqNum);
        result.put("oppositeNum", oppositeNum);
        return result;
    }

    private Map<String, Integer> getAbnormalEventNum(String projectId) {
        Map<String, Integer> result = new HashMap<>();
        AirTerminalAbnormalQueryParam airTerminalAbnormalQueryParam = new AirTerminalAbnormalQueryParam();
        airTerminalAbnormalQueryParam.setInstanceId(projectId);
        airTerminalAbnormalQueryParam.setInstanceType(InstanceTypeEnum.PROJECT);
        airTerminalAbnormalQueryParam.setProjectId(projectId);
        //异常事件
        List<ExceptionEventBaseInfoVO> riskControlObjectives = airTerminalService.getAbnormalDataList(airTerminalAbnormalQueryParam);
        result.put("abnormalEventNum", CollectionUtils.isEmpty(riskControlObjectives) ? 0 : riskControlObjectives.size());
        return result;
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "末端统计接口-统计信息";
    }


}
