package com.persagy.apccontrol.basic.outside.feign.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author shaohongbo
 * @date 2023年03月29日 20:04
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@ToString(callSuper = true)
@Builder
public class DtpTwinManualSend {

    @Schema(description = "设备id")
    private String objectId;

    @Schema(description = "信息点")
    private String infoCode;

    @Schema(description = "信息点设定值")
    private String value;



}

