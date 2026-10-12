package com.persagy.apccontrol.common.model.param.ai;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 需求配置查询参数
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Data
public class AiDemandConfigQueryParam {

    @Schema(description = "需求模板类型")
    @NotNull(message = "{key_7ffceae9d7494b699f3b58840b34fcf8}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "计算时间")
    @NotNull(message = "{key_ac4397df68e64166a67f7961f82e8b0b}")
    private LocalDateTime computeTime;

    @Schema(description = "空间ID")
    private Set<String> spaceIds;

    @Schema(description = "查询数量, 为空时查询所有")
    private Integer querySpaceCount;

}
