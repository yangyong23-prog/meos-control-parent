package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 需求管理-屏蔽空间树VO
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求管理-屏蔽空间树VO")
public class DemandTreeShieldedObjectVO extends DtpRwdObjectVO {

    @Schema(description = "空间数量")
    private Integer spaceCount;

    @Schema(description = "子集")
    private List<DemandTreeShieldedObjectVO> children;

}
