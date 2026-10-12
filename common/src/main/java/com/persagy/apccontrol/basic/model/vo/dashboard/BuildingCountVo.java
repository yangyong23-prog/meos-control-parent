package com.persagy.apccontrol.basic.model.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : wangchenghong
 * @date : 2024/12/3 16:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Dashboard照明-建筑统计")
public class BuildingCountVo {

    @Schema(description = "建筑ID")
    private String id;

    @Schema(description = "建筑名称")
    private String localName;

    @Schema(description = "楼层列表")
    private List<FloorCountVo> floors;

}
