package com.persagy.apccontrol.basic.service.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractDtpPersistServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.EnergyDataInfoQueryParam;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.EnumTimeType;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.freezepumpgroup.DtpRwdFreezePumpGroupInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.AbnormalStatusTypeEnum;
import com.persagy.apccontrol.basic.model.param.abnormal.AbnormalEventQueryParam;
import com.persagy.apccontrol.basic.model.param.dashboard.AbnormalStateParam;
import com.persagy.apccontrol.basic.model.param.dashboard.DashboardQueryParam;
import com.persagy.apccontrol.basic.model.param.dashboard.PeriodDataBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFreezeGroupObjectQueryParam;
import com.persagy.apccontrol.basic.model.vo.CurveVO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.PeriodDataBatchVO;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.feign.client.DtpRwdClient;
import com.persagy.apccontrol.basic.outside.feign.client.EnergyDdpServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdRelationResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizStrategyQueryService;
import com.persagy.apccontrol.basic.service.DashBoardService;
import com.persagy.apccontrol.basic.service.abnormal.AbnormalEventService;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.apccontrol.basic.utils.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @BelongsProject: apc-control-parent
 * @BelongsPackage: com.persagy.apccontrol.service.impl
 * @Author: laixingqi
 * @CreateTime: 2023-10-11  10:13
 * @Description:
 * @Version: v5.7.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DashBoardServiceImpl implements DashBoardService {

    private final DtpRwdClient dtpRwdClient;
    private final DtpRwdClientService dtpRwdClientService;
    private final EnergyDdpServerClient energyDdpServerClient;
    private final AbstractCommonServer abstractCommonServer;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final AbstractDtpPersistServer abstractDtpPersistServer;
    private final BizStrategyQueryService bizStrategyQueryService;
    private final AbnormalEventService abnormalEventService;
    private final NacosSwitchConfig nacosSwitchConfig;

    /**
     * @Description: 系统运行效率
     * @Author: laixingqi
     * @Date: 2023/10/11 10:17
     * @param: [param]
     * @return: java.util.List<com.persagy.apccontrol.controller.vo.GaugeVO>
     **/
    @Override
    public JSONArray getSystemEfficiency(DashboardQueryParam param) {

        //获取最新的配置，包括等级名称、等级对应的区间值、信息点最大值最小值（后评估接口）
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("projectId", param.getProjectId());

        //查询后评估报告的最近一条配置
        String listCustomerResponse = energyDdpServerClient.queryByProjectId(ContextUtil.getGroupCode(), jsonObject);

        JSONArray energyDdpList = ((JSONObject) JSONObject.parse(listCustomerResponse)).getJSONArray("Content");

        JSONArray efficiencyConfigList = null;

        if (CollectionUtils.isNotEmpty(energyDdpList)) {
            //解析配置内容，获取后评估配置范围
            List<JSONObject> listConfig = JSONObject.parseArray(energyDdpList.toJSONString(), JSONObject.class);
            JSONObject configContent = listConfig.stream().filter(s -> s.getString("systemId").equals(param.getSystemId())).collect(Collectors.toList()).get(0);
            //从后评估报告配置中，获取能效指标评价标准的配置范围
            efficiencyConfigList = configContent.getJSONArray("efficiencyConfigList");
        }

        //查询当前系统最新的孪生值
        List<String> listObjectIds = new ArrayList<>();
        listObjectIds.add(param.getSystemId());

        //获取查询的信息点
        List<String> listInfoCodes = DashBoardInfoCodeEnum.getInfoCodeList("1");

        Set<String> setClassCode = new HashSet<>();
        setClassCode.add(param.getSystemCode());

        //查询是否包含规则
        Map<String, Set<String>> queryObjectsOfExist = this.queryObjectsOfExist(param.getProjectId(), setClassCode,
                listInfoCodes);
        if (queryObjectsOfExist.isEmpty() || !queryObjectsOfExist.containsKey(param.getSystemId())) {
            return new JSONArray();
        }

        JSONArray resutls = new JSONArray();
        Set<String> infoCodes = queryObjectsOfExist.get(param.getSystemId());
        if (CollectionUtils.isNotEmpty(efficiencyConfigList)) {
            //组装值到配置中，返回到前端
            for (Object efficiencyConfig : efficiencyConfigList) {
                JSONObject efficiency = (JSONObject) efficiencyConfig;
                String efficiencyCode = efficiency.getString("efficiencyCode");
                //target是后评估返回的指标值 不消费单独查询
                efficiency.put("target", null);
                DashBoardInfoCodeEnum infoCodeEnum = DashBoardInfoCodeEnum.getLocalCode(efficiencyCode);
                if (infoCodeEnum == null || !infoCodes.contains(infoCodeEnum.getCode())) {
                    continue;
                }
                List<EnergyDataResponse> dataVal =
                        dtpRwdClientService.queryIpDataOnMoment(Collections.singleton(param.getSystemId()),
                                Collections.singleton(infoCodeEnum.getCode()),
                                DtpRwdPersistenceTimeTypeEnum.analyze(nacosSwitchConfig.getBdtpTimeTypeEfficiency()),
                                LocalDateTime.of(com.persagy.apccontrol.basic.utils.dateUtil.DateUtils.now().toLocalDate(), LocalTime.MIDNIGHT), null);
                if (CollectionUtil.isNotEmpty(dataVal)) {
                    efficiency.put("target", String.valueOf(dataVal.get(0).getIpValue()));
                }
                efficiency.put("efficiencyName", infoCodeEnum.getName());
                resutls.add(efficiency);
            }
        }
        return resutls;
    }


    /**
     * @Description: 冷机运行
     * @Author: laixingqi
     * @Date: 2023/10/11 10:19
     * @param: [param]
     * @return: java.util.List<com.persagy.apccontrol.controller.vo.CurveVO>
     **/
    @Override
    public List<CurveVO> getChillerLoad(DashboardQueryParam param) throws Exception {


        List list = new ArrayList<String>();
        list.add(param.getSystemCode());

        DtpRwdRelationQueryParam dmpRwdRelationQuery = new DtpRwdRelationQueryParam();
        DtpRwdRelationTypeEnum instanceRelationType = DtpRwdRelationTypeEnum.EQ_IN_SYSTEM;
        dmpRwdRelationQuery.setGraphCode(instanceRelationType.value().getLeft());
        dmpRwdRelationQuery.setRelCode(instanceRelationType.value().getRight());
        dmpRwdRelationQuery.setFromCriteria(new DtpRwdObjectQueryParam().setId(param.getSystemId()));
        dmpRwdRelationQuery.setToCriteria(new DtpRwdObjectQueryParam().setClassCodeEnum(
                Lists.newArrayList(
                        DtpRwdClassCodeEnum.ACCCCC, DtpRwdClassCodeEnum.ACCCSC,
                        DtpRwdClassCodeEnum.ACCCPC, DtpRwdClassCodeEnum.ACCCAC
                )
        ));

        List<DtpRwdRelationResponse> dtpRwdRelationRespons = dtpRwdClientService.listRelationObject(false, true,
                param.getProjectId(), dmpRwdRelationQuery);

        ControlLogUtil.info("冷机运行-getChillerLoad:{}", dtpRwdRelationRespons);

        if (CollectionUtils.isEmpty(dtpRwdRelationRespons)) {
            return new ArrayList<>();
        }

        Set<String> eqIds =
                dtpRwdRelationRespons.stream().map(DtpRwdRelationResponse::getToId).collect(Collectors.toSet());

        Set<String> classCodes =
                dtpRwdRelationRespons.stream().map(DtpRwdRelationResponse::getToClassCode).collect(Collectors.toSet());

        //获取设备名称
        Map<String, String> objects =
                dtpRwdRelationRespons.stream().collect(Collectors.toMap(DtpRwdRelationResponse::getToId,
                        o -> Optional.ofNullable(o.getToLocalName()).orElse(""), (key1, key2) -> key2));

        // 从0点-当前时间,组装时间轴
        List<Date> resultDateList = DateUtils.get15MinOrHourDateList(null, null, false, 15);

        //组装查询分精度数据参数
        List<String> listObjectIds = new ArrayList<>();
        listObjectIds.addAll(eqIds);

        //获取查询的信息点
        List<String> listInfoCodes = DashBoardInfoCodeEnum.getInfoCodeList("2");

        //在根据设备和信息点查询分精度,获取历史分精度,参数：系统id   信息点：iPLR ,查询分精度数据
        List<EnergyDataResponse> listEnergyData = this.batchQueryPeriodData(param.getProjectId(), listObjectIds,
                listInfoCodes, EnumTimeType.MINUTE15, null, null);

        Map<String, String> objectSyIds = new HashMap<>();
        objectSyIds.put(param.getSystemId(), LocaleMessageUtil.getMessage("key_4fa8b6e7a9ab493d93d738cbc6333452"));

        List<CurveVO> results = getCurveList(resultDateList, listEnergyData, objects, objectSyIds, listInfoCodes,
                param.getSystemId(), classCodes, param.getProjectId(), "object", null, null);

        return results;
    }

    /**
     * @Description: 冷却侧温度分析
     * @Author: laixingqi
     * @Date: 2023/10/11 11:06
     * @param: [param]
     * @return: java.util.List<com.persagy.apccontrol.controller.vo.CurveVO>
     **/
    @Override
    public List<CurveVO> getChillingTemp(DashboardQueryParam param) throws Exception {

        //从0点-当前时间，组装时间轴
        List<Date> resultDateList = DateUtils.get15MinOrHourDateList(null, null, false, 15);

        //组装查询分精度数据参数
        List<String> listObjectIds = new ArrayList<>();
        listObjectIds.add(param.getSystemId());

        //获取查询的信息点
        List<String> listInfoCodes = DashBoardInfoCodeEnum.getInfoCodeList("3");

        //测试使用
//        listInfoCodes.add("accCool");

        //根据系统id，和信息点：coolWaterMainReturnTemp、coolWaterReturnTempSet 查询分精度数据
        List<EnergyDataResponse> listEnergyData = this.batchQueryPeriodData(param.getProjectId(), listObjectIds,
                listInfoCodes, EnumTimeType.MINUTE15, null, null);

        Map<String, String> objects = new HashMap<>();
        objects.put(param.getSystemId(), LocaleMessageUtil.getMessage("key_66e26dcd721644ae896957f049ef9c46"));

        Set<String> setClassCode = new HashSet<>();
        setClassCode.add(param.getSystemCode());

        List<CurveVO> results = getCurveList(resultDateList, listEnergyData, objects, null, listInfoCodes,
                param.getSystemId(), setClassCode, param.getProjectId(), null, null, null);

        return results;
    }

    /**
     * @Description: 查询冷冻侧供回水温差信息
     * @Author: laixingqi
     * @Date: 2023/10/11 11:06
     * @param: [param]
     * @return: java.util.List<com.persagy.apccontrol.controller.vo.CurveVO>
     **/
    @Override
    public List<CurveVO> getFreezingTempDiff(DashboardQueryParam param) throws Exception {

        //  从0点-当前时间 ， 组装时间轴
        List<Date> resultDateList = DateUtils.get15MinOrHourDateList(null, null, false, 15);

        //组装查询分精度数据参数
        List<String> listObjectIds = new ArrayList<>();
        listObjectIds.add(param.getSystemId());

        //获取查询的信息点
        List<String> listInfoCodes = DashBoardInfoCodeEnum.getInfoCodeList("4");
        //测试使用
//        listInfoCodes.add("accCool");

        //根据系统id，和信息点：chillWaterDetaTemp、chillWaterDetaTempSet, 查询分精度数据
        List<EnergyDataResponse> listEnergyData = this.batchQueryPeriodData(param.getProjectId(), listObjectIds,
                listInfoCodes, EnumTimeType.MINUTE15, null, null);

        Map<String, String> objects = new HashMap<>();
        objects.put(param.getSystemId(), LocaleMessageUtil.getMessage("key_153991b6baf943a2b8d739028e00ace7"));

        Set<String> setClassCode = new HashSet<>();
        setClassCode.add(param.getSystemCode());

        List<CurveVO> results = getCurveList(resultDateList, listEnergyData, objects, null, listInfoCodes,
                param.getSystemId(), setClassCode, param.getProjectId(), null, null, null);

        return results;
    }

    /**
     * @Description: 查询冷冻侧供水温度信息
     * @Author: laixingqi
     * @Date: 2023/10/11 11:09
     * @param: [param]
     * @return: java.util.List<com.persagy.apccontrol.controller.vo.CurveVO>
     **/
    @Override
    public List<CurveVO> getFreezingTemp(DashboardQueryParam param) throws Exception {

        // 从0点-当前时间 ， 组装时间轴
        List<Date> resultDateList = DateUtils.get15MinOrHourDateList(null, null, false, 15);

        //组装查询分精度数据参数
        List<String> listObjectIds = new ArrayList<>();
        listObjectIds.add(param.getSystemId());

        //获取查询的信息点
        List<String> listInfoCodes = DashBoardInfoCodeEnum.getInfoCodeList("5");

        //测试使用
//        listInfoCodes.add("accCool");

        //根据系统id，和信息点：primaryChillWaterMainSupplyTemp、chillWaterOutTempSet ，查询分精度数据
        List<EnergyDataResponse> listEnergyData = this.batchQueryPeriodData(param.getProjectId(), listObjectIds,
                listInfoCodes, EnumTimeType.MINUTE15, null, null);

        Map<String, String> objects = new HashMap<>();
        objects.put(param.getSystemId(), LocaleMessageUtil.getMessage("key_b6c5e1468de94dd8b22cd78a15f4340f"));

        Set<String> setClassCode = new HashSet<>();
        setClassCode.add(param.getSystemCode());

        List<CurveVO> results = getCurveList(resultDateList, listEnergyData, objects, null, listInfoCodes,
                param.getSystemId(), setClassCode, param.getProjectId(), null, null, null);

        return results;

    }

    /**
     * @Description: 查询冷冻二次侧信息
     * @Author: laixingqi
     * @Date: 2023/10/11 11:10
     * @param: [param]
     * @return: java.util.List<java.util.List < com.persagy.apccontrol.controller.vo.CurveVO>>
     **/
    @Override
    public List<CurveVO> getFreezingSecondary(DashboardQueryParam param) throws Exception {

        log.info("查询冷冻二次侧信息");
        //根据系统id，组装查询二次侧的所有设备参数
        StrategyFreezeGroupObjectQueryParam strategy = new StrategyFreezeGroupObjectQueryParam();
        strategy.setProjectId(param.getProjectId());
        strategy.setSystemIds(Lists.newArrayList(param.getSystemId()));

        //根据系统id，查询二次侧的所有设备
        List<DtpRwdFreezePumpDTO> objectList = bizStrategyQueryService.listLastFreezePumpGroups(strategy);
        if (CollectionUtils.isEmpty(objectList)) {
            return null;
        }

        //获取设备名称
        Map<String, String> objects = objectList.stream().collect(HashMap::new,
                (map, item) -> map.put(item.getId(), item.getLocalName()),
                HashMap::putAll
        );

        // 从0点-当前时间 ， 组装时间轴
        List<Date> resultDateList = DateUtils.get15MinOrHourDateList(null, null, false, 15);

        //classCode
        Set<String> classCodes =
                objectList.stream().filter(this::freezePumpType)
                        .map(DtpRwdFreezePumpDTO::getClassCode).collect(Collectors.toSet());

        //温差id
        Set<String> eqTempIds =
                objectList.stream().filter(this::freezePumpType)
                        .map(DtpRwdFreezePumpDTO::getId).collect(Collectors.toSet());
        log.info("查询冷冻二次侧信息，设备id" + eqTempIds);
        List<String> listObjectIds = new ArrayList<>(eqTempIds);

        //获取查询的信息点
        List<String> listInfoCodes = DashBoardInfoCodeEnum.getInfoCodeList("6");

        List<EnergyDataResponse> listEnergyDataAll = new ArrayList<>();
        //根据设备id，和信息点 ，查询分精度数据
        List<EnergyDataResponse> listEnergyData = this.batchQueryPeriodData(param.getProjectId(), listObjectIds,
                listInfoCodes, EnumTimeType.MINUTE15, null, null);
        log.info("查询冷冻二次侧信息,温差分精度信息" + listEnergyData);

        if (CollectionUtils.isNotEmpty(listEnergyData)) {
            listEnergyDataAll.addAll(listEnergyData);
        }

        //压差id
        Set<String> eqPressIds =
                objectList.stream().filter(dr -> !freezePumpType(dr))
                        .map(DtpRwdFreezePumpDTO::getId).collect(Collectors.toSet());
        List<String> listObjectIdTwos = new ArrayList<>(eqPressIds);
        //获取查询的信息点
        List<String> listInfoCodeTwos = DashBoardInfoCodeEnum.getInfoCodeList("7");

        //根据设备id，和信息点 ，查询分精度数据
        List<EnergyDataResponse> listEnergyPressData = this.batchQueryPeriodData(param.getProjectId(), listObjectIdTwos,
                listInfoCodeTwos, EnumTimeType.MINUTE15, null, null);

        log.info("查询冷冻二次侧信息,压差分精度信息" + listEnergyPressData);

        if (CollectionUtils.isNotEmpty(listEnergyPressData)) {
            listEnergyDataAll.addAll(listEnergyData);
        }

//        listInfoCodeTwos.addAll(listInfoCodes);

        List<CurveVO> results = getCurveList(resultDateList, listEnergyDataAll, objects, null, listInfoCodes,
                param.getSystemId(), classCodes, param.getProjectId(), null, eqTempIds, eqPressIds);
        log.info("查询冷冻二次侧信息，results" + results);
        return results;
    }

    @Override
    public List<PeriodDataBatchVO> batchQueryPeriodData(PeriodDataBatchQueryParam param) {
        List<EnergyDataResponse> listEnergyData = new ArrayList<>();
        List<PeriodDataBatchVO> result = new ArrayList<>();
        //获取设备名称
        DtpRwdObjectQueryParam p = new DtpRwdObjectQueryParam().setId(param.getObjectIds());
        List<DtpRwdObjectDTO> objectDtoList = dtpRwdClientService.listObject(true, true, ContextUtil.getProjectId(), p);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(objectDtoList)) {
            return Lists.newArrayList();
        }
        Map<String, String> objects = objectDtoList.stream()
                .collect(HashMap::new,
                        (map, item) -> map.put(item.getId(), item.getLocalName()),
                        HashMap::putAll
                );
        //根据设备id，和信息点 ，查询分精度数据
        try {
            Date beginDate = new Date(Timestamp.valueOf(param.getBeginDate()).getTime());
            Date endDate = new Date(Timestamp.valueOf(param.getEndDate()).getTime());
            listEnergyData = this.batchQueryPeriodData(ContextUtil.getProjectId(), param.getObjectIds(),
                    param.getInfoCodes(), EnumTimeType.getCompareType(param.getEnumTime()), beginDate, endDate);
        } catch (Exception e) {
            log.error("查询历史分精度异常", e);
        }

        if (CollectionUtils.isEmpty(listEnergyData)) {
            return result;
        }
        // 按照objectId进行分组
        Map<String, List<EnergyDataResponse>> groupedByObjectId = listEnergyData.stream().collect(Collectors.groupingBy(EnergyDataResponse::getObjectId));

        for (Map.Entry<String, List<EnergyDataResponse>> entry : groupedByObjectId.entrySet()) {
            PeriodDataBatchVO periodDataBatchVO = new PeriodDataBatchVO();
            periodDataBatchVO.setObjectId(entry.getKey());

            List<PeriodDataBatchVO.CurveInfoCodeVO> curveInfoCodeVOS = new ArrayList<>();

            // 按照infoCode对每个objectId下的数据再次分组
            Map<String, List<EnergyDataResponse>> groupedByInfoCode = entry.getValue().stream().collect(Collectors.groupingBy(EnergyDataResponse::getInfoCode));

            for (Map.Entry<String, List<EnergyDataResponse>> infoCodeEntry : groupedByInfoCode.entrySet()) {
                curveInfoCodeVOS.add(createCurveInfoCodeVO(infoCodeEntry.getValue()));
            }
            periodDataBatchVO.setData(curveInfoCodeVOS);
            periodDataBatchVO.setObjectName(objects.get(entry.getKey()));
            result.add(periodDataBatchVO);
        }
        return result;
    }

    private PeriodDataBatchVO.CurveInfoCodeVO createCurveInfoCodeVO(List<EnergyDataResponse> energyDataResponses) {
        PeriodDataBatchVO.CurveInfoCodeVO curveInfoCodeVO = new PeriodDataBatchVO.CurveInfoCodeVO();
        curveInfoCodeVO.setInfoCode(energyDataResponses.get(0).getInfoCode());

        List<PeriodDataBatchVO.DataVO> dataList = energyDataResponses.stream().map(this::createDataVO).collect(Collectors.toList());
        curveInfoCodeVO.setData(dataList);
        return curveInfoCodeVO;
    }

    private PeriodDataBatchVO.DataVO createDataVO(EnergyDataResponse date) {
        PeriodDataBatchVO.DataVO data = new PeriodDataBatchVO.DataVO();
        data.setIpValue(date.getIpValue());
        data.setTime(date.getIpTime());
        return data;
    }


    /**
     * @Description: 判断返回是否是温差
     * @Author: laixingqi
     * @Date: 2023/11/14 16:44
     * @param: [expandField]
     * @return: boolean
     **/
    private boolean freezePumpType(@Nullable DtpRwdFreezePumpDTO expandField) {
        if (Objects.nonNull(expandField)) {
            DtpRwdFreezePumpGroupInfoCodeEnum branchDetaType = expandField.getInfoCodeSetType();
            List<String> listInfoCodes = DashBoardInfoCodeEnum.getInfoCodeList("6");

            if (branchDetaType != null && listInfoCodes.contains(branchDetaType.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @Description: 查询历史分精度
     * @Author: laixingqi
     * @Date: 2023/10/12 19:45
     * @param: [projectId, objectIdSet, infoCodeSet, enumTime, beginDate, endDate]
     * @return: java.util.List<com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse>
     **/
    public List<EnergyDataResponse> batchQueryPeriodData(String projectId, List<String> objectIdSet,
                                                         List<String> infoCodeSet, EnumTimeType enumTime,
                                                         Date beginDate, Date endDate) throws Exception {
        if (beginDate == null && endDate == null) {
            String startTime = DateUtils.getStartTimeOfDayStrNew(com.persagy.apccontrol.basic.utils.dateUtil.DateUtils.getDateNow());
            String endTime = DateUtils.getStartTimeOfDayStrNow(com.persagy.apccontrol.basic.utils.dateUtil.DateUtils.getDateNow());

            beginDate = DateUtils.str2Date(startTime, DateUtils.SDF_SECOND);
            endDate = DateUtils.str2Date(endTime, DateUtils.SDF_SECOND);
        }

        //组装查询iot数据的参数，objectId、infoCodes
        if (!CollectionUtils.isEmpty(objectIdSet) && !CollectionUtils.isEmpty(infoCodeSet)) {
            List<EnergyDataInfoQueryParam> objectList = new ArrayList<>();
            for (String objectId : objectIdSet) {
                EnergyDataInfoQueryParam energyDataInfoQueryParam = new EnergyDataInfoQueryParam();
                energyDataInfoQueryParam.setObjectId(objectId);
                energyDataInfoQueryParam.setInfoCodes(infoCodeSet);
                objectList.add(energyDataInfoQueryParam);
            }

            EnergyDataQueryParam energyDataQueryParam = new EnergyDataQueryParam();
            energyDataQueryParam.setStartTime(DateUtils.date2Str(beginDate, DateUtils.SDFSECOND));
            energyDataQueryParam.setEndTime(DateUtils.date2Str(endDate, DateUtils.SDFSECOND));
            energyDataQueryParam.setIpType(enumTime.getName());
            energyDataQueryParam.setObjects(objectList);

            //从中台查询iot数据
            CustomerResponse<List<EnergyDataResponse>> listCustomerResponse =
                    abstractDtpPersistServer.listIpDataInPeriod(ContextUtil.getGroupCode(), projectId, energyDataQueryParam);

            List<EnergyDataResponse> contentList = listCustomerResponse.getContent();
            if (CollectionUtils.isNotEmpty(contentList)) {
                return contentList;
            }
        }
        return null;
    }


    /**
     * @Description: 查询是否包含规则 都查询持久化规则
     * @Author: laixingqi
     * @Date: 2023/12/5 14:39
     * @param: [projectId, objectId, classCode, infoCodes]
     * @return: boolean
     **/
    private Map<String, Set<String>> queryObjectsOfExist(String projectId, Set<String> classCodes,
                                                         List<String> infoCodes) {

        JSONArray list = new JSONArray();

        Map<String, Set<String>> object_infoCode = new HashMap<>();
        if (CollectionUtils.isNotEmpty(classCodes)) {

            for (String classCode : classCodes) {
                JSONObject param = new JSONObject();
                param.put("classCode", classCode);
                param.put("infoCodes", infoCodes);
                list.add(param);
            }

            //从规则服务查询是否有此信息点的规则
            DmpResult<List<ObjectsExistRule>> listDmpResult =
                    dtpRwdClient.queryObjectsOfExistRule(ContextUtil.getGroupCode(), projectId, null, list);
            if (listDmpResult != null && CollectionUtils.isNotEmpty(listDmpResult.getData())) {
                object_infoCode =
                        listDmpResult.getData().stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId,
                                Collectors.mapping(ObjectsExistRule::getInfoCode, Collectors.toSet())));
            }
        }
        return object_infoCode;
    }


    /**
     * @Description: 根据时间轴，组装iot数据集合
     * @Author: laixingqi
     * @Date: 2023/10/12 19:45
     * @param: [resultDateList, listEnergyData, flag]
     * @return: java.util.List<com.persagy.apccontrol.controller.vo.CurveVO>
     **/
    @NotNull
    private List<CurveVO> getCurveList(List<Date> resultDateList, List<EnergyDataResponse> listEnergyData,
                                       Map<String, String> objects, Map<String, String> objectSyIds,
                                       List<String> listInfoCodes, String systemId, Set<String> classCodes,
                                       String projectId,
                                       String flag, Set<String> eqTempIds, Set<String> eqPressIds) throws Exception {


        //查询是否包含规则
        Map<String, Set<String>> queryObjectsOfExist = this.queryObjectsOfExist(projectId, classCodes, listInfoCodes
        );
        if (queryObjectsOfExist.isEmpty()) {
            return new ArrayList<>();
        }

        //objecId：infoCode:time：value
        Map<String, Map<String, Map<String, BigDecimal>>> object_map = new HashMap<>();
        //infoCode:time：value
        Map<String, Map<String, BigDecimal>> eq_dataMap = null;
        //时间：value
        Map<String, BigDecimal> time_dataMap;
        if (CollectionUtils.isNotEmpty(listEnergyData)) {
            for (EnergyDataResponse listEnergyDatum : listEnergyData) {

                String object_id = listEnergyDatum.getObjectId();
                String info_code = listEnergyDatum.getInfoCode();
                //如果是冷机运行，需要返回：信息点：List<设备集合>
                if ("object".equals(flag)) {
                    object_id = listEnergyDatum.getInfoCode();
                    info_code = listEnergyDatum.getObjectId();
                }

                //如果是冷机运行，结果为infoCode：objecId:time：value, 为了以下遍历使用
                eq_dataMap = object_map.get(object_id);
                if (eq_dataMap == null) {
                    eq_dataMap = new HashMap<>();
                }
                time_dataMap = eq_dataMap.get(info_code);
                String ipTime = listEnergyDatum.getIpTime();
                BigDecimal ipValue = listEnergyDatum.getIpValue();

                if (time_dataMap == null) {
                    time_dataMap = new HashMap<>();
                }
                time_dataMap.put(ipTime, ipValue);

                eq_dataMap.put(info_code, time_dataMap);

                object_map.put(object_id, eq_dataMap);
            }

            /*if (CollectionUtils.isNotEmpty(eqTempIds) || CollectionUtils.isNotEmpty(eqPressIds)) {
                for (Map.Entry<String, Map<String, Map<String, BigDecimal>>> objectMap : object_map.entrySet()) {
                    String objectId = objectMap.getKey();

                    // 供回水温差
                    Map<String, BigDecimal> stringBigDecimalMap = objectMap.getValue().get(DashBoardInfoCodeEnum.BRANCH_DETA_TEMP.getCode());
                    object_map.get(objectId).put(DashBoardInfoCodeEnum.BRANCH_SUPPLY_RETURN_TEMP.getCode(), stringBigDecimalMap == null ? new HashMap<>() : stringBigDecimalMap);

                    //供水温度
                    Map<String, BigDecimal> branchSupplyTempMap = objectMap.getValue().get(DashBoardInfoCodeEnum.BRANCH_SUPPLY_TEMP.getCode());

                    //回水温度
                    Map<String, BigDecimal> branchReturnTempMap = objectMap.getValue().get(DashBoardInfoCodeEnum.BRANCH_RETURN_TEMP.getCode());

                    //时间：value
                    Map<String, BigDecimal> time_branchSupplyReturnTempMap = new HashMap<>();

                    if (branchSupplyTempMap != null && branchReturnTempMap != null) {
                        //拼装供回水温差
                        for (Map.Entry<String, BigDecimal> map : branchSupplyTempMap.entrySet()) {
                            String time = map.getKey();
                            BigDecimal branchSupplyTemp = branchSupplyTempMap.get(time);
                            BigDecimal branchReturnTemp = branchReturnTempMap.get(time);

                            BigDecimal branchSupplyReturnTemp = null;
                            if (branchSupplyTemp != null && branchReturnTemp != null) {
                                branchSupplyReturnTemp = branchReturnTemp.subtract(branchSupplyTemp);
                            }
                            time_branchSupplyReturnTempMap.put(time, branchSupplyReturnTemp);
                        }
                        object_map.get(objectId).put(DashBoardInfoCodeEnum.BRANCH_DETA_TEMP.getCode(), time_branchSupplyReturnTempMap);
                    }
                }

            }*/

        } else {
            eq_dataMap = new HashMap<>();
            for (String infoCode : listInfoCodes) {
                eq_dataMap.put(infoCode, new HashMap<>());
                object_map.put(systemId, eq_dataMap);
            }
        }

        //组装iot数据与时间轴，返回结果
        List<CurveVO> resultObjects = new ArrayList();

        // 1、如果不是冷机运行，则正常返回： 结果为objecId：infoCode:time：value
        // 2、如果是冷机运行，需要返回：结果为infoCode：objecId:time：value
        for (Map.Entry<String, Map<String, Map<String, BigDecimal>>> objecMap : object_map.entrySet()) {
            String objectId = objecMap.getKey();
            eq_dataMap = object_map.get(objectId);

            if (!"object".equals(flag)) {
                if (CollectionUtils.isNotEmpty(eqTempIds) || CollectionUtils.isNotEmpty(eqPressIds)) {
                    if (eqTempIds.contains(objectId)) {
                        //温差
                        listInfoCodes = DashBoardInfoCodeEnum.getInfoCodeList("6");
                    } else if (eqPressIds.contains(objectId)) {
                        //压差
                        listInfoCodes = DashBoardInfoCodeEnum.getInfoCodeList("7");
                    } else {
                        listInfoCodes = new ArrayList<>();
                    }
                }

                for (String infoCode : listInfoCodes) {
                    if (!eq_dataMap.containsKey(infoCode)) {
                        eq_dataMap.put(infoCode, new HashMap<>());
                    }
                }
            }

            List<CurveVO.CurveInfoCodeVO> resultInfoCodes = new ArrayList();

            String object_id = objectId;
            for (Map.Entry<String, Map<String, BigDecimal>> infoCodeMap : eq_dataMap.entrySet()) {
                String infoCode = infoCodeMap.getKey();

                if ("object".equals(flag)) {
                    objectId = infoCode;
                    infoCode = object_id;
                }
                Set<String> infoCodes = CollectionUtils.isEmpty(queryObjectsOfExist.get(objectId))
                        ? new HashSet<>() : queryObjectsOfExist.get(objectId);

                if (!infoCodes.contains(infoCode)) {
                    continue;
                }

                Map<String, BigDecimal> value = infoCodeMap.getValue();

                List<Map<String, Object>> dataList = new ArrayList<>();
                for (Date date : resultDateList) {
                    Map<String, Object> dataMap = new HashedMap();
                    dataMap.put("time", DateUtils.date2Str(date, DateUtils.SDFSECOND));
                    dataMap.put("ipValue", value.get(DateUtils.date2Str(date, DateUtils.SDFSECOND)));
                    dataList.add(dataMap);
                }
                CurveVO.CurveInfoCodeVO curveInfoCode = new CurveVO.CurveInfoCodeVO();

                //如果是冷机运行，则返回objectId
                if ("object".equals(flag)) {
                    curveInfoCode.setObjectId(objectId);
                    curveInfoCode.setObjectName(objects.get(infoCode) == null ? objects.get(objectId) == null ?
                            LocaleMessageUtil.getMessage("key_b2240976a280416399d10fdba44bee74") : objects.get(objectId)
                            : objects.get(infoCode));
                } else {
                    curveInfoCode.setInfoCode(infoCode);
                    curveInfoCode.setInfoCodeName(DashBoardInfoCodeEnum.getName(infoCode));
                }

                curveInfoCode.setData(dataList);
                resultInfoCodes.add(curveInfoCode);
            }

            CurveVO curveVO = new CurveVO();
            curveVO.setObjectId(objectId);
            curveVO.setObjectName(objects.get(objectId));

            if ("object".equals(flag)) {
                String objectSyId = objectSyIds.keySet().iterator().next();
                curveVO.setObjectId(objectSyId);
                curveVO.setObjectName(objectSyIds.get(objectSyId));
                Collections.sort(resultInfoCodes, Comparator.comparing(CurveVO.CurveInfoCodeVO::getObjectName,
                        Comparator.nullsFirst(String::compareTo)));
            }

            curveVO.setData(resultInfoCodes);
            resultObjects.add(curveVO);
            Collections.sort(resultObjects, Comparator.comparing(CurveVO::getObjectName,
                    Comparator.nullsFirst(String::compareTo)));

        }

        return resultObjects;
    }

    /**
     * 在离线、控返不一致 信息点数组
     */
    private static final ArrayList<String> ABNORMAL_STATE_INFO_CODES =
            Lists.newArrayList(
                    InfoCodeEnum.ONLINE_STATUS.value(),
                    InfoCodeEnum.ORDER_FAIL_ALARM.value()
            );

    @Override
    public Map<String, List<AbnormalStatusTypeEnum>> listAbnormalState(AbnormalStateParam param) {
        // 空间仅需异常事件，设备需要在离线、控返不一致、异常事件
        Collection<String> equipmentIds = param.getEquipmentIds();
        Collection<String> spaceIds = param.getSpaceIds();
        if (CollectionUtils.isEmpty(spaceIds) && CollectionUtils.isEmpty(equipmentIds)) {
            return Collections.emptyMap();
        }
        ArrayList<String> spaceAndEquipmentIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(spaceIds)) {
            spaceAndEquipmentIds.addAll(spaceIds);
        }
        if (CollectionUtils.isNotEmpty(equipmentIds)) {
            spaceAndEquipmentIds.addAll(equipmentIds);
        }

        // 查询空间设备的异常事件
        AbnormalEventQueryParam abnormalDataQueryParam = new AbnormalEventQueryParam();
        abnormalDataQueryParam.setRunningMonitorType(param.getRunningMonitorType());
        abnormalDataQueryParam.setInstanceIds(spaceAndEquipmentIds);
        Set<String> hasAbnormalEventIds = abnormalEventService.countAbnormalEvent(abnormalDataQueryParam);
        // 设备的在离线和控返不一致信息点查询
        List<InfoCodeValueVO> equipmentInfoCodeValues =
                abstractIotDataCacheServer.listInfoCodeValueInOneHour(equipmentIds, ABNORMAL_STATE_INFO_CODES);
        Set<String> offlineEquipments = new HashSet<>();
        Set<String> orderFailAlarmEquipments = new HashSet<>();
        // 非空过滤信息点和信息点数值后，直接将id转为集合方便转换
        if (CollectionUtils.isNotEmpty(equipmentInfoCodeValues)) {
            offlineEquipments = processIotInfoCodeValue(equipmentInfoCodeValues, InfoCodeEnum.ONLINE_STATUS.value(), "0");
            orderFailAlarmEquipments = processIotInfoCodeValue(equipmentInfoCodeValues, InfoCodeEnum.ORDER_FAIL_ALARM.value(), "1");
        }

        // 将三个数组转换为 <id, List<StatusEnum>> 的形式
        return convertToMap(hasAbnormalEventIds, offlineEquipments, orderFailAlarmEquipments);
    }

    /**
     * 将异常事件、离线、控返不一致转换为响应的状态map
     *
     * @param hasAbnormalEventIds      有异常事件的id
     * @param offlineEquipments        离线的设备id
     * @param orderFailAlarmEquipments 控返不一致的设备id
     * @return <对象id,List<异常标签>
     */
    private Map<String, List<AbnormalStatusTypeEnum>> convertToMap(Set<String> hasAbnormalEventIds,
                                                                   Set<String> offlineEquipments,
                                                                   Set<String> orderFailAlarmEquipments) {
        Map<String, List<AbnormalStatusTypeEnum>> idToStatusEnumMap = new HashMap<>();

        // 辅助方法，将数组和枚举类型添加到 Map 中
        addIdsToMap(idToStatusEnumMap, hasAbnormalEventIds, AbnormalStatusTypeEnum.ABNORMAL_EVENT);
        addIdsToMap(idToStatusEnumMap, offlineEquipments, AbnormalStatusTypeEnum.OFFLINE);
        addIdsToMap(idToStatusEnumMap, orderFailAlarmEquipments, AbnormalStatusTypeEnum.ORDER_FAIL_ALARM);

        return idToStatusEnumMap;
    }

    /**
     * 将包含id数组转换为状态加入响应集合中
     *
     * @param idToStatusEnumMap      id状态Map集合
     * @param ids                    包含对象id的状态数组
     * @param abnormalStatusTypeEnum 异常枚举
     */
    private static void addIdsToMap(Map<String, List<AbnormalStatusTypeEnum>> idToStatusEnumMap,
                                    Set<String> ids,
                                    AbnormalStatusTypeEnum abnormalStatusTypeEnum) {
        for (String id : ids) {
            idToStatusEnumMap.putIfAbsent(id, new ArrayList<>());
            idToStatusEnumMap.get(id).add(abnormalStatusTypeEnum);
        }
    }

    /**
     * 处理信息点和信息点结果直接转为map集合
     *
     * @param equipmentInfoCodeValues 信息点结果集
     * @param infoCode                信息点
     * @param condition               条件
     * @return 去重后的对象id集合
     */
    private Set<String> processIotInfoCodeValue(List<InfoCodeValueVO> equipmentInfoCodeValues,
                                                String infoCode,
                                                String condition) {
        return equipmentInfoCodeValues.stream()
                .filter(x -> x.getInfoCode().equals(infoCode) && x.getInfoCodeValue().equals(condition))
                .map(InfoCodeValueVO::getObjectId).collect(Collectors.toSet());
    }


}