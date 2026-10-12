package com.persagy.apccontrol.basic.structure.demand.model.scene.domain.identifier;

import com.persagy.apccontrol.basic.model.base.domain.Identifier;
import lombok.Data;

/**
 * 需求配置分组标识符
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
public class DemandConfigGroupIdentifier implements Identifier {

    /**
     * 配置组ID
     */
    private Long groupId;

    /**
     * 关联模版ID
     */
    private Long relTemplateId;

}
