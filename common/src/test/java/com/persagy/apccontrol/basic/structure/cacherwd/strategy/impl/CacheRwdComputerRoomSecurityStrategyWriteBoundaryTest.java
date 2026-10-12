package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheIotRuleRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRuleRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorComputerRoomSecurityStrategy;
import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 机房安防完整构建的对象模型写入字段边界测试。
 *
 * <p>机房按楼层空间关系查询全量设备且无法限定 classCode，验证其查询字段为全量登记
 * 并集加关系字段；字段子集会在 UPSERT 整列覆盖时丢失其他策略写入的共享对象字段。</p>
 *
 * @author JiYinchuan
 * @since v7.260802.3
 */
public class CacheRwdComputerRoomSecurityStrategyWriteBoundaryTest {

    /**
     * 原始 Spring 上下文，测试后恢复。
     *
     * <p>机房策略引用的查询参数基类在构造时从 SpringUtil 获取物理世界服务，
     * 测试需注册隔离的静态上下文。</p>
     *
     * @since v7.260802.3
     */
    private static ApplicationContext originalApplicationContext;

    /**
     * 注册查询参数基类构造所需的 Bean。
     *
     * @since v7.260802.3
     */
    @BeforeAll
    public static void setUpSpringUtil() {
        originalApplicationContext = SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("dtpRwdClientService",
                Mockito.mock(DtpRwdClientService.class));
        applicationContext.getBeanFactory().registerSingleton("abstractIotDataCacheServer",
                Mockito.mock(AbstractIotDataCacheServer.class));
        applicationContext.getBeanFactory().registerSingleton("dtpRwdConvert",
                Mockito.mock(DtpRwdConvert.class));
        applicationContext.refresh();
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * 恢复原始 Spring 上下文。
     *
     * @since v7.260802.3
     */
    @AfterAll
    public static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(originalApplicationContext);
    }

    /**
     * 清理项目上下文。
     *
     * @since v7.260802.3
     */
    @AfterEach
    public void tearDown() {
        ContextUtil.unloadContext();
    }

    /**
     * 验证楼层全量设备查询使用全量登记字段并集与 relObjIds，而非手写字段子集。
     *
     * @since v7.260802.3
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldQueryFullFieldUnionAndRelObjIdsForFloorAllDevices() {
        ContextUtil.setProjectId("project-1");
        RunningMonitorComputerRoomSecurityStrategy sourceStrategy =
                Mockito.mock(RunningMonitorComputerRoomSecurityStrategy.class);
        DtpRwdClientService rwdClient = Mockito.mock(DtpRwdClientService.class);
        CacheObjectModelWriteService writeService = Mockito.mock(CacheObjectModelWriteService.class);
        CacheObjectModelStatusService statusService =
                Mockito.mock(CacheObjectModelStatusService.class);
        AbstractIotDataCacheServer iotDataCacheServer =
                Mockito.mock(AbstractIotDataCacheServer.class);
        CacheRuleRepository cacheRuleRepository = Mockito.mock(CacheRuleRepository.class);
        CacheIotRuleRepository cacheIotRuleRepository = Mockito.mock(CacheIotRuleRepository.class);
        CacheRwdComputerRoomSecurityStrategy strategy = new CacheRwdComputerRoomSecurityStrategy(
                sourceStrategy, rwdClient, writeService, statusService,
                iotDataCacheServer, cacheRuleRepository, cacheIotRuleRepository);

        // 机房安全空间（含楼层），触发楼层全量空间与设备查询
        Mockito.when(sourceStrategy.listSpacesComputerRoomSecurityCacheSerial(
                        Mockito.any(), Mockito.anyInt()))
                .thenReturn(Collections.singletonList(space("crs-space-1", "floor-1")));
        // 楼层全量空间：单页小于分页上限，终止分页
        Mockito.when(rwdClient.listSpace(Mockito.any()))
                .thenReturn(Collections.singletonList(space("floor-space-1", "floor-1")));
        // 楼层全量设备：照明回路（历史缺陷中曾因字段子集被覆盖为仅 major）
        DtpRwdEquipmentDTO circuit = new DtpRwdEquipmentDTO();
        circuit.setId("circuit-1");
        circuit.setClassCode(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value());
        Mockito.when(sourceStrategy.listEquipmentsForCacheBuildParallel(
                        Mockito.any(), Mockito.anyCollection(), Mockito.anyInt()))
                .thenReturn(Collections.singletonList(circuit));
        Mockito.when(writeService.replaceModelCacheKeysAndRelations(
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);
        Mockito.when(iotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                        Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyMap());
        Mockito.when(cacheIotRuleRepository.cacheIotRuleType())
                .thenReturn(CacheIotRuleTypeEnum.PERSISTENCE);

        boolean result = strategy.buildCacheData(RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY);

        Assertions.assertTrue(result);
        Mockito.verify(statusService).markReady(RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY);

        ArgumentCaptor<Collection<String>> columnsCaptor =
                ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(sourceStrategy).listEquipmentsForCacheBuildParallel(
                Mockito.any(), columnsCaptor.capture(), Mockito.anyInt());
        Set<String> withColumns = new LinkedHashSet<>(columnsCaptor.getValue());
        Assertions.assertTrue(withColumns.containsAll(
                CacheObjectExtensionFieldConfig.listAllRwdQueryFields()));
        Assertions.assertTrue(withColumns.containsAll(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(
                        DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value())));
        Assertions.assertTrue(withColumns.containsAll(Arrays.asList(
                DtpRwdObjectDTO.Fields.relObjIds,
                InfoCodeEnum.MAJOR.getCode(),
                DtpRwdEquipmentDTO.Fields.bimId,
                DtpRwdEquipmentDTO.Fields.bimLocation)));
        Mockito.verify(cacheRuleRepository).replaceRules(
                Mockito.eq(CacheObjectModelKeyConfig.COMPUTER_ROOM_SECURITY_LIST_EQUIPMENT),
                Mockito.any());
    }

    /**
     * 创建空间测试对象。
     *
     * @param id      空间 ID
     * @param floorId 楼层 ID
     * @return 空间
     * @since v7.260802.3
     */
    private static DtpRwdSpaceDTO space(String id, String floorId) {
        DtpRwdSpaceDTO result = new DtpRwdSpaceDTO();
        result.setId(id);
        result.setFloorId(floorId);
        result.setClassCode(DtpRwdClassCodeEnum.SPACE.value());
        return result;
    }
}
