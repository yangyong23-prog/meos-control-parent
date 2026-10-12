package com.persagy.apccontrol.basic.model.dto.demand.blacklist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 末端服务的空间
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "末端服务的空间")
public class DemandBlacklistAirTerminalServiceSpaceRuleDTO extends BasicsDemandBlacklistRuleDTO {
    
    @Override
    public void fill() {
    
    }
}
