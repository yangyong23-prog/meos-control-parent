package com.persagy.apccontrol.basic.structure.demand.repository.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 需求管理-未自定义配置的空间ID查询参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandFilterNotCustomizedConfigSpaceIdQueryParam extends BaseRepositoryQueryParam<DemandConfig> {

    /**
     * 模版ID
     */
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    /**
     * 场景ID, 存在场景时必传
     */
    private Long sceneId;

    /**
     * 空间ID
     */
    @NotEmpty(message = "{key_c78c2ee89cbd491ea193306bff87f524}")
    private Set<String> spaceIds;

}
