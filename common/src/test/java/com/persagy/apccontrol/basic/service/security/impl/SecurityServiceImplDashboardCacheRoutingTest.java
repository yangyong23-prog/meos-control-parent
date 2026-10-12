package com.persagy.apccontrol.basic.service.security.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.context.security.SecurityDashboardQueryContext;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.security.SecurityDashboardQueryParam;
import com.persagy.apccontrol.basic.model.vo.security.SecurityDashboardVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizComputeService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorSecurityStrategy;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.param.DemandInstanceConfigQueryParam;
import com.persagy.apccontrol.basic.structure.security.converter.SecurityConverterImpl;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 安防 Dashboard 静态缓存路由测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class SecurityServiceImplDashboardCacheRoutingTest {

    private DtpRwdClientService rwdClient;
    private RunningMonitorSecurityStrategy sourceStrategy;
    private CacheObjectModelStatusService statusService;
    private CacheObjectRepository objectRepository;
    private CacheObjectRelRepository relationRepository;
    private DemandInstanceGateway demandInstanceGateway;

    @BeforeEach
    public void setUp() {
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setTenant("group-1");
        ContextUtil.setProjectId("project-1");
        this.rwdClient = Mockito.mock(DtpRwdClientService.class);
        this.sourceStrategy = Mockito.mock(RunningMonitorSecurityStrategy.class);
        this.statusService = Mockito.mock(CacheObjectModelStatusService.class);
        this.objectRepository = Mockito.mock(CacheObjectRepository.class);
        this.relationRepository = Mockito.mock(CacheObjectRelRepository.class);
        this.demandInstanceGateway = Mockito.mock(DemandInstanceGateway.class);
    }

    @AfterEach
    public void tearDown() {
        ContextUtil.unloadContext();
    }

    /**
     * 验证 READY 后四类静态对象和两类关系全部来自缓存，并继续执行动态 IoT 阶段。
     *
     * @since v7.260802.0
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldUseCompleteStaticCacheSnapshotWhenReady() {
        SecurityDashboardQueryParam param = armingParam();
        SecurityDashboardQueryContext context = Mockito.mock(
                SecurityDashboardQueryContext.class);
        SecurityServiceImpl service = Mockito.spy(newService());
        Mockito.doReturn(context).when(service).newDashboardQueryContext(param);
        Mockito.when(this.statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);

        DtpRwdSpaceDTO space = new DtpRwdSpaceDTO();
        space.setId("space-1");
        space.setBuildingId("building-1");
        space.setFloorId("floor-1");
        SecurityDtpRwdEquipmentDTO controller = equipment(
                "controller-1", DtpRwdClassCodeEnum.SPIACU);
        SecurityDtpRwdEquipmentDTO camera = equipment(
                "camera-1", DtpRwdClassCodeEnum.SPVSCM);
        camera.setHasArmingFunc("1");
        DtpRwdBuildingDTO building = new DtpRwdBuildingDTO();
        building.setId("building-1");
        DtpRwdFloorDTO floor = new DtpRwdFloorDTO();
        floor.setId("floor-1");
        floor.setBuildingId("building-1");
        Mockito.when(this.objectRepository.listByCacheKey(
                        Mockito.anyString(), Mockito.any(), Mockito.anyCollection()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0);
                    if (CacheObjectModelKeyConfig.SECURITY_LIST_SPACE.equals(key)) {
                        return Collections.singletonList(space);
                    }
                    if (CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT.equals(key)) {
                        return Arrays.asList(controller, camera);
                    }
                    return Collections.emptyList();
                });
        Mockito.when(this.objectRepository.listByClassCode(
                        Mockito.eq(DtpRwdClassCodeEnum.BUILDING.value()),
                        Mockito.eq(DtpRwdBuildingDTO.class), Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(building));
        Mockito.when(this.objectRepository.listByClassCode(
                        Mockito.eq(DtpRwdClassCodeEnum.FLOOR.value()),
                        Mockito.eq(DtpRwdFloorDTO.class), Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(floor));
        Mockito.when(this.relationRepository.listForward(Mockito.any()))
                .thenAnswer(invocation -> {
                    CacheObjectRelQueryDaoParam query = invocation.getArgument(0);
                    String equipmentId = DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE
                            .value().getLeft().equals(query.getGraphCode())
                            ? "controller-1" : "camera-1";
                    CacheObjectRel relation = new CacheObjectRel();
                    relation.setObjectId(equipmentId);
                    relation.setRelObjectId("space-1");
                    return Collections.singletonList(relation);
                });
        Mockito.when(this.demandInstanceGateway.listInstanceIds(Mockito.any()))
                .thenReturn(Collections.emptySet());
        SecurityDashboardVO expected = new SecurityDashboardVO();
        Mockito.when(context.toDashboardVo()).thenReturn(expected);

        SecurityDashboardVO result = service.dashboard(param);

        Assertions.assertSame(expected, result);
        ArgumentCaptor<List> spacesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> equipmentsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map> serviceRelationsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> inRelationsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set> excludedCaptor = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<List> buildingsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> floorsCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(context).fillInstanceFromCache(
                spacesCaptor.capture(),
                equipmentsCaptor.capture(),
                serviceRelationsCaptor.capture(),
                inRelationsCaptor.capture(),
                excludedCaptor.capture(),
                buildingsCaptor.capture(),
                floorsCaptor.capture());
        Assertions.assertSame(space, spacesCaptor.getValue().get(0));
        Assertions.assertEquals(Arrays.asList(controller, camera), equipmentsCaptor.getValue());
        Assertions.assertEquals(Collections.singleton("space-1"),
                ((Map<String, Set<String>>) serviceRelationsCaptor.getValue())
                        .get("controller-1"));
        Assertions.assertEquals(Collections.singleton("space-1"),
                ((Map<String, Set<String>>) inRelationsCaptor.getValue())
                        .get("camera-1"));
        Assertions.assertTrue(excludedCaptor.getValue().isEmpty());
        Assertions.assertSame(building, buildingsCaptor.getValue().get(0));
        Assertions.assertSame(floor, floorsCaptor.getValue().get(0));
        Mockito.verify(context).fillIotData();
        Mockito.verify(this.statusService).isReady(RunningMonitorTypeEnum.SECURITY);
        Mockito.verifyNoInteractions(this.rwdClient, this.sourceStrategy);
    }

    /**
     * 验证未 READY 时完整保留旧实例填充路径，且不读取静态缓存。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldKeepLegacyDashboardPathWhenNotReady() {
        SecurityDashboardQueryParam param = armingParam();
        SecurityDashboardQueryContext context = Mockito.mock(
                SecurityDashboardQueryContext.class);
        SecurityServiceImpl service = Mockito.spy(newService());
        Mockito.doReturn(context).when(service).newDashboardQueryContext(param);
        Mockito.when(this.statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(false);
        SecurityDashboardVO expected = new SecurityDashboardVO();
        Mockito.when(context.toDashboardVo()).thenReturn(expected);

        Assertions.assertSame(expected, service.dashboard(param));

        Mockito.verify(context).fillInstance();
        Mockito.verify(context).fillIotData();
        Mockito.verifyNoInteractions(
                this.objectRepository,
                this.relationRepository,
                this.demandInstanceGateway);
    }

    /**
     * 验证 READY 空空间缓存是权威结果，不穿透旧实例查询。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldTreatReadyEmptySpaceCacheAsAuthoritative() {
        SecurityDashboardQueryParam param = armingParam();
        SecurityDashboardQueryContext context = Mockito.mock(
                SecurityDashboardQueryContext.class);
        SecurityServiceImpl service = Mockito.spy(newService());
        Mockito.doReturn(context).when(service).newDashboardQueryContext(param);
        Mockito.when(this.statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        Mockito.when(this.objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_SPACE),
                        Mockito.eq(DtpRwdSpaceDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());
        SecurityDashboardVO expected = new SecurityDashboardVO();
        Mockito.when(context.toDashboardVo()).thenReturn(expected);

        Assertions.assertSame(expected, service.dashboard(param));

        Mockito.verify(context).fillInstanceFromCache(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.emptyList());
        Mockito.verify(context, Mockito.never()).fillInstance();
        Mockito.verifyNoInteractions(
                this.rwdClient,
                this.sourceStrategy,
                this.relationRepository,
                this.demandInstanceGateway);
    }

    private SecurityServiceImpl newService() {
        return new SecurityServiceImpl(
                this.rwdClient,
                Mockito.mock(AbstractIotDataCacheServer.class),
                Mockito.mock(AbstractCommonServer.class),
                this.sourceStrategy,
                new SecurityConverterImpl(),
                Mockito.mock(BizDtpRwdService.class),
                Mockito.mock(InstanceService.class),
                Mockito.mock(BizComputeService.class),
                Mockito.mock(DemandConfigGateway.class),
                this.demandInstanceGateway,
                this.statusService,
                this.objectRepository,
                this.relationRepository,
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));
    }

    private static SecurityDashboardQueryParam armingParam() {
        SecurityDashboardQueryParam param = new SecurityDashboardQueryParam();
        param.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.SECURITY_ARMING);
        return param;
    }

    private static SecurityDtpRwdEquipmentDTO equipment(
            String id, DtpRwdClassCodeEnum classCode) {
        SecurityDtpRwdEquipmentDTO equipment = new SecurityDtpRwdEquipmentDTO();
        equipment.setId(id);
        equipment.setClassCode(classCode.value());
        return equipment;
    }
}
