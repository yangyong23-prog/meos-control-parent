package com.persagy.apccontrol.basic.mapper.demand;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplateCalendar;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 需求模版日历Mapper
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Mapper
@Repository
public interface DemandTemplateCalendarMapper extends BaseMapper<DemandTemplateCalendar> {

    /**
     * 查询需求模版日历
     *
     * @param param 查询参数
     * @return 需求模版日历
     * @since v6.0.0
     */
    DemandTemplateCalendar get(@NonNull @Param("param") DemandTemplateCalendarQueryDaoParam param);

    /**
     * 查询需求模版日历
     *
     * @param param 查询参数
     * @return 需求模版日历
     * @since v6.0.0
     */
    List<DemandTemplateCalendar> list(@NonNull @Param("param") DemandTemplateCalendarQueryDaoParam param);

}