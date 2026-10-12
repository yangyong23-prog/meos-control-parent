package com.persagy.apccontrol.basic.sse.strategy.common;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.dashboard.AbnormalStateParam;
import com.persagy.apccontrol.basic.service.DashBoardService;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 异常状态 SSE 对象范围聚合测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class AbnormalStateAggregationTest {

    /**
     * 验证空间和多设备类范围分别去重后只发起一次异常状态查询。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldAggregateDistinctSpaceAndEquipmentIdsOnce() {
        DashBoardService dashboardService = Mockito.mock(DashBoardService.class);
        Mockito.when(dashboardService.listAbnormalState(Mockito.any()))
                .thenReturn(Collections.emptyMap());
        TestAbnormalState strategy = new TestAbnormalState();
        strategy.dashBoardService = dashboardService;
        Map<String, Set<String>> idsByClassCode = new LinkedHashMap<>();
        idsByClassCode.put(DtpRwdClassCodeEnum.SPACE.value(),
                new LinkedHashSet<>(Arrays.asList("space-1", "space-2")));
        idsByClassCode.put(DtpRwdClassCodeEnum.CFCSDR.value(),
                new LinkedHashSet<>(Arrays.asList("equipment-1", "equipment-2")));
        idsByClassCode.put(DtpRwdClassCodeEnum.SPIACU.value(),
                new LinkedHashSet<>(Arrays.asList("equipment-2", "equipment-3")));
        SseSubscribeParam param = new SseSubscribeParam();
        param.setSubscribeType(SubscribeTypeEnum.SECURITY);
        param.setClaasCode2objectIds(idsByClassCode);

        SseEmitterObject result = strategy.call(param);

        ArgumentCaptor<AbnormalStateParam> paramCaptor =
                ArgumentCaptor.forClass(AbnormalStateParam.class);
        Mockito.verify(dashboardService).listAbnormalState(paramCaptor.capture());
        Assertions.assertEquals(
                new LinkedHashSet<>(Arrays.asList("space-1", "space-2")),
                new LinkedHashSet<>(paramCaptor.getValue().getSpaceIds()));
        Assertions.assertEquals(
                new LinkedHashSet<>(Arrays.asList(
                        "equipment-1", "equipment-2", "equipment-3")),
                new LinkedHashSet<>(paramCaptor.getValue().getEquipmentIds()));
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Collections.emptyMap(), result.getData());
    }

    /**
     * 暴露模板受保护方法供纯单测调用。
     *
     * @author JiYinchuan
     * @since v7.260802.0
     */
    private static final class TestAbnormalState extends AbnormalState {

        private SseEmitterObject call(SseSubscribeParam param) {
            return super.getSseEmitterObject(param);
        }
    }
}
