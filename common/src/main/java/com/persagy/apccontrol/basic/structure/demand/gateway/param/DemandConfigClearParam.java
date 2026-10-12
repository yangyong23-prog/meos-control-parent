package com.persagy.apccontrol.basic.structure.demand.gateway.param;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandConfigClearTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 需求配置清除参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandConfigClearParam {

    /**
     * 模版ID
     */
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 实例类型
     */
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private DemandInstanceTypeEnum instanceType;

    /**
     * 实例ID
     */
    @NotEmpty(message = "{key_e8dc2fefc0e244ff95cc77a3352c1826}")
    private Set<String> instanceIds;

    /**
     * 清除类型
     */
    @NotNull(message = "{key_e325eaac7c7a4694900da6b4266ca896}")
    private DemandConfigClearTypeEnum clearConfigType;

    /**
     * 配置组ID
     */
    private Long configGroupId;

}
