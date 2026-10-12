package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 需求管理-建筑楼层空间树
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
@Schema(title = "需求管理空间功能类型树查询配置参数")
public class DemandTreeBuildingFloorSpaceQueryParam {

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "是否过滤屏蔽空间", defaultValue = "true")
    private boolean filterInShieldedSpace = true;

    @Schema(description = "是否过滤黑名单空间", defaultValue = "true")
    private boolean filterInBlacklistedSpace = true;

    @Schema(description = "是否查询楼层", defaultValue = "true")
    private boolean queryFloor = true;

    @Schema(description = "是否查询空间", defaultValue = "true")
    private boolean querySpace = true;

}
