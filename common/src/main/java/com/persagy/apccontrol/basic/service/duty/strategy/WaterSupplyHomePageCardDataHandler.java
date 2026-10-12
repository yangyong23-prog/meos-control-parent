package com.persagy.apccontrol.basic.service.duty.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.map.MapUtil;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardDateVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.WaterSupplyCardDataVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS;
import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS_LIST;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WaterSupplyHomePageCardDataHandler extends AbstractHomePageCardDataHandler<CardDateVO<WaterSupplyCardDataVO>> {

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer iotDataCacheService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private static final Logger LOGGER = LoggerFactory.getLogger(WaterSupplyHomePageCardDataHandler.class);
    private static final String LOG_TAG = "运行值班首页-给排水";


    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.WATER_SUPPLY;
    }

    @Override
    public CardDateVO<WaterSupplyCardDataVO> execute(HomePageParam param) {

        CardDateVO<WaterSupplyCardDataVO> resultVo = new CardDateVO<>();
        resultVo.setRunningMonitorType(getRunningMonitorType());
        resultVo.setChildCardList(new ArrayList<>());
        Set<String> objIds = new ConcurrentHashSet<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        WaterSupplyCardDataVO wsdwppCardDataVO = new WaterSupplyCardDataVO();
        wsdwppCardDataVO.setName(LocaleMessageUtil.getMessage("key_17544a47cfbf4a45b087fba07b9d82b2"));
        wsdwppCardDataVO.setLevelAlarm(null);
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                setWsdwppCardData(wsdwppCardDataVO, objIds);
            } catch (Exception e) {
                LogExecutor.project().error(LOGGER, LOG_TAG, "生活给水数据加载异常", e);
            }
        }, BusinessThreadPoolExecutor.ttlExecutor()));

        WaterSupplyCardDataVO wsstcpCardDataVO = new WaterSupplyCardDataVO();
        wsstcpCardDataVO.setName(LocaleMessageUtil.getMessage("key_0465ea9a96f34b3fbc9835b0a2226c06"));
        futures.add(createCardDataFuture(wsstcpCardDataVO, DtpRwdClassCodeEnum.WSSTCP, objIds));

        WaterSupplyCardDataVO wsstosCardDataVO = new WaterSupplyCardDataVO();
        wsstosCardDataVO.setName(LocaleMessageUtil.getMessage("key_04a3eb317a7e42878797cd42277c9248"));
        futures.add(createCardDataFuture(wsstosCardDataVO, DtpRwdClassCodeEnum.WSSTOS, objIds));

        WaterSupplyCardDataVO wsstorCardDataVO = new WaterSupplyCardDataVO();
        wsstorCardDataVO.setName(LocaleMessageUtil.getMessage("key_b5206d5093b9477fb339a2c647ecc57b"));
        futures.add(createCardDataFuture(wsstorCardDataVO, DtpRwdClassCodeEnum.WSSTOR, objIds));
        
        WaterSupplyCardDataVO wsstluCardDataVO = new WaterSupplyCardDataVO();
        wsstluCardDataVO.setName(LocaleMessageUtil.getMessage("key_b5206d5093b9477fb339a2c647234532"));
        futures.add(createCardDataFuture(wsstluCardDataVO, DtpRwdClassCodeEnum.WSSTLU, objIds));
        
        
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "异步任务执行异常", e.getCause());
        }

        addIfValid(resultVo.getChildCardList(), wsdwppCardDataVO);
        addIfValid(resultVo.getChildCardList(), wsstcpCardDataVO);
        addIfValid(resultVo.getChildCardList(), wsstosCardDataVO);
        addIfValid(resultVo.getChildCardList(), wsstorCardDataVO);
        addIfValid(resultVo.getChildCardList(), wsstluCardDataVO);
        //构建大卡片信息的标签
        DutyHomePageGroupDTO dutyHomePageGroup = new DutyHomePageGroupDTO();
        CardTagVO tag = buildCardTagVO(dutyHomePageGroup, objIds);
        resultVo.setCardTag(tag);

        return resultVo;
    }

    private CompletableFuture<Void> createCardDataFuture(WaterSupplyCardDataVO vo, DtpRwdClassCodeEnum code, Set<String> objIds) {
        return CompletableFuture.runAsync(() -> {
            try {
                setCardData(vo, code, objIds);
            } catch (Exception e) {
                LogExecutor.project().error(LOGGER, LOG_TAG, code.getReasonPhrase() + "数据加载异常", e);
            }
        }, BusinessThreadPoolExecutor.ttlExecutor());
    }

    private void addIfValid(List<WaterSupplyCardDataVO> list, WaterSupplyCardDataVO vo) {
        if (vo != null && (vo.getLevelAlarm() != null || vo.getEquipmentTotal() != null || vo.getRunningTotal() != null)) {
            list.add(vo);
        }
    }

    //生活给水
    private void setWsdwppCardData(WaterSupplyCardDataVO waterSupplyCardDataVO,Set<String> objIds){
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST)
                .setClassCode(Arrays.asList(DtpRwdClassCodeEnum.WSDWPP.getValue()))
                .setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode));

        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDtos = dtpRwdClientService.listEquipment(queryParam);
        if(CollectionUtils.isEmpty(dtpRwdEquipmentDtos)){
            LogExecutor.project().info(LOGGER, LOG_TAG, "未获取到设备: "+DtpRwdClassCodeEnum.WSDWPP.getReasonPhrase());
            //waterSupplyCardDataVO.setName(null);
        }else {
            Set<String> ids = dtpRwdEquipmentDtos.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
            List<InfoCodeValueVO> infoCodeValues = iotDataCacheService.listInfoCodeValue(ContextUtil.getProjectId(), null, ids, Arrays.asList(InfoCodeEnum.RUN_STATUS.getCode()));
            int runningTotal = infoCodeValues.stream().filter(v -> Objects.nonNull(v.getInfoCodeValue()) && "1".equals(v.getInfoCodeValue().toString()))
                    .collect(Collectors.toList()).size();
            waterSupplyCardDataVO.setEquipmentTotal(ids.size());
            waterSupplyCardDataVO.setRunningTotal(runningTotal);
            waterSupplyCardDataVO.setClassCode(DtpRwdClassCodeEnum.WSDWPP.getValue());
            objIds.addAll(ids);
        }
    }

    private void setCardData(WaterSupplyCardDataVO waterSupplyCardDataVO, DtpRwdClassCodeEnum classCode, Set<String> objIds){
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setClassCode(Arrays.asList(classCode.getValue()))
                .setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode,MONITORING_SURVEILLANCE_STATUS));
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDtos = dtpRwdClientService.listEquipment(queryParam);
        if(CollectionUtils.isEmpty(dtpRwdEquipmentDtos)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未获取到设备: " + classCode.getReasonPhrase());
            //waterSupplyCardDataVO.setName(null);
            return;
        }
        waterSupplyCardDataVO.setClassCode(classCode.getValue());
        Set<String> ids = dtpRwdEquipmentDtos.parallelStream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        Set<String> controlledIds = new HashSet<>();

        //是否有可监可控设备
        dtpRwdEquipmentDtos.stream().forEach(dtpRwdEquipmentDTO -> {
            String stat = dtpRwdEquipmentDTO.analyzeProperties(MONITORING_SURVEILLANCE_STATUS);
            if(MONITORING_SURVEILLANCE_STATUS_LIST.contains(stat)){
                objIds.add(dtpRwdEquipmentDTO.getId());
                controlledIds.add(dtpRwdEquipmentDTO.getId());
            }
        });

        if(CollectionUtil.isNotEmpty(controlledIds)) {
            Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
            classCodeAndObjectIds.put(classCode.getValue(), controlledIds);
            //查实例化规则
            Map<String, Set<String>> ruleSetMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndObjectIds, Arrays.asList(InfoCodeEnum.SUPER_HIGH_LEVEL_ALARM.getCode(), InfoCodeEnum.HIGH_LEVEL_ALARM.getCode(),InfoCodeEnum.HIGH_LEV_ALARM.getCode()));

            if(MapUtil.isNotEmpty(ruleSetMap)) {
                //查液位报警
                List<InfoCodeValueVO> infoCodeValues = iotDataCacheService.listInfoCodeValue(ContextUtil.getProjectId(), null, controlledIds, Arrays.asList(InfoCodeEnum.HIGH_LEVEL_ALARM.getCode(), InfoCodeEnum.SUPER_HIGH_LEVEL_ALARM.getCode(),InfoCodeEnum.HIGH_LEV_ALARM.getCode()));
                int levelAlarm = (int) infoCodeValues.stream()
                        .filter(v -> "1".equals(Optional.ofNullable(v.getInfoCodeValue()).orElse("").toString()))
                        .count();
                waterSupplyCardDataVO.setLevelAlarm(levelAlarm);
            }

            //查泵
            queryParam.addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST);
            queryParam.setClassCode(DtpRwdClassCodeEnum.WSSTSP.getValue());
            queryParam.setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode));
            List<DtpRwdEquipmentDTO> subDtpRwdEquipmentDtos = Lists.newArrayList();
            if(classCode.getValue().equals(DtpRwdClassCodeEnum.WSSTLU.getValue()) ||
                    classCode.getValue().equals(DtpRwdClassCodeEnum.WSSTOR.getValue())){
                //走新关系
                subDtpRwdEquipmentDtos = dtpRwdClientService.listSubmersibleSewagePumpByEquipmentIds(
                        false, ContextUtil.getProjectId(), ids, queryParam);
            }else {
                //依旧走旧关系
                subDtpRwdEquipmentDtos = dtpRwdClientService.listLocationInsideByEquipmentIds(
                        false, ContextUtil.getProjectId(), ids, queryParam);
            }
            if (CollectionUtils.isNotEmpty(subDtpRwdEquipmentDtos)) {
                waterSupplyCardDataVO.setEquipmentTotal(subDtpRwdEquipmentDtos.size());

                Set<String> subObjIds = subDtpRwdEquipmentDtos.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
                List<InfoCodeValueVO> runStatusInfoCodeValues = iotDataCacheService.listInfoCodeValue(ContextUtil.getProjectId(), null, subObjIds, Arrays.asList(InfoCodeEnum.RUN_STATUS.getCode()));
                int runningTotal = (int) runStatusInfoCodeValues.stream().filter(v -> Objects.nonNull(v.getInfoCodeValue()) && "1".equals(v.getInfoCodeValue().toString())).count();
                waterSupplyCardDataVO.setRunningTotal(runningTotal);
                objIds.addAll(subObjIds);
            }
        }
    }
}
