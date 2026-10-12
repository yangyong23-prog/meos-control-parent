package com.persagy.apccontrol.basic.controller.strategy;

import com.persagy.apccontrol.basic.abstracts.AbstractControlConsoleStrategyServer;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.HotColdStationRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.HotColdStationUnitRunningPlanVO;
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

/**
 * 冷热站时间表策略控制器
 *
 * @author JiYinchuan
 * @version v2.0 v5.241101.0需求 云边均有控制台迁移至公共层
 * @since v5.6.0
 */
@Tag(name = "冷热站时间表策略控制器")
@RestController
@RequestMapping("/hotColdStationStrategyIntervention")
@Validated
public class HotColdStationCommonStrategyController {

    @Autowired
    private AbstractControlConsoleStrategyServer hotColdStationStrategyService;

    @Operation(summary = "查询实时需求运行计划", tags = VersionConstant.V6_0_0)
    @PostMapping("/listRunningStrategy")
    public R<RunningStrategyVO<HotColdStationUnitRunningPlanVO>> listRunningStrategy(
            @RequestBody @Valid HotColdStationRunningStrategyQueryParam param) {
        return R.success(hotColdStationStrategyService.listChillerRunningStrategy(param));
    }

}
