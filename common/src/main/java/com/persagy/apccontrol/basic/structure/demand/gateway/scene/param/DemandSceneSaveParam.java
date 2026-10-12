package com.persagy.apccontrol.basic.structure.demand.gateway.scene.param;

import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 需求场景新增参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandSceneSaveParam {

    /**
     * 场景名称
     */
    @NotBlank(message = "{key_364c43d7367446ba9e8ff60070ec270d}")
    private String sceneName;

    /**
     * 场景时段
     */
    @NotNull(message = "{key_3ed2ccd4c6034349807b19c347f714bb}")
    private DemandTimeIntervalValueObject timeInterval;

    /**
     * 是否跨天, 默认不跨天
     */
    private Boolean acrossDay;

}
