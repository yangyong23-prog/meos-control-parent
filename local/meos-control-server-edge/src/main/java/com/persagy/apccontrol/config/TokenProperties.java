package com.persagy.apccontrol.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 本地项目基础项目信息
 *
 * @author yuyao
 */
@Primary
@Data
@ConfigurationProperties(
        prefix = "paas.local.token"
)
@Configuration
public class TokenProperties {

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * saas token服务访问地址
     */
    private String saasTokenUrl;

    /**
     * token过期时间，单位秒
     */
    private Long tokenExpiration;

}
