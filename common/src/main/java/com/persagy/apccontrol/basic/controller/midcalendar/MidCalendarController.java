package com.persagy.apccontrol.basic.controller.midcalendar;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.midcalendar.MidCalendarWorkTimeBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.midcalendar.MidCalendarWorkTimeVO;
import com.persagy.apccontrol.basic.service.midcalendar.MidCalendarService;
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

/**
 * 工作历控制器
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@RestController
@RequestMapping(value = "/midCalendar")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MidCalendarController {

    private final MidCalendarService midCalendarService;

    @Operation(summary = "批量查询-工作历营业时间", tags = VersionConstant.V7_250702_0)
    @PostMapping("/listWorkTimes")
    public R<List<MidCalendarWorkTimeVO>> listWorkTimes(@RequestBody @Valid MidCalendarWorkTimeBatchQueryParam param) {
        return R.success(midCalendarService.listWorkTimes(param));
    }
}
