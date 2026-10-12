package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelViewService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 缓存对象模型视图仓储实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheObjectModelViewRepositoryImpl implements CacheObjectModelViewRepository {

    /**
     * 缓存对象模型视图资源服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelViewService cacheObjectModelViewService;

    @Override
    public @NonNull List<CacheObjectModelView> listModelViews(
            @NonNull CacheObjectModelViewQueryDaoParam param) {
        return this.cacheObjectModelViewService.list(param);
    }

    @Override
    public void upsertModelViews(@NonNull Collection<CacheObjectModelView> modelViews) {
        this.cacheObjectModelViewService.upsertModelViews(modelViews);
    }

    @Override
    public void removeModelViewsByClassCodes(@NonNull Collection<String> classCodes) {
        this.cacheObjectModelViewService.deleteByClassCodes(classCodes);
    }

    @Override
    public @NonNull String logTag() {
        return "CacheObjectModelViewRepositoryImpl";
    }
}
