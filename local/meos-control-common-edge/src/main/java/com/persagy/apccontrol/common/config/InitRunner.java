package com.persagy.apccontrol.common.config;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * @author yuyao
 * @Date 2022/3/2 8:00 PM
 * @Description 服务启动需要初始化的逻辑
 */
@Component
@Order(1)
@Slf4j
public class InitRunner implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProjectCfg projectCfg;

    @Override
    public void run(String... args) {
        log.info("项目启动--数据初始化执行！");
        Map<String, SystemInitService> beansOfType = applicationContext.getBeansOfType(SystemInitService.class);
        // 使用并行流 + 自定义线程池 并发执行 init()
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        beansOfType.forEach((key, initService) -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // 启动时没有请求上下文，Feign 拦截器与 DtpRwdCommonParam.instance(..) 取到的 groupCode 为空，
                    // dtp-rwd 会返回 A0400。边缘端是单项目部署，这里用配置的项目身份补齐上下文。
                    this.initProjectContext();
                    initService.init();
                } catch (Exception e) {
                    log.error("项目启动初始化{}失败{}", initService.getName(), e.getMessage());
                } finally {
                    // 线程池线程会复用，必须清理上下文，避免污染后续任务
                    ContextUtil.unloadContext();
                }
            }, BusinessThreadPoolExecutor.ttlExecutor());  // 使用自定义线程池
            futures.add(future);
        });
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("项目启动--数据初始化结束！");
    }

    /**
     * 用配置的 project.groupcode / project.id 补齐当前线程的项目上下文（只补空值）
     */
    private void initProjectContext() {
        String groupCode = projectCfg.getGroupCode();
        String projectId = projectCfg.getProjectId();
        if (StringUtils.isBlank(groupCode) || StringUtils.isBlank(projectId)) {
            log.warn("项目启动初始化未配置 project.groupcode 或 project.id，groupCode={}，projectId={}", groupCode, projectId);
        }
        if (StringUtils.isBlank(ContextUtil.getGroupCode()) && StringUtils.isNotBlank(groupCode)) {
            ContextUtil.setGroupCode(groupCode.trim());
            ContextUtil.setTenant(groupCode.trim());
        }
        if (StringUtils.isBlank(ContextUtil.getProjectId()) && StringUtils.isNotBlank(projectId)) {
            ContextUtil.setProjectId(projectId.trim());
        }
    }

}