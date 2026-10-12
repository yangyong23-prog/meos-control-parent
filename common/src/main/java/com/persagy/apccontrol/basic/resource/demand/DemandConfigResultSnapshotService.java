package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResultSnapshot;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 需求配置预计算结果快照服务
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface DemandConfigResultSnapshotService extends BaseService<DemandConfigResultSnapshot> {

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
     * 关闭开放快照
     *
     * @param resultIds    结果ID集合
     * @param validEndTime 生效截止时间
     * @since v7.260701.0
     */
    void closeOpenSnapshots(@NonNull Collection<Long> resultIds, @NonNull LocalDateTime validEndTime);

}
