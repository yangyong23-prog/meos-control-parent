package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.AirTerminalZoneControllerParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos.AirTerminalZoneControllerParameterResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos.AirTerminalZoneResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos.PumGroup;
import com.persagy.apccontrol.basic.outside.feign.service.DtpMeosServerCloudClientServiceImpl;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.impl.DtpRwdClientServiceImpl;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerSize;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.ExceptionRecordDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun.*;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import groovy.lang.Tuple2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author weiyizhong
 * @Date 2026/2/7 16:17
 * @Company yushu.biz
 * @Description: 空调控制分区
 */
@Component
public class AirConditioningControlZones implements LogComponent {

    @Autowired
    private DtpRwdClientService dtpRwdClientService;
    @Autowired
    private AbstractIotDataCacheServer<?> abstractIotDataCacheServer;
    @Autowired
    private DtpRwdClientServiceImpl dtpRwdClientServiceImpl;
    @Autowired
    private DtpMeosServerCloudClientServiceImpl dtpMeosServerCloudClientService;
    /** 基础字段：所有查询都需要返回的字段 */
    private static final String[] BASE_COLUMNS = {
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.relObjIds,
            DtpRwdObjectDTO.Fields.name,
            DtpRwdObjectDTO.Fields.localName,
            InfoCodeEnum.RATED_FLOW.getValue(),
    };


    /** 水泵运行频率编码 */
    private static final String WORK_FREQ = "workFreq";

    /** 额定流量编码 */
    private static final String RATED_FLOW = "ratedFlow";


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/3/13 10:47
    　　 * @description: 系统下 空调控制分区下 电动通断水阀服务或末级泵组服务集合
    */
    public Map<String,Map<String, List<PumGroup>>> queryTerminalEquipmentBySystems(Set<String> systemIds, LocalDate reportDate, Map<String, ControlZonesSwitchDto> controlZonesSwitchMap){
        Map<String,Map<String, List<PumGroup>>> result = Maps.newHashMap();
        //控制台没有批量查询的接口，暂时循环查询
        //构造查询参数
        AirTerminalZoneControllerParam param = new AirTerminalZoneControllerParam();
        param.setBeginDate(reportDate);
        param.setEndDate(reportDate);
        param.setProjectId(ContextUtil.getProjectId());
        param.setSystemType(DtpRwdClassCodeEnum.ACAC_SYSTEM);
        systemIds.forEach(systemId -> {
            param.setSystemId(systemId);
            AirTerminalZoneControllerParameterResponse response = dtpMeosServerCloudClientService.airTerminalZoneControllerParameter(param);
            if(Objects.nonNull(response)){
                List<AirTerminalZoneResponse> airTerminalZones = response.getAirTerminalZones();
                if(CollectionUtils.isNotEmpty(airTerminalZones)){
                    //控制分区不为空
                    Map<String, List<PumGroup>> zoneToPumpGroups = Maps.newHashMap();
                    airTerminalZones.forEach(airTerminalZone -> {
                        //获取泵组或电动水阀
                        List<PumGroup> pumpGroups = airTerminalZone.getPumpGroups();
                        if(CollectionUtils.isNotEmpty(pumpGroups)){
                            zoneToPumpGroups.put(airTerminalZone.getAirTerminalZoneId(),pumpGroups);
                        }
                        if(StringUtils.isNotEmpty(airTerminalZone.getSwitchSetInfoCode()) && StringUtils.isNotEmpty(airTerminalZone.getSwitchSetInfoCodeName())){
                            ControlZonesSwitchDto switchDto = new ControlZonesSwitchDto();
                            switchDto.setSwitchSetInfoCode(airTerminalZone.getSwitchSetInfoCode());
                            switchDto.setSwitchSetInfoCodeName(airTerminalZone.getSwitchSetInfoCodeName());
                            controlZonesSwitchMap.put(airTerminalZone.getAirTerminalZoneId(),switchDto);
                        }
                    });
                    result.put(systemId,zoneToPumpGroups);
                }
            }
        });
        return result;
    }

    /**
     * 空调控制分区数据计算 系统id to 空调控制分区
     */
    public Map<String,List<AirConditioningControlZonesDto>> buildAirConditioningControlZone(Set<String> systemIds,LocalDate reportDate){
        //查询空调制冷分区
        //查询空调控制分区 to 分区开关code
        Map<String, ControlZonesSwitchDto> controlZonesSwitchMap = Maps.newHashMap();
        Map<String, Map<String, List<PumGroup>>> systemIdToZoneToInstanceMap = queryTerminalEquipmentBySystems(systemIds, reportDate,controlZonesSwitchMap);
        if(MapUtils.isEmpty(systemIdToZoneToInstanceMap)){
            return Maps.newHashMap();
        }
        Map<String, List<PumGroup>> controlZoneIdToEqMap = Maps.newHashMap();
        systemIdToZoneToInstanceMap.forEach((systemId, zoneToInstanceMap) -> controlZoneIdToEqMap.putAll(zoneToInstanceMap));

        // 1、查询调节记录 - 按空调控制分区ID分组
        Map<String, AdjustmentRecords> controlZoneIdToAdjustmentRecords = queryEquipmentAdjustmentRecords(controlZoneIdToEqMap, reportDate,controlZonesSwitchMap);
        //2、处理开闭状态或水泵运行频率
        Tuple2<Map<String, List<IotFieldRecord>>, Map<String,List<DtpRwdObjectDTO>>> actualRunRecordData = queryActualRunRecordData(controlZoneIdToEqMap, reportDate);

        //3、设备异常记录
        Map<String, List<ExceptionRecordDTO>> controlZoneIdToExceptionRecordDTOS = queryExceptionRecords(reportDate, controlZoneIdToEqMap, actualRunRecordData.getSecond());

        //4、处理最终结果，按照系统id 分组
        return handlerAssembleResult(controlZoneIdToEqMap, systemIdToZoneToInstanceMap, controlZoneIdToAdjustmentRecords, actualRunRecordData.getFirst(), controlZoneIdToExceptionRecordDTOS);
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/3/13 14:04
    　　 * @description: 处理组装返回结果
    */
    private Map<String,List<AirConditioningControlZonesDto>>  handlerAssembleResult(Map<String, List<PumGroup>> controlZoneIdToEqMap, Map<String, Map<String, List<PumGroup>>> systemIdToZoneToInstanceMap, Map<String, AdjustmentRecords> controlZoneIdToAdjustmentRecords, Map<String, List<IotFieldRecord>> controlZoneIdActualRunRecordData, Map<String, List<ExceptionRecordDTO>> controlZoneIdToExceptionRecordDTOS) {
        Map<String,List<AirConditioningControlZonesDto>> result = Maps.newHashMap();
        //查询空调控制分区信息
        Map<String,DtpRwdObjectDTO> controlZoneIdToControlZoneMap = dtpRwdClientServiceImpl.listObject(new DtpRwdObjectQueryParam().setId(controlZoneIdToEqMap.keySet()).addWithColumn(
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.objType)).stream()
                .filter(item -> ObjectUtils.allNotNull(item.getId(), item.getLocalName()))
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, e -> e, (o1, o2) -> o1));

        systemIdToZoneToInstanceMap.keySet().forEach(systemId -> {
            List<AirConditioningControlZonesDto> airConditioningControlZonesDtos = new ArrayList<>();
            Map<String, List<PumGroup>> controlZoneToPumGroupsMap = systemIdToZoneToInstanceMap.get(systemId);
            controlZoneToPumGroupsMap.forEach((controlZoneId, pumGroups) -> {
                DtpRwdObjectDTO controlZone = controlZoneIdToControlZoneMap.getOrDefault(controlZoneId,new DtpRwdObjectDTO());
                //泵组下，空调分区是一样的去一个泵组中的空调分区作为名称
                AirConditioningControlZonesDto airConditioningControlZonesDto = new AirConditioningControlZonesDto();
                airConditioningControlZonesDto.setId(controlZone.getId());
                airConditioningControlZonesDto.setName(controlZone.getLocalName());
                airConditioningControlZonesDto.setObjType(controlZone.getObjType());
                airConditioningControlZonesDto.setClassCode(controlZone.getClassCode());
                airConditioningControlZonesDto.setAdjustmentRecords(controlZoneIdToAdjustmentRecords.get(controlZone.getId()));
                airConditioningControlZonesDto.setActualRun(controlZoneIdActualRunRecordData.get(controlZone.getId()));
                airConditioningControlZonesDto.setExceptionRecord(controlZoneIdToExceptionRecordDTOS.get(controlZone.getId()));
                airConditioningControlZonesDtos.add(airConditioningControlZonesDto);

            });
            result.put(systemId, airConditioningControlZonesDtos);
        });
        return result;
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/3/13 14:03
    　　 * @description: 处理设备异常记录
    */
    private Map<String, List<ExceptionRecordDTO>> queryExceptionRecords(LocalDate reportDate, Map<String, List<PumGroup>> controlZoneIdToEqMap, Map<String,List<DtpRwdObjectDTO>> controlZoneIdToWaterPump) {
        Map<String, List<DtpRwdObjectDTO>> controlZoneIdToWaterPumpValveMap = Maps.newHashMap();
        //查询对象信息
        List<DtpRwdObjectDTO> objectList = dtpRwdClientServiceImpl.listObject(new DtpRwdObjectQueryParam().setId(controlZoneIdToEqMap.values().stream().flatMap(List::stream).map(PumGroup::getPumpGroupId).collect(Collectors.toSet())).addWithColumn(
                        DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.objType));

        //水泵处理
        controlZoneIdToEqMap.forEach((controlZoneId, pumGroup) -> {
            //过滤出水阀
            Set<String> instanceIds = pumGroup.stream().filter(item -> DtpRwdClassCodeEnum.ACVVEO.getValue().equals(item.getClassCode())).map(PumGroup::getPumpGroupId).collect(Collectors.toSet());
            List<DtpRwdObjectDTO> waterPump = controlZoneIdToWaterPump.getOrDefault(controlZoneId, new ArrayList<DtpRwdObjectDTO>());
            waterPump.addAll(objectList.stream().filter(item -> instanceIds.contains(item.getId())).collect(Collectors.toList()));
            controlZoneIdToWaterPumpValveMap.put(controlZoneId,waterPump);
        });
        Map<String, List<ExceptionRecordDTO>> controlZoneIdToExceptionRecordDTOS = queryExceptionRecords(reportDate, controlZoneIdToWaterPumpValveMap);
        return controlZoneIdToExceptionRecordDTOS;
    }

    /**
     * 查询设备异常记录
     */
    private Map<String, List<ExceptionRecordDTO>> queryExceptionRecords(LocalDate reportDate, Map<String, List<DtpRwdObjectDTO>> controlZoneIdToEqMap ) {

        List<ExceptionRecordDTO> exceptionRecordDTOS = SpringUtil.getBean("runAnalysisReportCoolingSysRun",RunAnalysisReportCoolingSysRun.class).queryExceptionRecords(reportDate, controlZoneIdToEqMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
        Map<String, List<ExceptionRecordDTO>> controlZoneIdToExceptionRecordDTOS = Maps.newHashMap();
        //按照空调控制分区id 分组
        if(CollectionUtils.isNotEmpty(exceptionRecordDTOS)){
            Map<Object, List<ExceptionRecordDTO>> objIdToRecordMap = exceptionRecordDTOS.stream().collect(Collectors.groupingBy(e ->e.getObject().getId()));
            controlZoneIdToEqMap.forEach((controlZoneId, rwdObjectDTOS) -> rwdObjectDTOS.forEach(rwdObjectDTO -> {
                List<ExceptionRecordDTO> recordDTOS = objIdToRecordMap.get(rwdObjectDTO.getId());
                if(CollectionUtils.isNotEmpty(recordDTOS)){
                    List<ExceptionRecordDTO> orDefault = controlZoneIdToExceptionRecordDTOS.getOrDefault(recordDTOS, new ArrayList<>());
                    orDefault.addAll(recordDTOS);
                    controlZoneIdToExceptionRecordDTOS.put(controlZoneId, orDefault);
                }
            }));
        }
        return controlZoneIdToExceptionRecordDTOS;
    }

    public Tuple2<Map<String,List<IotFieldRecord>>,Map<String,List<DtpRwdObjectDTO>>> queryActualRunRecordData(Map<String, List<PumGroup>> controlZoneIdToEqMap, LocalDate reportDate) {
        Map<String,List<IotFieldRecord>> actualRunRecordData = Maps.newHashMap();
        //水泵
        Map<String,List<DtpRwdObjectDTO>> controlZoneIdToWaterPumpMap = Maps.newHashMap();
        if(MapUtil.isEmpty(controlZoneIdToEqMap)){
            return new Tuple2<>(actualRunRecordData,controlZoneIdToWaterPumpMap);
        }

        List<String> instanceIds = Lists.newArrayList();
        instanceIds.addAll(controlZoneIdToEqMap.values().stream().flatMap(List::stream).map(PumGroup::getPumpGroupId).filter(StringUtils::isNotEmpty).collect(Collectors.toList()));
        //查询实例信息
        Map<String, String> eqIdToClassCode = dtpRwdClientServiceImpl.listObject(new DtpRwdObjectQueryParam().setId(instanceIds).addWithColumn(
                        DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode)).stream()
                .filter(item -> ObjectUtils.allNotNull(item.getId(), item.getClassCode()))
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdObjectDTO::getClassCode, (o1, o2) -> o1));

        Map<String, List<InfoCodeDefineModel>> classCodeToFuncid = SpringUtil.getBean("runAnalysisReportCoolingSysRun",RunAnalysisReportCoolingSysRun.class).queryClassCode2InfoCodeDefines(eqIdToClassCode.values().stream().distinct().collect(Collectors.toList()));


        for (String controlZoneId : controlZoneIdToEqMap.keySet()) {
            List<PumGroup> pumGroups = controlZoneIdToEqMap.get(controlZoneId);
            //如果控制分区下只有电动水阀服务，则走电动水阀的分类
            List<PumGroup> water = pumGroups.stream().filter(item -> DtpRwdClassCodeEnum.ACVVEO.getValue().equals(item.getClassCode())).collect(Collectors.toList());
            if (pumGroups.size() == water.size()) {
                // 全部是电动水阀，查询开闭状态iot数据
                List<InfoCodeValueVO> infoCodeValueVOSPump = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                        DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES,
                        pumGroups.stream().map(PumGroup::getPumpGroupId).collect(Collectors.toList()),
                        Lists.newArrayList(InfoCodeEnum.SWITCH_STATUS.getCode()),
                        DateUtils.getDayBoundaryByDate(reportDate, 0, Boolean.TRUE),
                        DateUtils.getDayBoundaryByDate(reportDate, 1, Boolean.TRUE));
                Map<String, Map<LocalDateTime, String>> switchStatusMap = InfoCodeValueVO.mapObjectId2DateTime2Value(infoCodeValueVOSPump);
                List<IotFieldRecord> iotFieldRecordList = new ArrayList<>();
                for (PumGroup dtpRwdObjectDTO : pumGroups) {
                        String classCode = eqIdToClassCode.get(dtpRwdObjectDTO.getPumpGroupId());
                        List<InfoCodeDefineModel> infoCodeDefineModels = classCodeToFuncid.get(classCode);
                        Map<String, InfoCodeDefineModel> infoCodeToFuncid = Maps.newHashMap();
                        if(CollectionUtils.isNotEmpty(infoCodeDefineModels)){
                            infoCodeToFuncid = infoCodeDefineModels.stream().collect(Collectors.toMap(InfoCodeDefineModel::getCode, e -> e));
                        }
                        IotFieldRecord iotFieldRecord = new IotFieldRecord();
                        iotFieldRecord.setObjName(dtpRwdObjectDTO.getPumpGroupName());
                        iotFieldRecord.setObjId(dtpRwdObjectDTO.getPumpGroupId());
                        iotFieldRecord.setInfoName("开关状态");
                        iotFieldRecord.setInfoCode(InfoCodeEnum.SWITCH_STATUS.getCode());
                        iotFieldRecord.setInfoCodeDefineModel(infoCodeToFuncid.get(InfoCodeEnum.SWITCH_STATUS.getCode()));
                        iotFieldRecord.setIotValueSequences(buildIotValueSequence(switchStatusMap.getOrDefault(dtpRwdObjectDTO.getPumpGroupId(), Maps.newHashMap())));
                        iotFieldRecordList.add(iotFieldRecord);
                }
                actualRunRecordData.put(controlZoneId,iotFieldRecordList);
            }else {
                List<String> pumpIdList = pumGroups.stream().filter(item -> DtpRwdClassCodeEnum.ACCPC_GROUP.getValue().equals(item.getClassCode())).map(PumGroup::getPumpGroupId).collect(Collectors.toList());
                //根据关系 查询冷冻水泵控制组下的冷冻水泵：“设备组下的设备”(图类型编码为：CombineSubset，边类型编码为Eg2Eq)，from为设备组，to为设备
                List<DtpRwdObjectDTO> dtpRwdObjectEg = queryPumpToEg(pumpIdList);
                Map<String, List<InfoCodeDefineModel>> classCodeToFuncidCombineSubset = SpringUtil.getBean("runAnalysisReportCoolingSysRun",RunAnalysisReportCoolingSysRun.class).queryClassCode2InfoCodeDefines(dtpRwdObjectEg.stream().map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toList()));
                Map<String, String> eqIdToClassCodeCombineSubset = dtpRwdObjectEg.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdObjectDTO::getClassCode,(k1,k2) -> k1));

                List<InfoCodeValueVO> infoCodeValueVOSPump = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                        DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES,
                        dtpRwdObjectEg.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()),
                        Lists.newArrayList(WORK_FREQ,InfoCodeEnum.RUN_STATUS.getCode()),
                        DateUtils.getDayBoundaryByDate(reportDate, 0, Boolean.TRUE),
                        DateUtils.getDayBoundaryByDate(reportDate, 1, Boolean.TRUE));
                Map<String, Map<String, Map<LocalDateTime, Object>>> objectIdInfoCodeTimeValueMap = InfoCodeValueVO.listToObjectIdInfoCodeTimeValueMap(infoCodeValueVOSPump);

                List<IotFieldRecord> iotFieldRecordList = new ArrayList<>();
                for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectEg) {
                        String classCode = eqIdToClassCodeCombineSubset.get(dtpRwdObjectDTO.getId());
                        List<InfoCodeDefineModel> infoCodeDefineModels = classCodeToFuncidCombineSubset.get(classCode);
                        Map<String, InfoCodeDefineModel> infoCodeToFuncid = Maps.newHashMap();
                        if(CollectionUtils.isNotEmpty(infoCodeDefineModels)){
                            infoCodeToFuncid = infoCodeDefineModels.stream().collect(Collectors.toMap(InfoCodeDefineModel::getCode, e -> e));
                        }
                        IotFieldRecord iotFieldRecord = new IotFieldRecord();
                        iotFieldRecord.setObjName(dtpRwdObjectDTO.getLocalName());
                        iotFieldRecord.setObjId(dtpRwdObjectDTO.getId());
                        iotFieldRecord.setInfoName("水泵运行频率");
                        iotFieldRecord.setInfoCode(WORK_FREQ);
                        iotFieldRecord.setInfoCodeDefineModel(infoCodeToFuncid.get(WORK_FREQ));
                        iotFieldRecord.setRatedFlow(dtpRwdObjectDTO.dynamicAnalyzeProperties(RATED_FLOW));
                        iotFieldRecord.setRatedFlowUnit(infoCodeToFuncid.get(RATED_FLOW).getUnit());
                        //计算标识
                        iotFieldRecord.setStartRecordMark(startRecordMarkCalc(objectIdInfoCodeTimeValueMap.getOrDefault(dtpRwdObjectDTO.getId(),Maps.newHashMap()).getOrDefault(InfoCodeEnum.RUN_STATUS.getCode(),Maps.newHashMap())));
                        iotFieldRecord.setIotValueSequences(buildIotValueSequenceObject(objectIdInfoCodeTimeValueMap.getOrDefault(dtpRwdObjectDTO.getId(), Maps.newHashMap()).getOrDefault(WORK_FREQ,Maps.newHashMap())));
                        iotFieldRecordList.add(iotFieldRecord);
                }
                actualRunRecordData.put(controlZoneId,iotFieldRecordList);
                controlZoneIdToWaterPumpMap.put(controlZoneId,dtpRwdObjectEg);
            }
        }
        return new Tuple2(actualRunRecordData, controlZoneIdToWaterPumpMap);
    }

    public Boolean startRecordMarkCalc(Map<LocalDateTime, Object> iotValueMap){
        if(MapUtil.isEmpty(iotValueMap)){
            return null;
        }
        //判断iotValueMap value 有没有等于1的,只要有一个就返沪true
        return iotValueMap.values().stream().anyMatch(item -> "1".equals(String.valueOf(item)));
    }

    public List<IotValueSequence> buildIotValueSequence(Map<LocalDateTime, String> switchStatusMap) {
        List<IotValueSequence> iotValueSequenceList = new ArrayList<>();
        if (MapUtil.isEmpty(switchStatusMap)) {
            return iotValueSequenceList;
        }
        for (LocalDateTime timestamp : switchStatusMap.keySet()) {
            IotValueSequence iotValueSequence = new IotValueSequence();
            iotValueSequence.setTimestamp(timestamp);
            iotValueSequence.setValue(switchStatusMap.get(timestamp));
            iotValueSequenceList.add(iotValueSequence);
        }
        //在按照时间升序排列
        iotValueSequenceList.sort(Comparator.comparing(IotValueSequence::getTimestamp));
        return iotValueSequenceList;
    }
    public List<IotValueSequence> buildIotValueSequenceObject(Map<LocalDateTime, Object> switchStatusMap) {
        List<IotValueSequence> iotValueSequenceList = new ArrayList<>();
        if (MapUtil.isEmpty(switchStatusMap)) {
            return iotValueSequenceList;
        }
        for (LocalDateTime timestamp : switchStatusMap.keySet()) {
            IotValueSequence iotValueSequence = new IotValueSequence();
            iotValueSequence.setTimestamp(timestamp);
            iotValueSequence.setValue(switchStatusMap.get(timestamp));
            iotValueSequenceList.add(iotValueSequence);
        }
        //在按照时间升序排列
        iotValueSequenceList.sort(Comparator.comparing(IotValueSequence::getTimestamp));
        return iotValueSequenceList;
    }

    /**
     * 查询冷冻水泵控制组下的冷冻水泵：“设备组下的设备”(图类型编码为：CombineSubset，边类型编码为Eg2Eq)，from为设备组，to为设备
     */
    public List<DtpRwdObjectDTO> queryPumpToEg(List<String> pumpIds) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();

        // 设置关系：CombineSubset，边类型编码为Eg2Eq
        queryParam.setRelationTo(
                new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIP_IN_GROUP)
                        .setObjFrom(pumpIds)
        );

        // 添加基础字段
        addBaseColumns(queryParam);
        return dtpRwdClientService.listObject(queryParam);
    }

    /**
     * 查询设备调节记录
     * 按设备ID分组返回调节记录
     */
    private Map<String, AdjustmentRecords> queryEquipmentAdjustmentRecords(Map<String, List<PumGroup>> zoneToInstanceMap, LocalDate reportDate,Map<String, ControlZonesSwitchDto> controlZonesSwitchMap) {
        // 按单个空调控制分区展示；若空调控制分区是电动通断水阀服务的则走水阀的分类；若空调控制分区是末级泵组服务的则走末级泵组的分类；
        // 若既有电动通断水阀也有末级泵组，则也走末级泵组的分类，当有多个末级泵组时，将泵组名称作为分组名称按泵组的维度展示以下参数（水泵开启台数、水泵频率、供回水温差、供回水压差、最不利末端压差）；
        // 仅有一个泵组时不展示分组
        Map<String, AdjustmentRecords> controlZoneIdToAdjustmentRecords = Maps.newHashMap();
        Set<String> instanceIds = Sets.newHashSet();

        for (String controlZoneId : zoneToInstanceMap.keySet()) {
            List<PumGroup> dtpRwdObjectDTOS = zoneToInstanceMap.get(controlZoneId);
            //如果控制分区下只有电动水阀服务，则走电动水阀的分类
            AdjustmentRecords adjustmentRecord = new AdjustmentRecords();
            if (controlZonesSwitchMap.containsKey(controlZoneId)) {
                // 获取该设备类型的调节记录信息点编码
                ControlZonesSwitchDto controlZonesSwitchDto = controlZonesSwitchMap.get(controlZoneId);
                adjustmentRecord.setInfoName(controlZonesSwitchDto.getSwitchSetInfoCodeName());
                adjustmentRecord.setInfoCode(controlZonesSwitchDto.getSwitchSetInfoCode());
            }
           //若空调控制分区是末级泵组服务的则走末级泵组的分类；若既有电动通断水阀也有末级泵组，则也走末级泵组的分类
           List<AdjustmentRecords.AdjustmentRecordsPump> pumpList = new ArrayList<>();
           for (PumGroup pumGroup : dtpRwdObjectDTOS) {
               //排除水阀
               if(pumGroup.getClassCode().equals(DtpRwdClassCodeEnum.ACVVEO.getValue()) || CollectionUtils.isEmpty(pumGroup.getPumpGroupInfoCodes())){
                   continue;
               }
               AdjustmentRecords.AdjustmentRecordsPump pump = new AdjustmentRecords.AdjustmentRecordsPump();
               pump.setPumpName(pumGroup.getPumpGroupName());
               pump.setPumpId(pumGroup.getPumpGroupId());
               instanceIds.add(pumGroup.getPumpGroupId());
               List<IotFieldRecord>  iotFieldRecordList = Lists.newArrayList();
               //泵组信息点
               pumGroup.getPumpGroupInfoCodes().forEach(infoCode -> {
                   IotFieldRecord iotFieldRecord = new IotFieldRecord();
                   iotFieldRecord.setInfoName(infoCode.getInfoCodeName());
                   //泵组的取值不一样
                   iotFieldRecord.setInfoCode(StringUtils.isEmpty(infoCode.getSetInfoCode()) ? infoCode.getInfoCode() : infoCode.getSetInfoCode());
                   iotFieldRecordList.add(iotFieldRecord);
               });
               pump.setIotFieldRecords(iotFieldRecordList);
               pumpList.add(pump);
           }
           adjustmentRecord.setAdjustmentRecordsPumps(pumpList);
           controlZoneIdToAdjustmentRecords.put(controlZoneId,adjustmentRecord);
        }
        instanceIds.addAll(Lists.newArrayList(controlZoneIdToAdjustmentRecords.keySet().stream().collect(Collectors.toList())));
        //查询实例信息
        Map<String, String> eqIdToClassCode = dtpRwdClientServiceImpl.listObject(new DtpRwdObjectQueryParam().setId(instanceIds).addWithColumn(
                        DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode)).stream()
                .filter(item -> ObjectUtils.allNotNull(item.getId(), item.getClassCode()))
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdObjectDTO::getClassCode, (o1, o2) -> o1));

        Map<String, List<InfoCodeDefineModel>> classCodeToFuncid = SpringUtil.getBean("runAnalysisReportCoolingSysRun",RunAnalysisReportCoolingSysRun.class).queryClassCode2InfoCodeDefines(eqIdToClassCode.values().stream().distinct().collect(Collectors.toList()));
        // 处理调节记录数据
        handlerAdjustRecordData(controlZoneIdToAdjustmentRecords,  reportDate, classCodeToFuncid,eqIdToClassCode);
        return controlZoneIdToAdjustmentRecords;
    }

    /**
     * 查询设备IOT数据
     */
    private void handlerAdjustRecordData(Map<String, AdjustmentRecords> controlZoneIdToAdjustmentRecords, LocalDate reportDate, Map<String, List<InfoCodeDefineModel>> classCodeToFuncid,Map<String, String> eqIdToClassCode) {
        //判空
        if (MapUtils.isEmpty(controlZoneIdToAdjustmentRecords)) {
            return ;
        }
        //控制分区id集合
        Set<String> controlZoneIds = new HashSet<>(controlZoneIdToAdjustmentRecords.keySet());
        //泵组id集合
        Set<String> pumpIdIds = new HashSet<>();
        //要查询的信息点
        Set<String> infoCodes = Sets.newHashSet();

        //泵组id集合
        controlZoneIdToAdjustmentRecords.values().forEach(pump -> {
            List<AdjustmentRecords.AdjustmentRecordsPump> adjustmentRecordsPumps = pump.getAdjustmentRecordsPumps();
            if(CollectionUtils.isNotEmpty(adjustmentRecordsPumps)){
                pumpIdIds.addAll(adjustmentRecordsPumps.stream().map(AdjustmentRecords.AdjustmentRecordsPump::getPumpId).filter(StringUtils::isNotEmpty).collect(Collectors.toSet()));
                infoCodes.addAll(adjustmentRecordsPumps.stream().flatMap(e -> e.getIotFieldRecords().stream().map(IotFieldRecord::getInfoCode)).filter(StringUtils::isNotEmpty).collect(Collectors.toSet()));
            }
            if(StringUtils.isNotEmpty(pump.getInfoCode())){
                infoCodes.add(pump.getInfoCode());
            }
        });
        controlZoneIds.addAll(pumpIdIds);

        //查询 根据冷冻水泵控制组接口返回的大小泵清单
        Map<String, Map<ChillerSize, List<String>>> pumpIdToPumpSizeMap = dtpRwdClientServiceImpl.queryPumpGroups(pumpIdIds);
        //将pumpIdToPumpSizeMap 中的List<String> 获取出来
        List<String> allLists = pumpIdToPumpSizeMap.values().stream()
                .flatMap(innerMap -> innerMap.values().stream())
                .flatMap(Collection::stream).collect(Collectors.toList());
        List<InfoCodeValueVO> infoCodeValueVOSPump = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES,
                allLists,
                Lists.newArrayList("equipSwitchSet"),
                DateUtils.getDayBoundaryByDate(reportDate, 0, Boolean.TRUE),
                DateUtils.getDayBoundaryByDate(reportDate, 1, Boolean.TRUE)
        );
        Map<String, Map<String, Map<LocalDateTime, Object>>> objectIdInfoCodeTimeValuePumpMap =
                InfoCodeValueVO.listToObjectIdInfoCodeTimeValueMap(infoCodeValueVOSPump);


        // 查询基础数据 （5分钟间隔）
        List<InfoCodeValueVO> infoCodeValueVOS = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES,
                controlZoneIds,
                infoCodes,
                DateUtils.getDayBoundaryByDate(reportDate, 0, Boolean.TRUE),
                DateUtils.getDayBoundaryByDate(reportDate, 1, Boolean.TRUE)
        );
        if (CollectionUtils.isEmpty(infoCodeValueVOS)) {
            return ;
        }

        Map<String, Map<String, Map<LocalDateTime, Object>>> objectIdInfoCodeTimeValueMap =
                InfoCodeValueVO.listToObjectIdInfoCodeTimeValueMap(infoCodeValueVOS);

        for (String controlZoneId : controlZoneIdToAdjustmentRecords.keySet()) {
            //获取调节记录
            AdjustmentRecords adjustmentRecords = controlZoneIdToAdjustmentRecords.get(controlZoneId);
            //1、处理分区开关
            Map<String, Map<LocalDateTime, Object>> infoCodeToIotMap = objectIdInfoCodeTimeValueMap.get(controlZoneId);
            if(MapUtil.isNotEmpty(infoCodeToIotMap) && infoCodeToIotMap.containsKey(InfoCodeEnum.AIR_CONDITION_DEMAND_ACZONE.getCode())){
                String classCode = eqIdToClassCode.get(controlZoneId);
                List<InfoCodeDefineModel> infoCodeDefineModels = classCodeToFuncid.get(classCode);
                Map<String, InfoCodeDefineModel> infoCodeToFuncid = Maps.newHashMap();
                if(CollectionUtils.isNotEmpty(infoCodeDefineModels)){
                    infoCodeToFuncid = infoCodeDefineModels.stream().collect(Collectors.toMap(InfoCodeDefineModel::getCode, e -> e));
                }
                adjustmentRecords.setInfoCodeDefineModel(infoCodeToFuncid.get(InfoCodeEnum.AIR_CONDITION_DEMAND_ACZONE.getCode()));
                Map<LocalDateTime, Object> timeSeriesData = infoCodeToIotMap.get(InfoCodeEnum.AIR_CONDITION_DEMAND_ACZONE.getCode());
                List<IotValueSequence> iotValueSequences = timeSeriesData.entrySet().stream()
                        .map(entry -> {
                            IotValueSequence iotValueSequence = new IotValueSequence();
                            iotValueSequence.setTimestamp(entry.getKey());
                            iotValueSequence.setValue(entry.getValue());
                            return iotValueSequence;
                        }).collect(Collectors.toList());
                //按照时间戳升序
                iotValueSequences.sort(Comparator.comparing(IotValueSequence::getTimestamp));
                adjustmentRecords.setIotValueSequences(iotValueSequences);
            }
            //处理泵组
            if(CollectionUtils.isNotEmpty(adjustmentRecords.getAdjustmentRecordsPumps())){
                List<AdjustmentRecords.AdjustmentRecordsPump> adjustmentRecordsPumps = adjustmentRecords.getAdjustmentRecordsPumps();
                for (AdjustmentRecords.AdjustmentRecordsPump adjustmentRecordsPump : adjustmentRecordsPumps) {
                    String pumpId = adjustmentRecordsPump.getPumpId();
                    Map<String, Map<LocalDateTime, Object>> infoCodeToIotPumpMap = objectIdInfoCodeTimeValueMap.get(pumpId);
                    String pumpClassCode = eqIdToClassCode.get(pumpId);
                    List<InfoCodeDefineModel> infoCodeDefineModels = classCodeToFuncid.get(pumpClassCode);
                    Map<String, InfoCodeDefineModel> infoCodeToFuncid = Maps.newHashMap();
                    if(CollectionUtils.isNotEmpty(infoCodeDefineModels)){
                        infoCodeToFuncid = infoCodeDefineModels.stream().collect(Collectors.toMap(InfoCodeDefineModel::getCode, e -> e));
                    }
                    if(MapUtil.isNotEmpty(infoCodeToIotPumpMap) ){
                        for (IotFieldRecord iotFieldRecord : adjustmentRecordsPump.getIotFieldRecords()) {
                            if("PUMP_GROUP_BASE".equals(iotFieldRecord.getInfoCode())){
                                //特殊处理开启台数
                                handLargeM1SmallCount(iotFieldRecord, objectIdInfoCodeTimeValuePumpMap, pumpIdToPumpSizeMap.get(pumpId),reportDate);
                            }else {
                                //枚举待对接
                                iotFieldRecord.setInfoCodeDefineModel(infoCodeToFuncid.get(iotFieldRecord.getInfoCode()));
                                Map<LocalDateTime, Object> timeSeriesData = infoCodeToIotPumpMap.get(iotFieldRecord.getInfoCode());
                                if(MapUtil.isNotEmpty(timeSeriesData)){
                                    List<IotValueSequence> iotValueSequences = timeSeriesData.entrySet().stream()
                                            .map(entry -> {
                                                IotValueSequence iotValueSequence = new IotValueSequence();
                                                iotValueSequence.setTimestamp(entry.getKey());
                                                iotValueSequence.setValue(entry.getValue());
                                                return iotValueSequence;
                                            }).collect(Collectors.toList());
                                    //按照时间戳升序
                                    iotValueSequences.sort(Comparator.comparing(IotValueSequence::getTimestamp));
                                    iotFieldRecord.setIotValueSequences(iotValueSequences);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理大中小机台数
     * @param iotFieldRecord
     * @param objectIdInfoCodeTimeValuePumpMap
     * @param LargeM1SmallToInstanceIdMap
     * @param reportDate
     */
    private void handLargeM1SmallCount(IotFieldRecord iotFieldRecord,
                                       Map<String, Map<String, Map<LocalDateTime, Object>>> objectIdInfoCodeTimeValuePumpMap,
                                       Map<ChillerSize, List<String>> LargeM1SmallToInstanceIdMap,
                                       LocalDate reportDate) {

        Map<ChillerSize,Map<LocalDateTime, Integer>> chillerSizeToTimeToCountMap = new HashMap<>();
        for (ChillerSize chillerSize : LargeM1SmallToInstanceIdMap.keySet()) {
            List<String> instanceIds = LargeM1SmallToInstanceIdMap.get(chillerSize);
            Map<LocalDateTime, Integer> timeToCountMap = new HashMap<>();
            for (String instanceId : instanceIds) {
                if(objectIdInfoCodeTimeValuePumpMap.containsKey(instanceId) && objectIdInfoCodeTimeValuePumpMap.get(instanceId).containsKey("equipSwitchSet")){
                    Map<String, Map<LocalDateTime, Object>> infoCodeToIotMap = objectIdInfoCodeTimeValuePumpMap.get(instanceId);
                    if(MapUtil.isNotEmpty(infoCodeToIotMap)){
                        Map<LocalDateTime, Object> localDateTimeObjectMap = objectIdInfoCodeTimeValuePumpMap.get(instanceId).get("equipSwitchSet");
                        localDateTimeObjectMap.forEach((time, value) -> {
                            if(Objects.nonNull(value) && String.valueOf(value).equals("1")){
                                timeToCountMap.put(time, timeToCountMap.getOrDefault(time, 0) + 1);
                            }
                        });
                    }
                }
            }
              chillerSizeToTimeToCountMap.put(chillerSize, timeToCountMap);
        }
        //补齐时间刻度
        List<LocalDateTime> periodWithTimeRangeFiveMinutes = getPeriodWithTimeRangeFiveMinutes(DateUtils.getDayBoundaryByDate(reportDate, 0, Boolean.TRUE), DateUtils.getDayBoundaryByDate(reportDate, 1, Boolean.TRUE));

        //判断单位
        String unit;
        //只有大机，没有中机和小机的情况单位是台
        if (LargeM1SmallToInstanceIdMap.containsKey(ChillerSize.LARGE) &&
                !LargeM1SmallToInstanceIdMap.containsKey(ChillerSize.M1) &&
                !LargeM1SmallToInstanceIdMap.containsKey(ChillerSize.M2) &&
                !LargeM1SmallToInstanceIdMap.containsKey(ChillerSize.M3) &&
                !LargeM1SmallToInstanceIdMap.containsKey(ChillerSize.SMALL)) {
            unit = "台";
        } else {
            unit = "";
        }

        List<IotValueSequence> iotValueSequences = Lists.newArrayList();
        periodWithTimeRangeFiveMinutes.forEach(time -> {
            IotValueSequence iotValueSequence = new IotValueSequence();
            iotValueSequence.setTimestamp(time);
            //不为空
            Integer largeCount = getCount(time, chillerSizeToTimeToCountMap.get(ChillerSize.LARGE));
            if(null != largeCount){
                iotValueSequence.setValue(largeCount+ (StringUtils.isNotEmpty(unit)?unit: "大"));
            }else {
                //没有数据且包含大机显示0
                if(StringUtils.isEmpty(unit) && LargeM1SmallToInstanceIdMap.containsKey(ChillerSize.LARGE)){
                    iotValueSequence.setValue(Objects.nonNull(iotValueSequence.getValue())? iotValueSequence.getValue() + "0大":"0大");
                }
            }
            Integer m1Count = getCount(time, chillerSizeToTimeToCountMap.get(ChillerSize.M1));
            if(null != m1Count){
                iotValueSequence.setValue(Objects.nonNull(iotValueSequence.getValue())?String.valueOf(iotValueSequence.getValue())+m1Count+"中":m1Count+"中");
            }else {
                //没有数据且包含中机显示0
                if(StringUtils.isEmpty(unit) && (LargeM1SmallToInstanceIdMap.containsKey(ChillerSize.M1) ||
                        LargeM1SmallToInstanceIdMap.containsKey(ChillerSize.M2) || LargeM1SmallToInstanceIdMap.containsKey(ChillerSize.M3))){
                    iotValueSequence.setValue(Objects.nonNull(iotValueSequence.getValue())? iotValueSequence.getValue() + "0中":"0中");
                }
            }
            Integer smallCount = getCount(time, chillerSizeToTimeToCountMap.get(ChillerSize.SMALL));
            if(null != smallCount){
                iotValueSequence.setValue(Objects.nonNull(iotValueSequence.getValue())? String.valueOf(iotValueSequence.getValue())+smallCount+"小":smallCount+"小");
            }else {
                //没有数据且包含中机显示0
                if(StringUtils.isEmpty(unit) && LargeM1SmallToInstanceIdMap.containsKey(ChillerSize.SMALL)){
                    iotValueSequence.setValue(Objects.nonNull(iotValueSequence.getValue())? iotValueSequence.getValue() + "0小":"0小");
                }
            }
            if(null != iotValueSequence.getValue()){
                iotValueSequences.add(iotValueSequence);
            }
        });
        iotFieldRecord.setIotValueSequences(iotValueSequences);
    }

    private Integer getCount(LocalDateTime time,Map<LocalDateTime, Integer> timeToCountMap){
        if(MapUtil.isNotEmpty(timeToCountMap)){
          return timeToCountMap.get(time);
        }
        return null;
    }

    /**
     　　* @author weiyizhong
     * @Company yushu.biz
    　　 * @date 2025/1/14 16:20
    　　 * @description: 补齐时间刻度
     */
    public static List<LocalDateTime> getPeriodWithTimeRangeFiveMinutes(LocalDateTime startTime, LocalDateTime endTime) {
        List<LocalDateTime> resultList = org.apache.commons.compress.utils.Lists.newArrayList();
        getResultList(startTime,resultList,  getDifferenceInFiveMinutes(startTime, endTime),(time) -> time.plusMinutes(5));
        return resultList;
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2025/1/14 16:46
    　　 * @description: 生成间隔时刻
     */
    public static void getResultList(LocalDateTime startTime, List<LocalDateTime> resultList, long count, Function<LocalDateTime,LocalDateTime> function){
        for (int i = 0; i < count; i++) {
            resultList.add(startTime);
            startTime = function.apply(startTime);
        }
    }

    /**
     * 获取开始结束时间之间相差的五分钟数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 相差五分钟的个数
     */
    public static long getDifferenceInFiveMinutes(LocalDateTime startTime, LocalDateTime endTime) {
        // 计算两个时间点之间的分钟差
        long minutesBetween = ChronoUnit.MINUTES.between(startTime, endTime);
        // 除以5得到五分钟的差值
        return minutesBetween / 5;
    }

    /**
     * 添加基础查询字段
     */
    private void addBaseColumns(DtpRwdObjectQueryParam queryParam) {
        for (String column : BASE_COLUMNS) {
            queryParam.addWithColumn(column);
        }
    }

    /**
     * 添加基础字段 + 额外字段
     */
    private void addColumns(DtpRwdObjectQueryParam queryParam, String... extraColumns) {
        addBaseColumns(queryParam);
        for (String column : extraColumns) {
            queryParam.addWithColumn(column);
        }
    }

    @Override
    public String logTag() {
        return "运行分析报告-空调控制分区";
    }
}
