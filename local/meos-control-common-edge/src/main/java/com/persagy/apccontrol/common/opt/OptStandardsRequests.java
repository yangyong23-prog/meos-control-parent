package com.persagy.apccontrol.common.opt;

import com.persagy.framework.common.context.ContextUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * 运行标准过渡：拦截器共用的请求解析（应用内路径、项目 ID、调用方类型）。
 */
public final class OptStandardsRequests {

    private OptStandardsRequests() {
    }

    /** 去掉 context-path 后的路径 */
    public static String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (uri == null) {
            return "";
        }
        return ctx != null && !ctx.isEmpty() && uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
    }

    /** 依次取框架上下文、请求头、请求参数中的 projectId；都没有返回 null */
    public static String projectId(HttpServletRequest request) {
        String p = null;
        try {
            p = ContextUtil.getProjectId();
        } catch (RuntimeException ignored) {
            // 框架上下文未初始化时按请求头 / 参数取
            p = null;
        }
        if (isBlank(p)) {
            p = request.getHeader("projectId");
        }
        if (isBlank(p)) {
            p = request.getParameter("projectId");
        }
        return isBlank(p) ? null : p.trim();
    }

    /** 浏览器（经前端页面）还是服务间调用（Feign 等），用于区分 dtp-meos 的调用和页面自身的调用 */
    public static String caller(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null && ua.contains("Mozilla") ? "browser" : "service";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
