package com.persagy.apccontrol.basic.structure.common.strategy;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.BaseObjectQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSystemQueryParam;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.ObjectInstantiateQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.param.DemandInstanceConfigQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基础运行监测策略类
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
public abstract class BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull @Valid RunningMonitorSpaceQueryParam param) {
        return this.listSpaces(param.getRunningMonitorSecondType(), toInstance(param), param.toWithColumnArray());
    }

    @Override
    public List<? extends DtpRwdEquipmentDTO> listEquipments(@NonNull @Valid RunningMonitorEquipmentQueryParam param) {
        return this.listEquipments(param.getRunningMonitorSecondType().toRunningMonitorTypeEnum(),
                        toInstance(param), param.toWithColumnArray()).stream()
                .map(item -> {
                    DtpRwdEquipmentDTO object = new DtpRwdEquipmentDTO();
                    BeanUtils.copyProperties(item, object);
                    return object;
                }).collect(Collectors.toList());
    }

    @Override
    public List<? extends DtpRwdSpaceVO> listDashboardSpaces(@NonNull @Valid RunningMonitorSpaceQueryParam param) {
        DtpRwdConvert convert = SpringUtil.getBean(DtpRwdConvert.class);
        return this.listSpaces(param).stream()
                .map(convert::toSpaceVo)
                .collect(Collectors.toList());
    }

    @Override
    @Deprecated
    public List<? extends DtpRwdSpaceVO> listDashboardSpaces(
            @NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
        spaceQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
        spaceQueryParam.fillByInstance(instance);
        spaceQueryParam.addWithColumn(withColumns);
        return this.listDashboardSpaces(spaceQueryParam);
    }

    @Override
    public List<? extends DtpRwdObjectDTO> listDashboardSystems(@NonNull @Valid RunningMonitorSystemQueryParam param) {
        return Lists.newArrayList();
    }

    /**
     * 兼容老版本的实例入参
     *
     * @param param 查询参数
     * @return 老版本的实例入参
     * @since 7.250201.0
     */
    private <T extends DtpRwdObjectDTO> DemandInstanceValueObject toInstance(@NonNull BaseObjectQueryParam<T> param) {
        Set<String> filterBuildingIds = param.getFilterBuildingIds();
        Set<String> filterFloorIds = param.getFilterFloorIds();
        Set<String> filterObjectIds = param.getFilterObjectIds();
        boolean buildingIdsExists = CollectionUtils.isNotEmpty(filterBuildingIds);
        boolean floorIdsExists = CollectionUtils.isNotEmpty(filterFloorIds);
        boolean objectIdsExists = CollectionUtils.isNotEmpty(filterObjectIds);
        int existsCount = 0;
        existsCount = buildingIdsExists ? existsCount + 1 : existsCount;
        existsCount = floorIdsExists ? existsCount + 1 : existsCount;
        existsCount = objectIdsExists ? existsCount + 1 : existsCount;
        // 建筑ID/楼层ID/空间ID只能存在任意一个类型, 过时方法仅支持单个对象类型入参
        if (existsCount > 1) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_f3bbc68962344123a569e788fe003cea"));
        }
        boolean buildingIdsExceedingLimit = buildingIdsExists && filterBuildingIds.size() > 1;
        boolean floorIdsExceedingLimit = floorIdsExists && filterFloorIds.size() > 1;
        boolean objectIdsExceedingLimit = objectIdsExists && filterObjectIds.size() > 1;
        // 建筑ID/楼层ID/空间ID只能存在任意一个类型, 且集合中最多只能有一个元素, 过时方法仅支持单个对象入参
        if (buildingIdsExceedingLimit || floorIdsExceedingLimit || objectIdsExceedingLimit) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_f3bbc68962344123a569e788fe003cea"));
        }
        DemandInstanceValueObject instance = new DemandInstanceValueObject();
        if (buildingIdsExists) {
            instance.setInstanceType(DemandInstanceTypeEnum.BUILDING);
            instance.setInstanceId(IterableUtils.first(filterBuildingIds));
        }
        if (floorIdsExists) {
            instance.setInstanceType(DemandInstanceTypeEnum.FLOOR);
            instance.setInstanceId(IterableUtils.first(filterFloorIds));
        }
        if (objectIdsExists) {
            instance.setInstanceType(DemandInstanceTypeEnum.SPACE);
            instance.setInstanceId(IterableUtils.first(filterObjectIds));
        }
        if (Objects.isNull(instance.getInstanceType())) {
            instance.setInstanceType(DemandInstanceTypeEnum.PROJECT);
            instance.setInstanceId(ContextUtil.getProjectId());
        }
        return instance;
    }

    /**
     * 过滤实例化规则（按信息点取并集）,如果信息点只有一个，交集和并集的结果一样
     *
     * @param objects   对象列表
     * @param infoCodes 信息点列表
     * @return {@link List }<{@link T }>
     */
    @NonNull
    protected <T extends DtpRwdObjectDTO> List<T> filterInstantiateRuleToUnion(@NonNull Collection<T> objects, @NonNull Collection<String> infoCodes) {
        //过滤温度实例化的空间
        DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);
        Set<String> objectIds = objects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        List<ObjectInstantiateQueryParam> queryParams = new ArrayList<>();
        for (String objectId : objectIds) {
            for (String infoCode : infoCodes) {
                queryParams.add(ObjectInstantiateQueryParam.batchInstance(objectId, infoCode));
            }
        }
        List<String> instantiateRuleSpaceIds = dtpRwdClientService.listExistsInstantiateObjectIds(false, false, ContextUtil.getProjectId(), queryParams);
        return objects.stream().filter(object -> instantiateRuleSpaceIds.contains(object.getId())).collect(Collectors.toList());

    }

    /**
     * 过滤实例化规则（按信息点取交集）,如果信息点只有一个，交集和并集的结果一样
     *
     * @param objects   对象列表
     * @param infoCodes 信息点列表
     * @return {@link List }<{@link T }>
     */
    @NonNull
    protected <T extends DtpRwdObjectDTO> List<T> filterInstantiateRuleToIntersection(@NonNull Collection<T> objects, @NonNull Collection<String> infoCodes) {
        //过滤温度实例化的空间
        DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);
        Set<String> objectIds = objects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        List<ObjectInstantiateQueryParam> queryParams = new ArrayList<>();
        for (String objectId : objectIds) {
            for (String infoCode : infoCodes) {
                queryParams.add(ObjectInstantiateQueryParam.batchInstance(objectId, infoCode));
            }
        }
        Map<String, Set<String>> instantiateRuleSpaceIds = dtpRwdClientService.mapObjectIdAndInstantiateInfoCodes(ContextUtil.getProjectId(), queryParams);
        Set<String> filterObjectIds = new HashSet<>();
        instantiateRuleSpaceIds.forEach((k, v) -> {
            if (v.size() == infoCodes.size()) {
                filterObjectIds.add(k);
            }
        });
        return objects.stream().filter(object -> filterObjectIds.contains(object.getId())).collect(Collectors.toList());

    }

    /**
     * 过滤无效的需求空间(黑名单和屏蔽空间都会过滤)
     *
     * @param spaces             空间列表
     * @param demandTemplateType 需求模板类型
     * @return {@link List }<{@link T }>
     */
    @NonNull
    protected <T extends DtpRwdSpaceDTO> List<T> filterInvalidDemandSpace(@Nullable List<T> spaces, @NonNull DemandTemplateTypeEnum demandTemplateType) {
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptyList();
        }
        DemandInstanceGateway demandInstanceGateway = SpringUtil.getBean(DemandInstanceGateway.class);
        DemandInstanceConfigQueryParam instanceConfigQueryParam = new DemandInstanceConfigQueryParam();
        instanceConfigQueryParam.setTemplateType(demandTemplateType);
        instanceConfigQueryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
        instanceConfigQueryParam.setInstanceIds(spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet()));
        instanceConfigQueryParam.setFilterConfig(new DemandInstanceConfigValueObject().setInBlacklisted(true).setShielded(true));
        //拿到黑名单的空间
        Set<String> blackListSpaceIds = demandInstanceGateway.listInstanceIds(instanceConfigQueryParam);
        if (CollectionUtils.isEmpty(blackListSpaceIds)) {
            return spaces;
        }
        //过滤掉黑名单空间
        return spaces.stream()
                .filter(item -> !blackListSpaceIds.contains(item.getId()))
                .collect(Collectors.toList());
    }
}
