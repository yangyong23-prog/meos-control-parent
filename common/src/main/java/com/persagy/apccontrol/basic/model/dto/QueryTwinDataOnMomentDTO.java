package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 反馈孪生DTO
 *
 * @author yx
 * @date 2023/3/28
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "对象信息点参数")
public class QueryTwinDataOnMomentDTO {

    @Schema(description = "项目id")
    @NotBlank(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @NotBlank(message = "{key_084485a601e5437688eeacf92ba08c45}")
    @Schema(description = "对象实例id")
    private String objectId;

    @NotNull(message = "{key_0ad0f2789ec14197b646fbf159c47354}")
    @Schema(description = "信息点编码列表")
    private Set<String> infoCodes;

}
