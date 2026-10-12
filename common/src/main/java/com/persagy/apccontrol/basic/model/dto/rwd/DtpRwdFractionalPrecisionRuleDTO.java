package com.persagy.apccontrol.basic.model.dto.rwd;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 分精度规则
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/1/22 15:05
 * @since v7.250102.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "分精度规则")
@ToString(callSuper = true)
public class DtpRwdFractionalPrecisionRuleDTO extends BaseResponse {

    @Schema(description = "对象ID")
    private String objectId;

    @Schema(description = "信息点")
    private String infoCode;

    @Schema(description = "分精度规则数值时间")
    private String minute;

    @Schema(description = "分精度规则枚举形式")
    private String minuteDisplay;

}
