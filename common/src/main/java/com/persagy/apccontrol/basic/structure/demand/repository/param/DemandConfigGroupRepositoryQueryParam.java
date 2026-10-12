package com.persagy.apccontrol.basic.structure.demand.repository.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 查询需求配置组参数
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigGroupRepositoryQueryParam extends BaseRepositoryQueryParam<DemandConfigGroup> {

    /**
     * 分组ID
     */
    private Long configGroupId;

    /**
     * 模版ID
     */
    private Long templateId;

    /**
     * 场景ID, 存在场景时必传
     */
    private Long sceneId;

    /**
     * 分组名称, 模糊匹配
     */
    private String configGroupName;

}
