package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.framework.common.entity.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运行标准过渡 B2：已切到新运行标准的项目，配置写接口改为只读。
 *
 * <p>{@code switch.opt.standardsReadonly} 关（默认）时直接放行；开且 B1 判定项目为 STANDARD 时，
 * 命中 {@code opt.standards.readonly-paths}（Ant 风格，逗号分隔，不配用 {@link #DEFAULT_PATHS}）的请求
 * 不进 Controller，返回 HTTP 200 + {@code R.fail("A0403", "由云端运行标准管理")}。未切项目照旧可写。
 * 取不到项目 ID 的请求放行并计数 {@code standards.readonly.noProject}。
 */
@Slf4j
public class OptStandardsReadonlyInterceptor implements HandlerInterceptor {

    public static final String CODE = "A0403";
    public static final String MESSAGE = "由云端运行标准管理";

    /**
     * 默认只读清单：空调需求配置、实例归属/编组、黑名单、需求日历模板、价值选择、MPC 智能体配置；
     * 2026-11-19 Blake 定安防/通风/排油烟需求、干预任务、壳配置也归运行标准服务与 dtp-meos，追加干预任务/干预配置与壳配置写接口
     * （安防/通风/排油烟需求经 /demand/addDemandConfig 等通用接口写入，已在清单内）。
     * 多数需求配置写接口只在云端包存在，边缘包实际命中的是 /valueSelection/saveConfig、/shellConfig/mpcAgentCfg；
     * 清单与前端（meos-control-web src/apis/standards.js）保持一致，便于云端将来复用。
     */
    public static final List<String> DEFAULT_PATHS = Collections.unmodifiableList(Arrays.asList(
            "/demand/addInstanceConfig",
            "/demand/addDemandConfig",
            "/demand/clearSettings",
            "/demand/referenceSettings",
            "/demand/copyConfigs",
            "/demand/blacklist/addRules",
            "/demand/instanceGroup/add",
            "/demand/instanceGroup/edit",
            "/demand/instanceGroup/copy",
            "/demand/template/applyTemplateByDate",
            "/demand/template/clearCalendar",
            "/valueSelection/saveConfig",
            "/shellConfig/mpcAgentCfg",
            "/shellConfig/save",
            "/shellConfig/changeAlgorithmConfig",
            "/shellConfig/changeLtsmConfig",
            "/interventionTaskConfig/saveOrUpdate",
            "/interventionTaskConfig/remove",
            "/intervention/config/saveOrUpdate"));

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private final OptStandardsMode standardsMode;
    private final ObjectMapper objectMapper;

    public OptStandardsReadonlyInterceptor(OptStandardsMode standardsMode, ObjectMapper objectMapper) {
        this.standardsMode = standardsMode;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!OptSwitch.on(OptSwitch.STANDARDS_READONLY)) {
            return true;
        }
        String path = OptStandardsRequests.path(request);
        if (!matches(path, readonlyPaths())) {
            return true;
        }
        String projectId = OptStandardsRequests.projectId(request);
        if (projectId == null) {
            OptMetrics.incAlways("standards.readonly.noProject");
            return true;
        }
        if (!standardsMode.isStandard(projectId)) {
            return true;
        }
        OptMetrics.incAlways("standards.readonly.blocked");
        log.info("[opt-standards] 项目 {} 已切新运行标准，拒绝配置写入 {}", projectId, path);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.fail(CODE, MESSAGE)));
        return false;
    }

    static boolean matches(String path, List<String> patterns) {
        for (String p : patterns) {
            if (MATCHER.match(p, path)) {
                return true;
            }
        }
        return false;
    }

    static List<String> readonlyPaths() {
        String v = OptSwitch.value("standards.readonly-paths", "");
        if (v.isEmpty()) {
            return DEFAULT_PATHS;
        }
        return Arrays.stream(v.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
