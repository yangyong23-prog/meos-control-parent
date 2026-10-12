package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.persagy.apccontrol.basic.convert.LightingCircuitConvert;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingCircuitDetailDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 照明运行监测新缓存路由测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class RunningMonitorLightingStrategyTest {

    /**
     * 验证 READY 请求未指定二级类型时按固定顺序读取全部五个新缓存键。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReadAllLightingCacheKeysWhenReadyAndSecondTypesAreMissing() {
        DemandLightingStrategy demandLightingStrategy =
                Mockito.mock(DemandLightingStrategy.class);
        DtpRwdClientService rwdClientService = Mockito.mock(DtpRwdClientService.class);
        LightingCircuitConvert circuitConvert = Mockito.mock(LightingCircuitConvert.class);
        DtpKnowledgeClientService knowledgeClientService =
                Mockito.mock(DtpKnowledgeClientService.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        CacheObjectRepository cacheObjectRepository =
                Mockito.mock(CacheObjectRepository.class);
        CacheObjectRelRepository relationRepository =
                Mockito.mock(CacheObjectRelRepository.class);
        RunningMonitorLightingStrategy strategy = new RunningMonitorLightingStrategy(
                demandLightingStrategy, rwdClientService, circuitConvert,
                knowledgeClientService, statusService, cacheObjectRepository, relationRepository);

        List<RunningMonitorSecondTypeEnum> allSecondTypes = Arrays.asList(
                RunningMonitorSecondTypeEnum.PUBLIC_AREA_LIGHTING,
                RunningMonitorSecondTypeEnum.PARKING_LOT_LIGHTING,
                RunningMonitorSecondTypeEnum.BUILDING_LIGHTING,
                RunningMonitorSecondTypeEnum.LANDSCAPE_LIGHTING,
                RunningMonitorSecondTypeEnum.SIGN_LIGHTING);
        List<String> expectedCacheKeys = new ArrayList<>();
        Map<String, List<LightingCircuitDetailDTO>> cachedCircuits = new LinkedHashMap<>();
        for (RunningMonitorSecondTypeEnum secondType : allSecondTypes) {
            String cacheKey = CacheObjectModelKeyConfig
                    .LIGHTING_LIST_EQUIPMENT_BY_SECOND_TYPE.get(secondType);
            expectedCacheKeys.add(cacheKey);
            cachedCircuits.put(cacheKey, Collections.emptyList());
        }
        Mockito.when(statusService.isReady(RunningMonitorTypeEnum.LIGHTING))
                .thenReturn(true);
        Mockito.when(cacheObjectRepository.mapByCacheKeys(
                        Mockito.anyCollection(), Mockito.eq(LightingCircuitDetailDTO.class),
                        Mockito.anyCollection()))
                .thenReturn(cachedCircuits);

        RunningMonitorEquipmentQueryParam param =
                Mockito.mock(RunningMonitorEquipmentQueryParam.class);
        Mockito.when(param.isMysqlCache()).thenReturn(true);
        List<LightingCircuitDetailDTO> result = strategy.listEquipments(param);

        Assertions.assertTrue(result.isEmpty());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> cacheKeyCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(cacheObjectRepository).mapByCacheKeys(
                cacheKeyCaptor.capture(), Mockito.eq(LightingCircuitDetailDTO.class),
                Mockito.anyCollection());
        Assertions.assertEquals(expectedCacheKeys, new ArrayList<>(cacheKeyCaptor.getValue()));
        Mockito.verifyNoInteractions(demandLightingStrategy, rwdClientService);
    }
}
