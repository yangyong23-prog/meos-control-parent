package com.persagy.apccontrol.basic.model.param.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * 需求管理-回路配置统计
 *
 * @author yuyao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "需求管理-回路配置统计")
public class CircuitObjCfgStatisticsQueryParam extends DemandTreeQueryParam {


    @Schema(description = "空间ID 集合")
    private List<String> spaceIds;

    @Schema(description = "功能类型")
    private String funcType;

    @Schema(description = "灯具类型")
    private String lampFuncType;


}
