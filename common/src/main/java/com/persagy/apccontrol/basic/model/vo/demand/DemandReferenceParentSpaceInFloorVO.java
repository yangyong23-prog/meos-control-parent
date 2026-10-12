package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 需求管理-楼层下引用上级的需求空间VO
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求管理-楼层下引用上级的需求空间VO")
public class DemandReferenceParentSpaceInFloorVO extends DtpRwdSpaceDTO {
}
