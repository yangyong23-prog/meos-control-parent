package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 传感器信息
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/28 21:24
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "传感器信息")
public class LightingSidebarSensorInfoVO {

    /**
     * 传感器照度监测值
     */
    private String illum;
    /**
     * 传感器id
     */
    private String instanceId;
    /**
     * 传感器classCode
     */
    private String classCode;

    /**
     * 传感器照度监测infoCode
     */
    private String infoCode;

    /**
     * 传感器照度监测值
     */
    private String dataValue;

    /**
     * 传感器名称
     */
    private String instanceName;

}
