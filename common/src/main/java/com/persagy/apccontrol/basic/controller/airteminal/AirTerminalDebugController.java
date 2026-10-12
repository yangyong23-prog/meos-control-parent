package com.persagy.apccontrol.basic.controller.airteminal;

import com.persagy.apccontrol.basic.model.context.airterminal.AirTerminalQualityStatisticsContext;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalSpaceQualityStatisticsQueryParam;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_240601_0;

/**
 * 空调末端调试控制器
 *
 * @author JiYinchuan
 * @since 5.240601.0
 */
@RestController
@RequestMapping(value = "/debug/airTerminal")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AirTerminalDebugController {

    private final AirTerminalService airTerminalService;

    @Operation(summary = "查询-空间品质统计上下文", tags = V5_240601_0)
    @PostMapping(value = "/getSpaceQualityStatisticsContext")
    public R<AirTerminalQualityStatisticsContext> getSpaceQualityStatisticsContext(
            @RequestBody @Valid AirTerminalSpaceQualityStatisticsQueryParam param) {
        return R.success(airTerminalService.getSpaceQualityStatisticsContext(param));
    }
}
