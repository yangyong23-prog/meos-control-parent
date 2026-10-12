package com.persagy.apccontrol.basic.service.parking.impl;

import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;


import cn.hutool.core.date.StopWatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.parking.ChargingRateOptionalEnum;
import com.persagy.apccontrol.basic.model.enumtype.parking.ParkingSpaceCountTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.parking.ParkingSpaceStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.parking.ParkingSpaceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.SecurityEquipmentStatusEnum;
import com.persagy.apccontrol.basic.model.param.parking.ParkingSpaceStatisticsQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.parking.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.parking.ParkingService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum.PARKING;
import static com.persagy.framework.common.exception.enums.ErrorCodeConstants.USER_ERROR_A0400;

/**
 * 停车场统计服务实现。
 * <p>
 * 接口方法：
 * <ul>
 *     <li>{@link #getParkingSpaceGridStatistics(ParkingSpaceStatisticsQueryParam, Boolean)} 项目车位网格图统计</li>
 *     <li>{@link #getParkingSpaceStatistics(ParkingSpaceStatisticsQueryParam)} 车位统计（PROJECT 级实例化规则感知 + 车辆总数）</li>
 *     <li>{@link #getFloorParkingSpaceStatistics(ParkingSpaceStatisticsQueryParam)} 楼层车位统计</li>
 *     <li>{@link #getParkingEquipStatistics(ParkingSpaceStatisticsQueryParam)} 停车场设备统计</li>
 *     <li>{@link #logTag()} 日志标签</li>
 * </ul>
 *
 * @author JiYinchuan
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ParkingServiceImpl implements ParkingService {

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractCommonServer abstractCommonServer;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final MidCalendarClientService midCalendarClientService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;


    @Override
    public List<ParkingSpaceGridStatisticsVO> getParkingSpaceGridStatistics(ParkingSpaceStatisticsQueryParam param, Boolean isConvert) {

        List<ParkingSpaceGridStatisticsVO> buildingStatistics = new ArrayList<>();
        if (!DemandInstanceTypeEnum.PROJECT.value().equals(param.getInstanceType().getValue())) {
            throw new BusinessException(USER_ERROR_A0400);
        }

        StopWatch stopWatch = StopWatch.create("停车场网格统计");
        String projectId = ContextUtil.getProjectId();
        // 查询出项目下能停车的车位空间
        stopWatch.start("停车场空进查询");
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = getParkingSpaceList(null);
        stopWatch.stop();
        if (CollectionUtils.isEmpty(dtpRwdSpaceDTOS)) {
            logProjectInstance(ContextUtil.getProjectId()).warn("项目下没有停车空间");
            return buildingStatistics;
        }
        // 空间按照楼层分组
        Map<String, List<DtpRwdSpaceDTO>> floorIdAndSpaceListMap = dtpRwdSpaceDTOS.stream()
                .collect(Collectors.groupingBy(DtpRwdSpaceDTO::getFloorId));
        List<DtpRwdFloorDTO> dtpRwdFloorDTOS = dtpRwdClientService.listFloor(false, false, projectId,
                new DtpRwdObjectQueryParam().setId(floorIdAndSpaceListMap.keySet()));
        if (CollectionUtils.isEmpty(dtpRwdFloorDTOS)) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_dcddb938e2604ea5b9b6ac3c04cc958e"));
        }
        Map<String, DtpRwdFloorDTO> floorIdAndFloorEntity = dtpRwdFloorDTOS.stream().collect(Collectors.toMap(DtpRwdFloorDTO::getId, p -> p));
        Map<FloorParkingSpaceStatisticVO, List<DtpRwdSpaceDTO>> floorEntityAndSpaceListMap = floorIdAndSpaceListMap.entrySet().stream().
                collect(Collectors.toMap(p -> {
                            DtpRwdFloorDTO floor = floorIdAndFloorEntity.get(p.getKey());
                            FloorParkingSpaceStatisticVO vo = new FloorParkingSpaceStatisticVO();
                            vo.setFloorName(floor.getLocalName());
                            vo.setFloorId(floor.getId());
                            return vo;
                        },
                        Map.Entry::getValue));
        fillFloorParkingSpaceStatistic(floorEntityAndSpaceListMap);
        Map<String, List<DtpRwdFloorDTO>> buildingIdAndFloorList = dtpRwdFloorDTOS.stream().collect(Collectors.groupingBy(DtpRwdFloorDTO::getBuildingId));
        Map<String, FloorParkingSpaceStatisticVO> floorIdAndFloorStatistics = floorEntityAndSpaceListMap.keySet().stream().collect(Collectors.toMap(FloorParkingSpaceStatisticVO::getFloorId, p -> p));
        List<DtpRwdBuildingDTO> buildingDTOS = dtpRwdClientService.listBuilding(false, false, param.getInstanceId(), new DtpRwdObjectQueryParam().setId(buildingIdAndFloorList.keySet()));
        if (CollectionUtils.isEmpty(buildingDTOS)) {
            return buildingStatistics;
        }
        for (DtpRwdBuildingDTO buildingDTO : buildingDTOS) {
            ParkingSpaceGridStatisticsVO parkingSpaceGridStatisticsVo = new ParkingSpaceGridStatisticsVO();
            parkingSpaceGridStatisticsVo.setBuildingId(buildingDTO.getId());
            parkingSpaceGridStatisticsVo.setBuildingName(buildingDTO.getLocalName());
            List<DtpRwdFloorDTO> floorDTOS = buildingIdAndFloorList.get(buildingDTO.getId());
            List<FloorParkingSpaceStatisticVO> floorParkingSpaceStatistics = new ArrayList<>();
            if (CollectionUtils.isEmpty(floorDTOS)) {
                continue;
            }
            floorDTOS.forEach(floorDTO -> {
                FloorParkingSpaceStatisticVO floorParkingSpaceStatisticVO = floorIdAndFloorStatistics.get(floorDTO.getId());
                if (isConvert) {
                    convertResult(floorParkingSpaceStatisticVO);
                }
                floorParkingSpaceStatistics.add(floorParkingSpaceStatisticVO);
            });
            parkingSpaceGridStatisticsVo.setFloorParkingSpaceStatistics(floorParkingSpaceStatistics);
            buildingStatistics.add(parkingSpaceGridStatisticsVo);
        }

        logProjectInstance(ContextUtil.getProjectId()).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return buildingStatistics;
    }

    void convertResult(FloorParkingSpaceStatisticVO vo) {
        if (vo == null) {
            return;
        }
        if (CollectionUtils.isEmpty(vo.getSpaceParkIngSpaceStatistics())) {
            return;
        }
        for (SpaceParkIngSpaceStatisticVO parkIngSpaceStatisticsInSpace : vo.getSpaceParkIngSpaceStatistics()) {
            if (parkIngSpaceStatisticsInSpace == null) {
                continue;
            }
            Map<String, Integer> parkIngSpaceStatistics = parkIngSpaceStatisticsInSpace.getParkIngSpaceStatistics();

            if (parkIngSpaceStatistics == null || parkIngSpaceStatistics.size() < 1) {
                continue;
            }
            Map<String, Integer> tempMap = new HashMap<>();
            parkIngSpaceStatistics.forEach((parkingSpaceTypeEnum, integer) -> {
                ParkingSpaceCountTypeEnum parkingSpaceCountTypeEnum = ParkingSpaceTypeEnum.contVertParkingSpaceCountTypeEnum(parkingSpaceTypeEnum);
                if (parkingSpaceCountTypeEnum != null) {
                    tempMap.put(parkingSpaceCountTypeEnum.value(), integer);
                }
                parkIngSpaceStatisticsInSpace.setParkIngSpaceStatistics(tempMap);
            });
        }

    }


    @Override
    public Map<String, Integer> getParkingSpaceStatistics(ParkingSpaceStatisticsQueryParam param) {
        Map<String, Integer> result = new LinkedHashMap<>();
        if (DemandInstanceTypeEnum.PROJECT.equals(param.getInstanceType())) {
            List<ParkingSpaceGridStatisticsVO> parkingSpaceGridStatistics = this.getParkingSpaceGridStatistics(param, false);
            // 车位类型统计：仅在有停车空间时聚合，无停车空间直接跳过（保持改造前行为）
            if (CollectionUtils.isNotEmpty(parkingSpaceGridStatistics)) {
                result.putAll(this.aggregateParkingSpaceTypeStatistics(parkingSpaceGridStatistics));
            }
            // 空闲车位：存在任一停车位有 parkingStatus 实例化规则 → 保留聚合统计值；全无规则 → 项目对象 availableParkingSpaceNumber 兜底
            this.fillVacantParkingSpaceByProjectRule(result);
            // 车辆总数：项目对象 carNum 有实例化规则才返回
            this.fillCarTotalByProjectRule(result);
        }
        if (DemandInstanceTypeEnum.FLOOR.equals(param.getInstanceType())) {
            // FLOOR 级保持改造前行为：基于 CFPKPS parkingStatus 聚合，不执行项目级实例化规则逻辑
            SpaceParkIngSpaceStatisticVO vo = new SpaceParkIngSpaceStatisticVO();
            vo.initData();
            result.putAll(vo.getParkIngSpaceStatistics());
            List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = this.getParkingSpaceList(Collections.singletonList(param.getInstanceId()));
            if (CollectionUtils.isEmpty(dtpRwdSpaceDTOS)) {
                return result;
            }
            Map<FloorParkingSpaceStatisticVO, List<DtpRwdSpaceDTO>> floorEntityAndSpaceListMap = new HashMap<>();
            FloorParkingSpaceStatisticVO floorParkingSpaceStatisticVO = new FloorParkingSpaceStatisticVO();
            floorParkingSpaceStatisticVO.setFloorId(param.getInstanceId());
            floorEntityAndSpaceListMap.put(floorParkingSpaceStatisticVO, dtpRwdSpaceDTOS);
            this.fillFloorParkingSpaceStatistic(floorEntityAndSpaceListMap);
            FloorParkingSpaceStatisticVO floorParkingSpaceStatistics = floorEntityAndSpaceListMap.keySet().stream().findFirst().orElse(null);
            this.fillParkingSpaceTypeStatisticByFloor(vo.getParkIngSpaceStatistics(), floorParkingSpaceStatistics);
            result.putAll(vo.getParkIngSpaceStatistics());
        }
        return result;
    }

    /**
     * 聚合网格统计为 6 类车位类型统计
     * <p>
     * 以 initData 后的 6 类车位 key（初值 0）作为聚合表：fillParkingSpaceTypeStatisticByFloor 仅在 key 已存在时累加
     *
     * @param gridStatistics 网格统计结果
     * @return 车位类型 → 数量
     * @since v7.260901.0
     */
    private Map<String, Integer> aggregateParkingSpaceTypeStatistics(List<ParkingSpaceGridStatisticsVO> gridStatistics) {
        SpaceParkIngSpaceStatisticVO aggregateVo = new SpaceParkIngSpaceStatisticVO();
        aggregateVo.initData();
        Map<String, Integer> parkingSpaceTypeStatistics = aggregateVo.getParkIngSpaceStatistics();
        gridStatistics.forEach(buildingStatistics ->
                buildingStatistics.getFloorParkingSpaceStatistics().forEach(floorStatistics ->
                        this.fillParkingSpaceTypeStatisticByFloor(parkingSpaceTypeStatistics, floorStatistics)));
        return parkingSpaceTypeStatistics;
    }

    /**
     * 空闲车位：实例化规则感知。
     * <p>
     * 存在任一停车位（CFPKPS）有 parkingStatus 实例化规则时保留聚合统计值（全部无 IOT 数据时为 0）；
     * 所有停车位均无规则时改为项目对象 availableParkingSpaceNumber 兜底：项目对象无规则不返字段，
     * 有规则但无 IOT 值返回 null（前端 --）。
     *
     * @param result 项目级车位统计结果
     * @since v7.260901.0
     */
    private void fillVacantParkingSpaceByProjectRule(Map<String, Integer> result) {
        // 与网格统计同口径：停车空间下的 CFPKPS
        Set<String> parkingSpaceIds = this.getProjectParkingSpaceIds();
        if (CollectionUtils.isNotEmpty(parkingSpaceIds)) {
            Map<String, Set<String>> classCodeAndObjectIds = Collections.singletonMap(
                    DtpRwdClassCodeEnum.CFPKPS.value(), parkingSpaceIds);
            Map<String, Set<String>> ruleInfoCodeMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                    classCodeAndObjectIds, Collections.singleton(InfoCodeEnum.PARKING_STATUS.value()));
            if (MapUtils.isNotEmpty(ruleInfoCodeMap)) {
                return;
            }
        }
        // 所有停车位均无规则：覆盖聚合值，改为项目对象信息点
        result.remove(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value());
        if (MapUtils.isEmpty(this.projectObjectRuleInfoCodes(InfoCodeEnum.AVAILABLE_PARKING_SPACE_NUMBER))) {
            return;
        }
        result.put(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value(),
                this.getProjectInfoCodeValue(InfoCodeEnum.AVAILABLE_PARKING_SPACE_NUMBER));
    }

    /**
     * 车辆总数：项目对象 carNum 有实例化规则才返回 CAR_TOTAL，无规则不返字段，有规则无 IOT 值返回 null（前端 --）。
     *
     * @param result 项目级车位统计结果
     * @since v7.260901.0
     */
    private void fillCarTotalByProjectRule(Map<String, Integer> result) {
        if (MapUtils.isEmpty(this.projectObjectRuleInfoCodes(InfoCodeEnum.CAR_NUM))) {
            return;
        }
        result.put(CAR_TOTAL, this.getProjectInfoCodeValue(InfoCodeEnum.CAR_NUM));
    }

    /**
     * 查询项目对象是否存在指定信息点的实例化规则
     *
     * @param infoCodeEnum 信息点
     * @return 存在规则的信息点编码集合（空表示无规则）
     * @since v7.260901.0
     */
    private Map<String, Set<String>> projectObjectRuleInfoCodes(InfoCodeEnum infoCodeEnum) {
        return abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                Collections.singletonMap(DtpRwdClassCodeEnum.PROJECT.value(),
                        Collections.singleton(ContextUtil.getProjectId())),
                Collections.singleton(infoCodeEnum.value()));
    }

    /**
     * 获取项目对象指定信息点的实时值（60 分钟窗口），无值或非数字返回 null
     *
     * @param infoCodeEnum 信息点
     * @return 信息点数值，无数据时为 null
     * @since v7.260901.0
     */
    private Integer getProjectInfoCodeValue(InfoCodeEnum infoCodeEnum) {
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                Collections.singleton(ContextUtil.getProjectId()), Lists.newArrayList(infoCodeEnum.value()));
        if (CollectionUtils.isEmpty(infoCodeValues)) {
            return null;
        }
        for (InfoCodeValueVO infoCodeValueVO : infoCodeValues) {
            Object value = infoCodeValueVO.getInfoCodeValue();
            if (value == null) {
                continue;
            }
            // IOT 值可能为 Double 或字符串，统一转数值并四舍五入；非数字视为无值（返回 null，前端 --）
            try {
                return (int) Math.round(Double.parseDouble(String.valueOf(value)));
            } catch (NumberFormatException e) {
                logProjectInstance(ContextUtil.getProjectId()).warn("项目对象信息点值非数字，按无值处理",
                        Arrays.asList(Pair.of("infoCode", infoCodeEnum.value()), Pair.of("value", value)));
            }
        }
        return null;
    }

    /**
     * 获取项目下停车位 id 集合（与网格统计同口径：停车空间下的 CFPKPS）
     *
     * @return 停车位 id 集合，无停车空间时为空
     * @since v7.260901.0
     */
    private Set<String> getProjectParkingSpaceIds() {
        Set<String> parkingSpaceIds = new HashSet<>();
        List<DtpRwdSpaceDTO> parkingSpaces = this.getParkingSpaceList(null);
        if (CollectionUtils.isEmpty(parkingSpaces)) {
            return parkingSpaceIds;
        }
        Map<String, Set<String>> spaceIdAndParkingSpaceIds = this.getParkingSpaceBySpaceIds(
                parkingSpaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet()));
        spaceIdAndParkingSpaceIds.values().forEach(parkingSpaceIds::addAll);
        return parkingSpaceIds;
    }

    @Override
    public List<ParkingFloorParkingSpaceStatisticsVO> getFloorParkingSpaceStatistics(ParkingSpaceStatisticsQueryParam param) {
        List<ParkingFloorParkingSpaceStatisticsVO> floorParkingSpaceStatisticsVO = new ArrayList<>();
        if (!DemandInstanceTypeEnum.FLOOR.equals(param.getInstanceType())) {
            return floorParkingSpaceStatisticsVO;
        }
        StopWatch stopWatch = StopWatch.create("停车场楼层车位统计");
        stopWatch.start("查询楼层下的车位空间");
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = getParkingSpaceList(Collections.singletonList(param.getInstanceId()));
        stopWatch.stop();
        if (CollectionUtils.isEmpty(dtpRwdSpaceDTOS)) {
            return floorParkingSpaceStatisticsVO;
        }
        Map<FloorParkingSpaceStatisticVO, List<DtpRwdSpaceDTO>> floorEntityAndSpaceListMap = new HashMap<>();
        FloorParkingSpaceStatisticVO floorParkingSpaceStatisticVO = new FloorParkingSpaceStatisticVO();
        floorParkingSpaceStatisticVO.setFloorId(param.getInstanceId());
        floorEntityAndSpaceListMap.put(floorParkingSpaceStatisticVO, dtpRwdSpaceDTOS);

        stopWatch.start("填充楼层下的车位空间统计");
        fillFloorParkingSpaceStatistic(floorEntityAndSpaceListMap);
        stopWatch.stop();
        // 取出楼层的统计
        FloorParkingSpaceStatisticVO floorParkingSpaceStatistics = floorEntityAndSpaceListMap.keySet().stream().findFirst().orElse(null);
        if (floorParkingSpaceStatistics == null) {
            return floorParkingSpaceStatisticsVO;
        }
        // 楼层下的空间统计列表
        List<SpaceParkIngSpaceStatisticVO> spaceParkIngSpaceStatisticsInSpace = floorParkingSpaceStatistics.getSpaceParkIngSpaceStatistics();
        if (CollectionUtils.isEmpty(spaceParkIngSpaceStatisticsInSpace)) {
            return floorParkingSpaceStatisticsVO;
        }
        // 空间下不同类型的车位统计列表
        spaceParkIngSpaceStatisticsInSpace.forEach(spaceParkIngSpaceStatisticVO -> {
            ParkingFloorParkingSpaceStatisticsVO parkingFloorParkingSpaceStatisticsVO = new ParkingFloorParkingSpaceStatisticsVO();
            parkingFloorParkingSpaceStatisticsVO.setSpaceId(spaceParkIngSpaceStatisticVO.getSpaceId());
            parkingFloorParkingSpaceStatisticsVO.setSpaceName(spaceParkIngSpaceStatisticVO.getSpaceName());
            parkingFloorParkingSpaceStatisticsVO.setCount(spaceParkIngSpaceStatisticVO.sum());
            // 空间下不同类型的车位统计列表
            parkingFloorParkingSpaceStatisticsVO.setList(convertParkingSpaceStatisticsVO(spaceParkIngSpaceStatisticVO.getParkIngSpaceStatistics()));
            floorParkingSpaceStatisticsVO.add(parkingFloorParkingSpaceStatisticsVO);
        });
        logProjectInstance(ContextUtil.getProjectId()).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return floorParkingSpaceStatisticsVO;
    }

    @Override
    public List<ParkingEquipStatisticsVO> getParkingEquipStatistics(ParkingSpaceStatisticsQueryParam param) {
        List<ParkingEquipStatisticsVO> parkingEquipStatisticsVOList = new ArrayList<>();
        String projectId = ContextUtil.getProjectId();
        Set<String> returnInfoCode = Sets.newHashSet("id");
        StopWatch stopWatch = StopWatch.create("getParkingEquipStatistics");
        Map<ParkingEquipStatisticsVO, List<DtpRwdEquipmentDTO>> equipStatisticsMap = getEquipmentStatistics(projectId, PARKING_EQUIP_TYPE_LIST, param.getInstanceId(), param.getInstanceType(), returnInfoCode, null);
        if (equipStatisticsMap == null || equipStatisticsMap.size() < 1) {
            return parkingEquipStatisticsVOList;
        }
        equipStatisticsMap.forEach((equipmentStatisticsVo, equips) -> {
            Set<String> equipIds = equips.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
            stopWatch.start("查寻异常事件信息");
            // 异常事件
            fillEquipmentRiskStatusStatistics(equipIds, equipmentStatisticsVo);
            stopWatch.stop();
            stopWatch.start("在离线统计");
            // 离线/在线状态统计
            fillSecurityStatusStatistics(projectId, InfoCodeEnum.ONLINE_STATUS, equipIds, equipmentStatisticsVo, SecurityEquipmentStatusEnum::getOnlineStatus);
            stopWatch.stop();
            logProjectInstance(projectId).debug("响应时间记录",
                    Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
            // equipmentStatisticsVo.setCount(equipmentStatisticsVo.getCount() + equipmentStatisticsVo.getCount());
            parkingEquipStatisticsVOList.add(equipmentStatisticsVo);
        });

        fillEquipmentStatusNameStatistics(parkingEquipStatisticsVOList);

        return parkingEquipStatisticsVOList;
    }


    void fillEquipmentStatusNameStatistics(List<ParkingEquipStatisticsVO> equipmentStatistics) {
        if (CollectionUtils.isEmpty(equipmentStatistics)) {
            return;
        }
        equipmentStatistics.forEach(equipmentStatisticsVo -> {
            List<ParkingEquipStatusStatisticsVO> statusList = equipmentStatisticsVo.getStatusList();
            if (CollectionUtils.isEmpty(statusList)) {
                return;
            }
            statusList.forEach(statusStatisticsVo -> {
                if (statusStatisticsVo != null && statusStatisticsVo.getObjectCode() != null) {
                    SecurityEquipmentStatusEnum status = statusStatisticsVo.getObjectCode();
                    statusStatisticsVo.setObjectName(status.getReasonPhrase());
                }
            });
        });
    }


    public void fillEquipmentRiskStatusStatistics(Set<String> equipIds, ParkingEquipStatisticsVO equipmentStatisticsVo) {
        if (CollectionUtils.isNotEmpty(equipIds)) {
            Map<String, List<ExceptionEventBaseInfoVO>> riskEventMap =
                    this.abstractCommonServer.mapRiskEvent(equipIds, null);
            ParkingEquipStatusStatisticsVO riskEventStatistics = new ParkingEquipStatusStatisticsVO();
            riskEventStatistics.setObjectCode(SecurityEquipmentStatusEnum.EXCEPTION);
            if (!riskEventMap.isEmpty()) {
                riskEventStatistics.setCount(riskEventMap.size());
            }
            equipmentStatisticsVo.getStatusList().add(riskEventStatistics);
        }

    }

    @SuppressWarnings("unchecked")
    public void fillSecurityStatusStatistics(String projectId, InfoCodeEnum infoCodeEnum, Set<String> equipIds,
                                             ParkingEquipStatisticsVO equipStatisticsVo,
                                             Function<Object, SecurityEquipmentStatusEnum> getEquipStatusFunction) {
        if (CollectionUtils.isEmpty(equipIds) || infoCodeEnum == null || equipStatisticsVo == null) {
            return;
        }
        StopWatch stopWatch = StopWatch.create("查寻iot数据");
        stopWatch.start("查询iot信息点");
        List<InfoCodeValueVO> statusInfoCodes = abstractIotDataCacheServer.listInfoCodeValueInOneHour(equipIds, Lists.newArrayList(infoCodeEnum.value()));
        stopWatch.stop();
        if (CollectionUtils.isEmpty(statusInfoCodes)) {
            if (infoCodeEnum.getCode().equals(InfoCodeEnum.ONLINE_STATUS.getCode())) {
                ParkingEquipStatusStatisticsVO offLine = new ParkingEquipStatusStatisticsVO();
                offLine.setObjectCode(SecurityEquipmentStatusEnum.OFFLINE);
                offLine.setObjectName(SecurityEquipmentStatusEnum.OFFLINE.getReasonPhrase());
                equipStatisticsVo.getStatusList().add(offLine);
            }
            return;
        }
        Map<Object, List<InfoCodeValueVO>> statusGroupMap = statusInfoCodes.stream().filter(infoCodeValueVO -> infoCodeValueVO.getInfoCodeValue() != null)
                .collect(Collectors.groupingBy(InfoCodeValueVO::getInfoCodeValue));
        if (statusGroupMap.isEmpty()) {
            return;
        }
        statusGroupMap.forEach((status, infoCodeValueVOS) -> {
            ParkingEquipStatusStatisticsVO statusStatistics = new ParkingEquipStatusStatisticsVO();
            Set<String> ids = infoCodeValueVOS.stream().map(InfoCodeValueVO::getObjectId).collect(Collectors.toSet());
            if (status != null) {
                // 只统计离线状态的
                if (InfoCodeEnum.ONLINE_STATUS.value().equals(infoCodeEnum.getCode())) {
                    statusStatistics.setObjectCode(SecurityEquipmentStatusEnum.OFFLINE);
                    if (getEquipStatusFunction.apply(status.toString()).value().equals(SecurityEquipmentStatusEnum.OFFLINE.value())) {
                        statusStatistics.setCount(ids.size());
                        statusStatistics.setObjectCode(getEquipStatusFunction.apply(status.toString()));
                        equipStatisticsVo.getStatusList().add(statusStatistics);
                    } else {
                        statusStatistics.setCount(0);
                        if (statusGroupMap.size() == 1) {
                            equipStatisticsVo.getStatusList().add(statusStatistics);
                        }
                    }

                } else {
                    statusStatistics.setCount(ids.size());
                    statusStatistics.setObjectCode(getEquipStatusFunction.apply(status.toString()));
                    equipStatisticsVo.getStatusList().add(statusStatistics);
                }
            }
        });
        if (equipStatisticsVo.getStatusList() == null) {
            equipStatisticsVo.setStatusList(new ArrayList<>());
        }
        logProjectInstance(projectId).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
    }


    /**
     * @param projectId      项目id
     * @param classCodeList  对象类列表
     * @param returnInfoCode 查询bdtp返回的字段会与classCode
     * @param queryInfo      查询bdtp是的信息点过滤
     * @return key 是按照给定设备类统计，value 物理世界对象列表
     */

    public Map<ParkingEquipStatisticsVO, List<DtpRwdEquipmentDTO>> getEquipmentStatistics(@NonNull String projectId, @NonNull List<String> classCodeList, @NonNull String instanceId, DemandInstanceTypeEnum
            instanceType, @Nullable Set<String> returnInfoCode, @Nullable Map<String, Object> queryInfo) {
        StopWatch stopWatch = StopWatch.create("getEquipmentStatistics");
        stopWatch.start("查询前的操作");
        Map<ParkingEquipStatisticsVO, List<DtpRwdEquipmentDTO>> equipmentStatisticsMap = new HashMap<>(16);
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCode(classCodeList);
        if (queryInfo != null && !queryInfo.isEmpty()) {
            queryParam.setInfoCodes(queryInfo);
        }
        if (CollectionUtils.isNotEmpty(returnInfoCode)) {
            Set<String> returnInfoCodes = Sets.newHashSet(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
            returnInfoCodes.addAll(returnInfoCode);
            queryParam.setWithColumns(returnInfoCodes);
        }
        stopWatch.stop();
        stopWatch.start("查寻rwd实体类信息");
        long beginTime = System.currentTimeMillis();
        List<DtpRwdEquipmentDTO> equipmentDTOS = new ArrayList<>();
        if (instanceType.equals(DemandInstanceTypeEnum.PROJECT)) {
            equipmentDTOS = dtpRwdClientService.listEquipment(queryParam);
        }
        if (instanceType.equals(DemandInstanceTypeEnum.FLOOR)) {
            equipmentDTOS = dtpRwdClientService.listEquipmentInByFloorIds(Collections.singletonList(instanceId), queryParam);
        }
        log.info("查询设备耗时：" + (System.currentTimeMillis() - beginTime));
        stopWatch.stop();
        stopWatch.start("查询后数据处理");
        if (CollectionUtils.isNotEmpty(equipmentDTOS)) {
            Map<String, List<DtpRwdEquipmentDTO>> groupedByClassCode = equipmentDTOS.stream()
                    .collect(Collectors.groupingBy(DtpRwdEquipmentDTO::getClassCode));
            groupedByClassCode.forEach((classCode, entity) -> {
                ParkingEquipStatisticsVO eqStatisticsVo = new ParkingEquipStatisticsVO();

                String classCodeName = dtpKnowledgeClientService.getClassCodeName(false, false, classCode);
                classCodeName = StringUtils.isEmpty(classCodeName) ? DtpRwdClassCodeEnum.analyze(classCode).getReasonPhrase() : classCodeName;

                eqStatisticsVo.setObjectName(classCodeName);
                eqStatisticsVo.setObjectCode(classCode);
                eqStatisticsVo.setCount(entity.size());
                equipmentStatisticsMap.put(eqStatisticsVo, entity);
            });
        }
        stopWatch.stop();
        logProjectInstance(projectId).info("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return equipmentStatisticsMap;
    }


    public List<ParkingSpaceStatisticsVO> convertParkingSpaceStatisticsVO
            (Map<String, Integer> parkIngSpaceStatisticsMap) {

        List<ParkingSpaceStatisticsVO> parkingSpaceStatistics = new ArrayList<>();
        if (parkIngSpaceStatisticsMap == null || parkIngSpaceStatisticsMap.isEmpty()) {
            return parkingSpaceStatistics;
        }
        parkIngSpaceStatisticsMap.forEach((parkingSpaceTypeEnum, count) -> {

            if (parkingSpaceTypeEnum.equals(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value())) {
                ParkingSpaceStatisticsVO vo = new ParkingSpaceStatisticsVO();
                vo.setParkSpaceType(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE);
                vo.setCount(count);
                parkingSpaceStatistics.add(vo);
            }
            if (parkingSpaceTypeEnum.equals(ParkingSpaceTypeEnum.VACANT_CHARGING_PARKING_SPACE.value())) {
                ParkingSpaceStatisticsVO vo = new ParkingSpaceStatisticsVO();
                vo.setParkSpaceType(ParkingSpaceTypeEnum.VACANT_CHARGING_PARKING_SPACE);
                vo.setCount(count);
                parkingSpaceStatistics.add(vo);
            }
            if (parkingSpaceTypeEnum.equals(ParkingSpaceTypeEnum.VACANT_FAST_CHARGING_PARKING_SPACE.value())) {
                ParkingSpaceStatisticsVO vo = new ParkingSpaceStatisticsVO();
                vo.setParkSpaceType(ParkingSpaceTypeEnum.VACANT_FAST_CHARGING_PARKING_SPACE);
                vo.setCount(count);
                parkingSpaceStatistics.add(vo);
            }
        });
        return parkingSpaceStatistics;
    }

    public void fillParkingSpaceTypeStatisticByFloor(Map<String, Integer> parkingSpaceTypeStatistics, FloorParkingSpaceStatisticVO floorStatistics) {
        if (parkingSpaceTypeStatistics.isEmpty() || floorStatistics == null) {
            return;
        }

        if (parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.PARKING_SPACE.value()) != null) {
            int parkingSpaceCount = parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.PARKING_SPACE.value()) + floorStatistics.getParkIngSpaceCount();
            parkingSpaceTypeStatistics.put(ParkingSpaceTypeEnum.PARKING_SPACE.value(), parkingSpaceCount);
        }

        if (parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()) != null) {
            int vacantParkingSpaceCount = parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value()) + floorStatistics.getVacantParkIngSpaceCount();
            parkingSpaceTypeStatistics.put(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value(), vacantParkingSpaceCount);
        }
        if (parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.CHARGING_PARKING_SPACE.value()) != null) {
            int chargingParkingSpaceCount = parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.CHARGING_PARKING_SPACE.value()) + floorStatistics.getChargingParkingSpaceCount();
            parkingSpaceTypeStatistics.put(ParkingSpaceTypeEnum.CHARGING_PARKING_SPACE.value(), chargingParkingSpaceCount);
        }

        if (parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.VACANT_CHARGING_PARKING_SPACE.value()) != null) {
            int vacantChargingParkingSpaceCount = parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.VACANT_CHARGING_PARKING_SPACE.value()) + floorStatistics.getVacantChargingParkingSpace();
            parkingSpaceTypeStatistics.put(ParkingSpaceTypeEnum.VACANT_CHARGING_PARKING_SPACE.value(), vacantChargingParkingSpaceCount);

        }

        if (parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.FAST_CHARGING_PARKING_SPACE.value()) != null) {
            int fastChargingParkingSpaceCount = parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.FAST_CHARGING_PARKING_SPACE.value()) + parkingSpaceCountByType(ParkingSpaceTypeEnum.FAST_CHARGING_PARKING_SPACE, floorStatistics);
            parkingSpaceTypeStatistics.put(ParkingSpaceTypeEnum.FAST_CHARGING_PARKING_SPACE.value(), fastChargingParkingSpaceCount);
        }

        if (parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.VACANT_FAST_CHARGING_PARKING_SPACE.value()) != null) {
            int vacantFastChargingParkingSpaceCount = parkingSpaceTypeStatistics.get(ParkingSpaceTypeEnum.VACANT_FAST_CHARGING_PARKING_SPACE.value()) + parkingSpaceCountByType(ParkingSpaceTypeEnum.VACANT_FAST_CHARGING_PARKING_SPACE, floorStatistics);
            parkingSpaceTypeStatistics.put(ParkingSpaceTypeEnum.VACANT_FAST_CHARGING_PARKING_SPACE.value(), vacantFastChargingParkingSpaceCount);
        }

    }

    /**
     * 统计楼层下指定类型的车位数量统计
     *
     * @param parkingSpaceTypeEnum
     * @param floorParkingSpaceStatisticVO
     */
    public int parkingSpaceCountByType(ParkingSpaceTypeEnum parkingSpaceTypeEnum, FloorParkingSpaceStatisticVO
            floorParkingSpaceStatisticVO) {

        int parkingSpaceTypeCount = 0;
        if (parkingSpaceTypeEnum == null || floorParkingSpaceStatisticVO == null) {
            return 0;
        }
        List<SpaceParkIngSpaceStatisticVO> spaceParkIngSpaceStatistics = floorParkingSpaceStatisticVO.getSpaceParkIngSpaceStatistics();
        if (CollectionUtils.isEmpty(spaceParkIngSpaceStatistics)) {
            return 0;
        }
        for (SpaceParkIngSpaceStatisticVO spaceParkIngSpaceStatistic : spaceParkIngSpaceStatistics) {
            Map<String, Integer> parkingSpaceTypeStatistics = spaceParkIngSpaceStatistic.getParkIngSpaceStatistics();
            if (parkingSpaceTypeStatistics != null && !parkingSpaceTypeStatistics.isEmpty())
                parkingSpaceTypeCount = parkingSpaceTypeStatistics.get(parkingSpaceTypeEnum.value()) + parkingSpaceTypeCount;

        }
        return parkingSpaceTypeCount;
    }

    @SneakyThrows
    public void fillFloorParkingSpaceStatistic(Map<FloorParkingSpaceStatisticVO, List<DtpRwdSpaceDTO>> floorEntityAndSpaceListMap) {
        StopWatch stopWatch = StopWatch.create("fillFloorParkingSpaceStatistic");
        if (floorEntityAndSpaceListMap == null || floorEntityAndSpaceListMap.isEmpty()) {
            return;
        }
        stopWatch.start("统计所有楼层的车位统计");
        List<CompletableFuture<Void>> listCompletableFuture = new ArrayList<CompletableFuture<Void>>();
        floorEntityAndSpaceListMap.forEach((floorStatistic, spaceList) -> {
            CompletableFuture<Void> statisticsVoCompletableFuture = CompletableFuture.supplyAsync(() -> {
                fillSingleFloorParkingSpaceStatistic(floorStatistic, spaceList);
                return null;
            }, BusinessThreadPoolExecutor.ttlExecutor());
            listCompletableFuture.add(statisticsVoCompletableFuture);
        });

        CompletableFuture.allOf(listCompletableFuture.toArray(new CompletableFuture[0])).get();
        stopWatch.stop();
        logProjectInstance(ContextUtil.getProjectId()).info("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
    }

    /**
     * @param floorStatistic
     * @param spaceList
     */
    public void fillSingleFloorParkingSpaceStatistic(FloorParkingSpaceStatisticVO floorStatistic, List<DtpRwdSpaceDTO> spaceList) {
        if (CollectionUtils.isEmpty(spaceList)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        Map<String, DtpRwdSpaceDTO> spaceIdAndSpaceEntity = spaceList.stream().collect(Collectors.toMap(DtpRwdSpaceDTO::getId, p -> p));
        // 空间下对应的车位集合
        StopWatch stopWatch = StopWatch.create("楼层下的车位");
        stopWatch.start("查询空间下的车位");
        Map<String, Set<String>> spaceIdAndParkingSpaceIds = getParkingSpaceBySpaceIds(spaceIdAndSpaceEntity.keySet());
        Set<String> noParkingSpace = spaceIdAndSpaceEntity.keySet().stream().filter(spaceId -> !spaceIdAndParkingSpaceIds.containsKey(spaceId))
                .collect(Collectors.toSet());
        // 没有停车位的空间
        if (!CollectionUtils.isEmpty(noParkingSpace)) {
            noParkingSpace.forEach(spaceId -> {
                spaceIdAndParkingSpaceIds.put(spaceId, Sets.newHashSet());
            });
        }

        stopWatch.stop();
        if (!spaceIdAndParkingSpaceIds.isEmpty()) {
            Set<String> objectIds = new HashSet<>(spaceIdAndParkingSpaceIds.keySet());
            spaceIdAndParkingSpaceIds.values().forEach(parkingSpaceIds -> objectIds.addAll(parkingSpaceIds));
            // 楼层异常事件的数量
            stopWatch.start("停车位异常事件查询");
            floorStatistic.setExceptionCount(getExceptionCountByIds(objectIds));
            stopWatch.stop();
            spaceIdAndParkingSpaceIds.values().forEach(parkingSpaceIds ->
                    // 楼层车位总数
                    floorStatistic.setParkIngSpaceCount(floorStatistic.getParkIngSpaceCount() + parkingSpaceIds.size()));


            // 所有车位
            Set<String> floorParkingSpaceIds = new HashSet<>();
            // 楼层场充电车位列表
            Set<String> chargingParkingSpaceIds = new HashSet<>();
            // 楼层快充车位
            Set<String> fastChargingParkingSpaceIds = new HashSet<>();

            spaceIdAndParkingSpaceIds.values().forEach(floorParkingSpaceIds::addAll);
            DtpRwdObjectQueryParam chargingPointQueryParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.SEGECP)
                    .setWithColumns(Arrays.asList("isChargingRateOptional", DtpRwdObjectDTO.Fields.id));
            chargingPointQueryParam.addInfoCode("floorId", floorStatistic.getFloorId());
            // 楼层下的充电桩
            stopWatch.start("楼层下的充电桩");
            List<DtpRwdObjectDTO> chargingPoint = dtpRwdClientService.listObject(chargingPointQueryParam);
            stopWatch.stop();
            // 充电车位和快充车位的列表
            Map<ChargingRateOptionalEnum, Map<String, String>> parkingSpaceIdAndChargingPointId = getChargingParkingSpaceByParkingSpaces(floorParkingSpaceIds, chargingPoint);
            // 统计充电车位数量
            if (!parkingSpaceIdAndChargingPointId.isEmpty()) {
                parkingSpaceIdAndChargingPointId.forEach((chargingRateOptionalEnum, parkingSpaceIds) -> {
                    // 一个停车位要么是快充车位, 要么是普通车位, 所以仅添加一次
                    if (ChargingRateOptionalEnum.FAST_CHARGING.equals(chargingRateOptionalEnum)) {
                        fastChargingParkingSpaceIds.addAll(parkingSpaceIds.keySet());
                        return;
                    }
                    chargingParkingSpaceIds.addAll(parkingSpaceIds.keySet());
                });
            }
            // 楼层充电车位数量
            floorStatistic.setChargingParkingSpaceCount(chargingParkingSpaceIds.size());
            // 楼层空闲车位
            stopWatch.start("楼层下空闲车位信息点查询");
            Set<String> floorVacantParkingList = vacantParkingSpaceList(floorParkingSpaceIds);
            stopWatch.stop();
            floorStatistic.setVacantParkIngSpaceCount(floorVacantParkingList.size());
            // 楼层空闲充电车位
            Set<String> floorVacantChargingParkingSpaceList = new HashSet<>();
            if (!CollectionUtils.isEmpty(chargingParkingSpaceIds)) {
                floorVacantChargingParkingSpaceList = chargingParkingSpaceIds.stream().filter(floorVacantParkingList::contains).collect(Collectors.toSet());
            }
            // 楼层空闲快充充电车位
            Set<String> floorVacantFastChargingParkingSpaceList = new HashSet<>();
            if (!CollectionUtils.isEmpty(fastChargingParkingSpaceIds)) {
                floorVacantFastChargingParkingSpaceList = fastChargingParkingSpaceIds.stream().filter(floorVacantParkingList::contains).collect(Collectors.toSet());
            }

            floorStatistic.setVacantChargingParkingSpace(floorVacantChargingParkingSpaceList.size());
            List<SpaceParkIngSpaceStatisticVO> spaceParkIngSpaceStatistics = new ArrayList<>();
            floorStatistic.setSpaceParkIngSpaceStatistics(spaceParkIngSpaceStatistics);

            // 空间的车位统计
            Set<String> finalFloorVacantChargingParkingSpaceList = floorVacantChargingParkingSpaceList;
            // 空闲快充车位
            Set<String> finalFloorVacantFastChargingParkingSpaceList = floorVacantFastChargingParkingSpaceList;
            spaceIdAndParkingSpaceIds.forEach((spaceId, parkingSpaceIds) -> {
                SpaceParkIngSpaceStatisticVO spaceParkIngSpaceStatisticVO = new SpaceParkIngSpaceStatisticVO();
                spaceParkIngSpaceStatisticVO.setSpaceId(spaceId);
                spaceParkIngSpaceStatisticVO.setSpaceName(spaceIdAndSpaceEntity.get(spaceId).getLocalName());
                spaceParkIngSpaceStatisticVO.initData();
                // 空间的车位统计
                Map<String, Integer> parkIngSpaceStatisticsInSpace = spaceParkIngSpaceStatisticVO.getParkIngSpaceStatistics();
                parkIngSpaceStatisticsInSpace.put(ParkingSpaceTypeEnum.PARKING_SPACE.value(), parkingSpaceIds.size());
                Set<String> vacantParkingSpaceList = new HashSet<>();
                if (!CollectionUtils.isEmpty(parkingSpaceIds)) {
                    vacantParkingSpaceList = parkingSpaceIds.stream().filter(floorVacantParkingList::contains).collect(Collectors.toSet());
                }

                parkIngSpaceStatisticsInSpace.put(ParkingSpaceTypeEnum.VACANT_PARKING_SPACE.value(), vacantParkingSpaceList.size());
                Set<String> chargingParkingSpaceInSpace = parkingSpaceIds.stream().filter(chargingParkingSpaceIds::contains).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(chargingParkingSpaceInSpace)) {
                    parkIngSpaceStatisticsInSpace.put(ParkingSpaceTypeEnum.CHARGING_PARKING_SPACE.value(), chargingParkingSpaceInSpace.size());
                }
                Set<String> vacantChargingParkingSpaceInSpace = parkingSpaceIds.stream().filter(finalFloorVacantChargingParkingSpaceList::contains).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(vacantChargingParkingSpaceInSpace)) {
                    parkIngSpaceStatisticsInSpace.put(ParkingSpaceTypeEnum.VACANT_CHARGING_PARKING_SPACE.value(), vacantChargingParkingSpaceInSpace.size());
                }
                Set<String> fastChargingParkingSpaceInSpace = parkingSpaceIds.stream().filter(fastChargingParkingSpaceIds::contains).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(fastChargingParkingSpaceInSpace)) {
                    parkIngSpaceStatisticsInSpace.put(ParkingSpaceTypeEnum.FAST_CHARGING_PARKING_SPACE.value(), fastChargingParkingSpaceInSpace.size());
                }
                Set<String> vacantFastChargingParkingSpaceInSpace = parkingSpaceIds.stream().filter(finalFloorVacantFastChargingParkingSpaceList::contains).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(vacantFastChargingParkingSpaceInSpace)) {
                    parkIngSpaceStatisticsInSpace.put(ParkingSpaceTypeEnum.VACANT_FAST_CHARGING_PARKING_SPACE.value(), vacantFastChargingParkingSpaceInSpace.size());
                }
                spaceParkIngSpaceStatistics.add(spaceParkIngSpaceStatisticVO);
            });
        }
        logProjectInstance(projectId).info("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
    }

    /**
     * 根据车位列表返回空闲车位数
     *
     * @param parkingSpacesIds
     */
    public Set<String> vacantParkingSpaceList(Set<String> parkingSpacesIds) {
        StopWatch stopWatch = StopWatch.create("空闲车位");
        stopWatch.start("空闲车位查询");
        Set<String> vacantParkingSpaceList = new HashSet<>();
        List<InfoCodeValueVO> statusInfoCodes = abstractIotDataCacheServer.listInfoCodeValueInOneHour(parkingSpacesIds, Lists.newArrayList(InfoCodeEnum.PARKING_STATUS.value()));
        if (CollectionUtils.isEmpty(statusInfoCodes)) {
            return vacantParkingSpaceList;
        }
        stopWatch.stop();
        vacantParkingSpaceList = statusInfoCodes.stream().filter(p -> p != null && p.getInfoCodeValue().equals(ParkingSpaceStatusEnum.VACANT_PARKING_SPACE.value()))
                .map(p -> p.getObjectId()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(vacantParkingSpaceList)) {
            return vacantParkingSpaceList;
        }
        logProjectInstance(ContextUtil.getProjectId()).info("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return vacantParkingSpaceList;
    }

    /**
     * 返回充电和快充 key 是快充还是 普通充电，value 中key是停车位id,value是充电桩id
     *
     * @param parkingSpaces 停车位列表
     * @return CHARGING_PARKING_SPACE(" CHARGING_PARKING_SPACE ", " 充电车位 "),
     * <p>
     * FAST_CHARGING_PARKING_SPACE("FAST_CHARGING_PARKING_SPACE", "快充车位"),
     */
    public Map<ChargingRateOptionalEnum, Map<String, String>> getChargingParkingSpaceByParkingSpaces(
            Set<String> parkingSpaces, List<DtpRwdObjectDTO> chargingPointList) {
        Map<ChargingRateOptionalEnum, Map<String, String>> result = new HashMap<>();
        Map<String, String> fastCharging = new HashMap<>();
        Map<String, String> normalCharging = new HashMap<>();
        result.put(ChargingRateOptionalEnum.FAST_CHARGING, fastCharging);
        result.put(ChargingRateOptionalEnum.NORMAL_CHARGING, normalCharging);
        if (CollectionUtils.isEmpty(parkingSpaces) || CollectionUtils.isEmpty(chargingPointList)) {
            return result;

        }
        // 快充充电桩
        Set<String> fastChargePointIds = new HashSet<>();
        // 普通充电桩
        Set<String> normalChargePointIds = new HashSet<>();
        // 查询充电桩
        chargingPointList.forEach(object -> {
            String infoValue = object.analyzeProperties(isChargingRateOptional);
            // 一个停车位要么是快充车位, 要么是普通车位, 所以仅添加一次
            if (ChargingRateOptionalEnum.getChargingRate(infoValue) == ChargingRateOptionalEnum.FAST_CHARGING) {
                fastChargePointIds.add(object.getId());
                return;
            }
            normalChargePointIds.add(object.getId());
        });
        fastCharging.putAll(getChargeParkingSpace(fastChargePointIds, parkingSpaces));
        normalCharging.putAll(getChargeParkingSpace(normalChargePointIds, parkingSpaces));
        return result;
    }


    /**
     * 根据充电桩id和停车位id   返回key为 车位id,value 充电桩id
     *
     * @param chargePointIds
     * @param parkingSpaceIds
     */
    public Map<String, String> getChargeParkingSpace(Set<String> chargePointIds, Set<String> parkingSpaceIds) {
        if (CollectionUtils.isEmpty(chargePointIds) || CollectionUtils.isEmpty(parkingSpaceIds)) {
            return new HashMap<>();
        }
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setId(parkingSpaceIds);
        param.setClassCode(DtpRwdClassCodeEnum.CFPKPS.value());
        param.setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds));
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQ_SERVICE_EQ).setObjFrom(chargePointIds));
        List<DtpRwdObjectDTO> parkingSpaceDtoList = dtpRwdClientService.listObject(param);
        if (CollectionUtils.isEmpty(parkingSpaceDtoList)) {
            return new HashMap<>();
        }
        return parkingSpaceDtoList.stream().filter(e -> CollectionUtils.isNotEmpty(e.getRelObjIds())).collect(Collectors.toMap(DtpRwdObjectDTO::getId, p -> p.getRelObjIds().stream().findFirst().get()));
    }


    public int getExceptionCountByIds(Set<String> equipIds) {
        List<ExceptionEventBaseInfoVO> riskEventMap =
                abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(equipIds, PARKING.getRunningMonitorStrategy().exceptionFilterRiskMeasureAimLevel2());
        return riskEventMap.size();
    }


    /**
     * 根据空间spaceIds 列表查询对应空间的车位对象id列表
     *
     * @param spaceIds
     */
    public Map<String, Set<String>> getParkingSpaceBySpaceIds(Set<String> spaceIds) {
        StopWatch stopWatch = StopWatch.create("getParkingSpaceBySpaceIds");
        Map<String, Set<String>> spaceToParkingSpaceMap = new HashMap<>();
        if (CollectionUtils.isEmpty(spaceIds)) {
            return spaceToParkingSpaceMap;
        }
        DtpRwdObjectQueryParam queryParkingSpaceParam = new DtpRwdObjectQueryParam();
        queryParkingSpaceParam.setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds));
        queryParkingSpaceParam.setClassCode(DtpRwdClassCodeEnum.CFPKPS.getValue());
        // 查询查询停车位
        stopWatch.start("根据空间关系查询车位");
        log.info("车位空间数量：" + spaceIds.size());
        List<DtpRwdEquipmentDTO> parkingSpaceDTOS = dtpRwdClientService.listEquipmentInBySpaceIds(spaceIds, queryParkingSpaceParam);
        stopWatch.stop();
        for (DtpRwdEquipmentDTO parkingSpace : parkingSpaceDTOS) {
            if (parkingSpace.getRelObjIds() == null) {
                continue;
            }
            String spaceId = parkingSpace.getRelObjIds().stream().findFirst().orElse(null);
            if (StringUtils.isEmpty(spaceId)) {
                log.warn("parkingSpaceId:{},is not space}", parkingSpace.getId());
            }
            Set<String> parkingSpaceIds = spaceToParkingSpaceMap.get(spaceId);
            //  MapExecutor.dynamicPut(spaceToParkingSpaceMap, spaceId, parkingSpace.getId(), LinkedHashSet::new);
            if (parkingSpaceIds == null) {
                parkingSpaceIds = new HashSet<>();
                spaceToParkingSpaceMap.put(spaceId, parkingSpaceIds);
            }
            parkingSpaceIds.add(parkingSpace.getId());

        }
        logProjectInstance(ContextUtil.getProjectId()).info("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        // return DtpRwdSpaceDTO.mapRelObjIdAndIds(parkingSpaceDTOS);
        return spaceToParkingSpaceMap;
    }

    /**
     * 查询停车场空间
     *
     * @param floorIds 根据楼层id集合过滤
     */
    public List<DtpRwdSpaceDTO> getParkingSpaceList(@Nullable Collection<String> floorIds) {
        StopWatch stopWatch = StopWatch.create("查询楼层下的空间");

        String projectId = ContextUtil.getProjectId();
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, DtpRwdSpaceDTO.Fields.floorId);
        stopWatch.start("查询楼层下停车场空间");
        param.addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType, PARKING_SPACE_TYPE_LIST);
        if (!CollectionUtils.isEmpty(floorIds)) {
            param.addInfoCode(DtpRwdSpaceDTO.Fields.floorId, floorIds);
        }
        List<DtpRwdSpaceDTO> result = dtpRwdClientService.listSpace(false, false, projectId, param);
        stopWatch.stop();
        logProjectInstance(projectId).info("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return result;
    }

    @Override
    public String logTag() {
        return "停车场";
    }
}
