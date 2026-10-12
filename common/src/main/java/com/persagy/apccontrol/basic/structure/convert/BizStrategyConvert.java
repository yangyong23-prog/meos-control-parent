package com.persagy.apccontrol.basic.structure.convert;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyDayDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyPlanDTO;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyCfg;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyDateCfg;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyPlanCfg;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyCommonParam;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFixedQueryParam;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyCfgQueryDaoParam;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyDateCfgQueryDaoParam;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 策略干预转换器
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@SuppressWarnings("AlibabaAbstractClassShouldStartWithAbstractNaming")
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
@Slf4j
public abstract class BizStrategyConvert {

    private static final String LOG_TAG = "策略业务转换器";

    @Autowired
    private ObjectMapper objectMapper;

    // ------------------------- To Dto -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @param plans  策略计划
     * @return 模型
     * @since v5.6.0
     */
    @Mappings({
            @Mapping(target = "strategyDayId", source = "source.id")
    })
    public abstract StrategyDayDTO toDto(StrategyDateCfg source, List<StrategyPlanDTO> plans);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.6.0
     */
    @Mappings({
            @Mapping(target = "strategyPlanId", source = "id"),
            @Mapping(target = "status", source = "st")
    })
    public abstract StrategyPlanDTO toDto(StrategyPlanCfg source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.6.0
     */
    @InheritConfiguration
    public abstract List<StrategyPlanDTO> toDto(Collection<StrategyPlanCfg> source);

    // ------------------------- To Param -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.6.0
     */
    public abstract StrategyCommonParam toParam(StrategyDTO source);

    // ------------------------- To Dao Param -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.6.0
     */
    @Mappings({
            @Mapping(target = "status", ignore = true)
    })
    public abstract StrategyCfgQueryDaoParam toQueryDaoParam(StrategyCommonParam source);

    /**
     * 转换模型
     *
     * @param source      数据源
     * @param strategyIds 策略ID
     * @return 模型
     * @since v5.6.0
     */
    @Mappings({
            @Mapping(target = "strategyId", ignore = true),
            @Mapping(target = "date", ignore = true),
            @Mapping(target = "dates", ignore = true),
            @Mapping(target = "status", ignore = true),
    })
    public abstract StrategyDateCfgQueryDaoParam toDateQueryDaoParam(
            StrategyFixedQueryParam source, Collection<Long> strategyIds);

    // ------------------------- To Do -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @param st     状态
     * @return 模型
     * @since v5.6.0
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateUser", ignore = true),
            @Mapping(target = "classCode", ignore = true),
            @Mapping(target = "memo", ignore = true),
            @Mapping(target = "syncTime", ignore = true),
            @Mapping(target = "syncMessage", ignore = true)
    })
    public abstract StrategyCfg toDo(StrategyCommonParam source, StateTypeEnum st);

    /**
     * 解析策略计划DTO
     *
     * @param planClass  策略计划类
     * @param planResult 策略计划值
     * @param <T>        策略计划类型
     * @return 策略计划DTO
     * @since v5.6.0
     */
    public <T extends StrategyPlanDTO.Detail> T parsePlanDTO(
            @NonNull Class<T> planClass, @NonNull String planResult) {
        try {
            return objectMapper.readValue(planResult, planClass);
        } catch (JsonProcessingException e) {
            LogExecutor.instance().error(log, LOG_TAG, "解析控制参数失败", e, Arrays.asList(
                    Pair.of("classType", planClass),
                    Pair.of("data", planResult)
            ));
            throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_1e389b0cad7d44d9a8ccd3a7d16176d4"));
        }
    }
}
