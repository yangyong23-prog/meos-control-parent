package com.persagy.apccontrol.basic.model.param.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;
import java.util.Set;

/**
 * 物理世界-存在规则信息点查询参数
 *
 * @author JiYinchuan
 * @since 7.251202.0
 */
@Data
@Schema(title = "物理世界-存在规则信息点查询参数")
public class DtpRwdExistsRuleInfoCodeQueryParam {

    @Schema(description = "设备类编码和对象ID")
    @NotEmpty(message = "classCode2ObjectIds can not be null")
    private Map<String, Set<String>> classCode2ObjectIds;

    @Schema(description = "信息点编码")
    @NotEmpty(message = "infoCodes can not be null")
    private Set<String> infoCodes;

}
