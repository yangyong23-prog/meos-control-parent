package com.persagy.apccontrol.basic.service.elevator.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.elevator.ElevatorStatisticsParam;
import com.persagy.apccontrol.basic.model.vo.elevator.ElevatorSummaryVO;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.elevator.ElevatorService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS_LIST;

/**
 * 电梯处理类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/8/21 17:09
 */
@Slf4j
@Service
public class ElevatorServiceImpl implements ElevatorService {

    @Autowired
    private BizDtpRwdService bizDtpRwdService;

    @Autowired
    private AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public ElevatorSummaryVO statisticsRunningByClassCode(ElevatorStatisticsParam param) throws Exception {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = param.getProjectId();
        ElevatorSummaryVO.Detail summaryInfo = new ElevatorSummaryVO.Detail(0, 0, null);
        ElevatorSummaryVO.Detail visitorInfo = new ElevatorSummaryVO.Detail(0, 0, null);
        ElevatorSummaryVO.Detail freightInfo = new ElevatorSummaryVO.Detail(0, 0, null);
        ElevatorSummaryVO summaryVO = new ElevatorSummaryVO(summaryInfo, visitorInfo, freightInfo);

        // 是否是直梯
        boolean elelel = DtpRwdClassCodeEnum.ELELEL.getValue().equals(param.getClassCode());
        boolean floor = DemandInstanceTypeEnum.FLOOR.equals(param.getInstanceType());

        // 1.如果当前是楼层视角，获取数据过滤相关的关系数据
        CompletableFuture<Set<String>> future = null;
        if (floor) {
            future = CompletableFuture.supplyAsync(() -> this.getElevatorRelationMap(groupCode, projectId, param.getInstanceId(), elelel), BusinessThreadPoolExecutor.ttlExecutor());
        }

        // 2.获取 直梯或扶梯的设备数据，并进行直梯的分类
        List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(param.getClassCode()), null, null, null, null);
        if (CollectionUtil.isEmpty(objectList)) {
            return summaryVO;
        }

        objectList = objectList.stream().filter(item -> MONITORING_SURVEILLANCE_STATUS_LIST.contains(item.getString(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.getCode()))).collect(Collectors.toList());
        Set<String> objectIds = new HashSet<>();
        Set<String> visitorIds = new HashSet<>();
        Set<String> freightIds = new HashSet<>();
        for (JSONObject objectInfo : objectList) {
            String objectId = objectInfo.getString("id");
            String funcType = objectInfo.getString("funcType");
            boolean r = elelel ? "1".equals(funcType) ? visitorIds.add(objectId) : "2".equals(funcType) ? freightIds.add(objectId) : false : false;
            objectIds.add(objectId);
        }

        // 3.获取直梯或扶梯的IOT数据
        Map<String, Set<String>> classCodeAndIdsMap = new HashMap<>();
        classCodeAndIdsMap.put(param.getClassCode(), objectIds);

        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndIdsMap, Arrays.asList(InfoCodeEnum.LOCK_STATUS.getCode(),InfoCodeEnum.RUN_STATUS.getCode(),InfoCodeEnum.ONLINE_STATUS.getCode()));
        Set<String> existsRules = Optional.ofNullable(objectIdAndExistsRuleInfoCodes)
                .map(Map::values)
                .orElse(Collections.emptySet())
                .stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        Map<String, Map<String, String>> objectInfoCodeValueMap = new HashMap<>();
        String infoCode = null;
        if(existsRules.contains(InfoCodeEnum.LOCK_STATUS.getCode())){
            infoCode= InfoCodeEnum.LOCK_STATUS.getCode();
            objectInfoCodeValueMap = this.abstractIotDataCacheServer.queryIotRealDataMap(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), objectIds, Sets.newHashSet(InfoCodeEnum.LOCK_STATUS.getCode()));
        }else if(existsRules.contains(InfoCodeEnum.RUN_STATUS.getCode())){
            infoCode= InfoCodeEnum.RUN_STATUS.getCode();
            objectInfoCodeValueMap = this.abstractIotDataCacheServer.queryIotRealDataMap(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), objectIds, Sets.newHashSet(InfoCodeEnum.RUN_STATUS.getCode()));
        }else if(existsRules.contains(InfoCodeEnum.ONLINE_STATUS.getCode())){
            infoCode= InfoCodeEnum.ONLINE_STATUS.getCode();
            objectInfoCodeValueMap = this.abstractIotDataCacheServer.queryIotRealDataMap(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), objectIds, Sets.newHashSet(InfoCodeEnum.ONLINE_STATUS.getCode()));
        }
        //Map<String, Map<String, String>> objectInfoCodeValueMap = this.abstractIotDataCacheServer.queryIotRealDataMap(groupCode, projectId, objectIds, Sets.newHashSet("lockStatus"));

        // 4.根据IOT数据，进行统计
        Set<String> filterObjectIds = future == null ? null : future.get();
        for (String objectId : objectIds) {
            Map<String, String> infoCodeValueMap = objectInfoCodeValueMap.get(objectId);
            boolean visitor = visitorIds.contains(objectId);
            boolean freight = freightIds.contains(objectId);
            boolean running = false;
            if(InfoCodeEnum.LOCK_STATUS.getCode().equals(infoCode)) {
                running = infoCodeValueMap == null ? false : "0".equals(infoCodeValueMap.get(infoCode));
            }else{
                running = infoCodeValueMap == null ? false : "1".equals(infoCodeValueMap.get(infoCode));
            }
            if (floor) {
                if (CollectionUtil.isEmpty(filterObjectIds) || !filterObjectIds.contains(objectId)) {
                    continue;
                }
            }

            // 运行数
            if (running) {
                summaryInfo.setRunning(summaryInfo.getRunning() + 1);
                if (visitor) {
                    visitorInfo.setRunning(visitorInfo.getRunning() + 1);
                }
                if (freight) {
                    freightInfo.setRunning(freightInfo.getRunning() + 1);
                }
            }

            // 总数
            if (visitor) {
                visitorInfo.setTotal(visitorInfo.getTotal() + 1);
            }
            if (freight) {
                freightInfo.setTotal(freightInfo.getTotal() + 1);
            }
            summaryInfo.setTotal(summaryInfo.getTotal() + 1);
        }
        summaryInfo.setInfoCode(infoCode);
        visitorInfo.setInfoCode(infoCode);
        freightInfo.setInfoCode(infoCode);
        return summaryVO;
    }

    @Override
    public Map<String, Set<String>> mapClaasCode2objectIds(String projectId) {
        List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(ContextUtil.getGroupCode(), projectId,
                Lists.newArrayList(DtpRwdClassCodeEnum.ELELEL.getValue(), DtpRwdClassCodeEnum.ELELES.getValue()), null, null, null, null);

        if (CollUtil.isNotEmpty(objectList)) {
            // 获取ID集合，并根据classCode分组
            Map<String, Set<String>> map = objectList.stream().collect(Collectors.groupingBy(object -> object.getString("classCode"),
                    Collectors.mapping(object -> object.getString("id"), Collectors.toSet())));
            return map;
        }
        return Collections.emptyMap();
    }

    /**
     * 获取单楼层下对应后续计算所需要的关系数据
     *
     * @param groupCode
     * @param projectId
     * @param floorId 楼层ID
     * @param elelel true-直梯，false-扶梯
     * @return 当前楼层下的所有直梯、扶梯设备ID
     */
    private Set<String> getElevatorRelationMap(String groupCode, String projectId, String floorId, boolean elelel) {
        Pair<String, String> spaceInFloor = DtpRwdRelationTypeEnum.SPACE_IN_FLOOR.value();
        Pair<String, String> equipInSpace = DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE.value();

        // 优先获取该楼层下的所有物业分区
        List<JSONObject> spaceList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.SPACE.getValue()), spaceInFloor.getLeft(), spaceInFloor.getRight(), Lists.newArrayList(floorId), null);
        if (CollectionUtil.isEmpty(spaceList)) {
            return new HashSet<>();
        }

        // 直梯：该楼层下电梯井空间对应的直梯设备（通过直梯和竖井的关系、竖井和物业分区（电梯井）的关系进行关联）
        if (elelel) {
            // 该楼层下所有的电梯井空间信息 roomFuncType = SFT-03.03.03（货梯井），SFT-03.03.02（客梯井）
            List<String> spaceIds = spaceList.stream().filter(item -> "SFT-03.03.02".equals(item.getString("roomFuncType")) || "SFT-03.03.03".equals(item.getString("roomFuncType")))
                    .map(item -> item.getString("id")).distinct().collect(Collectors.toList());
            if (CollectionUtil.isEmpty(spaceIds)) {
                return new HashSet<>();
            }

            // 根据当前的电梯井空间，获取对应的竖井
            List<JSONObject> shaftList = CollectionUtil.isEmpty(spaceIds) ? null : this.bizDtpRwdService.queryRelation(groupCode, projectId, "ArchSubset", "Sh2Sp", null, null, spaceIds);
            List<String> shaftIds = CollectionUtil.isEmpty(shaftList) ? null : shaftList.stream().map(key -> key.getString("objFrom")).distinct().collect(Collectors.toList());

            // 根据竖井，获取对应的直梯设备
            List<JSONObject> elelelList = CollectionUtil.isEmpty(shaftIds) ? null : this.bizDtpRwdService.queryRelation(groupCode, projectId, "MechInArch", "Eq2Sh", null, null, shaftIds);
            return CollectionUtil.isEmpty(elelelList) ? new HashSet<>() : elelelList.stream().map(key -> key.getString("objFrom")).collect(Collectors.toSet());
        }

        // 扶梯：根据扶梯对象的floorId 来进行区分
        List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.ELELES.getValue()), null, null, null, null);
        return CollectionUtil.isEmpty(objectList) ? new HashSet<>() : objectList.stream().filter(item -> floorId.equals(item.getString("floorId"))).map(item -> item.getString("id")).collect(Collectors.toSet());
    }

}
