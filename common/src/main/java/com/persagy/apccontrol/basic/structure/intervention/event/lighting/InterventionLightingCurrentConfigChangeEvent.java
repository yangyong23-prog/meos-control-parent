package com.persagy.apccontrol.basic.structure.intervention.event.lighting;

import com.persagy.apccontrol.basic.structure.intervention.event.BaseInterventionConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.apccontrol.basic.structure.intervention.model.dto.ReleaseInterventionCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * 照明干预当前生效配置变动事件
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InterventionLightingCurrentConfigChangeEvent<T extends InterventionConfigData>
        extends BaseInterventionConfigChangeEvent<T> {

    /**
     * 解除干预指令
     */
    private final Collection<ReleaseInterventionCommand> releaseCommands;

    public InterventionLightingCurrentConfigChangeEvent(
            InterventionConfigModel<T> source,
            Collection<ReleaseInterventionCommand> releaseCommands) {
        super(source);
        this.releaseCommands = Optional.ofNullable(releaseCommands).orElse(Collections.emptyList());
    }

    /**
     * 解除指令
     *
     * @author JiYinchuan
     * @since 5.240702.0
     */
    @Data
    public static class ReleaseCommands {

        /**
         * 对象ID
         */
        private String objectId;

        /**
         * 信息点编码
         *
         * @see com.persagy.apccontrol.common.model.em.AutoMaticInfoCodeEnum#CONTROL_DEMAND
         * @see com.persagy.apccontrol.common.model.em.AutoMaticInfoCodeEnum#ORD_LIGHT_CONTROL_DEMAND
         * @see com.persagy.apccontrol.common.model.em.AutoMaticInfoCodeEnum#AD_LIGHT_CONTROL_DEMAND
         * @see com.persagy.apccontrol.common.model.em.AutoMaticInfoCodeEnum#SIGN_LIGHT_CONTROL_DEMAND
         * @see com.persagy.apccontrol.common.model.em.AutoMaticInfoCodeEnum#DEC_LIGHT_CONTROL_DEMAND
         * @see com.persagy.apccontrol.common.model.em.AutoMaticInfoCodeEnum#CONST_LIGHT_CONTROL_DEMAND
         */
        @SuppressWarnings("JavadocReference")
        private String infoCode;

    }
}
