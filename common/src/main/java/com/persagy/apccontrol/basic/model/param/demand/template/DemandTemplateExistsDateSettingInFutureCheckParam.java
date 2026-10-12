package com.persagy.apccontrol.basic.model.param.demand.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 需求管理-需求日历模版检查未来日期是否存在日期配置参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-需求日历模版检查未来日期是否存在日期配置参数")
public class DemandTemplateExistsDateSettingInFutureCheckParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "模版ID")
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    @Schema(description = "是否抛出异常")
    private Boolean isThrows = false;

}
