package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeIdentifier;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTackLogicIotInfoCodeHandlerModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.iot.IotConverter;
import com.persagy.apccontrol.basic.utils.template.SpelUtils;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 信息点处理解析策略
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@SuppressWarnings({"rawtypes", "SpringJavaInjectionPointsAutowiringInspection"})
@Component(IotInfoCodeHandlerAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IotInfoCodeHandlerAnalyzeStrategy
        extends BaseIotInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy, LogComponent {

    public static final String BEAN_NAME = "iotInfoCodeHandlerAnalyzeStrategy";
    private static final String OBJECT_ID = "objectId";

    private final Executor executor;
    private final IotConverter iotConverter;
    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.IOT_INFO_CODE_HANDLER;
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        // 根据模版类型进行分组, 每个模版为一个Sheet, 同一个Sheet中的配置按理来说应该一致
        Map<InfoCodeTemplateTypeModel, List<InfoCodeAggregate>> templateTypeAndAggregates = aggregates.stream().collect(
                Collectors.groupingBy(InfoCodeAggregate::getTemplateTypeModel, Collectors.toList()));
        // 缓存模版类型和取值逻辑映射, 避免重复反序列化
        Map<InfoCodeTemplateTypeModel, InfoCodeTackLogicIotInfoCodeHandlerModel>
                cacheTemplateTypeAndTackValueLogic = new HashMap<>();
        // 标识符 -> 关联对象ID
        Map<InfoCodeIdentifier, Set<String>> identifierAndRelObjectIds = new HashMap<>();
        Set<String> allRelInfoCodes = new HashSet<>();
        // 根据模版类型进行遍历, 每个模版类型仅处理一次
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<InfoCodeTemplateTypeModel, List<InfoCodeAggregate>> entry : templateTypeAndAggregates.entrySet()) {
            InfoCodeTemplateTypeModel templateTypeModel = entry.getKey();
            List<InfoCodeAggregate> values = entry.getValue();
            // 取出第一个聚合根, 按理说同一个模版下的取值逻辑是一致的
            InfoCodeAggregate firstAggregate = IterableUtils.first(values);
            InfoCodeTackLogicIotInfoCodeHandlerModel tackValueLogicModel = firstAggregate.getConfigModel()
                    .analyzeTackValueLogicModel(InfoCodeTackLogicIotInfoCodeHandlerModel.class);
            cacheTemplateTypeAndTackValueLogic.put(templateTypeModel, tackValueLogicModel);
            if (!tackValueLogicModel.isExecute()) {
                continue;
            }
            Map<String, DtpRwdObjectQueryParam> idSource = tackValueLogicModel.getIdSource();
            DtpRwdObjectQueryParam queryIdParam = tackValueLogicModel.getQueryIdParam();
            Set<String> infoCodes = tackValueLogicModel.getInfoCodes();
            futures.add(CompletableFuture.runAsync(() -> {
                Map<String, InfoCodeAggregate> objectIdAndAggregate = values.stream().collect(
                        Collectors.toMap(item -> item.getIdentifier().getObject().getId(), Function.identity()));
                Set<String> objectIds = objectIdAndAggregate.keySet();
                Map<String, Object> sourceMap = queryIdSourceMap(idSource, objectIds);
                Object sourceObjectIdPlaceholderValue = sourceMap.get(OBJECT_ID);
                if (ObjectUtils.isEmpty(sourceObjectIdPlaceholderValue)) {
                    sourceMap.put(OBJECT_ID, objectIds);
                }
                // 根据取值逻辑中的动态查询参数查询对象
                DtpRwdObjectQueryParam queryParam = queryIdParam.replacePlaceholder(sourceMap);
                if (Objects.nonNull(queryParam)) {
                    // 强制设置查询字段为ID和关联ID
                    queryParam.setWithColumns(Sets.newHashSet(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds));
                    // 此次查询是否为关联查询
                    boolean relationQuery = ObjectUtils.anyNotNull(queryParam.getRelationFrom(), queryIdParam.getRelationTo());
                    List<DtpRwdObjectDTO> objects = dtpRwdClientService.listObject(queryParam);
                    // 对查询结果进行处理
                    for (DtpRwdObjectDTO object : objects) {
                        String objectId = object.getId();
                        Set<String> relObjIds = object.getRelObjIds();
                        // 为关联查询时, 当关联对象不为空, 则设置每个聚合根标识符对应的关联对象ID
                        if (relationQuery) {
                            if (CollectionUtils.isNotEmpty(relObjIds)) {
                                for (String relObjId : relObjIds) {
                                    InfoCodeAggregate aggregate = objectIdAndAggregate.get(relObjId);
                                    MapExecutor.dynamicPut(identifierAndRelObjectIds,
                                            aggregate.getIdentifier(), objectId, HashSet::new);
                                }
                            }
                            continue;
                        }
                        // 不为关联查询时, 设置每个聚合根标识符对应的对象ID
                        values.forEach(item -> MapExecutor.dynamicPut(identifierAndRelObjectIds,
                                item.getIdentifier(), objectId, HashSet::new));
                    }
                }
            }, TtlExecutors.getTtlExecutor(executor)));
            allRelInfoCodes.addAll(infoCodes);
        }
        if (CollectionUtils.isNotEmpty(futures)) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // 批量查询一次IOT数据
        Set<String> allRelObjectIds = identifierAndRelObjectIds.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        Map<String, Map<String, List<InfoCodeDataModel>>> objectIdInfoCodeAndModels =
                mapObjectIdInfoCodeAndModels(allRelObjectIds, allRelInfoCodes);
        // 填充聚合根信息点值
        for (InfoCodeAggregate aggregate : aggregates) {
            InfoCodeIdentifier identifier = aggregate.getIdentifier();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            InfoCodeTemplateTypeModel templateTypeModel = aggregate.getTemplateTypeModel();
            InfoCodeTackLogicIotInfoCodeHandlerModel tackValueLogicModel =
                    cacheTemplateTypeAndTackValueLogic.get(templateTypeModel);
            if (Objects.isNull(tackValueLogicModel)) {
                continue;
            }
            Set<String> infoCodes = tackValueLogicModel.getInfoCodes();
            String takeValueExpression = tackValueLogicModel.getTakeValueExpression();
            Object dataValue = null;
            if (tackValueLogicModel.isExecute()) {
                Set<String> relObjectIds = identifierAndRelObjectIds.getOrDefault(identifier, Collections.emptySet());
                // 填充信息点和模型映射
                Map<String, List<InfoCodeDataModel>> infoCodeAndModels = new HashMap<>();
                for (String tackInfoCode : infoCodes) {
                    List<InfoCodeDataModel> infoCodeDataModels = new ArrayList<>();
                    relObjectIds.forEach(relObjectId -> infoCodeDataModels.addAll(objectIdInfoCodeAndModels
                            .getOrDefault(relObjectId, Collections.emptyMap())
                            .getOrDefault(tackInfoCode, Collections.emptyList())));
                    infoCodeAndModels.put(tackInfoCode, infoCodeDataModels);
                }
                Expression expression = SpelUtils.getSpelExpressionParser().parseExpression(takeValueExpression);
                StandardEvaluationContext context = SpelUtils.getDefaultStandardEvaluationContext(infoCodeAndModels);
                infoCodeAndModels.forEach(context::setVariable);
                dataValue = expression.getValue(context);
            }
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, dataValue, null, null));
        }
    }

    /**
     * 查询对象ID信息点和模型映射
     *
     * @return 对象ID -> [信息点编码 -> 信息点值模型]
     * @since 5.241101.0
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, List<InfoCodeDataModel>>> mapObjectIdInfoCodeAndModels(
            @NonNull Set<String> objectIds, @NonNull Set<String> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Maps.newHashMap();
        }
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(objectIds, infoCodes);
        // 解析IOT信息点值
        Map<String, Map<String, List<InfoCodeDataModel>>> objectIdInfoCodeAndModels = new HashMap<>();
        if (CollectionUtils.isNotEmpty(infoCodeValues)) {
            infoCodeValues.forEach(item -> MapExecutor.dynamicPut(objectIdInfoCodeAndModels,
                    item.getObjectId(), item.getInfoCode(),
                    iotConverter.toInfoCodeDataModel(item, null),
                    HashMap::new, ArrayList::new));
        }
        return objectIdInfoCodeAndModels;
    }

    /**
     * 查询占位符数据源ID值Map
     *
     * @param source    占位符数据源
     * @param objectIds 对象ID, 存在时将替换查询参数中的占位符
     * @return 占位符 -> 占位符ID值
     * @since 5.241101.0
     */
    private Map<String, Object> queryIdSourceMap(
            @NonNull Map<String, DtpRwdObjectQueryParam> source,
            @Nullable Collection<String> objectIds) {
        if (MapUtils.isEmpty(source)) {
            return Maps.newHashMap();
        }
        Map<String, Object> sourceMap = new HashMap<>();
        for (Map.Entry<String, DtpRwdObjectQueryParam> entry : source.entrySet()) {
            String key = entry.getKey();
            DtpRwdObjectQueryParam queryParam = entry.getValue();
            if (CollectionUtils.isNotEmpty(objectIds)) {
                queryParam = queryParam.replacePlaceholder(
                        Collections.singletonMap(OBJECT_ID, objectIds));
            }
            Object value = null;
            if (Objects.nonNull(queryParam)) {
                List<DtpRwdObjectDTO> sourceObjects = dtpRwdClientService.listObject(queryParam);
                Set<String> sourceObjectIds = sourceObjects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                value = sourceObjectIds.size() == 1 ? IterableUtils.first(sourceObjectIds) : sourceObjectIds;
            }
            sourceMap.put(key, value);
        }
        return sourceMap;
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-基础信息点";
    }
}
