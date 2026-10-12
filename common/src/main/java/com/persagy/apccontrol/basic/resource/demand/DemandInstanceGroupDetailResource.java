package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupDetailQueryDaoParam;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

public interface DemandInstanceGroupDetailResource extends BaseService<DemandInstanceGroupDetail> {

    /**
     * 查询需求实例分组详情列表
     *
     * @param param 查询参数
     * @return 需求实例分组详情列表
     * @since 7.250602.0
     */
    List<DemandInstanceGroupDetail> list(@NonNull @Valid DemandInstanceGroupDetailQueryDaoParam param);

    /**
     * 删除需求实例分组详情
     *
     * @param ids 需求实例分组详情ID
     * @return 是否成功
     * @since 7.250602.0
     */
    boolean del(@NonNull Set<Long> ids);

}
