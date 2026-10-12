package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheTemplateObjectDetailBusinessKey;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheTemplateObjectDetailRepository;
import com.persagy.apccontrol.basic.structure.infocodeconfig.context.InfoCodeRwdObjectDetailByTemplateContext;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeConfigModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectDetailModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 物理世界详情信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Component(RwdObjectDetailInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RwdObjectDetailInfoCodeAnalyzeStrategy
        extends BaseRwdObjectInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "equipmentRwdObjectDetailInfoCodeAnalyzeStrategy";

    /**
     * 强制从真实数据源重新计算对象明细的内部选项。
     *
     * <p>缓存明细重建必须使用该选项，避免 READY 后从目标缓存表反向生成本次写入结果，
     * 形成缓存自引用。</p>
     *
     * @since v7.260701.0
     */
    public static final @NonNull String FORCE_SOURCE_CALCULATION_OPTION =
            "cacheObjectModel.forceRwdObjectDetailSourceCalculation";

    /**
     * 模板对象明细缓存仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheTemplateObjectDetailRepository
            cacheTemplateObjectDetailRepository;

    /**
     * 缓存对象模型共享 READY 状态服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.RWD_OBJECT_DETAIL;
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        // 该内部选项只控制本层数据源选择，移除后再传入表达式计算上下文，避免被业务公式误用。
        boolean forceSourceCalculation = additionalValues != null
                && Boolean.TRUE.equals(additionalValues.get(
                RwdObjectDetailInfoCodeAnalyzeStrategy.FORCE_SOURCE_CALCULATION_OPTION));
        Map<String, Object> calculationValues = additionalValues;
        if (forceSourceCalculation) {
            calculationValues = new LinkedHashMap<>(additionalValues);
            calculationValues.remove(
                    RwdObjectDetailInfoCodeAnalyzeStrategy.FORCE_SOURCE_CALCULATION_OPTION);
        }
        Set<Long> newModelTemplateTypeIds = forceSourceCalculation
                ? Collections.emptySet()
                : this.selectNewModelTemplateTypeIds(aggregates);
        Map<CacheTemplateObjectDetailBusinessKey, List<InfoCodeRwdObjectDetailDTO>> cachedResults =
                forceSourceCalculation
                        ? Collections.emptyMap()
                        : this.loadCachedResults(aggregates, newModelTemplateTypeIds);
        final Map<String, Object> finalCalculationValues = calculationValues;
        Map<InfoCodeTemplateTypeModel, Map<InfoCodeConfigModel, List<InfoCodeAggregate>>>
                templateTypeConfigAndAggregates =
                super.mapTemplateTypeConfigAndAggregates(aggregates);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<InfoCodeTemplateTypeModel, Map<InfoCodeConfigModel, List<InfoCodeAggregate>>> entry
                : templateTypeConfigAndAggregates.entrySet()) {
            InfoCodeTemplateTypeModel templateTypeModel = entry.getKey();
            for (Map.Entry<InfoCodeConfigModel, List<InfoCodeAggregate>> innerEntry : entry.getValue().entrySet()) {
                futures.add(CompletableFuture.runAsync(() -> {
                    List<InfoCodeAggregate> eachAggregates = innerEntry.getValue();

                    // 取出第一个聚合根, 按理说同一个模版下的取值逻辑是一致的
                    InfoCodeAggregate firstAggregate = IterableUtils.first(eachAggregates);
                    InfoCodeTakeValueLogicRwdObjectDetailModel tackValueLogicModel = firstAggregate.getConfigModel()
                            .analyzeTackValueLogicModel(InfoCodeTakeValueLogicRwdObjectDetailModel.class);
                    // 所有对象ID
                    Set<String> objectIds = InfoCodeAggregate.listAllObjectIds(eachAggregates);

                    InfoCodeRwdObjectDetailByTemplateContext context = new InfoCodeRwdObjectDetailByTemplateContext(
                            templateTypeModel, tackValueLogicModel, objectIds, finalCalculationValues);

                    if (!forceSourceCalculation && newModelTemplateTypeIds.contains(
                            templateTypeModel.getTemplateTypeId())) {
                        // 选中新模型后按每个复合业务键返回实际命中结果；单个缺失键使用空结果，
                        // 不让同一模板/信息点聚合在一次请求中混用缓存明细和 RWD 实时计算。
                        this.fillFromCachedResults(eachAggregates, context, cachedResults);
                        return;
                    }
                    // 填充数据源映射
                    context.fillSourceMap();
                    // 填充对象
                    context.fillObjects();
                    // 填充对象ID和关联上下文
                    context.fillObjectIdAndRelation();
                    // 填充信息点配置
                    context.fillInfoCodeConfig();

                    Map<String, List<DtpRwdObjectDTO>> rootObjectIdAndObjects = context.getRootObjectIdAndObjects();
                    for (InfoCodeAggregate aggregate : eachAggregates) {
                        String rootObjectId = aggregate.getIdentifier().getObject().getId();
                        String infoCode = aggregate.getConfigModel().getInfoCode();

                        List<DtpRwdObjectDTO> objects = Collections.emptyList();
                        if (MapUtils.isNotEmpty(rootObjectIdAndObjects)) {
                            objects = rootObjectIdAndObjects.getOrDefault(rootObjectId, Collections.emptyList());
                        }
                        List<InfoCodeRwdObjectDetailDTO> result = context.toResult(objects);
                        aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, result, null, null));
                    }
                }, BusinessThreadPoolExecutor.ttlExecutor()));
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 按当前请求一次性取得所有已路由到新模型的模板对象明细。
     *
     * <p>仓储使用复合业务键查询并返回实际存在的记录；缺少记录的聚合使用空结果，
     * 不在同一请求回退 RWD。</p>
     *
     * @param aggregates 当前请求聚合根
     * @param newModelTemplateTypeIds 本次请求已经选中新模型的模板类型 ID
     * @return 业务键到缓存结果的映射
     * @since v7.260701.0
     */
    private @NonNull Map<CacheTemplateObjectDetailBusinessKey, List<InfoCodeRwdObjectDetailDTO>>
    loadCachedResults(
            @NonNull List<InfoCodeAggregate> aggregates,
            @NonNull Set<Long> newModelTemplateTypeIds) {
        Set<CacheTemplateObjectDetailBusinessKey> businessKeys = new LinkedHashSet<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            InfoCodeTemplateTypeModel templateTypeModel = aggregate.getTemplateTypeModel();
            if (templateTypeModel == null || !newModelTemplateTypeIds.contains(
                    templateTypeModel.getTemplateTypeId())) {
                continue;
            }
            businessKeys.add(this.toBusinessKey(aggregate));
        }
        if (CollectionUtils.isEmpty(businessKeys)) {
            return Collections.emptyMap();
        }
        return this.cacheTemplateObjectDetailRepository.mapResultsByBusinessKeys(businessKeys);
    }

    /**
     * 选择本次对象明细解析中读取新模型的模板类型。
     *
     * <p>一次解析可能按对象和信息点生成大量 aggregate，但 READY 只与一级运行监测类型有关。
     * 这里先按一级类型缓存本次 Redis 判断结果，再生成模板集合，后续业务键加载和并行分组
     * 都只读取该集合，避免对象数和信息点数放大 Redis 往返。</p>
     *
     * @param aggregates 当前请求聚合根
     * @return 本次请求选中新模型的模板类型 ID
     * @since v7.260701.0
     */
    private @NonNull Set<Long> selectNewModelTemplateTypeIds(
            @NonNull List<InfoCodeAggregate> aggregates) {
        if (LocalContextStorage.isDeprecatedVersion()) {
            return Collections.emptySet();
        }
        Map<RunningMonitorTypeEnum, Boolean> readyByRunningMonitorType =
                new EnumMap<>(RunningMonitorTypeEnum.class);
        Set<Long> result = new HashSet<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            InfoCodeTemplateTypeModel templateTypeModel = aggregate.getTemplateTypeModel();
            if (templateTypeModel == null || !templateTypeModel.allowCache()) {
                continue;
            }
            boolean ready = readyByRunningMonitorType.computeIfAbsent(
                    templateTypeModel.getRunningMonitorType(),
                    this.cacheObjectModelStatusService::isReady);
            if (ready) {
                result.add(templateTypeModel.getTemplateTypeId());
            }
        }
        return result;
    }

    /**
     * 使用一组缓存结果填充上下文并保持每个聚合业务键的结果边界。
     *
     * @param aggregates       同一模板和信息点配置下的聚合根
     * @param context           当前上下文
     * @param cachedResults     批量缓存结果
     * @since v7.260701.0
     */
    private void fillFromCachedResults(
            @NonNull List<InfoCodeAggregate> aggregates,
            @NonNull InfoCodeRwdObjectDetailByTemplateContext context,
            @NonNull Map<CacheTemplateObjectDetailBusinessKey, List<InfoCodeRwdObjectDetailDTO>> cachedResults) {
        List<List<InfoCodeRwdObjectDetailDTO>> resultGroups = new ArrayList<>(aggregates.size());
        for (InfoCodeAggregate aggregate : aggregates) {
            CacheTemplateObjectDetailBusinessKey businessKey = this.toBusinessKey(aggregate);
            resultGroups.add(cachedResults.getOrDefault(
                    businessKey, Collections.emptyList()));
        }

        List<List<InfoCodeRwdObjectDetailDTO>> preparedResultGroups =
                context.prepareCachedResults(resultGroups);
        for (int i = 0; i < aggregates.size(); i++) {
            InfoCodeAggregate aggregate = aggregates.get(i);
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(
                    aggregate.getConfigModel().getInfoCode(),
                    preparedResultGroups.get(i), null, null));
        }
    }

    /**
     * 创建模板对象明细的完整复合业务键。
     *
     * @param aggregate 信息点聚合根
     * @return 完整业务键
     * @since v7.260701.0
     */
    private @NonNull CacheTemplateObjectDetailBusinessKey toBusinessKey(
            @NonNull InfoCodeAggregate aggregate) {
        return new CacheTemplateObjectDetailBusinessKey()
                .setTemplateKey(aggregate.getTemplateTypeModel().getTemplateTypeKey())
                .setObjectId(aggregate.getIdentifier().getObject().getId())
                .setInfoCode(aggregate.getConfigModel().getInfoCode());
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-RWD对象";
    }
}
