package com.persagy.apccontrol.basic.model.param.demand.airconditioning;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 空调需求-查询空调工况与壳配置综合后的配置
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "空调需求-查询空调工况与壳配置综合后的配置")
public class DemandAirconditioningShellConfigDataQueryParam extends BaseDemandTemplateParam implements BaseCheckParamProvider {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

}
