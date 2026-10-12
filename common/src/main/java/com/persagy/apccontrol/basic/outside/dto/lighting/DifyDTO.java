package com.persagy.apccontrol.basic.outside.dto.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *  dify报告数据
 */
@Getter
@Setter
@Builder
@ToString(callSuper = true)
public class DifyDTO {

    @Schema(description = "报告数据")
    private String reportcsv;



}
