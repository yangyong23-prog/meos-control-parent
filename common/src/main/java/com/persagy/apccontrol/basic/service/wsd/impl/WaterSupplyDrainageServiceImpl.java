package com.persagy.apccontrol.basic.service.wsd.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.MapUtils;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.wsd.QueryFloorSpaceEquipmentParam;
import com.persagy.apccontrol.basic.model.vo.sewage.SewageEquipVO;
import com.persagy.apccontrol.basic.model.vo.wsd.SpaceEquipmentVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.wsd.WaterSupplyDrainageService;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 给排水设备相关服务
 */
@Service
@Slf4j
public class WaterSupplyDrainageServiceImpl implements WaterSupplyDrainageService {

    @Resource
    private DtpRwdClientService dtpRwdClientService;

    @Override
    public List<SpaceEquipmentVO> querySpaceEquipmentList(QueryFloorSpaceEquipmentParam param) {
        RunningMonitorStrategy runningMonitorStrategy = RunningMonitorTypeEnum.SEWAGE.getRunningMonitorStrategy();
        DemandInstanceValueObject instance = new DemandInstanceValueObject(param.getInstanceType().toDemandTemplateTypeEnum(), param.getInstanceId());

        List<SpaceEquipmentVO> r = new ArrayList<>();

        //查询空间下的设备
        List<SewageEquipVO> dtpRwdEquipmentVOS = (List<SewageEquipVO>) runningMonitorStrategy.listEquipments(RunningMonitorTypeEnum.SEWAGE, instance);

        if (CollectionUtils.isEmpty(dtpRwdEquipmentVOS)) {
            return Collections.emptyList();
        }

        //按照空间 ID 进行分组
        Map<String, List<SewageEquipVO>> equipMap = dtpRwdEquipmentVOS.stream()
                .filter(e -> e.getSpaceId() != null).collect(Collectors.groupingBy(SewageEquipVO::getSpaceId));

        if (MapUtils.isEmpty(equipMap)) {
            return r;
        }

        //通过空间 id 查询空间信息
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setId(equipMap.keySet());
        List<DtpRwdSpaceDTO> spaceList = dtpRwdClientService.listSpace(queryParam);
        if (CollectionUtils.isEmpty(spaceList)) {
            return r;
        }
        Map<String, DtpRwdSpaceDTO> spaceMap = spaceList.stream().collect(Collectors.toMap(DtpRwdSpaceDTO::getId, Function.identity()));

        // 填充空间的设备信息
        List<SpaceEquipmentVO> result = equipMap.entrySet().stream()
                .map(entry -> {
                    DtpRwdSpaceDTO space = spaceMap.get(entry.getKey());
                    if (space == null) {
                        return null;
                    }
                    SpaceEquipmentVO dto = new SpaceEquipmentVO();
                    BeanUtils.copyProperties(space, dto);
                    dto.setEquipmentList(entry.getValue());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        r.addAll(result);
        return r;
    }
}
