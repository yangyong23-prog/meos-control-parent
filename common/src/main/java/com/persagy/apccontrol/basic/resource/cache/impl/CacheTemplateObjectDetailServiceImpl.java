package com.persagy.apccontrol.basic.resource.cache.impl;

import com.google.common.collect.Iterables;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.cache.CacheTemplateObjectDetailMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import com.persagy.apccontrol.basic.resource.cache.CacheTemplateObjectDetailService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheTemplateObjectDetailBusinessKey;
import com.persagy.apccontrol.basic.resource.cache.param.CacheTemplateObjectDetailQueryDaoParam;
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
 * 缓存模板对象取值明细资源服务实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@Validated
public class CacheTemplateObjectDetailServiceImpl
        extends BaseServiceImpl<CacheTemplateObjectDetailMapper, CacheTemplateObjectDetail>
        implements CacheTemplateObjectDetailService {

    /**
     * 单次完整业务键查询数量上限。
     *
     * @since v7.260701.0
     */
    private static final int QUERY_BATCH_SIZE = 2000;

    @Override
    public @NonNull List<CacheTemplateObjectDetail> listByBusinessKeys(
            @NonNull CacheTemplateObjectDetailQueryDaoParam param) {
        if (CollectionUtils.isEmpty(param.getBusinessKeys())) {
            return Collections.emptyList();
        }
        String projectId = ContextUtil.getProjectId();
        String[] withColumns = param.toSelectColumns();
        List<CacheTemplateObjectDetail> result = new ArrayList<>();
        // 分片只限制 OR 条件数量；每个分组始终保留完整复合业务键，避免组合误命中。
        for (List<CacheTemplateObjectDetailBusinessKey> businessKeyPartition : Iterables.partition(
                param.getBusinessKeys(),
                CacheTemplateObjectDetailServiceImpl.QUERY_BATCH_SIZE)) {
            CacheTemplateObjectDetailQueryDaoParam partitionParam =
                    new CacheTemplateObjectDetailQueryDaoParam();
            partitionParam.setBusinessKeys(businessKeyPartition);
            result.addAll(super.getBaseMapper().listByBusinessKeys(
                    projectId, partitionParam, withColumns));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertDetails(@NonNull Collection<CacheTemplateObjectDetail> details) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        for (CacheTemplateObjectDetail detail : details) {
            // 当前项目上下文是持久化边界的权威来源，不能信任调用方预设的项目 ID。
            detail.setProjectId(projectId);
            // 手写 XML 插入不会触发 MyBatis-Plus 的主键和审计字段自动填充。
            super.fillInsertDefaultValue(detail);
        }
        CacheBatchExecutor.execute(
                details, partition -> super.getBaseMapper().insertBatch(partition));
    }

    @Override
    public void removeByTemplateKey(@NonNull String templateKey) {
        super.getBaseMapper().deleteByTemplateKey(
                ContextUtil.getProjectId(), templateKey);
    }

}
