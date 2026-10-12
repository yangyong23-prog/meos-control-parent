package com.persagy.apccontrol.basic.structure.demand.gateway.scene.param;

import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 需求场景新增参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandSceneUpdateParam {

    /**
     * 场景ID
     */
    @NotNull(message = "{key_59537c1a56194bd6ae31797f5dcad6ad}")
    private Long sceneId;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 场景时段
     */
    private DemandTimeIntervalValueObject timeInterval;

    /**
     * 是否跨天, 默认不跨天
     */
    private Boolean acrossDay;

}
