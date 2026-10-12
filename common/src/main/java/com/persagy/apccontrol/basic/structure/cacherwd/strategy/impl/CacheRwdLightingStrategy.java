package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingCircuitDetailDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheWriteAction;
import com.persagy.apccontrol.basic.structure.cacherwd.service.LightingCacheObjectRelationService;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.BaseCacheRwdStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorLightingStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 物理世界对象缓存-照明策略实现
 *
 * @author JiYinchuan
 * @since 7.260401.0
 */
@Component(CacheRwdLightingStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdLightingStrategy extends BaseCacheRwdStrategy {

    public static final String BEAN_NAME = "cacheRwdLightingStrategy";

    /**
     * 照明缓存对象模型源对象查询策略。
     *
     * @since v7.260701.0
     */
    private final @NonNull RunningMonitorLightingStrategy runningMonitorLightingStrategy;

    /**
     * 新版对象模型复合写入服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelWriteService cacheObjectModelWriteService;

    /**
     * 照明关系快照服务；初始化与模板增量共用同一关系写入动作。
     *
     * @since v7.260701.0
     */
    private final @NonNull LightingCacheObjectRelationService lightingCacheObjectRelationService;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.LIGHTING;
    }

    @Override
    public boolean buildCacheData(RunningMonitorTypeEnum runningMonitorType) {
        return this.cacheEquipments();
    }

    /**
     * 缓存设备。
     *
     * <p>五个二级类型的源对象和关系快照在事务外并行准备；源查询附着的服务空间或楼层由
     * 关系快照直接复用，控制模块和 DDC 仍由每个 Key 只按自身回路查询。全部关系动作准备
     * 成功后，按既定二级类型顺序串行替换缓存，避免数据库写入并发破坏短事务边界。</p>
     *
     * @return 五个照明缓存 Key 是否全部替换成功
     * @since 7.260401.0
     */
    private boolean cacheEquipments() {
        List<RunningMonitorSecondTypeEnum> lightingRunningMonitorSecondTypes = Lists.newArrayList(
                RunningMonitorSecondTypeEnum.PUBLIC_AREA_LIGHTING,
                RunningMonitorSecondTypeEnum.PARKING_LOT_LIGHTING,
                RunningMonitorSecondTypeEnum.BUILDING_LIGHTING,
                RunningMonitorSecondTypeEnum.LANDSCAPE_LIGHTING,
                RunningMonitorSecondTypeEnum.SIGN_LIGHTING
        );
        Map<RunningMonitorSecondTypeEnum, List<LightingCircuitDetailDTO>> typeEquipments =
                new ConcurrentHashMap<>();
        Map<String, CacheWriteAction> relationActions = new ConcurrentHashMap<>();
        // 五个 Key 的源对象及其关系快照可以并行准备，全部完成后再逐 Key 替换。
        CompletableFuture<Void> prepareFuture = CompletableFuture.allOf(lightingRunningMonitorSecondTypes.stream()
                .map(runningMonitorSecondType -> CompletableFuture.runAsync(() -> {
                    RunningMonitorEquipmentQueryParam param = new RunningMonitorEquipmentQueryParam();
                    param.setRunningMonitorSecondType(runningMonitorSecondType);
                    param.addWithColumn(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                            DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value()).toArray(new String[0]));
                    List<LightingCircuitDetailDTO> equipments =
                            this.runningMonitorLightingStrategy
                                    .listCacheModelSourceCircuits(param);
                    String modelCacheKey = CacheObjectModelKeyConfig
                            .LIGHTING_LIST_EQUIPMENT_BY_SECOND_TYPE.get(runningMonitorSecondType);
                    CacheWriteAction relationAction =
                            this.lightingCacheObjectRelationService
                                    .prepareSaveActionFromCircuitRelations(
                                            modelCacheKey, equipments);
                    typeEquipments.put(runningMonitorSecondType, equipments);
                    relationActions.put(modelCacheKey, relationAction);
                }, BusinessThreadPoolExecutor.ttlExecutor())).toArray(CompletableFuture[]::new));
        prepareFuture.join();
        // 每个二级类型是独立缓存范围；新版对象、模型视图、键关联和照明关系动作由复合写服务
        // 在该范围的同一个短事务内提交，不再删除或写入历史照明缓存。写入保持既定二级类型顺序。
        for (RunningMonitorSecondTypeEnum runningMonitorSecondType : lightingRunningMonitorSecondTypes) {
            String modelCacheKey = CacheObjectModelKeyConfig
                    .LIGHTING_LIST_EQUIPMENT_BY_SECOND_TYPE.get(runningMonitorSecondType);
            List<LightingCircuitDetailDTO> equipments =
                    typeEquipments.get(runningMonitorSecondType);
            CacheWriteAction relationAction = relationActions.get(modelCacheKey);
            boolean replaced = this.cacheObjectModelWriteService.replaceModelCacheKey(
                    modelCacheKey, equipments, Collections.singletonList(relationAction));
            if (!replaced) {
                // 任一 Key 拒绝提交时，当前一级类型不能继续标记 READY。
                return false;
            }
        }
        return true;
    }

    @Override
    public String logTag() {
        return "物理世界缓存-照明策略";
    }
}
