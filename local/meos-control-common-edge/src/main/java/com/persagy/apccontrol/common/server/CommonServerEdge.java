package com.persagy.apccontrol.common.server;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Method;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.convert.TagConverter;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InterfaceUrlConst;
import com.persagy.apccontrol.basic.model.dto.ObjectClassCodeDTO;
import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalSpaceTagDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.*;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdTagEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.AbnormalStatusTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.model.vo.CommonServerForwardVO;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.TagInfoVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.IotInfoCodeFieldVo;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.dto.riskevent.CheckDeviceInfoDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventEquipDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.TaskInfoDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectsOfExistRuleQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectElecQueryParam;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.number.NumericalUtils;
import com.persagy.apccontrol.common.outside.feign.RcSaasClient;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.util.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.service.systemgraph.SystemGraphEditorService.SHOW_HIGHT_LOW_LEVEL_ALARM_CLASS_CODE_LIST;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/15 14:15
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommonServerEdge extends AbstractCommonServer {

    private final ProjectCfg projectCfg;
    private final NacosSwitchConfig nacosSwitchConfig;
    private final RcSaasClient rcSaasClient;
    private final AbstractIotDataCacheServer<FeedbackInfoMsgMqttVO> abstractIotDataCacheServer;
    @Lazy
    @Autowired
    private AirTerminalService airTerminalService;
    private final TagConverter tagConverter;
    private final RunningMonitorServerEdge abstractRunningMonitorServer;
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public List<ObjectsExistRule> queryObjectsExistRule(String groupCode, String projectId, List<ClassCode> criteria, TerminalEnum terminal) {
        if (Objects.isNull(terminal)) {
            terminal = TerminalEnum.EDGE;
        }
        return this.dtpRwdClientService.listObjectsOfExistRuleByClassCodeToTerminal(DtpRwdObjectsOfExistRuleQueryParam.of(criteria), terminal);
    }


    @Override
    public List<RcInfoCodeCfg> queryAllInfoCodeConfigs() {
        try {
            CommonServerForwardVO forwardVO = new CommonServerForwardVO(projectCfg.getGroupCode(), projectCfg.getProjectId(), Method.GET, InterfaceUrlConst.INFO_CODE_LIST, null, null, null);
            String response = this.rcSaasClient.meoiAimServerCommon(projectCfg.getGroupCode(), forwardVO);
            return StrUtil.isBlank(response) ? Lists.newArrayList() : JsonUtils.fromJson(response, new TypeReference<List<RcInfoCodeCfg>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("信息点配置获取失败", e);
        }
        return Lists.newArrayList();
    }

    public List<ExceptionEventEquipDTO> queryRiskEventList(String groupCode, String projectId, List<ObjectClassCodeDTO> objectClassCodeDTOS) {
        if (CollectionUtil.isEmpty(objectClassCodeDTOS)) {
            return null;
        }
        CheckDeviceInfoDTO infoDTO = new CheckDeviceInfoDTO();
        infoDTO.setResultIot(false);
        infoDTO.setProjectId(projectId);
        infoDTO.setIotDataRequestVos(objectClassCodeDTOS);
        try {
            return this.riskEventService.queryRiskEventList(infoDTO);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 空间环境参数
     */
    private static final ArrayList<String> INFO_CODE_LIST = Lists.newArrayList(
            InfoCodeEnum.SPACE_TDB.getCode(),
            InfoCodeEnum.SPACE_CO2.getCode(),
            InfoCodeEnum.RH.getCode(),
            InfoCodeEnum.PM2D5.getCode(),
            InfoCodeEnum.PM10.getCode(),
            InfoCodeEnum.CO.getCode(),
            InfoCodeEnum.VOC.getCode(),
            InfoCodeEnum.TVOC.getCode(),
            InfoCodeEnum.HCHO.getCode()
    );

    /**
     * 一位空间信息点
     */
    private static final ArrayList<String> ONE_DECIMAL_PLACE_INFO_CODE = Lists.newArrayList(
            InfoCodeEnum.SPACE_TDB.getCode(),
            InfoCodeEnum.SPACE_CO2.getCode(),
            InfoCodeEnum.RH.getCode()
    );

    /**
     * 加载运行、控制信息点
     *
     * @param infoDTO 请求入参
     * @return 响应设备或者空间信息iot、异常事件、标签、巡检信息
     */
    @Override
    public List<ExceptionEventEquipDTO> queryEquipmentOrSpaceIot(CheckDeviceInfoDTO infoDTO) {
        List<ObjectClassCodeDTO> iotDataRequestVos = infoDTO.getIotDataRequestVos();
        String projectId = infoDTO.getProjectId();
        ObjectTypeEnum objectType = infoDTO.getObjectType();
        String groupCode = ContextUtil.getGroupCode();

        // iot异常事件巡检信息，云边可能有失联的情况
        List<ExceptionEventEquipDTO> objectIotAndExceptionEventList;
        try {
            objectIotAndExceptionEventList = this.riskEventService.queryRiskEventList(infoDTO);
        } catch (Exception e) {
            objectIotAndExceptionEventList = new ArrayList<>();
        }

        // 查询信息点是否有绑点
        ArrayList<ClassCode> criteria = Lists.newArrayList();
        if (ObjectTypeEnum.SPACE == objectType) {
            criteria.add(new ClassCode(DtpRwdClassCodeEnum.SPACE.value()));
        } else {
            iotDataRequestVos.forEach(x -> criteria.add(new ClassCode(x.getClassCode())));
        }
        List<ObjectsExistRule> objectsExistRules = this.queryObjectsExistRule(groupCode, projectId, criteria, null);
        Map<String, List<ObjectsExistRule>> objectRuleByIdMap = new HashMap<>(16);
        if (CollectionUtil.isNotEmpty(objectsExistRules)) {
            objectRuleByIdMap = objectsExistRules.stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId));
        }

        // 标签信息
        Map<String, TagInfoVO> tagInfoMap = getTagInfo(projectId, objectType, iotDataRequestVos, infoDTO, objectIotAndExceptionEventList);

        // 返回hover的配置的进行统一过滤，不进行二次处理
        if (CollectionUtils.isNotEmpty(objectIotAndExceptionEventList)) {
            // 全量信息点
            List<IotInfoCodeFieldVo> filterInfoCodeMap = new ArrayList<>();
            Map<String, List<ObjectsExistRule>> finalObjectRuleByIdMap = objectRuleByIdMap;
            objectIotAndExceptionEventList.forEach(x -> {
                List<IotInfoCodeFieldVo> collect = null;
                List<IotInfoCodeFieldVo> riskEventIotFieldVos = x.getRiskEventIotFieldVos();
                // hover的配置包含当前对象id
                if (finalObjectRuleByIdMap.containsKey(x.getId()) && CollectionUtils.isNotEmpty(riskEventIotFieldVos)) {
                    List<String> bindingPointInfoCodeList = finalObjectRuleByIdMap.get(x.getId()).stream().map(ObjectsExistRule::getInfoCode).collect(Collectors.toList());
                    // 过滤未绑点的信息点
                    collect = riskEventIotFieldVos.stream()
                            .filter(c -> bindingPointInfoCodeList.contains(c.getCode())
                                    || !DtpRwdTagEnum.RUN_PARAM.getValue().equals(c.getInfoType()))
                            .collect(Collectors.toList());
                    filterInfoCodeMap.addAll(collect);
                }
                x.setRiskEventIotFieldVos(collect);
            });

            List<String> infoCodeList = new ArrayList<>();
            // 全量hover的信息点
            if (CollectionUtils.isNotEmpty(filterInfoCodeMap)) {
                infoCodeList = filterInfoCodeMap.stream()
                        .map(IotInfoCodeFieldVo::getCode).distinct().collect(Collectors.toList());
            }

            // 有配置的iot数据信息
            List<String> objectIdList = objectIotAndExceptionEventList.stream()
                    .filter(x -> CollectionUtils.isNotEmpty(x.getRiskEventIotFieldVos()))
                    .map(ExceptionEventEquipDTO::getId).collect(Collectors.toList());

            // 查边缘测iot数据
            List<FeedbackInfoMsgMqttVO> objectDataList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(infoCodeList) && CollectionUtils.isNotEmpty(objectIdList)) {
                objectDataList =
                        this.abstractIotDataCacheServer.queryIotRealDataListByList(
                                ContextUtil.getGroupCode(), projectId, objectIdList, infoCodeList);
            }

            // 信息点数据非空则转成<对象id,<信息点，数值>>的map集合
            Map<String, Map<String, String>> objectInfoMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(objectDataList)) {
                objectInfoMap = objectDataList.stream()
                        .collect(Collectors.groupingBy(FeedbackInfoMsgMqttVO::getObjectId,
                                Collectors.toMap(FeedbackInfoMsgMqttVO::getInfoCode, FeedbackInfoMsgMqttVO::getDataValue)));
            }
            // 格式化数据
            formatDataList(objectIotAndExceptionEventList, tagInfoMap, objectInfoMap);
        }

        return objectIotAndExceptionEventList;
    }

    /**
     * 查询设备或者空间的标签信息
     *
     * @param projectId                      项目id
     * @param objectType                     对象类型
     * @param iotDataRequestVos              iot数据信息
     * @param infoDTO                        查询iothover信息
     * @param objectIotAndExceptionEventList 对象信息数据
     * @return 空间设备标签信息 对象id->标签
     */
    private Map<String, TagInfoVO> getTagInfo(String projectId,
                                              ObjectTypeEnum objectType,
                                              List<ObjectClassCodeDTO> iotDataRequestVos,
                                              CheckDeviceInfoDTO infoDTO,
                                              List<ExceptionEventEquipDTO> objectIotAndExceptionEventList) {
        Map<String, TagInfoVO> tagInfoMap;
        if (ObjectTypeEnum.EQUIPMENT == objectType) {
            tagInfoMap = this.mapEquipmentIdAndTag(projectId, iotDataRequestVos, objectIotAndExceptionEventList);
        } else {
            List<String> spaceIdList = infoDTO.getIotDataRequestVos().stream().map(ObjectClassCodeDTO::getObjectId).collect(Collectors.toList());
            tagInfoMap = new LinkedHashMap<>();
            Map<String, AirTerminalSpaceTagDTO> spaceIdAndTag = airTerminalService.mapSpaceIdAndTag(projectId, spaceIdList);
            if (MapUtils.isNotEmpty(spaceIdAndTag)) {
                spaceIdAndTag.forEach((spaceId, tag) -> tagInfoMap.put(spaceId, tagConverter.toCommonTag(tag)));
            }
        }
        return tagInfoMap;
    }

    /**
     * 数据格式化
     *
     * @param objectIotAndExceptionEventList 响应数据
     * @param tagInfoMap                     标签信息集合
     * @param objectInfoMap                  对象iot信息点数据
     */
    private void formatDataList(List<ExceptionEventEquipDTO> objectIotAndExceptionEventList,
                                Map<String, TagInfoVO> tagInfoMap,
                                Map<String, Map<String, String>> objectInfoMap) {
        objectIotAndExceptionEventList.forEach(equipIotInfo -> {
            equipIotInfo.setTagInfo(tagInfoMap.getOrDefault(equipIotInfo.getId(), new TagInfoVO()));
            List<IotInfoCodeFieldVo> riskEventIotFieldVos = equipIotInfo.getRiskEventIotFieldVos();
            // 非空空间定值信息点赋值格式化
            if (CollectionUtils.isNotEmpty(riskEventIotFieldVos)) {
                riskEventIotFieldVos.forEach(
                        iotField ->
                        {
                            String code = iotField.getCode();
                            String dataValue = objectInfoMap.getOrDefault(
                                            equipIotInfo.getId(), new HashMap<>(16)
                                    )
                                    .getOrDefault(code, "");
                            if (StringUtils.isNotBlank(dataValue) && INFO_CODE_LIST.contains(code)) {
                                dataValue = NumericalUtils.formatNumberWithDecimal(dataValue, ONE_DECIMAL_PLACE_INFO_CODE.contains(code) ? 1 : 2);
                            }
                            if (DtpRwdTagEnum.RUN_PARAM.getValue().equals(iotField.getInfoType())) {
                                iotField.setDataValue(dataValue);
                            }
                        }
                );
            }
        });
    }


    /**
     * 根据设备id返回标签  异常	控反不一致	运行状态	在线状态	手自动状态
     * TODO 后续考虑重构或者直接废弃
     *
     * @param projectId               项目id
     * @param iotDataRequestVos       设备id集合
     * @param exceptionEventEquipList 设备iot、异常事件、巡检信息
     * @return 对象id -> 类型标签
     * @since 5.240501.0
     */
    @Override
    public Map<String, TagInfoVO> mapEquipmentIdAndTag(String projectId,
                                                       List<ObjectClassCodeDTO> iotDataRequestVos,
                                                       List<ExceptionEventEquipDTO> exceptionEventEquipList) {
        List<String> objectIdList = iotDataRequestVos.stream().map(ObjectClassCodeDTO::getObjectId).collect(Collectors.toList());
        Map<String, String> objectIdAndClassCodeMap = iotDataRequestVos.stream().collect(Collectors.toMap(ObjectClassCodeDTO::getObjectId, ObjectClassCodeDTO::getClassCode));
        List<String> highAndLowLevelAlarmIds = iotDataRequestVos.stream()
                .filter(x -> SHOW_HIGHT_LOW_LEVEL_ALARM_CLASS_CODE_LIST.contains(x.getClassCode()))
                .map(ObjectClassCodeDTO::getObjectId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(objectIdList)) {
            return new HashMap<>(1);
        }

        // 查询标签信息点数据
        List<FeedbackInfoMsgMqttVO> objectDataList = this.abstractIotDataCacheServer.queryIotRealDataListByList(
                ContextUtil.getGroupCode(), projectId, objectIdList, TAG_INFO_CODE_LIST);
        iotDataRequestVos.forEach(x -> {
            String classCode = x.getClassCode();
            if (classCode.equals(DtpRwdClassCodeEnum.ACCCAH.getValue()) || DtpRwdClassCodeEnum.ACAHC_GROUP.getValue().equals(classCode)) {
                FeedbackInfoMsgMqttVO pointInfo = this.abstractRunningMonitorServer.getGroupObjectMessage(ContextUtil.getGroupCode(), projectId, x.getObjectId(),
                        classCode, InfoCodeEnum.RUN_STATUS.getCode());
                if (pointInfo != null) {
                    objectDataList.add(pointInfo);
                }
            }
        });
        // 信息点非空根据对象id进行分组
        Map<String, List<FeedbackInfoMsgMqttVO>> infoCodeByObjectIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(objectDataList)) {
            infoCodeByObjectIdMap = objectDataList.stream().collect(Collectors.groupingBy(FeedbackInfoMsgMqttVO::getObjectId));
        }

        // 为空补充异常事件查询，因为hover已经掉过一次，减少查询次数
        if (CollectionUtils.isEmpty(exceptionEventEquipList)) {
            exceptionEventEquipList = this.queryRiskEventList(ContextUtil.getGroupCode(), projectId, iotDataRequestVos);
        }
        List<String> hasExceptionEventIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(exceptionEventEquipList)) {
            // 过滤掉空对象
            hasExceptionEventIdList = exceptionEventEquipList.stream()
                    .filter(x -> CollectionUtils.isNotEmpty(x.getExceptionEventInfoList()))
                    // 将结果收集为列表
                    .collect(Collectors.toList()).stream().map(ExceptionEventEquipDTO::getId).distinct().collect(Collectors.toList());
        }

        return processDeviceIotInfoToTags(objectIdAndClassCodeMap, infoCodeByObjectIdMap, hasExceptionEventIdList, highAndLowLevelAlarmIds);
    }

    @Override
    public String getMeoiCommon(String groupCode, String projectId, String uri, Method method, Object body, Map<String, Object> param) {
        CommonServerForwardVO forwardVO = new CommonServerForwardVO(groupCode, projectId, method, uri, null, body, param);
        return this.rcSaasClient.meoiAimServerCommon(groupCode, forwardVO);
    }

    /**
     * 处理设备信息点转换成tag标签
     *
     * @param objectIdAndClassCodeMap 对象id，对象类集合
     * @param infoCodeByObjectIdMap   对象查询iot信息点返回的iot数据
     * @param hasExceptionEventIdList 有异常事件的id集合
     */
    private Map<String, TagInfoVO> processDeviceIotInfoToTags(Map<String, String> objectIdAndClassCodeMap,
                                                              Map<String, List<FeedbackInfoMsgMqttVO>> infoCodeByObjectIdMap,
                                                              List<String> hasExceptionEventIdList,
                                                              List<String> highAndLowLevelAlarmIds) {
        Map<String, TagInfoVO> resultTagInfo = new HashMap<>();
        objectIdAndClassCodeMap.forEach((objectId, classCode) -> {
            TagInfoVO value = new TagInfoVO();
            List<AbnormalStatusTypeEnum> abnormal = new ArrayList<>();
            if (infoCodeByObjectIdMap.containsKey(objectId)) {
                List<FeedbackInfoMsgMqttVO> iotInfoList = infoCodeByObjectIdMap.get(objectId);
                iotInfoList.forEach(iotInfo -> {
                    // 运行状态
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.RUN_STATUS.getCode())) {
                        value.setOnOffState(StringUtils.equals("0", iotInfo.getDataValue())
                                ? OnOffStateEnum.CLOSE : OnOffStateEnum.OPEN);
                    }
                    // 在离线状态
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.ONLINE_STATUS.getCode())) {
                        value.setOnlineStatusEnum(StringUtils.equals("0", iotInfo.getDataValue())
                                ? OnlineStatusEnum.OFFLINE : OnlineStatusEnum.ONLINE);
                    }
                    // 控反不一致 且非小附件
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.ORDER_FAIL_ALARM.getCode()) && !SMALL_ATTACHMENT_CLAS_CODE.contains(classCode)) {
                        if (StringUtils.equals("1", iotInfo.getDataValue())) {
                            abnormal.add(AbnormalStatusTypeEnum.ORDER_FAIL_ALARM);
                        }
                    }
                    // 手自动状态
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode())) {
                        value.setManualAutoStatus(StringUtils.equals("0", iotInfo.getDataValue())
                                ? HandAutomatismEnum.MANUAL_OPERATION : HandAutomatismEnum.AUTO);
                    }
                    // 本地远程
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode())) {
                        value.setLocalRemote(StringUtils.equals("0", iotInfo.getDataValue())
                                ? LocalRemoteEnum.LOCAL : LocalRemoteEnum.REMOTE);
                    }

                    if (highAndLowLevelAlarmIds.contains(objectId)) {
                        // 高液位报警
                        if (iotInfo.getInfoCode().equals(InfoCodeEnum.HIGH_LEV_ALARM.getCode())) {
                            if (StringUtils.equals("1", iotInfo.getDataValue())) {
                                abnormal.add(AbnormalStatusTypeEnum.HIGH_LEV_ALARM);
                            }
                        }
                        // 低液位报警
                        if (iotInfo.getInfoCode().equals(InfoCodeEnum.LOW_LEV_ALARM.getCode())) {
                            if (StringUtils.equals("1", iotInfo.getDataValue())) {
                                abnormal.add(AbnormalStatusTypeEnum.LOW_LEV_ALARM);
                            }
                        }
                        // 故障
                        if (iotInfo.getInfoCode().equals(InfoCodeEnum.FAULT_STATUS.getCode())) {
                            if (StringUtils.equals("1", iotInfo.getDataValue())) {
                                abnormal.add(AbnormalStatusTypeEnum.FAULT_STATUS);
                            }
                        }
                    }
                    // 断电
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.POWER_STATUS.getCode())) {
                        if (StringUtils.equals("0", iotInfo.getDataValue())) {
                            value.setPowerStatus(PowerStatusEnum.OUTAGE);
                        }
                    }
                });
            }
            // 异常事件
            if (hasExceptionEventIdList.contains(objectId)) {
                abnormal.add(AbnormalStatusTypeEnum.ABNORMAL_EVENT);
            }
            value.setAbnormal(abnormal);
            resultTagInfo.put(objectId, value);
        });
        return resultTagInfo;
    }

    /**
     * 获取巡检任务
     *
     * @param objectIdList 对象id
     * @return 对象id->巡检信息
     */
    @Override
    public Map<String, TaskInfoDTO> mapTaskInfo(List<String> objectIdList) {
        Map<String, TaskInfoDTO> taskInfoMap = new HashMap<>(16);
        if (!nacosSwitchConfig.getYihengServerSwitch()) {
            return taskInfoMap;
        }
        try {
            R<Map<String, TaskInfoDTO>> response = this.rcSaasClient.mapTaskInfo(ContextUtil.getProjectId(), objectIdList);
            taskInfoMap = response == null ? null : response.getData();
        } catch (Exception e) {
            logProjectInstance().error("调用巡检信息出错入参", e, Pair.of("objectIdList", objectIdList));
        }
        return taskInfoMap;
    }


    @Override
    public LinkedList<LocalDateTime> getFiveMinuteAccuracyTime() {
        return DateUtils.getFiveMinutesWithTimeRange(24);
    }

    @Override
    public LinkedList<LocalDateTime> getFifteenMinuteAccuracyTime() {
        return DateUtils.getTwoHourFiveMinuteAccuracyTime();
    }

    @Override
    public List<ValueSelectionTrendTableDto.ElecData> queryValueSelectionDataList(String groupCode, String projectId, LocalDateTime endDateTime, BasicsConfigTypeEnum valueSelectType) {
        ValueSelectElecQueryParam param = new ValueSelectElecQueryParam();
        param.setProjectId(projectId);
        param.setEnableSample(false);
        param.setStartTime(endDateTime.toLocalDate().atStartOfDay());
        param.setEndTime(endDateTime);
        R<ValueSelectionTrendTableDto> selectionTrendTableDto = BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CAST.equals(valueSelectType)
                ? this.rcSaasClient.listElectricityData(groupCode, param) : BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CARBON_EMISSIONS.equals(valueSelectType)
                ? this.rcSaasClient.listCarbonData(groupCode, param) : null;
        return selectionTrendTableDto == null || selectionTrendTableDto.getData() == null ? null : selectionTrendTableDto.getData().getElecDataList();
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "边缘端公共";
    }
}
