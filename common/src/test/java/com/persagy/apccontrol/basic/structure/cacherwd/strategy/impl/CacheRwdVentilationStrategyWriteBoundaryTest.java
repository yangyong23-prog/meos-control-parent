package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorVentilationStrategy;
import org.junit.jupiter.api.Assertions;
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
 * 通风完整构建的对象模型写入字段边界测试。
 *
 * <p>验证空间与设备查询字段均基于对应 classCode 的白名单并集组装，附加字段
 * （name、relObjIds）单独追加；防止回退为手写字段子集导致共享对象行丢字段。</p>
 *
 * @author JiYinchuan
 * @since v7.260802.3
 */
public class CacheRwdVentilationStrategyWriteBoundaryTest {

    /**
     * 通风固定五类设备。
     *
     * @since v7.260802.3
     */
    private static final List<DtpRwdClassCodeEnum> VENTILATION_CLASS_CODES = Arrays.asList(
            DtpRwdClassCodeEnum.FFSCMF,
            DtpRwdClassCodeEnum.FFSCAF,
            DtpRwdClassCodeEnum.ACVTSF,
            DtpRwdClassCodeEnum.CFCSWN,
            DtpRwdClassCodeEnum.CFCSEW);

    /**
     * 验证空间与设备查询均使用白名单并集字段，附加字段单独追加。
     *
     * @since v7.260802.3
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldQueryFullFieldUnionForSpacesAndEquipments() {
        RunningMonitorVentilationStrategy sourceStrategy =
                Mockito.mock(RunningMonitorVentilationStrategy.class);
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        CacheObjectModelWriteService writeService = Mockito.mock(CacheObjectModelWriteService.class);
        CacheRwdVentilationStrategy strategy = new CacheRwdVentilationStrategy(
                sourceStrategy, rwdClient, writeService);

        Mockito.when(rwdClient.listSpace(Mockito.any()))
                .thenReturn(Collections.singletonList(space("dome-space-1")));
        DtpRwdEquipmentDTO fan = new DtpRwdEquipmentDTO();
        fan.setId("fan-1");
        fan.setClassCode(DtpRwdClassCodeEnum.ACVTSF.value());
        fan.setRelObjIds(new LinkedHashSet<>(Collections.singleton("dome-space-1")));
        Mockito.when(sourceStrategy.listEquipmentsForCacheBuild(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(fan));
        Mockito.when(writeService.replaceModelCacheKeysAndRelations(
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);

        boolean result = strategy.buildCacheData(RunningMonitorTypeEnum.VENTILATION);

        Assertions.assertTrue(result);
        ArgumentCaptor<DtpRwdObjectQueryParam> spaceQueryCaptor =
                ArgumentCaptor.forClass(DtpRwdObjectQueryParam.class);
        Mockito.verify(rwdClient).listSpace(spaceQueryCaptor.capture());
        Set<String> spaceWithColumns =
                new LinkedHashSet<>(spaceQueryCaptor.getValue().getWithColumns());
        Assertions.assertTrue(spaceWithColumns.containsAll(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(
                        DtpRwdClassCodeEnum.SPACE.value())));
        Assertions.assertTrue(spaceWithColumns.contains(DtpRwdObjectDTO.Fields.name));

        ArgumentCaptor<Set<String>> columnsCaptor =
                ArgumentCaptor.forClass(Set.class);
        Mockito.verify(sourceStrategy).listEquipmentsForCacheBuild(
                Mockito.any(), columnsCaptor.capture());
        Set<String> equipmentWithColumns = new LinkedHashSet<>(columnsCaptor.getValue());
        Set<String> expectedFields = new LinkedHashSet<>();
        for (DtpRwdClassCodeEnum classCode : VENTILATION_CLASS_CODES) {
            expectedFields.addAll(
                    CacheObjectExtensionFieldConfig.listRwdQueryFields(classCode.value()));
        }
        Assertions.assertTrue(equipmentWithColumns.containsAll(expectedFields));
        Assertions.assertTrue(equipmentWithColumns.containsAll(Arrays.asList(
                DtpRwdObjectDTO.Fields.name, DtpRwdObjectDTO.Fields.relObjIds)));
    }

    /**
     * 创建空间测试对象。
     *
     * @param id 空间 ID
     * @return 空间
     * @since v7.260802.3
     */
    private static DtpRwdSpaceDTO space(String id) {
        DtpRwdSpaceDTO result = new DtpRwdSpaceDTO();
        result.setId(id);
        result.setClassCode(DtpRwdClassCodeEnum.SPACE.value());
        return result;
    }
}
