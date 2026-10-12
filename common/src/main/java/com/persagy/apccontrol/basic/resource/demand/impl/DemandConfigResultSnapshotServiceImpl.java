package com.persagy.apccontrol.basic.resource.demand.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.demand.DemandConfigResultSnapshotMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResultSnapshot;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigResultSnapshotService;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 需求配置预计算结果快照服务实现
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@Validated
public class DemandConfigResultSnapshotServiceImpl
        extends BaseServiceImpl<DemandConfigResultSnapshotMapper, DemandConfigResultSnapshot>
        implements DemandConfigResultSnapshotService {

    /**
     * 快照批量插入批次大小。
     *
     * <p>快照插入会写入最终配置JSON和继承链路JSON等大字段，单批过大容易放大 SQL 包大小和数据库写入压力，
     * 因此保持 500，不跟随只按 ID 更新的批次一起放大。</p>
     */
    private static final int INSERT_BATCH_SQL_BATCH_SIZE = 500;

    /**
     * 快照关闭批次大小。
     *
     * <p>关闭开放快照只按结果ID执行 IN 更新，SQL 形态比批量插入轻；这里与结果表按 ID 删除/更新批次保持一致，
     * 减少全量刷新或模板清理时的数据库往返次数。</p>
     */
    private static final int RESULT_ID_IN_UPDATE_BATCH_SIZE = 1000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertSnapshot(@NonNull DemandConfigResult result) {
        this.insertSnapshots(Collections.singleton(result));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertSnapshots(@NonNull Collection<DemandConfigResult> results) {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        List<DemandConfigResultSnapshot> snapshots = results.stream()
                .map(DemandConfigResultSnapshot::of)
                .collect(Collectors.toList());
        // 快照可能随全量刷新批量产生，直接走mapper XML分片写入，
        // 先复用基类补齐缺失的主键和审计字段，再避免MyBatis-Plus saveBatch在大量数据下逐条处理造成性能问题。
        for (DemandConfigResultSnapshot snapshot : snapshots) {
            super.fillInsertDefaultValue(snapshot);
        }
        for (List<DemandConfigResultSnapshot> snapshotPartition : Lists.partition(
                snapshots, DemandConfigResultSnapshotServiceImpl.INSERT_BATCH_SQL_BATCH_SIZE)) {
            super.getBaseMapper().insertBatch(snapshotPartition);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeOpenSnapshots(@NonNull Collection<Long> resultIds, @NonNull LocalDateTime validEndTime) {
        if (CollectionUtils.isEmpty(resultIds)) {
            return;
        }
        for (List<Long> resultIdPartition : Lists.partition(
                Lists.newArrayList(resultIds), DemandConfigResultSnapshotServiceImpl.RESULT_ID_IN_UPDATE_BATCH_SIZE)) {
            super.lambdaUpdate()
                    .in(DemandConfigResultSnapshot::getId, resultIdPartition)
                    .eq(DemandConfigResultSnapshot::getValidEndTime, DemandConfigResultSnapshot.OPEN_END_TIME)
                    .set(DemandConfigResultSnapshot::getValidEndTime, validEndTime)
                    .update();
        }
    }
}
