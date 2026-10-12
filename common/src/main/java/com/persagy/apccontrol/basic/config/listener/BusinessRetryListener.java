package com.persagy.apccontrol.basic.config.listener;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

import java.util.Arrays;
import java.util.List;

/**
 * 业务重试监听器
 *
 * @author JiYinchuan
 * @since 7.250901.0
 */
@AllArgsConstructor
public class BusinessRetryListener implements RetryListener, LogComponent {

    /**
     * 日志标签
     */
    private final String logTag;

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        logProjectInstance().trace("监听到重试流程", Pair.of("Tag", this.logTag));
        return true;
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        boolean isExecuteFinish = BooleanUtils.isNotTrue((Boolean) context.getAttribute(RetryContext.EXHAUSTED));
        List<Pair<String, Object>> logMessages = Arrays.asList(
                Pair.of("Tag", this.logTag),
                Pair.of("当前重试次数", context.getRetryCount())
        );
        if (isExecuteFinish) {
            logProjectInstance().trace("重试执行完毕", throwable, logMessages);
        } else {
            logProjectInstance().error("重试执行失败", throwable, logMessages);
        }
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        logProjectInstance().warn("重试执行失败", throwable, Arrays.asList(
                Pair.of("Tag", this.logTag),
                Pair.of("即将进行重试次数", context.getRetryCount())
        ));
    }

    @Override
    public String logTag() {
        return "重试监听器";
    }
}
