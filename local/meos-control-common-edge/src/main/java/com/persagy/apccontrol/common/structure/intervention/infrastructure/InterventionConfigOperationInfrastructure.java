package com.persagy.apccontrol.common.structure.intervention.infrastructure;

import com.persagy.apccontrol.basic.model.base.domain.Infrastructure;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.identifier.InterventionConfigIdentifier;
import org.springframework.lang.NonNull;

/**
 * 干预需求配置基础设施服务
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
public interface InterventionConfigOperationInfrastructure<T extends InterventionConfigData>
        extends Infrastructure<InterventionConfigIdentifier, InterventionConfigModel<T>> {

    /**
     * 解除干预
     *
     * @param model 干预配置模型
     * @return 操作是否成功
     * @since 5.240702.0
     */
    boolean release(@NonNull InterventionConfigModel<T> model);

}
