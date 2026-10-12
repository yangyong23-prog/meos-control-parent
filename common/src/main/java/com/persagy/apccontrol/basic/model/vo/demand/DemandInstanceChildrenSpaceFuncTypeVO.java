package com.persagy.apccontrol.basic.model.vo.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 需求实例子集空间功能类型VO
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@Schema(title = "需求实例子集空间功能类型VO")
public class DemandInstanceChildrenSpaceFuncTypeVO {

    @Schema(description = "空间功能类型编码")
    private String funcType;

    @Schema(description = "空间功能类型名称")
    private String funcTypeName;

    @Schema(description = "关联空间ID")
    private List<String> relSpaceIds;

}
