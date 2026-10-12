package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 照明-照明控制模块标签VO
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Schema(title = "照明-照明控制模块标签VO")
public class LightingControllerTagVO {

    @Schema(description = "在线状态")
    private InfoCodeValueVO onlineStatus;

}
