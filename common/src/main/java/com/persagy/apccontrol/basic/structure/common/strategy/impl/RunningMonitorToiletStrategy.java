package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.instance.SpaceVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.instance.converter.SpaceConverter;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wxf
 * @description 卫生间运行监测策略
 * @date 15:13 2024/12/13
 **/
@Component(RunningMonitorToiletStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorToiletStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorToiletStrategy";
    public static final String LOG_TAG = "运行监测-卫生间";
    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final SpaceConverter spaceConverter;
    private final DtpRwdConvert dtpRwdConvert;
    private static final Set<String> FUNCTION_CODES = Sets.newHashSet("SFT-01.06.01", "SFT-01.06.02", "SFT-01.06.03", "SFT-01.06.05");


    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.TOILET;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public List<SpaceVO> listDashboardSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        List<SpaceVO> result = new ArrayList<>();
        //查空间
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        param.setFilterClassCodes(Sets.newHashSet(DtpRwdClassCodeEnum.SPACE.getValue()));
        param.setFilterRoomFuncTypes(FUNCTION_CODES);
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = param.queryObject(queryParam);
        if(CollectionUtil.isEmpty(dtpRwdSpaceDTOS)){
            return result;
        }

        DtpKnowledgeInfoCodeDTO roomFuncTypeDefined = dtpKnowledgeClientService.getInfoCode(false,
                DtpRwdClassCodeEnum.SPACE.value(), DtpRwdSpaceDTO.Fields.roomFuncType);
        if (roomFuncTypeDefined == null) {
            return result;
        }

        Map<String, String> roomFuncTypeMap = roomFuncTypeDefined.toDataSourceCodeAndName();
        Map<String, DtpRwdSpaceDTO> spaceMap = dtpRwdSpaceDTOS.stream().collect(Collectors.toMap(DtpRwdSpaceDTO::getId, Function.identity(), (v1, v2) -> v2));
        Set<String> spaceIds = spaceMap.keySet();
        //查空间实例化规则
        Set<String> infoCodeList = Sets.newHashSet(InfoCodeEnum.SPACE_TDB.getCode(),
                InfoCodeEnum.NH3.getCode(), InfoCodeEnum.RH.getCode(),
                InfoCodeEnum.H2S.getCode(), InfoCodeEnum.PM2D5.getCode());
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
        classCodeAndObjectIds.put(CommonConst.DtpRwdConst.GeneralZone, spaceIds);
        Map<String, Set<String>> ruleSetMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndObjectIds, infoCodeList);
        Set<String> hasRuleSpaceIds = new HashSet<>();
        if(MapUtil.isNotEmpty(ruleSetMap)){
            ruleSetMap.forEach((k, v) -> {
                if (CollectionUtils.containsAny(v, infoCodeList)) {
                    hasRuleSpaceIds.add(k);
                }
            });
        }

        //spaceIds = Sets.newHashSet(CollectionUtil.intersection(spaceIds, hasRuleSpaceIds));
        
        //查空间下的卫生间隔间
        List<DtpRwdObjectDTO> cfidbpList = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setClassCodeEnum(DtpRwdClassCodeEnum.CFIDBP)
                .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjTo(spaceIds))
                .filterMonitoringSurveillanceStatus()
                .setWithColumns(param.getWithColumns())
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds));

        //两种情况满足任意一种
        Map<String, List<DtpRwdObjectDTO>> cfidbpListMap = DtpRwdSpaceDTO.mapRelObjIdAndData(cfidbpList);
        if (cfidbpListMap != null) {
            hasRuleSpaceIds.addAll(cfidbpListMap.keySet());
        }

        //查设备
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS1 = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setClassCodeEnum(
                Arrays.asList(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT, DtpRwdClassCodeEnum.SUPPLY_FAN,
                        DtpRwdClassCodeEnum.ACATAH, DtpRwdClassCodeEnum.ACATFC,
                        DtpRwdClassCodeEnum.FFSCMF, DtpRwdClassCodeEnum.FFSCAF))
                .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(hasRuleSpaceIds))
                .filterMonitoringSurveillanceStatus()
                .setWithColumns(param.getWithColumns())
                .addWithColumn(DtpRwdObjectDTO.Fields.relObjIds));

        //过滤 funcType 1or2 的 ACVTSF
        if(CollectionUtil.isNotEmpty(dtpRwdObjectDTOS1)) {
            dtpRwdObjectDTOS1 = dtpRwdObjectDTOS1.stream().filter(dtpRwdObjectDTO -> {
                return !dtpRwdObjectDTO.getClassCode().equals(DtpRwdClassCodeEnum.ACVTSF.value()) ||
                        "1".equals(dtpRwdObjectDTO.analyzeProperties("funcType")) ||
                        "2".equals(dtpRwdObjectDTO.analyzeProperties("funcType"));
            }).collect(Collectors.toList());
        }
        Map<String, List<DtpRwdObjectDTO>> stringListMap = DtpRwdSpaceDTO.mapRelObjIdAndData(dtpRwdObjectDTOS1);

        //查传感器
        /*
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS2 = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setClassCodeEnum(
                Arrays.asList(DtpRwdClassCodeEnum.WESSBP, DtpRwdClassCodeEnum.WESSCB,
                        DtpRwdClassCodeEnum.WESSTP, DtpRwdClassCodeEnum.WESSCP,
                        DtpRwdClassCodeEnum.WESSTO, DtpRwdClassCodeEnum.WESSCT, DtpRwdClassCodeEnum.WESSTH))
                .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIP_MONITOR_IN_SPACE).setObjTo(spaceIds))
                .filterMonitoringSurveillanceStatus()
                .setWithColumns(param.getWithColumns())
                .addWithColumn(DtpRwdObjectDTO.Fields.relObjIds));
        Map<String, List<DtpRwdObjectDTO>> stringListMap1 = DtpRwdSpaceDTO.mapRelObjIdAndData(dtpRwdObjectDTOS2);
         */

        for (String spaceId : hasRuleSpaceIds) {
            DtpRwdSpaceDTO space = spaceMap.get(spaceId);
            SpaceVO resultItem = spaceConverter.toSpaceVo(space);
            resultItem.setRoomFuncTypeName(roomFuncTypeMap.get(space.getRoomFuncType()));
            List<DtpRwdObjectDTO>  equipments =  new ArrayList<>();
            equipments.addAll(cfidbpListMap.getOrDefault(spaceId, Lists.newArrayList()));
            equipments.addAll(stringListMap.getOrDefault(spaceId, Lists.newArrayList()));
            //equipments.addAll(stringListMap1.getOrDefault(spaceId, Lists.newArrayList()));
            resultItem.setEquipments(equipments);
            result.add(resultItem);
        }

        return result;
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
