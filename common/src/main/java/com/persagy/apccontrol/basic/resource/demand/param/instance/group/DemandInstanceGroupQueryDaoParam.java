package com.persagy.apccontrol.basic.resource.demand.param.instance.group;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroup;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * 需求实例分组查询数据库参数
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandInstanceGroupQueryDaoParam extends BaseRepositoryQueryParam<DemandInstanceGroup> {

    /**
     * ID
     */
    private @Nullable Long id;

    /**
     * ID
     */
    private @Nullable Set<@NonNull Long> ids;

    /**
     * 标签, 为空时查询所有
     */
    private @Nullable String tag;

    /**
     * 模板类型, 为空时查询所有
     */
    private @Nullable DemandTemplateTypeEnum templateType;

    /**
     * 模板类型,为空时查询所有
     */
    private @Nullable List<@NonNull DemandTemplateTypeEnum> templateTypes;

    /**
     * 是否查询详情
     */
    private boolean queryDetail;

    /**
     * 详情查询参数
     */
    private DemandInstanceGroupDetailQueryDaoParam detailQueryParam;

}
