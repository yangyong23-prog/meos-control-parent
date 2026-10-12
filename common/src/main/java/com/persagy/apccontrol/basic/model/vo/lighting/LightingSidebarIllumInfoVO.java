package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 侧边栏照度监测信息
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/28 21:23
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "传感器信息")
public class LightingSidebarIllumInfoVO {

    /**
     * 平均照度监测值
     */
    private String outdoorIlluminance;
    /**
     * 传感器信息
     */
    private List<LightingSidebarSensorInfoVO> lightingSidebarSensorInfoVO;
    /**
     * 单位
     */
    private String unit;

}
