package com.persagy.apccontrol.basic.structure.intervention.infrastructure;

import com.persagy.apccontrol.basic.model.base.domain.Infrastructure;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.param.InterventionConfigQueryParam;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.identifier.InterventionConfigIdentifier;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * 干预需求配置基础设施服务
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
public interface InterventionConfigQueryInfrastructure<T extends InterventionConfigData>
        extends Infrastructure<InterventionConfigIdentifier, InterventionConfigModel<T>> {

    /**
     * 根据ID获取干预配置模型
     *
     * @param id 干预ID
     * @return 配置模型
     * @since 5.240702.0
     */
    Optional<InterventionConfigModel<T>> of(@NonNull Long id);

    /**
     * 生成干预配置模型
     *
     * @param param 参数
     * @return 干预配置模型
     * @since 5.240702.0
     */
    List<InterventionConfigModel<T>> of(@NonNull InterventionConfigQueryParam param);

}
