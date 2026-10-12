package com.persagy.apccontrol.basic.service.security.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.security.SecurityEquipmentStatusVO;
import com.persagy.apccontrol.basic.model.vo.security.SecurityFunctionVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.BizComputeService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorSecurityStrategy;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.security.converter.SecurityConverterImpl;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 安防基础静态接口 READY 路由纯单元测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class SecurityServiceImplCacheRoutingTest {

    /**
     * 初始化项目上下文。
     *
     * @since v7.260802.0
     */
    @BeforeEach
    public void setUpContext() {
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setTenant("group-1");
        ContextUtil.setProjectId("project-1");
    }

    /**
     * 清理项目上下文。
     *
     * @since v7.260802.0
     */
    @AfterEach
    public void tearDownContext() {
        ContextUtil.unloadContext();
    }

    /**
     * 验证 READY 设备列表只从对象和模型视图缓存读取静态数据，同时保留实时孪生可控查询。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldReadEquipmentStaticDataFromCacheAndKeepTwinControlLive() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        RunningMonitorSecurityStrategy sourceStrategy =
                Mockito.mock(RunningMonitorSecurityStrategy.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        SecurityServiceImpl service = newService(
                rwdClient, sourceStrategy, statusService,
                objectRepository, relationRepository,
                modelViewRepository, modelViewConverter);
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        SecurityDtpRwdEquipmentDTO cachedEquipment =
                equipment("camera-1", DtpRwdClassCodeEnum.WEGMCM);
        cachedEquipment.setLocalName("Local Camera Name");
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(cachedEquipment));
        Mockito.when(relationRepository.listForward(Mockito.any(
                        CacheObjectRelQueryDaoParam.class)))
                .thenReturn(Collections.emptyList());
        CacheObjectModelView modelView = new CacheObjectModelView();
        modelView.setId("camera-1");
        Mockito.when(modelViewRepository.listModelViews(Mockito.any(
                        CacheObjectModelViewQueryDaoParam.class)))
                .thenReturn(Collections.singletonList(modelView));
        SecurityDtpRwdEquipmentDTO modelFields =
                equipment("camera-1", DtpRwdClassCodeEnum.WEGMCM);
        modelFields.setBimId("bim-1");
        modelFields.setBimLocation("location-1");
        Mockito.when(modelViewConverter.toRwdObject(
                        modelView, SecurityDtpRwdEquipmentDTO.class))
                .thenReturn(modelFields);
        Mockito.when(rwdClient.listTwinControlObjects(
                        Mockito.eq(false), Mockito.eq("project-1"),
                        Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());

        List<SecurityEquipmentStatusVO> result = service.listEquipment();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("bim-1", result.get(0).getBimId());
        Assertions.assertEquals("location-1", result.get(0).getBimLocation());
        Assertions.assertNull(result.get(0).getName());
        Assertions.assertEquals("Local Camera Name", result.get(0).getLocalName());
        ArgumentCaptor<Collection> fieldsCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(objectRepository).listByCacheKey(
                Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                 Mockito.eq(SecurityDtpRwdEquipmentDTO.class),
                 fieldsCaptor.capture());
        Set<String> expectedFields = new LinkedHashSet<>();
        for (DtpRwdClassCodeEnum classCode : Arrays.asList(
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
                DtpRwdClassCodeEnum.SPVSDR)) {
            expectedFields.addAll(CacheObjectExtensionFieldConfig.listObjectQueryFields(
                    classCode.value()));
        }
        Assertions.assertEquals(
                expectedFields,
                new LinkedHashSet<>(fieldsCaptor.getValue()));
        Assertions.assertFalse(fieldsCaptor.getValue().contains(
                DtpRwdObjectDTO.Fields.name));
        Assertions.assertFalse(fieldsCaptor.getValue().contains(
                SecurityDtpRwdEquipmentDTO.Fields.bimId));
        Assertions.assertFalse(fieldsCaptor.getValue().contains(
                SecurityDtpRwdEquipmentDTO.Fields.bimLocation));
        Mockito.verify(statusService).isReady(RunningMonitorTypeEnum.SECURITY);
        Mockito.verify(rwdClient).listTwinControlObjects(
                Mockito.eq(false), Mockito.eq("project-1"),
                Mockito.eq(Collections.singleton(DtpRwdClassCodeEnum.WEGMCM.value())));
        Mockito.verifyNoMoreInteractions(rwdClient);
        Mockito.verifyNoInteractions(sourceStrategy);
    }

    /**
     * 验证 READY 撤布防空间通过设备、关系、空间和模型视图缓存一次组装。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldReadDeploymentSpacesFromCacheWithoutStaticRwdCalls() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        RunningMonitorSecurityStrategy sourceStrategy =
                Mockito.mock(RunningMonitorSecurityStrategy.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        CacheObjectModelViewRecordConverter modelViewConverter =
                Mockito.mock(CacheObjectModelViewRecordConverter.class);
        SecurityServiceImpl service = newService(
                rwdClient, sourceStrategy, statusService,
                objectRepository, relationRepository,
                modelViewRepository, modelViewConverter);
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(
                        equipment("controller-1", DtpRwdClassCodeEnum.SPIACU)));
        CacheObjectRel relation = new CacheObjectRel();
        relation.setObjectId("controller-1");
        relation.setRelObjectId("space-1");
        Mockito.when(relationRepository.listForward(Mockito.any(
                        CacheObjectRelQueryDaoParam.class)))
                .thenReturn(Collections.singletonList(relation));
        DtpRwdSpaceDTO cachedSpace = new DtpRwdSpaceDTO();
        cachedSpace.setId("space-1");
        cachedSpace.setClassCode(DtpRwdClassCodeEnum.SPACE.value());
        cachedSpace.setLocalName("Space 1");
        Mockito.when(objectRepository.listByObjectIds(
                        Mockito.eq(Collections.singleton("space-1")),
                        Mockito.eq(DtpRwdSpaceDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(cachedSpace));
        CacheObjectModelView modelView = new CacheObjectModelView();
        modelView.setId("space-1");
        Mockito.when(modelViewRepository.listModelViews(Mockito.any(
                        CacheObjectModelViewQueryDaoParam.class)))
                .thenReturn(Collections.singletonList(modelView));
        DtpRwdSpaceDTO modelFields = new DtpRwdSpaceDTO();
        modelFields.setId("space-1");
        modelFields.setOutline("outline-1");
        Mockito.when(modelViewConverter.toRwdObject(
                        modelView, DtpRwdSpaceDTO.class))
                .thenReturn(modelFields);

        List<DtpRwdSpaceVO> result = service.listDeploymentSpace();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("space-1", result.get(0).getId());
        Assertions.assertNull(result.get(0).getName());
        Assertions.assertEquals("Space 1", result.get(0).getLocalName());
        Assertions.assertEquals("outline-1", result.get(0).getOutline());
        ArgumentCaptor<Collection> fieldsCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(objectRepository).listByObjectIds(
                Mockito.eq(Collections.singleton("space-1")),
                Mockito.eq(DtpRwdSpaceDTO.class),
                fieldsCaptor.capture());
        Assertions.assertTrue(fieldsCaptor.getValue().contains(
                DtpRwdObjectDTO.Fields.localName));
        Assertions.assertFalse(fieldsCaptor.getValue().contains(
                DtpRwdObjectDTO.Fields.name));
        Mockito.verify(statusService).isReady(RunningMonitorTypeEnum.SECURITY);
        Mockito.verifyNoInteractions(rwdClient, sourceStrategy);
    }

    /**
     * 验证 READY 功能查询只读取最小设备缓存字段并且只判断一次 READY。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldReadSecurityFunctionFromCacheWithOneReadyDecision() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        RunningMonitorSecurityStrategy sourceStrategy =
                Mockito.mock(RunningMonitorSecurityStrategy.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        SecurityServiceImpl service = newService(
                rwdClient, sourceStrategy, statusService,
                objectRepository, Mockito.mock(CacheObjectRelRepository.class),
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        SecurityDtpRwdEquipmentDTO door =
                equipment("door-1", DtpRwdClassCodeEnum.CFCSDR);
        door.setUndefinedProperties(
                com.persagy.apccontrol.basic.model.base.InfoCodeEnum
                        .MONITORING_SURVEILLANCE_STATUS.value(), "2");
        SecurityDtpRwdEquipmentDTO camera =
                equipment("camera-1", DtpRwdClassCodeEnum.SPVSCM);
        camera.setCameraType("1");
        SecurityDtpRwdEquipmentDTO controller =
                equipment("controller-1", DtpRwdClassCodeEnum.SPIACU);
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(Arrays.asList(door, camera, controller));

        SecurityFunctionVO result = service.querySecurityFunction();

        Assertions.assertTrue(result.getAccessDemandFunction());
        Assertions.assertTrue(result.getDeploymentStateFunction());
        Assertions.assertTrue(result.getOverallFunction());
        Mockito.verify(statusService).isReady(RunningMonitorTypeEnum.SECURITY);
        Mockito.verifyNoMoreInteractions(statusService);
        Mockito.verifyNoInteractions(rwdClient, sourceStrategy);
    }

    /**
     * 验证 READY 缓存空结果是权威结果，不穿透原 RWD 路径。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldTreatReadyEmptyCacheAsAuthoritative() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        RunningMonitorSecurityStrategy sourceStrategy =
                Mockito.mock(RunningMonitorSecurityStrategy.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        SecurityServiceImpl service = newService(
                rwdClient, sourceStrategy, statusService,
                objectRepository, Mockito.mock(CacheObjectRelRepository.class),
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());

        SecurityFunctionVO result = service.querySecurityFunction();

        Assertions.assertFalse(result.getAccessDemandFunction());
        Assertions.assertFalse(result.getDeploymentStateFunction());
        Assertions.assertFalse(result.getOverallFunction());
        Mockito.verifyNoInteractions(rwdClient, sourceStrategy);
    }

    /**
     * 验证 READY 缓存基础设施异常直接上抛，不伪装成空结果或切回旧路径。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldPropagateReadyCacheFailureWithoutFallback() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        RunningMonitorSecurityStrategy sourceStrategy =
                Mockito.mock(RunningMonitorSecurityStrategy.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        SecurityServiceImpl service = newService(
                rwdClient, sourceStrategy, statusService,
                objectRepository, Mockito.mock(CacheObjectRelRepository.class),
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        IllegalStateException failure = new IllegalStateException("cache unavailable");
        Mockito.when(objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(SecurityDtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenThrow(failure);

        IllegalStateException actual = Assertions.assertThrows(
                IllegalStateException.class, service::querySecurityFunction);

        Assertions.assertSame(failure, actual);
        Mockito.verifyNoInteractions(rwdClient, sourceStrategy);
    }

    /**
     * 验证未 READY 时三个目标接口保留原设备、空间和两次功能查询路径。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldKeepLegacyPathsWhenSecurityCacheIsNotReady() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        RunningMonitorSecurityStrategy sourceStrategy =
                Mockito.mock(RunningMonitorSecurityStrategy.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        CacheObjectModelViewRepository modelViewRepository =
                Mockito.mock(CacheObjectModelViewRepository.class);
        SecurityServiceImpl service = newService(
                rwdClient, sourceStrategy, statusService,
                objectRepository, relationRepository,
                modelViewRepository,
                Mockito.mock(CacheObjectModelViewRecordConverter.class));
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(false);
        Mockito.when(rwdClient.listSecurityEquipment(
                        Mockito.eq(false), Mockito.eq("project-1"), Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(rwdClient.listTwinControlObjects(
                        Mockito.eq(false), Mockito.eq("project-1"),
                        Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());
        Mockito.when(sourceStrategy.listDeploymentSpace())
                .thenReturn(Collections.emptyList());

        Assertions.assertTrue(service.listEquipment().isEmpty());
        Assertions.assertTrue(service.listDeploymentSpace().isEmpty());
        SecurityFunctionVO function = service.querySecurityFunction();
        Assertions.assertFalse(function.getOverallFunction());

        Mockito.verify(statusService, Mockito.times(3))
                .isReady(RunningMonitorTypeEnum.SECURITY);
        Mockito.verify(rwdClient, Mockito.times(3)).listSecurityEquipment(
                Mockito.eq(false), Mockito.eq("project-1"), Mockito.any());
        Mockito.verify(sourceStrategy).listDeploymentSpace();
        Mockito.verifyNoInteractions(
                objectRepository, relationRepository, modelViewRepository);
    }

    /**
     * 验证公共设备查询即使项目 READY 仍保留调用方关系参数并始终走原 RWD 语义。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldKeepPublicEquipmentQueryOnRwdWithCallerRelationScope() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        RunningMonitorSecurityStrategy sourceStrategy =
                Mockito.mock(RunningMonitorSecurityStrategy.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository objectRepository = Mockito.mock(CacheObjectRepository.class);
        SecurityServiceImpl service = newService(
                rwdClient, sourceStrategy, statusService,
                objectRepository, Mockito.mock(CacheObjectRelRepository.class),
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        Mockito.when(rwdClient.listSecurityEquipment(
                        Mockito.eq(false), Mockito.eq("project-1"), Mockito.any()))
                .thenReturn(Collections.emptyList());
        DtpRwdObjectRelationQueryParam relationScope =
                new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE)
                        .setObjTo(Arrays.asList("space-1", "space-2"));
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setRelationFrom(relationScope);
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);

        Assertions.assertTrue(service.getSecureAllEquipmentList(
                "project-1", false, queryParam).isEmpty());

        ArgumentCaptor<DtpRwdObjectQueryParam> queryCaptor =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(rwdClient).listSecurityEquipment(
                Mockito.eq(false), Mockito.eq("project-1"), queryCaptor.capture());
        Assertions.assertSame(relationScope, queryCaptor.getValue().getRelationFrom());
        Assertions.assertEquals(
                new LinkedHashSet<>(Arrays.asList("space-1", "space-2")),
                new LinkedHashSet<>(queryCaptor.getValue().getRelationFrom().getObjTo()));
        Mockito.verifyNoInteractions(statusService, sourceStrategy, objectRepository);
    }

    /**
     * 创建待测服务，未参与当前路由断言的动态依赖使用独立 Mock。
     *
     * @param rwdClient RWD 客户端
     * @param sourceStrategy 原安防策略
     * @param statusService READY 状态服务
     * @param objectRepository 对象仓储
     * @param relationRepository 关系仓储
     * @param modelViewRepository 模型视图仓储
     * @param modelViewConverter 模型视图转换器
     * @return 待测安防服务
     * @since v7.260802.0
     */
    private static SecurityServiceImpl newService(
            DtpRwdClientService rwdClient,
            RunningMonitorSecurityStrategy sourceStrategy,
            CacheObjectModelStatusService statusService,
            CacheObjectRepository objectRepository,
            CacheObjectRelRepository relationRepository,
            CacheObjectModelViewRepository modelViewRepository,
            CacheObjectModelViewRecordConverter modelViewConverter) {
        return new SecurityServiceImpl(
                rwdClient,
                Mockito.mock(AbstractIotDataCacheServer.class),
                Mockito.mock(AbstractCommonServer.class),
                sourceStrategy,
                new SecurityConverterImpl(),
                Mockito.mock(BizDtpRwdService.class),
                Mockito.mock(InstanceService.class),
                Mockito.mock(BizComputeService.class),
                Mockito.mock(DemandConfigGateway.class),
                Mockito.mock(DemandInstanceGateway.class),
                statusService,
                objectRepository,
                relationRepository,
                modelViewRepository,
                modelViewConverter);
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
}
