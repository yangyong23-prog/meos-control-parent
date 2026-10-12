package com.persagy.apccontrol.basic.service.common.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.controller.shellconfig.param.ShellConfigVO;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.dto.BatchObjectInfoCodeConfigDTO;
import com.persagy.apccontrol.basic.model.dto.ObjectInfoCodeConfigDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.ShellConfigGroup;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigGroupEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigSceneEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.lighting.LightingInstanceStatusQueryParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdTwinControlObjectVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.common.RunningMonitorService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import com.persagy.validate.utility.pattern.DatePattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运行监测服务实现
 *
 * @author JiYinchuan
 * @since 5.241202.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings({"rawtypes", "SpringJavaInjectionPointsAutowiringInspection"})
@Validated
public class RunningMonitorServiceImpl implements RunningMonitorService, LogComponent {

    private final InfoCodeConverter infoCodeConverter;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final DemandLightingStrategy demandLightingStrategy;
    private final BizDtpRwdService bizDtpRwdService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractCommonServer abstractCommonServer;

    @Override
    public JSONObject countSpaceRelatedEquipManualAutoStatus(LightingInstanceStatusQueryParam instanceParam) {
        String projectId = instanceParam.getProjectId();
        DemandTemplateTypeEnum templateType = instanceParam.getDemandTemplateType();
        String instanceId = instanceParam.getInstanceId();
        List<String> controllerIds = new ArrayList<>();
        // 查询的
        Set<String> objectIdList = new HashSet<>();
        List<DtpRwdCircuitDTO> dtpRwdCircuitList = Lists.newArrayList();
        if (instanceParam.getInstanceType().equals(InstanceTypeEnum.EQUIPMENT)) {
            controllerIds.add(instanceId);
            // 查询照明控制模块下的照明回路
            Map<String, Set<DtpRwdCircuitDTO>> dtpRwdCircuitVOSetMap = demandLightingStrategy.mapControllerIdAndCircuits(projectId,
                    templateType, controllerIds, null);
            if (MapUtil.isNotEmpty(dtpRwdCircuitVOSetMap)) {
                Set<DtpRwdCircuitDTO> dtpRwdCircuits = dtpRwdCircuitVOSetMap.get(instanceId);
                if (CollectionUtils.isNotEmpty(dtpRwdCircuits)) {
                    dtpRwdCircuitList.addAll(dtpRwdCircuits);
                    for (DtpRwdCircuitDTO dtpRwdCircuit : dtpRwdCircuits) {
                        objectIdList.add(dtpRwdCircuit.getId());
                    }
                }
            }
        } else {
            // 照明回路
            dtpRwdCircuitList = demandLightingStrategy.listCircuitsByInstance(
                    projectId,
                    templateType,
                    DemandInstanceTypeEnum.getByCode(instanceParam.getInstanceType().getValue()),
                    instanceId);
            for (DtpRwdCircuitDTO dtpRwdCircuitDTO : dtpRwdCircuitList) {
                objectIdList.add(dtpRwdCircuitDTO.getId());
            }
        }
        Set<String> allClassCodes = dtpRwdCircuitList.stream().map(DtpRwdCircuitDTO::getClassCode).collect(Collectors.toSet());
        Map<String, DtpRwdTwinControlObjectVO> dtpRwdTwinControlObjectVOMap = bizDtpRwdService.queryControlObjects(allClassCodes);
        JSONObject result = new JSONObject();
        Integer manualStatusCount = 0;
        Integer autoStatusCount = 0;
        if (CollectionUtils.isEmpty(objectIdList)) {
            result.put("manualStatusCount", manualStatusCount);
            result.put("autoStatusCount", autoStatusCount);
            return result;
        }

        // 统计手动状态和自动状态的数量
        for (String objId : objectIdList) {
            // 控制对象过滤
            Boolean booleanControlObject = bizDtpRwdService.booleanControlObject(objId, InfoCodeEnum.MANUAL_AUTO_STATUS.getCode(), dtpRwdTwinControlObjectVOMap);
            if (!booleanControlObject) {
                continue;
            }
            // 查询设备的手自动状态
            String manualAutoStatus = this.abstractIotDataCacheServer.queryIotRealData(null, null, objId, null, InfoCodeEnum.MANUAL_AUTO_STATUS.getCode());
            if (StringUtils.isBlank(manualAutoStatus)) {
                continue;
            }
            if ("0".equals(manualAutoStatus)) {
                manualStatusCount = manualStatusCount + 1;
            } else {
                autoStatusCount = autoStatusCount + 1;
            }
        }
        result.put("manualStatusCount", manualStatusCount);
        result.put("autoStatusCount", autoStatusCount);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<InfoCodeValueVO> batchQueryInfoCode(@NonNull @Valid BatchObjectInfoCodeConfigDTO param) {
        param.checkAndSet();
        boolean queryInfoCodeRule = BooleanUtils.isTrue(param.getQueryInfoCodeRule());
        boolean queryInfoCodeDefine = BooleanUtils.isTrue(param.getQueryInfoCodeDefine());
        Set<String> queryInfoCodeRecoverTime = param.getQueryInfoCodeRecoverTime();
        List<ObjectInfoCodeConfigDTO> objects = param.getObject();
        if (CollectionUtils.isEmpty(objects)) {
            return Lists.newArrayList();
        }
        Set<String> allObjectIds = objects.stream()
                .map(ObjectInfoCodeConfigDTO::getObjectId)
                .collect(Collectors.toSet());
        Set<String> allInfoCodes = objects.stream()
                .flatMap(obj -> obj.getInfoCodes().stream())
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(allObjectIds) || CollectionUtils.isEmpty(allInfoCodes)) {
            return Lists.newArrayList();
        }
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValue(
                ContextUtil.getProjectId(), null, allObjectIds, allInfoCodes);
        if (queryInfoCodeRule) {
            infoCodeValues = queryInfoCodeRule(param, infoCodeValues);
        }
        if (queryInfoCodeDefine) {
            infoCodeValues = queryInfoCodeDefine(param, infoCodeValues);
        }
        if (CollectionUtil.isNotEmpty(queryInfoCodeRecoverTime)) {
            queryInfoCodeRecoverTime(infoCodeValues, queryInfoCodeRecoverTime);
        }
        // 若请求包含 rcRunStatus，从 Redis 中补充/覆盖开启中、关闭中等中间态值
        if (allInfoCodes.contains(InfoCodeEnum.RC_RUN_STATUS.getCode())) {
            this.fillRcRunStatusFromRedis(objects, infoCodeValues);
        }
        return infoCodeValues;
    }


    /**
     * 从 Redis 缓存中读取 rcRunStatus 中间态值并追加到结果集合
     *
     * <p>rcRunStatus 是自定义信息点，常规查询无法返回，需要逐对象调用
     * {@link AbstractIotDataCacheServer#queryIotRealDataRedisVO} 从 Redis 中直接获取。
     *
     * @param objects        请求对象列表
     * @param infoCodeValues 已查询到的信息点列表，查询结果直接追加到此集合
     * @since 7.260402.0
     */
    private void fillRcRunStatusFromRedis(
            @NonNull List<ObjectInfoCodeConfigDTO> objects,
            @NonNull List<InfoCodeValueVO> infoCodeValues) {
        String rcRunStatus = InfoCodeEnum.RC_RUN_STATUS.getCode();
        for (ObjectInfoCodeConfigDTO obj : objects) {
            String objectId = obj.getObjectId();
            Object redisVO = abstractIotDataCacheServer.queryIotRealDataRedisVO(objectId, rcRunStatus);
            if (redisVO == null) {
                continue;
            }
            InfoCodeValueVO vo = new InfoCodeValueVO()
                    .setObjectId(objectId)
                    .setInfoCode(rcRunStatus);
            if (redisVO instanceof FeedbackInfoMsgMqttVO) {
                FeedbackInfoMsgMqttVO feedbackVO = (FeedbackInfoMsgMqttVO) redisVO;
                vo.setInfoCodeValue(feedbackVO.getDataValue());
                vo.setDataValue(feedbackVO.getDataValue());
                vo.setDataTime(feedbackVO.toDataTime());
                vo.setDataIndate(feedbackVO.getDataIndate());
                vo.setDataIndicateTime(feedbackVO.toDataIndicateTime());
            }
            infoCodeValues.add(vo);
        }
    }

    /**
     * 查询信息点设定値的恢复时间
     * 该方法用于查询指定信息点的恢复时间，并将其添加到信息点值的附加属性中
     *
     * @param infoCodeValues           信息点值列表
     * @param queryInfoCodeRecoverTime 需要查询恢复时间的信息点编码列表
     * @since 7.250501.0
     */
    private void queryInfoCodeRecoverTime(List<InfoCodeValueVO> infoCodeValues, Set<String> queryInfoCodeRecoverTime) {
        if (CollectionUtils.isEmpty(infoCodeValues) || CollectionUtils.isEmpty(queryInfoCodeRecoverTime)) {
            return;
        }

        Set<String> allIotSetInfoCodes = new HashSet<>();
        Map<String, Set<String>> objectIdAndIotSetInfoCodes = new LinkedHashMap<>();
        for (InfoCodeValueVO aggregate : infoCodeValues) {
            String objectId = aggregate.getObjectId();
            String infoCode = aggregate.getInfoCode();
            if (queryInfoCodeRecoverTime.contains(infoCode)) {
                allIotSetInfoCodes.add(infoCode);
                MapExecutor.dynamicPut(objectIdAndIotSetInfoCodes, objectId, infoCode, HashSet::new);
            }
        }
        // 仅设定信息点才查干预截止时间
        if (CollectionUtils.isEmpty(allIotSetInfoCodes)) {
            return;
        }

        Set<String> objectIds = objectIdAndIotSetInfoCodes.keySet();
        Map<String, Map<String, LocalDateTime>> result = new LinkedHashMap<>();

        // 单独查询手自动设定恢复时间
        if (allIotSetInfoCodes.contains(InfoCodeEnum.MANUAL_AUTO_SET.getCode())) {
            Map<String, LocalDateTime> objectIdAndManualAutoSetRecoverTime =
                    abstractIotDataCacheServer.queryObjectIdAndManualAutoSetRecoverTime(objectIds);
            if (MapUtils.isNotEmpty(objectIdAndManualAutoSetRecoverTime)) {
                objectIdAndManualAutoSetRecoverTime.forEach((objectId, manualAutoSetRecoverTime) ->
                        MapExecutor.dynamicPut(result, objectId, InfoCodeEnum.MANUAL_AUTO_SET.getCode(),
                                manualAutoSetRecoverTime, HashMap::new));
            }
            allIotSetInfoCodes.remove(InfoCodeEnum.MANUAL_AUTO_SET.getCode());
        }

        // 查询非手自动设定外其他设定信息点恢复时间
        if (CollectionUtils.isNotEmpty(allIotSetInfoCodes)) {
            Map<String, Map<String, LocalDateTime>> objectIdIotSettingAndRecoverTime =
                    abstractIotDataCacheServer.queryObjectIdIotSettingAndRecoverTime(objectIds, allIotSetInfoCodes);
            if (MapUtils.isNotEmpty(objectIdIotSettingAndRecoverTime)) {
                objectIdIotSettingAndRecoverTime.forEach((objectId, iotSettingANdRecoverTime) -> {
                    iotSettingANdRecoverTime.forEach((infoCode, recoverTime) -> {
                        MapExecutor.dynamicPut(result, objectId, infoCode, recoverTime, HashMap::new);
                    });
                });
            }
        }

        if (MapUtils.isEmpty(result)) {
            return;
        }

        // 为信息点值添加恢复时间
        for (InfoCodeValueVO infoCodeValue : infoCodeValues) {
            Map<String, LocalDateTime> recoverTimeMap = result.get(infoCodeValue.getObjectId());
            if (MapUtils.isNotEmpty(recoverTimeMap)) {
                LocalDateTime recoverTime = recoverTimeMap.get(infoCodeValue.getInfoCode());
                if (Objects.nonNull(recoverTime)) {
                    Map<String, String> additionalMapValue = infoCodeValue.computeAdditionalMapValueIfAbsent();
                    additionalMapValue.put("recoveryDateTime", recoverTime.format(DatePattern.NORM_DATETIME_FORMATTER));
                    infoCodeValue.setAdditionalMapValue(additionalMapValue);
                }
            }
        }
    }

    /**
     * 查询信息点定义
     *
     * @param param          查询参数
     * @param infoCodeValues 信息点值
     * @return 信息点定义
     * @since 7.250501.0
     */
    private List<InfoCodeValueVO> queryInfoCodeDefine(
            @NonNull BatchObjectInfoCodeConfigDTO param,
            @NonNull List<InfoCodeValueVO> infoCodeValues) {
        List<ObjectInfoCodeConfigDTO> objects = param.getObject();
        if (CollectionUtils.isEmpty(objects)) {
            return Lists.newArrayList();
        }
        Set<String> classCodes = new HashSet<>();
        Map<String, String> idAndClassCode = objects.stream().collect(Collectors.toMap(
                ObjectInfoCodeConfigDTO::getClassCode, ObjectInfoCodeConfigDTO::getObjectId, (o1, o2) -> o2));
        for (ObjectInfoCodeConfigDTO object : objects) {
            classCodes.add(object.getClassCode());
            idAndClassCode.put(object.getObjectId(), object.getClassCode());
        }
        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCodeInfoCodeAndDefine =
                dtpKnowledgeClientService.mapClassCodeInfoCodeAndDefine(classCodes);
        if (MapUtils.isEmpty(classCodeInfoCodeAndDefine)) {
            return infoCodeValues;
        }
        List<InfoCodeValueVO> finalInfoCodeValues = new ArrayList<>();
        for (InfoCodeValueVO infoCodeValue : infoCodeValues) {
            String objectId = infoCodeValue.getObjectId();
            String infoCode = infoCodeValue.getInfoCode();

            String classCode = idAndClassCode.get(objectId);
            if (StringUtils.isBlank(classCode)) {
                continue;
            }
            Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndDefine = classCodeInfoCodeAndDefine.get(classCode);
            if (MapUtils.isEmpty(infoCodeAndDefine)) {
                continue;
            }
            DtpKnowledgeInfoCodeDTO infoCodeDefine = infoCodeAndDefine.get(infoCode);
            if (Objects.isNull(infoCodeDefine)) {
                continue;
            }
            finalInfoCodeValues.add(infoCodeConverter.fill(infoCodeValue, infoCodeDefine));
        }
        return finalInfoCodeValues;
    }

    /**
     * 查询信息点规则
     *
     * @param param          查询参数
     * @param infoCodeValues 信息点值
     * @return 查询结果
     * @since 7.250501.0
     */
    @SuppressWarnings("unchecked")
    private List<InfoCodeValueVO> queryInfoCodeRule(
            @NonNull BatchObjectInfoCodeConfigDTO param,
            @NonNull List<InfoCodeValueVO> infoCodeValues) {
        List<ObjectInfoCodeConfigDTO> objects = param.getObject();
        if (CollectionUtils.isEmpty(objects)) {
            return Lists.newArrayList();
        }
        Set<String> allInfoCodes = objects.stream()
                .flatMap(obj -> obj.getInfoCodes().stream())
                .collect(Collectors.toSet());

        MultiKeyMap<String, InfoCodeValueVO> objectIdInfoCodeAndVo = new MultiKeyMap<>();
        for (InfoCodeValueVO infoCodeValue : infoCodeValues) {
            MultiKey<String> key = new MultiKey<>(infoCodeValue.getObjectId(), infoCodeValue.getInfoCode());
            objectIdInfoCodeAndVo.put(key, infoCodeValue);
        }
        Map<String, Set<String>> classCodeAndObjectIds = objects.stream().collect(Collectors.groupingBy(
                ObjectInfoCodeConfigDTO::getClassCode, Collectors.mapping(
                        ObjectInfoCodeConfigDTO::getObjectId, Collectors.toSet())));
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = abstractIotDataCacheServer
                .mapObjectIdAndExistsRuleInfoCodes(classCodeAndObjectIds, allInfoCodes);
        List<InfoCodeValueVO> finalInfoCodeValues = new ArrayList<>();
        for (InfoCodeValueVO tempInfoCodeValue : InfoCodeValueVO.of(objects)) {
            String objectId = tempInfoCodeValue.getObjectId();
            String infoCode = tempInfoCodeValue.getInfoCode();

            if (objectIdAndExistsRuleInfoCodes.getOrDefault(objectId, Collections.emptySet()).contains(infoCode)) {
                InfoCodeValueVO infoCodeValue = objectIdInfoCodeAndVo.get(objectId, infoCode);
                if (Objects.nonNull(infoCodeValue)) {
                    infoCodeConverter.fill(tempInfoCodeValue, infoCodeValue);
                }
                finalInfoCodeValues.add(tempInfoCodeValue);
            }
        }
        return finalInfoCodeValues;
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
        } else if (DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue().equals(systemClassCode)
                || DtpRwdClassCodeEnum.ACAI_SYSTEM.getValue().equals(systemClassCode)) {
            List<DtpRwdEquipmentDTO> groupsInSystem = this.dtpRwdClientService.listEquipmentGroupInBySystemIds(
                    Sets.newHashSet(systemId), new DtpRwdObjectQueryParam()
                            .setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.ACCPC_GROUP, DtpRwdClassCodeEnum.ACOPC_GROUP,
                                    DtpRwdClassCodeEnum.ACOTC_GROUP, DtpRwdClassCodeEnum.ACCGP_GROUP,
                                    DtpRwdClassCodeEnum.ACCST_GROUP, DtpRwdClassCodeEnum.ACPPC_GROUP))
                            .addWithColumn(DtpRwdObjectDTO.Fields.id));
            pumpGroupObjectIds = groupsInSystem.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            if (DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue().equals(systemClassCode)) {
                groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_CONTROL_AIR_COOLING.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_PUMP_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_CHIL_PUMP_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_TOWER_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_CGLY_PUMP_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_HSLO_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_HOT_PUMP.getCode());
            } else if (DtpRwdClassCodeEnum.ACAI_SYSTEM.getValue().equals(systemClassCode)) {
                groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_CONTROL_COLD_HOT_ALL.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_PUMP_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_CHIL_PUMP_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_TOWER_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_CGLY_PUMP_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_HSLO_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_HOT_PUMP_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_HOT_PUMP.getCode());
            }
        } else {
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
                        if (item.getCode().equals("control_select_air_hot") || item.getCode().equals("control_select_air_cooling")
                                || item.getCode().equals("control_select_air_cooling_all")) {
                            JSONArray valSources = JSONObject.parseArray(item.getValSource());
                            List<String> classCodes = new ArrayList<>();
                            for (Object val : valSources) {
                                JSONObject valSourceObj = (JSONObject) val;
                                JSONArray classCodeCondition = valSourceObj.getJSONArray("classCode");
                                classCodes.addAll(classCodeCondition == null || classCodeCondition.size() == 0 ? null : classCodeCondition.toJavaList(String.class));
                            }
                            classCode = String.join(",", classCodes);
                        }
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
    public String logTag() {
        return "运行监测";
    }
}
