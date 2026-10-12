package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeStatDataModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeGroupVO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.context.InfoCodeRwdObjectDetailByTemplateContext;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeConfigModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectDetailModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 物理世界详情信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Component(RwdObjectStatInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RwdObjectStatInfoCodeAnalyzeStrategy
        extends BaseRwdObjectInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "equipmentRwdObjectStatInfoCodeAnalyzeStrategy";

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.RWD_OBJECT_STAT;
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        Map<InfoCodeTemplateTypeModel, Map<InfoCodeConfigModel, List<InfoCodeAggregate>>>
                templateTypeConfigAndAggregates = mapTemplateTypeConfigAndAggregates(aggregates);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<InfoCodeTemplateTypeModel, Map<InfoCodeConfigModel, List<InfoCodeAggregate>>> entry
                : templateTypeConfigAndAggregates.entrySet()) {
            InfoCodeTemplateTypeModel templateTypeModel = entry.getKey();
            for (Map.Entry<InfoCodeConfigModel, List<InfoCodeAggregate>> innerEntry : entry.getValue().entrySet()) {
                futures.add(CompletableFuture.runAsync(() -> {
                    List<InfoCodeAggregate> eachAggregates = innerEntry.getValue();

                    // 取出第一个聚合根, 按理说同一个模版下的取值逻辑是一致的
                    InfoCodeAggregate firstAggregate = IterableUtils.first(eachAggregates);
                    InfoCodeTakeValueLogicRwdObjectDetailModel tackValueLogicModel = firstAggregate.getConfigModel()
                            .analyzeTackValueLogicModel(InfoCodeTakeValueLogicRwdObjectDetailModel.class);
                    // 所有对象ID
                    Set<String> objectIds = InfoCodeAggregate.listAllObjectIds(eachAggregates);

                    InfoCodeRwdObjectDetailByTemplateContext context = new InfoCodeRwdObjectDetailByTemplateContext(
                            templateTypeModel, tackValueLogicModel, objectIds, additionalValues);
                    // 填充数据源映射
                    context.fillSourceMap();
                    // 填充对象
                    context.fillObjects();
                    // 填充对象ID和关联上下文
                    context.fillObjectIdAndRelation();
                    // 填充信息点配置
                    context.fillInfoCodeConfig();

                    Map<String, List<DtpRwdObjectDTO>> rootObjectIdAndObjects = context.getRootObjectIdAndObjects();
                    for (InfoCodeAggregate aggregate : eachAggregates) {
                        String rootObjectId = aggregate.getIdentifier().getObject().getId();
                        String infoCode = aggregate.getConfigModel().getInfoCode();
                        String showStyle = aggregate.getConfigModel().getShowStyle();
                        List<InfoCodeStatDataModel> infoCodeDataModels = JSON.parseArray(showStyle, InfoCodeStatDataModel.class);

                        List<DtpRwdObjectDTO> objects = Collections.emptyList();
                        if (MapUtils.isNotEmpty(rootObjectIdAndObjects)) {
                            objects = rootObjectIdAndObjects.getOrDefault(rootObjectId, Collections.emptyList());
                        }
                        List<InfoCodeRwdObjectDetailDTO> result = context.toResult(objects);
                        result.forEach(item -> {
                            List<InfoCodeGroupVO> relInfoCodeGroupConfigs = item.getRelInfoCodeGroupConfigs();
                            fillInfoCodeStat(relInfoCodeGroupConfigs, infoCodeDataModels);
                        });
                        aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, infoCodeDataModels, null, null));
                    }
                }, BusinessThreadPoolExecutor.ttlExecutor()));
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void fillInfoCodeStat(List<InfoCodeGroupVO> relInfoCodeGroupConfigs, List<InfoCodeStatDataModel> infoCodeDataModels) {
        if (CollUtil.isEmpty(relInfoCodeGroupConfigs) || CollUtil.isEmpty(infoCodeDataModels)) {
            return;
        }

        // 将infoCodeDataModels转换为Map以提高查找效率
        Map<String, InfoCodeStatDataModel> infoCodeDataModelMap = infoCodeDataModels.stream()
                .collect(Collectors.toMap(InfoCodeStatDataModel::getInfoCode, Function.identity(), (existing, replacement) -> existing));

        fillInfoCodeStatRecursive(relInfoCodeGroupConfigs, infoCodeDataModelMap);
    }

    private void fillInfoCodeStatRecursive(List<InfoCodeGroupVO> relInfoCodeGroupConfigs, Map<String, InfoCodeStatDataModel> infoCodeDataModelMap) {
        relInfoCodeGroupConfigs.forEach(item -> {
            if (CollUtil.isNotEmpty(item.getRelInfoCodeConfigs())) {
                item.getRelInfoCodeConfigs().forEach(infoCodeConfig -> {
                    String infoCodeWithDataValue = infoCodeConfig.getInfoCode() + infoCodeConfig.getDataValue();
                    String infoCode = infoCodeConfig.getInfoCode();

                    // 先尝试匹配infoCode+dataValue组合
                    InfoCodeStatDataModel modelWithValue = infoCodeDataModelMap.get(infoCodeWithDataValue);
                    InfoCodeStatDataModel model = infoCodeDataModelMap.get(infoCode);

                    // 如果找到匹配项，则更新dataValue
                    if (model != null) {
                        try {
                            model.setDataValue(Integer.parseInt(model.getDataValue().toString()) + 1);
                        } catch (NumberFormatException e) {
                            // 处理数据转换异常
                            logProjectInstance().warn("Failed to parse dataValue to integer", e);
                        }
                    }

                    if (modelWithValue != null) {
                        try {
                            modelWithValue.setDataValue(Integer.parseInt(modelWithValue.getDataValue().toString()) + 1);
                        } catch (NumberFormatException e) {
                            // 处理数据转换异常
                            logProjectInstance().warn("Failed to parse dataValue to integer", e);
                        }
                    }
                });
            }
            if (CollUtil.isNotEmpty(item.getGroupChildren())) {
                fillInfoCodeStatRecursive(item.getGroupChildren(), infoCodeDataModelMap);
            }
        });
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-RWD对象统计";
    }
}
