package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 当前需求满足情况子集数据
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 14:57
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "当前需求满足情况子集数据")
public class LightingSidebarLightLoopTotalChildrenVO {

    /**
     * 灯具功能类型，为空则为未配置
     */
    private String lampFuncType;

    /**
     * 灯具功能类型名称，为空则为未配置
     */
    private String lampFuncTypeName;

    /**
     * 少开
     */
    private Integer lessOpenNumber;

    /**
     * 多开
     */
    private Integer moreOpenNumber;

    /**
     * 当前开启数量
     */
    private Integer realOnNumber;

    /**
     * 需求开启数量
     */
    private Integer demandOnNumber;

}
