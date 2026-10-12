package com.persagy.apccontrol.basic.service.duty.strategy;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.ClassCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpKnowledgeClassCodeQueryParam;
import com.persagy.apccontrol.basic.model.vo.duty.CardDateVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.ChildCardDataVO;
import com.persagy.apccontrol.basic.service.knowledge.BizDtpKnowledgeService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirTerminalStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.service.airterminal.AirTerminalService.ALL_EQUIPMENT_CLASS_CODES;

@Service
@Slf4j
public class AirTerminalHomePageCardDataHandler extends AbstractHomePageCardDataHandler<CardDateVO<ChildCardDataVO>> {

    @Resource
    private RunningMonitorAirTerminalStrategy runningMonitorAirTerminalStrategy;
    @Autowired
    private BizDtpKnowledgeService bizDtpKnowledgeService;
    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.AIR_TERMINAL;
    }

    @Override
    public CardDateVO<ChildCardDataVO> execute(HomePageParam param) {

        CardDateVO<ChildCardDataVO> resultVo = new CardDateVO<>();
        resultVo.setRunningMonitorType(getRunningMonitorType());

        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.setFilterExcludeClassCodes(Sets.newHashSet(
                DtpRwdClassCodeEnum.ACAHG_GROUP.getValue(), DtpRwdClassCodeEnum.ACVVER.getValue(),
                DtpRwdClassCodeEnum.ACVVEA.getValue(), DtpRwdClassCodeEnum.ACATHT.getValue(),
                DtpRwdClassCodeEnum.ACVVEJ.getValue()
        ));
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        List<DtpRwdEquipmentDTO> equipmentDTOS = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam);
        if (CollectionUtil.isEmpty(equipmentDTOS)) {
            return resultVo;
        }
        Set<String> forceCodes = Collections.singleton(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode());
        Set<String> equipmentIds = equipmentDTOS.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        Map<String, Set<String>> mapByClassCode = equipmentDTOS.stream().collect(Collectors.groupingBy(DtpRwdEquipmentDTO::getClassCode, Collectors.mapping(DtpRwdEquipmentDTO::getId, Collectors.toSet())));
        Map<String, ObjectIdAndForceCodeQuery> queryMap = new HashMap<>();
        AtomicReference<ClassCodeDefineModel> classCodeEnum = new AtomicReference<>();
        
        Set<String> classCodes = new HashSet<>();
        ALL_EQUIPMENT_CLASS_CODES.forEach(classCodeEnumTemp -> classCodes.add(classCodeEnumTemp.value()));
        DtpKnowledgeClassCodeQueryParam classCodeParam = new DtpKnowledgeClassCodeQueryParam();
        classCodeParam.setQueryCache(false);
        classCodeParam.setClassCodes(classCodes);
        List<ClassCodeDefineModel> classCodeName = bizDtpKnowledgeService.listClassCodes(classCodeParam);

        for (Map.Entry<String, Set<String>> entry : mapByClassCode.entrySet()) {
            classCodeName.forEach(classCod -> {
                if (StringUtils.equals(classCod.getCode(), entry.getKey())) {
                    classCodeEnum.set(classCod);
                }
            });
            
            String childCardName = getCodeAndeName(classCodeEnum.get().getCode(), classCodeEnum.get().getName());
            if (queryMap.isEmpty()) {
                queryMap = ObjectIdAndForceCodeQuery.buildDutyHomePageQuery(childCardName,
                        entry.getValue(), forceCodes);
            } else {
                ObjectIdAndForceCodeQuery.addDutyHomePageQuery(queryMap, childCardName,
                        entry.getValue(), forceCodes);
            }
        }
        //获取聚合接口结果
        DutyHomePageGroupDTO dutyHomePageGroup = queryHomePageCardGroupIotData(queryMap);
        //构建大卡片信息的标签
        CardTagVO tag = buildCardTagVO(dutyHomePageGroup, equipmentIds);
        resultVo.setCardTag(tag);
        //填充小卡片信息
        resultVo.setChildCardList(buildChildCardDateVO(dutyHomePageGroup.getSubStat()));
        return resultVo;
    }
}
