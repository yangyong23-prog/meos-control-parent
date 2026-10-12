package com.persagy.apccontrol.basic.mapper.demand;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResultSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * 需求配置预计算结果快照Mapper
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Mapper
@Repository
public interface DemandConfigResultSnapshotMapper extends BaseMapper<DemandConfigResultSnapshot> {

    /**
     * 批量插入需求配置预计算结果快照
     *
     * @param snapshots 结果快照集合
     * @since v7.260701.0
     */
    void insertBatch(@NonNull @Param("snapshots") Collection<DemandConfigResultSnapshot> snapshots);

}
