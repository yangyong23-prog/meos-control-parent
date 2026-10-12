package com.persagy.apccontrol.common.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.entity.cache.CacheIotRule;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.AbstractCacheIotRuleRepository;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;

/**
 * 本地端-缓存-IOT规则仓储实现
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheIotRuleRepositoryImpl extends AbstractCacheIotRuleRepository {

    @Override
    public void add(@NonNull Collection<CacheIotRule> cacheIotRules) {
        throw new ApplicationException(ErrorCodeConstants.USER_ERROR_A0405, "边缘端-IOT规则缓存不支持添加");
    }

    @Override
    public void removeByKeys(@NonNull Collection<String> keys) {
        throw new ApplicationException(ErrorCodeConstants.USER_ERROR_A0405, "边缘端-IOT规则缓存不支持删除");
    }

    @Override
    public CacheIotRuleTypeEnum cacheIotRuleType() {
        return CacheIotRuleTypeEnum.TWIN;
    }
}
