package com.persagy.apccontrol.basic.structure.demand.strategy.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.validate.utility.enumeration.TimeTypeEnum;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * 抽象需求配置策略
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public abstract class AbstractDemandConfigAnalyzeStrategy<T extends BaseDemandConfig>
        implements DemandConfigAnalyzeStrategy<T>, LogComponent {

    public static final TimeTypeEnum TIME_TYPE = TimeTypeEnum.MINUTE;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * 配置类型
     *
     * @return 配置类型
     * @since 5.240902.0
     */
    protected abstract TypeReference<DemandConfigValueObject<T>> typeReference();

    @Override
    @SneakyThrows
    public DemandConfigValueObject<T> analyzeConfig(@NonNull JsonNode dataConfig) {
        return objectMapper.readValue(dataConfig.toString(), typeReference());
    }
}
