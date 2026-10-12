package com.persagy.apccontrol.basic.resource.cache.impl;

import com.google.common.collect.Iterables;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.cache.CacheRuleKeyRelMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRuleKeyRel;
import com.persagy.apccontrol.basic.resource.cache.CacheRuleKeyRelService;
import com.persagy.apccontrol.basic.resource.cache.support.CacheBatchExecutor;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 缓存键与规则写入归属关联资源服务实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@Validated
public class CacheRuleKeyRelServiceImpl
        extends BaseServiceImpl<CacheRuleKeyRelMapper, CacheRuleKeyRel>
        implements CacheRuleKeyRelService {

    /**
     * 单次缓存键删除数量上限。
     *
     * @since v7.260701.0
     */
    private static final int DELETE_BATCH_SIZE = 2000;

    @Override
    public @NonNull List<Long> listRuleIdsByCacheKey(@NonNull String cacheKey) {
        return super.getBaseMapper().listRuleIdsByCacheKey(
                ContextUtil.getProjectId(), cacheKey);
    }

    @Override
    public @NonNull List<Long> listRuleIdsByCacheKeys(
            @NonNull Collection<String> cacheKeys) {
        if (CollectionUtils.isEmpty(cacheKeys)) {
            return Collections.emptyList();
        }
        return super.getBaseMapper().listRuleIdsByCacheKeys(
                ContextUtil.getProjectId(), cacheKeys);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceByCacheKey(
            @NonNull String cacheKey,
            @NonNull Collection<CacheRuleKeyRel> relations) {
        super.getBaseMapper().deleteByCacheKey(ContextUtil.getProjectId(), cacheKey);
        if (CollectionUtils.isEmpty(relations)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        for (CacheRuleKeyRel relation : relations) {
            relation.setProjectId(projectId);
            relation.setCacheKey(cacheKey);
            super.fillInsertDefaultValue(relation);
        }
        CacheBatchExecutor.execute(
                relations,
                partition -> super.getBaseMapper().insertBatch(partition));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByCacheKeys(@NonNull Collection<String> cacheKeys) {
        if (CollectionUtils.isEmpty(cacheKeys)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        for (List<String> partition : Iterables.partition(
                cacheKeys, CacheRuleKeyRelServiceImpl.DELETE_BATCH_SIZE)) {
            super.getBaseMapper().deleteByCacheKeys(projectId, partition);
        }
    }
}
