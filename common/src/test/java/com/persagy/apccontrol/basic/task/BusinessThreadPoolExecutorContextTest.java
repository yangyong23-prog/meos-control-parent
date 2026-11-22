package com.persagy.apccontrol.basic.task;

import com.persagy.framework.common.context.ContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link BusinessThreadPoolExecutor} 异步任务租户上下文传递测试。
 *
 * <p>缓存热刷新等任务在 ttl 线程池里执行，租户丢失会让 SQL 落到全局库。</p>
 */
class BusinessThreadPoolExecutorContextTest {

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    void asyncTaskSeesSubmitterTenant() throws Exception {
        ContextUtil.setTenant("TYXN");
        ContextUtil.setGroupCode("TYXN");
        ContextUtil.setProjectId("Pj9909990007");
        AtomicReference<String> tenant = new AtomicReference<>();
        AtomicReference<String> groupCode = new AtomicReference<>();
        AtomicReference<String> projectId = new AtomicReference<>();

        BusinessThreadPoolExecutor.submitCommon(() -> {
            tenant.set(ContextUtil.getTenant());
            groupCode.set(ContextUtil.getGroupCode());
            projectId.set(ContextUtil.getProjectId());
            return null;
        }).get(10, TimeUnit.SECONDS);

        Assertions.assertEquals("TYXN", tenant.get());
        Assertions.assertEquals("TYXN", groupCode.get());
        Assertions.assertEquals("Pj9909990007", projectId.get());
    }

    @Test
    void callerRunKeepsCallerContext() {
        ContextUtil.setTenant("TYXN");
        ContextUtil.setGroupCode("TYXN");
        ContextUtil.setProjectId("Pj9909990007");
        Runnable decorated = BusinessThreadPoolExecutor.decorateWithContext(() -> {
            Assertions.assertEquals("TYXN", ContextUtil.getTenant());
        });

        // CallerRunsPolicy 场景：任务直接在提交线程执行，结束后调用方上下文必须还在
        decorated.run();

        Assertions.assertEquals("TYXN", ContextUtil.getTenant());
        Assertions.assertEquals("TYXN", ContextUtil.getGroupCode());
        Assertions.assertEquals("Pj9909990007", ContextUtil.getProjectId());
    }
}
