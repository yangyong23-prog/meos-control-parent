package com.persagy.apccontrol.basic.model.vo.computeroom;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@Schema(title = "机房空间对象VO")
public class ComputerRoomSecuritySpaceVo {

    @Schema(description = "空间Id")
    private String id;

    @Schema(description = "空间classCode")
    private String classCode;

    @Schema(description = "空间名")
    private String localName;

    @Schema(description = "楼层id")
    private String floorId;

    @Schema(description = "楼层名")
    private String floorName;

    @Schema(description = "建筑id")
    private String buildingId;

    @Schema(description = "建筑名")
    private String buildingName;
}
