package com.persagy.apccontrol.basic.service.duty.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.persagy.apccontrol.basic.model.vo.duty.ParkingCardDataVO;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ParkingHomePageCardDataHandler extends AbstractHomePageCardDataHandler<CardDateVO<ParkingCardDataVO> > {

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer iotDataCacheService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingHomePageCardDataHandler.class);
    private static final String LOG_TAG = "运行值班首页-停车场";

    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.PARKING;
    }

    @Override
    public CardDateVO<ParkingCardDataVO> execute(HomePageParam param) {
        CardDateVO<ParkingCardDataVO> resultVo = new CardDateVO<>();
        resultVo.setChildCardList(new ArrayList<>());
        ParkingCardDataVO parkingCardDataVO = new ParkingCardDataVO();
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                //.addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST)
                .setClassCode(Arrays.asList(DtpRwdClassCodeEnum.CFPKPS.getValue()))
                .setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id));
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = dtpRwdClientService.listEquipment(queryParam);

        if (CollectionUtil.isEmpty(dtpRwdEquipmentDTOS)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未获取到停车位设备");
            return resultVo;
        }

        Set<String> ids = dtpRwdEquipmentDTOS.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        parkingCardDataVO.setEquipmentTotal(ids.size());
        List<InfoCodeValueVO> infoCodeValues = iotDataCacheService.listInfoCodeValue(ContextUtil.getProjectId(), null, ids, Arrays.asList(InfoCodeEnum.PARKING_STATUS.getCode()));
        parkingCardDataVO.setFreeTotal((int) infoCodeValues.stream().filter(v -> ObjectUtil.isNotNull(v.getInfoCodeValue()) && "0".equals(v.getInfoCodeValue().toString())).count());
        resultVo.setChildCardList(Arrays.asList(parkingCardDataVO));

        //构建大卡片信息的标签
        DutyHomePageGroupDTO dutyHomePageGroup = new DutyHomePageGroupDTO();
        CardTagVO tag = buildCardTagVO(dutyHomePageGroup, ids);
        resultVo.setCardTag(tag);
        return resultVo;
    }
}
