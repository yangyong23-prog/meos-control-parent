package com.persagy.apccontrol.basic.outside.feign.interceptor;

import com.persagy.framework.common.context.ContextConstants;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * 请求Token拦截器
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public class RequestTokenInterceptor implements feign.RequestInterceptor {

    /**
     * Token
     */
    private final String token;

    /**
     * 构造器
     *
     * @param token token
     * @since v5.8.0
     */
    public RequestTokenInterceptor(String token) {
        this.token = token;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (StringUtils.isNotBlank(token)) {
            Map<String, Collection<String>> headers = template.headers();
            if (!headers.containsKey(ContextConstants.BEARER_HEADER_KEY)) {
                template.header(ContextConstants.BEARER_HEADER_KEY, token);
            }
        }
    }
}
