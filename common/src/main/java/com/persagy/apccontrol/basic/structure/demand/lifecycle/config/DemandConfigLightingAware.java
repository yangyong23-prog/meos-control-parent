package com.persagy.apccontrol.basic.structure.demand.lifecycle.config;

import com.persagy.apccontrol.basic.structure.demand.context.lighting.DemandLightingConfigInheritContext;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 需求管理-照明生命周期管理
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigLightingAware implements DemandConfigLifeAware<DemandConfigLightingValueObject> {

    @Override
    public void afterAggregateHandleInheritConfig(
            @NonNull List<DemandConfigAggregate<DemandConfigLightingValueObject>> aggregates) {
        List<DemandConfigAggregate<DemandConfigLightingValueObject>> finalAggregates = aggregates.stream()
                .filter(this.filterLightingConfig())
                .collect(Collectors.toList());
        DemandLightingConfigInheritContext context = new DemandLightingConfigInheritContext(finalAggregates);
        // 填充照明回路相关数据
        context.fillAboutCircuits();
        if (context.isFailed()) {
            this.clearLightingConfig(aggregates);
            return;
        }
        // 处理最终配置
        context.handleFinalConfig();
        if (context.isFailed()) {
            this.clearLightingConfig(aggregates);
            return;
        }
        // 填充回路编组相关数据
        context.fillAboutCircuitGroups();
        if (context.isFailed()) {
            this.clearLightingConfig(aggregates);
            return;
        }
        // 填充最终配置中照明回路配置
        context.fillFinalConfigCircuitSettings();
        if (context.isFailed()) {
            this.clearLightingConfig(aggregates);
        }
    }

    /**
     * 清空照明配置
     *
     * @param aggregates 聚合根
     * @since 7.250801.0
     */
    private void clearLightingConfig(@Nullable List<DemandConfigAggregate<DemandConfigLightingValueObject>> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        aggregates.removeIf(this.filterLightingConfig());
    }

    /**
     * 过滤出照明配置
     *
     * @return 过滤条件
     * @since 7.250801.0
     */
    private Predicate<DemandConfigAggregate<DemandConfigLightingValueObject>> filterLightingConfig() {
        return item -> item.getIdentifier()
                .getTemplateType().toDemandTypeEnum() == DemandTypeEnum.LIGHTING;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
