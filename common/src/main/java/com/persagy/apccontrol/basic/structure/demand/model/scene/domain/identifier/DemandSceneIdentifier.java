package com.persagy.apccontrol.basic.structure.demand.model.scene.domain.identifier;

import com.persagy.apccontrol.basic.model.base.domain.Identifier;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;

/**
 * 需求场景标识符
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandSceneIdentifier implements Identifier {

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 需求模版类型
     */
    private DemandTemplateTypeEnum templateType;

    /**
     * 需求模版标签
     */
    private String templateTag;

    /**
     * 关联模版ID
     */
    private Long relTemplateId;

}
