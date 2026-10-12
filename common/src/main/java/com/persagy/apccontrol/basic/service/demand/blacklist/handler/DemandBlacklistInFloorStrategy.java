package com.persagy.apccontrol.basic.service.demand.blacklist.handler;

import com.persagy.apccontrol.basic.model.dto.demand.blacklist.DemandBlacklistInFloorRuleDTO;
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
 * 所在楼层规则
 * 传递的是楼层 ID 通过楼层 ID 查询楼下所有空间
 */
@Validated
@Component(DemandBlacklistInFloorStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandBlacklistInFloorStrategy extends AbstractDemandBlacklistStrategy<DemandBlacklistInFloorRuleDTO> {

    public static final String BEAN_NAME = "demandBlacklistInFloorStrategy";

    @Override
    public Set<String> executeRule(@NonNull DemandTemplateTypeEnum demandTemplateType, @NonNull DemandBlacklistInFloorRuleDTO configData) {
        HashSet<String> result = new HashSet<>();
        if (CollectionUtils.isEmpty(configData.getInFloors())) {
            return result;
        }
        //查询楼层下的空间
        DemandStrategy strategy = demandTemplateType.toDemandTypeEnum().demandStrategy();
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(demandTemplateType);
        queryParam.setWithColumns(new HashSet<>(Collections.singletonList(DtpRwdObjectDTO.Fields.id)));
        queryParam.setFilterFloorIds(configData.rule());
        List<DtpRwdSpaceDTO> spacesId = strategy.listDemandSpace(queryParam);

        if (CollectionUtils.isNotEmpty(spacesId)) {
            return spacesId.stream()
                    .map(DtpRwdSpaceDTO::getId)
                    .collect(Collectors.toSet());
        }
        return result;
    }


}
