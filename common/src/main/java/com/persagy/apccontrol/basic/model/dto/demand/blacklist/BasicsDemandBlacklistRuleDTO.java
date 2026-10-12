package com.persagy.apccontrol.basic.model.dto.demand.blacklist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 需求黑名单配置
 */
@Data
@Schema(title = "需求黑名单配置")
public abstract class BasicsDemandBlacklistRuleDTO {

    public abstract void fill();
}
