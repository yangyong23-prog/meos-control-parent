package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.model.param.demand.DemandTreeQueryParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 需求管理-对象树VO
 *
 * @author yuyao
 */
@Data
@Schema(description = "需求管理-对象树VO")
public class DemandTreeControlledObjectVO {


    @Schema(description = "树结构")
    private DemandTreeObjectVO tree;

    @Schema(description = "请求参数")
    private DemandTreeQueryParam queryParam;

    public DemandTreeControlledObjectVO() {
    }

    public DemandTreeControlledObjectVO(DemandTreeQueryParam queryParam) {
        this.queryParam = queryParam;
    }
}
