package com.persagy.apccontrol.common.model.param.desktop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Set;

/**
 * 桌面端-设备详情查询参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S2-5.18.0
 */
@Data
@Schema(title = "桌面端-设备详情查询参数")
public class DesktopEquipmentDetailsQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "设备ID")
    private Set<String> equipmentIds;

}
