package com.persagy.apccontrol.basic.service.ventilation;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.ventilation.VentilationEquipStatisticsEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.vo.kitchen.AlarmControlVo;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.model.vo.kitchen.RunStatusVo;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationDtpRwdEquipmentVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractVentilationIotService implements VentilationIotService {
    
    @Resource
    private DtpRwdClientService dtpRwdClientService;
    @Resource
    private CacheRwdDomainService cacheRwdDomainService;
    @Resource
    private CacheObjectModelStatusService cacheObjectModelStatusService;
    @Resource
    private CacheObjectRepository cacheObjectRepository;
    public Map<VentilationEquipStatisticsEnum, Set<String>> queryEquipGroupId() {
        //数据分组
        Map<VentilationEquipStatisticsEnum, Set<String>> buildingIdAndSpaceIds = new HashMap<>();
        List<? extends DtpRwdEquipmentDTO> sb = listEquipments();
        if (Collections.isEmpty(sb)) {
            return buildingIdAndSpaceIds;
        }

        sb.forEach(equipment -> {
            if (DtpRwdClassCodeEnum.FFSCMF.getValue().equals(equipment.getClassCode())) {
                buildingIdAndSpaceIds.computeIfAbsent(VentilationEquipStatisticsEnum.FFSCMF_SEND_FAN,
                        k -> new HashSet<>()).add(equipment.getId());
            } else if (DtpRwdClassCodeEnum.FFSCAF.getValue().equals(equipment.getClassCode())) {
                buildingIdAndSpaceIds.computeIfAbsent(VentilationEquipStatisticsEnum.FFSCAF_RECEIVE_FAN,
                        k -> new HashSet<>()).add(equipment.getId());
            } else if (DtpRwdClassCodeEnum.ACVTSF.getValue().equals(equipment.getClassCode())) {
                Map<String, Object> ufp = equipment.getUndefinedProperties();
                if ("1".equals(ufp.putIfAbsent(VentilationDtpRwdEquipmentVO.Fields.funcType, ""))) {
                    buildingIdAndSpaceIds.computeIfAbsent(VentilationEquipStatisticsEnum.ACVTSF_SEND_FAN,
                            k -> new HashSet<>()).add(equipment.getId());
                } else if ("2".equals(ufp.putIfAbsent(VentilationDtpRwdEquipmentVO.Fields.funcType, ""))) {
                    buildingIdAndSpaceIds.computeIfAbsent(VentilationEquipStatisticsEnum.ACVTSF_RECEIVE_FAN,
                            k -> new HashSet<>()).add(equipment.getId());
                }
            } else if (DtpRwdClassCodeEnum.CFCSWN.getValue().equals(equipment.getClassCode())) {
                // PRD-290902: 普通窗分组
                buildingIdAndSpaceIds.computeIfAbsent(VentilationEquipStatisticsEnum.VENTILATION_FAN_CFCSWN,
                        k -> new HashSet<>()).add(equipment.getId());
            } else if (DtpRwdClassCodeEnum.CFCSEW.getValue().equals(equipment.getClassCode())) {
                // PRD-290902: 排烟窗分组
                buildingIdAndSpaceIds.computeIfAbsent(VentilationEquipStatisticsEnum.VENTILATION_FAN_CFCSEW,
                        k -> new HashSet<>()).add(equipment.getId());
            }
        });
        return buildingIdAndSpaceIds;
    }


    public List<? extends DtpRwdEquipmentDTO> listEquipments() {
        // READY 后走新缓存读取路径
        if (cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.VENTILATION)) {
            // 送/排风机分组只需要主键、设备类和功能类型，显式字段替代全字段恢复，
            // 避免读回包含模型图字段在内的无关扩展 JSON。
            Set<String> equipmentWithColumns = Sets.newHashSet(
                    DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.classCode,
                    InfoCodeEnum.FUNC_TYPE.value());
            List<DtpRwdEquipmentDTO> equipments = cacheObjectRepository.listByCacheKey(
                    CacheObjectModelKeyConfig.VENTILATION_LIST_EQUIPMENT,
                    DtpRwdEquipmentDTO.class, equipmentWithColumns);
            log.info("从新缓存获取通风设备数据, count={}", equipments.size());
            return equipments;
        }
        log.info("未命中缓存，直接查询获取通风设备数据");
        return getDtpRwdEquipmentDTOS();
    }

    private List<? extends DtpRwdEquipmentDTO> getDtpRwdEquipmentDTOS() {
        //查询空间
        DtpRwdObjectQueryParam queryParam =
                new DtpRwdObjectQueryParam().setWithColumns(Sets.newHashSet(DtpRwdObjectDTO.Fields.id,DtpRwdObjectDTO.Fields.classCode));
        List<DtpRwdSpaceDTO> spaceList = dtpRwdClientService.listSpace(queryParam);
        Set<String> spaceIds = spaceList.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
        RunningMonitorEquipmentQueryParam param = new RunningMonitorEquipmentQueryParam();
        param.setFilterEquipmentServiceSpaceIds(spaceIds);
        return  RunningMonitorTypeEnum.VENTILATION.getRunningMonitorStrategy().listEquipments(param);
    }


    /**
     * 构建空返回对象
     *
     * @param param
     */
    protected Map<String, KitChenRangeHoodEquipStatisticsVo> buildEmptyResult(Map<String, Set<String>> param) {
        Map<String, KitChenRangeHoodEquipStatisticsVo> r = new HashMap<>();
        //构建初始对象
        param.forEach((k, v) -> {
            KitChenRangeHoodEquipStatisticsVo vo = new KitChenRangeHoodEquipStatisticsVo();
            vo.setAlarmControl(AlarmControlVo.builder().build());
            vo.setTotal(param.get(k).size());
            vo.setRunStatus(new RunStatusVo());
            r.put(k, vo);
        });
        return r;
    }
}
