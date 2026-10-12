package com.persagy.apccontrol.basic.resource.cache;

import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 缓存对象模型视图资源服务。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheObjectModelViewService {

    /**
     * 查询当前项目的缓存对象模型视图。
     *
     * @param param 查询参数
     * @return 模型视图列表
     * @since v7.260701.0
     */
    @NonNull List<CacheObjectModelView> list(@NonNull CacheObjectModelViewQueryDaoParam param);

    /**
     * 按对象 ID 批量新增或更新当前项目的模型视图。
     *
     * @param modelViews 模型视图集合，由上游保证写入内容完整
     * @since v7.260701.0
     */
    void upsertModelViews(@NonNull Collection<CacheObjectModelView> modelViews);

    /**
     * 删除当前项目指定对象类编码的完整模型视图快照。
     *
     * @param classCodes 对象类编码集合
     * @since v7.260802.0
     */
    void deleteByClassCodes(@NonNull Collection<String> classCodes);

}
