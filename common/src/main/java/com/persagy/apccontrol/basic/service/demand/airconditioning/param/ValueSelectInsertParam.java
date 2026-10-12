package com.persagy.apccontrol.basic.service.demand.airconditioning.param;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ValueSelectInsertParam {

    @Schema(description = "模版ID")
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    @Schema(name = "价值类型")
    @NotNull(message = "{key_960ade936c2a498982a41df9cd891e43}")
    private BasicsConfigTypeEnum type;

    @Schema(name = "配置详情")
    @NotNull(message = "{key_960ade936c2a498982a41df9cd891e43}")
    private List<ValueSelectConfigDto> details;

}
