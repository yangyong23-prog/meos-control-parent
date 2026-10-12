package com.persagy.apccontrol.basic.service.dashboard.space;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.controller.shellconfig.param.QueryShellConfigByInfoCodeParam;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.cacherwd.WhitelistTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirDtpRwdSpaceVO;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheIotRuleRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRuleRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirTerminalStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 末端空间查询
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AirTerminalQuerySpace implements QuerySpace {

    private final RedisCacheTemplate redisCacheTemplate;
    private final AbstractIotDataCacheServer<?> abstractIotDataCacheServer;

    /**
     * 壳配置服务，用于读取末端空间范围模式。
     *
     * <p>这里直接保留配置查询异常，不能把读取失败误判为全量模式并扩大业务空间范围。</p>
     *
     * @since v7.260701.0
     */
    private final @NonNull ShellConfigService shellConfigService;

    private final RunningMonitorAirTerminalStrategy runningMonitorAirTerminalStrategy;
    /**
     * 缓存对象模型共享 READY 状态服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    /**
     * 缓存键与对象 ID 关联仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    /**
     * 模型视图独立表仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelViewRepository cacheObjectModelViewRepository;

    /**
     * 模型视图记录恢复转换器。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelViewRecordConverter
            cacheObjectModelViewRecordConverter;

    /**
     * 新规则读取仓储，按对象和部署端规则类型查询规则。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheRuleRepository cacheRuleRepository;

    /**
     * 当前部署端规则类型提供者。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheIotRuleRepository cacheIotRuleRepository;

    @Override
    public List<AirDtpRwdSpaceVO> querySpace(DemandInstanceValueObject instance, boolean useCache) {
        // useCache=false 时，直接查询实时数据，不检查白名单
        if (!useCache) {
            return getSpace(instance);
        }

        // 壳配置先决定业务空间范围，READY 只决定同一范围读取新表还是实时链路；
        // 不能根据某个缓存键是否命中反推用户配置，否则空白名单会被误判为项目全量。
        boolean useWhitelistSpaceRange = this.isWhitelistSpaceRangeConfigured();
        if (this.cacheObjectModelStatusService.isReady(
                RunningMonitorTypeEnum.AIR_TERMINAL)) {
            if (useWhitelistSpaceRange) {
                String whitelistKey =
                        CacheObjectModelKeyConfig.AIR_TERMINAL_WHITELIST_SPACE;
                // 白名单键空关联是合法结果，选中新模型后直接返回空，不回退项目全量键或 RWD。
                return this.queryNewModelSpaces(instance, whitelistKey);
            }
            return this.queryNewModelSpaces(
                    instance, CacheObjectModelKeyConfig.AIR_TERMINAL_LIST_SPACE);
        }

        if (useWhitelistSpaceRange) {
            RunningMonitorSpaceQueryParam queryParam = new RunningMonitorSpaceQueryParam();
            queryParam.fillByInstance(instance);
            // 范围已由壳配置确定为白名单；关闭缓存读取，避免 READY 状态在二次检查时发生竞态。
            queryParam.setMeosCache(false);
            return this.processSpaceRules(
                    this.runningMonitorAirTerminalStrategy.listWhitelistSpaces(queryParam));
        }

        return this.getSpace(instance);
    }

    /**
     * 判断末端空间范围壳配置是否选择用户白名单。
     *
     * <p>配置缺失时使用枚举声明的 {@code FULL} 默认值；未知配置值也按全量处理并记录告警。
     * 配置查询异常继续抛出，避免把真实白名单项目静默扩大为项目全量范围。</p>
     *
     * @return {@code true} 表示只允许读取白名单 Key
     * @since v7.260701.0
     */
    public boolean isWhitelistSpaceRangeConfigured() {
        QueryShellConfigByInfoCodeParam queryParam = new QueryShellConfigByInfoCodeParam();
        queryParam.setProjectId(ContextUtil.getProjectId());
        queryParam.setCode(ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getCode());
        ShellConfigItem configuredItem = this.shellConfigService.queryByInfoCode(
                ContextUtil.getGroupCode(), queryParam);
        String configuredRange = configuredItem == null
                ? null : configuredItem.getValue();
        String fullSpaceRange =
                ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getDefaultValues();
        if (StringUtils.isBlank(configuredRange)) {
            configuredRange = fullSpaceRange;
        }
        if (StringUtils.equals(
                WhitelistTypeEnum.CUSTOM.getShellConfigValue(), configuredRange)) {
            return true;
        }
        if (!StringUtils.equals(fullSpaceRange, configuredRange)) {
            this.logProjectInstance().warn(
                    "末端空间范围壳配置值无法识别，按全量可管控空间处理",
                    Arrays.asList(
                            Pair.of("configCode",
                                    ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getCode()),
                            Pair.of("configValue", configuredRange)));
        }
        return false;
    }
    
    /**
     * 从缓存键关联和模型视图表读取末端空间。
     *
     * <p>按建筑、楼层或空间过滤加载模型视图字段；缓存键没有对象关联时直接返回空列表，
     * 新链路不访问旧对象表或 RWD。</p>
     *
     * @param instance 当前需求实例
     * @param cacheKey 当前项目选中的项目级或白名单空间缓存键
     * @return 查询成功的末端空间
     * @since v7.260701.0
     */
    private @NonNull List<AirDtpRwdSpaceVO> queryNewModelSpaces(
            @NonNull DemandInstanceValueObject instance,
            @NonNull String cacheKey) {
        List<String> objectIds =
                this.cacheObjectRepository.listObjectIdsByCacheKey(cacheKey);
        if (CollectionUtils.isEmpty(objectIds)) {
            return Collections.emptyList();
        }
        CacheObjectModelViewQueryDaoParam queryParam =
                this.toSpaceModelViewQueryParam(instance, objectIds);
        List<CacheObjectModelView> modelViews =
                this.cacheObjectModelViewRepository.listModelViews(queryParam);
        List<DtpRwdSpaceDTO> spaces = modelViews.stream()
                .map(modelView -> this.cacheObjectModelViewRecordConverter.toRwdObject(
                        modelView,
                        DtpRwdSpaceDTO.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return this.processNewModelSpaceRules(spaces);
    }

    /**
     * 构造按当前需求实例过滤的模型视图查询参数。
     *
     * @param instance  当前需求实例
     * @param objectIds 当前缓存键关联的空间 ID
     * @return 模型视图查询参数
     * @since v7.260701.0
     */
    private @NonNull CacheObjectModelViewQueryDaoParam toSpaceModelViewQueryParam(
            @NonNull DemandInstanceValueObject instance,
            @NonNull List<String> objectIds) {
        CacheObjectModelViewQueryDaoParam queryParam =
                new CacheObjectModelViewQueryDaoParam();
        queryParam.setIds(objectIds);
        queryParam.setObjectType(DtpRwdObjectTypeEnum.SPACE.value());
        DemandInstanceTypeEnum instanceType = instance.getInstanceType();
        switch (instanceType) {
            case BUILDING:
                queryParam.setBuildingIds(Collections.singletonList(instance.getInstanceId()));
                break;
            case FLOOR:
                queryParam.setFloorIds(Collections.singletonList(instance.getInstanceId()));
                break;
            case SPACE:
                queryParam.setIds(objectIds.contains(instance.getInstanceId())
                        ? Collections.singletonList(instance.getInstanceId())
                        : Collections.emptyList());
                break;
            case PROJECT:
            default:
                break;
        }
        return queryParam;
    }

    /**
     * 查询空间
     *
     * @param instance
     * @return
     */
    private List<AirDtpRwdSpaceVO> getSpace(DemandInstanceValueObject instance) {
        //根据策略查询对应的空间 （空间有楼层信息）
        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
        spaceQueryParam.fillByInstance(instance);
        List<DtpRwdSpaceDTO> spaces = runningMonitorAirTerminalStrategy.listSpaces(spaceQueryParam);
        return processSpaceRules(spaces);
    }

    public List<AirDtpRwdSpaceVO> processSpaceRules(List<DtpRwdSpaceDTO> spaces) {
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptyList();
        }
        //转换为末端空间对象
        List<AirDtpRwdSpaceVO> airDtpRwdSpace = new ArrayList<>();
        for (DtpRwdSpaceDTO sourceVO : spaces) {
            AirDtpRwdSpaceVO vo = new AirDtpRwdSpaceVO();
            BeanUtils.copyProperties(sourceVO, vo);
            airDtpRwdSpace.add(vo);
        }
        List<String> spaceIds = airDtpRwdSpace.stream().map(AirDtpRwdSpaceVO::getId).collect(Collectors.toList());

        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
        spaceIds.forEach(item -> MapExecutor.dynamicPut(classCodeAndObjectIds,
                DtpRwdClassCodeEnum.SPACE.value(), item, HashSet::new));
        Map<String, Set<String>> objectIdToExistsRuleInfoCodesMap = abstractIotDataCacheServer
                .mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndObjectIds,
                        Lists.newArrayList(DtpRwdSpaceDTO.Fields.tdb, DtpRwdSpaceDTO.Fields.co2));

        if (MapUtils.isEmpty(objectIdToExistsRuleInfoCodesMap)) {
            return airDtpRwdSpace;
        }

        //对象标记
        airDtpRwdSpace.forEach(space -> {
            String spaceId = space.getId();
            Set<String> infoCode = objectIdToExistsRuleInfoCodesMap.get(spaceId);
            if (CollectionUtils.isNotEmpty(infoCode)) {
                space.setCo2Rule(infoCode.contains(DtpRwdSpaceDTO.Fields.co2));
                space.setTdbRule(infoCode.contains(DtpRwdSpaceDTO.Fields.tdb));
            }
        });
        return airDtpRwdSpace;
    }

    /**
     * 使用新规则主表为新模型空间填充规则标记。
     *
     * <p>READY 路由已经选中新模型时禁止再访问云端 RWD 规则接口。为保持实时查询语义，
     * 规则直接按空间 ID、温度/二氧化碳信息点和当前部署端规则类型查询，不附加模板 Key 范围。</p>
     *
     * @param spaces 新模型恢复的空间
     * @return 已填充温度和二氧化碳规则标记的空间
     * @since v7.260701.0
     */
    private @NonNull List<AirDtpRwdSpaceVO> processNewModelSpaceRules(
            @NonNull List<DtpRwdSpaceDTO> spaces) {
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptyList();
        }
        List<AirDtpRwdSpaceVO> result = new ArrayList<>(spaces.size());
        Set<String> spaceIds = new LinkedHashSet<>();
        for (DtpRwdSpaceDTO source : spaces) {
            AirDtpRwdSpaceVO target = new AirDtpRwdSpaceVO();
            BeanUtils.copyProperties(source, target);
            result.add(target);
            spaceIds.add(target.getId());
        }
        List<String> infoCodes = Arrays.asList(
                DtpRwdSpaceDTO.Fields.tdb, DtpRwdSpaceDTO.Fields.co2);
        Map<String, Set<String>> rulesByObjectId = this.cacheRuleRepository
                .mapInfoCodesByObjectIds(
                        spaceIds, infoCodes, this.cacheIotRuleRepository.cacheIotRuleType());
        for (AirDtpRwdSpaceVO space : result) {
            Set<String> ruleInfoCodes = rulesByObjectId.get(space.getId());
            if (CollectionUtils.isNotEmpty(ruleInfoCodes)) {
                space.setCo2Rule(ruleInfoCodes.contains(DtpRwdSpaceDTO.Fields.co2));
                space.setTdbRule(ruleInfoCodes.contains(DtpRwdSpaceDTO.Fields.tdb));
            }
        }
        return result;
    }

    @Override
    public String logTag() {
        return "AirTerminalQuerySpace";
    }
}
