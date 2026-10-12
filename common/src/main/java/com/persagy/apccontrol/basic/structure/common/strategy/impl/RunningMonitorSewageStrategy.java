package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.sewage.SewageEquipVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel1;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS;
import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS_LIST;

/**
 * @author wxf
 * @description 污水处理
 * @date 15:13 2024/12/13
 **/
@Component(RunningMonitorSewageStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorSewageStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorSewageStrategy";
    public static final String LOG_TAG = "运行监测-污水处理";
    private final DtpRwdClientService dtpRwdClientService;
    private final List<String> WITH_COLUMNS = Arrays.asList(DtpRwdEquipmentDTO.Fields.floorId, DtpRwdEquipmentDTO.Fields.buildingId,
            DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localId,
            DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.classCode, DtpRwdObjectDTO.Fields.relObjIds,MONITORING_SURVEILLANCE_STATUS);

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.SEWAGE;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                           @NonNull DemandInstanceValueObject instance, String... withColumns) {

        // 查询设备所在的空间 ID
        Set<String> querySpaceIds = listEquipments(runningMonitorSecondType.toRunningMonitorTypeEnum(), instance, DtpRwdObjectDTO.Fields.id)
                .stream().map(SewageEquipVO::getSpaceId).collect(Collectors.toSet());

        if (CollectionUtil.isEmpty(querySpaceIds)) {
            return new ArrayList<>();
        }

        //通过空间 id 查询空间信息
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setId(querySpaceIds);
        return dtpRwdClientService.listSpace(queryParam);
    }

    @Override
    public List<SewageEquipVO> listEquipments(
            @NonNull RunningMonitorTypeEnum runningMonitorType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {

        List<String> withColumnList = withColumns == null ? WITH_COLUMNS : Arrays.asList(withColumns);
        //集水坑，隔油池，餐饮除油成套设备
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                //.addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST)
                .setClassCode(Arrays.asList(DtpRwdClassCodeEnum.WSSTCP.getValue(), DtpRwdClassCodeEnum.WSSTOS.getValue(),
                        DtpRwdClassCodeEnum.WSSTOR.getValue(), DtpRwdClassCodeEnum.WSSTLU.getValue()))
                .setWithColumns(withColumnList);

        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDtos = null;
        if (instance.getInstanceType() == DemandInstanceTypeEnum.FLOOR) {
            dtpRwdEquipmentDtos = dtpRwdClientService.listEquipmentInByFloorIds(Collections.singletonList(instance.getInstanceId()), queryParam);
        } else if (instance.getInstanceType() == DemandInstanceTypeEnum.PROJECT) {
            dtpRwdEquipmentDtos = dtpRwdClientService.listEquipment(queryParam);
        }
        if (CollectionUtil.isEmpty(dtpRwdEquipmentDtos)) {
            return Lists.newArrayList();
        }

        //设备所在空间
        Map<String, DtpRwdSpaceDTO> equip2spaceMap = buildEquip2SpaceMap(dtpRwdEquipmentDtos);

        List<SewageEquipVO> sewageEquipVos = dtpRwdEquipmentDtos.stream()
                .map(dtpRwdEquipmentDTO -> {
                    SewageEquipVO sewageEquipVO = new SewageEquipVO();
                    BeanUtil.copyProperties(dtpRwdEquipmentDTO, sewageEquipVO);
                    sewageEquipVO.setMonitoringSurveillanceStatus(dtpRwdEquipmentDTO.analyzeProperties(MONITORING_SURVEILLANCE_STATUS));
                    return sewageEquipVO;
                })
                .collect(Collectors.toList());

        Map<String, SewageEquipVO> sewageEquipMap = sewageEquipVos.stream().collect(Collectors.toMap(SewageEquipVO::getId, Function.identity(), (v1, v2) -> v1));

        // 获取楼层和建筑名映射
        Map<String, String> floorMap = buildFloorMap(dtpRwdEquipmentDtos);
        Map<String, String> buildingMap = buildBuildingMap(dtpRwdEquipmentDtos);

        // 获取关联的泵并更新设备信息
        updateRelatedPumps(sewageEquipVos, sewageEquipMap, equip2spaceMap, floorMap, buildingMap, withColumnList);

        sewageEquipVos.forEach(sewageEquipVO -> {
            sewageEquipVO.setBuildingName(buildingMap.get(sewageEquipVO.getBuildingId()));
            sewageEquipVO.setFloorName(floorMap.get(sewageEquipVO.getFloorId()));
            DtpRwdSpaceDTO space = equip2spaceMap.get(sewageEquipVO.getId());
            if (Objects.nonNull(space)) {
                sewageEquipVO.setSpaceId(space.getId());
                sewageEquipVO.setSpaceName(space.getLocalName());
            }
        });

        return sewageEquipVos;
    }

    private Map<String, DtpRwdSpaceDTO> buildEquip2SpaceMap(List<DtpRwdEquipmentDTO> dtpRwdEquipmentDtos) {
        Map<String, DtpRwdSpaceDTO> equip2spaceMap = new HashMap<>();
        List<String> equipIds = dtpRwdEquipmentDtos.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList());
        if (!CollectionUtil.isEmpty(equipIds)) {
            List<DtpRwdSpaceDTO> dtpRwdSpaceDtos = dtpRwdClientService.listSpaceInByEquipmentId(
                    equipIds, new DtpRwdObjectQueryParam().setWithColumns(Arrays.asList(
                            DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.relObjIds)));
            dtpRwdSpaceDtos.forEach(space -> {
                if (space.getRelObjIds() != null) {
                    space.getRelObjIds().forEach(relObjId -> equip2spaceMap.put(relObjId, space));
                }
            });
        }
        return equip2spaceMap;
    }

    private Map<String, String> buildFloorMap(List<DtpRwdEquipmentDTO> dtpRwdEquipmentDtos) {
        Set<String> floorIds = dtpRwdEquipmentDtos.stream().map(DtpRwdEquipmentDTO::getFloorId).collect(Collectors.toSet());
        List<DtpRwdFloorDTO> dtpRwdFloorDtos = dtpRwdClientService.listFloor(
                new DtpRwdObjectQueryParam().setId(floorIds).setWithColumns(Collections.singletonList(DtpRwdObjectDTO.Fields.localName)));
        return dtpRwdFloorDtos.stream().collect(HashMap::new,
                (map, item) -> map.put(item.getId(), item.getLocalName()),
                HashMap::putAll
        );
    }

    private Map<String, String> buildBuildingMap(List<DtpRwdEquipmentDTO> dtpRwdEquipmentDtos) {
        Set<String> buildingIds = dtpRwdEquipmentDtos.stream().map(DtpRwdEquipmentDTO::getBuildingId).collect(Collectors.toSet());
        List<DtpRwdBuildingDTO> dtpRwdBuildingDtos = dtpRwdClientService.listBuilding(
                new DtpRwdObjectQueryParam().setId(buildingIds).setWithColumns(Collections.singletonList(DtpRwdObjectDTO.Fields.localName)));
        return dtpRwdBuildingDtos.stream().collect(HashMap::new,
                (map, item) -> map.put(item.getId(), item.getLocalName()),
                HashMap::putAll
        );
    }

    private void updateRelatedPumps(List<SewageEquipVO> sewageEquipVos, Map<String, SewageEquipVO> sewageEquipMap,
                                    Map<String, DtpRwdSpaceDTO> equip2spaceMap, Map<String, String> floorMap, Map<String, String> buildingMap,
                                    List<String> withColumns) {
        List<String> wsstluIds = sewageEquipVos.stream().filter(e -> DtpRwdClassCodeEnum.WSSTLU.getValue().equals(e.getClassCode())).map(SewageEquipVO::getId).collect(Collectors.toList());
        List<String> wsstorIds = sewageEquipVos.stream().filter(e -> DtpRwdClassCodeEnum.WSSTOR.getValue().equals(e.getClassCode())).map(SewageEquipVO::getId).collect(Collectors.toList());
        List<String> wsstcpAndwsstosIds = sewageEquipVos.stream().filter(e -> DtpRwdClassCodeEnum.WSSTCP.getValue().equals(e.getClassCode()) ||
                DtpRwdClassCodeEnum.WSSTOS.getValue().equals(e.getClassCode())).map(SewageEquipVO::getId).collect(Collectors.toList());
        // 独立构造关系查询参数，不携带主查询的状态（包括 relationFrom/relationTo 残留）
        DtpRwdObjectQueryParam relationQueryParam = new DtpRwdObjectQueryParam()
                .setClassCode(DtpRwdClassCodeEnum.WSSTSP.getValue())
                .addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST)
                .setWithColumns(withColumns);

        //每个并行查询使用独立的参数副本, 避免异步线程间共享可变参数产生竞态
        DtpRwdObjectQueryParam wsstluQueryParam = BeanUtil.copyProperties(relationQueryParam, DtpRwdObjectQueryParam.class);
        DtpRwdObjectQueryParam wsstorQueryParam = BeanUtil.copyProperties(relationQueryParam, DtpRwdObjectQueryParam.class);
        DtpRwdObjectQueryParam wsstcpAndwsstosQueryParam = BeanUtil.copyProperties(relationQueryParam, DtpRwdObjectQueryParam.class);

        //多线程并行查询
        CompletableFuture<List<DtpRwdEquipmentDTO>> wsstluIdsCompletableFuture = CompletableFuture.supplyAsync(() -> dtpRwdClientService.listSubmersibleSewagePumpByEquipmentIds(
                        false, ContextUtil.getProjectId(), wsstluIds, wsstluQueryParam)
                , BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture<List<DtpRwdEquipmentDTO>> wsstorIdsCompletableFuture = CompletableFuture.supplyAsync(() -> dtpRwdClientService.listSubmersibleSewagePumpByEquipmentIds(
                        false, ContextUtil.getProjectId(), wsstorIds, wsstorQueryParam)
                , BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture<List<DtpRwdEquipmentDTO>> wsstcpAndwsstosIdsCompletableFuture = CompletableFuture.supplyAsync(() -> dtpRwdClientService.listLocationInsideByEquipmentIds(
                        false, ContextUtil.getProjectId(), wsstcpAndwsstosIds, wsstcpAndwsstosQueryParam)
                , BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture.allOf(wsstluIdsCompletableFuture, wsstorIdsCompletableFuture, wsstcpAndwsstosIdsCompletableFuture).join();
        assemblyRelData(sewageEquipMap, wsstluIdsCompletableFuture.join(),Lists.newArrayList(DtpRwdClassCodeEnum.WSSTLU.getValue()));
        assemblyRelData(sewageEquipMap, wsstorIdsCompletableFuture.join(),Lists.newArrayList(DtpRwdClassCodeEnum.WSSTOR.getValue()));
        assemblyRelData(sewageEquipMap, wsstcpAndwsstosIdsCompletableFuture.join(),Lists.newArrayList(DtpRwdClassCodeEnum.WSSTCP.getValue(), DtpRwdClassCodeEnum.WSSTOS.getValue()));
        //将sewageEquipMap 里面的SewageEquipVO 中关联的relEquipment 列表按照 实例id 去重
        sewageEquipMap.forEach((key, value) -> {
            List<DtpRwdEquipmentVO> relEquipment = value.getRelEquipment();
            if (relEquipment != null) {
                relEquipment = relEquipment.stream().collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DtpRwdEquipmentVO::getId))),
                        ArrayList::new
                ));
                value.setRelEquipment(relEquipment);
            }
        });
    }

    private static void assemblyRelData(Map<String, SewageEquipVO> sewageEquipMap, List<DtpRwdEquipmentDTO> subDtpRwdEquipmentDtos, List<String> classCodes) {
        subDtpRwdEquipmentDtos.forEach(subDtpRwdEquipmentDTO -> {
            DtpRwdEquipmentVO subDtpRwdEquipmentVO = BeanUtil.copyProperties(subDtpRwdEquipmentDTO, DtpRwdEquipmentVO.class);
            if (subDtpRwdEquipmentDTO.getRelObjIds() != null) {
                subDtpRwdEquipmentDTO.getRelObjIds().forEach(relObjId -> {
                    SewageEquipVO sewageEquipVO = sewageEquipMap.get(relObjId);
                    if (sewageEquipVO != null && classCodes.contains(sewageEquipVO.getClassCode())) {
                        List<DtpRwdEquipmentVO> relEquipment = Optional.ofNullable(sewageEquipVO.getRelEquipment()).orElseGet(ArrayList::new);
                        relEquipment.add(subDtpRwdEquipmentVO);
                        sewageEquipVO.setRelEquipment(relEquipment);
                    }
                });
            }
        });
    }

    @Override
    public Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition() {
        //空间异常事件，通过”风控目标“进行过滤
        //”一级风控目标=“安全” & 二级风控目标=“漫水”
        return x -> x.stream().filter(item -> {
            boolean riskConditions = item.getObjectType().equals(ObjectTypeEnum.SPACE.value())
                    && CollectionUtils.isNotEmpty(item.getRiskMeasureAimLevel1Collect())
                    && item.getRiskMeasureAimLevel1Collect().stream().anyMatch(Lists.newArrayList(RiskMeasureAimLevel1.E2.getCode())::contains)
                    && CollectionUtils.isNotEmpty(item.getRiskMeasureAimLevel2Collect())
                    && item.getRiskMeasureAimLevel2Collect().stream().anyMatch(Lists.newArrayList(RiskMeasureAimLevel2.E31.getCode())::contains);
            return riskConditions || item.getObjectType().equals(ObjectTypeEnum.EQUIPMENT.value());
        }).collect(Collectors.toList());
    }

    @Override
    public Collection<RiskMeasureAimLevel2> exceptionFilterRiskMeasureAimLevel2() {
        return Lists.newArrayList(RiskMeasureAimLevel2.E31);
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
