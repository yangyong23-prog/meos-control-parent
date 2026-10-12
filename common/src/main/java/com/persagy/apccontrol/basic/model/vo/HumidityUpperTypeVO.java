package com.persagy.apccontrol.basic.model.vo;

import com.persagy.validate.utility.annotation.NumberToScaleString;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 湿度上限类型VO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "湿度上限类型VO")
public class HumidityUpperTypeVO extends KeyValueVO {

    @Schema(description = "湿度上限值")
    @NumberToScaleString
    private BigDecimal upperNumber;

}
