package com.persagy.apccontrol.basic.structure.infocodeconfig.lifecycle;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeConfigModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.StaticInfoCodeTackValueLogicModel;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基础信息点过滤回调
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InfoCodeAggregateLifeFilterAware implements InfoCodeAggregateLifeAware {

    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public void afterAggregateInit(@NonNull List<InfoCodeAggregate> aggregates) {
        Map<String, Set<String>> objectIdAndInfoCodes = new HashMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            DtpRwdObjectDTO object = aggregate.getIdentifier().getObject();
            String classCode = object.getClassCode();

            // 当为监控摄像头时, 如果hasArmingFunc不为1则移除armingSet设定信息点
            if (StringUtils.equals(classCode, DtpRwdClassCodeEnum.SPVSCM.value())) {
                MapExecutor.dynamicPut(objectIdAndInfoCodes, object.getId(), BaseConvert.INFO_CODE_HAS_ARMING_FUNC, HashSet::new);
            }
        }
        // noinspection DuplicatedCode
        Map<String, DtpRwdObjectDTO> objectIdAndData = Collections.emptyMap();
        if (MapUtils.isNotEmpty(objectIdAndInfoCodes)) {
            DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
            queryParam.setId(objectIdAndInfoCodes.keySet());
            queryParam.setWithColumns(objectIdAndInfoCodes.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet()));
            objectIdAndData = DtpRwdObjectDTO.mapIdAndData(dtpRwdClientService.listObject(queryParam));
        }
        Iterator<InfoCodeAggregate> iterator = aggregates.iterator();
        while (iterator.hasNext()) {
            InfoCodeAggregate aggregate = iterator.next();
            DtpRwdObjectDTO object = aggregate.getIdentifier().getObject();
            String classCode = object.getClassCode();
            InfoCodeConfigModel configModel = aggregate.getConfigModel();
            String infoCode = configModel.getInfoCode();
            StaticInfoCodeTackValueLogicModel tackValueLogicModel = configModel.analyzeTackValueLogicModel();

            // 存在项目过滤时, 移除不匹配项目的信息点
            if (!tackValueLogicModel.matchProject()) {
                iterator.remove();
                continue;
            }
            // 当为监控摄像头时, 如果hasArmingFunc不为1则移除armingSet设定信息点
            if (StringUtils.equals(classCode, DtpRwdClassCodeEnum.SPVSCM.value())) {
                DtpRwdObjectDTO matchObject = objectIdAndData.get(object.getId());
                if (Objects.nonNull(matchObject)) {
                    String hasArmingFunc = matchObject.analyzeExpressionByRwdObject(
                            BaseConvert.INFO_CODE_HAS_ARMING_FUNC, String.class);
                    if (!StringUtils.equals(hasArmingFunc, "1")
                            && (StringUtils.equals(BaseConvert.INFO_CODE_ARMING_SET, infoCode)
                            || StringUtils.equals(BaseConvert.INFO_CODE_ARMING_STATUS, infoCode))) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    @Override
    public void afterFillInfoCodeDataModel(@NonNull Collection<InfoCodeAggregate> aggregates) {
        Iterator<InfoCodeAggregate> iterator = aggregates.iterator();
        while (iterator.hasNext()) {
            InfoCodeAggregate aggregate = iterator.next();
            InfoCodeTypeEnum infoCodeType = aggregate.getConfigModel().getInfoCodeType();
            if (infoCodeType == InfoCodeTypeEnum.BASIC) {
                InfoCodeDataModel infoCodeDataModel = aggregate.getInfoCodeAndDataModel();
                if (Objects.isNull(infoCodeDataModel)) {
                    continue;
                }
                Object dataValue = infoCodeDataModel.getDataValue();
                if (ObjectUtils.isEmpty(dataValue)) {
                    iterator.remove();
                    continue;
                }
            }
            if (aggregate.isInvalid()) {
                iterator.remove();
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
