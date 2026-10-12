package com.persagy.apccontrol.basic.abstracts;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.controller.shellconfig.param.BatchQueryShellConfigParam;
import com.persagy.apccontrol.basic.controller.shellconfig.param.QueryShellConfigByInfoCodeParam;
import com.persagy.apccontrol.basic.controller.shellconfig.param.QueryShellConfigParam;
import com.persagy.apccontrol.basic.controller.shellconfig.param.ShellConfigVO;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.handler.SystemRelationGraphHandler;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.DirectRelationDTO;
import com.persagy.apccontrol.basic.model.dto.EnergyDataInfoQueryParam;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.ObjectClassCodeDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.WeatherResourceEnum;
import com.persagy.apccontrol.basic.model.vo.TagInfoVO;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.dto.riskevent.CheckDeviceInfoDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventEquipDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.TaskInfoDTO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarDayDateTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarDayDateTagResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.impl.RiskEventService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用的抽象类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/15 14:05
 */
@Slf4j
public abstract class AbstractCommonServer implements LogComponent {

    /**
     * 标签信息点
     */
    public static final ArrayList<String> TAG_INFO_CODE_LIST = Lists.newArrayList(
            InfoCodeEnum.RUN_STATUS.getCode(),
            InfoCodeEnum.ONLINE_STATUS.getCode(),
            InfoCodeEnum.ORDER_FAIL_ALARM.getCode(),
            InfoCodeEnum.MANUAL_AUTO_STATUS.getCode(),
            InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode(),
            InfoCodeEnum.FAULT_STATUS.getCode(),
            InfoCodeEnum.LOW_LEV_ALARM.getCode(),
            InfoCodeEnum.HIGH_LEV_ALARM.getCode(),
            InfoCodeEnum.POWER_STATUS.getCode()
    );


    /**
     * 小附件对象类
     */
    public static final ArrayList<String> SMALL_ATTACHMENT_CLAS_CODE = Lists.newArrayList(
            DtpRwdClassCodeEnum.WESSTP.value(),
            DtpRwdClassCodeEnum.WESSPM.value(),
            DtpRwdClassCodeEnum.WESSFL.value(),
            DtpRwdClassCodeEnum.ACVVEO.value(),
            DtpRwdClassCodeEnum.ACVVEJ.value(),
            DtpRwdClassCodeEnum.ACVVNO.value(),
            DtpRwdClassCodeEnum.ACVVNJ.value(),
            DtpRwdClassCodeEnum.FFGCFS.value()
    );


    @Autowired
    protected RiskEventService riskEventService;

    @Autowired
    protected BizDtpRwdService bizDtpRwdService;

    @Autowired
    protected ShellConfigService shellConfigService;

    @Autowired
    protected MidCalendarClientService midCalendarClientService;

    @Autowired
    protected SystemRelationGraphHandler systemRelationGraphHandler;

    @Autowired
    protected DtpRwdClientService dtpRwdClientService;

    /**
     * 获取空调季
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    public MidCalendarAirSeasonTypeEnum getProjectAirSeason(String groupCode, String projectId, LocalDate localDate) {
        List<String> tagList = this.listCalendarDayDateTag(groupCode, projectId, localDate);
        if (CollectionUtil.isEmpty(tagList)) {
            return null;
        }
        for (String tag : tagList) {
            MidCalendarAirSeasonTypeEnum airSeasonTypeEnum = MidCalendarAirSeasonTypeEnum.analyze(tag);
            if (airSeasonTypeEnum != null) {
                return airSeasonTypeEnum;
            }
        }
        return null;
    }

    /**
     * 获取空调工况
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    public MidCalendarAcSeasonTypeEnum getProjectSeason(String groupCode, String projectId, LocalDate localDate) {
        return this.midCalendarClientService.getAcSeasonType(projectId, projectId, localDate == null ? DateUtils.now().toLocalDate() : localDate);
    }

    /**
     * 获取工作历中得标签数据
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    public List<String> listCalendarDayDateTag(String groupCode, String projectId, LocalDate localDate) {
        localDate = localDate == null ? DateUtils.now().toLocalDate() : localDate;
        MidCalendarDayDateTagQueryParam midCalendarDayDateTagQueryParam = new MidCalendarDayDateTagQueryParam();
        midCalendarDayDateTagQueryParam.setObjId(projectId);
        midCalendarDayDateTagQueryParam.setBeginDate(localDate);
        midCalendarDayDateTagQueryParam.setEndDate(localDate);
        List<MidCalendarDayDateTagResponse> calendarDayDateTagResponses = this.midCalendarClientService.listCalendarDayDateTag(projectId, midCalendarDayDateTagQueryParam);
        if (CollectionUtil.isNotEmpty(calendarDayDateTagResponses)) {
            MidCalendarDayDateTagResponse calendarDayDateTagResponse = calendarDayDateTagResponses.get(0);
            return calendarDayDateTagResponse.getTagList();
        }
        return null;
    }

    /**
     * 获取数值类型的壳配置
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    public boolean getAlgorithmConfig(String groupCode, String projectId) {
        try {
            return this.shellConfigService.getAlgorithmConfig(groupCode, projectId);
        } catch (Exception e) {
            log.error("获取算法开关配置异常", e);
        }
        return true;
    }

    /**
     * 获取数值类型的壳配置
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param code
     * @param infoCode
     * @return
     */
    public BigDecimal getBigDecimalShellConfig(String groupCode, String projectId, String objectId, String code, String infoCode) {
        String shellConfig = this.getShellConfig(groupCode, projectId, objectId, code, infoCode);
        return StrUtil.isBlank(shellConfig) ? null : new BigDecimal(shellConfig);
    }

    /**
     * 获取天气变化类型
     *
     * @return 天气变化类型
     * @since 7.260102.0
     */
    public WeatherResourceEnum getShellConfigWeatherChange() {
        String shellConfig = this.getShellConfig(ContextUtil.getGroupCode(), ContextUtil.getProjectId(),
                null, ShellConfigItemEnum.WEATHER_WEATHER_CHANGE.getCode(), null);
        return Arrays.stream(WeatherResourceEnum.values())
                .filter(e -> e.getName().equals(shellConfig)).findFirst()
                .orElse(null);
    }

    /**
     * 获取壳配置数据
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param code
     * @param infoCode
     * @return
     */
    public String getShellConfig(String groupCode, String projectId, String objectId, String code, String infoCode) {
        ShellConfigItem shellConfigItem = null;
        try {
            QueryShellConfigByInfoCodeParam param = new QueryShellConfigByInfoCodeParam();
            param.setProjectId(projectId);
            param.setCode(code);
            param.setObjectId(objectId);
            param.setInfoCode(infoCode);
            shellConfigItem = this.shellConfigService.queryByInfoCode(groupCode, param);
        } catch (Exception e) {
            log.warn("获取开关壳配置数据异常", e);
        }
        return shellConfigItem == null ? null : StrUtil.isBlank(shellConfigItem.getValue()) ? shellConfigItem.getDefaultValues() : shellConfigItem.getValue();
    }

    /**
     * 批量查询对象的壳配置参数
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param group
     * @return
     */
    public List<ShellConfigItem> batchQueryShellConfigByObjectId(String groupCode, String projectId, String objectId, String group) {
        List<ShellConfigItem> shellConfigItems = null;
        try {
            BatchQueryShellConfigParam param = new BatchQueryShellConfigParam();
            param.setProjectId(projectId);
            param.setObjectId(objectId);
            param.setGroup(group);
            shellConfigItems = this.shellConfigService.batchQueryConfigByObjectId(groupCode, param);
        } catch (Exception e) {
            log.error("批量获取对象的壳配置异常，对象ID：{}", objectId, e);
        }
        return shellConfigItems;
    }

    /**
     * 批量查询某场景下的壳配置数据
     *
     * @param groupCode
     * @param projectId
     * @param scene
     * @param groups    分组编码
     * @return
     */
    public List<ShellConfigVO> queryShellConfigByScene(String groupCode, String projectId, String scene, Set<String> groups) {
        List<ShellConfigVO> shellConfigVOS = null;
        try {
            QueryShellConfigParam param = new QueryShellConfigParam();
            param.setProjectId(projectId);
            param.setScene(scene);
            param.setGroups(groups);
            shellConfigVOS = this.shellConfigService.query(groupCode, param);
        } catch (Exception e) {
            log.error("批量获取壳配置异常", e);
        }
        return shellConfigVOS;
    }

    /**
     * 查询对象列表存在规则
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param criteria  条件
     * @param terminal  规则类型，当规则类型为空的时候，边端查询孪生规则，云端查询持久化规则
     * @return {@code List<ObjectsExistRule> }
     * @since 7.250403.0
     */
    public abstract List<ObjectsExistRule> queryObjectsExistRule(String groupCode, String projectId, List<ClassCode> criteria, TerminalEnum terminal);

    /**
     * 查询对象列表存在规则
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param criteria  条件
     * @return {@code List<ObjectsExistRule> }
     * @since 7.250403.0
     */
    public List<ObjectsExistRule> queryObjectsExistRule(String groupCode, String projectId, List<ClassCode> criteria) {
        return queryObjectsExistRule(groupCode, projectId, criteria, null);
    }

    /**
     * 获取绑点规则
     *
     * @param groupCode
     * @param projectId
     * @param criteria
     * @return
     */
    public Map<String, Set<String>> queryObjectsExistRuleMap(String groupCode, String projectId, List<ClassCode> criteria) {
        List<ObjectsExistRule> objectsExistRules = this.queryObjectsExistRule(groupCode, projectId, criteria);
        Map<String, Set<String>> object_infoCode = CollectionUtil.isEmpty(objectsExistRules) ? new HashMap<>()
                : objectsExistRules.stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId, Collectors.mapping(ObjectsExistRule::getInfoCode, Collectors.toSet())));
        return object_infoCode;
    }

    /**
     * 获取绑点规则
     *
     * @param groupCode  集团编码
     * @param projectId  项目id
     * @param classCodes 对象类
     * @return 对象id->信息点
     */
    public Map<String, Set<String>> queryObjectsExistRuleMap(String groupCode, String projectId, Set<String> classCodes) {
        if (CollectionUtil.isEmpty(classCodes)) {
            return new HashMap<>();
        }
        List<ClassCode> criteria = new ArrayList<>();
        classCodes.forEach(classCode -> criteria.add(new ClassCode(classCode)));
        List<ObjectsExistRule> objectsExistRules = this.queryObjectsExistRule(groupCode, projectId, criteria);
        Map<String, Set<String>> object_infoCode = CollectionUtil.isEmpty(objectsExistRules) ? new HashMap<>()
                : objectsExistRules.stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId, Collectors.mapping(ObjectsExistRule::getInfoCode, Collectors.toSet())));
        return object_infoCode;
    }

    /**
     * 获取关系对象
     *
     * @param groupCode
     * @param projectId
     * @param objectIds    条件对象
     * @param classCodes   需要查询出的设备类
     * @param graphCode
     * @param relCode
     * @param relDirection 要查询的那一方
     * @return
     */
    public List<JSONObject> queryRelationObjectList(String groupCode, String projectId, List<String> objectIds, List<String> classCodes, String graphCode, String relCode, String relDirection) {
        // 获取关系对象详情
        List<JSONObject> relationObjects = null;
        if (CommonConst.FROM_RELATION.equals(relDirection)) {
            relationObjects = this.bizDtpRwdService.queryObjectList(groupCode, projectId, classCodes, graphCode, relCode, null, objectIds);
        } else {
            relationObjects = this.bizDtpRwdService.queryObjectList(groupCode, projectId, classCodes, graphCode, relCode, objectIds, null);
        }
        return relationObjects;
    }

    /**
     * 获取扩展规则所对应的中台对象集
     *
     * @param groupCode
     * @param projectId
     * @param systemClassCode
     * @return
     */
    public List<JSONObject> getExtraRuleObjects(String groupCode, String projectId, String systemId, String systemClassCode) {
        List<JSONObject> resultList = new ArrayList<>();
        List<DirectRelationDTO> primitivesConfig = this.systemRelationGraphHandler.getPrimitivesConfig(systemClassCode);
        if (CollectionUtil.isEmpty(primitivesConfig)) {
            return resultList;
        }

        // 返回扩展规则的对象集合
        for (DirectRelationDTO directRelationDTO : primitivesConfig) {
            boolean from = CommonConst.FROM_RELATION.equals(directRelationDTO.getDirect());
            List<JSONObject> objectList = this.queryRelationObjectList(groupCode, projectId, Lists.newArrayList(systemId), Lists.newArrayList(from ? directRelationDTO.getFromClassCode() : directRelationDTO.getToClassCode()),
                    directRelationDTO.getGraphCode(), directRelationDTO.getRelCode(), directRelationDTO.getDirect());
            if (CollectionUtil.isNotEmpty(objectList)) {
                resultList.addAll(objectList);
            }
        }
        return resultList;
    }

    /**
     * 获取异常事件配置数据，并解析
     *
     * @param classCode
     * @return
     */
    public List<DirectRelationDTO> getRiskEventConfig(String classCode) {
        return this.systemRelationGraphHandler.getRiskEventConfig(classCode);
    }

    /**
     * 获取所有的信息点配置
     *
     * @return
     */
    public abstract List<RcInfoCodeCfg> queryAllInfoCodeConfigs();

    /**
     * 获取异常事件、巡检信息
     *
     * @param groupCode
     * @param projectId
     * @param objectClassCodeDTOS
     * @return
     */
    public abstract List<ExceptionEventEquipDTO> queryRiskEventList(String groupCode, String projectId, List<ObjectClassCodeDTO> objectClassCodeDTOS);

    /**
     * 查询hover图中iot信息
     *
     * @param infoDTO 信息卡入参
     * @return 信息卡数据
     */
    public abstract List<ExceptionEventEquipDTO> queryEquipmentOrSpaceIot(@NotNull CheckDeviceInfoDTO infoDTO);

    /**
     * 根据设备id返回标签  异常	控反不一致	运行状态	在线状态	手自动状态
     * 此处需要注意云边为两套代码，边缘端取值逻辑与云端不一样
     *
     * @param projectId               项目id
     * @param iotDataRequestVos       设备id集合
     * @param exceptionEventEquipList 异常事件信息
     * @return 对象id -> 类型标签
     * @since 5.240501.0
     */
    public abstract Map<String, TagInfoVO> mapEquipmentIdAndTag(@NotNull String projectId,
                                                                @NotNull List<ObjectClassCodeDTO> iotDataRequestVos,
                                                                @Null List<ExceptionEventEquipDTO> exceptionEventEquipList);

    public abstract String getMeoiCommon(String groupCode, String projectId, String uri, Method method, Object body, Map<String, Object> param);

    /**
     * 查询空间|设备ID和异常事件映射
     *
     * @param spaceIds              空间ID
     * @param riskMeasureAimLevel2s
     * @return 空间|设备ID -> 异常事件
     * @since 5.241101.0
     */
    public Map<String, List<ExceptionEventBaseInfoVO>> mapInstanceIdAndExceptionEventsWithEquipments(
            @NonNull Collection<String> spaceIds,
            @Nullable Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Maps.newHashMap();
        }
        return riskEventService.exceptionMapToObjectKey(spaceIds, riskMeasureAimLevel2s);
    }

    /**
     * 获取异常事件，去重后
     *
     * @param objectIdList          对象id
     * @param riskMeasureAimLevel2s
     * @return 对象id->异常事件数组
     */
    public Map<String, List<ExceptionEventBaseInfoVO>> mapRiskEvent(Collection<String> objectIdList, Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s) {
        if (CollectionUtil.isEmpty(objectIdList)) {
            return Maps.newHashMap();
        }
        // 获取异常事件详情
        return this.riskEventService.exceptionMapToObjectKey(objectIdList, riskMeasureAimLevel2s);
//        Map<String, List<ExceptionEventBaseInfoVO>> exceptionMap =
//                this.riskEventService.exceptionMapToObjectKey(objectIdList);
//        String projectId = ContextUtil.getProjectId();
//        Map<String, DtpRwdObjectDTO> objectMap = new HashMap<>();
//        if (localName) {
//            DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
//            dtpRwdObjectQueryParam.setId(objectIdList);
//            dtpRwdObjectQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.objType, DtpRwdObjectDTO.Fields.localName);
//            // 如果需要设备的本地名称，这里查询一次中台对象信息
//            List<DtpRwdObjectDTO> dtpRwdObjectList = BusinessThreadPoolExecutor.parallelExecute(() -> this.dtpRwdClientService.listObject(false, false, projectId, dtpRwdObjectQueryParam));
//
//            //List<DtpRwdObjectDTO> dtpRwdObjectList = this.dtpRwdClientService.listObject(false, false, projectId, dtpRwdObjectQueryParam);
//            if (CollectionUtils.isNotEmpty(dtpRwdObjectList)) {
//                objectMap = dtpRwdObjectList.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity(), (v1, v2) -> v2));
//            }
//        }
//        // map集合重组，异常事件编码去重，去重为全局去重，而数据保持原有响应
//        Map<String, List<ExceptionEventBaseInfoVO>> resultMap = new LinkedHashMap<>();
//        for (Map.Entry<String, List<ExceptionEventBaseInfoVO>> entry : exceptionMap.entrySet()) {
//            List<ExceptionEventBaseInfoVO> updatedList = new ArrayList<>();
//            for (ExceptionEventBaseInfoVO obj : entry.getValue()) {
//                if (objectMap.containsKey(entry.getKey())) {
//                    DtpRwdObjectDTO dtpRwdObject = objectMap.get(entry.getKey());
//                    obj.setId(dtpRwdObject.getId());
//                    obj.setLocalName(dtpRwdObject.getLocalName());
//                    obj.setObjectType(dtpRwdObject.getObjType());
//                }
//                updatedList.add(obj);
//            }
//            // 排序
//            updatedList.sort(Comparator.comparing(ExceptionEventBaseInfoVO::getExceptionStartTime).reversed());
//            if (CollectionUtil.isNotEmpty(updatedList)) {
//                resultMap.put(entry.getKey(), updatedList);
//            }
//        }
//        return resultMap;
    }


    /**
     * 抽象异常事件叠加风控目标
     *
     * @param objectIdList          对象id
     * @param riskMeasureAimLevel2s
     * @return 对象id->异常事件数组
     */
    public List<ExceptionEventBaseInfoVO> listAbstractOverlayRiskControlTargetConditions(
            @NonNull Collection<String> objectIdList, @Nullable Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s) {
        Map<String, List<ExceptionEventBaseInfoVO>> exceptionEventMap = this.riskEventService.exceptionMapToObjectKey(objectIdList, riskMeasureAimLevel2s);
        if (MapUtils.isEmpty(exceptionEventMap)) {
            return new ArrayList<>();
        }
        // 去重
        exceptionEventMap = ExceptionEventBaseInfoVO.duplicateRemoval(exceptionEventMap);
        return exceptionEventMap.values().stream().flatMap(List::stream)
                .sorted(Comparator.comparing(ExceptionEventBaseInfoVO::getExceptionStartTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取巡检任务
     *
     * @param objectIdList 对象id
     * @return 对象id->巡检信息
     */
    public abstract Map<String, TaskInfoDTO> mapTaskInfo(List<String> objectIdList);

    /**
     * 获取rwd查询参数
     *
     * @param equipIdList 设备id
     * @param infoCodes   信息点
     * @return rwd的查询参数
     */
    public EnergyDataQueryParam getRwdInfo(List<String> equipIdList, List<String> infoCodes) {
        List<EnergyDataInfoQueryParam> objectInfoCode = new ArrayList<>();
        equipIdList.forEach(equipmentId -> {
            EnergyDataInfoQueryParam objectBodyDto = new EnergyDataInfoQueryParam();
            objectBodyDto.setObjectId(equipmentId);
            objectBodyDto.setInfoCodes(infoCodes);
            objectInfoCode.add(objectBodyDto);
        });
        EnergyDataQueryParam body = new EnergyDataQueryParam();
        body.setObjects(objectInfoCode);
        return body;
    }

    /**
     * iot历史数据
     *
     * @param objectIds    对象ID
     * @param infoCodeList 信息点编码
     * @return 对象ID -> [信息点编码 -> 数值]
     * @since 5.240702.0
     */
    public Map<String, Map<String, Map<String, String>>> queryObjectIdInfoCodeHistory(@NonNull DtpRwdPersistenceTimeTypeEnum ipType,
                                                                                      @NonNull Collection<String> objectIds,
                                                                                      @NonNull Collection<String> infoCodeList) {
        // 仅支持co2和tdb两个信息点
        EnergyDataQueryParam bodyDto = getRwdInfo(new ArrayList<>(objectIds), new ArrayList<>(infoCodeList));
        // 将时间设为当天的 0 点
        bodyDto.setStartTime(DateUtils.get24TimeStringBeforeHour());
        bodyDto.setEndTime(DateUtils.getNowTimeString());
        bodyDto.setIpType(ipType.value());

        // iot数据查询
        List<EnergyDataResponse> ipDataList = this.bizDtpRwdService.queryIpDataInPeriod(bodyDto);
        if (CollectionUtils.isNotEmpty(ipDataList)) {
            // 按空间分组，再按信息点，再按时间，最后的value是数值方便取值
            return ipDataList.stream()
                    .collect(Collectors.groupingBy(
                            EnergyDataResponse::getObjectId,
                            Collectors.groupingBy(
                                    EnergyDataResponse::getInfoCode,
                                    Collectors.toMap(
                                            EnergyDataResponse::getIpTime,
                                            item -> String.valueOf(item.getIpValue())
                                    )
                            )
                    ));
        }
        return new HashMap<>();

    }

    /**
     * 获取五分钟精度时间，云端24小时持久化时间，边缘端为2小时
     *
     * @return 有序的数组
     */
    public abstract LinkedList<LocalDateTime> getFiveMinuteAccuracyTime();

    /**
     * 获得当天五分钟精确时间，云端24小时所有五分钟精度时间，边缘端为2小时
     *
     * @return 有序的数组
     */
    public abstract LinkedList<LocalDateTime> getFifteenMinuteAccuracyTime();

    /**
     * 根据价值选择方案，获取对应的碳责任因子，或电价信息
     *
     * @param groupCode
     * @param projectId
     * @param currentDateTime
     * @param valueSelectType
     */
    public abstract List<ValueSelectionTrendTableDto.ElecData> queryValueSelectionDataList(String groupCode, String projectId, LocalDateTime currentDateTime, BasicsConfigTypeEnum valueSelectType);

}
