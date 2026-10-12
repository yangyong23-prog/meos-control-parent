package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeTypeCodeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeInfoCodeByTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeInfoCodeTagResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.iot.IotConverter;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据字典报警消息类型
 *
 * @author JiYinchuan
 * @since 7.251202.0
 */
@SuppressWarnings({"unchecked", "rawtypes", "SpringJavaInjectionPointsAutowiringInspection"})
@Component(AlarmMessageAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AlarmMessageAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "alarmMessageAnalyzeStrategy";
    private final IotConverter iotConverter;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.KNOWLEDGE_ALARM_MESSAGE;
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        Set<String> classCodes = aggregates.stream()
                .map(item -> item.getIdentifier().getObject().getClassCode())
                .collect(Collectors.toSet());
        DtpKnowledgeInfoCodeByTagQueryParam.TagParam tagParam = new DtpKnowledgeInfoCodeByTagQueryParam.TagParam();
        tagParam.setParamType(0);
        tagParam.setTypeCode(DtpKnowledgeTypeCodeEnum.X2_TAG.getValue());
        tagParam.setTagCode(Lists.newArrayList("100"));
        DtpKnowledgeInfoCodeByTagQueryParam tagQueryParam = new DtpKnowledgeInfoCodeByTagQueryParam();
        tagQueryParam.setClassCode(classCodes);
        tagQueryParam.setTagParams(Collections.singletonList(tagParam));
        Map<String, List<DtpKnowledgeInfoCodeTagResponse>> classCode2Tags =
                dtpKnowledgeClientService.listInfoByTag(tagQueryParam);
        if (MapUtils.isEmpty(classCode2Tags)) {
            return;
        }
        Map<String, Map<String, Integer>> classCode2InfoCodeSorting = new HashMap<>();
        Map<String, Set<String>> classCode2ObjectIds = new HashMap<>();
        Map<String, String> objectId2ClassCode = new HashMap<>();
        classCode2Tags.forEach((classCode, tags) -> {
            for (DtpKnowledgeInfoCodeTagResponse tag : tags) {
                Map<String, List<String>> infoCodeTag = tag.getTags();
                Integer sorting = null;
                if (MapUtils.isNotEmpty(infoCodeTag)) {
                    List<String> list = infoCodeTag.get("infosorting");
                    if (CollectionUtils.isNotEmpty(list)) {
                        sorting = Integer.parseInt(list.get(0));
                    }
                }
                MapExecutor.dynamicPut(classCode2InfoCodeSorting, classCode, tag.getCode(), sorting, HashMap::new);
            }
        });
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String classCode = aggregate.getIdentifier().getObject().getClassCode();
            MapExecutor.dynamicPut(classCode2ObjectIds, classCode, objectId, LinkedHashSet::new);
            objectId2ClassCode.put(objectId, classCode);
        }
        Map<String, Set<String>> objectId2ExistsRuleInfoCodes = abstractIotDataCacheServer
                .mapObjectIdAndExistsRuleInfoCodes(classCode2ObjectIds, classCode2InfoCodeSorting.values().stream()
                        .map(Map::keySet).flatMap(Set::stream).collect(Collectors.toSet()));
        if (MapUtils.isEmpty(objectId2ExistsRuleInfoCodes)) {
            return;
        }

        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCode2InfoCodeDefine = new ConcurrentHashMap<>();
        Map<String, Map<String, InfoCodeValueVO>> objectId2InfoCodeValues = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : classCode2ObjectIds.entrySet()) {
            Set<String> objectIds = entry.getValue();
            Set<String> allInfoCodes = new HashSet<>();
            for (String objectId : objectIds) {
                Set<String> infoCodes = objectId2ExistsRuleInfoCodes.get(objectId);
                if (CollectionUtils.isNotEmpty(infoCodes)) {
                    allInfoCodes.addAll(infoCodes);
                }
            }

            futures.add(CompletableFuture.runAsync(() -> {
                List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer
                        .listInfoCodeValueInOneHour(objectIds, allInfoCodes);
                if (CollectionUtils.isEmpty(infoCodeValues)) {
                    return;
                }
                infoCodeValues.forEach(item -> MapExecutor.dynamicPut(objectId2InfoCodeValues,
                        item.getObjectId(), item.getInfoCode(), item, HashMap::new));
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        futures.add(CompletableFuture.runAsync(() -> classCode2InfoCodeDefine.putAll(dtpKnowledgeClientService.
                mapClassCodeInfoCodeAndDefine(classCode2ObjectIds.keySet())), BusinessThreadPoolExecutor.ttlExecutor()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        Map<String, List<InfoCodeValueVO>> result = new HashMap<>();
        objectId2ExistsRuleInfoCodes.forEach((objectId, infoCodes) -> {
            String classCode = objectId2ClassCode.get(objectId);
            if (StringUtils.isBlank(classCode)) {
                return;
            }
            Map<String, DtpKnowledgeInfoCodeDTO> infoCodeDefines = classCode2InfoCodeDefine.get(classCode);
            if (MapUtils.isEmpty(infoCodeDefines)) {
                return;
            }
            List<InfoCodeValueVO> infoCodeValues = infoCodes.stream().map(item -> {
                InfoCodeValueVO infoCodeValue = MapExecutor.dynamicGet(objectId2InfoCodeValues, objectId, item);
                // 没有值时新建对象
                infoCodeValue = Optional.ofNullable(infoCodeValue).orElseGet(() ->
                        new InfoCodeValueVO().setObjectId(objectId).setInfoCode(item));
                // 填充信息点定义
                iotConverter.fill(infoCodeValue, infoCodeDefines.get(item));
                return infoCodeValue;
            }).collect(Collectors.toList());

            // 对信息点进行排序
            Map<String, Integer> infoCode2Sorting = classCode2InfoCodeSorting.getOrDefault(classCode, Collections.emptyMap());
            infoCodeValues.sort(Comparator.comparingInt(item ->
                    infoCode2Sorting.getOrDefault(item.getInfoCode(), Integer.MAX_VALUE)));
            result.put(objectId, infoCodeValues);
        });
        this.fillAggregateDataValueByMap(aggregates, result);
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-报警消息";
    }
}
