package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * 公共项目结构缓存策略测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class CacheRwdProjectStructureStrategyTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldReplaceBuildingAndFloorWithoutBusinessKey() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        CacheObjectModelWriteService writeService =
                Mockito.mock(CacheObjectModelWriteService.class);
        CacheRwdProjectStructureStrategy strategy =
                new CacheRwdProjectStructureStrategy(rwdClient, writeService);
        DtpRwdBuildingDTO building = new DtpRwdBuildingDTO();
        building.setId("building-1");
        DtpRwdFloorDTO floor = new DtpRwdFloorDTO();
        floor.setId("floor-1");
        Mockito.when(rwdClient.listBuilding(Mockito.any()))
                .thenReturn(Collections.singletonList(building));
        Mockito.when(rwdClient.listFloor(Mockito.any()))
                .thenReturn(Collections.singletonList(floor));
        Mockito.when(writeService.replaceObjectClassCodes(
                        Mockito.anyCollection(), Mockito.anyCollection()))
                .thenReturn(true);

        Assertions.assertNull(strategy.runningMonitorType());
        Assertions.assertTrue(strategy.onlyBuildInCommonPhase());
        Assertions.assertTrue(strategy.buildCacheData(RunningMonitorTypeEnum.SECURITY));

        ArgumentCaptor<DtpRwdObjectQueryParam> buildingQuery =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(rwdClient).listBuilding(buildingQuery.capture());
        Assertions.assertEquals(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(
                        DtpRwdClassCodeEnum.BUILDING.value()),
                new LinkedHashSet<>(buildingQuery.getValue().getWithColumns()));
        Assertions.assertEquals(Boolean.TRUE, buildingQuery.getValue().getValid());

        ArgumentCaptor<DtpRwdObjectQueryParam> floorQuery =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(rwdClient).listFloor(floorQuery.capture());
        Assertions.assertEquals(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(
                        DtpRwdClassCodeEnum.FLOOR.value()),
                new LinkedHashSet<>(floorQuery.getValue().getWithColumns()));
        Assertions.assertEquals(Boolean.TRUE, floorQuery.getValue().getValid());

        ArgumentCaptor<Collection> classCodes = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection> objects = ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(writeService).replaceObjectClassCodes(
                classCodes.capture(), objects.capture());
        Assertions.assertEquals(
                Arrays.asList(
                        DtpRwdClassCodeEnum.BUILDING.value(),
                        DtpRwdClassCodeEnum.FLOOR.value()),
                classCodes.getValue());
        Assertions.assertEquals(
                Arrays.asList(building, floor),
                Arrays.asList(objects.getValue().toArray(new DtpRwdObjectDTO[0])));
        Mockito.verifyNoMoreInteractions(writeService);
    }

    @Test
    public void shouldReplaceEmptyProjectStructureSnapshot() {
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        CacheObjectModelWriteService writeService =
                Mockito.mock(CacheObjectModelWriteService.class);
        CacheRwdProjectStructureStrategy strategy =
                new CacheRwdProjectStructureStrategy(rwdClient, writeService);
        Mockito.when(rwdClient.listBuilding(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(rwdClient.listFloor(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(writeService.replaceObjectClassCodes(
                        Mockito.anyCollection(), Mockito.anyCollection()))
                .thenReturn(true);

        Assertions.assertTrue(strategy.buildCacheData(null));

        Mockito.verify(writeService).replaceObjectClassCodes(
                Mockito.eq(Arrays.asList(
                        DtpRwdClassCodeEnum.BUILDING.value(),
                        DtpRwdClassCodeEnum.FLOOR.value())),
                Mockito.argThat(Collection::isEmpty));
    }
}
