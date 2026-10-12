package com.persagy.apccontrol.basic.controller.bizcheck;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.bizcheck.BizCheckConfigParam;
import com.persagy.apccontrol.basic.model.param.bizcheck.BizCheckQueryParam;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckConfigVO;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckResultVO;
import com.persagy.apccontrol.basic.service.bizcheck.BizCheckService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-02-26 13:33
 **/
@RestController
@RequestMapping("/bizCheck")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "业务可用性检查", description = VersionConstant.V7_260301_0)
public class BizCheckController {
    private final BizCheckService bizCheckService;

    @Operation(summary = "保存配置", tags = VersionConstant.V7_260301_0)
    @PostMapping("/saveConfig")
    public R<Boolean> saveConfig(@Valid @RequestBody BizCheckConfigParam param) {
        return R.success(bizCheckService.saveConfig(param));
    }

    @Operation(summary = "获取配置", tags = VersionConstant.V7_260301_0)
    @PostMapping("/getConfig")
    public R<BizCheckConfigVO> getConfig(@Valid @RequestBody BizCheckQueryParam param) {
        return R.success(bizCheckService.getConfig(param));
    }

    @Operation(summary = "获取检查结果", tags = VersionConstant.V7_260301_0)
    @PostMapping("/getCheckResults")
    public R<List<BizCheckResultVO>> getCheckResults(@Valid @RequestBody BizCheckQueryParam param) {
        return R.success(bizCheckService.getCheckResults(param));
    }

    @Operation(summary = "手动触发自动检查", tags = VersionConstant.V7_260301_0)
    @GetMapping("/triggerAutoCheck")
    public R<Void> triggerAutoCheck() {
        bizCheckService.triggerAutoCheck();
        return R.success(null);
    }

    @Operation(summary = "手动触发检查", tags = VersionConstant.V7_260301_0)
    @PostMapping("/triggerCheck")
    public R<Boolean> triggerCheck(@Valid @RequestBody BizCheckQueryParam param) {
        return R.success(bizCheckService.triggerCheck(param));
    }
}