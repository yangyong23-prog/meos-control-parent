package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectRelService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelReplaceScope;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 缓存对象关系仓储实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheObjectRelRepositoryImpl implements CacheObjectRelRepository {

    /**
     * 缓存对象关系资源服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRelService cacheObjectRelService;

    @Override
    public @NonNull List<CacheObjectRel> listForward(@NonNull CacheObjectRelQueryDaoParam param) {
        return this.cacheObjectRelService.listForward(param);
    }

    @Override
    public @NonNull List<CacheObjectRel> listReverse(@NonNull CacheObjectRelQueryDaoParam param) {
        return this.cacheObjectRelService.listReverse(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRelations(@NonNull Collection<CacheObjectRel> relations) {
        this.cacheObjectRelService.addRelations(relations);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceRelations(@NonNull Collection<CacheObjectRelReplaceScope> scopes) {
        this.cacheObjectRelService.replaceRelations(scopes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeForwardRelations(
            @NonNull Collection<String> objectIds,
            @NonNull String graphCode,
            @NonNull String relCode) {
        this.cacheObjectRelService.removeForwardRelations(objectIds, graphCode, relCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeReverseRelations(
            @NonNull Collection<String> relObjectIds,
            @NonNull String graphCode,
            @NonNull String relCode) {
        this.cacheObjectRelService.removeReverseRelations(relObjectIds, graphCode, relCode);
    }

    @Override
    public @NonNull String logTag() {
        return "CacheObjectRelRepositoryImpl";
    }

}
