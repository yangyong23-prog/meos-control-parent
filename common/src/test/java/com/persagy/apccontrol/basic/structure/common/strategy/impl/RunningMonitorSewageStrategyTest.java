package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.vo.sewage.SewageEquipVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 污水处理运行监测策略回归测试。
 *
 * @author Qoder
 * @since v7.260901.0
 */
public class RunningMonitorSewageStrategyTest {

    private DtpRwdClientService dtpRwdClientService;
    private RunningMonitorSewageStrategy strategy;

    @BeforeEach
    void setUp() {
        ContextUtil.setProjectId("project-1");
        dtpRwdClientService = Mockito.mock(DtpRwdClientService.class);
        strategy = new RunningMonitorSewageStrategy(dtpRwdClientService);
    }

    @AfterEach
    void tearDown() {
        ContextUtil.unloadContext();
    }

    /**
     * FLOOR 实例场景下，修复前主查询 listEquipmentInByFloorIds 会向同一入参写入
     * relationFrom(EQUIPMENT_IN_FLOOR)，该残留会通过副本传播至 listSubmersibleSewagePumpByEquipmentIds，
     * 导致 relationFrom 与 relationTo 同时下发。
     * <p>
     * 修复后关系子查询参数由 updateRelatedPumps 独立构造，不应携带任何 relationFrom/relationTo 残留。
     */
    @Test
    void shouldNotPropagateRelationFromWhenFloorInstance() {
        // ----- 构造 FLOOR 实例 -----
        DemandInstanceValueObject instance = new DemandInstanceValueObject();
        instance.setInstanceType(DemandInstanceTypeEnum.FLOOR);
        instance.setInstanceId("floor-1");

        // ----- 模拟主查询：listEquipmentInByFloorIds（模仿真实行为：直接修改入参的 relationFrom） -----
        DtpRwdEquipmentDTO wsstluDto = new DtpRwdEquipmentDTO();
        wsstluDto.setId("wsstlu-1");
        wsstluDto.setClassCode(DtpRwdClassCodeEnum.WSSTLU.getValue());
        wsstluDto.setFloorId("floor-1");
        wsstluDto.setBuildingId("building-1");

        Mockito.when(dtpRwdClientService.listEquipmentInByFloorIds(
                        Mockito.anyCollection(), Mockito.any(DtpRwdObjectQueryParam.class)))
                .thenAnswer(invocation -> {
                    // 模仿 DtpRwdClientServiceImpl.listEquipmentInByFloorIds 的入参污染行为
                    DtpRwdObjectQueryParam param = invocation.getArgument(1);
                    param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.EQUIPMENT_IN_FLOOR)
                            .setObjTo(Collections.singletonList(invocation.getArgument(0))));
                    return Collections.singletonList(wsstluDto);
                });

        // 其他辅助查询（listSpaceInByEquipmentId / listFloor / listBuilding / listLocationInsideByEquipmentIds）
        // Mockito 默认对 List/Collection 返回 emptyList, 无需逐一 stub。

        // ----- 执行（FLOOR 分支） -----
        List<SewageEquipVO> result = strategy.listEquipments(
                RunningMonitorTypeEnum.SEWAGE, instance);

        // ----- 验证主查询结果 -----
        assertFalse(result.isEmpty(), "应返回主查询设备列表");
        assertEquals("wsstlu-1", result.get(0).getId());

        // ----- 捕获 listSubmersibleSewagePumpByEquipmentIds 的参数 -----
        ArgumentCaptor<DtpRwdObjectQueryParam> relationParamCaptor =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(dtpRwdClientService, Mockito.atLeastOnce())
                .listSubmersibleSewagePumpByEquipmentIds(
                        Mockito.anyBoolean(), Mockito.anyString(),
                        Mockito.anyCollection(), relationParamCaptor.capture());

        // 取第一次调用的参数（wsstlu 场景）
        DtpRwdObjectQueryParam capturedParam = relationParamCaptor.getAllValues().get(0);
        assertNotNull(capturedParam, "传入 listSubmersibleSewagePumpByEquipmentIds 的参数不应为空");

        // 核心断言：无残留 relationFrom（修复前：RESIDUAL EQUIPMENT_IN_FLOOR；修复后：null）
        assertNull(capturedParam.getRelationFrom(),
                "修复后关系查询参数不应携带主查询的 relationFrom 残留");

        // 调用端不应预设 relationTo（由被调服务方法内部设置）
        assertNull(capturedParam.getRelationTo(),
                "修复后关系查询参数不应由调用端预设 relationTo");

        // 关键字段保持等价性
        assertNotNull(capturedParam.getClassCode(), "classCode 不应为空");
        assertTrue(capturedParam.getClassCode().contains(DtpRwdClassCodeEnum.WSSTSP.getValue()),
                "classCode 应包含 WSSTSP");
        assertNotNull(capturedParam.getInfoCodes(), "infoCodes 不应为空");
        assertTrue(capturedParam.getInfoCodes().containsKey(CommonConst.MONITORING_SURVEILLANCE_STATUS),
                "infoCodes 应包含 MONITORING_SURVEILLANCE_STATUS");
        assertNotNull(capturedParam.getWithColumns(), "withColumns 不应为空");
    }
}