package com.persagy.apccontrol.basic.model.entity.infocodeconfig;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import lombok.Data;

/**
 * 描述
 *
 * @author yuecaipu
 * @date 2024/8/31 14:06
 * @since v5.240402.00
 */
@Data
public class IotMonitorConfig {
    private RunningMonitorTypeEnum runningMonitorType;
    private String classCode;
    private String infoCode;

}
