package com.persagy.apccontrol.basic.model.enumtype.cacherwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * 白名单类型枚举
 *
 * @author wxf
 * @since 7.260302.0
 */
@Getter
@AllArgsConstructor
@Schema(description = "白名单类型")
public enum WhitelistTypeEnum {
    
    /**
     * 全量可管控空间
     */
    @Schema(description = "全量可管控空间")
    FULL("FULL", "全量可管控空间", "FULL"),
    
    /**
     * 用户配置白名单
     */
    @Schema(description = "用户配置白名单")
    CUSTOM("CUSTOM", "用户配置白名单", "WHITE_LIST");
    
    private final String value;
    private final String description;

    /**
     * 对应的空间范围壳配置值。
     *
     * @since v7.260701.0
     */
    private final @NonNull String shellConfigValue;
}
