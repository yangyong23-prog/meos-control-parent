package com.persagy.apccontrol.basic.resource.cache.impl;

import com.google.common.collect.Iterables;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.cache.CacheRuleMapper;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRule;
import com.persagy.apccontrol.basic.resource.cache.CacheRuleService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheRuleBusinessKey;
import com.persagy.apccontrol.basic.resource.cache.support.CacheBatchExecutor;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
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
 * 去重缓存规则资源服务实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Service
@Validated
public class CacheRuleServiceImpl extends BaseServiceImpl<CacheRuleMapper, CacheRule>
        implements CacheRuleService {

    /**
     * 单次复合业务键查询或孤儿规则删除数量上限。
     *
     * @since v7.260701.0
     */
    private static final int COMPOSITE_BATCH_SIZE = 2000;

    @Override
    public @NonNull List<CacheRule> listByBusinessKeys(
            @NonNull Collection<CacheRuleBusinessKey> businessKeys) {
        if (CollectionUtils.isEmpty(businessKeys)) {
            return Collections.emptyList();
        }
        List<CacheRule> result = new ArrayList<>();
        String projectId = ContextUtil.getProjectId();
        for (List<CacheRuleBusinessKey> partition : Iterables.partition(
                businessKeys, CacheRuleServiceImpl.COMPOSITE_BATCH_SIZE)) {
            result.addAll(super.getBaseMapper().listByBusinessKeys(projectId, partition));
        }
        return result;
    }

    @Override
    public @NonNull List<CacheRule> listByObjectIds(
            @NonNull Collection<String> objectIds,
            @NonNull Collection<String> infoCodes,
            @NonNull CacheIotRuleTypeEnum type) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Collections.emptyList();
        }
        return super.getBaseMapper().listByObjectIds(
                ContextUtil.getProjectId(), objectIds, infoCodes, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertRules(@NonNull Collection<CacheRule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        for (CacheRule rule : rules) {
            rule.setProjectId(projectId);
        }
        BaseDO.fillUpsertDefaultValue(rules);
        // 保留 BaseServiceImpl 对新规则的 ASSIGN_ID 生成；审计字段已由批量方法统一填充。
        for (CacheRule rule : rules) {
            if (rule.getId() == null) {
                // 父类审计分支看到的字段均非空，因此这里只承担缺失 Long ID 的生成职责。
                super.fillInsertDefaultValue(rule);
            }
        }
        CacheBatchExecutor.execute(
                rules, partition -> super.getBaseMapper().upsertBatch(partition));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeOrphanRules(@NonNull Collection<Long> ruleIds) {
        if (CollectionUtils.isEmpty(ruleIds)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        for (List<Long> partition : Iterables.partition(
                ruleIds, CacheRuleServiceImpl.COMPOSITE_BATCH_SIZE)) {
            super.getBaseMapper().deleteOrphanByIds(projectId, partition);
        }
    }
}
