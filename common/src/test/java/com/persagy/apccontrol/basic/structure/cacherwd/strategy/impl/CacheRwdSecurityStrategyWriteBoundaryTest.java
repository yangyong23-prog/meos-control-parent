package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheWriteAction;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 安防完整构建的新对象模型写入边界测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class CacheRwdSecurityStrategyWriteBoundaryTest {

    /**
     * 安防固定十二类设备。
     *
     * @since v7.260802.0
     */
    private static final List<DtpRwdClassCodeEnum> SECURITY_CLASS_CODES = Arrays.asList(
            DtpRwdClassCodeEnum.CFCSDR,
            DtpRwdClassCodeEnum.CFCSFR,
            DtpRwdClassCodeEnum.SPEGFG,
            DtpRwdClassCodeEnum.SPVSCM,
            DtpRwdClassCodeEnum.WEGMCM,
            DtpRwdClassCodeEnum.SPIACU,
            DtpRwdClassCodeEnum.SPIAGD,
            DtpRwdClassCodeEnum.SPIAID,
            DtpRwdClassCodeEnum.SPIAMA,
            DtpRwdClassCodeEnum.SPEGCU,
            DtpRwdClassCodeEnum.SPVSVE,
            DtpRwdClassCodeEnum.SPVSDR);

    /**
     * 清理项目上下文。
     *
     * @since v7.260802.0
     */
    @AfterEach
    public void tearDown() {
        ContextUtil.unloadContext();
    }

    /**
     * 验证十二类设备、空间和两类关系只各准备一次，并通过单个多 Key 原子入口提交。
     *
     * @since v7.260802.0
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldQueryCompleteSecurityScopeAndCommitThroughOneAtomicEntry() {
        ContextUtil.setProjectId("project-1");
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        CacheObjectModelWriteService writeService =
                Mockito.mock(CacheObjectModelWriteService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheRwdSecurityStrategy strategy = new CacheRwdSecurityStrategy(
                rwdClient, writeService, objectRepository);

        DtpRwdSpaceDTO space = space("space-1");
        List<DtpRwdSpaceDTO> spaces = Collections.singletonList(space);
        List<SecurityDtpRwdEquipmentDTO> equipments = Arrays.asList(
                equipment("door-current", DtpRwdClassCodeEnum.CFCSDR),
                equipment("camera-current", DtpRwdClassCodeEnum.SPVSCM),
                equipment("garage-camera-current", DtpRwdClassCodeEnum.WEGMCM),
                equipment("controller-current", DtpRwdClassCodeEnum.SPIACU));
        List<SecurityDtpRwdEquipmentDTO> previousEquipments = Arrays.asList(
                equipment("door-old", DtpRwdClassCodeEnum.CFCSFR),
                equipment("controller-old", DtpRwdClassCodeEnum.SPIACU));
        Mockito.when(rwdClient.listSpace(Mockito.any())).thenReturn(spaces);
        Mockito.when(rwdClient.listSecurityEquipment(
                        Mockito.eq(false), Mockito.eq("project-1"), Mockito.any()))
                .thenReturn(equipments);
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(previousEquipments);
        Mockito.when(rwdClient.listObject(Mockito.any())).thenAnswer(invocation -> {
            DtpRwdObjectQueryParam param = invocation.getArgument(0);
            List<DtpRwdObjectDTO> result = new ArrayList<>();
            param.getId().forEach(subjectId -> result.add(relationObject(
                    subjectId, "space-1", "space-1", "space-outside")));
            DtpRwdObjectDTO unexpectedSubject = relationObject(
                    "unexpected-subject", "space-1");
            result.add(unexpectedSubject);
            return result;
        });
        CacheWriteAction serviceAction = Mockito.mock(CacheWriteAction.class);
        CacheWriteAction inSpaceAction = Mockito.mock(CacheWriteAction.class);
        Mockito.when(writeService.prepareObjectRelationsReplaceAction(
                        Mockito.eq(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE),
                        Mockito.anyCollection()))
                .thenReturn(serviceAction);
        Mockito.when(writeService.prepareObjectRelationsReplaceAction(
                        Mockito.eq(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE),
                        Mockito.anyCollection()))
                .thenReturn(inSpaceAction);
        Mockito.when(writeService.replaceModelCacheKeys(
                        Mockito.anyMap(), Mockito.anyCollection()))
                .thenReturn(true);

        boolean result = strategy.buildCacheData(RunningMonitorTypeEnum.SECURITY);

        Assertions.assertTrue(result);
        ArgumentCaptor<DtpRwdObjectQueryParam> spaceQueryCaptor =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(rwdClient).listSpace(spaceQueryCaptor.capture());
        Assertions.assertEquals(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(
                        DtpRwdClassCodeEnum.SPACE.value()),
                new LinkedHashSet<>(spaceQueryCaptor.getValue().getWithColumns()));
        Assertions.assertEquals(Boolean.TRUE, spaceQueryCaptor.getValue().getValid());

        ArgumentCaptor<DtpRwdObjectQueryParam> equipmentQueryCaptor =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(rwdClient).listSecurityEquipment(
                Mockito.eq(false), Mockito.eq("project-1"),
                equipmentQueryCaptor.capture());
        Set<String> expectedClassCodes = new LinkedHashSet<>();
        Set<String> expectedFields = new LinkedHashSet<>();
        for (DtpRwdClassCodeEnum classCode : SECURITY_CLASS_CODES) {
            expectedClassCodes.add(classCode.value());
            expectedFields.addAll(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                    classCode.value()));
        }
        Assertions.assertEquals(
                expectedClassCodes,
                new LinkedHashSet<>(equipmentQueryCaptor.getValue().getClassCode()));
        Assertions.assertEquals(
                expectedFields,
                new LinkedHashSet<>(equipmentQueryCaptor.getValue().getWithColumns()));
        Assertions.assertEquals(Boolean.TRUE, equipmentQueryCaptor.getValue().getValid());

        ArgumentCaptor<DtpRwdObjectQueryParam> relationQueryCaptor =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(rwdClient, Mockito.times(2)).listObject(
                relationQueryCaptor.capture());
        Map<DtpRwdRelationTypeEnum, DtpRwdObjectQueryParam> queryByType =
                new LinkedHashMap<>();
        for (DtpRwdObjectQueryParam param : relationQueryCaptor.getAllValues()) {
            queryByType.put(param.getRelationFrom().getRelationType(), param);
        }
        Assertions.assertEquals(
                Collections.singleton("controller-current"),
                new LinkedHashSet<>(queryByType.get(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).getId()));
        Assertions.assertEquals(
                new LinkedHashSet<>(Arrays.asList(
                        "door-current", "camera-current", "garage-camera-current")),
                new LinkedHashSet<>(queryByType.get(
                        DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).getId()));
        queryByType.values().forEach(param -> Assertions.assertEquals(
                Collections.singleton("space-1"),
                new LinkedHashSet<>(param.getRelationFrom().getObjTo())));

        ArgumentCaptor<Collection> serviceRelationsCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(writeService).prepareObjectRelationsReplaceAction(
                Mockito.eq(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE),
                serviceRelationsCaptor.capture());
        Map<String, DtpRwdObjectDTO> serviceRelations = mapById(
                (Collection<DtpRwdObjectDTO>) serviceRelationsCaptor.getValue());
        Assertions.assertEquals(
                Collections.singleton("space-1"),
                serviceRelations.get("controller-current").getRelObjIds());
        Assertions.assertTrue(serviceRelations.get("controller-old").getRelObjIds().isEmpty());

        ArgumentCaptor<Collection> inSpaceRelationsCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(writeService).prepareObjectRelationsReplaceAction(
                Mockito.eq(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE),
                inSpaceRelationsCaptor.capture());
        Map<String, DtpRwdObjectDTO> inSpaceRelations = mapById(
                (Collection<DtpRwdObjectDTO>) inSpaceRelationsCaptor.getValue());
        Assertions.assertEquals(
                new LinkedHashSet<>(Arrays.asList(
                        "door-current", "camera-current", "garage-camera-current", "door-old")),
                inSpaceRelations.keySet());
        Assertions.assertTrue(inSpaceRelations.get("door-old").getRelObjIds().isEmpty());
        Assertions.assertEquals(
                Collections.singleton("space-1"),
                inSpaceRelations.get("camera-current").getRelObjIds());

        ArgumentCaptor<Map> sourceByKeyCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Collection> actionsCaptor = ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(writeService).replaceModelCacheKeys(
                sourceByKeyCaptor.capture(), actionsCaptor.capture());
        Map<String, Collection<? extends DtpRwdObjectDTO>> sourceByKey =
                sourceByKeyCaptor.getValue();
        Assertions.assertEquals(Arrays.asList(
                CacheObjectModelKeyConfig.SECURITY_LIST_SPACE,
                CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                new ArrayList<>(sourceByKey.keySet()));
        Assertions.assertSame(spaces, sourceByKey.get(
                CacheObjectModelKeyConfig.SECURITY_LIST_SPACE));
        Assertions.assertSame(equipments, sourceByKey.get(
                CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT));
        Assertions.assertEquals(
                Arrays.asList(serviceAction, inSpaceAction),
                new ArrayList<>(actionsCaptor.getValue()));
        Mockito.verifyNoMoreInteractions(writeService);
    }

    /**
     * 验证没有空间目标时不发起宽范围关系查询，仍提交旧主体空快照进行清理。
     *
     * @since v7.260802.0
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldSkipRelationQueriesWhenNoSpaceTargetsRemain() {
        ContextUtil.setProjectId("project-1");
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        CacheObjectModelWriteService writeService =
                Mockito.mock(CacheObjectModelWriteService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheRwdSecurityStrategy strategy = new CacheRwdSecurityStrategy(
                rwdClient, writeService, objectRepository);
        Mockito.when(rwdClient.listSpace(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(rwdClient.listSecurityEquipment(
                        Mockito.eq(false), Mockito.eq("project-1"), Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(Arrays.asList(
                        equipment("door-old", DtpRwdClassCodeEnum.CFCSDR),
                        equipment("controller-old", DtpRwdClassCodeEnum.SPIACU)));
        Mockito.when(writeService.prepareObjectRelationsReplaceAction(
                        Mockito.any(), Mockito.anyCollection()))
                .thenReturn(Mockito.mock(CacheWriteAction.class));
        Mockito.when(writeService.replaceModelCacheKeys(
                        Mockito.anyMap(), Mockito.anyCollection()))
                .thenReturn(true);

        Assertions.assertTrue(strategy.buildCacheData(RunningMonitorTypeEnum.SECURITY));

        Mockito.verify(rwdClient, Mockito.never()).listObject(Mockito.any());
        ArgumentCaptor<Collection> relationCaptor = ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(writeService, Mockito.times(2)).prepareObjectRelationsReplaceAction(
                Mockito.any(), relationCaptor.capture());
        Set<String> relationSubjectIds = new LinkedHashSet<>();
        for (Collection<DtpRwdObjectDTO> relations : relationCaptor.getAllValues()) {
            relations.forEach(relation -> {
                relationSubjectIds.add(relation.getId());
                Assertions.assertTrue(relation.getRelObjIds().isEmpty());
            });
        }
        Assertions.assertEquals(
                new LinkedHashSet<>(Arrays.asList("controller-old", "door-old")),
                relationSubjectIds);
    }

    /**
     * 创建安防设备测试对象。
     *
     * @param id 对象 ID
     * @param classCode 设备类
     * @return 安防设备
     * @since v7.260802.0
     */
    private static SecurityDtpRwdEquipmentDTO equipment(
            String id, DtpRwdClassCodeEnum classCode) {
        SecurityDtpRwdEquipmentDTO result = new SecurityDtpRwdEquipmentDTO();
        result.setId(id);
        result.setClassCode(classCode.value());
        return result;
    }

    /**
     * 创建空间测试对象。
     *
     * @param id 对象 ID
     * @return 空间
     * @since v7.260802.0
     */
    private static DtpRwdSpaceDTO space(String id) {
        DtpRwdSpaceDTO result = new DtpRwdSpaceDTO();
        result.setId(id);
        result.setClassCode(DtpRwdClassCodeEnum.SPACE.value());
        return result;
    }

    /**
     * 创建关系返回对象。
     *
     * @param id 关系主体 ID
     * @param targetIds 关系目标 ID
     * @return 关系对象
     * @since v7.260802.0
     */
    private static DtpRwdObjectDTO relationObject(String id, String... targetIds) {
        DtpRwdObjectDTO result = new DtpRwdObjectDTO();
        result.setId(id);
        result.setRelObjIds(new LinkedHashSet<>(Arrays.asList(targetIds)));
        return result;
    }

    /**
     * 按关系主体 ID 组装结果。
     *
     * @param relations 关系对象
     * @return 主体 ID 到关系对象
     * @since v7.260802.0
     */
    private static Map<String, DtpRwdObjectDTO> mapById(
            Collection<DtpRwdObjectDTO> relations) {
        Map<String, DtpRwdObjectDTO> result = new LinkedHashMap<>();
        relations.forEach(relation -> result.put(relation.getId(), relation));
        return result;
    }
}
