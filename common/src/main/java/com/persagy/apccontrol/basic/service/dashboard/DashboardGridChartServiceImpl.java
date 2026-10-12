package com.persagy.apccontrol.basic.service.dashboard;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.dashboard.DashboardBatchControlQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandSpaceByGroupFloorQueryParam;
import com.persagy.apccontrol.basic.model.vo.dashboard.DashboardBatchControlVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.dashboard.space.QuerySpace;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorComputerRoomSecurityStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.*;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryConfigValueObject;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Dashboard服务
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DashboardGridChartServiceImpl implements DashboardGridChartService {

    private final InfoCodeConverter infoCodeConverter;
    private final QuerySpace airTerminalQuerySpace;
    private final InfoCodeGateway infoCodeGateway;
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public List<? extends DtpRwdSpaceVO> querySpace(DemandSpaceByGroupFloorQueryParam param) {
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        RunningMonitorStrategy runningMonitorStrategy = runningMonitorType.getRunningMonitorStrategy();
        switch (runningMonitorType) {
            case AIR_TERMINAL:
                return airTerminalQuerySpace.querySpace(param.getInstance(), true);
            case VENTILATION:
                RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
                if (Objects.isNull(runningMonitorSecondType)) {
                    throw new ValidationException(LocaleMessageUtil.getMessage("key_4a1af0119a4f4fdc8f6cfe10e1ea451e"));
                }
                return runningMonitorStrategy.listDashboardSpaces(
                        runningMonitorSecondType, param.getInstance(), param.getWithColumns());
            case COMPUTER_ROOM_SECURITY:
                RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
                spaceQueryParam.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
                spaceQueryParam.fillByInstance(param.getInstance());
                spaceQueryParam.addWithColumn(param.getWithColumns());
                spaceQueryParam.setMeosCache(true);
                return runningMonitorStrategy.listDashboardSpaces(spaceQueryParam);
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public List<? extends DtpRwdSpaceVO> querySpaceComputerRoomSecurity() {
        //获取机房策略
        RunningMonitorComputerRoomSecurityStrategy runningMonitorStrategy = (RunningMonitorComputerRoomSecurityStrategy) RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY.getRunningMonitorStrategy();

        //构造固定查询参数
        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
        spaceQueryParam.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.COMPUTER_ROOM_SECURITY);
        DemandInstanceValueObject instance = new DemandInstanceValueObject();
        instance.setInstanceId(ContextUtil.getProjectId());
        instance.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        spaceQueryParam.fillByInstance(instance);
        spaceQueryParam.addWithColumn("floorId","localName","roomFuncType","roomFuncTypeName","classCode","buildingId","objType","id","roomFuncType");
        return runningMonitorStrategy.listDashboardSpacesComputerRoomSecurity(spaceQueryParam);
    }

    @Override
    public List<? extends DtpRwdEquipmentVO> queryEquipment(DemandSpaceByGroupFloorQueryParam param) {
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        RunningMonitorStrategy runningMonitorStrategy = runningMonitorType.getRunningMonitorStrategy();
        List<? extends DtpRwdEquipmentVO> dtpRwdEquipmentVOS = runningMonitorStrategy.listEquipments(param.getRunningMonitorType(), param.getInstance(), param.getWithColumns());
        return dtpRwdEquipmentVOS;
    }

    @Override
    public List<DashboardBatchControlVO> listBatchControl(@NonNull DashboardBatchControlQueryParam param) {
        InfoCodeObjectQueryParam objectQueryParam = infoCodeConverter.toTemplateQueryParam(param);
        List<InfoCodeTemplateTypeModel> templateTypeModels = infoCodeGateway
                .listTemplateTypesWithObjects(objectQueryParam);
        if (CollectionUtils.isEmpty(templateTypeModels)) {
            return Collections.emptyList();
        }
        infoCodeGateway.fillInfoCodeTemplateTypeModel(templateTypeModels);
        List<InfoCodeConfigQueryConfigValueObject> configFilters = InfoCodeGroupModel.IOT_SETTING_GROUP_NAMES.stream()
                .map(groupCode -> {
                    InfoCodeConfigQueryConfigValueObject configValueObject = new InfoCodeConfigQueryConfigValueObject();
                    configValueObject.setGroupCode(groupCode);
                    return configValueObject;
                }).collect(Collectors.toList());
        List<InfoCodeTypeEnum> infoCodeTypes = Lists.newArrayList(
                InfoCodeTypeEnum.IOT_SETTING, InfoCodeTypeEnum.IOT_RUNNING);
        List<DashboardBatchControlVO> result = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
            futures.add(CompletableFuture.runAsync(() -> {
                InfoCodeInitByObjectParam infoCodeInitByObjectParam =
                        infoCodeConverter.toInitByObjectParam(templateTypeModel);
                infoCodeInitByObjectParam.setInfoCodeTypes(infoCodeTypes);
                infoCodeInitByObjectParam.setFilterGroups(configFilters);
                List<InfoCodeAggregate> aggregates = infoCodeGateway.of(infoCodeInitByObjectParam);
                if (CollectionUtils.isEmpty(aggregates)) {
                    return;
                }
                infoCodeGateway.fillInfoCodeDefineModel(aggregates);
                Map<String, InfoCodeConfigModel> infoCodeConfigAndModel = new HashMap<>();
                Map<String, InfoCodeAggregate> infoCodeAndAggregate = new HashMap<>();
                Map<String, Set<String>> infoCodeAndObjectIds = new HashMap<>();
                Map<String, DtpRwdObjectDTO> objectIdAndData = new HashMap<>();
                Set<String> objectIds = new HashSet<>();
                Set<String> assembleInfoCodeUniqueKeys = new HashSet<>();
                for (InfoCodeAggregate aggregate : aggregates) {
                    DtpRwdObjectDTO object = aggregate.getIdentifier().getObject();
                    InfoCodeConfigModel configModel = aggregate.getConfigModel();
                    List<InfoCodeAssembleModel> assembleModels = aggregate.getAssembleModels();
                    InfoCodeGroupModel groupModel = aggregate.getGroupModel();
                    String infoCode = configModel.getInfoCode();
                    String groupCode = groupModel.getGroupCode();
                    if (!infoCodeConfigAndModel.containsKey(infoCode)) {
                        infoCodeConfigAndModel.put(infoCode, configModel);
                        infoCodeAndAggregate.put(infoCode, aggregate);
                    }
                    if (CollectionUtils.isNotEmpty(assembleModels)) {
                        for (InfoCodeAssembleModel assembleModel : assembleModels) {
                            assembleInfoCodeUniqueKeys.add(assembleModel.getConfigModel().getUniqueKey(groupCode));
                        }
                    }
                    objectIds.add(object.getId());
                    MapExecutor.dynamicPut(infoCodeAndObjectIds, infoCode, object.getId(), HashSet::new);
                }
                List<InfoCodeConfigVO> relInfoCodeConfigs = new ArrayList<>();
                for (InfoCodeConfigModel infoCodeConfig : infoCodeConfigAndModel.values()) {
                    String infoCode = infoCodeConfig.getInfoCode();
                    InfoCodeAggregate aggregate = infoCodeAndAggregate.get(infoCode);
                    if (Objects.isNull(aggregate)) {
                        continue;
                    }
                    InfoCodeGroupModel groupModel = aggregate.getGroupModel();
                    InfoCodeConfigModel configModel = aggregate.getConfigModel();
                    if (assembleInfoCodeUniqueKeys.contains(configModel.getUniqueKey(groupModel.getGroupCode()))) {
                        continue;
                    }
                    relInfoCodeConfigs.add(InfoCodeConfigVO.of(aggregate, aggregates));
                }
                if (CollectionUtils.isNotEmpty(relInfoCodeConfigs)) {
                    relInfoCodeConfigs.sort(Comparator.comparing(InfoCodeConfigVO::getSequence));
                }
                if (CollectionUtils.isNotEmpty(objectIds)) {
                    List<DtpRwdObjectDTO> objects = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                            .setId(objectIds).addWithColumn(DtpRwdObjectDTO.Fields.id,
                                    DtpRwdObjectDTO.Fields.classCode, BaseConvert.INFO_CODE_FLOOR_ID));
                    if (CollectionUtils.isNotEmpty(objects)) {
                        for (DtpRwdObjectDTO object : objects) {
                            objectIdAndData.put(object.getId(), object);
                        }
                    }
                }
                DashboardBatchControlVO resultItem = new DashboardBatchControlVO();
                resultItem.setTemplateTypeId(templateTypeModel.getTemplateTypeId());
                resultItem.setTemplateTypeName(templateTypeModel.getTemplateTypeName());
                resultItem.setSequence(templateTypeModel.getSequence());
                resultItem.setRelInfoCodeConfigs(relInfoCodeConfigs);
                resultItem.setInfoCodeAndObjectIds(infoCodeAndObjectIds);
                resultItem.setObjectIdAndData(objectIdAndData);
                result.add(resultItem);
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        if (CollectionUtils.isNotEmpty(result)) {
            result.sort(Comparator.comparing(DashboardBatchControlVO::getSequence));
        }
        return result;
    }
}
