package com.persagy.apccontrol.basic.model.dto.demand.blacklist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * iot规则
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "iot规则")
public class DemandBlacklistInstantiateRuleDTO extends BasicsDemandBlacklistRuleDTO {

    @Schema(description = "不启用的规则")
    @NotNull(message = "{key_2637c8df98ea49eda7141d822d5dd031}")
    private List<String> nonInstantiationCodes;

    @Override
    public void fill() {

    }
}
