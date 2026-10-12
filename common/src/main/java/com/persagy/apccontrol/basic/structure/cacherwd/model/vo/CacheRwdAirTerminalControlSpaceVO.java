package com.persagy.apccontrol.basic.structure.cacherwd.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 末端全量管控空间。
 *
 * <p>该模型只包含白名单配置选择空间时需要的基础定位字段，不返回对象扩展字段或模型视图字段。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@Schema(description = "末端全量管控空间")
public class CacheRwdAirTerminalControlSpaceVO {

    /**
     * 空间 ID。
     *
     * @since v7.260701.0
     */
    @Schema(description = "空间 ID")
    private String id;

    /**
     * 空间本地名称。
     *
     * @since v7.260701.0
     */
    @Schema(description = "空间本地名称")
    private String localName;

    /**
     * 所属楼层 ID。
     *
     * @since v7.260701.0
     */
    @Schema(description = "所属楼层 ID")
    private String floorId;

    /**
     * 所属建筑 ID。
     *
     * @since v7.260701.0
     */
    @Schema(description = "所属建筑 ID")
    private String buildingId;
}
