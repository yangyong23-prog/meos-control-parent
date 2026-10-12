package com.persagy.apccontrol.basic.model.param.bizcheck;

import com.persagy.apccontrol.basic.model.dto.StartEndDayDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 *
 * @author wxf
 * @date 2026-02-26
 */
@Data
public class BizCheckQueryParam {
    /**
     * 子业务编码
     */
    @Schema(description = "子业务编码")
    @NotNull(message = "子业务编码不能为空")
    private RunningMonitorTypeEnum bizCode;
}