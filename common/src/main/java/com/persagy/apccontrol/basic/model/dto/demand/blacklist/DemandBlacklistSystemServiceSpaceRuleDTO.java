package com.persagy.apccontrol.basic.model.dto.demand.blacklist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 系统服务的空间
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "系统服务的空间")
public class DemandBlacklistSystemServiceSpaceRuleDTO extends BasicsDemandBlacklistRuleDTO {
    
    @Schema(description = "系统 ID")
    @NotNull(message = "{key_c9da5b5bc7764f1eb6c0a8a89d1e6011}")
    private List<String> systemIds;
    
    @Override
    public void fill() {
    
    }
}
