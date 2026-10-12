package com.persagy.apccontrol.common.service.demand;

import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.*;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleLastUpdateTimeVO;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleVO;
import org.apache.commons.lang3.Range;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.time.LocalTime;
import java.util.LinkedHashSet;

/**
 * 需求管理-营业日历服务
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>getGlobalRule</b> - 查询全局规则</li>
 *     <li><b>getGlobalRuleLastUpdateTime</b> - 查询全局规则最后更新时间</li>
 *     <li><b>listInstantiationDateTag</b> - 查询实例化日期标签</li>
 *     <li><b>editGlobalRule</b> - 编辑全局规则</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface DemandOpeningCalendarService {

    /**
     * 查询全局规则
     *
     * @param param 查询参数
     * @return 全局规则
     * @since v6.0.0
     */
    DemandGlobalRuleVO getGlobalRule(@NonNull @Valid DemandGlobalRuleQueryParam param);

    /**
     * 查询全局规则最后更新时间
     *
     * @param param 查询参数
     * @return 全局规则最后更新时间
     * @since v6.0.0
     */
    DemandGlobalRuleLastUpdateTimeVO getGlobalRuleLastUpdateTime(
            @NonNull @Valid DemandGlobalRuleLastUpdateTimeQueryParam param);

    /**
     * 编辑全局规则
     *
     * @param param 全局规则更新参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean editGlobalRule(@NonNull @Valid DemandGlobalRuleUpdateParam param);

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/7 14:32
    　　 * @description: 特殊对象配置-批量删除配置内容
    */
    boolean batchDeleteSpecialObjectConfiguration(@Valid BatchDeleteSpecialObjectConfigurationParam batchDelete);

    /**
     * 编辑营业时间
     *
     * @param businessTimeRange
     * @return
     */
    boolean editBusinessTime(@NonNull @Valid BusinessTimeRangeParam businessTimeRange);


    /**
     * 查询工作历日期计划
     *
     * @param param
     * @return
     */
    public LinkedHashSet<String> listBusinessTimeByObjId(CalendarDayPlanByObjIdParam param);

    /**
     * 查询工作历日期计划
     *
     * @param param
     * @return
     */
    public LinkedHashSet<Range<LocalTime>> getBusinessTimeByObjId(CalendarDayPlanByObjIdParam param);

    /**
     * 判断时间是否在营业时间范围内
     *
     * @param date 查询工作历日期计划 获取到的时间
     * @param time 需要判断的时间
     * @return 是否在营业时间范围内
     */
    public boolean isInBusinessTime(LinkedHashSet<Range<LocalTime>> date, LocalTime time);

}
