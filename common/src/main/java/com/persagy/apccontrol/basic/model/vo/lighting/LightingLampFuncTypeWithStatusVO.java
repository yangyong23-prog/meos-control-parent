package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.dto.lighting.LightingLampFuncTypeTagDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

/**
 * 照明灯具功能类型带状态VO
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
@Data
@Schema(title = "照明空间带状态VO")
public class LightingLampFuncTypeWithStatusVO {

    @Schema(description = "灯具功能类型")
    private String lampFuncType;

    @Schema(description = "灯具功能类型名称")
    private String lampFuncTypeName;

    @Schema(description = "灯具功能类型标签")
    private LightingLampFuncTypeTagDTO tag;

    @Schema(description = "关联功能类型编码")
    private Set<String> relFuncTypes;

}
