package com.persagy.apccontrol.basic.resource.cache;

import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectQueryDaoParam;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 缓存对象资源服务。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheObjectService {

    /**
     * 查询当前项目的缓存对象。
     *
     * @param param 查询参数
     * @return 缓存对象列表
     * @since v7.260701.0
     */
    @NonNull List<CacheObject> list(@NonNull CacheObjectQueryDaoParam param);

    /**
     * 按对象 ID 批量新增或更新当前项目的缓存对象。
     *
     * @param objects 缓存对象集合
     * @since v7.260701.0
     */
    void upsertObjects(@NonNull Collection<CacheObject> objects);

    /**
     * 删除当前项目指定对象类编码的完整快照。
     *
     * @param classCodes 对象类编码集合
     * @since v7.260802.0
     */
    void deleteByClassCodes(@NonNull Collection<String> classCodes);

}
