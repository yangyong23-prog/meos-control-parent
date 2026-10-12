package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroup;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

public interface DemandInstanceGroupResource extends BaseService<DemandInstanceGroup> {

    /**
     * 查询需求实例分组列表
     *
     * @param param 查询参数
     * @return 需求实例分组列表
     * @since 7.250602.0
     */
    List<DemandInstanceGroupPO> list(@NonNull @Valid DemandInstanceGroupQueryDaoParam param);

    /**
     * 新增需求实例分组
     *
     * @param domain 需求实例分组
     * @return 新增结果
     * @since 7.250602.0
     */
    DemandInstanceGroup add(@NonNull DemandInstanceGroup domain);

    /**
     * 删除需求实例分组
     *
     * @param ids 需求实例分组ID
     * @return 是否成功
     * @since 7.250602.0
     */
    boolean del(@NonNull Set<Long> ids);

}
