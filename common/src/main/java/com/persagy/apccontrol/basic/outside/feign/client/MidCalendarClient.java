package com.persagy.apccontrol.basic.outside.feign.client;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * 工作历接口
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@FeignClient(value = "mid-calendar-server", url = "${inner-url.bdtp.mid-calendar.service:}", path = "/v2.0")
@Validated
public interface MidCalendarClient {

    /**
     * 查询工作历列表
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 工作历列表
     * @since v5.7.0
     */
    @PostMapping("/work/calendar/list")
    CustomerResponse<List<MidCalendarResponse>> listCalendar(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarQueryParam body);

    /**
     * 应用指定对象工作历的空间对象个数
     *
     * @param param 公共参数
     */
    @PostMapping("/work/calendar/object/num")
    CustomerResponse<List<CalendarObjectNumResponse>> calendarObjectNum(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid JSONObject jsonObject);


    @Deprecated
    @PostMapping("/work/calendar/dateTag")
    CustomerResponse<List<MidCalendarDateTag>> dateTag(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid JSONObject requestParam);

    /**
     * 查询工作历规则
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 工作历规则
     * @since v5.7.0
     */
    @PostMapping("/work/rule/get")
    CustomerResponse<List<MidCalendarRuleResponse>> listCalendarRule(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarRuleQueryParam body);

    /**
     * 查询工作历日期标签
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 工作历列表
     * @since v6.0.0
     */
    @PostMapping("/work/calendar/getEveryDayDateTag")
    CustomerResponse<List<MidCalendarDayDateTagResponse>> listCalendarDayDateTag(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarDayDateTagQueryParam body);

    /**
     * 查询工作历日期计划
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 工作历日期计划
     * @since v5.7.0
     */
    @PostMapping("/work/calendar/getEveryDayPlan")
    CustomerResponse<List<MidCalendarDayPlanResponse>> listCalendarDayPlan(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarDayPlanQueryParam body);

    /**
     * 获取工作历营业时间
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 工作历营业时间
     * @since 7.250202.0
     */
    @PostMapping("/work/calendar/openTime")
    CustomerResponse<List<MidCalendarOpenTimeResponse>> listCalendarOpenTime(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarWorkTimeQueryParam body);

    /**
     * 查询指定日期的项目下所有工作历营业时间
     *
     * @param param 公共参数
     * @param body  查询的日期
     * @return 全项目的营业时间
     */
    @PostMapping("/work/calendar/openTime/project")
    CustomerResponse<List<MidCalendarOpenTimeProjectResponse>> listOpenTimeProject(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarOpenTimeProjectQueryParam body);

    /**
     * 创建工作历
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 操作结果
     * @since v5.7.0
     */
    @PostMapping("/work/calendar/create")
    CustomerResponse<MidCalendarInsertResponse> addCalendar(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarInsertParam body);

    /**
     * 编辑对象计划内容
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 操作结果
     * @since v5.7.0
     */
    @PostMapping(value = "/work/detail/append/plan/data/objId")
    CustomerResponse<?> editObjectPlanContent(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody MidCalendarPlanDataInsertParam body);

    /**
     * 编辑日期标签规则
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 操作结果
     * @since v5.7.0
     */
    @PostMapping("/work/rule/upsertDateTag")
    CustomerResponse<?> editDateTagRule(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarDateTagRuleUpdateParam body);

    /**
     * 编辑时间计划规则
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 操作结果
     * @since v5.7.0
     */
    @PostMapping("/work/rule/upsertSchedulePlan")
    CustomerResponse<List<MidCalendarRuleResponse>> editSchedulePlan(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarSchedulePlanRuleUpdateParam body);

    /**
     * 删除工作历
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 操作结果
     * @since v5.7.0
     */
    @PostMapping("/work/calendar/delete")
    CustomerResponse<?> delCalendar(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarDeleteParam body);

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/7 14:43
    　　 * @description: 批量删除工作历
    */
    @PostMapping("/work/calendar/delete/batch")
    CustomerResponse<?> delBatchCalendar(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarDeleteBatchParam body);

    /**
     * 删除规则
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 操作结果
     * @since v5.7.0
     */
    @PostMapping("/work/rule/delete")
    CustomerResponse<?> delRule(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarRuleDeleteParam body);


    /**
     * 获取对象的运营时间
     *
     * @param param
     * @param body
     * @return
     * @deprecated {@link #listCalendarOpenTime(DtpRwdCommonParam, MidCalendarWorkTimeQueryParam)}
     */
    @Deprecated
    @PostMapping("/work/calendar/openTime")
    CustomerResponse<List<MidCalendarOpenTimeResponse>> getOpenTime(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid MidCalendarOpenTimeQueryParam body);

    /**
     * 筛选有工作历的对象实例
     *
     * @param param  公共参数
     * @param objIds 查询的对象 id (对象id集合不能超过100)
     * @return 有工作历的对象 ID列表
     */
    @PostMapping("/work/calendar/objects/filter")
    CustomerResponse<List<String>> getObjectsFilter(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid List<String> objIds);

}
