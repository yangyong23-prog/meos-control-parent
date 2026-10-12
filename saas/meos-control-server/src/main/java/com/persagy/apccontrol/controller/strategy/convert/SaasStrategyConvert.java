package com.persagy.apccontrol.controller.strategy.convert;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.strategy.ChillerUnitPlanStrategyDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyPlanDTO;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyBusinessTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyTypeEnum;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFixedQueryParam;
import com.persagy.apccontrol.controller.strategy.param.ChillerFixedStrategyQueryParam;
import com.persagy.apccontrol.controller.strategy.vo.ChillerStrategyPlanVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

/**
 * 策略干预转换器
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@SuppressWarnings("AlibabaAbstractClassShouldStartWithAbstractNaming")
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SaasStrategyConvert {

    protected static final StrategyBusinessTypeEnum LAST_FREEZE_PUMP_GROUP = StrategyBusinessTypeEnum.LAST_FREEZE_PUMP_GROUP;
    protected static final StrategyBusinessTypeEnum CHILLER_UNIT = StrategyBusinessTypeEnum.CHILLER_UNIT;
    protected static final StrategyTypeEnum STRATEGY_TYPE_ON_FIXED = StrategyTypeEnum.FIXED_STRATEGY;
    protected static final StrategyTypeEnum STRATEGY_TYPE_ON_TEMP = StrategyTypeEnum.TEMP_STRATEGY;
    /**
     * 转换模型
     *
     * @param strategyPlanDTO            策略计划
     * @param chillerUnitPlanStrategyDTO 冷接策略
     * @return 模型
     * @since v5.6.0
     */
    @Mappings({
            @Mapping(target = "date", ignore = true)
    })
    public abstract ChillerStrategyPlanVO toChillerVo(
            StrategyPlanDTO strategyPlanDTO,
            ChillerUnitPlanStrategyDTO chillerUnitPlanStrategyDTO);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.6.0
     */
    @Mappings({
            @Mapping(target = "beginDate", expression = "java(source.getBeginDateTime().toLocalDate())"),
            @Mapping(target = "endDate", expression = "java(source.getEndDateTime().toLocalDate())"),
            @Mapping(target = "businessType", expression = "java(CHILLER_UNIT)"),
            @Mapping(target = "strategyType", expression = "java(STRATEGY_TYPE_ON_FIXED)"),
            @Mapping(target = "objectId", ignore = true)
    })
    public abstract StrategyFixedQueryParam toFixedQueryParam(ChillerFixedStrategyQueryParam source);


}
