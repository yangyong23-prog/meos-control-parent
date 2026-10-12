package com.persagy.apccontrol.basic.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractDtpPersistServer;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdTwinControlObjectVO;
import com.persagy.apccontrol.basic.outside.dto.AcccccGroupDTO;
import com.persagy.apccontrol.basic.outside.dto.ControlObjectDto;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.client.DtpRwdClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdPageQuery;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdRelationResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.wrapper.DtpRwdClientPageWrapper;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.utils.DMPResultUtil;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.executor.MapExecutor;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 物理世界业务服务实现
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Service
@Slf4j
public class BizDtpRwdServiceImpl implements BizDtpRwdService {

    private static final String LOG_TAG = "物理世界业务服务";

    @Autowired
    private DtpRwdClient dtpRwdClient;

    @Autowired
    private DtpRwdClientPageWrapper dtpRwdClientPageWrapper;

    @Autowired
    private AbstractDtpPersistServer abstractDtpPersistServer;

    @Autowired
    private DtpPersistServerClient dtpPersistServerClient;

    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Cacheable(cacheNames = "queryObjectList", key = "#groupCode+#projectId+#objectIds.hashCode() +'-30m'", cacheManager = CacheKey.CAFFEINE_CACHE_MANAGER,
            unless = "#result == null || #result.isEmpty()")
    @Override
    public List<JSONObject> queryObjectList(String groupCode, String projectId, List<String> objectIds) {
        if (CollUtil.isEmpty(objectIds)) {
            return Lists.newArrayList();
        }
        BaseQueryPropertyDTO propertyDTO = new BaseQueryPropertyDTO();
        propertyDTO.setId(objectIds);

        BaseQueryParamDTO paramDTO = new BaseQueryParamDTO();
        paramDTO.setPage(new DtpRwdPageQuery().setPage(1).setSize(Integer.MAX_VALUE));
        paramDTO.setCriteria(propertyDTO);
        DmpResult<List<JSONObject>> listDmpResult = this.dtpRwdClientPageWrapper.queryObjectList(groupCode, projectId, paramDTO);
        DMPResultUtil.handleResult(listDmpResult);

        //List<JSONObject> r = ObjectUtil.isEmpty(listDmpResult.getData()) ? Lists.newArrayList() : listDmpResult.getData();
        return ObjectUtil.isEmpty(listDmpResult.getData()) ? Lists.newArrayList() : listDmpResult.getData();
    }

    @Override
    public List<JSONObject> queryObjectList(String groupCode, String projectId, List<String> classCode, String graphCode, String relCode, List<String> objFrom, List<String> objTo) {
        QueryEqParamDTO queryEqParamDTO = new QueryEqParamDTO();
        queryEqParamDTO.setClassCode(classCode);
        queryEqParamDTO.setValid(1);
        if (CollectionUtil.isNotEmpty(objTo)) {
            queryEqParamDTO.setRelationFrom(graphCode, relCode, objTo);
        } else if (CollectionUtil.isNotEmpty(objFrom)) {
            queryEqParamDTO.setRelationTo(graphCode, relCode, objFrom);
        }
        BaseQueryParamDTO paramDTO = new BaseQueryParamDTO();
        paramDTO.setPage(new DtpRwdPageQuery().setPage(1).setSize(Integer.MAX_VALUE));
        paramDTO.setCriteria(queryEqParamDTO);

        DmpResult<List<JSONObject>> listDmpResult = this.dtpRwdClientPageWrapper.queryObjectList(groupCode, projectId, paramDTO);
        DMPResultUtil.handleResult(listDmpResult);
        return ObjectUtil.isEmpty(listDmpResult.getData()) ? Lists.newArrayList() : listDmpResult.getData();
    }


    /**
     * @param params
     * @return List<Map>
     * @descrption 远程调用查询本地Iot数据
     * @author tangzy
     * @since 2023/3/21 22:00
     */
    @Override
    public List<DtpRwdObjectDTO> querySystemDiagramData(QueryDTO params) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setClassCode(params.getCriteria().getClassCode())
                .setObjType(params.getCriteria().getObjType());
        return this.dtpRwdClientService.listObject(queryParam);

    }


    @Override
    public Map<String, List<DtpRwdRelationResponse>> mapFloorRelationSpacesBySystem(
            boolean queryCache, boolean isThrows,
            @NonNull String projectId, @NonNull String systemId,
            @NonNull Collection<String> floorIds, @Nullable DtpRwdRelationQueryParam param)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdRelationQueryParam::new);
        DtpRwdRelationTypeEnum instanceRelationType = DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE;
        param.setGraphCode(instanceRelationType.value().getLeft());
        param.setRelCode(instanceRelationType.value().getRight());
        param.setFromCriteria(new DtpRwdObjectQueryParam().setId(systemId));
        // 查询中央供冷系统服务的空间
        List<DtpRwdRelationResponse> systemRelationSpaces = dtpRwdClientService.listRelationObject(
                queryCache, isThrows, projectId, param);
        systemRelationSpaces.sort((o1, o2) -> ObjectUtils.compare(o1.getToLocalId(), o2.getToLocalId()));
        Map<String, List<DtpRwdRelationResponse>> result = new LinkedHashMap<>();
        for (DtpRwdRelationResponse systemRelationSpace : systemRelationSpaces) {
            JSONObject toInfos = systemRelationSpace.getToInfos();
            String floorId;
            if (Objects.isNull(toInfos) || !toInfos.containsKey(DtpRwdConvert.INFO_CODE_FLOOR_ID)) {
                if (isThrows) {
                    LogExecutor.rpc(param, systemRelationSpace).error(log, LOG_TAG, "未找到关联楼层信息", Arrays.asList(
                            Pair.of("projectId", projectId),
                            Pair.of("systemId", systemId)
                    ));
                    throw new BusinessException(ErrorCodeConstants.SERVICE_ERROR_C0110.getCode(), LocaleMessageUtil.getMessage("key_5d2d1381bcf84e4b947e2905f40219de"));
                } else {
                    floorId = "null";
                }
            } else {
                floorId = toInfos.getString(DtpRwdConvert.INFO_CODE_FLOOR_ID);
                // 跳过不在查询条件中的空间
                if (!floorIds.contains(floorId)) {
                    continue;
                }
            }
            MapExecutor.dynamicPut(result, floorId, systemRelationSpace, ArrayList::new);
        }
        return result;
    }

    @Override
    public List<DtpRwdBuildingDTO> treeBuildingFloorSpace(
            boolean queryCache, boolean isThrows,
            @NonNull String projectId, @Nullable Collection<String> systemId,
            @Nullable DtpRwdObjectQueryParam buildingParam,
            @Nullable DtpRwdObjectQueryParam floorParam,
            @Nullable DtpRwdObjectQueryParam spaceParam,
            @Nullable Boolean isQuerySpace)
            throws FeignException, BusinessException {
        buildingParam = Optional.ofNullable(buildingParam).orElseGet(DtpRwdObjectQueryParam::new);
        floorParam = Optional.ofNullable(floorParam).orElseGet(DtpRwdObjectQueryParam::new);
        spaceParam = Optional.ofNullable(spaceParam).orElseGet(DtpRwdObjectQueryParam::new);
        isQuerySpace = BooleanUtils.toBooleanDefaultIfNull(isQuerySpace, true);
        if (CollectionUtils.isNotEmpty(systemId)) {
            DtpRwdRelationTypeEnum instanceRelationType = DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE;
            // 查询中央供冷系统服务的空间
            List<DtpRwdRelationResponse> systemRelationSpaces = dtpRwdClientService.listRelationObject(
                    queryCache, isThrows, projectId, new DtpRwdRelationQueryParam(instanceRelationType,
                            new DtpRwdObjectQueryParam().setId(systemId), null));
            Set<String> spaceIds = systemRelationSpaces.stream()
                    .map(DtpRwdRelationResponse::getToId)
                    .collect(Collectors.toSet());
            spaceParam = spaceParam.setId(spaceIds);
        }
        List<DtpRwdSpaceDTO> spaces = Collections.emptyList();
        if (isQuerySpace) {
            // 空间查询字段, 至少必须存在ID|楼层ID|建筑ID
            Collection<String> spaceWithColumns = spaceParam.getWithColumns();
            if (CollectionUtils.isNotEmpty(spaceWithColumns)) {
                spaceWithColumns.add(DtpRwdObjectDTO.Fields.id);
                spaceWithColumns.add(DtpRwdSpaceDTO.Fields.floorId);
                spaceWithColumns.add(DtpRwdSpaceDTO.Fields.buildingId);
                spaceParam.setWithColumns(spaceWithColumns);
            }
            spaces = dtpRwdClientService.listSpace(spaceParam);
            if (CollectionUtils.isEmpty(spaces)) {
                return Lists.newArrayList();
            }
            floorParam.setId(spaces.stream()
                    .map(DtpRwdSpaceDTO::getFloorId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
        }
        // 楼层查询字段, 至少必须存在ID和建筑ID
        Collection<String> floorWithColumns = floorParam.getWithColumns();
        if (CollectionUtils.isNotEmpty(floorWithColumns)) {
            floorWithColumns.add(DtpRwdObjectDTO.Fields.id);
            floorWithColumns.add(DtpRwdFloorDTO.Fields.floorSequenceId);
            floorWithColumns.add(DtpRwdFloorDTO.Fields.buildingId);
            floorParam.setWithColumns(floorWithColumns);
        }
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(floorParam);
        if (CollectionUtils.isEmpty(floors)) {
            return Lists.newArrayList();
        }
        buildingParam.setId(floors.stream()
                .map(DtpRwdFloorDTO::getBuildingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        // 楼层查询字段, 至少必须存在ID
        Collection<String> buildingWithColumns = buildingParam.getWithColumns();
        if (CollectionUtils.isNotEmpty(buildingWithColumns)) {
            buildingWithColumns.add(DtpRwdObjectDTO.Fields.id);
            buildingParam.setWithColumns(buildingWithColumns);
        }
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(buildingParam);
        if (CollectionUtils.isEmpty(buildings)) {
            return Lists.newArrayList();
        }
        Map<String, List<DtpRwdFloorDTO>> buildingIdAndFloors = DtpRwdFloorDTO.mapBuildingIdAndFloors(floors);
        MultiKeyMap<String, List<DtpRwdSpaceDTO>> buildingFloorIdAndSpaces = new MultiKeyMap<>();
        if (isQuerySpace) {
            buildingFloorIdAndSpaces = dtpRwdClientService.parseBuildingFloorIdAndSpaces(spaces);
        }
        for (DtpRwdBuildingDTO building : buildings) {
            String buildingId = building.getId();
            List<DtpRwdFloorDTO> tempFloors = buildingIdAndFloors.get(buildingId);
            if (CollectionUtils.isEmpty(tempFloors)) {
                continue;
            }
            for (DtpRwdFloorDTO tempFloor : tempFloors) {
                if (isQuerySpace) {
                    String floorId = tempFloor.getId();
                    List<DtpRwdSpaceDTO> tempSpaces = buildingFloorIdAndSpaces.get(buildingId, floorId);
                    if (CollectionUtils.isEmpty(tempSpaces)) {
                        continue;
                    }
                    tempFloor.setSpaces(tempSpaces);
                }
            }
            building.setFloors(tempFloors);
        }
        return buildings;
    }

    @Override
    public CustomerResponse<List<EnergyDataResponse>> listIpDataInPeriod(EnergyDataQueryParam param, String projectId, String groupCode) {
        try {
            return abstractDtpPersistServer.listIpDataInPeriod(groupCode, projectId, param);
        } catch (Exception e) {
            log.error("数据获取失败, groupCode={}, projectId={},params={}", ContextUtil.getGroupCode(), projectId, param);
        }
        return null;
    }

    @Override
    public List<EnergyDataResponse> queryIpDataInPeriod(EnergyDataQueryParam param) {
        try {
            CustomerResponse<List<EnergyDataResponse>> response = listIpDataInPeriod(param, ContextUtil.getProjectId(), ContextUtil.getGroupCode());
            CustomerResponse.handleResult(response);
            List<EnergyDataResponse> content = response.getContent();
            if (CollectionUtils.isEmpty(content)) {
                return new ArrayList<>();
            }
            return content;
        } catch (Exception e) {
            log.error("数据获取失败, groupCode={},params={}", ContextUtil.getGroupCode(), param);
        }
        return null;
    }

    @Override
    public List<EnergyDataResponse> queryIpDataOnMoment(String groupCode, String projectId, List<String> objectIds, List<String> infoCodes) {
        return this.dtpRwdClientService.queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes);
    }

    @Override
    public List<EnergyDataResponse> queryIpDataOnMomentInOneHour(Set<String> objectIds, Set<String> infoCodes) {
        return this.dtpRwdClientService.queryIpDataOnMomentInOneHour(objectIds, infoCodes);
    }

    @Override
    public List<JSONObject> queryRelation(String groupCode, String projectId, String graphCode, String relCode, String relValue, List<String> objFroms, List<String> objTos) {
        DtpRwdRelationQueryParam parent = new DtpRwdRelationQueryParam();

        DtpRwdObjectRelationQueryParam criteria = new DtpRwdObjectRelationQueryParam();
        criteria.setGraphCode(graphCode);
        criteria.setRelCode(relCode);
        if (StrUtil.isNotBlank(relValue)) {
            criteria.setRelValue(relValue);
        }
        if (CollectionUtil.isNotEmpty(objFroms)) {
            criteria.setObjFrom(objFroms);
        }
        if (CollectionUtil.isNotEmpty(objTos)) {
            criteria.setObjTo(objTos);
        }
        parent.setCriteria(criteria);
        DmpResult<List<JSONObject>> nodeDmpResult = dtpRwdClientPageWrapper.queryRelation(groupCode, projectId, null, null, parent);
        DMPResultUtil.handleResult(nodeDmpResult);
        if (ObjectUtil.isEmpty(nodeDmpResult.getData())) {
            return new ArrayList<>();
        }

        return nodeDmpResult.getData();
    }


    @Override
    //@Cacheable(cacheNames = "queryAcccccGroupBySystemId", key = "#groupCode+#projectId+#systemId + '_30m'", cacheManager = CacheKey.CAFFEINE_CACHE_MANAGER)
    public AcccccGroupDTO queryAcccccGroupBySystemId(String groupCode, String projectId, String systemId) {
        DmpResult<AcccccGroupDTO> dmpResult = dtpRwdClient.queryAcccccGroupBySystemId(groupCode, projectId, systemId, null, null);
        DMPResultUtil.handleResult(dmpResult);
        return dmpResult.getData();
    }

    @Override
    public JSONObject querySysGroupBySystemId(String groupCode, String projectId, String systemId) {
        CustomerResponse<JSONObject> response = dtpRwdClient.querySysGroupBySystemId(groupCode, projectId, systemId, null, null);
        CustomerResponse.handleResult(response);
        return response.getContent();
    }

    @Override
    public List<ControlObjectDto> queryControlObjects(String groupCode, String projectId, String... classCodeList) {
        DmpResult<List<ControlObjectDto>> dmpResult = dtpRwdClient.queryControlObjects(groupCode, projectId, classCodeList);
        DMPResultUtil.handleResult(dmpResult);
        return dmpResult.getData();
    }

    @Override
    public boolean existsObjects(String groupCode, String projectId, String objectId, String classCode, String graphCode, String relCode, String relDirection) {
        if (StrUtil.isBlank(classCode)) {
            return false;
        }
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = null;

        // 关系查询验证
        if (StrUtil.isAllNotBlank(objectId, classCode, graphCode, relCode, relDirection)) {
            DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setClassCode(classCode);
            DtpRwdObjectRelationQueryParam relationQueryParam = new DtpRwdObjectRelationQueryParam().setGraphCode(graphCode).setRelCode(relCode);
            if (CommonConst.FROM_RELATION.equals(relDirection)) {
                relationQueryParam.setObjFrom(objectId);
                queryParam.setRelationTo(relationQueryParam);
            } else {
                relationQueryParam.setObjTo(objectId);
                queryParam.setRelationFrom(relationQueryParam);
            }
            dtpRwdObjectDTOS = this.dtpRwdClientService.listObject(false, false, projectId, queryParam);
        } else {
            dtpRwdObjectDTOS = this.dtpRwdClientService.listObject(false, false, projectId, new DtpRwdObjectQueryParam().setClassCode(classCode));
        }
        return CollectionUtil.isNotEmpty(dtpRwdObjectDTOS);
    }


    /**
     * @param result
     * @return void
     * @descrption 验证远程调用数据
     * @author tangzy
     * @since 2023/3/21 22:00
     */
    @Deprecated
    public static void handleResult(DmpResult<ArrayNode> result) {
        String result1 = result.getResult();
        if (StringUtils.isBlank(result1) || !"success".equals(result1)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), result.getMessage() == null ? LocaleMessageUtil.getMessage("key_92cd29bb9b114e049ec56c1141b79bbd") : result.getMessage());
        }
    }

    @Override
    public List<DtpRwdObjectDTO> getAcahcObj(String projectId, String sysId) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setClassCodeEnum(DtpRwdClassCodeEnum.ACAHC_GROUP);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(sysId));
        // 查询系统下的空气源热泵控制组
        return dtpRwdClientService.listObject(true, false, projectId, param);
    }


    /**
     * 查询是否是管控对象
     *
     * @param allClassCodes classCode
     * @since 5.240602.4
     */
    @Override
    public Map<String, DtpRwdTwinControlObjectVO> queryControlObjects(@NonNull Set<String> allClassCodes) {
        if (CollectionUtils.isEmpty(allClassCodes)) {
            return new HashMap<>();
        }
        String projectId = ContextUtil.getProjectId();
        List<DtpRwdTwinControlObjectDTO> controlObjects = dtpRwdClientService
                .listTwinControlObjects(false, projectId, allClassCodes);
        if (CollectionUtils.isEmpty(controlObjects)) {
            return new HashMap<>();
        }
        Map<String, DtpRwdTwinControlObjectVO> dtpRwdObjectDTOMap = new HashMap<>();
        for (DtpRwdTwinControlObjectDTO controlObject : controlObjects) {
            DtpRwdTwinControlObjectVO dtpRwdTwinControlObjectVO = new DtpRwdTwinControlObjectVO();
            dtpRwdTwinControlObjectVO.setObjectId(controlObject.getObjectId());
            dtpRwdTwinControlObjectVO.setClassCode(controlObject.getClassCode());
            dtpRwdTwinControlObjectVO.setSystemId(controlObject.getSystemId());
            List<DtpRwdTwinControlInfoCodeDTO> infoCodes = controlObject.getInfoCodes();
            Map<String, DtpRwdTwinControlInfoCodeDTO> infoCodesMap = new HashMap<>(16);
            for (DtpRwdTwinControlInfoCodeDTO infoCode : infoCodes) {
                infoCodesMap.put(infoCode.getCode(), infoCode);
            }
            dtpRwdTwinControlObjectVO.setInfoCodesMap(infoCodesMap);
            dtpRwdObjectDTOMap.put(controlObject.getObjectId(), dtpRwdTwinControlObjectVO);
        }

        return dtpRwdObjectDTOMap;
    }

    @Override
    public Boolean booleanControlObject(String objectId, String InfoCode,
                                        Map<String, DtpRwdTwinControlObjectVO> dtpRwdTwinControlObjectVOMap) {
        DtpRwdTwinControlObjectVO dtpRwdTwinControlObjectVO = dtpRwdTwinControlObjectVOMap.get(objectId);
        if (Objects.isNull(dtpRwdTwinControlObjectVO)) {
            return false;
        }
        Map<String, DtpRwdTwinControlInfoCodeDTO> infoCodesMap = dtpRwdTwinControlObjectVO.getInfoCodesMap();
        DtpRwdTwinControlInfoCodeDTO dtpRwdTwinControlInfoCodeDTO = infoCodesMap.get(InfoCode);
        if (Objects.isNull(dtpRwdTwinControlInfoCodeDTO)) {
            return false;
        }
        return true;
    }

}
