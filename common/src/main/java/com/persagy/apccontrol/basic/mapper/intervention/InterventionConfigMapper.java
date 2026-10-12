package com.persagy.apccontrol.basic.mapper.intervention;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionInfoQueryParamEntity;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.resource.intervention.param.InterventionConfigQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 干预配置Mapper
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Mapper
@Repository
public interface InterventionConfigMapper extends BaseMapper<InterventionConfig> {

    /**
     * 查询干预配置
     *
     * @param param 查询参数
     * @return 干预配置
     * @since 5.240702.0
     */
    List<InterventionConfig> list(@NonNull @Param("param") InterventionConfigQueryDaoParam param);

    List<InterventionConfig> queryList(@NonNull @Param("param") InterventionInfoQueryParamEntity paramEntity);

    /**
     * 获取当前时间生效运行中的干预配置: beginTime<=nowTime<endTime
     *
     * @param projectId                项目ID
     * @param runningMonitorSecondType 类型
     * @return {@link List<InterventionConfig>}
     * @author yuecaipu
     * @date 2024/7/28 20:23
     * @since v5.240702
     */
    List<InterventionConfig> listActive(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType);

    List<InterventionConfig> listActiveByTime(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType, LocalDateTime localDateTime);
}