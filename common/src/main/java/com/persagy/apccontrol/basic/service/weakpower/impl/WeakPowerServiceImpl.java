package com.persagy.apccontrol.basic.service.weakpower.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.weakpower.RunningMonitorWeakPowerEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.weakpower.WeakPowerTreeEquipmentsQueryParam;
import com.persagy.apccontrol.basic.model.vo.weakpower.RunningMonitorWeakPowerEquipmentDTO;
import com.persagy.apccontrol.basic.model.vo.weakpower.WeakPowerSystemVO;
import com.persagy.apccontrol.basic.model.vo.weakpower.WeakPowerTreeEquipmentsVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.weakpower.WeakPowerService;
import com.persagy.apccontrol.basic.structure.weakpower.converter.WeakPowerConverter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorWeakPowerStrategy.RUNNING_MONITOR_SECOND_TYPE_AND_SYSTEM_CLASS_CODE;

/**
 * 弱电服务实现
 *
 * @author JiYinchuan
 * @since 7.250301.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WeakPowerServiceImpl implements WeakPowerService {

    private final ObjectMapper objectMapper;
    private final WeakPowerConverter weakPowerConverter;
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public WeakPowerTreeEquipmentsVO treeEquipments(WeakPowerTreeEquipmentsQueryParam param) {
        WeakPowerTreeEquipmentsVO vo = new WeakPowerTreeEquipmentsVO();
        RunningMonitorWeakPowerEquipmentQueryParam queryParam = getRunningMonitorWeakPowerEquipmentQueryParam(param);
        //先查询出系统 作为主节点
        DtpRwdObjectQueryParam objectQueryParam = new DtpRwdObjectQueryParam();
        objectQueryParam.setId(param.getSystemId());
        List<DtpRwdEquipmentDTO> equipments = queryParam.queryObject(objectQueryParam);
        if (equipments.isEmpty()) {
            return vo;
        }

        RunningMonitorWeakPowerEquipmentDTO result = weakPowerConverter.toWeakPowerEquipmentDto(equipments.get(0));
        List<? extends DtpRwdEquipmentDTO> dtoList = RunningMonitorTypeEnum.WEAK_POWER.getRunningMonitorStrategy().listEquipments(queryParam);

        if (CollectionUtils.isNotEmpty(dtoList)) {
            result.setChildren((List<RunningMonitorWeakPowerEquipmentDTO>) dtoList);
        }
        return objectMapper.convertValue(result, WeakPowerTreeEquipmentsVO.class);
    }


    /**
     * 获取查询参数
     */
    private RunningMonitorWeakPowerEquipmentQueryParam getRunningMonitorWeakPowerEquipmentQueryParam(WeakPowerTreeEquipmentsQueryParam param) {
        RunningMonitorWeakPowerEquipmentQueryParam queryParam = new RunningMonitorWeakPowerEquipmentQueryParam();
        queryParam.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
        queryParam.setSystemIds(Collections.singleton(param.getSystemId()));
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode,
                                 DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.objType,
                                 DtpRwdEquipmentDTO.Fields.floorId);
        return queryParam;
    }
    
    @Override
    public List<WeakPowerSystemVO> listSystems() {
        List<WeakPowerSystemVO> resultSort = Lists.newArrayList();
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam()
                .setClassCodeEnum(RUNNING_MONITOR_SECOND_TYPE_AND_SYSTEM_CLASS_CODE.values())
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode, DtpRwdObjectDTO.Fields.localName);
//                .filterMonitoringSurveillanceStatus();
        List<DtpRwdObjectDTO> dtpRwdObjects = dtpRwdClientService.listObject(param);
        List<WeakPowerSystemVO> result = Lists.newArrayList();
        for (DtpRwdObjectDTO dtpRwdObject : dtpRwdObjects) {
            result.add(weakPowerConverter.toWeakPowerSystem(dtpRwdObject));
        }
        //根据特定顺序排序,获取weba
        resultSort.addAll(filterWeakPowerSystemVO(result, DtpRwdClassCodeEnum.WEBA));
        //获取weem
        resultSort.addAll(filterWeakPowerSystemVO(result, DtpRwdClassCodeEnum.WEEM));
        //获取weie
        resultSort.addAll(filterWeakPowerSystemVO(result, DtpRwdClassCodeEnum.WEIE));
        //获取welm
        resultSort.addAll(filterWeakPowerSystemVO(result, DtpRwdClassCodeEnum.WELM));
        return resultSort;
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2025/12/25 16:18
    　　 * @description: 过滤指定的对象类实例数据
    */
    public List<WeakPowerSystemVO> filterWeakPowerSystemVO(List<WeakPowerSystemVO>  result,DtpRwdClassCodeEnum dtpRwdClassCodeEnum){
        List<WeakPowerSystemVO> weakPowerSystemVOS = result.stream().filter(vo -> dtpRwdClassCodeEnum.getValue().equals(vo.getClassCode())).collect(Collectors.toList());
        //按系统实例的名称进行升序排列
        // 获取中文拼音排序器
        Collator collator = Collator.getInstance(Locale.CHINA);   // 或 Locale.SIMPLIFIED_CHINESE
        collator.setStrength(Collator.PRIMARY);                   // 忽略大小写/全半角差异
        weakPowerSystemVOS.sort(Comparator.comparing(WeakPowerSystemVO::getSystemLocalName,collator));
        return weakPowerSystemVOS;
    }


}
