package com.persagy.apccontrol.basic.model.dto.demand.airconditioning;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ValueSelectConfigLogDto {

    @Schema(name = "项目id")
    private String projectId;

    @Schema(name = "配置时间")
    private LocalDateTime dateTime;

    @Schema(name = "价值选择类型")
    private BasicsConfigTypeEnum type;

    @Schema(name = "修改前的价值选择类型")
    private BasicsConfigTypeEnum beforeType;

    @Schema(name = "修改前操作时间")
    private LocalDateTime beforeDateTime;

    @Schema(name = "配置详情")
    private List<ValueSelectConfigDto> details;

    @Schema(name = "操作人")
    private String creatUser;
}
