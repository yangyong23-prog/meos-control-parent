package com.persagy.apccontrol.basic.service.security.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.security.SecurityModelViewParam;
import com.persagy.apccontrol.basic.model.vo.security.SecurityEquipmentTypeStatisticsVo;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.BizComputeService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorSecurityStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.security.converter.SecurityConverterImpl;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.i18n.I18nProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 安防设备统计 READY 路由纯单元测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class SecurityServiceImplEquipmentStatisticsCacheRoutingTest {

    private static ApplicationContext originalApplicationContext;
    private static StaticApplicationContext testApplicationContext;

    private DtpRwdClientService rwdClient;
    private AbstractIotDataCacheServer iotDataCacheServer;
    private AbstractCommonServer commonServer;
    private CacheObjectModelStatusService statusService;
    private CacheObjectRepository objectRepository;
    private SecurityServiceImpl service;

    /**
     * 注册枚举显示名解析依赖，避免纯单测依赖应用完整上下文。
     *
     * @since v7.260802.0
     */
    @BeforeAll
    public static void setUpSpringUtil() {
        originalApplicationContext = SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext =
                new StaticApplicationContext();
        I18nProperties i18nProperties = Mockito.mock(I18nProperties.class);
        Mockito.when(i18nProperties.getI18n()).thenReturn("zh-CN");
        applicationContext.getBeanFactory().registerSingleton(
                "i18nProperties", i18nProperties);
        applicationContext.refresh();
        testApplicationContext = applicationContext;
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * 恢复原 Spring 上下文。
     *
     * @since v7.260802.0
     */
    @AfterAll
    public static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(originalApplicationContext);
        if (testApplicationContext != null) {
            testApplicationContext.close();
        }
        testApplicationContext = null;
        originalApplicationContext = null;
    }

    /**
     * 初始化项目上下文和隔离依赖。
     *
     * @since v7.260802.0
     */
    @BeforeEach
    public void setUp() {
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setTenant("group-1");
        ContextUtil.setProjectId("project-1");
        this.rwdClient = Mockito.mock(DtpRwdClientService.class);
        this.iotDataCacheServer = Mockito.mock(AbstractIotDataCacheServer.class);
        this.commonServer = Mockito.mock(AbstractCommonServer.class);
        this.statusService = Mockito.mock(CacheObjectModelStatusService.class);
        this.objectRepository = Mockito.mock(CacheObjectRepository.class);
        this.service = new SecurityServiceImpl(
                this.rwdClient,
                this.iotDataCacheServer,
                this.commonServer,
                Mockito.mock(RunningMonitorSecurityStrategy.class),
                new SecurityConverterImpl(),
                Mockito.mock(BizDtpRwdService.class),
                Mockito.mock(InstanceService.class),
                Mockito.mock(BizComputeService.class),
                Mockito.mock(DemandConfigGateway.class),
                Mockito.mock(DemandInstanceGateway.class),
                this.statusService,
                this.objectRepository,
                Mockito.mock(CacheObjectRelRepository.class),
                Mockito.mock(CacheObjectModelViewRepository.class),
                Mockito.mock(CacheObjectModelViewRecordConverter.class));
        Mockito.when(this.statusService.isReady(RunningMonitorTypeEnum.SECURITY))
                .thenReturn(true);
        Mockito.when(this.iotDataCacheServer.listInfoCodeValueInOneHour(
                        Mockito.anyCollection(), Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());
        Mockito.when(this.commonServer
                        .listAbstractOverlayRiskControlTargetConditions(
                                Mockito.anyCollection(), Mockito.isNull()))
                .thenReturn(Collections.emptyList());
    }

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
     * 验证 READY 只使用六类静态候选，并以最终去重 ID 查询固定动态字段和异常事件。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldUseReadyStaticCandidatesAndFixedDynamicQueries() {
        DtpRwdEquipmentDTO door = equipment(
                "door-1", DtpRwdClassCodeEnum.CFCSDR, "floor-1");
        setProperty(door, InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(), "2");
        DtpRwdEquipmentDTO duplicateDoor = equipment(
                "door-1", DtpRwdClassCodeEnum.CFCSDR, "floor-1");
        setProperty(duplicateDoor,
                InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(), "2");
        DtpRwdEquipmentDTO fireDoor = equipment(
                "fire-door-1", DtpRwdClassCodeEnum.CFCSFR, "floor-1");
        setProperty(fireDoor,
                InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(), "3");
        DtpRwdEquipmentDTO gate = equipment(
                "gate-1", DtpRwdClassCodeEnum.SPEGFG, "floor-1");
        setProperty(gate,
                InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(), "2");
        DtpRwdEquipmentDTO controller = equipment(
                "controller-1", DtpRwdClassCodeEnum.SPIACU, "floor-1");
        DtpRwdEquipmentDTO securityCamera = equipment(
                "camera-1", DtpRwdClassCodeEnum.SPVSCM, "floor-1");
        setProperty(securityCamera, InfoCodeEnum.CAMERA_TYPE.getCode(), "1");
        setProperty(securityCamera,
                InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode(), "rtsp");
        DtpRwdEquipmentDTO parkingCamera = equipment(
                "parking-camera-1", DtpRwdClassCodeEnum.WEGMCM, "floor-1");
        setProperty(parkingCamera,
                InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode(), "rtsp");
        DtpRwdEquipmentDTO parkingCameraWithoutStream = equipment(
                "parking-camera-2", DtpRwdClassCodeEnum.WEGMCM, "floor-1");
        DtpRwdEquipmentDTO unsupported = equipment(
                "video-server-1", DtpRwdClassCodeEnum.SPVSVE, "floor-1");
        Mockito.when(this.objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(DtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(Arrays.asList(
                        door, duplicateDoor, fireDoor, gate, controller,
                        securityCamera, parkingCamera,
                        parkingCameraWithoutStream, unsupported));

        List<SecurityEquipmentTypeStatisticsVo> result =
                this.service.getEquipmentTypeStatistics(
                        new SecurityModelViewParam(
                                "project-1", DemandInstanceTypeEnum.PROJECT));

        Assertions.assertEquals(3, result.size());
        ArgumentCaptor<Collection> fieldsCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(this.objectRepository).listByCacheKey(
                Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                Mockito.eq(DtpRwdEquipmentDTO.class),
                fieldsCaptor.capture());
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList(
                        DtpRwdObjectDTO.Fields.id,
                        DtpRwdObjectDTO.Fields.classCode,
                        DtpRwdEquipmentDTO.Fields.floorId,
                        InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(),
                        InfoCodeEnum.HAS_ARMING_FUNC.value(),
                        InfoCodeEnum.CAMERA_TYPE.getCode(),
                        InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode())),
                new LinkedHashSet<>(fieldsCaptor.getValue()));

        Set<String> expectedIds = new LinkedHashSet<>(Arrays.asList(
                "door-1", "fire-door-1", "gate-1", "controller-1",
                "camera-1", "parking-camera-1"));
        ArgumentCaptor<Collection> iotIdsCaptor =
                ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection> infoCodesCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(this.iotDataCacheServer).listInfoCodeValueInOneHour(
                iotIdsCaptor.capture(), infoCodesCaptor.capture());
        Assertions.assertEquals(expectedIds,
                new LinkedHashSet<>(iotIdsCaptor.getValue()));
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList(
                        InfoCodeEnum.ARMING_STATUS.value(),
                        InfoCodeEnum.ONLINE_STATUS.getCode(),
                        InfoCodeEnum.RUN_STATUS.getCode(),
                        InfoCodeEnum.DEPLOY_WITHDRAWALARM_STATUS.getCode())),
                new LinkedHashSet<>(infoCodesCaptor.getValue()));
        ArgumentCaptor<Collection> exceptionIdsCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(this.commonServer)
                .listAbstractOverlayRiskControlTargetConditions(
                        exceptionIdsCaptor.capture(), Mockito.isNull());
        Assertions.assertEquals(expectedIds,
                new LinkedHashSet<>(exceptionIdsCaptor.getValue()));
        Mockito.verifyNoInteractions(this.rwdClient);
    }

    /**
     * 验证楼层视角只在缓存候选中收窄，且 READY 空缓存不触发动态查询或 RWD 回退。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldNarrowReadyCandidatesByFloorAndKeepEmptyAuthoritative() {
        DtpRwdEquipmentDTO floorOne = equipment(
                "controller-1", DtpRwdClassCodeEnum.SPIACU, "floor-1");
        DtpRwdEquipmentDTO floorTwo = equipment(
                "controller-2", DtpRwdClassCodeEnum.SPIACU, "floor-2");
        Mockito.when(this.objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(DtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(Arrays.asList(floorOne, floorTwo))
                .thenReturn(Collections.emptyList());

        this.service.getEquipmentTypeStatistics(new SecurityModelViewParam(
                "floor-1", DemandInstanceTypeEnum.FLOOR));
        this.service.getEquipmentTypeStatistics(new SecurityModelViewParam(
                "floor-1", DemandInstanceTypeEnum.FLOOR));

        ArgumentCaptor<Collection> iotIdsCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(this.iotDataCacheServer).listInfoCodeValueInOneHour(
                iotIdsCaptor.capture(), Mockito.anyCollection());
        Assertions.assertEquals(Collections.singleton("controller-1"),
                new LinkedHashSet<>(iotIdsCaptor.getValue()));
        Mockito.verify(this.commonServer)
                .listAbstractOverlayRiskControlTargetConditions(
                        Mockito.eq(Collections.singleton("controller-1")),
                        Mockito.isNull());
        Mockito.verifyNoMoreInteractions(
                this.iotDataCacheServer, this.commonServer);
        Mockito.verifyNoInteractions(this.rwdClient);
    }

    /**
     * 验证旧统计不支持的建筑视角继续返回空，且不加载缓存候选或动态数据。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldKeepUnsupportedStatisticsPerspectiveEmpty() {
        List<SecurityEquipmentTypeStatisticsVo> result =
                this.service.getEquipmentTypeStatistics(
                        new SecurityModelViewParam(
                                "building-1", DemandInstanceTypeEnum.BUILDING));

        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.stream()
                .allMatch(item -> item.getCount() == 0
                        && item.getList().isEmpty()));
        Mockito.verifyNoInteractions(
                this.objectRepository,
                this.iotDataCacheServer,
                this.commonServer,
                this.rwdClient);
    }

    /**
     * 验证 READY 缓存和动态查询异常直接传播，不伪装为空结果或回退 RWD。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldPropagateReadyCacheAndDynamicFailures() {
        IllegalStateException cacheFailure =
                new IllegalStateException("cache unavailable");
        Mockito.when(this.objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(DtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenThrow(cacheFailure);

        IllegalStateException actualCacheFailure = Assertions.assertThrows(
                IllegalStateException.class,
                () -> this.service.getEquipmentTypeStatistics(
                        new SecurityModelViewParam(
                                "project-1", DemandInstanceTypeEnum.PROJECT)));
        Assertions.assertSame(cacheFailure, actualCacheFailure);

        DtpRwdEquipmentDTO controller = equipment(
                "controller-1", DtpRwdClassCodeEnum.SPIACU, "floor-1");
        Mockito.reset(this.objectRepository, this.iotDataCacheServer);
        Mockito.when(this.objectRepository.listByCacheKey(
                        Mockito.eq(CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT),
                        Mockito.eq(DtpRwdEquipmentDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(Collections.singletonList(controller));
        IllegalArgumentException dynamicFailure =
                new IllegalArgumentException("iot unavailable");
        Mockito.when(this.iotDataCacheServer.listInfoCodeValueInOneHour(
                        Mockito.anyCollection(), Mockito.anyCollection()))
                .thenThrow(dynamicFailure);

        IllegalArgumentException actualDynamicFailure = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> this.service.getEquipmentTypeStatistics(
                        new SecurityModelViewParam(
                                "project-1", DemandInstanceTypeEnum.PROJECT)));
        Assertions.assertSame(dynamicFailure, actualDynamicFailure);
        Mockito.verifyNoInteractions(this.rwdClient);
    }

    /**
     * 创建设备测试对象。
     *
     * @param id 对象 ID
     * @param classCode 设备类
     * @param floorId 楼层 ID
     * @return 设备对象
     * @since v7.260802.0
     */
    private static DtpRwdEquipmentDTO equipment(
            String id, DtpRwdClassCodeEnum classCode, String floorId) {
        DtpRwdEquipmentDTO result = new DtpRwdEquipmentDTO();
        result.setId(id);
        result.setClassCode(classCode.value());
        result.setFloorId(floorId);
        return result;
    }

    /**
     * 写入 RWD 扩展静态字段。
     *
     * @param equipment 设备对象
     * @param key 字段名
     * @param value 字段值
     * @since v7.260802.0
     */
    private static void setProperty(
            DtpRwdEquipmentDTO equipment, String key, String value) {
        equipment.setUndefinedProperties(key, value);
    }
}
