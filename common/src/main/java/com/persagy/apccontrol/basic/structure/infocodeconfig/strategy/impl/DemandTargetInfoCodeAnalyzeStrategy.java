package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.calculate.CalculateResult;
import com.persagy.apccontrol.basic.model.calculate.CalculateResultDetail;
import com.persagy.apccontrol.basic.model.entity.RcCalculateResult;
import com.persagy.apccontrol.basic.resource.RcCalculateResultService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTackLogicDemandTargetModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求目标信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Component(DemandTargetInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandTargetInfoCodeAnalyzeStrategy
        extends BaseDemandTargetInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "equipmentDemandTargetInfoCodeAnalyzeStrategy";

    private final RcCalculateResultService rcCalculateResultService;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.DEMAND_TARGET;
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        List<String> spaceIds = aggregates.stream()
                .map(item -> item.getIdentifier().getObject().getId())
                .distinct()
                .collect(Collectors.toList());

        List<InfoCodeTackLogicDemandTargetModel> demandTargetModels = aggregates.stream()
                .map(item -> item.getConfigModel().parseTackValueLogicModelByDemandType())
                .distinct()
                .collect(Collectors.toList());

        Map<InfoCodeTackLogicDemandTargetModel.TargetTypeEnum, String> targetTypeEnumAndFilterTypeMap =
                demandTargetModels.stream()
                        .filter(x -> Objects.nonNull(x.getFilterType()))
                        .collect(Collectors.toMap(InfoCodeTackLogicDemandTargetModel::getCode,
                                InfoCodeTackLogicDemandTargetModel::getFilterType, (o1, o2) -> o1));

        List<InfoCodeTackLogicDemandTargetModel.TargetTypeEnum> typeEnum =
                demandTargetModels.stream()
                        .map(InfoCodeTackLogicDemandTargetModel::getCode)
                        .distinct()
                        .collect(Collectors.toList());
        LocalDateTime now = DateUtils.now();
        String format = now.format(DateUtils.DTF_SDFDAY);
        LocalTime time = now.toLocalTime();
        String projectId = ContextUtil.getProjectId();
        // 价值计算结果
        List<RcCalculateResult> rcCalculateResults = rcCalculateResultService.queryMultiDayCalculateResultList(
                ContextUtil.getGroupCode(), projectId, spaceIds, Lists.newArrayList(format), null);
        if (CollectionUtils.isEmpty(rcCalculateResults)) {
            logProjectInstance(projectId).warn("未查询到价值计算结果", Arrays.asList(
                    Pair.of("spaceIds", spaceIds),
                    Pair.of("date", format)
            ));
            return;
        }

        HashMap<String, HashMap<String, String>> dataMap = new HashMap<>(16);

        for (RcCalculateResult rcCalculateResult : rcCalculateResults) {
            String spaceId = rcCalculateResult.getSpaceId();
            String workDate = rcCalculateResult.getWorkDate();
            CalculateResult calculateResult = rcCalculateResult.getCalculateResult();
            if (Objects.isNull(calculateResult)) {
                continue;
            }
            HashMap<String, String> result = new HashMap<>();
            typeEnum.forEach(x -> {
                String type = x.getValue();
                TreeMap<LocalTime, CalculateResultDetail> convert;
                if (InfoCodeEnum.SPACE_TDB.value().equals(type)) {
                    convert = calculateResult.getTdb();
                } else {
                    convert = calculateResult.getCO2();
                }
                result.put(type,
                        analyzeCalculateValueType(projectId, spaceId, workDate, time, convert,
                                targetTypeEnumAndFilterTypeMap.getOrDefault(x, null)));
            });
            dataMap.put(spaceId, result);
        }


        // 重组数据响应前端
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            InfoCodeTackLogicDemandTargetModel.TargetTypeEnum type =
                    aggregate.getConfigModel().parseTackValueLogicModelByDemandType().getCode();
            String value = null;
            if (dataMap.containsKey(objectId)) {
                HashMap<String, String> valueMap = dataMap.get(objectId);
                value = valueMap.getOrDefault(type.getValue(), "");
            }
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, value, null, null));
        }
    }

    /**
     * 解析需求状态
     *
     * @param projectId 项目ID
     * @param spaceId   空间ID
     * @param workDate  计算日期
     * @param time      计算视角
     * @param source    价值计算结果详情
     * @return 需求状态
     * @since 5.240502.0
     */
    private String analyzeCalculateValueType(@NonNull String projectId,
                                             @NonNull String spaceId,
                                             @NonNull String workDate,
                                             @NonNull LocalTime time,
                                             @NonNull TreeMap<LocalTime, CalculateResultDetail> source,
                                             @Nullable String filterType) {
        if (MapUtils.isEmpty(source)) {
            return null;
        }
        LocalTime recent5MinuteEnd = DateUtils.getRecent5Minute(time);
        Map.Entry<LocalTime, CalculateResultDetail> entry = source.floorEntry(recent5MinuteEnd);
        if (Objects.isNull(entry)) {
            logProjectInstance(projectId).debug("最近五分钟无价值计算结果", Arrays.asList(
                    Pair.of("spaceId", spaceId),
                    Pair.of("workDate", workDate),
                    Pair.of("recent5MinuteBegin", recent5MinuteEnd),
                    Pair.of("recent5MinuteEnd", recent5MinuteEnd)
            ));
            return null;
        }
        LocalTime calculateTime = entry.getKey();
        CalculateResultDetail detail = entry.getValue();
        if (Objects.isNull(detail)) {
            return null;
        }
        Boolean enable = detail.getEnable();
        if (BooleanUtils.isNotTrue(enable)) {
            logProjectInstance(projectId).debug("最近五分钟价值计算无需求", Arrays.asList(
                    Pair.of("spaceId", spaceId),
                    Pair.of("workDate", workDate),
                    Pair.of("calculateTime", calculateTime)
            ));
            return null;
        }
        String type = detail.getType();
        if (StringUtils.isBlank(type)) {
            return null;
        }
        return targetValueFilter(detail, filterType);
    }

    /**
     * 目标值
     *
     * @param calculateResultDetail 价值计算的详情
     * @param filterType            过滤条件
     */
    private String targetValueFilter(CalculateResultDetail calculateResultDetail,
                                     String filterType) {
        if (calculateResultDetail.getEnable()) {
            if (StringUtils.isNotBlank(filterType)) {
                if (filterType.equals(calculateResultDetail.getType())) {
                    return String.valueOf(calculateResultDetail.getValue());
                }
            } else {
                return String.valueOf(calculateResultDetail.getValue());
            }
        }
        return null;
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-IOT信息点统计";
    }
}
