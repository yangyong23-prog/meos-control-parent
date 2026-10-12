package com.persagy.apccontrol.basic.model.param.external.demand;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 需求配置时序数据
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
public class DemandSpaceSequenceExternalQueryParam {

    @Schema(description = "需求开始日期（包含此日）")
    @NotNull(message = "{key_dd5a361aa8f04edc82c1acd7c7d60b79}")
    private LocalDate startDate;

    @Schema(description = "需求结束日期（包含此日）")
    @NotNull(message = "{key_391aa2b4ddac4c0a948b7c2ed182e7a4}")
    private LocalDate endDate;

    public void checkQueryDate() {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_3dfa70c1f241470d81c829548c2efaac"));
        }
    }

}
