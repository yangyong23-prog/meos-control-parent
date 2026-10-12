package com.persagy.apccontrol.basic.model.param.external.demand;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 需求实例VO
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class DemandSpaceExternalQueryParam extends BaseDemandTemplateParam implements BaseCheckParamProvider {

    @Schema(description = "查询字段, 为空时仅返回ID")
    private String[] withColumns;

}
