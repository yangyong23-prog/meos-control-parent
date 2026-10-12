package com.persagy.apccontrol.common.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.entity.cache.CacheIotRule;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.AbstractCacheIotRuleRepository;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;

/**
 * 云端-缓存-IOT规则仓储实现
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheIotRuleRepositoryImpl extends AbstractCacheIotRuleRepository {

    @Override
    public void removeByKeys(@NonNull Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        this.cacheIotRuleService.lambdaUpdate()
                .eq(CacheIotRule::getProjectId, ContextUtil.getProjectId())
                .in(CacheIotRule::getCustomKey, keys)
                .remove();
    }

    @Override
    public void add(@NonNull Collection<CacheIotRule> cacheIotRules) {
        if (CollectionUtils.isEmpty(cacheIotRules)) {
            return;
        }
        this.cacheIotRuleService.saveBatch(cacheIotRules);
    }

    @Override
    public CacheIotRuleTypeEnum cacheIotRuleType() {
        return CacheIotRuleTypeEnum.PERSISTENCE;
    }
}
