package com.persagy.apccontrol.basic.resource.cache;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.cache.CacheInfoCodeTemplate;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

/**
 * 缓存-信息点模版服务
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
public interface CacheInfoCodeTemplateService extends BaseService<CacheInfoCodeTemplate> {

    /**
     * 分片批量新增当前项目的模板元数据。
     *
     * @param templates 模板元数据
     * @since v7.260701.0
     */
    void insertBatch(@NonNull Collection<CacheInfoCodeTemplate> templates);

    /**
     * 在当前项目内原子替换指定缓存键的模板元数据。
     *
     * @param cacheKeys 需要清理的旧、新缓存键并集
     * @param templates 当前完整模板元数据
     * @since v7.260701.0
     */
    void replaceByCacheKeys(
            @NonNull Collection<String> cacheKeys,
            @NonNull Collection<CacheInfoCodeTemplate> templates);
}
