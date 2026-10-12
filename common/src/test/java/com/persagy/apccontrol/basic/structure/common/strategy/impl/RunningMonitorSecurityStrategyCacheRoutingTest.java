package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 安防通行空间静态缓存路由与规则聚合测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class RunningMonitorSecurityStrategyCacheRoutingTest {

    /**
     * 验证不同门分别提供两类规则时按正向关系聚合并去重，且保持空间缓存顺序。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldAggregateAccessRulesFromDifferentDoorsAndKeepSpaceCacheOrder() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        AbstractIotDataCacheServer<?> iotData = Mockito.mock(AbstractIotDataCacheServer.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        RunningMonitorSecurityStrategy strategy = new RunningMonitorSecurityStrategy(
                rwdClient, iotData, statusService, objectRepository, relationRepository,
                modelViewRepository, modelViewConverter);
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY)).thenReturn(true);

        SecurityDtpRwdEquipmentDTO accessDoor = equipment("door-access");
        SecurityDtpRwdEquipmentDTO setDoor = equipment("door-set");
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class), Mockito.anyCollection()))
                .thenReturn(Arrays.asList(accessDoor, setDoor));

        Map<String, java.util.Set<String>> ruleMap = new LinkedHashMap<>();
        ruleMap.put("door-access", Collections.singleton(
                InfoCodeEnum.ACCESS_DEMAND.getCode()));
        ruleMap.put("door-set", Collections.singleton(
                InfoCodeEnum.EQUIP_ON_SET.getCode()));
        Mockito.when(iotData.mapObjectIdAndExistsRuleInfoCodes(
                        Mockito.anyMap(), Mockito.anyCollection()))
                .thenReturn(ruleMap);

        CacheObjectRel first = relation("door-access", "space-1");
        CacheObjectRel duplicate = relation("door-access", "space-1");
        CacheObjectRel second = relation("door-set", "space-1");
        CacheObjectRel unrelated = relation("door-access", "space-2");
        Mockito.when(relationRepository.listForward(Mockito.any(CacheObjectRelQueryDaoParam.class)))
                .thenReturn(Arrays.asList(first, duplicate, second, unrelated));

        DtpRwdSpaceDTO spaceTwo = space("space-2", "Space 2");
        DtpRwdSpaceDTO spaceOne = space("space-1", "Space 1");
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_SPACE),
                        Mockito.eq(DtpRwdSpaceDTO.class), Mockito.anyCollection()))
                .thenReturn(Arrays.asList(spaceTwo, spaceOne));
        CacheObjectModelView modelView = new CacheObjectModelView();
        modelView.setId("space-1");
        DtpRwdSpaceDTO modelFields = space("space-1", "Space 1");
        modelFields.setBimId("bim-1");
        modelFields.setBimLocation("location-1");
        modelFields.setOutline("outline-1");
        Mockito.when(modelViewRepository.listModelViews(Mockito.any()))
                .thenReturn(Collections.singletonList(modelView));
        Mockito.when(modelViewConverter.toRwdObject(
                        Mockito.eq(modelView), Mockito.eq(DtpRwdSpaceDTO.class)))
                .thenReturn(modelFields);

        List<DtpRwdSpaceDTO> result = strategy.listAccessSpace();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("space-1", result.get(0).getId());
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList("door-access", "door-set")),
                result.get(0).getRelObjIds());
        Assertions.assertEquals("bim-1", result.get(0).getBimId());
        Assertions.assertEquals("location-1", result.get(0).getBimLocation());
        Assertions.assertEquals("outline-1", result.get(0).getOutline());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> ruleFieldsCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(iotData).mapObjectIdAndExistsRuleInfoCodes(
                Mockito.anyMap(), ruleFieldsCaptor.capture());
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList(
                        InfoCodeEnum.ACCESS_DEMAND.getCode(),
                        InfoCodeEnum.EQUIP_ON_SET.getCode())),
                new LinkedHashSet<>(ruleFieldsCaptor.getValue()));
        ArgumentCaptor<CacheObjectRelQueryDaoParam> relationCaptor =
                ArgumentCaptor.forClass(CacheObjectRelQueryDaoParam.class);
        Mockito.verify(relationRepository).listForward(relationCaptor.capture());
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList("door-access", "door-set")),
                new LinkedHashSet<>(relationCaptor.getValue().getObjectIds()));
        Assertions.assertEquals(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE.value().getLeft(),
                relationCaptor.getValue().getGraphCode());
        Assertions.assertEquals(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE.value().getRight(),
                relationCaptor.getValue().getRelCode());
        Mockito.verify(objectRepository).listByCacheKey(
                Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_SPACE),
                Mockito.eq(DtpRwdSpaceDTO.class), Mockito.anyCollection());
        Mockito.verify(objectRepository, Mockito.never()).listByObjectIds(
                Mockito.anyCollection(), Mockito.eq(DtpRwdSpaceDTO.class),
                Mockito.anyCollection());
        Mockito.verifyNoInteractions(rwdClient);
    }

    /**
     * 验证 READY 下空静态候选是权威空结果，不回查 RWD 或动态规则。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldTreatEmptyReadyEquipmentCacheAsAuthoritative() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        AbstractIotDataCacheServer<?> iotData = Mockito.mock(AbstractIotDataCacheServer.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        RunningMonitorSecurityStrategy strategy = new RunningMonitorSecurityStrategy(
                rwdClient, iotData, statusService, objectRepository, relationRepository,
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY)).thenReturn(true);
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class), Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());

        Assertions.assertTrue(strategy.listAccessSpace().isEmpty());
        Mockito.verifyNoInteractions(rwdClient, iotData, relationRepository);
        Mockito.verify(objectRepository, Mockito.never()).listByCacheKey(
                Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_SPACE),
                Mockito.eq(DtpRwdSpaceDTO.class), Mockito.anyCollection());
    }

    /**
     * 验证缺少任一动态规则时直接返回空结果，不读取关系和空间缓存。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldReturnEmptyWhenEitherDynamicRuleIsMissing() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        AbstractIotDataCacheServer<?> iotData = Mockito.mock(AbstractIotDataCacheServer.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        RunningMonitorSecurityStrategy strategy = new RunningMonitorSecurityStrategy(
                rwdClient, iotData, statusService, objectRepository, relationRepository,
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY)).thenReturn(true);
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class), Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(equipment("door-1")));
        Mockito.when(iotData.mapObjectIdAndExistsRuleInfoCodes(
                        Mockito.anyMap(), Mockito.anyCollection()))
                .thenReturn(Collections.singletonMap(
                        "door-1", Collections.singleton(
                                InfoCodeEnum.ACCESS_DEMAND.getCode())));

        Assertions.assertTrue(strategy.listAccessSpace().isEmpty());
        Mockito.verifyNoInteractions(rwdClient, relationRepository);
        Mockito.verify(objectRepository, Mockito.never()).listByCacheKey(
                Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_SPACE),
                Mockito.eq(DtpRwdSpaceDTO.class), Mockito.anyCollection());
    }

    /**
     * 验证门具有两类规则但没有空间关系时直接返回空结果。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldReturnEmptyWhenDoorRelationsAreMissing() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        AbstractIotDataCacheServer<?> iotData = Mockito.mock(AbstractIotDataCacheServer.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        RunningMonitorSecurityStrategy strategy = new RunningMonitorSecurityStrategy(
                rwdClient, iotData, statusService, objectRepository, relationRepository,
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY)).thenReturn(true);
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class), Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(equipment("door-1")));
        Mockito.when(iotData.mapObjectIdAndExistsRuleInfoCodes(
                        Mockito.anyMap(), Mockito.anyCollection()))
                .thenReturn(Collections.singletonMap("door-1", new LinkedHashSet<>(Arrays.asList(
                        InfoCodeEnum.ACCESS_DEMAND.getCode(),
                        InfoCodeEnum.EQUIP_ON_SET.getCode()))));
        Mockito.when(relationRepository.listForward(Mockito.any(CacheObjectRelQueryDaoParam.class)))
                .thenReturn(Collections.emptyList());

        Assertions.assertTrue(strategy.listAccessSpace().isEmpty());
        Mockito.verifyNoInteractions(rwdClient);
        Mockito.verify(objectRepository, Mockito.never()).listByCacheKey(
                Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_SPACE),
                Mockito.eq(DtpRwdSpaceDTO.class), Mockito.anyCollection());
    }

    /**
     * 验证匹配关系存在但空间 Key 为空时仍是权威空结果，不回查 RWD。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldTreatReadyEmptySpaceCacheAsAuthoritative() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        AbstractIotDataCacheServer<?> iotData = Mockito.mock(AbstractIotDataCacheServer.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        RunningMonitorSecurityStrategy strategy = new RunningMonitorSecurityStrategy(
                rwdClient, iotData, statusService, objectRepository, relationRepository,
                modelViewRepository, Mockito.mock(CacheObjectModelViewRecordConverter.class));
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY)).thenReturn(true);
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class), Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(equipment("door-1")));
        Mockito.when(iotData.mapObjectIdAndExistsRuleInfoCodes(
                        Mockito.anyMap(), Mockito.anyCollection()))
                .thenReturn(Collections.singletonMap("door-1", new LinkedHashSet<>(Arrays.asList(
                        InfoCodeEnum.ACCESS_DEMAND.getCode(),
                        InfoCodeEnum.EQUIP_ON_SET.getCode()))));
        Mockito.when(relationRepository.listForward(Mockito.any(CacheObjectRelQueryDaoParam.class)))
                .thenReturn(Collections.singletonList(relation("door-1", "space-1")));
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_SPACE),
                        Mockito.eq(DtpRwdSpaceDTO.class), Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());

        Assertions.assertTrue(strategy.listAccessSpace().isEmpty());
        Mockito.verifyNoInteractions(rwdClient, modelViewRepository);
    }

    /**
     * 验证缓存仓储异常直接传播，不伪装为空结果或回退 RWD。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldPropagateReadyCacheFailure() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        AbstractIotDataCacheServer<?> iotData = Mockito.mock(AbstractIotDataCacheServer.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        IllegalStateException failure = new IllegalStateException("cache unavailable");
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY)).thenReturn(true);
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class), Mockito.anyCollection()))
                .thenThrow(failure);
        RunningMonitorSecurityStrategy strategy = new RunningMonitorSecurityStrategy(
                rwdClient, iotData, statusService, objectRepository,
                Mockito.mock(CacheObjectRelRepository.class),
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));

        IllegalStateException actual = Assertions.assertThrows(
                IllegalStateException.class, () -> strategy.listAccessSpace());

        Assertions.assertSame(failure, actual);
        Mockito.verifyNoInteractions(rwdClient, iotData);
    }

    /**
     * 验证动态规则查询异常直接传播，不被转换为空结果。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldPropagateDynamicRuleFailure() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        AbstractIotDataCacheServer<?> iotData = Mockito.mock(AbstractIotDataCacheServer.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        SecurityDtpRwdEquipmentDTO door = equipment("door-1");
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY)).thenReturn(true);
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class), Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(door));
        IllegalStateException failure = new IllegalStateException("rule service unavailable");
        Mockito.when(iotData.mapObjectIdAndExistsRuleInfoCodes(
                        Mockito.anyMap(), Mockito.anyCollection()))
                .thenThrow(failure);
        RunningMonitorSecurityStrategy strategy = new RunningMonitorSecurityStrategy(
                rwdClient, iotData, statusService, objectRepository,
                Mockito.mock(CacheObjectRelRepository.class),
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));

        IllegalStateException actual = Assertions.assertThrows(
                IllegalStateException.class, () -> strategy.listAccessSpace());

        Assertions.assertSame(failure, actual);
        Mockito.verifyNoInteractions(rwdClient);
    }

    /**
     * 创建满足门禁监控状态过滤的缓存设备。
     *
     * @param id 设备 ID
     * @return 缓存设备
     * @since v7.260802.0
     */
    private static SecurityDtpRwdEquipmentDTO equipment(String id) {
        SecurityDtpRwdEquipmentDTO result = new SecurityDtpRwdEquipmentDTO();
        result.setId(id);
        result.setClassCode(DtpRwdClassCodeEnum.CFCSDR.value());
        result.setUndefinedProperties(Collections.singletonMap(
                InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(), "2"));
        return result;
    }

    /**
     * 创建缓存空间。
     *
     * @param id 空间 ID
     * @param localName 本地名称
     * @return 缓存空间
     * @since v7.260802.0
     */
    private static DtpRwdSpaceDTO space(String id, String localName) {
        DtpRwdSpaceDTO result = new DtpRwdSpaceDTO();
        result.setId(id);
        result.setLocalName(localName);
        result.setClassCode(DtpRwdClassCodeEnum.SPACE.value());
        return result;
    }

    /**
     * 创建设备到空间的正向缓存关系。
     *
     * @param objectId 设备 ID
     * @param relObjectId 空间 ID
     * @return 正向关系
     * @since v7.260802.0
     */
    private static CacheObjectRel relation(String objectId, String relObjectId) {
        CacheObjectRel result = new CacheObjectRel();
        result.setObjectId(objectId);
        result.setRelObjectId(relObjectId);
        return result;
    }
}
