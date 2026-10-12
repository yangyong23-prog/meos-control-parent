package com.persagy.apccontrol.basic.service.demand.airconditioning.param;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
public class ValueResultQueryParam {

    @Schema(name = "项目id")
    @NotBlank(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @Schema(name = "空间ID集合")
    private Set<String> spaceId;

    @Schema(name = "日期")
    private String workDate;

    @Schema(name = "价值计算类型")
    private BasicsConfigTypeEnum calculateType;

}
