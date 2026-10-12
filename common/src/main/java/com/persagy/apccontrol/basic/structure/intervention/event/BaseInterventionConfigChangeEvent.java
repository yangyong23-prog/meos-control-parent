package com.persagy.apccontrol.basic.structure.intervention.event;

import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * 基础干预配置变动事件
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseInterventionConfigChangeEvent<T extends InterventionConfigData> extends ApplicationEvent {

    /**
     * 配置模型
     */
    private final InterventionConfigModel<T> configModel;

    public BaseInterventionConfigChangeEvent(InterventionConfigModel<T> source) {
        super(source);
        this.configModel = source;
    }
}
