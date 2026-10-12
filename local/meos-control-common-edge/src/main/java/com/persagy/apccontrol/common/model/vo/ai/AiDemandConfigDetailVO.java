package com.persagy.apccontrol.common.model.vo.ai;

import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 需求配置详情VO
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Data
public class AiDemandConfigDetailVO {

    @Schema(description = "日期标签")
    private List<MidCalendarDateTagValueEnum> dateTags;

    @Schema(description = "价值选择配置")
    private DemandConfigAirconditioningCustomDTO valueSelect;

    @Schema(description = "环境配置")
    private Map<String, DemandConfigAirconditioningEnvironmentQualityObject> spaceIdAndConfig;

}
