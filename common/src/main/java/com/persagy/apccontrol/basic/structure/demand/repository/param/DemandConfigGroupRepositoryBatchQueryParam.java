package com.persagy.apccontrol.basic.structure.demand.repository.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

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
public class DemandConfigGroupRepositoryBatchQueryParam extends BaseRepositoryQueryParam<DemandConfigGroup> {

    /**
     * 分组ID
     */
    private Set<Long> configGroupIds;

    /**
     * 排除配置分组ID
     */
    private Long excludeConfigGroupId;

    /**
     * 模版ID
     */
    private Long templateId;

    /**
     * 场景ID, 存在场景时必传
     */
    private Long sceneId;

    /**
     * 配置分组名称, 全匹配
     */
    private String configGroupName;

}
