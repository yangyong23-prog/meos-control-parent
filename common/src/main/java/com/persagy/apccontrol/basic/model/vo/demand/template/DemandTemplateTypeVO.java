package com.persagy.apccontrol.basic.model.vo.demand.template;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 需求管理-需求日历模版类型VO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-需求日历模版类型VO")
public class DemandTemplateTypeVO {

    @Schema(description = "需求类型")
    private DemandTypeEnum demandType;

    @Schema(description = "产品功能编码")
    private String productCode;

    @Schema(description = "模版类型")
    private List<BaseEnumVO> templateTypes;

}
