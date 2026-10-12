package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.BaseCacheRwdStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorVentilationStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物理世界对象缓存-通风策略实现
 *
 * @author wyz
 * @since 7.260401.0
 */
@Component(CacheRwdVentilationStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdVentilationStrategy extends BaseCacheRwdStrategy {

    public static final String BEAN_NAME = "cacheRwdVentilationStrategy";

    private final RunningMonitorVentilationStrategy runningMonitorVentilationStrategy;
    private final DtpRwdClientService dtpRwdClientService;

    /**
     * 新版对象模型写入服务，不修改历史缓存数据。
     */
    private final @NonNull CacheObjectModelWriteService cacheObjectModelWriteService;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.VENTILATION;
    }

    @Override
    public boolean buildCacheData(RunningMonitorTypeEnum runningMonitorType) {
        // 查询原始空间 DTO，查询列基于白名单并集组装，保证 bimId/bimLocation/outline 等投影字段完整
        Set<String> spaceWithColumns = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(DtpRwdClassCodeEnum.SPACE.value()));
        spaceWithColumns.add(DtpRwdObjectDTO.Fields.name);
        DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam()
                .setWithColumns(spaceWithColumns);
        List<DtpRwdSpaceDTO> spaceList = dtpRwdClientService.listSpace(spaceQueryParam);
        List<DtpRwdObjectDTO> spaces = spaceList.stream()
                .map(space -> (DtpRwdObjectDTO) space)
                .collect(Collectors.toList());

        // 查询原始设备 DTO（含 filtrationId 过滤）
        Set<String> spaceIds = spaceList.stream()
                .map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
        // 设备查询列取 FFSCMF/FFSCAF/ACVTSF/CFCSWN/CFCSEW 各类白名单并集，多查的列在投影阶段丢弃，无副作用
        Set<String> equipmentWithColumns = new LinkedHashSet<>();
        equipmentWithColumns.addAll(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(DtpRwdClassCodeEnum.FFSCMF.value()));
        equipmentWithColumns.addAll(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(DtpRwdClassCodeEnum.FFSCAF.value()));
        equipmentWithColumns.addAll(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(DtpRwdClassCodeEnum.ACVTSF.value()));
        // PRD-290902: 窗类设备扩展字段白名单（无扩展字段时为基本字段集）
        equipmentWithColumns.addAll(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(DtpRwdClassCodeEnum.CFCSWN.value()));
        equipmentWithColumns.addAll(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(DtpRwdClassCodeEnum.CFCSEW.value()));
        equipmentWithColumns.add(DtpRwdObjectDTO.Fields.name);
        equipmentWithColumns.add(DtpRwdObjectDTO.Fields.relObjIds);
        List<DtpRwdEquipmentDTO> equipments = runningMonitorVentilationStrategy
                .listEquipmentsForCacheBuild(spaceIds, equipmentWithColumns);

        // 按设备类分离风机设备和窗类设备，用于不同关系类型写入
        Set<String> fanClassCodes = Sets.newHashSet(
                DtpRwdClassCodeEnum.FFSCMF.value(),
                DtpRwdClassCodeEnum.FFSCAF.value(),
                DtpRwdClassCodeEnum.ACVTSF.value());
        Set<String> windowClassCodes = Sets.newHashSet(
                DtpRwdClassCodeEnum.CFCSWN.value(),
                DtpRwdClassCodeEnum.CFCSEW.value());

        // 风机设备通过 relObjIds (EQUIPMENT_SERVICE_SPACE) 保留有服务的空间
        Set<String> fanServedSpaceIds = equipments.stream()
                .filter(eq -> fanClassCodes.contains(eq.getClassCode())
                        && CollectionUtils.isNotEmpty(eq.getRelObjIds()))
                .flatMap(eq -> eq.getRelObjIds().stream())
                .collect(Collectors.toSet());
        // PRD-290902: 窗类设备通过 relObjIds (EQUIPMENT_IN_SPACE) 保留所在穹顶空间
        Set<String> windowServedSpaceIds = equipments.stream()
                .filter(eq -> windowClassCodes.contains(eq.getClassCode())
                        && CollectionUtils.isNotEmpty(eq.getRelObjIds()))
                .flatMap(eq -> eq.getRelObjIds().stream())
                .collect(Collectors.toSet());
        Set<String> servedSpaceIds = new java.util.HashSet<>(fanServedSpaceIds);
        servedSpaceIds.addAll(windowServedSpaceIds);
        spaces = spaces.stream()
                .filter(space -> servedSpaceIds.contains(space.getId()))
                .collect(Collectors.toList());

        // 同一个短事务内原子写入空间 Key、设备 Key 和设备-空间关系
        String spaceKey = CacheObjectModelKeyConfig.VENTILATION_LIST_SPACE;
        String equipmentKey = CacheObjectModelKeyConfig.VENTILATION_LIST_EQUIPMENT;
        Map<String, Collection<? extends DtpRwdObjectDTO>> sourcesByKey = new LinkedHashMap<>();
        sourcesByKey.put(spaceKey, spaces);
        sourcesByKey.put(equipmentKey, equipments);
        // PRD-290902: 按设备类分离风机和窗类，分别存入不同关系类型
        List<DtpRwdEquipmentDTO> fanEquipments = equipments.stream()
                .filter(eq -> fanClassCodes.contains(eq.getClassCode()))
                .collect(Collectors.toList());
        List<DtpRwdEquipmentDTO> windowEquipments = equipments.stream()
                .filter(eq -> windowClassCodes.contains(eq.getClassCode()))
                .collect(Collectors.toList());
        Map<DtpRwdRelationTypeEnum, Collection<? extends DtpRwdObjectDTO>> relationsByType
                = new LinkedHashMap<>();
        relationsByType.put(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE, fanEquipments);
        relationsByType.put(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE, windowEquipments);
        return this.cacheObjectModelWriteService.replaceModelCacheKeysAndRelations(
                sourcesByKey, Collections.emptyList(), relationsByType, () -> true);
    }

    @Override
    public String logTag() {
        return "物理世界缓存-通风策略";
    }
}
