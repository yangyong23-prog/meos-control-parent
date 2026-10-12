package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 照明-空间详情VO
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "照明-空间详情VO")
public class LightingSpaceDetailVO extends DtpRwdObjectDTO {

    @Schema(description = "空间标签")
    private LightingSpaceTagVO tag;

    @Schema(description = "关联楼层")
    private DtpRwdObjectVO relFloor;

    @Schema(description = "关联照明回路")
    private List<LightingCircuitDetailVO> relCircuits;

    @Schema(description = "关联照明控制模块")
    private List<LightingControllerDetailVO> relControllers;

}
