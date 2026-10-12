package com.persagy.apccontrol.basic.service.demand.blacklist.handler;

import com.persagy.apccontrol.basic.model.dto.demand.blacklist.DemandBlacklistRoomFuncTypeRuleDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 空间功能类型
 */
@Validated
@Component(DemandBlacklistRoomFuncTypeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandBlacklistRoomFuncTypeStrategy extends AbstractDemandBlacklistStrategy<DemandBlacklistRoomFuncTypeRuleDTO> {

    public static final String BEAN_NAME = "demandBlacklistRoomFuncTypeStrategy";

    @Override
    public Set<String> executeRule(@NonNull DemandTemplateTypeEnum demandTemplateType, @NonNull DemandBlacklistRoomFuncTypeRuleDTO configData) {
        HashSet<String> result = new HashSet<>();
        if (CollectionUtils.isEmpty(configData.getRoomFuncTypes())) {
            return result;
        }
        //查询对应空间功能类型下的空间
        DemandStrategy strategy = demandTemplateType.toDemandTypeEnum().demandStrategy();
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(demandTemplateType);
        queryParam.setWithColumns(new HashSet<>(Collections.singletonList(DtpRwdObjectDTO.Fields.id)));
        queryParam.setFilterRoomFuncTypes(configData.rule());
        List<DtpRwdSpaceDTO> spacesId = strategy.listDemandSpace(queryParam);

        if (CollectionUtils.isNotEmpty(spacesId)) {
            return spacesId.stream()
                    .map(DtpRwdSpaceDTO::getId)
                    .collect(Collectors.toSet());
        }
        return result;
    }


}
