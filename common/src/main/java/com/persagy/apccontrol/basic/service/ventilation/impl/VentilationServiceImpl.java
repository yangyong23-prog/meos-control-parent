package com.persagy.apccontrol.basic.service.ventilation.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdTwinControlObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.AbnormalEquipmentEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.ventilation.VentilationEquipTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.ventilation.VentilationSpaceStatusEnum;
import com.persagy.apccontrol.basic.model.param.ventilation.VentilationModelViewParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationEquipmentStatusVO;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationModelViewVO;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationSpaceStatusVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdTwinObjectRuleQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.ventilation.VentilationService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorVentilationStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.knowledge.converter.DtpKnowledgeConverter;
import com.persagy.apccontrol.basic.structure.ventilation.converter.VentilationConverter;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class VentilationServiceImpl implements VentilationService {

    private final DtpRwdClientService dtpRwdClientService;
    private final VentilationConverter ventilationConverter;
    private final AbstractIotDataCacheServer iotDataCacheService;
    private final AbstractCommonServer abstractCommonServer;
    private final RunningMonitorVentilationStrategy runningMonitorVentilationStrategy;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final DtpKnowledgeConverter dtpKnowledgeConverter;

    /**
     * 查询通风模型视图
     *
     * @param param param
     * @return {@link VentilationModelViewVO }
     */
    @Override
    public VentilationModelViewVO queryVentilationModelView(VentilationModelViewParam param) {
        VentilationModelViewVO vo = new VentilationModelViewVO();
        String projectId = ContextUtil.getProjectId();
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        String instanceId = param.getInstanceId();
        if (StringUtils.isBlank(instanceId)) {
            instanceId = projectId;
        }
        String[] fieldNames = Stream.concat(FieldUtils.getAllFieldsList(DtpRwdSpaceDTO.Fields.class).stream(),
                        FieldUtils.getAllFieldsList(DtpRwdObjectDTO.Fields.class).stream())
                .map(Field::getName).distinct().toArray(String[]::new);
        List<DtpRwdSpaceDTO> spaces = listSpaces(param.getVentilationType(), instanceId, instanceType, fieldNames);
        if (CollectionUtils.isEmpty(spaces)) {
            return vo;
        }
        List<String> spaceIds = spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toList());
        List<DtpRwdEquipmentDTO> equipments = queryEquipments(projectId, spaceIds, null);
        List<String> riskEvenRiskObjectIds = new ArrayList<>(spaceIds);
        List<String> equipIds = equipments.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList());
        riskEvenRiskObjectIds.addAll(equipIds);
        //异常事件
        Map<String, List<ExceptionEventBaseInfoVO>> riskControlObjectives =
                abstractCommonServer.mapRiskEvent(
                        riskEvenRiskObjectIds, runningMonitorVentilationStrategy.exceptionFilterRiskMeasureAimLevel2());
        List<VentilationEquipmentStatusVO> equipmentStatusList = buildEquipmentStatusVO(equipments, riskControlObjectives);
        List<VentilationSpaceStatusVO> spaceStatusList = buildSpaceStatusVO(spaces, equipmentStatusList, param.getVentilationType(), riskControlObjectives);
        vo.setSpaces(spaceStatusList);
        vo.setEquipments(equipmentStatusList);
        return vo;
    }



    public List<DtpRwdSpaceDTO> listSpaces(RunningMonitorSecondTypeEnum ventilationType,
                                           String instanceId,
                                           DemandInstanceTypeEnum instanceType,
                                           String... withColumns) {
        DemandInstanceValueObject valueObject = new DemandInstanceValueObject();
        valueObject.setInstanceId(instanceId);
        valueObject.setInstanceType(instanceType);
        return runningMonitorVentilationStrategy.listSpaces(
                ventilationType, valueObject, withColumns);
    }

    private List<VentilationSpaceStatusVO> buildSpaceStatusVO(List<DtpRwdSpaceDTO> spaces, List<VentilationEquipmentStatusVO> equipments,
                                                              RunningMonitorSecondTypeEnum ventilationType,
                                                              Map<String, List<ExceptionEventBaseInfoVO>> riskControlObjectives) {
        List<VentilationSpaceStatusVO> result = new ArrayList<>();
        Set<String> filterSpaceIds = new HashSet<>();
        equipments.forEach(e -> {
            if (CollectionUtils.isNotEmpty(e.getRelObjIds())) {
                filterSpaceIds.addAll(e.getRelObjIds());
            }
        });
        //过滤空间
        spaces = spaces.stream().filter(s -> filterSpaceIds.contains(s.getId())).collect(Collectors.toList());
        List<String> spaceIds = spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toList());
        List<String> infoCodes;
        switch (ventilationType) {
            case LOGISTICS_VENTILATION:
                infoCodes = Arrays.asList(InfoCodeEnum.O2.getCode(), InfoCodeEnum.SPACE_TDB.getCode());
                break;
            case PARKING_VENTILATION:
                infoCodes = Collections.singletonList(InfoCodeEnum.CO.getCode());
                break;
            case TOILET_VENTILATION:
                infoCodes = Arrays.asList(InfoCodeEnum.NH3.getCode(), InfoCodeEnum.H2S.getCode());
                break;
            case DOME_VENTILATION:
                infoCodes = Collections.singletonList(InfoCodeEnum.SPACE_TDB.getCode());
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
        Map<String, Set<String>> objectRuleInfoCodesMap = dtpRwdClientService.mapObjectIdAndTwinObjectRuleInfoCodes(new DtpRwdTwinObjectRuleQueryParam(DtpRwdClassCodeEnum.SPACE.value()));
        List<InfoCodeValueVO> infoCodeValueList = iotDataCacheService.listInfoCodeValue(ContextUtil.getProjectId(), null, spaceIds, infoCodes);
        Map<String, List<InfoCodeValueVO>> infoCodeValueMap = infoCodeValueList.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId));
        Map<String, DtpKnowledgeInfoCodeDTO> dtpKnowledgeInfoCodeDTOMap = dtpKnowledgeClientService.mapInfoCodeAndData(false, false, DtpRwdClassCodeEnum.SPACE.value());
        spaces.forEach(space -> {
            VentilationSpaceStatusVO vo = ventilationConverter.toSpaceStatusVo(space);
            String id = vo.getId();
            List<ExceptionEventBaseInfoVO> exceptionEventBaseInfoVOS = riskControlObjectives.get(id);
            vo.setIsAbnormal(CollectionUtils.isNotEmpty(exceptionEventBaseInfoVOS));
            vo.setIsControl(false);
            Set<VentilationEquipTypeEnum> equipTypes = Sets.newHashSet();
            boolean flag = true;
            for (VentilationEquipmentStatusVO eq : equipments) {
                //判断是否可控
                if (CollectionUtils.isNotEmpty(eq.getRelObjIds()) && eq.getRelObjIds().contains(id) && flag) {
                    if (eq.getIsControl()) {
                        //如果有一个设备可控，则不需要再判断
                        vo.setIsControl(true);
                        flag = false;
                    }
                }
                //设备类型
                if (eq.getEquipType() != null) {
                    equipTypes.add(eq.getEquipType());
                }
            }
            vo.setEquipTypes(equipTypes);
            spaceStatus(vo, equipments);
            List<InfoCodeValueVO> infoCodeValueVOS = infoCodeValueMap.getOrDefault(id, Lists.newArrayList());
            Map<String, Object> infoValueMap = infoCodeValueVOS.stream().filter(e -> Objects.nonNull(e.getInfoCodeValue()))
                    .collect(Collectors.toMap(InfoCodeValueVO::getInfoCode, InfoCodeValueVO::getInfoCodeValue, (v1, v2) -> v1));
            infoCodes(vo, infoValueMap, infoCodes, dtpKnowledgeInfoCodeDTOMap, objectRuleInfoCodesMap);
            result.add(vo);
        });


        return result;
    }

    private void spaceStatus(VentilationSpaceStatusVO vo, List<VentilationEquipmentStatusVO> equipments) {
        String spaceId = vo.getId();
        List<VentilationEquipmentStatusVO> filterEquips = equipments.stream().filter(eq -> eq.getRelObjIds().contains(spaceId)).collect(Collectors.toList());
        //如果设备为0，表示无法监测
        if (CollectionUtils.isEmpty(filterEquips)) {
            vo.setSpaceStatus(VentilationSpaceStatusEnum.UNABLE_TO_DETECT);
            return;
        }
        //所有的排风机、送风机的运行状态的数量=0 and 所有的排风机、送风机的运行状态获取不到的数量大于等于1 = 无法监测
        List<VentilationEquipmentStatusVO> openList = filterEquips.stream().filter(eq -> eq.getRunStatus() == RunStatusEnum.OPEN).collect(Collectors.toList());
        List<VentilationEquipmentStatusVO> nullList = filterEquips.stream().filter(eq -> Objects.isNull(eq.getRunStatus())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(openList) && CollectionUtils.isNotEmpty(nullList)) {
            vo.setSpaceStatus(VentilationSpaceStatusEnum.UNABLE_TO_DETECT);
            return;
        }
        //所有的排风机、送风机都能获取到运行状态（runStatus）且运行状态=停止 = 未通风
        List<VentilationEquipmentStatusVO> closeList = filterEquips.stream().filter(eq -> eq.getRunStatus() == RunStatusEnum.CLOSE).collect(Collectors.toList());
        if (closeList.size() == filterEquips.size()) {
            vo.setSpaceStatus(VentilationSpaceStatusEnum.NOT_VENTILATION);
            return;
        }
        Map<VentilationEquipTypeEnum, List<VentilationEquipmentStatusVO>> equipTypeListMap = filterEquips.stream().filter(eq -> eq.getEquipType() != null).collect(Collectors.groupingBy(VentilationEquipmentStatusVO::getEquipType));
        //送风机列表
        List<VentilationEquipmentStatusVO> sendEquips = equipTypeListMap.get(VentilationEquipTypeEnum.SEND_FAN);
        //排风机列表
        List<VentilationEquipmentStatusVO> receiveEquips = equipTypeListMap.get(VentilationEquipTypeEnum.RECEIVE_FAN);
        List<VentilationEquipmentStatusVO> sendOpenList;
        List<VentilationEquipmentStatusVO> receiveOpenList;
        if (CollectionUtils.isNotEmpty(sendEquips) && CollectionUtils.isNotEmpty(receiveEquips)) {
            sendOpenList = sendEquips.stream().filter(eq -> eq.getRunStatus() == RunStatusEnum.OPEN).collect(Collectors.toList());
            receiveOpenList = receiveEquips.stream().filter(eq -> eq.getRunStatus() == RunStatusEnum.OPEN).collect(Collectors.toList());
            //送风机至少有一台能获取到运行状态 && 排风机至少有一台能获取到运行状态 = 正在送风&排风
            if (CollectionUtils.isNotEmpty(sendOpenList) && CollectionUtils.isNotEmpty(receiveOpenList)) {
                vo.setSpaceStatus(VentilationSpaceStatusEnum.SEND_AND_REMOVE);
            } else if (CollectionUtils.isNotEmpty(sendOpenList) && CollectionUtils.isEmpty(receiveOpenList)) {
                //送风机至少有一台能获取到运行状态 &&  有排风机或者排风机没有获取到运行状态或者排风机获取到运行状态=停止= 正在送风
                vo.setSpaceStatus(VentilationSpaceStatusEnum.SENDING);
            } else if (CollectionUtils.isEmpty(sendOpenList) && CollectionUtils.isNotEmpty(receiveOpenList)) {
                // 排风机至少有一台能获取到运行状态 &&  有送风机或者送风机没有获取到运行状态或者送风机获取到运行状态=停止 = 正在排风
                vo.setSpaceStatus(VentilationSpaceStatusEnum.REMOVING);
            }
            return;
        }
        if (CollectionUtils.isNotEmpty(sendEquips)) {
            sendOpenList = sendEquips.stream().filter(eq -> eq.getRunStatus() == RunStatusEnum.OPEN).collect(Collectors.toList());
            //送风机至少有一台能获取到运行状态 = 正在送风
            if (CollectionUtils.isNotEmpty(sendOpenList)) {
                vo.setSpaceStatus(VentilationSpaceStatusEnum.SENDING);
            }
        } else if (CollectionUtils.isNotEmpty(receiveEquips)) {
            // 排风机至少有一台能获取到运行状态 &&  有送风机或者送风机没有获取到运行状态或者送风机获取到运行状态=停止 = 正在排风
            receiveOpenList = receiveEquips.stream().filter(eq -> eq.getRunStatus() == RunStatusEnum.OPEN).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(receiveOpenList)) {
                vo.setSpaceStatus(VentilationSpaceStatusEnum.REMOVING);
            }
        }


    }

    private void infoCodes(VentilationSpaceStatusVO vo, Map<String, Object> infoValueMap, List<String> infoCodes,
                           Map<String, DtpKnowledgeInfoCodeDTO> dtpKnowledgeInfoCodeDTOMap, Map<String, Set<String>> objectRuleInfoCodesMap) {
        List<InfoCodeValueVO> InfoCodeValueVOs = new ArrayList<>();
        Set<String> infoCodeRules = objectRuleInfoCodesMap.get(vo.getId());
        if (Objects.isNull(infoCodeRules)) {
            vo.setInfoCodes(InfoCodeValueVOs);
            return;
        }
        infoCodes.forEach(e -> {
            //如果没有绑点规则，直接返回
            if (!infoCodeRules.contains(e)) {
                return;
            }
            DtpKnowledgeInfoCodeDTO infoCodeDTO = dtpKnowledgeInfoCodeDTOMap.get(e);
            if (Objects.nonNull(infoCodeDTO)) {
                InfoCodeValueVO valueVO = dtpKnowledgeConverter.toRunningParameterVo(infoCodeDTO);
                Object value = infoValueMap.get(e);
                valueVO.setDataValue(value != null ? value.toString() : null);
                InfoCodeValueVOs.add(valueVO);
            }
        });
        vo.setInfoCodes(InfoCodeValueVOs);
    }


    /**
     * 构建设备状态vo
     *
     * @param equipments 设备
     * @return {@link List }<{@link VentilationEquipmentStatusVO }>
     */
    private List<VentilationEquipmentStatusVO> buildEquipmentStatusVO(List<DtpRwdEquipmentDTO> equipments, Map<String, List<ExceptionEventBaseInfoVO>> riskControlObjectives) {
        List<VentilationEquipmentStatusVO> result = new ArrayList<>();
        Set<String> classCodeSet = equipments.stream().map(DtpRwdEquipmentDTO::getClassCode).collect(Collectors.toSet());
        List<String> equipIds = equipments.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList());
        //查询是否可控
        List<DtpRwdTwinControlObjectDTO> controlObjects = dtpRwdClientService.listTwinControlObjects(false, ContextUtil.getProjectId(), classCodeSet);
        Map<String, List<DtpRwdTwinControlObjectDTO>> classCodeControlObjectsMap = controlObjects.stream().collect(Collectors.groupingBy(DtpRwdTwinControlObjectDTO::getClassCode));
        //查询信息点
        List<String> infoCodes = Lists.newArrayList(InfoCodeEnum.RUN_STATUS.getCode(), InfoCodeEnum.ORDER_FAIL_ALARM.getCode(), InfoCodeEnum.ONLINE_STATUS.getCode());
        List<InfoCodeValueVO> infoCodeValueList = iotDataCacheService.listInfoCodeValue(ContextUtil.getProjectId(), null, equipIds, infoCodes);
        Map<String, List<InfoCodeValueVO>> infoCodeValueMap = infoCodeValueList.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId));
        equipments.forEach(equipment -> {
            VentilationEquipmentStatusVO vo = ventilationConverter.toEquipmentStatusVo(equipment);
            String classCode = vo.getClassCode();
            String id = vo.getId();
            //设备类型
            equipType(vo, equipment, classCode);
            //是否可控
            vo.setIsControl(false);
            List<DtpRwdTwinControlObjectDTO> controlObjectsList = classCodeControlObjectsMap.get(classCode);
            if (CollectionUtils.isNotEmpty(controlObjectsList)) {
                Set<String> idSet = controlObjectsList.stream().map(DtpRwdTwinControlObjectDTO::getObjectId).collect(Collectors.toSet());
                vo.setIsControl(idSet.contains(id));
            }
            List<InfoCodeValueVO> infoCodeValueVOS = infoCodeValueMap.get(id);
            if (CollectionUtils.isNotEmpty(infoCodeValueVOS)) {
                Map<String, Object> infoValueMap = infoCodeValueVOS.stream().filter(e -> Objects.nonNull(e.getInfoCodeValue()))
                        .collect(Collectors.toMap(InfoCodeValueVO::getInfoCode, InfoCodeValueVO::getInfoCodeValue, (v1, v2) -> v1));
                //运行状态
                Object value = infoValueMap.get(InfoCodeEnum.RUN_STATUS.getCode());
                if (value != null) {
                    RunStatusEnum runStatusEnum = RunStatusEnum.parseString(value.toString());
                    vo.setRunStatus(runStatusEnum);
                }
                //异常类型
                abnormalTypes(vo, infoValueMap);
            }
            List<ExceptionEventBaseInfoVO> exceptionEventBaseInfoVOS = riskControlObjectives.get(id);
            //异常
            if (CollectionUtils.isNotEmpty(exceptionEventBaseInfoVOS)) {
                List<AbnormalEquipmentEnum> abnormalTypes = vo.getAbnormalTypes();
                if (abnormalTypes == null) {
                    abnormalTypes = new ArrayList<>();
                    vo.setAbnormalTypes(abnormalTypes);
                }
                abnormalTypes.add(AbnormalEquipmentEnum.ABNORMAL);
            }
            if (vo.getEquipType() != null) {
                result.add(vo);
            }
        });
        return result;
    }

    private void equipType(VentilationEquipmentStatusVO vo, DtpRwdEquipmentDTO equipment, String classCode) {
        if (DtpRwdClassCodeEnum.ACVTSF.getValue().equals(classCode)) {
            Object funcType = equipment.getUndefinedProperties().getOrDefault("funcType", null);
            if (funcType != null) {
                String funcTypeStr = funcType.toString();
                if ("1".equals(funcTypeStr)) {
                    vo.setEquipType(VentilationEquipTypeEnum.SEND_FAN);
                } else if ("2".equals(funcTypeStr)) {
                    vo.setEquipType(VentilationEquipTypeEnum.RECEIVE_FAN);
                }
            }
        } else if (DtpRwdClassCodeEnum.FFSCAF.getValue().equals(classCode)) {
            vo.setEquipType(VentilationEquipTypeEnum.RECEIVE_FAN);
        } else if (DtpRwdClassCodeEnum.CFCSWN.getValue().equals(classCode)
                || DtpRwdClassCodeEnum.CFCSEW.getValue().equals(classCode)) {
            // PRD-290902: 窗类设备映射为自然通风
            vo.setEquipType(VentilationEquipTypeEnum.NATURAL_VENTILATION);
        } else {
            vo.setEquipType(VentilationEquipTypeEnum.SEND_FAN);
        }
    }

    private void abnormalTypes(VentilationEquipmentStatusVO vo, Map<String, Object> infoValueMap) {
        Object orderFailAlarm = infoValueMap.get(InfoCodeEnum.ORDER_FAIL_ALARM.getCode());
        Object onlineStatus = infoValueMap.get(InfoCodeEnum.ONLINE_STATUS.getCode());
        List<AbnormalEquipmentEnum> abnormalTypes = new ArrayList<>();
        //控反不一致
        if (Objects.nonNull(orderFailAlarm)) {
            String orderFailAlarmStr = orderFailAlarm.toString();
            if ("1".equals(orderFailAlarmStr)) {
                abnormalTypes.add(AbnormalEquipmentEnum.CONTROL_REVERSE_INCONSISTENT);
            }
        }
        //离线
        if (Objects.nonNull(onlineStatus)) {
            String onlineStatusStr = onlineStatus.toString();
            if ("0".equals(onlineStatusStr)) {
                abnormalTypes.add(AbnormalEquipmentEnum.OFFLINE);
            }
        }
        vo.setAbnormalTypes(abnormalTypes);
    }


    /**
     * 查询设备
     *
     * @param projectId          项目id
     * @param filterEquipmentIds 过滤设备ID
     * @param withColumns        列
     * @return {@link List }<{@link DtpRwdEquipmentDTO }>
     * @since 5.240702.0
     */
    public List<DtpRwdEquipmentDTO> queryEquipments(
            @NonNull String projectId,
            @NonNull Collection<String> spaceIds,
            @Nullable Collection<String> filterEquipmentIds,
            @Nullable String... withColumns) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setId(filterEquipmentIds)
                .setClassCodeEnum(ALL_EQUIPMENT_CLASS_CODES)
                .addWithColumn(withColumns);
        List<DtpRwdEquipmentDTO> equipments = dtpRwdClientService.listEquipmentServiceBySpaceIds(spaceIds, queryParam);
        // PRD-290902: 窗类设备通过 EQUIPMENT_IN_SPACE 关系查询，与风机结果按 ID 合并去重
        List<DtpRwdEquipmentDTO> windowEquipments = dtpRwdClientService.listEquipmentInBySpaceIds(spaceIds,
                new DtpRwdObjectQueryParam()
                        .setId(filterEquipmentIds)
                        .setClassCodeEnum(Sets.newHashSet(DtpRwdClassCodeEnum.CFCSWN, DtpRwdClassCodeEnum.CFCSEW))
                        .addWithColumn(withColumns));
        if (CollectionUtils.isNotEmpty(windowEquipments)) {
            Set<String> equipIdSet = new HashSet<>();
            if (CollectionUtils.isNotEmpty(equipments)) {
                equipments.forEach(e -> equipIdSet.add(e.getId()));
            } else {
                equipments = new ArrayList<>();
            }
            for (DtpRwdEquipmentDTO we : windowEquipments) {
                if (equipIdSet.add(we.getId())) {
                    equipments.add(we);
                }
            }
        }
        if (CollectionUtils.isEmpty(equipments)) {
            logProjectInstance(projectId).warn("无有效的设备");
            return Lists.newArrayList();
        }
        return equipments;
    }

    @Override
    public Map<String, Set<String>> mapClaasCode2objectIds(String projectId, String instanceId, DemandInstanceTypeEnum instanceType) {
        Map<String, Set<String>> map = new HashMap<>(8);
        // 空间
        List<DtpRwdSpaceDTO> allSpaces = new ArrayList<>();
        for (RunningMonitorSecondTypeEnum ventilationType : ALL_VENTILATION_TYPES) {
            DemandInstanceValueObject valueObject = new DemandInstanceValueObject();
            valueObject.setInstanceId(instanceId);
            valueObject.setInstanceType(instanceType);
            List<DtpRwdSpaceDTO> spaces = runningMonitorVentilationStrategy.listSpaces(
                    ventilationType, valueObject, DtpRwdObjectDTO.Fields.id);
            if (CollectionUtil.isNotEmpty(spaces)) {
                allSpaces.addAll(spaces);
            }
        }
        Set<String> spaceIds = new HashSet<>();
        if (CollectionUtil.isNotEmpty(allSpaces)) {
            // 获取space列表中的id集合
            spaceIds = allSpaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
            map.put(CommonConst.DtpRwdConst.GeneralZone, spaceIds);
        } else {
            map.put(CommonConst.DtpRwdConst.GeneralZone, new HashSet<>(0));
        }
        // 设备
        List<DtpRwdEquipmentDTO> dtpRwdObjectDTOS = queryEquipments(projectId, spaceIds, null);
        // 根据classCode分组对象id列表
        Map<String, Set<String>> objectIdMap = dtpRwdObjectDTOS.stream().collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode, Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
        for (DtpRwdClassCodeEnum clazzCodeEnum : ALL_EQUIPMENT_CLASS_CODES) {
            if (!objectIdMap.containsKey(clazzCodeEnum.getValue())) {
                objectIdMap.put(clazzCodeEnum.getValue(), new HashSet<>(0));
            }
        }
        map.putAll(objectIdMap);
        return map;
    }

    /**
     * 通风异常事件叠加风控目标
     *
     * @param param 通风入参
     * @return 异常事件列表
     */
    @Override
    public List<ExceptionEventBaseInfoVO> getAbnormalDataList(VentilationModelViewParam param) {

        String projectId = ContextUtil.getProjectId();
        String instanceId = param.getInstanceId();
        if (StringUtils.isBlank(instanceId)) {
            instanceId = projectId;
        }
        // 入参空间id不为空则不查全部，为空则仅空间相关设备以及空间
        List<DtpRwdSpaceDTO> spaces = listSpaces(param.getVentilationType(), instanceId, param.getInstanceType(), DtpRwdObjectDTO.Fields.id);
        if (CollectionUtils.isEmpty(spaces)) {
            return new ArrayList<>();
        }
        List<String> spaceIds = spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toList());
        ArrayList<String> objects = new ArrayList<>(spaceIds);
        List<String> equipIds = new ArrayList<>();
        List<DtpRwdEquipmentDTO> equipments = queryEquipments(projectId, spaceIds, null, DtpRwdObjectDTO.Fields.id);
        if (CollectionUtils.isNotEmpty(equipments)) {
            equipIds = equipments.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList());
        }
        objects.addAll(equipIds);
        return this.abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(
                objects, null);
    }

    @Override
    public String logTag() {
        return "通风";
    }
}
