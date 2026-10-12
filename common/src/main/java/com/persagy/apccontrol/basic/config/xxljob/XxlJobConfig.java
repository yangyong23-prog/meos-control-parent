/**
 *
 */
package com.persagy.apccontrol.basic.config.xxljob;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author gaoyu
 */

@Slf4j
@Configuration
@RefreshScope
@Data
public class XxlJobConfig {
    @Value("${xxl.job.admin.addresses:''}")
    private String adminAddresses;
    @Value("${xxl.job.admin.username:''}")
    private String username;
    @Value("${xxl.job.admin.password:''}")
    private String password;
    @Value("${xxl.job.executor.appname:''}")
    private String appname;
    @Value("${xxl.job.executor.title:''}")
    private String title;

    @Value("${xxl.job.executor.port:8080}")
    private int port;

    @Value("${xxl.job.executor.logpath:''}")
    private String logPath;
    @Value("${xxl.job.accessToken:''}")
    private String accessToken;

    @Value("${xxl.job.executor.logretentiondays:15}")
    private int logRetentionDays;


}
