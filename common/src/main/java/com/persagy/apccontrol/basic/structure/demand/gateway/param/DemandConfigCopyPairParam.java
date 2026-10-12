package com.persagy.apccontrol.basic.structure.demand.gateway.param;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.constraints.NotNull;

/**
 * 需求配置赋值参数
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Data
@NoArgsConstructor
public class DemandConfigCopyPairParam {

    /**
     * 模版ID
     */
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private @NonNull Long templateId;

    /**
     * 场景ID
     */
    private @Nullable Long sceneId;

}
