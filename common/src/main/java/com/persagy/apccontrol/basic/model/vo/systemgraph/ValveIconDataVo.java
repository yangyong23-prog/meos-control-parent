package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.enumtype.systemgraph.DmpObjTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 阀门图标基础数据，包含中心点，方向向量，classCode
 *
 * @author mj
 * @date 2023/4/12 10:58
 */
@ApiModel(value = "ValveIconVo", description = "阀门图标基础数据，包含中心点，方向向量，classCode")
@Data
public class ValveIconDataVo {
    @Schema(description = "阀门类（画图形用）", required = true)
    private String classCode;
    @Schema(description = "阀门对应的管道类（标记颜色用）", required = true)
    private String pipeClassCode;
    @Schema(description = "颜色", required = true)
    private BaseColorVo colorInfo;
    @Schema(description = "中心点", required = true)
    private Point3D centerPoint;
    @Schema(description = "方向向量列表", required = true)
    private List<Point3D> vectorQuantityList;
    @Schema(description = "id", required = true)
    private String id;
    @Schema(description = "类型", required = true)
    private DmpObjTypeEnum objType = DmpObjTypeEnum.EQUIPMENT;
}
