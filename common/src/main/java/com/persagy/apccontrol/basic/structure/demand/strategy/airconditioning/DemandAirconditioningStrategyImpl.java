package com.persagy.apccontrol.basic.structure.demand.strategy.airconditioning;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.dto.DemandInstanceCustomizedTagDTO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.AbstractDemandStrategyProcessor;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求管理-空调需求策略接口实现
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>listSpaces</b> - 查询空调需求空间</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Validated
@Service(DemandAirconditioningStrategyImpl.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes", "unchecked"})
public class DemandAirconditioningStrategyImpl
        extends AbstractDemandStrategyProcessor
        implements DemandAirconditioningStrategy {

    public static final String BEAN_NAME = "demandAirconditioningStrategyImpl";

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public List<DemandTemplateTypeEnum> listTemplateTypes() {
        return Lists.newArrayList(DemandTemplateTypeEnum.AIRCONDITIONING);
    }

    @Override
    public List<DtpRwdSpaceDTO> listDemandSpace(@NonNull DemandSpacesQueryParam param) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        if (CollectionUtils.isEmpty(param.getWithColumns())) {
            queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.class);
        }
        List<DtpRwdSpaceDTO> spaces = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Lists.newArrayList();
        }
        return spaces;
    }

    @Override
    public List<DemandInstanceCustomizedTagDTO> mapDemandInstanceCustomizedTagNameAndValue(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<DemandInstanceValueObject> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return Lists.newArrayList();
        }
        Set<String> spaceIds = instances.stream()
                .filter(item -> item.getInstanceType() == DemandInstanceTypeEnum.SPACE)
                .map(DemandInstanceValueObject::getInstanceId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Lists.newArrayList();
        }
        Map<DemandInstanceValueObject, Map<String, Object>> instanceAndTags = new HashMap<>();
        // 查询是否有服务冷源/热源/冷热源系统
        List<DtpRwdObjectDTO> serviceSystems = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                .setClassCodeEnum(Lists.newArrayList(
                        DtpRwdClassCodeEnum.COLD_SYSTEM,
                        DtpRwdClassCodeEnum.HOT_SYSTEM,
                        DtpRwdClassCodeEnum.COLD_HOT_SYSTEM))
                .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE).setObjTo(spaceIds))
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds)
        );
        if (CollectionUtils.isNotEmpty(serviceSystems)) {
            for (DtpRwdObjectDTO serviceSystem : serviceSystems) {
                String systemId = serviceSystem.getId();
                Set<String> relObjIds = serviceSystem.getRelObjIds();
                if (CollectionUtils.isNotEmpty(relObjIds)) {
                    for (String relObjId : relObjIds) {
                        DemandInstanceValueObject valueObject = new DemandInstanceValueObject(
                                DemandInstanceTypeEnum.SPACE, relObjId);
                        Set<String> relSystemIds = Optional.ofNullable((Set<String>) MapExecutor.dynamicGet(instanceAndTags,
                                valueObject, DemandInstanceCustomizedTagDTO.SERVICE_COLD_HEAT_SYSTEM_IDS)).orElseGet(HashSet::new);
                        relSystemIds.add(systemId);
                        MapExecutor.dynamicPut(instanceAndTags, valueObject,
                                DemandInstanceCustomizedTagDTO.SERVICE_COLD_HEAT_SYSTEM_IDS,
                                relSystemIds, HashMap::new);
                    }
                }
            }
        }
        // 查询是否存在温度IOT规则
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                Collections.singletonMap(DtpRwdClassCodeEnum.SPACE.value(), spaceIds),
                Lists.newArrayList(DtpRwdSpaceDTO.Fields.tdb));
        objectIdAndExistsRuleInfoCodes.forEach((spaceId, existsRuleInfoCodes) -> {
            if (existsRuleInfoCodes.contains(DtpRwdSpaceDTO.Fields.tdb)) {
                MapExecutor.dynamicPut(instanceAndTags,
                        new DemandInstanceValueObject(DemandInstanceTypeEnum.SPACE, spaceId),
                        DemandInstanceCustomizedTagDTO.EXIST_TAB_IOT_INFO_CODE_RULE, true, HashMap::new);
            }
        });
        return instanceAndTags.entrySet().stream()
                .map(item -> new DemandInstanceCustomizedTagDTO()
                        .setInstance(item.getKey())
                        .setCustomizedTag(item.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public DemandTypeEnum demandType() {
        return DemandTypeEnum.AIRCONDITIONING;
    }

    @Override
    public String logTag() {
        return "需求管理-空调需求策略";
    }
}
