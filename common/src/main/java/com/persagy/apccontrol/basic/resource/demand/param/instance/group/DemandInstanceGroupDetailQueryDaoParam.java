package com.persagy.apccontrol.basic.resource.demand.param.instance.group;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * 需求实例分组详情查询数据库参数
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandInstanceGroupDetailQueryDaoParam extends BaseRepositoryQueryParam<DemandInstanceGroupDetail> {

    /**
     * 实例ID
     */
    private @Nullable Set<@NonNull String> instanceIds;

    /**
     * 关联分组ID
     */
    private @Nullable Long relGroupId;

    /**
     * 关联分组ID
     */
    private @Nullable Set<@NonNull Long> relGroupIds;

}
