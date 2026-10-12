package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "查询风机")
public class QueryFanDTO extends SystemDTO {

    @NotBlank(message = "{key_75d9567b5d34499da7b8000512b6668b}")
    @Schema(description = "冷却塔id")
    private String coolingTowerId;

}
