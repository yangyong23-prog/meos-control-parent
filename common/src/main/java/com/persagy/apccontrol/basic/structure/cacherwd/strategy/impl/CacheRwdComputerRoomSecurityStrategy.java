package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRule;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheIotRuleRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRuleRepository;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.BaseCacheRwdStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorComputerRoomSecurityStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 物理世界对象缓存-机房安全策略实现
 *
 * @author JiYinchuan
 * @since 7.260402.0
 */
@Component(CacheRwdComputerRoomSecurityStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdComputerRoomSecurityStrategy extends BaseCacheRwdStrategy {

    public static final String BEAN_NAME = "cacheRwdComputerRoomSecurityStrategy";

    private final RunningMonitorComputerRoomSecurityStrategy runningMonitorComputerRoomSecurityStrategy;

    /**
     * BDTP 客户端服务，写入侧直接查询空间和设备原始 DTO。
     *
     * @since v7.260802.0
     */
    private final @NonNull DtpRwdClientService dtpRwdClientService;

    /**
     * 新版对象模型写入服务。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheObjectModelWriteService cacheObjectModelWriteService;

    /**
     * 缓存对象模型状态服务。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    /**
     * IoT 数据缓存服务，查询规则实例化状态。
     *
     * @since v7.260802.0
     */
    private final @NonNull AbstractIotDataCacheServer<?> abstractIotDataCacheServer;

    /**
     * 缓存规则仓库，写入/替换 IoT 规则缓存。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheRuleRepository cacheRuleRepository;

    /**
     * IoT 规则仓库，获取部署端规则类型。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheIotRuleRepository cacheIotRuleRepository;

    /**
     * Dashboard 空间缓存固定 key，读取侧 {@code RunningMonitorComputerRoomSecurityStrategy#listDashboardSpaces}
     * 仍按该 Key 查询历史缓存表。
     */
    public static final String SPACE_CACHE_KEY = "cache_rwd_computer_room_security_list_dashboard_spaces";

    /**
     * BDTP 串行分页每页大小，与 FeignPageExecutor 的 page.size.limit 保持一致，
     * 使分页查询走同步分支而非并行分支，降低对 BDTP 的并发压力。
     *
     * @since v7.260802.0
     */
    private static final int SERIAL_PAGE_SIZE = 1000;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY;
    }

    @Override
    public boolean buildCacheData(RunningMonitorTypeEnum runningMonitorType) {
        this.logProjectInstance().info("构建机房缓存开始");
        // 新缓存同步写入：楼层级空间 + 设备 + IoT 规则
        boolean success = this.buildNewCacheData(runningMonitorType);
        if (success) {
            this.cacheObjectModelStatusService.markReady(runningMonitorType);
        }
        this.logProjectInstance().info("构建机房缓存结束");

        return success;
    }

    /**
     * 构建楼层级新缓存数据，包含空间、设备和 IoT 规则。
     *
     * <p>先从机房安全空间提取楼层 ID，查询楼层全量空间和设备，写入新缓存表。
     * 然后查询设备的 IoT 规则实例化状态并写入规则缓存。</p>
     *
     * @param runningMonitorType 运行监测类型
     * @return 是否全部写入成功
     * @since v7.260802.0
     */
    private boolean buildNewCacheData(@NonNull RunningMonitorTypeEnum runningMonitorType) {
        // Step 1: 查询机房安全空间（按 roomFuncType 过滤），获取 floorId 集合
        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
        spaceQueryParam.setMeosCache(false);
        spaceQueryParam.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.COMPUTER_ROOM_SECURITY);
        spaceQueryParam.setWithColumns(Sets.newHashSet(
                DtpRwdObjectDTO.Fields.id,
                DtpRwdObjectDTO.Fields.classCode,
                DtpRwdSpaceDTO.Fields.floorId,
                DtpRwdSpaceDTO.Fields.roomFuncType,
                DtpRwdSpaceDTO.Fields.buildingId));
        this.logProjectInstance().info("查询机房空间缓存开始");
        List<DtpRwdSpaceDTO> crsSpaces = this.runningMonitorComputerRoomSecurityStrategy
                .listSpacesComputerRoomSecurityCacheSerial(spaceQueryParam, SERIAL_PAGE_SIZE);
        this.logProjectInstance().info("查询机房空间缓存结束：数量"+ crsSpaces.size());
        if (CollectionUtils.isEmpty(crsSpaces)) {
            return false;
        }

        // Step 2: 提取楼层 ID，查询楼层全量空间（串行分页，避免并行请求打挂 BDTP）
        Set<String> floorIds = crsSpaces.stream()
                .map(DtpRwdSpaceDTO::getFloorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(floorIds)) {
            return false;
        }

        // 楼层全量空间查询列：基于 SPACE classCode 白名单并集
        Set<String> spaceWithColumns = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(DtpRwdClassCodeEnum.SPACE.value()));
        spaceWithColumns.add(DtpRwdObjectDTO.Fields.name);
        // 与读取侧旧路径一致：包含 SPACE、SPVSCM、CFCSDR、CFCSFR、SPEGFG 五种 classCode
        List<String> floorSpaceClassCodes = Arrays.asList(
                DtpRwdClassCodeEnum.SPACE.value(),
                DtpRwdClassCodeEnum.SPVSCM.value(),
                DtpRwdClassCodeEnum.CFCSDR.value(),
                DtpRwdClassCodeEnum.CFCSFR.value(),
                DtpRwdClassCodeEnum.SPEGFG.value());
        List<DtpRwdSpaceDTO> floorSpaces = new ArrayList<>();
        DtpRwdObjectQueryParam floorSpaceQueryParam = new DtpRwdObjectQueryParam();
        floorSpaceQueryParam.addInfoCode(DtpRwdSpaceDTO.Fields.floorId, floorIds);
        int floorSpacePageNo = 1;
        this.logProjectInstance().info("查询楼层空间缓存开始");
        while (true) {
            floorSpaceQueryParam.setPageQuery(new PageQuery(floorSpacePageNo, SERIAL_PAGE_SIZE));
            // withColumns / classCode 可能被 checkAndSetParam 清除，每次迭代重新设置
            floorSpaceQueryParam.setWithColumns(spaceWithColumns);
            floorSpaceQueryParam.setClassCode(floorSpaceClassCodes);
            List<DtpRwdSpaceDTO> pageResult = this.dtpRwdClientService.listSpace(floorSpaceQueryParam);
            if (CollectionUtils.isEmpty(pageResult)) {
                break;
            }
            floorSpaces.addAll(pageResult);
            if (pageResult.size() < SERIAL_PAGE_SIZE) {
                break;
            }
            floorSpacePageNo++;
        }
        this.logProjectInstance().info("查询楼层空间缓存结束：数量"+ floorSpaces.size());
        if (CollectionUtils.isEmpty(floorSpaces)) {
            return false;
        }

        // Step 3: 查询楼层全量设备（串行分页）
        Set<String> floorSpaceIds = floorSpaces.stream()
                .map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
        Set<String> equipmentWithColumns = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.listAllRwdQueryFields());
        equipmentWithColumns.add(DtpRwdObjectDTO.Fields.relObjIds);
        RunningMonitorEquipmentQueryParam equipmentParam = new RunningMonitorEquipmentQueryParam();
        equipmentParam.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.COMPUTER_ROOM_SECURITY);
        equipmentParam.setFilterEquipmentInSpaceIds(floorSpaceIds);
        this.logProjectInstance().info("查询楼层设备缓存开始");
        List<DtpRwdEquipmentDTO> equipments = this.runningMonitorComputerRoomSecurityStrategy
                .listEquipmentsForCacheBuildParallel(equipmentParam, equipmentWithColumns, SERIAL_PAGE_SIZE);
        this.logProjectInstance().info("查询楼层设备缓存结束：数量"+ equipments.size());

        // Step 4: 准备空间对象快照
        String spaceKey = CacheObjectModelKeyConfig.COMPUTER_ROOM_SECURITY_LIST_SPACE;
        List<DtpRwdObjectDTO> spaceObjects = floorSpaces.stream()
                .map(s -> (DtpRwdObjectDTO) s).collect(Collectors.toList());

        // Step 5: 事务内原子替换空间 Key、设备 Key 和设备-空间关系
        String equipmentKey = CacheObjectModelKeyConfig.COMPUTER_ROOM_SECURITY_LIST_EQUIPMENT;
        Map<String, Collection<? extends DtpRwdObjectDTO>> sourcesByKey = new LinkedHashMap<>();
        sourcesByKey.put(spaceKey, spaceObjects);
        sourcesByKey.put(equipmentKey, equipments);
        Map<DtpRwdRelationTypeEnum, Collection<? extends DtpRwdObjectDTO>> relationsByType
                = new LinkedHashMap<>();
        relationsByType.put(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE, equipments);
        boolean replaced = this.cacheObjectModelWriteService.replaceModelCacheKeysAndRelations(
                sourcesByKey, Collections.emptyList(), relationsByType, () -> true);
        if (!replaced) {
            return false;
        }

        // Step 6-8: IoT 规则查询与写入（独立于缓存 Key 写入，无需同事务）
        this.buildAndWriteIotRules(equipments, equipmentKey);
        this.logProjectInstance().info("查询实例结束"+ equipments.size());
        return true;
    }

    /**
     * 查询 IoT 规则实例化状态并写入规则缓存。
     *
     * <p>按 classCode 分组设备 ID，查询 4 个信息点的实例化状态，
     * 构建 {@link CacheRule} 集合后调用 {@link CacheRuleRepository#replaceRules}。</p>
     *
     * @param equipments   楼层全量设备列表
     * @param equipmentKey 设备缓存键
     * @since v7.260802.0
     */
    private void buildAndWriteIotRules(
            @NonNull List<DtpRwdEquipmentDTO> equipments,
            @NonNull String equipmentKey) {
        if (CollectionUtils.isEmpty(equipments)) {
            return;
        }

        // 按 classCode 分组设备 ID
        Map<String, Set<String>> classCodeAndObjectIds = new LinkedHashMap<>();
        for (DtpRwdEquipmentDTO eq : equipments) {
            classCodeAndObjectIds
                    .computeIfAbsent(eq.getClassCode(), k -> new LinkedHashSet<>())
                    .add(eq.getId());
        }

        // 查询 4 个信息点的实例化状态
        Set<String> infoCodes = Sets.newHashSet(
                InfoCodeEnum.RUN_STATUS.getCode(),
                InfoCodeEnum.SWITCH_STATUS.getCode(),
                InfoCodeEnum.ON_OFF_STATUS.getCode(),
                InfoCodeEnum.DEPLOY_WITHDRAWALARM_STATUS.getCode());
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes =
                this.abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                        classCodeAndObjectIds, infoCodes);

        // 构建 CacheRule 集合
        CacheIotRuleTypeEnum ruleType = this.cacheIotRuleRepository.cacheIotRuleType();
        List<CacheRule> rules = new ArrayList<>();
        for (DtpRwdEquipmentDTO eq : equipments) {
            String objectId = eq.getId();
            Set<String> existsRuleInfoCodes = objectIdAndExistsRuleInfoCodes
                    .getOrDefault(objectId, Collections.emptySet());
            for (String infoCode : existsRuleInfoCodes) {
                CacheRule rule = new CacheRule();
                rule.setObjectId(objectId);
                rule.setInfoCode(infoCode);
                rule.setType(ruleType);
                rules.add(rule);
            }
        }

        // 写入规则缓存
        this.cacheRuleRepository.replaceRules(equipmentKey, rules);
    }

    @Override
    public String logTag() {
        return "物理世界缓存-机房安全策略";
    }
}