package com.persagy.apccontrol.common.config;

import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void run(String... args) {
        log.info("项目启动--数据初始化执行！");
        Map<String, SystemInitService> beansOfType = applicationContext.getBeansOfType(SystemInitService.class);
        // 使用并行流 + 自定义线程池 并发执行 init()
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        beansOfType.forEach((key, initService) -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    initService.init();
                } catch (Exception e) {
                    log.error("项目启动初始化{}失败{}", initService.getName(), e.getMessage());
                }
            }, BusinessThreadPoolExecutor.ttlExecutor());  // 使用自定义线程池
            futures.add(future);
        });
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("项目启动--数据初始化结束！");
    }

}