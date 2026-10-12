package com.persagy.apccontrol.basic.outside.feign.interceptor;

import com.persagy.apccontrol.basic.config.FeignConfiguration;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.utils.feign.FeignUtils;
import com.persagy.cloud.interceptor.FeignAddHeaderRequestInterceptor;
import com.persagy.framework.common.context.ContextConstants;
import feign.RequestTemplate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * 请求头拦截器
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public class RequestCustomHeaderInterceptor extends FeignAddHeaderRequestInterceptor implements LogComponent {

    private static final String APP_ID = "meos";

    @Override
    public void apply(RequestTemplate template) {
        super.apply(template);
        Map<String, Collection<String>> headers = template.headers();
        Collection<String> groupCodes = headers.get(ContextConstants.GROUP_CODE);
        Collection<String> projectIds = headers.get(ContextConstants.PROJECT_ID);

        template.header(ContextConstants.HEADER_GROUP_CODE, groupCodes);
        template.header(ContextConstants.HEADER_PROJECT_ID, projectIds);
        template.header(FeignConfiguration.HEADER_REQUEST_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        template.query(FeignConfiguration.HEADER_APP_ID, APP_ID);
        if (log().isTraceEnabled()) {
            logProjectInstance().trace("开始请求", Arrays.asList(
                    Pair.of("url", template.feignTarget().url() + template.url()),
                    Pair.of("headers", template.headers()),
                    Pair.of("body", FeignUtils.parseRequestBody(template.request()))
            ));
        }
    }

    @Override
    public String logTag() {
        return "RPC Interceptor";
    }
}
