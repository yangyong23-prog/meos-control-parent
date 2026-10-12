package com.persagy.apccontrol.basic.model.vo.demand;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 需求配置预计算结果VO.
 *
 * @param <T> 配置类型
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@NoArgsConstructor
@Schema(description = "需求配置预计算结果VO")
public class DemandConfigResultVO<T extends BaseDemandConfig> {

    /**
     * 实例ID.
     */
    @Schema(description = "实例ID")
    private @NonNull String instanceId;

    /**
     * 是否被屏蔽.
     */
    @Schema(description = "是否被屏蔽")
    private boolean shielded;

    /**
     * 实例标签.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "实例标签；仅在请求查询实例标签时返回")
    private @Nullable DemandInstanceTagVO instanceTag;

    /**
     * 最终配置.
     */
    @Schema(description = "最终配置")
    private @Nullable DemandConfigValueObject<T> finalConfig;
}
