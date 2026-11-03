package com.persagy.apccontrol.common.service.impl;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.opt.OptCallMemo;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.basic.utils.IotDataUtil;
import com.persagy.apccontrol.common.opt.OptPointBatch;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.controller.shellconfig.param.ShellConfigVO;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.mapper.EquipRecordMapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.EnumYesNo;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.PointInfoDTO;
import com.persagy.apccontrol.basic.model.dto.ReleaseInterventionDTO;
import com.persagy.apccontrol.basic.model.dto.RwdObjectBusinessFeildDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.entity.*;
import com.persagy.apccontrol.basic.model.enumtype.*;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogObjectCategpryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.websocket.WebSocketTypeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.dto.WeatherContent;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinManualSend;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherServerService;
import com.persagy.apccontrol.basic.resource.ClassPositionCodeConfigService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.basic.service.InfoCodeSetExtendService;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.handle.InfoCodeLinkControlHandler;
import com.persagy.apccontrol.common.handle.RunningMonitorLogHandler;
import com.persagy.apccontrol.common.model.dto.ExecSend;
import com.persagy.apccontrol.common.model.dto.RunningModeDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.TwcInstructionSend;
import com.persagy.apccontrol.common.server.IotDataCacheServerEdge;
import com.persagy.apccontrol.common.server.SseEmitterEdgeServer;
import com.persagy.apccontrol.common.service.IWSMessageService;
import com.persagy.apccontrol.common.service.RunningMonitorService;
import com.persagy.apccontrol.common.task.ObjectCommandSendResult;
import com.persagy.apccontrol.common.task.ObjectCommandSendTask;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 运行监测处理类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 15:30
 */
@Slf4j
@Service("edgeRunningMonitorServiceImpl")
public class RunningMonitorServiceImpl implements RunningMonitorService {

    @Autowired
    private DtpTwinClientService dtpTwinClientService;

    /** opt B10（switch.opt.forceInfoBatch）：与 IotDataCacheServiceImpl 相同，用本项目配置查 Twin */
    @Autowired(required = false)
    private ProjectCfg projectCfg;

    @Autowired
    private BizDtpRwdService bizDtpRwdService;

    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Lazy
    @Autowired
    private AbstractCommonServer abstractCommonServer;

    @Autowired
    private EquipRecordMapper equipRecordMapper;

    @Autowired
    private EquipRecordService equipRecordService;

    @Autowired
    private IotDataCacheServerEdge iotDataCacheServerEdge;

    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;

    @Autowired
    private RunningMonitorLogHandler runningMonitorLogHandler;

    @Autowired
    private InfoCodeLinkControlHandler infoCodeLinkControlHandler;

    @Autowired
    private ClassPositionCodeConfigService classPositionCodeConfigService;

    @Autowired
    private DemandLightingStrategy demandLightingStrategy;

    @Autowired
    private SseEmitterEdgeServer sseEmitterEdgeServer;

    @Autowired
    private IWSMessageService iWSMessageService;

    @Autowired
    private InfoCodeSetExtendService infoCodeSetExtendService;

    @Override
    public TreeMap<String, String> getManualAutoChangeReason(String groupCode, String projectId, String systemId) {
        List<JSONObject> systemInfos = StrUtil.isBlank(systemId) ? null : bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(systemId));
        if (CollectionUtil.isEmpty(systemInfos)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_b5cd92caf3874c88af713348992bd911"));
        }
        String systemClassCode = systemInfos.get(0).getString("classCode");
        return EnumManualAutoChangeReason.getEnumManualAutoChangeReasonMap(systemClassCode);
    }

    @Override
    public Boolean setOperatingParameters(TwcInstructionSend dto, boolean logFlag) {
        // 验证对象是否存在
        RwdObjectBusinessFeildDTO businessFeildDTO = this.localRedisCacheHandler.getObjectBusinessFeildAndSystemInfo(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getObjectId(), dto.getSystemId());
        if (businessFeildDTO == null) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_aad238dd5dc649de8a43056aae2fb91b"));
        }

        // 远程调用接口进行修改
        InfoCodeSetExtend infoCodeSetExtend;
        for (PointInfoDTO result : dto.getPointInfos()) {
            // 保存信息点设置扩展数据
            infoCodeSetExtend = new InfoCodeSetExtend();
            infoCodeSetExtend.setProjectId(dto.getProjectId());
            infoCodeSetExtend.setSystemId(dto.getSystemId());
            infoCodeSetExtend.setObjId(dto.getObjectId());
            infoCodeSetExtend.setInfoCode(result.getInfoCode());
            infoCodeSetExtend.setExtend(result.getExtendSetting());
            infoCodeSetExtendService.saveOrUpdateWithObjectIdAndInfoCode(infoCodeSetExtend);

            // 指令下发
            sendInstruction(dto.getProjectId(), dto.getSystemId(), dto.getObjectId(), businessFeildDTO, result, logFlag);

            // 推送扩展配置消息
            if (null != result.getExtendSetting()) {
                FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = new FeedbackInfoMsgMqttVO();
                feedbackInfoMsgMqttVO.setSystemId(dto.getSystemId());
                feedbackInfoMsgMqttVO.setObjectId(dto.getObjectId());
                feedbackInfoMsgMqttVO.setProjectId(dto.getProjectId());
                feedbackInfoMsgMqttVO.setExtendSetting(result.getExtendSetting());
                feedbackInfoMsgMqttVO.setInfoCode(result.getInfoCode());
                feedbackInfoMsgMqttVO.setDataTime(DateUtils.getNowTimeString());
                feedbackInfoMsgMqttVO.setWebType(WebSocketTypeEnum.INFO_EXTEND_SET);
                iWSMessageService.pushMessageWeb(feedbackInfoMsgMqttVO);
            }

        }
        return true;
    }

    @Override
    public void sendInstruction(String projectId, String systemId, String objectId, RwdObjectBusinessFeildDTO businessFeildDTO, PointInfoDTO pointInfoDTO, boolean logFlag) {
        // 调用第三方接口进行参数修改 如果调用成功即可保存到系统日志里面
        RcInfoCodeCfg infoCodeConfig = this.localRedisCacheHandler.getInfoCodeConfig(businessFeildDTO.getClassCode(), pointInfoDTO.getInfoCode());
        // 冷却塔出水温度设定 自动需求计算
        if (null != pointInfoDTO.getExtendSetting() && ManualAutoSetEnum.AUTO.equals(pointInfoDTO.getExtendSetting().getManualAutoSet())) {
            // coolWaterReturnTempSet 自动模式时：冷却塔出水温度设定值=室外当前的湿球温度+冷却塔散热趋近温差ΔT
            if (InfoCodeEnum.COOL_WATER_RETURN_TEMP_SET.getCode().equals(pointInfoDTO.getInfoCode())) {
                pointInfoDTO.setValue(getCoolWaterReturnTempSet(projectId, pointInfoDTO.getExtendSetting().getHeatDissipationTemperatureDifference()));
            }
        }
        pointInfoDTO.setBeforeValue(this.iotDataCacheServerEdge.queryIotRealData(null, null, objectId, null, pointInfoDTO.getInfoCode()));
        if (null != pointInfoDTO.getBeforeValue() && pointInfoDTO.getBeforeValue().equals(pointInfoDTO.getValue())) {
            log.info("对象：{} 信息点：{} 设置值：{} ，与之前设置无变化，忽略执行", objectId, pointInfoDTO.getInfoCode(), pointInfoDTO.getValue());
            return;
        }
        String iotValue = this.dtpTwinClientService.setOperatingParameters(objectId, pointInfoDTO.getInfoCode(), pointInfoDTO.getValue(), infoCodeConfig);
        pointInfoDTO.setValue(iotValue);
        if (logFlag && infoCodeConfig != null && pointInfoDTO.getValue() != null) {
            this.runningMonitorLogHandler.saveColdStationParamSetRunningLog(ContextUtil.getGroupCode(), projectId, objectId, pointInfoDTO, infoCodeConfig);
        }
    }


    @Autowired
    private WeatherServerService weatherServerService;

    /**
     * 自动模式下coolWaterReturnTempSet求值
     * 冷却塔出水温度设定值=室外当前的湿球温度+冷却塔散热趋近温差ΔT
     * 结果四舍五入一位小数
     *
     * @param projectId                            项目ID
     * @param heatDissipationTemperatureDifference 冷却塔散热趋近温差ΔT
     * @return {@link String}
     * @author yuecaipu
     * @date 2024/8/5 18:13
     * @since v5.240801
     */
    private String getCoolWaterReturnTempSet(String projectId, BigDecimal heatDissipationTemperatureDifference) {
        if (null == heatDissipationTemperatureDifference) {
            return null;
        }
        // 获取湿球温度
        WeatherContent weatherContent = weatherServerService.queryConfigWeather(ContextUtil.getGroupCode(), projectId);
        if (null != weatherContent && null != weatherContent.getWet_bulb_temp()) {
            // 计算 并四舍五入一位小数
            return heatDissipationTemperatureDifference
                    .add(new BigDecimal(weatherContent.getWet_bulb_temp()))
                    .setScale(1, BigDecimal.ROUND_HALF_UP)
                    .toPlainString();
        }
        return null;
    }


    @Override
    public String releaseIntervention(String groupCode, String projectId, Set<String> objectIds, String infoCode) {
        // 1.获取需要进行解除的设定点位集合
        Set<String> infoCodes = new HashSet<>();
        if (StrUtil.isBlank(infoCode)) {
            List<EquipRecord> records = this.equipRecordService.queryEquipRecordBySourceFrom(groupCode, projectId, objectIds, null, SourceFromEnum.RELEASE_INTERVENTION);
            infoCodes = CollectionUtil.isEmpty(records) ? null : records.stream().filter(item ->
                    item.getChangeContent() != null && StrUtil.isNotBlank(item.getChangeContent().getInfoCode())).map(item -> item.getChangeContent().getInfoCode()).collect(Collectors.toSet());
        } else {
            infoCodes.add(infoCode);
        }
        if (CollectionUtil.isEmpty(infoCodes)) {
            return null;
        }

        // 2.获取所有的设备信息
        List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectIds));
        if (CollectionUtil.isEmpty(objectList)) {
            return null;
        }
        boolean single = objectList.size() == 1;
        Set<String> classCodes = objectList.stream().map(item -> item.getString("classCode")).collect(Collectors.toSet());
        Map<String, Set<String>> objectsExistRuleMap = this.abstractCommonServer.queryObjectsExistRuleMap(groupCode, projectId, classCodes);

        // 3.指令下发
        Map<String, JSONObject> objectId_info = new HashMap<>();
        List<Future<ObjectCommandSendResult>> futureList = new ArrayList<>();
        for (JSONObject objectInfo : objectList) {
            String equipId = objectInfo.getString("id");
            Set<String> rules = objectsExistRuleMap.get(equipId);
            if (CollectionUtil.isEmpty(rules)) {
                continue;
            }
            // 不判段手自动状态，哪怕是处于自动，多下发一次，也无所谓
            for (String sendCode : infoCodes) {
                if (rules.contains(sendCode)) {
                    objectId_info.put(equipId, objectInfo);
                    Future<ObjectCommandSendResult> future = BusinessThreadPoolExecutor.submitCommon(new ObjectCommandSendTask(groupCode, projectId, equipId, sendCode, CommonConst.FORCE_VALUE, dtpTwinClientService, false, false));
                    futureList.add(future);
                }
            }
        }

        // 4.根据执行结果处理业务数据
        Map<String, Set<String>> success_codes = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        List<ReleaseInterventionDTO> releaseInterventionDTOS = new ArrayList<>();
        List<ObjectCommandSendResult> commandSendResults = ObjectCommandSendTask.joinAll(futureList);
        for (ObjectCommandSendResult commandSendResult : commandSendResults) {
            JSONObject objectInfo = objectId_info.get(commandSendResult.getObjectId());
            String classCode = objectInfo.getString("classCode");
            String localName = objectInfo.getString("localName");
            if (commandSendResult.isSuccess()) {
                // 成功的，删除对应 equip_record 中的记录
                success_codes.computeIfAbsent(commandSendResult.getObjectId(), e -> Sets.newHashSet()).add(commandSendResult.getInfoCode());
                releaseInterventionDTOS.add(new ReleaseInterventionDTO(groupCode, projectId, commandSendResult.getObjectId(), classCode, commandSendResult.getInfoCode(), false));
            } else {
                // 失败的
                stringBuilder = stringBuilder.length() == 0 ? stringBuilder.append((single ? LocaleMessageUtil.getMessage("key_021b875c7964464b9c6c12e6a8746563") : LocaleMessageUtil.getMessage("key_59dd71f9572844cabe231dd60c633f65")) + localName + LocaleMessageUtil.getMessage("key_808830bc999d4057b395c7d174c1981e") + commandSendResult.getException())
                        : stringBuilder.append("\n;" + localName + LocaleMessageUtil.getMessage("key_808830bc999d4057b395c7d174c1981e") + commandSendResult.getException());
            }
        }

        // 5.成功的，删除对应 equip_record 中的记录，并推送数据
        if (success_codes.size() > 0) {
            for (Map.Entry<String, Set<String>> stringSetEntry : success_codes.entrySet()) {
                this.equipRecordService.deleteEquipRecordBySourceFrom(groupCode, projectId, Sets.newHashSet(stringSetEntry.getKey()), stringSetEntry.getValue(), SourceFromEnum.RELEASE_INTERVENTION);
            }
            this.sseEmitterEdgeServer.releaseInterventions(releaseInterventionDTOS);
        }

        return stringBuilder.length() == 0 ? null : stringBuilder.toString();
    }

    @Override
    public List<JSONObject> queryManualAutoClassList(Long systemGraphId, String projectId, String systemId) {
        List<JSONObject> resultList = new ArrayList<>();
        String groupCode = ContextUtil.getGroupCode();

        // 1.获取系统数据
        List<JSONObject> systemInfo = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(systemId));
        if (CollectionUtil.isEmpty(systemInfo)) {
            return resultList;
        }
        String systemClassCode = systemInfo.get(0).getString("classCode");

        // 2.获取当前系统下所有与系统图有关的数据
        Map<String, Set<String>> classObjectMap = this.localRedisCacheHandler.getObjectIdsBySystemId(projectId,
                systemId,
                systemGraphId, systemClassCode);

        if (CollectionUtil.isEmpty(classObjectMap)) {
            return resultList;
        }

        List<ClassCode> classCodes = new ArrayList<>();
        Set<String> allObjectIds = new HashSet<>();
        classObjectMap.forEach((classCode, objectIds) -> {
            classCodes.add(new ClassCode(classCode));
            allObjectIds.addAll(objectIds);
        });

        // 3.获取所有的绑点规则与设备数据
        Map<String, Set<String>> objectInfoCodeRuleMap = this.abstractCommonServer.queryObjectsExistRuleMap(groupCode, projectId, classCodes);
        List<JSONObject> allEquipments = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(allObjectIds));
        if (CollectionUtil.isEmpty(objectInfoCodeRuleMap) || CollectionUtil.isEmpty(allEquipments)) {
            return resultList;
        }

        // 4.获取冷却塔控制单元下的所有风机与水阀
        Set<String> acotuFilterIds = new HashSet<>();
        Set<String> acotuObjectIds = classObjectMap.get("ACOTU");
        if (CollectionUtil.isNotEmpty(acotuObjectIds)) {
            List<JSONObject> allFanAndWaterObject = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACCCOF", "ACVVEO"), "CombineSubset", "Eg2Eq", Lists.newArrayList(acotuObjectIds), null);
            if (CollectionUtil.isNotEmpty(allFanAndWaterObject)) {
                allFanAndWaterObject.forEach(item -> acotuFilterIds.add(item.getString("id")));
            }
        }

        // 5.获取当前所有设备的IOT数据
        Map<String, JSONObject> object_info_map = allEquipments.stream().collect(Collectors.toMap(key -> key.getString("id"), Function.identity(), (v1, v2) -> v2));
        Map<String, Map<String, String>> objectInfoIotValueMap = this.iotDataCacheServerEdge.queryIotRealDataMap(groupCode, projectId, object_info_map.keySet(), Sets.newHashSet(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode(), InfoCodeEnum.ONLINE_STATUS.getCode()));

        // 6.数据封装
        for (Map.Entry<String, Set<String>> stringSetEntry : classObjectMap.entrySet()) {
            String classCode = stringSetEntry.getKey();
            Set<String> objectIds = stringSetEntry.getValue();
            if (CollectionUtil.isEmpty(objectIds)) {
                continue;
            }
            List<BasePropertyDTO> filterList = new ArrayList<>();
            for (String objectId : objectIds) {
                JSONObject objectInfo = object_info_map.get(objectId);
                if (objectInfo == null || acotuFilterIds.contains(objectInfo.getString("id"))) {
                    continue;
                }
                CustomBasePropertyDTO propertyDTO = new CustomBasePropertyDTO();
                propertyDTO.setId(objectInfo.getString("id"));
                propertyDTO.setLocalId(objectInfo.getString("localId"));
                propertyDTO.setLocalName(objectInfo.getString("localName"));
                propertyDTO.setClassCode(objectInfo.getString("classCode"));
                propertyDTO.setEnable(true);

                Set<String> infoCodeRules = objectInfoCodeRuleMap.get(propertyDTO.getId());
                Map<String, String> infoCodeIotValueMap = objectInfoIotValueMap.get(propertyDTO.getId());
                String onlineStatus = infoCodeIotValueMap == null ? null : infoCodeIotValueMap.get(InfoCodeEnum.ONLINE_STATUS.getCode());
                String localRemoteStatus = infoCodeIotValueMap == null ? null : infoCodeIotValueMap.get(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode());
                if (CollectionUtil.isNotEmpty(infoCodeRules)) {
                    // 这里区分下系统，热源的需要返回，但是置灰不可选择
                    if (DtpRwdSystemClassCodeEnum.ACCH.getValue().equals(systemClassCode)) {
                        if ((infoCodeRules.contains(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode()) && "0".equals(localRemoteStatus))
                                || (infoCodeRules.contains(InfoCodeEnum.ONLINE_STATUS.getCode()) && !"1".equals(onlineStatus))) {
                            propertyDTO.setEnable(false);
                        }
                    } else if (infoCodeRules.contains(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode()) && "0".equals(localRemoteStatus)) {
                        continue;
                    }

                    // 存在手自动设定点位的数据，返回
                    if (infoCodeRules.contains(InfoCodeEnum.MANUAL_AUTO_SET.getCode())) {
                        filterList.add(propertyDTO);
                    }
                }
            }
            if (filterList.size() > 0) {
                JSONObject result = new JSONObject();
                result.put("classCode", classCode);
                result.put("className", this.localRedisCacheHandler.getClassName(classCode));
                result.put("children", filterList);
                resultList.add(result);
            }
        }

        return resultList;
    }

    @Override
    public JSONObject queryManualAutoStatusByObjectId(String groupCode, String projectId, String objectId) {
        JSONObject result = new JSONObject();
        String manualAutoStatus = this.iotDataCacheServerEdge.queryIotRealData(groupCode, projectId, objectId, null, InfoCodeEnum.MANUAL_AUTO_STATUS.getCode());
        result.put(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode(), manualAutoStatus);

        String expireTime = null;
        if ("0".equals(manualAutoStatus)) {
            EquipRecord equipRecord = this.equipRecordService.getEquipRecordByInfoCodeDesc(groupCode, projectId, null, objectId, InfoCodeEnum.MANUAL_AUTO_SET.getCode(), -1);
            expireTime = equipRecord == null ? null : equipRecord.getChangeContent() == null ? null : equipRecord.getChangeContent().getExecTime();
        }
        result.put("expireTime", expireTime);
        return result;
    }


    @NotNull
    @Override
    public Map<String, Set<String>> getBindRule(String groupCode, String projectId, List<JSONObject> objectList) {
        List<ClassCode> classCodes = new ArrayList<>();
        objectList.stream().forEach(item -> {
            classCodes.add(new ClassCode(item.getString("classCode")));
        });
        List<ObjectsExistRule> objectsExistRules = abstractCommonServer.queryObjectsExistRule(groupCode, projectId, classCodes);
        if (CollectionUtil.isEmpty(objectsExistRules)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_3db14c24430549c6b21638954afcdf5f"));
        }
        Map<String, Set<String>> object_infoCode = objectsExistRules.stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId, Collectors.mapping(ObjectsExistRule::getInfoCode, Collectors.toSet())));
        return object_infoCode;
    }

    @NotNull
    private ExecSend getManualAutoStatusSendResult(String groupCode, RunningModeDTO dto, List<JSONObject> objectList, Map<String, Set<String>> object_infoCode, String sysClassCode, String systemId) {
        boolean auto = "1".equals(dto.getModel());
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, String> expireObjectIdMap = new HashMap<>();
        boolean single = objectList.size() == 1;

        Map<String, JSONObject> objectId_info = new HashMap<>();
        List<EquipRecord> addEquipRecordList = new ArrayList<>();
        List<Future<ObjectCommandSendResult>> exec_result = new ArrayList<>();

        /** 失败对象数据*/
        List<DtpRwdObjectDTO> errorCountObject = Lists.newArrayList();

        /** 不可控对象数据*/
        Set<DtpRwdObjectDTO> noControlObject = new HashSet<>();

        // 指令下发
        int failure = 0;
        for (JSONObject objectInfo : objectList) {
            String equipId = objectInfo.getString("id");
            String localName = objectInfo.getString("localName");
            Set<String> rules = object_infoCode.get(equipId);
            if (CollectionUtil.isEmpty(rules) || !rules.contains(InfoCodeEnum.MANUAL_AUTO_SET.getCode())) {
                stringBuilder = stringBuilder.length() == 0 ? stringBuilder.append((single ? LocaleMessageUtil.getMessage("key_021b875c7964464b9c6c12e6a8746563") : LocaleMessageUtil.getMessage("key_59dd71f9572844cabe231dd60c633f65")) + localName) : stringBuilder.append(";" + localName);
                failure++;
                noControlObject.add(JSONObject.parseObject(objectInfo.toJSONString(), DtpRwdObjectDTO.class));
                continue;
            }
            objectId_info.put(equipId, objectInfo);
            Future<ObjectCommandSendResult> future = BusinessThreadPoolExecutor.submitCommon(new ObjectCommandSendTask(groupCode, dto.getProjectId(), equipId, InfoCodeEnum.MANUAL_AUTO_SET.getCode(), dto.getModel(), dtpTwinClientService, false, false));
            exec_result.add(future);
        }

        // 根据执行结果处理业务数据
        List<ReleaseInterventionDTO> releaseInterventionDTOS = new ArrayList<>();
        List<ObjectCommandSendResult> commandSendResults = ObjectCommandSendTask.joinAll(exec_result);
        for (ObjectCommandSendResult commandSendResult : commandSendResults) {
            JSONObject objectInfo = objectId_info.get(commandSendResult.getObjectId());
            String classCode = objectInfo.getString("classCode");
            String localName = objectInfo.getString("localName");
            if (commandSendResult.isSuccess()) {
                // 成功
                expireObjectIdMap.put(commandSendResult.getObjectId(), classCode);
                releaseInterventionDTOS.add(new ReleaseInterventionDTO(groupCode, dto.getProjectId(), commandSendResult.getObjectId(), classCode, commandSendResult.getInfoCode(), null, false));
                // 在保存此指令下发的记录
                EquipRecord equipRecord = new EquipRecord(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getSystemId(), sysClassCode, commandSendResult.getObjectId(),
                        new ChangeContent(classCode, dto.getExpireTime(), InfoCodeEnum.MANUAL_AUTO_SET.getCode(), EnumYesNo.YES.getNum().toString()),
                        ContextUtil.getUserId(), DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN));
                addEquipRecordList.add(equipRecord);
            } else {
                failure++;
                stringBuilder = stringBuilder.length() == 0 ? stringBuilder.append((single ? LocaleMessageUtil.getMessage("key_021b875c7964464b9c6c12e6a8746563") : LocaleMessageUtil.getMessage("key_59dd71f9572844cabe231dd60c633f65")) + localName) : stringBuilder.append(";" + localName);
                errorCountObject.add(JSONObject.parseObject(objectInfo.toJSONString(), DtpRwdObjectDTO.class));
            }
        }

        // 添加手自动切换日志
        String reason = dto.getOperateType() == null ? "" : dto.getOperateType().getDesc();
        try {
            if (StrUtil.isNotBlank(systemId)) {
                // 一键手自动切换日志
                this.runningMonitorLogHandler.saveManualAutoSetRunningLog(groupCode, dto.getProjectId(), systemId, dto.getModel(), RunningLogTypeEnum.MANUAL, dto.getExpireTime(), reason, ContextUtil.getUserName());
            } else {
                this.runningMonitorLogHandler.saveBatchManualAutoSetRunningLog(groupCode, dto, objectList, RunningLogTypeEnum.MANUAL);
            }
        } catch (Exception e) {
            log.error("手自动切换运行日志记录失败！error:{}", ExceptionUtils.getStackTrace(e));
        }
        ExecSend sendResult = new ExecSend(auto, failure, stringBuilder);
        sendResult.setAllCount(objectList.size());
        sendResult.setErrorCountObject(errorCountObject);
        sendResult.setNoControlObject(noControlObject);
        sendResult.setSuccessCount(0);
        // 4.设置缓存信息
        if (expireObjectIdMap.size() > 0) {
            // 将需要自动置为自动状态的对象、或者新增的手动对象，添加或删除缓存
            if (auto) {
                this.infoCodeLinkControlHandler.resetObjectInfoCodeForcedValue(expireObjectIdMap);
                // 成功切换为自动时，取消该设备下，所有点位的人工干预信息，并推送消息
                this.equipRecordService.deleteEquipRecordBySourceFrom(groupCode, dto.getProjectId(), expireObjectIdMap.keySet(), null, SourceFromEnum.RELEASE_INTERVENTION);
                this.equipRecordService.deleteEquipRecord(addEquipRecordList, sysClassCode, dto.getProjectId(), Arrays.asList(InfoCodeEnum.MANUAL_AUTO_SET.getCode()), null, null, null);
                this.sseEmitterEdgeServer.releaseInterventions(releaseInterventionDTOS);
            } else {
                this.equipRecordService.saveOrUpdateRecord(addEquipRecordList, sysClassCode, dto.getProjectId(), Arrays.asList(InfoCodeEnum.MANUAL_AUTO_SET.getCode()));
                releaseInterventionDTOS.forEach(releaseInterventionDTO -> releaseInterventionDTO.setExpireTime(dto.getExpireTime()));
                this.sseEmitterEdgeServer.releaseInterventions(releaseInterventionDTOS);
            }
            sendResult.setSuccessCount(expireObjectIdMap.size());
        }
        return sendResult;
    }

    @NotNull
    private List<JSONObject> getObjectInfo(String groupCode, RunningModeDTO dto, Set<String> objectIds) {
        List<String> equipmentIds = CollectionUtil.isEmpty(objectIds) ? null : objectIds.stream().filter(item -> !item.startsWith("Sy")).distinct().collect(Collectors.toList());
        List<JSONObject> objectList = CollectionUtil.isEmpty(equipmentIds) ? null : this.bizDtpRwdService.queryObjectList(groupCode, dto.getProjectId(), equipmentIds);
        if (CollectionUtil.isEmpty(objectList)) {
            log.warn("中台未交付此对象信息，对象ID集合：{}", objectIds);
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_c534a161540b4a70ab1e12bb88ab8fea"));
        }
        return objectList;
    }

    /**
     * 获取所有的风机、水阀设备对象信息
     *
     * @param groupCode      集团编码
     * @param projectId      项目ID
     * @param coolingTowerId 冷却塔控制单元对象ID
     * @return 关系对象
     */
    @Override
    public List<JSONObject> queryFanAndWaterValveListByCoolingTowerId(String groupCode, String projectId, String systemId, String coolingTowerId) {
        List<JSONObject> result = new ArrayList<>();

        // 获取所有的冷却塔风机、电动通断水阀
        List<JSONObject> relationObjects = this.queryRelationObjectList(groupCode, projectId, Lists.newArrayList(coolingTowerId), Lists.newArrayList("ACCCOF", "ACVVEO"),
                "CombineSubset", "Eg2Eq", CommonConst.FROM_RELATION);
        if (CollectionUtil.isEmpty(relationObjects)) {
            return result;
        }
        JSONObject fan = new JSONObject();
        JSONArray fanList = new JSONArray();
        fan.put("classCode", "ACCCOF");
        fan.put("className", this.localRedisCacheHandler.getClassName("ACCCOF"));
        fan.put("children", fanList);

        JSONObject water = new JSONObject();
        JSONArray waterList = new JSONArray();
        water.put("classCode", "ACVVEO");
        water.put("className", this.localRedisCacheHandler.getClassName("ACVVEO"));
        water.put("children", waterList);

        List<ObjectsExistRule> objectsExistRules = this.abstractCommonServer.queryObjectsExistRule(groupCode, projectId, Lists.newArrayList(new ClassCode("ACCCOF"), new ClassCode("ACVVEO")));
        Map<String, Set<String>> object_infoCode = CollectionUtil.isEmpty(objectsExistRules) ? new HashMap<>()
                : objectsExistRules.stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId, Collectors.mapping(ObjectsExistRule::getInfoCode, Collectors.toSet())));
        relationObjects.sort(Comparator.comparing(item -> JSONObject.parseObject(item.toString()).getString("localId"), Comparator.nullsLast(String::compareTo)));
        for (JSONObject relationObject : relationObjects) {
            String objectId = relationObject.getString("id");
            String classCode = relationObject.getString("classCode");
            if ("ACCCOF".equals(classCode)) {
                fanList.add(relationObject);
            } else {
                waterList.add(relationObject);
            }
            if (!object_infoCode.containsKey(objectId) || CollectionUtil.isEmpty(object_infoCode.get(objectId))) {
                continue;
            }
            Set<String> infoCodes = object_infoCode.get(objectId);

            // 填充IOT数据
            List<RcInfoCodeCfg> infoCodeConfigs = this.localRedisCacheHandler.getInfoCodeConfigs(classCode);
            if (CollectionUtil.isNotEmpty(infoCodeConfigs)) {
                for (RcInfoCodeCfg infoCodeCfg : infoCodeConfigs) {
                    if (infoCodeCfg.isDynamicParameter() && infoCodes.contains(infoCodeCfg.getInfoCode())) {
                        FeedbackInfoMsgMqttVO infoMsgMqttVO = iotDataCacheServerEdge.queryIotRealDataVO(groupCode, projectId, objectId, infoCodeCfg);
                        this.iotDataCacheServerEdge.setDataValueAndTime(infoMsgMqttVO, infoCodeCfg, null);
                        relationObject.put(infoCodeCfg.getInfoCode(), infoCodeCfg);

                        if (infoCodeCfg.getExtendInfo() != null && infoCodeCfg.getExtendInfo().getTriggerFormulaInfo() != null
                                && StrUtil.isNotBlank(infoCodeCfg.getExtendInfo().getTriggerFormulaInfo().getTriggerInfoCode())) {
                            RcInfoCodeCfg virtualCfg = this.localRedisCacheHandler.getInfoCodeConfig(classCode, infoCodeCfg.getExtendInfo().getTriggerFormulaInfo().getTriggerInfoCode());
                            if (virtualCfg != null) {
                                FeedbackInfoMsgMqttVO extraVo = iotDataCacheServerEdge.queryIotRealDataVO(groupCode, projectId, objectId, virtualCfg);
                                this.iotDataCacheServerEdge.setDataValueAndTime(extraVo, virtualCfg, null);
                                relationObject.put(virtualCfg.getInfoCode(), virtualCfg);
                            }
                        }
                    }
                }
            }
        }
        result.add(fan);
        result.add(water);
        return result;
    }

    @Override
    public List<RcInfoCodeCfg> changeLinkForceControlStatus(String groupCode, String projectId, String systemId, String objectId, String infoCode, String model) {
        boolean force = "1".endsWith(model);

        List<JSONObject> objectList = bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectId));
        if (CollectionUtil.isEmpty(objectList)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_30c2275801254b309814db8f52882f20"));
        }
        JSONObject objectInfo = objectList.get(0);
        String classCode = objectInfo.getString("classCode");

        // 判断是否存在强控信息点
        RcInfoCodeCfg rcInfoCodeCfg = this.localRedisCacheHandler.getInfoCodeConfig(classCode, infoCode);
        List<ForceInfoCodeInfo> forceInfoCodeInfos = rcInfoCodeCfg == null ? null : rcInfoCodeCfg.getExtendInfo() == null ? null : rcInfoCodeCfg.getExtendInfo().getForceInfoCodeInfos();
        if (CollectionUtil.isEmpty(forceInfoCodeInfos)) {
            return Lists.newArrayList();
        }
        // 获取此强控信息集所对应的所有设备信息
        Map<String, Map<String, List<RcInfoCodeCfg>>> classObjectForceInfoCodes = this.getClassObjectForceInfoCodeValue(groupCode, projectId, objectId, classCode, forceInfoCodeInfos, !force);

        if (force) {
            // 如果需要置为强控模式，直接返回所有的强控信息点信息，如果与主设备非同一设备类，不返回，走单独的接口
            List<RcInfoCodeCfg> rcInfoCodeCfgs = new ArrayList<>();
            this.parseObjectForceInfoCodeMap(groupCode, projectId, rcInfoCodeCfgs, classObjectForceInfoCodes, systemId, false);
            this.iotDataCacheServerEdge.setStringCacheData(RedisKey.buildObjectInfoCodeForcedKey(objectId, infoCode), model);
            // 当切换为单点控制的时候，同步取消掉扩展信息点中的信息点缓存值
            this.infoCodeLinkControlHandler.resetObjectTriggerInfoCodeValue(systemId, objectId, classCode, infoCode);
            return rcInfoCodeCfgs;
        }
        // 如果需要置为联动模式，需要将强控信息点置为非强控模式
        if ("ACOTU".equals(classCode)) {
            // 冷却塔控制单元的不进行风机与阀门的强控模式重置
            classObjectForceInfoCodes.clear();
            rcInfoCodeCfg.setObjectId(objectId);
            classObjectForceInfoCodes.put(classCode, MapUtil.of(infoCode, Lists.newArrayList(rcInfoCodeCfg)));
        }
        this.parseObjectForceInfoCodeMap(groupCode, projectId, null, classObjectForceInfoCodes, systemId, true);
        FeedbackInfoMsgMqttVO infoMsgMqttVO = iotDataCacheServerEdge.queryIotRealDataVO(groupCode, projectId, classCode, objectId, infoCode);
        this.iotDataCacheServerEdge.setDataValueAndTime(infoMsgMqttVO, rcInfoCodeCfg, null);
        this.iotDataCacheServerEdge.setStringCacheData(RedisKey.buildObjectInfoCodeForcedKey(objectId, infoCode), model);
        return Lists.newArrayList(rcInfoCodeCfg);
    }

    @Override
    public List<JSONObject> queryClassPositionCodeConfigs(String groupCode, String projectId, String objectId) {
        List<JSONObject> resultList = new ArrayList<>();

        // 1.检验设备是否存在
        List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectId));
        String classCode = CollectionUtil.isEmpty(objectList) ? null : objectList.get(0).getString("classCode");
        if (StrUtil.isBlank(classCode)) {
            return resultList;
        }

        // 2.获取该设备类下的设备半剖图配置
        List<ClassPositionCodeConfig> classPositionCodeConfigs = this.classPositionCodeConfigService.queryClassPositionCodeConfigsByClassCode(groupCode, projectId, classCode);
        if (CollectionUtil.isEmpty(classPositionCodeConfigs)) {
            return resultList;
        }
        Map<String, List<ClassPositionCodeConfig>> group_infoCodes = classPositionCodeConfigs.stream().collect(Collectors.groupingBy(ClassPositionCodeConfig::getPositionName));
        for (Map.Entry<String, List<ClassPositionCodeConfig>> listEntry : group_infoCodes.entrySet()) {
            List<ClassPositionCodeConfig> entryValue = listEntry.getValue();
            if (CollectionUtil.isEmpty(entryValue)) {
                continue;
            }
            JSONObject result = new JSONObject();
            result.put("groupName", listEntry.getKey());

            List<RcInfoCodeCfg> allInfoCodes = new ArrayList<>();
            for (ClassPositionCodeConfig positionCodeConfig : entryValue) {
                RcInfoCodeCfg infoCodeConfig = this.localRedisCacheHandler.getInfoCodeConfig(classCode, positionCodeConfig.getInfoCode());
                if (infoCodeConfig != null) {
                    infoCodeConfig.setDataValue(this.iotDataCacheServerEdge.queryIotRealData(groupCode, projectId, objectId, infoCodeConfig));
                }
            }
            allInfoCodes.sort(Comparator.comparing(RcInfoCodeCfg::getOrdered, Comparator.nullsFirst(Integer::compareTo)));
            result.put("infoCodes", allInfoCodes);
            resultList.add(result);
        }
        return resultList;
    }

    /**
     * 解析 强控信息点
     *
     * @param rcInfoCodeCfgs
     * @param classObjectForceInfoCodes
     * @param systemId
     * @param sendCommand
     */
    private void parseObjectForceInfoCodeMap(String groupCode, String projectId, List<RcInfoCodeCfg> rcInfoCodeCfgs, Map<String, Map<String, List<RcInfoCodeCfg>>> classObjectForceInfoCodes, String systemId, boolean sendCommand) {
        Map<String, Set<String>> object_infoCode = null;
        if (sendCommand) {
            List<ClassCode> classCodeList = new ArrayList<>();
            Set<String> classCodes = classObjectForceInfoCodes.keySet();
            classCodes.forEach(classCode -> classCodeList.add(new ClassCode(classCode)));
            List<ObjectsExistRule> objectsExistRules = this.abstractCommonServer.queryObjectsExistRule(groupCode, projectId, classCodeList);
            if (CollectionUtil.isEmpty(objectsExistRules)) {
                return;
            }
            object_infoCode = objectsExistRules.stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId, Collectors.mapping(ObjectsExistRule::getInfoCode, Collectors.toSet())));
        }

        for (Map.Entry<String, Map<String, List<RcInfoCodeCfg>>> classCode_objectInfoCode : classObjectForceInfoCodes.entrySet()) {
            if (classCode_objectInfoCode == null) {
                continue;
            }
            Map<String, List<RcInfoCodeCfg>> infoCode_cfgMap = classCode_objectInfoCode.getValue();
            if (infoCode_cfgMap == null) {
                continue;
            }
            for (Map.Entry<String, List<RcInfoCodeCfg>> infoCode_cfg : infoCode_cfgMap.entrySet()) {
                if (infoCode_cfg == null) {
                    continue;
                }
                List<RcInfoCodeCfg> infoCodeCfgs = infoCode_cfg.getValue();
                if (CollectionUtil.isEmpty(infoCodeCfgs)) {
                    continue;
                }
                for (RcInfoCodeCfg cfg : infoCodeCfgs) {
                    if (sendCommand) {
                        try {
                            Set<String> infoCodes = object_infoCode.get(cfg.getObjectId());
                            if (CollectionUtil.isEmpty(infoCodes) || !infoCodes.contains(cfg.getInfoCode())) {
                                continue;
                            }
                            this.dtpTwinClientService.manualSend(DtpTwinManualSend.builder().objectId(cfg.getObjectId()).infoCode(cfg.getInfoCode()).value(CommonConst.FORCE_VALUE).build());
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("强控模式重置失败，对象ID：{}", cfg.getObjectId(), e);
                            throw new BusinessException("Z9999", LocaleMessageUtil.getMessage("key_3d252ba8a1134b559e7dd4caebef640a"));
                        }
                    } else {
                        rcInfoCodeCfgs.add(cfg);
                    }
                }
            }
        }
    }

    /**
     * 获取强控信息点的详细信息
     *
     * @param masterObjectId
     * @param masterClassCode
     * @param forceInfoCodeInfo
     * @return
     */
    @Override
    public JSONArray getAvailableForceInfo(String groupCode, String projectId, String masterObjectId, String masterClassCode, List<ForceInfoCodeInfo> forceInfoCodeInfo) {
        if (CollectionUtil.isEmpty(forceInfoCodeInfo)) {
            return new JSONArray();
        }

        // 获取此强控信息集所对应的所有设备信息
        Map<String, Map<String, List<RcInfoCodeCfg>>> classObjectForceInfoCodes = this.getClassObjectForceInfoCodeValue(groupCode, projectId, masterObjectId, masterClassCode, forceInfoCodeInfo, false);

        List<RcInfoCodeCfg> rcInfoCodeCfgs = new ArrayList<>();
        this.parseObjectForceInfoCodeMap(groupCode, projectId, rcInfoCodeCfgs, classObjectForceInfoCodes, null, false);
        return JSONArray.parseArray(JSONArray.toJSONString(rcInfoCodeCfgs));
    }

    @Override
    public ExecSend changeManualAutoStatusByObjectIds(String groupCode, RunningModeDTO dto, boolean throwException) {

        if (StrUtil.isNotBlank(dto.getExpireTime())) {
            DateTime dateTime = DateUtil.parse(dto.getExpireTime(), "yyyyMMddHHmmss");
            dto.setExpireTime(dateTime.setField(DateField.SECOND, 0).toString("yyyyMMddHHmmss"));
        }
        if (StringUtils.equals(dto.getObjectType(), CommonConst.space)) {
            Collection<String> filterSpaceIds;
            if (StringUtils.isNotBlank(dto.getObjectId())) {
                filterSpaceIds = Lists.newArrayList(dto.getObjectId());
            } else {
                filterSpaceIds = dto.getObjectIds();
            }
            if (CollectionUtils.isEmpty(filterSpaceIds)) {
                return new ExecSend(false, filterSpaceIds.size(), new StringBuilder());
            }
            // 空间：
            List<DtpRwdCircuitDTO> dtpRwdCircuitDTOS = demandLightingStrategy.listCircuitsBySpaceIds(
                    dto.getProjectId(), dto.getTemplateType(),
                    filterSpaceIds, null, null, null);

            if (CollectionUtils.isEmpty(dtpRwdCircuitDTOS)) {
                ExecSend sendResult = new ExecSend(true, 200, new StringBuilder(""));
                return sendResult;
            }
            Set<String> objectIds = new HashSet<>();
            for (DtpRwdCircuitDTO dtpRwdObjectDTO : dtpRwdCircuitDTOS) {
                objectIds.add(dtpRwdObjectDTO.getId());
            }
            dto.setObjectIds(objectIds);
        }
        Set<String> objectIds = CollectionUtil.isEmpty(dto.getObjectIds()) ? new HashSet<>() : dto.getObjectIds();
        if (StrUtil.isNotBlank(dto.getObjectId())) {
            objectIds.add(dto.getObjectId());
        }

        // 1.获取设备对应的对象信息
        List<JSONObject> objectList = getObjectInfo(groupCode, dto, objectIds);

        // 2.获取绑定规则
        Map<String, Set<String>> object_infoCode = getBindRule(groupCode, dto.getProjectId(), objectList);

        // 3.根据绑点规则进行指令下发
        long currentTimeMillis = System.currentTimeMillis();
        ExecSend sendResult = getManualAutoStatusSendResult(groupCode, dto, objectList, object_infoCode, "", null);
        log.info("手自动状态切换总耗时：{}ms", (System.currentTimeMillis() - currentTimeMillis));

        // 4.新增一条页面播报日志
        // addPlayLogs(dto, InfoCodeEnum.MANUAL_AUTO_SET.getCode(), objectIds, objectList, sendResult.isAuto(), this.runningMonitorLogHandler);

        if (sendResult.getStringBuilder() != null && sendResult.getStringBuilder().length() > 0 && throwException) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), sendResult.getStringBuilder().toString());
        }
        return sendResult;
    }

    @Override
    public List<JSONObject> listAccRuntimeClass(String groupCode, String projectId, String systemId, String systemClassCode) {
        List<JSONObject> resultList = new ArrayList<>();

        // 1.获取该系统下的泵组（包含非末级的泵组）
        /*
        List<DtpRwdFreezePumpDTO> accpcGroups = this.dtpRwdClientService.listFreezePumpGroup(false, false,
                projectId, Sets.newHashSet(systemId), null, null, true);
        Set<String> pumpGroupObjectIds = CollectionUtil.isEmpty(accpcGroups) ? new HashSet<>() : accpcGroups.stream().map(item -> item.getId()).collect(Collectors.toSet());

         */

        // 2.获取对应的壳配置数据
       Set<String> groups = null;
        Set<String> pumpGroupObjectIds = null;
        if (DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue().equals(systemClassCode)) {
            List<DtpRwdEquipmentDTO> groupsInSystem = this.dtpRwdClientService.listEquipmentGroupInBySystemIds(
                    Sets.newHashSet(systemId), new DtpRwdObjectQueryParam()
                            .setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.ACHPC_GROUP,
                                    DtpRwdClassCodeEnum.ACPPC_GROUP))
                            .addWithColumn(DtpRwdObjectDTO.Fields.id));
            pumpGroupObjectIds = groupsInSystem.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_CONTROL_AIR_HOT.getCode(),
                    ShellConfigGroupEnum.GROUP_CONTROL_HOT_PUMP_GROUP.getCode(),
                    ShellConfigGroupEnum.GROUP_CONTROL_COOL_HOT_PUMP.getCode());
        }else if (DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue().equals(systemClassCode)){
            List<DtpRwdEquipmentDTO> groupsInSystem = this.dtpRwdClientService.listEquipmentGroupInBySystemIds(
                    Sets.newHashSet(systemId), new DtpRwdObjectQueryParam()
                            .setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.ACCPC_GROUP,DtpRwdClassCodeEnum.ACOPC_GROUP,
                                    DtpRwdClassCodeEnum.ACOTC_GROUP,DtpRwdClassCodeEnum.ACCGP_GROUP,
                                    DtpRwdClassCodeEnum.ACCST_GROUP, DtpRwdClassCodeEnum.ACPPC_GROUP))
                            .addWithColumn(DtpRwdObjectDTO.Fields.id));
            pumpGroupObjectIds = groupsInSystem.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_CONTROL_AIR_COOLING.getCode(),
                    ShellConfigGroupEnum.GROUP_CONTROL_COOL_PUMP_GROUP.getCode(),
                    ShellConfigGroupEnum.GROUP_CONTROL_CHIL_PUMP_GROUP.getCode(),
                    ShellConfigGroupEnum.GROUP_CONTROL_COOL_TOWER_GROUP.getCode(),
                    ShellConfigGroupEnum.GROUP_CONTROL_CGLY_PUMP_GROUP.getCode(),
                    ShellConfigGroupEnum.GROUP_CONTROL_COOL_HSLO_GROUP.getCode(),
                    ShellConfigGroupEnum.GROUP_CONTROL_COOL_HOT_PUMP.getCode());
        }else{
            return resultList;
        }
        List<ShellConfigVO> shellConfigVOS = this.abstractCommonServer.queryShellConfigByScene(groupCode, projectId, ShellConfigSceneEnum.SCENE_CONTROL.getCode(), groups);
        if (CollectionUtil.isEmpty(shellConfigVOS)) {
            return resultList;
        }

        // 3.根据壳配置，返回对应的设备类及其所属对象
        for (ShellConfigVO shellConfigVO : shellConfigVOS) {
            List<ShellConfigGroup> shellConfigGroups = shellConfigVO.getGroups();
            if (CollectionUtil.isEmpty(shellConfigGroups)) {
                continue;
            }
            for (ShellConfigGroup shellConfigGroup : shellConfigGroups) {
                List<ShellConfigItem> items = shellConfigGroup.getItems();
                if (CollectionUtil.isEmpty(items)) {
                    continue;
                }
                for (ShellConfigItem item : items) {
                    boolean pump = pumpGroupObjectIds.contains(item.getObjectId());
                    String value = StrUtil.isBlank(item.getValue()) ? item.getDefaultValues() : item.getValue();
                    if ("autoCtrlAccRunTime".equals(value) && (systemId.equals(item.getObjectId()) || pump)) {
                        String classCode = item.getCode().substring(item.getCode().lastIndexOf("_") + 1);
                        JSONObject resultObject = new JSONObject();
                        resultObject.put("objectId", item.getObjectId());
                        resultObject.put("classCode", classCode);
                        resultObject.put("className", pump ? item.getObjectName() : item.getName());
                        resultList.add(resultObject);
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    public void clearAccRuntimeClass(String groupCode, String projectId, String objectId, String classCode) {
        List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectId));
        if (CollectionUtil.isEmpty(objectList)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_30c2275801254b309814db8f52882f20"));
        }
        String objType = objectList.get(0).getString("objType");
        if (RunningLogObjectCategpryEnum.SYSTEM.getCode().equals(objType)) {
            this.dtpTwinClientService.resetEquipInfoCodeInSystem(objectId, classCode, "autoCtrlAccRunTime");
        } else if ("equipGroup".equals(objType)) {
            this.dtpTwinClientService.resetEquipInfoCodeInGroup(objectId, classCode, "autoCtrlAccRunTime");
        }
    }

    /**
     * 获取强控信息点配置，实际对应的设备ID，及其对应的IOT数据值
     * <p>
     * 2023.09.06 目前冷却塔控制单元的强控，走单独的接口，不在独立返回，所以其余的都是同一设备
     *
     * @param groupCode
     * @param projectId
     * @param masterObjectId
     * @param masterClassCode
     * @param forceInfoCodeInfos 强控信息点
     * @param queryRealtion      true 如果非同一设备吗，会查询出来，false-非同一设备的不在处理了
     * @return key->classCode, value-> key: infoCode，value -> iotVOs 一个 object 一个
     */
    public Map<String, Map<String, List<RcInfoCodeCfg>>> getClassObjectForceInfoCodeValue(String groupCode, String projectId, String masterObjectId, String masterClassCode, List<ForceInfoCodeInfo> forceInfoCodeInfos, boolean queryRealtion) {
        Map<String, Map<String, List<RcInfoCodeCfg>>> result = new HashMap<>();
        if (CollectionUtil.isEmpty(forceInfoCodeInfos)) {
            return result;
        }
        // opt B10（switch.opt.forceInfoBatch）：相同关系只查一次；各点的 Twin 当前值先登记，循环结束后一次查完再回填
        boolean batch = OptSwitch.on(OptSwitch.FORCE_INFO_BATCH) && projectCfg != null;
        OptPointBatch<RcInfoCodeCfg[]> points = batch ? new OptPointBatch<>() : null;
        OptCallMemo<String, List<JSONObject>> relationMemo = OptCallMemo.create("forceInfoRelation", OptSwitch.FORCE_INFO_BATCH);
        for (ForceInfoCodeInfo forceInfoCodeInfo : forceInfoCodeInfos) {
            List<JSONObject> relationObjects = null;
            RcInfoCodeCfg rcInfoCodeCfg = this.localRedisCacheHandler.getInfoCodeConfig(forceInfoCodeInfo.getClassCode(), forceInfoCodeInfo.getInfoCode());
            if (rcInfoCodeCfg == null) {
                continue;
            }

            if (!result.containsKey(forceInfoCodeInfo.getClassCode())) {
                result.put(forceInfoCodeInfo.getClassCode(), new HashMap<>());
            }
            if (!result.get(forceInfoCodeInfo.getClassCode()).containsKey(forceInfoCodeInfo.getInfoCode())) {
                result.get(forceInfoCodeInfo.getClassCode()).put(forceInfoCodeInfo.getInfoCode(), new ArrayList<>());
            }
            if (masterClassCode.equals(forceInfoCodeInfo.getClassCode())) {
                // 必为相同的对象
                RcInfoCodeCfg newCfg = new RcInfoCodeCfg();
                BeanUtil.copyProperties(rcInfoCodeCfg, newCfg);
                if (points != null) {
                    points.add(masterObjectId, rcInfoCodeCfg.getInfoCode(), new RcInfoCodeCfg[]{rcInfoCodeCfg, newCfg});
                } else {
                    FeedbackInfoMsgMqttVO infoMsgMqttVO = this.iotDataCacheServerEdge.queryIotRealDataVO(groupCode, projectId, masterObjectId, rcInfoCodeCfg);
                    this.iotDataCacheServerEdge.setDataValueAndTime(infoMsgMqttVO, newCfg, null);
                }
                newCfg.setObjectId(masterObjectId);
                result.get(forceInfoCodeInfo.getClassCode()).get(forceInfoCodeInfo.getInfoCode()).add(newCfg);
                continue;
            }
            // 需要根据关系查询
            if (queryRealtion) {
                relationObjects = relationMemo.get(forceInfoCodeInfo.getClassCode() + "|" + forceInfoCodeInfo.getGraphCode() + "|" + forceInfoCodeInfo.getRelCode() + "|" + forceInfoCodeInfo.getRelDirection(),
                        k -> this.queryRelationObjectList(groupCode, projectId, Lists.newArrayList(masterObjectId), Lists.newArrayList(forceInfoCodeInfo.getClassCode()), forceInfoCodeInfo.getGraphCode(), forceInfoCodeInfo.getRelCode(), forceInfoCodeInfo.getRelDirection()));
                if (CollectionUtil.isNotEmpty(relationObjects)) {
                    for (JSONObject relationObject : relationObjects) {
                        RcInfoCodeCfg newCfg = new RcInfoCodeCfg();
                        BeanUtil.copyProperties(rcInfoCodeCfg, newCfg);
                        String relationObjectId = relationObject.getString("id");
                        if (points != null) {
                            points.add(relationObjectId, rcInfoCodeCfg.getInfoCode(), new RcInfoCodeCfg[]{rcInfoCodeCfg, newCfg});
                        } else {
                            FeedbackInfoMsgMqttVO infoMsgMqttVO = this.iotDataCacheServerEdge.queryIotRealDataVO(groupCode, projectId, relationObjectId, rcInfoCodeCfg);
                            this.iotDataCacheServerEdge.setDataValueAndTime(infoMsgMqttVO, newCfg, null);
                        }
                        newCfg.setObjectId(relationObjectId);
                        result.get(forceInfoCodeInfo.getClassCode()).get(forceInfoCodeInfo.getInfoCode()).add(newCfg);
                    }
                }
            }
        }
        if (points != null) {
            String gc = projectCfg.getGroupCode();
            String pid = projectCfg.getProjectId();
            // 与 IotDataCacheServiceImpl#queryIotRealDataVO 相同：查 Twin 当前值 → 取有效记录 → 按配置覆盖值 → 写入 newCfg
            points.flush(pid,
                    dto -> DtpTwinIotData.getVaildIotDatas(gc, pid, this.dtpTwinClientService.queryTwinDataOnMoment(gc, pid, null, dto)),
                    FeedbackInfoMsgMqttVO::getObjectId, FeedbackInfoMsgMqttVO::getInfoCode,
                    (cfgs, vo) -> {
                        // 每个点用独立副本，避免同一记录被多次覆盖值
                        FeedbackInfoMsgMqttVO copy = vo == null ? null : BeanUtil.toBean(vo, FeedbackInfoMsgMqttVO.class);
                        IotDataUtil.overrideDataValue(copy, cfgs[0]);
                        this.iotDataCacheServerEdge.setDataValueAndTime(copy, cfgs[1], null);
                    });
        }
        return result;
    }

    /**
     * 获取关系对象信息
     *
     * @param groupCode
     * @param projectId
     * @param objectIds    与 classCodes 分列两端
     * @param classCodes   与 objectIds 分列两端
     * @param graphCode
     * @param relCode
     * @param relDirection from-主，即objectId是主对象的ID， to-从
     * @return
     */
    private List<JSONObject> queryRelationObjectList(String groupCode, String projectId, List<String> objectIds, List<String> classCodes, String graphCode, String relCode, String relDirection) {
        // 获取关系对象详情
        List<JSONObject> relationObjects = null;
        if (CommonConst.FROM_RELATION.equals(relDirection)) {
            relationObjects = this.bizDtpRwdService.queryObjectList(groupCode, projectId, classCodes, graphCode, relCode, objectIds, null);
        } else {
            relationObjects = this.bizDtpRwdService.queryObjectList(groupCode, projectId, classCodes, graphCode, relCode, null, objectIds);
        }
        return relationObjects;
    }

}
