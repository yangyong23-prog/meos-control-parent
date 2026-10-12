package com.persagy.apccontrol.basic.service.duty.strategy;

import cn.hutool.core.date.StopWatch;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingCircuitDetailDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeControlDeviationStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingRunningDeviationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardDateVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.ChildCardDataVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingCircuitTagVO;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.service.lighting.AbstractLightingService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorLightingStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 照明查询实现类
 */
@Service
@Slf4j
public class LightHomePageCardDataHandler extends AbstractHomePageCardDataHandler<CardDateVO<ChildCardDataVO>> implements LogComponent {

    private final String NIGHT_SCENE_LIGHTING = "NIGHT_SCENE_LIGHTING";

    @Resource
    private DemandLightingStrategy demandLightingStrategy;

    @Resource
    private AbstractLightingService abstractLightingService;

    @Resource
    private RunningMonitorLightingStrategy runningMonitorLightingStrategy;

    /**
     * 缓存对象模型共享 READY 状态服务。
     *
     * @since v7.260701.0
     */
    @Resource
    private @NonNull CacheObjectModelStatusService
            cacheObjectModelStatusService;

    /**
     * 新缓存对象批量读取服务。
     *
     * @since v7.260701.0
     */
    @Resource
    private @NonNull CacheObjectRepository cacheObjectRepository;

    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.LIGHTING;
    }

    @Override
    public CardDateVO<ChildCardDataVO> execute(HomePageParam param) {
        CardDateVO<ChildCardDataVO> vo = new CardDateVO<>();
        vo.setRunningMonitorType(getRunningMonitorType());
        String projectId = ContextUtil.getProjectId();
        StopWatch stopWatch = new StopWatch();

        // 检查是否为【强制】的信息点
        Set<String> forceCodes = Collections.singleton(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode());

        Map<String, ObjectIdAndForceCodeQuery> queryMap = new HashMap<>();
        boolean useNewCache = this.cacheObjectModelStatusService.isReady(
                RunningMonitorTypeEnum.LIGHTING);

        String publicAreaKey = CacheObjectModelKeyConfig
                .LIGHTING_LIST_EQUIPMENT_BY_SECOND_TYPE.get(
                        RunningMonitorSecondTypeEnum.PUBLIC_AREA_LIGHTING);
        String parkingLotKey = CacheObjectModelKeyConfig
                .LIGHTING_LIST_EQUIPMENT_BY_SECOND_TYPE.get(
                        RunningMonitorSecondTypeEnum.PARKING_LOT_LIGHTING);
        List<String> nightLightingKeys = Arrays.asList(
                CacheObjectModelKeyConfig.LIGHTING_LIST_EQUIPMENT_BY_SECOND_TYPE.get(
                        RunningMonitorSecondTypeEnum.BUILDING_LIGHTING),
                CacheObjectModelKeyConfig.LIGHTING_LIST_EQUIPMENT_BY_SECOND_TYPE.get(
                        RunningMonitorSecondTypeEnum.LANDSCAPE_LIGHTING),
                CacheObjectModelKeyConfig.LIGHTING_LIST_EQUIPMENT_BY_SECOND_TYPE.get(
                        RunningMonitorSecondTypeEnum.SIGN_LIGHTING));
        List<String> lightingCacheKeys = new ArrayList<>(nightLightingKeys.size() + 2);
        lightingCacheKeys.add(publicAreaKey);
        lightingCacheKeys.add(parkingLotKey);
        lightingCacheKeys.addAll(nightLightingKeys);
        Map<String, List<LightingCircuitDetailDTO>> newObjectsByCacheKey = useNewCache
                ? this.cacheObjectRepository.mapByCacheKeys(
                        lightingCacheKeys,
                        LightingCircuitDetailDTO.class,
                        Arrays.asList(
                                DtpRwdObjectDTO.Fields.id,
                                InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value()))
                : Collections.emptyMap();

        stopWatch.start("获取公区照明回路");
        // 查询公区回路ID
        Set<String> publicLightingObjIds;
        if (useNewCache) {
            publicLightingObjIds = newObjectsByCacheKey
                    .getOrDefault(publicAreaKey, Collections.emptyList()).stream()
                    .map(LightingCircuitDetailDTO::getId)
                    .collect(Collectors.toSet());
        } else {
            RunningMonitorEquipmentQueryParam queryParam = new RunningMonitorEquipmentQueryParam()
                    .setRunningMonitorSecondTypes(Collections.singletonList(RunningMonitorSecondTypeEnum.PUBLIC_AREA_LIGHTING));
            List<LightingCircuitDetailDTO> publicLightingCircuits =
                    this.runningMonitorLightingStrategy.listCircuits(queryParam);
            publicLightingObjIds = publicLightingCircuits.stream()
                    .map(LightingCircuitDetailDTO::getId)
                    .collect(Collectors.toSet());
        }
        // 查询停车场回路ID
        Set<String> parkingLightingObjIds;
        if (useNewCache) {
            parkingLightingObjIds = newObjectsByCacheKey
                    .getOrDefault(parkingLotKey, Collections.emptyList()).stream()
                    .map(LightingCircuitDetailDTO::getId)
                    .collect(Collectors.toSet());
        } else {
            RunningMonitorEquipmentQueryParam queryParam = new RunningMonitorEquipmentQueryParam()
                    .setRunningMonitorSecondTypes(Collections.singletonList(RunningMonitorSecondTypeEnum.PARKING_LOT_LIGHTING));
            List<LightingCircuitDetailDTO> parkingLightingCircuits =
                    this.runningMonitorLightingStrategy.listCircuits(queryParam);
            parkingLightingObjIds = parkingLightingCircuits.stream()
                    .map(LightingCircuitDetailDTO::getId)
                    .collect(Collectors.toSet());
        }

        if (CollectionUtils.isNotEmpty(publicLightingObjIds)) {
            queryMap = ObjectIdAndForceCodeQuery.buildDutyHomePageQuery(
                    getCodeAndeName(DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING.getValue(), DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING.getReasonPhrase()),
                    publicLightingObjIds, forceCodes);
        }
        if (CollectionUtils.isNotEmpty(parkingLightingObjIds)) {
            ObjectIdAndForceCodeQuery.addDutyHomePageQuery(queryMap,
                    getCodeAndeName(DemandTemplateTypeEnum.PARKING_LOT_LIGHTING.getValue(), DemandTemplateTypeEnum.PARKING_LOT_LIGHTING.getReasonPhrase()),
                    parkingLightingObjIds, forceCodes);
        }
        stopWatch.stop();

        stopWatch.start("获取夜景照明回路");
        // 夜景照明
        String nightLighting = getCodeAndeName(NIGHT_SCENE_LIGHTING, LocaleMessageUtil.getMessage("key_7aeb6d4df12d4ca690ed8d972ecc643f"));
        // 优化：使用缓存查询楼体、景观、标识照明回路
        List<LightingCircuitDetailDTO> nightLightingLighting = new ArrayList<>();
        if (useNewCache) {
            for (String nightLightingKey : nightLightingKeys) {
                nightLightingLighting.addAll(newObjectsByCacheKey.getOrDefault(
                        nightLightingKey, Collections.emptyList()));
            }
        } else {
            List<RunningMonitorSecondTypeEnum> nightTypes = Arrays.asList(
                    RunningMonitorSecondTypeEnum.BUILDING_LIGHTING,
                    RunningMonitorSecondTypeEnum.LANDSCAPE_LIGHTING,
                    RunningMonitorSecondTypeEnum.SIGN_LIGHTING
            );
            RunningMonitorEquipmentQueryParam queryParam = new RunningMonitorEquipmentQueryParam()
                    .setRunningMonitorSecondTypes(nightTypes);
            nightLightingLighting = this.runningMonitorLightingStrategy.listCircuits(queryParam);
        }
        super.filtrationMonitoringSurveillanceStatus(nightLightingLighting);
        Set<String> nightLightingLightingObjIds = CollectionUtils.isEmpty(nightLightingLighting)
                ? new HashSet<>()
                : nightLightingLighting.stream().map(LightingCircuitDetailDTO::getId).collect(Collectors.toSet());
        ObjectIdAndForceCodeQuery.addDutyHomePageQuery(queryMap, nightLighting, nightLightingLightingObjIds, new HashSet<>(forceCodes));
        stopWatch.stop();

        // 卡片标签数据
        Set<String> objIds = new HashSet<>();
        objIds.addAll(publicLightingObjIds);
        objIds.addAll(parkingLightingObjIds);
        objIds.addAll(nightLightingLightingObjIds);

        // 并行查询：主线程执行聚合接口，三路提交线程池
        stopWatch.start("并行查询（聚合+异常事件+偏差+模版）");
        Map<String, ObjectIdAndForceCodeQuery> finalQueryMap = queryMap;
        long callerThreadId = Thread.currentThread().getId();
        CompletableFuture<Integer> errorEventFuture = CompletableFuture.supplyAsync(
                () -> {
                    long taskStart = System.currentTimeMillis();
                    int result = getExceptionEventCountByObjIds(objIds);
                    logProjectInstance().info(log, logTag(), "[性能] 异常事件查询 耗时: " + (System.currentTimeMillis() - taskStart) + "ms, thread=" + Thread.currentThread().getName() + (Thread.currentThread().getId() == callerThreadId ? " [串行降级]" : " [并行]"));
                    return result;
                }, BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture<Map<String, Map<String, LightingRunningDeviationTypeEnum>>> deviationFuture = CompletableFuture.supplyAsync(
                () -> {
                    long taskStart = System.currentTimeMillis();
                    Map<String, Map<String, LightingRunningDeviationTypeEnum>> result = queryControlDeviationData(finalQueryMap);
                    logProjectInstance().info(log, logTag(), "[性能] 偏差数据查询 耗时: " + (System.currentTimeMillis() - taskStart) + "ms, thread=" + Thread.currentThread().getName() + (Thread.currentThread().getId() == callerThreadId ? " [串行降级]" : " [并行]"));
                    return result;
                }, BusinessThreadPoolExecutor.ttlExecutor());
        List<DemandTemplateTypeEnum> templateTypeList = Arrays.asList(
                DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING,
                DemandTemplateTypeEnum.PARKING_LOT_LIGHTING,
                DemandTemplateTypeEnum.BUILDING_LIGHTING);
        CompletableFuture<Map<String, DemandTemplateVO>> nameMapFuture = CompletableFuture.supplyAsync(
                () -> {
                    long taskStart = System.currentTimeMillis();
                    Map<String, DemandTemplateVO> result = getDemandTemplateType(templateTypeList);
                    logProjectInstance().info(log, logTag(), "[性能] 需求模版查询 耗时: " + (System.currentTimeMillis() - taskStart) + "ms, thread=" + Thread.currentThread().getName() + (Thread.currentThread().getId() == callerThreadId ? " [串行降级]" : " [并行]"));
                    return result;
                }, BusinessThreadPoolExecutor.ttlExecutor());
        // 主线程执行聚合接口
        DutyHomePageGroupDTO dutyHomePageGroup = queryHomePageCardGroupIotData(queryMap);
        stopWatch.stop();

        if (dutyHomePageGroup == null) {
            logProjectInstance().error("获取IOT卡片数据失败",
                    Pair.of("系统", getRunningMonitorType().getReasonPhrase()));
            return vo;
        }

        // 等待其余三路完成
        stopWatch.start("等待并行查询完成");
        CompletableFuture.allOf(errorEventFuture, deviationFuture, nameMapFuture).join();
        stopWatch.stop();

        // 本地组装
        stopWatch.start("本地组装");
        // 内联构造 CardTagVO（不调用基类 buildCardTagVO，offlineTotal/orderFailAlarmTotal 默认为 0）
        CardTagVO tag = new CardTagVO();
        tag.setErrorEventTotal(errorEventFuture.join());
        vo.setCardTag(tag);
        // 填充小卡片信息
        List<ChildCardDataVO> childCardList = buildChildCardDateVO(dutyHomePageGroup.getSubStat());
        // 应用控制偏差查询结果
        applyDeviationResults(childCardList, deviationFuture.join());
        // 排序
        childCardList.sort(Comparator.comparing(ChildCardDataVO::getNameCode).reversed());
        vo.setChildCardList(childCardList);
        // 填充小卡片的日期标签
        setDemandTemplateType(vo, nightLighting, nameMapFuture.join());
        stopWatch.stop();

        // 阶段5：queryCanJump（产品已确认移除）
        // stopWatch.start("查询是否可跳转标签");
        // queryCanJump(childCardList);
        // stopWatch.stop();

        LogExecutor.project(projectId).info(log, logTag(), "照明值班首页",
                Collections.singletonList(Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS))));
        return vo;
    }

    /**
     * 查询控制偏差数据（仅远程查询，不含本地写入逻辑）
     *
     * @param queryMap 查询参数
     * @return 模版类型 -> (回路ID -> 偏差类型)
     * @since v7.260801.0
     */
    private Map<String, Map<String, LightingRunningDeviationTypeEnum>> queryControlDeviationData(
            @NonNull Map<@NonNull String, @NonNull ObjectIdAndForceCodeQuery> queryMap) {
        long deviationStart = System.currentTimeMillis();
        if (MapUtils.isEmpty(queryMap)) {
            return new HashMap<>();
        }
        Map<String, Map<String, LightingRunningDeviationTypeEnum>> templateTypeCircuitIdAndDeviationType = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        long deviationCallerThreadId = Thread.currentThread().getId();
        for (Map.Entry<String, ObjectIdAndForceCodeQuery> entry : queryMap.entrySet()) {
            Set<String> circuitIds = entry.getValue().getObjectIds();
            ChildCardDataVO vo = new ChildCardDataVO();
            vo.setName(entry.getKey());
            if (StringUtils.equals(vo.getNameCode(), NIGHT_SCENE_LIGHTING)) {
                futures.add(CompletableFuture.runAsync(() -> {
                    long innerStart = System.currentTimeMillis();
                    // mapCircuitIdAndInfoCodeValues 调用提到 forEach 外部，只调 1 次
                    Map<String, Map<String, Object>> circuitIdAndInfoCodeValues =
                            abstractLightingService.mapCircuitIdAndInfoCodeValues(
                                    circuitIds, InfoCodeEnum.CONTROL_DEVIATION_STATUS.value());

                    for (String circuitId : circuitIds) {
                        Map<String, Object> infoCodeAndValue = circuitIdAndInfoCodeValues.get(circuitId);
                        // 直接使用局部变量，去掉 LightingCircuitTagVO 中间对象
                        DtpKnowledgeControlDeviationStatusEnum controlDeviationStatusEnum =
                                DtpKnowledgeControlDeviationStatusEnum.analyze(infoCodeAndValue);
                        LightingRunningDeviationTypeEnum deviationType =
                                LightingRunningDeviationTypeEnum.of(controlDeviationStatusEnum);

                        if (Objects.nonNull(deviationType)) {
                            Map<String, LightingRunningDeviationTypeEnum> circuitIdAndDeviationType =
                                    Optional.ofNullable(templateTypeCircuitIdAndDeviationType
                                            .get(NIGHT_SCENE_LIGHTING)).orElseGet(HashMap::new);
                            circuitIdAndDeviationType.put(circuitId, deviationType);
                            templateTypeCircuitIdAndDeviationType.put(NIGHT_SCENE_LIGHTING, circuitIdAndDeviationType);
                        }
                    }
                    logProjectInstance().info(log, logTag(), "[性能] queryControlDeviationData-夜景照明 耗时: " + (System.currentTimeMillis() - innerStart) + "ms, circuitIds=" + circuitIds.size() + (Thread.currentThread().getId() == deviationCallerThreadId ? " [串行降级]" : " [并行]"));
                }, BusinessThreadPoolExecutor.ttlExecutor()));
                continue;
            }
            DemandTemplateTypeEnum templateType = DemandTemplateTypeEnum.analyze(vo.getNameCode());

            futures.add(CompletableFuture.runAsync(() -> {
                long innerStart = System.currentTimeMillis();
                Map<String, LightingCircuitTagVO> circuitIdAndTag = abstractLightingService.mapCircuitIdAndTag(
                        ContextUtil.getProjectId(), templateType, circuitIds);
                if (MapUtils.isNotEmpty(circuitIdAndTag)) {
                    for (Map.Entry<String, LightingCircuitTagVO> tagEntry : circuitIdAndTag.entrySet()) {
                        String circuitId = tagEntry.getKey();
                        LightingCircuitTagVO tag = tagEntry.getValue();
                        if (Objects.nonNull(tag) && Objects.nonNull(tag.getRunningDeviationType())) {
                            MapExecutor.dynamicPut(templateTypeCircuitIdAndDeviationType, templateType.value(),
                                    circuitId, tag.getRunningDeviationType(), ConcurrentHashMap::new);
                        }
                    }
                }
                logProjectInstance().info(log, logTag(), "[性能] queryControlDeviationData-" + templateType.value() + " 耗时: " + (System.currentTimeMillis() - innerStart) + "ms, circuitIds=" + circuitIds.size() + (Thread.currentThread().getId() == deviationCallerThreadId ? " [串行降级]" : " [并行]"));
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        logProjectInstance().info(log, logTag(), "[性能] queryControlDeviationData 总耗时: " + (System.currentTimeMillis() - deviationStart) + "ms, queryMapSize=" + queryMap.size());
        return templateTypeCircuitIdAndDeviationType;
    }

    /**
     * 应用控制偏差查询结果到小卡片（清空 + 填充）
     *
     * @param childCardList 卡片
     * @param deviationMap  偏差查询结果
     * @since v7.260801.0
     */
    private void applyDeviationResults(
            @NonNull List<@NonNull ChildCardDataVO> childCardList,
            @NonNull Map<@NonNull String, @NonNull Map<@NonNull String, LightingRunningDeviationTypeEnum>> deviationMap) {
        if (CollectionUtils.isEmpty(childCardList) || MapUtils.isEmpty(deviationMap)) {
            return;
        }
        // 先清空已存在的多开少开
        for (ChildCardDataVO vo : childCardList) {
            vo.setMoreOpenTotal(0);
            vo.setLessOpenTotal(0);
        }
        // 重新填充卡片多开少开偏移数量
        for (ChildCardDataVO vo : childCardList) {
            Map<String, LightingRunningDeviationTypeEnum> circuitIdAndDeviationType =
                    deviationMap.get(vo.getNameCode());
            if (MapUtils.isNotEmpty(circuitIdAndDeviationType)) {
                int totalMoreOpen = 0;
                int totalLessOpen = 0;
                for (LightingRunningDeviationTypeEnum deviationType : circuitIdAndDeviationType.values()) {
                    switch (deviationType) {
                        case MORE_OPEN:
                            totalMoreOpen++;
                            break;
                        case LESS_OPEN:
                            totalLessOpen++;
                            break;
                        default:
                    }
                }
                vo.setMoreOpenTotal(totalMoreOpen);
                vo.setLessOpenTotal(totalLessOpen);
            }
        }
    }

    private void queryCanJump(List<ChildCardDataVO> childCardList) {
        // 获取 code
        Set<String> set = queryDemandTemplateType(DemandTypeEnum.LIGHTING);
        // 遍历小卡片 是否可以跳转进行标识
        childCardList.forEach(card -> {
            if (set.contains(card.getNameCode())) {
                card.setCanJump(true);
            }
            // 夜景照明特殊处理
            if (card.getNameCode().equals("NIGHT_SCENE_LIGHTING")) {
                card.setCanJump(set.contains(DemandTemplateTypeEnum.BUILDING_LIGHTING.value()) ||
                        set.contains(DemandTemplateTypeEnum.LANDSCAPE_LIGHTING.value()) ||
                        set.contains(DemandTemplateTypeEnum.SIGN_LIGHTING.value())
                );
                card.setCanJump(true);

            }
        });
    }

    private void setDemandTemplateType(CardDateVO<ChildCardDataVO> vo, String nightLighting,
                                       Map<String, DemandTemplateVO> nameMap) {
        // 填充小卡片的日期标签
        vo.getChildCardList().forEach(card -> {
            if (nightLighting.contains(card.getNameCode())) {
                card.setDemandTemplate(nameMap.get(DemandTemplateTypeEnum.BUILDING_LIGHTING.getValue()));
            } else {
                card.setDemandTemplate(nameMap.get(card.getNameCode()));
            }
            //默认都能跳转，主要是用于前端固定使用，不然前端就得去掉这个判断逻辑
            card.setCanJump(Boolean.TRUE);
        });
    }


    @Override
    public String logTag() {
        return "值班首页-照明查询实现";
    }
}
