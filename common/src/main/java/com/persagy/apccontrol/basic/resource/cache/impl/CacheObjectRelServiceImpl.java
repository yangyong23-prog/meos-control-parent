package com.persagy.apccontrol.basic.resource.cache.impl;

import com.google.common.collect.Iterables;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.cache.CacheObjectRelMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectRelService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelReplaceScope;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
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
 * 缓存对象关系资源服务实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@Validated
public class CacheObjectRelServiceImpl
        extends BaseServiceImpl<CacheObjectRelMapper, CacheObjectRel>
        implements CacheObjectRelService {

    /**
     * 单次关系范围删除数量上限。
     *
     * @since v7.260701.0
     */
    private static final int DELETE_SCOPE_BATCH_SIZE = 2000;

    @Override
    public @NonNull List<CacheObjectRel> listForward(@NonNull CacheObjectRelQueryDaoParam param) {
        if (CollectionUtils.isEmpty(param.getObjectIds())) {
            return Collections.emptyList();
        }
        return super.getBaseMapper().listByFrom(
                ContextUtil.getProjectId(), param, param.toSelectColumns());
    }

    @Override
    public @NonNull List<CacheObjectRel> listReverse(@NonNull CacheObjectRelQueryDaoParam param) {
        if (CollectionUtils.isEmpty(param.getRelObjectIds())) {
            return Collections.emptyList();
        }
        return super.getBaseMapper().listByTo(
                ContextUtil.getProjectId(), param, param.toSelectColumns());
    }

    @Override
    public void addRelations(@NonNull Collection<CacheObjectRel> relations) {
        String projectId = ContextUtil.getProjectId();
        for (CacheObjectRel relation : relations) {
            relation.setProjectId(projectId);
            // 手写 XML 插入不会触发 MyBatis-Plus 的主键和审计字段自动填充。
            super.fillInsertDefaultValue(relation);
        }
        CacheBatchExecutor.execute(
                relations, partition -> super.getBaseMapper().insertBatch(partition));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceRelations(@NonNull Collection<CacheObjectRelReplaceScope> scopes) {
        if (CollectionUtils.isEmpty(scopes)) {
            return;
        }
        List<CacheObjectRel> relations = new ArrayList<>();
        for (CacheObjectRelReplaceScope scope : scopes) {
            String objectId = scope.getObjectId();
            String graphCode = scope.getGraphCode();
            String relCode = scope.getRelCode();
            for (String relObjectId : scope.getRelObjectIds()) {
                CacheObjectRel relation = new CacheObjectRel();
                relation.setObjectId(objectId);
                relation.setGraphCode(graphCode);
                relation.setRelCode(relCode);
                relation.setRelObjectId(relObjectId);
                relations.add(relation);
            }
        }
        String projectId = ContextUtil.getProjectId();
        for (List<CacheObjectRelReplaceScope> partition : Iterables.partition(
                scopes, CacheObjectRelServiceImpl.DELETE_SCOPE_BATCH_SIZE)) {
            super.getBaseMapper().deleteScopes(projectId, partition);
        }
        this.addRelations(relations);
    }

    @Override
    public void removeForwardRelations(
            @NonNull Collection<String> objectIds,
            @NonNull String graphCode,
            @NonNull String relCode) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        // 复用缓存 DML 统一分片，限制万级回路生成的单条 IN SQL 长度。
        CacheBatchExecutor.execute(objectIds, partition ->
                super.getBaseMapper().deleteForward(
                        projectId, partition, graphCode, relCode));
    }

    @Override
    public void removeReverseRelations(
            @NonNull Collection<String> relObjectIds,
            @NonNull String graphCode,
            @NonNull String relCode) {
        if (CollectionUtils.isEmpty(relObjectIds)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        CacheBatchExecutor.execute(relObjectIds, partition ->
                super.getBaseMapper().deleteReverse(
                        projectId, partition, graphCode, relCode));
    }
}
