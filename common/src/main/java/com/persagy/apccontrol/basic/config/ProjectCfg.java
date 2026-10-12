package com.persagy.apccontrol.basic.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

/**
 * 本地项目基础项目信息
 *
 * @author yuyao
 */
@Primary
@Data
@Configuration
public class ProjectCfg {

    /**
     * 集团编码
     */
    @Value("${project.groupcode: }")
    private String groupCode;
    /**
     * 项目id
     */
    @Value("${project.id: }")
    private String projectId;
    /**
     * 项目名称
     */
    @Value("${project.projectName: }")
    private String projectName;

    /**
     * 本地服务IP地址
     */
    private String localityIp;
    /**
     * 本地服务端口
     */
    private String localityPort;
}
