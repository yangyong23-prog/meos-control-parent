package com.persagy.apccontrol.common.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.constant.LocalCacheConstant;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.DirectRelationDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdCriteriaSignEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigObjectByFiltersQueryParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeGroupVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeObjectGroupVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.InteractiveConfigVo;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirTerminalStrategy;
import com.persagy.apccontrol.basic.utils.systemgraph.ObjTypeUtil;
import com.persagy.apccontrol.common.model.param.client.EquipmentInfoCodeConfigByObjectQueryClientParam;
import com.persagy.apccontrol.common.model.vo.SystemRelationVO;
import com.persagy.apccontrol.common.opt.OptSystemObjectsPrefetch;
import com.persagy.apccontrol.common.model.vo.client.*;
import com.persagy.apccontrol.common.server.IotDataCacheServerEdge;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.util.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CS端
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/7 16:49
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ClientServerHandler {

    private final BizDtpRwdService bizDtpRwdService;
    private final EquipRecordService equipRecordService;
    private final IotDataCacheServerEdge iotDataCacheServerEdge;
    private final LocalRedisCacheHandler localRedisCacheHandler;
    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractCommonServer abstractCommonServer;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final InfoCodeConfigService infoCodeConfigService;
    private final SystemGraphService systemGraphService;
    private static final List<String> FUNCTION_CODES = Arrays.asList("SFT-02.07.01", "SFT-02.07.02", "SFT-02.07.03", "SFT-02.07.04", "SFT-02.07.05", "SFT-02.02.14");
    private final InstanceService instanceService;
    private final Executor executorService;
    private final RunningMonitorAirTerminalStrategy runningMonitorAirTerminalStrategy;

    public List<SystemRelationVO> querySystemRelationObjects(String systemGraphId, DtpRwdClassCodeEnum systemClassCode, String projectId) {
        String groupCode = ContextUtil.getGroupCode();

        List<SystemRelationVO> resultList = new ArrayList<>();
        // 1. 优先获取所有的空间信息
        List<JSONObject> spaceFloorList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.FLOOR.getValue(), DtpRwdClassCodeEnum.SPACE.getValue()), null, null, null, null);
        Map<String, JSONObject> spaceInfo = CollectionUtil.isEmpty(spaceFloorList) ? new HashMap<>()
                : spaceFloorList.stream().filter(item -> "space".equals(item.getString("objType"))).collect(Collectors.toMap(key -> key.getString("id"), Function.identity(), (v1, v2) -> v1));
        Map<String, JSONObject> floorInfo = CollectionUtil.isEmpty(spaceFloorList) ? new HashMap<>()
                : spaceFloorList.stream().filter(item -> "floor".equals(item.getString("objType"))).collect(Collectors.toMap(key -> key.getString("id"), Function.identity(), (v1, v2) -> v1));
        List<SystemRelationVO> vo = this.querySystemRelationBySystemClassCode(groupCode, projectId, systemClassCode, spaceInfo, floorInfo);
        resultList.addAll(vo);
        return resultList;
    }

    public List<SystemRelationVO> querySystemRelationByFunctionCode(String projectId, List<String> funcCodes) {
        SystemRelationVO result = new SystemRelationVO();
        result.setEquipments(Lists.newArrayList());
        Collection<Object> codes = funcCodes.stream().filter(FUNCTION_CODES::contains).collect(Collectors.toCollection(LinkedHashSet::new));
        if (CollectionUtil.isEmpty(codes)) {
            result.setSpaces(Lists.newArrayList());
            return Collections.singletonList(result);
        }
        List<DtpRwdFloorDTO> floorList = this.dtpRwdClientService.listFloor(false, false, projectId, null);
        Map<String, DtpRwdFloorDTO> floorInfo = CollectionUtil.isEmpty(floorList) ? new HashMap<>()
                : floorList.stream().collect(Collectors.toMap(DtpRwdFloorDTO::getId, Function.identity(), (v1, v2) -> v1));
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addInfoCode("roomFuncType", codes);
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = dtpRwdClientService.listSpace(true, false, projectId, queryParam);
        List<String> buildingIds = floorList.parallelStream().map(DtpRwdFloorDTO::getBuildingId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        DtpRwdObjectQueryParam bdParam = new DtpRwdObjectQueryParam().setId(buildingIds);
        List<DtpRwdBuildingDTO> dtpRwdBuildings = dtpRwdClientService.listBuilding(true, false, projectId, bdParam);
        Map<String, String> buildingNameMap = dtpRwdBuildings.parallelStream().filter(e -> StringUtils.isNotBlank(e.getName())).collect(Collectors.toMap(DtpRwdBuildingDTO::getId, DtpRwdBuildingDTO::getName, (v1, v2) -> v2));
        Map<String, String> buildingLocalNameMap = dtpRwdBuildings.parallelStream().filter(e -> StringUtils.isNotBlank(e.getLocalName())).collect(Collectors.toMap(DtpRwdBuildingDTO::getId, DtpRwdBuildingDTO::getLocalName, (v1, v2) -> v2));
        List<BaseNamePropertyDTO> spaces = dtpRwdSpaceDTOS.stream().map(e -> {
            BaseNamePropertyDTO dto = new BaseNamePropertyDTO();
            BeanUtils.copyProperties(e, dto);
            String buildingId = e.getBuildingId();
            DtpRwdFloorDTO dtpRwdFloorDTO = floorInfo.get(e.getFloorId());
            dto.setFloorLocalName(dtpRwdFloorDTO.getLocalName());
            dto.setFloorLocalId(dtpRwdFloorDTO.getLocalId());
            dto.setFloorSequenceID(dtpRwdFloorDTO.getFloorSequenceId());
            dto.setBuildArea(Double.parseDouble(e.getUndefinedProperties().getOrDefault("buildArea", "0").toString()));
            dto.setGroupCode(ContextUtil.getGroupCode());
            if (StringUtils.isNotBlank(buildingId)) {
                dto.setBuildingName(buildingNameMap.get(buildingId));
                dto.setBuildingLocalName(buildingLocalNameMap.get(buildingId));
            }
            return dto;
        }).collect(Collectors.toList());
        result.setSpaces(spaces);
        return Collections.singletonList(result);
    }


    private List<SystemRelationVO> querySystemRelationBySystemClassCode(String groupCode, String projectId, DtpRwdClassCodeEnum systemClassCode,
                                                                        Map<String, JSONObject> spaceInfo, Map<String, JSONObject> floorInfo) {
        //查询建筑名称
        List<String> buildingIds = floorInfo.values().parallelStream().map(e -> e.getString("buildingId")).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        DtpRwdObjectQueryParam bdParam = new DtpRwdObjectQueryParam().setId(buildingIds);
        List<DtpRwdBuildingDTO> dtpRwdBuildings = dtpRwdClientService.listBuilding(true, false, projectId, bdParam);
        Map<String, String> buildingNameMap = dtpRwdBuildings.stream().filter(e -> StringUtils.isNotBlank(e.getName())).collect(Collectors.toMap(DtpRwdBuildingDTO::getId, DtpRwdBuildingDTO::getName, (v1, v2) -> v2));
        Map<String, String> buildingLocalNameMap = dtpRwdBuildings.stream().filter(e -> StringUtils.isNotBlank(e.getLocalName()))
                .collect(Collectors.toMap(DtpRwdBuildingDTO::getId, DtpRwdBuildingDTO::getLocalName, (v1, v2) -> v2));
        floorInfo.values().parallelStream().forEach(e -> {
            String buildingId = e.getString("buildingId");
            if (StringUtils.isNotBlank(buildingId)) {
                String buildingName = buildingNameMap.get(buildingId);
                String buildingLocalName = buildingLocalNameMap.get(buildingId);
                e.put("buildingName", buildingName);
                e.put("buildingLocalName", buildingLocalName);
            }
        });

        //根据系统，获取对应的设备
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(systemClassCode);
        List<DtpRwdObjectDTO> systemList = dtpRwdClientService.listObject(true, false, projectId, queryParam);
        List<CompletableFuture<SystemRelationVO>> allTaskList = new ArrayList<>();
        Set<String> systemIds = systemList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        DtpRwdObjectRelationQueryParam relationTo = new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQ_IN_SYSTEM);
        relationTo.setObjFrom(systemIds);
        param.setRelationTo(relationTo);
        List<DtpRwdObjectDTO> allDtpRwdObjectDTOS = dtpRwdClientService.listObject(false, false, projectId, param);
        Map<String, List<DtpRwdObjectDTO>> mapDtpRwdObjectDTOs = new HashMap<>();
        allDtpRwdObjectDTOS.forEach(e -> {
            Set<String> relObjIds = e.getRelObjIds();
            if (CollectionUtil.isEmpty(relObjIds)) {
                return;
            }
            for (String relObjId : relObjIds) {
                List<DtpRwdObjectDTO> dtpRwdObjectDTOS = mapDtpRwdObjectDTOs.get(relObjId);
                if (CollectionUtil.isEmpty(dtpRwdObjectDTOS)) {
                    dtpRwdObjectDTOS = new ArrayList<>();
                    mapDtpRwdObjectDTOs.put(relObjId, dtpRwdObjectDTOS);
                }
                dtpRwdObjectDTOS.add(e);
            }
        });
        Set<String> classCodeList = allDtpRwdObjectDTOS.parallelStream().distinct().map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toSet());
        Map<String, List<RcInfoCodeCfg>> infoCodeCfgMap = new HashMap<>();
        for (String equipmentCode : classCodeList) {
            infoCodeCfgMap.put(equipmentCode, this.localRedisCacheHandler.getInfoCodeConfigs(equipmentCode, true, false));
        }
        // 获取所有的绑点信息
        Map<String, Set<String>> mapObjectInfoCodeRules = this.abstractCommonServer.queryObjectsExistRuleMap(groupCode, projectId, classCodeList);
        // opt B10（switch.opt.clientSystemRelationBatch）：各系统的空间一次查完，按系统拆分
        OptSystemObjectsPrefetch prefetch = prefetchSystemObjects(groupCode, projectId, systemIds, null);
        for (DtpRwdObjectDTO system : systemList) {
            CompletableFuture<SystemRelationVO> future = CompletableFuture.supplyAsync(() -> {
                String systemId = system.getId();
                SystemRelationVO systemInfo = new SystemRelationVO();
                BeanUtils.copyProperties(system, systemInfo);
                List<DtpRwdObjectDTO> dtpRwdObjectDTOS = mapDtpRwdObjectDTOs.get(systemId);
                List<JSONObject> allObjects = dtpRwdObjectDTOS.parallelStream().map(e -> {
                    JSONObject eq = JSONObject.parseObject(JSONObject.toJSONString(e));
                    eq.putAll(e.getUndefinedProperties());
                    return eq;
                }).collect(Collectors.toList());
                List<JSONObject> equipments = this.convertEquipmentInfoBySystemClassCode(groupCode, projectId, classCodeList, spaceInfo, floorInfo,
                        allObjects, mapObjectInfoCodeRules, infoCodeCfgMap);
                systemInfo.setEquipments(equipments);

                // 获取所有的空间
                List<JSONObject> allSpaces = prefetch != null && prefetch.spaces(systemId) != null ? prefetch.spaces(systemId)
                        : this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.SPACE.getValue()), "ArchforMech", "Sp2Sy", null, Lists.newArrayList(systemId));
                systemInfo.setSpaces(this.fillSpaceFloorInfoBySystemClassCode(allSpaces, spaceInfo, floorInfo));
                return systemInfo;
            }, executorService);
            allTaskList.add(future);
        }

        List<SystemRelationVO> resultList = allTaskList.stream().map(CompletableFuture::join).collect(Collectors.toList());
        return resultList;
    }


    /** 系统下的管网类编码（原在 querySystemRelationObjectsByAccc 循环内每次新建，内容不变） */
    private static final Set<String> MESH_CODES = Collections.unmodifiableSet(
            Sets.newHashSet("ACPPCE", "ACPPCP", "ACPPCR", "ACPPCS", "ACPPOP", "ACPPOR", "ACPPOS", "ACPPWC"));

    /** opt B10：开关关闭或系统数 ≤1 时返回 null，调用方按原逻辑逐个系统查询 */
    @Nullable
    private OptSystemObjectsPrefetch prefetchSystemObjects(String groupCode, String projectId, Collection<String> systemIds,
                                                           @Nullable Set<String> meshCodes) {
        if (!OptSwitch.on(OptSwitch.CLIENT_SYSTEM_RELATION_BATCH) || systemIds == null || systemIds.size() <= 1) {
            return null;
        }
        OptSystemObjectsPrefetch prefetch = OptSystemObjectsPrefetch.load(
                (classCodes, graph, rel, objFrom, objTo) -> this.bizDtpRwdService.queryObjectList(groupCode, projectId, classCodes, graph, rel, objFrom, objTo),
                systemIds, DtpRwdClassCodeEnum.SPACE.getValue(), meshCodes, "ACPPCS");
        if (prefetch.failure() != null) {
            log.warn("系统关联对象批量查询失败，退回按系统逐个查询", prefetch.failure());
        }
        return prefetch;
    }

    public List<JSONObject> querySystemRelationObjectsByAccc(String groupCode, String projectId, String systemClassCode) {
        // 1. 优先获取所有的空间信息
        List<JSONObject> spaceFloorList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.FLOOR.getValue(), DtpRwdClassCodeEnum.SPACE.getValue()), null, null, null, null);
        Map<String, JSONObject> space_info = CollectionUtil.isEmpty(spaceFloorList) ? new HashMap<>()
                : spaceFloorList.stream().filter(item -> "space".equals(item.getString("objType"))).collect(Collectors.toMap(key -> key.getString("id"), Function.identity(), (v1, v2) -> v1));
        Map<String, JSONObject> floor_info = CollectionUtil.isEmpty(spaceFloorList) ? new HashMap<>()
                : spaceFloorList.stream().filter(item -> "floor".equals(item.getString("objType"))).collect(Collectors.toMap(key -> key.getString("id"), Function.identity(), (v1, v2) -> v1));
        //查询建筑名称
        List<String> buildingIds = floor_info.values().parallelStream().map(e -> e.getString("buildingId")).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        DtpRwdObjectQueryParam bdParam = new DtpRwdObjectQueryParam().setId(buildingIds);
        List<DtpRwdBuildingDTO> dtpRwdBuildings = dtpRwdClientService.listBuilding(true, false, projectId, bdParam);
        Map<String, String> buildingNameMap = dtpRwdBuildings.stream().filter(e -> StringUtils.isNotBlank(e.getName())).collect(Collectors.toMap(DtpRwdBuildingDTO::getId, DtpRwdBuildingDTO::getName, (v1, v2) -> v2));
        Map<String, String> buildingLocalNameMap = dtpRwdBuildings.stream().filter(e -> StringUtils.isNotBlank(e.getLocalName()))
                .collect(Collectors.toMap(DtpRwdBuildingDTO::getId, DtpRwdBuildingDTO::getLocalName, (v1, v2) -> v2));
        floor_info.values().parallelStream().forEach(e -> {
            String buildingId = e.getString("buildingId");
            if (StringUtils.isNotBlank(buildingId)) {
                String buildingName = buildingNameMap.get(buildingId);
                String buildingLocalName = buildingLocalNameMap.get(buildingId);
                e.put("buildingName", buildingName);
                e.put("buildingLocalName", buildingLocalName);
            }
        });

        // 2.根据系统，获取对应的设备、管网、竖井、空间
        // 获取该系统下所有的设备
        Collection<BasePropertyDTO> values = LocalCacheConstant.SYSTEM_INFO.values();
        if (CollectionUtil.isEmpty(values)) {
            this.localRedisCacheHandler.resetSystemCache(groupCode, projectId);
            values = LocalCacheConstant.SYSTEM_INFO.values();
        }
        List<String> systemIds = values.stream().map(BasePropertyDTO::getId).collect(Collectors.toList());
        Set<String> equipCodes = Sets.newHashSet("ACCCCC", "ACCCCD", "ACCCCP", "ACCCOP", "ACCCSC", "ACCCWT", "ACGECL", "ACGEDB", "ACGEET", "ACGEFP",
                "ACGEHE", "ACVVEO", "ACVVNJ", "WESSFL", "WESSHS", "WESSPM", "WESSTP", "ACVVEJ", "ACVVNO", "ACVVNJ");
        List<JSONObject> allEquipObjectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(equipCodes), "MechSubset", "Sy2Eq", systemIds, null);
        List<CompletableFuture<JSONObject>> futureList = new ArrayList<>();
        Map<String, List<JSONObject>> mapDtpRwdObjectDTOs = new HashMap<>();
        allEquipObjectList.forEach(e -> {
            JSONArray relObjIds = e.getJSONArray("relObjIds");
            if (CollectionUtil.isEmpty(relObjIds)) {
                return;
            }
            for (Object obj : relObjIds) {
                String relObjId = obj.toString();
                List<JSONObject> dtpRwdObjectDTOS = mapDtpRwdObjectDTOs.get(relObjId);
                if (CollectionUtil.isEmpty(dtpRwdObjectDTOS)) {
                    dtpRwdObjectDTOS = new ArrayList<>();
                    mapDtpRwdObjectDTOs.put(relObjId, dtpRwdObjectDTOS);
                }
                dtpRwdObjectDTOS.add(e);
            }
        });
        Set<String> classCodeList = allEquipObjectList.parallelStream().distinct().map(e -> e.getString("classCode")).collect(Collectors.toSet());
        // 获取所有的绑点信息
        Map<String, List<RcInfoCodeCfg>> infoCodeCfgMap = new HashMap<>();
        for (String equipmentCode : classCodeList) {
            infoCodeCfgMap.put(equipmentCode, this.localRedisCacheHandler.getInfoCodeConfigs(equipmentCode, true, false));
        }
        Map<String, Set<String>> mapObjectInfoCodeRules = this.abstractCommonServer.queryObjectsExistRuleMap(groupCode, projectId, classCodeList);
        // opt B10（switch.opt.clientSystemRelationBatch）：各系统的管网、竖井、空间各查一次，按系统拆分
        OptSystemObjectsPrefetch prefetch = prefetchSystemObjects(groupCode, projectId, values.stream()
                .filter(sys -> StrUtil.isBlank(systemClassCode) || sys.getClassCode().equals(systemClassCode))
                .map(BasePropertyDTO::getId).collect(Collectors.toList()), MESH_CODES);
        for (BasePropertyDTO system : values) {
            String systemId = system.getId();
            String classCode = system.getClassCode();
            if (StrUtil.isNotBlank(systemClassCode) && !classCode.equals(systemClassCode)) {
                continue;
            }

            JSONObject systemInfo = JSON.parseObject(JSONObject.toJSONString(system));

            CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(() -> {

                List<JSONObject> allEquipObjects = mapDtpRwdObjectDTOs.get(systemId);
                systemInfo.put("equipments", this.convertEquipmentInfoBySystemClassCode(groupCode, projectId, equipCodes, space_info, floor_info, allEquipObjects, mapObjectInfoCodeRules, infoCodeCfgMap));

                // 获取该系统下所有的管网
                Set<String> mesheCodes = MESH_CODES;
                List<JSONObject> allMeshes = prefetch != null && prefetch.meshes(systemId) != null ? prefetch.meshes(systemId)
                        : CollectionUtil.isEmpty(mesheCodes) ? null : this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(mesheCodes), "MechSubset", "Sy2Eq", Lists.newArrayList(systemId), null);
                systemInfo.put("meshes", this.fillSpaceFloorInfoBySystemClassCode(allMeshes, space_info, floor_info));

                // 根据冷冻水供水管，获取所有的竖井
                Set<String> allACPPCSIds = CollectionUtil.isEmpty(allMeshes) ? Sets.newHashSet()
                        : allMeshes.stream().filter(item -> "ACPPCS".equals(item.getString("classCode"))).map(item -> item.getString("id")).collect(Collectors.toSet());

                List<JSONObject> allShafts = CollectionUtil.isEmpty(allACPPCSIds) ? null
                        : prefetch != null && prefetch.shafts(systemId) != null ? prefetch.shafts(systemId) : this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("shaft"), "MechInArch", "Eq2Sh", Lists.newArrayList(allACPPCSIds), null);
                systemInfo.put("shafts", CollectionUtil.isEmpty(allShafts) ? Lists.newArrayList()
                        : JsonUtils.fromJson(JsonUtils.toJson(allShafts), new TypeReference<List<BaseShaftPropertyDTO>>() {
                }));

                // 获取所有的空间
                List<JSONObject> allSpaces = prefetch != null && prefetch.spaces(systemId) != null ? prefetch.spaces(systemId)
                        : this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.SPACE.getValue()), "ArchforMech", "Sp2Sy", null, Lists.newArrayList(systemId));
                systemInfo.put("spaces", this.fillSpaceFloorInfoBySystemClassCode(allSpaces, space_info, floor_info));
                return systemInfo;
            }, executorService);

            futureList.add(future);
        }

        List<JSONObject> resultList = new ArrayList<>();
        for (CompletableFuture<JSONObject> completableFuture : futureList) {
            try {
                JSONObject jsonObject = completableFuture.get();
                if (jsonObject != null) {
                    resultList.add(jsonObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }

    /**
     * 填充空间、楼层信息
     *
     * @param objectList 对象列表
     * @param space_info 空间信息
     * @param floor_info 楼层信息
     * @return {@link List}<{@link BaseNamePropertyDTO}>
     */
    private List<BaseNamePropertyDTO> fillSpaceFloorInfoBySystemClassCode(List<JSONObject> objectList, Map<String, JSONObject> space_info, Map<String, JSONObject> floor_info) {
        List<BaseNamePropertyDTO> objects = CollectionUtil.isEmpty(objectList) ? Lists.newArrayList() : JsonUtils.fromJson(JsonUtils.toJson(objectList), new TypeReference<List<BaseNamePropertyDTO>>() {
        });
        for (BaseNamePropertyDTO object : objects) {
            if (StrUtil.isNotBlank(object.getSpaceId()) && space_info.containsKey(object.getSpaceId())) {
                object.setSpaceLocalId(space_info.get(object.getSpaceId()).getString("localId"));
                object.setSpaceLocalName(StrUtil.isBlank(space_info.get(object.getSpaceId()).getString("localName")) ? object.getSpaceLocalId()
                        : space_info.get(object.getSpaceId()).getString("localName"));
            }
            if (StrUtil.isNotBlank(object.getFloorId()) && floor_info.containsKey(object.getFloorId())) {
                object.setFloorLocalId(floor_info.get(object.getFloorId()).getString("localId"));
                object.setFloorLocalName(floor_info.get(object.getFloorId()).getString("localName"));
                object.setFloorSequenceID(floor_info.get(object.getFloorId()).getInteger("floorSequenceID"));
                object.setBuildingName(floor_info.get(object.getFloorId()).getString("buildingName"));
                object.setBuildingLocalName(floor_info.get(object.getFloorId()).getString("buildingLocalName"));
            }
        }
        return objects;
    }

    /**
     * 获取设备信息，填充空间ID与楼层ID，并设置运行状态信息
     *
     * @param groupCode       集团编码
     * @param projectId       项目id
     * @param equipmentCodes  设备代码
     * @param space_info      空间信息
     * @param floor_info      楼层信息
     * @param allEquipObjects 所有装备对象
     * @return {@link List}<{@link JSONObject}>
     */
    private List<JSONObject> convertEquipmentInfoBySystemClassCode(String groupCode, String projectId, Collection<String> equipmentCodes,
                                                                   Map<String, JSONObject> space_info, Map<String, JSONObject> floor_info,
                                                                   List<JSONObject> allEquipObjects, Map<String, Set<String>> object_infoCode,
                                                                   Map<String, List<RcInfoCodeCfg>> infoCodeCfgMap) {
        List<JSONObject> resultList = new ArrayList<>();
        if (CollectionUtil.isEmpty(allEquipObjects)) {
            return resultList;
        }
        // 获取设备与空间的关系
        List<String> equipmentIds = allEquipObjects.stream().map(item -> item.getString("id")).distinct().collect(Collectors.toList());
        List<JSONObject> jsonObjects = this.bizDtpRwdService.queryRelation(groupCode, projectId, "MechInArch", "Eq2Sp", DtpRwdClassCodeEnum.SPACE.getValue(), equipmentIds, null);
        Map<String, String> object_space = CollectionUtil.isEmpty(jsonObjects) ? new HashMap<>() : jsonObjects.stream().collect(Collectors.toMap(key -> key.getString("objFrom"), value -> value.getString("objTo"), (v1, v2) -> v1));
//        List<InteractiveConfigVo> interactiveConfigs = commonConfigService.getInteractiveConfig();
        // 获取异常事件及其IOT数据
        Map<String, String> classNameMap = dtpKnowledgeClientService.mapClassCodeAndNameByType(true, false, DtpRwdObjectTypeEnum.EQUIPMENT);
        for (JSONObject equipObject : allEquipObjects) {
            JSONObject equipment = new JSONObject();
            String id = equipObject.getString("id");
            String objType = equipObject.getString("objType");
            String classCode = equipObject.getString("classCode");
            String localId = equipObject.getString("localId");
            String localName = equipObject.getString("localName");
            String name = equipObject.getString("name");
            String floorId = equipObject.getString("floorId");

            equipment.put("id", id);
            equipment.put("objType", objType);
            equipment.put("classCode", classCode);
            equipment.put("className", classNameMap.get(classCode));
            equipment.put("localId", localId);
            equipment.put("localName", StrUtil.isBlank(localName) ? localId : localName);
            equipment.put("name", name);
            equipment.put("floorId", floorId);


            //查询是否存在系统图
//            InteractiveConfigVo interactiveConfigVo = buildInteractiveConfig(classCode, id, interactiveConfigs);
            InteractiveConfigVo interactiveConfigVo = null;
            if (interactiveConfigVo != null) {
                equipment.put("hasSystemGraph", interactiveConfigVo.isCanJump());
                equipment.put("interactiveConfigVo", interactiveConfigVo);
            } else {
                equipment.put("hasSystemGraph", false);
            }
            if (StrUtil.isNotBlank(floorId) && floor_info.containsKey(floorId)) {
                equipment.put("floorLocalId", floor_info.get(floorId).getString("localId"));
                equipment.put("floorLocalName", floor_info.get(floorId).getString("localName"));
                equipment.put("buildingId", floor_info.get(floorId).getString("buildingId"));
                equipment.put("floorSequenceID", floor_info.get(floorId).getInteger("floorSequenceID"));
                equipment.put("buildingName", floor_info.get(floorId).getString("buildingName"));
                equipment.put("buildingLocalName", floor_info.get(floorId).getString("buildingLocalName"));
            }
            resultList.add(equipment);

            // 设置空间与楼层ID
            String spaceId = object_space.get(id);
            if (StrUtil.isNotBlank(spaceId)) {
                equipment.put("spaceId", spaceId);
                if (space_info.get(spaceId) != null) {
                    String spaceLocalId = space_info.get(spaceId).getString("localId");
                    String spaceLocalName = space_info.get(spaceId).getString("localName");
                    equipment.put("spaceLocalId", spaceLocalId);
                    equipment.put("spaceLocalName", StrUtil.isBlank(spaceLocalName) ? spaceLocalId : spaceLocalName);
                }
            }

            // 设置运行信息点的状态值
            Set<String> infoCodes = object_infoCode.get(id);
            List<RcInfoCodeCfg> rcInfoCodeCfgs = infoCodeCfgMap.get(classCode);
            List<RcInfoCodeCfg> resultCfgs = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(rcInfoCodeCfgs) && CollectionUtil.isNotEmpty(infoCodes)) {
                List<FeedbackInfoMsgMqttVO> feedbackInfoMsgMqttVOS = this.iotDataCacheServerEdge.queryIotRealDataList(groupCode, projectId, id, rcInfoCodeCfgs);
                Map<String, String> infoValueMap = CollectionUtil.isEmpty(feedbackInfoMsgMqttVOS) ? new HashMap<>()
                        : feedbackInfoMsgMqttVOS.stream().collect(Collectors.toMap(FeedbackInfoMsgMqttVO::getInfoCode, FeedbackInfoMsgMqttVO::getDataValue, (v1, v2) -> v2));
                for (RcInfoCodeCfg infoCodeCfg : rcInfoCodeCfgs) {
                    if (infoCodes.contains(infoCodeCfg.getInfoCode())) {
                        RcInfoCodeCfg rc = new RcInfoCodeCfg();
                        rc.setDataSource(infoCodeCfg.getDataSource());
                        rc.setInfoCode(infoCodeCfg.getInfoCode());
                        rc.setName(infoCodeCfg.getName());
                        rc.setUnit(infoCodeCfg.getUnit());
                        rc.setDataValue(infoValueMap.get(infoCodeCfg.getInfoCode()));
                        if (InfoCodeEnum.MANUAL_AUTO_STATUS.getCode().equals(infoCodeCfg.getInfoCode()) && "0".equals(rc.getDataValue())) {
                            // 手动设备，返回对应的到期时间
                            EquipRecord equipRecord = this.equipRecordService.getEquipRecordByInfoCodeDesc(groupCode, projectId, null, id, InfoCodeEnum.MANUAL_AUTO_SET.getCode(), -1);
                            String expireTime = equipRecord == null ? null : equipRecord.getChangeContent() == null ? null : equipRecord.getChangeContent().getExecTime();
                            rc.setExpireTime(expireTime);
                        }
                        resultCfgs.add(rc);
                    }
                }
            }
            equipment.put("infoCodes", resultCfgs);
        }
        return resultList;
    }

    private InteractiveConfigVo buildInteractiveConfig(String classCode, String objId, List<InteractiveConfigVo> interactiveConfig) {
        if (StringUtils.isBlank(classCode)) {
            return null;
        }
        Optional<InteractiveConfigVo> first = interactiveConfig.stream().filter(it -> it.getInteractiveObjectCode().equals(classCode)).findFirst();
        if (first.isPresent()) {
            InteractiveConfigVo interactiveConfigVo = new InteractiveConfigVo();
            BeanUtils.copyProperties(first.get(), interactiveConfigVo);
            String relation = interactiveConfigVo.getRelation();
            //直接跳转
            if ("direct".equalsIgnoreCase(relation)) {
                generateInteractiveConfig(objId, classCode, interactiveConfigVo);
                if (interactiveConfigVo.isCanJump()) {
                    return interactiveConfigVo;
                }
                return null;
            }
            if ("any".equalsIgnoreCase(relation)) {
                return null;
            }
            //按系统关系跳转
            DirectRelationDTO directRelationDTO = ObjTypeUtil.parseRule(relation);
            if (Objects.isNull(directRelationDTO)) {
                return null;
            }
            DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam().setClassCode(directRelationDTO.getToClassCode());
            List<DtpRwdObjectDTO> rwdObjectDTOS = dtpRwdClientService.listObject(false, false, ContextUtil.getProjectId(), param);
            List<String> systemIds = rwdObjectDTOS.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
            List<DtpRwdObjectDTO> list = queryRelation(objId, directRelationDTO, directRelationDTO.getFromClassCode(), systemIds);
            if (CollectionUtil.isEmpty(list)) {
                return null;
            }
            DtpRwdObjectDTO rwdObject = list.get(0);
            Set<String> relObjIds = rwdObject.getRelObjIds();
            if (CollectionUtil.isEmpty(relObjIds)) {
                return null;
            }
            for (String relId : relObjIds) {
                generateInteractiveConfig(relId, directRelationDTO.getToClassCode(), interactiveConfigVo);
                //如果找到一个可跳转的跳转对象，则跳出循环
                if (interactiveConfigVo.isCanJump()) {
                    return interactiveConfigVo;
                }
            }
        }
        return null;
    }

    private List<DtpRwdObjectDTO> queryRelation(String id, DirectRelationDTO relationDTO, String classCode, Collection<String> objFrom) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        if (StringUtils.isNotBlank(id)) {
            param.setId(Lists.newArrayList(id));
        }
        param.setClassCode(classCode);
        DtpRwdObjectRelationQueryParam relationTo = new DtpRwdObjectRelationQueryParam();
        relationTo.setObjFrom(objFrom);
        relationTo.setGraphCode(relationDTO.getGraphCode());
        relationTo.setRelCode(relationDTO.getRelCode());
        param.setRelationTo(relationTo);

        List<DtpRwdObjectDTO> lazyListDmpResponse = dtpRwdClientService.listObject(false, false, ContextUtil.getProjectId(), param);
        return lazyListDmpResponse;
    }

    private void generateInteractiveConfig(String dataObjectId, String classCode, InteractiveConfigVo interactiveConfigVo) {
        interactiveConfigVo.setSystemObjectId(dataObjectId);
        //查询是否存在系统图
        SystemGraph systemGraph = systemGraphService.getSystemGraph(dataObjectId, classCode, interactiveConfigVo.getGraphTypeCode());
        interactiveConfigVo.setCanJump(systemGraph != null && systemGraph.getSystemGraphDuplicateId() != null);
        interactiveConfigVo.setInteractiveObjectCode(classCode);
    }

    /**
     * @return java.util.List<com.persagy.apccontrol.basic.model.equipment.EquipmentInfoCodeObjectGroupByTemplateVO>
     * @Author 薛涛
     * @Description 查询设备列表根据项目/建筑/楼层/空间查询
     * @Date 11:34 2024/7/15
     * @Param [param]
     **/
    public List<EquipmentInfoCodeObjectGroupByTemplateVO> listInfoCodeConfigsByObject(EquipmentInfoCodeConfigByObjectQueryClientParam param) {
        InfoCodeConfigObjectByFiltersQueryParam paramFilter = new InfoCodeConfigObjectByFiltersQueryParam();
        BeanUtil.copyProperties(param, paramFilter);
        Set<String> spaceIds = instanceService.listSpaceIdsByInstance(param.getInstanceType(), param.getObjectIds().get(0));
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Lists.newArrayList();
        }
        paramFilter.setInSpaceIds(Sets.newHashSet(spaceIds));
        paramFilter.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        List<InfoCodeObjectGroupVO> infoCodeObjectGroupVOS = infoCodeConfigService.listInfoCodeConfigsByObjectFilters(paramFilter);
        if (CollectionUtil.isEmpty(infoCodeObjectGroupVOS)) {
            return new ArrayList<>();
        }
        Map<Long, List<InfoCodeObjectGroupVO>> equipmentInfoCodeObjectGroupVOMap = new HashMap<>(16);
        List<String> objectIds = Lists.newArrayList();
        for (InfoCodeObjectGroupVO infoCodeObjectGroupVO : infoCodeObjectGroupVOS) {
            objectIds.add(infoCodeObjectGroupVO.getId());
            Long templateTypeId = infoCodeObjectGroupVO.getTemplateTypeId();
            List<InfoCodeObjectGroupVO> infoCodeObjectGroupVOSListMap = equipmentInfoCodeObjectGroupVOMap.get(templateTypeId);
            if (CollectionUtil.isEmpty(infoCodeObjectGroupVOSListMap)) {
                infoCodeObjectGroupVOSListMap = new ArrayList<>();
                infoCodeObjectGroupVOSListMap.add(infoCodeObjectGroupVO);
                equipmentInfoCodeObjectGroupVOMap.put(templateTypeId, infoCodeObjectGroupVOSListMap);
            } else {
                infoCodeObjectGroupVOSListMap.add(infoCodeObjectGroupVO);
                equipmentInfoCodeObjectGroupVOMap.put(templateTypeId, infoCodeObjectGroupVOSListMap);
            }
        }
        //空间对应设备map
        Map<String, DtpRwdSpaceDTO> dtpRwdSpaceDTOMap = new HashMap<>();
        //设备所属空间
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceInByEquipmentId(
                true, false, param.getProjectId(), objectIds, new DtpRwdObjectQueryParam());
        if (CollectionUtils.isNotEmpty(spaces)) {
            for (DtpRwdSpaceDTO space : spaces) {
                for (String relObjId : space.getRelObjIds()) {
                    dtpRwdSpaceDTOMap.put(relObjId, space);
                }
            }
        }

        List<EquipmentInfoCodeObjectGroupByTemplateVO> equipmentInfoCodeObjectGroupByTemplateVOs = new ArrayList<>();
        for (Long aLong : equipmentInfoCodeObjectGroupVOMap.keySet()) {
            List<InfoCodeObjectGroupVO> infoCodeObjectGroupVOSNew = equipmentInfoCodeObjectGroupVOMap.get(aLong);
            List<InfoCodeObjectGroupCSVO> equipmentInfoCodeObjectGroupVOSCSNew = Lists.newArrayList();
            for (InfoCodeObjectGroupVO infoCodeObjectGroupVO : infoCodeObjectGroupVOSNew) {
                InfoCodeObjectGroupCSVO equipmentInfoCodeObjectGroupCSVO = new InfoCodeObjectGroupCSVO();
                BeanUtil.copyProperties(infoCodeObjectGroupVO, equipmentInfoCodeObjectGroupCSVO);
                DtpRwdSpaceDTO dtpRwdSpaceDTO = dtpRwdSpaceDTOMap.get(equipmentInfoCodeObjectGroupCSVO.getId());
                if (Objects.nonNull(dtpRwdSpaceDTO)) {
                    equipmentInfoCodeObjectGroupCSVO.setSpaceId(dtpRwdSpaceDTO.getId());
                    equipmentInfoCodeObjectGroupCSVO.setFloorId(dtpRwdSpaceDTO.getFloorId());
                }
                equipmentInfoCodeObjectGroupVOSCSNew.add(equipmentInfoCodeObjectGroupCSVO);
            }
            EquipmentInfoCodeObjectGroupByTemplateVO equipmentInfoCodeObjectGroupByTemplateVO = new EquipmentInfoCodeObjectGroupByTemplateVO();
            equipmentInfoCodeObjectGroupByTemplateVO.setTemplateTypeId(infoCodeObjectGroupVOSNew.get(0).getTemplateTypeId());
            equipmentInfoCodeObjectGroupByTemplateVO.setTemplateTypeName(infoCodeObjectGroupVOSNew.get(0).getTemplateTypeName());
            equipmentInfoCodeObjectGroupByTemplateVO.setObjects(equipmentInfoCodeObjectGroupVOSCSNew);
            equipmentInfoCodeObjectGroupByTemplateVOs.add(equipmentInfoCodeObjectGroupByTemplateVO);
        }
        return equipmentInfoCodeObjectGroupByTemplateVOs;

    }

    public RelationObjectsAndSpaceVO queryRelationObjectsAndSpace(EquipmentInfoCodeConfigByObjectQueryClientParam param) {

        RelationObjectsAndSpaceVO relationObjectsAndSpaceVO = new RelationObjectsAndSpaceVO();
        InfoCodeConfigObjectByFiltersQueryParam paramFilter = new InfoCodeConfigObjectByFiltersQueryParam();
        BeanUtil.copyProperties(param, paramFilter);
        Set<String> inSpaceIds = instanceService.listSpaceIdsByInstance(param.getInstanceType(), param.getObjectIds().get(0));
        if (CollectionUtils.isEmpty(inSpaceIds)) {
            return relationObjectsAndSpaceVO;
        }
        paramFilter.setInSpaceIds(Sets.newHashSet(inSpaceIds));
        paramFilter.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        List<InfoCodeObjectGroupVO> infoCodeObjectGroupVOS = infoCodeConfigService.listInfoCodeConfigsByObjectFilters(paramFilter);
        List<EquipmentInfoCodes> equipmentInfoCodesList = new ArrayList<>();
        Set<String> objectIds = new HashSet<>();

        //建筑map
        Map<String, DtpRwdBuildingDTO> dtpRwdBuildingDTOMap = new HashMap<>(16);
        //楼层map
        Map<String, DtpRwdFloorDTO> dtpRwdFloorDTOMap = new HashMap<>(16);
        //空间对应设备map
        Map<String, DtpRwdSpaceDTO> dtpRwdSpaceDTOMap = new HashMap<>();

        if (CollectionUtil.isNotEmpty(infoCodeObjectGroupVOS)) {
            for (InfoCodeObjectGroupVO infoCodeObjectGroupVO : infoCodeObjectGroupVOS) {
                objectIds.add(infoCodeObjectGroupVO.getId());
                EquipmentInfoCodes equipmentInfoCodes = new EquipmentInfoCodes();
                equipmentInfoCodes.setClassCode("");
                equipmentInfoCodes.setId(infoCodeObjectGroupVO.getId());
                equipmentInfoCodes.setLocalId("");
                equipmentInfoCodes.setLocalName(infoCodeObjectGroupVO.getLocalName());
                List<InfoCodeDataVO> infoCodeDataVOList = new ArrayList<>();
                handlerInfoCode(infoCodeObjectGroupVO.getRelInfoCodeGroupConfigs(), infoCodeDataVOList);
                equipmentInfoCodes.setInfoCodes(infoCodeDataVOList);
                equipmentInfoCodesList.add(equipmentInfoCodes);
            }

            List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(true, false, param.getProjectId()
                    , new DtpRwdObjectQueryParam().setId(objectIds));
            Map<String, DtpRwdObjectDTO> dtpRwdObjectDTOMap = new HashMap<>(16);
            if (CollectionUtils.isNotEmpty(dtpRwdObjectDTOS)) {
                for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
                    dtpRwdObjectDTOMap.put(dtpRwdObjectDTO.getId(), dtpRwdObjectDTO);
                }
            }

            //设备所属空间
            List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceInByEquipmentId(
                    true, false, param.getProjectId(), objectIds, new DtpRwdObjectQueryParam());
            if (CollectionUtils.isNotEmpty(spaces)) {
                for (DtpRwdSpaceDTO space : spaces) {
                    for (String relObjId : space.getRelObjIds()) {
                        dtpRwdSpaceDTOMap.put(relObjId, space);
                    }
                }
            }
            List<DtpRwdBuildingDTO> dtpRwdBuilding = dtpRwdClientService
                    .listBuilding(false, true, param.getProjectId(), null);
            if (CollectionUtils.isNotEmpty(dtpRwdBuilding)) {
                for (DtpRwdBuildingDTO dtpRwdBuildingDTO : dtpRwdBuilding) {
                    dtpRwdBuildingDTOMap.put(dtpRwdBuildingDTO.getId(), dtpRwdBuildingDTO);
                }
            }
            List<DtpRwdFloorDTO> dtpRwdFloor = dtpRwdClientService
                    .listFloor(false, true, param.getProjectId(), null);
            if (CollectionUtils.isNotEmpty(dtpRwdFloor)) {
                for (DtpRwdFloorDTO dtpRwdFloorDTO : dtpRwdFloor) {
                    dtpRwdFloorDTOMap.put(dtpRwdFloorDTO.getId(), dtpRwdFloorDTO);
                }
            }
            for (EquipmentInfoCodes equipmentInfoCodes : equipmentInfoCodesList) {
                String id = equipmentInfoCodes.getId();
                DtpRwdObjectDTO dtpRwdObjectDTO = dtpRwdObjectDTOMap.get(id);
                DtpRwdSpaceDTO dtpRwdSpaceDTO = dtpRwdSpaceDTOMap.get(id);
                equipmentInfoCodes.setLocalId(dtpRwdObjectDTO.getLocalId());
                equipmentInfoCodes.setClassCode(dtpRwdObjectDTO.getClassCode());
                equipmentInfoCodes.setObjType(dtpRwdObjectDTO.getObjType());
                if (dtpRwdSpaceDTO != null) {
                    equipmentInfoCodes.setSpaceLocalId(dtpRwdSpaceDTO.getLocalId());
                    equipmentInfoCodes.setSpaceId(dtpRwdSpaceDTO.getId());
                    equipmentInfoCodes.setSpaceLocalName(dtpRwdSpaceDTO.getLocalName());
                    String floorId = dtpRwdSpaceDTO.getFloorId();
                    DtpRwdFloorDTO dtpRwdFloorDTO = dtpRwdFloorDTOMap.get(floorId);
                    String buildingId = dtpRwdSpaceDTO.getBuildingId();
                    DtpRwdBuildingDTO dtpRwdBuildingDTO = dtpRwdBuildingDTOMap.get(buildingId);
                    if (dtpRwdFloorDTO != null) {
                        equipmentInfoCodes.setFloorSequenceID(dtpRwdFloorDTO.getFloorSequenceId());
                        equipmentInfoCodes.setFloorLocalName(dtpRwdFloorDTO.getLocalName());
                        equipmentInfoCodes.setFloorLocalId(dtpRwdFloorDTO.getLocalId());
                        equipmentInfoCodes.setFloorId(dtpRwdFloorDTO.getId());
                    }
                    if (dtpRwdBuildingDTO != null) {
                        equipmentInfoCodes.setBuildingId(dtpRwdBuildingDTO.getId());
                        equipmentInfoCodes.setBuildingName(dtpRwdBuildingDTO.getName());
                        equipmentInfoCodes.setBuildingLocalName(dtpRwdBuildingDTO.getLocalName());
                    }
                }
            }
        }
        relationObjectsAndSpaceVO.setEquipments(equipmentInfoCodesList);

        List<SpaceInfoVO> spaceIds = new ArrayList<>();
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = querySpaces(param.getProjectId(), null);
        for (DtpRwdSpaceDTO dtpRwdSpaceDTO : dtpRwdSpaceDTOS) {
            SpaceInfoVO spaceInfoVO = new SpaceInfoVO();
            spaceInfoVO.setId(dtpRwdSpaceDTO.getId());
            spaceInfoVO.setLocalId(dtpRwdSpaceDTO.getLocalId());
            spaceInfoVO.setLocalName(dtpRwdSpaceDTO.getLocalName());
            spaceInfoVO.setProjectId(dtpRwdSpaceDTO.getProjectId());
            spaceInfoVO.setObjType(dtpRwdSpaceDTO.getObjType());
            spaceInfoVO.setClassCode(dtpRwdSpaceDTO.getClassCode());
            spaceInfoVO.setBuildingId(dtpRwdSpaceDTO.getBuildingId());
            spaceInfoVO.setFloorId(dtpRwdSpaceDTO.getFloorId());

            DtpRwdBuildingDTO dtpRwdBuildingDTO = dtpRwdBuildingDTOMap.get(dtpRwdSpaceDTO.getBuildingId());
            if (Objects.nonNull(dtpRwdBuildingDTO)) {
                spaceInfoVO.setBuildArea(dtpRwdBuildingDTO.getUndefinedProperties().getOrDefault("roofArea", "0"));
                spaceInfoVO.setBuildingName(dtpRwdBuildingDTO.getName());
                spaceInfoVO.setBuildingLocalName(dtpRwdBuildingDTO.getLocalName());
            }
            DtpRwdFloorDTO dtpRwdFloorDTO = dtpRwdFloorDTOMap.get(dtpRwdSpaceDTO.getFloorId());
            if (Objects.nonNull(dtpRwdFloorDTO)) {
                spaceInfoVO.setFloorLocalId(dtpRwdFloorDTO.getLocalId());
                spaceInfoVO.setFloorLocalName(dtpRwdFloorDTO.getLocalName());
                spaceInfoVO.setFloorSequenceID(dtpRwdFloorDTO.getFloorSequenceId());
            }


            spaceIds.add(spaceInfoVO);
        }
        relationObjectsAndSpaceVO.setSpaces(spaceIds);
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.setId(param.getObjectIds().get(0));
        DtpRwdObjectDTO object = dtpRwdClientService.getObject(true,
                false,
                param.getProjectId(),
                dtpRwdObjectQueryParam);
        relationObjectsAndSpaceVO.setId(object.getId());
        relationObjectsAndSpaceVO.setLocalName(object.getLocalName());
        relationObjectsAndSpaceVO.setLocalId(object.getLocalId());

        return relationObjectsAndSpaceVO;
    }

    /**
     * @return void
     * @Author 薛涛
     * @Description 将分组的信息点数据重新组装成平铺
     * @Date 23:53 2024/7/22
     * @Param [relInfoCodeConfigs, infoCodeDataVOList]
     **/
    private void handlerInfoCode(List<InfoCodeGroupVO> relInfoCodeConfigs, List<InfoCodeDataVO> infoCodeDataVOList) {
        for (InfoCodeGroupVO relInfoCodeConfig : relInfoCodeConfigs) {
            List<InfoCodeConfigVO> relInfoCodeConfigsCh = relInfoCodeConfig.getRelInfoCodeConfigs();
            if (CollectionUtil.isNotEmpty(relInfoCodeConfigsCh)) {
                for (InfoCodeConfigVO infoCodeConfigsCh : relInfoCodeConfigsCh) {
                    InfoCodeDataVO infoCodeDataVO = new InfoCodeDataVO();
                    infoCodeDataVO.setInfoCode(infoCodeConfigsCh.getInfoCode());
                    infoCodeDataVO.setName(infoCodeConfigsCh.getInfoCodeName());
                    infoCodeDataVO.setDataValue(infoCodeConfigsCh.getDataValue());
                    infoCodeDataVO.setUnit(infoCodeConfigsCh.getUnit());
                    infoCodeDataVO.setDataSource(infoCodeConfigsCh.getDataSource());
                    infoCodeDataVOList.add(infoCodeDataVO);
                }
            }
            if (CollectionUtil.isNotEmpty(relInfoCodeConfig.getGroupChildren())) {
                handlerInfoCode(relInfoCodeConfig.getGroupChildren(), infoCodeDataVOList);
            }
        }

    }

    /**
     * 查询空间
     *
     * @param projectId      项目ID
     * @param filterSpaceIds 筛选空间ID, 为空时不进行过滤
     * @return 空间
     * @since 5.240502.0
     */
    @SuppressWarnings("SameParameterValue")
    private List<DtpRwdSpaceDTO> querySpaces(
            @NonNull String projectId,
            @Nullable Collection<String> filterSpaceIds) {
        // 机房：判断条件除了满足空间功能类型外，还需要空间中有上述末端设备（设备所在空间关系），满足这两个条件才算是机房(卢璐)
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        Set<String> equipmentIds = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam).stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(equipmentIds)) {
            return Lists.newArrayList();
        }
        List<DtpRwdEquipmentDTO> dtpRwdObjectList = dtpRwdClientService.listEquipmentServiceBySpaceIds(
                filterSpaceIds, new DtpRwdObjectQueryParam().setId(equipmentIds));

        List<String> objectIds = new ArrayList<>();
        for (DtpRwdEquipmentDTO dtpRwdEquipmentDTO : dtpRwdObjectList) {
            objectIds.add(dtpRwdEquipmentDTO.getId());
        }
        return dtpRwdClientService.listSpaceInByEquipmentId(true, false,
                projectId, objectIds, new DtpRwdObjectQueryParam()
                        .addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType,
                                DtpRwdCriteriaSignEnum.LIKE_RIGHT, "SFT-02.04"));
    }
}
