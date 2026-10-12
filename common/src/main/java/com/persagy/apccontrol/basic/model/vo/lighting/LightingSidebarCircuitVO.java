package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 照明数据统计结果
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 14:49
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "照明数据统计结果")
public class LightingSidebarCircuitVO {

    /**
     * 自动数量
     */
    private LightingSidebarStatisticalJumpVo auto;

    /**
     * 手动数量
     */
    private LightingSidebarStatisticalJumpVo hand;

    /**
     * 控反不一致
     */
    private LightingSidebarStatisticalJumpVo inconformity;

    /**
     * 离线
     */
    private LightingSidebarStatisticalJumpVo offline;

}
