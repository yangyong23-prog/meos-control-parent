package com.persagy.apccontrol.common.sse.strategy.light;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarDeviceStateVO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.common.service.lighting.LightingService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 照明当前设备状态
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240402.00
 */
@Component(value = "LIGHT_EQUIPMENT_STATUS")
public class LightEquipmentStatus extends AbstractSseEventStrategyTemplate {

    @Resource
    LightingService lightingService;
    @Resource
    DemandLightingStrategy demandLightingStrategy;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.LIGHT_EQUIPMENT_STATUS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey+":LIGHT_EQUIPMENT_STATUS";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        List<String> objectIds = listObjectIdsByInstance(param);
        if (CollUtil.isEmpty(objectIds)) {
            return null;
        }
        LightingSidebarDeviceStateVO equipmentStatus = lightingService.countEquipmentStatus(param.getProjectId(), objectIds);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.LIGHT_EQUIPMENT_STATUS)
                .data(equipmentStatus)
                .build();

    }


    /**
     * 查询配置实例关联的回路和控制模块对象ID ，使用本地缓存降低查询频率
     *
     * @param param
     * @return {@link Set <String>}
     * @author yuecaipu
     * @date 2024/8/19 10:49
     * @since v5.240802
     */
    private List<String> listObjectIdsByInstance(SseSubscribeParam param) {

        List<String> objectIdList = new ArrayList<>();
        // 照明回路
        List<DtpRwdCircuitDTO> dtpRwdCircuitDtoList = demandLightingStrategy.listCircuitsByInstance(
                param.getProjectId(), param.getDemandTemplateType(), DemandInstanceTypeEnum.analyze(param.getInstanceType().getValue()), param.getInstanceId());
        if (CollUtil.isEmpty(dtpRwdCircuitDtoList)) {
            return objectIdList;
        }
        objectIdList.addAll(dtpRwdCircuitDtoList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
        // 控制模块
        List<DtpRwdObjectDTO> dtpRwdObjectList = demandLightingStrategy.listControllersByCircuit(
                param.getProjectId(), param.getDemandTemplateType(), objectIdList, Lists.newArrayList());
        if (CollUtil.isNotEmpty(dtpRwdObjectList)) {
            objectIdList.addAll(dtpRwdObjectList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
        }

        return objectIdList;

    }
}
