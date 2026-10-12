package com.persagy.apccontrol.basic.resource.cache;

import com.persagy.apccontrol.basic.model.entity.cache.CacheRule;
import com.persagy.apccontrol.basic.resource.cache.param.CacheRuleBusinessKey;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 去重缓存规则资源服务。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheRuleService {

    /**
     * 按完整业务键批量查询当前项目规则。
     *
     * @param businessKeys 规则业务键集合
     * @return 规则列表
     * @since v7.260701.0
     */
    @NonNull List<CacheRule> listByBusinessKeys(@NonNull Collection<CacheRuleBusinessKey> businessKeys);

    /**
     * 按对象和信息点批量查询指定部署端规则。
     *
     * @param objectIds 对象 ID 集合
     * @param infoCodes 信息点编码集合
     * @param type      云端或边缘端规则类型
     * @return 规则列表
     * @since v7.260701.0
     */
    @NonNull List<CacheRule> listByObjectIds(
            @NonNull Collection<String> objectIds,
            @NonNull Collection<String> infoCodes,
            @NonNull CacheIotRuleTypeEnum type);

    /**
     * 批量新增或更新规则主表。
     *
     * @param rules 规则集合
     * @since v7.260701.0
     */
    void upsertRules(@NonNull Collection<CacheRule> rules);

    /**
     * 删除指定候选集合中已经没有任何缓存键关联的规则。
     *
     * @param ruleIds 规则 ID 集合
     * @since v7.260701.0
     */
    void removeOrphanRules(@NonNull Collection<Long> ruleIds);
}
