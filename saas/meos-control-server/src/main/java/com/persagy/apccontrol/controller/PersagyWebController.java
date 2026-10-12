package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarDateTag;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.common.model.param.WorkDateParam;
import com.persagy.apccontrol.config.PersagyWebConfig;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersagyWebController {

    @Autowired
    private PersagyWebConfig persagyWebConfig;

    @Autowired
    private MidCalendarClientService midCalendarClientService;

    @PostMapping(value = "/persagyWeb/serviceUrl")
    public R<PersagyWebConfig> serviceUrl() {
        return R.success(persagyWebConfig);
    }

    @PostMapping("/air-terminal/queryProjectSeason")
    @Operation(summary = "查询项目的空调季", tags = {VersionConstant.V5_8_0})
    public R<MidCalendarDateTag> queryProjectSeason(@RequestBody @Validated WorkDateParam param) throws Exception {
        MidCalendarDateTag serviceSeason = this.midCalendarClientService.getSeason(ContextUtil.getGroupCode(), param.getProjectId(), param.getWorkDate());
        return R.success(serviceSeason);
    }

}
