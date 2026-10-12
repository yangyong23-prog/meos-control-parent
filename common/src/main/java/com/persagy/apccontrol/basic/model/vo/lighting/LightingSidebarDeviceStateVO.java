package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 当前设备状态响应
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 14:46
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "当前设备状态响应")
public class LightingSidebarDeviceStateVO {

    /**
     * 照明
     */
    private LightingSidebarCircuitVO lightingSidebarCircuitVo;

    /**
     * 控制
     */
    private LightingSidebarControlVO control;

}
