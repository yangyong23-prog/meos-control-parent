package com.persagy.apccontrol.basic.model.vo.airconditioning;

import com.persagy.apccontrol.basic.model.enumtype.OnOffStateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 强制控制状态
 *
 * @author JiYinchuan
 * @since 7.250403.0
 */
@Data
public class ForceControlStatusVO {

    @Schema(description = "强制控制状态")
    private OnOffStateEnum status;

}
