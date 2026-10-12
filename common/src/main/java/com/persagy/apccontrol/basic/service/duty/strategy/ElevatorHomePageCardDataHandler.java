package com.persagy.apccontrol.basic.service.duty.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardDateVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.ElevatorCardDataVO;
import com.persagy.apccontrol.basic.model.vo.duty.ElevatorChildCardDataVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS;
import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS_LIST;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ElevatorHomePageCardDataHandler extends AbstractHomePageCardDataHandler<CardDateVO<ElevatorCardDataVO>> {

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer iotDataCacheService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private static final Logger LOGGER = LoggerFactory.getLogger(ElevatorHomePageCardDataHandler.class);
    private static final String LOG_TAG = "运行值班首页-电梯";
    private static final String FUNC_TYPE = "funcType";
    private static final String ACTIVE_STATUS_VALUE = "1";

    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.ELEVATOR;
    }

    @Override
    public CardDateVO<ElevatorCardDataVO> execute(HomePageParam param) {
        CardDateVO<ElevatorCardDataVO> resultVo = new CardDateVO<>();
        resultVo.setChildCardList(new ArrayList<>());
        ElevatorCardDataVO elevatorCardData = new ElevatorCardDataVO();
        ElevatorCardDataVO escalatorCardData = new ElevatorCardDataVO();

        //摄像头
        ElevatorCardDataVO elevatorCameraCardData = new ElevatorCardDataVO();
        ElevatorCardDataVO escalatorCameraCardData = new ElevatorCardDataVO();
        Set<String> allObjIds = new ConcurrentHashSet<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                setElevatorCardData(allObjIds,elevatorCardData,elevatorCameraCardData);
            } catch (Exception e) {
                LogExecutor.project().error(LOGGER, LOG_TAG, "直梯数据加载异常", e);
            }
        }, BusinessThreadPoolExecutor.ttlExecutor()));
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                setEscalatorCardData(allObjIds,escalatorCardData,escalatorCameraCardData);
            } catch (Exception e) {
                LogExecutor.project().error(LOGGER, LOG_TAG, "扶梯数据加载异常", e);
            }
        }, BusinessThreadPoolExecutor.ttlExecutor()));

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "异步任务执行异常", e.getCause());
        }

        if(elevatorCardData.getName() != null){
            resultVo.getChildCardList().add(elevatorCardData);
        }

        if(elevatorCameraCardData.getName() != null){
            resultVo.getChildCardList().add(elevatorCameraCardData);
        }

        if(escalatorCardData.getName() != null){
            resultVo.getChildCardList().add(escalatorCardData);
        }

        if(escalatorCameraCardData.getName() != null){
            resultVo.getChildCardList().add(escalatorCameraCardData);
        }


        //构建大卡片信息的标签
        DutyHomePageGroupDTO dutyHomePageGroup = new DutyHomePageGroupDTO();
        CardTagVO tag = buildCardTagVO(dutyHomePageGroup, allObjIds);
        resultVo.setCardTag(tag);
        return resultVo;
    }

    private List<DtpRwdEquipmentDTO> queryEquipment(String classCode, List<String> columns) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                //.addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST)
                .setClassCode(Arrays.asList(classCode))
                .setWithColumns(columns);
        return dtpRwdClientService.listEquipment(queryParam);
    }

    private List<DtpRwdObjectDTO> queryCamera(Set<String> ids) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setClassCode(DtpRwdClassCodeEnum.SPVSCM.value())
                .setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id))
                .setRelationFrom(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQ_SERVICE_EQ).setObjTo(ids));
        return dtpRwdClientService.listObject(queryParam);
    }

    private Set<String> processInfoCode(Set<String> ids,Map<String,AtomicInteger> cameraMap){
        Map<String, Set<String>> classCodeAndIdsMap = new HashMap<>();
        classCodeAndIdsMap.put(DtpRwdClassCodeEnum.SPVSCM.value(),ids);
        Set<String> infoCodes = Sets.newHashSet(InfoCodeEnum.ONLINE_STATUS.getCode());

        Map<String, Set<String>> ruleMap = iotDataCacheService.mapObjectIdAndExistsRuleInfoCodes(classCodeAndIdsMap, infoCodes);
        if(MapUtil.isEmpty(ruleMap)){
            ids.clear();
            return ids;
        }

        ids = ruleMap.keySet();

        List<InfoCodeValueVO> infoCodeValues = iotDataCacheService.listInfoCodeValue(ContextUtil.getProjectId(), null, ids, Arrays.asList(InfoCodeEnum.RUN_STATUS.getCode(), InfoCodeEnum.ONLINE_STATUS.getCode()));
        infoCodeValues.stream().forEach(infoCodeValueVO -> {
            if (infoCodeValueVO.getInfoCode().equals(InfoCodeEnum.ONLINE_STATUS.getCode())) {
                if (ACTIVE_STATUS_VALUE.equals(infoCodeValueVO.getDataValue())) {
                    if (cameraMap.get(infoCodeValueVO.getObjectId()) != null) {
                        cameraMap.get(infoCodeValueVO.getObjectId()).incrementAndGet();
                    }
                }
            }
        });

        return ids;
    }

    private void processStatusValues(Set<String> ids, String statusCode, Map<String,AtomicInteger> escalatorMap, boolean activeMatch) {
        List<InfoCodeValueVO> infoCodeValues = iotDataCacheService.listInfoCodeValue(
                ContextUtil.getProjectId(), null, ids, Arrays.asList(statusCode));

        infoCodeValues.forEach(infoCodeValueVO -> {
            boolean conditionMet = activeMatch ?
                    ACTIVE_STATUS_VALUE.equals(infoCodeValueVO.getDataValue()) :
                    !ACTIVE_STATUS_VALUE.equals(infoCodeValueVO.getDataValue());

            if (conditionMet) {
                AtomicInteger counter = escalatorMap.get(infoCodeValueVO.getObjectId());
                if (counter != null) {
                    counter.incrementAndGet();
                }
            }
        });
    }

    /**
     * @author wxf
     * @description 按优先级获取电梯信息点
     * @date 10:16 2025/6/5
     * @param classCodeAndIdsMap
     * @param escalatorMap
     * @return java.lang.String
     **/
    private String processElevatorInfoCode(Map<String, Set<String>> classCodeAndIdsMap,Map<String,AtomicInteger> escalatorMap){
        //按优先级查询规则和信息点数据
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndIdsMap, Arrays.asList(InfoCodeEnum.LOCK_STATUS.getCode(),InfoCodeEnum.RUN_STATUS.getCode(),InfoCodeEnum.ONLINE_STATUS.getCode()));
        Set<String> existsRules = objectIdAndExistsRuleInfoCodes.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        Set<String> ids = objectIdAndExistsRuleInfoCodes.keySet();

        if(existsRules.contains(InfoCodeEnum.LOCK_STATUS.getCode())){
            processStatusValues(ids, InfoCodeEnum.LOCK_STATUS.getCode(), escalatorMap, false);
            return InfoCodeEnum.LOCK_STATUS.getCode();
        }else if(existsRules.contains(InfoCodeEnum.RUN_STATUS.getCode())){
            processStatusValues(ids, InfoCodeEnum.RUN_STATUS.getCode(), escalatorMap, true);
            return InfoCodeEnum.RUN_STATUS.getCode();
        }else if(existsRules.contains(InfoCodeEnum.ONLINE_STATUS.getCode())){
            processStatusValues(ids, InfoCodeEnum.ONLINE_STATUS.getCode(), escalatorMap, true);
            return InfoCodeEnum.ONLINE_STATUS.getCode();
        }

        return null;
    }

    private ElevatorChildCardDataVO buildChildCard(String name, int total, int runningTotal, String classCode, String infoCode) {
        ElevatorChildCardDataVO card = new ElevatorChildCardDataVO();
        //card.setName(name);
        card.setClassCode(classCode);
        card.setEquipmentTotal(total);
        card.setRunningTotal(runningTotal);
        card.setInfoCode(infoCode);
        return card;
    }

    public ElevatorCardDataVO setEscalatorCardData(Set<String> allObjIds,ElevatorCardDataVO elevatorCardDataVO,ElevatorCardDataVO cameraCardDataVO) {
        // 查直梯数据
        List<DtpRwdEquipmentDTO> allEscalatorDtos = queryEquipment(DtpRwdClassCodeEnum.ELELES.getValue(), Arrays.asList(DtpRwdObjectDTO.Fields.id,FUNC_TYPE,MONITORING_SURVEILLANCE_STATUS));
        if(CollectionUtil.isEmpty(allEscalatorDtos)){
            return null;
        }

        List<DtpRwdEquipmentDTO> escalatorDtos = allEscalatorDtos.stream().filter(dtpRwdEquipmentDTO -> {
            if (MONITORING_SURVEILLANCE_STATUS_LIST.contains(dtpRwdEquipmentDTO.analyzeProperties(MONITORING_SURVEILLANCE_STATUS))) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        if (CollectionUtil.isNotEmpty(escalatorDtos)) {
            elevatorCardDataVO.setElevatorDataList(new ArrayList<>());
            elevatorCardDataVO.setName(LocaleMessageUtil.getMessage("key_1e4083b8459b49f5a076dd5d484c1b98"));
            elevatorCardDataVO.setClassCode(DtpRwdClassCodeEnum.ELELES.getValue());
        }

        AtomicInteger runningTotal = new AtomicInteger();
        AtomicInteger cameraRunningTotal = new AtomicInteger();

        Set<String> allEscalatorIds = allEscalatorDtos.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        Set<String> escalatorIds = escalatorDtos.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());

        Map<String, AtomicInteger> escalatorMap = escalatorDtos.stream().collect(Collectors.toMap(DtpRwdEquipmentDTO::getId, v -> runningTotal, (o1, o2) -> o1));

        List<DtpRwdObjectDTO> cameraDtos = queryCamera(allEscalatorIds);
        Map<String, AtomicInteger> cameraMap = cameraDtos.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, v -> cameraRunningTotal, (o1, o2) -> o1));

        Set<String> cameraIds = cameraMap.keySet();
        Set<String> allIds = new HashSet<>(escalatorIds);
        allIds.addAll(cameraIds);
        allObjIds.addAll(allIds);
        cameraIds = processInfoCode(cameraIds, cameraMap);

        Map<String, Set<String>> classCodeAndIdsMap = new HashMap<>();
        classCodeAndIdsMap.put(DtpRwdClassCodeEnum.ELELES.getValue(), escalatorIds);
        String infoCode = processElevatorInfoCode(classCodeAndIdsMap,  escalatorMap);

        ElevatorChildCardDataVO cardDataVO = buildChildCard(LocaleMessageUtil.getMessage("key_b82cadb33e2e495c884a1ba9214003d5"), escalatorIds.size(), runningTotal.get(), DtpRwdClassCodeEnum.ELELES.getValue(),infoCode);
        ElevatorChildCardDataVO cameraDataVO = buildChildCard(LocaleMessageUtil.getMessage("key_48b7cb2c81ec4a9c9bce142257206f78"), cameraIds.size(), cameraRunningTotal.get(), DtpRwdClassCodeEnum.SPVSCM.getValue(),null);
        if(elevatorCardDataVO.getElevatorDataList() != null) {
            elevatorCardDataVO.getElevatorDataList().add(cardDataVO);
        }
        //elevatorCardDataVO.getCameraDataList().add(cameraCardDataVO);

        if(CollectionUtil.isNotEmpty(cameraIds)) {
            cameraCardDataVO.setName(LocaleMessageUtil.getMessage("key_48b7cb2c81ec4a9c9bce142257206f77"));
            cameraCardDataVO.setClassCode(DtpRwdClassCodeEnum.SPVSCM.getValue());
            cameraCardDataVO.setCameraDataList(Arrays.asList(cameraDataVO));
        }
        return elevatorCardDataVO;
    }

    public ElevatorCardDataVO setElevatorCardData(Set<String> allObjIds,ElevatorCardDataVO elevatorCardDataVO,ElevatorCardDataVO cameraCardDataVO) {
        //ElevatorCardDataVO elevatorCardDataVO = new ElevatorCardDataVO();

        //elevatorCardDataVO.setCameraDataList(new ArrayList<>());

        // 查直梯数据
        List<DtpRwdEquipmentDTO> allElevatorDtos = queryEquipment(DtpRwdClassCodeEnum.ELELEL.getValue(), Arrays.asList(DtpRwdObjectDTO.Fields.id,FUNC_TYPE,MONITORING_SURVEILLANCE_STATUS));
        if(CollectionUtil.isEmpty(allElevatorDtos)){
            return null;
        }

        List<DtpRwdEquipmentDTO> elevatorDtos = allElevatorDtos.stream().filter(dtpRwdEquipmentDTO -> {
            if (MONITORING_SURVEILLANCE_STATUS_LIST.contains(dtpRwdEquipmentDTO.analyzeProperties(MONITORING_SURVEILLANCE_STATUS))) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        /*
        if (CollectionUtil.isEmpty(elevatorDtos)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未获取到直梯设备");
            return null;
        }
         */
        if(CollectionUtil.isNotEmpty(elevatorDtos)){
            elevatorCardDataVO.setElevatorDataList(new ArrayList<>());
            elevatorCardDataVO.setName(LocaleMessageUtil.getMessage("key_387f0cc66fbe4b4397b72051c16ba06e"));
            elevatorCardDataVO.setClassCode(DtpRwdClassCodeEnum.ELELEL.getValue());
        }

        AtomicInteger equipmentTotal1 = new AtomicInteger();
        AtomicInteger equipmentTotal2 = new AtomicInteger();
        AtomicInteger runningTotal1 = new AtomicInteger();
        AtomicInteger runningTotal2 = new AtomicInteger();
        AtomicInteger cameraRunningTotal = new AtomicInteger();
        Map<String, AtomicInteger> id2cardMap = new HashMap<>();

        elevatorDtos.stream().forEach(dtpRwdEquipmentDTO -> {
            String funcType = dtpRwdEquipmentDTO.analyzeProperties(FUNC_TYPE);
            if ("1".equals(funcType)) {
                equipmentTotal1.getAndIncrement();
                id2cardMap.put(dtpRwdEquipmentDTO.getId(), runningTotal1);
            } else if ("2".equals(funcType)) {
                equipmentTotal2.getAndIncrement();
                id2cardMap.put(dtpRwdEquipmentDTO.getId(), runningTotal2);
            }
        });

        //查摄像头
        Set<String> allElevatorIds = allElevatorDtos.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        Set<String> elevatorIds = elevatorDtos.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());

        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCode(DtpRwdClassCodeEnum.SPVSCM.getValue());
        queryParam.setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id));
        List<DtpRwdEquipmentDTO> cameraDtos = dtpRwdClientService.listLocationInsideByEquipmentIds(
                false, ContextUtil.getProjectId(), allElevatorIds, queryParam);

        Map<String, AtomicInteger> cameraRunningTotalMap = new HashMap<>();
        cameraDtos.stream().forEach(dtpRwdEquipmentDTO -> {
            cameraRunningTotalMap.put(dtpRwdEquipmentDTO.getId(), cameraRunningTotal);
        });

        //查信息点
        Set<String> cameraIds = cameraDtos.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        Set<String> allIds = CollectionUtil.newHashSet(elevatorIds);
        allIds.addAll(cameraIds);
        allObjIds.addAll(allIds);

        //处理信息点
        cameraIds = processInfoCode(cameraIds, cameraRunningTotalMap);

        Map<String, Set<String>> classCodeAndIdsMap = new HashMap<>();
        classCodeAndIdsMap.put(DtpRwdClassCodeEnum.ELELEL.getValue(), elevatorIds);
        String infoCode = processElevatorInfoCode(classCodeAndIdsMap,  id2cardMap);

        ElevatorChildCardDataVO cardDataVO1 = buildChildCard(LocaleMessageUtil.getMessage("key_7deecccef98a43f29a15cd096b72afb2"), equipmentTotal1.get(), runningTotal1.get(), DtpRwdClassCodeEnum.ELELEL.getValue(),infoCode);
        cardDataVO1.setFuncType("1");

        ElevatorChildCardDataVO cardDataVO2 = buildChildCard(LocaleMessageUtil.getMessage("key_270cffaabb22407682537713050d0b39"), equipmentTotal2.get(), runningTotal2.get(), DtpRwdClassCodeEnum.ELELEL.getValue(),infoCode);
        cardDataVO2.setFuncType("2");

        ElevatorChildCardDataVO cameraDataVO = buildChildCard(LocaleMessageUtil.getMessage("key_48b7cb2c81ec4a9c9bce142257206f76"), cameraIds.size(), cameraRunningTotal.get(), DtpRwdClassCodeEnum.SPVSCM.getValue(),null);
        if (cardDataVO1.getEquipmentTotal() > 0) {
            elevatorCardDataVO.getElevatorDataList().add(cardDataVO1);
        }
        if (cardDataVO2.getEquipmentTotal() > 0) {
            elevatorCardDataVO.getElevatorDataList().add(cardDataVO2);
        }
        if(CollectionUtil.isNotEmpty(cameraIds)) {
            cameraCardDataVO.setName(LocaleMessageUtil.getMessage("key_48b7cb2c81ec4a9c9bce142257206f76"));
            cameraCardDataVO.setClassCode(DtpRwdClassCodeEnum.SPVSCM.getValue());
            cameraCardDataVO.setCameraDataList(Arrays.asList(cameraDataVO));
        }
        return elevatorCardDataVO;
    }
}
