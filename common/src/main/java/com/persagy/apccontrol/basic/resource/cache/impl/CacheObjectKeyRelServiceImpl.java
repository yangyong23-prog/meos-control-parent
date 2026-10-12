package com.persagy.apccontrol.basic.resource.cache.impl;

import com.google.common.collect.Iterables;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.cache.CacheObjectKeyRelMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectKeyRel;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectKeyRelService;
import com.persagy.apccontrol.basic.resource.cache.support.CacheBatchExecutor;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 缓存键与对象关联资源服务实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@Validated
public class CacheObjectKeyRelServiceImpl
        extends BaseServiceImpl<CacheObjectKeyRelMapper, CacheObjectKeyRel>
        implements CacheObjectKeyRelService {

    /**
     * 单次缓存键删除数量上限。
     *
     * @since v7.260701.0
     */
    private static final int DELETE_BATCH_SIZE = 2000;

    @Override
    public @NonNull List<CacheObjectKeyRel> listByCacheKeys(@NonNull Collection<String> cacheKeys) {
        if (CollectionUtils.isEmpty(cacheKeys)) {
            return Collections.emptyList();
        }
        return super.getBaseMapper().listByCacheKeys(
                ContextUtil.getProjectId(), cacheKeys);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertObjectIds(@NonNull String cacheKey, @NonNull Collection<String> objectIds) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        List<CacheObjectKeyRel> relations = new ArrayList<>(objectIds.size());
        for (String objectId : objectIds) {
            CacheObjectKeyRel relation = new CacheObjectKeyRel();
            relation.setProjectId(projectId);
            relation.setCacheKey(cacheKey);
            relation.setObjectId(objectId);
            // 手写 XML 插入不会触发 MyBatis-Plus 的主键和审计字段自动填充。
            super.fillInsertDefaultValue(relation);
            relations.add(relation);
        }
        CacheBatchExecutor.execute(
                relations, partition -> super.getBaseMapper().insertBatch(partition));
    }

    @Override
    public void removeByCacheKey(@NonNull String cacheKey) {
        super.getBaseMapper().deleteByCacheKey(
                ContextUtil.getProjectId(), cacheKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByCacheKeys(@NonNull Collection<String> cacheKeys) {
        if (CollectionUtils.isEmpty(cacheKeys)) {
            return;
        }
        for (List<String> cacheKeyPartition : Iterables.partition(
                cacheKeys, CacheObjectKeyRelServiceImpl.DELETE_BATCH_SIZE)) {
            super.getBaseMapper().deleteByCacheKeys(ContextUtil.getProjectId(), cacheKeyPartition);
        }
    }
}
