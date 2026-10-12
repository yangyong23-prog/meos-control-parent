package com.persagy.apccontrol.basic.model.param.demand.lighting;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * 需求管理-对象树查询参数
 *
 * @author yuyao
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-对象树查询参数")
public class DemandTreeQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "需求需求类型")
    @NotNull(message = "{key_ce03747a7f4340ed8c4f31d95710bb71}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "场景时段ID")
    private Long sceneId;


    public void check() {
        //场景id存在情况下 必须传递模版id
        if (sceneId == null) {
            ParameterCheck.isTrue(templateId == null, LocaleMessageUtil.getMessage("key_5fb6ca923c95448f96676062bf658c8c"));
        }
    }

}
