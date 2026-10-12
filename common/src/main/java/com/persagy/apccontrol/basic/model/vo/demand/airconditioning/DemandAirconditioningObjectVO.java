package com.persagy.apccontrol.basic.model.vo.demand.airconditioning;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 需求管理-管控对象VO
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@Schema(description = "需求管理-管控对象VO")
public class DemandAirconditioningObjectVO {

    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "对象父类ID")
    private String parentId;

    @Schema(description = "对象名称")
    private String name;

    @Schema(description = "对象本地编码")
    private String localId;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "是否自定义配置")
    private Boolean isCustomizedSetting;

    @Schema(description = "是否为自定义管控空间")
    private Boolean isCustomControlSpace;

    @Schema(description = "是否被屏蔽")
    private Boolean isShielded;

    @Schema(description = "系统ID")
    private Set<String> systemIds;

    @Schema(description = "序列ID")
    private Integer sequenceId;

    @Schema(description = "上级楼层ID")
    private String parentFloorId;

    @Schema(description = "上级建筑ID")
    private String parentBuildingId;

    @Schema(description = "子节点")
    private List<DemandAirconditioningObjectVO> children;

}
