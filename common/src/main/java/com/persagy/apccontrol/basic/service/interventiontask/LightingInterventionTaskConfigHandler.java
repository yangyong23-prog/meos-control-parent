package com.persagy.apccontrol.basic.service.interventiontask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskInstanceDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskTimeDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.LightingInterventionTaskConfigDataDTO;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogMeosMsgDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionTaskConfig;
import com.persagy.apccontrol.basic.model.enumtype.InterventionTaskConfigDataTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.intervention.InterventionExecuteTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.intervention.InterventionInstanceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogKeyWordEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.EquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.SpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.datatransfer.PaasDataTransferParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigOutsideQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskManualControlParam;
import com.persagy.apccontrol.basic.model.param.lighting.LightingSpaceFuncTypeCircuitParam;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigSaveOrUpdateVO;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSpaceFuncTypeCircuitVO;
import com.persagy.apccontrol.basic.outside.feign.client.DtpMeosServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.DtpMeosCircuitDemandParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupResource;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupDetailQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import com.persagy.apccontrol.basic.service.demand.instancegroup.DemandInstanceGroupDetailService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorLightingStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.structure.performance.StopWatch;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.DateTypeEnum;
import com.persagy.validate.utility.executor.DateTimeRangeExecutor;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 照明干预处理类
 */
@Slf4j
@Service("lightingInterventionTaskConfigHandler")
public class LightingInterventionTaskConfigHandler extends AbstractInterventionTaskConfigHandler implements LogComponent {

    @Resource
    private DemandLightingStrategy demandLightingStrategy;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private DemandInstanceGroupDetailService demandInstanceGroupDetailService;
    @Resource
    private DemandInstanceGroupResource demandInstanceGroupResource;
    @Resource
    private RunningMonitorLightingStrategy runningMonitorLightingStrategy;
    @Resource
    private DtpMeosServerClient dtpMeosServerClient;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private RedisCacheTemplate redisCacheTemplate;

    @NonNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.LIGHTING;
    }

    @Override
    public InterventionTaskConfigSaveOrUpdateVO saveOrUpdate(InterventionTaskConfigDTO dto) {
        List<InterventionTaskConfigSaveOrUpdateVO.LightingRepetition> lightingRepetitions = this.checkRepeat(dto);
        if (CollectionUtils.isNotEmpty(lightingRepetitions)) {
            InterventionTaskConfigSaveOrUpdateVO vo = new InterventionTaskConfigSaveOrUpdateVO();
            vo.setErrorCode("TIME_REPETITION_ERROR");
            vo.setLightingRepetitions(lightingRepetitions);
            return vo;
        }
        // 填充执行时间
        List<InterventionTaskTimeDTO> timeSlot = dto.getTimeSlot();
        InterventionTaskTimeDTO taskTime = dto.getTimeSlot().get(0);
        InterventionTaskConfigDTO.Lighting configData = this.parseConfig(dto.getConfigData());
        // 只修改非人工手动执行任务的执行时间，人工手动任务走手动控制修改
        if (configData.getType() != InterventionExecuteTypeEnum.MANUAL) {
            LocalDateTime now = DateUtils.now();
            LocalDate today = now.toLocalDate();
            LinkedList<Range<ChronoLocalDateTime<?>>> executeTimeRanges = new LinkedList<>();
            LinkedList<Range<ChronoLocalDateTime<?>>> existRanges;
            boolean edit = dto.getTaskId() != null;
            boolean changeRange = false;
            if (edit) {
                InterventionTaskConfigVO originConfig = this.interventionTaskConfigService.listByTaskId(
                        dto.getRunningMonitorType(), dto.getRunningMonitorSecondType(), dto.getTaskId());
                // 判断执行时间是否有修改
                if (!taskTime.getBeginTime().isEqual(originConfig.getBeginTime())
                        || !taskTime.getEndTime().isEqual(originConfig.getEndTime())) {
                    changeRange = true;
                }
                // 找到已执行的执行时间添加到执行时间范围中
                existRanges = this.parseConfig(originConfig.getConfigData()).getExecuteTimeRanges();
                existRanges.removeIf(range -> {
                    if (range.getMaximum().isBefore(now)) {
                        executeTimeRanges.add(range);
                        return true;
                    }
                    return false;
                });
            } else {
                existRanges = new LinkedList<>();
            }
            switch (configData.getType()) {
                case ONCE:
                case IMMEDIATELY:
                    // 立即执行和仅生效一次永远仅存在一个时间段
                    executeTimeRanges.addAll(timeSlot.stream()
                            .map(slot -> Range.between(slot.getBeginTime(), slot.getEndTime()))
                            .collect(Collectors.toList()));
                    break;
                case MULTI:
                    if (changeRange) {
                        for (Range<ChronoLocalDateTime<?>> range : existRanges) {
                            if (range.contains(now)) {
                                executeTimeRanges.add(Range.between(range.getMinimum(), now));
                            }
                        }
                    }
                    Boolean acrossDay = configData.getAcrossDay();
                    for (InterventionTaskTimeDTO slot : timeSlot) {
                        LocalTime beginTime = slot.getBeginTime().toLocalTime();
                        LocalTime endTime = slot.getEndTime().toLocalTime();
                        DateTimeRangeExecutor.generateDate(DateTypeEnum.DAY,
                                slot.getBeginTime().toLocalDate(), slot.getEndTime().toLocalDate(),
                                true, eachDate -> {
                                    // 跳过已执行的日期，已执行过的时间段已经添加到默认的执行时间范围中
                                    if (eachDate.isBefore(today)) {
                                        return;
                                    }
                                    LocalDateTime finalBeginTime = LocalDateTime.of(eachDate, beginTime);
                                    LocalDateTime finalEndTime = LocalDateTime.of(eachDate, endTime);
                                    if (BooleanUtils.isTrue(acrossDay)) {
                                        finalEndTime = finalEndTime.plusDays(1);
                                    }
                                    executeTimeRanges.add(Range.between(finalBeginTime, finalEndTime));
                                });
                        if (BooleanUtils.isTrue(acrossDay)) {
                            slot.setEndTime(slot.getEndTime().plusDays(1));
                        }
                    }
                    break;
            }
            // 保存时或修改时间范围时更新执行时间
            if (!edit || changeRange) {
                executeTimeRanges.sort(Comparator.comparing(Range::getMaximum, Collections.reverseOrder()));
                configData.setExecuteTimeRanges(executeTimeRanges);
                dto.setConfigData(ObjectMapperUtils.convertValue(configData, JsonNode.class));
            }
        }

        // 保存数据
        InterventionTaskConfigSaveOrUpdateVO result = this.transactionTemplate.execute(
                status -> super.saveOrUpdate(dto));
        // 数据生效后，触发回路计算通知
        this.sendCircutDemandCalculateNotice(dto.getRunningMonitorSecondType());
        return result;
    }

    @Override
    public void del(Long taskId, boolean logRecord) {
        this.transactionTemplate.execute(status -> {
            super.del(taskId, logRecord);
            return status;
        });
        // 数据生效后，触发回路计算通知
        this.interventionTaskConfigService.lambdaQuery()
                .eq(InterventionTaskConfig::getProjectId, ContextUtil.getProjectId())
                .eq(InterventionTaskConfig::getTaskId, taskId).list().stream()
                .map(InterventionTaskConfig::getRunningMonitorSecondType)
                .forEach(this::sendCircutDemandCalculateNotice);
    }

    @Override
    public boolean manualControl(InterventionTaskManualControlParam param) {
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        Long taskId = param.getTaskId();
        LocalDateTime controlEndTime = param.getControlEndTime();
        LocalDateTime now = DateUtils.now();

        InterventionTaskConfigVO config = this.interventionTaskConfigService.listByTaskId(
                runningMonitorType, runningMonitorSecondType, taskId);
        if (config == null) {
            return false;
        }
        InterventionTaskConfigDTO.Lighting configData = this.parseConfig(config.getConfigData());
        LinkedList<Range<ChronoLocalDateTime<?>>> executeTimeRanges = Optional.ofNullable(
                configData.getExecuteTimeRanges()).orElseGet(LinkedList::new);
        // 存在手动结束时间时则为手动执行，否则就是提前结束
        if (controlEndTime != null) {
            // 只有手动执行的配置才允许手动控制
            if (configData.getType() != InterventionExecuteTypeEnum.MANUAL) {
                return false;
            }
            // 找到当前时间所在的执行时间范围，并更新其结束时间为控制截止时间
            boolean containsRunningRage = false;
            for (int i = 0; i < executeTimeRanges.size(); i++) {
                Range<ChronoLocalDateTime<?>> rage = executeTimeRanges.get(i);
                if (rage.contains(now)) {
                    executeTimeRanges.set(i, Range.between(rage.getMinimum(), controlEndTime));
                    containsRunningRage = true;
                }
            }
            // 如果没有找到当前时间所在的执行时间范围，则添加一个新的执行时间范围
            if (!containsRunningRage) {
                executeTimeRanges.addFirst(Range.between(now, controlEndTime));
            }
        } else {
            // 找到当前时间所在的执行时间范围，并更新其结束时间为当前时间
            for (int i = 0; i < executeTimeRanges.size(); i++) {
                Range<ChronoLocalDateTime<?>> range = executeTimeRanges.get(i);
                if (range.contains(now)) {
                    executeTimeRanges.set(i, Range.between(range.getMinimum(), now));
                }
            }
        }
        executeTimeRanges.sort(Comparator.comparing(Range::getMaximum, Collections.reverseOrder()));
        configData.setExecuteTimeRanges(executeTimeRanges);
        List<InterventionTaskConfig> configs = this.interventionTaskConfigService.lambdaQuery()
                .eq(InterventionTaskConfig::getTaskId, taskId)
                .eq(InterventionTaskConfig::getDataType, InterventionTaskConfigDataTypeEnum.MASTER)
                .list();
        if (CollectionUtils.isEmpty(configs)) {
            return false;
        }
        JsonNode jsonNode = ObjectMapperUtils.convertValue(configData, JsonNode.class);
        configs.forEach(item -> item.setConfigData(jsonNode));
        this.interventionTaskConfigService.updateByIdValidate(configs);
        this.paasDataTransferClientService.syncDbDataToCloud(PaasDataTransferParam.of(DataTypeEnum.EDIT, configs));
        // 数据生效后，触发回路计算通知
        this.sendCircutDemandCalculateNotice(runningMonitorSecondType);
        return true;
    }

    @Override
    public List<InterventionTaskConfigVO> queryList(InterventionTaskConfigQueryParam param) {
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        DemandTemplateTypeEnum templateType = runningMonitorSecondType.toDemandTemplateTypeEnum();
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        Set<String> instanceIds = param.getInstanceIds();
        // 查询实例下照明回路
        StopWatch stopWatch = StopWatch.create();
        stopWatch.start("查询照明回路");
        List<DtpRwdCircuitDTO> circuits = this.queryCircuits(templateType, instanceType, instanceIds);
        stopWatch.stop();
        this.globalStopWatch().addTask(stopWatch);
        if (CollectionUtils.isEmpty(circuits)) {
            return Collections.emptyList();
        }
        return this.queryList(param, circuits);
    }

    @Override
    public List<LightingInterventionTaskConfigDataDTO> outsideQueryTaskConfigListList(
            InterventionTaskConfigOutsideQueryParam param) {
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        DemandTemplateTypeEnum templateType = runningMonitorSecondType.toDemandTemplateTypeEnum();
        LocalDateTime taskTime = param.getTaskTime();
        // 查询实例下照明回路
        List<DtpRwdCircuitDTO> circuits = this.queryCircuits(templateType,
                DemandInstanceTypeEnum.PROJECT, Sets.newHashSet(ContextUtil.getProjectId()));
        if (CollectionUtils.isEmpty(circuits)) {
            return Collections.emptyList();
        }
        Set<String> circuitIds = new HashSet<>();
        Set<String> queryInstanceIds = new HashSet<>();
        Map<String, Set<String>> spaceId2CircuitIds = new HashMap<>();
        for (DtpRwdCircuitDTO circuit : circuits) {
            circuitIds.add(circuit.getId());
            List<DtpRwdSpaceDTO> serviceSpaces = circuit.getServiceSpaces();
            if (CollectionUtils.isNotEmpty(serviceSpaces)) {
                queryInstanceIds.addAll(serviceSpaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
                serviceSpaces.forEach(space -> MapExecutor.dynamicPut(spaceId2CircuitIds,
                        space.getId(), circuit.getId(), HashSet::new));
            }
        }
        // 查询回路分组 ID -> 分组下回路 ID
        Map<String, Set<String>> groupId2CircuitIds = this.queryGroupId2CircuitIds(templateType, circuitIds);
        queryInstanceIds.addAll(groupId2CircuitIds.keySet());
        // 查询干预配置 - 配置中可能包含跨天数据，所以需要将结束时间设置为下一天
        List<InterventionTaskConfigVO> configs = interventionTaskConfigService.queryListByVO(
                runningMonitorType, runningMonitorSecondType, null,
                queryInstanceIds, taskTime, taskTime.plusDays(1));
        if (CollectionUtils.isEmpty(configs)) {
            return Collections.emptyList();
        }
        // 处理结果数据
        this.handleInterventionConfigs(configs, taskTime, circuitIds, groupId2CircuitIds);
        if (CollectionUtils.isEmpty(configs)) {
            return Collections.emptyList();
        }
        // 过滤不在执行时间范围内的配置
        configs.removeIf(interventionConfig -> {
            InterventionTaskConfigDTO.Lighting configData = this.parseConfig(interventionConfig.getConfigData());
            LinkedList<Range<ChronoLocalDateTime<?>>> executeTimeRanges = configData.getExecuteTimeRanges();
            return CollectionUtils.isEmpty(executeTimeRanges) || executeTimeRanges.stream()
                    .noneMatch(range -> Range.between(range.getMinimum(),
                            range.getMaximum().minus(Duration.ofMinutes(1))).contains(taskTime));
        });
        if (CollectionUtils.isEmpty(configs)) {
            return Collections.emptyList();
        }
        // 构建回路 ID 到设置模式的映射
        Map<String, DemandLightingCircuitSettingModeEnum> circuitId2SettingMode = this.buildCircuitId2SettingModel(
                configs, spaceId2CircuitIds, groupId2CircuitIds);
        // 转换结果
        return circuitId2SettingMode.entrySet().stream()
                .map(entry -> new LightingInterventionTaskConfigDataDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // 保存日志
    @Override
    public void addLogMsg(InterventionTaskConfig cfg, RunningLogKeyWordEnum logKeyWord) {
        NowRunningLogCategory firstLevelCategory = NowRunningLogCategory.lightxplantrunmonitor;
        String secondLevelCategory = NowRunningLogCategory.lightxplantrunmonitor_LTLTCC.getCode();
        if (cfg.getRunningMonitorSecondType().toDemandTemplateTypeEnum().isNightLighting()) {
            firstLevelCategory = NowRunningLogCategory.odlightxplantrunmonitor;
            secondLevelCategory = NowRunningLogCategory.odlightxplantrunmonitor_LTLTCC.getCode();
        }
        String name = "\"" + cfg.getRunningMonitorSecondType().getReasonPhrase() + CommonConst.MINUS + cfg.getTaskName() + "\"";

        runningLogAddService.saveByMeosMsg(RunningLogMeosMsgDTO.buildManual(cfg.getTaskId().toString(), name,
                logKeyWord.getReasonPhrase(),
                firstLevelCategory, secondLevelCategory, RunningLogSourceTypeEnum.MEOS_LIGHTING_INTERVENTION_TASK));
    }

    /**
     * 查询列表
     *
     * @param param    查询参数
     * @param circuits 回路列表
     * @return 列表
     * @since 7.260101.0
     */
    public List<InterventionTaskConfigVO> queryList(
            InterventionTaskConfigQueryParam param,
            List<DtpRwdCircuitDTO> circuits) {
        if (CollectionUtils.isEmpty(circuits)) {
            return Collections.emptyList();
        }
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        DemandTemplateTypeEnum templateType = runningMonitorSecondType.toDemandTemplateTypeEnum();
        LocalDateTime beginTime = param.getBeginTime();
        LocalDateTime endTime = param.getEndTime();

        Set<String> circuitIds = new HashSet<>();
        Set<String> queryInstanceIds = new HashSet<>();
        for (DtpRwdCircuitDTO circuit : circuits) {
            circuitIds.add(circuit.getId());
            List<DtpRwdSpaceDTO> serviceSpaces = circuit.getServiceSpaces();
            if (CollectionUtils.isNotEmpty(serviceSpaces)) {
                queryInstanceIds.addAll(serviceSpaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
            }
        }
        // 查询回路分组 ID -> 分组下回路 ID
        StopWatch stopWatch = StopWatch.create();
        stopWatch.start("查询回路编组配置");
        Map<String, Set<String>> groupId2CircuitIds = this.queryGroupId2CircuitIds(templateType, circuitIds);
        stopWatch.stop();
        queryInstanceIds.addAll(groupId2CircuitIds.keySet());
        // 查询干预配置
        stopWatch.start("查询干预配置");
        List<InterventionTaskConfigVO> configs = this.interventionTaskConfigService.queryListByVO(
                runningMonitorType, runningMonitorSecondType, null, queryInstanceIds, beginTime, endTime);
        stopWatch.stop();
        if (CollectionUtils.isNotEmpty(configs)) {
            // 处理结果数据
            stopWatch.start("处理结果数据");
            this.handleInterventionConfigs(configs, beginTime, circuitIds, groupId2CircuitIds);
            stopWatch.stop();
        }
        this.globalStopWatch().addTask(stopWatch);
        return configs;
    }

    /**
     * 发送回路计算通知
     *
     * @param runningMonitorSecondType 运行监控二级类型
     * @since 7.260101.0
     */
    private void sendCircutDemandCalculateNotice(@NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType) {
        DtpMeosCircuitDemandParam param = new DtpMeosCircuitDemandParam();
        param.setTemplateType(runningMonitorSecondType.toDemandTemplateTypeEnum());
        param.setTime(LocalDateTime.now());
        param.setForcibleExecution(false);
        this.dtpMeosServerClient.circuitDemand(param);
    }

    /**
     * 处理结果数据
     *
     * @param configs            配置列表
     * @param beginTime          开始时间
     * @param circuitIds         所有照明回路 ID
     * @param groupId2CircuitIds 回路编组 ID -> 编组下回路ID
     * @since 7.260101.0
     */
    private void handleInterventionConfigs(
            List<InterventionTaskConfigVO> configs,
            LocalDateTime beginTime,
            Set<String> circuitIds,
            Map<String, Set<String>> groupId2CircuitIds) {
        // 处理结果数据
        Iterator<InterventionTaskConfigVO> iterator = configs.iterator();
        while (iterator.hasNext()) {
            InterventionTaskConfigVO config = iterator.next();
            InterventionTaskConfigDTO.Lighting configData = this.parseConfig(config.getConfigData());
            // 如果是回路编组配置，则设置回路ID
            if (config.getConfigObject().getInstanceType() == InterventionInstanceTypeEnum.CIRCUIT_GROUP) {
                List<String> dbInstanceIds = config.getConfigObject().getInstanceIds();
                configData.getConfig().setCircuitIds(dbInstanceIds.stream()
                        .flatMap(groupId -> groupId2CircuitIds.getOrDefault(groupId, Collections.emptySet()).stream())
                        .collect(Collectors.toSet()));
            }
            // 如果配置的是手动模式，则直接使用保存时的 belong 属性，无需处理
            // 如果配置的不是手动模式，则需要判断配置的回路是否属于当前查询实例
            if (configData.getType() != InterventionExecuteTypeEnum.MANUAL) {
                Set<String> dbCircuitIds = configData.getConfig().getCircuitIds();
                // 当配置中的回路和当前查询实例下的回路没有交集时，则移除该配置
                if (CollectionUtils.isEmpty(CollectionUtils.intersection(dbCircuitIds, circuitIds))) {
                    iterator.remove();
                    continue;
                }
                // 如果配置的执行时间范围都在开始时间之前，则移除该配置
                if (configData.getExecuteTimeRanges().stream()
                        .allMatch(range -> range.getMaximum().isBefore(beginTime))) {
                    iterator.remove();
                }
            }
            if (configData.getType() == InterventionExecuteTypeEnum.MULTI
                    && BooleanUtils.isTrue(configData.getAcrossDay())) {
                config.setEndTime(config.getEndTime().minusDays(1));
            }
            config.setConfigData(ObjectMapperUtils.convertValue(configData, JsonNode.class));
        }
    }

    /**
     * 构建回路 ID 到设置模式的映射
     *
     * @param configs            配置列表
     * @param spaceId2CircuitIds 空间 ID 到回路 ID 的映射
     * @param groupId2CircuitIds 回路编组 ID 到回路 ID 的映射
     * @return 回路 ID 到设置模式的映射
     * @since 7.260101.0
     */
    private Map<String, DemandLightingCircuitSettingModeEnum> buildCircuitId2SettingModel(
            List<InterventionTaskConfigVO> configs,
            Map<String, Set<String>> spaceId2CircuitIds,
            Map<String, Set<String>> groupId2CircuitIds) {
        Map<String, DemandLightingCircuitSettingModeEnum> circuitId2SettingMode = new HashMap<>();
        // 遍历配置列表,根据配置更新回路状态
        for (InterventionTaskConfigVO config : configs) {
            InterventionInstanceTypeEnum instanceType = config.getConfigObject().getInstanceType();
            List<String> instanceIds = config.getConfigObject().getInstanceIds();
            InterventionTaskConfigDTO.Lighting configData = this.parseConfig(config.getConfigData());
            Set<String> circuitIdsInConfig = configData.getConfig().getCircuitIds();
            DemandLightingCircuitSettingModeEnum settingMode = configData.getConfig().getSettingMode();

            Set<String> circuitIdsInInstance = Collections.emptySet();
            switch (instanceType) {
                case SPACE:
                    circuitIdsInInstance = instanceIds.stream()
                            .map(spaceId2CircuitIds::get)
                            .filter(CollectionUtils::isNotEmpty)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet());
                    break;
                case CIRCUIT_GROUP:
                    circuitIdsInInstance = instanceIds.stream()
                            .map(groupId2CircuitIds::get)
                            .filter(CollectionUtils::isNotEmpty)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet());
                    break;
                default:
                    break;
            }
            if (CollectionUtils.isNotEmpty(circuitIdsInInstance)
                    && CollectionUtils.isNotEmpty(circuitIdsInConfig)) {
                for (String circuitId : circuitIdsInInstance) {
                    // 如果当前回路已经设置为开，则保持开
                    if (circuitId2SettingMode.get(circuitId) == DemandLightingCircuitSettingModeEnum.OPEN) {
                        continue;
                    }
                    // 当前回路在配置中时，使用当前配置
                    if (circuitIdsInConfig.contains(circuitId)) {
                        circuitId2SettingMode.put(circuitId, settingMode);
                    }

                }
            }
        }
        return circuitId2SettingMode;
    }

    /**
     * 查询回路
     *
     * @param templateType 模板类型
     * @param instanceType 实例类型
     * @param instanceIds  实例 ID
     * @return 回路列表
     * @since 1.0.0
     */
    public List<DtpRwdCircuitDTO> queryCircuits(
            DemandTemplateTypeEnum templateType,
            DemandInstanceTypeEnum instanceType,
            Set<String> instanceIds) {
        EquipmentQueryParam equipmentQueryParam = new EquipmentQueryParam();
        SpaceQueryParam spaceQueryParam = new SpaceQueryParam();
        switch (instanceType) {
            case BUILDING:
                spaceQueryParam.setFilterBuildingIds(instanceIds);
                break;
            case FLOOR:
                spaceQueryParam.setFilterFloorIds(instanceIds);
                break;
            case SPACE:
                spaceQueryParam.setFilterObjectIds(instanceIds);
                break;
            default:
                break;
        }
        spaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        return redisCacheTemplate.asyncRefreshTemplate("lighting-interventionTask-circuits",
                templateType + instanceIds.toString(), () -> demandLightingStrategy.listCircuits(
                        Lists.newArrayList(templateType), equipmentQueryParam, spaceQueryParam));
    }

    /**
     * 查询回路
     *
     * @param templateType 模板类型
     * @param circuitIds   回路 ID
     * @return 回路 ID和回路组 ID
     */
    private Map<String, Set<String>> queryGroupId2CircuitIds(
            DemandTemplateTypeEnum templateType,
            Set<String> circuitIds) {
        if (CollectionUtils.isEmpty(circuitIds)) {
            return Collections.emptyMap();
        }
        DemandInstanceGroupDetailQueryDaoParam detailQueryDaoParam = new DemandInstanceGroupDetailQueryDaoParam();
        detailQueryDaoParam.setInstanceIds(circuitIds);
        detailQueryDaoParam.addColumns(DemandInstanceGroupDetail::getInstanceId, DemandInstanceGroupDetail::getRelGroupId);
        DemandInstanceGroupQueryDaoParam queryDaoParam = new DemandInstanceGroupQueryDaoParam();
        queryDaoParam.setTemplateType(templateType);
        queryDaoParam.setQueryDetail(true);
        queryDaoParam.setDetailQueryParam(detailQueryDaoParam);
        queryDaoParam.addColumns(BaseAssignIdDO::getId);
        List<DemandInstanceGroupPO> groups = this.demandInstanceGroupResource.list(queryDaoParam);
        Map<String, Set<String>> groupId2CircuitIds = new HashMap<>();
        if (CollectionUtils.isNotEmpty(groups)) {
            groups.stream().filter(group -> CollectionUtils.isNotEmpty(group.getDetails()))
                    .forEach(group -> MapExecutor.dynamicPutAll(groupId2CircuitIds,
                            String.valueOf(group.getId()), group.getDetails().stream()
                                    .map(DemandInstanceGroupDetail::getInstanceId)
                                    .collect(Collectors.toSet()), HashSet::new));
        }
        return groupId2CircuitIds;
    }

    /**
     * 检查重复配置
     *
     * @param dto 配置数据
     * @return 重复配置
     * @since 7.250601.0
     */
    private List<InterventionTaskConfigSaveOrUpdateVO.LightingRepetition> checkRepeat(InterventionTaskConfigDTO dto) {
        RunningMonitorTypeEnum runningMonitorType = dto.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = dto.getRunningMonitorSecondType();
        DemandTemplateTypeEnum templateType = runningMonitorSecondType.toDemandTemplateTypeEnum();
        InterventionTaskInstanceDTO configObject = dto.getConfigObject();
        InterventionTaskConfigDTO.Lighting config = this.parseConfig(dto.getConfigData());
        List<InterventionTaskTimeDTO> timeSlot = dto.getTimeSlot();
        LocalDateTime beginTime = timeSlot.get(0).getBeginTime();
        LocalDateTime endTime = timeSlot.get(0).getEndTime();

        Set<String> circuitIds;
        Map<String, Set<String>> groupId2CircuitIds;
        // 如果配置的是回路编组，则查询回路编组关联的回路ID
        if (configObject.getInstanceType() == InterventionInstanceTypeEnum.CIRCUIT_GROUP) {
            // 回路编组配置不需要设置照明回路，每次查询的时候实时获取
            config.getConfig().setCircuitIds(null);
            List<DemandInstanceGroupDetail> groupDetails = this.demandInstanceGroupDetailService.ListDetailByGroupIds(
                    configObject.getInstanceIds().stream().map(Long::parseLong).collect(Collectors.toList()));
            if (CollectionUtils.isEmpty(groupDetails)) {
                throw new ValidationException("编组下无照明回路");
            }
            circuitIds = groupDetails.stream()
                    .map(DemandInstanceGroupDetail::getInstanceId)
                    .collect(Collectors.toSet());
            groupId2CircuitIds = groupDetails.stream().collect(Collectors.groupingBy(
                    item -> String.valueOf(item.getRelGroupId()), Collectors.mapping(
                            DemandInstanceGroupDetail::getInstanceId, Collectors.toSet())));
        } else {
            circuitIds = config.getConfig().getCircuitIds();
            groupId2CircuitIds = Collections.emptyMap();
        }

        if (CollectionUtils.isEmpty(circuitIds)) {
            throw new ValidationException("照明回路不能为空");
        }

        // 如果是手动执行，则不检查重复配置
        if (config.getType() == InterventionExecuteTypeEnum.MANUAL) {
            return null;
        }

        // 查询实例 ID
        EquipmentQueryParam equipmentQueryParam = new EquipmentQueryParam();
        equipmentQueryParam.setFilterObjectIds(circuitIds);
        Set<String> queryInstanceIds = this.demandLightingStrategy.listCircuits(Lists.newArrayList(templateType),
                        equipmentQueryParam, null).stream()
                .map(DtpRwdCircuitDTO::getServiceSpaces)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toSet());
        queryInstanceIds.addAll(groupId2CircuitIds.keySet());
        // 查询干预配置
        List<InterventionTaskConfig> dbConfigs = this.interventionTaskConfigService.queryListByTypeAndInstanceIds(
                runningMonitorType, runningMonitorSecondType, InterventionTaskConfigDataTypeEnum.RELATION,
                null, queryInstanceIds);
        // 过滤掉当前任务
        dbConfigs.removeIf(dbConfig -> dbConfig.getTaskId().equals(dto.getTaskId()));
        if (CollectionUtils.isEmpty(dbConfigs)) {
            return null;
        }

        Map<Long, List<InterventionTaskConfig>> taskId2Relations = dbConfigs.stream()
                .collect(Collectors.groupingBy(InterventionTaskConfig::getTaskId));
        if (MapUtils.isEmpty(taskId2Relations)) {
            return null;
        }
        Set<Long> taskIds = taskId2Relations.keySet();
        Map<Long, InterventionTaskConfig> taskId2Data = this.interventionTaskConfigService.mapTaskId2Data(taskIds);
        Map<String, String> circuitId2LocalName = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                        .setId(circuitIds)
                        .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName)).stream()
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdObjectDTO::getLocalName));
        LocalDateTime now = DateUtils.now();
        return taskId2Data.values().stream()
                .filter(dbConfig -> {
                    InterventionTaskConfigDTO.Lighting configData = this.parseConfig(dbConfig.getConfigData());
                    // 仅获取时间有交集的配置
                    Range<ChronoLocalDateTime<?>> timeRange = Range.between(beginTime, endTime);
                    List<Range<ChronoLocalDateTime<?>>> dbTimeRanges = configData.getExecuteTimeRanges();
                    if (CollectionUtils.isEmpty(dbTimeRanges)) {
                        return false;
                    }
                    // 仅保留时间有交集的配置
                    dbTimeRanges.removeIf(range -> range.getMaximum().isBefore(now)
                            || !Range.between(range.getMinimum(), range.getMaximum().minus(Duration.ofMinutes(1))).isOverlappedBy(timeRange));
                    dbConfig.setConfigData(ObjectMapperUtils.convertValue(configData, JsonNode.class));
                    return CollectionUtils.isNotEmpty(dbTimeRanges);
                })
                .flatMap(dbConfig -> {
                    List<InterventionTaskConfig> relations = taskId2Relations.get(dbConfig.getTaskId());
                    if (CollectionUtils.isEmpty(relations)) {
                        return Stream.empty();
                    }
                    Set<String> repeatCircuitIds = relations.stream().flatMap(relation -> {
                        InterventionInstanceTypeEnum instanceType = relation.getInstanceType();
                        String instanceId = relation.getInstanceId();
                        InterventionTaskConfigDTO.Lighting configData = this.parseConfig(dbConfig.getConfigData());

                        Set<String> circuitIdsInConfig = instanceType == InterventionInstanceTypeEnum.CIRCUIT_GROUP
                                ? groupId2CircuitIds.get(instanceId)
                                : configData.getConfig().getCircuitIds();
                        if (CollectionUtils.isEmpty(circuitIdsInConfig)) {
                            return Stream.empty();
                        }
                        return CollectionUtils.intersection(circuitIds, circuitIdsInConfig).stream();
                    }).collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(repeatCircuitIds)) {
                        return Stream.empty();
                    }
                    List<InterventionTaskConfigSaveOrUpdateVO.LightingRepetition> eachRepetition = new ArrayList<>();
                    for (String repeatCircuitId : repeatCircuitIds) {
                        InterventionTaskConfigDTO taskConfig = new InterventionTaskConfigDTO();
                        taskConfig.setTaskName(dbConfig.getTaskName());
                        taskConfig.setRunningMonitorType(dbConfig.getRunningMonitorType());
                        taskConfig.setRunningMonitorSecondType(dbConfig.getRunningMonitorSecondType());
                        taskConfig.setConfigObject(new InterventionTaskInstanceDTO()
                                .setInstanceType(relations.stream().map(InterventionTaskConfig::getInstanceType).findFirst().orElse(null))
                                .setInstanceIds(relations.stream().map(InterventionTaskConfig::getInstanceId).collect(Collectors.toList())));
                        taskConfig.setConfigData(dbConfig.getConfigData());
                        taskConfig.setReason(dbConfig.getReason());
                        taskConfig.setTimeSlot(Lists.newArrayList(new InterventionTaskTimeDTO(
                                dbConfig.getBeginTime(), dbConfig.getEndTime())));
                        taskConfig.setTaskId(dbConfig.getTaskId());

                        InterventionTaskConfigSaveOrUpdateVO.LightingRepetition lightingRepetition =
                                new InterventionTaskConfigSaveOrUpdateVO.LightingRepetition();
                        lightingRepetition.setId(repeatCircuitId);
                        lightingRepetition.setLocalName(circuitId2LocalName.get(repeatCircuitId));
                        lightingRepetition.setConfig(taskConfig);
                        eachRepetition.add(lightingRepetition);
                    }
                    return eachRepetition.stream();
                })
                .collect(Collectors.toList());
    }

    /**
     * 解析配置
     *
     * @param config 配置
     * @return 解析后的配置
     * @since 7.260101.0
     */
    public InterventionTaskConfigDTO.Lighting parseConfig(JsonNode config) {
        return this.objectMapper.convertValue(config, InterventionTaskConfigDTO.Lighting.class);
    }

    /**
     * 查询空间下的回路
     *
     * @param runningMonitorSecondType
     * @param spaceIds
     * @return
     */
    private List<DtpRwdCircuitDTO> getCircuitBySpace(RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                                     @NonNull Set<String> spaceIds) {
        Collection<String> withColumns = Arrays.asList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.name,
                DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.localId, DtpRwdObjectDTO.Fields.relObjIds);
        return demandLightingStrategy.listCircuitsBySpaceIds(
                ContextUtil.getProjectId(), runningMonitorSecondType.toDemandTemplateTypeEnum(), spaceIds,
                null, false, withColumns);
    }

    /**
     * 查询照明空间的回路功能类型和关联的控制模块
     *
     * @param param
     * @return
     */
    public List<LightingSpaceFuncTypeCircuitVO> getSpaceFuncTypeCircuitController(LightingSpaceFuncTypeCircuitParam param) {
        // 查询空间
        List<DtpRwdSpaceDTO> spList = dtpRwdClientService.listSpace(new DtpRwdObjectQueryParam()
                .setId(param.getSpaceIds()).addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localId
                        , DtpRwdObjectDTO.Fields.localName));
        // 查询空间下的照明回路
        List<DtpRwdCircuitDTO> circuits = getCircuitBySpace(param.getRunningMonitorSecondType(), param.getSpaceIds());

        List<String> ids = circuits.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toList());
        // 回路关联的控制模块
        List<DtpRwdObjectDTO> controllers = dtpRwdClientService.listCircuitControllerByCircuitIds(
                false, false, ContextUtil.getProjectId(), ids, new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id,
                        DtpRwdObjectDTO.Fields.localId, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.relObjIds));

        // 空间 对应映射
        Map<String, DtpRwdSpaceDTO> spIdMap = spList.stream().collect(Collectors.toMap(DtpRwdSpaceDTO::getId, x -> x));
        // 只取有控制模块的回路
        Map<String, List<DtpRwdObjectDTO>> mapRelObjIdAndIds = DtpRwdObjectDTO.mapRelObjIdAndData(controllers);
        // 查询关联控制器
        List<DtpRwdObjectDTO> relDDCs = runningMonitorLightingStrategy.queryDDC(ids, Lists.newArrayList(
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.relObjIds));
        Map<String, List<DtpRwdObjectDTO>> circuitId2DDCs = DtpRwdObjectDTO.mapRelObjIdAndData(relDDCs);
        // 删除没有控制模块的回路
        // circuits.removeIf(circuit -> !mapRelObjIdAndIds.containsKey(circuit.getId()));

        // 先关联回路基准对象的 控制模块和空间信息
        List<LightingSpaceFuncTypeCircuitVO.BasicObj> basicObjs = circuits.stream().map(circuit -> {
            LightingSpaceFuncTypeCircuitVO.BasicObj basicObj = new LightingSpaceFuncTypeCircuitVO.BasicObj();
            basicObj.setFuncType(circuit.getFuncType());
            basicObj.setLampFuncType(circuit.getLampFuncType());
            basicObj.setRelController(mapRelObjIdAndIds.get(circuit.getId()));
            basicObj.setRelDDC(circuitId2DDCs.get(circuit.getId()));
            List<DtpRwdSpaceDTO> serviceSpace = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(circuit.getRelObjIds())) {
                circuit.getRelObjIds().forEach(relObjId -> {
                    serviceSpace.add(spIdMap.get(relObjId));
                });
            }
            basicObj.setServiceSpace(serviceSpace);
            basicObj.setId(circuit.getId());
            basicObj.setLocalId(circuit.getLocalId());
            basicObj.setLocalName(circuit.getLocalName());
            return basicObj;
        }).collect(Collectors.toList());
        // 获取功能类型和灯具类型的名称映射
        Map<String, String> funcTypeNameMap =
                demandLightingStrategy.mapCircuitInfoCodeAndName(DtpRwdCircuitDTO.Fields.funcType);
        Map<String, String> lampFuncTypeNameMap =
                demandLightingStrategy.mapCircuitInfoCodeAndName(DtpRwdCircuitDTO.Fields.lampFuncType);
        return groupBasicObjs(basicObjs, funcTypeNameMap, lampFuncTypeNameMap);
    }

    /**
     * 将 BasicObj 按 funcType 和 lampFuncType 分组，构建成 LightingSpaceFuncTypeCircuitVO 结构
     *
     * @param basicObjs 输入的基础对象列表
     * @return 分组后的 VO 列表
     */
    private List<LightingSpaceFuncTypeCircuitVO> groupBasicObjs(List<LightingSpaceFuncTypeCircuitVO.BasicObj> basicObjs,
                                                                Map<String, String> funcTypeNameMap,
                                                                Map<String, String> lampFuncTypeNameMap) {
        if (CollectionUtils.isEmpty(basicObjs)) {
            return java.util.Collections.emptyList();
        }


        // 第一步：按 funcType -> lampFuncType 分组
        Map<String, Map<String, List<LightingSpaceFuncTypeCircuitVO.BasicObj>>> groupedByFuncAndLamp =
                groupByFuncTypeAndLampFuncType(basicObjs);

        // 第二步：转换为最终的 VO 列表
        return convertToVOList(groupedByFuncAndLamp, funcTypeNameMap, lampFuncTypeNameMap);
    }

    /**
     * 按 funcType 和 lampFuncType 对基础对象进行分组
     */
    private Map<String, Map<String, List<LightingSpaceFuncTypeCircuitVO.BasicObj>>> groupByFuncTypeAndLampFuncType(
            List<LightingSpaceFuncTypeCircuitVO.BasicObj> basicObjs) {
        return basicObjs.stream().filter(item -> Objects.nonNull(item.getLampFuncType()))
                .collect(Collectors.groupingBy(LightingSpaceFuncTypeCircuitVO.BasicObj::getFuncType,
                        Collectors.groupingBy(LightingSpaceFuncTypeCircuitVO.BasicObj::getLampFuncType)));
    }

    /**
     * 将分组后的 Map 转换为最终的 VO 列表
     */
    private List<LightingSpaceFuncTypeCircuitVO> convertToVOList(
            Map<String, Map<String, List<LightingSpaceFuncTypeCircuitVO.BasicObj>>> groupedData,
            Map<String, String> funcTypeNameMap,
            Map<String, String> lampFuncTypeNameMap) {

        return groupedData.entrySet().stream()
                .map(entry -> {
                    String funcType = entry.getKey();
                    Map<String, List<LightingSpaceFuncTypeCircuitVO.BasicObj>> lampFuncTypeMap = entry.getValue();
                    List<LightingSpaceFuncTypeCircuitVO.LampFuncType> lampFuncTypes = convertToLampFuncTypeList(
                            lampFuncTypeMap, lampFuncTypeNameMap);

                    return new LightingSpaceFuncTypeCircuitVO(
                            funcType,
                            funcTypeNameMap.get(funcType),
                            lampFuncTypes
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 将每个 lampFuncType 的数据转换为 LampFuncType 对象列表
     */
    private List<LightingSpaceFuncTypeCircuitVO.LampFuncType> convertToLampFuncTypeList(
            Map<String, List<LightingSpaceFuncTypeCircuitVO.BasicObj>> lampFuncTypeMap,
            Map<String, String> lampFuncTypeNameMap) {

        return lampFuncTypeMap.entrySet().stream()
                .map(lampEntry -> new LightingSpaceFuncTypeCircuitVO.LampFuncType(
                        lampEntry.getKey(), lampFuncTypeNameMap.get(lampEntry.getKey()),
                        lampEntry.getValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public String logTag() {
        return "照明临时控制";
    }
}
