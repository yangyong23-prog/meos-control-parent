package com.persagy.apccontrol.basic.outside.feign.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.abstracts.AbstractRunningMonitorServer;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.DmpObjTypeEnum;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigByObjectQueryParam;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigObjectByFiltersQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.infocode.*;
import com.persagy.apccontrol.basic.model.vo.knowledge.DtpKnowledgeInfoCdeDataSourceVO;
import com.persagy.apccontrol.basic.model.vo.outside.energy.*;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.ImportanceDegreeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.SystemStatisticsService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import com.persagy.apccontrol.basic.service.parking.ParkingService;
import com.persagy.apccontrol.basic.service.security.SecurityService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum.MODEL_VIEW;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/8/31 15:19
 * @Version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemStatisticsServiceImpl implements SystemStatisticsService {

    private final InfoCodeConfigService infoCodeConfigService;

    private final InfoCodeGateway infoCodeGateway;

    private final AbstractCommonServer abstractCommonServer;

    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    @Lazy
    private final LocalRedisCacheHandler localRedisCacheHandler;

    private final DtpRwdClientService dtpRwdClientService;

    private final BizDtpRwdService bizDtpRwdService;

    private final AbstractRunningMonitorServer abstractRunningMonitorServer;

    @Override
    public SubstationStatistics substationAndDistributionStatistics(String projectId) {
        SubstationStatistics substationStatistics = new SubstationStatistics();
        SystemGraphCommon systemGraphCommon = systemGraphStatistics(projectId, LocaleMessageUtil.getMessage("key_1251998e58e149a794a42a0ca350201c"),
                Sets.newHashSet("lULoadRate", "onlineStatus"));
        if (Objects.isNull(systemGraphCommon)) {
            return substationStatistics;
        }
        List<ExceptionEventBaseInfoVO> eventInfoDTOS = systemGraphCommon.getEventInfoDTOS();
        List<InfoCodeValueVO> listInfoCodeValueInOneHour = systemGraphCommon.getListInfoCodeValueInOneHour();
        if (CollectionUtils.isEmpty(listInfoCodeValueInOneHour)) {
            listInfoCodeValueInOneHour = new ArrayList<>();
        }
        Map<String, DtpRwdObjectDTO> dtpRwdObjectDTOMap = systemGraphCommon.getDtpRwdObjectDTOMap();
        if (MapUtils.isEmpty(dtpRwdObjectDTOMap)) {
            dtpRwdObjectDTOMap = new HashMap<>();
        }

        Integer exceptionEventCount = 0;
        if (CollectionUtils.isNotEmpty(eventInfoDTOS)) {
            Set<Integer> importanceDegreeSet = new HashSet<>();
            importanceDegreeSet.add(ImportanceDegreeEnum.HIGH.getValue());
            for (ExceptionEventBaseInfoVO eventInfoDTO : eventInfoDTOS) {
                int value = eventInfoDTO.getImportanceDegree().getValue();
                if (importanceDegreeSet.contains(value)) {
                    exceptionEventCount++;
                }
            }
        }
        Integer offlineCount = 0;


        Map<String, InfoCodeValueVO> lULoadRateMap = new HashMap<>();
        for (InfoCodeValueVO infoCodeValueVO : listInfoCodeValueInOneHour) {
            String infoCode = infoCodeValueVO.getInfoCode();
            Object infoCodeValue = infoCodeValueVO.getInfoCodeValue();
            String objectId = infoCodeValueVO.getObjectId();
            if ("onlineStatus".equals(infoCode)) {
                if ("0".equals(infoCodeValue)) {
                    offlineCount++;
                }
            } else if ("lULoadRate".equals(infoCode)) {
                lULoadRateMap.put(objectId, infoCodeValueVO);
            }
        }

        List<SubstationObjectRwdStatistics> substationObjectRwdStatisticsList = new ArrayList<>();
        for (Map.Entry<String, DtpRwdObjectDTO> dtpRwdObjectDTOEntry : dtpRwdObjectDTOMap.entrySet()) {
            String objectId = dtpRwdObjectDTOEntry.getKey();
            DtpRwdObjectDTO dtpRwdObjectDTO = dtpRwdObjectDTOEntry.getValue();
            SubstationObjectRwdStatistics substationObjectRwdStatistics = new SubstationObjectRwdStatistics();
            substationObjectRwdStatistics.setObjectId(dtpRwdObjectDTO.getId());
            substationObjectRwdStatistics.setName(dtpRwdObjectDTO.getName());
            substationObjectRwdStatistics.setLocalId(dtpRwdObjectDTO.getLocalId());
            substationObjectRwdStatistics.setLocalName(dtpRwdObjectDTO.getLocalName());
            InfoCodeValueVO infoCodeValueVO = lULoadRateMap.get(objectId);
            if (Objects.nonNull(infoCodeValueVO)) {
                Object infoCodeValue = infoCodeValueVO.getDataValue();
                if (Objects.nonNull(infoCodeValue)) {
                    substationObjectRwdStatistics.setLULoadRate(infoCodeValue.toString());
                } else {
                    substationObjectRwdStatistics.setLULoadRate("");
                }
            } else {
                substationObjectRwdStatistics.setLULoadRate("");
            }
            substationObjectRwdStatisticsList.add(substationObjectRwdStatistics);
        }
        substationStatistics.setOfflineCount(offlineCount);
        substationStatistics.setExceptionEventCount(exceptionEventCount);
        substationStatistics.setObjects(substationObjectRwdStatisticsList);

        return substationStatistics;

    }

    /**
     * 系统图数据统计公共
     *
     * @param projectId     项目id
     * @param graphTypeCode 系统图类型
     * @param infoCodes     信息点
     * @return 系统图公共返回
     */
    public SystemGraphCommon systemGraphStatistics(String projectId, String graphTypeCode, Set<String> infoCodes) {
        SystemGraphCommon systemGraphCommon = new SystemGraphCommon();
        // 2.在获取该系统图下所有的设备
        Set<String> equipmentIds = localRedisCacheHandler
                .getObjectIdsBySystemGraph(projectId, projectId, "project", graphTypeCode);
        if (CollectionUtils.isEmpty(equipmentIds)) {
            return systemGraphCommon;
        }
        //对象
        List<DtpRwdObjectDTO> objects = dtpRwdClientService.listObject(false, false,
                ContextUtil.getProjectId(), new DtpRwdObjectQueryParam().setId(equipmentIds)
                        .setObjType(DmpObjTypeEnum.EQUIPMENT.getValue()));

        if (CollectionUtils.isEmpty(objects)) {
            return systemGraphCommon;
        }
        Map<String, DtpRwdObjectDTO> dtpRwdObjectDTOMap = new HashMap<>();
        Set<String> objectSets = new HashSet<>();
        for (DtpRwdObjectDTO object : objects) {
            dtpRwdObjectDTOMap.put(object.getId(), object);
            objectSets.add(object.getId());
        }
        systemGraphCommon.setDtpRwdObjectDTOMap(dtpRwdObjectDTOMap);
        //异常事件
        List<ExceptionEventBaseInfoVO> eventInfoDTOS = this.abstractRunningMonitorServer.queryAbnormalEventList
                (ContextUtil.getGroupCode(), projectId, projectId, "project", graphTypeCode);
        systemGraphCommon.setEventInfoDTOS(eventInfoDTOS);

        //信息点查询
        List<InfoCodeValueVO> listInfoCodeValueInOneHour = abstractIotDataCacheServer.listInfoCodeValue
                (projectId, null, objectSets, infoCodes);

        // objectId+infocode===>>InfoCodeValueVO
        Map<String, InfoCodeValueVO> infoCodeValueVOMap = new HashMap<>();

        Map<String, List<DtpKnowledgeInfoCdeDataSourceVO>> infoCodeValueVODatasourceMap = new HashMap<>();
        for (InfoCodeValueVO infoCodeValueVO : listInfoCodeValueInOneHour) {
            String objectId = infoCodeValueVO.getObjectId();
            String infoCode = infoCodeValueVO.getInfoCode();
            infoCodeValueVOMap.put(objectId + infoCode, infoCodeValueVO);
            infoCodeValueVODatasourceMap.put(infoCode, infoCodeValueVO.getDataSources());
        }
        List<InfoCodeValueVO> listInfoCodeValues = new ArrayList<>();
        for (DtpRwdObjectDTO object : objects) {
            String objectId = object.getId();
            for (String infoCode : infoCodes) {
                InfoCodeValueVO infoCodeValueVO = infoCodeValueVOMap.get(objectId + infoCode);
                if (Objects.isNull(infoCodeValueVO)) {
                    InfoCodeValueVO infoCodeValueVONull = new InfoCodeValueVO();
                    infoCodeValueVONull.setObjectId(objectId);
                    infoCodeValueVONull.setInfoCode(infoCode);
                    infoCodeValueVONull.setDataSources(infoCodeValueVODatasourceMap.get(infoCode));
                    listInfoCodeValues.add(infoCodeValueVONull);
                } else {
                    listInfoCodeValues.add(infoCodeValueVO);
                }
            }
        }
        systemGraphCommon.setListInfoCodeValueInOneHour(listInfoCodeValues);
        return systemGraphCommon;
    }


    @Override
    public StatisticsPassengerFlowVO statisticsPassengerFlow(String projectId) throws Exception {
        StatisticsPassengerFlowVO statisticsPassengerFlowVO = new StatisticsPassengerFlowVO();

        CompletableFuture<Void> onlineFuture = CompletableFuture.supplyAsync(() -> {
            // 1.获取当前项目客流模块下具体的设备ID集合，返回对应的离线数量，与高等级的异常事件数量
            InfoCodeObjectQueryParam infoCodeObjectQueryParam = new InfoCodeObjectQueryParam();
            infoCodeObjectQueryParam.setRunningMonitorType(RunningMonitorTypeEnum.PASSENGER_FLOW);
            infoCodeObjectQueryParam.setTemplateTypes(Lists.newArrayList(MODEL_VIEW));
            List<DtpRwdObjectDTO> objects = infoCodeGateway.listTemplateTypeObjects(infoCodeObjectQueryParam);
            List<String> collectIds = objects.stream().map(DtpRwdObjectDTO::getId).distinct().collect(Collectors.toList());

            // 设备在离线判断
            Map<String, Map<String, String>> objectInfoCodeValueMap = this.abstractIotDataCacheServer.queryIotRealDataMap(ContextUtil.getGroupCode(), projectId, Sets.newHashSet(collectIds), Sets.newHashSet(InfoCodeEnum.ONLINE_STATUS.getCode()));
            long offlineCount = objectInfoCodeValueMap.values().stream().filter(item -> !"1".equals(item.get(InfoCodeEnum.ONLINE_STATUS.getCode()))).count();
            statisticsPassengerFlowVO.setOfflineCount((int) offlineCount);

            List<ExceptionEventBaseInfoVO> exceptionEventInfoVOS =
                    abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(collectIds,
                            RunningMonitorTypeEnum.PASSENGER_FLOW.getRunningMonitorStrategy().exceptionFilterRiskMeasureAimLevel2());
            statisticsPassengerFlowVO.setExceptionEventCount(exceptionEventInfoVOS.size());
            return null;
        }, BusinessThreadPoolExecutor.ttlExecutor());

        // 2.获取曲线图与实时客流量
        CompletableFuture<Void> mapFuture = CompletableFuture.supplyAsync(() -> {
            InfoCodeConfigByObjectQueryParam objectQueryParam = new InfoCodeConfigByObjectQueryParam();
            objectQueryParam.setObjectIds(Sets.newHashSet(projectId));
            objectQueryParam.setRunningMonitorType(RunningMonitorTypeEnum.PASSENGER_FLOW);
            objectQueryParam.setTemplateType(InfoCodeTemplateTypeEnum.DETAIL);
            List<InfoCodeObjectGroupVO> infoCodeObjectGroupVOS = this.infoCodeConfigService.listInfoCodeConfigsByObject(objectQueryParam);
            List<InfoCodeGroupVO> relInfoCodeGroupConfigs = CollectionUtils.isEmpty(infoCodeObjectGroupVOS) ? null : infoCodeObjectGroupVOS.get(0).getRelInfoCodeGroupConfigs();
            if (CollectionUtils.isEmpty(relInfoCodeGroupConfigs)) {
                return null;
            }

            // 获取客流的信息点配置
            Optional<InfoCodeGroupVO> currentPassengerFlowStatisticOptional = relInfoCodeGroupConfigs.stream().filter(item -> "currentPassengerFlowStatistic".equals(item.getGroupCode())).findFirst();
            InfoCodeGroupVO currentPassengerFlowStatistic = currentPassengerFlowStatisticOptional.orElse(null);
            if (currentPassengerFlowStatistic == null) {
                return null;
            }

            // 获取实时客流量
            Optional<InfoCodeConfigVO> exsitPeopleNumVO = CollectionUtils.isEmpty(currentPassengerFlowStatistic.getRelInfoCodeConfigs()) ? null
                    : currentPassengerFlowStatistic.getRelInfoCodeConfigs().stream().filter(item -> InfoCodeEnum.EXSIT_PEOPLE_NUM.getCode().equals(item.getInfoCode())).findFirst();
            String exsitPeopleNum = (String) (exsitPeopleNumVO == null || exsitPeopleNumVO.get() == null ? null : exsitPeopleNumVO.get().getDataValue());
            statisticsPassengerFlowVO.setExsitPeopleNum(StrUtil.isNumeric(exsitPeopleNum) ? Integer.valueOf(exsitPeopleNum) : null);

            // 获取曲线图数据
            Optional<InfoCodeGroupVO> statisticTrendOptional = CollectionUtils.isEmpty(currentPassengerFlowStatistic.getGroupChildren()) ? null
                    : currentPassengerFlowStatistic.getGroupChildren().stream().filter(item -> "currentPassengerFlowStatisticTrend".equals(item.getGroupCode())).findFirst();
            InfoCodeGroupVO statisticTrend = statisticTrendOptional == null || statisticTrendOptional.get() == null ? null : statisticTrendOptional.get();
            Optional<InfoCodeConfigVO> statisticTrendVOOptional = CollectionUtils.isEmpty(statisticTrend.getRelInfoCodeConfigs()) ? null
                    : statisticTrend.getRelInfoCodeConfigs().stream().filter(item -> InfoCodeEnum.EXSIT_PEOPLE_NUM.getCode().equals(item.getInfoCode())).findFirst();
            Object statisticTrendVO = statisticTrendVOOptional == null || statisticTrendVOOptional.get() == null ? null : statisticTrendVOOptional.get().getDataValue();
            if (statisticTrendVO != null && statisticTrendVO instanceof LinkedList) {
                LinkedList<InfoCodeFigureVO> linkedList = (LinkedList<InfoCodeFigureVO>) statisticTrendVO;
                List<StatisticsPassengerFlowVO.StatisticTrend> statisticTrends = new ArrayList<>();
                for (InfoCodeFigureVO infoCodeFigureVO : linkedList) {
                    Integer currentExsitPeopleNum = infoCodeFigureVO.getValue() == null ? null : ((BigDecimal) infoCodeFigureVO.getValue()).intValue();
                    statisticTrends.add(new StatisticsPassengerFlowVO.StatisticTrend(infoCodeFigureVO.getTime(), currentExsitPeopleNum));
                }
                statisticsPassengerFlowVO.setCurrentPassengerFlowStatisticTrend(statisticTrends);
            }

            return null;
        }, BusinessThreadPoolExecutor.ttlExecutor());

        // 4.获取累计客流量 accdInPeopleFlow
        EnergyDataQueryParam bodyDto = abstractCommonServer.getRwdInfo(Lists.newArrayList(projectId), Lists.newArrayList("accdInPeopleFlow"));
        bodyDto.setStartTime(DateUtils.now().toLocalDate().atStartOfDay().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
        bodyDto.setEndTime(DateUtils.now().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
        bodyDto.setIpType("1d");
        CustomerResponse<List<EnergyDataResponse>> listCustomerResponse = this.bizDtpRwdService.listIpDataInPeriod(bodyDto, projectId, ContextUtil.getGroupCode());
        EnergyDataResponse energyDataResponse = listCustomerResponse == null || CollectionUtils.isEmpty(listCustomerResponse.getContent()) ? null : listCustomerResponse.getContent().get(0);
        BigDecimal accdInPeopleFlow = energyDataResponse == null ? null : energyDataResponse.getIpValue();
        statisticsPassengerFlowVO.setAccExsitPeopleNum(accdInPeopleFlow == null ? null : accdInPeopleFlow.intValue());

        Void online = onlineFuture == null ? null : onlineFuture.get();
        Void map = mapFuture == null ? null : mapFuture.get();
        return statisticsPassengerFlowVO;
    }

    @Override
    public WaterSupplyDrainageStatisticsVO waterSupplyDrainageStatistics(String projectId) {
        WaterSupplyDrainageStatisticsVO waterSupplyDrainageStatisticsVO = new WaterSupplyDrainageStatisticsVO();
        List<ClaasCodeStatistics> claasCodes = new ArrayList<>();
        List<ExceptionEventBaseInfoVO> eventInfoDTOS = new ArrayList<>();
        SystemGraphCommon systemGraphCommonGeYouChi = systemGraphStatistics(projectId, "隔油池排水关系图", Sets.newHashSet("highLevelAlarm", "onlineStatus", "runStatus"));
        List<InfoCodeValueVO> listInfoCodeValueInOneHourGeYouChi = systemGraphCommonGeYouChi.getListInfoCodeValueInOneHour();
        ClaasCodeStatistics claasCodeStatisticsGeYouChi = new ClaasCodeStatistics();
        if (CollectionUtils.isNotEmpty(listInfoCodeValueInOneHourGeYouChi)) {
            List<InfoCodeStatistics> infoCodeStatisticsGeYouChi = InfoCodeStatistics.ofInfoCodeValueVO(listInfoCodeValueInOneHourGeYouChi);
            claasCodeStatisticsGeYouChi.setInfoCodes(infoCodeStatisticsGeYouChi);
        }
        claasCodeStatisticsGeYouChi.setClassCode(DtpRwdClassCodeEnum.WSSTOS.getValue());
        claasCodeStatisticsGeYouChi.setClassCodeName(DtpRwdClassCodeEnum.WSSTOS.getReasonPhrase());
        claasCodes.add(claasCodeStatisticsGeYouChi);

        if (CollectionUtils.isNotEmpty(systemGraphCommonGeYouChi.getEventInfoDTOS())) {
            eventInfoDTOS.addAll(systemGraphCommonGeYouChi.getEventInfoDTOS());
        }

        //潜污泵在集水坑里面
        SystemGraphCommon systemGraphCommonJiShuiKeng = systemGraphStatistics(projectId, "集水坑排水关系图", Sets.newHashSet("highLevelAlarm", "runStatus", "onlineStatus"));
        if (CollectionUtils.isNotEmpty(systemGraphCommonJiShuiKeng.getEventInfoDTOS())) {
            eventInfoDTOS.addAll(systemGraphCommonJiShuiKeng.getEventInfoDTOS());
        }

        List<InfoCodeValueVO> listInfoCodeValueInOneHour = systemGraphCommonJiShuiKeng.getListInfoCodeValueInOneHour();
        if (CollectionUtils.isEmpty(listInfoCodeValueInOneHour)) {
            listInfoCodeValueInOneHour = new ArrayList<>();
        }
        Map<String, DtpRwdObjectDTO> dtpRwdObjectDTOMap = systemGraphCommonJiShuiKeng.getDtpRwdObjectDTOMap();


        ClaasCodeStatistics claasCodeStatisticsJiShuiKeng = new ClaasCodeStatistics();
        claasCodeStatisticsJiShuiKeng.setClassCode(DtpRwdClassCodeEnum.WSSTCP.getValue());
        claasCodeStatisticsJiShuiKeng.setClassCodeName(DtpRwdClassCodeEnum.WSSTCP.getReasonPhrase());

        ClaasCodeStatistics claasCodeStatisticsQianWuBen = new ClaasCodeStatistics();
        claasCodeStatisticsQianWuBen.setClassCode(DtpRwdClassCodeEnum.WSSTSP.getValue());
        claasCodeStatisticsQianWuBen.setClassCodeName(DtpRwdClassCodeEnum.WSSTSP.getReasonPhrase());


        Integer offlineCount = 0;

        List<InfoCodeValueVO> infoCodeValueVOJiShuiKeng = new ArrayList<>();
        List<InfoCodeValueVO> infoCodeValueVOQianWuBen = new ArrayList<>();
        for (InfoCodeValueVO infoCodeValueVO : listInfoCodeValueInOneHour) {
            String objectId = infoCodeValueVO.getObjectId();
            String infoCode = infoCodeValueVO.getInfoCode();
            Object infoCodeValue = infoCodeValueVO.getInfoCodeValue();
            DtpRwdObjectDTO dtpRwdObjectDTO = dtpRwdObjectDTOMap.get(objectId);
            String classCode = dtpRwdObjectDTO.getClassCode();
            if (classCode.equals(DtpRwdClassCodeEnum.WSSTCP.getValue())) {
                infoCodeValueVOJiShuiKeng.add(infoCodeValueVO);
            } else if (classCode.equals(DtpRwdClassCodeEnum.WSSTSP.getValue())) {
                infoCodeValueVOQianWuBen.add(infoCodeValueVO);
            }
            if ("onlineStatus".equals(infoCode)) {
                if ("0".equals(infoCodeValue)) {
                    offlineCount++;
                }
            }
        }
        List<InfoCodeStatistics> infoCodeStatistics = InfoCodeStatistics.ofInfoCodeValueVO(infoCodeValueVOJiShuiKeng);
        claasCodeStatisticsJiShuiKeng.setInfoCodes(infoCodeStatistics);
        List<InfoCodeStatistics> infoCodeStatisticsQianWuBen = InfoCodeStatistics.ofInfoCodeValueVO(infoCodeValueVOQianWuBen);
        claasCodeStatisticsQianWuBen.setInfoCodes(infoCodeStatisticsQianWuBen);
        claasCodes.add(claasCodeStatisticsQianWuBen);
        claasCodes.add(claasCodeStatisticsJiShuiKeng);


        waterSupplyDrainageStatisticsVO.setOfflineCount(offlineCount);

        SystemGraphCommon systemGraphCommonGeiShuiJiFang = systemGraphStatistics(projectId, "给水机房关系图", Sets.newHashSet("onlineStatus", "runStatus"));
        List<InfoCodeValueVO> listInfoCodeValueInOneHourGeiShuiJiFang = systemGraphCommonGeiShuiJiFang.getListInfoCodeValueInOneHour();
        if (CollectionUtils.isEmpty(listInfoCodeValueInOneHourGeiShuiJiFang)) {
            listInfoCodeValueInOneHourGeiShuiJiFang = new ArrayList<>();
        }
        List<InfoCodeStatistics> infoCodeStatisticsGeiShuiJiFang = InfoCodeStatistics.ofInfoCodeValueVO(listInfoCodeValueInOneHourGeiShuiJiFang);
        ClaasCodeStatistics claasCodeStatisticsGeiShuiJiFang = new ClaasCodeStatistics();
        claasCodeStatisticsGeiShuiJiFang.setClassCode(DtpRwdClassCodeEnum.WSDWSU.getValue());
        claasCodeStatisticsGeiShuiJiFang.setClassCodeName(DtpRwdClassCodeEnum.WSDWSU.getReasonPhrase());
        claasCodeStatisticsGeiShuiJiFang.setInfoCodes(infoCodeStatisticsGeiShuiJiFang);
        claasCodes.add(claasCodeStatisticsGeiShuiJiFang);

        if (CollectionUtils.isNotEmpty(systemGraphCommonGeiShuiJiFang.getEventInfoDTOS())) {
            eventInfoDTOS.addAll(systemGraphCommonGeiShuiJiFang.getEventInfoDTOS());
        }
        Integer exceptionEventCount = 0;
        if (CollectionUtils.isNotEmpty(eventInfoDTOS)) {
            Set<Integer> importanceDegreeSet = new HashSet<>();
            importanceDegreeSet.add(ImportanceDegreeEnum.HIGH.getValue());
            for (ExceptionEventBaseInfoVO eventInfoDTO : eventInfoDTOS) {
                int value = eventInfoDTO.getImportanceDegree().getValue();
                if (importanceDegreeSet.contains(value)) {
                    exceptionEventCount++;
                }
            }
        }
        waterSupplyDrainageStatisticsVO.setClaasCodes(claasCodes);
        waterSupplyDrainageStatisticsVO.setExceptionEventCount(exceptionEventCount);

        return waterSupplyDrainageStatisticsVO;
    }

    @Override
    public SecurityStatisticsVO parkingLotStatistics(String projectId) {
        //查询对应对象数据
        InfoCodeConfigObjectByFiltersQueryParam param = new InfoCodeConfigObjectByFiltersQueryParam();
        param.setRunningMonitorType(RunningMonitorTypeEnum.PARKING);
        param.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.PARKING);
        param.setTemplateType(InfoCodeTemplateTypeEnum.DETAIL);
        param.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        Set<String> classCodes = new HashSet<>();
        classCodes.addAll(ParkingService.PARKING_EQUIPMENT_CLASS_VALUE);
        param.setClassCodes(classCodes);
        param.setFilterBothNotExistIotInfoCodeRule(true);
        param.setNotFilterIotInfoCodeRule(true);
        //param.setFilterControlObject(true);
        Set<String> groupCodes = new HashSet<>();
        groupCodes.add("equipStatus");
        groupCodes.add("riskEvent");
        param.setGroupCodes(groupCodes);
        List<InfoCodeObjectGroupVO> infoCodeObjectGroupVOS = infoCodeConfigService.listInfoCodeConfigsByObjectFilters(param);
        SecurityStatisticsVO securityStatisticsVO = new SecurityStatisticsVO();
        if (CollectionUtils.isEmpty(infoCodeObjectGroupVOS)) {
            return securityStatisticsVO;
        }
        //进行统计
        //数据打平
        List<InfoCodeObjectVO> infoCodeObjectVOs = new ArrayList<>();
        for (InfoCodeObjectGroupVO infoCodeObjectGroupVO : infoCodeObjectGroupVOS) {
            infoCodeObjectVOs.add(InfoCodeObjectVO.of(infoCodeObjectGroupVO));
        }

        Map<String, List<InfoCodeObjectVO>> claasCodeInfoCodeObjectVOMap = new HashMap<>();
        for (InfoCodeObjectVO infoCodeObjectVO : infoCodeObjectVOs) {
            String classCode = infoCodeObjectVO.getClassCode();
            List<InfoCodeObjectVO> infoCodeObjectVOClassCodeList = claasCodeInfoCodeObjectVOMap.get(classCode);
            if (CollectionUtils.isEmpty(infoCodeObjectVOClassCodeList)) {
                infoCodeObjectVOClassCodeList = new ArrayList<>();
                infoCodeObjectVOClassCodeList.add(infoCodeObjectVO);
                claasCodeInfoCodeObjectVOMap.put(classCode, infoCodeObjectVOClassCodeList);
            } else {
                infoCodeObjectVOClassCodeList.add(infoCodeObjectVO);
                claasCodeInfoCodeObjectVOMap.put(classCode, infoCodeObjectVOClassCodeList);
            }
        }

        List<ClaasCodeStatistics> claasCodeStatisticsList = new ArrayList<>();
        List<InfoCodeStatistics> infoCodeStatisticsAll = new ArrayList<>();
        for (Map.Entry<String, List<InfoCodeObjectVO>> classCodeKey : claasCodeInfoCodeObjectVOMap.entrySet()) {
            List<InfoCodeObjectVO> infoCodeObjectVOClassCodeLists = claasCodeInfoCodeObjectVOMap.get(classCodeKey.getKey());
            List<InfoCodeStatistics> infoCodeStatistics = InfoCodeStatistics.of(infoCodeObjectVOClassCodeLists);
            infoCodeStatisticsAll.addAll(infoCodeStatistics);
            ClaasCodeStatistics claasCodeSecurityStatistics = new ClaasCodeStatistics();
            claasCodeSecurityStatistics.setClassCode(classCodeKey.getKey());
            claasCodeSecurityStatistics.setClassCodeName(DtpRwdClassCodeEnum.analyze(classCodeKey.getKey()).getReasonPhrase());
            claasCodeSecurityStatistics.setInfoCodes(infoCodeStatistics);
            claasCodeStatisticsList.add(claasCodeSecurityStatistics);
        }
        Set<String> infoCodeSet = new HashSet<>();
        infoCodeSet.add("onlineStatus");
        infoCodeSet.add("alarmstatus");
        infoCodeSet.add("alarmeventdetail");
        Map<String, List<DataSourceStatistic>> dataSourceStatisticMap = countInfoCodeAll(infoCodeSet, infoCodeStatisticsAll);
        securityStatisticsVO.setClaasCodes(claasCodeStatisticsList);
        List<InfoCodeAllCount> infoCodeAllCounts = new ArrayList<>();
        for (Map.Entry<String, List<DataSourceStatistic>> dataSourceStatistic : dataSourceStatisticMap.entrySet()) {
            InfoCodeAllCount infoCodeAllCount = new InfoCodeAllCount();
            infoCodeAllCount.setInfoCode(dataSourceStatistic.getKey());
            infoCodeAllCount.setDataSource(dataSourceStatistic.getValue());
            infoCodeAllCounts.add(infoCodeAllCount);
        }
        securityStatisticsVO.setInfoCodeAllCounts(infoCodeAllCounts);
        return securityStatisticsVO;

    }

    @Override
    public Object improveAirCirculationStatistics(String projectId) {

        return null;
    }

    @Override
    public SecurityStatisticsVO securityProtectionStatistics(String projectId) {
        //查询对应对象数据
        InfoCodeConfigObjectByFiltersQueryParam param = new InfoCodeConfigObjectByFiltersQueryParam();
        param.setRunningMonitorType(RunningMonitorTypeEnum.SECURITY);
        param.setTemplateType(InfoCodeTemplateTypeEnum.DETAIL);
        param.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        Set<String> classCodes = new HashSet<>();
        classCodes.addAll(SecurityService.SECURITY_EQUIPMENT_CLASS_VALUE);
        param.setClassCodes(classCodes);
        param.setFilterBothNotExistIotInfoCodeRule(true);
        param.setNotFilterIotInfoCodeRule(true);
        // param.setFilterControlObject(true);
        Set<String> groupCodes = new HashSet<>();
        groupCodes.add("equipStatus");
        groupCodes.add("riskEvent");
        param.setGroupCodes(groupCodes);
        List<InfoCodeObjectGroupVO> infoCodeObjectGroupVOS = infoCodeConfigService.listInfoCodeConfigsByObjectFilters(param);
        SecurityStatisticsVO securityStatisticsVO = new SecurityStatisticsVO();
        if (CollectionUtils.isEmpty(infoCodeObjectGroupVOS)) {
            return securityStatisticsVO;
        }
        //进行统计
        List<InfoCodeObjectVO> infoCodeObjectVOs = new ArrayList<>();
        for (InfoCodeObjectGroupVO infoCodeObjectGroupVO : infoCodeObjectGroupVOS) {
            infoCodeObjectVOs.add(InfoCodeObjectVO.of(infoCodeObjectGroupVO));
        }

        Map<String, List<InfoCodeObjectVO>> claasCodeInfoCodeObjectVOMap = new HashMap<>();

        for (InfoCodeObjectVO infoCodeObjectVO : infoCodeObjectVOs) {
            String classCode = infoCodeObjectVO.getClassCode();
            List<InfoCodeObjectVO> infoCodeObjectVOClassCodeList = claasCodeInfoCodeObjectVOMap.get(classCode);
            if (CollectionUtils.isEmpty(infoCodeObjectVOClassCodeList)) {
                infoCodeObjectVOClassCodeList = new ArrayList<>();
                infoCodeObjectVOClassCodeList.add(infoCodeObjectVO);
                claasCodeInfoCodeObjectVOMap.put(classCode, infoCodeObjectVOClassCodeList);
            } else {
                infoCodeObjectVOClassCodeList.add(infoCodeObjectVO);
                claasCodeInfoCodeObjectVOMap.put(classCode, infoCodeObjectVOClassCodeList);
            }
        }

        List<ClaasCodeStatistics> claasCodeStatisticsList = new ArrayList<>();
        List<InfoCodeStatistics> infoCodeStatisticsAll = new ArrayList<>();
        for (Map.Entry<String, List<InfoCodeObjectVO>> classCodeKey : claasCodeInfoCodeObjectVOMap.entrySet()) {
            List<InfoCodeObjectVO> infoCodeObjectVOClassCodeLists = claasCodeInfoCodeObjectVOMap.get(classCodeKey.getKey());
            List<InfoCodeStatistics> infoCodeStatistics = InfoCodeStatistics.of(infoCodeObjectVOClassCodeLists);
            infoCodeStatisticsAll.addAll(infoCodeStatistics);
            ClaasCodeStatistics claasCodeSecurityStatistics = new ClaasCodeStatistics();
            claasCodeSecurityStatistics.setClassCode(classCodeKey.getKey());
            claasCodeSecurityStatistics.setClassCodeName(DtpRwdClassCodeEnum.analyze(classCodeKey.getKey()).getReasonPhrase());
            claasCodeSecurityStatistics.setInfoCodes(infoCodeStatistics);
            claasCodeStatisticsList.add(claasCodeSecurityStatistics);
        }
        securityStatisticsVO.setClaasCodes(claasCodeStatisticsList);
        Set<String> infoCodeSet = new HashSet<>();
        infoCodeSet.add("onlineStatus");
        infoCodeSet.add("alarmstatus");
        infoCodeSet.add("orderFailAlarm");
        infoCodeSet.add("alarmeventdetail");
        Map<String, List<DataSourceStatistic>> dataSourceStatisticMap = countInfoCodeAll(infoCodeSet, infoCodeStatisticsAll);
        List<InfoCodeAllCount> infoCodeAllCounts = new ArrayList<>();
        for (Map.Entry<String, List<DataSourceStatistic>> dataSourceStatistic : dataSourceStatisticMap.entrySet()) {
            InfoCodeAllCount infoCodeAllCount = new InfoCodeAllCount();
            infoCodeAllCount.setInfoCode(dataSourceStatistic.getKey());
            infoCodeAllCount.setDataSource(dataSourceStatistic.getValue());
            infoCodeAllCounts.add(infoCodeAllCount);
        }
        securityStatisticsVO.setInfoCodeAllCounts(infoCodeAllCounts);
        return securityStatisticsVO;

    }

    @Override
    public ElevatorStatisticsVO elevatorStatistics(String projectId) {
        //查询对应对象数据（直梯和扶梯的）
        InfoCodeConfigObjectByFiltersQueryParam param = new InfoCodeConfigObjectByFiltersQueryParam();
        param.setRunningMonitorType(RunningMonitorTypeEnum.ELEVATOR);
        param.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.ELEVATOR);
        param.setTemplateType(InfoCodeTemplateTypeEnum.DETAIL);
        param.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        Set<String> classCodes = new HashSet<>();
        classCodes.add(DtpRwdClassCodeEnum.ELELEL.getValue());
        classCodes.add(DtpRwdClassCodeEnum.ELELES.getValue());
        param.setClassCodes(classCodes);
        param.setFilterBothNotExistIotInfoCodeRule(true);
        param.setNotFilterIotInfoCodeRule(true);
        //  param.setFilterControlObject(true);
        Set<String> groupCodes = new HashSet<>();
        groupCodes.add("relEquipment");
        groupCodes.add("machineBasicWC");
        groupCodes.add("equipStatus");
        param.setGroupCodes(groupCodes);
        List<InfoCodeObjectGroupVO> infoCodeObjectGroupVOS = infoCodeConfigService.listInfoCodeConfigsByObjectFilters(param);
        ElevatorStatisticsVO elevatorStatisticsVO = new ElevatorStatisticsVO();
        if (CollectionUtils.isEmpty(infoCodeObjectGroupVOS)) {
            return elevatorStatisticsVO;
        }
        //进行统计
        List<InfoCodeObjectVO> infoCodeObjectVOs = new ArrayList<>();
        for (InfoCodeObjectGroupVO infoCodeObjectGroupVO : infoCodeObjectGroupVOS) {
            infoCodeObjectVOs.add(InfoCodeObjectVO.of(infoCodeObjectGroupVO));
        }

        Map<String, Map<String, List<InfoCodeObjectVO>>> claasCodeFuncTypeInfoCodeObjectVOMap = new HashMap<>();
        for (InfoCodeObjectVO infoCodeObjectVO : infoCodeObjectVOs) {
            String classCode = infoCodeObjectVO.getClassCode();
            Map<String, List<InfoCodeObjectVO>> funcTypeInfoCodeObjectVOMap = claasCodeFuncTypeInfoCodeObjectVOMap.get(classCode);
            if (funcTypeInfoCodeObjectVOMap == null) {
                funcTypeInfoCodeObjectVOMap = new HashMap<>();
                String funcType = (String) infoCodeObjectVO.getUndefinedProperties().get("funcType");
                funcTypeInfoCodeObjectVOMap.put(funcType, Lists.newArrayList(infoCodeObjectVO));
                claasCodeFuncTypeInfoCodeObjectVOMap.put(classCode, funcTypeInfoCodeObjectVOMap);
            } else {
                String funcType = (String) infoCodeObjectVO.getUndefinedProperties().get("funcType");
                List<InfoCodeObjectVO> infoCodeObjectVOS = funcTypeInfoCodeObjectVOMap.get(funcType);
                if (CollectionUtils.isEmpty(infoCodeObjectVOS)) {
                    funcTypeInfoCodeObjectVOMap.put(funcType, Lists.newArrayList(infoCodeObjectVO));
                    claasCodeFuncTypeInfoCodeObjectVOMap.put(classCode, funcTypeInfoCodeObjectVOMap);
                } else {
                    infoCodeObjectVOS.add(infoCodeObjectVO);
                    funcTypeInfoCodeObjectVOMap.put(funcType, infoCodeObjectVOS);
                    claasCodeFuncTypeInfoCodeObjectVOMap.put(classCode, funcTypeInfoCodeObjectVOMap);
                }
            }
        }
        List<InfoCodeStatistics> infoCodeStatisticsAll = new ArrayList<>();
        List<ClaasCodeFuncTypeStatistics> claasCodeStatisticsList = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<InfoCodeObjectVO>>> classCodeKey : claasCodeFuncTypeInfoCodeObjectVOMap.entrySet()) {
            Map<String, List<InfoCodeObjectVO>> funcTypeKeyMap = claasCodeFuncTypeInfoCodeObjectVOMap.get(classCodeKey.getKey());
            ClaasCodeFuncTypeStatistics claasCodeStatistics = new ClaasCodeFuncTypeStatistics();
            claasCodeStatistics.setClassCode(classCodeKey.getKey());
            claasCodeStatistics.setClassCodeName(DtpRwdClassCodeEnum.analyze(classCodeKey.getKey()).getReasonPhrase());
            List<FuncTypeObjects> funcTypeObjectsList = new ArrayList<>();
            for (Map.Entry<String, List<InfoCodeObjectVO>> funcTypeKey : funcTypeKeyMap.entrySet()) {
                Map<String, String> funcTypeMap = new HashMap<>();
                FuncTypeObjects funcTypeObjects = new FuncTypeObjects();
                String funcTypeKeyKey = funcTypeKey.getKey();
                if (StringUtils.isEmpty(funcTypeKeyKey)) {
                    //2024.10.25 根据产品吴亮得指示如果此值因为交付为空，则不返回
                    continue;
                }
                funcTypeObjects.setFuncType(funcTypeKeyKey);

                List<InfoCodeObjectVO> infoCodeObjectVOS = funcTypeKeyMap.get(funcTypeKeyKey);
                List<InfoCodeStatistics> infoCodeStatistics = InfoCodeStatistics.of(infoCodeObjectVOS);
                infoCodeStatisticsAll.addAll(infoCodeStatistics);
                if (MapUtils.isEmpty(funcTypeMap)) {
                    for (InfoCodeObjectVO infoCodeObjectVO : infoCodeObjectVOS) {
                        for (InfoCodeConfigGroupInformationVO infoCodeConfigGroupVO : infoCodeObjectVO.getInfoCodeConfigGroupVOs()) {
                            if (infoCodeConfigGroupVO.getInfoCode().equals("funcType")) {
                                for (InfoCodeDataSourceDefineModel infoCodeDataSourceDefineModel : infoCodeConfigGroupVO.getDataSource()) {
                                    funcTypeMap.put(infoCodeDataSourceDefineModel.getCode(), infoCodeDataSourceDefineModel.getName());
                                }
                            }
                        }
                    }
                }
                funcTypeObjects.setFuncTypeName(funcTypeMap.get(funcTypeKeyKey));
                funcTypeObjects.setInfoCodes(infoCodeStatistics);
                funcTypeObjectsList.add(funcTypeObjects);
                relEquipmentCount(claasCodeStatistics, infoCodeObjectVOS);
            }
            claasCodeStatistics.setFuncTypeObjects(funcTypeObjectsList);
            claasCodeStatisticsList.add(claasCodeStatistics);
        }
        Set<String> infoCodeSet = new HashSet<>();
        infoCodeSet.add("alarmstatus");
        Map<String, List<DataSourceStatistic>> dataSourceStatisticMap = countInfoCodeAll(infoCodeSet, infoCodeStatisticsAll);

        InfoCodeAllCount infoCodeAllCount = new InfoCodeAllCount();
        infoCodeAllCount.setInfoCode("alarmstatus");
        infoCodeAllCount.setDataSource(dataSourceStatisticMap.get("alarmstatus"));
        elevatorStatisticsVO.setInfoCodeAllCounts(Lists.newArrayList(infoCodeAllCount));
        elevatorStatisticsVO.setClaasCodes(claasCodeStatisticsList);
        return elevatorStatisticsVO;
    }


    /**
     * @return com.persagy.apccontrol.basic.model.vo.outside.energy.ClaasCodeStatistics
     * @Author 薛涛
     * @Description 计算关联设备的数量
     * @Date 19:59 2024/9/3
     * @Param [infoCodeObjectVOS]
     **/
    private void relEquipmentCount(ClaasCodeFuncTypeStatistics claasCodeStatistics, List<InfoCodeObjectVO> infoCodeObjectVOS) {
        for (InfoCodeObjectVO infoCodeObjectVO : infoCodeObjectVOS) {
            List<InfoCodeConfigGroupInformationVO> infoCodeConfigGroupVOs = infoCodeObjectVO.getInfoCodeConfigGroupVOs();
            if (CollectionUtils.isEmpty(infoCodeConfigGroupVOs)) {
                continue;
            }
            for (InfoCodeConfigGroupInformationVO infoCodeConfigGroupVO : infoCodeConfigGroupVOs) {
                String infoCode = infoCodeConfigGroupVO.getInfoCode();
                if (infoCode.equals("relEquipment")) {
                    List<InfoCodeRwdObjectDetailDTO> infoCodeRwdObjectDetailDTOs = (List<InfoCodeRwdObjectDetailDTO>) infoCodeConfigGroupVO.getDataValue();
                    if (CollectionUtils.isEmpty(infoCodeRwdObjectDetailDTOs)) {
                        return;
                    }
                    //进行统计
                    List<InfoCodeObjectVO> infoCodeObjectChildrenVOs = new ArrayList<>();
                    for (InfoCodeRwdObjectDetailDTO infoCodeRwdObjectDetailDTO : infoCodeRwdObjectDetailDTOs) {
                        List<InfoCodeGroupVO> infoCodeObjectGroupVOS = infoCodeRwdObjectDetailDTO.getRelInfoCodeGroupConfigs();
                        InfoCodeObjectGroupVO infoCodeObjectGroupVO = new InfoCodeObjectGroupVO();
                        infoCodeObjectGroupVO.setRelInfoCodeGroupConfigs(infoCodeObjectGroupVOS);
                        infoCodeObjectChildrenVOs.add(InfoCodeObjectVO.of(infoCodeObjectGroupVO));
                    }
                    List<InfoCodeStatistics> infoCodeStatistics = InfoCodeStatistics.of(infoCodeObjectChildrenVOs);
                    if (CollectionUtils.isEmpty(infoCodeStatistics)) {
                        continue;
                    }
                    for (InfoCodeStatistics infoCodeStatistic : infoCodeStatistics) {
                        infoCode = infoCodeStatistic.getInfoCode();
                        if (infoCode.equals(InfoCodeEnum.ONLINE_STATUS.getCode())) {
                            for (DataSourceStatistic dataSourceStatistic : infoCodeStatistic.getDataSource()) {
                                if (dataSourceStatistic.getCode().equals("1")) {
                                    claasCodeStatistics.setCameraRunStatusCount(claasCodeStatistics.getCameraRunStatusCount() + dataSourceStatistic.getCodeCount());
                                }
                            }
                            claasCodeStatistics.setCameraAllCount(claasCodeStatistics.getCameraAllCount() + infoCodeStatistic.getInfoCodeObjectCount());
                        }
                    }
                }
            }
        }
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.Integer>
     * @Author 薛涛
     * @Description 统计某个具体信息点得具体总数
     * @Date 11:35 2024/9/5
     * @Param [infoCodeSet, infoCodeStatistics]
     **/
    public static Map<String, List<DataSourceStatistic>> countInfoCodeAll(Set<String> infoCodeSet, List<InfoCodeStatistics> infoCodeStatistics) {
        if (CollectionUtils.isEmpty(infoCodeSet) || CollectionUtils.isEmpty(infoCodeStatistics)) {
            return new HashMap<>();
        }
        Map<String, List<DataSourceStatistic>> dataSourceStatisticMap = new HashMap<>();
        for (InfoCodeStatistics infoCodeStatistic : infoCodeStatistics) {
            String infoCode = infoCodeStatistic.getInfoCode();
            if (infoCodeSet.contains(infoCode)) {
                List<DataSourceStatistic> dataSourceStatistics = dataSourceStatisticMap.get(infoCode);
                if (CollectionUtils.isEmpty(dataSourceStatistics)) {
                    List<DataSourceStatistic> dataSource = new ArrayList<>();
                    for (DataSourceStatistic dataSourceStatistic : infoCodeStatistic.getDataSource()) {
                        //值传递导致数据计算出错，再次做处理
                        DataSourceStatistic dataSourceStatisticNew = new DataSourceStatistic();
                        BeanUtils.copyProperties(dataSourceStatistic, dataSourceStatisticNew);
                        dataSource.add(dataSourceStatisticNew);
                    }
                    dataSourceStatisticMap.put(infoCode, dataSource);
                } else {
                    List<DataSourceStatistic> dataSourcesNew = new ArrayList<>();
                    for (DataSourceStatistic dataSourceStatistic : infoCodeStatistic.getDataSource()) {
                        //值传递导致数据计算出错，再次做处理
                        DataSourceStatistic dataSourceStatisticNew = new DataSourceStatistic();
                        BeanUtils.copyProperties(dataSourceStatistic, dataSourceStatisticNew);
                        dataSourcesNew.add(dataSourceStatisticNew);
                    }
                    Map<String, DataSourceStatistic> dataSourceStatisticsOldMap = new HashMap<>();
                    for (DataSourceStatistic dataSourceStatistic : dataSourceStatistics) {
                        String code = dataSourceStatistic.getCode();
                        dataSourceStatisticsOldMap.put(code, dataSourceStatistic);
                    }
                    for (DataSourceStatistic dataSource : dataSourcesNew) {
                        String code = dataSource.getCode();
                        DataSourceStatistic dataSourceStatistic = dataSourceStatisticsOldMap.get(code);
                        if (dataSourceStatistic == null) {
                            dataSourceStatisticsOldMap.put(code, dataSource);
                        } else {
                            Integer codeCount = dataSourceStatistic.getCodeCount();
                            Integer codeCountNew = dataSource.getCodeCount();
                            dataSourceStatistic.setCodeCount(codeCount + codeCountNew);
                            dataSourceStatisticsOldMap.put(code, dataSourceStatistic);
                        }
                    }
                    dataSourceStatisticMap.put(infoCode, Lists.newArrayList(dataSourceStatisticsOldMap.values()));
                }


            }
        }
        return dataSourceStatisticMap;

    }


}
