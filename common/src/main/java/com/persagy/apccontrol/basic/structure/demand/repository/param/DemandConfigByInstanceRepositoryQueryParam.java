package com.persagy.apccontrol.basic.structure.demand.repository.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 根据实例查询需求配置参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigByInstanceRepositoryQueryParam extends BaseRepositoryQueryParam<DemandConfig> {

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 模版ID
     */
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 配置组ID
     */
    private Long configGroupId;

    /**
     * 实例
     */
    @NotNull(message = "{key_9215211c1b2542e794cf219fda25978e}")
    private DemandInstanceValueObject instance;

}
