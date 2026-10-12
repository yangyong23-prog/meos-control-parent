package com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.*;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl.*;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 信息点类型枚举
 *
 * @author JiYinchuan
 * @see #listSsePublishInfoCodeTypes(boolean)
 * @see #isIotInfoCode()
 * @see #isKnowledgeInfoCode()
 * @see #isRiskEventInfoCode()
 * @since 5.240602.0
 */
@Getter
@AllArgsConstructor
public enum InfoCodeTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 基础信息点
     */
    BASIC("BASIC", "key_189e338ede71456cbd6433138f1d9dfe",
            BasicInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * IOT信息点-运行
     */
    IOT_RUNNING("IOT_RUNNING", "key_0d159c962a1647718893311dd99ef464",
            IotRunningInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * IOT信息点-设定
     */
    IOT_SETTING("IOT_SETTING", "key_3ff691d435454195a5d218386f3df501",
            IotSettingInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * IOT信息点-统计
     */
    IOT_STATISTIC("IOT_STATISTIC", "key_9eca54be253a4ddba6e2ecc72387c940",
            IotStatisticsInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * IOT信息点-统计-都取持久化数据
     */
    IOT_CLOUD_STATISTIC("IOT_CLOUD_STATISTIC", "key_9eca54be253a4ddba6e2ecc72387c940",
            IotCloudStatisticsInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * IOT信息点处理
     */
    IOT_INFO_CODE_HANDLER("IOT_INFO_CODE_HANDLER", "key_3622b191357c4bcaa6f57b34f526c5d3",
            IotInfoCodeHandlerAnalyzeStrategy.BEAN_NAME,
            InfoCodeTackLogicIotInfoCodeHandlerModel.class),

    /**
     * 品质区间
     */
    QUALITY_INTERVAL("QUALITY_INTERVAL", "key_176126eae25b4e43843b1dacb95710b6",
            QualityIntervalInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTackLogicDemandTargetModel.class),

    /**
     * 物理世界对象
     */
    RWD_OBJECT("RWD_OBJECT", "key_50553bdaa12b4e57ba88817ac0343bde",
            RwdObjectInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTakeValueLogicRwdObjectModel.class),

    /**
     * 物理世界对象详情
     */
    RWD_OBJECT_DETAIL("RWD_OBJECT_DETAIL", "key_a207cddeda1446bd9d5c65745506c226",
            RwdObjectDetailInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTakeValueLogicRwdObjectDetailModel.class),

    /**
     * 异常事件
     */
    RISK_EVENT("RISK_EVENT", "key_4e717c8ec9a34332a4a5ef73d80922d8",
            RiskEventInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * 风险事件
     */
    DANGER_EVENT("DANGER_EVENT", "key_3909b2f3ac9a4937afaa7aebc1859190",
            DangerEventInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * 异常事件详情
     */
    RISK_EVENT_DETAIL("RISK_EVENT_DETAIL", "key_6df1ec01bd544768ae77a0978819a361",
            RiskEventDetailInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * 空间带设备异常事件详情
     */
    RISK_EVENT_DETAIL_SPACE_WITH_EQUIPMENTS("RISK_EVENT_DETAIL_SPACE_WITH_EQUIPMENTS", "key_57b0964a43af40caabf59deaa819d717",
            RiskEventDetailSpaceWithEquipmentsInfoCodeAnalyzeStrategy.BEAN_NAME,
            StaticInfoCodeTackValueLogicModel.class),

    /**
     * 巡检信息
     */
    ROUTING_INSPECTION("ROUTING_INSPECTION", "key_9a62b1e53bd1463ead3dcc5e0ef49a73",
            RoutingInspectionInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * 需求目标值
     */
    DEMAND_TARGET("DEMAND_TARGET", "key_86ce6d4b686c4cb2b52191373a22d0c0",
            DemandTargetInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTackLogicDemandTargetModel.class),

    /**
     * 需求目标统计
     */
    DEMAND_TARGET_STATISTIC("DEMAND_TARGET_STATISTIC", "key_74801bbaaf1f4544ba149a6e575c3965",
            DemandTargetStatisticsInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTackLogicDemandTargetModel.class),

    /**
     * 需求目标类型
     */
    DEMAND_TARGET_TYPE("DEMAND_TARGET_TYPE", "key_8e0c0e23f6fc4a6cbcf6625b8a98aa44",
            DemandTargetTypeInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTackLogicDemandTargetModel.class),

    /**
     * 需求配置
     */
    DEMAND_CONFIG("DEMAND_CONFIG", "key_ce4a33050fe14ea6af865b5e81efe28c",
            DemandConfigInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * 空间通行状态
     */
    SPACE_ACCESS_DEMAND_STATUS("SPACE_ACCESS_DEMAND_STATUS", "key_5c110665bb4042fbb27f0eac8a0dc41e",
            SpaceAccessDemandStatusInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * 持久化数值
     */
    IOT_PERSISTENCE("IOT_PERSISTENCE", "key_0f49de34bd214fc0b6d8dafeb14d0e8e",
            IotPersistenceInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTackLogicIotPersistenceModel.class),

    /**
     * 持久化数值
     */
    IOT_PERSISTENCE_MULTI("IOT_PERSISTENCE_MULTI", "key_0f49de34bd214fc0b6d8dafeb14d0e8e",
            IotPersistenceMultiInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTackLogicIotPersistenceModel.class),

    /**
     * 持久化数值趋势图
     */
    IOT_PERSISTENCE_TREND("IOT_PERSISTENCE_TREND", "key_b2ef8a25c00a44d9af0ae2da5d4e97f5",
            IotPersistenceTrendInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTackLogicIotPersistenceModel.class),

    /**
     * 工作历营业时间
     */
    CALENDAR_WORK_TIME("CALENDAR_WORK_TIME", "key_1fcd1e79a6ae46bda21214440762f3ee",
            CalendarWorkTimeInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * 工作历自身配置营业时间
     */
    CALENDAR_WORK_TIME_SELF("CALENDAR_WORK_TIME_SELF", "key_87d28bf2c16f4cb1b46b248083b93ad1",
            CalendarWorkTimeSelfInfoCodeAnalyzeStrategy.BEAN_NAME, null),

    /**
     * 是否存在信息点配置
     */
    EXIST_INFO_CODE_CONFIG("EXIST_INFO_CODE_CONFIG", "key_6f2a8ed3d4714be79980bae911408f8f",
            ExistInfoCodeConfigAnalyzeStrategy.BEAN_NAME,
            ExistInfoCodeConfigModel.class),

    /**
     * 信息点设置扩展配置
     */
    INFO_CODE_SET_EXTEND_CONFIG("INFO_CODE_SET_EXTEND_CONFIG", "key_9c7db228428a4b66aa854bb3c4a8aab0",
            InfoCodeSetExtendConfigAnalyzeStrategy.BEAN_NAME,
            InfoCodeSetExtendConfigModel.class),

    /**
     * 物理世界对象列表
     */
    WEB_CONFIG("WEB_CONFIG", "key_0c901ea408cc44b2a7571cc710984115",
            InfoCodeWebConfigAnalyzeStrategy.BEAN_NAME,
            null),

    /**
     * 物理世界对象详情列表
     */
    RWD_OBJECT_LIST("RWD_OBJECT_LIST", "key_7ff0f1802edf4cb7a572cbde8df7cf89",
            RwdObjectDetailInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTakeValueLogicRwdObjectDetailModel.class),

    /**
     * 物理世界对象按信息点值做统计
     */
    RWD_OBJECT_STAT("RWD_OBJECT_STAT", "key_cc57b23c0d2244698b0a1ea5f4e1c66a",
            RwdObjectStatInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTakeValueLogicRwdObjectDetailModel.class),

    /**
     * JSON对象
     *
     * @see com.fasterxml.jackson.databind.JsonNode
     */
    JSON_OBJECT("JSON_OBJECT", "Json Object",
            JsonObjectInfoCodeAnalyzeStrategy.BEAN_NAME,
            null),

    /**
     * 物理世界标签
     *
     */
    RWD_INFO_TAG("RWD_INFO_TAG", "rwd info tag",
            RwdTagInfoCodeAnalyzeStrategy.BEAN_NAME,
            InfoCodeTagTakeLogicModel.class),

    /**
     * 数据字典报警消息
     */
    KNOWLEDGE_ALARM_MESSAGE("KNOWLEDGE_ALARM_MESSAGE", "Alarm message",
            AlarmMessageAnalyzeStrategy.BEAN_NAME,
            null),

    ;

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 解析策略Bean名称
     */
    private final String analyzeStrategyBeanName;

    /**
     * 取值逻辑模型
     */
    private final Class<? extends BaseInfoCodeTackValueLogicModel> tackValueLogicModel;

    /**
     * 获取SSE发布信息点类型
     *
     * @param oldVersion 是否为旧版本
     * @return 非IOT信息点类型
     * @since 5.240602.0
     */
    public static List<InfoCodeTypeEnum> listSsePublishInfoCodeTypes(boolean oldVersion) {
        List<InfoCodeTypeEnum> notSeePublishInfoCodeTypes = Lists.newArrayList(
                RISK_EVENT, RISK_EVENT_DETAIL, RISK_EVENT_DETAIL_SPACE_WITH_EQUIPMENTS, ROUTING_INSPECTION, DEMAND_CONFIG);
        // 老版本推送所有信息点类型
        if (oldVersion) {
            notSeePublishInfoCodeTypes.clear();
        }
        List<Function<InfoCodeTypeEnum, Boolean>> publishConditions = new ArrayList<>();
        // IOT信息点不推送
        publishConditions.add(infoCodeType -> infoCodeType != IOT_RUNNING);
        publishConditions.add(infoCodeType -> infoCodeType != IOT_SETTING);
        // 基础信息点不推送
        publishConditions.add(infoCodeType -> infoCodeType != BASIC);
        // 其他不需要推送的信息点, 特指按需订阅后不需要推送的信息点, 为了兼容老版本存在
        publishConditions.add(infoCodeType -> !notSeePublishInfoCodeTypes.contains(infoCodeType));
        return Arrays.stream(InfoCodeTypeEnum.values())
                .filter(item -> publishConditions.stream().allMatch(condition -> condition.apply(item)))
                .collect(Collectors.toList());
    }

    /**
     * 是否为数据字典信息点
     *
     * @return 是否为数据字典信息点
     * @since 5.240602.0
     */
    public boolean isKnowledgeInfoCode() {
        switch (this) {
            case BASIC:
            case IOT_RUNNING:
            case IOT_SETTING:
            case IOT_STATISTIC:
            case IOT_CLOUD_STATISTIC:
            case IOT_PERSISTENCE:
            case IOT_PERSISTENCE_TREND:
            case IOT_PERSISTENCE_MULTI:
                return true;
            default:
                return false;
        }
    }

    /**
     * 是否为IOT信息点
     *
     * @return 是否为IOT信息点
     * @since 5.240602.0
     */
    public boolean isIotInfoCode() {
        switch (this) {
            case IOT_RUNNING:
            case IOT_SETTING:
            case IOT_STATISTIC:
            case IOT_CLOUD_STATISTIC:
            case IOT_PERSISTENCE:
            case IOT_PERSISTENCE_TREND:
            case IOT_PERSISTENCE_MULTI:
                return true;
            default:
                return false;
        }
    }

    /**
     * 是否为云端IOT信息点
     *
     * @return 是否为云端IOT信息点
     * @since 7.250801.0
     */
    public boolean isPersistenceIotInfoCode() {
        switch (this) {
            case IOT_PERSISTENCE:
            case IOT_PERSISTENCE_TREND:
            case IOT_PERSISTENCE_MULTI:
            case IOT_CLOUD_STATISTIC:
                return true;
            default:
                return false;
        }
    }

    /**
     * 是否为异常事件信息点
     *
     * @return 是否为异常事件信息点
     * @since 5.241101.0
     */
    public boolean isRiskEventInfoCode() {
        switch (this) {
            case RISK_EVENT:
            case RISK_EVENT_DETAIL:
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取信息点解析策略
     *
     * @return 信息点解析策略
     * @since 5.240602.0
     */
    public InfoCodeAnalyzeStrategy getAnalyzeStrategy() {
        return SpringUtil.getBean(analyzeStrategyBeanName);
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InfoCodeTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, InfoCodeTypeEnum.class);
    }
}
