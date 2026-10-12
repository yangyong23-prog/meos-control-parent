package com.persagy.apccontrol.basic.kafka.listener;

import com.persagy.apccontrol.basic.model.base.CommonConst;
import org.springframework.stereotype.Component;

/**
 * kafka多实例下动态groupId
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/3/4 20:20
 * @since 7.250202.0
 */
@Component("groupIdGeneratorSaas")
public class GroupIdGenerator {

    public static String generateId() {
        return String.format("%s-group-%tQ",
                CommonConst.APPLICATION_NAME,
                System.currentTimeMillis());
    }
}
