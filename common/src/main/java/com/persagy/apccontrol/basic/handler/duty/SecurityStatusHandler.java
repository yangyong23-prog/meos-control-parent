package com.persagy.apccontrol.basic.handler.duty;

import com.persagy.apccontrol.basic.model.enumtype.security.SecurityEquipmentStatusEnum;
import com.persagy.apccontrol.basic.model.vo.duty.SecurityChildCardDataVO;

import java.util.Set;

/**
 * 安防首页卡片状态处理器
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/4/24 19:19
 * @since 7.250402.0
 */
public interface SecurityStatusHandler {
    void handle(SecurityChildCardDataVO vo, long count, Set<String> rules);

    SecurityEquipmentStatusEnum getStatusType();
}
