package com.persagy.apccontrol.basic.opt;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 边缘调用优化（opt/edge-call 分支）的统一开关。
 *
 * <p>所有开关都在 Nacos 里以 {@code switch.opt.<名字>} 配置，默认关闭；关闭时行为与改造前完全一致。
 * 直接读 {@link Environment}：Nacos 刷新后环境里的属性立即生效，不依赖 {@code @RefreshScope} 代理，
 * 因此在线程池拒绝策略、静态工具等非 Spring 管理的位置也能用 {@link #on(String)} 读取。
 *
 * <p>开关清单见 docs/opt/README.md。
 */
@Component
public class OptSwitch {

    public static final String PREFIX = "switch.opt.";

    /** B0：async 池满时丢弃并计数，替代 CallerRuns 压回调用线程 */
    public static final String ASYNC_DISCARD_ON_FULL = "asyncDiscardOnFull";
    /** B0：本地度量（每分钟打一行日志） */
    public static final String METRICS = "metrics";

    /** B1：MQTT 消费只解析一次 payload、"~" 日志降级限频、ContextUtil 在 finally 清理、收包计数 */
    public static final String MQTT_ISOLATION = "mqttIsolation";
    /** B1：运行日志上云改为本地有界队列 + 后台合批发送 */
    public static final String RUNNING_LOG_QUEUE = "runningLogQueue";
    /** B1：自动日志建档按 traceId 进程内去重（10 分钟） */
    public static final String AUTO_LOG_DEDUPE = "autoLogDedupe";
    /** B1：修 16.9，同一控制指令“完成”帧先到后，丢弃后到的“进行中”帧 */
    public static final String CTRL_FRAME_ORDER_FIX = "ctrlFrameOrderFix";

    private static volatile Environment environment;

    public OptSwitch(Environment environment) {
        OptSwitch.environment = environment;
    }

    /**
     * 布尔开关，未配置或 Spring 尚未启动时返回 false。
     */
    public static boolean on(String name) {
        Environment env = environment;
        if (env == null) {
            return false;
        }
        return Boolean.parseBoolean(env.getProperty(PREFIX + name, "false").trim());
    }

    /**
     * 字符串开关（如三态 off / verify / on），未配置时返回 defaultValue。
     */
    public static String value(String name, String defaultValue) {
        Environment env = environment;
        if (env == null) {
            return defaultValue;
        }
        String v = env.getProperty(PREFIX + name);
        return v == null ? defaultValue : v.trim();
    }

    /**
     * 数值参数，未配置或格式错误时返回 defaultValue。
     */
    public static long number(String name, long defaultValue) {
        String v = value(name, null);
        if (v == null || v.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** 仅供单测注入 */
    static void setEnvironmentForTest(Environment env) {
        environment = env;
    }
}
