package com.persagy.apccontrol.basic.model.param.security;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 安防通用视角入参
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/6 17:24
 * @since v5.240801.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityModelViewParam {

    @Schema(description = "实例id,为空时查询所有")
    private String instanceId;

    @Schema(description = "实例类型,为空时查询所有", defaultValue = "PROJECT")
    private DemandInstanceTypeEnum instanceType;

}
