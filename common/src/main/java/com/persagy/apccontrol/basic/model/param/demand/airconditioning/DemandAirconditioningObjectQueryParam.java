package com.persagy.apccontrol.basic.model.param.demand.airconditioning;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Set;

/**
 * 空调需求-对象查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "空调需求-对象查询参数")
public class DemandAirconditioningObjectQueryParam
        extends BaseDemandTemplateParam implements BaseCheckParamProvider {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "系统ID, 为空时查询所有")
    private Set<String> systemIds;

    @Schema(description = "实例类型 -> 实例ID, 为空时查询所有")
    private Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndIds;

}
