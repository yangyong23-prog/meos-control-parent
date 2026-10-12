package com.persagy.apccontrol.basic.controller.demand;

import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.DemandInstantiationDateTagQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandInstantiationDateTagVO;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * 营业日历公共接口
 */
@RestController
@RequestMapping("/demand/openingCalendar")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandCommonOpeningCalendarController {

    private final MidCalendarClientService midCalendarClientService;
    private final DemandCalendarQueryService demandCalendarQueryService;

    @Operation(summary = "查询-实例化日期标签")
    @PostMapping("/listInstantiationDateTag")
    public R<List<DemandInstantiationDateTagVO>> listInstantiationDateTag(
            @RequestBody @Valid DemandInstantiationDateTagQueryParam param) {
        return R.success(demandCalendarQueryService.listInstantiationDateTag(param));
    }

    @Operation(summary = "查询-空调季标签规则")
    @PostMapping("/listAirDateTagByRule")
    public R<Set<MidCalendarDateTagValueEnum>> listAirDateTagByRule() {
        return R.success(midCalendarClientService.listAirDateTagByRule());
    }
}
