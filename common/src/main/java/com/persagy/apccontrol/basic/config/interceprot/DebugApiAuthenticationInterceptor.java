package com.persagy.apccontrol.basic.config.interceprot;

import cn.hutool.core.date.DatePattern;
import com.persagy.apccontrol.basic.config.properties.ProjectProperties;
import com.persagy.apccontrol.basic.config.properties.debug.DebugInterceptorProperties;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 调试请求鉴权拦截器
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
public class DebugApiAuthenticationInterceptor implements HandlerInterceptor {

    /**
     * 项目配置文件
     */
    private final ProjectProperties projectProperties;

    /**
     * 构造器
     *
     * @param projectProperties 项目配置文件
     * @since 5.241201.0
     */
    public DebugApiAuthenticationInterceptor(ProjectProperties projectProperties) {
        this.projectProperties = projectProperties;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        DebugInterceptorProperties debugInterceptorProperties = projectProperties.getDebugInterceptor();
        String headerName = debugInterceptorProperties.getHeaderName();
        String encryptionSalt = debugInterceptorProperties.getEncryptionSalt();
        String authorization = request.getHeader(headerName);
        if (StringUtils.isBlank(authorization)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return false;
        }
        String dateString = LocalDate.now().format(DatePattern.PURE_DATE_FORMATTER);
        String md5Hex = DigestUtils.md5Hex(String.join("_", dateString, encryptionSalt));
        if (!StringUtils.equalsIgnoreCase(authorization, md5Hex)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        return true;
    }
}
