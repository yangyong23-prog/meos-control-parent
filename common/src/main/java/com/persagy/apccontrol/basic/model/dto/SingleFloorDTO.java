package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * 根据对象ID查询
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/23 16:39
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "楼层信息")
public class SingleFloorDTO {

    @NotBlank(message = "{key_61bdc1ef7fa249738155e48dcb89f5c8}")
    @Schema(description = "项目编码")
    private String projectId;

    @NotBlank(message = "{key_80aee18fa59747098fc1ab5494f7eac3}")
    @Schema(description = "楼层ID")
    private String floorId;

    @Schema(description = "是否包含设备所在楼层")
    private Boolean includeInSpace;

}
