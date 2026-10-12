package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.kitchen.KitchenRangeHoodDemandTimeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherSunRiseAndSetDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.kitchen.KitchenRangeHoodDemandTimeQueryParam;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitchenRangeHoodDemandTimeVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel1;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.kitchen.DemandConfigKitchenRangeHoodValueObject;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component(RunningMonitorKitChenRangeHoodStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorKitChenRangeHoodStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {
    public static final String BEAN_NAME = "runningMonitorKitChenRangeHoodStrategy";
    public static final String LOG_TAG = "运行监测-厨房油烟机策略";

    /**
     * 需求配置网关
     *
     * @since v7.261001.0
     */
    private final DemandConfigGateway<DemandConfigKitchenRangeHoodValueObject> demandConfigGateway;

    /**
     * 天气服务
     *
     * @since v7.261001.0
     */
    private final WeatherClientService weatherClientService;

    // ------------------------- list -------------------------

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(
            @NotNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NotNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        return Collections.emptyList();
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(RunningMonitorSpaceQueryParam param) {
        //同时满足以下条件：
        //①该项目下设备类=ACKLSF，且风机定位（fanPos）=1（集中风机）；
        //②设备有服务空间的关系：图类型“机电类服务区域关系”（MechForArch），边类型“设备服务于空间”（Eq2Sp），from设备，to空间
        //③服务空间的信息点“是否需要排油烟（needExaustSmoke）=1(是)”的空间
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        DtpRwdObjectQueryParam eqQueryParam = new DtpRwdObjectQueryParam();
        eqQueryParam.setClassCode(DtpRwdClassCodeEnum.ACKLSF);
        eqQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        eqQueryParam.addInfoCode(InfoCodeEnum.FAN_POS.getCode(), "1");
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = equipmentQueryParam.queryObject(eqQueryParam);
        //得到所有风机id
        Set<String> equipIds = dtpRwdEquipmentDTOS.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
        spaceQueryParam.addInfoCode(InfoCodeEnum.NEED_EXAUST_SMOKE.getCode(), "1");
        // 调用方传入查询字段时以其为准, 未传入时保持原有简单字段
        String[] withColumns = param.toWithColumnArray();
        if (ArrayUtils.isNotEmpty(withColumns)) {
            spaceQueryParam.addWithColumn(withColumns);
        } else {
            spaceQueryParam.addWithColumn(DtpRwdSpaceDTO.listSimpleWithColumns());
        }
        param.setFilterSpaceServiceEquipmentIds(equipIds);
        return param.queryObject(spaceQueryParam);
    }

    /**
     * 查询厨房排油烟需求时间(风机与服务空间)
     *
     * <p>根据传入的空间与空间关联设备, 查询当天生效的厨房排油烟需求配置(含字段级继承),
     * 将空间与设备的合并需求时间原位回填到入参对应的节点。</p>
     *
     * @param param 查询参数, 包含空间与其关联设备
     * @return 需求时间结果, 空间与设备节点均回填合并后的需求时段
     * @since v7.261001.0
     */
    public @NonNull KitchenRangeHoodDemandTimeVO listDemandTime(@NonNull KitchenRangeHoodDemandTimeQueryParam param) {
        KitchenRangeHoodDemandTimeVO result = new KitchenRangeHoodDemandTimeVO();
        // 入参非空与节点ID非空由 @NotEmpty/@NotBlank 校验保证
        List<KitchenRangeHoodDemandTimeDTO> spaceNodes = param.getSpaces();
        result.setSpaces(spaceNodes);
        // 项目未查询到需求模板时, 按契约返回空需求时间, 不中断响应
        LocalDate date = DateUtils.now().toLocalDate();
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(DemandTemplateTypeEnum.KITCHEN_RANGE_HOOD);
        templateParam.setDate(date);
        DemandTemplate template = templateParam.queryAndGetTemplate();
        if (Objects.isNull(template)) {
            super.logProjectInstance().warn("未查询到厨房排油烟需求模板, 返回空需求时间", Pair.of("date", date));
            spaceNodes.forEach(KitchenRangeHoodDemandTimeDTO::fillEmptyDemandTimes);
            return result;
        }

        // 批量查询空间级配置(含字段级继承), 装配 空间ID -> 排油烟需求时间区间;
        // 必须走实时配置通道, 预计算结果表未接入厨房排油烟需求
        Set<String> spaceIds = spaceNodes.stream()
                .map(KitchenRangeHoodDemandTimeDTO::getId)
                .collect(Collectors.toSet());
        DemandConfigByInstanceBatchQueryParam configQueryParam = new DemandConfigByInstanceBatchQueryParam();
        configQueryParam.setDate(date);
        configQueryParam.setTemplateId(template.getId());
        configQueryParam.setInstanceTypeAndInstanceIds(
                Collections.singletonMap(DemandInstanceTypeEnum.SPACE, spaceIds));
        List<DemandConfigAggregate<DemandConfigKitchenRangeHoodValueObject>> aggregates =
                this.demandConfigGateway.listConfigsByInstances(configQueryParam);
        Map<String, List<DemandTimeIntervalValueObject>> spaceIdAndIntervals =
                this.parseSpaceIdAndIntervals(aggregates);

        // 仅在存在日出日落类型时段时才查询日出日落时间, 避免无谓的远程调用
        WeatherSunRiseAndSetDTO sunRiseAndSet = null;
        if (this.needSunRiseAndSet(spaceIdAndIntervals.values())) {
            sunRiseAndSet = this.weatherClientService.getSunRiseAndSetCache(ContextUtil.getProjectId(), date);
            if (Objects.isNull(sunRiseAndSet)) {
                super.logProjectInstance().warn("未查询到日出日落时间, 对应需求时段将被跳过", Pair.of("date", date));
            }
        }

        // 空间需求时间 = 空间自身配置的解析合并结果; 同一空间重复传入时按一次查询处理
        Map<String, List<DemandTimeIntervalValueObject>> spaceIdAndMerged =
                new HashMap<>(spaceIds.size());
        for (String spaceId : spaceIds) {
            spaceIdAndMerged.put(spaceId, DemandTimeIntervalValueObject.merge(
                    spaceIdAndIntervals.get(spaceId), sunRiseAndSet));
        }

        // 设备需求时间 = 其关联空间(由入参推导, 可能多个)合并结果汇总后二次归并
        Map<String, List<DemandTimeIntervalValueObject>> equipmentIdAndMerged = new HashMap<>();
        for (KitchenRangeHoodDemandTimeDTO spaceNode : spaceNodes) {
            List<DemandTimeIntervalValueObject> spaceMerged = spaceIdAndMerged.get(spaceNode.getId());
            spaceNode.fillDemandTimes(spaceMerged);
            for (KitchenRangeHoodDemandTimeDTO equipmentNode : spaceNode.listRelEquipments()) {
                equipmentIdAndMerged.computeIfAbsent(equipmentNode.getId(), key -> new ArrayList<>())
                        .addAll(spaceMerged);
            }
        }
        equipmentIdAndMerged.replaceAll(
                (equipmentId, mergedIntervals) -> DemandTimeIntervalValueObject.mergeIntervals(mergedIntervals));
        // 需求时间原位回填到 space 与 equipment 节点, 保持与入参一致的顺序
        for (KitchenRangeHoodDemandTimeDTO spaceNode : spaceNodes) {
            for (KitchenRangeHoodDemandTimeDTO equipmentNode : spaceNode.listRelEquipments()) {
                equipmentNode.fillDemandTimes(equipmentIdAndMerged.get(equipmentNode.getId()));
            }
        }
        return result;
    }

    // ------------------------- 其他 -------------------------

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.KITCHEN_RANGE_HOOD;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition() {
        //空间异常事件，通过”风控目标“进行过滤
        //”一级风控目标=“品质” & 二级风控目标=“偏冷”、二级风控目标=“偏热”、二级风控目标=“空气质量差”
        return x -> x.stream().filter(item -> {
            boolean riskConditions = item.getObjectType().equals(ObjectTypeEnum.SPACE.value())
                    && CollectionUtils.isNotEmpty(item.getRiskMeasureAimLevel1Collect())
                    && item.getRiskMeasureAimLevel1Collect().stream().anyMatch(Lists.newArrayList(RiskMeasureAimLevel1.E1.getCode())::contains)
                    && CollectionUtils.isNotEmpty(item.getRiskMeasureAimLevel2Collect())
                    && item.getRiskMeasureAimLevel2Collect().stream().anyMatch(Lists.newArrayList(RiskMeasureAimLevel2.E35.getCode(),
                    RiskMeasureAimLevel2.E34.getCode(), RiskMeasureAimLevel2.E5.getCode())::contains);
            return riskConditions || item.getObjectType().equals(ObjectTypeEnum.EQUIPMENT.value());
        }).collect(Collectors.toList());
    }

    @Override
    public Collection<RiskMeasureAimLevel2> exceptionFilterRiskMeasureAimLevel2() {
        return Lists.newArrayList(RiskMeasureAimLevel2.E5, RiskMeasureAimLevel2.E34, RiskMeasureAimLevel2.E35);
    }

    // ------------------------- 私有辅助 -------------------------

    /**
     * 解析配置聚合列表, 装配 空间ID -> 排油烟需求时间区间
     *
     * @param aggregates 配置聚合列表, 可为 {@literal null}
     * @return 空间ID与排油烟需求时间区间的映射, 永不为 {@literal null}
     * @since v7.261001.0
     */
    private @NonNull Map<String, List<DemandTimeIntervalValueObject>> parseSpaceIdAndIntervals(
            @Nullable List<DemandConfigAggregate<DemandConfigKitchenRangeHoodValueObject>> aggregates) {
        Map<String, List<DemandTimeIntervalValueObject>> result = new HashMap<>();
        if (CollectionUtils.isEmpty(aggregates)) {
            return result;
        }
        for (DemandConfigAggregate<DemandConfigKitchenRangeHoodValueObject> aggregate : aggregates) {
            if (Objects.isNull(aggregate.getFinalConfig()) || Objects.isNull(aggregate.getIdentifier())) {
                continue;
            }
            // finalConfig 已合成本级配置与继承链上级配置, globalConfig 即当前生效的排油烟配置
            DemandConfigKitchenRangeHoodValueObject globalConfig = aggregate.getFinalConfig().getGlobalConfig();
            if (Objects.isNull(globalConfig) || CollectionUtils.isEmpty(globalConfig.getExhaustTimeIntervals())) {
                continue;
            }
            result.put(aggregate.getIdentifier().getInstance().getInstanceId(),
                    globalConfig.getExhaustTimeIntervals());
        }
        return result;
    }

    /**
     * 判断是否存在日出日落类型的需求时间
     *
     * @param intervalCollections 各空间的需求时间区间集合
     * @return 是否存在日出日落类型的需求时间
     * @since v7.261001.0
     */
    private boolean needSunRiseAndSet(@NonNull Collection<List<DemandTimeIntervalValueObject>> intervalCollections) {
        return intervalCollections.stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .anyMatch(DemandTimeIntervalValueObject::isSunRiseAndSetTime);
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
