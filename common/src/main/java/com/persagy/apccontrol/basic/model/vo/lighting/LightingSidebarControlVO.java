package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 控制数据统计结果
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 14:50
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "控制数据统计结果")
public class LightingSidebarControlVO {

    /**
     * 离线
     */
    private LightingSidebarStatisticalJumpVo offline;

}
