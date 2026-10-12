package com.persagy.apccontrol.interceptor;

import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * token过滤器
 *
 * @author muanfu
 * @date 2024年7月15日19:58:09
 */
@Slf4j
@Component
public class WebInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handgoler) throws Exception {

        String requestURI = request.getRequestURI();
        if (requestURI.contains("/graph/icon/") || requestURI.contains("/graph/legend/")
                || requestURI.contains("/common/config/save") || requestURI.contains("/common/config/get")) {
            ContextUtil.setTenant(null);
            ContextUtil.setGroupCode(null);
            return true;
        }

        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
