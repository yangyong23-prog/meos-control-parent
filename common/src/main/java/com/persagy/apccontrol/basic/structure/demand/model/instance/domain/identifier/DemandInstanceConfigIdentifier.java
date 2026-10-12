package com.persagy.apccontrol.basic.structure.demand.model.instance.domain.identifier;

import com.persagy.apccontrol.basic.model.base.domain.Identifier;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.Data;

/**
 * 需求配置标识符
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandInstanceConfigIdentifier implements Identifier {

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 模版类型
     */
    private DemandTemplateTypeEnum templateType;

    /**
     * 需求实例
     */
    private DemandInstanceValueObject instance;

}
