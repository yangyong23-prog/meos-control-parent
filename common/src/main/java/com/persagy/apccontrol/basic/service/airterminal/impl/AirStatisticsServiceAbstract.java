package com.persagy.apccontrol.basic.service.airterminal.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.AirHomePageEquipmentParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.statistics.AirHomePageEquipmentVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.airterminal.AirStatisticsService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirTerminalStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import org.apache.commons.collections4.MapUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class AirStatisticsServiceAbstract implements AirStatisticsService {
    @Resource
    private RunningMonitorAirTerminalStrategy runningMonitorAirTerminalStrategy;

    @Resource
    private DtpKnowledgeClientService dtpKnowledgeClientService;
    @Resource
    private AbstractCommonServer abstractCommonServer;

    /**
     * 查询末端统计的设备列表
     * @return key ClassCode val:ids
     */
    public Map<String, Set<String>> queryEquipmentList(AirHomePageEquipmentParam param) {
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        //equipmentQueryParam.setFilterExcludeClassCodes(Sets.newHashSet(DtpRwdClassCodeEnum.ACVVEJ.value()));
        //equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        if (InstanceTypeEnum.FLOOR.equals(param.getInstanceType())) {
            equipmentQueryParam.setFilterFloorIds(Sets.newHashSet(param.getInstanceId()));
        }
        equipmentQueryParam.setFilterExcludeClassCodes(Sets.newHashSet(
                DtpRwdClassCodeEnum.ACAHG_GROUP.getValue(), DtpRwdClassCodeEnum.ACVVER.getValue(),
                DtpRwdClassCodeEnum.ACVVEA.getValue(), DtpRwdClassCodeEnum.ACATHT.getValue(),
                DtpRwdClassCodeEnum.ACVVEJ.getValue()
        ));
        List<DtpRwdEquipmentDTO> controlEquipmentIds = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam);
        if (CollectionUtil.isEmpty(controlEquipmentIds)) {
            return new HashMap<>();
        }
        //按照classCode 分组
        return controlEquipmentIds.stream().collect(Collectors.groupingBy(DtpRwdEquipmentDTO::getClassCode, Collectors.mapping(DtpRwdEquipmentDTO::getId, Collectors.toSet())));
    }

    /**
     * 查询设备类名称
     *
     * @param classCodes
     * @return 设备类编码 -> 设备类名称
     */
    public Map<String, String> getClassCodeAndName(Set<String> classCodes) {
        return dtpKnowledgeClientService.mapClassCodeAndName(false, false, classCodes);
    }

    public List<AirHomePageEquipmentVO> getInitAirHomePageEquipmentVO(Map<String, Set<String>> eqList) {

        if (MapUtils.isEmpty(eqList)) {
            return new ArrayList<>();
        }

        List<AirHomePageEquipmentVO> airHomePageEquipmentVOS = new ArrayList<>();
        //先构造空数据
        Map<String, String> nameMap = getClassCodeAndName(eqList.keySet());
        eqList.forEach((k, v) -> {

            AirHomePageEquipmentVO airHomePageEquipmentVO = AirHomePageEquipmentVO.builder()
                    .classCode(k)
                    .equipmentTotal(eqList.get(k).size())
                    .noDataTotal(eqList.get(k).size())
                    .build();
            if (MapUtils.isNotEmpty(nameMap)) {
                airHomePageEquipmentVO.setClassCodeName(nameMap.get(k));
            }
            airHomePageEquipmentVOS.add(airHomePageEquipmentVO);
        });
        return airHomePageEquipmentVOS;
    }

    /**
     * 查询异常事件
     *
     * @param eqList
     * @return
     */
    public Map<String, Integer> getExceptionEventCountByObjIds(Map<String, Set<String>> eqList) {
        Map<String, Integer> event = new HashMap<>();
        if (MapUtils.isEmpty(eqList)) {
            return event;
        }
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        eqList.forEach((k, objIds) -> {
            if (CollectionUtils.isEmpty(objIds)) {
                return;
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Map<String, List<ExceptionEventBaseInfoVO>> riskControlTargetConditions = abstractCommonServer.mapRiskEvent(objIds, null);
                event.put(k, riskControlTargetConditions.keySet().size());
            }, BusinessThreadPoolExecutor.ttlExecutor());
            futures.add(future);
        });
        for (CompletableFuture<Void> future : futures) {
            future.join();
        }
        return event;
    }

    /**
     * 设置异常事件数量
     *
     * @param airHomePageEquipmentVOS
     * @param eqList
     */
    public void setExceptionTotal(List<AirHomePageEquipmentVO> airHomePageEquipmentVOS, Map<String, Set<String>> eqList) {
        if (CollectionUtils.isEmpty(airHomePageEquipmentVOS) ||
                MapUtils.isEmpty(eqList)
        ) {
            return;
        }
        Map<String, Integer> map = getExceptionEventCountByObjIds(eqList);
        airHomePageEquipmentVOS.forEach(vo -> {
            if (vo == null) {
                return;
            }
            Integer exceptionTotal = map.get(vo.getClassCode());
            vo.setExceptionTotal(exceptionTotal == null ? 0 : exceptionTotal);
        });

    }


}
