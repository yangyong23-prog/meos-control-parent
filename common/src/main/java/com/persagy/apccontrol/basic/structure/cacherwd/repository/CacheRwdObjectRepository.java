package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.param.CacheRwdObjectQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cacherwd.model.entity.CacheRwdObject;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public interface CacheRwdObjectRepository extends IService<CacheRwdObject> {

    List<CacheRwdObject> getByCustomIndexKey(@NonNull String customIndexKey);

    /**
     * 查询缓存列表
     *
     * @param queryDaoParam 查询参数
     * @return CacheRwdObject 列表
     * @since 7.260401.0
     */
    List<CacheRwdObject> list(@NonNull CacheRwdObjectQueryDaoParam queryDaoParam);

    /**
     * 根据自定义索引 key 删除所有数据
     *
     * @param customIndexKey 自定义索引 key
     * @return 删除的行数
     */
    int removeByCustomIndexKey(@NonNull String customIndexKey);

    /**
     * 根据运行监测类型获取一条数据（按更新时间倒序取第一条）
     *
     * @param runningMonitorType 运行监测类型
     * @return CacheRwdObject 对象，如果没有数据则返回 null
     */
    CacheRwdObject getOneByRunningMonitorType(RunningMonitorTypeEnum runningMonitorType);

    /**
     * 按运行监测类型分组统计缓存数量
     *
     * @return 按运行监测类型分组的数量统计，key为监测类型枚举值，value为数量
     */
    Map<RunningMonitorTypeEnum, Long> countGroupByMonitorType();
}
