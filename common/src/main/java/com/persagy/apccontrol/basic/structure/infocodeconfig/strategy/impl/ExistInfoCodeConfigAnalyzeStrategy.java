package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeTemplateTypeResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeIdentifier;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.ExistInfoCodeConfigModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 存在信息点配置解析策略
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Component(ExistInfoCodeConfigAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ExistInfoCodeConfigAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    private final InfoCodeTemplateTypeResource infoCodeTemplateTypeResource;

    public static final String BEAN_NAME = "existInfoCodeConfigAnalyzeStrategy";

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.EXIST_INFO_CODE_CONFIG;
    }

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        Set<String> classCodeSet = new HashSet<>();
        Set<InfoCodeTemplateTypeEnum> templateTypeEnumSet = new HashSet<>();
        Set<RunningMonitorTypeEnum> runningMonitorTypeEnums = new HashSet<>();
        Set<DtpRwdObjectTypeEnum> dtpRwdObjectTypeEnums = new HashSet<>();
        Set<RunningMonitorSecondTypeEnum> runningMonitorSecondTypeEnums = new HashSet<>();
        Set<DtpRwdObjectDTO> objects = new HashSet<>();
        //组装条件
        for (InfoCodeAggregate aggregate : aggregates) {
            ExistInfoCodeConfigModel existInfoCodeConfigModel = aggregate.getConfigModel().analyzeTackValueLogicModel(ExistInfoCodeConfigModel.class);
            templateTypeEnumSet.add(existInfoCodeConfigModel.getInfoCodeTemplateType());
            runningMonitorTypeEnums.add(aggregate.getTemplateTypeModel().getRunningMonitorType());
            classCodeSet.add(aggregate.getIdentifier().getObject().getClassCode());
            dtpRwdObjectTypeEnums.add(aggregate.getTemplateTypeModel().getObjectType());
            RunningMonitorSecondTypeEnum runningMonitorSecondType = aggregate.getTemplateTypeModel().getRunningMonitorSecondType();
            if (Objects.nonNull(runningMonitorSecondType)) {
                runningMonitorSecondTypeEnums.add(runningMonitorSecondType);
            }
            objects.add(aggregate.getIdentifier().getObject());
        }
        InfoCodeTemplateTypeQueryDaoParam queryParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryParam.setTemplateTypes(templateTypeEnumSet);
        queryParam.setClassCodes(classCodeSet);
        queryParam.setObjectTypes(dtpRwdObjectTypeEnums);
        queryParam.setRunningMonitorTypes(runningMonitorTypeEnums);
        queryParam.setRunningMonitorSecondTypes(runningMonitorSecondTypeEnums);
        //查询结果
        List<InfoCodeTemplateType> resTemplateTypes = infoCodeTemplateTypeResource.list(queryParam);
        Map<String, Boolean> valueMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(resTemplateTypes)) {
            InfoCodeConverter converter = SpringUtil.getBean(InfoCodeConverter.class);
            List<InfoCodeTemplateTypeModel> templateTypeModels = resTemplateTypes.stream()
                    .map(converter::toTemplateTypeModel)
                    .sorted(Comparator.comparing(item -> Objects.nonNull(item.parseFilterValueObject()) ? 1 : 0))
                    .collect(Collectors.toList());

            for (DtpRwdObjectDTO object : objects) {
                for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
                    boolean match = templateTypeModel.match(object);
                    if (match) {
                        //如果找到一个，直接退出
                        valueMap.put(object.getId(), true);
                        break;
                    }
                }
            }
        }
        //组装值对象
        for (InfoCodeAggregate aggregate : aggregates) {
            InfoCodeDataModel infoCodeDataModel = new InfoCodeDataModel();
            InfoCodeIdentifier identifier = aggregate.getIdentifier();
            infoCodeDataModel.setInfoCode(identifier.getInfoCode());
            infoCodeDataModel.setDataValue(valueMap.getOrDefault(identifier.getObject().getId(), false));
            aggregate.setInfoCodeAndDataModel(infoCodeDataModel);
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-存在信息点配置";
    }
}
