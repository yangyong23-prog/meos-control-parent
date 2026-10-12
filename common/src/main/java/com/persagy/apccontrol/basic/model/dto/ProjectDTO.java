package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;

/**
 * 项目DTO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "项目DTO")
public class ProjectDTO extends GroupDTO {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}", groups = Default.class)
    private String projectId;

    @Schema(description = "项目本地编码")
    private String projectLocalId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目本地名称")
    private String projectLocalName;

    @Schema(description = "功能类型")
    private String functionTypeCode;

    @Schema(description = "功能类型名称")
    private String functionTypeName;

    @Schema(description = "城市编码")
    private String cityCode;

    @Schema(description = "省份")
    private String provinceName;

    @Schema(description = "城市")
    private String cityName;

    @Schema(description = "区域")
    private String partitionName;

}