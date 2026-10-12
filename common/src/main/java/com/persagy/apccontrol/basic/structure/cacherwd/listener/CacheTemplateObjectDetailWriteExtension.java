package com.persagy.apccontrol.basic.structure.cacherwd.listener;

import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheTemplateObjectDetailRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheTemplateObjectDetailRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheTemplateWriteExtension;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheWriteAction;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl.RwdObjectDetailInfoCodeAnalyzeStrategy;
import com.persagy.framework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 模板对象明细复合写入扩展。
 *
 * <p>模板重建编排取得当前模板键的完整对象后，本扩展复用同一批对象构造
 * {@code RWD_OBJECT_DETAIL} 聚合结果，并返回进入复合事务后执行的明细替换动作。所有远程查询、
 * 聚合和 JSON 投影均在动作创建阶段完成，因而任何计算失败都不会先删除该模板键的旧明细。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheTemplateObjectDetailWriteExtension implements CacheTemplateWriteExtension {

    /**
     * 信息点聚合网关，负责复用现有对象详情计算上下文。
     *
     * @since v7.260701.0
     */
    private final @NonNull InfoCodeGateway infoCodeGateway;

    /**
     * 模板对象明细范围替换仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheTemplateObjectDetailRepository detailRepository;

    /**
     * 模板对象明细 JSON 投影转换器。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheTemplateObjectDetailRecordConverter detailRecordConverter;

    @Override
    public @NonNull CacheWriteAction prepareModelSave(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull List<DtpRwdObjectDTO> objects) {
        String templateKey = templateTypeModel.getTemplateTypeKey();
        List<CacheTemplateObjectDetail> details = this.toDetails(
                templateKey, templateTypeModel, objects);
        return () -> this.detailRepository.replaceDetails(templateKey, details);
    }

    @Override
    public @NonNull CacheWriteAction prepareModelRemove(
            @NonNull Collection<String> templateKeys,
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        List<String> keys = new ArrayList<>(templateKeys);
        return () -> {
            for (String templateKey : keys) {
                this.detailRepository.replaceDetails(templateKey, Collections.emptyList());
            }
        };
    }

    /**
     * 使用现有聚合网关生成一个模板键的全部明细记录。
     *
     * <p>该方法不触碰数据库；只有整个列表成功生成后，返回的写动作才会进入范围替换事务。</p>
     *
     * @param templateKey       模板键
     * @param templateTypeModel 模板类型模型
     * @param objects           当前模板键的完整对象集合
     * @return 当前模板键的完整明细记录，空集合表示应清空范围
     * @throws BusinessException 聚合结果缺失或不符合明细契约时抛出
     * @since v7.260701.0
     */
    private @NonNull List<CacheTemplateObjectDetail> toDetails(
            @NonNull String templateKey,
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull List<DtpRwdObjectDTO> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return Collections.emptyList();
        }
        InfoCodeInitByObjectParam param = this.toInitParam(templateTypeModel, objects);
        List<InfoCodeAggregate> aggregates = this.infoCodeGateway.of(param);
        List<CacheTemplateObjectDetail> details = new ArrayList<>(aggregates.size());
        for (InfoCodeAggregate aggregate : aggregates) {
            details.add(this.toDetail(templateKey, aggregate));
        }
        return details;
    }

    /**
     * 构造仅计算对象详情信息点的聚合初始化参数。
     *
     * @param templateTypeModel 模板类型模型
     * @param objects            当前模板键对象集合
     * @return 聚合初始化参数
     * @since v7.260701.0
     */
    private @NonNull InfoCodeInitByObjectParam toInitParam(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull List<DtpRwdObjectDTO> objects) {
        InfoCodeInitByObjectParam param = new InfoCodeInitByObjectParam();
        param.setObjects(objects);
        param.setRunningMonitorType(templateTypeModel.getRunningMonitorType());
        param.setRunningMonitorSecondType(templateTypeModel.getRunningMonitorSecondType());
        param.setTemplateTypeIds(Collections.singleton(templateTypeModel.getTemplateTypeId()));
        param.setTemplateTypes(Collections.singleton(templateTypeModel.getTemplateType()));
        param.setTemplateTypeModels(Collections.singleton(templateTypeModel));
        param.setInfoCodeTypes(Collections.singletonList(InfoCodeTypeEnum.RWD_OBJECT_DETAIL));
        param.setForceMathTemplate(true);
        param.setQueryIotInfoCodeRule(false);
        param.setFillInfoCodeDataModel(true);
        // 本次写入结果必须独立于目标缓存表，READY 后也强制执行真实对象明细计算。
        param.setPlaceholders(Collections.singletonMap(
                RwdObjectDetailInfoCodeAnalyzeStrategy.FORCE_SOURCE_CALCULATION_OPTION,
                Boolean.TRUE));
        return param;
    }

    /**
     * 将一个聚合根的数据模型转换为自包含明细记录。
     *
     * @param templateKey 模板键
     * @param aggregate   信息点聚合根
     * @return 待持久化明细记录
     * @throws BusinessException 聚合根没有明细数据或数据类型不正确时抛出
     * @since v7.260701.0
     */
    @SuppressWarnings("unchecked")
    private @NonNull CacheTemplateObjectDetail toDetail(
            @NonNull String templateKey,
            @NonNull InfoCodeAggregate aggregate) {
        if (aggregate.getIdentifier() == null || aggregate.getIdentifier().getObject() == null) {
            throw CacheObjectModelExceptionFactory.requiredValue(
                    "cacheTemplateObjectDetail.object");
        }
        if (aggregate.getConfigModel() == null) {
            throw CacheObjectModelExceptionFactory.requiredValue(
                    "cacheTemplateObjectDetail.configModel");
        }
        InfoCodeDataModel dataModel = aggregate.getInfoCodeAndDataModel();
        if (dataModel == null || !(dataModel.getDataValue() instanceof List)) {
            throw CacheObjectModelExceptionFactory.invalidStructure(
                    "cacheTemplateObjectDetail.infoCodeDataModel");
        }
        DtpRwdObjectDTO object = aggregate.getIdentifier().getObject();
        String objectId = this.requireText(
                object.getId(), "cacheTemplateObjectDetail.objectId");
        String infoCode = this.requireText(
                aggregate.getConfigModel().getInfoCode(),
                "cacheTemplateObjectDetail.infoCode");
        List<InfoCodeRwdObjectDetailDTO> result =
                (List<InfoCodeRwdObjectDetailDTO>) dataModel.getDataValue();
        return this.detailRecordConverter.toDetail(templateKey, objectId, infoCode, result);
    }

    /**
     * 校验聚合结果中将作为持久化业务键的文本。
     *
     * @param value     待校验文本
     * @param valueName 稳定字段名称
     * @return 非空文本
     * @throws BusinessException 文本为空时抛出
     * @since v7.260701.0
     */
    private @NonNull String requireText(
            @Nullable String value,
            @NonNull String valueName) {
        if (StringUtils.isBlank(value)) {
            throw CacheObjectModelExceptionFactory.requiredValue(valueName);
        }
        return value;
    }
}
