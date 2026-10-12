package com.persagy.apccontrol.basic.structure.intervention.event.lighting;

import com.persagy.apccontrol.basic.structure.intervention.event.BaseInterventionConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 照明干预配置变动事件
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InterventionLightingConfigChangeEvent<T extends InterventionConfigData>
        extends BaseInterventionConfigChangeEvent<T> {

    public InterventionLightingConfigChangeEvent(InterventionConfigModel<T> source) {
        super(source);
    }
}
