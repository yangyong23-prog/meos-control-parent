package com.persagy.apccontrol.basic.model.vo.external.demand;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 需求空间实例树对外VO
 *
 * @author JiYinchuan
 * @since 5.241202.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求空间实例树对外VO")
public class DemandTreeInstanceSpaceExternalVO extends DtpRwdSpaceDTO {

    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType = DemandInstanceTypeEnum.SPACE;

}
