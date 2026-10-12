package com.persagy.apccontrol.basic.resource.cache.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.cache.CacheInfoCodeTemplateMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheInfoCodeTemplate;
import com.persagy.apccontrol.basic.resource.cache.CacheInfoCodeTemplateService;
import com.persagy.apccontrol.basic.resource.cache.support.CacheBatchExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;

/**
 * 缓存-信息点模版服务实现
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheInfoCodeTemplateServiceImpl
        extends BaseServiceImpl<CacheInfoCodeTemplateMapper, CacheInfoCodeTemplate>
        implements CacheInfoCodeTemplateService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertBatch(@NonNull Collection<CacheInfoCodeTemplate> templates) {
        if (CollectionUtils.isEmpty(templates)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        for (CacheInfoCodeTemplate record : templates) {
            record.setProjectId(projectId);
            // 手写 XML 不触发 MyBatis-Plus 的主键和审计字段自动填充。
            super.fillInsertDefaultValue(record);
        }
        // 统一按通用批处理器的固定上限拆分多值 INSERT，避免模板量大时生成无界 SQL。
        CacheBatchExecutor.execute(
                templates, partition -> super.getBaseMapper().insertBatch(partition));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceByCacheKeys(
            @NonNull Collection<String> cacheKeys,
            @NonNull Collection<CacheInfoCodeTemplate> templates) {
        // 删除旧范围和插入完整目标集合共享当前事务；任一分片失败都会整体回滚。
        CacheBatchExecutor.execute(cacheKeys, partition -> this.lambdaUpdate()
                .eq(CacheInfoCodeTemplate::getProjectId, ContextUtil.getProjectId())
                .in(CacheInfoCodeTemplate::getCustomKey, partition)
                .remove());
        this.insertBatch(templates);
    }
}
