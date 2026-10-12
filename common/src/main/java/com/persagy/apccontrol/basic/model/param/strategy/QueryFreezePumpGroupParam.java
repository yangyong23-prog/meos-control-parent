package com.persagy.apccontrol.basic.model.param.strategy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author yuyao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "泵组查询查询参数")
public class QueryFreezePumpGroupParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "泵组id")
    @NotBlank(message = "{key_7f591dca7e5d41269c558084ca6abad8}")
    private String freezePumpGroupId;

}
