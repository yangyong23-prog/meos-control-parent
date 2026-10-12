package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 集团DTO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@Schema(title = "集团DTO")
public class GroupDTO {

    @Schema(description = "集团ID")
    private String groupId;

    @Schema(description = "集团编码")
    private String groupCode;

    @Schema(description = "集团名称")
    private String groupName;

}
