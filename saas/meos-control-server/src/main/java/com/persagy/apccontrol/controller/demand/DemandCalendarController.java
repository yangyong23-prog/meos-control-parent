package com.persagy.apccontrol.controller.demand;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.demand.template.*;
import com.persagy.apccontrol.basic.model.vo.KeyValueVO;
import com.persagy.apccontrol.common.service.demand.DemandCalendarService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 需求管理-需求日历模版控制器
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@RestController("saasDemandCalendarController")
@RequestMapping("/demand/template")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandCalendarController {

    private final DemandCalendarService demandCalendarService;

    @Operation(summary = "编辑-模版", tags = VersionConstant.V6_0_0)
    @PostMapping("/editTemplate")
    public R<Boolean> editTemplate(@RequestBody @Valid DemandTemplateUpdateParam param) {
        return R.success(demandCalendarService.editTemplate(param));
    }

    @Operation(summary = "删除-模版", tags = VersionConstant.V7_250301_0)
    @PostMapping("/delTemplate")
    public R<Boolean> delTemplate(@RequestBody @Valid DemandTemplateRemoveParam param) {
        return R.success(demandCalendarService.delTemplate(param));
    }

    @Operation(summary = "清除-日历配置", tags = VersionConstant.V6_0_0)
    @PostMapping("/clearCalendar")
    public R<Boolean> delTemplate(@RequestBody @Valid DemandTemplateClearCalendarParam param) {
        return R.success(demandCalendarService.clearCalendar(param));
    }

    @Operation(summary = "应用-按日期应用模版", tags = VersionConstant.V6_0_0)
    @PostMapping("/applyTemplateByDate")
    public R<Boolean> applyTemplate(@RequestBody @Valid DemandTemplateApplyByDateParam param) {
        return R.success(demandCalendarService.applyTemplateByDate(param));
    }

    @Operation(summary = "检查-模版名称", tags = VersionConstant.V6_0_0)
    @PostMapping("/checkTemplateName")
    public R<KeyValueVO> checkTemplateName(@RequestBody @Valid DemandTemplateNameCheckParam param) {
        return R.success(demandCalendarService.checkTemplateName(param));
    }

    @Operation(summary = "检查-未来日期是否存在配置", tags = VersionConstant.V6_0_0)
    @PostMapping("/checkExistsDateSettingInFuture")
    public R<KeyValueVO> checkExistsDateSettingInFuture(
            @RequestBody @Valid DemandTemplateExistsDateSettingInFutureCheckParam param) {
        return R.success(demandCalendarService.checkExistsDateSettingInFuture(param));
    }
}
