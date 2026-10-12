package com.persagy.apccontrol.basic.structure.intervention.model.domain;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.domain.Entity;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.common.model.valueobject.DateTimeRangeValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.intervention.converter.InterventionConfigConverter;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.identifier.InterventionConfigIdentifier;
import com.persagy.apccontrol.basic.structure.intervention.model.valueobject.InterventionConfigValueObject;
import com.persagy.apccontrol.basic.structure.intervention.strategy.InterventionConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.validate.utility.enumeration.DateTimeTypeEnum;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 需求干预配置实体
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Data
public class InterventionConfigModel<T extends InterventionConfigData>
        implements Entity<InterventionConfigIdentifier> {

    public static final DateTimeTypeEnum DATE_TIME_TYPE = DateTimeTypeEnum.MINUTE;

    /**
     * 标识符
     */
    private InterventionConfigIdentifier identifier;

    /**
     * 运行监测二级类型
     */
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 干预时间范围
     */
    private DateTimeRangeValueObject dateTimeRange;

    /**
     * 需求实例
     */
    private DemandInstanceValueObject instance;

    /**
     * 干预配置
     */
    private InterventionConfigValueObject<T> config;

    /**
     * 干预原因
     */
    private String interventionReason;

    public static <T extends InterventionConfigData> InterventionConfigModel<T> of(
            @NonNull InterventionConfig config, @Nullable String instanceName) {
        RunningMonitorSecondTypeEnum runningMonitorSecondType = config.getRunningMonitorSecondType();
        LocalDateTime beginTime = config.getBeginTime();
        LocalDateTime endTime = config.getEndTime();
        DemandInstanceTypeEnum instanceType = config.getInstanceType();
        String instanceId = config.getInstanceId();
        String reason = config.getReason();

        InterventionConfigConverter converter = SpringUtil.getBean(InterventionConfigConverter.class);
        InterventionConfigAnalyzeStrategy configAnalyzeStrategy = runningMonitorSecondType
                .toRunningMonitorTypeEnum().getInterventionConfigAnalyzeStrategy();
        T configData = configAnalyzeStrategy.analyzeConfigObject(config.getConfigData());

        InterventionConfigValueObject<T> configValueObject = new InterventionConfigValueObject<>();
        configValueObject.setSelfConfig(configData);

        InterventionConfigIdentifier identifier = converter.toIdentifier(config);
        InterventionConfigModel<T> model = new InterventionConfigModel<>();
        model.setIdentifier(identifier);
        model.setRunningMonitorSecondType(config.getRunningMonitorSecondType());
        model.setDateTimeRange(new DateTimeRangeValueObject(beginTime, endTime));
        model.setInstance(new DemandInstanceValueObject(instanceType, instanceId, instanceName));
        model.setConfig(configValueObject);
        model.setInterventionReason(reason);
        return model;
    }

    public static <T extends InterventionConfigData> InterventionConfigModel<T> ofParent(
            @NonNull DemandInstanceValueObject originInstance, @Nullable T originConfig,
            @NonNull InterventionConfig parentConfig, @Nullable String parentInstanceName,
            @Nullable DateTimeRangeValueObject newParentTimeRange) {
        RunningMonitorSecondTypeEnum runningMonitorSecondType = parentConfig.getRunningMonitorSecondType();
        LocalDateTime beginTime = parentConfig.getBeginTime();
        LocalDateTime endTime = parentConfig.getEndTime();
        DemandInstanceTypeEnum instanceType = parentConfig.getInstanceType();
        String instanceId = parentConfig.getInstanceId();
        String reason = parentConfig.getReason();
        newParentTimeRange = Objects.isNull(newParentTimeRange)
                ? new DateTimeRangeValueObject(beginTime, endTime)
                : newParentTimeRange;

        InterventionConfigConverter converter = SpringUtil.getBean(InterventionConfigConverter.class);
        InterventionConfigAnalyzeStrategy configAnalyzeStrategy = runningMonitorSecondType
                .toRunningMonitorTypeEnum().getInterventionConfigAnalyzeStrategy();
        T configData = configAnalyzeStrategy.analyzeConfigObject(parentConfig.getConfigData());
        originConfig = Optional.ofNullable(originConfig).orElse(configData);

        InterventionConfigValueObject<T> configValueObject = new InterventionConfigValueObject<>();
        configValueObject.setSelfConfig(originConfig);
        configValueObject.setFinalConfig(configData);
        configValueObject.setInheritInstances(Lists.newArrayList(
                new DemandInstanceValueObject(instanceType, instanceId, parentInstanceName)));

        InterventionConfigIdentifier identifier = converter.toIdentifier(parentConfig);
        InterventionConfigModel<T> model = new InterventionConfigModel<>();
        model.setIdentifier(identifier);
        model.setRunningMonitorSecondType(parentConfig.getRunningMonitorSecondType());
        model.setDateTimeRange(newParentTimeRange);
        model.setInstance(originInstance);
        model.setConfig(configValueObject);
        model.setInterventionReason(reason);
        return model;
    }

    /**
     * 是否在当前时间生效
     *
     * @return 当前时间是否生效
     * @since 5.240702.0
     */
    public boolean isEffectiveByNow() {
        return dateTimeRange.toRange(DATE_TIME_TYPE.convertTemporalUnit())
                .contains(DATE_TIME_TYPE.truncate(DateUtils.now()));
    }

    /**
     * 将截止时间设置为当前时间
     *
     * @since 5.240702.0
     */
    public void resetEndTimeToNow() {
        this.dateTimeRange.setEndDateTime(DATE_TIME_TYPE.truncate(DateUtils.now()));
    }
}
