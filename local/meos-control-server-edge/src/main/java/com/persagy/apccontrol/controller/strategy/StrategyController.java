package com.persagy.apccontrol.controller.strategy;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.dashboard.StrategyAupportAiQueryParam;
import com.persagy.apccontrol.common.model.param.strategy.StrategyQueryParam;
import com.persagy.apccontrol.common.model.param.strategy.StrategyTypeChangeInsertParam;
import com.persagy.apccontrol.common.service.StrategyService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 策略控制器
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Tag(name = "策略控制器")
@RestController
@RequestMapping("/strategyIntervention")
@Validated
public class StrategyController {

    @Autowired
    private StrategyService strategyService;

    @Operation(summary = "查询算法是否支持AI策略", tags = VersionConstant.V5_7_0)
    @PostMapping("/fetchSupportAi")
    public R<Boolean> fetchSupportAi(@RequestBody @Valid StrategyQueryParam param) {
        return R.success(strategyService.fetchSupportAi(param));
    }

    @Operation(summary = "本地获取AI策略状态", tags = VersionConstant.V5_7_0)
    @PostMapping("/querySupportAi")
    public R<List<StrategyTypeChangeInsertParam.AiStrategyStatus>> querySupportAi(@RequestBody @Valid StrategyAupportAiQueryParam param) {
        return R.success(strategyService.querySupportAi(param));
    }

    @Operation(summary = "保存策略转换配置", tags = VersionConstant.V5_7_0)
    @PostMapping("/saveSupportAiConfig")
    public R<Boolean> saveSupportAiConfig(@RequestBody @Valid StrategyTypeChangeInsertParam param) {
        return R.success(strategyService.saveSupportAi(param));
    }
}
