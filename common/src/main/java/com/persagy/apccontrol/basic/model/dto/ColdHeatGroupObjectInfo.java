package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

/**
 * 空气源热泵控制组和空气源热泵
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/3/4 17:54
 */
@Data
public class ColdHeatGroupObjectInfo {

    @Schema(description = "是否存在泵组规则")
    public boolean groupRule;

    @Schema(description = "泵组下的热泵是否全部存在规则")
    public boolean allEquipRule;

    @Schema(description = "泵组对象ID")
    public String groupObjectId;

    @Schema(description = "所有的热泵对象ID")
    public Set<String> equipObjectIds;

    public ColdHeatGroupObjectInfo(boolean groupRule, boolean allEquipRule, String groupObjectId, Set<String> equipObjectIds) {
        this.groupRule = groupRule;
        this.allEquipRule = allEquipRule;
        this.groupObjectId = groupObjectId;
        this.equipObjectIds = equipObjectIds;
    }

}
