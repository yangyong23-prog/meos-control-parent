package com.persagy.apccontrol.controller.strategy;

import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.controller.strategy.param.ChillerFixedStrategyQueryParam;
import com.persagy.apccontrol.controller.strategy.vo.ChillerStrategyPlanVO;
import com.persagy.apccontrol.service.SaasStrategyService;
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
 * @author wangtaofang
 * @since v5.7.0
 */
@Tag(name = "策略控制器")
@RestController
@RequestMapping("/strategyIntervention")
@Validated
public class StrategyController {

    @Autowired
    private SaasStrategyService saasStrategyService;

    @Operation(summary = "查询冷机固定时间策略（算法使用）", tags = {
            VersionConstant.V5_7_0,
            ExternalServerConstant.AI_AU_SERVER
    })
    @PostMapping("/chillerStrategy/queryByParam")
    public R<List<ChillerStrategyPlanVO>> queryByParam(@RequestBody @Valid ChillerFixedStrategyQueryParam param) {
        return R.success(saasStrategyService.queryByParam(param));
    }
}
