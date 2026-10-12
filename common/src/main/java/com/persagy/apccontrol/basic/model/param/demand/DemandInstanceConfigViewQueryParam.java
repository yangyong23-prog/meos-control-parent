package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * 需求管理-实例配置树
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-实例配置树")
public class DemandInstanceConfigViewQueryParam {

    @Schema(description = "需求模版类型")
    @NotNull(message = "{key_67d92d2b08624257a2b1303cf198a074}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "查询日期")
    private LocalDate date;

    @Schema(description = "实例类型 -> 查询字段, 为空时查询默认字段")
    private Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndWithColumns;

}
