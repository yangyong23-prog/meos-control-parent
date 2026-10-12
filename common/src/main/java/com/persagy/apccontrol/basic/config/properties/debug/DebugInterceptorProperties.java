package com.persagy.apccontrol.basic.config.properties.debug;

import com.persagy.apccontrol.basic.config.properties.ProjectProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 调试拦截器配置文件
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
@ConfigurationProperties(prefix = DebugInterceptorProperties.KEY)
@RefreshScope
public class DebugInterceptorProperties {

    public static final String KEY = ProjectProperties.KEY + ".debug-interceptor";

    /**
     * 是否开启拦截
     */
    private boolean enabled = true;

    /**
     * 请求头名称
     */
    private String headerName = "debugToken";

    /**
     * 加密盐
     */
    private String encryptionSalt = "persagy666";

}
