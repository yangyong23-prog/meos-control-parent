package com.persagy.apccontrol;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.cloud.config.OpenFeignAutoConfiguration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

/**
 * @author yuyao
 */
@SpringBootApplication(exclude = {
        OpenFeignAutoConfiguration.class
})
@Import(SpringUtil.class)
@ServletComponentScan
@EnableCaching
@EnableRetry
@EnableAsync
@EnableFeignClients({
        "com.persagy.apccontrol.basic.outside.feign.client",
        "com.persagy.apccontrol.common.outside.feign",
        "com.persagy.apccontrol.common.outside"
})
@ConfigurationPropertiesScan("com.persagy.apccontrol.basic.config.properties")
@Slf4j
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class ApcControlServerEdgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApcControlServerEdgeApplication.class, args);
        printSwagger();
    }

    @SneakyThrows
    private static void printSwagger() {
        Environment env = SpringUtil.getApplicationContext().getEnvironment();
        log.info("-------------------------------------------------- Swagger --------------------------------------------------");
        // noinspection HttpUrlsUsage
        log.info("API文档地址: http://{}:{}{}{}/doc.html",
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path", ""),
                env.getProperty("spring.mvc.servlet.path", ""));
        log.info("-------------------------------------------------- Swagger --------------------------------------------------");
    }

}
