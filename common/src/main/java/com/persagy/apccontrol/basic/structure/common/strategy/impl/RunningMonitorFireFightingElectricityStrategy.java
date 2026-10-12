package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSystemQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.performance.StopWatch;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.client.RequestServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消防电策略
 *
 * @author LiuYang
 * @since 5.241102.0
 */
@Component(RunningMonitorFireFightingElectricityStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class RunningMonitorFireFightingElectricityStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorFireFightingElectricityStrategy";
    // 系统编码
    public static final List<DtpRwdClassCodeEnum> systemClassCode = Lists.newArrayList(
            DtpRwdClassCodeEnum.FFFA,
            DtpRwdClassCodeEnum.FFEA,
            DtpRwdClassCodeEnum.FFGA,
            DtpRwdClassCodeEnum.FFPM,
            DtpRwdClassCodeEnum.FFCU,
            DtpRwdClassCodeEnum.FFSC,
            DtpRwdClassCodeEnum.FFEE,
            DtpRwdClassCodeEnum.FFEB,
            DtpRwdClassCodeEnum.FFFR,
            DtpRwdClassCodeEnum.FFGE,
            DtpRwdClassCodeEnum.FFRP
    );

    private final DtpRwdClientService dtpRwdClientService;

    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private static final String LOG_TAG = "消防电策略";
    private static final List<String> EQUIPMENT_QUERY_FIELDS = Arrays.asList(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdObjectDTO.Fields.objType,
            DtpRwdEquipmentDTO.Fields.floorId,
            DtpRwdObjectDTO.Fields.relObjIds
    );

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.FIRE_FIGHTING_ELECTRICITY;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return InheritConfigTypeEnum.FIELD;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        if (CollectionUtils.isEmpty(param.getFilterSpaceInEquipmentIds())) {
            RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
            List<DtpRwdEquipmentDTO> equipments = listEquipments(equipmentQueryParam);
            if (CollectionUtils.isEmpty(equipments)) {
                return new ArrayList<>();
            }
            param.setFilterSpaceInEquipmentIds(equipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
        }
        return BusinessThreadPoolExecutor.parallelExecute(() -> param.queryObject(queryParam));
    }

    @Override
    public List<? extends DtpRwdObjectDTO> listDashboardSystems(@NonNull RunningMonitorSystemQueryParam param) {
        StopWatch stopWatch = new StopWatch("查询DashboardSystems耗时统计");
        stopWatch.start("查询系统");
        // 记录classCode用于查询对应的名称
        Set<String> classCodes = new ConcurrentHashSet<>(64);
        // 查询系统
        Map<String, DtpRwdObjectDTO> sysMap = querySystem(classCodes, param);
        stopWatch.stop();
        // 查询设备
        stopWatch.start("查询设备");
        List<? extends DtpRwdEquipmentDTO> equipments = getDtpRwdEquipmentDTOS(sysMap);
        stopWatch.stop();
        Map<String, Set<DtpRwdEquipmentDTO>> sysEquipmentMap = new ConcurrentHashMap<>(sysMap.size());
        // 记录设备 ID 和 classCode
        Set<String> equipmentIds = new ConcurrentHashSet<>();
        stopWatch.start("遍历设备 ID 映射");
        if (CollectionUtils.isNotEmpty(equipments)) {
            equipments.parallelStream().forEach(equipment -> {
                Set<String> relObjIds = equipment.getRelObjIds();
                equipmentIds.add(equipment.getId());
                classCodes.add(equipment.getClassCode());
                if (relObjIds != null) {
                    relObjIds.parallelStream()
                            .forEach(relObjId -> sysEquipmentMap.computeIfAbsent(relObjId,
                                    k -> new ConcurrentHashSet<>()).add(equipment));
                }
            });
        }
        stopWatch.stop();
        // 设备和空间关系
        stopWatch.start("查询设备关联的空间");
        Map<String, Set<String>> spacesMap = querySpaces(equipmentIds);
        stopWatch.stop();
        // 查询classCode对应的名称
        Map<String, String> classCodeAndName = dtpKnowledgeClientService.mapClassCodeAndName(false, true, classCodes);
        // 转换为结果对象
        stopWatch.start("构建结果对象");
        List<DtpRwdDashboardSystemsDTO> r = buildVo(sysMap, classCodeAndName, sysEquipmentMap, spacesMap);
        stopWatch.stop();

        // 耗时统计
        LogExecutor.project(ContextUtil.getProjectId()).info(log, LOG_TAG, "查询DashboardSystems结束",
                Collections.singletonList(Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS))));
        return r;
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipments(@NonNull RunningMonitorEquipmentQueryParam param) {

        DtpRwdObjectQueryParam sysQueryParam = new DtpRwdObjectQueryParam();
        sysQueryParam.setWithColumns(Lists.newArrayList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode));
        // 没传递系统 查询全量系统
        sysQueryParam.setClassCodeEnum(systemClassCode);
        if (CollectionUtils.isNotEmpty(param.getFilterEquipmentInSystemIds())) {
            sysQueryParam.setId(param.getFilterEquipmentInSystemIds());
        }
        List<DtpRwdObjectDTO> sys = dtpRwdClientService.listObject(sysQueryParam);
        if (CollectionUtils.isEmpty(sys)) {
            return new ArrayList<>();
        }
        // 系统的 classCode 和系统 ID列表
        Map<String, List<String>> classCodeIds = dtpRwdClientService.listObject(sysQueryParam).stream().collect(
                Collectors.groupingBy(DtpRwdObjectDTO::getClassCode,
                        Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toList())));

        // 并发查询系统下的设备
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // 创建线程安全的 list
        List<DtpRwdEquipmentDTO> equipments = Collections.synchronizedList(new ArrayList<>());
        classCodeIds.forEach((classCode, ids) -> futures.add(CompletableFuture.runAsync(() -> {
            DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(getSystemRelationObjectsClassCode(DtpRwdClassCodeEnum.valueOf(classCode)))
                    .setRelationTo(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(ids))
                    .filterMonitoringSurveillanceStatus();
            // 追加必要字段
            param.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);

            equipments.addAll(BusinessThreadPoolExecutor.parallelExecute(() -> param.queryObject(queryParam)));
        }, BusinessThreadPoolExecutor.ttlExecutor())));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return equipments;
    }

    // 构建最终对象
    private List<DtpRwdDashboardSystemsDTO> buildVo(Map<String, DtpRwdObjectDTO> sysMap,
                                                    Map<String, String> classCodeAndName,
                                                    Map<String, Set<DtpRwdEquipmentDTO>> sysEquipmentMap, Map<String
                    , Set<String>> spacesMap) {
        List<DtpRwdDashboardSystemsDTO> voList = new ArrayList<>(32);
        sysMap.forEach((id, sys) -> {
            DtpRwdDashboardSystemsDTO vo = new DtpRwdDashboardSystemsDTO();
            vo.setId(sys.getId());
            vo.setClassCode(sys.getClassCode());
            vo.setClassCodeName(classCodeAndName.get(sys.getClassCode()));
            vo.setLocalName(sys.getLocalName());
            // 获取系统下设备
            Set<DtpRwdEquipmentDTO> equipmentList = sysEquipmentMap.get(sys.getId());
            if (CollectionUtils.isEmpty(equipmentList)) {
                return;
            }
            // 批量转换为 DtpRwdDashboardElectricityDTO
            Set<DtpRwdDashboardElectricityDTO> equipmentVoList = new ConcurrentHashSet<>();
            vo.setEquipments(equipmentVoList);

            equipmentList.parallelStream().forEach(equipment -> {
                DtpRwdDashboardElectricityDTO eq = new DtpRwdDashboardElectricityDTO();
                eq.setId(equipment.getId());
                eq.setLocalName(equipment.getLocalName());
                eq.setObjType(equipment.getObjType());
                eq.setClassCode(equipment.getClassCode());
                eq.setClassCodeName(classCodeAndName.get(equipment.getClassCode()));
                eq.setSpaceIds(spacesMap.get(equipment.getId()));
                eq.setFloorId(equipment.getFloorId());
                equipmentVoList.add(eq);
            });
            voList.add(vo);
        });
        return voList;
    }

    // 查询设备
    private List<? extends DtpRwdEquipmentDTO> getDtpRwdEquipmentDTOS(Map<String, DtpRwdObjectDTO> sysMap) {
        // 查询设备
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.setFilterEquipmentInSystemIds(sysMap.keySet());
        equipmentQueryParam.setWithColumns(new HashSet<>(EQUIPMENT_QUERY_FIELDS));

        return RunningMonitorTypeEnum.FIRE_FIGHTING_ELECTRICITY.getRunningMonitorStrategy().listEquipments(equipmentQueryParam);
    }

    // 查询系统
    private Map<String, DtpRwdObjectDTO> querySystem(Set<String> classCodes, RunningMonitorSystemQueryParam param) {
        Map<String, DtpRwdObjectDTO> sysMap = new LinkedHashMap<>();
        // 查询系统
        DtpRwdObjectQueryParam sysQueryParam = new DtpRwdObjectQueryParam();
        sysQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode,
                DtpRwdObjectDTO.Fields.localName);

        sysQueryParam.setClassCodeEnum(RunningMonitorFireFightingElectricityStrategy.systemClassCode);
        List<DtpRwdObjectDTO> sysObj = param.queryObject(sysQueryParam);

        if (CollectionUtils.isEmpty(sysObj)) {
            return sysMap;
        }

        // 根据 systemClassCode 的顺序进行排序
        systemClassCode.forEach(classCode -> {
            sysObj.stream()
                    .filter(sys -> classCode.name().equals(sys.getClassCode()))
                    .forEach(sys -> {
                        classCodes.add(sys.getClassCode());
                        sysMap.put(sys.getId(), sys);
                    });
        });
        return sysMap;
    }

    // 查询空间
    private Map<String, Set<String>> querySpaces(Set<String> equipmentIds) {
        // 查询设备关联的空间
        List<DtpRwdSpaceDTO> spaces = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(equipmentIds)) {
            RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
            spaceQueryParam.setFilterSpaceInEquipmentIds(equipmentIds);
            spaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.relObjIds);
            spaces =
                    RunningMonitorTypeEnum.FIRE_FIGHTING_ELECTRICITY.getRunningMonitorStrategy().listSpaces(spaceQueryParam);
        }
        if (CollectionUtils.isEmpty(spaces)) {
            return new HashMap<>();
        }
        // 取出relObjIds和ID 的映射关系
        return DtpRwdObjectDTO.mapRelObjIdAndIds(spaces);
    }

    /**
     * 获取系统下的设备类
     */
    public List<DtpRwdClassCodeEnum> getSystemRelationObjectsClassCode(DtpRwdClassCodeEnum dtpRwdClassCode) {
        switch (dtpRwdClassCode) {
            case FFFA:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.FFFACU,
                        DtpRwdClassCodeEnum.FFFASE,
                        DtpRwdClassCodeEnum.FFFAMA,
                        DtpRwdClassCodeEnum.FFFATE,
                        DtpRwdClassCodeEnum.FFFACE,
                        DtpRwdClassCodeEnum.FFFAHA);
            case FFEA:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.FFEAFM,
                        DtpRwdClassCodeEnum.FFEAFD
                );
            case FFGA:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.FFGACR,
                        DtpRwdClassCodeEnum.FFGADT
                );
            case FFPM:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.FFPMSM,
                        DtpRwdClassCodeEnum.FFPMSR
                );
            case FFCU:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.FFCUCR,
                        DtpRwdClassCodeEnum.FFCUDT
                );
            case FFSC:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.FFSCSF,
                        DtpRwdClassCodeEnum.FFSCEF,
                        DtpRwdClassCodeEnum.FFSCAF,
                        DtpRwdClassCodeEnum.FFSCDF,
                        DtpRwdClassCodeEnum.FFSCMF
                );
            case FFEE:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.FFEESL,
                        DtpRwdClassCodeEnum.FFEELG,
                        DtpRwdClassCodeEnum.FFEECT,
                        DtpRwdClassCodeEnum.FFEECR
                );
            case FFEB:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.FFEBHT,
                        DtpRwdClassCodeEnum.FFEBCR
                );
            case FFFR:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.CFCAHW,
                        DtpRwdClassCodeEnum.CFCSFS,
                        DtpRwdClassCodeEnum.CFCSFW,
                        DtpRwdClassCodeEnum.CFCSFR
                );
            case FFGE:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.FFGECR);
            case FFRP:
                return Lists.newArrayList(
                        DtpRwdClassCodeEnum.FFRPPM,
                        DtpRwdClassCodeEnum.FFRPCL,
                        DtpRwdClassCodeEnum.FFRPDT
                );
            default:
                throw new RequestServerException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_0e89e855675947bc96309701a40352b8"));
        }
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "运行检测-消防电策略";
    }
}
