package com.persagy.apccontrol.basic.service.duty.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractControlConsoleStrategyServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.entity.ChangeContent;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dutyHome.CombustionStateEnum;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.duty.*;
import com.persagy.apccontrol.basic.model.vo.outside.SystemGraphVO;
import com.persagy.apccontrol.basic.service.param.equiprecord.EquipRecordQueryDaoParam;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorHeatSourceStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.base.InfoCodeEnum.COMBUSTION_STATE;
import static com.persagy.apccontrol.basic.model.base.InfoCodeEnum.RUN_STATUS;


/**
 * 冷源运行值班首页策略实现类
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/11/6 15:27
 * @since v5.241102.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class HeatSourceHomePageCardDataHandler extends AbstractHomePageCardDataHandler<HotColdCardDataVO> implements LogComponent {

    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final ColdSourceHomePageCardDataHandler coldSourceHomePageCardDataHandler;
    private final RunningMonitorHeatSourceStrategy runningMonitorHeatSourceStrategy;
    private final AbstractControlConsoleStrategyServer abstractControlConsoleStrategyServer;

    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.HEAT_SOURCE;
    }

    public static final ArrayList<String> WITH_COLUMNS = Lists.newArrayList(
            DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.localId, DtpRwdObjectDTO.Fields.localName,
            // 热源系统类型
            "heatSoureType");

    public static final Collection<DtpRwdClassCodeEnum> CLASS_CODE_LIST = Lists.newArrayList(
            DtpRwdClassCodeEnum.ACCHGB, DtpRwdClassCodeEnum.ACGEHE, DtpRwdClassCodeEnum.ACCHHP, DtpRwdClassCodeEnum.ACCCVD,
            DtpRwdClassCodeEnum.ACCCCD, DtpRwdClassCodeEnum.ACCCTU, DtpRwdClassCodeEnum.ACCCST,
            DtpRwdClassCodeEnum.ACCCWT, DtpRwdClassCodeEnum.ACCCOC, DtpRwdClassCodeEnum.ACGEST, DtpRwdClassCodeEnum.ACGEET,
            DtpRwdClassCodeEnum.ACGECF, DtpRwdClassCodeEnum.ACGEWS, DtpRwdClassCodeEnum.ACGEDB, DtpRwdClassCodeEnum.ACGECL,
            DtpRwdClassCodeEnum.ACGEFP, DtpRwdClassCodeEnum.ACGEPT, DtpRwdClassCodeEnum.ACVVNO,
            DtpRwdClassCodeEnum.ACVVEO, DtpRwdClassCodeEnum.ACVVNJ, DtpRwdClassCodeEnum.ACVVEJ, DtpRwdClassCodeEnum.ACVVSB,
            DtpRwdClassCodeEnum.ACVVDB, DtpRwdClassCodeEnum.ACVVWC, DtpRwdClassCodeEnum.ACVVRV, DtpRwdClassCodeEnum.ACVVSV,
            DtpRwdClassCodeEnum.ACVVEV, DtpRwdClassCodeEnum.ACVVTW, DtpRwdClassCodeEnum.WESSTP, DtpRwdClassCodeEnum.WESSFL,
            DtpRwdClassCodeEnum.WESSHS, DtpRwdClassCodeEnum.WESSPM
    );

    public static final List<String> SYSTEM_INFO_CODE_LIST = Arrays.asList(
            DashBoardInfoCodeEnum.HEAT_WATER_MAIN_SUPPLY_TEMP.getCode(),
            DashBoardInfoCodeEnum.HEAT_WATER_MAIN_RETURN_TEMP.getCode(),
            DashBoardInfoCodeEnum.WATER_DETA_TEMP.getCode(),
            DashBoardInfoCodeEnum.HEAT_WATER_MAIN_SUPPLY_TEMP_2.getCode(),
            DashBoardInfoCodeEnum.HEAT_WATER_MAIN_RETURN_TEMP_2.getCode(),
            DashBoardInfoCodeEnum.HEAT_SECONDARY_SIDE_WATER_DETA_TEMP.getCode()
    );

    /**
     * 此处入参为热源的指定类型 1 市政热水 4 燃气锅炉
     */
    private static final JSONObject CONDITIONS = new JSONObject().fluentPut("$contains$", Arrays.asList("1", "4"));

    @Override
    @SuppressWarnings({"DuplicatedCode"})
    public HotColdCardDataVO execute(HomePageParam param) {
        HotColdCardDataVO vo = new HotColdCardDataVO();
        vo.setRunningMonitorType(getRunningMonitorType());
        String projectId = ContextUtil.getProjectId();
        List<HotColdChildCardDataVO> childCardList = new ArrayList<>();

        // 查询热战系统
        List<DtpRwdObjectDTO> rwdSystems = runningMonitorHeatSourceStrategy.queryTheTypeOfSpecifiedHeatSource(WITH_COLUMNS, CONDITIONS);

        if (CollectionUtils.isEmpty(rwdSystems)) {
            return vo;
        }

        List<String> gasBoilerSystemIds = rwdSystems.stream()
                .filter(x -> processRwdInfoHeatSource(x).contains(4))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toList());

        Set<String> systemIds = rwdSystems.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        // 查询热源关联的设备
        List<DtpRwdObjectDTO> equipmentList = runningMonitorHeatSourceStrategy.getTheHeatSourceAssociatedWithAllDevices(systemIds);

        Map<String, Set<String>> relObjectIdMap = equipmentList.stream()
                .filter(obj -> Objects.nonNull(obj.getRelObjIds()))
                .flatMap(obj -> obj.getRelObjIds().stream()
                        .map(relId -> new AbstractMap.SimpleEntry<>(relId, obj.getId())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));

        HashMap<String, CardTagVO> systemCardTagMap = processCardTagMap(relObjectIdMap);

        // 查询iot信息点数值，然后根据对象id分组
        Map<String, List<InfoCodeValueVO>> systemIdInfoCodeValueMap = objectInfoCodeValueStatistics(systemIds, SYSTEM_INFO_CODE_LIST);

        Map<String, RunStatusEnum> objectIdAndRunStatus = new HashMap<>();
        List<EquipRecord> equipRecords = new ArrayList<>();
        try {
            EquipRecordQueryDaoParam equipRecordQueryDaoParam = new EquipRecordQueryDaoParam()
                    .setProjectId(projectId)
                    .setGroupCode(ContextUtil.getGroupCode())
                    .setSystemCode(DtpRwdClassCodeEnum.HOT_SYSTEM.value()).setObjIds(systemIds)
                    .setChangeContent(new ChangeContent().setInfoCode(
                            AutoMaticInfoCodeEnum.RUN_STATUS.getPracticalCode())).setExecStatus(1);
            objectIdAndRunStatus = abstractControlConsoleStrategyServer.mapRecentRunningStatus(equipRecordQueryDaoParam);
            // 查询冷站运行记录  只显示已经执行成功和未执行的指令
            equipRecordQueryDaoParam.setBeginExecuteTime(DateUtils.getPastTime(1))
                    .setEndExecuteTime(DateUtils.getLastTime(2)).setExecStatus(null)
                    .setExecStatuses(Arrays.asList(-1, 1));
            equipRecords = abstractControlConsoleStrategyServer.listEquipRecord(equipRecordQueryDaoParam);
        } catch (Exception e) {
            logProjectInstance().error("查询运行记录异常", e);
        }
        Map<String, RunningStatisticsVO> runningStatisticsMap = runningStatisticsMap(equipmentList, gasBoilerSystemIds);

        HashMap<String, SystemGraphVO> drawingsThatCanBeJumped =
                coldSourceHomePageCardDataHandler.getDrawingsThatCanBeJumped(DtpRwdClassCodeEnum.HOT_SYSTEM.value(), systemIds);

        Map<String, RunStatusEnum> finalObjectIdAndRunStatus = objectIdAndRunStatus;
        List<EquipRecord> finalEquipRecords = equipRecords;
        rwdSystems.forEach(rwdSystem -> {
            HotColdChildCardDataVO hotColdChildCardDataVO =
                    HotColdChildCardDataVO.processSystemInformationAndSystemDiagrams(rwdSystem, drawingsThatCanBeJumped);
            String systemId = rwdSystem.getId();
            hotColdChildCardDataVO.setSystemId(systemId);
            hotColdChildCardDataVO.setCardTag(systemCardTagMap.get(systemId));
            JSONArray heatSoureType = processRwdInfoHeatSource(rwdSystem);
            hotColdChildCardDataVO.setHeatSoureType(heatSoureType);
            processInfoCodeData(heatSoureType, hotColdChildCardDataVO, systemIdInfoCodeValueMap.get(systemId), finalEquipRecords);
            hotColdChildCardDataVO.setRunStatus(finalObjectIdAndRunStatus.getOrDefault(systemId, null));
            hotColdChildCardDataVO.setRunningStatistics(runningStatisticsMap.getOrDefault(systemId, new RunningStatisticsVO()));
            coldSourceHomePageCardDataHandler.setDemandTemplateType(hotColdChildCardDataVO);
            childCardList.add(hotColdChildCardDataVO);
        });

        vo.setHotColdchildCardVOList(childCardList);
        return vo;
    }

    /**
     * 处理系统的卡片信息
     *
     * @param relObjectIdMap 所属系统分组后的map集合
     * @return <系统id,卡片>
     */
    private HashMap<String, CardTagVO> processCardTagMap(Map<String, Set<String>> relObjectIdMap) {
        HashMap<String, CardTagVO> systemCardTagMap = new HashMap<>();
        Set<String> forceCodes = Collections.singleton(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode());
        // 查询在离线、控反不一致、异常事件数量
        relObjectIdMap.forEach((systemId, relEquipmentIds) -> {
            Map<String, ObjectIdAndForceCodeQuery> queryMap = ObjectIdAndForceCodeQuery.buildDutyHomePageQuery(
                    RunningMonitorTypeEnum.HEAT_SOURCE.getValue(), relEquipmentIds, forceCodes);
            DutyHomePageGroupDTO dutyHomePageGroup = queryHomePageCardGroupIotData(queryMap);
            CardTagVO tag = buildCardTagVO(dutyHomePageGroup, relEquipmentIds);
            systemCardTagMap.put(systemId, tag);
        });
        return systemCardTagMap;
    }

    /**
     * 处理rwd响应的热源类型
     *
     * @param rwdSystem 对象信息
     * @return 热源类类型
     */
    private JSONArray processRwdInfoHeatSource(DtpRwdObjectDTO rwdSystem) {
        return JSON.parseArray(String.valueOf(rwdSystem.getUndefinedProperties().get("heatSoureType")));
    }

    /**
     * 处理iot信息点
     *
     * @param heatSoureType          热源系统类型
     * @param hotColdChildCardDataVO 响应数据
     * @param infoCodeValues         iot数值
     */
    private void processInfoCodeData(JSONArray heatSoureType,
                                     HotColdChildCardDataVO hotColdChildCardDataVO,
                                     List<InfoCodeValueVO> infoCodeValues,
                                     List<EquipRecord> finalEquipRecords) {
        Map<String, Object> infoCodeMap = InfoCodeValueVO.listToInfoCodeValueMap(infoCodeValues);
        hotColdChildCardDataVO.setHeatWaterMainSupplyTemp(infoCodeMap.getOrDefault(DashBoardInfoCodeEnum.HEAT_WATER_MAIN_SUPPLY_TEMP.getCode(), null));
        hotColdChildCardDataVO.setHeatWaterMainReturnTemp(infoCodeMap.getOrDefault(DashBoardInfoCodeEnum.HEAT_WATER_MAIN_RETURN_TEMP.getCode(), null));
        hotColdChildCardDataVO.setWaterDetaTemp(infoCodeMap.getOrDefault(DashBoardInfoCodeEnum.WATER_DETA_TEMP.getCode(), null));
        if (heatSoureType.contains(1)) {
            hotColdChildCardDataVO.setHeatWaterMainSupplyTemp2(infoCodeMap.getOrDefault(DashBoardInfoCodeEnum.HEAT_WATER_MAIN_SUPPLY_TEMP_2.getCode(), null));
            hotColdChildCardDataVO.setHeatWaterMainReturnTemp2(infoCodeMap.getOrDefault(DashBoardInfoCodeEnum.HEAT_WATER_MAIN_RETURN_TEMP_2.getCode(), null));
            hotColdChildCardDataVO.setHeatSecondarySideWaterDetaTemp(infoCodeMap.getOrDefault(DashBoardInfoCodeEnum.HEAT_SECONDARY_SIDE_WATER_DETA_TEMP.getCode(), null));
            coldSourceHomePageCardDataHandler.fillRunningTime(hotColdChildCardDataVO, finalEquipRecords);
        }
    }

    /**
     * 对象信息点查询后根绝对象id分组
     *
     * @param objIds   对象id
     * @param infoCode 信息点
     * @return 分组后的数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<InfoCodeValueVO>> objectInfoCodeValueStatistics(Collection<String> objIds, Collection<String> infoCode) {
        List<InfoCodeValueVO> infoCodeValueVOList =
                abstractIotDataCacheServer.listInfoCodeValueInOneHour(objIds, infoCode);
        return infoCodeValueVOList.stream().collect(Collectors.groupingBy(
                InfoCodeValueVO::getObjectId));
    }

    List<String> classCodeList = Arrays.asList(DtpRwdClassCodeEnum.ACCHGB.value(), DtpRwdClassCodeEnum.ACCHHP.value());

    /**
     * 供热泵和锅炉统计
     *
     * @param equipmentList 设备列表
     * @return <系统id,运行统计>
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    public Map<String, RunningStatisticsVO> runningStatisticsMap(List<DtpRwdObjectDTO> equipmentList, List<String> gasBoilerSystemIds) {
        List<String> heatSourceList = equipmentList.stream()
                .filter(item -> classCodeList.contains(item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toList());
        List<InfoCodeValueVO> infoCodeValues =
                abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                        heatSourceList, Arrays.asList(RUN_STATUS.value(), COMBUSTION_STATE.value()));
        Map<String, String> objectInfoMap = equipmentList.stream().
                collect(HashMap::new,
                        (map, item) -> map.put(item.getId(), item.getLocalName()),
                        HashMap::putAll
                );

        Map<String, RunningStatisticsVO> resultRunningStatistics = new HashMap<>();
        Map<String, List<String>> acchgbEquipmentMap = filterClassCodeToSystemFilterMap(equipmentList, DtpRwdClassCodeEnum.ACCHGB);
        Map<String, List<String>> acchhpEquipmentMap = filterClassCodeToSystemFilterMap(equipmentList, DtpRwdClassCodeEnum.ACCHHP);

        acchgbEquipmentMap.forEach((systemId, hgbEquipmentIds) -> {
            if (!gasBoilerSystemIds.contains(systemId)) {
                return;
            }
            RunningStatisticsVO runningStatisticsVO = resultRunningStatistics.getOrDefault(systemId, new RunningStatisticsVO());
            List<InfoCodeValueVO> collect = infoCodeValues.stream()
                    .filter(x -> hgbEquipmentIds.contains(x.getObjectId()))
                    .collect(Collectors.toList());
            Long count = collect.stream()
                    .filter(x -> x.getInfoCode().equals(RUN_STATUS.value()) && "1".equals(x.getInfoCodeValue()))
                    .count();
            ArrayList<BoilerCombustionStateVO> boilerCombustionStates = new ArrayList<>();
            List<InfoCodeValueVO> combustionStates = collect.stream()
                    .filter(x -> x.getInfoCode().equals(COMBUSTION_STATE.value()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(combustionStates)) {
                combustionStates.forEach(x -> {
                    BoilerCombustionStateVO boilerCombustionStateVO = new BoilerCombustionStateVO();
                    CombustionStateEnum combustionStateEnum = CombustionStateEnum.getCombustionStateEnum(x.getInfoCodeValue());
                    if (Objects.nonNull(combustionStateEnum)) {
                        boilerCombustionStateVO.setCombustionStateEnum(combustionStateEnum);
                        boilerCombustionStateVO.setObjectName(objectInfoMap.get(x.getObjectId()));
                        boilerCombustionStateVO.setObjectId(x.getObjectId());
                        boilerCombustionStates.add(boilerCombustionStateVO);
                    }
                });
            }
            runningStatisticsVO.setBoilerNum(count);
            runningStatisticsVO.setBoilerCombustionStates(boilerCombustionStates);
            resultRunningStatistics.put(systemId, runningStatisticsVO);
        });

        acchhpEquipmentMap.forEach((systemId, hgbEquipmentIds) -> {
            RunningStatisticsVO runningStatisticsVO = resultRunningStatistics.getOrDefault(systemId, new RunningStatisticsVO());
            Long count = infoCodeValues.stream()
                    .filter(x -> hgbEquipmentIds.contains(x.getObjectId())
                            && x.getInfoCode().equals(RUN_STATUS.value())
                            && "1".equals(x.getInfoCodeValue()))
                    .count();
            runningStatisticsVO.setHeatSupplyPumpNum(count);
            resultRunningStatistics.put(systemId, runningStatisticsVO);
        });

        return resultRunningStatistics;
    }

    /**
     * 根据对象类进行过滤设备
     *
     * @param equipmentList 设备列表
     * @param classCodeEnum 对象类
     * @return 过滤后的设备信息
     */
    private Map<String, List<String>> filterClassCodeToSystemFilterMap(List<DtpRwdObjectDTO> equipmentList, DtpRwdClassCodeEnum classCodeEnum) {
        List<DtpRwdObjectDTO> acchgbEquipment = equipmentList.stream()
                .filter(obj -> obj.getClassCode().equals(classCodeEnum.value()))
                .collect(Collectors.toList());
        return acchgbEquipment.stream()
                .filter(obj -> Objects.nonNull(obj.getRelObjIds()))
                .flatMap(obj -> obj.getRelObjIds().stream()
                        .map(relId -> new AbstractMap.SimpleEntry<>(relId, obj.getId())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }


    @Override
    public String logTag() {
        return "热源运行首页策略";
    }
}
