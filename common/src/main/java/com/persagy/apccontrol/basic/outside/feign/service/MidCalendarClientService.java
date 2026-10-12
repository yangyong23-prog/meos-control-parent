package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarDTO;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarGlobalRuleDTO;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarWorkTimeDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandInstantiationDateTagVO;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.*;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.*;
import com.persagy.apccontrol.basic.outside.feign.service.param.calendar.*;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工作历客户端服务
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public interface MidCalendarClientService extends BaseFeignClientService {

    /**
     * 营业时间格式
     */
    DateTimeFormatter OPENING_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 查询对象工作历
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 对象工作历
     * @since v5.7.0
     */
    MidCalendarDTO getCalendar(@NonNull String projectId, @NonNull MidCalendarQueryParam param);

    /**
     * 查询或初始化对象真实工作历
     *
     * @param projectId    项目ID
     * @param instanceType 实例类型
     * @param instanceId   实例ID
     * @return 对象工作历
     * @since v6.0.0
     */
    MidCalendarDTO getOrInitObjectRealCalendar(
            @NonNull String projectId,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId);

    /**
     * 获取当前季节
     *
     * @param groupCode
     * @param projectId
     * @param localDate
     * @return
     */
    @Deprecated
    MidCalendarDateTag getSeason(@NonNull String groupCode, @NonNull String projectId, @NonNull LocalDate localDate);

    /**
     * 查询工作历空调工况标签
     *
     * @param projectId 项目ID
     * @param objectId  对象ID
     * @param date      查询日期
     * @return 工作历空调工况标签
     * @see #listCalendarDayDateTag(String, MidCalendarDayDateTagQueryParam)
     * @since 5.240502.0
     */
    @Nullable
    MidCalendarAcSeasonTypeEnum getAcSeasonType(
            @NonNull String projectId,
            @NonNull String objectId,
            @NonNull LocalDate date);

    /**
     * 查询工作历空调季标签
     *
     * @param objectId 对象ID
     * @param date     查询日期
     * @return 工作历空调季标签
     * @see #listCalendarDayDateTag(String, MidCalendarDayDateTagQueryParam)
     * @since 5.241102.0
     */
    @Nullable
    MidCalendarAirSeasonTypeEnum getAirSeasonType(@NonNull String objectId, @NonNull LocalDate date);

    /**
     * 查询工作历空调季标签
     *
     * @param objectId  对象 ID
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return 工作历空调季标签
     * @since 7.260102.0
     */
    Map<LocalDate, MidCalendarAirSeasonTypeEnum> mapAirSeasonType(
            @NonNull String objectId,
            @NonNull LocalDate beginDate,
            @NonNull LocalDate endDate);

    /**
     * 查询工作历规则
     *
     * @param projectId    项目ID
     * @param calendarId   工作历ID
     * @param dateTagType  日期标签类型
     * @param dateTagValue 日期标签值
     * @return 工作历规则
     * @since v5.7.0
     */
    MidCalendarGlobalRuleDTO getGlobalCalendarRule(
            @NonNull String projectId, @NonNull String calendarId,
            @Nullable MidCalendarDateTagTypeEnum dateTagType,
            @Nullable MidCalendarDateTagValueEnum dateTagValue);

    /**
     * 查询或初始化对象全局规则
     *
     * @param projectId    项目ID
     * @param instanceType 实例类型
     * @param instanceId   实例ID
     * @param dateTagType  日期标签, 为空时查询所有
     * @param dateTagValue 日期标签值, 为空时查询所有
     * @return 对象全局规则
     * @since v6.0.0
     */
    MidCalendarGlobalRuleDTO getOrInitObjectGlobalCalendarRule(
            @NonNull String projectId,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId,
            @Nullable MidCalendarDateTagTypeEnum dateTagType,
            @Nullable MidCalendarDateTagValueEnum dateTagValue);

    /**
     * 查询工作历
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 工作历
     * @since v5.7.0
     */
    List<MidCalendarDTO> listCalendar(@NonNull String projectId, @NonNull MidCalendarQueryParam param);

    /**
     * 应用指定对象工作历的空间对象个数限制十条
     *
     * @return 工作历
     * @since v5.7.0
     */
    List<CalendarObjectNumResponse> calendarObjectNum(List<CalendarObjectNumQueryParam> param);

    /**
     * 查询工作历日期标签
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 工作历日期标签
     * @since v6.0.0
     */
    List<MidCalendarDayDateTagResponse> listCalendarDayDateTag(
            @NonNull String projectId, @NonNull @Valid MidCalendarDayDateTagQueryParam param);

    /**
     * 查询工作历日期计划
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 工作历日期计划
     * @since v5.7.0
     */
    List<MidCalendarDayPlanResponse> listCalendarDayPlan(
            @NonNull String projectId, @NonNull @Valid MidCalendarDayPlanQueryParam param);

    /**
     * 查询工作历营业时间
     *
     * @param param 查询参数
     * @return 工作历营业时间
     * @since 7.250202.0
     */
    List<MidCalendarWorkTimeDTO> listCalendarWorkTime(@NonNull @Valid MidCalendarWorkTimeQueryParam param);

    /**
     * 查询项目下所有项目对象营业时间
     *
     * @param workDate 查询日期
     * @return 项目下所有项目对象营业时间
     * @since 7.250202.0
     */
    List<MidCalendarOpenTimeProjectDTO> listOpenTimeProject(@NonNull LocalDate workDate);

    /**
     * 创建工作历
     *
     * @param projectId  项目ID
     * @param instanceId 实例ID
     * @return 工作历ID
     * @since v6.0.0
     */
    String addCalendar(@NonNull String projectId, @NonNull String instanceId);

    /**
     * 新建工作历
     *
     * @param projectId    项目ID
     * @param objectId     对象ID
     * @param calendarName 工作历名称
     * @param replicaCalId 复制已有工作历ID
     * @return 新建成功的工作历ID
     * @since v5.7.0
     */
    String addCalendar(
            @NonNull String projectId, @NonNull String objectId,
            @NonNull String calendarName, @Nullable String replicaCalId);

    /**
     * 编辑对象计划数据
     *
     * @param projectId 项目ID
     * @param param     新增参数
     * @since v6.0.0
     */
    void editObjectPlanContent(
            @NonNull String projectId,
            @NonNull @Valid MidCalendarPlanDataInsertParam param);

    /**
     * 编辑全局工休日规则
     *
     * @param projectId 项目ID
     * @param param     编辑参数
     * @since v5.7.0
     */
    void editGlobalDateWorkDayTagRule(
            @NonNull String projectId,
            @NonNull @Valid MidCalendarGlobalDateWorkDateTagRuleUpdateParam param);

    /**
     * 编辑全局空调季规则
     *
     * @param projectId 项目ID
     * @param params    编辑参数
     * @since v5.7.0
     */
    void editGlobalDateAcSeasonTagRule(
            @NonNull String projectId,
            @NonNull @Valid Collection<MidCalendarGlobalDateAcSeasonTagRuleUpdateParam> params);

    /**
     * 编辑全局店庆日规则
     *
     * @param projectId 项目ID
     * @param param     编辑参数
     * @since v5.8.0
     */
    void editGlobalAnniversaryRule(
            @NonNull String projectId,
            @NonNull @Valid MidCalendarGlobalAnniversaryRuleUpdateParam param);

    /**
     * 编辑全局营业时间规则
     *
     * @param projectId   项目ID
     * @param calendarId  工作历ID
     * @param isAsyncInit 是否异步实例化每天规则
     * @param params      更新参数
     * @since v5.7.0
     */
    void editGlobalOpeningRule(
            @NonNull String projectId, @NonNull String calendarId,
            @Nullable Boolean isAsyncInit,
            @NonNull @Valid Collection<MidCalendarGlobalOpeningRuleUpdateParam> params);

    /**
     * 批量编辑全局标签规则
     *
     * @param projectId 项目ID
     * @param param     编辑参数
     * @since v6.0.0
     */
    void batchEditDateTagGlobalRule(
            @NonNull String projectId,
            @NonNull @Valid MidCalendarDateTagGlobalRuleBatchUpdateParam param);

    /**
     * 重置项目工作历
     *
     * @param projectId 项目ID
     * @return 工作历ID
     * @since v6.0.0
     */
    String resetProjectCalendar(@NonNull String projectId);

    /**
     * 删除工作历
     *
     * @param projectId  项目ID
     * @param calendarId 工作历ID
     * @since v5.7.0
     */
    void delCalendar(@NonNull String projectId, @NonNull String calendarId);

    /**
     * 批量删除规则
     *
     * @param projectId   项目ID
     * @param calendarIds 工作历ID集合
     * @since v5.7.0
     */
    void delBatchCalendar(@NonNull String projectId, @NonNull Collection<String> calendarIds);

    /**
     * 删除规则
     *
     * @param projectId  项目ID
     * @param calendarId 工作历ID
     * @param ruleIds    规则ID
     * @since v5.7.0
     */
    void delRule(@NonNull String projectId, @NonNull String calendarId, @Nullable Collection<String> ruleIds);

    /**
     * 查询对象的营业时间
     *
     * @param projectId
     * @param param
     */
    MidCalendarOpenTimeResponse getMidCalendarOpenTime(String projectId, MidCalendarOpenTimeQueryParam param);

    /**
     * 查询对象某天的营业时间
     *
     * @param param
     * @param workDate
     * @return
     */
    MidCalendarOpenTimeResponse getMidCalendarOpenTimeByDay(MidCalendarQueryParam param, LocalDate workDate);

    /**
     * 查询对象多个的营业时间
     *
     * @param projectId
     * @param param
     */
    Map<String, MidCalendarOpenTimeResponse> getMidCalendarOpenTimeMap(String projectId,
                                                                       Set<String> objects, MidCalendarOpenTimeQueryParam param);


    /**
     * 筛选有工作历的对象实例
     *
     * @param objIds 对象 ID
     * @return 有工作历的对象 id
     */
    Set<String> getObjectsFilter(List<String> objIds);


    /**
     * 获取项目下所有项目对象营业时间
     *
     * @param workDate 具体日期
     * @return List<MidCalendarOpenTimeProject> 所有对象的营业时间
     * @deprecated {@link #listOpenTimeProject(LocalDate)}
     */
    @Deprecated
    List<MidCalendarOpenTimeProjectResponse> getOpenTimeProject(LocalDate workDate);

    /**
     * 查询实例化日期标签
     *
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return 实例化日期标签
     */
    List<DemandInstantiationDateTagVO> listInstantiationDateTag(
            @NonNull LocalDate beginDate, @NonNull LocalDate endDate);

    /**
     * 查询空调季标签规则
     *
     * @return 空调季标签规则
     * @since 5.241102.0
     */
    Set<MidCalendarDateTagValueEnum> listAirDateTagByRule();

}
