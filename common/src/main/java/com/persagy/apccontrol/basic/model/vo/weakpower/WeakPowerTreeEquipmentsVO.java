package com.persagy.apccontrol.basic.model.vo.weakpower;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 设备树 VO
 */
@Data
public class WeakPowerTreeEquipmentsVO {

    @Schema(description = "对象 ID")
    private String id;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "类型编码")
    private String classCode;
    
    @Schema(description = "对象类型")
    private String objType;
    
    @Schema(description = "楼层id")
    private String floorId;

    @Schema(description = "子节点")
    private List<WeakPowerTreeEquipmentsVO> children;


}
