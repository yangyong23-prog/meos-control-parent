package com.persagy.apccontrol.basic.service.demand.blacklist.handler;

import cn.hutool.core.map.MapUtil;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.dto.demand.blacklist.DemandBlacklistInstantiateRuleDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * iot规则处理类
 * 查询不包含过滤信息点的空间
 */
@Validated
@Component(DemandBlacklistInstantiateRuleStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes", "unchecked"})
public class DemandBlacklistInstantiateRuleStrategy extends AbstractDemandBlacklistStrategy<DemandBlacklistInstantiateRuleDTO> {

    public static final String BEAN_NAME = "demandBlacklistInstantiateRuleStrategy";
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public Set<String> executeRule(@NonNull DemandTemplateTypeEnum demandTemplateType, @NonNull DemandBlacklistInstantiateRuleDTO configData) {
        HashSet<String> result = new HashSet<>();
        if (CollectionUtils.isEmpty(configData.getNonInstantiationCodes())) {
            return result;
        }
        //查询项目下所有空间 ID
        DemandStrategy strategy = demandTemplateType.toDemandTypeEnum().demandStrategy();
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(demandTemplateType);
        queryParam.setWithColumns(new HashSet<>(Collections.singletonList(DtpRwdObjectDTO.Fields.id)));
        List<DtpRwdSpaceDTO> spaces = strategy.listDemandSpace(queryParam);

        if (CollectionUtils.isEmpty(spaces)) {
            return result;
        }

        Set<String> spacesId = spaces.stream()
                .map(DtpRwdSpaceDTO::getId)
                .collect(Collectors.toSet());

        //查询是否有对应信息点的规则
        Map<String, Set<String>> classCodeAndObjectIds = Collections.singletonMap(
                DtpRwdClassCodeEnum.SPACE.value(), new HashSet<>(spacesId));
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = abstractIotDataCacheServer.
                mapObjectIdAndExistsRuleInfoCodes(classCodeAndObjectIds, new HashSet<>(configData.getNonInstantiationCodes()));

        if (MapUtil.isEmpty(objectIdAndExistsRuleInfoCodes)) {
            return spacesId;
        }
        //排除有规则的空间
        spacesId.forEach(item -> {
            if (!objectIdAndExistsRuleInfoCodes.containsKey(item)) {
                result.add(item);
            }
        });
        return result;
    }

}
