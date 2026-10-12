package com.persagy.apccontrol.basic.structure.demand.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultBusinessKey;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultRepositoryQueryParam;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 需求配置预计算结果仓储
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface DemandConfigResultRepository extends Repository {

    /**
     * 根据业务键查询需求配置预计算结果
     *
     * @param businessKey 业务键
     * @return 需求配置预计算结果
     * @since v7.260701.0
     */
    @NonNull Optional<DemandConfigResult> getOptByBusinessKey(@NonNull @Valid DemandConfigResultBusinessKey businessKey);

    /**
     * 查询需求配置预计算结果
     *
     * @param param 查询参数
     * @return 需求配置预计算结果
     * @since v7.260701.0
     */
    @NonNull List<DemandConfigResult> list(@NonNull @Valid DemandConfigResultRepositoryQueryParam param);

    /**
     * 新增或更新结果，并维护开放快照
     *
     * @param result 需求配置预计算结果
     * @return 新增或更新后的结果
     * @since v7.260701.0
     */
    @NonNull DemandConfigResult upsert(@NonNull @Valid DemandConfigResult result);

    /**
     * 批量新增或更新结果，并维护开放快照
     *
     * @param results 需求配置预计算结果集合
     * @return 新增或更新后的结果集合
     * @since v7.260701.0
     */
    @NonNull List<DemandConfigResult> upsert(@NonNull @Valid Collection<DemandConfigResult> results);

    /**
     * 同步删除失效结果并新增或更新有效结果
     *
     * @param deleteParams 删除定位参数集合
     * @param upsertParams 新增或更新结果集合
     * @return 新增或更新后的结果集合
     * @since v7.260701.0
     */
    @NonNull List<DemandConfigResult> syncResults(
            @NonNull @Valid Collection<DemandConfigResult> deleteParams,
            @NonNull @Valid Collection<DemandConfigResult> upsertParams);

    /**
     * 按模板和场景刷新范围同步结果
     *
     * <p>该方法会先获取刷新范围写锁，再查询锁内最新结果并计算删除差异，确保旧结果查询、
     * 删除和新增或更新处于同一个事务及写锁范围内。</p>
     *
     * @param scopeParam  刷新范围查询参数，必须指定关联模板ID和明确的场景范围
     * @param upsertParams 新增或更新结果集合
     * @return 新增或更新后的结果集合
     * @since v7.260701.0
     */
    @NonNull List<DemandConfigResult> syncRefreshScope(
            @NonNull @Valid DemandConfigResultRepositoryQueryParam scopeParam,
            @NonNull @Valid Collection<DemandConfigResult> upsertParams);

    /**
     * 插入开放快照
     *
     * @param result 需求配置预计算结果
     * @since v7.260701.0
     */
    void insertSnapshot(@NonNull DemandConfigResult result);

    /**
     * 批量插入开放快照
     *
     * @param results 需求配置预计算结果集合
     * @since v7.260701.0
     */
    void insertSnapshots(@NonNull Collection<DemandConfigResult> results);

    /**
     * 关闭指定结果开放快照
     *
     * @param resultIds 结果ID集合
     * @since v7.260701.0
     */
    void closeSnapshot(@NonNull Collection<Long> resultIds);

    /**
     * 更新屏蔽空间标识
     *
     * @param businessKey 业务键
     * @param shielded    是否屏蔽
     * @since v7.260701.0
     */
    void updateShielded(@NonNull @Valid DemandConfigResultBusinessKey businessKey, boolean shielded);

    /**
     * 批量更新屏蔽空间标识
     *
     * @param businessKeys 业务键集合
     * @param shielded     是否屏蔽
     * @since v7.260701.0
     */
    void updateShielded(@NonNull @Valid Collection<DemandConfigResultBusinessKey> businessKeys, boolean shielded);

    /**
     * 按查询参数删除结果，并关闭开放快照
     *
     * @param param 查询参数
     * @since v7.260701.0
     */
    void removeResults(@NonNull @Valid DemandConfigResultRepositoryQueryParam param);

}
