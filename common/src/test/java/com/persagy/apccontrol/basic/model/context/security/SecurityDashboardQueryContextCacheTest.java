package com.persagy.apccontrol.basic.model.context.security;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigResultBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.security.SecurityDashboardQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigResultVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizComputeService;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorSecurityStrategy;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.DemandConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.security.DemandConfigSecurityArmingAnalyzeStrategy;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 安防 Dashboard READY 静态实例组装测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class SecurityDashboardQueryContextCacheTest {

    /**
     * 测试需求模板日期解析所需的日历服务替身。
     *
     * @since v7.260802.0
     */
    private static final DemandCalendarQueryService DEMAND_CALENDAR_QUERY_SERVICE =
            Mockito.mock(DemandCalendarQueryService.class);

    /**
     * 测试需求模板查询所需的模板仓储替身。
     *
     * @since v7.260802.0
     */
    private static final DemandTemplateRepository DEMAND_TEMPLATE_REPOSITORY =
            Mockito.mock(DemandTemplateRepository.class);

    /**
     * 保存测试前的 Spring 应用上下文，以便测试结束后恢复。
     *
     * @since v7.260802.0
     */
    private static ApplicationContext originalApplicationContext;

    /**
     * 提供需求结果查询测试依赖的临时 Spring 应用上下文。
     *
     * @since v7.260802.0
     */
    private static StaticApplicationContext testApplicationContext;

    /**
     * 注册需求配置结果查询测试所需的 Spring Bean，并替换全局测试上下文。
     *
     * @since v7.260802.0
     */
    @BeforeAll
    static void setUpSpringUtil() {
        originalApplicationContext = SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton(
                "demandCalendarQueryService", DEMAND_CALENDAR_QUERY_SERVICE);
        applicationContext.getBeanFactory().registerSingleton(
                "demandTemplateRepository", DEMAND_TEMPLATE_REPOSITORY);
        DemandConfigAnalyzeStrategy<?> armingStrategy = Mockito.mock(DemandConfigAnalyzeStrategy.class);
        Mockito.when(armingStrategy.inheritConfigType()).thenReturn(InheritConfigTypeEnum.OBJECT);
        applicationContext.getBeanFactory().registerSingleton(
                DemandConfigSecurityArmingAnalyzeStrategy.BEAN_NAME, armingStrategy);
        applicationContext.refresh();
        testApplicationContext = applicationContext;
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * 关闭测试上下文并恢复测试前的全局 Spring 应用上下文。
     *
     * @since v7.260802.0
     */
    @AfterAll
    static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(originalApplicationContext);
        if (testApplicationContext != null) {
            testApplicationContext.close();
        }
        testApplicationContext = null;
        originalApplicationContext = null;
    }

    /**
     * 验证 ARMING 保留两类设备关系并排除屏蔽或黑名单空间。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldAssembleArmingSpacesFromCachedRelations() {
        SecurityDashboardQueryContext context = newContext(
                RunningMonitorSecondTypeEnum.SECURITY_ARMING);
        DtpRwdSpaceDTO controllerSpace = space("space-controller");
        DtpRwdSpaceDTO cameraSpace = space("space-camera");
        DtpRwdSpaceDTO excludedSpace = space("space-excluded");
        SecurityDtpRwdEquipmentDTO controller = equipment(
                "controller-1", DtpRwdClassCodeEnum.SPIACU);
        SecurityDtpRwdEquipmentDTO camera = equipment(
                "camera-1", DtpRwdClassCodeEnum.SPVSCM);
        camera.setHasArmingFunc("1");
        SecurityDtpRwdEquipmentDTO cameraWithoutArming = equipment(
                "camera-2", DtpRwdClassCodeEnum.SPVSCM);
        cameraWithoutArming.setHasArmingFunc("0");
        DtpRwdBuildingDTO building = new DtpRwdBuildingDTO();
        building.setId("building-1");
        DtpRwdFloorDTO floor = new DtpRwdFloorDTO();
        floor.setId("floor-1");
        floor.setBuildingId("building-1");

        Map<String, Set<String>> serviceRelations = new LinkedHashMap<>();
        serviceRelations.put("controller-1", new LinkedHashSet<>(Arrays.asList(
                "space-controller", "space-excluded")));
        Map<String, Set<String>> inRelations = new LinkedHashMap<>();
        inRelations.put("camera-1", Collections.singleton("space-camera"));
        inRelations.put("camera-2", Collections.singleton("space-controller"));

        context.fillInstanceFromCache(
                Arrays.asList(controllerSpace, cameraSpace, excludedSpace),
                Arrays.asList(controller, camera, cameraWithoutArming),
                serviceRelations,
                inRelations,
                Collections.singleton("space-excluded"),
                Collections.singletonList(building),
                Collections.singletonList(floor));

        Assertions.assertEquals(
                new LinkedHashSet<>(Arrays.asList(
                        "space-controller", "space-camera")),
                context.getSpaces().stream()
                        .map(DtpRwdSpaceDTO::getId)
                        .collect(java.util.stream.Collectors.toCollection(
                                LinkedHashSet::new)));
        Map<String, Map<DtpRwdClassCodeEnum, Set<String>>> floorSpaces =
                context.getFloorIdSpaceIdAndEquipmentIds().get("floor-1");
        Assertions.assertEquals(
                Collections.singleton("controller-1"),
                floorSpaces.get("space-controller").get(DtpRwdClassCodeEnum.SPIACU));
        Assertions.assertEquals(
                Collections.singleton("camera-1"),
                floorSpaces.get("space-camera").get(DtpRwdClassCodeEnum.SPVSCM));
        Assertions.assertSame(building, context.getBuildings().get(0));
        Assertions.assertSame(floor, context.getBuildingIdAndFloors()
                .get("building-1").get(0));
    }

    /**
     * 验证 PASSING 继续保持原 Dashboard 的空设备关系过滤语义。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldKeepPassingEmptyEquipmentRelationBehavior() {
        SecurityDashboardQueryContext context = newContext(
                RunningMonitorSecondTypeEnum.SECURITY_ACCESS);

        context.fillInstanceFromCache(
                Collections.singletonList(space("space-1")),
                Collections.singletonList(equipment(
                        "door-1", DtpRwdClassCodeEnum.CFCSDR)),
                Collections.emptyMap(),
                Collections.singletonMap(
                        "door-1", Collections.singleton("space-1")),
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.emptyList());

        Assertions.assertTrue(context.getSpaces().isEmpty());
        Assertions.assertTrue(context.getFloorIdSpaceIdAndEquipmentIds().isEmpty());
    }

    /**
     * 验证 READY 缓存路径保持与 RWD 路径一致的建筑升序、楼层从高到低排序。
     *
     * <p>回归 UYAI-1311：性能优化将远程查询替换为本地缓存后丢失了
     * {@link DtpRwdClientService#listFloor} / {@link DtpRwdClientService#listBuilding}
     * 内部的序号排序行为。</p>
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldSortBuildingsAndFloorsFromCachedSnapshot() {
        SecurityDashboardQueryContext context = newContext(
                RunningMonitorSecondTypeEnum.SECURITY_ARMING);
        DtpRwdBuildingDTO buildingLow = new DtpRwdBuildingDTO();
        buildingLow.setId("building-low");
        buildingLow.setBuildingSequenceId(1);
        DtpRwdBuildingDTO buildingHigh = new DtpRwdBuildingDTO();
        buildingHigh.setId("building-high");
        buildingHigh.setBuildingSequenceId(2);
        DtpRwdBuildingDTO buildingNoSequence = new DtpRwdBuildingDTO();
        buildingNoSequence.setId("building-no-sequence");
        DtpRwdFloorDTO floorTop = floor("floor-top", "building-low", 10);
        DtpRwdFloorDTO floorBottom = floor("floor-bottom", "building-low", 5);
        DtpRwdFloorDTO floorHigh = floor("floor-high", "building-high", 9);
        DtpRwdFloorDTO floorNoSequence = floor("floor-no-sequence", "building-high", null);
        DtpRwdFloorDTO floorNoSequenceBuilding = floor("floor-no-sequence-building", "building-no-sequence", 1);
        SecurityDtpRwdEquipmentDTO controller = equipment(
                "controller-1", DtpRwdClassCodeEnum.SPIACU);
        Map<String, Set<String>> serviceRelations = new LinkedHashMap<>();
        serviceRelations.put("controller-1", new LinkedHashSet<>(Arrays.asList(
                "space-top", "space-bottom", "space-high", "space-no-sequence", "space-no-sequence-building")));

        context.fillInstanceFromCache(
                Arrays.asList(
                        space("space-top", "building-low", "floor-top"),
                        space("space-bottom", "building-low", "floor-bottom"),
                        space("space-high", "building-high", "floor-high"),
                        space("space-no-sequence", "building-high", "floor-no-sequence"),
                        space("space-no-sequence-building", "building-no-sequence", "floor-no-sequence-building")),
                Collections.singletonList(controller),
                serviceRelations,
                Collections.emptyMap(),
                Collections.emptySet(),
                Arrays.asList(buildingHigh, buildingLow, buildingNoSequence),
                Arrays.asList(floorBottom, floorNoSequence, floorTop, floorHigh, floorNoSequenceBuilding));

        Assertions.assertEquals(
                Arrays.asList("building-no-sequence", "building-low", "building-high"),
                context.getBuildings().stream()
                        .map(DtpRwdBuildingDTO::getId)
                        .collect(java.util.stream.Collectors.toList()));
        Assertions.assertEquals(
                Arrays.asList("floor-top", "floor-bottom"),
                context.getBuildingIdAndFloors().get("building-low").stream()
                        .map(DtpRwdFloorDTO::getId)
                        .collect(java.util.stream.Collectors.toList()));
        Assertions.assertEquals(
                Arrays.asList("floor-high", "floor-no-sequence"),
                context.getBuildingIdAndFloors().get("building-high").stream()
                        .map(DtpRwdFloorDTO::getId)
                        .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * 验证 Dashboard 使用需求配置结果查询，并只保留当前监测空间的有效结果。
     *
     * @since v7.260802.0
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldReadDemandConfigResultsForDashboard() {
        Long templateId = 101L;
        DemandTemplate template = new DemandTemplate();
        template.setId(templateId);
        template.setDemandTemplateType(
                com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum.ARMING);
        template.setDemandTemplateTag("arming-template");
        DemandTemplateDateSettingVO dateSetting = new DemandTemplateDateSettingVO();
        DemandTemplateVO templateVO = new DemandTemplateVO();
        templateVO.setTemplateId(templateId);
        dateSetting.setRelTemplate(templateVO);
        Mockito.when(DEMAND_CALENDAR_QUERY_SERVICE.getDateSetting(
                        Mockito.any(), Mockito.any(LocalDate.class)))
                .thenReturn(dateSetting);
        Mockito.when(DEMAND_TEMPLATE_REPOSITORY.getByTemplateIdAndDate(
                        Mockito.eq(false), Mockito.eq(templateId), Mockito.any(LocalDate.class)))
                .thenReturn(template);

        @SuppressWarnings("rawtypes")
        DemandConfigGateway gateway = Mockito.mock(DemandConfigGateway.class);
        DemandConfigResultVO<BaseDemandConfig> configured = new DemandConfigResultVO<>();
        configured.setInstanceId("space-1");
        configured.setFinalConfig(new DemandConfigValueObject<>());
        DemandConfigResultVO<BaseDemandConfig> outside = new DemandConfigResultVO<>();
        outside.setInstanceId("space-outside");
        outside.setFinalConfig(new DemandConfigValueObject<>());
        Mockito.when(gateway.listConfigResultsByInstances(
                        Mockito.any(DemandConfigResultBatchQueryParam.class)))
                .thenReturn(Arrays.asList(configured, outside));

        SecurityDashboardQueryContext context = new SecurityDashboardQueryContext(
                dashboardParam(RunningMonitorSecondTypeEnum.SECURITY_ARMING),
                Mockito.mock(RunningMonitorSecurityStrategy.class),
                Mockito.mock(DtpRwdClientService.class),
                Mockito.mock(AbstractIotDataCacheServer.class),
                gateway,
                Mockito.mock(InstanceService.class),
                Mockito.mock(BizComputeService.class));
        context.setSpaces(Collections.singletonList(space("space-1")));
        ContextUtil.setProjectId("project-1");
        try {
            context.fillDemandConfig();
        } finally {
            ContextUtil.remove();
        }

        Assertions.assertEquals(1, context.getExistsDemandSpaceConfigs().size());
        Assertions.assertEquals("space-1", context.getExistsDemandSpaceConfigs().get(0)
                .getIdentifier().getInstance().getInstanceId());
        Mockito.verify(gateway).listConfigResultsByInstances(
                Mockito.argThat(param -> param.getTemplateId().equals(templateId)
                        && param.getInstanceType()
                        == com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum.SPACE
                        && param.isFilterShieldedSpaces()
                        && !param.isAutoFillEmptyInstanceIds()));
        Mockito.verify(gateway, Mockito.never()).listConfigsByInstances(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    private static SecurityDashboardQueryContext newContext(
            RunningMonitorSecondTypeEnum secondType) {
        SecurityDashboardQueryParam param = dashboardParam(secondType);
        return new SecurityDashboardQueryContext(
                param,
                Mockito.mock(RunningMonitorSecurityStrategy.class),
                Mockito.mock(DtpRwdClientService.class),
                Mockito.mock(AbstractIotDataCacheServer.class),
                Mockito.mock(DemandConfigGateway.class),
                Mockito.mock(InstanceService.class),
                Mockito.mock(BizComputeService.class));
    }

    /**
     * 创建指定安防场景的 Dashboard 查询参数。
     *
     * @param secondType 安防 Dashboard 的二级运行监测类型
     * @return 已设置二级运行监测类型的 Dashboard 查询参数，不返回 null
     * @since v7.260802.0
     */
    private static SecurityDashboardQueryParam dashboardParam(
            RunningMonitorSecondTypeEnum secondType) {
        SecurityDashboardQueryParam param = new SecurityDashboardQueryParam();
        param.setRunningMonitorSecondType(secondType);
        return param;
    }

    private static DtpRwdSpaceDTO space(String id) {
        DtpRwdSpaceDTO space = new DtpRwdSpaceDTO();
        space.setId(id);
        space.setBuildingId("building-1");
        space.setFloorId("floor-1");
        return space;
    }

    private static DtpRwdSpaceDTO space(String id, String buildingId, String floorId) {
        DtpRwdSpaceDTO space = new DtpRwdSpaceDTO();
        space.setId(id);
        space.setBuildingId(buildingId);
        space.setFloorId(floorId);
        return space;
    }

    private static DtpRwdFloorDTO floor(String id, String buildingId, Integer sequenceId) {
        DtpRwdFloorDTO floor = new DtpRwdFloorDTO();
        floor.setId(id);
        floor.setBuildingId(buildingId);
        floor.setFloorSequenceId(sequenceId);
        return floor;
    }

    private static SecurityDtpRwdEquipmentDTO equipment(
            String id, DtpRwdClassCodeEnum classCode) {
        SecurityDtpRwdEquipmentDTO equipment = new SecurityDtpRwdEquipmentDTO();
        equipment.setId(id);
        equipment.setClassCode(classCode.value());
        return equipment;
    }
}
