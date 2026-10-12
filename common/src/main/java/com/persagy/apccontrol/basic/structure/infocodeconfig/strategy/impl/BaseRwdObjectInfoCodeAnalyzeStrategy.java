package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeConfigModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.validate.utility.executor.MapExecutor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * 物理世界详情信息点基础解析策略
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
public abstract class BaseRwdObjectInfoCodeAnalyzeStrategy extends BaseInfoCodeAnalyzeStrategy {

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        fillAggregateDefineOfEmpty(aggregates, String.class.getSimpleName());
    }

    /**
     * 模版与配置信息点聚合映射
     *
     * @param aggregates 聚合根
     * @return 模版 -> [信息点 -> 聚合根]
     * @since 7.250301.0
     */
    protected Map<InfoCodeTemplateTypeModel, Map<InfoCodeConfigModel, List<InfoCodeAggregate>>>
    mapTemplateTypeConfigAndAggregates(@NonNull Collection<InfoCodeAggregate> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return Maps.newLinkedHashMap();
        }
        // 根据模版类型进行分组, 每个模版为一个Sheet, 同一个Sheet中的配置按理来说应该一致
        Map<InfoCodeTemplateTypeModel, Map<InfoCodeConfigModel, List<InfoCodeAggregate>>>
                templateTypeConfigAndAggregates = new LinkedHashMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            InfoCodeTemplateTypeModel templateTypeModel = aggregate.getTemplateTypeModel();
            InfoCodeConfigModel configModel = aggregate.getConfigModel();
            MapExecutor.dynamicPut(templateTypeConfigAndAggregates, templateTypeModel,
                    configModel, aggregate, LinkedHashMap::new, ArrayList::new);
        }
        return templateTypeConfigAndAggregates;
    }
}
