package com.persagy.apccontrol.basic.resource.cache.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.cache.CacheObjectMapper;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectQueryDaoParam;
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
 * 缓存对象资源服务实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@Validated
public class CacheObjectServiceImpl extends ServiceImpl<CacheObjectMapper, CacheObject>
        implements CacheObjectService {

    @Override
    public @NonNull List<CacheObject> list(@NonNull CacheObjectQueryDaoParam param) {
        Collection<String> ids = param.getIds();
        if (ids != null && CollectionUtils.isEmpty(ids)) {
            // 显式空 ID 集合必须短路，防止 XML 省略 IN 条件后误查当前项目全表。
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
    public void upsertObjects(@NonNull Collection<CacheObject> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        for (CacheObject object : objects) {
            object.setProjectId(projectId);
        }
        BaseDO.fillUpsertDefaultValue(objects);
        CacheBatchExecutor.execute(
                objects, partition -> super.getBaseMapper().upsertBatch(partition));
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
