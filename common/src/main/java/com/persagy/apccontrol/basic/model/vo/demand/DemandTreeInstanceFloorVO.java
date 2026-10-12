package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 需求楼层实例树VO
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求楼层实例树VO")
public class DemandTreeInstanceFloorVO extends DtpRwdFloorDTO {

    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType = DemandInstanceTypeEnum.FLOOR;

    @Schema(description = "子集")
    private List<DemandTreeInstanceSpaceVO> children;

}
