package com.persagy.apccontrol.basic.mapper.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRule;
import com.persagy.apccontrol.basic.resource.cache.param.CacheRuleBusinessKey;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 去重缓存规则 Mapper。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Mapper
@Repository
public interface CacheRuleMapper extends BaseMapper<CacheRule> {

    /**
     * 按对象、信息点和规则类型批量查询规则主表。
     *
     * @param projectId 项目 ID
     * @param objectIds 对象 ID
     * @param infoCodes 信息点编码
     * @param type      云端或边缘规则类型
     * @return 规则列表
     * @since v7.260701.0
     */
    @NonNull List<CacheRule> listByObjectIds(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("objectIds") Collection<String> objectIds,
            @NonNull @Param("infoCodes") Collection<String> infoCodes,
            @NonNull @Param("type") CacheIotRuleTypeEnum type);

    /**
     * 按完整规则业务键批量查询当前项目的规则。
     *
     * @param projectId    项目 ID
     * @param businessKeys 规则业务键集合
     * @return 规则列表
     * @since v7.260701.0
     */
    @NonNull List<CacheRule> listByBusinessKeys(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("businessKeys") Collection<CacheRuleBusinessKey> businessKeys);

    /**
     * 批量新增或更新规则主表。
     *
     * @param rules 规则集合
     * @since v7.260701.0
     */
    void upsertBatch(@NonNull @Param("rules") Collection<CacheRule> rules);

    /**
     * 仅删除当前项目且已无任何键关联的规则。
     *
     * @param projectId 项目 ID
     * @param ruleIds   待检查的规则 ID 集合
     * @return 删除行数
     * @since v7.260701.0
     */
    int deleteOrphanByIds(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("ruleIds") Collection<Long> ruleIds);
}
