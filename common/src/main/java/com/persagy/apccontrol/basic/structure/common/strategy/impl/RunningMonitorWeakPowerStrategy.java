package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.WeakPowerRelationConfig;
import com.persagy.apccontrol.basic.model.context.weakpower.WeakPowerRelationContext;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.weakpower.RunningMonitorWeakPowerEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.vo.weakpower.RunningMonitorWeakPowerEquipmentDTO;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptRelationBatch;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.weakpower.converter.WeakPowerConverter;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.ValidationException;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 弱电策略实现
 *
 * @author JiYinchuan
 * @since 7.250301.0
 */
@Component(RunningMonitorWeakPowerStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class RunningMonitorWeakPowerStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorWeakPowerStrategy";

    /**
     * 运行监测二级类型和系统设备类编码映射, 双向唯一
     */
    public static final BidiMap<RunningMonitorSecondTypeEnum, DtpRwdClassCodeEnum> RUNNING_MONITOR_SECOND_TYPE_AND_SYSTEM_CLASS_CODE = new DualHashBidiMap<>();

    static {
        RUNNING_MONITOR_SECOND_TYPE_AND_SYSTEM_CLASS_CODE.put(RunningMonitorSecondTypeEnum.INTELLIGENT_BUILDING_AUTOMATION, DtpRwdClassCodeEnum.WEBA);
        RUNNING_MONITOR_SECOND_TYPE_AND_SYSTEM_CLASS_CODE.put(RunningMonitorSecondTypeEnum.ENERGY_MANAGEMENT,
                            DtpRwdClassCodeEnum.WEEM);
        RUNNING_MONITOR_SECOND_TYPE_AND_SYSTEM_CLASS_CODE.put(RunningMonitorSecondTypeEnum.INDOOR_ENVIRONMENT_MONITORING, DtpRwdClassCodeEnum.WEIE);
        RUNNING_MONITOR_SECOND_TYPE_AND_SYSTEM_CLASS_CODE.put(RunningMonitorSecondTypeEnum.LIGHTING_MONITORING,
                DtpRwdClassCodeEnum.WELM);
    }

    private final WeakPowerConverter weakPowerConverter;

    private final WeakPowerRelationConfig weakPowerRelationConfig;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.WEAK_POWER;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        return Collections.emptyList();
    }

    @Override
    public List<RunningMonitorWeakPowerEquipmentDTO> listEquipments(@NonNull RunningMonitorEquipmentQueryParam param) {
        Set<String> systemIds = Optional.of(param).filter(RunningMonitorWeakPowerEquipmentQueryParam.class::isInstance)
                .map(RunningMonitorWeakPowerEquipmentQueryParam.class::cast)
                .map(RunningMonitorWeakPowerEquipmentQueryParam::getSystemIds)
                .orElse(null);
        //校验
        validateParameters(param, systemIds);

        if (param instanceof RunningMonitorWeakPowerEquipmentQueryParam) {
            RunningMonitorWeakPowerEquipmentQueryParam weakPowerParam =
                    (RunningMonitorWeakPowerEquipmentQueryParam) param;
            systemIds = weakPowerParam.getSystemIds();
        }
        if (CollectionUtils.isEmpty(systemIds)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_6fc14063bc9143f19673d1d21b6c5e4d"));
        }

        // 追加查询最少字段
        param.addWithColumn(DtpRwdObjectDTO.Fields.id);
        param.addWithColumn(DtpRwdObjectDTO.Fields.classCode);
        param.addWithColumn(DtpRwdObjectDTO.Fields.relObjIds);
        param.addWithColumn(DtpRwdObjectDTO.Fields.name);

        List<RunningMonitorWeakPowerEquipmentDTO> result = new ArrayList<>();
        Set<String> processedIds = ConcurrentHashMap.newKeySet();
        // 查询系统下的实例
        List<DtpRwdEquipmentDTO> equipInSystems = queryEquipsInSystem(param, systemIds);
        List<String> equipIdsInSystem = equipInSystems.stream().map(equip -> equip.getId()).collect(Collectors.toList());
        // 过滤出系统下的服务器实例(WEICSV)
        List<DtpRwdEquipmentDTO> serverInstances = equipInSystems.stream()
                .filter(equip -> DtpRwdClassCodeEnum.WEICSV.getValue().equals(equip.getClassCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(serverInstances)) {
            // 没有服务器，仅展示系统实例作为一级节点
            return result;
        }
        //只有一个服务器实例
        if (serverInstances.size() == 1) {
            // 只有一个服务器，直接作为二级节点
            DtpRwdEquipmentDTO server = serverInstances.get(0);
            RunningMonitorWeakPowerEquipmentDTO serverDto = weakPowerConverter.toWeakPowerEquipmentDto(server);
            //添加数据
            result.add(serverDto);
            // 查询服务器下的设备
            List<RunningMonitorWeakPowerEquipmentDTO> serverChildren =queryChildrenByDeviceType(equipIdsInSystem,param, server, processedIds,1);
            if (CollectionUtils.isNotEmpty(serverChildren)) {
                serverDto.setChildren(serverChildren);
            }
            return result;
        }
        // 多个服务器，需要查找顶级服务器
        Map<String, DtpRwdEquipmentDTO> idToServerEquipMap = serverInstances.stream().collect(Collectors.toMap(DtpRwdEquipmentDTO::getId, e -> e));
        //找顶级服务器实例
        Set<String> topLevelServerIds = findTopLevelServers(param, idToServerEquipMap.keySet(), idToServerEquipMap.keySet());
        List<DtpRwdEquipmentDTO> topLevelServers = topLevelServerIds.stream().map(e -> idToServerEquipMap.get(e)).collect(Collectors.toList());
        //topLevelServers 根据名称排序
        // 获取中文拼音排序器
        Collator collator = Collator.getInstance(Locale.CHINA);   // 或 Locale.SIMPLIFIED_CHINESE
        collator.setStrength(Collator.PRIMARY);                   // 忽略大小写/全半角差异
        topLevelServers.sort(Comparator.comparing(DtpRwdEquipmentDTO::getLocalName,collator));
        //多线程查询
        List<Future<RunningMonitorWeakPowerEquipmentDTO>> futureList = topLevelServers.stream().map(topServer ->
                BusinessThreadPoolExecutor.ttlExecutor().submit(() -> {
                    RunningMonitorWeakPowerEquipmentDTO topServerDto = weakPowerConverter.toWeakPowerEquipmentDto(topServer);
                    // 查询每个顶级服务器下的设备树
                    List<RunningMonitorWeakPowerEquipmentDTO> serverChildren = queryChildrenByDeviceType(equipIdsInSystem,param, topServerDto, processedIds,1);
                    if (!serverChildren.isEmpty()) {
                        topServerDto.setChildren(serverChildren);
                    }
                    return topServerDto ;
                })).collect(toList());

        futureList.stream().map(element -> {
            try {
                return element.get();
            } catch (Exception e) {
                logProjectInstance().error("Querying weak electrical relationship diagram data for anomalies:", e);
                throw new ApplicationException(ResponseCode.SYSTEM_ERROR_B0001.getCode(), "Query data exception，reason：" + ExceptionUtil.stacktraceToString(e.getCause()), "Weak electrical relationship diagram query failed");
            }
        }).forEach(result::add);
        return result;
    }

    /**
     * 构建查询参数
     */
    private DtpRwdObjectQueryParam buildQueryParam(RunningMonitorSecondTypeEnum type, DtpRwdClassCodeEnum classCode,
                                                   Set<String> objectIds) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        // 还未交付先注释 queryParam.filterMonitoringSurveillanceStatus();
        WeakPowerRelationContext context = new WeakPowerRelationContext();
        context.setRunningMonitorSecondType(type);
        context.setClassCode(classCode);
        context.setObjectIds(objectIds);
        context.setQueryParam(queryParam);
        return context.fillObjectQueryParam();
    }

    /**
     * 校验
     */
    private void validateParameters(RunningMonitorEquipmentQueryParam param, Set<String> systemIds) {
        if (Objects.isNull(param.getRunningMonitorSecondType())) {
            throw new IllegalArgumentException(LocaleMessageUtil.getMessage("key_4a1af0119a4f4fdc8f6cfe10e1ea451e"));
        }
        if (CollectionUtils.isEmpty(systemIds)) {
            throw new IllegalArgumentException(LocaleMessageUtil.getMessage("key_6fc14063bc9143f19673d1d21b6c5e4d"));
        }
    }


    /**
     * 填充设备子节点
     *
     * @param param  查询参数
     * @param target 目标源
     * @param equipmentId 已添加的设备 ID 用于全局设备去重
     * @return 目标源
     * @since 7.250301.0
     */
    private List<RunningMonitorWeakPowerEquipmentDTO> fillEquipmentChildren(@NonNull RunningMonitorEquipmentQueryParam param,
                                                                            @NonNull List<RunningMonitorWeakPowerEquipmentDTO> target,
                                                                            @NonNull Set<String> equipmentId) {
        if (CollectionUtils.isEmpty(target)) {
            return target;
        }
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        Map<String, List<RunningMonitorWeakPowerEquipmentDTO>> classCodeAndEquipments =
                target.stream().collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode));
        for (Map.Entry<String, List<RunningMonitorWeakPowerEquipmentDTO>> entry : classCodeAndEquipments.entrySet()) {
            DtpRwdClassCodeEnum classCode = DtpRwdClassCodeEnum.analyze(entry.getKey());
            List<RunningMonitorWeakPowerEquipmentDTO> equipments = entry.getValue();
            Set<String> equipmentIds = equipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            DtpRwdObjectQueryParam queryParam = buildQueryParam(runningMonitorSecondType, classCode, equipmentIds);
            if (Objects.isNull(queryParam)) {
                continue;
            }
            List<DtpRwdEquipmentDTO> childrenEquipments = Collections.synchronizedList(new ArrayList<>());
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                queryObject(param, queryParam, childrenEquipments, equipmentId);
            }, BusinessThreadPoolExecutor.ttlExecutor());

            CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                //进行一次反向查询
                DtpRwdObjectQueryParam queryParam2 = buildQueryParam(runningMonitorSecondType, classCode, equipmentIds);
                DtpRwdObjectRelationQueryParam relationQuery = queryParam2.getRelationTo();
                relationQuery.setObjTo(relationQuery.getObjFrom());
                relationQuery.setObjFrom(new ArrayList<>());
                queryParam2.setRelationFrom(relationQuery);
                queryParam2.setRelationTo(null);
                queryObject(param, queryParam2, childrenEquipments, equipmentId);
            }, BusinessThreadPoolExecutor.ttlExecutor());
            // 等待所有任务完成
            CompletableFuture.allOf(future1, future2).join();

            if (CollectionUtils.isEmpty(childrenEquipments)) {
                continue;
            }
            List<RunningMonitorWeakPowerEquipmentDTO> children = new ArrayList<>();
            for (DtpRwdEquipmentDTO childrenEquipment : childrenEquipments) {
                RunningMonitorWeakPowerEquipmentDTO childrenItem =
                        weakPowerConverter.toWeakPowerEquipmentDto(childrenEquipment);
                children.add(childrenItem);
            }
            //每个层级 ID 不同直接添加
            Map<String, List<RunningMonitorWeakPowerEquipmentDTO>> relObjIdDtoMap =
                    DtpRwdObjectDTO.mapRelObjIdAndData(children);
            //映射子节点
            target.forEach(equipment -> {
                if (CollectionUtils.isEmpty(relObjIdDtoMap.get(equipment.getId()))) {
                    return;
                }
                if (CollectionUtils.isEmpty(equipment.getChildren())) {
                    equipment.setChildren(relObjIdDtoMap.get(equipment.getId()));
                } else {
                    equipment.getChildren().addAll((relObjIdDtoMap.get(equipment.getId())));
                }

            });
            fillEquipmentChildren(param, children, equipmentId);
        }
        return target;
    }

    /**
     * 查询对象 通过 ID 去重
     */
    private void queryObject(RunningMonitorEquipmentQueryParam param, DtpRwdObjectQueryParam queryParam,
                             List<DtpRwdEquipmentDTO> childrenEquipments, Set<String> equipmentId) {
        List<DtpRwdEquipmentDTO> dto = param.queryObject(queryParam);
        if (CollectionUtils.isNotEmpty(dto)) {
            //通过 ID 做一次去重
            Map<String, DtpRwdEquipmentDTO> uniqueDto = dto.stream()
                    .collect(Collectors.toMap(DtpRwdEquipmentDTO::getId,
                            item -> item,
                            (existing, replacement) -> existing));
            //删除已经存在了的 ID
            uniqueDto.values().removeIf(item -> equipmentId.contains(item.getId()));
            //把新的 id 添加进去
            equipmentId.addAll(uniqueDto.keySet());
            childrenEquipments.addAll(uniqueDto.values());
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
        return "运行检测-弱电策略";
    }

    /**
     * 查询系统实例信息
     */
    private List<DtpRwdEquipmentDTO> querySystems(RunningMonitorEquipmentQueryParam param, Set<String> systemIds) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(new ArrayList<>(systemIds));
        // 追加查询字段
        if (param.getWithColumns() != null) {
            param.getWithColumns().forEach(queryParam::addWithColumn);
        }

        return param.queryObject(queryParam);
    }

    /**
     * 查询系统下的服务器
     */
    private List<DtpRwdEquipmentDTO> queryEquipsInSystem(RunningMonitorEquipmentQueryParam param, Set<String> systemIds) {
        //构造条件
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(systemIds));
        //查询数据
        return param.queryObject(queryParam);
    }

    /**
     * 查找顶级服务器（处理服务器之间的上下级关系）
     */
    private Set<String> findTopLevelServers(RunningMonitorEquipmentQueryParam param, Set<String> allServerEquips,Set<String> serverIds) {
        // 查询服务器之间的网络关系，找上级
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(serverIds);
        queryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.WE_NETWORK).setObjTo(new ArrayList<>(serverIds)));
        List<DtpRwdEquipmentDTO> relatedServers = param.queryObject(queryParam);
        //定义父级关系
        Map<String,List<String>> parentIdToChildIds = new HashMap<>();
        //定义顶级节点
        Set<String> topLevelServerIds = new HashSet<>();
        // 找出被其他服务器连接的服务器（非顶级）
        for (DtpRwdEquipmentDTO relatedServer : relatedServers) {
            Set<String> relObjIds = relatedServer.getRelObjIds();
            if (relObjIds == null || relObjIds.isEmpty()) {
                continue;
            }
            if(allServerEquips.contains(relatedServer.getId())){
                //一定要是指定的服务器实例
                relObjIds.forEach(parentId -> parentIdToChildIds.computeIfAbsent(parentId, k -> new ArrayList<>()).add(relatedServer.getId()));
                topLevelServerIds.remove(relatedServer.getId());
            }
        }
        if(MapUtil.isEmpty(parentIdToChildIds)){
            //表示所有服务器都是顶级服务器
            return serverIds;
        }
        //和找到的去重,去掉找打顶节点的数据放入顶节点容器中
        List<String> noTop = parentIdToChildIds.values().stream().flatMap(e -> e.stream()).collect(Collectors.toList());
        topLevelServerIds.addAll(serverIds.stream().filter(e -> !noTop.contains(e)).collect(Collectors.toList()));
        //顶节点，继续寻找
        topLevelServerIds.addAll(parentIdToChildIds.keySet());
        return findTopLevelServers(param, allServerEquips, topLevelServerIds);
    }


    /**
     * 根据设备类型查询下级设备
     */
    private List<RunningMonitorWeakPowerEquipmentDTO> queryChildrenByDeviceType(List<String> equipIdsInSystem,RunningMonitorEquipmentQueryParam param, DtpRwdEquipmentDTO device, Set<String> processedIds,int level) {
        // opt B10（switch.opt.weakPowerRelationBatch）：下一层设备的关系查询按批预取
        return queryChildrenByDeviceType(equipIdsInSystem, param, device, processedIds, level,
                OptSwitch.on(OptSwitch.WEAK_POWER_RELATION_BATCH) ? new HashMap<>() : null);
    }

    private List<RunningMonitorWeakPowerEquipmentDTO> queryChildrenByDeviceType(List<String> equipIdsInSystem,RunningMonitorEquipmentQueryParam param, DtpRwdEquipmentDTO device, Set<String> processedIds,int level,
                                                                                 Map<String, List<DtpRwdEquipmentDTO>> prefetched) {
        if (level > weakPowerRelationConfig.getMaxHierarchyDepth()) { // 限制层级上限
            return Lists.newArrayList();
        }
        // 避免重复处理
        if (processedIds.contains(device.getId())) {
            return Lists.newArrayList();
        }
        processedIds.add(device.getId());
        String classCode = device.getClassCode();
        String deviceId = device.getId();

        //获取需要的查询关系
        List<WeakPowerRelationConfig.Relation> relations = weakPowerRelationConfig.getRelationByMasterCode(classCode);
        List<DtpRwdEquipmentDTO> allEquipList = new ArrayList<>();
        //缓存查询的设备id，因为有类型会查询两种关系再合并数据，两种关系查询的实例可能有相同的所以徐瑶去重
        List<String> allEquipIdList = new ArrayList<>();

        for (WeakPowerRelationConfig.Relation relation : relations) {
            // 查询关系数据（opt：已批量预取的直接取用）
            List<DtpRwdEquipmentDTO> equipmentDTOS = prefetched == null ? null : prefetched.remove(relationKey(relation, deviceId));
            if (equipmentDTOS == null) {
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
                queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        relation.getGraphType(),relation.getEdgeType()).setObjFrom(Lists.newArrayList(deviceId)));
                equipmentDTOS = param.queryObject(queryParam);
            }
            //去重
            equipmentDTOS = equipmentDTOS.stream().filter(e -> !allEquipIdList.contains(e.getId())).collect(Collectors.toList());
            //去重后添加到集合中
            allEquipIdList.addAll(equipmentDTOS.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList()));
            //添加数据
            allEquipList.addAll(equipmentDTOS);
        }
        allEquipList = allEquipList.stream().filter(it -> {
            if(weakPowerRelationConfig.getIntermediateDeviceTypes().contains(it.getClassCode()) && equipIdsInSystem.contains(it.getId())){
                //五大类 & 系统下的设备 保留
                return Boolean.TRUE;
            }
            if(!weakPowerRelationConfig.getIntermediateDeviceTypes().contains(it.getClassCode())){
                //非五大类，保留
                return Boolean.TRUE;
            }
            //其他情况过滤掉
            return Boolean.FALSE;
        }).collect(toList());

        //判空
        if (CollectionUtils.isEmpty(allEquipList)) {
            return Lists.newArrayList();
        }

        List<RunningMonitorWeakPowerEquipmentDTO> result = new ArrayList<>();
        //按实例的名称进行升序排列
        // 获取中文拼音排序器
        Collator collator = Collator.getInstance(Locale.CHINA);   // 或 Locale.SIMPLIFIED_CHINESE
        collator.setStrength(Collator.PRIMARY);
        allEquipList.sort(Comparator.comparing(DtpRwdEquipmentDTO::getLocalName,collator));
        if (prefetched != null && level + 1 <= weakPowerRelationConfig.getMaxHierarchyDepth()) {
            prefetchRelations(equipIdsInSystem, param, allEquipList, processedIds, prefetched);
        }
        for (DtpRwdEquipmentDTO weakPowerEquipmentDto: allEquipList) {
            RunningMonitorWeakPowerEquipmentDTO child = weakPowerConverter.toWeakPowerEquipmentDto(weakPowerEquipmentDto);
            // 避免循环引用
            if (processedIds.contains(child.getId())) {
                continue;
            }
            result.add(child);
            // 如果是需要继续查询的设备类型 & 设备是在系统下，递归查询
            if (weakPowerRelationConfig.getIntermediateDeviceTypes().contains(child.getClassCode()) && equipIdsInSystem.contains(child.getId())) {
                List<RunningMonitorWeakPowerEquipmentDTO> subChildren = queryChildrenByDeviceType(equipIdsInSystem,param, child, processedIds, level + 1, prefetched);
                child.setChildren(subChildren);
            }else {
                //不需要继续下钻查询时，添加到已处理列表中
                processedIds.add(child.getId());
            }
        }
        return result;
    }



    private static String relationKey(WeakPowerRelationConfig.Relation relation, String deviceId) {
        return relation.getGraphType() + "/" + relation.getEdgeType() + "|" + deviceId;
    }

    /**
     * opt B10：对接下来会递归下钻的兄弟设备，按关系类型合并成一次查询（objFrom 为多个设备，200 一批），
     * 再按 relObjIds 拆回各设备。结果只是原逐个查询的提前执行，递归顺序和 processedIds 判断不变。
     * 无法按 relObjIds 归属时不预取，该批设备在递归时按原逻辑逐个查询。
     */
    private void prefetchRelations(List<String> equipIdsInSystem, RunningMonitorEquipmentQueryParam param,
                                   List<DtpRwdEquipmentDTO> siblings, Set<String> processedIds,
                                   Map<String, List<DtpRwdEquipmentDTO>> prefetched) {
        Map<String, WeakPowerRelationConfig.Relation> relations = new LinkedHashMap<>();
        Map<String, LinkedHashSet<String>> idsByRelation = new LinkedHashMap<>();
        for (DtpRwdEquipmentDTO e : siblings) {
            if (!weakPowerRelationConfig.getIntermediateDeviceTypes().contains(e.getClassCode())
                    || !equipIdsInSystem.contains(e.getId()) || processedIds.contains(e.getId()) || e.getClassCode() == null) {
                continue;
            }
            for (WeakPowerRelationConfig.Relation r : weakPowerRelationConfig.getRelationByMasterCode(e.getClassCode())) {
                String rk = r.getGraphType() + "/" + r.getEdgeType();
                relations.putIfAbsent(rk, r);
                idsByRelation.computeIfAbsent(rk, k -> new LinkedHashSet<>()).add(e.getId());
            }
        }
        idsByRelation.forEach((rk, ids) -> {
            if (ids.size() < 2) {
                return;
            }
            WeakPowerRelationConfig.Relation r = relations.get(rk);
            for (List<String> part : Lists.partition(new ArrayList<>(ids), 200)) {
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
                queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        r.getGraphType(), r.getEdgeType()).setObjFrom(new ArrayList<>(part)));
                List<DtpRwdEquipmentDTO> rows;
                try {
                    rows = param.queryObject(queryParam);
                } catch (RuntimeException ex) {
                    // 预取失败不影响原流程：这批设备在递归时按原逻辑逐个查询（异常也在那时按原样抛出）
                    OptMetrics.incAlways("weakPower.relationBatch.error");
                    logProjectInstance().warn("弱电关系批量预取失败，退回逐个查询", ex);
                    continue;
                }
                Map<String, List<DtpRwdEquipmentDTO>> split = OptRelationBatch.splitBySource(part, rows,
                        DtpRwdObjectDTO::getRelObjIds, (row, source) -> {
                            DtpRwdEquipmentDTO copy = BeanUtil.toBean(row, DtpRwdEquipmentDTO.class);
                            copy.setRelObjIds(new HashSet<>(Collections.singleton(source)));
                            return copy;
                        });
                if (split == null) {
                    OptMetrics.incAlways("weakPower.relationBatch.fallback");
                    continue;
                }
                OptMetrics.add("weakPower.relationBatch.saved", part.size() - 1L);
                split.forEach((id, idRows) -> prefetched.put(r.getGraphType() + "/" + r.getEdgeType() + "|" + id, idRows));
            }
        });
    }
}
