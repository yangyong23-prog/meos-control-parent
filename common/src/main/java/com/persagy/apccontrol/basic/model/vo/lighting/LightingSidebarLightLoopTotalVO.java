package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 当前需求满足情况
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 14:56
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "当前需求满足情况")
public class LightingSidebarLightLoopTotalVO {

    /**
     * 功能类型
     */
    private String funcType;

    /**
     * 功能类型名称
     */
    private String funcTypeName;

    /**
     * 少开
     */
    private Integer lessOpenNumber;

    /**
     * 多开
     */
    private Integer moreOpenNumber;

    /**
     * 需求开启数量
     */
    private Integer demandOnNumber;

    /**
     * 当前开启数量
     */
    private Integer realOnNumber;
    /**
     * 子集数据
     */
    private List<LightingSidebarLightLoopTotalChildrenVO> children;

}
