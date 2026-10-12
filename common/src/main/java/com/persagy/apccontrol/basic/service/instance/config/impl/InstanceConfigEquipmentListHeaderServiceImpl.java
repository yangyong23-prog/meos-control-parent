package com.persagy.apccontrol.basic.service.instance.config.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.equipmentlist.ConfigEquipmentListHeaderDTO;
import com.persagy.apccontrol.basic.model.dto.equipmentlist.InfoCodeDefineModelCfgDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.instance.config.InstanceConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.instance.InstanceConfigTypeEnum;
import com.persagy.apccontrol.basic.model.param.instance.InstanceConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.instance.InstanceConfigSaveParam;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeGroupVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeObjectGroupVO;
import com.persagy.apccontrol.basic.service.instance.config.InstanceConfigService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeConfigModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeGroupModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectDetailModel;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备列表自定义表头配置
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InstanceConfigEquipmentListHeaderServiceImpl implements InstanceConfigEquipmentListHeaderService {


    private final InstanceConfigService instanceConfigService;
    private final ObjectMapper objectMapper;
    private final InfoCodeGateway infoCodeGateway;

    @Override
    public Boolean saveInstanceConfig(ConfigEquipmentListHeaderDTO param) {

        List<InfoCodeDefineModelCfgDTO> cfg = param.getCfgInfoCodeGroupVOList();


        // 保存配置
        InstanceConfigSaveParam saveParam = new InstanceConfigSaveParam();
        saveParam.setConfigType(InstanceConfigTypeEnum.EQUIPMENT_LIST_HEADER);
        saveParam.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        saveParam.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
        saveParam.setRunningMonitorType(param.getRunningMonitorType());
        saveParam.setConfigData(objectMapper.convertValue(cfg, JsonNode.class));
        saveParam.setRelMsg(param.getTemplateTypeKey());
        return instanceConfigService.saveInstanceConfig(saveParam, true);
    }

    @Override
    public ConfigEquipmentListHeaderDTO getInstanceConfig(RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                                          @NonNull RunningMonitorTypeEnum runningMonitorType,
                                                          @NonNull String templateTypeKey) {
        ConfigEquipmentListHeaderDTO result = new ConfigEquipmentListHeaderDTO();

        InstanceConfig cfg = instanceConfigService.getInstanceConfig(InstanceConfigTypeEnum.EQUIPMENT_LIST_HEADER,
                DtpRwdObjectTypeEnum.EQUIPMENT,
                runningMonitorSecondType,
                runningMonitorType, templateTypeKey);

        if (Objects.isNull(cfg)) {
            return result;
        }

        // 数据处理
        List<InfoCodeDefineModelCfgDTO> list = objectMapper.convertValue(cfg.getConfigData(),
                new TypeReference<List<InfoCodeDefineModelCfgDTO>>() {
                });

        if (CollectionUtils.isNotEmpty(list)) {
            result.setRunningMonitorSecondType(cfg.getRunningMonitorSecondType());
            result.setRunningMonitorType(cfg.getRunningMonitorType());
            result.setTemplateTypeKey(cfg.getRelMsg());
            result.setCfgInfoCodeGroupVOList(list);
            result.setConfig(true);
        }
        return result;
    }

    @Override
    public ConfigEquipmentListHeaderDTO listAllConfigAndHeader(InstanceConfigQueryParam param) {
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        String templateTypeKey = param.getTemplateTypeKey();
        // 查询配置
        ConfigEquipmentListHeaderDTO result = getInstanceConfig(runningMonitorSecondType, runningMonitorType, templateTypeKey);
        // 查询全量表头
        List<InfoCodeGroupVO> infoCodeGroupVOList = getInfoCodeGroupVOS(param);
        if (CollectionUtils.isEmpty(infoCodeGroupVOList)) {
            return result;
        }
        result.setInfoCodeGroupVOList(infoCodeGroupVOList);
        return result;
    }

    /**
     * 查询分组信息点列表
     *
     * @param param 查询参数
     * @return 分组信息点列表
     * @since 7.250802.0
     */
    public List<InfoCodeGroupVO> listGroupInfoCodes(@NonNull InfoCodeInitByObjectParam param) {
        List<InfoCodeAggregate> aggregates = infoCodeGateway.of(param);
        if (CollectionUtils.isEmpty(aggregates)) {
            return Collections.emptyList();
        }
        infoCodeGateway.fillInfoCodeDefineModel(aggregates);
        List<InfoCodeObjectGroupVO> objectGroups = InfoCodeObjectGroupVO.of(aggregates);
        if (CollectionUtils.isEmpty(objectGroups)) {
            return Collections.emptyList();
        }
        MultiKeyMap<String, List<InfoCodeRwdObjectDetailDTO>> groupCodeInfoCodeTypeCodeAndRelObjectInfoCodes =
                this.queryGroupCodeInfoCodeTypeCodeAndRelObjectInfoCodes(aggregates);
        Map<String, InfoCodeGroupVO> groupCodeAndModel = new HashMap<>();
        Map<String, Map<String, InfoCodeGroupVO>> groupCodeAndChildren = new HashMap<>();
        Map<String, Map<InfoCodeTypeEnum, Map<String, InfoCodeConfigVO>>> groupCodeInfoCodeTypeCodeAndConfig = new HashMap<>();
        MultiKeyMap<String, List<InfoCodeConfigVO>> groupCodeInfoCodeTypeCodeAndRelConfig = new MultiKeyMap<>();
        Map<String, Set<MultiKey<String>>> groupCodeAndRelChildrenKeys = new HashMap<>();
        for (InfoCodeObjectGroupVO objectGroup : objectGroups) {
            List<InfoCodeGroupVO> relInfoCodeGroupConfigs = objectGroup.getRelInfoCodeGroupConfigs();
            if (CollectionUtils.isEmpty(relInfoCodeGroupConfigs)) {
                continue;
            }
            for (InfoCodeGroupVO group : relInfoCodeGroupConfigs) {
                String groupCode = group.getGroupCode();
                List<InfoCodeGroupVO> groupChildren = group.getGroupChildren();
                List<InfoCodeConfigVO> configs = group.getRelInfoCodeConfigs();
                if (CollectionUtils.isNotEmpty(groupChildren)) {
                    for (InfoCodeGroupVO groupChild : groupChildren) {
                        MapExecutor.dynamicPut(groupCodeAndChildren, groupCode,
                                groupChild.getGroupCode(), groupChild, HashMap::new);
                    }
                }
                Map<InfoCodeTypeEnum, Map<String, InfoCodeConfigVO>> infoCodeTypeCodeAndConfig = Optional.ofNullable(
                        groupCodeInfoCodeTypeCodeAndConfig.get(groupCode)).orElseGet(HashMap::new);
                if (CollectionUtils.isNotEmpty(configs)) {
                    for (InfoCodeConfigVO config : configs) {
                        InfoCodeTypeEnum infoCodeType = config.getInfoCodeType();
                        String infoCode = config.getInfoCode();
                        List<InfoCodeConfigVO> relInfoCodeConfigs = config.getRelInfoCodeConfigs();
                        MapExecutor.dynamicPutAll(groupCodeAndRelChildrenKeys, groupCode,
                                new HashSet<>(config.toRelInfoCodeKeys()), HashSet::new);
                        MapExecutor.dynamicPut(infoCodeTypeCodeAndConfig, infoCodeType, infoCode, config, HashMap::new);
                        if (CollectionUtils.isNotEmpty(relInfoCodeConfigs)) {
                            MultiKey<String> multiKey = new MultiKey<>(groupCode, infoCodeType.getValue(), infoCode);
                            for (InfoCodeConfigVO relInfoCodeConfig : relInfoCodeConfigs) {
                                if (!groupCodeInfoCodeTypeCodeAndRelConfig.containsKey(multiKey)) {
                                    MapExecutor.dynamicPut(groupCodeInfoCodeTypeCodeAndRelConfig,
                                            multiKey, relInfoCodeConfig, ArrayList::new);
                                }
                            }
                        }
                    }
                }
                groupCodeAndModel.putIfAbsent(groupCode, group);
                groupCodeInfoCodeTypeCodeAndConfig.putIfAbsent(groupCode, infoCodeTypeCodeAndConfig);
            }
        }
        // 移除已存在关联信息点, 避免重复
        groupCodeAndRelChildrenKeys.forEach((groupCode, childrenKeys) -> {
            for (MultiKey<String> key : childrenKeys) {
                Map<InfoCodeTypeEnum, Map<String, InfoCodeConfigVO>> infoCodeTypeCodeConfig =
                        groupCodeInfoCodeTypeCodeAndConfig.get(groupCode);
                if (MapUtils.isNotEmpty(infoCodeTypeCodeConfig)) {
                    Map<String, InfoCodeConfigVO> infoCodeConfigs = infoCodeTypeCodeConfig.get(
                            InfoCodeTypeEnum.analyze(key.getKey(0)));
                    if (MapUtils.isNotEmpty(infoCodeConfigs)) {
                        infoCodeConfigs.remove(key.getKey(1));
                    }
                }
            }
        });
        List<InfoCodeGroupVO> groups = new ArrayList<>();
        for (InfoCodeGroupVO group : groupCodeAndModel.values()) {
            String groupCode = group.getGroupCode();
            Map<String, InfoCodeGroupVO> childrenGroupAndData = groupCodeAndChildren.get(groupCode);
            Map<InfoCodeTypeEnum, Map<String, InfoCodeConfigVO>> configsInGroup =
                    groupCodeInfoCodeTypeCodeAndConfig.get(groupCode);

            if (InfoCodeGroupModel.IOT_SETTING_CONDITION_GROUP_NAMES.contains(groupCode)
                    || InfoCodeGroupModel.OBJECT_ADDITIONAL_INFO.contains(groupCode)) {
                continue;
            }
            List<InfoCodeGroupVO> groupChildren = null;
            if (MapUtils.isNotEmpty(childrenGroupAndData)) {
                groupChildren = new ArrayList<>();
                for (InfoCodeGroupVO groupChild : childrenGroupAndData.values()) {
                    if (InfoCodeGroupModel.IOT_SETTING_CONDITION_GROUP_NAMES.contains(groupChild.getGroupCode())
                            || InfoCodeGroupModel.OBJECT_ADDITIONAL_INFO.contains(groupChild.getGroupCode())) {
                        continue;
                    }
                    InfoCodeGroupVO groupVo = new InfoCodeGroupVO();
                    groupVo.setGroupCode(groupCode);
                    groupVo.setGroupName(group.getGroupName());
                    groupVo.setGroupOrder(group.getGroupOrder());
                    groupVo.setRelInfoCodeConfigs(groupChild.getRelInfoCodeConfigs());
                    groupChildren.add(groupVo);
                }
            }
            List<InfoCodeConfigVO> relInfoCodeConfigs = null;
            if (MapUtils.isNotEmpty(configsInGroup)) {
                relInfoCodeConfigs = configsInGroup.values().stream()
                        .flatMap(item -> item.values().stream())
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(relInfoCodeConfigs)) {
                    for (InfoCodeConfigVO config : relInfoCodeConfigs) {
                        InfoCodeTypeEnum infoCodeType = config.getInfoCodeType();
                        String infoCode = config.getInfoCode();
                        MultiKey<String> multiKey = new MultiKey<>(groupCode, infoCodeType.getValue(), infoCode);

                        if (infoCodeType == InfoCodeTypeEnum.RWD_OBJECT_DETAIL) {
                            List<InfoCodeRwdObjectDetailDTO> relObjectInfoCodeValues =
                                    groupCodeInfoCodeTypeCodeAndRelObjectInfoCodes.get(multiKey);
                            if (CollectionUtils.isNotEmpty(relObjectInfoCodeValues)) {
                                List<InfoCodeConfigVO> relObjectInfoCodes = new ArrayList<>();
                                for (InfoCodeRwdObjectDetailDTO relObjectInfoCodeValue : relObjectInfoCodeValues) {
                                    List<InfoCodeGroupVO> relInfoCodeGroupConfigs =
                                            relObjectInfoCodeValue.getRelInfoCodeGroupConfigs();
                                    if (CollectionUtils.isEmpty(relInfoCodeGroupConfigs)) {
                                        continue;
                                    }
                                    relObjectInfoCodes.addAll(relInfoCodeGroupConfigs.stream()
                                            .map(InfoCodeGroupVO::getRelInfoCodeConfigs)
                                            .filter(CollectionUtils::isNotEmpty)
                                            .flatMap(Collection::stream)
                                            .peek(InfoCodeConfigVO::onlyRetainInfoCode)
                                            .collect(Collectors.toList())
                                    );
                                }
                                config.setRelInfoCodeConfigs(relObjectInfoCodes);
                            }
                        }
                        if (MapUtils.isNotEmpty(groupCodeInfoCodeTypeCodeAndRelConfig)) {
                            List<InfoCodeConfigVO> childrenConfigs = groupCodeInfoCodeTypeCodeAndRelConfig.get(multiKey);
                            if (CollectionUtils.isNotEmpty(childrenConfigs)) {
                                childrenConfigs.sort(Comparator.comparing(InfoCodeConfigVO::getSequence));
                                config.setRelInfoCodeConfigs(childrenConfigs);
                            }
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(relInfoCodeConfigs)) {
                relInfoCodeConfigs.sort(Comparator.comparing(InfoCodeConfigVO::getSequence));
                InfoCodeGroupVO groupVo = new InfoCodeGroupVO();
                groupVo.setGroupChildren(groupChildren);
                groupVo.setGroupCode(groupCode);
                groupVo.setGroupName(group.getGroupName());
                groupVo.setGroupOrder(group.getGroupOrder());
                groupVo.setRelInfoCodeConfigs(relInfoCodeConfigs);
                groups.add(groupVo);
            }
        }
        groups.sort(Comparator.comparing(InfoCodeGroupVO::getGroupOrder));
        return groups;
    }

    /**
     * 查询关联对象的关系信息点配置
     *
     * @param aggregates 聚合根
     * @return 关联对象关系信息点配置
     * @since 7.250802.0
     */
    private MultiKeyMap<String, List<InfoCodeRwdObjectDetailDTO>> queryGroupCodeInfoCodeTypeCodeAndRelObjectInfoCodes(
            @Nullable List<InfoCodeAggregate> aggregates) {
        MultiKeyMap<String, List<InfoCodeRwdObjectDetailDTO>> groupCodeInfoCodeTypeCodeAndRelObjectInfoCodes = new MultiKeyMap<>();
        if (CollectionUtils.isEmpty(aggregates)) {
            return groupCodeInfoCodeTypeCodeAndRelObjectInfoCodes;
        }
        List<InfoCodeAggregate> rwdObjectDetailAggregates = aggregates.stream()
                .filter(item -> {
                    InfoCodeConfigModel configModel = item.getConfigModel();
                    if (configModel.getInfoCodeType() != InfoCodeTypeEnum.RWD_OBJECT_DETAIL) {
                        return false;
                    }
                    InfoCodeTakeValueLogicRwdObjectDetailModel tackValueLogic = configModel
                            .analyzeTackValueLogicModel(InfoCodeTakeValueLogicRwdObjectDetailModel.class);
                    return Objects.nonNull(tackValueLogic) && Objects.nonNull(tackValueLogic.getQueryInfoCodeConfig());
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(rwdObjectDetailAggregates)) {
            infoCodeGateway.fillInfoCodeDataModel(rwdObjectDetailAggregates, null);
            for (InfoCodeAggregate aggregate : rwdObjectDetailAggregates) {
                InfoCodeGroupModel groupModel = aggregate.getGroupModel();
                InfoCodeConfigModel configModel = aggregate.getConfigModel();
                InfoCodeDataModel infoCodeAndDataModel = aggregate.getInfoCodeAndDataModel();

                // noinspection unchecked
                List<InfoCodeRwdObjectDetailDTO> davaValues = (List<InfoCodeRwdObjectDetailDTO>)
                        infoCodeAndDataModel.getDataValue();
                if (CollectionUtils.isEmpty(davaValues)) {
                    continue;
                }
                MultiKey<String> multiKey = new MultiKey<>(groupModel.getGroupCode(),
                        configModel.getInfoCodeType().getValue(),
                        configModel.getInfoCode());
                groupCodeInfoCodeTypeCodeAndRelObjectInfoCodes.putIfAbsent(multiKey, davaValues);
            }
        }
        return groupCodeInfoCodeTypeCodeAndRelObjectInfoCodes;
    }

    /**
     * 查询全量表头
     *
     * @param param 查询参数
     * @return 全量表头
     */
    private List<InfoCodeGroupVO> getInfoCodeGroupVOS(InstanceConfigQueryParam param) {
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        Long templateTypeId = param.getTemplateTypeId();
        Set<String> serviceSpaceIds = param.getServiceSpaceIds();
        Set<String> inSpaceIds = param.getInSpaceIds();

        InfoCodeObjectQueryParam objectQueryParam = new InfoCodeObjectQueryParam();
        objectQueryParam.setTemplateTypeId(templateTypeId);
        objectQueryParam.setServiceSpaceIds(serviceSpaceIds);
        objectQueryParam.setInSpaceIds(inSpaceIds);
        List<DtpRwdObjectDTO> objects = infoCodeGateway.listTemplateTypeObjects(objectQueryParam);
        InfoCodeInitByObjectParam infoCodeInitByObjectParam = new InfoCodeInitByObjectParam();
        infoCodeInitByObjectParam.setObjects(objects);
        infoCodeInitByObjectParam.setRunningMonitorType(runningMonitorType);
        infoCodeInitByObjectParam.setRunningMonitorSecondType(runningMonitorSecondType);
        infoCodeInitByObjectParam.setTemplateTypeIds(Sets.newHashSet(templateTypeId));
        infoCodeInitByObjectParam.setTemplateTypes(Lists.newArrayList(InfoCodeTemplateTypeEnum.TABLE_LIST));
        infoCodeInitByObjectParam.setFilterCustomGroupInfoCodeConfig(false);
        return this.listGroupInfoCodes(infoCodeInitByObjectParam);
    }
}
