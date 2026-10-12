package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultRepositoryQueryParam;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 需求配置预计算结果服务
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface DemandConfigResultService extends BaseService<DemandConfigResult> {

    /**
     * 查询需求配置预计算结果
     *
     * @param param 查询参数
     * @return 需求配置预计算结果
     * @since v7.260701.0
     */
    @NonNull List<DemandConfigResult> list(@NonNull DemandConfigResultRepositoryQueryParam param);

    /**
     * 分页查询需求配置预计算结果ID
     *
     * @param param 查询参数
     * @param limit 查询数量上限
     * @return 需求配置预计算结果ID
     * @since v7.260701.0
     */
    @NonNull List<Long> listIds(@NonNull DemandConfigResultRepositoryQueryParam param, int limit);

    /**
     * 批量新增需求配置预计算结果
     *
     * @param results 预计算结果集合
     * @since v7.260701.0
     */
    void insertResults(@NonNull Collection<DemandConfigResult> results);

    /**
     * 根据ID删除需求配置预计算结果
     *
     * @param ids 预计算结果ID集合
     * @return 是否删除成功
     * @since v7.260701.0
     */
    boolean removeResultByIds(@NonNull Collection<Long> ids);

}
