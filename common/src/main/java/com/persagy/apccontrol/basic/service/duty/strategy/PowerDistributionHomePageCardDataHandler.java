package com.persagy.apccontrol.basic.service.duty.strategy;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardDateVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.PowerDistributionCardDataVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS;
import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS_LIST;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PowerDistributionHomePageCardDataHandler extends AbstractHomePageCardDataHandler<CardDateVO<PowerDistributionCardDataVO>> {

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer iotDataCacheService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingHomePageCardDataHandler.class);
    private static final String LOG_TAG = "运行值班首页-变配电";
    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.POWER_DISTRIBUTION;
    }

    @Override
    public CardDateVO<PowerDistributionCardDataVO> execute(HomePageParam param) {
        CardDateVO<PowerDistributionCardDataVO> resultVo = new CardDateVO<>();
        resultVo.setChildCardList(new ArrayList<>());

        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST)
                .setClassCode(Arrays.asList(DtpRwdClassCodeEnum.SETDTF.getValue()))
                .setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id,DtpRwdObjectDTO.Fields.localName));
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = dtpRwdClientService.listEquipment(queryParam);

        if (CollectionUtil.isEmpty(dtpRwdEquipmentDTOS)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未获取到设备");
            return resultVo;
        }

        //Map<String,DtpRwdEquipmentDTO> dtpRwdEquipmentMap = dtpRwdEquipmentDTOS.stream().collect(Collectors.toMap(DtpRwdEquipmentDTO::getId, v -> v, (v1, v2) -> v1));
        Set<String> ids = dtpRwdEquipmentDTOS.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
        classCodeAndObjectIds.put(DtpRwdClassCodeEnum.SETDTF.getValue(), ids);
        //查实例化规则
        Map<String, Set<String>> ruleSetMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndObjectIds, Arrays.asList(InfoCodeEnum.L_U_LOAD_RATE.getCode()));
        Set<String> ruleIds = ruleSetMap.keySet();

        List<InfoCodeValueVO> infoCodeValues = iotDataCacheService.listInfoCodeValue(ContextUtil.getProjectId(), null, ruleIds, Arrays.asList(InfoCodeEnum.L_U_LOAD_RATE.getCode()));
        Map<String, InfoCodeValueVO> infoCodeValueMap = infoCodeValues.stream().collect(Collectors.toMap(InfoCodeValueVO::getObjectId, v -> v, (v1, v2) -> v1));
        /*
        infoCodeValues.stream().forEach(v -> {
            DtpRwdEquipmentDTO dtpRwdEquipmentDTO = dtpRwdEquipmentMap.get(v.getObjectId());
            PowerDistributionCardDataVO powerDistributionCardDataVO = new PowerDistributionCardDataVO();
            powerDistributionCardDataVO.setObjectId(v.getObjectId());
            powerDistributionCardDataVO.setLULoadRate(v.getDataValue().toString());
            powerDistributionCardDataVO.setLocalName(dtpRwdEquipmentDTO.getLocalName());
            resultVo.getChildCardList().add(powerDistributionCardDataVO);
        });*/
        dtpRwdEquipmentDTOS.stream().forEach(dtpRwdEquipmentDTO -> {
            String id = dtpRwdEquipmentDTO.getId();
            PowerDistributionCardDataVO powerDistributionCardDataVO = new PowerDistributionCardDataVO();
            powerDistributionCardDataVO.setObjectId(id);
            if(infoCodeValueMap.get(id)!=null) {
                powerDistributionCardDataVO.setLULoadRate(infoCodeValueMap.get(id).getDataValue().toString());
            }
            powerDistributionCardDataVO.setLocalName(dtpRwdEquipmentDTO.getLocalName());
            resultVo.getChildCardList().add(powerDistributionCardDataVO);
        });


        //构建大卡片信息的标签
        DutyHomePageGroupDTO dutyHomePageGroup = new DutyHomePageGroupDTO();
        CardTagVO tag = buildCardTagVO(dutyHomePageGroup, ids);
        resultVo.setCardTag(tag);
        return resultVo;
    }
}
