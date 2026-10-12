package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandConfigCancelReferenceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 需求引用操作请求参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "需求引用操作请求参数")
public class DemandReferenceParam extends DemandBasicsOperateParam {

    //操作参数
    @Schema(description = "引用状态")
    private DemandConfigCancelReferenceTypeEnum referenceType;

}
