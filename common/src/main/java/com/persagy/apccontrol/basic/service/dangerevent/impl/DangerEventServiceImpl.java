package com.persagy.apccontrol.basic.service.dangerevent.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.dangerevent.QueryByObjectParam;
import com.persagy.apccontrol.basic.model.vo.computeroom.ComputerRoomEquipmentVo;
import com.persagy.apccontrol.basic.model.vo.dangerevent.DangerEventObjectVO;
import com.persagy.apccontrol.basic.model.vo.dangerevent.DangerEventVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.outside.feign.client.ExceptionEventClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerEventQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerInstanceTreeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerEventInstance;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerEventResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerInstanceTreeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.ExceptionEventClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.service.dangerevent.DangerEventService;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorComputerRoomSecurityStrategy;
import com.persagy.apccontrol.basic.structure.event.converter.DangerEventConverter;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY;

/**
 * @author wxf
 * @description 风险事件服务
 * @date 19:04 2025/2/12
 **/
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DangerEventServiceImpl implements DangerEventService, LogComponent {
    private final ExceptionEventClient exceptionEventClient;
    private final RunningMonitorComputerRoomSecurityStrategy runningMonitorComputerRoomSecurityStrategy;
    //private final DangerEventResourceService dangerEventResourceService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final RedisCacheTemplate redisCacheTemplate;
    private final DangerEventConverter dangerEventConverter;
    private final ExceptionEventClientService exceptionEventClientService;
    private final DtpRwdClientService dtpRwdClientService;

    /**
     * 新缓存对象仓库，读取缓存对象数据。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    /**
     * 缓存对象模型状态服务，检查 isReady 状态。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    @Override
    public List<DangerEventVO> listByObject(@NonNull List<String> objectIds) {
        DangerEventQueryParam param = new DangerEventQueryParam();
        param.setRwdInstanceIds(objectIds);

        return list(param);
    }

    @Override
    public List<DangerEventVO> listByDangerEvent(@NonNull List<String> dangerEventIds) {
        DangerEventQueryParam param = new DangerEventQueryParam();
        param.setRiskEventInstanceTreeIds(dangerEventIds);

        return list(param);
    }

    private List<DangerEventVO> list(DangerEventQueryParam param) {
        //todo 提高查询效率
        List<DangerEventVO> dangerEvents = new ArrayList<>();
        List<DangerEventInstance> dangerEventInstances = exceptionEventClientService.listDangerEvent(param);


        if (dangerEventInstances != null) {
            for (DangerEventInstance record : dangerEventInstances) {
                DangerEventVO dangerEvent = dangerEventConverter.toDangerEventVO(record);
                dangerEvents.add(dangerEvent);
            }
        }
        return dangerEvents;
    }

    /**
     * 获取空间和空间下的设备信息
     * 今天数据走异步缓存刷新
     *
     * @param objectIds 空间 ID
     * @return 列表
     */
    private List<DangerEventObjectVO> querySpaceEquipment(List<String> objectIds) {
        List<DangerEventObjectVO> result = new ArrayList<>();
        //1.获取空间对象静态数据--------------
        //指定查询的字段
        Set<String> withColumns = new HashSet<>(Arrays.asList(DtpRwdObjectVO.Fields.id, DtpRwdObjectVO.Fields.localId, DtpRwdObjectVO.Fields.name,
                DtpRwdObjectVO.Fields.localName, DtpRwdObjectVO.Fields.classCode, DtpRwdObjectVO.Fields.objType, DtpRwdObjectVO.Fields.valid));
        //查询空间
        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
        spaceQueryParam.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.COMPUTER_ROOM_SECURITY);
        spaceQueryParam.setFilterObjectIds(new HashSet<>(objectIds));
        spaceQueryParam.setWithColumns(withColumns);
        List<DtpRwdSpaceDTO> spaces = runningMonitorComputerRoomSecurityStrategy.listSpaces(spaceQueryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptyList();
        }
        //查询空间下的设备
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.COMPUTER_ROOM_SECURITY);
        equipmentQueryParam.setFilterEquipmentInSpaceIds(new HashSet<>(objectIds));
        withColumns.add(DtpRwdObjectDTO.Fields.relObjIds);
        equipmentQueryParam.setWithColumns(withColumns);
        List<ComputerRoomEquipmentVo> equipments = runningMonitorComputerRoomSecurityStrategy.listEquipments(equipmentQueryParam);

        //获取空间ID下的设备 映射关系(1对多)
        Map<String, List<DtpRwdEquipmentDTO>> spaceIdEquipment = new HashMap<>();
        for (DtpRwdEquipmentDTO equipment : equipments) {
            Set<String> relObjIds = equipment.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relObjIds)) {
                for (String relObjId : relObjIds) {
                    spaceIdEquipment.computeIfAbsent(relObjId, k -> new ArrayList<>()).add(equipment);
                }
            }
        }

        //2.获取设备类型(空间的设备类型固定)--------------
        List<String> classCodes = equipments.stream().map(DtpRwdEquipmentDTO::getClassCode).collect(Collectors.toList());
        classCodes.add(spaces.get(0).getClassCode());
        Map<String, String> classCodeAndName = dtpKnowledgeClientService.mapClassCodeAndName(
                false, false, classCodes);

        //3.组装结果数据--------------
        spaces.forEach(space -> {
            //获取空间下的设备的数据
            List<DtpRwdEquipmentDTO> spaceEquipmentList = spaceIdEquipment.get(space.getId());
            List<DangerEventObjectVO> relDangerEventObjects = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(spaceEquipmentList)) {
                spaceEquipmentList.forEach(equipment -> {
                    DangerEventObjectVO vo = getDangerEventObjectVO(equipment, classCodeAndName, null);
                    relDangerEventObjects.add(vo);
                });
            }
            DangerEventObjectVO vo = getDangerEventObjectVO(space, classCodeAndName, relDangerEventObjects);
            result.add(vo);
        });
        return result;
    }

    private DangerEventObjectVO getDangerEventObjectVO(DtpRwdObjectDTO dtpRwdObjectDTO, Map<String, String> classCodeAndName,
                                                       List<DangerEventObjectVO> relDangerEventObjects) {

        DangerEventObjectVO vo = new DangerEventObjectVO();
        /*
        vo.setClassCodeName(classCodeAndName.get(dtpRwdObjectDTO.getClassCode()));
        vo.setRelDangerEventObjects(relDangerEventObjects);
        vo.setId(dtpRwdObjectDTO.getId());
        vo.setName(dtpRwdObjectDTO.getName());
        vo.setLocalId(dtpRwdObjectDTO.getLocalId());
        vo.setLocalName(dtpRwdObjectDTO.getLocalName());
        vo.setObjType(dtpRwdObjectDTO.getObjType());
        vo.setClassCode(dtpRwdObjectDTO.getClassCode());
        vo.setValid(dtpRwdObjectDTO.getValid());

         */
        return vo;
    }

    @Override
    public void pullAllDangerEvent() {
        //todo 1.风险事件返回结构整理；2.同步数据加锁；3. po对象要手动设置projectid?
        int page = 1;
        int pages = 1;
        DangerEventQueryParam param = new DangerEventQueryParam();

        try {
            while (page <= pages) {
                param.setCurrent(page);
                //String dangerEvents = exceptionEventClient.listDangerEvents(param);
                String dangerEvents = null;
                DangerEventResponse dangerEventResponse = JSONObject.parseObject(dangerEvents, DangerEventResponse.class);
                if (dangerEventResponse != null) {
                    pages = dangerEventResponse.getPages();
                    List<DangerEventInstance> records = dangerEventResponse.getRecords();
                    for (DangerEventInstance record : records) {
                        try {

                        } catch (Exception e) {
                            logProjectInstance().error("解析风险事件失败：", e);
                        }
                    }
                }
                page++;
            }
        } catch (Exception e) {
            logProjectInstance().error("获取风险事件失败：", e);
        }
    }

    /**
     * 查询风险事件对象
     *
     * @param param 参数
     * @return {@code List<DangerEventVO> }
     * @since 7.250902.0
     */
    @Override
    public List<DangerEventVO> listByDangerObject(QueryByObjectParam param) {
            long methodStart = System.currentTimeMillis();
            // 新缓存路径：3 路并行执行
            long isReadyStart = System.currentTimeMillis();
            boolean isReady = this.cacheObjectModelStatusService.isReady(param.getRunningMonitorType());
            logProjectInstance().info("[性能] listByDangerObject-isReady检查[串行] 耗时: " + (System.currentTimeMillis() - isReadyStart) + "ms, isReady: " + isReady);
            if (isReady) {
                return listByDangerObjectFromCache(param);
            }

            DangerEventQueryParam queryParam = new DangerEventQueryParam();
            queryParam.setRwdInstanceIds(param.getObjectIds());
            Map<String, DangerEventInstance> dangerEventInstanceMap = Maps.newHashMap();
            Map<String, DangerInstanceTreeResponse> dangerInstanceTreeMap = Maps.newHashMap();
            //查询风险事件
            long parallelStart = System.currentTimeMillis();
            AtomicLong task1Duration = new AtomicLong();
            AtomicLong task2Duration = new AtomicLong();
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                long feignStart = System.currentTimeMillis();
                List<DangerEventInstance> dangerEvents = exceptionEventClientService.listDangerEvent(queryParam);
                task1Duration.set(System.currentTimeMillis() - feignStart);
                dangerEventInstanceMap.putAll(dangerEvents.stream()
                        .collect(Collectors.toMap(e -> e.getId() + e.getRwdInstanceId(), Function.identity(), (v1, v2) -> v2)));
            }, BusinessThreadPoolExecutor.ttlExecutor());
            //查询风险事件实例树
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                long feignStart = System.currentTimeMillis();
                List<DangerInstanceTreeResponse> treeResponses = exceptionEventClientService.listDangerInstanceTree(DangerInstanceTreeQueryParam.of(param.getObjectIds(), param.getFollowMark()));
                task2Duration.set(System.currentTimeMillis() - feignStart);
                dangerInstanceTreeMap.putAll(treeResponses.stream()
                        .collect(Collectors.toMap(e -> e.getId() + e.getRwdInstanceId(), Function.identity(), (v1, v2) -> v2)));
            }, BusinessThreadPoolExecutor.ttlExecutor());
            //保证任务完成
            CompletableFuture.allOf(future, future1).join();
            long parallelTotal = System.currentTimeMillis() - parallelStart;
            long sumDuration = task1Duration.get() + task2Duration.get();
            String execMode = (parallelTotal < sumDuration) ? "并行" : "串行";
            logProjectInstance().info("[性能] listByDangerObject-2路执行(风险事件Feign+实例树Feign)[" + execMode + "] 耗时: " + parallelTotal + "ms, 风险事件(Feign): " + task1Duration.get() + "ms, 实例树(Feign): " + task2Duration.get() + "ms, 风险事件数据量: " + dangerEventInstanceMap.size() + ", 实例树数据量: " + dangerInstanceTreeMap.size());
            if (MapUtils.isEmpty(dangerInstanceTreeMap) || MapUtils.isEmpty(dangerEventInstanceMap)) {
                return Collections.emptyList();
            }
            long queryLocalNameStart = System.currentTimeMillis();
            Map<String, String> objectIdAndLocalName = dtpRwdClientService.mapObjectIdAndLocalName(param.getObjectIds());
            logProjectInstance().info("[性能] listByDangerObject-查询对象名称(BDTP Feign)[串行] 耗时: " + (System.currentTimeMillis() - queryLocalNameStart) + "ms, 数据量: " + objectIdAndLocalName.size());
            List<DangerEventVO> result = new ArrayList<>();
            //合并风险事件状态和实例树
            dangerEventInstanceMap.forEach((key, value) -> {
                DangerInstanceTreeResponse treeResponse = dangerInstanceTreeMap.get(key);
                if (Objects.isNull(treeResponse)) {
                    return;
                }
                DangerEventVO dangerEventVO = dangerEventConverter.toDangerEventVO(value, treeResponse);
                dangerEventVO.setLocalName(objectIdAndLocalName.get(treeResponse.getRwdInstanceId()));
                result.add(dangerEventVO);
            });
            logProjectInstance().info("[性能] listByDangerObject-总耗时 耗时: " + (System.currentTimeMillis() - methodStart) + "ms, 结果数据量: " + result.size());
            return result;
    }

    /**
     * 从新缓存读取名称映射并与 2 次 ExceptionEvent Feign 3 路并行执行（API 5）。
     *
     * <p>将 BDTP {@code mapObjectIdAndLocalName} 替换为
     * {@code CacheObjectRepository.listByObjectIds} 本地 DB 查询，
     * 并将其与 2 次 ExceptionEvent Feign 改为 3 路 CompletableFuture 并行执行。</p>
     *
     * @param param 查询参数
     * @return 风险事件 VO 列表
     * @since v7.260802.0
     */
    private List<DangerEventVO> listByDangerObjectFromCache(@NonNull QueryByObjectParam param) {
        long cacheStart = System.currentTimeMillis();
        DangerEventQueryParam queryParam = new DangerEventQueryParam();
        queryParam.setRwdInstanceIds(param.getObjectIds());
        Map<String, DangerEventInstance> dangerEventInstanceMap = Maps.newHashMap();
        Map<String, DangerInstanceTreeResponse> dangerInstanceTreeMap = Maps.newHashMap();
        Map<String, String> objectIdAndLocalName = new ConcurrentHashMap<>();

        // 3 路并行执行
        long parallelStart = System.currentTimeMillis();
        AtomicLong task1Duration = new AtomicLong();
        AtomicLong task2Duration = new AtomicLong();
        AtomicLong task3Duration = new AtomicLong();
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            long feignStart = System.currentTimeMillis();
            List<DangerEventInstance> dangerEvents = exceptionEventClientService.listDangerEvent(queryParam);
            task1Duration.set(System.currentTimeMillis() - feignStart);
            dangerEventInstanceMap.putAll(dangerEvents.stream()
                    .collect(Collectors.toMap(e -> e.getId() + e.getRwdInstanceId(),
                            Function.identity(), (v1, v2) -> v2)));
        }, BusinessThreadPoolExecutor.ttlExecutor());

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            long feignStart = System.currentTimeMillis();
            List<DangerInstanceTreeResponse> treeResponses = exceptionEventClientService.listDangerInstanceTree(
                            DangerInstanceTreeQueryParam.of(param.getObjectIds(), param.getFollowMark()));
            task2Duration.set(System.currentTimeMillis() - feignStart);
            dangerInstanceTreeMap.putAll(treeResponses.stream()
                    .collect(Collectors.toMap(e -> e.getId() + e.getRwdInstanceId(),
                            Function.identity(), (v1, v2) -> v2)));
        }, BusinessThreadPoolExecutor.ttlExecutor());

        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            long dbStart = System.currentTimeMillis();
            List<DtpRwdObjectDTO> cachedObjects = this.cacheObjectRepository.listByObjectIds(
                    param.getObjectIds(), Arrays.asList("id", "localName"));
            task3Duration.set(System.currentTimeMillis() - dbStart);
            for (DtpRwdObjectDTO obj : cachedObjects) {
                if (obj.getLocalName() != null) {
                    objectIdAndLocalName.put(obj.getId(), obj.getLocalName());
                }
            }
        }, BusinessThreadPoolExecutor.ttlExecutor());

        CompletableFuture.allOf(future1, future2, future3).join();
        long parallelTotal = System.currentTimeMillis() - parallelStart;
        long sumDuration = task1Duration.get() + task2Duration.get() + task3Duration.get();
        String execMode = (parallelTotal < sumDuration) ? "并行" : "串行";
        logProjectInstance().info("[性能] listByDangerObjectFromCache-3路执行[" + execMode + "] 耗时: " + parallelTotal + "ms, 风险事件(Feign): " + task1Duration.get() + "ms, 实例树(Feign): " + task2Duration.get() + "ms, 缓存查询(DB): " + task3Duration.get() + "ms, 风险事件数据量: " + dangerEventInstanceMap.size() + ", 实例树数据量: " + dangerInstanceTreeMap.size());

        if (MapUtils.isEmpty(dangerInstanceTreeMap) || MapUtils.isEmpty(dangerEventInstanceMap)) {
            return Collections.emptyList();
        }

        List<DangerEventVO> result = new ArrayList<>();
        dangerEventInstanceMap.forEach((key, value) -> {
            DangerInstanceTreeResponse treeResponse = dangerInstanceTreeMap.get(key);
            if (Objects.isNull(treeResponse)) {
                return;
            }
            DangerEventVO dangerEventVO = dangerEventConverter.toDangerEventVO(value, treeResponse);
            dangerEventVO.setLocalName(objectIdAndLocalName.get(treeResponse.getRwdInstanceId()));
            result.add(dangerEventVO);
        });
        logProjectInstance().info("[性能] listByDangerObjectFromCache-总耗时: " + (System.currentTimeMillis() - cacheStart) + "ms, 结果数据量: " + result.size());
        return result;
    }

    public List<DangerEventVO> listByDangerObjectOld(QueryByObjectParam param) {
        DangerEventQueryParam queryParam = new DangerEventQueryParam();
        queryParam.setRwdInstanceIds(param.getObjectIds());
        Map<String, DangerEventInstance> dangerEventInstanceMap = Maps.newHashMap();
        Map<String, DangerInstanceTreeResponse> dangerInstanceTreeMap = Maps.newHashMap();
        //查询风险事件
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            dangerEventInstanceMap.putAll(exceptionEventClientService.listDangerEvent(queryParam).stream()
                    .collect(Collectors.toMap(e -> e.getId() + e.getRwdInstanceId(), Function.identity(), (v1, v2) -> v2)));
        }, BusinessThreadPoolExecutor.ttlExecutor());
        //查询风险事件实例树
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            dangerInstanceTreeMap.putAll(exceptionEventClientService.listDangerInstanceTree(DangerInstanceTreeQueryParam.of(param.getObjectIds(), param.getFollowMark()))
                    .stream().collect(Collectors.toMap(e -> e.getId() + e.getRwdInstanceId(), Function.identity(), (v1, v2) -> v2)));
        }, BusinessThreadPoolExecutor.ttlExecutor());
        //保证任务完成
        CompletableFuture.allOf(future, future1).join();
        if (MapUtils.isEmpty(dangerInstanceTreeMap) || MapUtils.isEmpty(dangerEventInstanceMap)) {
            return Collections.emptyList();
        }
        Map<String, String> objectIdAndLocalName = dtpRwdClientService.mapObjectIdAndLocalName(param.getObjectIds());
        List<DangerEventVO> result = new ArrayList<>();
        //合并风险事件状态和实例树
        dangerEventInstanceMap.forEach((key, value) -> {
            DangerInstanceTreeResponse treeResponse = dangerInstanceTreeMap.get(key);
            if (Objects.isNull(treeResponse)) {
                return;
            }
            DangerEventVO dangerEventVO = dangerEventConverter.toDangerEventVO(value, treeResponse);
            dangerEventVO.setLocalName(objectIdAndLocalName.get(treeResponse.getRwdInstanceId()));
            result.add(dangerEventVO);
        });
        return result;
    }

    @Override
    public String logTag() {
        return "风险事件";
    }
}
