package com.persagy.apccontrol.basic.resource.intervention.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.intervention.InterventionTaskConfigMapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskInstanceDTO;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionTaskConfig;
import com.persagy.apccontrol.basic.model.enumtype.InterventionTaskConfigDataTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.intervention.InterventionInstanceTypeEnum;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;
import com.persagy.apccontrol.basic.resource.intervention.InterventionTaskConfigResource;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.utils.SnowflakeIdWorker;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.annotation.jsr.NonNull;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yuyao
 * @description 针对表【intervention_task_config(干预任务配置表)】的数据库操作Service实现
 * @createDate 2025-06-16 14:50:44
 */
@Service
public class InterventionTaskConfigResourceImpl extends BaseServiceImpl<InterventionTaskConfigMapper, InterventionTaskConfig>
        implements InterventionTaskConfigResource {

    @Override
    public List<InterventionTaskConfig> queryList(@NonNull RunningMonitorTypeEnum runningMonitorType,
                                                  RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                                  DemandInstanceTypeEnum demandInstanceType,
                                                  Collection<String> instanceIds,
                                                  @NonNull LocalDateTime beginTime,
                                                  LocalDateTime endTime) {
        List<InterventionTaskConfig> list = new ArrayList<>();


        // 先查询出示例数据 再查询具体配置 按照开始时间和结束时间排序 升序
        List<InterventionTaskConfig> instance = this.lambdaQuery()
                .eq(InterventionTaskConfig::getProjectId, ContextUtil.getProjectId())
                .eq(InterventionTaskConfig::getRunningMonitorType, runningMonitorType)
                .eq(runningMonitorSecondType != null, InterventionTaskConfig::getRunningMonitorSecondType, runningMonitorSecondType)
                .eq(InterventionTaskConfig::getDataType, InterventionTaskConfigDataTypeEnum.RELATION)
                .eq(demandInstanceType != null,
                        InterventionTaskConfig::getInstanceType, demandInstanceType)
                .in(CollectionUtils.isNotEmpty(instanceIds),
                        InterventionTaskConfig::getInstanceId,
                        instanceIds)
                .ge(InterventionTaskConfig::getEndTime, beginTime)
                .lt(endTime != null, InterventionTaskConfig::getBeginTime, endTime)
                .orderByAsc(InterventionTaskConfig::getBeginTime,
                        InterventionTaskConfig::getEndTime)
                .list();
        if (CollectionUtils.isEmpty(instance)) {
            return list;
        }
        list.addAll(instance);

        // 取出所有的任务 ID
        List<InterventionTaskConfig> cfgList = getInterventionTaskConfigs(instance);
        list.addAll(cfgList);
        return list;
    }

    @Override
    public List<InterventionTaskConfigVO> queryListByVO(@NonNull RunningMonitorTypeEnum runningMonitorType,
                                                        RunningMonitorSecondTypeEnum RunningMonitorSecondType,
                                                        @Nullable DemandInstanceTypeEnum DemandInstanceType,
                                                        @NonNull Collection<String> instanceIds,
                                                        @NonNull LocalDateTime beginTime, LocalDateTime endTime) {
        return toVO(queryList(runningMonitorType, RunningMonitorSecondType, DemandInstanceType, instanceIds,
                beginTime, endTime));
    }

    @Override
    public InterventionTaskConfigVO listByTaskId(
            @NonNull RunningMonitorTypeEnum runningMonitorType,
            @Nullable RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull Long taskId) {
        List<InterventionTaskConfig> configs = this.lambdaQuery()
                .eq(InterventionTaskConfig::getProjectId, ContextUtil.getProjectId())
                .eq(InterventionTaskConfig::getRunningMonitorType, runningMonitorType)
                .eq(runningMonitorSecondType != null, InterventionTaskConfig::getRunningMonitorSecondType, runningMonitorSecondType)
                .eq(InterventionTaskConfig::getDataType, InterventionTaskConfigDataTypeEnum.MASTER)
                .eq(InterventionTaskConfig::getTaskId, taskId)
                .list();
        if (CollectionUtils.isEmpty(configs)) {
            return null;
        }
        List<InterventionTaskConfig> relationConfigs = this.getInterventionTaskConfigs(configs);
        configs.addAll(relationConfigs);
        List<InterventionTaskConfigVO> result = this.toVO(configs);
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return IterableUtils.first(result);
    }

    @Override
    public List<InterventionTaskConfigVO> queryListByTime(@NonNull RunningMonitorTypeEnum runningMonitorType,
                                                          RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                                          @NonNull LocalDateTime taskTime) {


        // 先查询出示例数据 再查询具体配置
        List<InterventionTaskConfig> instance = this.lambdaQuery()
                .eq(InterventionTaskConfig::getProjectId, ContextUtil.getProjectId())
                .eq(InterventionTaskConfig::getRunningMonitorType, runningMonitorType)
                .eq(runningMonitorSecondType != null, InterventionTaskConfig::getRunningMonitorSecondType, runningMonitorSecondType)
                .eq(InterventionTaskConfig::getDataType, InterventionTaskConfigDataTypeEnum.RELATION)
                .le(InterventionTaskConfig::getBeginTime, taskTime)
                // 时间范围是左闭右开
                .gt(InterventionTaskConfig::getEndTime, taskTime)
                .list();
        if (CollectionUtils.isEmpty(instance)) {
            return new ArrayList<>();
        }
        List<InterventionTaskConfig> list = new ArrayList<>(instance);

        // 取出所有的任务 ID
        List<InterventionTaskConfig> cfgList = getInterventionTaskConfigs(instance);
        list.addAll(cfgList);

        return toVO(list);
    }

    /**
     * 获取主配置
     *
     * @param instance
     * @return
     */
    private List<InterventionTaskConfig> getInterventionTaskConfigs(List<InterventionTaskConfig> instance) {
        Set<Long> taskIdList = instance.stream().map(InterventionTaskConfig::getTaskId).collect(Collectors.toSet());
        return this.lambdaQuery()
                .eq(InterventionTaskConfig::getProjectId, ContextUtil.getProjectId())
                .in(InterventionTaskConfig::getTaskId, taskIdList)
                .eq(InterventionTaskConfig::getDataType, InterventionTaskConfigDataTypeEnum.MASTER)
                .list();
    }

    @Override
    public List<InterventionTaskConfig> queryListByTypeAndInstanceIds(@NotNull RunningMonitorTypeEnum runningMonitorType,
                                                                      @Nullable RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                                                      @NotNull InterventionTaskConfigDataTypeEnum type,
                                                                      @Nullable InterventionInstanceTypeEnum instanceType,
                                                                      @NotNull Collection<String> instanceIds) {
        return this.lambdaQuery()
                .eq(InterventionTaskConfig::getProjectId, ContextUtil.getProjectId())
                .eq(InterventionTaskConfig::getRunningMonitorType, runningMonitorType)
                .eq(runningMonitorSecondType != null, InterventionTaskConfig::getRunningMonitorSecondType, runningMonitorSecondType)
                .eq(InterventionTaskConfig::getDataType, type)
                .eq(instanceType != null, InterventionTaskConfig::getInstanceType, instanceType)
                .in(InterventionTaskConfig::getInstanceId, instanceIds)
                .ge(InterventionTaskConfig::getEndTime, DateUtils.now())
                .list();
    }

    @Override
    public void delByTaskId(Long taskId) {
        this.lambdaUpdate()
                .eq(InterventionTaskConfig::getProjectId, ContextUtil.getProjectId())
                .eq(InterventionTaskConfig::getTaskId, taskId)
                .remove();
    }

    @Override
    public InterventionTaskConfig queryByTaskId(Long taskId) {
        return this.lambdaQuery()
                .eq(InterventionTaskConfig::getProjectId, ContextUtil.getProjectId())
                .eq(InterventionTaskConfig::getTaskId, taskId)
                .last(CommonConst.LIMIT_ONE).one();
    }

    @Override
    public Map<Long, InterventionTaskConfig> mapTaskId2Data(Set<Long> taskIds) {
        return this.lambdaQuery()
                .eq(InterventionTaskConfig::getProjectId, ContextUtil.getProjectId())
                .in(InterventionTaskConfig::getTaskId, taskIds)
                .eq(InterventionTaskConfig::getDataType, InterventionTaskConfigDataTypeEnum.MASTER)
                .list().stream()
                .collect(Collectors.toMap(InterventionTaskConfig::getTaskId, Function.identity()));
    }

    @Override
    public List<InterventionTaskConfig> toEntity(@NonNull InterventionTaskConfigDTO dto) {

        List<InterventionTaskConfig> dataList = new ArrayList<>();
        // 构建基础配置信息
        dto.getTimeSlot().forEach(item -> {
            long taskId = SnowflakeIdWorker.getWorkerIdByLong();
            InterventionTaskConfig cfg = new InterventionTaskConfig();
            cfg.setTaskName(dto.getTaskName());
            cfg.setBeginTime(item.getBeginTime());
            cfg.setEndTime(item.getEndTime());
            cfg.setConfigData(dto.getConfigData());
            cfg.setReason(dto.getReason());
            setBaseDate(cfg, dto);
            cfg.setDataType(InterventionTaskConfigDataTypeEnum.MASTER);
            cfg.setTaskId(taskId);
            if (dto.getTaskId() != null) {
                cfg.setTaskId(dto.getTaskId());
            }
            dataList.add(cfg);

            // 添加实例类型配置
            dto.getConfigObject().getInstanceIds().forEach(objId -> {
                InterventionTaskConfig configObjectCfg = new InterventionTaskConfig();
                setBaseDate(configObjectCfg, dto);
                configObjectCfg.setBeginTime(item.getBeginTime());
                configObjectCfg.setEndTime(item.getEndTime());
                configObjectCfg.setDataType(InterventionTaskConfigDataTypeEnum.RELATION);
                configObjectCfg.setTaskId(taskId);
                if (dto.getTaskId() != null) {
                    configObjectCfg.setTaskId(dto.getTaskId());
                }
                configObjectCfg.setInstanceType(dto.getConfigObject().getInstanceType());
                configObjectCfg.setInstanceId(objId);
                dataList.add(configObjectCfg);
            });
        });

        return dataList;
    }

    @Override
    public List<InterventionTaskConfigVO> toVO(@NotEmpty List<InterventionTaskConfig> cfg) {

        List<InterventionTaskConfigVO> list = new ArrayList<>();
        // 通过任务 ID 进行数据分组
        Map<Long, List<InterventionTaskConfig>> map = cfg.stream().collect(Collectors.groupingBy(InterventionTaskConfig::getTaskId));

        // 遍历分组 主配置类型创建 VO子配置类型创建实例集合
        map.forEach((taskId, taskCfgList) -> {
            InterventionTaskConfigVO vo = new InterventionTaskConfigVO();
            list.add(vo);
            InterventionTaskInstanceDTO instance = new InterventionTaskInstanceDTO();
            instance.setInstanceIds(new ArrayList<>());
            vo.setConfigObject(instance);
            taskCfgList.forEach(item -> {
                if (item.getDataType() == InterventionTaskConfigDataTypeEnum.MASTER) {
                    vo.setTaskId(item.getTaskId());
                    vo.setTaskName(item.getTaskName());
                    vo.setConfigData(item.getConfigData());
                    vo.setReason(item.getReason());
                    vo.setBeginTime(item.getBeginTime());
                    vo.setEndTime(item.getEndTime());
                    vo.setRunningMonitorType(item.getRunningMonitorType());
                    vo.setRunningMonitorSecondType(item.getRunningMonitorSecondType());
                    vo.setCreateTime(item.getCreateTime());
                } else {
                    instance.getInstanceIds().add(item.getInstanceId());
                    instance.setInstanceType(item.getInstanceType());
                }
            });
        });
        // 按照开始时间 和结束时间 进行排序
        list.sort((o1, o2) -> {
            // 1. 先比较开始时间
            if (o1.getBeginTime().isBefore(o2.getBeginTime())) {
                return -1;
            }
            if (o1.getBeginTime().isAfter(o2.getBeginTime())) {
                return 1;
            }
            // 2. 开始时间相同的情况下比较结束时间
            if (o1.getEndTime().isBefore(o2.getEndTime())) {
                return -1;
            }
            if (o1.getEndTime().isAfter(o2.getEndTime())) {
                return 1;
            }
            // 3. 开始时间和结束时间都相同的情况下比较创建时间
            return o1.getCreateTime().compareTo(o2.getCreateTime());
        });

        return list;
    }

    private void setBaseDate(InterventionTaskConfig cfg, InterventionTaskConfigDTO dto) {
        cfg.setRunningMonitorType(dto.getRunningMonitorType());
        cfg.setRunningMonitorSecondType(dto.getRunningMonitorSecondType());
        cfg.setProjectId(ContextUtil.getProjectId());
        cfg.setCreateUser(ContextUtil.getUserId());
        cfg.setUpdateUser(ContextUtil.getUserId());
        cfg.setUpdateTime(LocalDateTime.now());
        cfg.setCreateTime(LocalDateTime.now());
    }

}




