package com.persagy.apccontrol.basic.service.common.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.EquipmentServiceSpaceBySpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.EquipmentServiceSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.SensorByServiceSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigByObjectQueryParam;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigObjectByFiltersQueryParam;
import com.persagy.apccontrol.basic.model.vo.common.DTPRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.common.EquipmentServiceSpaceVO;
import com.persagy.apccontrol.basic.model.vo.common.SensorByServiceEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.common.ServiceSpaceBySpaceVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeObjectGroupVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.service.common.SpaceCommonService;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName 薛涛
 * @Author
 * @Date 2024/7/23 16:10
 * @Version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SpaceCommonServiceImpl implements SpaceCommonService {

    private final DtpRwdClientService dtpRwdClientService;

    private final InfoCodeConfigService infoCodeConfigService;
    
    
    @Override
    public List<SensorByServiceEquipmentVO> listSensorByServiceSpace(SensorByServiceSpaceQueryParam param) {
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = dtpRwdClientService.listEquipmentSensorBySpaceIds(
                Lists.newArrayList(param.getSpaceId()), new DtpRwdObjectQueryParam());
        if (CollectionUtils.isEmpty(dtpRwdEquipmentDTOS)) {
            return Lists.newArrayList();
        }
        List<SensorByServiceEquipmentVO> sensorByServiceEquipmentVOList = Lists.newArrayList();
        List<String> objectIds = Lists.newArrayList();
        for (DtpRwdEquipmentDTO dtpRwdEquipmentDTO : dtpRwdEquipmentDTOS) {
            SensorByServiceEquipmentVO sensorByServiceEquipmentVO = new SensorByServiceEquipmentVO();
            sensorByServiceEquipmentVO.setClassCode(dtpRwdEquipmentDTO.getClassCode());
            sensorByServiceEquipmentVO.setId(dtpRwdEquipmentDTO.getId());
            sensorByServiceEquipmentVO.setLocalId(dtpRwdEquipmentDTO.getLocalId());
            sensorByServiceEquipmentVO.setLocalName(dtpRwdEquipmentDTO.getLocalName());
            sensorByServiceEquipmentVO.setName(dtpRwdEquipmentDTO.getName());
            sensorByServiceEquipmentVO.setObjType(dtpRwdEquipmentDTO.getObjType());
            sensorByServiceEquipmentVO.setProjectId(dtpRwdEquipmentDTO.getProjectId());
            sensorByServiceEquipmentVOList.add(sensorByServiceEquipmentVO);
            objectIds.add(dtpRwdEquipmentDTO.getId());
        }
        //设备所在空间
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = dtpRwdClientService.listSpaceInByEquipmentId(false, false,
                param.getProjectId(), objectIds, new DtpRwdObjectQueryParam());
        //设备所在空间map
        Map<String, List<DtpRwdSpaceDTO>> dtpRwdSpaceDTOMap = new HashMap<>(16);


        if (CollectionUtils.isNotEmpty(dtpRwdSpaceDTOS)) {

            for (DtpRwdSpaceDTO dtpRwdSpaceDTO : dtpRwdSpaceDTOS) {
                Set<String> relObjIds = dtpRwdSpaceDTO.getRelObjIds();
                if (CollectionUtils.isNotEmpty(relObjIds)) {
                    for (String relObjId : relObjIds) {
                        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOSMap = dtpRwdSpaceDTOMap.get(relObjId);
                        if (CollectionUtils.isEmpty(dtpRwdSpaceDTOSMap)) {
                            dtpRwdSpaceDTOSMap = new ArrayList<>();
                            dtpRwdSpaceDTOSMap.add(dtpRwdSpaceDTO);
                            dtpRwdSpaceDTOMap.put(relObjId, dtpRwdSpaceDTOSMap);
                        } else {
                            dtpRwdSpaceDTOSMap.add(dtpRwdSpaceDTO);
                            dtpRwdSpaceDTOMap.put(relObjId, dtpRwdSpaceDTOSMap);
                        }
                    }
                }
            }
            DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
            dtpRwdObjectQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.EQUIPMENT_IN_FLOOR).setObjFrom(objectIds));
            //查设备所在楼层
            List<DtpRwdObjectDTO> dtpRwdObjectFloorDTOS = dtpRwdClientService
                    .listObject(false, false, param.getProjectId(), dtpRwdObjectQueryParam);
            Map<String, DtpRwdObjectDTO> dtpRwdObjectFloorMap = new HashMap<>();
            for (DtpRwdObjectDTO dtpRwdObjectFloorDTO : dtpRwdObjectFloorDTOS) {
                Set<String> relObjIds = dtpRwdObjectFloorDTO.getRelObjIds();
                for (String relObjId : relObjIds) {
                    dtpRwdObjectFloorMap.put(relObjId, dtpRwdObjectFloorDTO);
                }
            }
            dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
            dtpRwdObjectQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.EQUIPMENT_IN_BUILDING).setObjFrom(objectIds));
            //查设备所在建筑
            List<DtpRwdObjectDTO> dtpRwdObjectBuildingDTOS = dtpRwdClientService
                    .listObject(false, false, param.getProjectId(), dtpRwdObjectQueryParam);

            Map<String, DtpRwdObjectDTO> dtpRwdObjectBuildingMap = new HashMap<>();
            for (DtpRwdObjectDTO dtpRwdObjectBuildingDTO : dtpRwdObjectBuildingDTOS) {
                Set<String> relObjIds = dtpRwdObjectBuildingDTO.getRelObjIds();
                for (String relObjId : relObjIds) {
                    dtpRwdObjectBuildingMap.put(relObjId, dtpRwdObjectBuildingDTO);
                }
            }

            for (SensorByServiceEquipmentVO sensorByServiceEquipmentVO : sensorByServiceEquipmentVOList) {
                String id = sensorByServiceEquipmentVO.getId();
                DtpRwdObjectDTO dtpRwdObjectFloorDTO = dtpRwdObjectFloorMap.get(id);
                DtpRwdObjectDTO dtpRwdObjectBuildingDTO = dtpRwdObjectBuildingMap.get(id);
                if (Objects.nonNull(dtpRwdObjectBuildingDTO)) {
                    sensorByServiceEquipmentVO.setBuildingId(dtpRwdObjectBuildingDTO.getId());
                    sensorByServiceEquipmentVO.setBuildingLocalName(dtpRwdObjectBuildingDTO.getLocalName());
                }
                if (Objects.nonNull(dtpRwdObjectFloorDTO)) {
                    sensorByServiceEquipmentVO.setFloorId(dtpRwdObjectFloorDTO.getId());
                    sensorByServiceEquipmentVO.setFloorLocalName(dtpRwdObjectFloorDTO.getLocalName());
                }
                List<DtpRwdSpaceDTO> dtpRwdSpaceList = dtpRwdSpaceDTOMap.get(id);
                if (CollectionUtils.isNotEmpty(dtpRwdSpaceList)) {
                    List<DTPRwdSpaceVO> spaces = new ArrayList<>();
                    for (DtpRwdSpaceDTO dtpRwdSpaceDTO : dtpRwdSpaceList) {
                        DTPRwdSpaceVO dTPRwdSpaceNew = new DTPRwdSpaceVO();
                        BeanUtil.copyProperties(dtpRwdSpaceDTO, dTPRwdSpaceNew);
                        spaces.add(dTPRwdSpaceNew);
                    }
                    sensorByServiceEquipmentVO.setSpaces(spaces);
                }

            }
        }

        return sensorByServiceEquipmentVOList;
    }

    @Override
    public List<EquipmentServiceSpaceVO> listEquipmentServiceSpaceByEquipmentId(EquipmentServiceSpaceQueryParam param) {

        //设备服务空间
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = dtpRwdClientService.listSpaceServiceByEquipmentId(
                Lists.newArrayList(param.getEquipmentId()), new DtpRwdObjectQueryParam());
        List<EquipmentServiceSpaceVO> equipmentServiceSpaceVOList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(dtpRwdSpaceDTOS)) {
            return equipmentServiceSpaceVOList;
        }
        //空间的楼层建筑信息
        Map<String, DtpRwdObjectDTO> dtpRwdObjectDTOMap = new HashMap<>(16);
        Set<String> allObject = new HashSet<>(16);
        Set<String> spaceIds = new HashSet<>();
        for (DtpRwdSpaceDTO dtpRwdSpaceDTO : dtpRwdSpaceDTOS) {
            EquipmentServiceSpaceVO equipmentServiceSpaceVO = new EquipmentServiceSpaceVO();
            BeanUtil.copyProperties(dtpRwdSpaceDTO, equipmentServiceSpaceVO);
            allObject.add(dtpRwdSpaceDTO.getFloorId());
            allObject.add(dtpRwdSpaceDTO.getBuildingId());
            spaceIds.add(dtpRwdSpaceDTO.getId());
            equipmentServiceSpaceVOList.add(equipmentServiceSpaceVO);
        }
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(false, false,
                param.getProjectId(), new DtpRwdObjectQueryParam().setId(allObject));
        if (CollectionUtils.isNotEmpty(dtpRwdObjectDTOS)) {
            for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
                dtpRwdObjectDTOMap.put(dtpRwdObjectDTO.getId(), dtpRwdObjectDTO);
            }
        }
        //查询空间信息点
        InfoCodeConfigByObjectQueryParam infoCodeConfigByObjectQueryParam = new InfoCodeConfigByObjectQueryParam();
        infoCodeConfigByObjectQueryParam.setRunningMonitorType(param.getRunningMonitorSecondType().toRunningMonitorTypeEnum());
        infoCodeConfigByObjectQueryParam.setTemplateType(param.getTemplateCode());
        infoCodeConfigByObjectQueryParam.setObjectIds(spaceIds);
        List<InfoCodeObjectGroupVO> infoCodeObjectGroupVOS = infoCodeConfigService.listInfoCodeConfigsByObject(infoCodeConfigByObjectQueryParam);
        Map<String, InfoCodeObjectGroupVO> equipmentInfoCodeObjectGroupVOMap = new HashMap<>();
        for (InfoCodeObjectGroupVO infoCodeObjectGroupVO : infoCodeObjectGroupVOS) {
            equipmentInfoCodeObjectGroupVOMap.put(infoCodeObjectGroupVO.getId(), infoCodeObjectGroupVO);
        }
        for (EquipmentServiceSpaceVO equipmentServiceSpaceVO : equipmentServiceSpaceVOList) {
            String id = equipmentServiceSpaceVO.getId();
            String floorId = equipmentServiceSpaceVO.getFloorId();
            String buildingId = equipmentServiceSpaceVO.getBuildingId();
            DtpRwdObjectDTO dtpRwdObjectFloorDTO = dtpRwdObjectDTOMap.get(floorId);
            DtpRwdObjectDTO dtpRwdObjectBuildingDTO = dtpRwdObjectDTOMap.get(buildingId);

            if (Objects.nonNull(dtpRwdObjectBuildingDTO)) {
                equipmentServiceSpaceVO.setBuildingId(dtpRwdObjectBuildingDTO.getId());
                equipmentServiceSpaceVO.setBuildingLocalName(dtpRwdObjectBuildingDTO.getLocalName());
            }
            if (Objects.nonNull(dtpRwdObjectFloorDTO)) {
                equipmentServiceSpaceVO.setFloorId(dtpRwdObjectFloorDTO.getId());
                equipmentServiceSpaceVO.setFloorLocalName(dtpRwdObjectFloorDTO.getLocalName());
            }
            InfoCodeObjectGroupVO infoCodeObjectGroupVO = equipmentInfoCodeObjectGroupVOMap.get(id);
            if (Objects.nonNull(infoCodeObjectGroupVO)) {
                equipmentServiceSpaceVO.setRelInfoCodeGroupConfigs(infoCodeObjectGroupVO.getRelInfoCodeGroupConfigs());
            }

        }
        return equipmentServiceSpaceVOList;
    }

    @Override
    @Deprecated
    public List<ServiceSpaceBySpaceVO> listEquipmentServiceSpaceBySpaceId(EquipmentServiceSpaceBySpaceQueryParam param) {

        //服务该空间的设备(查询信息点自带)
        //查询信息点
        InfoCodeConfigObjectByFiltersQueryParam equipmentInfoCodeConfigByObjectQueryParam = new InfoCodeConfigObjectByFiltersQueryParam();
        equipmentInfoCodeConfigByObjectQueryParam.setRunningMonitorType(param.getRunningMonitorSecondType().toRunningMonitorTypeEnum());
        // fix. 解决通风空间详情关联设备无数据问题, 因需要读取配置表, 通风详情未按照二级类型进行配置, 该方法不再维护, 需要废弃
        // equipmentInfoCodeConfigByObjectQueryParam.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
        equipmentInfoCodeConfigByObjectQueryParam.setTemplateType(param.getTemplateCode());
        equipmentInfoCodeConfigByObjectQueryParam.setServiceSpaceIds(Sets.newHashSet(param.getSpaceId()));
        equipmentInfoCodeConfigByObjectQueryParam.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        List<InfoCodeObjectGroupVO> infoCodeObjectGroupVOS = infoCodeConfigService
                .listInfoCodeConfigsByObjectFilters(equipmentInfoCodeConfigByObjectQueryParam);

        List<ServiceSpaceBySpaceVO> equipmentServiceSpaceBySpaceVOList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(infoCodeObjectGroupVOS)) {
            return equipmentServiceSpaceBySpaceVOList;
        }
        List<String> objects = Lists.newArrayList();
        for (InfoCodeObjectGroupVO infoCodeObjectGroupVO : infoCodeObjectGroupVOS) {
            ServiceSpaceBySpaceVO equipmentServiceSpaceBySpaceVO = new ServiceSpaceBySpaceVO();
            BeanUtil.copyProperties(infoCodeObjectGroupVO, equipmentServiceSpaceBySpaceVO);
            equipmentServiceSpaceBySpaceVOList.add(equipmentServiceSpaceBySpaceVO);
            objects.add(equipmentServiceSpaceBySpaceVO.getId());
        }
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_FLOOR).setObjFrom(objects));
        //查设备所在楼层
        List<DtpRwdFloorDTO> dtpRwdObjectFloorDTOS = dtpRwdClientService
                .listFloor(false, false, param.getProjectId(), dtpRwdObjectQueryParam);
        Map<String, DtpRwdFloorDTO> dtpRwdObjectFloorMap = new HashMap<>();
        for (DtpRwdFloorDTO dtpRwdObjectFloorDTO : dtpRwdObjectFloorDTOS) {
            Set<String> relObjIds = dtpRwdObjectFloorDTO.getRelObjIds();
            if (relObjIds != null) {
                for (String relObjId : relObjIds) {
                    dtpRwdObjectFloorMap.put(relObjId, dtpRwdObjectFloorDTO);
                }
            }
        }

        //设备服务的空间
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = dtpRwdClientService.listSpaceServiceByEquipmentId(objects, new DtpRwdObjectQueryParam());
        //设备所在空间map
        Map<String, List<DTPRwdSpaceVO>> dtpRwdSpaceDTOMap = new HashMap<>(16);
        for (DtpRwdSpaceDTO dtpRwdSpaceDTO : dtpRwdSpaceDTOS) {
            Set<String> relObjIds = dtpRwdSpaceDTO.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relObjIds)) {
                for (String relObjId : relObjIds) {
                    List<DTPRwdSpaceVO> dtpRwdSpaceDTOSMap = dtpRwdSpaceDTOMap.get(relObjId);
                    if (CollectionUtils.isEmpty(dtpRwdSpaceDTOSMap)) {
                        dtpRwdSpaceDTOSMap = new ArrayList<>();
                        DTPRwdSpaceVO dTPRwdSpaceVO = new DTPRwdSpaceVO();
                        BeanUtil.copyProperties(dtpRwdSpaceDTO, dTPRwdSpaceVO);
                        dtpRwdSpaceDTOSMap.add(dTPRwdSpaceVO);
                        dtpRwdSpaceDTOMap.put(relObjId, dtpRwdSpaceDTOSMap);
                    } else {
                        DTPRwdSpaceVO dTPRwdSpaceVO = new DTPRwdSpaceVO();
                        BeanUtil.copyProperties(dtpRwdSpaceDTO, dTPRwdSpaceVO);
                        dtpRwdSpaceDTOSMap.add(dTPRwdSpaceVO);
                        dtpRwdSpaceDTOMap.put(relObjId, dtpRwdSpaceDTOSMap);
                    }
                }
            }
        }
        //DTPRwdSpaceVO
        for (ServiceSpaceBySpaceVO equipmentServiceSpaceBySpaceVO : equipmentServiceSpaceBySpaceVOList) {
            String objectId = equipmentServiceSpaceBySpaceVO.getId();
            List<DTPRwdSpaceVO> dtpRwdSpaceVOS = dtpRwdSpaceDTOMap.get(objectId);
            equipmentServiceSpaceBySpaceVO.setServiceSpace(dtpRwdSpaceVOS);
            DtpRwdFloorDTO dtpRwdObjectDTO = dtpRwdObjectFloorMap.get(objectId);
            if (Objects.nonNull(dtpRwdObjectDTO)) {
                equipmentServiceSpaceBySpaceVO.setFloorId(dtpRwdObjectDTO.getId());
                equipmentServiceSpaceBySpaceVO.setFloorLocalName(dtpRwdObjectDTO.getLocalName());
                equipmentServiceSpaceBySpaceVO.setFloorSequenceId(dtpRwdObjectDTO.getFloorSequenceId());
                equipmentServiceSpaceBySpaceVO.setBuildingId(dtpRwdObjectDTO.getBuildingId());
            }
        }
        List<String> sequences = new ArrayList<>();
        AirTerminalService.ALL_EQUIPMENT_CLASS_CODES.forEach(item -> sequences.add(item.getValue()));
        // 按照业务需求排序：设备类名称 → 建筑名称 → 楼层序号(由高到低) → 设备名称
        equipmentServiceSpaceBySpaceVOList.sort(Comparator
                // 1. 首先按设备类名称排序
                .comparingInt((ServiceSpaceBySpaceVO o) -> sequences.indexOf(o.getClassCode()))
                // 2. 每个设备类下按建筑名称排序
                .thenComparing(ServiceSpaceBySpaceVO::getBuildingId, Comparator.nullsLast(Comparator.naturalOrder()))
                // 3. 每个建筑下按楼层序号由高到低排序
                .thenComparing(ServiceSpaceBySpaceVO::getFloorSequenceId,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                // 4. 每个楼层下按设备名称排序
                .thenComparing(ServiceSpaceBySpaceVO::getLocalName, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        
        return equipmentServiceSpaceBySpaceVOList;
    }

    @Override
    public List<DtpRwdBuildingDTO> listObjectServiceSpaceByObjectId(String groupCode, String projectId, String objectId) {
        // 1.校验对象是否存在
        List<DtpRwdObjectDTO> objectList = !StrUtil.isAllNotBlank(groupCode, projectId, objectId) ? null : dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setId(objectId));
        DtpRwdObjectDTO objectInfo = CollectionUtils.isEmpty(objectList) ? null : objectList.get(0);
        if (objectInfo == null) {
            return Lists.newArrayList();
        }
        String objType = objectInfo.getObjType();

        List<DtpRwdSpaceDTO> relationSpaces = null;
        if (DtpRwdObjectTypeEnum.SYSTEM.getValue().equals(objType)) {
            // 查询系统服务的空间
            relationSpaces = dtpRwdClientService.listSpace(new DtpRwdObjectQueryParam().setClassCodeEnum(DtpRwdClassCodeEnum.SPACE)
                    .setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE).setObjFrom(objectId)));
        } else {
            // 不论是控制组还是单设备，均查询设备所服务的空间关系
            relationSpaces = dtpRwdClientService.listSpace(new DtpRwdObjectQueryParam().setClassCodeEnum(DtpRwdClassCodeEnum.SPACE)
                    .setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjFrom(objectId)));
        }
        if (CollectionUtils.isEmpty(relationSpaces)) {
            return Lists.newArrayList();
        }

        // 查询楼层
        DtpRwdObjectQueryParam floorParam = new DtpRwdObjectQueryParam().setId(relationSpaces.stream().map(DtpRwdSpaceDTO::getFloorId).collect(Collectors.toSet()));
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(false, false, projectId, floorParam);

        // 查询建筑
        DtpRwdObjectQueryParam buildingParam = new DtpRwdObjectQueryParam().setId(relationSpaces.stream().map(DtpRwdSpaceDTO::getBuildingId).collect(Collectors.toSet()));
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(false, false, projectId, buildingParam);

        // 返回树形结构
        Map<String, List<DtpRwdSpaceDTO>> floorIdSpaces = relationSpaces.stream().collect(Collectors.groupingBy(DtpRwdSpaceDTO::getFloorId));
        Map<String, List<DtpRwdFloorDTO>> buildingIdFloors = floors.stream().collect(Collectors.groupingBy(DtpRwdFloorDTO::getBuildingId));
        for (DtpRwdBuildingDTO building : buildings) {
            String buildingId = building.getId();
            List<DtpRwdFloorDTO> tempFloors = buildingIdFloors.get(buildingId);
            if (CollectionUtils.isNotEmpty(tempFloors)) {
                for (DtpRwdFloorDTO tempFloor : tempFloors) {
                    List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = floorIdSpaces.get(tempFloor.getId());
                    if (CollectionUtils.isEmpty(dtpRwdSpaceDTOS)) {
                        tempFloor.setSpaces(Lists.newArrayList());
                    } else {
                        dtpRwdSpaceDTOS.sort(Comparator.comparing(e -> StringUtils.isNotBlank(e.getLocalName()) ? e.getLocalName() : ""));
                        tempFloor.setSpaces(dtpRwdSpaceDTOS);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(tempFloors)) {
                tempFloors.sort(Comparator.comparingInt(DtpRwdFloorDTO::getFloorSequenceId).reversed());
                building.setFloors(tempFloors);
            } else {
                building.setFloors(Lists.newArrayList());
            }
        }
        buildings.sort(Comparator.comparing(e -> StringUtils.isNotBlank(e.getLocalName()) ? e.getLocalName() : ""));
        return buildings;
    }
}
