package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRule;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRuleKeyRel;
import com.persagy.apccontrol.basic.resource.cache.CacheRuleKeyRelService;
import com.persagy.apccontrol.basic.resource.cache.CacheRuleService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheRuleBusinessKey;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 去重缓存规则领域仓储实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRuleRepositoryImpl implements CacheRuleRepository {

    /**
     * 缓存规则资源服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheRuleService cacheRuleService;

    /**
     * 缓存键与规则写入归属关联资源服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheRuleKeyRelService cacheRuleKeyRelService;

    /**
     * 项目级规则业务键分配锁服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull RedissonDistributedLockerService redissonDistributedLockerService;

    @Override
    public @NonNull Map<String, Set<String>> mapInfoCodesByObjectIds(
            @NonNull Collection<String> objectIds,
            @NonNull Collection<String> infoCodes,
            @NonNull CacheIotRuleTypeEnum type) {
        List<CacheRule> rules = this.cacheRuleService.listByObjectIds(
                objectIds, infoCodes, type);
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (CacheRule rule : rules) {
            result.computeIfAbsent(rule.getObjectId(), ignored -> new LinkedHashSet<>())
                    .add(rule.getInfoCode());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceRules(
            @NonNull String cacheKey,
            @NonNull Collection<CacheRule> rules) {
        this.withProjectRuleLock(() -> this.replaceRulesUnderLock(cacheKey, rules));
    }

    /**
     * 在项目级分配锁内匹配稳定规则 ID 并替换关联。
     *
     * @param cacheKey 缓存键
     * @param rules    规则完整目标集合
     * @since v7.260701.0
     */
    private void replaceRulesUnderLock(
            @NonNull String cacheKey,
            @NonNull Collection<CacheRule> rules) {
        List<CacheRule> normalizedRules = this.normalizeRules(rules);
        List<Long> oldRuleIds = this.cacheRuleKeyRelService.listRuleIdsByCacheKey(cacheKey);
        List<CacheRuleBusinessKey> businessKeys = new ArrayList<>(normalizedRules.size());
        for (CacheRule rule : normalizedRules) {
            businessKeys.add(CacheRuleBusinessKey.of(rule));
        }
        Map<CacheRuleBusinessKey, CacheRule> existingRules =
                this.mapExistingRules(businessKeys);
        for (CacheRule rule : normalizedRules) {
            CacheRule existsRule = existingRules.get(CacheRuleBusinessKey.of(rule));
            if (existsRule != null) {
                rule.setId(existsRule.getId());
            } else {
                // 新业务键不沿用调用方可能携带的旧 ID，统一由资源服务生成新的稳定 ID。
                rule.setId(null);
            }
        }
        // 先完成主表 UPSERT；关联替换在其后执行，关联写入失败时外层事务回滚主表。
        this.cacheRuleService.upsertRules(normalizedRules);
        List<CacheRuleKeyRel> relations = new ArrayList<>(normalizedRules.size());
        for (CacheRule rule : normalizedRules) {
            CacheRuleKeyRel relation = new CacheRuleKeyRel();
            relation.setCacheKey(cacheKey);
            relation.setRuleId(rule.getId());
            relations.add(relation);
        }
        this.cacheRuleKeyRelService.replaceByCacheKey(cacheKey, relations);
        this.cacheRuleService.removeOrphanRules(oldRuleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByCacheKey(@NonNull String cacheKey) {
        this.removeByKeys(Collections.singletonList(cacheKey));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByKeys(@NonNull Collection<String> cacheKeys) {
        if (CollectionUtils.isEmpty(cacheKeys)) {
            return;
        }
        this.withProjectRuleLock(() -> {
            List<Long> oldRuleIds = this.cacheRuleKeyRelService.listRuleIdsByCacheKeys(
                    cacheKeys);
            this.cacheRuleKeyRelService.removeByCacheKeys(cacheKeys);
            this.cacheRuleService.removeOrphanRules(oldRuleIds);
        });
    }

    /**
     * 在项目级规则锁内执行主表和键关联的写操作。
     *
     * <p>替换和删除必须共享同一把锁；否则删除可能在另一事务完成主规则 UPSERT、尚未插入
     * 关联的窗口内清理暂时孤儿规则，最终留下悬空或缺失关联。</p>
     *
     * @param action 规则写操作
     * @since v7.260701.0
     */
    private void withProjectRuleLock(@NonNull Runnable action) {
        String lockKey = RedisKey.getCacheRuleWriteLockKey(ContextUtil.getProjectId());
        if (!this.redissonDistributedLockerService.tryLock(
                lockKey, TimeUnit.SECONDS, 60, -1)) {
            throw CacheObjectModelExceptionFactory.invalidStructure("cacheRule.writeLock");
        }
        boolean unlockInFinally = !TransactionSynchronizationManager.isSynchronizationActive();
        if (!unlockInFinally) {
            // 锁必须覆盖外层复合事务提交；在方法返回时提前释放仍会让并发事务读不到本次新规则。
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            CacheRuleRepositoryImpl.this.redissonDistributedLockerService
                                    .unlock(lockKey);
                        }
                    });
        }
        try {
            action.run();
        } finally {
            if (unlockInFinally) {
                this.redissonDistributedLockerService.unlock(lockKey);
            }
        }
    }

    @Override
    public @NonNull String logTag() {
        return "CacheRuleRepositoryImpl";
    }

    /**
     * 按业务键合并规则并填充当前项目。
     *
     * @param rules 原始规则集合
     * @return 当前项目且按业务键去重的规则列表
     * @since v7.260701.0
     */
    private @NonNull List<CacheRule> normalizeRules(@NonNull Collection<CacheRule> rules) {
        String projectId = ContextUtil.getProjectId();
        Map<CacheRuleBusinessKey, CacheRule> result = new LinkedHashMap<>();
        for (CacheRule rule : rules) {
            rule.setProjectId(projectId);
            CacheRuleBusinessKey businessKey = CacheRuleBusinessKey.of(rule);
            result.putIfAbsent(businessKey, rule);
        }
        return new ArrayList<>(result.values());
    }

    /**
     * 批量映射已有规则业务键。
     *
     * @param businessKeys 规则业务键集合
     * @return 业务键到已有规则的映射
     * @since v7.260701.0
     */
    private @NonNull Map<CacheRuleBusinessKey, CacheRule> mapExistingRules(
            @NonNull Collection<CacheRuleBusinessKey> businessKeys) {
        Map<CacheRuleBusinessKey, CacheRule> result = new LinkedHashMap<>();
        for (CacheRule rule : this.cacheRuleService.listByBusinessKeys(businessKeys)) {
            CacheRuleBusinessKey businessKey = CacheRuleBusinessKey.of(rule);
            result.putIfAbsent(businessKey, rule);
        }
        return result;
    }
}
