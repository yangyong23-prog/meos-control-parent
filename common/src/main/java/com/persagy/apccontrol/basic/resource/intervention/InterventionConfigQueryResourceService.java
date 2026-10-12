package com.persagy.apccontrol.basic.resource.intervention;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.context.intervention.InterventionInfoCodeConfigQueryContext;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionAddEntity;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionInfoQueryParamEntity;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.resource.intervention.param.InterventionConfigQueryDaoParam;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 干预配置服务
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
public interface InterventionConfigQueryResourceService extends BaseService<InterventionConfig> {

    /**
     * 查询干预配置
     *
     * @param param 查询参数
     * @return 干预配置
     * @since 5.241201.0
     */
    Optional<InterventionConfig> get(@NonNull @Valid InterventionConfigQueryDaoParam param);

    /**
     * 查询干预配置
     *
     * @param param 查询参数
     * @return 干预配置
     * @since 5.240702.0
     */
    List<InterventionConfig> list(@NonNull @Valid InterventionConfigQueryDaoParam param);

    void queryInterventionConfigList(InterventionInfoCodeConfigQueryContext queryContext);

    List<InterventionConfig> listActiveByTime(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType, LocalDateTime localDateTime);

    List<InterventionConfig> getConfigByRunningMonitorType(InterventionInfoQueryParamEntity paramEntity);

    Boolean validatedAddConfigSlot(InterventionAddEntity entity);

    /**
     * 获取当前时间生效运行中的干预配置: beginTime<=nowTime<=endTime
     *
     * @param projectId                项目ID
     * @param runningMonitorSecondType 类型
     * @return {@link List<InterventionConfig>}
     * @author yuecaipu
     * @date 2024/7/28 20:23
     * @since v5.240702
     */
    List<InterventionConfig> listActive(@NotNull String projectId, @NotNull RunningMonitorSecondTypeEnum runningMonitorSecondType);

}
