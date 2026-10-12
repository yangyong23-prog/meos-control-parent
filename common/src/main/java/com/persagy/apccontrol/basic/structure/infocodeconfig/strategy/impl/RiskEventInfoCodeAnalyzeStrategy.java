package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 设备异常事件世界信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Component(RiskEventInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RiskEventInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "riskEventInfoCodeAnalyzeStrategy";
    private static final String INFO_CODE_NAME = LocaleMessageUtil.getMessage("key_5bf0b8410a1f4947ab5c4d9c580e3db4");

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.RISK_EVENT;
    }

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        for (InfoCodeAggregate aggregate : aggregates) {
            DtpRwdObjectDTO object = aggregate.getIdentifier().getObject();
            String classCode = object.getClassCode();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            String infoCodeAliasName = aggregate.getConfigModel().getInfoCodeAliasName();

            InfoCodeDefineModel infoCodeDefineModel = new InfoCodeDefineModel(infoCode,
                    LocaleMessageUtil.getMessage("key_4e717c8ec9a34332a4a5ef73d80922d8"), classCode, "", "", "");
            infoCodeDefineModel.setDataSources(Lists.newArrayList(
                    new InfoCodeDataSourceDefineModel(infoCode, INFO_CODE_NAME)));
            aggregate.setInfoCodeAndDefineModel(infoCodeDefineModel.setAliasName(infoCodeAliasName));
        }
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        Map<String, List<ExceptionEventBaseInfoVO>> objectIdAndRiskEvents = analyzeAdditionalValues(
                additionalValues, ADDITIONAL_KEY_RISK_EVENT_DETAIL, Collections::emptyMap);
        if (MapUtils.isEmpty(objectIdAndRiskEvents)) {
            return;
        }
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();

            if (objectIdAndRiskEvents.containsKey(objectId)) {
                aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, infoCode, null, null));
            }
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-异常事件标签";
    }
}
