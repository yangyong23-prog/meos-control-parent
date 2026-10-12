package com.persagy.apccontrol.basic.model.vo.external.demand;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 需求空间查询参数
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求空间查询参数")
public class DemandSpaceExternalVO extends DtpRwdSpaceVO {

}
