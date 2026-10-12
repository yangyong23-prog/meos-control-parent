package com.persagy.apccontrol.basic.model.vo.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 安防-Dashboard建筑VO
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
@Schema(title = "安防-Dashboard建筑VO")
public class SecurityDashboardBuildingVO {

    @Schema(description = "建筑ID")
    private String id;

    @Schema(description = "建筑名称")
    private String localName;

    @Schema(description = "楼层列表")
    private List<SecurityDashboardFloorVO> floors;

}
