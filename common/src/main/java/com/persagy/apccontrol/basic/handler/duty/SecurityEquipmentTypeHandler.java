package com.persagy.apccontrol.basic.handler.duty;

import com.persagy.apccontrol.basic.model.enumtype.security.SecurityEquipmentTypeEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 安防设备状态处理枚举
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/4/24 19:22
 * @since 7.250402.0
 */
@Getter
public enum SecurityEquipmentTypeHandler {

    DOOR(SecurityEquipmentTypeEnum.DOOR,
            Arrays.asList(SecurityStatusHandlers.OPEN_HANDLER, SecurityStatusHandlers.ONLINE_HANDLER)),

    BURGLAR_ALARM(SecurityEquipmentTypeEnum.BURGLAR_ALARM_DEVICE,
            Arrays.asList(SecurityStatusHandlers.AMAZING_HANDLER, SecurityStatusHandlers.ONLINE_HANDLER)),

    CAMERA(SecurityEquipmentTypeEnum.CAMERA,
            Collections.singletonList(SecurityStatusHandlers.ONLINE_HANDLER));

    private final SecurityEquipmentTypeEnum equipmentType;
    private final List<SecurityStatusHandler> handlers;

    SecurityEquipmentTypeHandler(SecurityEquipmentTypeEnum equipmentType, List<SecurityStatusHandler> handlers) {
        this.equipmentType = equipmentType;
        this.handlers = handlers;
    }

    public static SecurityEquipmentTypeHandler from(SecurityEquipmentTypeEnum type) {
        return Arrays.stream(values())
                .filter(h -> h.equipmentType == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("key_9febfb2870e84603a2ffb360d2596654" + type));
    }

}
