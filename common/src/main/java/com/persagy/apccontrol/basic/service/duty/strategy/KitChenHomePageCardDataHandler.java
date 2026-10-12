package com.persagy.apccontrol.basic.service.duty.strategy;

import cn.hutool.core.collection.CollUtil;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.kitchen.KitChenRangeHoodEquipStatisticsParamEnum;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.duty.CardDateVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.ChildCardDataVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.kitchen.KitchenRangeHoodService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.service.kitchen.KitchenRangeHoodService.*;

@Service
@Slf4j
public class KitChenHomePageCardDataHandler extends AbstractHomePageCardDataHandler<CardDateVO<ChildCardDataVO>> {

    @Resource
    private KitchenRangeHoodService kitChenService;

    @Resource
    private DtpRwdClientService dtpRwdClientService;


    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.KITCHEN_RANGE_HOOD;
    }

    @Override
    public CardDateVO<ChildCardDataVO> execute(HomePageParam param) {
        CardDateVO<ChildCardDataVO> resultVo = new CardDateVO<>();
        resultVo.setRunningMonitorType(getRunningMonitorType());
        Map<String, Set<String>> paramMap = kitChenService.handlerStaticAggregateToMap(ContextUtil.getGroupCode(), ContextUtil.getProjectId());
        if (paramMap.isEmpty()) {
            return resultVo;
        }
        List<String> forceCodes = Collections.singletonList(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode());
        Map<String, ObjectIdAndForceCodeQuery> queryMap = new HashMap<>();
        Set<String> objIds = new HashSet<>();
        paramMap.values().forEach(objIds::addAll);
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setId(objIds)
                .setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id, CommonConst.MONITORING_SURVEILLANCE_STATUS));
        List<DtpRwdEquipmentDTO> equipmentDTOS =  dtpRwdClientService.listEquipment(queryParam);
        filtrationMonitoringSurveillanceStatus(equipmentDTOS);
        if(CollectionUtils.isEmpty(equipmentDTOS)){
            return resultVo;
        }
        List<String> fllterIds = equipmentDTOS.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList());


        Set<String> acklsfObjIds = paramMap.get(KitChenRangeHoodEquipStatisticsParamEnum.ACKLSF_OBJIDS.getValue());
        if(CollUtil.isNotEmpty(acklsfObjIds)) {
            acklsfObjIds = acklsfObjIds.stream().filter(fllterIds::contains).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty((acklsfObjIds))) {
                queryMap = ObjectIdAndForceCodeQuery.buildDutyHomePageQuery(ACKLSF, acklsfObjIds, new HashSet<>(forceCodes));
            }
        }

        //补风风机
        Set<String> acatfuObjIds = paramMap.get(KitChenRangeHoodEquipStatisticsParamEnum.ACATFU_OBJIDS.getValue());
        if(CollUtil.isNotEmpty(acatfuObjIds)) {
            acatfuObjIds = acatfuObjIds.stream().filter(fllterIds::contains).collect(Collectors.toSet());
        }

        Set<String> acvtsfObjIds = paramMap.get(KitChenRangeHoodEquipStatisticsParamEnum.ACVTSF_OBJIDS.getValue());
        if(CollUtil.isNotEmpty(acvtsfObjIds)) {
            acvtsfObjIds = acvtsfObjIds.stream().filter(fllterIds::contains).collect(Collectors.toSet());
        }

        Set<String> acvtsfAndAcatfu = new HashSet<>();
        if (CollectionUtils.isNotEmpty((acatfuObjIds))) {
            acvtsfAndAcatfu.addAll(acatfuObjIds);
        }
        if (CollectionUtils.isNotEmpty((acvtsfObjIds))) {
            acvtsfAndAcatfu.addAll(acvtsfObjIds);
        }
        if (CollectionUtils.isNotEmpty(acvtsfAndAcatfu)) {
            ObjectIdAndForceCodeQuery.addDutyHomePageQuery(queryMap, ACATFU_ACVTSF, acvtsfAndAcatfu, new HashSet<>(forceCodes));
        }

        //油烟净化器
        Set<String> acklapObjIds = paramMap.get(KitChenRangeHoodEquipStatisticsParamEnum.ACKLAP_OBJIDS.getValue());
        if(CollUtil.isNotEmpty(acklapObjIds)) {
            acklapObjIds = acklapObjIds.stream().filter(fllterIds::contains).collect(Collectors.toSet());
        }

        if (CollectionUtils.isNotEmpty((acklapObjIds))) {
            ObjectIdAndForceCodeQuery.addDutyHomePageQuery(queryMap, ACKLAP, acklapObjIds, new HashSet<>(forceCodes));
        }
        //获取聚合接口结果
        DutyHomePageGroupDTO dutyHomePageGroup = queryHomePageCardGroupIotData(queryMap);
        //构建大卡片信息的标签
        CardTagVO tag = buildCardTagVO(dutyHomePageGroup, objIds);
        resultVo.setCardTag(tag);
        //填充小卡片信息
        resultVo.setChildCardList(buildChildCardDateVO(dutyHomePageGroup.getSubStat()));
        return resultVo;
    }
}
