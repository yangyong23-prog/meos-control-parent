package com.persagy.apccontrol.basic.resource.cache.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.cache.CacheObjectModelViewMapper;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelViewService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
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
 * 缓存对象模型视图资源服务实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@Validated
public class CacheObjectModelViewServiceImpl
        extends ServiceImpl<CacheObjectModelViewMapper, CacheObjectModelView>
        implements CacheObjectModelViewService {

    @Override
    public @NonNull List<CacheObjectModelView> list(
            @NonNull CacheObjectModelViewQueryDaoParam param) {
        Collection<String> ids = param.getIds();
        if (ids != null && CollectionUtils.isEmpty(ids)) {
            // 显式空 ID 集合必须短路，防止 XML 省略 IN 条件后误查当前项目全部模型视图。
            return Collections.emptyList();
        }
        Collection<String> classCodes = param.getClassCodes();
        if (classCodes != null && CollectionUtils.isEmpty(classCodes)) {
            return Collections.emptyList();
        }
        return super.getBaseMapper().list(
                ContextUtil.getProjectId(), param, param.toSelectColumns());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertModelViews(@NonNull Collection<CacheObjectModelView> modelViews) {
        if (CollectionUtils.isEmpty(modelViews)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        for (CacheObjectModelView modelView : modelViews) {
            modelView.setProjectId(projectId);
        }
        BaseDO.fillUpsertDefaultValue(modelViews);
        CacheBatchExecutor.execute(
                modelViews, partition -> super.getBaseMapper().upsertBatch(partition));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByClassCodes(@NonNull Collection<String> classCodes) {
        if (CollectionUtils.isEmpty(classCodes)) {
            return;
        }
        super.getBaseMapper().deleteByClassCodes(
                ContextUtil.getProjectId(), classCodes);
    }

}
