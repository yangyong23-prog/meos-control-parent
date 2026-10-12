package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 侧边栏-手自动控制
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
@Schema(title = "侧边栏-手自动控制")
public class LightingSidebarManualAutomaticControlVO {

    @Schema(description = "功能类型")
    private String funcType;

    @Schema(description = "功能类型名称")
    private String funcTypeName;

    @Schema(description = "灯具功能类型，为空则为未配置")
    private String lampFuncType;

    @Schema(description = "灯具功能类型名称，为空则为未配置")
    private String lampFuncTypeName;

    @Schema(description = "子集数据")
    private List<LightingSidebarManualAutomaticControlVO> children;

    @Schema(description = "自动数量")
    private LightingManualAutoVo auto;

    @Schema(description = "手动数量")
    private LightingManualAutoVo manualOperation;

}

