package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.ExtendSetting;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.InfoCodeSetExtend;
import com.persagy.apccontrol.basic.model.enumtype.ManualAutoSetEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.InfoCodeSetExtendService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeSetExtendConfigModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.service.duty.strategy.HeatSourceHomePageCardDataHandler.WITH_COLUMNS;

/**
 * 冷源出水温度设定配置
 */
@Component(InfoCodeSetExtendConfigAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InfoCodeSetExtendConfigAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "coolWaterReturnTempConfigAnalyzeStrategy";

    /**
     * 空调能力设定
     */
    public static final String ZDYairConditioningCapacitySet = "ZDYairConditioningCapacitySet";

    /**
     * 运行模式设定
     */
    public static final String runModeSet = "runModeSet";

    /**
     * 空调冷源页首次无扩展配置时，需要返回默认值的冷却塔出水温度设定信息点。
     */
    private static final Set<String> AIR_CONDITIONING_COLD_SOURCE_DEFAULT_INFO_CODES = Sets.newHashSet(
            InfoCodeEnum.COOLING_TOWER_OUT_TEMP_SET.getCode(),
            InfoCodeEnum.COOLING_TOWER_OUT_TEMP_COOLING_SET.getCode(),
            InfoCodeEnum.COOLING_TOWER_OUT_TEMP_STORAGE_SET.getCode());

    /**
     * 空调冷热源页首次无扩展配置时，需要返回默认值的冷却塔出水温度设定信息点。
     */
    private static final Set<String> AIR_CONDITIONING_C_H_SOURCE_DEFAULT_INFO_CODES = Sets.newHashSet(
            InfoCodeEnum.COOLING_TOWER_OUT_TEMP_SET.getCode());

    /**
     * 自定义key分隔符
     */
    private static final String KEY_SEPARATOR = ":";

    private final InfoCodeSetExtendService infoCodeSetExtendService;
    private final BizDtpRwdService bizDtpRwdService;
    private final DtpRwdClientService dtpRwdClientService;
    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.INFO_CODE_SET_EXTEND_CONFIG;
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }

        // 批量查询所有配置，避免循环内多次查询数据库
        Map<String, InfoCodeSetExtend> configMap = this.batchQueryConfigs(aggregates);

        // 处理每个聚合根
        aggregates.forEach(aggregate -> this.processAggregate(aggregate, configMap));
    }

    /**
     * 批量查询配置信息
     *
     * @param aggregates 聚合根列表
     * @return 对象ID+信息点编码 -> 配置映射
     * @since 7.260302.0
     */
    private Map<String, InfoCodeSetExtend> batchQueryConfigs(List<InfoCodeAggregate> aggregates) {
        // 收集所有需要查询的对象ID和信息点编码
        Map<String, String> objectIdAndInfoCodeMap = aggregates.stream()
                .filter(aggregate -> {
                    InfoCodeSetExtendConfigModel extendConfig = aggregate.getConfigModel()
                            .analyzeTackValueLogicModel(InfoCodeSetExtendConfigModel.class);
                    return extendConfig != null && StringUtils.isNotEmpty(extendConfig.getInfoCode());
                })
                .collect(Collectors.toMap(aggregate -> this.buildConfigKey(
                                aggregate.getIdentifier().getObject().getId(),
                                aggregate.getConfigModel().analyzeTackValueLogicModel(InfoCodeSetExtendConfigModel.class).getInfoCode()),
                        aggregate -> aggregate.getConfigModel().analyzeTackValueLogicModel(InfoCodeSetExtendConfigModel.class).getInfoCode(),
                        (v1, v2) -> v1
                ));

        if (objectIdAndInfoCodeMap.isEmpty()) {
            return Collections.emptyMap();
        }

        // 批量查询所有相关对象的配置
        Map<String, InfoCodeSetExtend> configMap = new HashMap<>();
        objectIdAndInfoCodeMap.forEach((key, infoCode) -> {
            String objectId = key.split(KEY_SEPARATOR)[0];
            InfoCodeSetExtend config = this.infoCodeSetExtendService.getByObjectIdAndInfoCode(objectId, infoCode);
            if (config != null) {
                configMap.put(key, config);
            }
        });

        return configMap;
    }

    /**
     * 处理单个聚合根
     *
     * @param aggregate 聚合根
     * @param configMap 配置映射
     * @since 7.260302.0
     */
    private void processAggregate(InfoCodeAggregate aggregate, Map<String, InfoCodeSetExtend> configMap) {
        String objectId = aggregate.getIdentifier().getObject().getId();
        RunningMonitorTypeEnum runningMonitorType = aggregate.getTemplateTypeModel().getRunningMonitorType();

        InfoCodeSetExtendConfigModel extendConfig = aggregate.getConfigModel()
                .analyzeTackValueLogicModel(InfoCodeSetExtendConfigModel.class);

        // 提前返回：配置无效
        if (extendConfig == null || StringUtils.isEmpty(extendConfig.getInfoCode())) {
            return;
        }

        String infoCode = extendConfig.getInfoCode();
        String configKey = this.buildConfigKey(objectId, infoCode);

        // 获取或创建配置
        InfoCodeSetExtend config = Optional.ofNullable(configMap.get(configKey))
                .orElseGet(() -> this.createDefaultConfig(objectId, infoCode, runningMonitorType));

        // 提前返回：无配置
        if (config == null) {
            return;
        }

        // 处理空调能力设定特殊逻辑
        if (ZDYairConditioningCapacitySet.equals(config.getInfoCode())) {
            if (!this.handleAirConditioningCapacitySet(config)) {
                aggregate.setInvalid(true);
                return;
            }
        }

        // 处理运行模式设定特殊逻辑：没有规则时不展示
        if (runModeSet.equals(config.getInfoCode())) {
            if (!this.handleRunModeSet(aggregate, config)) {
                aggregate.setInvalid(true);
                return;
            }
        }

        // 设置数据模型
        InfoCodeDataModel infoCodeDataModel = new InfoCodeDataModel();
        infoCodeDataModel.setInfoCode(aggregate.getIdentifier().getInfoCode());
        infoCodeDataModel.setDataValue(config.getExtend());
        aggregate.setInfoCodeAndDataModel(infoCodeDataModel);
    }

    /**
     * 创建默认配置
     *
     * @param objectId           对象ID
     * @param infoCode           信息点编码
     * @param runningMonitorType 运行监控类型
     * @return 默认配置，如果不满足创建条件则返回null
     * @since 7.260302.0
     */
    private InfoCodeSetExtend createDefaultConfig(String objectId, String infoCode, RunningMonitorTypeEnum runningMonitorType) {
        boolean shouldCreateDefault = this.isAirConditioningCapacitySetDefault(runningMonitorType, infoCode)
                || this.isRunModeSetDefault(runningMonitorType, infoCode)
                || this.isAirConditioningColdSourceExtendConfigDefault(runningMonitorType, infoCode)
                || this.isAirConditioningCHSourceExtendConfigDefault(runningMonitorType, infoCode);

        if (!shouldCreateDefault) {
            return null;
        }

        InfoCodeSetExtend config = buildDefaultInfoCodeSetExtend(objectId, infoCode);

        // ACAI coolingTowerOutTempSet 默认人工（PRD-260801）
        if (this.isAirConditioningCHSourceExtendConfigDefault(runningMonitorType, infoCode)) {
            config.getExtend().setManualAutoSet(ManualAutoSetEnum.MANUAL);
        }

        return config;
    }

    /**
     * 判断是否需要为空调能力设定创建默认配置
     *
     * @param runningMonitorType 运行监控类型
     * @param infoCode           信息点编码
     * @return 是否需要创建默认配置
     * @since 7.260302.0
     */
    private boolean isAirConditioningCapacitySetDefault(RunningMonitorTypeEnum runningMonitorType, String infoCode) {
        return (runningMonitorType == RunningMonitorTypeEnum.AIR_CONDITIONING_HEAT_SOURCE
                || runningMonitorType == RunningMonitorTypeEnum.AIR_CONDITIONING_C_H_SOURCE)
                && ZDYairConditioningCapacitySet.equals(infoCode);
    }

    /**
     * 判断是否需要为运行模式设定创建默认配置
     *
     * @param runningMonitorType 运行监控类型
     * @param infoCode           信息点编码
     * @return 是否需要创建默认配置
     * @since 7.260302.0
     */
    private boolean isRunModeSetDefault(RunningMonitorTypeEnum runningMonitorType, String infoCode) {
        return runningMonitorType == RunningMonitorTypeEnum.AIR_CONDITIONING_C_H_SOURCE
                && runModeSet.equals(infoCode);
    }

    /**
     * 空调冷源页首次无扩展配置时，返回冷却塔温度设定相关默认配置。
     *
     * @param runningMonitorType 运行监测类型
     * @param infoCode           信息点编码
     * @return 是否需要创建默认配置
     * @since 7.260302.0
     */
    private boolean isAirConditioningColdSourceExtendConfigDefault(
            RunningMonitorTypeEnum runningMonitorType, String infoCode) {
        return runningMonitorType == RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE
                && AIR_CONDITIONING_COLD_SOURCE_DEFAULT_INFO_CODES.contains(infoCode);
    }

    /**
     * 空调冷热源页首次无扩展配置时，返回冷却塔出水温度设定相关默认配置。
     *
     * @param runningMonitorType 运行监测类型
     * @param infoCode           信息点编码
     * @return 是否需要创建默认配置
     * @since 7.260801.0
     */
    private boolean isAirConditioningCHSourceExtendConfigDefault(
            RunningMonitorTypeEnum runningMonitorType, String infoCode) {
        return runningMonitorType == RunningMonitorTypeEnum.AIR_CONDITIONING_C_H_SOURCE
                && AIR_CONDITIONING_C_H_SOURCE_DEFAULT_INFO_CODES.contains(infoCode);
    }

    /**
     * 构建默认的信息点设定扩展配置
     *
     * @param objectId 对象ID
     * @param infoCode 信息点编码
     * @return 默认配置
     * @since 7.260302.0
     */
    private InfoCodeSetExtend buildDefaultInfoCodeSetExtend(String objectId, String infoCode) {
        ExtendSetting extendSetting = new ExtendSetting();
        extendSetting.setManualAutoSet(ManualAutoSetEnum.AUTO);

        InfoCodeSetExtend config = new InfoCodeSetExtend();
        config.setObjId(objectId);
        config.setInfoCode(infoCode);
        config.setExtend(extendSetting);
        return config;
    }

    /**
     * 处理空调能力设定特殊逻辑
     *
     * @param config 配置信息
     * @return 是否处理成功，false表示需要标记为无效
     * @since 7.260302.0
     */
    private boolean handleAirConditioningCapacitySet(InfoCodeSetExtend config) {
        // 查询系统下的设备
        List<DtpRwdObjectDTO> objects = queryAcssvgObjects(config.getObjId());
        if (CollectionUtils.isEmpty(objects)) {
            return false;
        }

        // 仅自动模式需要查询IOT数据
        if (!ManualAutoSetEnum.AUTO.equals(config.getExtend().getManualAutoSet())) {
            return true;
        }

        // 清空设定值，从IOT获取
        config.getExtend().setAirConditioningCapacitySet(null);

        DtpRwdObjectDTO obj = objects.get(0);
        this.fetchAndSetAirConditioningCapacity(obj, config);

        return true;
    }

    /**
     * 处理运行模式设定特殊逻辑
     *
     * <p>当对象没有运行模式设定规则时，不展示该信息点</p>
     *
     * @param aggregate 聚合根
     * @param config    配置信息
     * @return 是否处理成功，false表示需要标记为无效
     * @since 7.260302.0
     */
    private boolean handleRunModeSet(InfoCodeAggregate aggregate, InfoCodeSetExtend config) {
        String objectId = config.getObjId();
        String classCode = aggregate.getIdentifier().getObject().getClassCode();

        // 检查对象是否存在运行模式设定规则
        // noinspection unchecked
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = this.abstractIotDataCacheServer
                .mapObjectIdAndExistsRuleInfoCodes(
                        Collections.singletonMap(classCode, Sets.newHashSet(objectId)),
                        Lists.newArrayList(runModeSet));

        // 无规则时不展示
        boolean hasRule = MapUtils.isNotEmpty(objectIdAndExistsRuleInfoCodes)
                && objectIdAndExistsRuleInfoCodes.containsKey(objectId)
                && objectIdAndExistsRuleInfoCodes.get(objectId).contains(runModeSet);
        if (!hasRule) {
            return false;
        }

        // 查询IOT数据并设置运行模式设定值
        this.fetchAndSetRunModeSet(objectId, config);

        return true;
    }

    /**
     * 获取并设置运行模式设定值
     *
     * <p>当manualAutoSet为人工时直接使用数据库中的runModeSet，为自动时查询IOT获取实时值</p>
     *
     * @param objectId 对象ID
     * @param config   配置信息
     * @since 7.260302.0
     */
    private void fetchAndSetRunModeSet(String objectId, InfoCodeSetExtend config) {
        // 人工模式：直接使用数据库中的runModeSet，不查询IOT
        if (ManualAutoSetEnum.MANUAL.equals(config.getExtend().getManualAutoSet())) {
            return;
        }

        // 自动模式：查询IOT数据
        List<EnergyDataResponse> energyDataResponses = this.bizDtpRwdService.queryIpDataOnMoment(
                ContextUtil.getGroupCode(),
                ContextUtil.getProjectId(),
                Lists.newArrayList(objectId),
                Lists.newArrayList(runModeSet));

        if (CollectionUtils.isNotEmpty(energyDataResponses)) {
            EnergyDataResponse energyDataResponse = energyDataResponses.get(0);
            config.getExtend().setRunModeSet(String.valueOf(energyDataResponse.getIpValue()));
        }
    }

    /**
     * 查询ACSSVG设备对象
     *
     * @param objectId 对象ID
     * @return ACSSVG设备对象列表
     * @since 7.260302.0
     */
    private List<DtpRwdObjectDTO> queryAcssvgObjects(String objectId) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setWithColumns(WITH_COLUMNS);
        queryParam.setClassCodeEnum(DtpRwdClassCodeEnum.ACSSVG);
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(objectId));
        return dtpRwdClientService.listObject(queryParam);
    }

    /**
     * 获取并设置空调能力设定值
     *
     * @param obj    设备对象
     * @param config 配置信息
     * @since 7.260302.0
     */
    private void fetchAndSetAirConditioningCapacity(DtpRwdObjectDTO obj, InfoCodeSetExtend config) {
        String targetInfoCode = InfoCodeEnum.AIR_CONDITIONING_CAPACITY_SET.getCode();

        // 检查是否存在IOT规则
        // noinspection unchecked
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = this.abstractIotDataCacheServer
                .mapObjectIdAndExistsRuleInfoCodes(Collections.singletonMap(DtpRwdClassCodeEnum.ACSSVG.getValue(),
                        Sets.newHashSet(obj.getId())), Lists.newArrayList(targetInfoCode));

        // 提前返回：无IOT规则
        if (MapUtils.isEmpty(objectIdAndExistsRuleInfoCodes)
                || !objectIdAndExistsRuleInfoCodes.containsKey(obj.getId())
                || !objectIdAndExistsRuleInfoCodes.get(obj.getId()).contains(targetInfoCode)) {
            return;
        }

        // 查询IOT数据
        List<EnergyDataResponse> energyDataResponses = bizDtpRwdService.queryIpDataOnMoment(
                ContextUtil.getGroupCode(),
                ContextUtil.getProjectId(),
                Lists.newArrayList(obj.getId()),
                Lists.newArrayList(targetInfoCode));

        if (CollectionUtils.isNotEmpty(energyDataResponses)) {
            EnergyDataResponse energyDataResponse = energyDataResponses.get(0);
            config.getExtend().setAirConditioningCapacitySet(String.valueOf(energyDataResponse.getIpValue()));
        }
    }

    /**
     * 构建配置缓存key
     *
     * @param objectId 对象ID
     * @param infoCode 信息点编码
     * @return 缓存key，格式为 objectId:infoCode
     * @since 7.260302.0
     */
    private String buildConfigKey(String objectId, String infoCode) {
        return objectId + KEY_SEPARATOR + infoCode;
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-存在信息点配置";
    }
}
