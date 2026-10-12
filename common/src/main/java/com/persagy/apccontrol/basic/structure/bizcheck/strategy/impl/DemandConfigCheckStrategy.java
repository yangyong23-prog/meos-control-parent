package com.persagy.apccontrol.basic.structure.bizcheck.strategy.impl;

import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.structure.bizcheck.context.BizCheckContext;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.BizCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.DemandConfigCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.strategy.BizCheckStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 空调运行标准检查
 *
 * @author JiYinchuan
 * @since 7.260301.0
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigCheckStrategy implements BizCheckStrategy<DemandConfigCheckItemDTO> {

    private final DemandConfigGateway<DemandConfigAirconditioningValueObject> demandConfigGateway;

    @Override
    public BizCheckStrategyTypeEnum getStrategyType() {
        return BizCheckStrategyTypeEnum.HVAC_OPERATION_STANDARD;
    }

    @Override
    public BizCheckItemDTO<DemandConfigCheckItemDTO> execute(BizCheckContext context) {
        BizCheckItemDTO<DemandConfigCheckItemDTO> checkItemDTO = new BizCheckItemDTO<>();
        DemandConfigCheckItemDTO checkResult = new DemandConfigCheckItemDTO();
        checkItemDTO.setCheckResult(checkResult);

        try {
            LocalDate checkDate = context.getCheckDate();

            // 1. 查询当日运行的空调模板
            BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
            templateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
            templateParam.setDate(checkDate);
            DemandTemplate template = templateParam.queryAndGetTemplate();

            // 2. 检查运行模板
            if (Objects.isNull(template)) {
                checkResult.setTemplateStatus(BizCheckStatusEnum.EXCEPTION);
                checkResult.setTemperatureThresholdStatus(BizCheckStatusEnum.EXCEPTION);
                checkItemDTO.setStatus(BizCheckStatusEnum.REJECTED);
                return checkItemDTO;
            }

            checkResult.setTemplateStatus(BizCheckStatusEnum.NORMAL);
            checkResult.setTemplateName(template.getDemandTemplateName());

            // 3. 获取项目实例的空调需求配置，检查温度门限值
            String projectId = ContextUtil.getProjectId();
            DemandConfigByInstanceQueryParam instanceQueryParam = new DemandConfigByInstanceQueryParam();
            instanceQueryParam.setDate(checkDate);
            instanceQueryParam.setTemplateId(template.getId());
            instanceQueryParam.setInstance(new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT, projectId));

            Optional<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> configOpt =
                    demandConfigGateway.getConfigByInstance(instanceQueryParam);

            boolean temperatureConfigured = false;
            if (configOpt.isPresent()) {
                DemandConfigValueObject<DemandConfigAirconditioningValueObject> finalConfig = configOpt.get().getFinalConfig();
                if (Objects.nonNull(finalConfig) && finalConfig.getGlobalConfig() != null) {
                    DemandConfigAirconditioningValueObject airconditioningConfig = finalConfig.getGlobalConfig();
                    temperatureConfigured = checkTemperatureThreshold(airconditioningConfig);
                }
            }

            if (temperatureConfigured) {
                checkResult.setTemperatureThresholdStatus(BizCheckStatusEnum.NORMAL);
                checkItemDTO.setStatus(BizCheckStatusEnum.NORMAL);
            } else {
                checkResult.setTemperatureThresholdStatus(BizCheckStatusEnum.EXCEPTION);
                checkItemDTO.setStatus(BizCheckStatusEnum.REJECTED);
            }

        } catch (Exception e) {
            logProjectInstance().error("执行空调运行标准检查时发生异常", e, Pair.of("context", context));
            checkItemDTO.setStatus(BizCheckStatusEnum.FAILED);
        }
        return checkItemDTO;
    }

    /**
     * 检查项目级温度门限值是否完整配置
     * <p>温度过冷门限、温度舒适区下限、温度舒适区上限、温度过热门限均不为空则视为已配置
     *
     * @param airconditioningConfig 空调需求配置值对象
     * @return 是否完整配置
     * @since 7.260301.0
     */
    private boolean checkTemperatureThreshold(DemandConfigAirconditioningValueObject airconditioningConfig) {
        DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = airconditioningConfig.getEnvironmentQuality();
        if (Objects.isNull(environmentQuality)) {
            return false;
        }
        DemandConfigAirconditioningTdbQualityValueObject tdbQuality = environmentQuality.getTdbQuality();
        if (Objects.isNull(tdbQuality)) {
            return false;
        }
        List<DemandConfigAirconditioningTdbSeasonValueObject> seasonQuality = tdbQuality.getSeasonQuality();
        if (CollectionUtils.isEmpty(seasonQuality)) {
            return false;
        }
        for (DemandConfigAirconditioningTdbSeasonValueObject season : seasonQuality) {
            if (Objects.isNull(season.getUnderCoolingSeasonLimitValue())
                    || Objects.isNull(season.getLowerComfortSeasonLimitValue())
                    || Objects.isNull(season.getUpperComfortSeasonLimitValue())
                    || Objects.isNull(season.getOverHeatingSeasonLimitValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String logTag() {
        return "空调运行标准检查";
    }
}
