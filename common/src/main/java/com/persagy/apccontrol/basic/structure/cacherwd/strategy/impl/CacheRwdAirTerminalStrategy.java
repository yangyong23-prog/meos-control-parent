package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalInstanceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalSpaceWithStatusQueryParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalSpaceWithStatusVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.BaseCacheRwdStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 物理世界对象缓存-空调末端策略实现
 *
 * @author wxf
 * @since 7.260302.0
 */
@Component(CacheRwdAirTerminalStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdAirTerminalStrategy extends BaseCacheRwdStrategy {

    public static final String BEAN_NAME = "cacheRwdAirTerminalStrategy";

    private final AirTerminalService airTerminalService;

    /**
     * 新版对象模型写入服务，不修改历史缓存数据。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelWriteService cacheObjectModelWriteService;

    /**
     * RWD 对象服务，用于在写事务外查询末端设备的完整关系快照。
     *
     * @since v7.260701.0
     */
    private final @NonNull DtpRwdClientService dtpRwdClientService;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.AIR_TERMINAL;
    }

    @Override
    public boolean buildCacheData(RunningMonitorTypeEnum runningMonitorType) {
        AirTerminalSpaceWithStatusQueryParam queryParam = new AirTerminalSpaceWithStatusQueryParam();
        queryParam.setProjectId(ContextUtil.getProjectId());
        queryParam.setInstanceType(AirTerminalInstanceTypeEnum.PROJECT);
        queryParam.setInstanceId(ContextUtil.getProjectId());
        AirTerminalSpaceWithStatusVO spaceWithStatusVO =
                this.airTerminalService.listFullSpaceEquipmentForCacheBuild(queryParam);
        // 末端已经切换到新版对象模型，构建链路不再读写历史缓存表。
        return this.cacheNewObjectModel(spaceWithStatusVO);
    }

    /**
     * 按同一次空间设备查询范围构建新版对象模型缓存。
     *
     * <p>空间设备查询按目标 classCode 字段配置取得完整 RWD 快照，调用方在事务外完成
     * 三类关系查询。对象 Key 成功替换后，再通过独立本地事务提交关系快照。</p>
     *
     * @param spaceWithStatusVO 空间设备查询结果
     * @return 空间和设备两个新版缓存 Key 是否均替换成功
     * @since v7.260701.0
     */
    private boolean cacheNewObjectModel(
            @Nullable AirTerminalSpaceWithStatusVO spaceWithStatusVO) {
        List<DtpRwdObjectDTO> spaces;
        List<DtpRwdObjectDTO> equipments;
        if (spaceWithStatusVO == null) {
            this.logProjectInstance().debug("末端空间和设备均为空，按空结果清理新版缓存");
            spaces = Collections.emptyList();
            equipments = Collections.emptyList();
        } else {
            spaces = new ArrayList<>(spaceWithStatusVO.getRwdSpaces());
            equipments = new ArrayList<>(spaceWithStatusVO.getRwdEquipments());
        }

        Set<String> spaceIds = spaces.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> equipmentIds = equipments.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> acatahIds = equipments.stream()
                .filter(item -> StringUtils.equals(
                        DtpRwdClassCodeEnum.ACATAH.value(), item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> acahgIds = equipments.stream()
                .filter(item -> StringUtils.equals(
                        DtpRwdClassCodeEnum.ACAHG_GROUP.value(), item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        CompletableFuture<List<DtpRwdObjectDTO>> serviceSpaceFuture =
                CompletableFuture.supplyAsync(
                        () -> this.listRelationFromObjects(
                                equipmentIds, spaceIds,
                                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE),
                        BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture<List<DtpRwdObjectDTO>> inSpaceFuture =
                CompletableFuture.supplyAsync(
                        () -> this.listRelationFromObjects(
                                equipmentIds, spaceIds,
                                DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE),
                        BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture<List<DtpRwdObjectDTO>> groupEquipmentFuture =
                CompletableFuture.supplyAsync(
                        () -> this.listRelationFromObjects(
                                acahgIds, acatahIds,
                                DtpRwdRelationTypeEnum.EQUIP_IN_GROUP),
                        BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture.allOf(
                serviceSpaceFuture, inSpaceFuture, groupEquipmentFuture).join();

        String spaceKey = CacheObjectModelKeyConfig.AIR_TERMINAL_LIST_SPACE;
        String equipmentKey = CacheObjectModelKeyConfig.AIR_TERMINAL_LIST_EQUIPMENT;
        Map<String, Collection<? extends DtpRwdObjectDTO>> sourcesByKey = new LinkedHashMap<>();
        sourcesByKey.put(spaceKey, spaces);
        sourcesByKey.put(equipmentKey, equipments);
        Map<DtpRwdRelationTypeEnum, Collection<? extends DtpRwdObjectDTO>> relationsByType
                = new LinkedHashMap<>();
        relationsByType.put(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE,
                serviceSpaceFuture.join());
        relationsByType.put(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE,
                inSpaceFuture.join());
        relationsByType.put(DtpRwdRelationTypeEnum.EQUIP_IN_GROUP,
                groupEquipmentFuture.join());
        return this.cacheObjectModelWriteService.replaceModelCacheKeysAndRelations(
                sourcesByKey, Collections.emptyList(), relationsByType, () -> true);
    }

    /**
     * 查询并规范化一类设备正向关系。
     *
     * <p>每个请求主体都保留一条 DTO；RWD 未命中或返回空目标时，{@code relObjIds}
     * 保持为空，供写服务明确清理旧关系。RWD 返回的目标同时限制在本次请求范围内。</p>
     *
     * @param subjectIds 关系主体 ID
     * @param targetIds 允许的关系目标 ID
     * @param relationType 关系类型
     * @return 可直接提交给关系写入口的完整主体快照
     * @since v7.260701.0
     */
    private @NonNull List<DtpRwdObjectDTO> listRelationFromObjects(
            @NonNull Collection<String> subjectIds,
            @NonNull Collection<String> targetIds,
            @NonNull DtpRwdRelationTypeEnum relationType) {
        Map<String, DtpRwdObjectDTO> normalized = new LinkedHashMap<>();
        for (String subjectId : subjectIds) {
            DtpRwdObjectDTO relationObject = new DtpRwdObjectDTO();
            relationObject.setId(subjectId);
            relationObject.setRelObjIds(new LinkedHashSet<>());
            normalized.put(subjectId, relationObject);
        }
        if (normalized.isEmpty() || targetIds.isEmpty()) {
            return new ArrayList<>(normalized.values());
        }

        Set<String> expectedTargetIds = new LinkedHashSet<>(targetIds);
        DtpRwdObjectQueryParam query = new DtpRwdObjectQueryParam();
        query.setId(new ArrayList<>(normalized.keySet()));
        query.setRelationFrom(new DtpRwdObjectRelationQueryParam(relationType)
                .setObjTo(new ArrayList<>(expectedTargetIds)));
        query.addWithColumn(
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
        for (DtpRwdObjectDTO result : this.dtpRwdClientService.listObject(query)) {
            DtpRwdObjectDTO relationObject = normalized.get(result.getId());
            if (relationObject == null || result.getRelObjIds() == null) {
                continue;
            }
            result.getRelObjIds().stream()
                    .filter(expectedTargetIds::contains)
                    .forEach(relationObject.getRelObjIds()::add);
        }
        return new ArrayList<>(normalized.values());
    }

    @Override
    public String logTag() {
        return "物理世界缓存-空调末端策略";
    }
}
