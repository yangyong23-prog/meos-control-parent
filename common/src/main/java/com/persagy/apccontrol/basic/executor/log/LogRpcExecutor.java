package com.persagy.apccontrol.basic.executor.log;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * RPC日志执行器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public class LogRpcExecutor extends BaseLogExecutor implements LogProvider {

    /**
     * RPC参数
     */
    private final Object param;

    /**
     * RPC响应
     */
    private final Object response;

    /**
     * 构造器
     *
     * @param param    RPC参数
     * @param response RPC响应
     * @since v5.1.0
     */
    public LogRpcExecutor(Object param, Object response) {
        super(null, null);
        this.param = param;
        this.response = response;
    }

    /**
     * 构造器
     *
     * @param groupCode 集团编码
     * @param param     RPC参数
     * @param response  RPC响应
     * @since v5.1.0
     */
    public LogRpcExecutor(String groupCode, Object param, Object response) {
        super(groupCode, null);
        this.param = param;
        this.response = response;
    }

    /**
     * 构造器
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param param     RPC参数
     * @param response  RPC响应
     * @since 5.240402.0
     */
    public LogRpcExecutor(String groupCode, String projectId, Object param, Object response) {
        super(groupCode, projectId);
        this.param = param;
        this.response = response;
    }

    @Override
    public String log(
            @NonNull Logger logger, @NonNull Level level, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable List<Pair<String, Object>> additionalMessages) {
        ArgArray argArray = ArgArray.builder();
        argArray.originalMessages(additionalMessages);
        argArray.additionalMessages(Pair.of("param", param), Pair.of("response", response));
        return super.log(logger, level, logTag, logDetailTag, throwable, argArray.parse());
    }
}
