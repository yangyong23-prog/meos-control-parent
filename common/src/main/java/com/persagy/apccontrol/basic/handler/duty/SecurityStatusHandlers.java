package com.persagy.apccontrol.basic.handler.duty;

import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.SecurityEquipmentStatusEnum;
import com.persagy.apccontrol.basic.model.vo.duty.SecurityChildCardDataVO;

import java.util.Set;

/**
 * 对象大类状态处理器实现
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/4/24 19:20
 * @since 7.250402.0
 */
public enum SecurityStatusHandlers implements SecurityStatusHandler {
    ONLINE_HANDLER {
        @Override
        public void handle(SecurityChildCardDataVO vo, long count, Set<String> rules) {
            if (rules.contains(InfoCodeEnum.ONLINE_STATUS.getCode())) {
                vo.setOnlineStatusTotal((int) count);
            }
        }

        @Override
        public SecurityEquipmentStatusEnum getStatusType() {
            return SecurityEquipmentStatusEnum.ONLINE;
        }
    },
    OPEN_HANDLER {
        @Override
        public void handle(SecurityChildCardDataVO vo, long count, Set<String> rules) {
            if (rules.contains(InfoCodeEnum.RUN_STATUS.getCode())) {
                vo.setRunStatusTotal((int) count);
            }
        }

        @Override
        public SecurityEquipmentStatusEnum getStatusType() {
            return SecurityEquipmentStatusEnum.OPEN;
        }
    },
    AMAZING_HANDLER {
        @Override
        public void handle(SecurityChildCardDataVO vo, long count, Set<String> rules) {
            if (rules.contains(InfoCodeEnum.ARMING_STATUS.getCode())
                    || rules.contains(InfoCodeEnum.DEPLOY_WITHDRAWALARM_STATUS.getCode())) {
                vo.setArmingStatusTotal((int) count);
            }
        }

        @Override
        public SecurityEquipmentStatusEnum getStatusType() {
            return SecurityEquipmentStatusEnum.AMAZING;
        }
    };
}
