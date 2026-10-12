package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 统计结果带跳转
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 14:51
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "统计结果带跳转")
public class LightingSidebarStatisticalJumpVo {

    /**
     * 编码
     */
    private String code;

    /**
     * 数量
     */
    private long number;

}
