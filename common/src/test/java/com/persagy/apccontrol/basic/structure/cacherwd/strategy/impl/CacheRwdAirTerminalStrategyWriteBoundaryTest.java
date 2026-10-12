package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalSpaceWithStatusVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * 末端完整构建的新对象模型写入边界测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheRwdAirTerminalStrategyWriteBoundaryTest {

    @AfterEach
    public void clearContext() {
        ContextUtil.remove();
    }

    /**
     * 验证末端调用方先查询并补齐关系 DTO，再提交对象和独立关系事务。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldQueryRelationsAndCommitNormalizedSnapshots() {
        ContextUtil.setProjectId("project-1");
        AirTerminalService airTerminalService = Mockito.mock(AirTerminalService.class);
        CacheObjectModelWriteService writeService =
                Mockito.mock(CacheObjectModelWriteService.class);
        DtpRwdClientService rwdClientService = Mockito.mock(DtpRwdClientService.class);
        CacheRwdAirTerminalStrategy strategy = new CacheRwdAirTerminalStrategy(
                airTerminalService, writeService, rwdClientService);

        DtpRwdSpaceDTO space = new DtpRwdSpaceDTO();
        space.setId("space-source");
        DtpRwdEquipmentDTO equipment = new DtpRwdEquipmentDTO();
        equipment.setId("equipment-source");
        equipment.setClassCode(DtpRwdClassCodeEnum.ACATAH.value());
        DtpRwdEquipmentDTO group = new DtpRwdEquipmentDTO();
        group.setId("group-source");
        group.setClassCode(DtpRwdClassCodeEnum.ACAHG_GROUP.value());
        AirTerminalSpaceWithStatusVO source = new AirTerminalSpaceWithStatusVO();
        source.setRwdSpaces(Collections.singletonList(space));
        source.setRwdEquipments(Arrays.asList(equipment, group));
        Mockito.when(airTerminalService.listFullSpaceEquipmentForCacheBuild(
                        Mockito.any()))
                .thenReturn(source);
        Mockito.when(rwdClientService.listObject(
                        Mockito.any(DtpRwdObjectQueryParam.class)))
                .thenAnswer(invocation -> {
                    DtpRwdObjectQueryParam query = invocation.getArgument(0);
                    if (query.getRelationFrom().getRelationType()
                            != DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE) {
                        return Collections.emptyList();
                    }
                    DtpRwdObjectDTO relation = new DtpRwdObjectDTO();
                    relation.setId("equipment-source");
                    relation.setRelObjIds(new LinkedHashSet<>(Arrays.asList(
                            "space-source", "outside-request-scope")));
                    return Collections.singletonList(relation);
                });
        Mockito.when(writeService.replaceModelCacheKeysAndRelations(
                        Mockito.anyMap(), Mockito.anyCollection(), Mockito.anyMap(),
                        Mockito.any(java.util.function.BooleanSupplier.class)))
                .thenReturn(true);

        boolean result = strategy.buildCacheData(RunningMonitorTypeEnum.AIR_TERMINAL);

        Assertions.assertTrue(result);
        Mockito.verify(rwdClientService, Mockito.times(3)).listObject(
                Mockito.any(DtpRwdObjectQueryParam.class));

        Map<String, Collection<? extends DtpRwdObjectDTO>> expectedSourcesByKey =
                new LinkedHashMap<>();
        expectedSourcesByKey.put(
                CacheObjectModelKeyConfig.AIR_TERMINAL_LIST_SPACE,
                Collections.singletonList(space));
        expectedSourcesByKey.put(
                CacheObjectModelKeyConfig.AIR_TERMINAL_LIST_EQUIPMENT,
                Arrays.asList(equipment, group));

        Map<String, Set<String>> serviceSpaceRelations = new LinkedHashMap<>();
        serviceSpaceRelations.put(
                "equipment-source", Collections.singleton("space-source"));
        serviceSpaceRelations.put("group-source", Collections.emptySet());
        Map<String, Set<String>> emptyEquipmentRelations = new LinkedHashMap<>();
        emptyEquipmentRelations.put("equipment-source", Collections.emptySet());
        emptyEquipmentRelations.put("group-source", Collections.emptySet());
        Map<String, Set<String>> emptyGroupRelations = new LinkedHashMap<>();
        emptyGroupRelations.put("group-source", Collections.emptySet());

        Mockito.verify(writeService).replaceModelCacheKeysAndRelations(
                Mockito.argThat(map ->
                        CacheRwdAirTerminalStrategyWriteBoundaryTest
                                .sourcesByKeyMatches(map, expectedSourcesByKey)),
                Mockito.eq(Collections.emptyList()),
                Mockito.argThat(relations ->
                        CacheRwdAirTerminalStrategyWriteBoundaryTest
                                .relationsByTypeMatches(relations,
                                        serviceSpaceRelations,
                                        emptyEquipmentRelations,
                                        emptyGroupRelations)),
                Mockito.any(java.util.function.BooleanSupplier.class));
    }

    private static boolean sourcesByKeyMatches(
            Map<String, ? extends Collection<? extends DtpRwdObjectDTO>> actual,
            Map<String, ? extends Collection<? extends DtpRwdObjectDTO>> expected) {
        if (actual.size() != expected.size()) {
            return false;
        }
        for (Map.Entry<String, ? extends Collection<? extends DtpRwdObjectDTO>> entry
                : expected.entrySet()) {
            Collection<? extends DtpRwdObjectDTO> actualObjects = actual.get(entry.getKey());
            if (actualObjects == null || actualObjects.size() != entry.getValue().size()) {
                return false;
            }
        }
        return true;
    }

    private static boolean relationsByTypeMatches(
            Map<DtpRwdRelationTypeEnum, ? extends Collection<? extends DtpRwdObjectDTO>> actual,
            Map<String, Set<String>> expectedServiceSpace,
            Map<String, Set<String>> expectedInSpace,
            Map<String, Set<String>> expectedInGroup) {
        Map<String, Set<String>> actualServiceSpace = mapRelations(
                actual.get(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE));
        if (!actualServiceSpace.equals(expectedServiceSpace)) {
            return false;
        }
        Map<String, Set<String>> actualInSpace = mapRelations(
                actual.get(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE));
        if (!actualInSpace.equals(expectedInSpace)) {
            return false;
        }
        Map<String, Set<String>> actualInGroup = mapRelations(
                actual.get(DtpRwdRelationTypeEnum.EQUIP_IN_GROUP));
        return actualInGroup.equals(expectedInGroup);
    }

    /**
     * 把关系 DTO 转为稳定断言结构。
     *
     * @param relations 关系 DTO
     * @return 主体到目标集合的映射
     */
    private static Map<String, Set<String>> mapRelations(
            Collection<? extends DtpRwdObjectDTO> relations) {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (DtpRwdObjectDTO relation : relations) {
            result.put(
                    relation.getId(),
                    relation.getRelObjIds() == null
                            ? Collections.emptySet()
                            : relation.getRelObjIds());
        }
        return result;
    }
}
