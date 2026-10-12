package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 需求管理-照明需求-楼层照明回路VO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S2-5.18.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求管理-照明需求-楼层照明回路VO")
public class DemandLightingFloorWithCircuitVO extends DtpRwdObjectVO {

    @Schema(description = "关联照明回路")
    private List<DemandLightingCircuitVO> relCircuits;

}
