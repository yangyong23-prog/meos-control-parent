package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.knowledge.converter.DtpKnowledgeConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 基础信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public abstract class BaseInfoCodeAnalyzeStrategy implements InfoCodeAnalyzeStrategy, LogComponent {

    public static final String ADDITIONAL_KEY_KNOWLEDGE_DEFINE = "KNOWLEDGE_DEFINE";
    public static final String ADDITIONAL_KEY_IOT_DATA_CONTEXT = "IOT_DATA_CONTEXT";
    public static final String ADDITIONAL_KEY_RISK_EVENT_DETAIL = "RISK_EVENT_DETAIL";
    public static final String TIME_RANGE = "TIME_RANGE";
    public static final String PLACEHOLDERS = "PLACEHOLDERS";

    @Autowired
    private DtpKnowledgeConverter dtpKnowledgeConverter;

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        this.fillAggregateDefineOfEmpty(aggregates, String.class.getSimpleName());
    }

    protected void fillAggregateDefineOfEmpty(
            @NonNull Collection<InfoCodeAggregate> aggregates,
            @NonNull String dataType) {
        for (InfoCodeAggregate aggregate : aggregates) {
            String classCode = aggregate.getIdentifier().getObject().getClassCode();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            String infoCodeName = aggregate.getConfigModel().getInfoCodeName();
            String infoCodeAliasName = aggregate.getConfigModel().getInfoCodeAliasName();

            InfoCodeDefineModel infoCodeDefineModel = InfoCodeDefineModel.ofCustom(
                    infoCode, infoCodeName, classCode, dataType);
            aggregate.setInfoCodeAndDefineModel(infoCodeDefineModel.setAliasName(infoCodeAliasName));
        }
    }

    /**
     * 根据附加值填充聚合根定义值
     *
     * @param aggregates       聚合根
     * @param additionalValues 附加值
     * @since 5.241101.0
     */
    protected void fillAggregateDefineValueByKnowledge(
            @NonNull Collection<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        MultiKeyMap<String, InfoCodeDefineModel> classCodeInfoCodeAndDefine = analyzeAdditionalValues(
                additionalValues, ADDITIONAL_KEY_KNOWLEDGE_DEFINE, MultiKeyMap::new);
        for (InfoCodeAggregate aggregate : aggregates) {
            DtpRwdObjectDTO object = aggregate.getIdentifier().getObject();
            String classCode = object.getClassCode();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            String infoCodeName = aggregate.getConfigModel().getInfoCodeName();
            String infoCodeAliasName = aggregate.getConfigModel().getInfoCodeAliasName();

            InfoCodeDefineModel infoCodeDefine = dtpKnowledgeConverter.copy(
                    classCodeInfoCodeAndDefine.get(classCode, infoCode));
            if (Objects.isNull(infoCodeDefine)) {
                infoCodeDefine = InfoCodeDefineModel.ofCustom(infoCode, infoCodeName,
                        classCode, Object.class.getSimpleName());
            }
            aggregate.setInfoCodeAndDefineModel(infoCodeDefine.setAliasName(infoCodeAliasName));
        }
    }

    /**
     * 根据附加值填充聚合根值
     *
     * @param aggregates           聚合根
     * @param objectIdAndDataValue 对象ID -> 数据值
     * @since 5.241101.0
     */
    protected void fillAggregateDataValueByMap(
            @NonNull Collection<InfoCodeAggregate> aggregates,
            @Nullable Map<String, ?> objectIdAndDataValue) {
        if (CollectionUtils.isEmpty(aggregates) || Objects.isNull(objectIdAndDataValue)) {
            return;
        }
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            Object dataValue = objectIdAndDataValue.get(objectId);
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, dataValue, null, null));
        }
    }

    /**
     * 解析附加值
     *
     * @param additionalValues 附加值
     * @param key              键
     * @param <T>              附加值类型
     * @return 附加值
     * @since 5.241101.0
     */
    public static <T> T analyzeAdditionalValues(@Nullable Map<String, Object> additionalValues, @NonNull String key) {
        return analyzeAdditionalValues(additionalValues, key, () -> null);
    }

    /**
     * 解析附加值
     *
     * @param additionalValues 附加值
     * @param key              键
     * @param defaultIfEmpty   当 {@code additionalValues} 为空或键不存在时, 返回的默认值
     * @param <T>              附加值类型
     * @return 附加值
     * @since 5.241101.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T analyzeAdditionalValues(
            @Nullable Map<String, Object> additionalValues,
            @NonNull String key,
            @NonNull Supplier<T> defaultIfEmpty) {
        if (MapUtils.isEmpty(additionalValues)) {
            return defaultIfEmpty.get();
        }
        Object value = additionalValues.get(key);
        if (Objects.isNull(value)) {
            return defaultIfEmpty.get();
        }
        return (T) value;
    }
}
