package com.persagy.apccontrol.basic.outside.feign.decoder;

import com.persagy.framework.common.context.ContextUtil;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;


public class SystemGraphFeignConfig {

    private static final String HEADER_PROJECT_ID = "projectId";
    private static final String HEADER_GROUP_CODE = "groupCode";
    private static final String HEADER_APP_ID = "appId";
    private static final String HEADER_USER_ID = "userId";



    @Bean
    public RequestInterceptor feignInterceptor() {
        return template -> {
            template.query(HEADER_GROUP_CODE, ContextUtil.getGroupCode());
            template.query(HEADER_PROJECT_ID, ContextUtil.getProjectId());
            template.query(HEADER_APP_ID, "meos");
            template.query(HEADER_USER_ID, "systemId");
        };
    }
}