package com.persagy.apccontrol.basic.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyDayDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyPlanDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyCfg;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyDateCfg;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyPlanCfg;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.freezepumpgroup.DtpRwdFreezePumpGroupInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyBusinessTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyTypeEnum;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFixedQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFreezeGroupObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdTwinObjectRuleQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdTwinObjectRuleResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.StrategyCfgService;
import com.persagy.apccontrol.basic.resource.StrategyDateCfgService;
import com.persagy.apccontrol.basic.resource.StrategyPlanCfgService;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyCfgQueryDaoParam;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyDateCfgQueryDaoParam;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyPlanCfgQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizStrategyQueryService;
import com.persagy.apccontrol.basic.structure.convert.BizStrategyConvert;
import com.persagy.framework.common.util.json.JsonUtils;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import com.persagy.validate.utility.executor.ObjectExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 策略干预查询Service
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Service
@Slf4j
public class BizStrategyQueryServiceImpl implements BizStrategyQueryService {

    private static final String LOG_TAG = "业务策略查询服务";

    @Autowired
    private BizStrategyConvert bizStrategyConvert;
    @Autowired
    private StrategyCfgService strategyCfgService;
    @Autowired
    private StrategyDateCfgService strategyDateCfgService;
    @Autowired
    private StrategyPlanCfgService strategyPlanCfgService;
    @Autowired
    private DtpRwdClientService dtpRwdClientService;
    @Autowired
    private RedisCacheUtil redisCacheUtil;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public List<DtpRwdFreezePumpDTO> listLastFreezePumpGroups(@NonNull StrategyFreezeGroupObjectQueryParam param) {
        String projectId = param.getProjectId();
        Collection<String> acccSystemIds = param.getSystemIds();
        Collection<String> acatSystemIds = param.getAcatSystemIds();

        // 查询末级泵组对象
        Map<String, DtpRwdFreezePumpDTO> objectIdAndData = queryObjectIdAndData(
                projectId, acccSystemIds, acatSystemIds, param.getObjectId());
        if (MapUtils.isEmpty(objectIdAndData)) {
            return Lists.newArrayList();
        }

        // 查询设定值信息点实例化规则
        String branchDetaTempSet = DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_TEMP_SET.value();
        String branchDetaPressSet = DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_PRESS_SET.value();
        String worstTerminalDetaPressSet = DtpRwdFreezePumpGroupInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.value();
        List<DtpRwdTwinObjectRuleResponse> twinObjectRules = dtpRwdClientService.listTwinObjectRule(
                Lists.newArrayList(new DtpRwdTwinObjectRuleQueryParam(DtpRwdClassCodeEnum.ACCPC_GROUP)));
        if (CollectionUtils.isEmpty(twinObjectRules)) {
            return Lists.newArrayList();
        }

        List<DtpRwdFreezePumpDTO> result = new ArrayList<>();

        // 过滤设定值信息点实例化有值的对象
        Map<String, List<String>> objectIdAndInfoCodes = new HashMap<>(twinObjectRules.size());
        twinObjectRules.forEach(item -> MapExecutor.dynamicPut(objectIdAndInfoCodes,
                item.getObjectId(), item.getInfoCode(), ArrayList::new));
        for (Map.Entry<String, List<String>> entry : objectIdAndInfoCodes.entrySet()) {
            String objectId = entry.getKey();
            List<String> infoCodes = entry.getValue();
            DtpRwdFreezePumpDTO object = objectIdAndData.get(objectId);
            if (Objects.isNull(object)) {
                continue;
            }

            if (!CollectionUtils.containsAny(infoCodes, branchDetaTempSet, branchDetaPressSet, worstTerminalDetaPressSet)) {
                continue;
            }
            AtomicReference<String> unit = new AtomicReference<>();
            AtomicReference<DtpRwdFreezePumpGroupInfoCodeEnum> infoCodeSetType = new AtomicReference<>();
            if (infoCodes.contains(worstTerminalDetaPressSet)) {
                unit.set(getFreezePumpUnit(projectId, worstTerminalDetaPressSet));
                infoCodeSetType.set(DtpRwdFreezePumpGroupInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET);
            } else if (infoCodes.contains(branchDetaTempSet)) {
                unit.set(getFreezePumpUnit(projectId, branchDetaTempSet));
                infoCodeSetType.set(DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_TEMP_SET);
            } else if (infoCodes.contains(branchDetaPressSet)) {
                unit.set(getFreezePumpUnit(projectId, branchDetaPressSet));
                infoCodeSetType.set(DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_PRESS_SET);
            }
            ObjectExecutor.notNullExecute(unit.get(), () -> object.setInfoCodeUnit(unit.get()));
            ObjectExecutor.notNullExecute(infoCodeSetType.get(), () -> object.setInfoCodeSetType(infoCodeSetType.get()));
            result.add(object);
        }
        return result;
    }

    @Override
    public List<StrategyDTO> listFixedStrategy(@NonNull StrategyFixedQueryParam param) {
        param.setStrategyType(StrategyTypeEnum.FIXED_STRATEGY);
        // 查询策略配置
        StrategyCfgQueryDaoParam queryDaoParam = bizStrategyConvert.toQueryDaoParam(param);
        List<StrategyCfg> dbStrategies = queryStrategies(queryDaoParam);
        if (CollectionUtils.isEmpty(dbStrategies)) {
            return Lists.newArrayList();
        }
        List<Long> dbStrategyIds = new ArrayList<>();
        MultiKeyMap<String, List<Long>> multiKeyAndStrategyIds = new MultiKeyMap<>();
        for (StrategyCfg dbStrategy : dbStrategies) {
            dbStrategyIds.add(dbStrategy.getId());
            MultiKey<String> multiKey = new MultiKey<>(dbStrategy.getProjectId(),
                    dbStrategy.getSystemType().value(), dbStrategy.getSystemId(),
                    dbStrategy.getBusinessType().value(), dbStrategy.getObjectId());
            MapExecutor.dynamicPut(multiKeyAndStrategyIds, multiKey, dbStrategy.getId(), ArrayList::new);
        }

        // 查询策略日期
        StrategyDateCfgQueryDaoParam dateQueryDaoParam = bizStrategyConvert.toDateQueryDaoParam(param, dbStrategyIds);
        List<StrategyDateCfg> dbStrategyDateList = strategyDateCfgService.list(dateQueryDaoParam);
        if (CollectionUtils.isEmpty(dbStrategyDateList)) {
            LogExecutor.instance().warn(log, LOG_TAG, "无策略日期配置",
                    Collections.singletonList(Pair.of("dateQueryDaoParam", dateQueryDaoParam)));
            return Lists.newArrayList();
        }
        Map<Long, List<StrategyDateCfg>> strategyIdAndDataList = dbStrategyDateList.stream()
                .collect(Collectors.groupingBy(StrategyDateCfg::getStrategyId));

        // 查询策略计划
        StrategyPlanCfgQueryDaoParam planQueryDaoParam = new StrategyPlanCfgQueryDaoParam();
        planQueryDaoParam.setStrategyIds(dbStrategyIds);
        List<StrategyPlanCfg> dbStrategyPlans = strategyPlanCfgService.list(planQueryDaoParam);
        if (CollectionUtils.isEmpty(dbStrategyPlans)) {
            LogExecutor.instance().warn(log, LOG_TAG, "无策略计划配置",
                    Collections.singletonList(Pair.of("planQueryDaoParam", planQueryDaoParam)));
            return Lists.newArrayList();
        }
        Map<Long, List<StrategyPlanCfg>> strategyIdAndPlanList = dbStrategyPlans.stream()
                .collect(Collectors.groupingBy(StrategyPlanCfg::getStrategyId));
        return getFixedStrategyDTOList(multiKeyAndStrategyIds, strategyIdAndDataList, strategyIdAndPlanList);
    }

    /**
     * 获取末级泵组单位
     *
     * @param projectId 项目ID
     * @param infoCode  信息点
     * @return 末级泵组单位
     * @since v5.6.0
     */
    @Nullable
    private String getFreezePumpUnit(@NonNull String projectId, @NonNull String infoCode) {
        Object value = this.redisCacheUtil.get(RedisKey.buildInfoCodeConfigStringKey(DtpRwdClassCodeEnum.ACCPC_GROUP.value(), infoCode));
        RcInfoCodeCfg infoCodeCfg = value == null ? null : JsonUtils.fromJson(
                JsonUtils.toJson(value), new TypeReference<RcInfoCodeCfg>() {
                });
        if (infoCodeCfg == null) {
            LogExecutor.instance().warn(log, LOG_TAG, "无信息点配置",
                    Collections.singletonList(Pair.of("projectId", projectId)));
            return null;
        }
        return infoCodeCfg.getUnit();
    }

    /**
     * 查询末级泵组对象
     *
     * @param projectId     项目ID
     * @param systemIds     中央供冷系统ID, 为空时查询所有
     * @param acatSystemIds 空调末端系统ID, 为空时查询所有
     * @param objectId      对象ID
     * @return 末级泵组对象
     * @since v5.6.0
     */
    private Map<String, DtpRwdFreezePumpDTO> queryObjectIdAndData(
            @NonNull String projectId,
            @Nullable Collection<String> systemIds,
            @Nullable Collection<String> acatSystemIds,
            @Nullable String objectId) {
        // 查询冷冻水泵控制组
        List<DtpRwdFreezePumpDTO> accpcGroups = dtpRwdClientService.listFreezePumpGroup(false, false,
                projectId, systemIds, acatSystemIds, new DtpRwdObjectQueryParam().setId(objectId), false);
        if (CollectionUtils.isEmpty(accpcGroups)) {
            return Maps.newHashMap();
        }

        // 过滤末级冷冻水泵控制组
        Map<String, DtpRwdFreezePumpDTO> objectIdAndData = new HashMap<>(accpcGroups.size());
        for (DtpRwdFreezePumpDTO accpcGroup : accpcGroups) {
            Boolean isTerminalPump = accpcGroup.getIsTerminalPump();
            if (BooleanUtils.isTrue(isTerminalPump)) {
                objectIdAndData.put(accpcGroup.getId(), accpcGroup);
            }
        }
        if (MapUtils.isEmpty(objectIdAndData)) {
            LogExecutor.instance().warn(log, LOG_TAG, "无末级冷冻水泵控制组对象", Arrays.asList(
                    Pair.of("projectId", projectId),
                    Pair.of("acccSystemIds", systemIds),
                    Pair.of("acatSystemIds", acatSystemIds),
                    Pair.of("accpcGroups", accpcGroups)
            ));
            return Maps.newHashMap();
        }
        return objectIdAndData;
    }

    /**
     * 查询策略配置
     *
     * @param param 查询参数
     * @return 策略配置
     * @since v5.6.0
     */
    private List<StrategyCfg> queryStrategies(@NonNull StrategyCfgQueryDaoParam param) {
        List<StrategyCfg> dbStrategies = strategyCfgService.list(param);
        Set<Long> invalidStrategyIds = clearInvalidObjectStrategy(param.getProjectId(), dbStrategies);
        if (CollectionUtils.isNotEmpty(invalidStrategyIds)) {
            dbStrategies.removeIf(item -> invalidStrategyIds.contains(item.getId()));
        }
        if (CollectionUtils.isEmpty(dbStrategies)) {
            LogExecutor.instance().info(log, LOG_TAG, "无策略配置",
                    Collections.singletonList(Pair.of("param", param)));
            return Lists.newArrayList();
        }
        return dbStrategies;
    }

    /**
     * 获取策略DTO列表
     *
     * @param multiKeyAndStrategyIds [项目ID-系统类型-系统ID-业务类型-对象ID] -> 策略ID
     * @param strategyIdAndDataList  策略ID -> 策略日期
     * @param strategyIdAndPlanList  策略ID -> 策略计划
     * @return 策略DTO列表
     * @since v5.6.0
     */
    private List<StrategyDTO> getFixedStrategyDTOList(
            @NonNull MultiKeyMap<String, List<Long>> multiKeyAndStrategyIds,
            @NonNull Map<Long, List<StrategyDateCfg>> strategyIdAndDataList,
            @NonNull Map<Long, List<StrategyPlanCfg>> strategyIdAndPlanList) {
        List<StrategyDTO> strategies = new ArrayList<>();
        for (Map.Entry<MultiKey<? extends String>, List<Long>> entry : multiKeyAndStrategyIds.entrySet()) {
            MultiKey<? extends String> multiKey = entry.getKey();
            List<Long> strategyIds = entry.getValue();
            TreeSet<StrategyDateCfg> strategyDateList = new TreeSet<>(Comparator.comparing(StrategyDateCfg::getDate));
            for (Long strategyId : strategyIds) {
                if (strategyIdAndDataList.containsKey(strategyId)) {
                    strategyDateList.addAll(strategyIdAndDataList.get(strategyId));
                }
            }
            StrategyDTO strategyDTO = new StrategyDTO();
            strategyDTO.setStrategyType(StrategyTypeEnum.FIXED_STRATEGY);
            strategyDTO.setProjectId(multiKey.getKey(0));
            strategyDTO.setSystemType(BaseEnum.analyze(multiKey.getKey(1), DtpRwdSystemClassCodeEnum.class));
            strategyDTO.setSystemId(multiKey.getKey(2));
            strategyDTO.setBusinessType(BaseEnum.analyze(multiKey.getKey(3), StrategyBusinessTypeEnum.class));
            strategyDTO.setObjectId(multiKey.getKey(4));
            List<StrategyDayDTO> dayPlans = new ArrayList<>();
            for (StrategyDateCfg strategyDate : strategyDateList) {
                List<String> ignorePlanIds = strategyDate.getIgnorePlanIds();
                List<StrategyPlanDTO> strategyPlanDTOList = new ArrayList<>();
                List<StrategyPlanCfg> strategyPlans = strategyIdAndPlanList.get(strategyDate.getStrategyId());
                if (CollectionUtils.isEmpty(strategyPlans)) {
                    continue;
                }
                for (StrategyPlanCfg dbStrategyPlan : strategyPlans) {
                    // 过滤忽略的计划ID
                    if (CollectionUtils.isNotEmpty(ignorePlanIds)
                            && ignorePlanIds.contains(dbStrategyPlan.getId().toString())) {
                        continue;
                    }
                    strategyPlanDTOList.add(bizStrategyConvert.toDto(dbStrategyPlan));
                }
                if (CollectionUtils.isNotEmpty(strategyPlanDTOList)) {
                    dayPlans.add(bizStrategyConvert.toDto(strategyDate, strategyPlanDTOList));
                }
            }
            if (CollectionUtils.isNotEmpty(dayPlans)) {
                strategyDTO.setDayPlans(dayPlans);
                strategies.add(strategyDTO);
            }
        }
        return strategies;
    }

    /**
     * 清除无效对象策略
     *
     * @param projectId  项目ID
     * @param strategies 策略列表
     * @return 无效策略ID
     * @since v5.6.0
     */
    @Nullable
    private Set<Long> clearInvalidObjectStrategy(@NonNull String projectId, @NonNull List<StrategyCfg> strategies) {
        if (CollectionUtils.isEmpty(strategies)) {
            return null;
        }
        Set<String> objectIds = strategies.stream().map(StrategyCfg::getObjectId).collect(Collectors.toSet());
        List<DtpRwdObjectDTO> objects;
        try {
            objects = dtpRwdClientService.listObject(false, true,
                    projectId, new DtpRwdObjectQueryParam().setId(objectIds));
        } catch (Exception e) {
            LogExecutor.instance().error(log, LOG_TAG, "查询物理世界失败, 不进行清空无效对象", e, Arrays.asList(
                    Pair.of("projectId", projectId),
                    Pair.of("strategies", strategies)
            ));
            return null;
        }
        if (objects.size() == objectIds.size()) {
            return null;
        }
        Set<String> rwdObjectIds = objects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Collection<String> invalidObjectIds = CollectionUtils.subtract(objectIds, rwdObjectIds);
        LogExecutor.instance().info(log, LOG_TAG, "检测到无效对象, 开始执行清除流程", Arrays.asList(
                Pair.of("projectId", projectId),
                Pair.of("objectIds", objectIds),
                Pair.of("rwdObjectIds", rwdObjectIds),
                Pair.of("invalidObjectIds", invalidObjectIds)
        ));

        Set<Long> invalidStrategyIds = new HashSet<>();
        Set<StrategyCfg> invalidStrategies = new HashSet<>();
        for (StrategyCfg strategy : strategies) {
            if (invalidObjectIds.contains(strategy.getObjectId())) {
                strategy.setSt(StateTypeEnum.INVALID);
                strategy.clearUpdateParameter();
                invalidStrategyIds.add(strategy.getId());
                invalidStrategies.add(strategy);
            }
        }

        return transactionTemplate.execute(status -> {
            // 将策略配置设置为无效
            strategyCfgService.updateBatchById(invalidStrategies);
            // 将策略日期设置为无效
            List<StrategyDateCfg> invaliStrategyDateList = strategyDateCfgService.list(
                    new StrategyDateCfgQueryDaoParam().setStrategyIds(invalidStrategyIds));
            if (CollectionUtils.isNotEmpty(invaliStrategyDateList)) {
                strategyDateCfgService.updateBatchById(invaliStrategyDateList.stream()
                        .peek(item -> {
                            item.setSt(StateTypeEnum.INVALID);
                            item.clearUpdateParameter();
                        })
                        .collect(Collectors.toList()));
            }
            // 将策略计划设置为无效
            List<StrategyPlanCfg> invalidStrategyPlans = strategyPlanCfgService.list(
                    new StrategyPlanCfgQueryDaoParam().setStrategyIds(invalidStrategyIds));
            if (CollectionUtils.isNotEmpty(invalidStrategyPlans)) {
                strategyPlanCfgService.updateBatchById(invalidStrategyPlans.stream()
                        .peek(item -> {
                            item.setSt(StateTypeEnum.INVALID);
                            item.clearUpdateParameter();
                        })
                        .collect(Collectors.toList()));
            }
            return invalidStrategyIds;
        });
    }
}
