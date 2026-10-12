package com.persagy.apccontrol.common.server;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Method;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.abstracts.AbstractRunningMonitorServer;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
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
import com.persagy.apccontrol.basic.model.vo.TagInfoVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.IotInfoCodeFieldVo;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.DtpPointInfo;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.dto.riskevent.CheckDeviceInfoDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventEquipDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.TaskInfoDTO;
import com.persagy.apccontrol.basic.outside.feign.client.FmWorkOrderClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectsOfExistRuleQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.MeoiAimServerService;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.service.demand.airconditioning.ValueSelectionService;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectElecQueryParam;
import com.persagy.apccontrol.basic.sse.SseEmitterServer;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
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
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/15 14:08
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommonServerCloud extends AbstractCommonServer {

    private final NacosSwitchConfig nacosSwitchConfig;

    private final MeoiAimServerService meoiAimServerService;
    private final AbstractIotDataCacheServer<DtpPointInfo> abstractIotDataCacheServer;
    @Lazy
    @Autowired
    private AirTerminalService airTerminalService;
    private final TagConverter tagConverter;
    private final AbstractRunningMonitorServer abstractRunningMonitorServer;
    private final FmWorkOrderClient fmWorkOrderClient;
    private final SseEmitterServer sseEmitterServer;
    private final ValueSelectionService valueSelectionService;
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public List<ObjectsExistRule> queryObjectsExistRule(String groupCode, String projectId, List<ClassCode> criteria, TerminalEnum terminal) {
        if (Objects.isNull(terminal)) {
            terminal = TerminalEnum.CLOUD;
        }
        return this.dtpRwdClientService.listObjectsOfExistRuleByClassCodeToTerminal(DtpRwdObjectsOfExistRuleQueryParam.of(criteria), terminal);
    }

    /**
     * 获取所有的信息点配置
     *
     * @return
     */
    @Override
    public List<RcInfoCodeCfg> queryAllInfoCodeConfigs() {
        CommonServerForwardVO forwardVO = new CommonServerForwardVO(null, null, Method.GET, InterfaceUrlConst.INFO_CODE_LIST, null, null, null);
        String response = this.meoiAimServerService.commonRoute(forwardVO);
        if (StrUtil.isBlank(response)) {
            return Lists.newArrayList();
        }
        return StrUtil.isBlank(response) ? Lists.newArrayList() : JsonUtils.fromJson(response, new TypeReference<List<RcInfoCodeCfg>>() {
        });
    }

    @Override
    public List<ExceptionEventEquipDTO> queryRiskEventList(String groupCode, String projectId, List<ObjectClassCodeDTO> objectClassCodeDTOS) {
        return this.meoiAimServerService.queryRiskEventList(groupCode, projectId, objectClassCodeDTOS);
    }

    /**
     * 查询hover图中iot信息
     *
     * @param infoDTO 信息卡入参
     * @return 信息卡数据
     */
    @Override
    public List<ExceptionEventEquipDTO> queryEquipmentOrSpaceIot(CheckDeviceInfoDTO infoDTO) {
        List<ObjectClassCodeDTO> iotDataRequestVos = infoDTO.getIotDataRequestVos();
        String projectId = infoDTO.getProjectId();
        String groupCode = ContextUtil.getGroupCode();
        ObjectTypeEnum objectType = infoDTO.getObjectType();

        // iot异常事件巡检信息
        List<ExceptionEventEquipDTO> objectIotAndExceptionEventList = this.riskEventService.queryRiskEventList(infoDTO);

        // 查询信息点是否有绑点
        ArrayList<ClassCode> criteria = Lists.newArrayList();
        if (ObjectTypeEnum.SPACE == objectType) {
            criteria.add(new ClassCode(DtpRwdClassCodeEnum.SPACE.value()));
        } else {
            iotDataRequestVos.forEach(x -> {
                criteria.add(new ClassCode(x.getClassCode()));
            });
        }
        List<ObjectsExistRule> objectsExistRules = this.queryObjectsExistRule(groupCode, projectId, criteria, null);

        // 绑点的信息点集合
        Map<String, List<ObjectsExistRule>> objectRuleByIdMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(objectsExistRules)) {
            objectRuleByIdMap = objectsExistRules.stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId));
        }

        // 规则map为空则直接将信息点全量制空
        if (CollectionUtils.isNotEmpty(objectIotAndExceptionEventList)) {
            Map<String, List<ObjectsExistRule>> finalObjectRuleByIdMap = objectRuleByIdMap;
            objectIotAndExceptionEventList.forEach(x -> {
                // 当前id规则不存在，则将hover信息点制空 且信息点规则非空，则过滤
                List<IotInfoCodeFieldVo> collect = null;
                List<IotInfoCodeFieldVo> riskEventIotFieldVos = x.getRiskEventIotFieldVos();
                if (finalObjectRuleByIdMap.containsKey(x.getId()) && CollectionUtils.isNotEmpty(riskEventIotFieldVos)) {
                    // 当前id的所有规则
                    List<String> bindingPointInfoCodeList =
                            finalObjectRuleByIdMap.get(x.getId()).stream().map(ObjectsExistRule::getInfoCode).collect(Collectors.toList());
                    collect = riskEventIotFieldVos.stream()
                            .filter(c -> bindingPointInfoCodeList.contains(c.getCode())
                                    || !DtpRwdTagEnum.RUN_PARAM.getValue().equals(c.getInfoType()))
                            .collect(Collectors.toList());
                }
                x.setRiskEventIotFieldVos(collect);
            });
        }

        Map<String, TagInfoVO> tagInfoMap;
        if (ObjectTypeEnum.EQUIPMENT == objectType) {
            // 查询设备的标签
            tagInfoMap = this.mapEquipmentIdAndTag(projectId, iotDataRequestVos, objectIotAndExceptionEventList);
        } else {
            //查询空间下得数据
            List<String> spaceIdList = iotDataRequestVos.stream().map(ObjectClassCodeDTO::getObjectId).collect(Collectors.toList());
            tagInfoMap = new LinkedHashMap<>();
            Map<String, AirTerminalSpaceTagDTO> spaceIdAndTag = this.airTerminalService.mapSpaceIdAndTag(projectId, spaceIdList);
            if (MapUtils.isNotEmpty(spaceIdAndTag)) {
                // 空间标签转通用
                spaceIdAndTag.forEach((spaceId, tag) -> tagInfoMap.put(spaceId, tagConverter.toCommonTag(tag)));
            }
        }
        if (CollectionUtils.isNotEmpty(objectIotAndExceptionEventList)) {
            objectIotAndExceptionEventList.forEach(x -> x.setTagInfo(tagInfoMap.getOrDefault(x.getId(), new TagInfoVO())));
        }
        return objectIotAndExceptionEventList;
    }

    /**
     * 根据设备id返回标签
     *
     * @param projectId         项目id
     * @param iotDataRequestVos 设备id集合
     * @return 设备标签信息
     */
    @Override
    public Map<String, TagInfoVO> mapEquipmentIdAndTag(String projectId,
                                                       List<ObjectClassCodeDTO> iotDataRequestVos,
                                                       List<ExceptionEventEquipDTO> objectIotAndExceptionEventList) {
        // id数组
        List<String> objectIdList = iotDataRequestVos.stream().map(ObjectClassCodeDTO::getObjectId).collect(Collectors.toList());
        Map<String, String> objectIdAndClassCodeMap = iotDataRequestVos.stream().collect(Collectors.toMap(ObjectClassCodeDTO::getObjectId, ObjectClassCodeDTO::getClassCode));
        List<String> highAndLowLevelAlarmIds = iotDataRequestVos.stream()
                .filter(x -> SHOW_HIGHT_LOW_LEVEL_ALARM_CLASS_CODE_LIST.contains(x.getClassCode()))
                .map(ObjectClassCodeDTO::getObjectId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(objectIdList)) {
            return new HashMap<>(1);
        }

        // 查iot实际状态
        List<DtpPointInfo> objectDataList = this.abstractIotDataCacheServer.queryIotRealDataListByList(
                ContextUtil.getGroupCode(), projectId, objectIdList, TAG_INFO_CODE_LIST);

        return processInfoCodeToTag(objectIdAndClassCodeMap, objectDataList, iotDataRequestVos, objectIotAndExceptionEventList, highAndLowLevelAlarmIds);
    }

    /**
     * 将iot数据转为标签
     *
     * @param objectIdAndClassCodeMap        对象id,对象类编码
     * @param objectDataList                 iot数据
     * @param iotDataRequestVos              对象对象类dto
     * @param objectIotAndExceptionEventList 对象iot数据和异常事件结果集
     * @return 标签
     */
    private Map<String, TagInfoVO> processInfoCodeToTag(Map<String, String> objectIdAndClassCodeMap,
                                                        List<DtpPointInfo> objectDataList,
                                                        List<ObjectClassCodeDTO> iotDataRequestVos,
                                                        List<ExceptionEventEquipDTO> objectIotAndExceptionEventList,
                                                        List<String> highAndLowLevelAlarmIds) {
        String projectId = ContextUtil.getProjectId();
        if (CollectionUtils.isEmpty(objectDataList)) {
            objectDataList = new ArrayList<>(16);
        }
        List<DtpPointInfo> finalObjectDataList = objectDataList;
        iotDataRequestVos.forEach(x -> {
            String classCode = x.getClassCode();
            if (classCode.equals(DtpRwdClassCodeEnum.ACCCAH.getValue()) || DtpRwdClassCodeEnum.ACAHC_GROUP.getValue().equals(classCode)) {
                DtpPointInfo pointInfo = this.abstractRunningMonitorServer.getGroupObjectInfoCodeValue(ContextUtil.getGroupCode(), projectId, x.getObjectId(), classCode, InfoCodeEnum.RUN_STATUS.getCode());
                if (pointInfo != null) {
                    finalObjectDataList.add(pointInfo);
                }
            }
        });
        Map<String, List<DtpPointInfo>> iotInfoMap = new HashMap<>();
        // 非空将状态转换为标签枚举类
        if (CollectionUtils.isNotEmpty(objectDataList)) {
            iotInfoMap = objectDataList.stream().collect(Collectors.groupingBy(DtpPointInfo::getObjectId));
        }

        List<String> hasExceptionEventIdList = null;
        // 为空补充异常事件查询，因为hover已经掉过一次，减少查询次数
        if (CollectionUtils.isEmpty(objectIotAndExceptionEventList)) {
            objectIotAndExceptionEventList = meoiAimServerService.queryRiskEventList(ContextUtil.getGroupCode(), projectId, iotDataRequestVos);
        }
        if (CollectionUtils.isNotEmpty(objectIotAndExceptionEventList)) {
            // 过滤掉空对象
            hasExceptionEventIdList = objectIotAndExceptionEventList.stream()
                    .filter(x -> CollectionUtils.isNotEmpty(x.getExceptionEventInfoList()))
                    // 将结果收集为列表
                    .collect(Collectors.toList()).stream().map(ExceptionEventEquipDTO::getId).distinct().collect(Collectors.toList());
        }
        return processDeviceIotInfoToTags(objectIdAndClassCodeMap, iotInfoMap, hasExceptionEventIdList, highAndLowLevelAlarmIds);
    }

    @Override
    public String getMeoiCommon(String groupCode, String projectId, String uri, Method method, Object body, Map<String, Object> param) {
        CommonServerForwardVO forwardVO = new CommonServerForwardVO(groupCode, projectId, method, uri, null, body, param);
        return this.meoiAimServerService.commonRoute(forwardVO);
    }

    /**
     * 获取异常事件，去重后
     *
     * @param objectIdList 对象id
     * @return 对象id->巡检信息
     */
    @Override
    public Map<String, TaskInfoDTO> mapTaskInfo(List<String> objectIdList) {
        HashMap<String, TaskInfoDTO> takInfoMap = new HashMap<>();
        if (!nacosSwitchConfig.getYihengServerSwitch()) {
            return takInfoMap;
        }
        // 过滤数组中为空的
        objectIdList = objectIdList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(objectIdList)) {
            return takInfoMap;
        }
        logProjectInstance().error("查询巡检信息的对象id", Pair.of("objectIdList", objectIdList));

        try {
            R<List<TaskInfoDTO>> resultData = fmWorkOrderClient.getTaskByRwdInstanceIds(objectIdList);
            List<TaskInfoDTO> taskByRwdInstanceIds = resultData.getData();
            return taskByRwdInstanceIds.stream().collect(Collectors.toMap(TaskInfoDTO::getObjectInstanceId, x -> x));
        } catch (Exception e) {
            logProjectInstance().error("查询巡检信息出错", e, Pair.of("objectIdList", objectIdList));
        }
        return takInfoMap;
    }

    /**
     * 信息点和异常事件数据转换为标签
     * TODO 后续考虑重构或者直接废弃
     *
     * @param objectIdAndClassCodeMap 对象id集合
     * @param iotInfoMap              信息点map
     * @param hasExceptionEventIdList 有异常事件的id集合
     */
    private Map<String, TagInfoVO> processDeviceIotInfoToTags(Map<String, String> objectIdAndClassCodeMap,
                                                              Map<String, List<DtpPointInfo>> iotInfoMap,
                                                              List<String> hasExceptionEventIdList,
                                                              List<String> highAndLowLevelAlarmIds) {
        Map<String, TagInfoVO> resultTagInfo = new LinkedHashMap<>();
        objectIdAndClassCodeMap.forEach((objectId, classCode) -> {
            TagInfoVO value = new TagInfoVO();
            List<AbnormalStatusTypeEnum> abnormal = new ArrayList<>();
            if (iotInfoMap.containsKey(objectId)) {
                iotInfoMap.get(objectId).forEach(iotInfo -> {
                    // 运行
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.RUN_STATUS.getCode())) {
                        value.setOnOffState(
                                StringUtils.equals("0", iotInfo.getIpValue())
                                        ? OnOffStateEnum.CLOSE : OnOffStateEnum.OPEN);
                    }
                    // 在离线
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.ONLINE_STATUS.getCode())) {
                        value.setOnlineStatusEnum(StringUtils.equals("0", iotInfo.getIpValue())
                                ? OnlineStatusEnum.OFFLINE : OnlineStatusEnum.ONLINE);
                    }
                    // 控反不一致 且非小附件
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.ORDER_FAIL_ALARM.getCode()) && !SMALL_ATTACHMENT_CLAS_CODE.contains(classCode)) {
                        if (StringUtils.equals("1", iotInfo.getIpValue())) {
                            abnormal.add(AbnormalStatusTypeEnum.ORDER_FAIL_ALARM);
                        }
                    }
                    // 手自动
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode())) {
                        value.setManualAutoStatus(StringUtils.equals("0", iotInfo.getIpValue())
                                ? HandAutomatismEnum.MANUAL_OPERATION : HandAutomatismEnum.AUTO);
                    }
                    // 本地远程
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode())) {
                        value.setLocalRemote(StringUtils.equals("0", iotInfo.getIpValue())
                                ? LocalRemoteEnum.LOCAL : LocalRemoteEnum.REMOTE);
                    }
                    if (highAndLowLevelAlarmIds.contains(objectId)) {
                        // 高液位报警
                        if (iotInfo.getInfoCode().equals(InfoCodeEnum.HIGH_LEV_ALARM.getCode())) {
                            if (StringUtils.equals("1", iotInfo.getIpValue())) {
                                abnormal.add(AbnormalStatusTypeEnum.HIGH_LEV_ALARM);
                            }
                        }
                        // 低液位报警
                        if (iotInfo.getInfoCode().equals(InfoCodeEnum.LOW_LEV_ALARM.getCode())) {
                            if (StringUtils.equals("1", iotInfo.getIpValue())) {
                                abnormal.add(AbnormalStatusTypeEnum.LOW_LEV_ALARM);
                            }
                        }
                        // 故障
                        if (iotInfo.getInfoCode().equals(InfoCodeEnum.FAULT_STATUS.getCode())) {
                            if (StringUtils.equals("1", iotInfo.getIpValue())) {
                                abnormal.add(AbnormalStatusTypeEnum.FAULT_STATUS);
                            }
                        }
                    }
                    // 断电
                    if (iotInfo.getInfoCode().equals(InfoCodeEnum.POWER_STATUS.getCode())) {
                        if (StringUtils.equals("0", iotInfo.getIpValue())) {
                            value.setPowerStatus(PowerStatusEnum.OUTAGE);
                        }
                    }
                });
            }
            // 有异常事件id直接赋值
            if (CollectionUtils.isNotEmpty(hasExceptionEventIdList)
                    && hasExceptionEventIdList.contains(objectId)) {
                abnormal.add(AbnormalStatusTypeEnum.ABNORMAL_EVENT);
            }
            value.setAbnormal(abnormal);
            resultTagInfo.put(objectId, value);
        });
        return resultTagInfo;
    }

    @Override
    public LinkedList<LocalDateTime> getFiveMinuteAccuracyTime() {
        return DateUtils.getFiveMinutesWithTimeRange(288);
    }


    @Override
    public LinkedList<LocalDateTime> getFifteenMinuteAccuracyTime() {
        return DateUtils.getTheSameDayFiveMinuteAccuracyTime();
    }

    @Override
    public List<ValueSelectionTrendTableDto.ElecData> queryValueSelectionDataList(String groupCode, String projectId, LocalDateTime endDateTime, BasicsConfigTypeEnum valueSelectType) {
        ValueSelectElecQueryParam param = new ValueSelectElecQueryParam();
        param.setProjectId(projectId);
        param.setEnableSample(false);
        param.setStartTime(endDateTime.toLocalDate().atStartOfDay());
        param.setEndTime(endDateTime);
        ValueSelectionTrendTableDto selectionTrendTableDto = BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CAST.equals(valueSelectType)
                ? this.valueSelectionService.listElectricityData(param) : BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CARBON_EMISSIONS.equals(valueSelectType)
                ? this.valueSelectionService.listCarbonData(param) : null;
        return selectionTrendTableDto == null ? null : selectionTrendTableDto.getElecDataList();
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240702.0
     */
    @Override
    public String logTag() {
        return "云端公共";
    }

}
