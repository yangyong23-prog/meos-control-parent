package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRule;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import com.persagy.framework.common.exception.BusinessException;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 去重缓存规则领域仓储。
 *
 * <p>业务读取直接按对象、信息点和规则类型查询主表；写入时负责把规则业务键匹配、
 * 规则主表 UPSERT 和缓存键归属关联替换编排在同一事务中。资源服务只负责当前项目
 * 隔离及批量 SQL。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheRuleRepository extends Repository {

    /**
     * 按对象映射指定部署端已经实例化的规则信息点。
     *
     * <p>查询直接按对象 ID、信息点编码和部署端规则类型限定规则主表，不使用缓存键附加
     * 业务场景范围；重复规则由信息点集合自然去重。</p>
     *
     * @param objectIds 对象 ID 集合
     * @param infoCodes 目标信息点编码集合
     * @param type      云端或边缘端规则类型
     * @return 对象 ID 到已实例化信息点编码集合
     * @since v7.260701.0
     */
    @NonNull Map<String, Set<String>> mapInfoCodesByObjectIds(
            @NonNull Collection<String> objectIds,
            @NonNull Collection<String> infoCodes,
            @NonNull CacheIotRuleTypeEnum type);

    /**
     * 完整替换单个缓存键的规则集合。
     *
     * <p>方法先按规则业务键复用已有主键，再 UPSERT 规则主表，最后替换键关联；
     * 关联写入失败时外层事务回滚主表写入。空规则集合表示清空该缓存键。</p>
     *
     * @param cacheKey 缓存键
     * @param rules    当前缓存键的完整规则集合
     * @throws BusinessException 项目级规则写锁未取得时抛出
     * @since v7.260701.0
     */
    void replaceRules(
            @NonNull String cacheKey,
            @NonNull Collection<CacheRule> rules);

    /**
     * 删除单个缓存键及其不再被引用的规则。
     *
     * @param cacheKey 缓存键
     * @throws BusinessException 项目级规则写锁未取得时抛出
     * @since v7.260701.0
     */
    void removeByCacheKey(@NonNull String cacheKey);

    /**
     * 批量删除缓存键及其不再被引用的规则。
     *
     * @param cacheKeys 缓存键集合
     * @throws BusinessException 项目级规则写锁未取得时抛出
     * @since v7.260701.0
     */
    void removeByKeys(@NonNull Collection<String> cacheKeys);
}
