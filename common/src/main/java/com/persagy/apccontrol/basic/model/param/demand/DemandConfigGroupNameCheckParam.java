package com.persagy.apccontrol.basic.model.param.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 需求配置分组名称检查参数
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Schema(title = "需求配置分组名称检查参数")
public class DemandConfigGroupNameCheckParam {

    @Schema(description = "模版ID")
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    @Schema(description = "场景ID")
    private Long sceneId;

    @Schema(description = "配置分组名称")
    @NotBlank(message = "{key_f78e0b5120e2484fb3102f24bba5223b}")
    private String configGroupName;

    @Schema(description = "排除配置分组ID")
    private Long excludeConfigGroupId;

    /**
     * 设置配置分组名称
     *
     * @param configGroupName 设置配置分组名称
     * @since 7.250202.0
     */
    @SuppressWarnings("unused")
    public void setConfigGroupName(String configGroupName) {
        this.configGroupName = StringUtils.trimToNull(configGroupName);
    }
}
