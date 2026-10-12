package com.persagy.apccontrol.basic.resource.demand.param.lighting.scene;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 需求配置-照明需求-场景更新参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigLightingSceneRemoveDaoParam implements BaseCheckParamProvider {

    /**
     * ID
     */
    private Long id;

}
