package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryQueryParam;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 需求配置服务
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
public interface DemandConfigGroupService extends BaseService<DemandConfigGroup> {

    /**
     * 查询配置组
     *
     * @param param 查询参数
     * @return 配置组
     * @since 7.250202.0
     */
    Optional<DemandConfigGroup> getOpt(@NonNull @Valid DemandConfigGroupRepositoryQueryParam param);

    /**
     * 查询配置组
     *
     * @param param 查询参数
     * @return 配置组
     * @since 7.250202.0
     */
    List<DemandConfigGroup> list(@NonNull @Valid DemandConfigGroupRepositoryBatchQueryParam param);

    /**
     * 新增分组配置
     *
     * @param configGroup 分组配置
     * @since 7.250202.0
     */
    DemandConfigGroup add(@NonNull DemandConfigGroup configGroup);

    /**
     * 新增分组配置
     *
     * @param configGroups 分组配置
     * @since 7.250801.0
     */
    List<DemandConfigGroup> add(@NonNull List<DemandConfigGroup> configGroups);

    /**
     * 编辑分组名称
     *
     * @param configGroupId 分组ID
     * @param groupName     分组名称
     * @since 7.250202.0
     */
    void editGroupName(@NonNull Long configGroupId, @NonNull String groupName);

}
