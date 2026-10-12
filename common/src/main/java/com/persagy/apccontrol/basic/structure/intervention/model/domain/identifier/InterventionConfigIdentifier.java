package com.persagy.apccontrol.basic.structure.intervention.model.domain.identifier;

import com.persagy.apccontrol.basic.model.base.domain.Identifier;
import lombok.Data;

/**
 * 需求干预配置标识符
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Data
public class InterventionConfigIdentifier implements Identifier {

    /**
     * 干预配置ID
     */
    private Long interventionConfigId;

}
