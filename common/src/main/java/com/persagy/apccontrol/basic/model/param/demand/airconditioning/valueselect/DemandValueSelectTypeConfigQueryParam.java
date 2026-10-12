package com.persagy.apccontrol.basic.model.param.demand.airconditioning.valueselect;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

/**
 * 需求管理-价值选择-类型配置查询参数
 *
 * @author JiYinchuan
 * @since 7.250102.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求管理-价值选择-类型配置查询参数")
public class DemandValueSelectTypeConfigQueryParam
        extends BaseDemandTemplateParam
        implements BaseCheckParamProvider {

    @Schema(description = "价值选择类型")
    @NotNull(message = "{key_bd34e63e4de64cbea23c06e632e1dfe8}")
    private BasicsConfigTypeEnum type;

    @Override
    public void checkAndSet() {
        if (!BasicsConfigTypeEnum.listValueSelectTypes().contains(type)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_335f753837d6499c99f529de2f5b0176"));
        }
    }
}
