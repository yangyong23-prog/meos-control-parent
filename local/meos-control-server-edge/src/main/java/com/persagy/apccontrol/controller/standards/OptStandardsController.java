package com.persagy.apccontrol.controller.standards;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.common.opt.OptDtpMeosCallCounter;
import com.persagy.apccontrol.common.opt.OptStandardsDisplay;
import com.persagy.apccontrol.common.opt.OptStandardsMode;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 运行标准过渡（B1 / B3 / B4）对外接口。开关默认关闭，详见 docs/opt/README.md「运行标准过渡」。
 */
@Slf4j
@RestController
@RequestMapping("/standards")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OptStandardsController {

    private static final String DISABLED = "A0403";
    private static final String REMOTE_ERROR = "C0001";

    private final OptStandardsMode standardsMode;
    private final OptDtpMeosCallCounter callCounter;
    private final OptStandardsDisplay standardsDisplay;

    @Operation(summary = "B1 查询项目运行标准模式：STANDARD 已切新运行标准 / LEGACY 旧配置")
    @GetMapping("/mode")
    public R<Map<String, Object>> mode(@RequestParam(value = "projectId", required = false) String projectId,
                                       @RequestHeader(value = "projectId", required = false) String headerProjectId) {
        return R.success(standardsMode.resolve(firstNonBlank(projectId, headerProjectId, contextProjectId())));
    }

    @Operation(summary = "B1 清空模式缓存（切换项目后立即生效）")
    @PostMapping("/mode/evict")
    public R<Boolean> evict() {
        standardsMode.evict();
        return R.success(true);
    }

    @Operation(summary = "B3 dtp-meos 调我方 5 个接口的按日计数")
    @GetMapping("/dtpMeosCalls")
    public R<Map<String, Object>> dtpMeosCalls(@RequestParam(value = "days", defaultValue = "7") int days) {
        return R.success(callCounter.stats(days));
    }

    @Operation(summary = "B4 查询运行标准解析结果（按日，转 dtp-meos /calculate/listStandardComputeResultService）")
    @PostMapping("/display/computeResult")
    public R<JsonNode> computeResult(@RequestBody(required = false) Map<String, Object> param) {
        return display(param, true);
    }

    @Operation(summary = "B4 查询运行标准解析详情（按时间点，转 dtp-meos /calculate/listStandardComputeResultDetailService）")
    @PostMapping("/display/computeResultDetail")
    public R<JsonNode> computeResultDetail(@RequestBody(required = false) Map<String, Object> param) {
        return display(param, false);
    }

    private R<JsonNode> display(Map<String, Object> param, boolean result) {
        Object bodyProject = param == null ? null : param.get("projectId");
        String projectId = firstNonBlank(contextProjectId(), bodyProject == null ? null : String.valueOf(bodyProject));
        try {
            return R.success(result ? standardsDisplay.listResult(projectId, param) : standardsDisplay.listDetail(projectId, param));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return R.fail(DISABLED, e.getMessage());
        } catch (RuntimeException e) {
            log.warn("[opt-standards] 转查 dtp-meos 运行标准解析结果失败，项目 {}：{}", projectId, e.toString());
            return R.fail(REMOTE_ERROR, "查询 dtp-meos 运行标准解析结果失败");
        }
    }

    private static String contextProjectId() {
        try {
            return ContextUtil.getProjectId();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return null;
    }
}
