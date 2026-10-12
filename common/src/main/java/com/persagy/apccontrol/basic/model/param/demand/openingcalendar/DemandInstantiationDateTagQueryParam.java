package com.persagy.apccontrol.basic.model.param.demand.openingcalendar;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.validate.utility.validate.range.DataRangeValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 需求管理-营业日历实例化日期标签查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理-营业日历实例化日期标签查询参数")
public class DemandInstantiationDateTagQueryParam implements BaseCheckParamProvider {

    @Schema(description = "开始日期")
    @NotNull(message = "{key_6dc6cc33082c41d189980dd415b06083}")
    private LocalDate beginDate;

    @Schema(description = "结束日期")
    @NotNull(message = "{key_fe10d89294244a90a5a3d488db40d202}")
    private LocalDate endDate;

    @Schema(description = "需求模版类型, 根据需求模版过滤时不能为空")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "是否根据需求模版过滤")
    private boolean filterByDemandTemplate = false;

    @Override
    public void checkAndSet() {
        DataRangeValidator.date().allowFutureTime().validate(beginDate, endDate);
        if (filterByDemandTemplate && Objects.isNull(templateType)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_67d92d2b08624257a2b1303cf198a074"));
        }
    }
}
