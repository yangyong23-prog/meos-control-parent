package com.persagy.apccontrol.basic.model.param.demand.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 照明需求管理-模板复制参数
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "照明需求管理-模板复制参数")
public class DemandTemplateCopyParam {

    @Schema(description = "模板来源ID")
    @NotNull(message = "{key_ef344fcf968c433bb010c7af98ea9e17}")
    private Long fromTemplateId;

    @Schema(description = "模板ID")
    @NotNull(message = "{key_a8a0a53f475f4137af42b23076047014}")
    private Long templateId;

}
