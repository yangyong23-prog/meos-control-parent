package com.persagy.apccontrol.basic.model.param.demand.template;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 需求管理-需求日历模版批量查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-需求日历模版批量查询参数")
public class DemandTemplateBatchQueryParam {

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "模版标签, 为空时查询所有")
    private String templateTag;

    @Schema(description = "模版ID, 为空时查询所有")
    private Long templateId;

    @Schema(description = "是否启用, 为空时查询所有")
    private Boolean enabled;

    @Schema(description = "过滤sceneId, 为空时不过滤")
    @Nullable
    private List<Long> excludeSceneIds;

}
