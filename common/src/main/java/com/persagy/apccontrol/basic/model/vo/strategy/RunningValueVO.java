package com.persagy.apccontrol.basic.model.vo.strategy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 运行值VO
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "运行值VO")
public class RunningValueVO<T> {

    @Schema(description = "实际值")
    private T actualValue;

    @Schema(description = "设定值")
    private T settingValue;

}
