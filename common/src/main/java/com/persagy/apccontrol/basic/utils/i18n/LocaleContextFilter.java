package com.persagy.apccontrol.basic.utils.i18n;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.framework.common.i18n.I18nProperties;
import org.apache.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

public class LocaleContextFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 获取语言环境
        String lang = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (!StringUtils.hasLength(lang)) {
            //没有传参则处理成中文
//            LocaleContext.setLocaleContext(Locale.CHINESE);
            // 改为paas环境配置
            String i18n = SpringUtil.getBean(I18nProperties.class).getI18n();
            LocaleContext.setLocaleContext(Locale.forLanguageTag(i18n));
        }else {
            // 存入上下文
            LocaleContext.setLocaleContext(request.getLocale());
            LocaleContext.setAcceptLanguageContext(lang);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            LocaleContext.remove();
        }
    }
}
