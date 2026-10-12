package com.persagy.apccontrol.basic.controller.strategy;

import com.persagy.apccontrol.basic.abstracts.AbstractControlConsoleStrategyServer;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.strategy.ChillersNumberQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.ChillerRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.lastfreezepumpgroup.LastFreezePumpGroupRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.strategy.ChillersNumberVO;
import com.persagy.apccontrol.basic.model.vo.strategy.InfoCodeDataTimeAndPrecisionType;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.ChillerUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.LastFreezePumpGroupRunningPlanVO;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 策略公共控制器
 *
 * @author JiYinchuan
 * @version v2.0 v5.241101.0需求 云边均有控制台迁移至公共层
 * @since v5.6.0
 */
@Tag(name = "策略控制器")
@RestController
@RequestMapping("/strategyIntervention")
@Validated
public class StrategyCommonController {

    @Autowired
    private AbstractControlConsoleStrategyServer strategyService;


    // ------------------------- 冷机策略 -------------------------
    @Operation(summary = "查询冷机运行策略", tags = VersionConstant.V5_6_0)
    @PostMapping("/chillerStrategy/listRunningStrategy")
    public R<RunningStrategyVO<ChillerUnitRunningPlanVO>> listRunningStrategy(
            @RequestBody @Valid ChillerRunningStrategyQueryParam param) {
        return R.success(strategyService.listChillerRunningStrategy(param));
    }

    // ------------------------- 末级冷冻泵组策略 -------------------------

    @Operation(summary = "[末级冷冻泵组] 查询运行策略", tags = VersionConstant.V5_6_0)
    @PostMapping("/lastFreezePumpGroup/listRunningStrategy")
    public R<List<RunningStrategyVO<LastFreezePumpGroupRunningPlanVO>>> listRunningStrategy(
            @RequestBody @Valid LastFreezePumpGroupRunningStrategyQueryParam param) {
        return R.success(strategyService.listLastFreezePumpGroupRunningStrategy(param));
    }

    // ------------------------- 末级冷冻泵组策略 -------------------------

    @Operation(summary = "获取信息点数据时间和时间精度类型", tags = VersionConstant.V5_6_0)
    @GetMapping("/getInfoCodeDataTimeAndPrecisionType")
    public R<InfoCodeDataTimeAndPrecisionType> getInfoCodeDataTimeAndPrecisionType() {
        return R.success(strategyService.getInfoCodeDataTimeAndPrecisionType());
    }

    @Operation(summary = "查询系统包含的大小机数量", tags = VersionConstant.V7_250201_0)
    @PostMapping("/getNumberOfChillers")
    public R<ChillersNumberVO> getNumberOfChillers(@RequestBody @Valid ChillersNumberQueryParam param) {
        return R.success(strategyService.getNumberOfChillers(param.getSystemId()));
    }

}
