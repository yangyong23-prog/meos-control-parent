package com.persagy.apccontrol.basic.outside.feign.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.abstracts.AbstractDtpPersistServer;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.controller.shellconfig.param.QueryShellConfigByInfoCodeParam;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.EnergyDataInfoQueryParam;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.dto.QueryIPDataInPeriodDTO;
import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.client.DtpRwdClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCriteriaQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdPageQuery;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.kitchen.KitchenExhaustResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.*;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.wrapper.DtpRwdClientPageWrapper;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerSize;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerTypeSub;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 查询物理世界对象和关系
 *
 * @author yuyao
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DtpRwdClientServiceImpl implements DtpRwdClientService {

    private static final String LOG_TAG = "物理世界服务";

    private final DtpRwdClient dtpRwdClient;
    private final DtpRwdClientPageWrapper dtpRwdClientPageWrapper;
    private final AbstractDtpPersistServer abstractDtpPersistServer;
    private final DtpRwdConvert dtpRwdConvert;
    private final DtpPersistServerClient dtpPersistServerClient;

    @Lazy
    @Autowired
    private ShellConfigService shellConfigService;

    @Override
    public int countObject(@NonNull DtpRwdObjectQueryParam param) throws FeignException {
        return queryObjectCountResponse(ContextUtil.getProjectId(), new DtpRwdCriteriaQueryParam<>(param));
    }

    @Override
    public List<DtpRwdObjectDTO> listObject(@NonNull DtpRwdObjectQueryParam param) throws FeignException {
        List<DtpRwdObjectResponse> content = queryObjectResponse(new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toDto)
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT, key = "#projectId + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdObjectDTO> listObject(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toDto)
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public Map<String, String> mapObjectIdAndLocalName(@NonNull Collection<String> objectIds) {
        return mapObjectIdAndLocalName(false, objectIds);
    }

    @Override
    public Map<String, String> mapObjectIdAndLocalName(
            boolean isThrows, @NonNull Collection<String> objectIds) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Maps.newHashMap();
        }
        return listObject(new DtpRwdObjectQueryParam().setId(objectIds).addWithColumn(
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName)).stream()
                .filter(item -> ObjectUtils.allNotNull(item.getId(), item.getLocalName()))
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdObjectDTO::getLocalName, (o1, o2) -> o1));
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_RELATION_OBJECT, key = "#projectId + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdRelationResponse> listRelationObject(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull DtpRwdRelationQueryParam param)
            throws FeignException, BusinessException {
        return queryRelationObjectResponse(isThrows, projectId, param, CustomerResponse::getContent, ArrayList::new);
    }

    @Override
    public List<DtpRwdBuildingDTO> listBuilding(@Nullable DtpRwdObjectQueryParam param) throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.BUILDING);
        List<DtpRwdObjectResponse> content = queryObjectResponse(new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toBuildingDto)
                    .sorted((o1, o2) -> ObjectUtils.compare(o1.getBuildingSequenceId(), o2.getBuildingSequenceId()))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_BUILDING, key = "#projectId + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdBuildingDTO> listBuilding(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.BUILDING);
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toBuildingDto)
                    .sorted((o1, o2) -> ObjectUtils.compare(o1.getBuildingSequenceId(), o2.getBuildingSequenceId()))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public List<DtpRwdBuildingDTO> listBuildingByFloorId(
            @Nullable Collection<String> floorIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.BUILDING);
        if (Objects.nonNull(floorIds)) {
            param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.FLOOR_IN_BUILDING).setObjTo(floorIds));
        }
        return listBuilding(param);
    }

    @Override
    public List<DtpRwdBuildingDTO> listBuildingBySpaceId(
            @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.BUILDING);
        if (Objects.nonNull(spaceIds)) {
            param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.SPACE_IN_BUILDING).setObjTo(spaceIds));
        }
        return listBuilding(param);
    }

    @Override
    public List<DtpRwdBuildingDTO> listBuildingInByEquipmentId(
            @Nullable Collection<String> equipmentId,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.BUILDING);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_BUILDING).setObjFrom(equipmentId));
        return listBuilding(param);
    }

    @Override
    public List<DtpRwdFloorDTO> listFloor(DtpRwdObjectQueryParam param) throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.FLOOR);
        List<DtpRwdObjectResponse> content = queryObjectResponse(new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toFloorDto)
                    .sorted(Comparator.comparing(DtpRwdFloorDTO::getFloorSequenceId,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_FLOOR, key = "#projectId + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdFloorDTO> listFloor(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.FLOOR);
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toFloorDto)
                    .sorted(Comparator.comparing(DtpRwdFloorDTO::getFloorSequenceId,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public List<DtpRwdFloorDTO> listFloorByBuildingId(
            @Nullable Collection<String> buildingIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.FLOOR);
        if (Objects.nonNull(buildingIds)) {
            param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.FLOOR_IN_BUILDING).setObjFrom(buildingIds));
        }
        return listFloor(param);
    }

    @Override
    public List<DtpRwdFloorDTO> listFloorBySpaceId(
            @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.FLOOR);
        if (Objects.nonNull(spaceIds)) {
            param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.SPACE_IN_FLOOR).setObjTo(spaceIds));
        }
        return listFloor(param);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_FLOOR_BY_SPACE_ID, key = "#projectId + #spaceIds?.hashCode() + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdFloorDTO> listFloorBySpaceId(
            boolean queryCache, boolean isThrows,
            @NonNull String projectId, @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.FLOOR);
        if (Objects.nonNull(spaceIds)) {
            param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.SPACE_IN_FLOOR).setObjTo(spaceIds));
        }
        return listFloor(queryCache, isThrows, projectId, param);
    }

    @Override
    public List<DtpRwdFloorDTO> listFloorInByEquipmentId(
            @Nullable Collection<String> equipmentId,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.FLOOR);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_FLOOR).setObjFrom(equipmentId));
        return listFloor(param);
    }

    @Override
    public List<DtpRwdFloorDTO> listFloorServiceByEquipmentId(
            @Nullable Collection<String> equipmentId,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.FLOOR);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_FLOOR).setObjFrom(equipmentId));
        return listFloor(param);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_FLOOR_SERVICE_BY_EQUIPMENT_ID, key = "#projectId + #equipmentId?.hashCode() + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdFloorDTO> listFloorServiceByEquipmentId(
            boolean queryCache, boolean isThrows,
            @NonNull String projectId, @Nullable Collection<String> equipmentId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.FLOOR);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_FLOOR).setObjFrom(equipmentId));
        return listFloor(queryCache, isThrows, projectId, param);
    }

    @Override
    @SuppressWarnings({"DuplicatedCode"})
    public List<DtpRwdSpaceDTO> listSpace(@Nullable DtpRwdObjectQueryParam param) throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.SPACE);
        List<DtpRwdObjectResponse> content = queryObjectResponse(new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toSpaceDto)
                    .sorted((o1, o2) -> ObjectUtils.compare(o1.getLocalId(), o2.getLocalId()))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    @SuppressWarnings({"DuplicatedCode"})
    public List<DtpRwdSpaceDTO> listACSpace(@Nullable DtpRwdObjectQueryParam param) throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.AC_SPACE);
        List<DtpRwdObjectResponse> content = queryObjectResponse(new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toSpaceDto)
                    .sorted((o1, o2) -> ObjectUtils.compare(o1.getLocalId(), o2.getLocalId()))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_SPACE, key = "#projectId + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdSpaceDTO> listSpace(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.SPACE);
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toSpaceDto)
                    .sorted((o1, o2) -> ObjectUtils.compare(o1.getLocalId(), o2.getLocalId()))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaceByRoomFuncType(
            @NonNull DtpRwdSpaceByRoomFuncTypeQueryParam param)
            throws FeignException {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        List<DtpRwdObjectResponse> responses = query(false, groupCode, projectId, LocaleMessageUtil.getMessage("key_93930e54e51b4dbda89417183881b172"), param,
                () -> dtpRwdClientPageWrapper.listSpacesByRoomFuncType(commonParam, param), ArrayList::new);
        if (CollectionUtils.isEmpty(responses)) {
            return Lists.newArrayList();
        }
        return responses.stream()
                .map(dtpRwdConvert::toSpaceDto)
                .sorted((o1, o2) -> ObjectUtils.compare(o1.getLocalId(), o2.getLocalId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaceByFloorId(
            @Nullable Collection<String> floorIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.SPACE);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.SPACE_IN_FLOOR).setObjFrom(floorIds));
        return listSpace(param);
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaceByBuildingId(
            @Nullable Collection<String> buildingIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.SPACE);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.SPACE_IN_BUILDING).setObjFrom(buildingIds));
        return listSpace(param);
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaceServiceByEquipmentId(
            @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.SPACE);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjFrom(equipmentIds));
        return listSpace(param);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_SPACE_SERVICE_BY_EQUIPMENT_ID,
            key = "#equipmentIds?.hashCode() + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdSpaceDTO> listSpaceServiceByEquipmentId(
            boolean queryCache,
            @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        return listSpaceServiceByEquipmentId(equipmentIds, param);
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaceInByEquipmentId(
            @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.SPACE);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjFrom(equipmentIds));
        return listSpace(param);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_SPACE_IN_BY_EQUIPMENT_ID, key = "#projectId + #equipmentIds?.hashCode() + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdSpaceDTO> listSpaceInByEquipmentId(
            boolean queryCache, boolean isThrows,
            @NonNull String projectId, @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.SPACE);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjFrom(equipmentIds));
        return listSpace(queryCache, isThrows, projectId, param);
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipment(
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        List<DtpRwdObjectResponse> content = queryObjectResponse(false, ContextUtil.getProjectId(),
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toEquipmentDto)
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipmentServiceBySpaceIds(
            @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(spaceIds));
        return listEquipment(param);
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipmentSensorBySpaceIds(
            @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_MONITOR_IN_SPACE).setObjTo(spaceIds));
        return listEquipment(param);
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipmentInBySpaceIds(
            @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjTo(spaceIds));
        return listEquipment(param);
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipmentInByBuildingIds(
            @Nullable Collection<String> buildingIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_BUILDING).setObjTo(buildingIds));
        return listEquipment(param);
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipmentInBySystemIds(
            @Nullable Collection<String> systemIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(systemIds));
        return listEquipment(param);
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipmentGroupInBySystemIds(
            @Nullable Collection<String> systemIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(systemIds));
        return listEquipment(param);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_CENTRAL_SYSTEM, key = "#projectId + #param?.hashCode()", condition = "#queryCache", sync = true)
    public List<DtpRwdObjectDTO> listCentralSystem(
            boolean queryCache, boolean isThrows,
            @NonNull String projectId, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        if (param != null) {
            List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                    new DtpRwdCriteriaQueryParam<>(param));
            if (CollectionUtils.isNotEmpty(content)) {
                return content.stream()
                        .map(dtpRwdConvert::toDto)
                        .collect(Collectors.toList());
            }
        }
        return Lists.newArrayList();
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_CENTRAL_COOLING_SYSTEM, key = "#projectId + #param?.hashCode()", condition = "#queryCache", sync = true)
    public List<DtpRwdObjectDTO> listCentralCoolingSystem(
            boolean queryCache, boolean isThrows,
            @NonNull String projectId, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.COLD_SYSTEM);
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toDto)
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_CENTRAL_COOLING_HEATING_SYSTEM, key = "#projectId + #param?.hashCode()", condition = "#queryCache", sync = true)
    public List<DtpRwdObjectDTO> listCentralCoolingHeatingSystem(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.COLD_HOT_SYSTEM);
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toDto)
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_CHILLER_UNIT, key = "#projectId + #coldSystemId + #param?.hashCode()", condition = "#queryCache", sync = true)
    public List<DtpRwdRelationResponse> listChillerUnit(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull String coldSystemId, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        DtpRwdObjectQueryParam fromQueryParam = new DtpRwdObjectQueryParam()
                .setId(coldSystemId)
                .setClassCodeEnum(DtpRwdClassCodeEnum.COLD_SYSTEM);
        DtpRwdRelationQueryParam queryParam = new DtpRwdRelationQueryParam()
                .setFromCriteria(fromQueryParam)
                .setToCriteria(param);
        queryParam.fillRelationType(DtpRwdRelationTypeEnum.EQ_IN_SYSTEM);
        return queryRelationObjectResponse(isThrows, projectId,
                queryParam, CustomerResponse::getContent, ArrayList::new);
    }

    @Override
    public Map<ChillerTypeSub, Map<ChillerSize, List<String>>> queryChillerType2Size2Ids(@NonNull String systemId) {
        Map<String, Map<String, List<String>>> response = query(false, "系统下冷机分组", systemId,
                () -> this.dtpRwdClient.querySystemGroup(ContextUtil.getGroupCode(), ContextUtil.getProjectId(),
                        systemId, null, null), Collections::emptyMap);
        Map<ChillerTypeSub, Map<ChillerSize, List<String>>> result = new LinkedHashMap<>();
        response.forEach((type, size2ChillerId) -> {
            ChillerTypeSub chillerType = BaseEnum.analyze(type, ChillerTypeSub.class, false);
            if (Objects.isNull(chillerType)) {
                logProjectInstance().warn("无法解析冷机类型", Arrays.asList(
                        Pair.of("systemId", systemId),
                        Pair.of("type", type)
                ));
                return;
            }
            if (MapUtils.isEmpty(size2ChillerId)) {
                return;
            }
            size2ChillerId.forEach((size, chillerIds) -> {
                ChillerSize chillerSize = ChillerSize.getByCode(size);
                if (Objects.isNull(chillerSize)) {
                    logProjectInstance().warn("无法解析冷机大小", Arrays.asList(
                            Pair.of("systemId", systemId),
                            Pair.of("type", type),
                            Pair.of("size", size)
                    ));
                    return;
                }
                if (CollectionUtils.isEmpty(chillerIds)) {
                    return;
                }
                MapExecutor.dynamicPutAll(result, chillerType, chillerSize, chillerIds, LinkedHashMap::new, ArrayList::new);
            });
        });
        return result;
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_FREEZE_PUMP_GROUP, key = "#projectId + #acccSystemIds + #acatSystemIds + #param?.hashCode()", condition = "#queryCache", sync = true)
    public List<DtpRwdFreezePumpDTO> listFreezePumpGroup(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> acccSystemIds,
            @Nullable Collection<String> acatSystemIds,
            @Nullable DtpRwdObjectQueryParam param,
            boolean realLocalName)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.ACCPC_GROUP);
        // 查询供冷系统关联的空调末端系统
        DtpRwdObjectQueryParam acatSystemQueryParam = new DtpRwdObjectQueryParam();
        acatSystemQueryParam.setClassCodeEnum(DtpRwdClassCodeEnum.ACAT_SYSTEM);
        if (Objects.isNull(acccSystemIds)) {
            DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.COLD_SYSTEM);
            List<DtpRwdObjectResponse> acccSystems = queryObjectResponse(false, projectId,
                    new DtpRwdCriteriaQueryParam<>(queryParam));
            if (CollectionUtils.isEmpty(acccSystems)) {
                LogExecutor.instance().warn(log, LOG_TAG, "无中央供冷系统", Arrays.asList(
                        Pair.of("projectId", projectId),
                        Pair.of("param", queryParam)
                ));
                return Lists.newArrayList();
            }
            acccSystemIds = acccSystems.stream().map(DtpRwdObjectResponse::getId).collect(Collectors.toSet());
        }
        if (Objects.nonNull(acatSystemIds)) {
            acatSystemQueryParam.setId(acatSystemIds);
        }
        acatSystemQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SYSTEM).setObjFrom(acccSystemIds));
        List<DtpRwdObjectResponse> acatSystems = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(acatSystemQueryParam));
        if (CollectionUtils.isEmpty(acatSystems)) {
            LogExecutor.instance().debug(log, LOG_TAG, "中央供冷系统下无空调末端系统", Arrays.asList(
                    Pair.of("projectId", projectId),
                    Pair.of("param", acatSystemQueryParam)
            ));
            return Lists.newArrayList();
        }
        // 一个空调系统关联一个中央供冷系统
        Map<String, String> acatSystemIdAndAcccSystemId = new HashMap<>(Math.max(acatSystems.size(), 16));
        Map<String, String> acatSystemIdAndName = new HashMap<>(Math.max(acatSystems.size(), 16));
        Set<String> tempAcatSystemIds = new HashSet<>();
        for (DtpRwdObjectResponse acatSystem : acatSystems) {
            String acatSystemId = acatSystem.getId();
            Set<String> relObjIds = acatSystem.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relObjIds)) {
                relObjIds.forEach(item -> acatSystemIdAndAcccSystemId.put(acatSystemId, item));
            }
            acatSystemIdAndName.put(acatSystemId, acatSystem.getDynamicName());
            tempAcatSystemIds.add(acatSystemId);
        }
        // 查询空调末端系统关联的冷冻水泵控制组
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(tempAcatSystemIds));
        List<DtpRwdObjectResponse> accpcGroups = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isEmpty(accpcGroups)) {
            LogExecutor.instance().debug(log, LOG_TAG, "空调末端系统下无冷冻泵组", Arrays.asList(
                    Pair.of("param", projectId),
                    Pair.of("param", param)
            ));
            return Lists.newArrayList();
        }
        return accpcGroups.stream()
                .map(item -> {
                    // 一个末级泵组关联在一个空调末端系统下
                    String relAcatSystemId = IterableUtils.first(item.getRelObjIds());
                    String acccSystemId = acatSystemIdAndAcccSystemId.get(relAcatSystemId);
                    String acatSystemName = acatSystemIdAndName.get(relAcatSystemId);
                    DtpRwdFreezePumpDTO result = dtpRwdConvert.toFreezePumpDto(item, acccSystemId);
                    // 全局设置统一名称
                    result.setLocalName(realLocalName ? result.getDynamicName() : String.format("%s(%s)", result.getDynamicName(), acatSystemName));
                    return result;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_CIRCUIT, key = "#projectId + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdCircuitDTO> listCircuit(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT);
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isEmpty(content)) {
            return Lists.newArrayList();
        }
        return content.stream()
                .map(dtpRwdConvert::toCircuitDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_CIRCUIT_BY_SPACE_ID, key = "#projectId + #spaceIds?.hashCode() + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdCircuitDTO> listCircuitBySpaceId(
            boolean queryCache, boolean isThrows,
            @NonNull String projectId, @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT);
        if (CollectionUtils.isNotEmpty(spaceIds)) {
            param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(spaceIds));
        }
        return listCircuit(queryCache, isThrows, projectId, param);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_CIRCUIT_BY_CONTROLLER_ID, key = "#projectId + #controllerIds?.hashCode() + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdCircuitDTO> listCircuitByControllerId(
            boolean queryCache, boolean isThrows,
            @NonNull String projectId, @Nullable Collection<String> controllerIds,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT);
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_CONTROLLER).setObjFrom(controllerIds));
        return listCircuit(queryCache, isThrows, projectId, param);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_CIRCUIT_CONTROLLER, key = "#projectId + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdObjectDTO> listCircuitController(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.LIGHTING_CONTROLLER);
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isEmpty(content)) {
            return Lists.newArrayList();
        }
        return content.stream()
                .map(dtpRwdConvert::toCircuitDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_CIRCUIT_CONTROLLER_IN_BY_SPACE_ID, key = "#projectId + #spaceIds?.hashCode() + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdObjectDTO> listCircuitControllerInBySpaceIds(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> spaceIds, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.LIGHTING_CONTROLLER);
        param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjTo(spaceIds));
        return listCircuitController(queryCache, isThrows, projectId, param);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_CIRCUIT_CONTROLLER_BY_CIRCUIT_ID, key = "#projectId + #circuitIds?.hashCode() + #param?.hashCode()", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<DtpRwdObjectDTO> listCircuitControllerByCircuitIds(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> circuitIds, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.LIGHTING_CONTROLLER);
        param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_CONTROLLER).setObjTo(circuitIds));
        return listCircuitController(queryCache, isThrows, projectId, param);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_DISTRIBUTION_CABINET, key = "#projectId + #param?.hashCode()", condition = "#queryCache", sync = true)
    public List<DtpRwdObjectDTO> listDistributionCabinet(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.DISTRIBUTION_CABINET);
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isEmpty(content)) {
            return Lists.newArrayList();
        }
        return content.stream()
                .map(dtpRwdConvert::toCircuitDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.RWD_OBJECT_DISTRIBUTION_CABINET_BY_EQUIPMENT_ID, key = "#projectId + #equipmentIds?.hashCode() + #param?.hashCode()", condition = "#queryCache", sync = true)
    public List<DtpRwdObjectDTO> listDistributionCabinetByEquipmentIds(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> equipmentIds, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setClassCodeEnum(DtpRwdClassCodeEnum.DISTRIBUTION_CABINET);
        param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQ_IN_EQ).setObjTo(equipmentIds));
        return listDistributionCabinet(queryCache, isThrows, projectId, param);
    }

    @Override
    public List<DtpRwdEquipmentDTO> listLocationInsideByEquipmentIds(boolean isThrows, String projectId, Collection<String> equipmentIds, DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        // 复制参数后再设置关系条件, 避免修改调用方传入的共享参数(并行查询下会产生竞态污染)
        DtpRwdObjectQueryParam queryParam = dtpRwdConvert.copy(
                Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new));
        queryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQ_LOCATION_IN_EQ).setObjTo(equipmentIds));
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(queryParam));
        if (CollectionUtils.isEmpty(content)) {
            return Lists.newArrayList();
        }
        return content.stream()
                .map(dtpRwdConvert::toEquipmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DtpRwdEquipmentDTO> listSubmersibleSewagePumpByEquipmentIds(
            boolean isThrows, String projectId, Collection<String> equipmentIds, DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        DtpRwdObjectQueryParam queryParam = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        // 优先查询新关系 MechSubset/Eq2Eq（父设备=FROM，泵=TO，查泵用 relationTo+objFrom）
        DtpRwdObjectQueryParam newRelationParam = dtpRwdConvert.copy(queryParam);
        newRelationParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQ_IN_EQ).setObjFrom(equipmentIds));
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(newRelationParam));
        if (CollectionUtils.isEmpty(content)) {
            // 无结果时回退到旧关系 MechLocation/Inside
            DtpRwdObjectQueryParam oldRelationParam = dtpRwdConvert.copy(queryParam);
            oldRelationParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.EQ_LOCATION_IN_EQ).setObjTo(equipmentIds));
            content = queryObjectResponse(isThrows, projectId,
                    new DtpRwdCriteriaQueryParam<>(oldRelationParam));
        }
        if (CollectionUtils.isEmpty(content)) {
            return Lists.newArrayList();
        }
        return content.stream()
                .map(dtpRwdConvert::toEquipmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SecurityDtpRwdEquipmentDTO> listSecurityEquipment(
            boolean isThrows, @NonNull String projectId,
            @NonNull DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        List<DtpRwdObjectResponse> content = queryObjectResponse(isThrows, projectId,
                new DtpRwdCriteriaQueryParam<>(param));
        if (CollectionUtils.isNotEmpty(content)) {
            return content.stream()
                    .map(dtpRwdConvert::toSecurityEquipmentDto)
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipmentInByFloorIds(
            @Nullable Collection<String> floorIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException {
        param = Optional.ofNullable(param).orElseGet(DtpRwdObjectQueryParam::new);
        param.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_FLOOR).setObjTo(floorIds));
        return listEquipment(param);
    }

    @Override
    public List<DtpRwdFractionalPrecisionRuleDTO> queryInfoPointPeroid(
            @NonNull Set<String> objectIds) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return new ArrayList<>();
        }
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        return query(false, groupCode, projectId, LocaleMessageUtil.getMessage("key_767a0ae5fcac49b1a3e61428efaf0b64"), objectIds,
                () -> dtpRwdClient.queryInfoPointPeroid(commonParam, objectIds), null);
    }

    @Override
    public List<DtpRwdQueryPointListDTO> queryPointList(
            @NonNull Set<String> objectIds, @NonNull Set<String> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return new ArrayList<>();
        }
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        JSONObject jsonObject = new JSONObject()
                .fluentPut("criteria", new JSONObject()
                        .fluentPut("objectIdSet", objectIds)
                        .fluentPut("infoCodeSet", infoCodes)
                        .fluentPut("onlineStatusSet", Arrays.asList(0, 1)));
        return query(false, groupCode, projectId, LocaleMessageUtil.getMessage("key_5792c8d7a9464dfeb909bd7f33d59afa"), objectIds,
                () -> dtpRwdClient.queryPointList(commonParam, jsonObject), null);
    }

    @Override
    public ObjectInstantiateResponse getInstantiate(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull String objectId, @NonNull String infoCode) {
        String groupCode = ContextUtil.getGroupCode();
        ObjectInstantiateQueryParam param = ObjectInstantiateQueryParam.instance(groupCode, projectId);
        param.setObjectId(objectId);
        param.setInfoCode(infoCode);
        return query(isThrows, groupCode, projectId, LocaleMessageUtil.getMessage("key_a63f63bde4854bf78d606dcdfd350301"), param,
                () -> dtpRwdClient.getInstantiate(param), null);
    }

    @Override
    public List<String> listExistsInstantiateObjectIds(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull Collection<ObjectInstantiateQueryParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return Lists.newArrayList();
        }
        String groupCode = ContextUtil.getGroupCode();
        DtpRwdCommonParam dtpRwdCommonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        // 最大查询数量为1000
        List<List<ObjectInstantiateQueryParam>> partitionParams = ListUtils.partition(
                new ArrayList<>(params), ObjectInstantiateQueryParam.MAX_QUERY_SIZE);
        List<ObjectInstantiateResponse> responses = new ArrayList<>();
        for (List<ObjectInstantiateQueryParam> partitionParam : partitionParams) {
            ObjectInstantiateBatchQueryParam queryParam = new ObjectInstantiateBatchQueryParam();
            queryParam.setObjIdInfos(partitionParam);
            responses.addAll(query(isThrows, groupCode, projectId, LocaleMessageUtil.getMessage("key_a63f63bde4854bf78d606dcdfd350301"), partitionParam,
                    () -> dtpRwdClient.listInstantiate(dtpRwdCommonParam, queryParam), ArrayList::new));
        }
        if (CollectionUtils.isEmpty(responses)) {
            return Lists.newArrayList();
        }
        return responses.stream().map(ObjectInstantiateResponse::getObjectId).collect(Collectors.toList());
    }

    @Override
    public Map<String, Set<String>> mapObjectIdAndInstantiateInfoCodes(
            @NonNull String projectId, @NonNull Collection<ObjectInstantiateQueryParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return Maps.newHashMap();
        }
        String groupCode = ContextUtil.getGroupCode();
        DtpRwdCommonParam dtpRwdCommonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        // 最大查询数量为1000
        List<List<ObjectInstantiateQueryParam>> partitionParams = ListUtils.partition(
                new ArrayList<>(params), ObjectInstantiateQueryParam.MAX_QUERY_SIZE);
        List<ObjectInstantiateResponse> responses = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (List<ObjectInstantiateQueryParam> partitionParam : partitionParams) {
            ObjectInstantiateBatchQueryParam queryParam = new ObjectInstantiateBatchQueryParam();
            queryParam.setObjIdInfos(partitionParam);
            futures.add(CompletableFuture.runAsync(() -> responses.addAll(query(false,
                            groupCode, projectId, LocaleMessageUtil.getMessage("key_a63f63bde4854bf78d606dcdfd350301"), partitionParam,
                            () -> dtpRwdClient.listInstantiate(dtpRwdCommonParam, queryParam), ArrayList::new)),
                    BusinessThreadPoolExecutor.ttlExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        if (CollectionUtils.isEmpty(responses)) {
            return Maps.newHashMap();
        }
        return responses.stream().collect(Collectors.groupingBy(ObjectInstantiateResponse::getObjectId,
                Collectors.mapping(ObjectInstantiateResponse::getInfoCode, Collectors.toSet())));
    }

    @Override
    public List<ObjectsExistRule> listObjectsOfExistRuleByClassCodeToTerminal(Collection<DtpRwdObjectsOfExistRuleQueryParam> param, TerminalEnum terminal) {
        return terminal == TerminalEnum.EDGE ? listTwinObjectsOfExistRuleByClassCode(param)
                : listObjectsOfExistRuleByClassCode(param);
    }

    @Override
    public List<ObjectsExistRule> listObjectsOfExistRuleByClassCode(Collection<DtpRwdObjectsOfExistRuleQueryParam> param) {
        if (CollectionUtils.isEmpty(param)) {
            return Lists.newArrayList();
        }
        DtpRwdCommonParam dtpRwdCommonParam = DtpRwdCommonParam.instance();
        return query(false, LocaleMessageUtil.getMessage("key_7beda310769f412487d7c332fd3d759e"), param, () -> dtpRwdClient.listObjectsOfExistRule(dtpRwdCommonParam, param), ArrayList::new);
    }

    @Override
    public List<ObjectsExistRule> listTwinObjectsOfExistRuleByClassCode(Collection<DtpRwdObjectsOfExistRuleQueryParam> param) {
        if (CollectionUtils.isEmpty(param)) {
            return Lists.newArrayList();
        }
        DtpRwdCommonParam dtpRwdCommonParam = DtpRwdCommonParam.instance();
        return query(false, LocaleMessageUtil.getMessage("key_e6645107e0b94a5fa408c13b4d68cd59"), param, () -> dtpRwdClient.listTwinObjectsOfExistRule(dtpRwdCommonParam, param), ArrayList::new);
    }

    @Override
    public Map<String, Map<String, Set<String>>> mapClassCodeObjectIdInfoCodesByClassCode(Collection<DtpRwdObjectsOfExistRuleQueryParam> param) {
        return listObjectsOfExistRuleByClassCode(param).stream().collect(Collectors.groupingBy(ObjectsExistRule::getClassCode, Collectors.groupingBy(
                ObjectsExistRule::getObjectId,
                Collectors.mapping(
                        ObjectsExistRule::getInfoCode,
                        Collectors.toSet()
                )
        )));
    }


    @Override
    public List<DtpRwdTwinObjectRuleResponse> listTwinObjectRule(
            @NonNull DtpRwdTwinObjectRuleQueryParam param) {
        return listTwinObjectRule(Lists.newArrayList(param));
    }

    @Override
    public List<DtpRwdTwinObjectRuleByIdResponse> listTwinObjectRuleById(
            @NonNull DtpRwdTwinObjectRuleByIdQueryParam param) {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        return query(false, LocaleMessageUtil.getMessage("key_75b677002c90490686667c90b6fab8e9"), param,
                () -> dtpRwdClient.listTwinObjectRuleById(commonParam, param), ArrayList::new);
    }

    @Override
    public List<DtpRwdTwinObjectRuleResponse> listTwinObjectRule(
            @NonNull Collection<DtpRwdTwinObjectRuleQueryParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return Lists.newArrayList();
        }
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        return query(false, groupCode, projectId, LocaleMessageUtil.getMessage("key_75b677002c90490686667c90b6fab8e9"), params,
                () -> dtpRwdClient.listTwinObjectRule(commonParam, params), ArrayList::new);
    }

    @Override
    public List<DtpRwdTwinObjectRuleResponse> listTwinObjectRule(
            boolean isThrows, @NonNull String projectId,
            @NonNull Collection<DtpRwdTwinObjectRuleQueryParam> params) {
        String groupCode = ContextUtil.getGroupCode();
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        return query(isThrows, groupCode, projectId, LocaleMessageUtil.getMessage("key_75b677002c90490686667c90b6fab8e9"), params,
                () -> dtpRwdClient.listTwinObjectRule(commonParam, params), ArrayList::new);
    }

    @Override
    public Map<String, Set<String>> mapObjectIdAndTwinObjectRuleInfoCodes(
            @NonNull DtpRwdTwinObjectRuleQueryParam param) {
        return mapObjectIdAndTwinObjectRuleInfoCodes(Lists.newArrayList(param));
    }

    @Override
    public Map<String, Set<String>> mapObjectIdAndTwinObjectRuleInfoCodes(
            @NonNull Collection<DtpRwdTwinObjectRuleQueryParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return Maps.newHashMap();
        }
        String projectId = ContextUtil.getProjectId();
        return listTwinObjectRule(false, projectId, params).stream()
                .collect(Collectors.groupingBy(DtpRwdTwinObjectRuleResponse::getObjectId,
                        Collectors.mapping(DtpRwdTwinObjectRuleResponse::getInfoCode, Collectors.toSet())));
    }

    @Override
    public List<DtpRwdTwinControlObjectDTO> listTwinControlObjects(
            boolean isThrows, @NonNull String projectId,
            @NonNull Collection<String> classCodes) {
        if (CollectionUtils.isEmpty(classCodes)) {
            return Lists.newArrayList();
        }
        String groupCode = ContextUtil.getGroupCode();
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(groupCode, projectId);

        List<DtpRwdTwinControlObjectResponse> responses = query(isThrows, groupCode, projectId, LocaleMessageUtil.getMessage("key_e3f81604996449f3af0b21026172e0ca"), classCodes,
                () -> dtpRwdClient.listTwinControlObjects(commonParam, classCodes), ArrayList::new);
        if (CollectionUtils.isEmpty(responses)) {
            return Lists.newArrayList();
        }
        return responses.stream().map(dtpRwdConvert::toTwinControlObjectDto).collect(Collectors.toList());
    }

    @Override
    public List<EnergyDataResponse> queryIpDataOnMoment(String groupCode, String projectId, List<String> objectIds, List<String> infoCodes) {
        QueryIPDataInPeriodDTO periodDTO = new QueryIPDataInPeriodDTO();

        if (LocalContextStorage.isRemoveDefaultIpType() && this.isLatestDataQueryLogic()) {
            // 壳配置为"查询距离当前最新的数据"：时间窗口取壳配置间隔，固定传 5min 分精度（不传会被持久化服务默认按 15min 处理导致结果不对；传 5min 也可查到更大分精度的数据）
            LocalDateTime now = DateUtils.now();
            periodDTO.setStartTime(now.minusHours(this.getPersistentDataInterval()).format(DateUtils.DTF_SDF_SECOND));
            periodDTO.setEndTime(now.format(DateUtils.DTF_SDF_SECOND));
            periodDTO.setIpType("5min");
        } else {
            // 壳配置为查询同一时刻点数据或未带请求头标识：保持改造前逻辑（前端替换参数优先）
            periodDTO.setIpType("15min");
            this.substitutionParameter(periodDTO);
        }

        List<QueryIPDataInPeriodDTO.ObjectInfo> objectInfos = new ArrayList<>();
        for (String objectId : objectIds) {
            objectInfos.add(new QueryIPDataInPeriodDTO.ObjectInfo(objectId, infoCodes));
        }
        periodDTO.setObjects(objectInfos);
        DmpResult<List<EnergyDataResponse>> listDmpResult = this.abstractDtpPersistServer.queryIPDataOnMoment(groupCode, projectId, null, null, periodDTO);
        return listDmpResult != null && CollectionUtil.isNotEmpty(listDmpResult.getData()) ? listDmpResult.getData() : null;
    }

    @Override
    public List<EnergyDataResponse> queryIPDataInPeriod(String groupCode, String projectId, Set<String> objectIds, Set<String> infoCodes, LocalDateTime startDateTime, LocalDateTime endDateTime, String ipType) {
        EnergyDataQueryParam queryParam = new EnergyDataQueryParam();
        queryParam.setIpType(ipType);
        queryParam.setStartTime(startDateTime.format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
        queryParam.setEndTime(endDateTime.format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
        List<EnergyDataInfoQueryParam> objectInfos = new ArrayList<>();
        for (String objectId : objectIds) {
            EnergyDataInfoQueryParam infoQueryParam = new EnergyDataInfoQueryParam();
            infoQueryParam.setObjectId(objectId);
            infoQueryParam.setInfoCodes(Lists.newArrayList(infoCodes));
            objectInfos.add(infoQueryParam);
        }
        queryParam.setObjects(objectInfos);
        CustomerResponse<List<EnergyDataResponse>> customerResponse = this.abstractDtpPersistServer.listIpDataInPeriod(groupCode, projectId, queryParam);
        return customerResponse != null && CollectionUtil.isNotEmpty(customerResponse.getContent()) ? customerResponse.getContent() : null;
    }


    @Override
    public List<EnergyDataResponse> queryIpDataOnMomentInOneHour(Set<String> objectIds, Set<String> infoCodes) {
        QueryIPDataInPeriodDTO periodDTO = new QueryIPDataInPeriodDTO();

        if (LocalContextStorage.isRemoveDefaultIpType() && this.isLatestDataQueryLogic()) {
            // 壳配置为"查询距离当前最新的数据"：时间窗口取壳配置间隔，固定传 5min 分精度（不传会被持久化服务默认按 15min 处理导致结果不对；传 5min 也可查到更大分精度的数据）
            LocalDateTime now = DateUtils.now();
            periodDTO.setStartTime(now.minusHours(this.getPersistentDataInterval()).format(DateUtils.DTF_SDF_SECOND));
            periodDTO.setEndTime(now.format(DateUtils.DTF_SDF_SECOND));
            periodDTO.setIpType("5min");
        } else {
            // 壳配置为查询同一时刻点数据或未带请求头标识：保持改造前逻辑 15min + (-1h ~ now)（前端替换参数优先）
            periodDTO.setIpType("15min");
            periodDTO.setStartTime(DateUtils.currentTimeOneHourAgo());
            periodDTO.setEndTime(DateUtils.getNowTimeStr());
            this.substitutionParameter(periodDTO);
        }

        List<QueryIPDataInPeriodDTO.ObjectInfo> objectInfos = new ArrayList<>();
        for (String objectId : objectIds) {
            objectInfos.add(new QueryIPDataInPeriodDTO.ObjectInfo(objectId, Lists.newArrayList(infoCodes)));
        }
        periodDTO.setObjects(objectInfos);
        DmpResult<List<EnergyDataResponse>> listDmpResult = this.abstractDtpPersistServer.queryIPDataOnMoment(
                ContextUtil.getGroupCode(), ContextUtil.getProjectId(), null, null, periodDTO);
        return listDmpResult != null && CollectionUtil.isNotEmpty(listDmpResult.getData()) ? listDmpResult.getData()
                : null;
    }

    @Override
    public List<EnergyDataResponse> queryIpDataOnMoment(@NonNull Set<String> objectIds, @NonNull Set<String> infoCodes,
                                                        @NonNull DtpRwdPersistenceTimeTypeEnum ipType,
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        QueryIPDataInPeriodDTO periodDTO = new QueryIPDataInPeriodDTO();
        periodDTO.setIpType(ipType.value());
        if (startTime != null) {
            periodDTO.setStartTime(startTime.format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
        }
        if (endTime != null) {
            periodDTO.setEndTime(endTime.format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
        }

        periodDTO.setDataStatus(1);
        List<QueryIPDataInPeriodDTO.ObjectInfo> objectInfos = new ArrayList<>();
        for (String objectId : objectIds) {
            objectInfos.add(new QueryIPDataInPeriodDTO.ObjectInfo(objectId, Lists.newArrayList(infoCodes)));
        }
        periodDTO.setObjects(objectInfos);
        DmpResult<List<EnergyDataResponse>> listDmpResult = this.dtpPersistServerClient.queryIPDataOnMoment(
                ContextUtil.getGroupCode(), ContextUtil.getProjectId(), null, null, periodDTO);
        if (Objects.isNull(listDmpResult) || CollectionUtils.isEmpty(listDmpResult.getData())) {
            return Collections.emptyList();
        }
        return listDmpResult.getData();
    }

    /**
     * 处理请求头参数转到iot查询参数中
     *
     * @param periodDTO 查询入参
     */
    private void substitutionParameter(QueryIPDataInPeriodDTO periodDTO) {
        Map<String, Object> extra = ContextUtil.getContext().getExtra();
        if (CommonConst.isNeedReplacedSystem(extra)) {
            periodDTO.setStartTime(DateUtils.transferDateFormat(extra.get(CommonConst.IPDATA_START_TIME).toString(),
                    DateUtils.SDF_SECOND, DateUtils.SDFSECOND));
            periodDTO.setEndTime(DateUtils.transferDateFormat(extra.get(CommonConst.IPDATA_END_TIME).toString(),
                    DateUtils.SDF_SECOND, DateUtils.SDFSECOND));
            periodDTO.setIpType(extra.get(CommonConst.IPTYPE).toString());
            periodDTO.setDataStatus(1);
        }
    }

    @Override
    public List<DtpRwdSpaceDTO> queryInterventionSpace(boolean queryCache, boolean isThrows, String projectId, List<String> targetRwdObjectTypeIds, DtpRwdObjectTypeEnum targetRwdObjectType, DtpRwdClassCodeEnum filterClassCode) {
        List<DtpRwdObjectDTO> objectDtoList = listObject(true, true, projectId, new DtpRwdObjectQueryParam().setClassCodeEnum(filterClassCode));
        if (CollectionUtils.isEmpty(objectDtoList)) {
            return Lists.newArrayList();
        }
        String groupCode = ContextUtil.getGroupCode();
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setGroupCode(groupCode);
        queryParam.setProjectId(projectId);
        Set<String> equipmentIds = objectDtoList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        return listSpaceInByEquipmentId(true, true, projectId, equipmentIds, queryParam);
    }


    /**
     * 查询厨房油烟机的聚合业务信息
     *
     * @return 厨房油烟机聚合业务信息
     */

    @Override
    public List<KitchenExhaustResponse> queryKitchenExhaust() {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        return query(false, groupCode, projectId, LocaleMessageUtil.getMessage("key_861ff43a53c44fa1b19accfbffe7dd49"), new Object(),
                () -> dtpRwdClient.queryKitchenExhaust(commonParam), ArrayList::new);
    }

    @Override
    public Map<ChillerTypeSub, Map<String, List<String>>> queryChillerGroupById(String groupCode, String projectId, String objectId, String classCode) {
        try {
            if (DtpRwdClassCodeEnum.ACCPC_GROUP.getValue().equals(classCode)) {
                DmpResult<Map<String, List<String>>> dmpResult = dtpRwdClient.queryPumpGroup(groupCode, projectId, objectId, null, null);
                if (dmpResult != null && MapUtils.isNotEmpty(dmpResult.getData())) {
                    Map<ChillerTypeSub, Map<String, List<String>>> resultMap = new HashMap<>();
                    resultMap.put(ChillerTypeSub.PUMP_GROUP_BASE, dmpResult.getData());
                    return resultMap;
                }
            } else {
                DmpResult<Map<ChillerTypeSub, Map<String, List<String>>>> dmpResult = dtpRwdClient.querySysGroup(groupCode, projectId, objectId, null, null);
                return dmpResult == null || MapUtils.isEmpty(dmpResult.getData()) ? new HashMap<>() : dmpResult.getData();
            }
        } catch (Exception e) {
            log.error("台数分类查询异常", e);
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, Map<ChillerSize, List<String>>> queryPumpGroups(Set<String> pumpIds) {
        if(CollectionUtils.isEmpty(pumpIds)){
            return Collections.emptyMap();
        }
        DtpRwdCommonParam commonParam = new DtpRwdCommonParam();
        commonParam.setGroupCode(ContextUtil.getGroupCode());
        String systemId = ContextUtil.getProjectId();
        Map<String, Map<String, List<String>>> response = query(false, "冷冻水泵控制组接口返回的大小泵清单", pumpIds,
                () -> this.dtpRwdClient.queryPumpGroups(commonParam, pumpIds), Collections::emptyMap);
        Map<String, Map<ChillerSize, List<String>>> result = new LinkedHashMap<>();
        response.forEach((groupId, size2ChillerId) -> {
            if (MapUtils.isEmpty(size2ChillerId)) {
                return;
            }
            size2ChillerId.forEach((size, chillerIds) -> {
                ChillerSize chillerSize = ChillerSize.getByCode(size);
                if (Objects.isNull(chillerSize)) {
                    logProjectInstance().warn("无法解析冷机大小", Arrays.asList(
                            Pair.of("systemId", systemId),
                            Pair.of("size", size)
                    ));
                    return;
                }
                if (CollectionUtils.isEmpty(chillerIds)) {
                    return;
                }
                MapExecutor.dynamicPutAll(result, groupId, chillerSize, chillerIds, LinkedHashMap::new, ArrayList::new);
            });
        });
        return result;
    }


    /**
     * 查询物理世界关联对象
     *
     * @param isThrows        是否在查询失败|数据为空时抛出异常
     * @param projectId       项目ID
     * @param param           查询参数
     * @param successCallback 查询成功时的回调方法
     * @param defaultResult   默认返回值
     * @param <T>             返回值类型
     * @return 物理世界关联对象
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到对象
     * @since v5.1.0
     */
    private <T> T queryRelationObjectResponse(
            boolean isThrows, @NonNull String projectId,
            @NonNull DtpRwdRelationQueryParam param,
            @NonNull Function<CustomerResponse<List<DtpRwdRelationResponse>>, T> successCallback,
            @NonNull Supplier<T> defaultResult)
            throws FeignException, BusinessException {
        if (Objects.isNull(param.getFromCriteria()) || Objects.isNull(param.getFromCriteria().getValid())) {
            param.setFromCriteria(Optional.ofNullable(param.getFromCriteria())
                    .orElseGet(DtpRwdObjectQueryParam::new));
        }
        if (Objects.isNull(param.getToCriteria()) || Objects.isNull(param.getToCriteria().getValid())) {
            param.setToCriteria(Optional.ofNullable(param.getToCriteria())
                    .orElseGet(DtpRwdObjectQueryParam::new));
        }
        String groupCode = ContextUtil.getGroupCode();
        DtpRwdCommonParam dtpRwdCommonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        CustomerResponse<List<DtpRwdRelationResponse>> relationResponse;
        try {
            relationResponse = dtpRwdClientPageWrapper.listRelationObject(dtpRwdCommonParam, param);
        } catch (FeignException e) {
            LogExecutor.rpc(groupCode, param, null).error(log, LOG_TAG, "查询对象关系实例失败",
                    Collections.singletonList(Pair.of("projectId", projectId)));
            if (isThrows) {
                throw e;
            }
            return defaultResult.get();
        }
        List<DtpRwdRelationResponse> relations = relationResponse.getContent();
        if (CollectionUtils.isNotEmpty(relations)) {
            if (log().isTraceEnabled()) {
                LogExecutor.rpc(groupCode, param, relationResponse).trace(log, LOG_TAG, "查询关联对象实例成功",
                        Collections.singletonList(Pair.of("projectId", projectId)));
            }
            return successCallback.apply(relationResponse);
        }
        LogExecutor.rpc(groupCode, param, relationResponse).warn(log, LOG_TAG, "未查询到关联对象实例",
                Collections.singletonList(Pair.of("projectId", projectId)));
        if (isThrows) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_1b6a793b34f2417cb6580a7219b22c2d"));
        }
        return defaultResult.get();
    }

    /**
     * 查询物理世界对象
     *
     * @param param 查询参数
     * @return 物理世界对象
     * @throws FeignException RPC调用异常
     * @since 5.241102.0
     */
    private List<DtpRwdObjectResponse> queryObjectResponse(
            @NonNull DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> param)
            throws FeignException, BusinessException {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> queryParam = checkAndSetParam(projectId, param);
        DtpRwdObjectQueryParam criteria = queryParam.getCriteria();
        if (criteria.invalid()) {
            logProjectInstance().warn("物理世界参数无效", Collections.singletonList(
                    Pair.of("queryParam", criteria)));
            return Lists.newArrayList();
        }
        String instanceName = parseInstanceName(criteria.getClassCode());
        DtpRwdCommonParam dtpRwdCommonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        return query(false, groupCode, projectId, instanceName, criteria,
                () -> dtpRwdClientPageWrapper.listObject(dtpRwdCommonParam, queryParam), ArrayList::new);
    }

    /**
     * 查询物理世界对象
     *
     * @param isThrows  是否在查询失败|数据为空时抛出异常
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 物理世界对象
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到对象
     * @since v5.1.0
     */
    private List<DtpRwdObjectResponse> queryObjectResponse(
            boolean isThrows, @NonNull String projectId,
            @NonNull DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> param)
            throws FeignException, BusinessException {
        String groupCode = ContextUtil.getGroupCode();
        DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> queryParam = checkAndSetParam(projectId, param);
        DtpRwdObjectQueryParam criteria = queryParam.getCriteria();
        if (criteria.invalid()) {
            logProjectInstance().warn("物理世界参数无效", Collections.singletonList(
                    Pair.of("queryParam", criteria)));
            return Lists.newArrayList();
        }
        String instanceName = parseInstanceName(criteria.getClassCode());
        DtpRwdCommonParam dtpRwdCommonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        return query(isThrows, groupCode, projectId, instanceName, criteria,
                () -> dtpRwdClientPageWrapper.listObject(dtpRwdCommonParam, queryParam), ArrayList::new);
    }

    /**
     * 查询物理世界对象数量
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 物理世界对象
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到对象
     * @since 5.240602.0
     */
    private Integer queryObjectCountResponse(
            @NonNull String projectId,
            @NonNull DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> param)
            throws FeignException, BusinessException {
        String groupCode = ContextUtil.getGroupCode();
        DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> queryParam = checkAndSetParam(projectId, param);
        DtpRwdObjectQueryParam criteria = queryParam.getCriteria();
        if (criteria.invalid()) {
            logProjectInstance().warn("物理世界参数无效", Collections.singletonList(
                    Pair.of("queryParam", criteria)));
            return 0;
        }
        queryParam.setOnlyCount(true);
        String instanceName = parseInstanceName(criteria.getClassCode());
        DtpRwdCommonParam dtpRwdCommonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        return queryCount(true, groupCode, projectId, instanceName, criteria,
                () -> dtpRwdClientPageWrapper.listObject(dtpRwdCommonParam, queryParam), () -> 0);
    }

    /**
     * 检查并设置查询参数
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 查询参数
     * @since 5.240602.0
     */
    private DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> checkAndSetParam(
            @NonNull String projectId,
            @NonNull DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> param) {
        String groupCode = ContextUtil.getGroupCode();
        DtpRwdObjectQueryParam criteria = param.getCriteria();
        if (CollectionUtils.isEmpty(criteria.getGroupCode())) {
            criteria.setGroupCode(groupCode);
        }
        if (CollectionUtils.isEmpty(criteria.getProjectId())) {
            criteria.setProjectId(projectId);
        }
        Supplier<DtpRwdObjectQueryParam> copyCriteriaSupplier = () -> dtpRwdConvert.copy(criteria);
        DtpRwdObjectQueryParam copyCriteria = null;
        if (Objects.nonNull(criteria.getPageQuery())) {
            copyCriteria = copyCriteriaSupplier.get();
            param.setPage(new DtpRwdPageQuery()
                    .setPage(copyCriteria.getPageQuery().getPageNo())
                    .setSize(copyCriteria.getPageQuery().getPageSize()));
            param.setOnlyCount(copyCriteria.getPageQuery().getOnlyCount());
            copyCriteria.setPageQuery(null);
            param.setCriteria(copyCriteria);
        }
        if (CollectionUtils.isNotEmpty(criteria.getOrderCustoms())) {
            copyCriteria = Optional.ofNullable(copyCriteria).orElseGet(copyCriteriaSupplier);
            param.setOrderCustoms(copyCriteria.getOrderCustoms());
            copyCriteria.setOrderCustoms(null);
            param.setCriteria(copyCriteria);
        }
        if (CollectionUtils.isNotEmpty(criteria.getWithColumns())) {
            copyCriteria = Optional.ofNullable(copyCriteria).orElseGet(copyCriteriaSupplier);
            param.setWithColumns(copyCriteria.getWithColumns());
            copyCriteria.setWithColumns(null);
            param.setCriteria(copyCriteria);
        }
        return param;
    }

    @Override
    public Logger log() {
        return log;
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }

    /**
     * 判断壳配置是否为"查询距离当前最新的数据"逻辑
     * <p>壳配置 persistent_data_query_logic 语义：same_time=查询同一个时刻点的数据（保持默认分精度）；latest=查询距离当前最新的数据（固定传 5min 分精度）。
     * 配置缺失、为 same_time 或读取异常时均回退 same_time 语义，走改造前查询逻辑。
     * 该配置仅控制查询逻辑门控，与时间配置 persistent_data_time_interval 相互独立（间隔仅在 latest 语义下消费）</p>
     *
     * @return true 表示壳配置为 latest（调用方需按壳配置间隔查询且固定传 5min 分精度）
     * @since v7.260901.0
     */
    private boolean isLatestDataQueryLogic() {
        try {
            QueryShellConfigByInfoCodeParam param = new QueryShellConfigByInfoCodeParam();
            param.setProjectId(ContextUtil.getProjectId());
            param.setCode(ShellConfigItemEnum.PERSISTENT_DATA_QUERY_LOGIC.getCode());
            ShellConfigItem item = this.shellConfigService.queryByInfoCode(ContextUtil.getGroupCode(), param);
            String value = (item != null && StringUtils.isNotBlank(item.getValue()))
                    ? item.getValue()
                    : ShellConfigItemEnum.PERSISTENT_DATA_QUERY_LOGIC.getDefaultValues();
            return "latest".equals(value);
        } catch (Exception e) {
            this.logProjectInstance().error("读取壳配置持久化数据查询逻辑失败，回退默认", e);
            return false;
        }
    }

    /**
     * 获取"距离当前的时间间隔"配置值（小时）
     * <p>配置缺失或异常时回退默认值 1h；范围 1-168 由壳配置保存时校验，消费端不再判断。
     * 查询时间范围固定取该配置，与分精度配置相互独立</p>
     *
     * @return 间隔小时数
     * @since v7.260901.0
     */
    private int getPersistentDataInterval() {
        try {
            QueryShellConfigByInfoCodeParam param = new QueryShellConfigByInfoCodeParam();
            param.setProjectId(ContextUtil.getProjectId());
            param.setCode(ShellConfigItemEnum.PERSISTENT_DATA_TIME_INTERVAL.getCode());
            ShellConfigItem item = this.shellConfigService.queryByInfoCode(ContextUtil.getGroupCode(), param);
            String value = (item != null && StringUtils.isNotBlank(item.getValue()))
                    ? item.getValue()
                    : ShellConfigItemEnum.PERSISTENT_DATA_TIME_INTERVAL.getDefaultValues();
            return Integer.parseInt(value);
        } catch (Exception e) {
            this.logProjectInstance().error("读取壳配置距离当前的时间间隔失败，回退默认", e);
            return Integer.parseInt(ShellConfigItemEnum.PERSISTENT_DATA_TIME_INTERVAL.getDefaultValues());
        }
    }
}
