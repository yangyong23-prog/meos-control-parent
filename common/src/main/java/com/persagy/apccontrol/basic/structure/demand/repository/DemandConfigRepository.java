package com.persagy.apccontrol.basic.structure.demand.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.repository.param.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 需求配置仓储
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface DemandConfigRepository extends Repository {

    /**
     * 存在自定义配置实例的数量
     *
     * @param date       查询日期, 不为 {@literal null} 且为历史日期时查询快照表, 否则查询当前配置
     * @param templateId 模版ID
     * @return 存在自定义配置实例的数量
     * @since 5.241201.0
     */
    int countExistsCustomizedInstance(@Nullable LocalDate date, @NonNull Long templateId);

    /**
     * 根据实例查询配置
     *
     * @param param 查询参数
     * @return 配置
     * @since 5.240902.0
     */
    Optional<DemandConfig> getOptByInstance(@NonNull @Valid DemandConfigByInstanceRepositoryQueryParam param);

    /**
     * 根据实例查询配置
     *
     * @param param 查询参数
     * @return 配置
     * @since 5.240902.0
     */
    List<DemandConfig> listByInstances(@NonNull @Valid DemandConfigByInstanceRepositoryBatchQueryParam param);

    /**
     * 过滤未自定义配置的空间ID
     *
     * @param param 查询参数
     * @return 未自定义的空间ID
     * @since 5.241102.0
     */
    Set<String> filterNotCustomizedConfigSpaceIds(@NonNull DemandFilterNotCustomizedConfigSpaceIdQueryParam param);

    /**
     * 新增/更新配置
     *
     * @param param 新增/更新参数
     * @return 新增/更新结果
     * @since 5.240902.0
     */
    Map<DataTypeEnum, List<DemandConfigChangeEvent.Config>> addOrEditConfig(
            @NonNull @Valid DemandConfigAddOrEditParam param);

    /**
     * 新增/更新配置
     *
     * @param params 新增/更新参数
     * @since 7.250902.0
     */
    void addOrEditConfig(@NonNull @Valid List<DemandConfigAddOrEditParam> params);

    /**
     * 批量更新已存在的配置，不发布配置变更事件。
     *
     * <p>仅用于一次上层保存中的内部清理，由上层最终配置事件统一刷新结果。</p>
     *
     * @param configs 已填充主键和最终配置数据的实体
     * @since v7.260701.0
     */
    void editConfigDataWithoutEvent(@NonNull List<DemandConfig> configs);

    /**
     * 根据配置ID仅删除配置
     *
     * @param configIds 配置ID
     * @since 5.240902.0
     */
    void onlyDelConfigById(@NonNull Set<Long> configIds);

    /**
     * 根据实例删除配置
     *
     * @param param 删除参数
     * @since 5.240902.0
     */
    void delConfigByInstance(@NonNull @Valid DemandConfigByInstanceRepositoryRemoveParam param);

}
