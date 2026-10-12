package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.entity.cache.CacheRule;
import com.persagy.apccontrol.basic.resource.cache.CacheRuleKeyRelService;
import com.persagy.apccontrol.basic.resource.cache.CacheRuleService;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存规则主表读取仓储测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheRuleRepositoryImplTest {

    /**
     * 验证业务读取直接按对象、信息点和规则类型查询主表。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReadRulesByObjectIdsWithoutCacheKey() {
        CacheRuleService ruleService = Mockito.mock(CacheRuleService.class);
        CacheRuleKeyRelService relationService = Mockito.mock(CacheRuleKeyRelService.class);
        CacheRuleRepositoryImpl repository = new CacheRuleRepositoryImpl(
                ruleService,
                relationService,
                Mockito.mock(RedissonDistributedLockerService.class));
        List<String> objectIds = Collections.singletonList("circuit-1");
        List<String> infoCodes = Collections.singletonList("OnOff");
        CacheRule rule = new CacheRule();
        rule.setId(101L);
        rule.setObjectId("circuit-1");
        rule.setInfoCode("OnOff");
        rule.setType(CacheIotRuleTypeEnum.PERSISTENCE);
        Mockito.when(ruleService.listByObjectIds(
                        objectIds, infoCodes, CacheIotRuleTypeEnum.PERSISTENCE))
                .thenReturn(Collections.singletonList(rule));

        Map<String, Set<String>> result = repository.mapInfoCodesByObjectIds(
                objectIds, infoCodes, CacheIotRuleTypeEnum.PERSISTENCE);

        Assertions.assertEquals(Collections.singleton("OnOff"), result.get("circuit-1"));
        Mockito.verify(ruleService).listByObjectIds(
                objectIds, infoCodes, CacheIotRuleTypeEnum.PERSISTENCE);
        Mockito.verifyNoInteractions(relationService);
    }

    /**
     * 验证主表未命中规则时返回空映射且不读取规则归属关系。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReturnEmptyMapWhenRuleTableHasNoMatch() {
        CacheRuleService ruleService = Mockito.mock(CacheRuleService.class);
        CacheRuleKeyRelService relationService = Mockito.mock(CacheRuleKeyRelService.class);
        CacheRuleRepositoryImpl repository = new CacheRuleRepositoryImpl(
                ruleService,
                relationService,
                Mockito.mock(RedissonDistributedLockerService.class));
        List<String> objectIds = Collections.singletonList("space-1");
        List<String> infoCodes = Collections.singletonList("tdb");
        Mockito.when(ruleService.listByObjectIds(
                        objectIds, infoCodes, CacheIotRuleTypeEnum.PERSISTENCE))
                .thenReturn(Collections.emptyList());

        Map<String, Set<String>> result = repository.mapInfoCodesByObjectIds(
                objectIds, infoCodes, CacheIotRuleTypeEnum.PERSISTENCE);

        Assertions.assertTrue(result.isEmpty());
        Mockito.verifyNoInteractions(relationService);
    }
}
