package com.persagy.apccontrol.controller.demand;

import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.*;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleLastUpdateTimeVO;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleVO;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.common.service.demand.DemandOpeningCalendarService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V7_260101_0;

/**
 * 需求管理-营业日期控制器
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@RestController
@RequestMapping("/demand/openingCalendar")
public class DemandOpeningCalendarController {

    @Autowired
    private DemandOpeningCalendarService demandOpeningCalendarService;

    @Autowired
    private MidCalendarClientService midCalendarClientService;

    @Operation(summary = "查询-全局规则",tags = {V7_260101_0})
    @PostMapping("/getGlobalRule")
    public R<DemandGlobalRuleVO> getGlobalRule(@RequestBody @Valid DemandGlobalRuleQueryParam param) {
        return R.success(demandOpeningCalendarService.getGlobalRule(param));
    }

    @Operation(summary = "查询-最后一次更新时间")
    @PostMapping("/getGlobalRuleLastUpdateTime")
    public R<DemandGlobalRuleLastUpdateTimeVO> getGlobalRuleLastUpdateTime(
            @RequestBody @Valid DemandGlobalRuleLastUpdateTimeQueryParam param) {
        return R.success(demandOpeningCalendarService.getGlobalRuleLastUpdateTime(param));
    }

    @Operation(summary = "编辑-全局规则",tags = {V7_260101_0})
    @PostMapping("/editGlobalRule")
    public R<Boolean> editGlobalRule(@RequestBody @Valid DemandGlobalRuleUpdateParam param) {
        return R.success(demandOpeningCalendarService.editGlobalRule(param));
    }

    @Operation(summary = "批量删除-特殊对象配置",tags = {V7_260101_0})
    @PostMapping("/batchDelete/specialObjectConfiguration")
    public R<Boolean> batchDeleteSpecialObjectConfiguration(@RequestBody @Valid BatchDeleteSpecialObjectConfigurationParam batchDelete) {
        return R.success(demandOpeningCalendarService.batchDeleteSpecialObjectConfiguration(batchDelete));
    }

    @Operation(summary = "编辑-营业时间",tags = {V7_260101_0})
    @PostMapping("/editBusinessTime")
    public R<Boolean> editBusinessTime(@RequestBody @Valid BusinessTimeRangeParam businessTimeRange) {
        return R.success(demandOpeningCalendarService.editBusinessTime(businessTimeRange));
    }

    @Operation(summary = "查询有工作历的对象")
    @PostMapping("/queryCalendarByInstanceId")
    public R<Set<String>> queryCalendarByInstanceId(@RequestBody @Valid List<String> instanceIds) {
        return R.success(midCalendarClientService.getObjectsFilter(instanceIds));
    }

    @Operation(summary = "查询营业时间")
    @PostMapping("/listBusinessTimeByObjId")
    public R<LinkedHashSet<String>> listBusinessTimeByObjId(@RequestBody @Valid CalendarDayPlanByObjIdParam param) {
        return R.success(demandOpeningCalendarService.listBusinessTimeByObjId(param));
    }
}
