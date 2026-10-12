package com.persagy.apccontrol.basic.model.param.watersupply;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Set;

/**
 * 生活给水-模型图查询参数
 *
 * @author JiYinchuan
 * @since 7.251202.0
 */
@Data
public class WaterSupplyModelViewQueryParam {

    /**
     * 系统ID
     */
    @NotBlank(message = "SystemId must not be blank")
    private String systemId;

    /**
     * 空间ID
     */
    @NotBlank(message = "SpaceId must not be blank")
    private String spaceId;

    /**
     * 关系图中的设备ID
     */
    private Set<String> equipmentIdsInGraph;

    /**
     * 查询字段
     */
    private String[] withColumns;

}
