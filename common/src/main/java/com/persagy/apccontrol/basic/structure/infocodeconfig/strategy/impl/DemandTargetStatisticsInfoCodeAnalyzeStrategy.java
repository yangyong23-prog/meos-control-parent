package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.calculate.CalculateResult;
import com.persagy.apccontrol.basic.model.calculate.CalculateResultDetail;
import com.persagy.apccontrol.basic.model.entity.RcCalculateResult;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeFigureVO;
import com.persagy.apccontrol.basic.resource.RcCalculateResultService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTackLogicDemandTargetModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
 * 需求目标统计信息点解析策略
 *
 * @author LiuYang
 * @since 5.240702.0
 */
@Slf4j
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component(DemandTargetStatisticsInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandTargetStatisticsInfoCodeAnalyzeStrategy
        extends BaseDemandTargetInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "equipmentDemandStatisticsInfoCodeAnalyzeStrategy";

    private final AbstractCommonServer abstractCommonServer;
    private final RcCalculateResultService rcCalculateResultService;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.DEMAND_TARGET_STATISTIC;
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }

        // 获取配置的取值逻辑
        List<InfoCodeTackLogicDemandTargetModel> demandTargetModels = aggregates.stream()
                .map(item -> item.getConfigModel().parseTackValueLogicModelByDemandType())
                .distinct()
                .collect(Collectors.toList());

        // 将过滤条件非空的转成map，如果过滤条件为空则直接取值为空代表无取值类型
        Map<InfoCodeTackLogicDemandTargetModel.TargetTypeEnum, String> targetTypeEnumAndFilterTypeMap =
                demandTargetModels.stream()
                        .filter(x -> Objects.nonNull(x.getFilterType()))
                        .collect(Collectors.toMap(InfoCodeTackLogicDemandTargetModel::getCode,
                                InfoCodeTackLogicDemandTargetModel::getFilterType, (o1, o2) -> o1));

        // 获取所有的配置信息点
        List<InfoCodeTackLogicDemandTargetModel.TargetTypeEnum> typeEnum =
                demandTargetModels.stream()
                        .map(InfoCodeTackLogicDemandTargetModel::getCode)
                        .distinct()
                        .collect(Collectors.toList());

        // 获取所有的空间id
        List<String> spaceIds = aggregates.stream()
                .map(item -> item.getIdentifier().getObject().getId())
                .distinct()
                .collect(Collectors.toList());
        // 获取当前时间往前推24小时
        List<String> workDayList = DateUtils.get24WithHourDay();
        String projectId = ContextUtil.getProjectId();
        // 价值计算结果
        List<RcCalculateResult> rcCalculateResults = this.rcCalculateResultService.queryMultiDayCalculateResultList(
                ContextUtil.getGroupCode(), projectId, spaceIds, workDayList, null);
        if (CollectionUtils.isEmpty(rcCalculateResults)) {
            logProjectInstance(projectId).warn("未查询到价值计算结果", Arrays.asList(
                    Pair.of("spaceIds", spaceIds),
                    Pair.of("date", workDayList)
            ));
            return;
        }

        // 云边取值范围不一样 云端24小时，边缘端2小时
        LinkedList<LocalDateTime> fiveMinutesWithTimeRange = this.abstractCommonServer.getFiveMinuteAccuracyTime();
        // 价值计算结果
        HashMap<String, HashMap<String, LinkedList<InfoCodeFigureVO>>> calculateResultMap =
                analyzeCalculateValueTypeBy24Hover(spaceIds, fiveMinutesWithTimeRange,
                        rcCalculateResults, typeEnum, targetTypeEnumAndFilterTypeMap);

        // 重组数据响应前端
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            LinkedList<InfoCodeFigureVO> calculateList = new LinkedList<>();
            if (calculateResultMap.containsKey(objectId)) {
                HashMap<String, LinkedList<InfoCodeFigureVO>> calculateMap = calculateResultMap.get(objectId);
                if (calculateMap.containsKey(infoCode)) {
                    calculateList = calculateMap.get(infoCode);
                }
            }
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, calculateList, null, null));
        }
    }


    /**
     * 解析需求状态
     *
     * @param spaceIds                 空间ID集合
     * @param fiveMinutesWithTimeRange 五分钟集合
     * @param rcCalculateResults       价值计算结果详情
     * @return 需求状态
     * @since 5.240502.0
     */
    private HashMap<String, HashMap<String, LinkedList<InfoCodeFigureVO>>> analyzeCalculateValueTypeBy24Hover(
            @NonNull List<String> spaceIds,
            @NonNull LinkedList<LocalDateTime> fiveMinutesWithTimeRange,
            @NonNull List<RcCalculateResult> rcCalculateResults,
            List<InfoCodeTackLogicDemandTargetModel.TargetTypeEnum> typeEnum,
            Map<InfoCodeTackLogicDemandTargetModel.TargetTypeEnum, String> targetTypeEnumAndFilterTypeMap) {

        // 价值计算结果转map集合，空间id->工作日->价值计算结果
        Map<String, Map<String, CalculateResult>> resultMapBySpaceIdAndWorkDate = rcCalculateResults.stream()
                .collect(Collectors.groupingBy(
                        RcCalculateResult::getSpaceId,
                        Collectors.toMap(
                                RcCalculateResult::getWorkDate,
                                RcCalculateResult::getCalculateResult
                        )
                ));


        HashMap<String, HashMap<String, LinkedList<InfoCodeFigureVO>>> objectObjectHashMap = new HashMap<>(16);
        // 根据空间id进行循环取响应前端的结果集
        spaceIds.forEach(spaceId -> {
            HashMap<String, LinkedList<InfoCodeFigureVO>> dataMap = new HashMap<>(16);
            // 获取空间id所有的日期map集合
            if (resultMapBySpaceIdAndWorkDate.containsKey(spaceId)) {
                setRcCalculateResultDataByTimeList(resultMapBySpaceIdAndWorkDate.get(spaceId),
                        fiveMinutesWithTimeRange, dataMap, typeEnum, targetTypeEnumAndFilterTypeMap);
            }
            objectObjectHashMap.put(spaceId, dataMap);
        });

        return objectObjectHashMap;
    }

    /**
     * 设置价值计算结果根据时间集合
     *
     * @param resultMap                按时间分组的价值计算结果
     * @param fiveMinutesWithTimeRange 时间数组
     * @param dataMap                  数据结果集合
     * @param typeEnum                 价值计算类型结果
     */
    private void setRcCalculateResultDataByTimeList(Map<String, CalculateResult> resultMap,
                                                    List<LocalDateTime> fiveMinutesWithTimeRange,
                                                    HashMap<String, LinkedList<InfoCodeFigureVO>> dataMap,
                                                    List<InfoCodeTackLogicDemandTargetModel.TargetTypeEnum> typeEnum,
                                                    Map<InfoCodeTackLogicDemandTargetModel.TargetTypeEnum, String> targetTypeEnumAndFilterTypeMap) {
        if (MapUtils.isEmpty(resultMap)) {
            return;
        }
        for (LocalDateTime minute : fiveMinutesWithTimeRange) {
            String day = minute.format(DateUtils.DTF_SDFDAY);
            typeEnum.forEach(x -> {
                String filterType = targetTypeEnumAndFilterTypeMap.getOrDefault(x, null);
                String value = x.getValue();
                TreeMap<LocalTime, CalculateResultDetail> detailTreeMap = new TreeMap<>();
                if (resultMap.containsKey(day)) {
                    CalculateResult calculateResult = resultMap.get(day);
                    if (InfoCodeEnum.SPACE_TDB.value().equals(value)) {
                        detailTreeMap = calculateResult.getTdb();
                    } else {
                        detailTreeMap = calculateResult.getCO2();
                    }
                }
                LinkedList<InfoCodeFigureVO> valueList = dataMap.getOrDefault(value, new LinkedList<>());
                LocalTime key = minute.toLocalTime();
                CalculateResultDetail calculateResultDetail = MapUtils.isNotEmpty(detailTreeMap) ? detailTreeMap.get(key) : null;
                targetValueFilter(minute, calculateResultDetail, valueList, filterType);
                dataMap.put(value, valueList);
            });
        }
    }

    /**
     * 目标值
     *
     * @param calculateResultDetail 价值计算的详情
     * @param dataList              结果集
     * @param filterType            过滤条件
     */
    private void targetValueFilter(LocalDateTime minute,
                                   CalculateResultDetail calculateResultDetail,
                                   LinkedList<InfoCodeFigureVO> dataList,
                                   String filterType) {
        if (Objects.nonNull(calculateResultDetail) && calculateResultDetail.getEnable()) {
            if (StringUtils.isNotBlank(filterType)) {
                if (filterType.equals(calculateResultDetail.getType())) {
                    dataList.add(new InfoCodeFigureVO(minute, calculateResultDetail.getValue()));
                } else {
                    dataList.add(new InfoCodeFigureVO(minute, null));
                }
            } else {
                dataList.add(new InfoCodeFigureVO(minute, calculateResultDetail.getValue()));
            }
        } else {
            dataList.add(new InfoCodeFigureVO(minute, null));
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-需求目标信息点统计";
    }
}
