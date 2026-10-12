package com.persagy.apccontrol.basic.model.param.demand.template;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.validate.utility.validate.range.DataRangeValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 需求管理-需求日历模版日期配置查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理-需求日历模版日期配置查询参数")
public class DemandTemplateDateSettingQueryParam implements BaseCheckParamProvider {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "开始日期, 闭区间")
    @NotNull(message = "{key_6dc6cc33082c41d189980dd415b06083}")
    private LocalDate beginDate;

    @Schema(description = "结束日期, 开区间")
    @NotNull(message = "{key_fe10d89294244a90a5a3d488db40d202}")
    private LocalDate endDate;

    @Override
    public void checkAndSet() {
        DataRangeValidator.date().allowFutureTime().validate(beginDate, endDate);
    }
}
