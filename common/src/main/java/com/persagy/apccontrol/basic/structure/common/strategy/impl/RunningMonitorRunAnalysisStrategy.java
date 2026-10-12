package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 运行分析策略
 *
 * @author muanfu
 * @since 5.241201.0
 */
@Component(RunningMonitorRunAnalysisStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorRunAnalysisStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunningMonitorRunAnalysisStrategy.class);
    private static final String LOG_TAG = "运行分析策略";
    public static final String BEAN_NAME = "runningMonitorRunAnalysisStrategy";

    private final DtpRwdClientService dtpRwdClientService;

    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    private final MidCalendarClientService midCalendarClientService;

    public RunningMonitorRunAnalysisStrategy(AbstractIotDataCacheServer abstractIotDataCacheServer,
                                             DtpRwdClientService dtpRwdClientService, MidCalendarClientService midCalendarClientService) {
        this.abstractIotDataCacheServer = abstractIotDataCacheServer;
        this.dtpRwdClientService = dtpRwdClientService;
        this.midCalendarClientService = midCalendarClientService;
    }

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.RUN_ANALYSIS;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(
            @NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        return Collections.emptyList();
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "运行检测-运行分析";
    }

    /**
     * @param infoCode
     * @param projectId
     * @return java.util.Set<java.lang.String>
     * @author wxf
     * @description 查询有信息点实例化规则的空间
     * @date 15:19 2024/12/16
     **/
    @Cacheable(cacheNames = CacheKey.Dynamic.RUN_ANALYSIS_EXISTS_RULE_SPACE, key = "#projectId+#infoCode.value()", unless = "#result == null || #result.isEmpty()")
    public Set<String> listSpaceWithInfoCode(InfoCodeEnum infoCode, String projectId) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setWithColumns(Collections.singleton(DtpRwdObjectDTO.Fields.id));
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpace(queryParam);
        Set<String> spaceIds = spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());

        Set<String> infoCodeList = Sets.newHashSet(infoCode.getCode());
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
        classCodeAndObjectIds.put(CommonConst.DtpRwdConst.GeneralZone, spaceIds);
        Map<String, Set<String>> ruleSetMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(projectId, classCodeAndObjectIds, infoCodeList);
        Set<String> res = new HashSet<>();
        if (MapUtil.isNotEmpty(ruleSetMap)) {
            String code = infoCode.getCode();
            for (Map.Entry<String, Set<String>> entry : ruleSetMap.entrySet()) {
                if (entry.getValue().contains(code)) {
                    res.add(entry.getKey());
                }
            }
        }

        LogExecutor.project().info(LOGGER, LOG_TAG, "实例化规则的空间数" + res.size());
        return res;
    }

    public Set<String> listExistsRuleSpaceIdBySystemIds(String projectId, Collection<String> systemIds, InfoCodeEnum... infoCode) {
        DtpRwdObjectQueryParam spaceParam = new DtpRwdObjectQueryParam();
        spaceParam.setClassCode(DtpRwdClassCodeEnum.SPACE.getValue());
        spaceParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        DtpRwdObjectRelationQueryParam relationQueryParam = new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE);
        relationQueryParam.setObjFrom(systemIds);
        spaceParam.setRelationTo(relationQueryParam);
        List<DtpRwdObjectDTO> spaces = dtpRwdClientService.listObject(spaceParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptySet();
        }
        Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Set<String> infoCodeList = Arrays.stream(infoCode).map(InfoCodeEnum::getCode).collect(Collectors.toSet());
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
        classCodeAndObjectIds.put(CommonConst.DtpRwdConst.GeneralZone, spaceIds);
        Map<String, Set<String>> ruleSetMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(projectId, classCodeAndObjectIds, infoCodeList);
        Set<String> res = new HashSet<>();
        if (MapUtil.isNotEmpty(ruleSetMap)) {
            for (Map.Entry<String, Set<String>> entry : ruleSetMap.entrySet()) {
                if (CollectionUtils.containsAny(entry.getValue(), infoCodeList)) {
                    res.add(entry.getKey());
                }
            }
        }
        return res;
    }


    public void fillQuality(DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality, DemandConfigAirconditioningCO2QualityValueObject co2Quality, DemandConfigAggregate<DemandConfigAirconditioningValueObject> config, MidCalendarAirSeasonTypeEnum airSeasonType) {
        DemandConfigValueObject<DemandConfigAirconditioningValueObject> finalConfig = config.getFinalConfig();
        if (Objects.isNull(finalConfig) || ObjectUtil.isNull(finalConfig.getGlobalConfig())) {
            return;
        }

        DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = finalConfig.getGlobalConfig().getEnvironmentQuality();
        if (ObjectUtil.isNull(environmentQuality)) {
            return;
        }

        DemandConfigAirconditioningTdbQualityValueObject tdbQuality = environmentQuality.getTdbQuality();
        if (Objects.nonNull(airSeasonType) && Objects.nonNull(tdbQuality) && Objects.nonNull(tdbSeasonQuality)) {
            DemandConfigAirconditioningTdbSeasonValueObject currentSeasonQuality = tdbQuality.getCurrentSeasonQuality(airSeasonType);
            if (ObjectUtil.isNotNull(currentSeasonQuality)) {
                BeanUtil.copyProperties(currentSeasonQuality, tdbSeasonQuality);
            }
        }

        if (Objects.nonNull(co2Quality)) {
            BeanUtil.copyProperties(environmentQuality.getCO2Quality(), co2Quality);
        }
    }
}
