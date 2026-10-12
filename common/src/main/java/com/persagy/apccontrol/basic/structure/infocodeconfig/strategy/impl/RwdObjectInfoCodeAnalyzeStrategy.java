package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeConfigModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executor;

/**
 * 设备物理世界信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Component(RwdObjectInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RwdObjectInfoCodeAnalyzeStrategy
        extends BaseRwdObjectInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "equipmentRwdObjectInfoCodeAnalyzeStrategy";
    private static final String OBJECT_ID = "objectId";

    private final Executor executor;
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.RWD_OBJECT;
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
        for (Map.Entry<InfoCodeTemplateTypeModel, Map<InfoCodeConfigModel, List<InfoCodeAggregate>>> entry
                : templateTypeConfigAndAggregates.entrySet()) {
            for (Map.Entry<InfoCodeConfigModel, List<InfoCodeAggregate>> innerEntry : entry.getValue().entrySet()) {
                List<InfoCodeAggregate> eachAggregates = innerEntry.getValue();

                // 所有对象ID
                Set<String> objectIds = InfoCodeAggregate.listAllObjectIds(eachAggregates);

                // 取出第一个聚合根, 按理说同一个模版下的取值逻辑是一致的
                InfoCodeAggregate firstAggregate = IterableUtils.first(eachAggregates);
                InfoCodeTakeValueLogicRwdObjectModel tackValueLogicModel = firstAggregate.getConfigModel()
                        .analyzeTackValueLogicModel(InfoCodeTakeValueLogicRwdObjectModel.class);

                String infoCode = tackValueLogicModel.getInfoCode();
                String takeValueExpression = tackValueLogicModel.getTakeValueExpression();
                if (Objects.isNull(tackValueLogicModel.getQueryParam())) {
                    continue;
                }
                DtpRwdObjectQueryParam queryParam = tackValueLogicModel.getQueryParam()
                        .replacePlaceholder(Collections.singletonMap(OBJECT_ID, objectIds));
                Map<String, List<DtpRwdObjectDTO>> rootObjectIdAndObjects = Collections.emptyMap();
                if (Objects.nonNull(queryParam)) {
                    queryParam.addWithColumnWithIdRelation();
                    List<DtpRwdObjectDTO> objects = BusinessThreadPoolExecutor.parallelExecute(() ->
                            dtpRwdClientService.listObject(queryParam));
                    rootObjectIdAndObjects = toRootObjectIdAndObjects(objectIds, objects);
                }
                for (InfoCodeAggregate aggregate : eachAggregates) {
                    String rootObjectId = aggregate.getIdentifier().getObject().getId();

                    List<String> dataValues = new ArrayList<>();
                    if (MapUtils.isNotEmpty(rootObjectIdAndObjects) && rootObjectIdAndObjects.containsKey(rootObjectId)) {
                        for (DtpRwdObjectDTO object : rootObjectIdAndObjects.get(rootObjectId)) {
                            dataValues.add(object.analyzeExpressionByRwdObject(takeValueExpression, String.class));
                        }
                    }
                    aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, dataValues, null, null));
                }
            }
        }

    }

    /**
     * 转换根对象ID和对象映射
     *
     * @param rootObjectIds 根对象ID
     * @param objects       对象
     * @return 根对象ID -> 关联对象
     * @since 7.250301.0
     */
    private Map<String, List<DtpRwdObjectDTO>> toRootObjectIdAndObjects(
            @NonNull Set<String> rootObjectIds,
            @NonNull List<DtpRwdObjectDTO> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return Maps.newHashMap();
        }
        Map<String, List<DtpRwdObjectDTO>> result = new HashMap<>();
        // 关联查询时根据关联ID填充根对象和查询对象映射
        for (DtpRwdObjectDTO object : objects) {
            Set<String> relObjIds = object.getRelObjIds();
            if (CollectionUtils.isEmpty(relObjIds)) {
                // 不是关联查询时如果根对象存在, 则将对象分别和每个根对象关联
                for (String baseObjectId : rootObjectIds) {
                    MapExecutor.dynamicPut(result, baseObjectId, object, ArrayList::new);
                }
                continue;
            }
            for (String relObjId : relObjIds) {
                MapExecutor.dynamicPut(result, relObjId, object, ArrayList::new);
            }
        }
        return result;
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-RWD对象";
    }
}
