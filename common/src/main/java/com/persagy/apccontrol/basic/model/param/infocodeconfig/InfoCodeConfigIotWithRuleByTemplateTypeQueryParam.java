package com.persagy.apccontrol.basic.model.param.infocodeconfig;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 信息点-对象信息点规则
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeConfigIotWithRuleByTemplateTypeQueryParam implements BaseCheckParamProvider {

    @Schema(description = "模版类型ID")
    @NotNull(message = "{key_02761da21e69441c86b58c7195dc5b83}")
    private Long templateTypeId;

    @Schema(description = "分组编码")
    @NotEmpty(message = "{key_9e2dcb4d4f3b4023ad2b608d7fa62336}")
    private Set<String> groupCodes;

    @Schema(description = "信息点类型, 仅支持IOT_RUNNING和IOT_SETTING")
    @NotEmpty(message = "{key_19bd49dca3e14bacb02f5937e83445f2}")
    private Set<InfoCodeTypeEnum> infoCodeTypes;

    @Schema(description = "是否过滤管控对象", defaultValue = "false")
    private Boolean filterControlObject;

    @Override
    public void checkAndSet() {
        for (InfoCodeTypeEnum infoCodeType : infoCodeTypes) {
            switch (infoCodeType) {
                case IOT_RUNNING:
                case IOT_SETTING:
                    break;
                default:
                    throw new ValidationException(LocaleMessageUtil.getMessage("key_d4460277d8a14442834da75a9380857e"));
            }
        }
    }
}
