package com.persagy.apccontrol.basic.config.cache;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.persagy.apccontrol.basic.config.dao.partition.AbstractPartitionTableStrategy;
import com.persagy.apccontrol.basic.structure.performance.StopWatchContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Supplier;

/**
 * 当前线程唯一变量储存
 *
 * <p><a href="https://github.com/alibaba/p3c">摘选自Java开发手册(黄山版7.6)</a>: 必须回收自定义的 [ThreadLocal] 变量,
 * 尤其在线程池场景下, 线程经常会被复用, 如果不清理自定义的 [ThreadLocal] 变量, 可能会影响后续业务逻辑和造成内存泄露等问题.
 * 尽量在代理中使用 [try-finally] 块进行回收.
 * 正例:
 * <pre>{@code
 *     objectThreadLocal.set(userInfo);
 *     try {
 *         // ...
 *     } finally {
 *         objectThreadLocal.remove();
 *     }
 * }</pre>
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public class LocalContextStorage {

    /**
     * 分表Key
     */
    public static final TransmittableThreadLocal<Map<String, AbstractPartitionTableStrategy>> PARTITION_KEY = new TransmittableThreadLocal<>();

    /**
     * 性能计时器
     */
    private static final TransmittableThreadLocal<StopWatchContext> STOP_WATCH = new TransmittableThreadLocal<>();

    /**
     * 老版本标识
     */
    private static final TransmittableThreadLocal<Boolean> DEPRECATED_VERSION = new TransmittableThreadLocal<>();

    /**
     * 是否并行执行
     */
    private static final TransmittableThreadLocal<Boolean> PARALLEL_EXECUTE = new TransmittableThreadLocal<>();

    /**
     * 当前请求
     */
    private static final TransmittableThreadLocal<HttpServletRequest> REQUEST = new TransmittableThreadLocal<>();

    /**
     * 请求调试
     */
    private static final TransmittableThreadLocal<Level> REQUEST_DEBUG = new TransmittableThreadLocal<>();
    public static final TransmittableThreadLocal<List<Pair<String, Object>>> REQUEST_DEBUG_DETAIL = new TransmittableThreadLocal<>();

    // ------------------------- 性能计时器 -------------------------

    /**
     * 获取计时器
     *
     * @param id 计时器ID
     * @return 计时器
     * @since 7.250302.0
     */
    public static StopWatchContext stopWatch(@NonNull String id) {
        StopWatchContext stopWatch = Optional.ofNullable(STOP_WATCH.get()).orElseGet(() -> StopWatchContext.create(id));
        STOP_WATCH.set(stopWatch);
        return stopWatch;
    }

    public static void request(@NonNull HttpServletRequest request) {
        REQUEST.set(request);
    }

    public static HttpServletRequest request() {
        return REQUEST.get();
    }

    /**
     * 清除计时器
     *
     * @since 7.250302.0
     */
    public static void clearStopWatch() {
        STOP_WATCH.remove();
    }

    public static void clearRequest() {
        REQUEST.remove();
    }

    // ------------------------- 去除默认分精度类型标识 -------------------------

    /**
     * 是否去除默认分精度类型（供 SSE 推送线程等无 HTTP 请求上下文的场景手动设置）
     */
    private static final TransmittableThreadLocal<Boolean> REMOVE_DEFAULT_IP_TYPE = new TransmittableThreadLocal<>();

    /**
     * 判断是否去除默认分精度类型
     * <p>HTTP 线程优先读请求头 {@code removeDefaultIpType}；无请求头时回退读取手动设置的标识（SSE 推送线程）</p>
     *
     * @return true 表示需要去除默认分精度
     * @since v7.260901.0
     */
    public static boolean isRemoveDefaultIpType() {
        HttpServletRequest request = REQUEST.get();
        if (request != null) {
            return "true".equals(request.getHeader("removeDefaultIpType"));
        }
        return BooleanUtils.isTrue(REMOVE_DEFAULT_IP_TYPE.get());
    }

    /**
     * 设置去除默认分精度类型标识（供 SSE 推送线程在查询前手动设置）
     *
     * @param removeDefaultIpType 是否去除
     * @since v7.260901.0
     */
    public static void setRemoveDefaultIpType(boolean removeDefaultIpType) {
        REMOVE_DEFAULT_IP_TYPE.set(removeDefaultIpType);
    }

    /**
     * 清除去除默认分精度类型标识（供 SSE 推送线程在查询后清理）
     *
     * @since v7.260901.0
     */
    public static void clearRemoveDefaultIpType() {
        REMOVE_DEFAULT_IP_TYPE.remove();
    }

    // ------------------------- 老版本标识 -------------------------

    /**
     * 是否为老版本
     *
     * @since 7.250302.0
     */
    public static boolean isDeprecatedVersion() {
        return BooleanUtils.isTrue(DEPRECATED_VERSION.get());
    }

    /**
     * 设置老版本
     *
     * @param supplier 执行内容
     * @since 7.250302.0
     */
    public static <T> T deprecatedVersion(@NonNull Supplier<T> supplier) {
        T result;
        try {
            DEPRECATED_VERSION.set(true);
            result = supplier.get();
        } finally {
            DEPRECATED_VERSION.remove();
        }
        return result;
    }

    // ------------------------- 并行执行 -------------------------

    /**
     * 是否并行执行
     *
     * @since 7.250201.0
     */
    public static boolean isParallelExecute() {
        return BooleanUtils.isTrue(PARALLEL_EXECUTE.get());
    }

    /**
     * 设置并行执行
     *
     * @since 7.250201.0
     */
    public static void parallelExecute() {
        PARALLEL_EXECUTE.set(true);
    }

    /**
     * 清除并行执行
     *
     * @since 7.250201.0
     */
    public static void clearParallelExecute() {
        PARALLEL_EXECUTE.remove();
    }

    // ------------------------- 请求调试 -------------------------

    /**
     * 是否存在请求调试
     *
     * @return 是否存在请求调试
     * @since 5.241001.0
     */
    public static boolean containsRequestDebug() {
        return ObjectUtils.anyNotNull(REQUEST_DEBUG.get(), REQUEST_DEBUG_DETAIL.get());
    }

    /**
     * 是否支持调试日志打印
     *
     * @param level 调试日志级别
     * @return 是否支持调试日志打印
     * @since 5.241201.0
     */
    public static boolean isRequestDebug(@NonNull Level level) {
        Level requestDebugLevel = requestDebugLevel();
        return Objects.nonNull(requestDebugLevel) && requestDebugLevel.toInt() <= level.toInt();
    }

    /**
     * 请求调试日志等级
     *
     * @return 请求调试日志等级
     * @since 7.250102.0
     */
    @Nullable
    public static Level requestDebugLevel() {
        return REQUEST_DEBUG.get();
    }

    /**
     * 设置请求调试级别
     *
     * @param level 调试日志级别
     * @since 5.241001.0
     */
    public static void requestDebugLevel(@NonNull Level level) {
        REQUEST_DEBUG.set(level);
    }

    /**
     * 获取请求调试内容
     *
     * @return 请求调试内容
     * @since 5.241001.0
     */
    @Nullable
    public static List<Pair<String, Object>> getRequestDebug() {
        return REQUEST_DEBUG_DETAIL.get();
    }

    /**
     * 添加请求调试内容
     *
     * @param logTag       日志标签
     * @param logDetailTag 日志详细标签
     * @param message      消息
     * @since 5.241001.0
     */
    public static void putRequestDebug(String logTag, String logDetailTag, String message) {
        List<Pair<String, Object>> requestDebugDetail = REQUEST_DEBUG_DETAIL.get();
        requestDebugDetail.add(Pair.of(String.format("[%s] %s", logTag, logDetailTag), message));
        REQUEST_DEBUG_DETAIL.set(requestDebugDetail);
    }

    /**
     * 清空请求调试内容
     *
     * @since 5.241001.0
     */
    public static void clearRequestDebug() {
        if (containsRequestDebug()) {
            REQUEST_DEBUG.remove();
            REQUEST_DEBUG_DETAIL.remove();
        }
    }
}
