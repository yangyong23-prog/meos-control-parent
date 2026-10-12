package com.persagy.apccontrol.basic.resource.intervention;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionTaskConfig;
import com.persagy.apccontrol.basic.model.enumtype.InterventionTaskConfigDataTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.intervention.InterventionInstanceTypeEnum;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuyao
 * @description 针对表【intervention_task_config(干预任务配置表)】的数据库操作Service
 * @createDate 2025-06-16 14:50:44
 */
public interface InterventionTaskConfigResource extends BaseService<InterventionTaskConfig> {

    /**
     * 通过实例对象和时间查询配置
     *
     * @return 列表
     */
    List<InterventionTaskConfig> queryList(@NonNull RunningMonitorTypeEnum runningMonitorType,
                                           RunningMonitorSecondTypeEnum RunningMonitorSecondType,
                                           DemandInstanceTypeEnum DemandInstanceType,
                                           Collection<String> instanceIds,
                                           @NonNull LocalDateTime beginTime,
                                           LocalDateTime endTime);

    List<InterventionTaskConfigVO> queryListByVO(@NonNull RunningMonitorTypeEnum runningMonitorType,
                                                 RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                                 @Nullable DemandInstanceTypeEnum DemandInstanceType,
                                                 @NonNull Collection<String> instanceIds,
                                                 @NonNull LocalDateTime beginTime,
                                                 LocalDateTime endTime);

    InterventionTaskConfigVO listByTaskId(@NonNull RunningMonitorTypeEnum runningMonitorType,
                                                @Nullable RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                                @NonNull Long taskId);


    /**
     * 查询taskTime 时间生效的配置
     *
     * @param runningMonitorType
     * @param RunningMonitorSecondType
     * @param taskTime                 生效时间
     * @return
     */
    List<InterventionTaskConfigVO> queryListByTime(@NonNull RunningMonitorTypeEnum runningMonitorType,
                                                   RunningMonitorSecondTypeEnum RunningMonitorSecondType,
                                                   @NonNull LocalDateTime taskTime);


    /**
     * 通过类型 实例类型 和实例 IDS 查询配置 只查询当前生效或者未来生效的配置
     *
     * @param runningMonitorType
     * @param runningMonitorSecondType
     * @param type
     * @param instanceType
     * @param instanceIds
     * @return
     */
    List<InterventionTaskConfig> queryListByTypeAndInstanceIds(
            @NonNull RunningMonitorTypeEnum runningMonitorType,
            @Nullable RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull InterventionTaskConfigDataTypeEnum type,
            @Nullable InterventionInstanceTypeEnum instanceType,
            @NonNull Collection<String> instanceIds);

    /**
     * 通过 taskId 删除数据
     *
     * @param taskId 任务ID
     */
    void delByTaskId(Long taskId);

    /**
     * 通过 taskId 查询配置数据
     *
     * @param taskId 任务ID
     */
    InterventionTaskConfig queryByTaskId(Long taskId);

    /**
     * 通过 taskId 查询配置数据
     *
     * @param taskIds 任务ID
     * @return TaskId -> 配置
     * @since 7.260101.0
     */
    Map<Long, InterventionTaskConfig> mapTaskId2Data(Set<Long> taskIds);


    /**
     * InterventionTaskConfigDto转InterventionTaskConfig
     *
     * @param interventionTaskConfigDto InterventionTaskConfigDto
     * @return List
     */
    List<InterventionTaskConfig> toEntity(@NonNull InterventionTaskConfigDTO interventionTaskConfigDto);

    /**
     * 配置转换成 VO 对象
     *
     * @param cfg
     * @return
     */
    List<InterventionTaskConfigVO> toVO(@NotEmpty List<InterventionTaskConfig> cfg);

}
