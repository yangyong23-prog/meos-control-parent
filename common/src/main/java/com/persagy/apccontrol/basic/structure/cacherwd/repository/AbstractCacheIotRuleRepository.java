package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheIotRule;
import com.persagy.apccontrol.basic.resource.cache.CacheIotRuleService;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 缓存-IOT规则仓储抽象类
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Validated
public abstract class AbstractCacheIotRuleRepository implements CacheIotRuleRepository {

    @Resource
    protected CacheIotRuleService cacheIotRuleService;

    /**
     * 缓存-IOT规则类型
     *
     * @return CacheIotRuleTypeEnum
     * @since 7.260302.0
     */
    public abstract CacheIotRuleTypeEnum cacheIotRuleType();

    @Override
    public Map<String, Set<String>> mapObjectIdAndInfoCodes(@NonNull String key, @Nullable Set<String> objectIds) {
        LambdaQueryChainWrapper<CacheIotRule> queryChainWrapper = this.cacheIotRuleService.lambdaQuery()
                .select(CacheIotRule::getObjectId, CacheIotRule::getInfoCode)
                .eq(CacheIotRule::getProjectId, ContextUtil.getProjectId())
                .eq(CacheIotRule::getCustomKey, key)
                .eq(CacheIotRule::getType, this.cacheIotRuleType());
        if (objectIds != null) {
            if (CollectionUtils.isEmpty(objectIds)) {
                return Collections.emptyMap();
            }
            queryChainWrapper.in(CacheIotRule::getObjectId, objectIds);
        }
        return queryChainWrapper.list()
                .stream()
                .collect(Collectors.groupingBy(
                        CacheIotRule::getObjectId,
                        Collectors.mapping(CacheIotRule::getInfoCode, Collectors.toSet())
                ));
    }

    @Override
    public String logTag() {
        return "缓存-IOT规则仓储实现";
    }
}
