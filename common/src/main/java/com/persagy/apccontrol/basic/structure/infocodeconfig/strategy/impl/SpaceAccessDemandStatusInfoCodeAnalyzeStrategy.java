package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.AccessDemandStatusEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.security.SecurityService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS;
import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS_LIST;
import static com.persagy.apccontrol.basic.service.security.SecurityService.*;

/**
 * 空间通行需求状态信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Component(SpaceAccessDemandStatusInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SpaceAccessDemandStatusInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "spaceAccessDemandStatusInfoCodeAnalyzeStrategy";

    private final SecurityService securityService;
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.SPACE_ACCESS_DEMAND_STATUS;
    }

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        String dataType = Enum.class.getSimpleName();
        for (InfoCodeAggregate aggregate : aggregates) {
            String infoCode = aggregate.getConfigModel().getInfoCode();
            String infoCodeName = aggregate.getConfigModel().getInfoCodeName();
            String infoCodeAliasName = aggregate.getConfigModel().getInfoCodeAliasName();

            InfoCodeDefineModel infoCodeDefineModel = InfoCodeDefineModel.ofCustom(
                    infoCode, infoCodeName, DtpRwdClassCodeEnum.SPACE.value(), dataType);
            List<InfoCodeDataSourceDefineModel> dataSources = new ArrayList<>();
            for (AccessDemandStatusEnum accessDemandStatus : AccessDemandStatusEnum.values()) {
                dataSources.add(new InfoCodeDataSourceDefineModel(
                        accessDemandStatus.value(), accessDemandStatus.getReasonPhrase()));
            }
            aggregate.setInfoCodeAndDefineModel(infoCodeDefineModel
                    .setAliasName(infoCodeAliasName).setDataSources(dataSources));
        }
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        String projectId = ContextUtil.getProjectId();
        Set<String> spaceIds = InfoCodeAggregate.listAllObjectIds(aggregates);
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        dtpRwdObjectQueryParam.setClassCodeEnum(DOOR_EQUIPMENT_CLASS);
        dtpRwdObjectQueryParam.addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST);
        List<DtpRwdEquipmentDTO> secureEquipmentList = dtpRwdClientService.listEquipmentInBySpaceIds(
                spaceIds, dtpRwdObjectQueryParam);
        ArrayList<String> spaceIdList = new ArrayList<>(spaceIds);
        Map<String, AccessDemandStatusEnum> spaceIdAndAccessDemandStatus = new HashMap<>();
        if (CollectionUtils.isNotEmpty(secureEquipmentList)) {
            spaceIdAndAccessDemandStatus.putAll(securityService.mapSpaceAccessState(
                    projectId, spaceIdList, null, null));
            spaceIdList.forEach(x -> {
                if (!spaceIdAndAccessDemandStatus.containsKey(x)) {
                    spaceIdAndAccessDemandStatus.put(x, AccessDemandStatusEnum.UNMONITORED);
                }
            });
        }


        for (InfoCodeAggregate aggregate : aggregates) {
            String spaceId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();

            AccessDemandStatusEnum accessDemandStatus = spaceIdAndAccessDemandStatus.get(spaceId);
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(
                    infoCode, accessDemandStatus, null, null));
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-空间通行需求状态";
    }
}
