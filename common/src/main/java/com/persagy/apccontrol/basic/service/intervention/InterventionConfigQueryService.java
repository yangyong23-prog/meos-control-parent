package com.persagy.apccontrol.basic.service.intervention;

import com.persagy.apccontrol.basic.model.dto.intervention.InterventionConfigAddDto;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionInfoQueryListParamDto;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionQueryTimeSlotDto;
import com.persagy.apccontrol.basic.model.entity.intervention.TimeSlotEntity;
import com.persagy.apccontrol.basic.model.vo.intervention.*;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author yuecaipu
 */
public interface InterventionConfigQueryService<T extends InterventionConfigData> {


    List<InterventionQueryListVo<T>> queryInterventionConfig(@NotNull InterventionInfoQueryListParamDto paramDto);

    Boolean validatedAddConfigSlot(InterventionConfigAddDto addDto);

    InterventionConfigTimeSlotVo queryInterventionTimeSlot(InterventionQueryTimeSlotDto timeSlotDto);

    List<TimeSlotEntity> queryDemandTimeSlot(InterventionQueryTimeSlotDto timeSlotDto);

    /**
     * 需求干预详情
     *
     * @param param 入参
     * @return {@link ConfigDetailsVo}
     * @author yuecaipu
     * @date 2024/7/23 16:22
     * @since v5.240702
     */
    ConfigDetailsVo getConfigDetails(InterventionQueryParam param);

    /**
     * 需求空间统计
     *
     * @param param 入参
     * @return {@link SpaceStatisticsVo}
     * @author yuecaipu
     * @date 2024/7/23 16:23
     * @since v5.240702
     */
    SpaceStatisticsVo spaceStatistics(InterventionQueryParam param);

}
