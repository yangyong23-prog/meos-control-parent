package com.persagy.apccontrol.basic.controller.shellconfig;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.controller.shellconfig.param.BasicsAnalysisParam;
import com.persagy.apccontrol.basic.controller.shellconfig.param.EfficiencyVO;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/runAnalysis")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "运行分析", description = VersionConstant.V5_241102_0)
public class AnalysisController {

    private final ShellConfigService shellConfigService;

    @Operation(summary = "效率指标数据", tags = VersionConstant.V7_250501_0)
    @PostMapping("/queryEfficiency")
    public R<EfficiencyVO> queryEfficiency(@RequestBody @Validated BasicsAnalysisParam param) {
        return R.success(shellConfigService.queryEfficiency(param));
    }
}
