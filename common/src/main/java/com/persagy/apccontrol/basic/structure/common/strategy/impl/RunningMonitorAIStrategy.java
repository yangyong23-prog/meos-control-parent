package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.vo.AI.AISystemVo;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(RunningMonitorAIStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorAIStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorAIStrategy";
    public static final String LOG_TAG = "运行监测-智能体";
    private final DtpRwdClientService dtpRwdClientService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final Set<String> CLASS_CODE= Sets.newHashSet(DtpRwdClassCodeEnum.SPACE.getValue(),
            DtpRwdClassCodeEnum.ACATAH.getValue(), DtpRwdClassCodeEnum.ACATFU.getValue(),
            DtpRwdClassCodeEnum.ACCCCC.getValue(), DtpRwdClassCodeEnum.ACCCSC.getValue(),
            DtpRwdClassCodeEnum.ACCCCP.getValue(), DtpRwdClassCodeEnum.ACCCOP.getValue(), DtpRwdClassCodeEnum.ACCCOT.getValue()
            );

    private final Set<String> INFO_CODE = Sets.newHashSet(InfoCodeEnum.RATEDP.getCode(), InfoCodeEnum.SUMMER_DESIGN_CHILL_WATER_FLOW.getCode(),
            InfoCodeEnum.SUPPLY_CAP.getCode(), InfoCodeEnum.RATED_FAN_FLOW.getCode(),
            InfoCodeEnum.DESIGN_CHILL_WATER_FLOW.getCode(), InfoCodeEnum.IS_COOL_COIL_SECTION.getCode(),
            InfoCodeEnum.IS_HEAT_RECOVERY_SECTION.getCode(),InfoCodeEnum.BUILD_AREA.getCode(),
            InfoCodeEnum.RATED_COOL.getCode(), InfoCodeEnum.RATED_COP.getCode(), InfoCodeEnum.DESIGN_EVAP_WATER_OUT_TEMP.getCode(),
            InfoCodeEnum.DESIGN_EVAP_WATER_IN_TEMP.getCode(), InfoCodeEnum.DESIGN_COND_WATER_IN_TEMP.getCode(),
            InfoCodeEnum.DESIGN_COND_WATER_OUT_TEMP.getCode(), InfoCodeEnum.DESIGN_COOL_WATER_FLOW.getCode(), InfoCodeEnum.RATED_FLOW.getCode(),
            InfoCodeEnum.RATED_WATER_IN_TEMP.getCode(), InfoCodeEnum.RATED_WATER_OUT_TEMP.getCode(),
            InfoCodeEnum.RATED_FAN_AIR_FLOW.getCode(), InfoCodeEnum.RATED_WATER_FLOW.getCode(),
            InfoCodeEnum.FAN_NUM.getCode());


    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.AI;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public List<? extends DtpRwdEquipmentDTO> listEquipments(@NonNull @Valid RunningMonitorEquipmentQueryParam param) {
        List<AISystemVo> aiSystems = new ArrayList<>();
        Set<String> systemIds = param.getFilterEquipmentInSystemIds();

        //系统服务空间
        Map<String, List<DtpRwdSpaceDTO>> spaceMap = new HashMap<>();
        Map<String, List<DtpRwdEquipmentDTO>> sp2equipmentMap = new HashMap<>();
        fillSpaceData( systemIds, param, spaceMap,sp2equipmentMap);

        //系统设备
        Map<String, List<DtpRwdEquipmentDTO>> sy2equipmentMap = new HashMap<>();
        fillEquipData(systemIds, param,sy2equipmentMap);

        //信息点定义
        Set<String> classCodes = new HashSet<>();
        Set<String> infoCodes = new HashSet<>();
        setDataBySystem(param.getFilterClassCodes(), classCodes, infoCodes);

        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCodeInfoCodeMap = dtpKnowledgeClientService.mapClassCodeInfoCodeAndDefine(true, true, classCodes);
        Map<String,DtpKnowledgeInfoCodeDTO> infoCodeDefines = new HashMap<>();
        classCodeInfoCodeMap.values().stream().forEach(infoCodeMap -> {
            Set<String> infoCodeKeys = infoCodeMap.keySet();
            Collection<String> intersectionKeys = CollectionUtil.intersection(infoCodeKeys, infoCodes);
            intersectionKeys.stream().forEach(infoCodeKey -> {
                DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO = infoCodeMap.get(infoCodeKey);
                dtpKnowledgeInfoCodeDTO.setUndefinedProperties(null);
                infoCodeDefines.put(infoCodeKey, dtpKnowledgeInfoCodeDTO);
            });
        });

        for (String systemId : systemIds) {
            AISystemVo aiSystem = new AISystemVo();
            List<DtpRwdSpaceDTO> spaces = spaceMap.getOrDefault(systemId, Collections.emptyList());
            aiSystem.setServiceSpaces(spaces);

            List<DtpRwdEquipmentDTO> allEquipments = new ArrayList<>();
            allEquipments.addAll(sy2equipmentMap.getOrDefault(systemId, Collections.emptyList()));
            for (DtpRwdSpaceDTO space : spaces) {
                allEquipments.addAll(sp2equipmentMap.getOrDefault(space.getId(), Collections.emptyList()));
            }

            Map<String, DtpRwdEquipmentDTO> equipmentMap = allEquipments.stream().collect(Collectors.toMap(
                    DtpRwdEquipmentDTO::getId,
                    Function.identity(),
                    (o1, o2) -> o1));

            Map<String, List<DtpRwdEquipmentDTO>> relEquipments = equipmentMap.values().stream().collect(Collectors.groupingBy(DtpRwdEquipmentDTO::getClassCode));
            aiSystem.setRelEquipments(relEquipments);
            aiSystem.setInfoCodeDefine(infoCodeDefines);

            aiSystems.add(aiSystem);
        }

        return aiSystems;
    }

    private void setDataBySystem(Set<String> systemClassCodes, Set<String> classCodes, Set<String> infoCodes) {
        if(systemClassCodes.contains(DtpRwdClassCodeEnum.ACCI_SYSTEM.getValue())){
            classCodes.addAll(Sets.newHashSet(DtpRwdClassCodeEnum.ACAHC_GROUP.getValue(),DtpRwdClassCodeEnum.ACCHCH.getValue(),
                    DtpRwdClassCodeEnum.ACATAH.getValue(),DtpRwdClassCodeEnum.ACATFU.getValue()));
            infoCodes.addAll(Sets.newHashSet(InfoCodeEnum.COOL_RATED_INDOOR_MEDUIM_FLOW.getCode(), InfoCodeEnum.VFD_MIN_FREQ.getCode(),
                    InfoCodeEnum.HEAT_RATED_INDOOR_MEDUIM_FLOW.getCode(), InfoCodeEnum.RATEDP.getCode(),
                    InfoCodeEnum.SUMMER_DESIGN_CHILL_WATER_FLOW.getCode(),InfoCodeEnum.SUPPLY_CAP.getCode(),
                    InfoCodeEnum.RATED_FAN_FLOW.getCode(),InfoCodeEnum.DESIGN_CHILL_WATER_FLOW.getCode(),
                    InfoCodeEnum.IS_COOL_COIL_SECTION.getCode(),InfoCodeEnum.IS_HEAT_RECOVERY_SECTION.getCode()));
        }else{
            classCodes.addAll(CLASS_CODE);
            infoCodes.addAll(INFO_CODE);
        }
    }

    private void fillEquipData(Set<String> systemIds, RunningMonitorEquipmentQueryParam param, Map<String, List<DtpRwdEquipmentDTO>> sy2equipmentMap){
        //系统下的设备
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setWithColumns(param.getWithColumns());
        if(param.getFilterClassCodes().contains(DtpRwdClassCodeEnum.ACCI_SYSTEM.getValue())){
            queryParam.setClassCode(DtpRwdClassCodeEnum.ACCHCH.getValue());
            queryParam.addWithColumn(InfoCodeEnum.RATEDP.getCode(), InfoCodeEnum.VFD_MIN_FREQ.getCode());
        }else {
            queryParam.setClassCode(Sets.newHashSet(DtpRwdClassCodeEnum.ACCCCC.getValue(), DtpRwdClassCodeEnum.ACCCSC.getValue(),
                    DtpRwdClassCodeEnum.ACCCCP.getValue(), DtpRwdClassCodeEnum.ACCCOP.getValue(), DtpRwdClassCodeEnum.ACCCOT.getValue()
            ));
            queryParam.addWithColumn(InfoCodeEnum.RATED_COOL.getCode(), InfoCodeEnum.RATEDP.getCode(),
                    InfoCodeEnum.RATED_COP.getCode(), InfoCodeEnum.DESIGN_EVAP_WATER_OUT_TEMP.getCode(),
                    InfoCodeEnum.DESIGN_EVAP_WATER_IN_TEMP.getCode(), InfoCodeEnum.DESIGN_COND_WATER_IN_TEMP.getCode(),
                    InfoCodeEnum.DESIGN_COND_WATER_OUT_TEMP.getCode(), InfoCodeEnum.DESIGN_CHILL_WATER_FLOW.getCode(),
                    InfoCodeEnum.DESIGN_COOL_WATER_FLOW.getCode(), InfoCodeEnum.RATED_FLOW.getCode(),
                    InfoCodeEnum.RATED_WATER_IN_TEMP.getCode(), InfoCodeEnum.RATED_WATER_OUT_TEMP.getCode(),
                    InfoCodeEnum.RATED_FAN_AIR_FLOW.getCode(), InfoCodeEnum.RATED_WATER_FLOW.getCode(),
                    InfoCodeEnum.FAN_NUM.getCode());
        }
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = dtpRwdClientService.listEquipmentInBySystemIds(systemIds, queryParam);

        queryParam.setWithColumns(param.getWithColumns());
        if(param.getFilterClassCodes().contains(DtpRwdClassCodeEnum.ACCI_SYSTEM.getValue())){
            queryParam.setClassCode(DtpRwdClassCodeEnum.ACAHC_GROUP);
            queryParam.addWithColumn(InfoCodeEnum.COOL_RATED_INDOOR_MEDUIM_FLOW.getCode(), InfoCodeEnum.HEAT_RATED_INDOOR_MEDUIM_FLOW.getCode());
        }else {
            //冷却塔控制单元
            queryParam.setClassCode(DtpRwdClassCodeEnum.ACOTU_GROUP);
        }
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(systemIds));
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS1 = dtpRwdClientService.listEquipment(queryParam);
        dtpRwdEquipmentDTOS.addAll(dtpRwdEquipmentDTOS1);

        if (CollectionUtil.isNotEmpty(dtpRwdEquipmentDTOS)) {
            sy2equipmentMap.putAll(DtpRwdEquipmentDTO.mapRelObjIdAndData(dtpRwdEquipmentDTOS));
            sy2equipmentMap.values().stream().flatMap(Collection::stream).forEach(item->{
                item.setRelObjIds(null);
            });
        }
    }

    private void fillSpaceData(Set<String> systemIds, RunningMonitorEquipmentQueryParam param, Map<String, List<DtpRwdSpaceDTO>> spaceMap,Map<String, List<DtpRwdEquipmentDTO>> sp2equipmentMap){
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(DtpRwdClassCodeEnum.SPACE);
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE).setObjFrom(systemIds));
        queryParam.setWithColumns(param.getWithColumns());
        queryParam.addWithColumnWithIdRelation();
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = dtpRwdClientService.listSpace(queryParam);
        if (CollectionUtil.isNotEmpty(dtpRwdSpaceDTOS)) {
            spaceMap.putAll(DtpRwdSpaceDTO.mapRelObjIdAndData(dtpRwdSpaceDTOS));
            spaceMap.values().stream().flatMap(Collection::stream).forEach(item->{
                item.setRelObjIds(null);
            });

            Set<String> spaceIds = dtpRwdSpaceDTOS.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            //设备服务于空间
            queryParam = new DtpRwdObjectQueryParam();
            queryParam.setWithColumns(param.getWithColumns());
            queryParam.setClassCode(Sets.newHashSet(DtpRwdClassCodeEnum.ACATAH.getValue(), DtpRwdClassCodeEnum.ACATFU.getValue()));
            queryParam.addWithColumn(InfoCodeEnum.RATEDP.getCode(), InfoCodeEnum.SUMMER_DESIGN_CHILL_WATER_FLOW.getCode(),
                    InfoCodeEnum.SUPPLY_CAP.getCode(), InfoCodeEnum.RATED_FAN_FLOW.getCode(),
                    InfoCodeEnum.DESIGN_CHILL_WATER_FLOW.getCode(), InfoCodeEnum.IS_COOL_COIL_SECTION.getCode(),
                    InfoCodeEnum.IS_HEAT_RECOVERY_SECTION.getCode());
            List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = dtpRwdClientService.listEquipmentServiceBySpaceIds(spaceIds, queryParam);
            sp2equipmentMap.putAll(DtpRwdEquipmentDTO.mapRelObjIdAndData(dtpRwdEquipmentDTOS));
            sp2equipmentMap.values().stream().flatMap(Collection::stream).forEach(item->{
                item.setRelObjIds(null);
            });
        }
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
