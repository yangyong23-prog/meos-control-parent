package com.persagy.apccontrol.basic.service.parking.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.parking.ParkingSpaceTypeEnum;
import com.persagy.apccontrol.basic.model.param.parking.ParkingSpaceStatisticsQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.parking.FloorParkingSpaceStatisticVO;
import com.persagy.apccontrol.basic.model.vo.parking.ParkingSpaceGridStatisticsVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.parking.ParkingService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 停车场项目级统计规则分支测试。
 *
 * @author JiYinchuan
 * @since v7.260901.0
 */
public class ParkingServiceImplTest {

    private static final String PROJECT_ID = "project-1";
    private static final String FLOOR_ID = "floor-1";

    private final DtpRwdClientService dtpRwdClientService = Mockito.mock(DtpRwdClientService.class);
    private final AbstractCommonServer abstractCommonServer = Mockito.mock(AbstractCommonServer.class);
    private final AbstractIotDataCacheServer abstractIotDataCacheServer = Mockito.mock(AbstractIotDataCacheServer.class);
    private final com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService midCalendarClientService =
            Mockito.mock(com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService.class);
    private final DtpKnowledgeClientService dtpKnowledgeClientService = Mockito.mock(DtpKnowledgeClientService.class);

    private final ParkingServiceImpl service = new ParkingServiceImpl(
            dtpRwdClientService, abstractCommonServer, abstractIotDataCacheServer,
            midCalendarClientService, dtpKnowledgeClientService);

    @BeforeEach
    public void setUp() {
        ContextUtil.setProjectId(PROJECT_ID);
    }

    @AfterEach
    public void tearDown() {
        ContextUtil.remove();
    }

    @Test
    public void shouldKeepAggregatedVacantCountWhenAnyParkingSpaceHasParkingStatusRule() {
        ParkingServiceImpl spy = this.spyWithGridStatistics();
        this.stubRuleInfoCodes(true, Collections.emptySet());
        this.stubIotValues(this.parkingStatusValues(), null, null);

        Map<String, Integer> result = spy.getParkingSpaceStatistics(this.projectParam());

        Assertions.assertEquals(2, result.get(ParkingSpaceTypeEnum.PARKING_SPACE.value()));
        Assertions.assertEquals(1, result.get(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()));
        Assertions.assertFalse(result.containsKey(ParkingService.CAR_TOTAL));
    }

    @Test
    public void shouldReturnZeroVacantAndAllParkingTypeKeysWhenRuleExistsButNoIotData() {
        // AC1：CFPKPS 有规则但全部无 IOT 数据 → VACANT_PARKING_SPACE = 0；AC8：6 类车位 key 均存在且行为不变
        ParkingServiceImpl spy = this.spyWithGridStatistics(this.buildGridStatistics(0));
        this.stubRuleInfoCodes(true, Collections.emptySet());
        this.stubIotValues(Collections.emptyList(), null, null);

        Map<String, Integer> result = spy.getParkingSpaceStatistics(this.projectParam());

        Assertions.assertEquals(2, result.get(ParkingSpaceTypeEnum.PARKING_SPACE.value()));
        Assertions.assertEquals(0, result.get(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()));
        Assertions.assertEquals(0, result.get(ParkingSpaceTypeEnum.CHARGING_PARKING_SPACE.value()));
        Assertions.assertEquals(0, result.get(ParkingSpaceTypeEnum.VACANT_CHARGING_PARKING_SPACE.value()));
        Assertions.assertEquals(0, result.get(ParkingSpaceTypeEnum.FAST_CHARGING_PARKING_SPACE.value()));
        Assertions.assertEquals(0, result.get(ParkingSpaceTypeEnum.VACANT_FAST_CHARGING_PARKING_SPACE.value()));
        Assertions.assertFalse(result.containsKey(ParkingService.CAR_TOTAL));
    }

    @Test
    public void shouldReturnCarTotalWhenProjectHasCarNumRule() {
        ParkingServiceImpl spy = this.spyWithGridStatistics();
        this.stubRuleInfoCodes(true, Collections.singleton(InfoCodeEnum.CAR_NUM.value()));
        this.stubIotValues(this.parkingStatusValues(), null, 70);

        Map<String, Integer> result = spy.getParkingSpaceStatistics(this.projectParam());

        Assertions.assertEquals(1, result.get(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()));
        Assertions.assertEquals(70, result.get(ParkingService.CAR_TOTAL));
    }

    @Test
    public void shouldReturnNullCarTotalWhenProjectHasCarNumRuleButNoIotValue() {
        ParkingServiceImpl spy = this.spyWithGridStatistics();
        this.stubRuleInfoCodes(true, Collections.singleton(InfoCodeEnum.CAR_NUM.value()));
        this.stubIotValues(this.parkingStatusValues(), null, null);

        Map<String, Integer> result = spy.getParkingSpaceStatistics(this.projectParam());

        Assertions.assertTrue(result.containsKey(ParkingService.CAR_TOTAL));
        Assertions.assertNull(result.get(ParkingService.CAR_TOTAL));
    }

    @Test
    public void shouldUseProjectAvailableParkingSpaceNumberWhenNoParkingSpaceHasRule() {
        ParkingServiceImpl spy = this.spyWithGridStatistics();
        Set<String> projectRuleInfoCodes = new HashSet<>(Arrays.asList(
                InfoCodeEnum.AVAILABLE_PARKING_SPACE_NUMBER.value(), InfoCodeEnum.CAR_NUM.value()));
        this.stubRuleInfoCodes(false, projectRuleInfoCodes);
        this.stubIotValues(this.parkingStatusValues(), 25, 70);

        Map<String, Integer> result = spy.getParkingSpaceStatistics(this.projectParam());

        Assertions.assertEquals(2, result.get(ParkingSpaceTypeEnum.PARKING_SPACE.value()));
        Assertions.assertEquals(25, result.get(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()));
        Assertions.assertEquals(70, result.get(ParkingService.CAR_TOTAL));
    }

    @Test
    public void shouldReturnNullVacantWhenProjectHasAvailableRuleButNoIotValue() {
        ParkingServiceImpl spy = this.spyWithGridStatistics();
        this.stubRuleInfoCodes(false, Collections.singleton(InfoCodeEnum.AVAILABLE_PARKING_SPACE_NUMBER.value()));
        this.stubIotValues(this.parkingStatusValues(), null, null);

        Map<String, Integer> result = spy.getParkingSpaceStatistics(this.projectParam());

        Assertions.assertTrue(result.containsKey(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()));
        Assertions.assertNull(result.get(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()));
    }

    @Test
    public void shouldOmitVacantWhenProjectHasNoAvailableRule() {
        ParkingServiceImpl spy = this.spyWithGridStatistics();
        this.stubRuleInfoCodes(false, Collections.emptySet());
        this.stubIotValues(this.parkingStatusValues(), null, null);

        Map<String, Integer> result = spy.getParkingSpaceStatistics(this.projectParam());

        Assertions.assertFalse(result.containsKey(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()));
        Assertions.assertEquals(2, result.get(ParkingSpaceTypeEnum.PARKING_SPACE.value()));
    }

    @Test
    public void shouldReturnProjectInfoCodeStatisticsForProjectWithoutParkingSpace() {
        // 无停车空间：网格统计为空，车位类型统计不返回，项目对象信息点路径仍生效
        ParkingServiceImpl spy = Mockito.spy(service);
        Mockito.doReturn(Collections.emptyList()).when(spy).getParkingSpaceGridStatistics(Mockito.any(), Mockito.eq(false));
        Mockito.when(dtpRwdClientService.listSpace(Mockito.eq(false), Mockito.eq(false), Mockito.eq(PROJECT_ID), Mockito.any()))
                .thenReturn(Collections.emptyList());
        Set<String> projectRuleInfoCodes = new HashSet<>(Arrays.asList(
                InfoCodeEnum.AVAILABLE_PARKING_SPACE_NUMBER.value(), InfoCodeEnum.CAR_NUM.value()));
        this.stubRuleInfoCodes(false, projectRuleInfoCodes);
        this.stubIotValues(Collections.emptyList(), 25, 70);

        Map<String, Integer> result = spy.getParkingSpaceStatistics(this.projectParam());

        Assertions.assertFalse(result.containsKey(ParkingSpaceTypeEnum.PARKING_SPACE.value()));
        Assertions.assertEquals(25, result.get(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()));
        Assertions.assertEquals(70, result.get(ParkingService.CAR_TOTAL));
    }

    @Test
    public void shouldKeepFloorStatisticsUnchanged() {
        // FLOOR 级：不触发实例化规则逻辑，仍基于 CFPKPS parkingStatus 聚合
        this.stubParkingSpaceLink();
        this.stubRuleInfoCodes(true, Collections.emptySet());
        this.stubIotValues(this.parkingStatusValues(), null, null);
        // 异常事件统计依赖运行监测策略 Bean（非 Spring 测试环境），stub 为 0
        ParkingServiceImpl spy = Mockito.spy(service);
        Mockito.doReturn(0).when(spy).getExceptionCountByIds(Mockito.anySet());

        Map<String, Integer> result = spy.getParkingSpaceStatistics(this.floorParam());

        Assertions.assertEquals(2, result.get(ParkingSpaceTypeEnum.PARKING_SPACE.value()));
        Assertions.assertEquals(1, result.get(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()));
        Assertions.assertFalse(result.containsKey(ParkingService.CAR_TOTAL));
        Mockito.verify(abstractIotDataCacheServer, Mockito.never())
                .mapObjectIdAndExistsRuleInfoCodes(Mockito.anyMap(), Mockito.anyCollection());
    }

    // ------------------------- 测试辅助 -------------------------

    private ParkingServiceImpl spyWithGridStatistics() {
        return this.spyWithGridStatistics(this.buildGridStatistics(1));
    }

    private ParkingServiceImpl spyWithGridStatistics(List<ParkingSpaceGridStatisticsVO> gridStatistics) {
        ParkingServiceImpl spy = Mockito.spy(service);
        Mockito.doReturn(gridStatistics).when(spy).getParkingSpaceGridStatistics(Mockito.any(), Mockito.eq(false));
        // 供 getProjectParkingSpaceIds 使用的停车空间与车位链路
        this.stubParkingSpaceLink();
        return spy;
    }

    /**
     * 停车空间与车位查询链路：1 个停车空间下 2 个停车位（与网格统计口径一致）
     */
    private void stubParkingSpaceLink() {
        DtpRwdSpaceDTO space = new DtpRwdSpaceDTO();
        space.setId("space-1");
        space.setLocalName("停车区");
        space.setFloorId(FLOOR_ID);
        Mockito.when(dtpRwdClientService.listSpace(Mockito.eq(false), Mockito.eq(false), Mockito.eq(PROJECT_ID), Mockito.any()))
                .thenReturn(Collections.singletonList(space));

        DtpRwdEquipmentDTO parkingSpace1 = new DtpRwdEquipmentDTO();
        parkingSpace1.setId("parking-space-1");
        parkingSpace1.setRelObjIds(Collections.singleton("space-1"));
        DtpRwdEquipmentDTO parkingSpace2 = new DtpRwdEquipmentDTO();
        parkingSpace2.setId("parking-space-2");
        parkingSpace2.setRelObjIds(Collections.singleton("space-1"));
        Mockito.when(dtpRwdClientService.listEquipmentInBySpaceIds(Mockito.anySet(), Mockito.any()))
                .thenReturn(Arrays.asList(parkingSpace1, parkingSpace2));
        // 充电桩查询（楼层统计内部）
        Mockito.when(dtpRwdClientService.listObject(Mockito.any())).thenReturn(Collections.emptyList());
    }

    /**
     * 实例化规则 stub：CFPKPS 与项目对象按 classCode 区分返回
     *
     * @param cfpkpsHasRule    停车位（CFPKPS）是否存在 parkingStatus 规则
     * @param projectRuleInfoCodes 项目对象存在规则的信息点集合
     */
    private void stubRuleInfoCodes(boolean cfpkpsHasRule, Set<String> projectRuleInfoCodes) {
        Mockito.when(abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(Mockito.anyMap(), Mockito.anyCollection()))
                .thenAnswer(invocation -> {
                    Map<String, Set<String>> classCodeAndObjectIds = invocation.getArgument(0);
                    if (classCodeAndObjectIds.containsKey(DtpRwdClassCodeEnum.CFPKPS.value())) {
                        if (!cfpkpsHasRule) {
                            return Collections.emptyMap();
                        }
                        return Collections.singletonMap("parking-space-1",
                                Collections.singleton(InfoCodeEnum.PARKING_STATUS.value()));
                    }
                    if (classCodeAndObjectIds.containsKey(DtpRwdClassCodeEnum.PROJECT.value())) {
                        if (projectRuleInfoCodes.isEmpty()) {
                            return Collections.emptyMap();
                        }
                        return Collections.singletonMap(PROJECT_ID, projectRuleInfoCodes);
                    }
                    return Collections.emptyMap();
                });
    }

    /**
     * IOT 取值 stub：按信息点区分返回（停车位 parkingStatus / 项目对象 availableParkingSpaceNumber / carNum）
     */
    private void stubIotValues(List<InfoCodeValueVO> parkingStatusValues, Integer availableValue, Integer carNumValue) {
        Mockito.when(abstractIotDataCacheServer.listInfoCodeValueInOneHour(Mockito.anyCollection(), Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<String> infoCodes = invocation.getArgument(1);
                    if (infoCodes.contains(InfoCodeEnum.PARKING_STATUS.value())) {
                        return parkingStatusValues;
                    }
                    if (infoCodes.contains(InfoCodeEnum.AVAILABLE_PARKING_SPACE_NUMBER.value())) {
                        return availableValue == null ? Collections.emptyList() : Collections.singletonList(projectValue(availableValue));
                    }
                    if (infoCodes.contains(InfoCodeEnum.CAR_NUM.value())) {
                        return carNumValue == null ? Collections.emptyList() : Collections.singletonList(projectValue(carNumValue));
                    }
                    return Collections.emptyList();
                });
    }

    /**
     * 停车位 parkingStatus 取值：1 个"无车"（value=0），1 个有车（value=1）
     */
    private List<InfoCodeValueVO> parkingStatusValues() {
        return Arrays.asList(
                new InfoCodeValueVO().setObjectId("parking-space-1").setInfoCodeValue(ParkingSpaceStatusTestConst.VACANT),
                new InfoCodeValueVO().setObjectId("parking-space-2").setInfoCodeValue(ParkingSpaceStatusTestConst.NOT_VACANT));
    }

    private InfoCodeValueVO projectValue(int value) {
        return new InfoCodeValueVO().setObjectId(PROJECT_ID).setInfoCodeValue(value);
    }

    /**
     * 网格统计结果：1 建筑 1 楼层，车位总数 2
     *
     * @param vacantParkingSpaceCount 空闲车位统计数（模拟 IOT parkingStatus 聚合结果，0 表示无 IOT 数据）
     */
    private List<ParkingSpaceGridStatisticsVO> buildGridStatistics(int vacantParkingSpaceCount) {
        FloorParkingSpaceStatisticVO floor = new FloorParkingSpaceStatisticVO();
        floor.setFloorId(FLOOR_ID);
        floor.setFloorName("F1");
        floor.setParkIngSpaceCount(2);
        floor.setVacantParkIngSpaceCount(vacantParkingSpaceCount);
        floor.setChargingParkingSpaceCount(0);
        floor.setVacantChargingParkingSpace(0);
        floor.setSpaceParkIngSpaceStatistics(new ArrayList<>());
        ParkingSpaceGridStatisticsVO building = new ParkingSpaceGridStatisticsVO();
        building.setBuildingId("building-1");
        building.setBuildingName("B1");
        building.setFloorParkingSpaceStatistics(Collections.singletonList(floor));
        return Collections.singletonList(building);
    }

    private ParkingSpaceStatisticsQueryParam projectParam() {
        ParkingSpaceStatisticsQueryParam param = new ParkingSpaceStatisticsQueryParam();
        param.setInstanceId(PROJECT_ID);
        param.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        return param;
    }

    private ParkingSpaceStatisticsQueryParam floorParam() {
        ParkingSpaceStatisticsQueryParam param = new ParkingSpaceStatisticsQueryParam();
        param.setInstanceId(FLOOR_ID);
        param.setInstanceType(DemandInstanceTypeEnum.FLOOR);
        return param;
    }

    /**
     * 停车状态测试常量（与 ParkingSpaceStatusEnum 值一致，避免枚举 i18n 依赖）
     */
    private static final class ParkingSpaceStatusTestConst {
        private static final String VACANT = "0";
        private static final String NOT_VACANT = "1";
    }
}
